package com.junjunguo.phialmaps.fragments;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.junjunguo.phialmaps.R;
import com.graphhopper.util.shapes.GHPoint;
import com.junjunguo.phialmaps.activities.MapActivity;

import java.util.List;

public class MultiAddressRecyclerViewAdapter extends RecyclerView.Adapter<MultiAddressRecyclerViewAdapter.ViewHolder> {

    private int itemLayoutResID;
    private List<GHPoint> points;
    private Activity activity;
    public MultiAddressRecyclerViewAdapter(Activity activity, int itemLayoutResID, List<GHPoint> points) {
        this.itemLayoutResID = itemLayoutResID;
        this.points = points;
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
        if(points.size()==1)holder.btn_del_point.setVisibility(View.GONE);
        holder.btn_del_point.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MapActivity)activity).removeTOPoint(position);
            }
        });
        holder.btn_settings_to_sel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MapActivity)activity).setToPoint(position);
            }
        });
        if(points.get(position+1).isValid()){
            holder.settings_to_local_et.setText("" + points.get(position+1).lat + ", " + points.get(position+1).lon);
            holder.btn_del_point.setVisibility(View.VISIBLE);
            holder.btn_settings_to_sel.setVisibility(View.GONE);
            holder.btn_settings_to_search.setVisibility(View.GONE);
        }else {
            holder.settings_to_local_et.setText("");
            holder.btn_settings_to_sel.setVisibility(View.VISIBLE);
            holder.btn_settings_to_search.setVisibility(View.VISIBLE);
            holder.btn_del_point.setVisibility(View.GONE);
        }
        holder.btn_settings_to_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MapActivity)activity).searchToAddress(position);
            }
        });
    }
//
//    private int getPixel(int dp) {
//        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, fragment.getContext().getResources().getDisplayMetrics());
//        return height;
//    }

    @Override
    public int getItemCount() {
        return points.size()-1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageButton btn_del_point;
        ImageButton btn_settings_to_sel;
        TextView settings_to_local_et;
        ImageButton btn_settings_to_search;
        public ViewHolder(View itemView) {
            super(itemView);
            btn_del_point = (ImageButton)itemView.findViewById(R.id.settings_to_del_btn);
            btn_settings_to_sel = (ImageButton)itemView.findViewById(R.id.settings_to_sel_btn);
            settings_to_local_et = (TextView)itemView.findViewById(R.id.settings_to_local_et);
            btn_settings_to_search = (ImageButton)itemView.findViewById(R.id.settings_to_search_btn);
        }
    }
}
