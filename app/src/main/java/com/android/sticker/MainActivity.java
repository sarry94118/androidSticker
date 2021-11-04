package com.android.sticker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {
    // variable for FirebaseAuth class

    private static final String TAG = MainActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    private TextView userNameEditText;
    private Button logoutButton;
    private TextView loginInfo;
    private TextView logoutMsg;
    private String deviceToken;
    private String username;
    private DatabaseReference database;

    private String curUsername = "";

    TextInputEditText editTextCountryCode, editTextPhone;
    AppCompatButton buttonContinue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userNameEditText = (TextView) findViewById(R.id.username);
//        logoutButton = findViewById(R.id.login);
        loginInfo = findViewById(R.id.loginInfo);
//        logoutMsg = findViewById(R.id.logoutMsg);
        database = accessDatabase();
        getToken();
    }

    private void getToken() {
        /***************************************************************************************
         *    source: https://firebase.google.com/docs/cloud-messaging/android/client
         ***************************************************************************************/
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    private static final String TAG = "Token";

                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        deviceToken = task.getResult();

                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, deviceToken);
                        Log.d(TAG, msg);
                        //  Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private DatabaseReference accessDatabase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Log.d("database", "Got database: " + databaseReference.toString());
        return databaseReference;
    }

    public void login(View view) {
        if (deviceToken == null) {
            Toast.makeText(this, "Fail to get device token", Toast.LENGTH_SHORT).show();
            return;
        }
        //get username
        username = userNameEditText.getText().toString().trim();
        curUsername = username;
        //check username
        if (username.equals("\\s+")) {
            Toast.makeText(this, "Please enter a valid username", Toast.LENGTH_SHORT).show();
            return;
        }

        if(username == null || username.equals("")){
            loginInfo.setText("Please enter a username to log in");
            return;
        }

        //set input view to empty
        //userNameEditText.setText("");

        //update token and user name in database
        database.child("users").child(username).child("token").setValue(deviceToken);

        // deal with same token but different username
        database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot kv : dataSnapshot.getChildren()) {
                    if (kv.child("token").getValue(String.class).equals(deviceToken) && !kv.getKey().equals(username)) {
                        database.child("users").child((kv.getKey())).child("token").setValue("offline");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        loginInfo.setText("Current user: " + curUsername);
        logoutMsg.setText("");

        //start after log in activity
//        Intent intent = new Intent(MainActivity.this, AfterLogInActivity.class);
//        intent.putExtra("username", username);
//        intent.putExtra("deviceToken", deviceToken);
//        startActivity(intent);

    }

    public void getLoginStatus(View view) {

        loginInfo.setText("No User");

        if(curUsername.equals("")){
            //  loginInfo.setText("Please log in");
            Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show();
            return;
        }
        loginInfo.setText("Current user: " + curUsername);
        logoutMsg.setText("");
    }

}