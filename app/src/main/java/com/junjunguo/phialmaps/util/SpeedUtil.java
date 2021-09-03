package com.junjunguo.phialmaps.util;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.UiThread;
import com.graphhopper.util.details.PathDetail;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Starcommander@github.com
 */
public class SpeedUtil
{
    List<PathDetail> maxSpeedList;
    List<PathDetail> aveSpeedList;
    
    TextView view;
    LinearLayout view_layout;
    boolean enabled = false;
    int pointsDone = 0;
    
    public SpeedUtil() {}
    
    public void initTextView(TextView view, LinearLayout layout)
    {
        this.view = view;
        this.view_layout = layout;
        updateViewVis();
    }
    
    @UiThread
    public void updateTextView(int pointNum)
    {
        if (!enabled) { return; }
        if (view==null) { Log.w(SpeedUtil.class.getName(), "Missing view for speed!"); }
        else
        {
            view.setText(getSpeedValue(pointNum + pointsDone));
        }
    }
    
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        updateViewVis();
        if (view != null)
        { this.view_layout.setVisibility(View.GONE); }
    }
    
    private void updateViewVis()
    {
        if (view == null) { return; }
        if (enabled) { this.view_layout.setVisibility(View.VISIBLE); }
        else { this.view_layout.setVisibility(View.GONE); }
    }
    
    public void updateList(Map<String, List<PathDetail>> pathDetails)
    {
        maxSpeedList = pathDetails.get("max_speed");
        aveSpeedList = pathDetails.get("average_speed");
        pointsDone = 0;
    }
    
    public void updateInstructionDone(int pLen)
    {
        pointsDone += pLen;
    }
    
    private String getSpeedValue(int pos)
    {
        if (maxSpeedList == null) { return "---"; }
        if (aveSpeedList == null) { return "---"; }
        for (PathDetail curDetail : maxSpeedList)
        {
            if (curDetail.getFirst() > pos) { continue; }
            if (curDetail.getLast() < pos) { continue; }
            if (curDetail.getValue()==null) { continue; }
            return "" + getUnitIntValue(Double.parseDouble(curDetail.getValue().toString()));
        }
        for (PathDetail curDetail : aveSpeedList)
        {
            if (curDetail.getFirst() > pos) { continue; }
            if (curDetail.getLast() < pos) { continue; }
            if (curDetail.getValue()==null) { continue; }
            return "" + getUnitIntValue(Double.parseDouble(curDetail.getValue().toString()));
        }
        return "---";
    }
    
    private int getUnitIntValue(double v)
    {
        if (Variable.getVariable().isImperalUnit())
        {
            v = v / (UnitCalculator.METERS_OF_MILE * 0.001);
            v = 5*(Math.round(v/5)); // Round to multible of 5
        }
        return (int)v;
    }

}
