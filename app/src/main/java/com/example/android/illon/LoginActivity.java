package com.example.android.illon;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class LoginActivity extends Activity {
    private static final String url = "http://164.132.47.236/illon/illon_api/user/";
    private static final String api_read_one = url + "read_one.php";
    private static final String api_create = url + "create.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button button_login = (Button) findViewById(R.id.button_login);
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Connection();
            }
        });
    }

    public void Connection() {
        EditText edit_username = (EditText) findViewById(R.id.username);
        String username = edit_username.toString();

        String read_username = api_read_one + "?user_name='" + username + "'";

        try {
            URL server = new URL(read_username);
            HttpURLConnection connection = (HttpURLConnection) server.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int response_code = connection.getResponseCode();

            if(response_code == 404){
                //da aggiungere il token
                String create_username = api_create + "?user_name='" + username + "'";
                URL create_server = new URL(read_username);
                HttpURLConnection create_connection = (HttpURLConnection) create_server.openConnection();
                create_connection.setRequestMethod("GET");
                create_connection.connect();

                int create_response_code = create_connection.getResponseCode();
                if(create_response_code == 200)
                    Toast.makeText(this,"L'utente "+username+" è stato creato correttamente",Toast.LENGTH_LONG);
            }else{
                //lettura user
            }

        } catch (MalformedURLException ex){
            System.out.println("URL exception");
        }catch(IOException ex){
            System.out.println("URLConnection exception");
        }

    }
}
