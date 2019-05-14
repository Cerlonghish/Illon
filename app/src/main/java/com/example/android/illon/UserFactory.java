package com.example.android.illon;

import android.util.Log;
import android.util.Pair;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class UserFactory {
    private static final String url = "http://164.132.47.236/illon/illon_api/user/";
    private static final String api_read_one = url + "read_one_name.php";
    private static final String api_create = url + "create.php";
    private static String[] s = new String[2];

    public static User getUser(String username) {
        String read_username = api_read_one + "?user_name=" + username;

        //prima connessione: verifica presenza username nel database
        s[0] = read_username;
        s[1] = "user/read_one_name.php";
        Connection read_conn = new Connection();
        Pair<Integer, InputStream> read_response = null;
        try {
            read_response = read_conn.execute(s).get();
        } catch (ExecutionException ex) {
            Log.d("LOGIN:Eccezione", "Execution exception");
        } catch (InterruptedException ex) {
            Log.d("LOGIN:Eccezione", "Interrupted exception");
        }
        read_conn.disconnect();
        //controllo risposta lettura
        if (read_response.first != 200) {
            return null;
        } else {
            //lettura file da input stream
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = null;
            try {
                db = dbf.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                Log.d("LOGIN:Eccezione", "ParserConfigurationException");
            }
            Document file_read;

            try {
                assert db != null;
                file_read = db.parse(read_response.second);

                return parserXMLtoUser(file_read);
            } catch (IOException ex) {
                Log.d("LOGIN:Eccezione", "IOException");
            } catch (SAXException ex) {
                Log.d("LOGIN:Eccezione", "SAXException");
            }
        }
        return null;
    }

    /**
     *
     * @param file Document XML
     * @return User
     */
    private static User parserXMLtoUser (Document file){
        Node user = file.getElementsByTagName("user").item(0);
        Element eUser = (Element) user;
        Node idTag = eUser.getElementsByTagName("user_id").item(0);
        int user_id = Integer.parseInt(idTag.getTextContent());
        Node nameTag = eUser.getElementsByTagName("user_name").item(0);
        String user_name = nameTag.getTextContent();
        Node moneyTag = eUser.getElementsByTagName("user_money").item(0);
        int user_money = Integer.parseInt(moneyTag.getTextContent());

        if (file.getElementsByTagName("bid").getLength() > 0) {
            int bid = Integer.parseInt(file.getElementsByTagName("bid_value").item(0).getTextContent());
            return new User(user_id, user_name, user_money, bid);
        } else {
            return new User(user_id, user_name, user_money);
        }
    }

}
