package com.example.android.illon;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class UserActivity extends AppCompatActivity {
    private User u;
    private TextView user_name,stats;
    private ListView wonList;
    private Button back;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_layout);

        u = (User) getIntent().getSerializableExtra("User");
        user_name = (TextView) findViewById(R.id.user_name);
        stats = (TextView) findViewById(R.id.stats);
        wonList = (ListView) findViewById(R.id.wonlist);
        back = (Button) findViewById(R.id.back);
        fab = (FloatingActionButton) findViewById(R.id.fab);

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
    }

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
