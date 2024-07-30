package com.teste.apiconnection;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
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
    private List<JsonObject> results;

    public DatabaseManager() {
        this.results = new ArrayList<>();
    }

    public void execute(String query) {
        String jsonPayload = "{\"query\": \"" + query + "\"}";
        new QueryExecutorTask(this).execute(jsonPayload);
    }

    public List<JsonObject> fetchAll() {
        return results;
    }

    private void setResults(List<JsonObject> results) {
        this.results = results;
    }

    private static class QueryExecutorTask extends AsyncTask<String, Void, List<JsonObject>> {
        private DatabaseManager dbManager;

        QueryExecutorTask(DatabaseManager dbManager) {
            this.dbManager = dbManager;
        }

        @Override
        protected List<JsonObject> doInBackground(String... params) {
            JsonObject result = queryExecute(params[0]);
            return processQueryResult(result);
        }

        @Override
        protected void onPostExecute(List<JsonObject> result) {
            dbManager.setResults(result);
        }

        private static JsonObject queryExecute(String jsonPayload) {
            String strUrl = "http://192.168.0.165:5000/mysql/query"; // Altere para o seu URL
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL(strUrl);
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

            } catch (Exception e) {
                Log.e("API_ERROR", "Erro ao executar a consulta: " + e.getMessage());
                return null;
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
}
