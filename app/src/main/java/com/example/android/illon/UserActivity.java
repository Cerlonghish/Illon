package com.example.android.illon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class UserActivity extends AppCompatActivity {
    private User u;
    private TextView user_name,stats;
    private ListView wonList;
    private Button back;
    private FloatingActionButton fab;
    private String urlReadWon = "http://164.132.47.236/illon/illon_api/user/read_won.php";
    private ArrayList<String> imageUrls;
    private ArrayList<Lot> lotList;
    private ArrayList<LotAndImage> lai = new ArrayList<>();
    private int moneySpent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_layout);

        u = (User) getIntent().getSerializableExtra("User");
        user_name = findViewById(R.id.user_name);
        stats = findViewById(R.id.stats);
        wonList = findViewById(R.id.wonlist);
        back = findViewById(R.id.back);
        fab = findViewById(R.id.fab);
        urlReadWon += "?user_id="+u.getId();
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchLotActivity(u);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchLoginActivity();
            }
        });

        user_name.setText(u.getName());
        createList();
    }

    private void createList() {
        Connection c = new Connection();
        Pair<Integer, InputStream> read_response =  null;
        try{
            read_response = c.execute(urlReadWon).get();
        }catch (ExecutionException ex){
            Log.d("LOT:Eccezione","Execution exception");
        }catch (InterruptedException ex){
            Log.d("LOT:Eccezione","Interrupted exception");
        }
        if(read_response.first == 200) {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = null;
            try {
                db = dbf.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                Log.d("LOT:Eccezione","ParserConfigurationException");
            }
            Document file_read;
            try {
                file_read = db.parse(read_response.second);
                lotList = new ArrayList<>();
                parserXMLtoLots(lotList, file_read);
                //ORA HO TUTTI I LOTTI VINTI DALL'UTENTE --> recupero le immagini, una per lotto
                imageUrls = new ArrayList<>();
                getImageUrls();
                //ORA SI HA L'ARRAYLIST DEGLI URL
                LayoutInflater inflater = LayoutInflater.from(this);


                for(int i=0;i<imageUrls.size();i++) {
                    Log.d("IMMAGINE", "createList: "+imageUrls.get(i));
                    lai.add(new LotAndImage(lotList.get(i),imageUrls.get(i)));
                }
                WonAdapter adapter = new WonAdapter(this,lai);
                wonList.setAdapter(adapter);


                stats.setText("Money:\t"+u.getMoney()+"\nBid won:\t"+lotList.size()+"\nMoney spent:\t"+moneySpent);
            } catch (IOException ex) {
                Log.d("LOT:Eccezione","IOException");
            } catch (SAXException ex) {
                Log.d("LOT:Eccezione","SAXException");
            }
        }
        c.disconnect();
    }

    private void getImageUrls() {
        String url = "http://164.132.47.236/illon/illon_api/pic/read.php?pic_lot=";
        for(int i=0;i<lotList.size();i++) {
            String s = url+lotList.get(i).getId();
            Connection c = new Connection();
            Pair<Integer, InputStream> read_response =  null;
            try{
                read_response = c.execute(s).get();
            }catch (ExecutionException ex){
                Log.d("LOT:Eccezione","Execution exception");
            }catch (InterruptedException ex){
                Log.d("LOT:Eccezione","Interrupted exception");
            }
            if(read_response.first==200) {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = null;
                try {
                    db = dbf.newDocumentBuilder();
                } catch (ParserConfigurationException e) {
                    Log.d("LOT:Eccezione","ParserConfigurationException");
                }
                Document file_read;
                try {
                    file_read = db.parse(read_response.second);
                    NodeList nl = file_read.getElementsByTagName("pic");
                    Element e = (Element) nl.item(0);
                    imageUrls.add(e.getElementsByTagName("pic_path").item(0).getTextContent());
                } catch (IOException ex) {
                    Log.d("LOT:Eccezione","IOException");
                } catch (SAXException ex) {
                    Log.d("LOT:Eccezione","SAXException");
                }
            }
            c.disconnect();
        }
    }

    public void parserXMLtoLots(ArrayList l, Document file) {
        moneySpent=0;
        NodeList nl = file.getElementsByTagName("lot");
        for(int i=0;i<nl.getLength();i++) {
            Element e = (Element) nl.item(i);
            int id = Integer.parseInt(e.getElementsByTagName("lot_id").item(0).getTextContent());
            String name = e.getElementsByTagName("lot_name").item(0).getTextContent();
            String about = e.getElementsByTagName("lot_about").item(0).getTextContent();
            int minValue = Integer.parseInt(e.getElementsByTagName("lot_min_value").item(0).getTextContent());
            int value = -1;
            if(!e.getElementsByTagName("lot_value").item(0).getTextContent().equals("NULL")) {
                value = Integer.parseInt(e.getElementsByTagName("lot_value").item(0).getTextContent());
                moneySpent+=value;
            }
            Node dateTag = e.getElementsByTagName("lot_start_time").item(0);
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ITALIAN);
            Date lot_start_time = null;
            try {
                lot_start_time = format.parse(dateTag.getTextContent());
            } catch (ParseException ex) {
                Log.d("LOT:Eccezione","ParseException");
            }
            Node winnerTag = e.getElementsByTagName("lot_winner").item(0);
            int lot_winner = Integer.parseInt(winnerTag.getTextContent());
            l.add(new Lot(id,name,about,minValue,value,lot_start_time,lot_winner));
        }
    }

    //------------------------- OK -----------------------------------------
    public void launchLoginActivity() {
        Log.d("USER", "Torno in LOGIN");
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void launchLotActivity (User u) {
        Log.d("USER", "APRO LAYOUT DEL LOTTO");
        Intent intent = new Intent(this, LotActivity.class);
        intent.putExtra("User", u);
        startActivity(intent);
    }
}
