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
    private static final String api_read_one = url + "read_one_name0.php";
    private static final String api_create = url + "create.php";
    private String username;
    private String read_username;
    private String create_username;
    public boolean creation = false;
    private Pair <Integer, InputStream> create_response;
    private String[] s = new String[2];
    private Pair<Integer, InputStream> read_response;

    /**
     * l'attributo l viene salvato se esiste
     * richiama connect quando si preme su login
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button button_login =  findViewById(R.id.button_login);
        if(getIntent().getStringExtra("Provenienza")==null) {
            DeleteCacheDir.deleteDir(getCacheDir());
        }
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });
    }

    @Override
    public void onBackPressed() {
        //non fa niente perchè di si
    }

    /**
     * disabilita il bottone di login, prova la connessione a read_one con l'username inserito
     * se la risposta http non è 200, lo crea (DA SISTEMARE)
     * se la risposta http è 200, creo l'oggetto user parsando l'xml della risposta http, passo all'activity "LotActivity"
     */
    public void connect() {
        Button button_login = findViewById(R.id.button_login);
        button_login.setEnabled(false); //disabilito il bottone

        //prendo l'username dall'edit text
        EditText edit_username = findViewById(R.id.username);
        username = edit_username.getText().toString();
        read_username = api_read_one + "?user_name=" + username;

        //prima connessione: verifica presenza username nel database
        s[0] = read_username;
        s[1] = "user/read_one_name.php";
        Connection read_conn = new Connection();
        read_response = null;
        try{
            read_response = read_conn.execute(s).get();
        }catch (ExecutionException ex){
            Log.d("LOGIN:Eccezione","Execution exception");
        }catch (InterruptedException ex){
            Log.d("LOGIN:Eccezione","Interrupted exception");
        }

        //controllo risposta lettura
        if(read_response.first != 200){
            userCreation();
            button_login.setEnabled(true);
        } else {
            //lettura file da input stream
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = null;
            try {
                db = dbf.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                Log.d("LOGIN:Eccezione","ParserConfigurationException");
            }
            Document file_read;


            User user_login = null;
            try {
                assert db != null;
                file_read = db.parse(read_response.second);

                user_login = parserXMLtoUser(file_read);

            } catch (IOException ex) {
                Log.d("LOGIN:Eccezione","IOException");
            } catch (SAXException ex) {
                Log.d("LOGIN:Eccezione","SAXException");
            }

            button_login.setEnabled(true);
            read_conn.disconnect();
            //lancio l'activity delle aste passandogli l'user
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
                        creation = true;
                        // ***da aggiungere il token (Tene) ***
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

                            //ultima connessione per leggere i dati dell'utente dopo averli creati
                            Connection read_conn = new Connection();
                            s[0] = read_username;
                            s[1] = "user/read_one_name.php";
                            try{
                                read_response = read_conn.execute(s).get();
                            }catch (ExecutionException ex){
                                Log.d("Eccezione","Execution exception");
                            }catch (InterruptedException ex){
                                Log.d("Eccezione","Interrupted exception");
                            }

                            if(read_response.first == 200) {

                                //lettura file da input stream
                                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                                DocumentBuilder db = null;
                                try {
                                    db = dbf.newDocumentBuilder();
                                } catch (ParserConfigurationException e) {
                                    e.printStackTrace();
                                }
                                Document file_read;

                                User user_login=null;

                                try {
                                    assert db != null;
                                    file_read = db.parse(read_response.second);

                                    user_login = parserXMLtoUser(file_read);

                                } catch (IOException ex) {
                                    Log.d("LOGIN:Eccezione","IOException");
                                } catch (SAXException ex) {
                                    Log.d("LOGIN:Eccezione","IOException");
                                }
                                read_conn.disconnect();
                                launchLotActivity(user_login);
                            }
                        }
                        create_conn.disconnect();

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

    /**
     *
     * @param file xml (dato dall'inputstream della connessione http
     * @return oggetto utente parsato
     */
    private User parserXMLtoUser(Document file){
        Node user = file.getElementsByTagName("user").item(0);
        Element eUser = (Element)user;
        Node idTag = eUser.getElementsByTagName("user_id").item(0);
        int user_id = Integer.parseInt(idTag.getTextContent());
        Node nameTag =  eUser.getElementsByTagName("user_name").item(0);
        String user_name = nameTag.getTextContent();
        Node moneyTag = eUser.getElementsByTagName("user_money").item(0);
        int user_money = Integer.parseInt(moneyTag.getTextContent());

        if(file.getElementsByTagName("bid").getLength()>0) {
            int bid = Integer.parseInt(file.getElementsByTagName("bid_value").item(0).getTextContent());
            return new User(user_id, user_name, user_money, bid);
        } else {
            return new User(user_id, user_name, user_money);
        }
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
