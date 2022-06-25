package com.example.toys_exchange.adapter;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.auth.AuthUser;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.model.query.Where;
import com.amplifyframework.datastore.generated.model.Account;
import com.amplifyframework.datastore.generated.model.Comment;
import com.example.toys_exchange.R;
import com.example.toys_exchange.UI.EventDetailsActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class adaptorComment extends RecyclerView.Adapter<adaptorComment.CustomViewHolder> {
    private static final String TAG = adaptorComment.class.getSimpleName();

    List<Comment> commentsList;
    public String userId;

    public static String loginUserId;

    public static void getLoginUserId() {
        AuthUser logedInUser = Amplify.Auth.getCurrentUser();
        String cognitoId = logedInUser.getUserId();
        Amplify.API.query(
                ModelQuery.list(Account.class),
                allUsers -> {
                    for (Account userAc:
                            allUsers.getData()) {
                        if(userAc.getIdcognito().equals(cognitoId)){
                            loginUserId = userAc.getId();
                        }
                    }

                },
                error -> Log.e(TAG, error.toString(), error)
        );

    }

    public adaptorComment(List<Comment> commentList) {
        this.commentsList = commentList;

    }
    @NonNull
    @Override
    public CustomViewHolder  onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItemView = layoutInflater.inflate(R.layout.comment_layout, parent, false);
        return new CustomViewHolder(listItemView).linkAdapter(this);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.loginUserIdSaved = loginUserId;
         userId = commentsList.get(position).getAccountCommentsId();
        Log.i(TAG, "login User Id onBindViewHolder "+loginUserId );

        Amplify.API.query(
                    ModelQuery.get(Account.class,userId),
                    user -> {
                        holder.username.setText(user.getData().getUsername());
                    },
                    error -> Log.e("Adaptor", error.toString(), error)
            );
        holder.text.setText(commentsList.get(position).getText());
        holder.position = position;
        holder.comment = commentsList.get(position);
        if(holder.flag.equals("me")){
            holder.deletebtn.setVisibility(View.VISIBLE);
        }else{
            holder.deletebtn.setVisibility(View.GONE);
        }

    }


    @Override
    public int getItemCount() {
        return commentsList.size();    }


    // CustomViewHolder //
    static class CustomViewHolder extends RecyclerView.ViewHolder {
        public static String loginUserIdSaved ;

        private adaptorComment adapter;
        TextView username;
        TextView text;
        Button deletebtn;

        View rootView;
        int position;
        Comment comment;

        String flag = "me";

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);

            rootView = itemView;

            username = itemView.findViewById(R.id.username_comment);
            text = itemView.findViewById(R.id.text_comment);
            deletebtn = itemView.findViewById(R.id.btn_deleteComment);

            getLoginUserId();
            Log.i(TAG, "login User Id CustomViewHolder "+loginUserIdSaved );
            // delete button
            Amplify.API.query(
                    ModelQuery.list(Comment.class),
                    comments -> {
                        if(comments.hasData()) {
                            for (Comment comment :
                                    comments.getData()) {
                                if(comment.getAccountCommentsId().equals(loginUserIdSaved)) // Add For comments check
                                {
                                    Log.i(TAG, "login User Id commentId "+comment.getAccountCommentsId() );

                                    flag = "me";
                                }else{

                                    flag = "not_me";
                                }

                            }

                        }

                    },
                    error -> Log.e(TAG, error.toString(), error)
            );

            // Action delete
            deletebtn.setOnClickListener( view -> {
                Amplify.DataStore.query(Comment.class ,
                Where.id(comment.getId()), matches -> {
                            if(matches.hasNext()){
                                Comment commentDelete = matches.next();
                                Amplify.DataStore.delete(commentDelete,
                                        deleted -> Log.i(TAG, "Comment deleted from Datastore " ),
                                        error -> Log.e(TAG, "delete failed", error));
                            }
                        },
                        error -> Log.e(TAG, "delete failed", error)
                );

                Amplify.API.mutate(ModelMutation.delete(comment),
                        response ->{
                            Log.i(TAG, "comment deleted " + response.getData().getId());
                            // https://www.youtube.com/watch?v=LQmGU3UCOPQ
                            adapter.commentsList.remove(getAdapterPosition());
                            adapter.notifyItemRemoved(getAdapterPosition());
                        },
                        error -> Log.e(TAG, "delete failed", error)
                );
            });





        }



        public CustomViewHolder linkAdapter(adaptorComment adapter){
            this.adapter = adapter;
            return this;
        }

    }



}