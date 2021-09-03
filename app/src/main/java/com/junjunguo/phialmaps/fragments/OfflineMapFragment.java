package com.junjunguo.phialmaps.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.graphhopper.util.shapes.GHPoint;
import com.junjunguo.phialmaps.R;
import com.junjunguo.phialmaps.activities.MainActivity;
import com.junjunguo.phialmaps.activities.MapActivity;
import com.junjunguo.phialmaps.map.MapHandler;
import com.junjunguo.phialmaps.navigator.NaviEngine;
import com.junjunguo.phialmaps.util.SetStatusBarColor;
import com.junjunguo.phialmaps.util.Variable;

import org.oscim.android.MapView;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.event.Event;
import org.oscim.map.Map;

import java.io.File;

import static android.content.Context.MODE_PRIVATE;


public class OfflineMapFragment extends Fragment {
    public MapView mapView;
    private FloatingActionButton naviCenterBtn;
    public OfflineMapFragment() {
    }

    public static OfflineMapFragment newInstance() {
        OfflineMapFragment fragment = new OfflineMapFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_offline_map, container, false);
        mapView = (MapView)rootView.findViewById(R.id.offline_map);
        mapView.setClickable(true);
        MapHandler.getMapHandler()
                .init(mapView, Variable.getVariable().getCountry(), Variable.getVariable().getMapsFolder());
        try
        {
            MapHandler.getMapHandler().loadMap(new File(Variable.getVariable().getMapsFolder().getAbsolutePath(),
                Variable.getVariable().getCountry() + "-gh"), MapActivity._mapActivity);
            getActivity().getIntent().putExtra("com.junjunguo.pocketmaps.activities.MapActivity.SELECTNEWMAP", false);
            MapActivity._mapActivity.offline_map_status = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            SharedPreferences.Editor editor = getActivity().getSharedPreferences("Phial Map", MODE_PRIVATE).edit();
            editor.putBoolean("reloadFlag", true);
            String point_str = "";
            for(GHPoint point : MapActivity._mapActivity.allPoints){
                if(point_str!="")point_str += ",";
                point_str += point.lat + ":" + point.lon;
            }
            editor.putString("allPoints",point_str);
            editor.apply();
            MapActivity._mapActivity.offline_map_status = false;
            MapActivity.isMapAlive_preFinish();
            getActivity().finish();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra("com.junjunguo.pocketmaps.activities.MapActivity.SELECTNEWMAP", true);
            startActivity(intent);
        }
        mapView.map().events.bind(createUpdateListener());
        mapView.map().getEventLayer().enableRotation(false);
        mapView.map().getEventLayer().enableTilt(false);
        naviCenterBtn = (FloatingActionButton) MapActivity._mapActivity.findViewById(R.id.map_southbar_navicenter_fab);
        MapHandler.getMapHandler().setNaviCenterBtn(naviCenterBtn);
        naviCenterBtn.setOnClickListener(createNaviCenterListener());
        MapActivity._mapActivity.findViewById(R.id.map_show_my_position_fab).performClick();
        paintMarkers();
        new SetStatusBarColor().setSystemBarColor(rootView.findViewById(R.id.statusBarBackgroundMap2),
                getResources().getColor(R.color.my_primary), getActivity());
        return rootView;
    }
    public void paintMarkers() {
        MapHandler.getMapHandler().pathLayer = null;
        for (int i = 0; i < MapActivity._mapActivity.allPoints.size(); i++) {
            if (i == 0 && MapActivity._mapActivity.allPoints.get(i).isValid()) {
                MapActivity._mapActivity.getMapActions().doSelectCurrentPos(new GeoPoint(MapActivity._mapActivity.allPoints.get(i).lat, MapActivity._mapActivity.allPoints.get(i).lon), String.format("%.6f", new Object[]{Double.valueOf(MapActivity._mapActivity.allPoints.get(i).lat)}) + ", " + String.format("%.6f", new Object[]{Double.valueOf(MapActivity._mapActivity.allPoints.get(i).lon)}), true);
            } else if (MapActivity._mapActivity.allPoints.get(i).isValid()) {
                MapActivity._mapActivity.getMapActions().doSelectCurrentPos(new GeoPoint(MapActivity._mapActivity.allPoints.get(i).lat, MapActivity._mapActivity.allPoints.get(i).lon), String.format("%.6f", new Object[]{Double.valueOf(MapActivity._mapActivity.allPoints.get(i).lat)}) + ", " + String.format("%.6f", new Object[]{Double.valueOf(MapActivity._mapActivity.allPoints.get(i).lon)}), false);
            }
        }
    }
    public View.OnClickListener createNaviCenterListener()
    {
        View.OnClickListener l = new View.OnClickListener()
        {
            @Override public void onClick(View view)
            {
                NaviEngine.getNaviEngine().setMapUpdatesAllowed(getContext(), true);
                naviCenterBtn.setVisibility(View.INVISIBLE);
            }
        };
        return l;
    }
    private Map.UpdateListener createUpdateListener()
    {
        Map.UpdateListener d = new Map.UpdateListener(){
            @Override
            public void onMapEvent(Event e, MapPosition mapPosition)
            {
                if (e == org.oscim.map.Map.MOVE_EVENT && NaviEngine.getNaviEngine().isNavigating())
                {
                    NaviEngine.getNaviEngine().setMapUpdatesAllowed(getContext(), false);
                }
            }
        };
        return d;
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}