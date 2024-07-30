package com.teste.apiconnection;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
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
 */
public class DatabaseManager {
    private static String apiUrl = "http://192.168.0.165:5000/mysql/query"; // URL padrão
    private List<Pair<String, String>> results; // Armazenar pares chave-valor
    private TextView txtResult; // Referência ao TextView para exibir resultados

    public DatabaseManager(TextView txtResult) {
        this.txtResult = txtResult;
        this.results = new ArrayList<>();
    }

    public static void setApiUrl(String url) {
        apiUrl = url;
    }

    public void execute(String query) {
        String jsonPayload = "{\"query\": \"" + query + "\"}";
        new QueryExecutorTask(this).execute(jsonPayload);
    }

    public List<Pair<String, String>> fetchAll() {
        return results;
    }

    public String getValueAt(int index) {
        if (index >= 0 && index < results.size()) {
            return results.get(index).second; // Retorna o valor do par chave-valor
        }
        return null; // Retorna null se o índice estiver fora do intervalo
    }

    private void setResults(List<Pair<String, String>> results) {
        this.results = results;
    }

    private static class QueryExecutorTask extends AsyncTask<String, Void, Boolean> {
        private DatabaseManager dbManager;

        QueryExecutorTask(DatabaseManager dbManager) {
            this.dbManager = dbManager;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JsonObject result = queryExecute(params[0]);
                List<Pair<String, String>> data = processQueryResult(result);
                dbManager.setResults(data);
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
                // Aqui você pode chamar um método para exibir o resultado
                dbManager.displayResults(1); // Exibe o segundo valor (índice 1)
            } else {
                Log.e("QUERY_RESULT", "Erro ao executar a consulta.");
            }
        }

        private static JsonObject queryExecute(String jsonPayload) throws IOException {
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL(apiUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoOutput(true);

                try (OutputStream os = urlConnection.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

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

        private static List<Pair<String, String>> processQueryResult(JsonObject result) {
            List<Pair<String, String>> data = new ArrayList<>();

            if (result != null) {
                if (result.has("status") && "success".equals(result.get("status").getAsString())) {
                    if (result.has("data")) {
                        JsonElement dataElement = result.get("data");
                        if (dataElement.isJsonArray()) {
                            JsonArray jsonData = dataElement.getAsJsonArray();
                            for (JsonElement element : jsonData) {
                                if (element.isJsonObject()) {
                                    JsonObject obj = element.getAsJsonObject();
                                    for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                                        String key = entry.getKey();
                                        String value = entry.getValue().getAsString();
                                        data.add(new Pair<>(key, value));
                                    }
                                }
                            }
                        } else if (dataElement.isJsonObject()) {
                            JsonObject obj = dataElement.getAsJsonObject();
                            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                                String key = entry.getKey();
                                String value = entry.getValue().getAsString();
                                data.add(new Pair<>(key, value));
                            }
                        }
                    }
                }
            }
            return data;
        }

        private void displayResults(int index) {
            String output;
            if (index >= 0 && index < results.size()) {
                output = results.get(index).second; // Obtém o valor do índice especificado
            } else {
                output = "Valor não encontrado.";
            }
            txtResult.setText(output); // Exibe o valor no TextView
        }
    }

    public interface QueryCallback {
        void onQueryResult(List<Pair<String, String>> result); // Retorna uma lista de pares chave-valor
        void onInsertResult(boolean success);
    }
}
