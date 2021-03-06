package com.example.toys_exchange.UI;



import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Account;
import com.amplifyframework.datastore.generated.model.Comment;
import com.amplifyframework.datastore.generated.model.Event;
import com.amplifyframework.datastore.generated.model.UserAttendEvent;
import com.example.toys_exchange.R;
import com.example.toys_exchange.adapter.adaptorComment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class EventDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = EventDetailsActivity.class.getSimpleName();

    adaptorComment commentRecyclerViewAdapter;
    List<Comment> commentsListDatabase = new ArrayList<>();

    TextView description ;
    CollapsingToolbarLayout title;

    Event event;

    Account userWhoAttend;
    UserAttendEvent userAttendEvent;

    GoogleMap googleMap;

    Button addComment;
    Button btnAttend;
    Button deleteComment;
    Handler handler;
    Button updateBtn;

    Double longitude;
    Double latitude;

    String eventIdFromMain;
    String cognitoIdFromMain;
    String loginUserIdFromMain;
    String userIdAddedEventFromMain;
    String titEvent;

    Button updateForm;

    TextView showLocation;

    Intent passedIntent;
    String userAddEventId;
    private String titleText;
    private String descriptionText;

    ConstraintLayout rlEditComment;
    ImageView ivEditComment;
    EditText etEditComment;


    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shophop_activity_event_detail);
        getUserAttend();

        passedIntent = getIntent();
        eventIdFromMain = passedIntent.getStringExtra("eventID");
        cognitoIdFromMain = passedIntent.getStringExtra("cognitoID");
        loginUserIdFromMain = passedIntent.getStringExtra("loginUserID");
        userIdAddedEventFromMain = passedIntent.getStringExtra("userID");
        longitude=passedIntent.getDoubleExtra("longitude",0.0);
        latitude=passedIntent.getDoubleExtra("latitude",0.0);

        updateForm = findViewById(R.id.btn_updateComment);


        handler = new Handler(Looper.getMainLooper(), msg -> {
            if(commentsListDatabase.size()!=0) {
                recyclerViewWork();

            }
            return true;
        });


        title = findViewById(R.id.toolbar_layout);
        title.setTitle(passedIntent.getStringExtra("eventTitle"));


        description = findViewById(R.id.tvEventDescription);
        description.setText(passedIntent.getStringExtra("description"));

         userAddEventId =  passedIntent.getStringExtra("userID");
        getUserNameAndImage(userAddEventId);

        setEventValues();

        // The Add Comment Button
        addComment = findViewById(R.id.btnComment);
        btnAttend = findViewById(R.id.btnAttend);
        addBtnListner();

        showLocation=findViewById(R.id.tvLocation);
        showLocation.setOnClickListener(view -> {

            if(latitude!=0 && longitude!=0){
                Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("geo:"+latitude+","+longitude+"?q="+latitude+","+longitude+"name"));
                startActivity(intent);

            }else {
                Toast.makeText(this, "no location provide", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getUserNameAndImage(String userAddEventId ) {
        CircleImageView image = findViewById(R.id.ivProfileImage);
        TextView username = findViewById(R.id.tvUserName);
        Amplify.API.query(
                ModelQuery.get(Account.class,userAddEventId),
                user -> {
                    runOnUiThread(()->{
                        username.setText(user.getData().getUsername());
                        String image1= user.getData().getImage();
                        if(image1!=null) {
                            Amplify.Storage.getUrl(
                                    image1,
                                    result -> {
                                        runOnUiThread(() -> {
                                            Picasso.get().load(result.getUrl().toString()).into(image);
                                        });
                                    },
                                    error -> Log.e("MyAmplifyApp", "URL generation failure", error)
                            );
                        }
                    });
                },
                error -> Log.e("Adaptor", error.toString(), error)
        );
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addBtnListner() {
        addComment.setOnClickListener(view->{
                EditText comment = findViewById(R.id.etComment);
                String comment_text = comment.getText().toString();
                Comment commentAPI = Comment.builder()
                        .text(comment_text)
                        .accountCommentsId(loginUserIdFromMain) // User loginUser From function setEventValues
                        .eventCommentsId(event.getId())
                        .build();

                // API save to backend
                Amplify.API.mutate(
                        ModelMutation.create(commentAPI),
                        success -> {
                            runOnUiThread(() -> {
                                comment.setText("");
                                commentsListDatabase.add(commentAPI);

                                Bundle bundle = new Bundle();
                                bundle.putString("commentsListUpdate", "commentsListUpdate");

                                Message message = new Message();
                                message.setData(bundle);

                                handler.sendMessage(message);

                            });
                        },
                        error -> Log.e(TAG, "Could not save item to API", error)
                );

            });

        btnAttend.setOnClickListener(view->{
            // If the User Not Attend the Event
            if(btnAttend.getText().equals("Attend")) {
                                Amplify.API.query(
                                        ModelQuery.list(Account.class),
                                        users -> {
                                            for (Account userAccount :
                                                    users.getData()) {
                                                if (userAccount.getIdcognito().equals(cognitoIdFromMain)) {
                                                    userWhoAttend = userAccount;
                                                    userAttendEvent = UserAttendEvent.builder()
                                                            .account(userAccount)
                                                            .event(event)
                                                            .build();
                                                }
                                            }
//
                                            runOnUiThread(() -> {

                                                Amplify.API.mutate(
                                                        ModelMutation.create(userAttendEvent),
                                                        success -> {
                                                            runOnUiThread(() -> {
                                                                btnAttend.setText("Un Attend");
                                                          //      Toast.makeText(getApplicationContext(), "user Attend", Toast.LENGTH_SHORT).show();
                                                            });
                                                        },
                                                        error -> Log.e(TAG, "Could not save item to API", error)
                                                );


                                            });

                                        },
                                        error -> Log.e(TAG, error.toString(), error)
                                );

            }else{ // User want Un Attend // Delete From tables
                // https://docs.amplify.aws/lib/graphqlapi/mutate-data/q/platform/android/#run-a-mutation
                Amplify.API.query(ModelQuery.list(UserAttendEvent.class),
                        usersAttend -> {
                            for (UserAttendEvent user:
                                    usersAttend.getData()) {
                                if(user.getAccount().getId().equals(loginUserIdFromMain)
                                        && user.getEvent().getId().equals(eventIdFromMain)){
                                    runOnUiThread(() -> {
                                        Amplify.API.mutate(ModelMutation.delete(user),
                                                response ->{
                                                    runOnUiThread(() -> {
                                                        btnAttend.setText("Attend");
                                                    });

                                            },
                                                error -> Log.e(TAG, "delete failed", error)
                                        );
                                    });
                                    break;
                                }
                            }

                        },
                        error -> Log.e(TAG, error.toString(), error)
                );

            }
        });

    }

    private void getUserAttend(){
        Amplify.API.query(ModelQuery.list(UserAttendEvent.class),
           usersAttend -> {
            if(usersAttend.hasData()) {
                for (UserAttendEvent user :
                        usersAttend.getData()) {
                    if (user.getAccount().getId().equals(loginUserIdFromMain)
                            && user.getEvent().getId().equals(eventIdFromMain)) {
                        runOnUiThread(() -> {
                            btnAttend.setText("Un Attend");
                        });
                        break;
                    }
                }
            }

          },
        error -> Log.e(TAG, error.toString(), error)
        );
    }

    // Updated From the Main
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setEventValues() {
        Amplify.API.query(
                ModelQuery.get(Event.class, eventIdFromMain),

                events -> {
                    if(events.hasData()) {
                    event = events.getData();
                    // Use To do Sync
                    runOnUiThread(() -> {
                        Amplify.API.query(ModelQuery.get(Account.class,event.getAccountEventsaddedId()),
                                useradd -> {
                               runOnUiThread(()->{
                                   if(event.getAccountEventsaddedId().equals(loginUserIdFromMain)){
                                       btnAttend.setVisibility(View.INVISIBLE);
                                   }
                               });

                                },
                                error -> Log.e(TAG, error.toString(), error)
                        );
                    });

                        event = events.getData();
                        // Use To do Sync
                        runOnUiThread(() -> {
                            Amplify.API.query(ModelQuery.get(Account.class, event.getAccountEventsaddedId()),
                                    useradd -> {
                                        if (useradd.hasData()) {
                                            runOnUiThread(() -> {
                                                if (event.getAccountEventsaddedId().equals(loginUserIdFromMain)) {
                                                    btnAttend.setVisibility(View.INVISIBLE);
                                                }
                                            });
                                        }
                                    },
                                    error -> Log.e(TAG, error.toString(), error)
                            );
                        });
                    }

                                                },
           error -> Log.e(TAG, error.toString(), error)
                                        );

    }

    //    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getCommentsList() {
        commentsListDatabase = new ArrayList<>();
        Amplify.API.query(
                ModelQuery.list(Comment.class),
                comments -> {
                    if(comments.hasData()) {
                        for (Comment comment :
                                comments.getData()) {
                            if(comment.getEventCommentsId().equals(eventIdFromMain)) // Add For comments check
                                  commentsListDatabase.add(comment);
                        }
                        // Sort the Created At
                        Collections.sort(commentsListDatabase, new SortByDate());
                    }


                        Bundle bundle = new Bundle();
                        bundle.putString("commentsListUpdate", "commentsListUpdate");

                        Message message = new Message();
                        message.setData(bundle);

                        handler.sendMessage(message);
                },
                error -> Log.e(TAG, error.toString(), error)
        );
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));
    }

    // Class to sort the comments by date
    // https://www.delftstack.com/howto/java/how-to-sort-objects-in-arraylist-by-date-in-java/ 
    static class SortByDate implements Comparator<Comment> {
        @Override
        public int compare(Comment a, Comment b) {
            return a.getCreatedAt().compareTo(b.getCreatedAt());
        }
    }

    // Recycler View
    private void recyclerViewWork() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        // create an Adapter // Custom Adapter
        commentRecyclerViewAdapter = new adaptorComment(commentsListDatabase, loginUserIdFromMain );

        // https://gist.github.com/codinginflow/7b9dd1c12ba015f2955bdd15b09b1cb1
        commentRecyclerViewAdapter.setOnItemClickListener(new adaptorComment.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {

                Amplify.API.mutate(ModelMutation.delete(commentsListDatabase.get(position)),
                        response -> {
                            // https://www.youtube.com/watch?v=LQmGU3UCOPQ
                            commentsListDatabase.remove(position);
                            commentRecyclerViewAdapter.notifyItemRemoved(position);
                        },
                        error -> Log.e(TAG, "delete failed", error)
                );
            }

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onUpdateClick(int position,ConstraintLayout rlEditComment
                    ,    ImageView ivEditComment, EditText etEditComment) {
                rlEditComment.setVisibility(View.VISIBLE);
                ivEditComment.setOnClickListener(view -> {
                    Comment newComment = Comment.builder().text(etEditComment.getText().toString())
                            .id(commentsListDatabase.get(position).getId()).accountCommentsId(commentsListDatabase.get(position).getAccountCommentsId())
                            .eventCommentsId(commentsListDatabase.get(position).getEventCommentsId()).build();

                    Amplify.API.mutate(ModelMutation.update(newComment),
                            response -> {

                                runOnUiThread(() -> {
                                    rlEditComment.setVisibility(View.GONE);
                                    commentsListDatabase.remove(position);
                                    commentsListDatabase.add(position,newComment);
                                    commentRecyclerViewAdapter.notifyItemChanged(position);
                                });
                                // https://www.youtube.com/watch?v=LQmGU3UCOPQ
                            },
                            error -> Log.e(TAG, "update failed", error)
                    );

                });

            }
        });

        // set adapter on recycler view
        recyclerView.setAdapter(commentRecyclerViewAdapter);
        // set other important properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onResume() {

        handler = new Handler(Looper.getMainLooper(), msg -> {
            if(commentsListDatabase.size()!=0) {
                recyclerViewWork();
            }
            return true;
        });
        getUserNameAndImage(userAddEventId);
        getCommentsList();
        super.onResume();
    }

}