package com.example.android.illon;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class Connection extends AsyncTask<String, Void, Integer> {
    @Override
    protected Integer doInBackground(String[] url){
        try{
            URL server = new URL(url[0]);
            HttpURLConnection connection = (HttpURLConnection) server.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int response_code = connection.getResponseCode();

            return response_code;
        }catch (MalformedURLException ex){
            System.out.println("URL exception");
        }catch(IOException ex){
            System.out.println("URLConnection exception");
        }


        return null;
    }

    protected void onPostExecute(Integer result){
        super.onPostExecute(result);
    }
}
