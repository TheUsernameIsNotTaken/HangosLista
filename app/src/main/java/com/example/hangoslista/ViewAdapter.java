package com.example.hangoslista;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ViewAdapter extends RecyclerView.Adapter<ViewAdapter.MyViewHolder>{
    private List<ListItem> data;

    public ViewAdapter(List<ListItem> data){
        this.data = data;
    }

    //Boradcast
    public static final String BROADCAST_EXTRA_ID = "READ_ID";
    public static final String BROADCAST_EXTRA_NAME = "READ_NAME";
    public static final String BROADCAST_EXTRA_PRICE = "READ_PRICE";
    public static final String ACTION_CUSTOM_BROADCAST = BuildConfig.APPLICATION_ID + ".TTS_CUSTOM_BROADCAST";

    //XML data for layout
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new MyViewHolder(view);
    }

    //When binding
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.name.setText(data.get(position).getItemName());
        String priceText = "" + data.get(position).getItemPrice();
        holder.price.setText(priceText);
    }

    //# of data
    @Override
    public int getItemCount() {
        return data.size();
    }

    //A specific items
    public ListItem getItemAtPosition(int m){
        return data.get(m);
    }

    //A created ViewHolder
    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView name, price;
        public MyViewHolder(@NonNull View tv) {
            super(tv);
            MyViewHolder.this.name = tv.findViewById(R.id.itemNameText);
            MyViewHolder.this.price = tv.findViewById(R.id.itemPriceText);

            //An onClick listener - Play sound
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ListItem toRead = data.get(getLayoutPosition());
                    //Broadcast the data to read out.
                    Context context = v.getContext();
                    Intent customBroadcastIntent = new Intent(ACTION_CUSTOM_BROADCAST);
                    customBroadcastIntent.putExtra(BROADCAST_EXTRA_ID, toRead.getItemId());
                    customBroadcastIntent.putExtra(BROADCAST_EXTRA_NAME, toRead.getItemName());
                    customBroadcastIntent.putExtra(BROADCAST_EXTRA_PRICE, toRead.getItemPrice());
                    LocalBroadcastManager.getInstance(context).sendBroadcast(customBroadcastIntent);
                }
            });
        }
    }
}
