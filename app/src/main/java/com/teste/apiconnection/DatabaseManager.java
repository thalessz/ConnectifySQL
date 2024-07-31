package com.teste.apiconnection;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Classe DatabaseManager
 *
 * Esta classe gerencia operações de banco de dados via API.
 * Ela executa consultas SQL e processa os resultados.
 */
public class DatabaseManager {
    private static String API_URL = "http://192.168.0.165:5000/mysql/query"; // URL padrão da API
    private final List<List<String>> results; // Armazenar resultados em forma de matriz
    private final TextView txtResult; // Referência ao TextView para exibir resultados
    private JsonObject lastResult; // Armazena a última resposta da API

    /**
     * Construtor da classe DatabaseManager.
     *
     * @param txtResult O TextView onde os resultados serão exibidos.
     */
    public DatabaseManager(TextView txtResult) {
        this.txtResult = txtResult;
        this.results = new ArrayList<>(); // Inicializa a matriz de resultados
    }

    /**
     * Define a URL da API.
     *
     * @param url A nova URL da API.
     */
    public static void setApiUrl(String url) {
        API_URL = url;
    }

    /**
     * Executa uma consulta SQL com parâmetros.
     *
     * @param query A consulta SQL a ser executada.
     * @param params Um dicionário de parâmetros a serem incluídos na consulta.
     */
    public void execute(String query, Map<String, String> params) {
        // Cria o payload JSON com a consulta
        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("query", query);

        // Adiciona os parâmetros, se houver
        if (params != null && !params.isEmpty()) {
            JsonObject paramsJson = new JsonObject();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                paramsJson.addProperty(entry.getKey(), entry.getValue()); // Adiciona cada parâmetro
            }
            jsonPayload.add("params", paramsJson);
        }

        // Log do JSON antes de enviar
        Log.d("API_PAYLOAD", jsonPayload.toString());

        // Executa a tarefa assíncrona com o payload JSON
        new QueryExecutorTask(this).execute(jsonPayload.toString());
    }

    /**
     * Retorna o número de resultados.
     *
     * @return O número de resultados.
     */
    public int getResultCount() {
        return results.size();
    }

    /**
     * Retorna o valor no índice especificado (linha, coluna).
     *
     * @param row O índice da linha.
     * @param col O índice da coluna.
     * @return O valor no índice especificado.
     */
    public String getValueAt(int row, int col) {
        if (row >= 0 && row < results.size() && col >= 0 && col < results.get(row).size()) {
            return results.get(row).get(col);
        }
        return null;
    }

    /**
     * Retorna todos os valores armazenados.
     *
     * @return Uma lista de listas com os valores retornados pela consulta.
     */
    public List<List<String>> fetchAll() {
        return new ArrayList<>(results); // Retorna uma cópia da matriz de resultados
    }

    /**
     * Armazena os resultados da consulta.
     *
     * @param result O objeto JSON com o resultado da consulta.
     */
    public void processQueryResult(JsonObject result) {
        if (result != null && result.has("data")) {
            JsonArray jsonData = result.getAsJsonArray("data");
            for (JsonElement element : jsonData) {
                if (element.isJsonObject()) {
                    JsonObject row = element.getAsJsonObject();
                    List<String> rowData = new ArrayList<>();
                    for (Map.Entry<String, JsonElement> entry : row.entrySet()) {
                        rowData.add(entry.getValue().getAsString()); // Adiciona cada valor da linha
                    }
                    results.add(rowData); // Adiciona a linha à matriz de resultados
                }
            }
        }
    }

    /**
     * Armazena a última resposta da API.
     *
     * @param result O objeto JSON com a resposta da API.
     */
    public void setLastResult(JsonObject result) {
        this.lastResult = result;
        processQueryResult(result); // Processa os resultados ao armazenar a última resposta
    }

    /**
     * Retorna a última resposta da API.
     *
     * @return O objeto JSON com a última resposta.
     */
    public JsonObject getLastResult() {
        return lastResult;
    }

    /**
     * Classe interna QueryExecutorTask.
     *
     * Esta classe é responsável por executar a consulta em segundo plano.
     */
    private static class QueryExecutorTask extends AsyncTask<String, Void, Boolean> {
        private final DatabaseManager dbManager; // Referência ao DatabaseManager

        /**
         * Construtor da classe QueryExecutorTask.
         *
         * @param dbManager O gerenciador de banco de dados.
         */
        QueryExecutorTask(DatabaseManager dbManager) {
            this.dbManager = dbManager;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JsonObject result = queryExecute(params[0]); // Executa a consulta
                dbManager.setLastResult(result); // Armazena a última resposta
                return true;
            } catch (Exception e) {
                Log.e("API_ERROR", "Erro ao executar a consulta: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Log.d("QUERY_RESULT", "Consulta realizada com sucesso.");
                // Chama o método da MainActivity para exibir os resultados
                if (dbManager.txtResult.getContext() instanceof MainActivity) {
                    MainActivity activity = (MainActivity) dbManager.txtResult.getContext();
                    activity.displayResults();
                }
            } else {
                Log.e("QUERY_RESULT", "Erro ao executar a consulta.");
                // Log para ver a resposta da API
                JsonObject result = dbManager.getLastResult();
                if (result != null) {
                    Log.e("API_RESPONSE", "Erro na resposta da API: " + result.toString());
                } else {
                    Log.e("API_RESPONSE", "Resposta da API é nula.");
                }
            }
        }

        /**
         * Executa a consulta na API e retorna o resultado.
         *
         * @param jsonPayload O payload JSON da consulta.
         * @return O objeto JSON com o resultado da consulta.
         * @throws IOException Se ocorrer um erro de entrada/saída.
         */
        private static JsonObject queryExecute(String jsonPayload) throws IOException {
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL(API_URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoOutput(true);

                // Envia o payload JSON
                try (OutputStream os = urlConnection.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Lê a resposta da API
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = bufferedReader.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                Log.d("API_RESPONSE", response.toString()); // Log da resposta da API

                return JsonParser.parseString(response.toString()).getAsJsonObject();

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }
    }

    /**
     * Interface QueryCallback.
     *
     * Esta interface é usada para retornar os resultados da consulta e inserção.
     */
    public interface QueryCallback {
        void onQueryResult(List<List<String>> result); // Retorna uma lista de listas de valores
        void onInsertResult(boolean success); // Indica se a inserção foi bem-sucedida
    }
}
