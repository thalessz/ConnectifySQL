package com.teste.apiconnection;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;

import java.util.List;

public class MainActivity extends AppCompatActivity implements DatabaseManager.QueryCallback {
    private TextView txtResult;
    private DatabaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtResult = findViewById(R.id.txtResult);
        dbManager = new DatabaseManager();

        // Executar uma consulta SELECT automaticamente ao iniciar a atividade
        executeQuery("SELECT NOME FROM PESSOA");
    }

    private void executeQuery(String query) {
        dbManager.execute(query, this); // Passa a referência da MainActivity como callback
    }

    @Override
    public void onQueryResult(List<JsonObject> result) {
        Log.d("QUERY_RESULT", "Consulta realizada com sucesso.");
        displayResults(result);
    }

    @Override
    public void onInsertResult(boolean success) {
        if (success) {
            Log.d("INSERT_RESULT", "Inserção realizada com sucesso.");
        } else {
            Log.e("INSERT_RESULT", "Erro ao inserir registro.");
        }
    }

    private void displayResults(List<JsonObject> results) {
        StringBuilder output = new StringBuilder();
        for (JsonObject result : results) {
            output.append(result.toString()).append("\n"); // Exibe o objeto JSON completo
        }
        txtResult.setText(output.toString());
    }
}
