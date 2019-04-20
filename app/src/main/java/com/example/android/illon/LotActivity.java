package com.example.android.illon;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class LotActivity extends Activity {
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

        money.setText("Money: "+u.getMoney());
        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchLotActivity(u);
            }
        });
    }

    public void launchLotActivity(User u) {
        Log.d("LOT", "APRO LAYOUT USER");
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra("User", u);
        startActivity(intent);
    }
}
