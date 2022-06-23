package com.example.toys_exchange.fragmenrs;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Toy;
import com.example.toys_exchange.R;
import com.example.toys_exchange.UI.EventActivity;
import com.example.toys_exchange.UI.ToyDetailActivity;
import com.example.toys_exchange.adapter.CustomAdapter;

import java.util.ArrayList;
import java.util.List;


public class ToyFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<Toy> toyList = new ArrayList<>();
    private Handler handler;

    public ToyFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_toy, container, false);

        // Add the following lines to create RecyclerView

        toyList =new ArrayList<>();

        handler = new Handler(Looper.getMainLooper(), msg ->{

            //    GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2, LinearLayoutManager.VERTICAL,false);
            recyclerView = view.findViewById(R.id.recycler_view);

            GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2, LinearLayoutManager.VERTICAL,false);

            CustomAdapter customAdapter = new CustomAdapter(toyList, new CustomAdapter.CustomClickListener() {
                @Override
                public void onTaskClickListener(int position) {
                    Intent intent = new Intent(getContext(), ToyDetailActivity.class);
                    intent.putExtra("toyName",toyList.get(position).getToyname());
                    intent.putExtra("description",toyList.get(position).getToydescription());
                    intent.putExtra("image",toyList.get(position).getImage());
                    intent.putExtra("price",toyList.get(position).getPrice());
                    intent.putExtra("condition",toyList.get(position).getCondition());
                    startActivity(intent);
                }
            });

            recyclerView.setAdapter(customAdapter);

            recyclerView.setHasFixedSize(true);

            recyclerView.setLayoutManager(gridLayoutManager);
//          recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
            recyclerView.setHasFixedSize(true);

            // recyclerView.setLayoutManager(gridLayoutManager);
            return  true;
        });


        Amplify.API.query(ModelQuery.list(Toy.class), success ->{

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
        return view;
    }
}