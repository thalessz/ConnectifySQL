package com.teste.apiconnection;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private TextView txtResult;
    private DatabaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtResult = findViewById(R.id.txtResult);
        dbManager = new DatabaseManager(txtResult);

        // Exemplo de consulta
        Map<String, String> params = new HashMap<>();
        // Você pode adicionar parâmetros aqui, se necessário

        // Executa a consulta
        dbManager.execute("SELECT NOME, IDADE FROM PESSOA", params);
    }

    /**
     * Exibe os resultados da consulta no TextView.
     */
    public void displayResults() {
        StringBuilder output = new StringBuilder();
        List<List<String>> results = dbManager.fetchAll(); // Obtém todos os resultados

        for (int i = 0; i < results.size(); i++) {
            List<String> row = results.get(i);
            output.append("Linha ").append(i).append(": ");
            for (int j = 0; j < row.size(); j++) {
                output.append("Coluna ").append(j).append(": ").append(row.get(j)).append(" | ");
            }
            output.append("\n");
        }
        txtResult.setText(output.toString()); // Exibe todos os valores no TextView
    }
}
