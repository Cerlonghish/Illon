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
    private int response_code;
    public boolean creation = false;
    private Button button_login = null;

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
        Toast t = Toast.makeText(this, "Funzione ConnectionsLogin() ", Toast.LENGTH_LONG);
        t.show();
        EditText edit_username = (EditText) findViewById(R.id.username);
        String username = edit_username.toString();
        boolean user_create = false;
        InputStream server = null;
        InputStream create_server = null;
        Pair<Integer, InputStream> p;
        String read_username = api_read_one + "?user_name='" + username + "'";

        String[] s = new String[1];
        s[0] = read_username;
        ConnectionsLogin conn = new ConnectionsLogin();

        conn.execute(s);
        try {
            p =  conn.get();
            response_code = p.first;
            server = p.second;
        } catch (InterruptedException ex) {
            t = Toast.makeText(this, "InterruptedException", Toast.LENGTH_LONG);
            t.show();
        } catch (ExecutionException  ex) {
            t = Toast.makeText(this, "ExecutionException", Toast.LENGTH_LONG);
            t.show();
        }

        try {
           /* //first read
            URL server = new URL(read_username);
            HttpURLConnection connection = (HttpURLConnection) server.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int response_code = connection.getResponseCode();
            URL create_server = null;*/

            if(response_code != 200){ //provo a creare un utente
                userCreation();

                if(creation) {
                    user_create = true;
                    int create_response_code = 503;
                    //da aggiungere il token
                    String create_username = api_create + "?user_name='" + username + "'";
                    s[0] = create_username;
                    try {
                        p = conn.execute(s).get();
                        create_response_code = p.first;
                        create_server = p.second;
                    } catch (InterruptedException ex) {
                        t = Toast.makeText(this, "InterruptedException", Toast.LENGTH_LONG);
                        t.show();
                    } catch (ExecutionException  ex) {
                        t = Toast.makeText(this, "ExecutionException", Toast.LENGTH_LONG);
                        t.show();
                    }

                    if (create_response_code == 200) {
                        Toast toast = Toast.makeText(this, "User '" + username + "' has been created successfully ", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            }
            if(user_create || response_code == 200) { //creo il file xml
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document file_read;
                if(response_code == 200)
                    file_read = db.parse(server);
                else
                    file_read = db.parse(create_server);

                User user_login = parserXMLtoUser(file_read);

                System.out.println(user_login.toString());
                Toast toast = Toast.makeText(this, user_login.toString(), Toast.LENGTH_LONG);
                toast.show();

            }

        } catch (MalformedURLException ex){
            System.out.println("URL exception");
            t = Toast.makeText(this, "url exception", Toast.LENGTH_LONG);
            t.show();
        }catch(IOException ex){
            System.out.println("URLConnection exception");
            t = Toast.makeText(this, "IO exception", Toast.LENGTH_LONG);
            t.show();
        }catch(ParserConfigurationException ex){
            System.out.println("DocumentBuilder exception");
            t = Toast.makeText(this, "Parser Configuration Exception", Toast.LENGTH_LONG);
            t.show();
        }catch(SAXException ex){
            System.out.println("Parser exception");
            t = Toast.makeText(this, "SAXException", Toast.LENGTH_LONG);
            t.show();
        }

        button_login.setEnabled(true);
    }

    private void userCreation(){ //chiede all'utente se vuole creare un account
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
