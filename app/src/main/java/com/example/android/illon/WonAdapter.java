package com.example.android.illon;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class WonAdapter extends ArrayAdapter<LotAndImage> {
    public WonAdapter(Context context, ArrayList<LotAndImage> l) {
        super(context, 0, l);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position

        LotAndImage l = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.product_item, parent, false);
        }

        // Lookup view for data population

        TextView name = convertView.findViewById(R.id.product_name);
        TextView desc = convertView.findViewById(R.id.product_desc);
        TextView price = convertView.findViewById(R.id.product_price);
        ImageView iv = convertView.findViewById(R.id.product_pic);

        // Populate the data into the template view using the data object
        name.setText(l.l.getName());
        desc.setText(l.l.getAbout());
        price.setText(l.l.getValue()+"");
        Bitmap difi = null;
        try {
            difi = new DownloadImageFromInternet(iv).execute(l.imgUrl).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(difi != null && iv!=null) {
            iv.setImageBitmap(difi);
        } else {
            Log.d("ERROR", "setLotView: ");
        }

        // Return the completed view to render on screen

        return convertView;
    }

}
