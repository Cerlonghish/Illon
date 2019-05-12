package com.example.android.illon;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.Pair;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class Connection extends AsyncTask<String, Void, Pair<Integer,InputStream>> {
   private HttpURLConnection connection;
   private String connectionType;
    @Override
    protected Pair<Integer,InputStream> doInBackground (String [] url){
        int response_code;
        try{
            Pair<Integer,InputStream> p;
            URL server = new URL(url[0]);
            connectionType = url[1];
            connection = (HttpURLConnection) server.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            response_code = connection.getResponseCode();
            Log.d("RESPONSEpreIf", "doInBackground: "+response_code);
            boolean stream = false;

            switch (connectionType) {
                case "user/read_one_name.php":
                    if(response_code==200) stream = true;
                    break;
                case "user/read_one_id.php":
                    if(response_code==200) stream = true;
                    break;
                case "user/read_won.php":
                    if(response_code==200) stream = true;
                    break;
                case "lot/read_one.php":
                    if(response_code==200) stream = true;
                    break;
                case "lot/read_current.php":
                    if(response_code==200) stream = true;
                    break;
                case "pic/read.php":
                    if(response_code==200) stream = true;
                    break;
                case "bid/create.php":
                    if(response_code==200) {
                        Log.d("RETURNTTRUECHECK", "check: "+response_code);
                        stream = true;
                    }
                    break;
                case "bid/read_last.php":
                    if(response_code==200) stream = true;
                    break;
            }

            if(stream) {
                InputStream is = server.openStream();
                Log.d("RESPONSE", "doInBackground: "+response_code);
                p = new Pair<>(response_code,is);
            } else {
                Log.d("RESPONSEelse", "doInBackground: "+response_code);
                p = new Pair<>(response_code,null);
            }
            Log.d("RESPONSEpostIf", "doInBackground: "+response_code);
            Log.d("Connection:","fine connessione: "+response_code);
            return p;
        }catch (MalformedURLException ex){
            Log.d("CONNECTION:Eccezione","URL exception");
        }catch(IOException ex){
            Log.d("CONNECTION:Eccezione","URLConnection exception");
        }
        return null;
    }



    void disconnect() {
        connection.disconnect();
    }
}
