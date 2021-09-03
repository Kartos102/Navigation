package com.junjunguo.phialmaps.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.location.Address;
import android.location.Location;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.graphhopper.util.shapes.GHPoint;
import com.junjunguo.phialmaps.R;
import com.junjunguo.phialmaps.fragments.AppSettings;
import com.junjunguo.phialmaps.fragments.AppSettings.SettType;
import com.junjunguo.phialmaps.fragments.SummaryAddressRecyclerViewAdapter;
import com.junjunguo.phialmaps.map.Destination;
import com.junjunguo.phialmaps.model.listeners.MapHandlerListener;
import com.junjunguo.phialmaps.model.listeners.NavigatorListener;
import com.junjunguo.phialmaps.model.listeners.OnClickAddressListener;
import com.junjunguo.phialmaps.navigator.NaviEngine;
import com.junjunguo.phialmaps.map.MapHandler;
import com.junjunguo.phialmaps.map.Navigator;
import com.junjunguo.phialmaps.fragments.InstructionAdapter;
import com.junjunguo.phialmaps.util.Variable;
//import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
//import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;

import org.oscim.android.MapView;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;

/**
 * This file is part of PocketMaps
 * <p>
 * menu controller, controls menus for map activity
 * <p>
 * Created by GuoJunjun <junjunguo.com> on June 24, 2015.
 */
public class MapActions implements NavigatorListener, MapHandlerListener {
    public final static String EMPTY_LOC_STR = "..........";
    private final static int ZOOM_MAX = 22;
    private final static int ZOOM_MIN = 1;
    private final static int REQUEST_CODE_AUTOCOMPLETE = 106;
    
    enum TabAction{ StartPoint, EndPoint, AddFavourit, None };
    private TabAction tabAction = TabAction.None;
    private Activity activity;
    private AppSettings appSettings;
    protected FloatingActionButton showPositionBtn, navigationBtn, settingsBtn, settingsSetBtn, settingsNavBtn, controlBtn, favourBtn, mapStatusBtn;
    protected FloatingActionButton zoomInBtn, zoomOutBtn, naviCenterBtn, searchAddressBtn;
    private ViewGroup sideBarVP, sideBarMenuVP, southBarSettVP, southBarFavourVP, navSettingsVP, navSettingsFromVP, navSettingsToVP,
            navInstructionListVP, navTopVP, navBottomVP;
    private boolean menuVisible;
    private ImageButton btn_add_to_address;
    public Button nav_show_btn, btn_nav_start, btn_nav_stop;
    private TextView fromLocalET, toLocalET;
    public boolean map_status = true;

    public MapActions(Activity activity) {
        this.activity = activity;
        this.showPositionBtn = (FloatingActionButton) activity.findViewById(R.id.map_show_my_position_fab);
        this.navigationBtn = (FloatingActionButton) activity.findViewById(R.id.map_nav_fab);
        this.settingsBtn = (FloatingActionButton) activity.findViewById(R.id.map_southbar_settings_fab);
        this.settingsSetBtn = (FloatingActionButton) activity.findViewById(R.id.map_southbar_sett_sett_fab);
        this.settingsNavBtn = (FloatingActionButton) activity.findViewById(R.id.map_southbar_sett_nav_fab);
        this.favourBtn = (FloatingActionButton) activity.findViewById(R.id.map_southbar_favour_fab);
        this.controlBtn = (FloatingActionButton) activity.findViewById(R.id.map_sidebar_control_fab);
        this.zoomInBtn = (FloatingActionButton) activity.findViewById(R.id.map_zoom_in_fab);
        this.zoomOutBtn = (FloatingActionButton) activity.findViewById(R.id.map_zoom_out_fab);
        this.mapStatusBtn = (FloatingActionButton) activity.findViewById(R.id.btn_map_status);
        this.searchAddressBtn = (FloatingActionButton) activity.findViewById(R.id.map_search_address);
        this.naviCenterBtn = (FloatingActionButton) activity.findViewById(R.id.map_southbar_navicenter_fab);
        // view groups managed by separate layout xml file : //map_sidebar_layout/map_sidebar_menu_layout
        this.sideBarVP = (ViewGroup) activity.findViewById(R.id.map_sidebar_layout);
        this.sideBarMenuVP = (ViewGroup) activity.findViewById(R.id.map_sidebar_menu_layout);
        this.southBarSettVP = (ViewGroup) activity.findViewById(R.id.map_southbar_sett_layout);
        this.southBarFavourVP = (ViewGroup) activity.findViewById(R.id.map_southbar_favour_layout);
        this.navSettingsVP = (ViewGroup) activity.findViewById(R.id.nav_settings_layout);
        this.navTopVP = (ViewGroup) activity.findViewById(R.id.navtop_layout);
        this.navBottomVP = (ViewGroup) activity.findViewById(R.id.map_nav_bottom_layout);
        this.navSettingsFromVP = (ViewGroup) activity.findViewById(R.id.nav_settings_from_layout);
        this.navSettingsToVP = (ViewGroup) activity.findViewById(R.id.nav_settings_to_layout);
        this.navInstructionListVP = (ViewGroup) activity.findViewById(R.id.nav_instruction_list_layout);
        //form location and to location textView
        this.fromLocalET = (TextView) activity.findViewById(R.id.nav_settings_from_local_et);
        this.toLocalET = (TextView) activity.findViewById(R.id.nav_settings_to_local_et);
        this.btn_nav_start = (Button)activity.findViewById(R.id.btn_nav_start);
        this.btn_nav_start.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!map_status) {
                    searchAddressBtn.setVisibility(View.INVISIBLE);
                    Navigator.getNavigator().setGhResponse(((MapActivity) activity).resp);
                    initNavListView();
                    Navigator.getNavigator().setNaviStart(activity, true);
                    Location curLoc = MapActivity.getmCurrentLocation();
                    if (curLoc!=null)
                    {
                        NaviEngine.getNaviEngine().updatePosition(activity, curLoc);
                    }
                }else {
                    ((MapActivity)activity).onlineMapFragment._launchNavigationWithRoute();
                }
            }
        });
        this.btn_nav_stop = (Button)activity.findViewById(R.id.btn_nav_stop);
        this.btn_nav_stop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!map_status) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage(R.string.stop_navigation_msg).setTitle(R.string.stop_navigation)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Navigator.getNavigator().setOn(false);
                                    navInstructionListVP.setVisibility(View.INVISIBLE);
                                    navSettingsVP.setVisibility(View.VISIBLE);
                                    searchAddressBtn.setVisibility(View.VISIBLE);
                                    ((MapActivity)activity).clearAllPoints();
                                    btn_nav_start.setVisibility(View.INVISIBLE);
                                    btn_nav_stop.setVisibility(View.INVISIBLE);
                                    dialog.dismiss();
                                }
                            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }else {
                    ((MapActivity)activity).allPoints.clear();
                    ((MapActivity)activity).allPoints.add(new GHPoint());
                    ((MapActivity)activity).allPoints.add(new GHPoint());
                    ((MapActivity)activity).multiAddressRecyclerViewAdapter.notifyDataSetChanged();
                    fromLocalET.setText("");
                    setQuickButtonsClearVisible(true, false);
                    activity.findViewById(R.id.nav_settings_to_del_btn).setVisibility(View.GONE);
                    ((MapActivity)activity).onlineMapFragment.paintMarkers();
                    if (((MapActivity)activity).onlineMapFragment.isValidRoute()) ((MapActivity)activity).onlineMapFragment.fetchRoute();
                    btn_nav_start.setVisibility(View.INVISIBLE);
                    btn_nav_stop.setVisibility(View.INVISIBLE);
                }
            }
        });
        this.nav_show_btn = (Button) activity.findViewById(R.id.nav_show_btn);
        this.nav_show_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!map_status) {
                    if(((MapActivity)activity).resp!=null){
                        if(((MapActivity)activity).isValidRoute()) {
                            searchAddressBtn.setVisibility(View.INVISIBLE);
                            Navigator.getNavigator().setGhResponse(((MapActivity) activity).resp);
                            initNavListView();
                            Navigator.getNavigator().setNaviStart(activity, true);
                            Location curLoc = MapActivity.getmCurrentLocation();
                            if (curLoc!=null)
                            {
                                NaviEngine.getNaviEngine().updatePosition(activity, curLoc);
                            }
                            //MapHandler.getMapHandler().recalcPath(activity);
                            //Toast.makeText(activity.getApplicationContext(),"sfsfsdf",Toast.LENGTH_LONG).show();
//                            Navigator.getNavigator().setGhResponse(((MapActivity) activity).resp);
//                            MapHandler.getMapHandler().setCalculatePath(false, true);
                        }
                    }
                }else {
                    ((MapActivity)activity).onlineMapFragment._launchNavigationWithRoute();
                }
            }
        });
        this.menuVisible = false;
//        MapHandler.getMapHandler().setMapHandlerListener(this);
        MapHandler.getMapHandler().setNaviCenterBtn(naviCenterBtn);
        Navigator.getNavigator().addListener(this);
        appSettings = new AppSettings(activity);
        naviCenterBtn.setOnClickListener(createNaviCenterListener());
        this.btn_add_to_address = (ImageButton) activity.findViewById(R.id.add_to_point);
        this.btn_add_to_address.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MapActivity)activity).addToAddressPoint();
            }
        });
        this.mapStatusBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!map_status) {
                    if(Navigator.getNavigator().isOn())Navigator.getNavigator().setOn(false);
                    mapStatusBtn.setImageResource(R.drawable.ic_online_white_24dp);
                    MapActivity mapActivity = (MapActivity)activity;
                    mapActivity.changeMapStatus(true);
                }else {
//                    if(!MapActivity._mapActivity.offline_map_status){
//
//                                MapActivity.isMapAlive_preFinish();
//                                activity.finish();
//                                Intent intent = new Intent(activity, MainActivity.class);
//                                intent.putExtra("com.junjunguo.pocketmaps.activities.MapActivity.SELECTNEWMAP", true);
//                                activity.startActivity(intent);
//
//                    }else {
                        mapStatusBtn.setImageResource(R.drawable.ic_offline_white_24dp);
                        MapActivity mapActivity = (MapActivity) activity;
                        mapActivity.changeMapStatus(false);
//                    }
                }
                map_status = !map_status;
            }
        });
        this.searchAddressBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, SearchActivity.class);
                ((MapActivity)activity).add_to_pos = -2;
                activity.startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });
        initControlBtnHandler();
        initZoomControlHandler();
        initShowMyLocation();
        initNavBtnHandler();
        initNavSettingsHandler();
        initSettingsBtnHandler();
        initFavourBtnHandler();
    }



    public OnClickListener createNaviCenterListener()
    {
      OnClickListener l = new OnClickListener()
      {
        @Override public void onClick(View view)
        {
          NaviEngine.getNaviEngine().setMapUpdatesAllowed(activity.getApplicationContext(), true);
          naviCenterBtn.setVisibility(View.INVISIBLE);
        }
      };
      return l;
    }

    /**
     * init and implement performance for settings
     */
    @SuppressLint("ResourceAsColor")
    private void initSettingsBtnHandler() {
        settingsSetBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                appSettings.showAppSettings(sideBarVP, SettType.Default);
            }
        });
        settingsNavBtn.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View v) {
              appSettings.showAppSettings(sideBarVP, SettType.Navi);
          }
        });

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            ColorStateList oriColor;
            @Override public void onClick(View v) {
//              if (southBarSettVP.getVisibility() == View.VISIBLE)
//              {
//                southBarSettVP.setVisibility(View.INVISIBLE);
//              }
//              else
//              {
//                 southBarSettVP.setVisibility(View.VISIBLE);
//              }
                searchAddressBtn.setVisibility(View.INVISIBLE);
                appSettings.showAppSettings(sideBarVP, SettType.Default);
            }
        });
    }
    
    /**
     * init and implement performance for favourites
     */
    @SuppressLint("ResourceAsColor")
    private void initFavourBtnHandler() {
        initSearchLocationHandler(false, true, R.id.map_southbar_favour_add_fab, true);
        initPointOnMapHandler(TabAction.AddFavourit, R.id.map_southbar_favour_select_fab, true);

        favourBtn.setOnClickListener(new View.OnClickListener() {
            ColorStateList oriColor;
            @Override public void onClick(View v) {
              if (southBarFavourVP.getVisibility() == View.VISIBLE)
              {
                favourBtn.setBackgroundTintList(oriColor);
                southBarFavourVP.setVisibility(View.INVISIBLE);
                settingsBtn.setVisibility(View.VISIBLE);
                sideBarMenuVP.setVisibility(View.VISIBLE);
                controlBtn.setVisibility(View.VISIBLE);
              }
              else
              {
                oriColor = favourBtn.getBackgroundTintList();
                favourBtn.setBackgroundTintList(ColorStateList.valueOf(R.color.abc_color_highlight_material));
                southBarFavourVP.setVisibility(View.VISIBLE);
                settingsBtn.setVisibility(View.INVISIBLE);
                sideBarMenuVP.setVisibility(View.INVISIBLE);
                controlBtn.clearAnimation();
                controlBtn.setVisibility(View.INVISIBLE);
              }
            }
        });
    }
    
    /**
     * navigation settings implementation
     * <p>
     * settings clear button
     * <p>
     * settings search button
     */
    private void initNavSettingsHandler() {
        final ImageButton navSettingsClearBtn = (ImageButton) activity.findViewById(R.id.nav_settings_clear_btn);
        navSettingsClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                navSettingsVP.setVisibility(View.INVISIBLE);
                sideBarVP.setVisibility(View.VISIBLE);
            }
        });
        initTravelModeSetting();
        initSettingsFromItemHandler();
        initSettingsToItemHandler();
    }

    @SuppressWarnings("deprecation")
    private void setBgColor(View v, int color)
    {
      v.setBackgroundColor(activity.getResources().getColor(color));
    }

    /**
     * settings layout:
     * <p>
     * to item handler: when to item is clicked
     */
    private void initSettingsToItemHandler() {
        final ViewGroup toItemVG = (ViewGroup) activity.findViewById(R.id.map_nav_settings_to_item);
        toItemVG.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setBgColor(toItemVG, R.color.my_primary_light);
                        return true;
                    case MotionEvent.ACTION_UP:
                        setBgColor(toItemVG, R.color.my_primary);
                        navSettingsVP.setVisibility(View.INVISIBLE);
                        navSettingsToVP.setVisibility(View.VISIBLE);
                        return true;
                }
                return false;
            }
        });
        //        to layout
        //clear button
        ImageButton toLayoutClearBtn = (ImageButton) activity.findViewById(R.id.nav_settings_to_clear_btn);
        toLayoutClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                navSettingsVP.setVisibility(View.VISIBLE);
                navSettingsToVP.setVisibility(View.INVISIBLE);
            }
        });
        //  to layout: items
        initUseCurrentLocationHandler(false, R.id.map_nav_settings_to_current, true);
        initPointOnMapHandler(TabAction.EndPoint, R.id.map_nav_settings_to_point, true);
        initPointOnMapHandler(TabAction.EndPoint, R.id.nav_settings_to_sel_btn, false);
        initEnterLatLonHandler(false, R.id.map_nav_settings_to_latlon);
        initClearCurrentLocationHandler(false, R.id.nav_settings_to_del_btn);
        initSearchLocationHandler(false, true, R.id.map_nav_settings_to_favorite, true);
        initSearchLocationHandler(false, false, R.id.map_nav_settings_to_search, true);
        initSearchLocationHandler(false, true, R.id.nav_settings_to_fav_btn, false);
        initSearchLocationHandler(false, false, R.id.nav_settings_to_search_btn, false);
    }

    /**
     * add end point marker to map
     *
     * @param endPoint
     */
    private void addToMarker(GeoPoint endPoint, boolean recalculate) {
        MapHandler.getMapHandler().setStartEndPoint(activity, endPoint, false, recalculate);
    }

    /**
     * add start point marker to map
     *
     * @param startPoint
     */
    private void addFromMarker(GeoPoint startPoint, boolean recalculate) {
        MapHandler.getMapHandler().setStartEndPoint(activity, startPoint, true, recalculate);
    }

    /**
     * settings layout:
     * <p>
     * from item handler: when from item is clicked
     */
    private void initSettingsFromItemHandler() {
        final ViewGroup fromFieldVG = (ViewGroup) activity.findViewById(R.id.map_nav_settings_from_item);
//        fromFieldVG.setOnTouchListener(new View.OnTouchListener() {
//            @Override public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        setBgColor(fromFieldVG, R.color.my_primary_light);
//                        return true;
//                    case MotionEvent.ACTION_UP:
//                        //setBgColor(fromFieldVG, R.color.my_primary);
//                        navSettingsVP.setVisibility(View.INVISIBLE);
//                        navSettingsFromVP.setVisibility(View.VISIBLE);
//                        return true;
//                }
//                return false;
//            }
//        });
        ImageButton fromLayoutClearBtn = (ImageButton) activity.findViewById(R.id.nav_settings_from_clear_btn);
        fromLayoutClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                navSettingsVP.setVisibility(View.VISIBLE);
                navSettingsFromVP.setVisibility(View.INVISIBLE);
            }
        });
        initUseCurrentLocationHandler(true, R.id.map_nav_settings_from_current, true);
        initUseCurrentLocationHandler(true, R.id.nav_settings_from_cur_btn, false);
        initEnterLatLonHandler(true, R.id.map_nav_settings_from_latlon);
        initClearCurrentLocationHandler(true, R.id.nav_settings_from_del_btn);
        initPointOnMapHandler(TabAction.StartPoint, R.id.map_nav_settings_from_point, true);
        initSearchLocationHandler(true, true, R.id.map_nav_settings_from_favorite, true);
        initSearchLocationHandler(true, false, R.id.map_nav_settings_from_search, true);
        initSearchLocationHandler(true, true, R.id.nav_settings_from_fav_btn, false);
        initSearchLocationHandler(true, false, R.id.nav_settings_from_search_btn, false);
    }

    /**
     * Point item view group
     * <p>
     * preform actions when point on map item is clicked
     */
    private void initPointOnMapHandler(final TabAction tabType, int viewID, final boolean setBg) {
        final View pointItem = (View) activity.findViewById(viewID);
        pointItem.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (setBg) setBgColor(pointItem, R.color.my_primary_light);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (setBg) setBgColor(pointItem, R.color.my_primary);
                        if (tabType == TabAction.StartPoint)
                        { //touch on map
                          tabAction = TabAction.StartPoint;
                          navSettingsFromVP.setVisibility(View.INVISIBLE);
                          Toast.makeText(activity, "Touch on Map to choose your start Location",
                            Toast.LENGTH_SHORT).show();
                        }
                        else if (tabType == TabAction.EndPoint)
                        {
                          tabAction = TabAction.EndPoint;
                          navSettingsToVP.setVisibility(View.INVISIBLE);
                          Toast.makeText(activity, "Touch on Map to choose your destination Location",
                            Toast.LENGTH_SHORT).show();
                          if(map_status)((MapActivity)activity).onlineMapFragment.setMapTouchListener();
                        }
                        else
                        {
                          tabAction = TabAction.AddFavourit;
                          sideBarVP.setVisibility(View.INVISIBLE);
                          Toast.makeText(activity, "Touch on Map to choose your destination Location",
                            Toast.LENGTH_SHORT).show();
                        }
                        navSettingsVP.setVisibility(View.INVISIBLE);
                        MapHandler.getMapHandler().setNeedLocation(true);
                        return true;
                }
                return false;
            }
        });
    }
    
    private void initEnterLatLonHandler(final boolean isStartP, int viewID) {
      final View pointItem = (View) activity.findViewById(viewID);
      pointItem.setOnTouchListener(new View.OnTouchListener() {
          @Override public boolean onTouch(View v, MotionEvent event) {
              switch (event.getAction()) {
                  case MotionEvent.ACTION_DOWN:
                      setBgColor(pointItem, R.color.my_primary_light);
                      return true;
                  case MotionEvent.ACTION_UP:
                      setBgColor(pointItem, R.color.my_primary);
                      Intent intent = new Intent(activity, LatLonActivity.class);
                      OnClickAddressListener callbackListener = createPosSelectedListener(isStartP);
                      LatLonActivity.setPre(callbackListener);
                      activity.startActivity(intent);
                      return true;
              }
              return false;
          }
      });
    }

    private void initSearchLocationHandler(final boolean isStartP, final boolean fromFavourite, int viewID, final boolean setBg) {
      final View pointItem = (View) activity.findViewById(viewID);
      pointItem.setOnTouchListener(new View.OnTouchListener() {
          @Override public boolean onTouch(View v, MotionEvent event) {
              switch (event.getAction()) {
                  case MotionEvent.ACTION_DOWN:
                      if (setBg) setBgColor(pointItem, R.color.my_primary_light);
                      return true;
                  case MotionEvent.ACTION_UP:
                      if (setBg) setBgColor(pointItem, R.color.my_primary);
                      GeoPoint[] points = null;
                      String names[] = null;
                      if (fromFavourite)
                      {
                        points = new GeoPoint[3];
                        points[0] = Destination.getDestination().getStartPoint();
                        points[1] = Destination.getDestination().getEndPoint();
                        names = new String[2];
                        names[0] = Destination.getDestination().getStartPointName();
                        names[1] = Destination.getDestination().getEndPointName();
                        Location curLoc = MapActivity.getmCurrentLocation();
                        if (curLoc != null)
                        {
                          points[2] = new GeoPoint(curLoc.getLatitude(), curLoc.getLongitude());
                        }
                      }
//                      Intent intent = new PlaceAutocomplete.IntentBuilder()
//                              .accessToken(activity.getString(R.string.mapbox_access_token))
//                              .placeOptions(PlaceOptions.builder()
//                                      .backgroundColor(Color.parseColor("#EEEEEE"))
//                                      .limit(10)
//                                      .build(PlaceOptions.MODE_CARDS))
//                              .build(activity);
//                      ((MapActivity)activity).add_to_pos = -1;
//                      activity.startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
                      //startGeocodeActivity(points, names, isStartP, false);
                      Intent intent = new Intent(activity, SearchActivity.class);
                      ((MapActivity)activity).add_to_pos = -1;
                      activity.startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
                      return true;
              }
              return false;
          }
      });
  }
    
    /** Shows the GeocodeActivity, or Favourites, if points are not null.
     *  @param points The points to add as favourites, [0]=start [1]=end [2]=cur. **/
    public void startGeocodeActivity(GeoPoint[] points, String[] names, boolean isStartP, boolean autoEdit)
    {
      Intent intent = new Intent(activity, GeocodeActivity.class);
      OnClickAddressListener callbackListener = createPosSelectedListener(isStartP);
      GeocodeActivity.setPre(callbackListener, points, names, autoEdit);
      activity.startActivity(intent);
    }

    private OnClickAddressListener createPosSelectedListener(final boolean isStartP)
    {
      OnClickAddressListener callbackListener = new OnClickAddressListener()
      {
        @Override
        public void onClick(Address addr)
        {
          GeoPoint newPos = new GeoPoint(addr.getLatitude(), addr.getLongitude());
          String fullAddress = "";
          for (int i=0; i<5; i++)
          {
            String curAddr = addr.getAddressLine(i);
            if (curAddr == null || curAddr.isEmpty()) { continue; }
            if (!fullAddress.isEmpty()) { fullAddress = fullAddress + ", "; }
            fullAddress = fullAddress + curAddr;
          }
          doSelectCurrentPos(newPos, fullAddress, isStartP);
        }
      };
      return callbackListener;
    }
    
    public void doSelectCurrentPos(GeoPoint newPos, String text, boolean isStartP)
    {
      if (isStartP)
      {
        Destination.getDestination().setStartPoint(newPos, text);
        fromLocalET.setText(text);
        addFromMarker(Destination.getDestination().getStartPoint(), true);
        navSettingsFromVP.setVisibility(View.INVISIBLE);
          ((MapActivity)activity).addStartPoint(newPos);
      }
      else
      {
        Destination.getDestination().setEndPoint(newPos, text);
        toLocalET.setText(text);
        addToMarker(Destination.getDestination().getEndPoint(), true);
        navSettingsToVP.setVisibility(View.INVISIBLE);
      }
      setQuickButtonsClearVisible(isStartP, true);
      sideBarVP.setVisibility(View.INVISIBLE);
      if (!activateNavigator())
      {
        navSettingsVP.setVisibility(View.VISIBLE);
      }
      MapHandler.getMapHandler().centerPointOnMap(newPos, 0, 0, 0);
    }

    /**
     * current location handler: preform actions when current location item is clicked
     */
    private void initUseCurrentLocationHandler(final boolean isStartP, int viewID, final boolean setBg) {
        final View useCurrentLocal = (View) activity.findViewById(viewID);
        useCurrentLocal.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (setBg) setBgColor(useCurrentLocal, R.color.my_primary_light);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (setBg) setBgColor(useCurrentLocal, R.color.my_primary);
                        if (MapActivity.getmCurrentLocation() != null) {
                            GeoPoint newPos = new GeoPoint(MapActivity.getmCurrentLocation().getLatitude(),
                                    MapActivity.getmCurrentLocation().getLongitude());
                            String text = "" + newPos.getLatitude() + ", " + newPos.getLongitude();
                            if(((MapActivity)activity).offline_map_status) {
                                doSelectCurrentPos(newPos, text, isStartP);
                            }
                            fromLocalET.setText(text);
                            setQuickButtonsClearVisible(isStartP, true);
                            if(map_status)((MapActivity)activity).onlineMapFragment.addStartMarker();
                        } else {
                            Toast.makeText(activity, "Current Location not available, Check your GPS signal!",
                                    Toast.LENGTH_SHORT).show();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * current location handler: preform actions when clear location item is clicked
     */
    private void initClearCurrentLocationHandler(final boolean isStartP, int viewID) {
        final View useCurrentLocal = (View) activity.findViewById(viewID);
        useCurrentLocal.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Destination.getDestination().setStartPoint(null, null);
                addFromMarker(null, false);
                MapActivity._mapActivity.allPoints.set(0,new GHPoint());
                if(!map_status)MapHandler.getMapHandler().removeMarker(activity,MapActivity._mapActivity.allPoints);
                else {
                    MapActivity._mapActivity.onlineMapFragment.paintMarkers();
                    if(MapActivity._mapActivity.onlineMapFragment.isValidRoute())MapActivity._mapActivity.onlineMapFragment.fetchRoute();
                }
                    fromLocalET.setText("");
                useCurrentLocal.setVisibility(View.GONE);
                setQuickButtonsClearVisible(true, false);
            }
        });

    }
    
    void setQuickButtonsClearVisible(boolean isStartP, boolean vis)
    {
      int curVis = View.VISIBLE;
      if (isStartP)
      {
        if (!vis) { curVis = View.GONE; }
        if(vis)activity.findViewById(R.id.nav_settings_from_del_btn).setVisibility(curVis);
        if (vis) { curVis = View.GONE; }
        else { curVis = View.VISIBLE; }
        activity.findViewById(R.id.nav_settings_from_search_btn).setVisibility(curVis);
        //activity.findViewById(R.id.nav_settings_from_fav_btn).setVisibility(curVis);
        activity.findViewById(R.id.nav_settings_from_cur_btn).setVisibility(curVis);
      }
      else
      {
        if (!vis) { curVis = View.INVISIBLE; }
        activity.findViewById(R.id.nav_settings_to_del_btn).setVisibility(curVis);
        if (vis) { curVis = View.INVISIBLE; }
        else { curVis = View.VISIBLE; }
        activity.findViewById(R.id.nav_settings_to_search_btn).setVisibility(curVis);
        //activity.findViewById(R.id.nav_settings_to_fav_btn).setVisibility(curVis);
        activity.findViewById(R.id.nav_settings_to_sel_btn).setVisibility(curVis);
      }
    }
    
    /**
     * when use press on the screen to get a location form map
     *
     * @param latLong
     */
    @Override public void onPressLocation(GeoPoint latLong) {
        Log.e("sdfa","sdfsdf");
        if(!MapHandler.getMapHandler().isNeedLocation())return;
//        if (tabAction == TabAction.None) { return; }
        if (tabAction == TabAction.AddFavourit)
        {
          sideBarVP.setVisibility(View.VISIBLE);
          tabAction = TabAction.None;
          GeoPoint[] points = new GeoPoint[3];
          points[2] = latLong;
          String[] names = new String[3];
          names[2] = "Selected position";
          startGeocodeActivity(points, names, false, true);
          return;
        }
        String text = "" + latLong.getLatitude() + ", " + latLong.getLongitude();
        if (tabAction == TabAction.None){
            ((MapActivity)activity).setPointToMap(latLong);
        }
        doSelectCurrentPos(latLong, text, tabAction == TabAction.StartPoint);
        MapHandler.getMapHandler().setNeedLocation(false);
        tabAction = TabAction.None;
    }
    
    public void onPressLocationEndPoint(GeoPoint latLong)
    {
      tabAction = TabAction.EndPoint;
      onPressLocation(latLong);
    }

    /**
     * calculate path calculating (running) true NOT running or finished false
     *
     * @parashortestPathRunning
     */
    @Override public void pathCalculating(boolean calculatingPathActive) {
        if (!calculatingPathActive && Navigator.getNavigator().getGhResponse() != null) {
            if (!NaviEngine.getNaviEngine().isNavigating())
            {
              activateDirections();
            }
        }
    }

    /**
     * drawer polyline on map , active navigator instructions(directions) if on
     * @return True when pathfinder-routes will be shown.
     */
    private boolean activateNavigator() {
        GeoPoint startPoint = Destination.getDestination().getStartPoint();
        GeoPoint endPoint = Destination.getDestination().getEndPoint();
        if (startPoint != null && endPoint != null) {
            // show path finding process
            navSettingsVP.setVisibility(View.INVISIBLE);

            View pathfinding = activity.findViewById(R.id.map_nav_settings_path_finding);
            pathfinding.setVisibility(View.VISIBLE);
            if (Variable.getVariable().isDirectionsON()) {
                MapHandler.getMapHandler().setNeedPathCal(true);
                // Waiting for calculating
            }
            return true;
        }
        return false;
    }

    /**
     * active directions, and directions view
     */
    private void activateDirections() {
        RecyclerView instructionsRV;
        RecyclerView.Adapter<?> instructionsAdapter;
        RecyclerView.LayoutManager instructionsLayoutManager;

        instructionsRV = (RecyclerView) activity.findViewById(R.id.nav_instruction_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        instructionsRV.setHasFixedSize(true);

        // use a linear layout manager
        instructionsLayoutManager = new LinearLayoutManager(activity);
        instructionsRV.setLayoutManager(instructionsLayoutManager);

        // specify an adapter (see also next example)
        instructionsAdapter = new InstructionAdapter(Navigator.getNavigator().getGhResponse().getInstructions());
        instructionsRV.setAdapter(instructionsAdapter);
        initNavListView();
    }

    /**
     * navigation list view
     * <p>
     * make nav list view control button ready to use
     */
    private void initNavListView() {
        fillNavListSummaryValues();
        navSettingsVP.setVisibility(View.INVISIBLE);
        searchAddressBtn.setVisibility(View.INVISIBLE);
        navInstructionListVP.setVisibility(View.VISIBLE);
        ImageButton clearBtn, stopNavBtn;
        Button stopBtn, startNavBtn;
        stopBtn = (Button) activity.findViewById(R.id.nav_instruction_list_stop_btn);
        clearBtn = (ImageButton) activity.findViewById(R.id.nav_instruction_list_clear_btn);
        startNavBtn = (Button) activity.findViewById(R.id.nav_instruction_list_start_btn);
        stopNavBtn = (ImageButton) activity.findViewById(R.id.navtop_stop);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setMessage(R.string.stop_navigation_msg).setTitle(R.string.stop_navigation)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // stop!
                                Navigator.getNavigator().setOn(false);
                                navInstructionListVP.setVisibility(View.INVISIBLE);
                                navSettingsVP.setVisibility(View.VISIBLE);
                                searchAddressBtn.setVisibility(View.VISIBLE);
                                ((MapActivity)activity).clearAllPoints();
                                btn_nav_start.setVisibility(View.INVISIBLE);
                                btn_nav_stop.setVisibility(View.INVISIBLE);
                                dialog.dismiss();
                            }
                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.dismiss();
                    }
                });
                // Create the AlertDialog object and return it

                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                navInstructionListVP.setVisibility(View.INVISIBLE);
                navSettingsVP.setVisibility(View.INVISIBLE);
                sideBarVP.setVisibility(View.VISIBLE);
                searchAddressBtn.setVisibility(View.VISIBLE);
            }
        });

        startNavBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Navigator.getNavigator().setNaviStart(activity, true);
                Location curLoc = MapActivity.getmCurrentLocation();
                if (curLoc!=null)
                {
                  NaviEngine.getNaviEngine().updatePosition(activity, curLoc);
                }
            }
        });

        stopNavBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Navigator.getNavigator().setNaviStart(activity, false);
            }
        });
    }

    /**
     * fill up values for nav list summary
     */
    private void fillNavListSummaryValues() {
        initSpinner();
        initSummaryRecyclerView();
        TextView distance, time, arrive_time;
        distance = (TextView) activity.findViewById(R.id.nav_instruction_list_summary_distance_tv);
        time = (TextView) activity.findViewById(R.id.nav_instruction_list_summary_time_tv);
        arrive_time = (TextView) activity.findViewById(R.id.navtop_arrive_time);
        arrive_time.setText(Navigator.getNavigator().getDistance() + "  " + Navigator.getNavigator().getArriveTime());
        distance.setText(Navigator.getNavigator().getDistance());
        time.setText(Navigator.getNavigator().getTime());
    }
    private void initSummaryRecyclerView() {
        RecyclerView summary_recyclerview = (RecyclerView) activity.findViewById(R.id.points_recyclerview);
        SummaryAddressRecyclerViewAdapter summaryAdapter = new SummaryAddressRecyclerViewAdapter(R.layout.summary_row_address, ((MapActivity)activity).allPoints);
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity.getApplicationContext());
        summary_recyclerview.setLayoutManager(layoutManager);
        summary_recyclerview.setAdapter(summaryAdapter);
    }
    private void initSpinner() {
        ImageButton btn_foot = (ImageButton)activity.findViewById(R.id.nav_settings_foot_btn2);
        ImageButton btn_bike = (ImageButton)activity.findViewById(R.id.nav_settings_bike_btn2);
        ImageButton btn_car = (ImageButton)activity.findViewById(R.id.nav_settings_car_btn2);
        btn_foot.setBackgroundResource(R.drawable.nav_style_bg_white);
        btn_bike.setBackgroundResource(R.drawable.nav_style_bg_white);
        btn_car.setBackgroundResource(R.drawable.nav_style_bg_white);
        if(Navigator.getNavigator().getTravelModeArrayIndex()==0){
            btn_foot.setBackgroundResource(R.drawable.nav_style_bg);
        }else if(Navigator.getNavigator().getTravelModeArrayIndex()==1){
            btn_bike.setBackgroundResource(R.drawable.nav_style_bg);
        }else{
            btn_car.setBackgroundResource(R.drawable.nav_style_bg);
        }
        btn_foot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Navigator.getNavigator().getTravelModeArrayIndex()==0){return;}
                Navigator.getNavigator().setTravelModeArrayIndex(0);
                navSettingsVP.setVisibility(View.VISIBLE);
                searchAddressBtn.setVisibility(View.VISIBLE);
                navInstructionListVP.setVisibility(View.INVISIBLE);
                initTravelModeSetting();
                MapHandler.getMapHandler().recalcPath(activity);
                if(map_status){
                    if(MapActivity._mapActivity.onlineMapFragment.isValidRoute())MapActivity._mapActivity.onlineMapFragment.fetchRoute();
                }
            }
        });
        btn_bike.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Navigator.getNavigator().getTravelModeArrayIndex()==1){return;}
                Navigator.getNavigator().setTravelModeArrayIndex(1);
                navSettingsVP.setVisibility(View.VISIBLE);
                searchAddressBtn.setVisibility(View.VISIBLE);
                navInstructionListVP.setVisibility(View.INVISIBLE);
                initTravelModeSetting();
                MapHandler.getMapHandler().recalcPath(activity);
                if(map_status){
                    if(MapActivity._mapActivity.onlineMapFragment.isValidRoute())MapActivity._mapActivity.onlineMapFragment.fetchRoute();
                }
            }
        });
        btn_car.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Navigator.getNavigator().getTravelModeArrayIndex()==2){return;}
                Navigator.getNavigator().setTravelModeArrayIndex(2);
                navSettingsVP.setVisibility(View.VISIBLE);
                searchAddressBtn.setVisibility(View.VISIBLE);
                navInstructionListVP.setVisibility(View.INVISIBLE);
                initTravelModeSetting();
                MapHandler.getMapHandler().recalcPath(activity);
                if(map_status){
                    if(MapActivity._mapActivity.onlineMapFragment.isValidRoute())MapActivity._mapActivity.onlineMapFragment.fetchRoute();
                }
            }
        });
    }

    /**
     * set up travel mode
     */
    private void initTravelModeSetting() {
        final ImageButton footBtn, bikeBtn, carBtn;
        footBtn = (ImageButton) activity.findViewById(R.id.nav_settings_foot_btn);
        bikeBtn = (ImageButton) activity.findViewById(R.id.nav_settings_bike_btn);
        carBtn = (ImageButton) activity.findViewById(R.id.nav_settings_car_btn);
        footBtn.setBackgroundResource(R.drawable.nav_style_bg_white);
        bikeBtn.setBackgroundResource(R.drawable.nav_style_bg_white);
        carBtn.setBackgroundResource(R.drawable.nav_style_bg_white);
        // init travel mode
        if (Variable.getVariable().getTravelMode() == Variable.TravelMode.Foot)
        {
          footBtn.setBackgroundResource(R.drawable.nav_style_bg);
        }
        else if (Variable.getVariable().getTravelMode() == Variable.TravelMode.Bike)
        {
          bikeBtn.setBackgroundResource(R.drawable.nav_style_bg);
        }
        else if (Variable.getVariable().getTravelMode() == Variable.TravelMode.Car)
        {
          carBtn.setBackgroundResource(R.drawable.nav_style_bg);
        }

        //foot
        footBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                footBtn.setBackgroundResource(R.drawable.nav_style_bg);
                bikeBtn.setBackgroundResource(R.drawable.nav_style_bg_white);
                carBtn.setBackgroundResource(R.drawable.nav_style_bg_white);
                if (Variable.getVariable().getTravelMode() != Variable.TravelMode.Foot) {
                    Variable.getVariable().setTravelMode(Variable.TravelMode.Foot);
                    if (activateNavigator())
                    {
                      MapHandler.getMapHandler().recalcPath(activity);
                    }
                }
                if(map_status){
                    if(MapActivity._mapActivity.onlineMapFragment.isValidRoute())MapActivity._mapActivity.onlineMapFragment.fetchRoute();
                }
            }
        });
        //bike
        bikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                footBtn.setBackgroundResource(R.drawable.nav_style_bg_white);
                bikeBtn.setBackgroundResource(R.drawable.nav_style_bg);
                carBtn.setBackgroundResource(R.drawable.nav_style_bg_white);
                if (Variable.getVariable().getTravelMode() != Variable.TravelMode.Bike) {
                    Variable.getVariable().setTravelMode(Variable.TravelMode.Bike);
                    if (activateNavigator())
                    {
                      MapHandler.getMapHandler().recalcPath(activity);
                    }
                }
                if(map_status){
                    if(MapActivity._mapActivity.onlineMapFragment.isValidRoute())MapActivity._mapActivity.onlineMapFragment.fetchRoute();
                }
            }
        });
        // car
        carBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                footBtn.setBackgroundResource(R.drawable.nav_style_bg_white);
                bikeBtn.setBackgroundResource(R.drawable.nav_style_bg_white);
                carBtn.setBackgroundResource(R.drawable.nav_style_bg);
                if (Variable.getVariable().getTravelMode() != Variable.TravelMode.Car) {
                    Variable.getVariable().setTravelMode(Variable.TravelMode.Car);
                    if (activateNavigator())
                    {
                      MapHandler.getMapHandler().recalcPath(activity);
                    }
                }
                if(map_status){
                    if(MapActivity._mapActivity.onlineMapFragment.isValidRoute())MapActivity._mapActivity.onlineMapFragment.fetchRoute();
                }
            }
        });
    }

    /**
     * handler clicks on nav button
     */
    private void initNavBtnHandler() {
        navigationBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                sideBarVP.setVisibility(View.INVISIBLE);
//                if (Navigator.getNavigator().isOn() && !map_status) {
//                    navInstructionListVP.setVisibility(View.VISIBLE);
//                    searchAddressBtn.setVisibility(View.INVISIBLE);
//                } else {
                    navSettingsVP.setVisibility(View.VISIBLE);
//                }
            }
        });
    }


    /**
     * start button: control button handler FAB
     */

    private void initControlBtnHandler() {
        final ScaleAnimation anim = new ScaleAnimation(0, 1, 0, 1);
        anim.setFillBefore(true);
        anim.setFillAfter(true);
        anim.setFillEnabled(true);
        anim.setDuration(300);
        anim.setInterpolator(new OvershootInterpolator());

        controlBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (isMenuVisible()) {
                    setMenuVisible(false);
                    sideBarMenuVP.setVisibility(View.INVISIBLE);
                    favourBtn.setVisibility(View.INVISIBLE);
                    settingsBtn.setVisibility(View.INVISIBLE);
                    navigationBtn.setVisibility(View.INVISIBLE);
                    controlBtn.setImageResource(R.drawable.ic_keyboard_arrow_up_white_24dp);
                    controlBtn.startAnimation(anim);
                } else {
                    setMenuVisible(true);
                    sideBarMenuVP.setVisibility(View.VISIBLE);
                    favourBtn.setVisibility(View.VISIBLE);
                    settingsBtn.setVisibility(View.VISIBLE);
                    navigationBtn.setVisibility(View.VISIBLE);
                    controlBtn.setImageResource(R.drawable.ic_keyboard_arrow_down_white_24dp);
                    controlBtn.startAnimation(anim);
                }
            }
        });
    }

    /**
     * implement zoom btn
     */
    protected void initZoomControlHandler() {
        zoomInBtn.setImageResource(R.drawable.ic_add_white_24dp);
        zoomOutBtn.setImageResource(R.drawable.ic_remove_white_24dp);

        zoomInBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if(map_status)((MapActivity)activity).onlineMapFragment.changeMapZoom(true);
                else {
                    if(MapActivity._mapActivity.offlineMapFragment.mapView.map().getMapPosition().getZoomLevel() < ZOOM_MAX) {
                        doZoom(MapActivity._mapActivity.offlineMapFragment.mapView, true);
                    }
                }
            }
        });
        zoomOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if(map_status)((MapActivity)activity).onlineMapFragment.changeMapZoom(false);
                else{
                    if(MapActivity._mapActivity.offlineMapFragment.mapView.map().getMapPosition().getZoomLevel() > ZOOM_MIN){
                        doZoom(MapActivity._mapActivity.offlineMapFragment.mapView, false);
                    }
                }
            }
        });
    }
    
    void doZoom(MapView mapView, boolean doZoomIn)
    {
      MapPosition mvp = mapView.map().getMapPosition();
      int i = mvp.getZoomLevel();
      log("Zoom from " + mvp.getZoomLevel() + " scale=" + mvp.getScale());
      if (doZoomIn) { mvp.setZoomLevel(++i); mvp.setScale(mvp.getScale() * 1.1); /* roundoff err */ }
      else { mvp.setZoomLevel(--i); }
      log("Zoom to " + mvp.getZoomLevel());
      //Toast.makeText(activity.getApplicationContext(),mvp.getZoomLevel()+"", Toast.LENGTH_LONG).show();
      mapView.map().animator().animateTo(300, mvp);
    }

    /**
     * move map to my current location as the center of the screen
     */
    protected void initShowMyLocation() {
        showPositionBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (MapActivity.getmCurrentLocation() != null) {
                    showPositionBtn.setImageResource(R.drawable.ic_my_location_white_24dp);
                    MapHandler.getMapHandler().centerPointOnMap(
                            new GeoPoint(MapActivity.getmCurrentLocation().getLatitude(),
                                    MapActivity.getmCurrentLocation().getLongitude()), 0, 0, 0);

                    //                    mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(
                    //                            new LatLong(MapActivity.getmCurrentLocation().getLatitude(),
                    //                                    MapActivity.getmCurrentLocation().getLongitude()),
                    //                            mapView.getModel().mapViewPosition.getZoomLevel()));

                } else {
                    showPositionBtn.setImageResource(R.drawable.ic_location_searching_white_24dp);
                    Toast.makeText(activity, "No Location Available", Toast.LENGTH_SHORT).show();
                }
                ((MapActivity)activity).ensureLocationListener(false);
                if(map_status)((MapActivity)activity).onlineMapFragment.showCurrentPosition();
            }
        });
    }

    /**
     * @return side bar menu visibility status
     */
    public boolean isMenuVisible() {
        return menuVisible;
    }

    /**
     * side bar menu visibility
     *
     * @param menuVisible
     */
    public void setMenuVisible(boolean menuVisible) {
        this.menuVisible = menuVisible;
    }

    /**
     * the change on navigator: navigation is used or not
     *
     * @param on
     */
    @Override public void onStatusChanged(boolean on) {
//        if (on) {
            navigationBtn.setImageResource(R.drawable.ic_directions_white_24dp);
//        } else {
//            navigationBtn.setImageResource(R.drawable.ic_navigation_white_24dp);
//        }
    }
    
    @Override public void onNaviStart(boolean on) {
      navInstructionListVP.setVisibility(View.INVISIBLE);
      navSettingsVP.setVisibility(View.INVISIBLE);
      if (on) {
          sideBarVP.setVisibility(View.INVISIBLE);
          navTopVP.setVisibility(View.VISIBLE);
          navBottomVP.setVisibility(View.VISIBLE);
      } else {
          sideBarVP.setVisibility(View.VISIBLE);
          searchAddressBtn.setVisibility(View.VISIBLE);
          navTopVP.setVisibility(View.INVISIBLE);
          navBottomVP.setVisibility(View.GONE);
      }
    }

    /**
     * called from Map activity when onBackpressed
     *
     * @return false no actions will perform; return true MapActivity will be placed back in the activity stack
     */
    public boolean homeBackKeyPressed() {
        if (navSettingsVP.getVisibility() == View.VISIBLE) {
            navSettingsVP.setVisibility(View.INVISIBLE);
            sideBarVP.setVisibility(View.VISIBLE);
            return false;
        } else if (navSettingsFromVP.getVisibility() == View.VISIBLE) {
            navSettingsFromVP.setVisibility(View.INVISIBLE);
            navSettingsVP.setVisibility(View.VISIBLE);
            return false;
        } else if (navSettingsToVP.getVisibility() == View.VISIBLE) {
            navSettingsToVP.setVisibility(View.INVISIBLE);
            navSettingsVP.setVisibility(View.VISIBLE);
            return false;
        } else if (navInstructionListVP.getVisibility() == View.VISIBLE) {
            navInstructionListVP.setVisibility(View.INVISIBLE);
            sideBarVP.setVisibility(View.VISIBLE);
            searchAddressBtn.setVisibility(View.VISIBLE);
            return false;
        } else if (appSettings.getAppSettingsVP() != null &&
                appSettings.getAppSettingsVP().getVisibility() == View.VISIBLE) {
            appSettings.getAppSettingsVP().setVisibility(View.INVISIBLE);
            searchAddressBtn.setVisibility(View.VISIBLE);
            sideBarVP.setVisibility(View.VISIBLE);
            return false;
        } else if (NaviEngine.getNaviEngine().isNavigating()) {
            Navigator.getNavigator().setNaviStart(activity, false);
            return false;
        }  else if (southBarFavourVP.getVisibility() == View.VISIBLE) {
            favourBtn.callOnClick();
            return false;
        } else {
            return true;
        }
    }

    public AppSettings getAppSettings() { return appSettings; }

    private void log(String str) {
        Log.i(MapActions.class.getName(), str);
    }

}