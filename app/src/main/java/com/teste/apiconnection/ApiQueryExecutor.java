package com.teste.apiconnection;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Classe ApiQueryExecutor
 *
 * Esta classe contém métodos para executar consultas em uma API e retornar os resultados.
 */
public class ApiQueryExecutor {

    /**
     * Executa uma consulta na API e retorna os resultados.
     *
     * @param jsonPayload O payload JSON contendo a consulta a ser executada.
     * @return Um objeto JsonObject contendo a resposta da API.
     */
    public static JsonObject queryExecute(String jsonPayload) {
        String strUrl = "http://192.168.0.76:5000/mysql/query"; // Altere para o seu URL
        HttpURLConnection urlConnection = null;

        try {
            // Configuração da conexão
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setDoOutput(true);

            // Enviar o payload JSON
            try (OutputStream os = urlConnection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Ler a resposta
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = bufferedReader.readLine()) != null) {
                response.append(responseLine.trim());
            }

            // Converter a resposta em um objeto JSON
            return JsonParser.parseString(response.toString()).getAsJsonObject();

        } catch (Exception e) {
            e.printStackTrace();
            return null; // Retorna nulo em caso de erro
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    /**
     * Classe QueryExecutorTask
     *
     * Esta classe executa consultas em uma API de forma assíncrona e retorna os resultados.
     */
    public static class QueryExecutorTask extends AsyncTask<String, Void, JsonObject> {

        private ApiQueryResultListener listener;

        /**
         * Construtor da classe QueryExecutorTask.
         *
         * @param listener A interface para receber os resultados da consulta.
         */
        public QueryExecutorTask(ApiQueryResultListener listener) {
            this.listener = listener;
        }

        @Override
        protected JsonObject doInBackground(String... params) {
            // params[0] é o payload JSON que contém a consulta
            return queryExecute(params[0]);
        }

        @Override
        protected void onPostExecute(JsonObject result) {
            super.onPostExecute(result);
            // Notifica o listener com o resultado da consulta
            if (listener != null) {
                listener.onQueryResult(result);
            }
        }
    }

    /**
     * Interface para receber resultados da consulta.
     */
    public interface ApiQueryResultListener {
        void onQueryResult(JsonObject result);
    }

    /**
     * Processa o resultado da consulta e retorna uma string formatada.
     *
     * @param result O resultado da consulta em formato JSON.
     * @return Uma string formatada com os resultados.
     */
    public static String processQueryResult(JsonObject result) {
        StringBuilder output = new StringBuilder();

        if (result != null && "success".equals(result.get("status").getAsString())) {
            JsonArray data = result.getAsJsonArray("data");
            for (int i = 0; i < data.size(); i++) {
                JsonObject row = data.get(i).getAsJsonObject();
                output.append(row.toString()).append("\n"); // Adiciona cada linha no formato JSON
            }
        } else {
            output.append("Erro ao obter dados da API");
            Log.e("ERRO", "Erro ao obter dados da API");
        }

        return output.toString(); // Retorna a string formatada
    }
}