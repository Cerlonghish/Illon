package com.example.android.illon;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
    private ImageButton back;
    private FloatingActionButton fab;
    private String urlReadWon = "http://164.132.47.236/illon/illon_api/user/read_won.php";
    private ArrayList<Lot> lotList;
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
                setContentView(R.layout.loading_layout);
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
        createCachedList();
    }

    private void createList() {
        Connection c = new Connection();
        String[] s = new String[2];
        s[0] = urlReadWon;
        s[1] = "user/read_won.php";
        Pair<Integer, InputStream> read_response =  null;
        try{
            read_response = c.execute(s).get();
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
                u.setMoneySpent(0);
                for(int i=0;i<lotList.size();i++) {
                    u.setMoneySpent(u.getMoneySpent()+lotList.get(i).getValue());
                }
                WonAdapter adapter = new WonAdapter(this,lotList);
                wonList.setAdapter(adapter);

                stats.setText("Money:\t"+u.getMoney()+"\nBid won:\t"+lotList.size()+"\nMoney spent:\t"+u.getMoneySpent());
            } catch (IOException ex) {
                Log.d("LOT:Eccezione","IOException");
            } catch (SAXException ex) {
                Log.d("LOT:Eccezione","SAXException");
            }
        } else {
            stats.setText("Money:\t"+u.getMoney()+"\nBid won:\t0\nMoney spent:\t0");
        }
        c.disconnect();
    }

    public void createCachedList() {
        retriveList();
        if(lotList==null) {
            createList();
        } else {
            u.setMoneySpent(0);
            for(int i=0;i<lotList.size();i++) {
                u.setMoneySpent(u.getMoneySpent()+lotList.get(i).getValue());
            }
            WonAdapter adapter = new WonAdapter(this,lotList);
            wonList.setAdapter(adapter);
            stats.setText("Money:\t"+u.getMoney()+"\nBid won:\t"+lotList.size()+"\nMoney spent:\t"+u.getMoneySpent());
        }
    }

    public void parserXMLtoLots(ArrayList l, Document file) {
        NodeList nlLot = file.getElementsByTagName("lot");
        NodeList nlPic = file.getElementsByTagName("pic");
        for(int i=0;i<nlLot.getLength();i++) {
            Element eLot = (Element) nlLot.item(i);
            Element ePic = (Element) nlPic.item(i);
            int id = Integer.parseInt(eLot.getElementsByTagName("lot_id").item(0).getTextContent());
            String name = eLot.getElementsByTagName("lot_name").item(0).getTextContent();
            String about = eLot.getElementsByTagName("lot_about").item(0).getTextContent();
            int minValue = Integer.parseInt(eLot.getElementsByTagName("lot_min_value").item(0).getTextContent());
            int value = -1;
            if(!eLot.getElementsByTagName("lot_value").item(0).getTextContent().equals("NULL")) {
                value = Integer.parseInt(eLot.getElementsByTagName("lot_value").item(0).getTextContent());
            }
            Node dateTag = eLot.getElementsByTagName("lot_start_time").item(0);
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ITALIAN);
            Date lot_start_time = null;
            try {
                lot_start_time = format.parse(dateTag.getTextContent());
            } catch (ParseException ex) {
                Log.d("LOT:Eccezione","ParseException");
            }
            Node winnerTag = eLot.getElementsByTagName("lot_winner").item(0);
            int lot_winner = Integer.parseInt(winnerTag.getTextContent());
            String imagePath = ePic.getElementsByTagName("pic_path").item(0).getTextContent();
            l.add(new Lot(id,name,about,minValue,value,lot_start_time,lot_winner,imagePath));
        }
    }

    public void launchLoginActivity() {
        cacheList();
        Log.d("USER", "Torno in LOGIN");
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("Provenienza","User");
        startActivity(intent);
    }

    private void cacheList() {
        File dir = getCacheDir();
        File fileLot = new File(dir.getAbsolutePath(),"fileList.txt");
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(fileLot);
            ObjectOutputStream oos = new ObjectOutputStream(fOut);
            oos.writeInt(u.getId());
            oos.writeObject(lotList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void retriveList() {
        File dir = getCacheDir();
        File file = new File(dir,"fileList.txt");
        if(file.exists()) {
            try {
                FileInputStream fIn = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fIn);
                int id = ois.readInt();
                if(id == u.getId()) {
                    lotList = (ArrayList<Lot>) ois.readObject();
                } else {
                    lotList = null;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            lotList=null;
        }
    }

    public void launchLotActivity (User u) {
        cacheList();
        Log.d("USER", "APRO LAYOUT DEL LOTTO");
        Intent intent = new Intent(this, LotActivity.class);
        intent.putExtra("User", u);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {}
}
