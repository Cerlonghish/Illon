package com.example.android.illon;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class LotActivity extends Activity {
    private static final String url = "http://164.132.47.236/illon/illon_api/lot/read_current.php";
    private static final String urlBidButton = "http://164.132.47.236/illon/illon_api/bid/create.php";
    private static final String urlPic = "http://164.132.47.236/illon/illon_api/pic/read.php";
    private long millsRimanenti;
    private User u;
    private TextView money,lot,lotName,tRimanente,minBid,yourBid,about;
    private EditText enterBid;
    private Button bidButton;
    private ImageButton userButton;
    private ImageView illonImage;
    private HorizontalScrollView lotImages;
    private  Lot current_lot;
    private LinearLayout containter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.current_lot);
        u = (User) getIntent().getSerializableExtra("User");
        money = findViewById(R.id.money);
        lot =findViewById(R.id.lot);
        lotName = findViewById(R.id.lotName);
        tRimanente =findViewById(R.id.tRimanente);
        minBid = findViewById(R.id.minBid);
        yourBid =  findViewById(R.id.yourBid);
        about = findViewById(R.id.about);
        enterBid = findViewById(R.id.enterBid);
        bidButton = findViewById(R.id.bidButton);
        userButton = findViewById(R.id.userButton);
        illonImage = findViewById(R.id.illonImage);
        lotImages = findViewById(R.id.lotImages);
        containter = lotImages.findViewById(R.id.container);

        money.setText("Money: "+u.getMoney());
        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchLotActivity(u);
            }
        });
        bidButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendBid();
                    }
                }
        );

        current_lot = setLot();

        setLotView(current_lot);
    }

    private void sendBid() {
        String s[] = new String[1];

        Pair <Integer, InputStream> read_response =  null;
        //prende il numero
        int offerta = -1;
        if(enterBid.getText().toString().length()>0) {
            offerta = Integer.parseInt(enterBid.getText().toString());
        }
        if((current_lot.getValue() != -1 ? offerta>current_lot.getValue() : offerta>=current_lot.getMin_value()) && u.getMoney()>=offerta) {
            Connection c = new Connection();
            s[0] = urlBidButton+"?bid_user="+u.getId()+"&bid_lot="+current_lot.getId()+"&bid_value="+offerta;
            try{
                read_response = c.execute(s).get();
            }catch (ExecutionException ex){
                Log.d("LOT:Eccezione","Execution exception");
            }catch (InterruptedException ex){
                Log.d("LOT:Eccezione","Interrupted exception");
            }
            if(read_response.first==201) {
                Toast.makeText(this,"Bid created successfully",Toast.LENGTH_SHORT).show();
                u.setMyBid(offerta);
            } else {
                Toast.makeText(this,"An error uccurred while creating bid "+read_response.first,Toast.LENGTH_SHORT).show();
                Toast.makeText(this,""+s[0],Toast.LENGTH_SHORT).show();
            }
            c.disconnect();
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
        } else  {
            Toast.makeText(this,"Bid too small",Toast.LENGTH_SHORT).show();
        }
    }

    public void setLotView(Lot l){
        illonImage.setImageDrawable(getDrawable(R.drawable.illon_logo));
        //ID E NOME DEL LOTTO
        lot.setText("LOT #"+l.getId());
        lotName.setText(l.getName());

        //tRimanente
        long millsStartTime = l.getStart_time().getTime();
        millsStartTime += 600000;
        millsRimanenti = millsStartTime - Calendar.getInstance().getTime().getTime();
        //millsRimanenti=600000;
        //CountDownTimer countDownTimer =
        new CountDownTimer(millsRimanenti, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                millsRimanenti = millisUntilFinished;
                updateTimer(millsRimanenti);
            }

            @Override
            public void onFinish() {
                u.setMyBid(-1);
            }
        }.start();

        //ABOUT
        about.setText("About: "+l.getAbout());

        //MINBID
        if(l.getValue()!=-1) {
            minBid.setText("Min bid: " + l.getValue());
        } else {
            minBid.setText("Min bid: " + l.getMin_value());
        }
        //deve essere null finchè non viene fatta la prima bid
        //yourBid.setText("Your bid: "+l.getValue());
        //YOURBID --> DA SISTEMARE
        if(u.getMyBid() == -1)
            yourBid.setText("Your bid: X");
        else
            yourBid.setText("Your bid: "+u.getMyBid());

        //IMMAGINI
        String s[] = new String[1];
        s[0] = urlPic+"?pic_lot="+l.getId();
        Connection c = new Connection();
        Pair <Integer, InputStream> read_response =  null;
        try{
            read_response = c.execute(s).get();
        }catch (ExecutionException ex){
            Log.d("LOT:Eccezione","Execution exception");
        }catch (InterruptedException ex){
            Log.d("LOT:Eccezione","Interrupted exception");
        }
        ArrayList<String> imageUrls =new ArrayList();
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
                parserXMLtoImages(imageUrls, file_read);
                //ORA SI HA L'ARRAYLIST DEGLI URL
                LayoutInflater inflater = LayoutInflater.from(this);


                for(int i=0;i<imageUrls.size();i++) {
                    View view = inflater.inflate(R.layout.item_image, containter, false);
                    ImageView iv = view.findViewById(R.id.imageList);
                    Bitmap difi = new DownloadImageFromInternet().execute(imageUrls.get(i)).get();
                    if(difi != null) {
                        iv.setImageBitmap(difi);
                    } else {
                        Log.d("ERROR", "setLotView: ");
                    }
                    containter.addView(view);
                }


            } catch (IOException ex) {
                Log.d("LOT:Eccezione","IOException");
            } catch (SAXException ex) {
                Log.d("LOT:Eccezione","SAXException");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this,"Unable to find Images",Toast.LENGTH_SHORT);
        }
        c.disconnect();
    }

    private void parserXMLtoImages(ArrayList<String> imageUrls, Document file) {
        NodeList nl = file.getElementsByTagName("pic");
        for(int i=0;i<nl.getLength();i++) {
            Element e = (Element) nl.item(i);
            String p = e.getElementsByTagName("pic_path").item(0).getTextContent();
            imageUrls.add(p);
        }
    }

    private Lot parserXMLtoLot(Document file){
        Node user = file.getElementsByTagName("lot").item(0);
        Element eLot = (Element)user;
        Node idTag = eLot.getElementsByTagName("lot_id").item(0);
        int lot_id = Integer.parseInt(idTag.getTextContent());
        Node nameTag =  eLot.getElementsByTagName("lot_name").item(0);
        String lot_name = nameTag.getTextContent();
        Node aboutTag =  eLot.getElementsByTagName("lot_about").item(0);
        String lot_about = aboutTag.getTextContent();
        Node min_valueTag = eLot.getElementsByTagName("lot_min_value").item(0);
        int lot_min_value = Integer.parseInt(min_valueTag.getTextContent());
        //null fino alla prima bid
        Node valueTag = eLot.getElementsByTagName("lot_value").item(0);
        //int lot_value = Integer.parseInt(valueTag.getTextContent());
        int lot_value = -1;
        if(!valueTag.getTextContent().equals("NULL")) {
            lot_value = Integer.parseInt(valueTag.getTextContent()); //VALUE
        }
        Node dateTag = eLot.getElementsByTagName("lot_start_time").item(0);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ITALIAN);
        Date lot_start_time = null;
        try {
            lot_start_time = (Date) format.parse(dateTag.getTextContent());
        } catch (ParseException e) {
            Log.d("LOT:Eccezione","ParseException");
        }

        //null finchè non finisce (ovvio)
        //Node winnerTag = eLot.getElementsByTagName("lot_winner").item(0);
        //int lot_winner = Integer.parseInt(winnerTag.getTextContent());

        Lot l = new Lot(lot_id, lot_name, lot_about, lot_min_value, lot_value, lot_start_time, -1);

        return l;
    }

    public void launchLotActivity(User u) {
        Log.d("LOT", "APRO LAYOUT USER");
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra("User", u);
        startActivity(intent);
    }

    public void updateTimer (long millis) {
        int minutes = (int) (millis/60000);
        int seconds = (int) (millis%60000/1000);
        String timeLeftText=minutes+":";
        if(seconds<10) timeLeftText+="0";
        timeLeftText += seconds;
        tRimanente.setText(timeLeftText);
    }

    public Lot setLot(){
        String s[] = new String[1];
        s[0] = url;
        Pair <Integer, InputStream> read_response =  null;

        Connection read_conn = new Connection();
        try{
            read_response = read_conn.execute(s).get();
        }catch (ExecutionException ex){
            Log.d("LOT:Eccezione","Execution exception");
        }catch (InterruptedException ex){
            Log.d("LOT:Eccezione","Interrupted exception");
        }

        if(read_response.first == 200){
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
                read_conn.disconnect();
                return parserXMLtoLot(file_read);

            } catch (IOException ex) {
                Log.d("LOT:Eccezione","IOException");
            } catch (SAXException ex) {
                Log.d("LOT:Eccezione","SAXException");
            }
        }else{
            read_conn.disconnect();
            Log.d("LOT: Errore","Lettura Lot attuale errata");
            return null;
        }
        return null;
    }
}
