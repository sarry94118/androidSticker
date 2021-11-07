package com.android.sticker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class AfterLogInActivity extends AppCompatActivity {
    private static final String TAG = "AfterLoginActivity";
    private TextView textView4, textView11, textView12;
    private DatabaseReference mDatabase;
    private String username;
    private String deviceToken;
    private String targetToken;
    private Button button3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_login);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");
            deviceToken = extras.getString("deviceToken");
        }

        textView4 = findViewById(R.id.textView4);
        textView11 = findViewById(R.id.textView11);
        textView12 = findViewById(R.id.tv_selected_sticker);
        button3 = findViewById(R.id.btn_send);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        textView4.setText("Username: " + username);

        // Download stickers from database
        mDatabase.child("stickers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        // Get special notifications during the time when the username is previously dissociated with a token and get associated again
        mDatabase.child("messages").orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> keyArray = new ArrayList<>();
                for (DataSnapshot kv : dataSnapshot.getChildren()) {
                    if (kv.child("offline").getValue(boolean.class) && kv.child("receiver").getValue(String.class).equals(username)) {
                        String sender = kv.child("sender").getValue(String.class);
                        String sticker = kv.child("sticker").getValue(String.class);
//                        sendNotification(sender, sticker);
                        keyArray.add(kv.getKey());
                    }
                }
                // Update the offline boolean label of the message from the database after being notified
                for (int i = 0; i < keyArray.size(); i++) {
                    mDatabase.child("messages").child(keyArray.get(i)).child("offline").setValue(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void selectSticker(View view) {
        final EditText receiverEditText = findViewById(R.id.et_receiver_name);
        if (receiverEditText.equals("")) {
            Toast.makeText(this, "Username can't be empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (view.getId() == R.id.emoji1checkBox) {
            textView12.setText("emoji1");
        } else if (view.getId() == R.id.emoji2checkBox) {
            textView12.setText("emoji2");
        } else if (view.getId() == R.id.emoji3checkBox) {
            textView12.setText("emoji3");
        } else if (view.getId() == R.id.emoji4checkBox) {
            textView12.setText("emoji4");
        } else if (view.getId() == R.id.emoji5checkBox) {
            textView12.setText("emoji5");
        }else if (view.getId() == R.id.emoji6checkBox) {
            textView12.setText("emoji6");
        }
    }

    public void reset(View view) {
        textView12.setText("");
    }

    public void sendSticker(View view) {
        final EditText receiverEditText = findViewById(R.id.et_receiver_name);
        final String receiver = receiverEditText.getText().toString().trim();
        final String candidate = textView12.getText().toString();

        if (receiver.equals("") || candidate.equals("")) {
            Toast.makeText(this, "Username and sticker can't be empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        // Reset input EditText as empty after button click
        receiverEditText.setText("");

        // Disable button before sending is done
        button3.setEnabled(false);
        textView11.setText("Sending to " + receiver);

        // Check the existence of the receiver
        mDatabase.child("users").child(receiver).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check the existence of the receiver
                if (!dataSnapshot.exists()) {
                    Toast.makeText(AfterLogInActivity.this, "Receiver " + receiver + " does not exist!", Toast.LENGTH_SHORT).show();
                } else {
                    targetToken = dataSnapshot.child("token").getValue(String.class);

                    // Send and save message to the database
                    DatabaseReference pushMessagesRef = mDatabase.child("messages").push();
                    class Message {
                        public String receiver;
                        public String sender;
                        public String sticker;
                        public Map time;
                        public boolean offline;

                        public Message(String receiver, String sender, String sticker, Map time, boolean offline) {
                            this.receiver = receiver;
                            this.sender = sender;
                            this.sticker = sticker;
                            this.time = time;
                            this.offline = offline;
                        }
                    }
                    Map time = new HashMap();
                    time.put("timestamp", ServerValue.TIMESTAMP);

                    // Upload to the database
                    if (!targetToken.equals("offline")) {
                        // Send notification via FCM if the receiver has a token
                        pushMessagesRef.setValue(new Message(receiver, username, candidate, time, false));
                        fcmSend(username, targetToken, candidate);
                    } else {
                        pushMessagesRef.setValue(new Message(receiver, username, candidate, time, true));
                    }

                }
                button3.setEnabled(true);
                textView11.setText("Click one emoji to pick. Click \"Reset\" to cancel.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    // Code referenced from Dr. Dan Feinberg's sample code this week
    public void fcmSend(final String sender, final String targetToken, final String sticker) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject jPayload = new JSONObject();
                JSONObject jNotification = new JSONObject();
                JSONObject jdata = new JSONObject();
                try {
                    final String SERVER_KEY = "key=AAAA7xASAtE:APA91bHBsTH0mTra96iS8XlxrITX-kBKHUYEu9VzifoT2OVpsouZYAlN1XRq-QcDvv7GU8urCY3jYoy-b-k9x7QVJodXfiVptFRgOn3SbIT7RR2jZxDis3898jF8S8FVxenIc1RaQMLM";
                    jNotification.put("title", "Emoji Sticker Received");
                    jNotification.put("body", sender + " sent you the emoji sticker: " + sticker);
                    jNotification.put("sound", "default");
                    jNotification.put("badge", "1");
                    jNotification.put("click_action", "OPEN_ACTIVITY_1");

                    jdata.put("sender", sender);
                    jdata.put("sticker", sticker);

                    jPayload.put("to", targetToken);
                    jPayload.put("priority", "high");
                    jPayload.put("notification", jNotification);
                    jPayload.put("data", jdata);

                    URL url = new URL("https://fcm.googleapis.com/fcm/send");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Authorization", SERVER_KEY);
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    OutputStream outputStream = conn.getOutputStream();
                    outputStream.write(jPayload.toString().getBytes());
                    outputStream.close();

                    InputStream inputStream = conn.getInputStream();
                    final String resp = convertStreamToString(inputStream);
                    Handler h = new Handler(Looper.getMainLooper());

                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AfterLogInActivity.this, resp, Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (JSONException | IOException e) {
                    Toast.makeText(AfterLogInActivity.this, "FCM failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).start();
    }

    // Direct to sent history activity
    public void sendHistory(View view) {
        Intent intent = new Intent(this, SendHistoryActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("deviceToken", deviceToken);
        startActivity(intent);
    }

    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next().replace(",", ",\n") : "";
    }

    // Direct to Received History activity
    public void openReceiveHistoryActivity(View view) {
        Intent intent = new Intent(this, HistoryReceivedActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("deviceToken", deviceToken);
        startActivity(intent);
    }
}
