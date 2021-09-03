package com.junjunguo.phialmaps.fragments;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.junjunguo.phialmaps.R;
import com.junjunguo.phialmaps.model.Place;

import java.util.List;

public class SearchPlaceRecyclerViewAdapter extends RecyclerView.Adapter<SearchPlaceRecyclerViewAdapter.ViewHolder> {

    private int itemLayoutResID;
    private List<Place> itemList;
    public static Activity activity = null;
    public SearchPlaceRecyclerViewAdapter(Activity activity, int itemLayoutResID, List<Place> itemList) {
        this.itemLayoutResID = itemLayoutResID;
        this.itemList = itemList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewHolder = LayoutInflater.from(parent.getContext()).inflate(itemLayoutResID, parent, false);
        return new ViewHolder(viewHolder);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.bind(itemList.get(position));
    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txt_address;
        TextView txt_name;
        public ViewHolder(View itemView) {
            super(itemView);
            txt_address = (TextView)itemView.findViewById(R.id.txt_address);
            txt_name = (TextView)itemView.findViewById(R.id.txt_name);
        }
        public void bind(final Place item) {
            txt_address.setText(item.getAddress());
            txt_name.setText(item.getName());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("lat",item.getLat());
                    returnIntent.putExtra("lng",item.getLng());
                    activity.setResult(Activity.RESULT_OK,returnIntent);
                    activity.finish();
                }
            });
        }
    }
}
