package com.example.android.illon;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;

public class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
    protected Bitmap doInBackground(String... urls) {
        String imageURL = "http://164.132.47.236"+urls[0];
        Bitmap bimage = null;
        InputStream in;
        try {
            in = new java.net.URL(imageURL).openStream();
            bimage = BitmapFactory.decodeStream(in);
            in.close();
            Log.d("BIMAGE", "doInBackground: "+bimage.toString());
        } catch (Exception e) {
            Log.e("Error Message", e.getMessage());
            e.printStackTrace();
        }
        return bimage;
    }
}
