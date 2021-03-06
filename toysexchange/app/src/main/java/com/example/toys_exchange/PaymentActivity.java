package com.example.toys_exchange;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.auth.AuthUser;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Account;
import com.amplifyframework.datastore.generated.model.Notification;
import com.amplifyframework.datastore.generated.model.Toy;
import com.amplifyframework.datastore.generated.model.UserWishList;
import com.braintreepayments.cardform.view.CardForm;
import com.example.toys_exchange.Firebase.FcnNotificationSender;
import com.example.toys_exchange.UI.EventActivity;
import com.example.toys_exchange.UI.ToyDetailActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;


public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = PaymentActivity.class.getSimpleName();
    private TextView toyName;
    private TextView toyCost;
    private Button btn;

    String acc_id;
    Toy toy;

    String toyId;
    AlertDialog.Builder alertBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_payment);
        setContentView(R.layout.payment_card);


        Toolbar toolBar = findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Payment");

        Intent intent = getIntent();
        toyId = intent.getStringExtra("toyId");

        CardForm cardForm = findViewById(R.id.card_form);
        Button buy = findViewById(R.id.btnBuy);


        // https://www.codingdemos.com/android-credit-card-form-tutorial/
        cardForm.cardRequired(true)
                .expirationRequired(true)
                .cvvRequired(true)
                .postalCodeRequired(false)
                .mobileNumberRequired(true)
                .mobileNumberExplanation("SMS is required on this number")
                .setup(PaymentActivity.this);

        // Make the CVV number invisible
        cardForm.getCvvEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);


        getToy();

        buy.setOnClickListener(view->{
             if (cardForm.isValid()) {
               //  deleteToyFromAPI();
                 alertBuilder = new AlertDialog.Builder(PaymentActivity.this);
                 alertBuilder.setTitle("Confirm before purchase");
                 alertBuilder.setMessage("Card number: " + cardForm.getCardNumber() + "\n" +
                         "Card expiry date: " + cardForm.getExpirationDateEditText().getText().toString() + "\n" +
                         "Card CVV: " + cardForm.getCvv() + "\n" +
                         "Postal code: " + cardForm.getPostalCode() + "\n" +
                         "Phone number: " + cardForm.getMobileNumber());
                 alertBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         dialogInterface.dismiss();
                         // Delete From the Toys
                         deleteToyFromAPI();
                         Toast.makeText(PaymentActivity.this, "Thank you for purchase", Toast.LENGTH_LONG).show();
                         firebaseActionPay();
                         startActivity(new Intent(getApplicationContext(),MainActivity.class));
                     }
                 });
                 alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         dialogInterface.dismiss();
                     }
                 });
                 AlertDialog alertDialog = alertBuilder.create();
                 alertDialog.show();
             }else {
                 Toast.makeText(PaymentActivity.this, "Please complete the form", Toast.LENGTH_LONG).show();
             }
         });

    }

    public void getToy(){
        Amplify.API.query(
                ModelQuery.list(Toy.class),
                toys -> {
                    for (Toy toyRec :
                            toys.getData()) {
                        if (toyRec.getId().equals(toyId)) {
                            runOnUiThread(()->{
                                toy = toyRec;
                            });
                        }
                    }

                },
                error -> Log.e(TAG, error.toString(), error)
        );
    }

    public void deleteToyFromAPI() {
        Amplify.API.query(
                ModelQuery.list(UserWishList.class),
                toys -> {
                    if(toys.hasData()) {
                        for (UserWishList toyRec :
                                toys.getData()) {
                            if(toyRec.getToy().getId().equals(toyId))                                     {
                                Amplify.API.mutate(ModelMutation.delete(toyRec),
                                        response ->{
                                            Log.i(TAG, "Toy wishList deleted " + response);
                                        },
                                        error -> Log.e(TAG, "toy failed", error)
                                );
                            }
                        }
                    }

                    runOnUiThread(()->{
                        getToy();
                        Amplify.API.mutate(ModelMutation.delete(toy),
                                response ->{
                                    // https://www.youtube.com/watch?v=LQmGU3UCOPQ
                                    Log.i(TAG, "Toy deleted " + response);
                                    runOnUiThread(()->{
                                        Toast.makeText(getApplicationContext(), "Toy Deleted", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this,MainActivity.class));
                                    });
                                },
                                error -> Log.e(TAG, "delete failed", error)
                        );
                    });

                },
                error -> Log.e(TAG, error.toString(), error)
        );
    }



    public void firebaseActionPay()
    {

        FirebaseMessaging.getInstance().subscribeToTopic("all");

        FcnNotificationSender notificationSender = new FcnNotificationSender("Hello", "your Toy is sold", getApplicationContext(), PaymentActivity.this,"dz3rZETJS6evPSjWTLynSU:APA91bHg3EPti8H_CiKGlR7p9ETJcvoK8yXJKEWDj_Idimn73TxKhTcu6_O2SqPhwFv8f8qpbsGPi2xVd66hiyvz_Z7jOOAWKNa-lr1h0PV9Oi9DNlSSmXQhcKk5qMgk1iLbF9FQxZYz");
        notificationSender.SendNotifications();
    }

}