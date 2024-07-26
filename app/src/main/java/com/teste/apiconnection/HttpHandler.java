package com.teste.apiconnection;
import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpHandler {
    public static String PostRequest(String strUrl, String jsonPayload){
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setDoOutput(true);

            StringBuilder response = getStringBuilder(jsonPayload, urlConnection);
            return response.toString();
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        } finally {
            if (urlConnection != null){
                urlConnection.disconnect();
            }
        }
    }

    private static StringBuilder getStringBuilder(String jsonPayload, HttpURLConnection urlConnection) throws IOException {
        try(OutputStream os = urlConnection.getOutputStream()){
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input,0,input.length);
        }

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String responseLine;
        while((responseLine = bufferedReader.readLine()) != null){
            response.append(responseLine.trim());
        }
        return response;
    }
}
