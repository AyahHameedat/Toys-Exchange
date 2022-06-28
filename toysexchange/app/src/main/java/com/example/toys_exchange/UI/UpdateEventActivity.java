package com.example.toys_exchange.UI;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Account;
import com.amplifyframework.datastore.generated.model.Comment;
import com.amplifyframework.datastore.generated.model.Event;
import com.amplifyframework.datastore.generated.model.UserAttendEvent;
import com.example.toys_exchange.R;
import com.example.toys_exchange.adapter.EventDeleteAdapter;

import java.util.ArrayList;
import java.util.List;

public class UpdateEventActivity extends AppCompatActivity {

//    private static final String TAG = UpdateEventActivity.class.getSimpleName();
//    List<Event> eventList = new ArrayList<>();
//    private Handler handler;
//    PopupWindow popUp;
//    boolean click = true;
//    Button updateForm = findViewById(R.id.btn_addEvent);
//
//
//    public String acc_id;
//    Account acc;
//    List<Account> acclist = new ArrayList<>();
//    public String userId;
//
//    String cognitoId;
//    private String loginUserId;
//    private String loginUserName;
//
//    EventDeleteAdapter customEventAdapter;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_update_event);
//
//        handler = new Handler(Looper.getMainLooper(), msg -> {
//            userId = msg.getData().getString("id");
//            getEventByUser();
//            return true;
//        });
//
//    }
//
//
//
//    private void getEventByUser() {
//
//        RecyclerView recyclerView = findViewById(R.id.recycler_view);
//        // create an Adapter // Custom Adapter
//
//        customEventAdapter = new EventDeleteAdapter(eventList, new EventDeleteAdapter.CustomClickListener() {
//            @Override
//            public void onDeleteClickListener(int position) {
//                Amplify.API.query(
//                        ModelQuery.list(Comment.class),
//                        comments -> {
//                            if (comments.hasData()) {
//                                for (Comment comment :
//                                        comments.getData()) {
//                                    if (comment.getEventCommentsId().equals(eventList.get(position).getId())) // Add For comments check
//                                    {
//                                        Amplify.API.mutate(ModelMutation.delete(comment),
//                                                response -> {
//                                                    Log.i(TAG, "Comment deleted " + response);
//                                                },
//                                                error -> Log.e(TAG, "delete failed", error)
//                                        );
//                                    }
//                                }
//                            }
//                            runOnUiThread(() -> {
//                                Amplify.API.query(
//                                        ModelQuery.list(UserAttendEvent.class),
//                                        userAttendEvent -> {
//                                            if (userAttendEvent.hasData()) {
//                                                for (UserAttendEvent userAttendEvent1 :
//                                                        userAttendEvent.getData()) {
//                                                    if (userAttendEvent1.getEvent().getId().equals(eventList.get(position).getId())) // Add For comments check
//                                                    {
//                                                        Amplify.API.mutate(ModelMutation.delete(userAttendEvent1),
//                                                                response -> {
//                                                                    Log.i(TAG, "UserAttendEvent deleted " + response);
//                                                                },
//                                                                error -> Log.e(TAG, "delete failed", error)
//                                                        );
//                                                    }
//                                                }
//                                            }
//                                            runOnUiThread(() -> {
//                                                Amplify.API.mutate(ModelMutation.delete(eventList.get(position)),
//                                                        response -> {
//                                                            // https://www.youtube.com/watch?v=LQmGU3UCOPQ
//                                                            Log.i(TAG, "Event deleted " + response);
//                                                            eventList.remove(position);
//                                                            customEventAdapter.notifyItemRemoved(position);
//                                                        },
//                                                        error -> Log.e(TAG, "delete failed", error)
//                                                );
//                                            });
//
//                                        },
//                                        error -> Log.e(TAG, error.toString(), error)
//                                );
////
//                            });
//
//                        },
//                        error -> Log.e(TAG, error.toString(), error)
//                );
//
//            }
//
//            @Override
//            public void ontItemClickListener(int position) {
//                Intent intent = new Intent(getApplicationContext(), EventDetailsActivity.class);
//                intent.putExtra("eventTitle", eventList.get(position).getTitle());
//                intent.putExtra("description", eventList.get(position).getEventdescription());
//                intent.putExtra("userID", eventList.get(position).getAccountEventsaddedId());
//                intent.putExtra("eventID", eventList.get(position).getId());
//                intent.putExtra("cognitoID", cognitoId);
//                intent.putExtra("loginUserID", loginUserId);
//                intent.putExtra("loginUserName", loginUserName);
//                startActivity(intent);
//            }
//
//            @Override
//            public void onUpdateClickListener(int position) {
//                updateForm.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//
//                        Amplify.API.query(
//                                ModelQuery.list(Event.class),
//                                events -> {
//                                    if (events.hasData()) {
//                                        for (Event event :
//                                                events.getData()) {
//                                            if (event.getAccountEventsaddedId().equals(eventList.get(position).getId())) // Add For events check
//                                            {
//                                                Amplify.API.mutate(ModelMutation.update(event),
//                                                        response -> {
//                                                            Log.i(TAG, "Update done " + response);
//                                                        },
//                                                        error -> Log.e(TAG, "update failed", error)
//                                                );
//                                            }
//                                            Log.i(TAG, "onUpdateClickListener: ");
//                                        }
//                                    }
//                                    runOnUiThread(() -> {
//                                        Amplify.API.mutate(ModelMutation.update(eventList.get(position)),
//                                                response -> {
//                                                    // https://www.youtube.com/watch?v=LQmGU3UCOPQ
//                                                    Log.i(TAG, "Event updated " + response);
//                                                    customEventAdapter.notifyItemRemoved(position);
//                                                },
//                                                error -> Log.e(TAG, "update failed", error)
//                                        );
//                                    });
//
//                                },
//                                error -> Log.e(TAG, error.toString(), error)
//                        );
//
////
//                    }
//                });
//
//
//
//                // Setting onClick behavior to the button
////                updateBtn.setOnClickListener(new View.OnClickListener() {
////                    @Override
////                    public void onClick(View view) {
////                        // Initializing the popup menu and giving the reference as current context
////                        PopupMenu popupMenu = new PopupMenu(eventListActivity.this, updateBtn);
////
////                        // Inflating popup menu from popup_menu.xml file
////                        popupMenu.getMenuInflater().inflate(R.menu.event_menu, popupMenu.getMenu());
////                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
////                            @Override
////                            public boolean onMenuItemClick(MenuItem menuItem) {
////                                // Toast message on menu item clicked
////                                Toast.makeText(eventListActivity.this, "You Clicked " + menuItem.getTitle(), Toast.LENGTH_SHORT).show();
////                                return true;
////                            }
////                        });
////                        // Showing the popup menu
////                        popupMenu.show();
////                    }
////                });
//            }
//            ///******
////                LinearLayout layout = new LinearLayout(getApplicationContext());
////                updateBtn.setText("Click Me");
////                updateBtn.setOnClickListener(new View.OnClickListener() {
////                    @Override
////                    public void onClick(View v) {
////                        if (click) {
////                            popUp.showAtLocation(layout, Gravity.BOTTOM, 10, 10);
////                            popUp.update(50, 50, 300, 80);
////                            click = false;
////                        } else {
////                            popUp.dismiss();
////                            click = true;
////                        }
////                    }
////                });
//
//        });
//
//        // set adapter on recycler view
//        recyclerView.setAdapter(customEventAdapter);
//        // set other important properties
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//    }
}