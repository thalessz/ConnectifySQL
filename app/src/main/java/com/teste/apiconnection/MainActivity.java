package com.teste.apiconnection;

import android.os.Bundle;
import android.widget.TextView;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Agora Começa a diversão
        TextView txtResultado = findViewById(R.id.txtResult);

        String url = "192.168.0.76:5000/mysql/query";
        String jsonPayload = "{ \"query:\": \"SELECT NOME FROM PESSOA LIMIT 1\"}";

        new HttpPostRequest().execute(url, jsonPayload);
    }
    private class HttpPostRequest extends AsyncTask<String, Void, String >{
        @Override
        protected String doInBackground(String ... params){
            String url = params[0];
            String jsonPayload = params[1];
            return HttpHandler.PostRequest(url, jsonPayload);

        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            if(result!=null){
                Log.d("Resposta da API", result);
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            } else {
                Log.e("ERRO DA API", "Failed to fetch data");

            }
        }

    }

}