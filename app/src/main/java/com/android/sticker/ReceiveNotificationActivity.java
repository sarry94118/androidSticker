package com.android.sticker;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ReceiveNotificationActivity extends AppCompatActivity {
    private String sender;
    private String sticker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_notification);

        TextView receiveNotificationText = findViewById(R.id.receiveNotificationText);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            sender = extras.getString("sender");
            sticker = extras.getString("sticker");
        }
        receiveNotificationText.setText(sender + " sent you the emoji sticker: " + sticker);
    }
}
