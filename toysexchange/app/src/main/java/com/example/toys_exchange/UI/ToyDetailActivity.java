package com.example.toys_exchange.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.auth.AuthUser;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Account;
import com.amplifyframework.datastore.generated.model.Notification;
import com.amplifyframework.datastore.generated.model.Toy;
import com.amplifyframework.datastore.generated.model.UserWishList;
import com.example.toys_exchange.Firebase.FcnNotificationSender;
import com.example.toys_exchange.PaymentActivity;
import com.example.toys_exchange.R;
import com.example.toys_exchange.UI.data.model.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ToyDetailActivity extends AppCompatActivity {

    private static final String TAG = ToyDetailActivity.class.getSimpleName();
    private TextView toyName;
    private TextView toyDescription;
    private TextView toyUser;
    private TextView toyCondition;
    private TextView contactMe;
    private TextView toyPrice;
    private TextView toyType;

    private ImageView addToWishList;
    private ImageView toyImage;




    private Handler handler;
    private Handler handler1;
    private Handler handler2;


    private String userId;
    private String toyId;
    private String loggedAccountId;
    private String idCognito;

    private URL url;
    private String test;

    private int count=0;

    ImageView ivFavourite;
    ImageView ivDislike;
    ImageView removeFromWishList;
    CircleImageView ivProfileImage;
    TextView tvName;
    TextView btnBuyNow;

    String acc_id;

    String userToyId;

    Double price;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shophop_activity_product_detail);



        handler=new Handler(Looper.getMainLooper(), msg->{
            loggedAccountId=msg.getData().get("loggedUser").toString();
            idCognito=msg.getData().get("idCognito").toString();
            return true;
        });

        handler1=new Handler(Looper.getMainLooper(), msg->{
            tvName.setText(msg.getData().get("username").toString());
            if(msg.getData().get("userImage") != null){
                getUserImage(msg.getData().get("userImage").toString());
            }

            return true;
        });


        CollapsingToolbarLayout collapsingToolBar = findViewById(R.id.toolbar_layout);

        Toolbar toolBar = findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent toyIntent = getIntent();
        String description= toyIntent.getStringExtra("description");
         price= toyIntent.getDoubleExtra("price",0.0);
        String condition= toyIntent.getStringExtra("condition");
        String type= toyIntent.getStringExtra("toyType");
        String image = toyIntent.getStringExtra("image");
        String contactInfo= toyIntent.getStringExtra("contactInfo");
        toyId = toyIntent.getStringExtra("toyId");


        toyDescription=findViewById(R.id.tvUserName);
        toyDescription.setText(description);

        toyCondition=findViewById(R.id.tvCondition);
        toyCondition.setText(condition);

        contactMe=findViewById(R.id.tvContact);
        contactMe.setText(contactInfo);

        toyType=findViewById(R.id.tvType);
        toyType.setText(type);

        toyPrice=findViewById(R.id.tvPrice);
        toyPrice.setText(price +" Jd");

        tvName = findViewById(R.id.tvName);



        addToWishList=findViewById(R.id.ivFavourite);
        removeFromWishList=findViewById(R.id.ivDislike);
        toyImage=findViewById(R.id.productViewPager);

        userToyId = toyIntent.getStringExtra("userToyId");
        getUserName(userToyId);


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        userId =  sharedPreferences.getString(LoginActivity.USERNAME, "");

        collapsingToolBar.setTitle(toyIntent.getStringExtra("toyName"));

        getLoggedInAccount();

        getUrl(image);
        ivFavourite = findViewById(R.id.ivFavourite);
//        ivDislike = findViewById(R.id.ivDislike);

        addToWishList.setOnClickListener(view -> {
            if(count == 0){
                addToWish();
                addToWishList.setImageDrawable(getDrawable(R.drawable.shophop_ic_heart_fill));
                addToWishList.setBackground(getDrawable(R.drawable.shophop_bg_circle_primary_light));
                count++;
            }else {
                removeFromWishList();
                addToWishList.setImageDrawable(getDrawable(R.drawable.shophop_ic_heart));
                addToWishList.setBackground(getDrawable(R.drawable.shophop_bg_circle));
                count--;

            }
        });

        btnBuyNow = findViewById(R.id.btnBuyNow);
        btnBuyNow.setOnClickListener(view->{
            Intent intent = new Intent(this, PaymentActivity.class);

            intent.putExtra("toyId",toyId);
            startActivity(intent);
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void firebaseAction() {

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

                                                            if(!noti.getAccountid().equals(acc_id)) {
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

                        Log.d("TOKEN", token);
                        Toast.makeText(ToyDetailActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                });

        FirebaseMessaging.getInstance().subscribeToTopic("all");

        FcnNotificationSender notificationSender = new FcnNotificationSender("Hello", "your Toy is sold", getApplicationContext(), ToyDetailActivity.this,"dz3rZETJS6evPSjWTLynSU:APA91bHg3EPti8H_CiKGlR7p9ETJcvoK8yXJKEWDj_Idimn73TxKhTcu6_O2SqPhwFv8f8qpbsGPi2xVd66hiyvz_Z7jOOAWKNa-lr1h0PV9Oi9DNlSSmXQhcKk5qMgk1iLbF9FQxZYz");
        notificationSender.SendNotifications();
    }

    public void getUserName(String id){
        Amplify.API.query(
                ModelQuery.list(Account.class),
                accounts -> {
                  //  Log.i(TAG, "getUserName: -----------------------------------<>"+accounts.getData());
                    for (Account user :
                            accounts.getData()) {
                        if (user.getId().equals(id)) {
                            Bundle bundle=new Bundle();
                            bundle.putString("username",user.getUsername());
                            bundle.putString("userImage",user.getImage());

                            Message message = new Message();
                            message.setData(bundle);

                            handler1.sendMessage(message);
                        }
                    }
                },
                error -> Log.e(TAG, error.toString(), error)
        );
    }

    public void addToWish(){
        Amplify.API.query(
                ModelQuery.list(Toy.class,Toy.ID.eq(toyId)),
                toys -> {
                    for (Toy toy :
                            toys.getData()) {
                              Amplify.Auth.fetchUserAttributes(
                                 attributes -> {
                                   String id=attributes.get(0).getValue();
                                       Amplify.API.query(
                                            ModelQuery.list(Account.class),
                                                       accounts -> {
                                                              for (Account user :
                                                                  accounts.getData()) {
                                                          if (user.getIdcognito().equals(id)) {
                                                              runOnUiThread(()->{
                                                                  UserWishList wishList=UserWishList.builder().toy(toy)
                                                                          .account(user).build();
                                                                  // API save to backend
                                                                  Amplify.API.mutate(
                                                                          ModelMutation.create(wishList),
                                                                          success -> {
                                                                              Log.i(TAG, "Saved item API: addToWish " + success.getData());
                                                                          },
                                                                          error -> Log.e(TAG, "Could not save item to API addToWish", error)
                                                                  );
                                                              });
                        }

                    }

                },
                error -> Log.e(TAG, error.toString(), error)
        );

    },
    error -> Log.e(TAG, "Failed to fetch user attributes.", error)
            );


                    }
                },
                error -> Log.e(TAG, error.toString(), error)
        );
    }

    public void removeFromWishList(){
        Amplify.API.query(
                ModelQuery.list(UserWishList.class),
                wishList -> {
                    for (UserWishList wishToy :
                            wishList.getData()) {
                        Amplify.Auth.fetchUserAttributes(
                                attributes -> {
                                    String id=attributes.get(0).getValue();

                                    Amplify.API.query(
                                            ModelQuery.list(Account.class),
                                            accounts -> {
                                                if(Objects.equals(toyId, wishToy.getToy().getId())){

                                                    Amplify.API.mutate(ModelMutation.delete(wishToy),
                                                            response -> Log.i(TAG, "Todo with id: " + response.getData().getId()),
                                                            error -> Log.e(TAG, "Create failed", error)
                                                    );
                                                }
                                            },
                                            error -> Log.e(TAG, error.toString(), error)
                                    );

                                },
                                error -> Log.e(TAG, "Failed to fetch user attributes.", error)
                        );


                    }
                },
                error -> Log.e(TAG, error.toString(), error)
        );

    }


    public void getLoggedInAccount(){
        Amplify.Auth.fetchUserAttributes(
                attributes -> {
                    Amplify.API.query(
                            ModelQuery.list(Account.class),
                            accounts -> {
                                for (Account user :
                                        accounts.getData()) {
                                    if (user.getIdcognito().equals(attributes.get(0).getValue())) {

                                        Amplify.API.query(
                                                ModelQuery.list(UserWishList.class),
                                                wishList -> {
                                                    if(wishList.hasData()){
                                                        for (UserWishList wishToy :
                                                                wishList.getData()) {
                                                            if(wishToy.getAccount().getId().equals(user.getId()) && wishToy.getToy().getId().equals(toyId)){

                                                               runOnUiThread(()->{
                                                                   addToWishList.setImageDrawable(getDrawable(R.drawable.shophop_ic_heart_fill));
                                                                   addToWishList.setBackground(getDrawable(R.drawable.shophop_bg_circle_primary_light));
                                                               });

                                                                count=1;

                                                            }

                                                        }
                                                    }
                                                },
                                                error -> Log.e(TAG, error.toString(), error)
                                        );
                                        Bundle bundle=new Bundle();

                                        bundle.putString("loggedUser",user.getId());
                                        bundle.putString("idCognito",  attributes.get(0).getValue());

                                        Message message = new Message();
                                        message.setData(bundle);

                                        handler.sendMessage(message);
                                    }
                                }
                            },
                            error -> Log.e(TAG, error.toString(), error)
                    );
                },
                error -> Log.e(TAG, "Failed to fetch user attributes.", error)
        );
    }

    private void getUrl(String image){
        Amplify.Storage.getUrl(
                image,
                result -> {
                    runOnUiThread(()->{
                        Picasso.get().load(result.getUrl().toString()).into(toyImage);
                    });
                },
                error -> Log.e("MyAmplifyApp", "URL generation failure", error)
        );
    }

    private void getUserImage(String image){

        ivProfileImage = findViewById(R.id.ivProfileImage);
        Amplify.API.query(
                ModelQuery.get(Account.class,userToyId),
                user -> {
                    runOnUiThread(()->{
                        Amplify.Storage.getUrl(
                                image,
                                result -> {
                                    runOnUiThread(()->{
                                        Picasso.get().load(result.getUrl().toString()).into(ivProfileImage);
                                    });
                                },
                                error -> Log.e("MyAmplifyApp", "URL generation failure", error)
                        );
                    });
                },
                error -> Log.e("Adaptor", error.toString(), error)
        );
    }
}