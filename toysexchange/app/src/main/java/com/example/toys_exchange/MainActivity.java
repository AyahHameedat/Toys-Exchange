package com.example.toys_exchange;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Toy;
import com.example.toys_exchange.UI.EventActivity;
import com.example.toys_exchange.UI.LoginActivity;
import com.example.toys_exchange.UI.data.model.EventDetailsActivity;
import com.example.toys_exchange.adapter.CustomToyAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private FloatingActionButton mAdd;
    private TextView mProfile;
    private FloatingActionButton mAddEvent;
    private FloatingActionButton mAddToy;

    private List<Toy> toyList = new ArrayList<>();
    private Handler handler;
    private Handler userHandler;

    private String userId;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button button = findViewById(R.id.addEvent);

        button.setOnClickListener(view -> {
            startActivity(new Intent(this, EventDetailsActivity.class));
        });

        mAdd = findViewById(R.id.add_fab);
        mAddEvent = findViewById(R.id.add_event);
        mAddToy = findViewById(R.id.add_toy);


        mAdd.setOnClickListener(view -> {
            if(mAddToy.getVisibility() != View.VISIBLE){
                mAddToy.setVisibility(View.VISIBLE);
                mAddEvent.setVisibility(View.VISIBLE);
            }else {
                mAddToy.setVisibility(View.GONE);
                mAddEvent.setVisibility(View.GONE);
            }
        });


        mAddEvent.setOnClickListener(view -> {
            startActivity(new Intent(this, EventActivity.class));
        });

    }

    @Override
    protected void onResume() {

        toyList =new ArrayList<>();

        handler = new Handler(Looper.getMainLooper(),msg ->{
            RecyclerView recyclerView = findViewById(R.id.recycler_view);

            GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2, LinearLayoutManager.VERTICAL,false);

            CustomToyAdapter customAdapter = new CustomToyAdapter(toyList, new CustomToyAdapter.CustomClickListener() {
                @Override
                public void onTaskClickListener(int position) {
                    Intent intent = new Intent(getApplicationContext(),EventActivity.class);
                    intent.putExtra("toyName",toyList.get(position).getToyname());
                    intent.putExtra("description",toyList.get(position).getToydescription());
                    intent.putExtra("image",toyList.get(position).getImage());
                    intent.putExtra("price",toyList.get(position).getPrice());
                    intent.putExtra("condition",toyList.get(position).getCondition());

                    authAttribute();

                    intent.putExtra("username", username);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                }
            });
            recyclerView.setAdapter(customAdapter);

            recyclerView.setHasFixedSize(true);

            recyclerView.setLayoutManager(gridLayoutManager);
            return  true;
        });


        Amplify.API.query(ModelQuery.list(Toy.class),success ->{

            for(Toy toy: success.getData()){
                Log.i("get toy ", toy.toString());
               toyList.add(toy);
            }

                    Bundle bundle =new Bundle();
                    bundle.putString("data", "done");

                    Message message = new Message();
                    message.setData(bundle);
                    handler.sendMessage(message);
        },error -> Log.e("error: ","-> ",error)
        );


        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                Toast.makeText(this, "Setting ", Toast.LENGTH_SHORT).show();
//                goToSetting();
                return true;
            case R.id.action_profile:
                Toast.makeText(this, "Copyright 2022 ", Toast.LENGTH_SHORT).show();
//                goToProfile();
                return true;
            case R.id.logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goToSetting(){}

    private void goToProfile(){
//        Intent profileIntent = new Intent(this,)
    }

    private void logout(){
        Amplify.Auth.signOut(()->{
            Log.i(TAG, "Signed out successfully");
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            authSession("logout");
            finish();
        },error -> Log.e(TAG, error.toString())
        );
    }

    private void authSession(String method){
        Amplify.Auth.fetchAuthSession(result ->{
            Log.i(TAG, "Auth Session => " + method + result.toString()) ;

            AWSCognitoAuthSession cognitoAuthSession = (AWSCognitoAuthSession) result;

            switch(cognitoAuthSession.getIdentityId().getType()) {
                case SUCCESS:
                {
                    Log.i("AuthQuickStart", "IdentityId: " + cognitoAuthSession.getIdentityId().getValue());
                    userId = cognitoAuthSession.getIdentityId().getValue();
                    break;
                }

                case FAILURE:
                    Log.i("AuthQuickStart", "IdentityId not present because: " + cognitoAuthSession.getIdentityId().getError().toString());
            }
        },error -> Log.e(TAG, error.toString()));
    }




    private void authAttribute(){
        Amplify.Auth.fetchUserAttributes(
                attributes -> {
                    Log.i(TAG, "Attributes => "+ attributes);
                    //  Send message to the handler to get the user Id >>
                    username =attributes.get(2).getValue();
                    userId = attributes.get(0).getValue();

                },
                error -> Log.e(TAG, "Failed to fetch user attributes.", error)
        );
    }

    /*
    handler = new Handler(Looper.getMainLooper(), msg -> {
             String user = msg.getData().getString("name");
             TextView name = findViewById(R.id.txt_username);
             name.setText(user);
             userId = msg.getData().getString("id");
             return true;
         });
     */

}