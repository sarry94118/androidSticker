package com.android.sticker;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class SendHistoryActivity extends AppCompatActivity {
    private String username;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_history);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");
        }
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Get sent data from database
        mDatabase.child("messages").orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Integer> sentCountMap = new HashMap<>();

                // get sent data and count sent time for each sticker
                for (DataSnapshot kv : dataSnapshot.getChildren()) {
                    if (kv.child("sender").getValue(String.class).equals(username)) {
                        String stickerId = kv.child("sticker").getValue(String.class);
                        Integer previousCount = sentCountMap.containsKey(stickerId) ?
                                sentCountMap.get(stickerId) : 0;
                        sentCountMap.put(stickerId, previousCount + 1);
                    }
                }

                // set count of sent times for each sticker in the textViews
                for (String stickerId : sentCountMap.keySet()) {
                    String countResult = sentCountMap.get(stickerId).toString();

                    TextView textView = null;
                    switch (stickerId) {
                        case "emoji1":
                            textView = findViewById(R.id.emoji1Count);
                            break;
                        case "emoji2":
                            textView = findViewById(R.id.emoji2Count);
                            break;
                        case "emoji3":
                            textView = findViewById(R.id.emoji3Count);
                            break;
                        case "emoji4":
                            textView = findViewById(R.id.emoji4Count);
                            break;
                        case "emoji5":
                            textView = findViewById(R.id.emoji5Count);
                            break;
                        case "emoji6":
                            textView = findViewById(R.id.emoji6Count);
                            break;
                        default:
                            break;
                    }
                    if (textView != null) {
                        textView.setText(countResult);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

}
