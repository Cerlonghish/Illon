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

   /* public interface AsyncResponse {
        void processFinish(Integer output);
    }
    public AsyncResponse delegate = null;

    public Connection(AsyncResponse delegate){
        this.delegate = delegate;
    }*/

    @Override
    protected Pair<Integer,InputStream> doInBackground (String [] url){
        int response_code;
        try{
            Pair<Integer,InputStream> p;
            URL server = new URL(url[0]);
            HttpURLConnection connection = (HttpURLConnection) server.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            response_code = connection.getResponseCode();

            if(response_code==200) {
                InputStream is = server.openStream();
                p = new Pair<>(response_code,is);
            } else {
                p = new Pair<>(response_code,null);
            }
            Log.d("Connection:","fine connessione: "+response_code);

            return p;
        }catch (MalformedURLException ex){
            Log.d("CONNECTION:Eccezione","URL exception");
        }catch(IOException ex){
            Log.d("CONNECTION:Eccezione","URLConnection exception");
        }
        return null;
    }
}
