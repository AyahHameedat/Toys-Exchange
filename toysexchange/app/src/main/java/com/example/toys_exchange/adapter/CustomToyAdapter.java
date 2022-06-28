package com.example.toys_exchange.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.datastore.generated.model.Toy;
import com.example.toys_exchange.R;

import java.util.List;

public class CustomToyAdapter extends RecyclerView.Adapter<CustomToyAdapter.CustomViewHolder> {

    private static final String TAG = CustomToyAdapter.class.getSimpleName();
    List<Toy> toysData;
    private String toyId;

    CustomClickListener listener;

    public CustomToyAdapter(List<Toy> toysData, CustomClickListener listener) {
        this.toysData = toysData;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItemView = layoutInflater.inflate(R.layout.item_wishlist,parent,false);

        return new CustomViewHolder(listItemView,listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {

        holder.toyName.setText(toysData.get(position).getToyname());
        holder.toyPrice.setText(toysData.get(position).getPrice().toString());

    }

    @Override
    public int getItemCount() {
        return toysData.size();
    }

    public void onTaskClickListener(int position) {

    }

    static class CustomViewHolder extends RecyclerView.ViewHolder{

        ImageView toyImage;
        TextView toyName;
        TextView toyPrice;
        CustomClickListener listener;

        LinearLayout Remove;
        LinearLayout AddToCart;
        public CustomViewHolder(@NonNull View itemView, CustomClickListener listener) {
            super(itemView);

            this.listener = listener;

            toyName = itemView.findViewById(R.id.tvName);
            toyImage = itemView.findViewById(R.id.ivImage);
            toyPrice = itemView.findViewById(R.id.tvPrice);
            Remove = itemView.findViewById(R.id.Remove);

//            itemView.setOnClickListener(view -> {
//                listener.onTaskClickListener(getAdapterPosition());
//            });

            Remove.setOnClickListener(view -> {
                listener.onRemoveClickListener(getAdapterPosition());
            });

            itemView.setOnClickListener(view -> {
                listener.ontItemClickListener(getAdapterPosition());
            });

        }
    }


    public interface CustomClickListener{
        void onRemoveClickListener(int position);
        void ontItemClickListener(int position);
    }
}
