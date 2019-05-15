package com.example.android.illon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.SeekBar;
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
    private Lot current_lot;
    private LinearLayout containter;
    private String provenienza;

    /**
     * si salva tutti gli elementi del layout
     * prende l'utente dalla precedente activity
     * pulsante che chiama launchUserActivity
     * pulsante che invia la bid
     * currentlot DA SISTEMARE
     * chiama setLotView
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.current_lot);
        u = (User) getIntent().getSerializableExtra("User");
        current_lot = (Lot) getIntent().getSerializableExtra("Lot");
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
                setContentView(R.layout.loading_layout);
                launchUserActivity(u);
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

        retriveLot();
        if(current_lot==null || System.currentTimeMillis()-current_lot.getStart_time().getTime()>600000) {
            Log.d("BO", "onCreate: SETLOT");
            DeleteCacheDir.deleteDir(getCacheDir());
            u=UserFactory.getUser(u.getName());
            current_lot = setLot();
        }

        setLotView(current_lot);
        Log.d("IDLOTTO", "onCreate: "+current_lot.getId());
        Log.d("IDUTENTE", "onCreate: "+u.getId());
    }

    /**
     * prende l'offerta e se è accettabile (>bestBid e  < moneyUtente) fa la richiesta http
     * ricarica l'activity terminata la connessione
     */
    private void sendBid() {
        String s[] = new String[2];

        Pair <Integer, InputStream> read_response =  null;
        //prende il numero
        int offerta = -1;
        if(enterBid.getText().toString().length()>0) {
            offerta = Integer.parseInt(enterBid.getText().toString());
        }
        if((current_lot.getValue() != -1 ? offerta>current_lot.getValue() : offerta>=current_lot.getMin_value()) && u.getMoney()>=offerta) {
            Connection c = new Connection();
            s[0] = urlBidButton+"?bid_user="+u.getId()+"&bid_lot="+current_lot.getId()+"&bid_value="+offerta;
            s[1] = "bid/create.php";
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
                current_lot.setValue(offerta);
                minBid.setText("Min bid: " + current_lot.getValue());
                yourBid.setText("Your bid: "+current_lot.getValue());
            } else if(read_response.first==200) {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = null;
                try {
                    db = dbf.newDocumentBuilder();
                } catch (ParserConfigurationException e) {
                    Log.d("LOT:Eccezione","ParserConfigurationException");
                }
                Document file_read;
                try {
                    Toast.makeText(this,"Soomebody already made a better or equivalent bid",Toast.LENGTH_SHORT).show();
                    file_read = db.parse(read_response.second);

                    int v = Integer.parseInt(file_read.getElementsByTagName("bid_value").item(0).getTextContent());
                    current_lot.setValue(v);
                    minBid.setText("Min bid: "+v);
                } catch (IOException ex) {
                    Log.d("LOT:Eccezione","IOException");
                } catch (SAXException ex) {
                    Log.d("LOT:Eccezione","SAXException");
                }
            } else {
                Toast.makeText(this,"An error uccurred while creating bid "+read_response.first,Toast.LENGTH_SHORT).show();
                Toast.makeText(this,""+s[0],Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LotActivity.class);
                intent.putExtra("User", u);
                finish();
                overridePendingTransition(0, 0);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            c.disconnect();
        } else  {
            Toast.makeText(this,"Bid too small",Toast.LENGTH_SHORT).show();
        }
    }

    private void cacheLot() {
        //----------caching--------------------
        File dir = getCacheDir();
        File fileLot = new File(dir.getAbsolutePath(),"fileLot.txt");
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(fileLot);
            ObjectOutputStream oos = new ObjectOutputStream(fOut);
            oos.writeObject(current_lot);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //--------------------------------------
    }

    private void retriveLot() {
        File dir = getCacheDir();
        File file = new File(dir,"fileLot.txt");
        if(file.exists()) {
            try {
                FileInputStream fIn = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fIn);
                current_lot = (Lot) ois.readObject();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            current_lot = null;
        }
    }

    /**
     * imposta la schermata
     * es. visualizza i soldi dell'utente, il countDownTimer (DA SISTEMARE)
     * YOURBID DA SISTEMARE
     * scarica e mostra le immagini
     * @param l
     */
    public void setLotView(Lot l){
        illonImage.setImageDrawable(getDrawable(R.drawable.illon_logo));
        //ID E NOME DEL LOTTO
        lot.setText("LOT #"+l.getId());
        lotName.setText(l.getName());

        //tRimanente
        long millsStartTime = l.getStart_time().getTime();
        millsStartTime += 600000;
        millsRimanenti = millsStartTime - Calendar.getInstance().getTime().getTime() + 2000;
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
                u=UserFactory.getUser(u.getName());
                money.setText("Money: "+u.getMoney());
                yourBid.setText("Your bid: X");
                DeleteCacheDir.deleteDir(getCacheDir());
                dialogFine();
            }
        }.start();

        //ABOUT
        about.setText("About: "+l.getAbout());

        //MINBID
        if(l.getValue()!=-1) {
            if(u.getMyBid()>l.getValue()) {
                minBid.setText("Min bid: " + u.getMyBid());
            } else minBid.setText("Min bid: " + l.getValue());
        } else {
            if(u.getMyBid()>l.getMin_value()) {
                minBid.setText("Min bid: " + u.getMyBid());
            } else minBid.setText("Min bid: " + l.getMin_value());
        }
        //deve essere null finchè non viene fatta la prima bid
        //yourBid.setText("Your bid: "+l.getValue());
        //YOURBID --> DA SISTEMARE
        if(u.getMyBid() == -1)
            yourBid.setText("Your bid: X");
        else
            yourBid.setText("Your bid: "+u.getMyBid());

        //IMMAGINI
        ArrayList<Bitmap> imgs = l.getImages();
        LayoutInflater inflater = LayoutInflater.from(this);
        for(int i=0;i<imgs.size();i++) {
            View view = inflater.inflate(R.layout.item_image, containter, false);
            ImageView iv = view.findViewById(R.id.imageList);
            iv.setPadding(0,0,25,0);
            if(imgs.get(i) != null) {
                iv.setImageBitmap(imgs.get(i));
            } else {
                Log.d("ERROR", "setLotView: ");
            }
            containter.addView(view);
        }
    }

    /**
     * dialog che dovrebbe apparire alla fine del countDownTimerm chiama getNomeVincitore per sapere chi ha vinto l'asta
     * premuto su ok si ricarica l'activity
     * DA SISTEMARE (Crasha al momento di aprire il dialog)
     */
    private void dialogFine() {
        String nomeUtenteVincitore = getNomeVincitore();
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Intent intent = getIntent();
                intent.putExtra("User", u);
                current_lot = null;
                finish();
                overridePendingTransition(0, 0);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
        alertDialog.setTitle("L'asta è terminata!");
        alertDialog.setMessage(nomeUtenteVincitore);
        alertDialog.setButton(
                DialogInterface.BUTTON_NEUTRAL,
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = getIntent();
                        intent.putExtra("User", u);
                        current_lot = null;
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    }
                });
        alertDialog.show();
    }

    /**
     * richiesta http per ottenere il lotto corrente, per prendere il lot_winner (id)
     * richiesta http per ottenere il nome del vincitore dal suo id
     * DA SISTEMARE
     * @return il nome del vincitore
     */
    private String getNomeVincitore() {
        Connection c = new Connection();
        String[] s = new String[2];
        s[0] = "http://164.132.47.236/illon/illon_api/lot/read_one.php?lot_id="+current_lot.getId();
        s[1] = "lot/read_one.php";
        Pair <Integer, InputStream> read_response =  null;
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
            Document file_read_name;
            try {
                file_read = db.parse(read_response.second);
                String winner = file_read.getElementsByTagName("lot_winner").item(0).getTextContent();
                if(winner.equals("NULL")) {
                    return "Nessun vincitore";
                } else  {
                    Pair <Integer, InputStream> read_response_name =  null;
                    Connection c2 = new Connection();
                    s[0] = "http://164.132.47.236/illon/illon_api/user/read_one_id.php?user_id="+winner;
                    s[1] = "user/read_one_id.php";
                    read_response_name = c2.execute(s).get();
                    if(read_response_name.first==200) {
                        file_read_name = db.parse(read_response_name.second);
                        return "Il vincitore è "+file_read_name.getElementsByTagName("user_name").item(0).getTextContent();
                    }
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
        }
        return null;
    }

    /**
     * apre acivity UserActivity passandogli oggetti USER E LOTTO
     * @param u
     */
    public void launchUserActivity(User u) {
        cacheLot();
        Log.d("LOT", "APRO LAYOUT USER");
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra("User", u);
        startActivity(intent);
    }

    /**
     * aggiorna la textview del countDown
     * @param millis
     */
    public void updateTimer (long millis) {
        int minutes = (int) (millis/60000);
        int seconds = (int) (millis%60000/1000);
        String timeLeftText=minutes+":";
        if(seconds<10) timeLeftText+="0";
        timeLeftText += seconds;
        tRimanente.setText(timeLeftText);
    }

    /**
     * crea oggetto lotto se non è recuperabile dall'intent
     * @return oggetto Lot
     */
    public Lot setLot(){
        Lot l;
        String s[] = new String[2];
        s[0] = url;
        s[1] = "lot/read_current.php";
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
                l = parserXMLtoLot(file_read);
                l.setImages(downloadImages(l.getId()));
                return l;
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

    /**
     * parsa l'xml per creare l'oggetto lotto
     * @param file
     * @return Lot
     */
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

    /**
     * richiesta http per ricevere le path delle immagini del lotto poi scarico ogni immagine
     * @param id
     * @return arrayList di Bitmap delle immagini scaricate
     */
    public ArrayList<Bitmap> downloadImages(int id) {
        //IMMAGINI
        ArrayList<Bitmap> imgs = new ArrayList<>();
        ArrayList<String> imageUrls =new ArrayList();
        String s[] = new String[2];
        s[0] = urlPic+"?pic_lot="+id;
        s[1] = "pic/read.php";
        Connection c = new Connection();
        Pair <Integer, InputStream> read_response =  null;
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
                Log.d("LOT:Eccezione", "ParserConfigurationException");
            }
            Document file_read;
            try {
                file_read = db.parse(read_response.second);
                parserXMLtoImages(imageUrls, file_read);
                for (int i = 0; i < imageUrls.size(); i++) {
                    imgs.add(new DownloadImageFromInternet().execute(imageUrls.get(i)).get());
                }
            } catch (IOException ex) {
                Log.d("LOT:Eccezione", "IOException");
            } catch (SAXException ex) {
                Log.d("LOT:Eccezione", "SAXException");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return imgs;
    }

    /**
     *
     * @param imageUrls document path
     * @param file
     */
    private void parserXMLtoImages(ArrayList<String> imageUrls, Document file) {
        NodeList nl = file.getElementsByTagName("pic");
        for(int i=0;i<nl.getLength();i++) {
            Element e = (Element) nl.item(i);
            String p = e.getElementsByTagName("pic_path").item(0).getTextContent();
            imageUrls.add(p);
        }
    }

    @Override
    public void onBackPressed() {}
}
