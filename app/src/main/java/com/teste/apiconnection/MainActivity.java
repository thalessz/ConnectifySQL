package com.teste.apiconnection;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;

public class MainActivity extends AppCompatActivity implements ApiQueryExecutor.ApiQueryResultListener {

    private TextView txtResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtResult = findViewById(R.id.txtResult);

        // Executar uma consulta genérica
        String jsonPayload = "{\"query\": \"SELECT * FROM PESSOA\"}"; // Exemplo de consulta
        new ApiQueryExecutor.QueryExecutorTask(this).execute(jsonPayload);
    }

    @Override
    public void onQueryResult(JsonObject result) {
        // Processar o resultado da consulta e exibir no TextView
        String output = ApiQueryExecutor.processQueryResult(result);
        txtResult.setText(output); // Atualiza o TextView com os resultados
    }
}