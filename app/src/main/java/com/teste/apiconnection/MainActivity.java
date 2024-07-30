package com.teste.apiconnection;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    TextView txtResult;
    DatabaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtResult = findViewById(R.id.txtResult);
        dbManager = new DatabaseManager(txtResult);

        // Executar uma consulta SELECT automaticamente ao iniciar a atividade
        dbManager.execute("SELECT NOME FROM PESSOA");
    }
}
