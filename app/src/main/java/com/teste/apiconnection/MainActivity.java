package com.teste.apiconnection;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
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

            // Exemplo de consulta com múltiplos parâmetros
            Map<String, String> params = new HashMap<>();
            params.put("param1", "5"); // Valor para ID

            // A consulta deve usar '?' como placeholders
            dbManager.execute("SELECT NOME FROM PESSOA WHERE ID = %s", params);
        }

        /**
         * Exibe os resultados da consulta no TextView.
         */
        public void displayResults() {
            StringBuilder output = new StringBuilder();
            for (int i = 0; i < dbManager.getResultCount(); i++) {
                output.append(dbManager.getValueAt(i)).append("\n"); // Adiciona todos os valores
            }
            txtResult.setText(output.toString()); // Exibe todos os valores no TextView
        }
    }
