package com.junjunguo.phialmaps.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.graphhopper.util.shapes.GHPoint;
import com.junjunguo.phialmaps.R;

import java.util.List;

public class SummaryAddressRecyclerViewAdapter extends RecyclerView.Adapter<SummaryAddressRecyclerViewAdapter.ViewHolder> {

    private int itemLayoutResID;
    private List<GHPoint> points;
    public SummaryAddressRecyclerViewAdapter(int itemLayoutResID, List<GHPoint> points) {
        this.itemLayoutResID = itemLayoutResID;
        this.points = points;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewHolder = LayoutInflater.from(parent.getContext()).inflate(itemLayoutResID, parent, false);

        return new ViewHolder(viewHolder);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if(position==0){
            holder.row_icon.setImageResource(R.drawable.ic_location_start_white_24dp);
            holder.row_label.setText("From");
        }else{
            holder.row_icon.setImageResource(R.drawable.ic_location_end_white_24dp);
            holder.row_label.setText("To");
        }
        holder.row_value.setText("" + points.get(position).lat + ", " + points.get(position).lon);
    }

    @Override
    public int getItemCount() {
        return points.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView row_icon;
        TextView row_label;
        TextView row_value;
        public ViewHolder(View itemView) {
            super(itemView);
            row_icon = (ImageView)itemView.findViewById(R.id.row_icon);
            row_label = (TextView)itemView.findViewById(R.id.row_label);
            row_value = (TextView)itemView.findViewById(R.id.row_value);
        }
    }
}
