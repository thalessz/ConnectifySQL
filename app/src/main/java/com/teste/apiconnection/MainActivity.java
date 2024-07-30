package com.teste.apiconnection;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;

import java.util.List;

public class MainActivity extends AppCompatActivity implements DatabaseListener {
    private TextView txtResult;
    private DatabaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtResult = findViewById(R.id.txtResult);
        dbManager = new DatabaseManager();

        // Executar uma consulta SELECT
        dbManager.execute("SELECT NOME FROM PESSOA");

        // Aguardar um pouco para garantir que a consulta seja realizada
        new android.os.Handler().postDelayed(() -> {
            List<JsonObject> results = dbManager.fetchAll();
            displayResults(results);
        }, 2000); // Aguarda 2 segundos (ajuste conforme necessário)
    }

    private void displayResults(List<JsonObject> results) {
        StringBuilder output = new StringBuilder();
        for (JsonObject result : results) {
            output.append(result.toString()).append("\n"); // Exibe o objeto JSON completo
        }
        txtResult.setText(output.toString());
    }

    @Override
    public void onQueryResult(List<JsonObject> result) {
        Log.d("QUERY_RESULT", "Consulta realizada com sucesso.");
    }

    @Override
    public void onInsertResult(boolean success) {
        if (success) {
            Log.d("INSERT_RESULT", "Inserção realizada com sucesso.");
        } else {
            Log.e("INSERT_RESULT", "Erro ao inserir registro.");
        }
    }
}
