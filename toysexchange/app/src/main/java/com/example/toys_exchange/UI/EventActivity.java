package com.example.toys_exchange.UI;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.auth.AuthUser;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Account;
import com.amplifyframework.datastore.generated.model.Event;
import com.amplifyframework.datastore.generated.model.Notification;
import com.amplifyframework.datastore.generated.model.Store;
import com.example.toys_exchange.Firebase.FcnNotificationSender;

import com.example.toys_exchange.MainActivity;
import com.example.toys_exchange.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

public class EventActivity extends AppCompatActivity {

    private static final String TAG = EventActivity.class.getSimpleName() ;

    Button btnSubmit;
    Button cancelAdd;

    TextView location;
    EditText eventDescription;
    EditText eventTitle;

    String eventDescriptionText;
    String eventTitleText;

    String userId;

    Double longitude;
    Double latitude;
    String acc_id;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toyexchange_activity_add_event);

        Toolbar toolBar = findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add Event");

         eventDescription = findViewById(R.id.etDescription);
         eventTitle = findViewById(R.id.etTitle);

         eventDescriptionText = eventDescription.getText().toString();
         eventTitleText = eventTitle.getText().toString();

        location=findViewById(R.id.tvLocation);

        cancelAdd = findViewById(R.id.btnCancel);

        handler = new Handler(Looper.getMainLooper(), msg -> {
            // get the username
            String user = msg.getData().getString("name");
            //get the user Id
            userId =  msg.getData().getString("Id");
            return true;

        });
        authAttribute(); //get the username and userID

        btnSubmit = findViewById(R.id.btnSubmit);

        addBtnListener(); // Listeners

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(getApplicationContext(),MapActivity.class);
                intent.putExtra("type","event");
                intent.putExtra("title",eventTitle.getText().toString());
                intent.putExtra("desc",eventDescription.getText().toString());
                startActivity(intent);
            }
        });
        
    }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void authAttribute(){
        Amplify.Auth.fetchUserAttributes(
                attributes -> {
                    //  Send message to the handler to get the user Id >>
                    Bundle bundle = new Bundle();
                    bundle.putString("name",  attributes.get(2).getValue());
                    bundle.putString("Id",  attributes.get(0).getValue());

                    Message message = new Message();
                    message.setData(bundle);

                    handler.sendMessage(message);
                },
                error -> Log.e(TAG, "Failed to fetch user attributes.", error)
        );
    }

    private void addBtnListener() {

        btnSubmit.setOnClickListener(view -> {

            eventDescriptionText = eventDescription.getText().toString();
            eventTitleText = eventTitle.getText().toString();

            if(eventDescriptionText.length()>0 && eventTitleText.length()>0 && longitude!=0.0 & latitude!=0.0){
                Amplify.API.query(
                        ModelQuery.list(Account.class),
                        users -> {
                            if(users.hasData()) {
                                for (Account user :
                                        users.getData()) {
                                    if (user.getIdcognito().equals(userId)) {
                                        Event event;
                                        if(longitude!=null && latitude!=null){
                                            event = Event.builder()
                                                    .title(eventTitleText)
                                                    .eventdescription(eventDescriptionText)
                                                    .latitude(latitude)
                                                    .longitude(longitude)
                                                    .accountEventsaddedId(user.getId())
                                                    .build();
                                        }else {
                                            event = Event.builder()
                                                    .title(eventTitleText)
                                                    .eventdescription(eventDescriptionText)
                                                    .accountEventsaddedId(user.getId())
                                                    .build();

                                        }

                                        Amplify.API.mutate(
                                                ModelMutation.create(event),
                                                success -> {
                                                    Log.i(TAG, "Saved item API: " + success.getData());
                                                },
                                                error -> Log.e(TAG, "Could not save item to API", error)
                                        );
                                    }
                                }
                            }
                        },
                        error -> Log.e(TAG, error.toString(), error)
                );

                Toast.makeText(getApplicationContext(), "Event Added", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this,MainActivity.class));
            }else {
                if (!isFinishing()){
                    new AlertDialog.Builder(EventActivity.this)
                            .setTitle("Error")
                            .setMessage("you should add title and description and location ")
                            .setCancelable(false)
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Whatever...
                                    dialog.cancel();
                                }
                            }).show();
                }
            }

        });

        cancelAdd.setOnClickListener(view -> {
           startActivity(new Intent(this, MainActivity.class));
        });
    }


    @Override
    public void onResume() {
        super.onResume();

        Intent locationIntent=getIntent();
        longitude= locationIntent.getDoubleExtra("longitude",0.0);
        latitude= locationIntent.getDoubleExtra("latitude",0.0);
        eventTitle.setText(locationIntent.getStringExtra("title"));
        eventDescription.setText(locationIntent.getStringExtra("desc"));

    }


    public void firebaseAction()
    {

        AuthUser logedInUser = Amplify.Auth.getCurrentUser();
        String cognitoId = logedInUser.getUserId();

        FirebaseApp.initializeApp(this);
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        FirebaseMessaging.getInstance().subscribeToTopic("all");
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        // send it to API
                        Amplify.API.query(
                                ModelQuery.list(Account.class),
                                allUsers -> {
                                    for (Account userAc:
                                            allUsers.getData()) {
                                        if(userAc.getIdcognito().equals(cognitoId)){
                                            acc_id = userAc.getId();
                                            Notification notification = Notification.builder()
                                                    .tokenid(token)
                                                    .accountid(acc_id)
                                                    .build();
                                            Amplify.API.query(
                                                    ModelQuery.list(Notification.class),
                                                    notify -> {
                                                        for (Notification noti:
                                                                notify.getData()) {
                                                            if(!noti.getAccountid().equals(acc_id)){
                                                                Amplify.API.mutate(
                                                                        ModelMutation.create(notification),
                                                                        success -> {
                                                                            Log.i(TAG, "Saved item API: " + success.getData());
                                                                        },
                                                                        error -> Log.e(TAG, "Could not save item to API", error)
                                                                );

                                                            }

                                                        }

                                                    },
                                                    error -> Log.e(TAG, error.toString(), error)
                                            );
                                        }
                                    }
                                },
                                error -> Log.e(TAG, error.toString(), error)
                        );

                        Toast.makeText(EventActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                });

        FirebaseMessaging.getInstance().subscribeToTopic("all");

        FcnNotificationSender notificationSender = new FcnNotificationSender("Hello", "your Toy is sold", getApplicationContext(), EventActivity.this,"dz3rZETJS6evPSjWTLynSU:APA91bHg3EPti8H_CiKGlR7p9ETJcvoK8yXJKEWDj_Idimn73TxKhTcu6_O2SqPhwFv8f8qpbsGPi2xVd66hiyvz_Z7jOOAWKNa-lr1h0PV9Oi9DNlSSmXQhcKk5qMgk1iLbF9FQxZYz");
        notificationSender.SendNotifications();
    }

}