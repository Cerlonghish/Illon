package com.example.android.illon;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

public class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
    ImageView imageView;

    public DownloadImageFromInternet(ImageView imageView) {
        this.imageView = imageView;
        //Toast.makeText(getApplicationContext(), "Please wait, it may take a few minute...", Toast.LENGTH_SHORT).show();
    }

    protected Bitmap doInBackground(String... urls) {
        String imageURL = "http://164.132.47.236"+urls[0];
        Bitmap bimage = null;
        InputStream in = null;
        try {
            in = new java.net.URL(imageURL).openStream();
            bimage = BitmapFactory.decodeStream(in);
            in.close();
            Log.d("BIMAGE: ", "doInBackground: "+bimage.toString());
        } catch (Exception e) {
            Log.e("Error Message", e.getMessage());
            e.printStackTrace();
        }
        return bimage;
    }
}
