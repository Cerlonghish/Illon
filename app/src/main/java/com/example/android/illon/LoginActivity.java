package com.example.android.illon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class LoginActivity extends Activity {
    private static final String url = "http://164.132.47.236/illon/illon_api/user/";
    private static final String api_read_one = url + "read_one.php";
    private static final String api_create = url + "create.php";

    public boolean creation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button button_login = (Button) findViewById(R.id.button_login);
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
                v.setEnabled(false);
            }
        });
    }

    public void connect() {
        Toast t = Toast.makeText(this, "Funzione Connection() ", Toast.LENGTH_LONG);
        t.show();
        EditText edit_username = (EditText) findViewById(R.id.username);
        String username = edit_username.toString();
        boolean user_create = false;

        String read_username = api_read_one + "?user_name='" + username + "'";

        String[] s = new String[1];
        s[0] = read_username;
        ConnectionsLogin read_conn = new ConnectionsLogin();
        Pair<Integer, InputStream> read_response = null;
        try{
            read_response = read_conn.execute(s).get();
        }catch (ExecutionException ex){
            System.out.println("Execution exception");
            t = Toast.makeText(this, "Execution exception", Toast.LENGTH_LONG);
            t.show();
        }catch (InterruptedException ex){
            System.out.println("Interrupted exception");
            t = Toast.makeText(this, "Interrupted exception", Toast.LENGTH_LONG);
            t.show();
        }


        if(read_response.first != 200){
            userCreation();
            if(creation){
                user_create = true;

                //da aggiungere il token
                String create_username = api_create + "?user_name='" + username + "'";
                s[0] = create_username;
                ConnectionsLogin create_conn = new ConnectionsLogin();
                Pair <Integer, InputStream> create_response = null;

                try{
                    create_response = create_conn.execute(s).get();
                }catch (ExecutionException ex){
                    System.out.println("create Execution exception");
                    t = Toast.makeText(this, "create Execution exception", Toast.LENGTH_LONG);
                    t.show();
                }catch (InterruptedException ex){
                    System.out.println("create Interrupted exception");
                    t = Toast.makeText(this, "create Interrupted exception", Toast.LENGTH_LONG);
                    t.show();
                }

                if (create_response.first == 200) {
                    Toast toast = Toast.makeText(this, "User '" + username + "' has been created successfully ", Toast.LENGTH_LONG);
                    toast.show();
                }

                read_conn = new ConnectionsLogin();
                s[0] = read_username;
                try{
                    read_response = read_conn.execute(s).get();
                }catch (ExecutionException ex){
                    System.out.println("Execution exception");
                    t = Toast.makeText(this, "Execution exception", Toast.LENGTH_LONG);
                    t.show();
                }catch (InterruptedException ex){
                    System.out.println("Interrupted exception");
                    t = Toast.makeText(this, "Interrupted exception", Toast.LENGTH_LONG);
                    t.show();
                }
            }
        }

        //lettura file da input stream
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document file_read;
        User user_login;
        if(read_response.first == 200) {
            try {
                file_read = db.parse(read_response.second);
                user_login = parserXMLtoUser(file_read);
                Toast toast = Toast.makeText(this, user_login.toString(), Toast.LENGTH_LONG);
                toast.show();
            } catch (IOException ex) {
                Toast toast = Toast.makeText(this, "IOException", Toast.LENGTH_LONG);
                toast.show();
            } catch (SAXException ex) {
                Toast toast = Toast.makeText(this, "SAXException", Toast.LENGTH_LONG);
                toast.show();
            }
        }else {
            Toast toast = Toast.makeText(this, "Lettura non riuscita.", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void userCreation(){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("");
        alertDialog.setMessage("Do you want to create the account?");
        alertDialog.setButton(
                DialogInterface.BUTTON_POSITIVE,
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        creation = true;
                    }
                });
        alertDialog.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        creation = false;
                    }
                });
        alertDialog.show();
    }

    private User parserXMLtoUser(Document file){
        Element user = file.getElementById("user");
        Node idTag = user.getElementsByTagName("user_id").item(0);
        int user_id = Integer.parseInt(idTag.getNodeValue());
        Node nameTag = user.getElementsByTagName("user_id").item(0);
        String user_name = idTag.getNodeValue();
        Node moneyTag = user.getElementsByTagName("user_id").item(0);
        int user_money = Integer.parseInt(idTag.getNodeValue());

        User u = new User(user_id, user_name, user_money);
        return u;
    }
}
