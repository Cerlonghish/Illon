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
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class LoginActivity extends Activity {
    private static final String url = "http://164.132.47.236/illon/illon_api/user/";
    private static final String api_read_one = url + "read_one_name.php";
    private static final String api_create = url + "create.php";
    private String username;
    private String create_username;
    private Pair <Integer, InputStream> create_response;
    private String[] s = new String[2];
    private Button button_login;
    private EditText edit_username;

    /**
     * l'attributo l viene salvato se esiste
     * richiama connect quando si preme su login
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        button_login =  findViewById(R.id.button_login);
        edit_username = findViewById(R.id.username);
        if(getIntent().getStringExtra("Provenienza")==null) {
            DeleteCacheDir.deleteDir(getCacheDir());
        }
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PULSANTE PREMUTO", "onClick: ");
                connect();
            }
        });
    }

    /**
     * disabilita il bottone di login, prova la connessione a read_one con l'username inserito
     * se la risposta http non è 200, lo crea (DA SISTEMARE)
     * se la risposta http è 200, creo l'oggetto user parsando l'xml della risposta http, passo all'activity "LotActivity"
     */
    public void connect() {
        Log.d("CONNESSIONE_LOGIN", "connect: "+edit_username.getText().toString());
        setContentView(R.layout.loading_layout);

        username = edit_username.getText().toString();
        User user_login = UserFactory.getUser(username);

        if(user_login==null){
            setContentView(R.layout.activity_login);
            userCreation();
            Log.d("FINE USERCREATION", "ciao");
        } else {
            launchLotActivity(user_login);
        }
    }

    /**
     * con un dialog si chiede all'utente se si vuole creare veramente un nuovo account con l'username inserito
     * se si preme si --> connessione http per creare l'account e connessione per leggerne i dati e parsarli
     * quindi passo all'activity "LotActivity"
     */
    private void userCreation(){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("");
        alertDialog.setMessage("Do you want to create the account?");
        alertDialog.setButton(
                DialogInterface.BUTTON_POSITIVE,
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setContentView(R.layout.loading_layout);
                        create_username = api_create + "?user_name=" + username;
                        s[0] = create_username;
                        s[1] = "user/create.php";
                        //connessione per creare l'user nel database
                        Connection create_conn = new Connection();
                        create_response = null;
                        try{
                            create_response = create_conn.execute(s).get();
                        }catch (ExecutionException ex){
                            Log.d("LOGIN:Eccezione","create Execution exception");
                        }catch (InterruptedException ex){
                            Log.d("LOGIN:Eccezione","create Interrupted exception");
                        }

                        //creazione avvenuta con successo
                        if (create_response.first == 201) {
                            Toast toast = Toast.makeText(LoginActivity.this, "User '" + username + "' has been created successfully ", Toast.LENGTH_SHORT);
                            toast.show();

                            User user = UserFactory.getUser(username);
                            if(user!=null) {
                                launchLotActivity(user);
                            } else {
                                Toast.makeText(LoginActivity.this,"ERRORE",Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast toast = Toast.makeText(LoginActivity.this, "Errore nella creazione dell'utente", Toast.LENGTH_SHORT);
                        }
                        setContentView(R.layout.activity_login);
                        create_conn.disconnect();
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    /**
     * apre l'activity LotActivity passando l'oggetto utente e il lotto (se presente) (DA MODIFICARE)
     * @param u utente che passo all'activity
     */
    public void launchLotActivity(User u) {
        Log.d("LOGIN", "APRO LAYOUT DEL LOTTO");
        Intent intent = new Intent(this, LotActivity.class);
        intent.putExtra("User", u);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {}
}