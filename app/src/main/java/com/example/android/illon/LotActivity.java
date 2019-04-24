package com.example.android.illon;

import android.app.Activity;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class LotActivity extends Activity {
    private static final String url = "http://164.132.47.236/illon/illon_api/lot/read_current.php";
    private long millsRimanenti;
    private CountDownTimer countDownTimer;
    private User u;
    private TextView money,lot,lotName,tRimanente,minBid,yourBid,about;
    private EditText enterBid;
    private Button bidButton;
    private ImageButton userButton;
    private ImageView illonImage;
    private HorizontalScrollView lotImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.current_lot);
        u = (User) getIntent().getSerializableExtra("User");
        money = (TextView) findViewById(R.id.money);
        lot = (TextView) findViewById(R.id.lot);
        lotName = (TextView) findViewById(R.id.lotName);
        tRimanente = (TextView) findViewById(R.id.tRimanente);
        minBid = (TextView) findViewById(R.id.minBid);
        yourBid = (TextView) findViewById(R.id.yourBid);
        about = (TextView) findViewById(R.id.about);
        enterBid = (EditText) findViewById(R.id.enterBid);
        bidButton = (Button) findViewById(R.id.bidButton);
        userButton = (ImageButton) findViewById(R.id.userButton);
        illonImage = (ImageView) findViewById(R.id.illonImage);
        lotImages = (HorizontalScrollView) findViewById(R.id.lotImages);

        money.setText("Money:"+u.getMoney());
        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchLotActivity(u);
            }
        });

        Lot current_lot = setLot();

        setLotView(current_lot);
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

                return parserXMLtoLot(file_read);

            } catch (IOException ex) {
                Log.d("LOT:Eccezione","IOException");
            } catch (SAXException ex) {
                Log.d("LOT:Eccezione","SAXException");
            }
        }else{
            Log.d("LOT: Errore","Lettura Lot attuale errata");
            return null;
        }
        return null;
    }

    public void setLotView(Lot l){
        Log.d("SETLOTVIEW", "ENTRATO");
        lot.setText("LOT #"+l.getId());
        lotName.setText(l.getName());
        //tRimanente
        long millsStartTime = l.getStart_time().getTime();
        millsStartTime += 600000;
        millsRimanenti = millsStartTime - Calendar.getInstance().getTime().getTime();
        //millsRimanenti=600000;
        Log.d("MILLSRIMANENTI", ""+millsRimanenti);
        countDownTimer = new CountDownTimer(millsRimanenti, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                millsRimanenti = millisUntilFinished;
                updateTimer(millsRimanenti);
            }

            @Override
            public void onFinish() {

            }
        }.start();


        about.setText("About: "+l.getAbout());
        minBid.setText("Min bid: "+l.getMin_value());
        //deve essere null finchè non viene fatta la prima bid
        //yourBid.setText("Your bid: "+l.getValue());
        if(l.getValue() == -1)
            yourBid.setText("Your bid: X");

        //manca immagini
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
        Log.d("DEBUGGGGGGGGGGGGGGGGGGG",dateTag.getTextContent());
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
}
