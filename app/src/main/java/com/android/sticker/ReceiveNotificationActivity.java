package com.android.sticker;

import android.os.Bundle;
import android.widget.ImageView;
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


        ImageView imageView = (ImageView) findViewById(R.id.notificationImageView);;

        switch (sticker) {
            case "emoji1":
                imageView.setImageResource(R.drawable.emoji1);
                break;
            case "emoji2":
                imageView.setImageResource(R.drawable.emoji2);
                break;
            case "emoji3":
                imageView.setImageResource(R.drawable.emoji3);
                break;
            case "emoji4":
                imageView.setImageResource(R.drawable.emoji4);
                break;
            case "emoji5":
                imageView.setImageResource(R.drawable.emoji5);
                break;
            case "emoji6":
                imageView.setImageResource(R.drawable.emoji6);
                break;
            default:
                break;
        }

        receiveNotificationText.setText(sender + " sent you the emoji sticker: " + sticker);
    }
}
