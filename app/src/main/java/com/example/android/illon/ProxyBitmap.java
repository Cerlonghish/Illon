package com.example.android.illon;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
/** * Created by John on 07-Sep-15. */
public class ProxyBitmap implements Serializable{
    private byte[] byteArray;

    public ProxyBitmap(Bitmap bitmap){
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, o);
        byteArray = o.toByteArray();
    }

    public Bitmap getBitmap(){
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
}
