package com.teste.apiconnection;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MainActivity extends AppCompatActivity {

    private TextView txtResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtResult = findViewById(R.id.txtResult);

        String url = "http://192.168.0.76:5000/mysql/query";
        String jsonPayload = "{\"query\": \"SELECT NOME FROM PESSOA LIMIT 1\"}";

        new HttpPostRequest().execute(url, jsonPayload);
    }

    private class HttpPostRequest extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            String jsonPayload = params[1];
            return HttpHandler.postRequest(url, jsonPayload);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                Log.d("Resposta da API", result);
                JsonObject jsonResponse = JsonParser.parseString(result).getAsJsonObject();
                String status = jsonResponse.get("status").getAsString();
                if ("success".equals(status)) {
                    String nome = jsonResponse.getAsJsonArray("data").get(0).getAsJsonObject().get("NOME").getAsString();
                    txtResult.setText(nome);
                } else {
                    Toast.makeText(MainActivity.this, "Erro: " + jsonResponse.get("message").getAsString(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Falha ao obter dados da API", Toast.LENGTH_SHORT).show();
            }
        }
    }
}