package com.example.toys_exchange;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.auth.AuthUser;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Account;
import com.example.toys_exchange.fragmenrs.ToyFragment;

public class MainActivity2 extends AppCompatActivity {

    private static final String TAG = MainActivity2.class.getSimpleName();
    LinearLayout llHome;
    private String acc_id;
    private ToyFragment toyFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toyexchange_activity_add_event);

    }


    public  void getLoginUserId() {
        AuthUser logedInUser = Amplify.Auth.getCurrentUser();
        String cognitoId = logedInUser.getUserId();
        Amplify.API.query(
                ModelQuery.list(Account.class),
                allUsers -> {
                    for (Account userAc:
                            allUsers.getData()) {
                        if(userAc.getIdcognito().equals(cognitoId)){
                            acc_id = userAc.getId();
                        }
                    }
                },
                error -> Log.e(TAG, error.toString(), error)
        );
    }
}