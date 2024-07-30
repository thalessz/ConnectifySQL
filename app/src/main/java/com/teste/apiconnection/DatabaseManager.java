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
    private static  String API_URL = "http://192.168.0.165:5000/mysql/query"; // URL padrão
    private final List<String> results; // Armazenar valores
    private final TextView txtResult; // Referência ao TextView para exibir resultados

    public DatabaseManager(TextView txtResult) {
        this.txtResult = txtResult;
        this.results = new ArrayList<>();
    }

    public static void setApiUrl(String url) {
        DatabaseManager.API_URL = url;
    }

    public void execute(String query) {
        String jsonPayload = "{\"query\": \"" + query + "\"}";
        new QueryExecutorTask(this).execute(jsonPayload);
    }

    public List<String> fetchAllValues() {
        return results;
    }

    private void addResult(String value) {
        results.add(value);
    }

    private static class QueryExecutorTask extends AsyncTask<String, Void, Boolean> {
        private final DatabaseManager dbManager;

        QueryExecutorTask(DatabaseManager dbManager) {
            this.dbManager = dbManager;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JsonObject result = queryExecute(params[0]);
                processQueryResult(result, dbManager);
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
            }
        }

        private static JsonObject queryExecute(String jsonPayload) throws IOException {
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL(API_URL);
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

        private static void processQueryResult(JsonObject result, DatabaseManager dbManager) {
            if (result != null) {
                Log.d("API_RESULT", result.toString()); // Log da resposta da API
                if (result.has("status") && "success".equals(result.get("status").getAsString())) {
                    if (result.has("data")) {
                        JsonElement dataElement = result.get("data");
                        if (dataElement.isJsonArray()) {
                            JsonArray jsonData = dataElement.getAsJsonArray();
                            for (JsonElement element : jsonData) {
                                if (element.isJsonObject()) {
                                    JsonObject obj = element.getAsJsonObject();
                                    for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                                        String value = entry.getValue().getAsString();
                                        dbManager.addResult(value);
                                    }
                                }
                            }
                        } else if (dataElement.isJsonObject()) {
                            JsonObject obj = dataElement.getAsJsonObject();
                            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                                String value = entry.getValue().getAsString();
                                dbManager.addResult(value);
                            }
                        }
                    }
                }
            }
        }
    }

    public interface QueryCallback {
        void onQueryResult(List<String> result); // Retorna uma lista de valores
        void onInsertResult(boolean success);
    }
}