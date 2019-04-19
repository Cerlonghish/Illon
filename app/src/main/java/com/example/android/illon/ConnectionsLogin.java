package com.example.android.illon;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Pair;
import android.widget.Toast;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class ConnectionsLogin extends AsyncTask<String, Void, Pair<Integer,InputStream>> {

   /* public interface AsyncResponse {
        void processFinish(Integer output);
    }
    public AsyncResponse delegate = null;

    public ConnectionsLogin(AsyncResponse delegate){
        this.delegate = delegate;
    }*/

    @Override
    protected Pair<Integer,InputStream> doInBackground (String [] url){
        try{
            Pair<Integer,InputStream> p;
            URL server = new URL(url[0]);
            HttpURLConnection connection = (HttpURLConnection) server.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int response_code = connection.getResponseCode();
            if(response_code==200) {
                InputStream is = server.openStream();
                p = new Pair<>(response_code,is);
            } else {
                p = new Pair<>(response_code,null);
            }

            return p;
        }catch (MalformedURLException ex){
            System.out.println("URL exception");
        }catch(IOException ex){
            System.out.println("URLConnection exception");
        }
        return null;
    }

    protected void onPostExecute(Integer result){
        //delegate.processFinish(result);
    }
}
