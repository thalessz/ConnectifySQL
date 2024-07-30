package com.teste.apiconnection;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity é a atividade principal do aplicativo.
 * Ela inicializa a interface do usuário e executa consultas ao banco de dados.
 */
public class MainActivity extends AppCompatActivity {
    private TextView txtResult; // TextView para exibir os resultados da consulta
    private DatabaseManager dbManager; // Gerenciador de banco de dados

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa o TextView e o DatabaseManager
        txtResult = findViewById(R.id.txtResult);
        dbManager = new DatabaseManager(txtResult);

        // Executa uma consulta SELECT automaticamente ao iniciar a atividade
        dbManager.execute("SELECT NOME FROM PESSOA");
    }

    /**
     * Exibe os resultados da consulta no TextView.
     */
    public void displayResults() {
        StringBuilder output = new StringBuilder();
        for (String value : dbManager.fetchAllValues()) {
            output.append(value).append("\n"); // Adiciona todos os valores
        }
        txtResult.setText(output.toString()); // Exibe todos os valores no TextView
    }
}