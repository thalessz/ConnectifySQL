package com.teste.apiconnection;

import android.os.AsyncTask;
import android.util.Log;

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

/**
 * Classe DatabaseManager
 *
 * Esta classe gerencia operações de banco de dados via API.
 */
public class DatabaseManager {
    private static String apiUrl = "http://192.168.0.165:5000/mysql/query"; // URL padrão
    private List<JsonObject> results;

    public DatabaseManager() {
        this.results = new ArrayList<>();
    }

    public static void setApiUrl(String url) {
        apiUrl = url;
    }

    public void execute(String query, QueryCallback callback) {
        String jsonPayload = "{\"query\": \"" + query + "\"}";
        new QueryExecutorTask(this, callback).execute(jsonPayload);
    }

    public List<JsonObject> fetchAll() {
        return results;
    }

    private void setResults(List<JsonObject> results) {
        this.results = results;
    }

    private static class QueryExecutorTask extends AsyncTask<String, Void, Boolean> {
        private DatabaseManager dbManager;
        private QueryCallback callback;

        QueryExecutorTask(DatabaseManager dbManager, QueryCallback callback) {
            this.dbManager = dbManager;
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JsonObject result = queryExecute(params[0]);
                List<JsonObject> data = processQueryResult(result);
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
                callback.onQueryResult(dbManager.fetchAll());
            } else {
                Log.e("QUERY_RESULT", "Erro ao executar a consulta.");
                callback.onInsertResult(false);
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

        private static List<JsonObject> processQueryResult(JsonObject result) {
            List<JsonObject> data = new ArrayList<>();

            if (result != null) {
                if (result.has("status") && "success".equals(result.get("status").getAsString())) {
                    if (result.has("data")) {
                        JsonElement dataElement = result.get("data");
                        if (dataElement.isJsonArray()) {
                            JsonArray jsonData = dataElement.getAsJsonArray();
                            for (JsonElement element : jsonData) {
                                if (element.isJsonObject()) {
                                    data.add(element.getAsJsonObject());
                                } else {
                                    Log.w("RESULT_PROCESSING", "Elemento não é um objeto JSON: " + element.toString());
                                }
                            }
                        } else if (dataElement.isJsonObject()) {
                            data.add(dataElement.getAsJsonObject());
                        } else {
                            Log.w("RESULT_PROCESSING", "O campo 'data' não é um array ou objeto JSON: " + dataElement.toString());
                        }
                    } else {
                        Log.e("RESULT_PROCESSING", "Campo 'data' não encontrado no resultado JSON");
                    }
                } else {
                    Log.e("RESULT_PROCESSING", "Status não é 'success' no resultado JSON");
                }
            } else {
                Log.e("RESULT_PROCESSING", "Resultado JSON é nulo");
            }

            return data;
        }
    }

    public interface QueryCallback {
        void onQueryResult(List<JsonObject> result);
        void onInsertResult(boolean success);
    }
}
