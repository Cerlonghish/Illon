package com.example.android.illon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class LoginActivity extends Activity {
    private static final String url = "http://164.132.47.236/illon/illon_api/user/";
    private static final String api_read_one = url + "read_one.php";
    private static final String api_create = url + "create.php";
    private Button button_login;
    private EditText edit_username;
    private String username;
    private String read_username;
    private String create_username;
    public boolean creation = false;
    private Pair <Integer, InputStream> create_response;
    private String[] s = new String[1];
    private Toast t;
    private Pair<Integer, InputStream> read_response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        button_login = (Button) findViewById(R.id.button_login);
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });
    }

    public void connect() {
        Button button_login = (Button)findViewById(R.id.button_login);
        button_login.setEnabled(false); //disabilito il bottone
        t = Toast.makeText(this, "Funzione Connection() ", Toast.LENGTH_SHORT);
        t.show();
        edit_username = (EditText) findViewById(R.id.username);
        username = edit_username.getText().toString();
        read_username = api_read_one + "?user_name=" + username;

        //prima connessione
        s[0] = read_username;
        ConnectionsLogin read_conn = new ConnectionsLogin();
        read_response = null;
        try{
            read_response = read_conn.execute(s).get();
            t = Toast.makeText(this, "First read", Toast.LENGTH_SHORT);
            t.show();
        }catch (ExecutionException ex){
            System.out.println("Execution exception");
            t = Toast.makeText(this, "Execution exception", Toast.LENGTH_SHORT);
            t.show();
        }catch (InterruptedException ex){
            System.out.println("Interrupted exception");
            t = Toast.makeText(this, "Interrupted exception", Toast.LENGTH_SHORT);
            t.show();
        }

        //controllo
        Log.d("LoginActivity/response",read_response.first.toString());
        if(read_response.first != 200){
            t = Toast.makeText(this, "response: "+read_response.first, Toast.LENGTH_SHORT);
            t.show();
            userCreation();
            button_login.setEnabled(true);
        } else {
            t = Toast.makeText(this, "response read: " + read_response.first, Toast.LENGTH_SHORT);
            t.show();
            //lettura file da input stream
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = null;
            try {
                db = dbf.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            Document file_read;

            t = Toast.makeText(this, "xml read", Toast.LENGTH_SHORT);
            t.show();

            User user_login = null;
            try {
                file_read = db.parse(read_response.second);

                user_login = parserXMLtoUser(file_read);
                Log.d("USER: ", user_login.toString());

            } catch (IOException ex) {
                Toast toast = Toast.makeText(this, "IOException", Toast.LENGTH_SHORT);
                toast.show();
            } catch (SAXException ex) {
                Toast toast = Toast.makeText(this, "SAXException", Toast.LENGTH_SHORT);
                toast.show();
            }
            button_login.setEnabled(true);
            launchLotActivity(user_login);
        }
    }

    private void userCreation(){
        Toast t = Toast.makeText(this, "user creation()", Toast.LENGTH_SHORT);
        t.show();
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("");
        alertDialog.setMessage("Do you want to create the account?");
        alertDialog.setButton(
                DialogInterface.BUTTON_POSITIVE,
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast t;

                        creation = true;
                        //da aggiungere il token
                        create_username = api_create + "?user_name=" + username;
                        s[0] = create_username;
                        ConnectionsLogin create_conn = new ConnectionsLogin();
                        create_response = null;
                        try{
                            create_response = create_conn.execute(s).get();
                            Toast.makeText(LoginActivity.this, "create", Toast.LENGTH_SHORT).show();
                        }catch (ExecutionException ex){
                            System.out.println("create Execution exception");
                            t = Toast.makeText(LoginActivity.this, "create Execution exception", Toast.LENGTH_SHORT);
                            t.show();
                        }catch (InterruptedException ex){
                            System.out.println("create Interrupted exception");
                            t = Toast.makeText(LoginActivity.this, "create Interrupted exception", Toast.LENGTH_SHORT);
                            t.show();
                        }

                        if (create_response.first == 201) {
                            Toast toast = Toast.makeText(LoginActivity.this, "User '" + username + "' has been created successfully ", Toast.LENGTH_SHORT);
                            toast.show();

                            ConnectionsLogin read_conn = new ConnectionsLogin();
                            s[0] = read_username;
                            try{
                                read_response = read_conn.execute(s).get();
                                t = Toast.makeText(LoginActivity.this, "Second read", Toast.LENGTH_SHORT);
                                t.show();
                            }catch (ExecutionException ex){
                                System.out.println("Execution exception");
                                t = Toast.makeText(LoginActivity.this, "Execution exception", Toast.LENGTH_SHORT);
                                t.show();
                            }catch (InterruptedException ex){
                                System.out.println("Interrupted exception");
                                t = Toast.makeText(LoginActivity.this, "Interrupted exception", Toast.LENGTH_SHORT);
                                t.show();
                            }

                            if(read_response.first == 200) {
                                t = Toast.makeText(LoginActivity.this, "response read: " + read_response.first, Toast.LENGTH_SHORT);
                                t.show();
                                //lettura file da input stream
                                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                                DocumentBuilder db = null;
                                try {
                                    db = dbf.newDocumentBuilder();
                                } catch (ParserConfigurationException e) {
                                    e.printStackTrace();
                                }
                                Document file_read;

                                t = Toast.makeText(LoginActivity.this, "xml read", Toast.LENGTH_SHORT);
                                t.show();

                                User user_login=null;

                                try {
                                    file_read = db.parse(read_response.second);

                                    user_login = parserXMLtoUser(file_read);
                                    Log.d("USER: ", user_login.toString());

                                } catch (IOException ex) {
                                    toast = Toast.makeText(LoginActivity.this, "IOException", Toast.LENGTH_SHORT);
                                    toast.show();
                                } catch (SAXException ex) {
                                     toast = Toast.makeText(LoginActivity.this, "SAXException", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                                launchLotActivity(user_login);
                            }
                        }

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
        Log.d("RISPOSTAXML", "parserXMLtoUser: "+file.toString());
        Toast t = Toast.makeText(this, "parser xml", Toast.LENGTH_SHORT);
        t.show();
        Node user = file.getElementsByTagName("user").item(0);
        Element eUser = (Element)user;
        Node idTag = eUser.getElementsByTagName("user_id").item(0);
        int user_id = Integer.parseInt(idTag.getTextContent());
        Node nameTag =  eUser.getElementsByTagName("user_name").item(0);
        String user_name = nameTag.getTextContent();
        Node moneyTag = eUser.getElementsByTagName("user_money").item(0);
        int user_money = Integer.parseInt(moneyTag.getTextContent());

        User u = new User(user_id, user_name, user_money);

        return u;
    }

    public void launchLotActivity(User u) {
        Log.d("LOGIN", "APRO LAYOUT DEL LOTTO");
        Intent intent = new Intent(this, LotActivity.class);
        intent.putExtra("User", u);
        startActivity(intent);
    }
}
