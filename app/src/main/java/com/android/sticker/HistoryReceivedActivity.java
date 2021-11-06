package com.android.sticker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class HistoryReceivedActivity extends AppCompatActivity {
    private TextView tvReceiveHistoryResult, tvUsername;
    private String username, deviceToken;
    private DatabaseReference mDatabase;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_received);

        tvReceiveHistoryResult = findViewById(R.id.tv_receive_history_result);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");
            deviceToken = extras.getString("deviceToken");
        }

        // Set username textview
        tvUsername = findViewById(R.id.tv_username);
        tvUsername.setTextColor(Color.rgb(69,139,230));
        tvUsername.setText("Username: " + username);

        // Retrieve data of receive history from Firebase
        mDatabase.child("messages").orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String result = "";
                for (DataSnapshot kv : dataSnapshot.getChildren()) {
                    if (kv.child("receiver").getValue(String.class).equals(username)) {
                        String sender = kv.child("sender").getValue(String.class);
                        String time = formatTimestamp(kv.child("time").child("timestamp").getValue(Long.class));
                        String sticker = kv.child("sticker").getValue(String.class);
                        result += "\n You received " + sticker + " from user " + sender + " at " + time + "\n";
                    }
                }
                tvReceiveHistoryResult.setTextSize(16);
                tvReceiveHistoryResult.setText(result);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }


    // Code reference: https://stackoverflow.com/questions/41139218/from-timestamp-to-date-android
    private String formatTimestamp(long timeStamp) {
        Date date = new Date(timeStamp); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a, EEE, dd MMM yyyy"); // the format of your date
        sdf.setTimeZone(TimeZone.getTimeZone("GMT-7"));
        return sdf.format(date);
    }



}