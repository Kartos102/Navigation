package com.junjunguo.phialmaps.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.graphhopper.PathWrapper;
import com.graphhopper.util.shapes.GHPoint;
import com.junjunguo.phialmaps.R;
import com.junjunguo.phialmaps.fragments.Dialog;
import com.junjunguo.phialmaps.fragments.MultiAddressRecyclerViewAdapter;
import com.junjunguo.phialmaps.fragments.OfflineMapFragment;
import com.junjunguo.phialmaps.fragments.OnlineMapFragment;
import com.junjunguo.phialmaps.map.Destination;
import com.junjunguo.phialmaps.map.MapHandler;
import com.junjunguo.phialmaps.map.Navigator;
import com.junjunguo.phialmaps.map.Tracking;
import com.junjunguo.phialmaps.model.listeners.OnClickAddressListener;
import com.junjunguo.phialmaps.util.IO;
import com.junjunguo.phialmaps.util.SetStatusBarColor;
import com.junjunguo.phialmaps.util.Variable;
import com.junjunguo.phialmaps.navigator.NaviEngine;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
//import com.mapbox.mapboxsdk.location.LocationComponent;
//import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
//import com.mapbox.mapboxsdk.location.modes.CameraMode;
//import com.mapbox.mapboxsdk.location.modes.RenderMode;
//import com.mapbox.mapboxsdk.maps.Style;
//import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
//import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.villoren.android.kalmanlocationmanager.lib.KalmanLocationManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.oscim.core.GeoPoint;

import com.villoren.android.kalmanlocationmanager.lib.KalmanLocationManager.UseProvider;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

public class MapActivity extends AppCompatActivity implements LocationListener,
         PermissionsListener {

    public static MapActivity _mapActivity;
    enum PermissionStatus { Enabled, Disabled, Requesting, Unknown };
    private static Location mCurrentLocation;
    private static boolean mapAlive = false;
    private Location mLastLocation;
    private MapActions mapActions;
    private PermissionsManager permissionsManager;
    private LocationManager locationManager;
    private KalmanLocationManager kalmanLocationManager;
    private PermissionStatus locationListenerStatus = PermissionStatus.Unknown;
    private String lastProvider;
    public List<GHPoint> allPoints;
    private RecyclerView nav_to_addresses_recyclerview;
    public MultiAddressRecyclerViewAdapter multiAddressRecyclerViewAdapter;
    private static final long GPS_TIME = 1000;
    private static final long NET_TIME = 5000;
    private static final long FILTER_TIME = 40;
    private final static int REQUEST_CODE_AUTOCOMPLETE = 106;
    private ViewGroup inclusionViewGroup;
    public PathWrapper resp;
    public static Bundle _savedInstanceState;
    public int add_to_pos;
    public OnlineMapFragment onlineMapFragment;
    public OfflineMapFragment offlineMapFragment;
    public boolean offline_map_status = false;
    private boolean reload_flag = false;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences("Phial Map", MODE_PRIVATE);
        reload_flag = prefs.getBoolean("reloadFlag", false);
        _savedInstanceState = savedInstanceState;
        _mapActivity = this;
        lastProvider = null;
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_map);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        kalmanLocationManager = new KalmanLocationManager(this);
        kalmanLocationManager.setMaxPredictTime(10000);
        Variable.getVariable().setContext(getApplicationContext());
//        mapView = (MapView)findViewById(R.id.offline_mapview);
//        mapView.setClickable(true);
//        MapHandler.getMapHandler()
//                .init(mapView, Variable.getVariable().getCountry(), Variable.getVariable().getMapsFolder());
//        try
//        {
//            MapHandler.getMapHandler().loadMap(new File(Variable.getVariable().getMapsFolder().getAbsolutePath(),
//                Variable.getVariable().getCountry() + "-gh"), this);
//            getIntent().putExtra("com.junjunguo.pocketmaps.activities.MapActivity.SELECTNEWMAP", false);
//            offline_map_status = true;
//        }
//        catch (Exception e)
//        {
//            logUser("Map file seems corrupt!\nPlease try to re-download.");
//            log("Error while loading map!");
//            e.printStackTrace();
//            offline_map_status = false;
////          finish();
////          Intent intent = new Intent(this, MainActivity.class);
////          intent.putExtra("com.junjunguo.pocketmaps.activities.MapActivity.SELECTNEWMAP", true);
////          startActivity(intent);
////          return;
//        }
        File defMapsDir = IO.getDefaultBaseDirectory(this);
        if (defMapsDir!=null) Variable.getVariable().setBaseFolder(defMapsDir.getPath());
        boolean offlinemap = getIntent().getBooleanExtra("offlineMap", false);
        if(!offlinemap) {
            onlineMapFragment = OnlineMapFragment.newInstance();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mapbox_layout, onlineMapFragment);
            ft.commitAllowingStateLoss();
        }else {
            offlineMapFragment = OfflineMapFragment.newInstance();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mapbox_layout, offlineMapFragment);
            ft.commitAllowingStateLoss();
        }
        customMapView();
        ensureLastLocationInit();
        updateCurrentLocation(null);
        checkGpsAvailability();
        mapAlive = true;
        allPoints = new ArrayList<>();
        if(!reload_flag) {
            allPoints.add(new GHPoint());
            allPoints.add(new GHPoint());
        }else{
            String point_str = prefs.getString("allPoints", "");
            for (String point : point_str.split(",")) {
                Double lat = Double.valueOf(point.split(":")[0]);
                Double lng = Double.valueOf(point.split(":")[1]);
                allPoints.add(new GHPoint(lat, lng));
            }
            SharedPreferences.Editor editor = getSharedPreferences("Phial Map", MODE_PRIVATE).edit();
            editor.putBoolean("reloadFlag", false);
            editor.apply();
            if(allPoints.get(0).isValid()) {
                ((TextView) findViewById(R.id.nav_settings_from_local_et)).setText(allPoints.get(0).lat + ", " + allPoints.get(0).lon);
                mapActions.setQuickButtonsClearVisible(true, true);
            }
        }
        nav_to_addresses_recyclerview = (RecyclerView)findViewById(R.id.nav_to_addresses_recyclerview);
        multiAddressRecyclerViewAdapter = new MultiAddressRecyclerViewAdapter(this,R.layout.row_address_to,allPoints);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        nav_to_addresses_recyclerview.setLayoutManager(layoutManager);
        nav_to_addresses_recyclerview.setAdapter(multiAddressRecyclerViewAdapter);
        if(offlinemap)findViewById(R.id.btn_map_status).performClick();
        findViewById(R.id.map_show_my_position_fab).performClick();
        Log.e("create","create");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
        } else {
            finish();
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        _savedInstanceState = outState;
    }

    public void addStartPoint(GeoPoint _point){
        allPoints.set(0,new GHPoint(_point.getLatitude(), _point.getLongitude()));
        multiAddressRecyclerViewAdapter.notifyDataSetChanged();
    }
    public void clearAllPoints(){
        allPoints.clear();
        allPoints.add(new GHPoint());
        allPoints.add(new GHPoint());
        multiAddressRecyclerViewAdapter.notifyDataSetChanged();
        MapHandler.getMapHandler().endMarker = null;
        MapHandler.getMapHandler().startMarker = null;
        Destination.getDestination().setEndPoint(null,"");
        Destination.getDestination().setStartPoint(null,"");
        findViewById(R.id.nav_settings_from_del_btn).performClick();
        MapHandler.getMapHandler().removeMarker(this, allPoints);
    }
    public void removeTOPoint(int position){
        if(allPoints.get(position+1).isValid()){
            allPoints.remove(position+1);
            if(!mapActions.map_status)MapHandler.getMapHandler().removeMarker(this, allPoints);
        }
        else allPoints.remove(position+1);
        multiAddressRecyclerViewAdapter.notifyDataSetChanged();
        if(!isValidRoute()){
            getMapActions().btn_nav_start.setVisibility(View.INVISIBLE);
            getMapActions().btn_nav_stop.setVisibility(View.INVISIBLE);
        }
        if(mapActions.map_status) {
            onlineMapFragment.paintMarkers();
            if (onlineMapFragment.isValidRoute()) onlineMapFragment.fetchRoute();
        }
    }
    public boolean isValidRoute(){
        int available_count = 0;
        for(int i=0;i<allPoints.size();i++){
            if(allPoints.get(i).isValid())available_count++;
        }
        return available_count >= 2;
    }
    public void addToAddressPoint(){
        allPoints.add(new GHPoint());
        multiAddressRecyclerViewAdapter.notifyDataSetChanged();
    }
    public void setPointToMap(GeoPoint _point){
        allPoints.set(add_to_pos+1, new GHPoint(_point.getLatitude(), _point.getLongitude()));
        multiAddressRecyclerViewAdapter.notifyDataSetChanged();
        add_to_pos = -2;
    }
    public void searchToAddress(int position){
        add_to_pos = position;
        Intent intent = new Intent(this, SearchActivity.class);
        startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
    }
    private OnClickAddressListener createPosSelectedListener(final int pos)
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
                allPoints.set(pos+1, new GHPoint(newPos.getLatitude(), newPos.getLongitude()));
                multiAddressRecyclerViewAdapter.notifyDataSetChanged();
                getMapActions().doSelectCurrentPos(newPos, fullAddress, false);
            }
        };
        return callbackListener;
    }
    public void setToPoint(int pos){
        add_to_pos = pos;
        findViewById(R.id.nav_settings_to_layout).setVisibility(View.INVISIBLE);
        Toast.makeText(this, "Touch on Map to choose your destination Location",
                Toast.LENGTH_SHORT).show();
        findViewById(R.id.nav_settings_layout).setVisibility(View.INVISIBLE);
        if(!mapActions.map_status)MapHandler.getMapHandler().setNeedLocation(true);
        else onlineMapFragment.setMapTouchListener();
    }
    public void changeMapStatus(boolean status){
        if(status){
            if(onlineMapFragment==null)onlineMapFragment = OnlineMapFragment.newInstance();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mapbox_layout, onlineMapFragment);
            ft.commitAllowingStateLoss();
        }
        else {
            if(offlineMapFragment==null)offlineMapFragment = OfflineMapFragment.newInstance();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mapbox_layout, offlineMapFragment);
            ft.commitAllowingStateLoss();
        }
    }
    public void ensureLocationListener(boolean showMsgEverytime)
    {
      if (locationListenerStatus == PermissionStatus.Disabled) { return; }
      if (locationListenerStatus != PermissionStatus.Enabled)
      {
        boolean f_loc = Permission.checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, this);
        if (!f_loc)
        {
          if (locationListenerStatus == PermissionStatus.Requesting)
          {
            locationListenerStatus = PermissionStatus.Disabled;
            return;
          }
          locationListenerStatus = PermissionStatus.Requesting;
          String[] permissions = new String[2];
          permissions[0] = android.Manifest.permission.ACCESS_FINE_LOCATION;
          permissions[1] = android.Manifest.permission.ACCESS_COARSE_LOCATION;
          Permission.startRequest(permissions, false, this);
          return;
        }
      }
      try
      {
        if (Variable.getVariable().isSmoothON()) {
          locationManager.removeUpdates(this);
          kalmanLocationManager.requestLocationUpdates(UseProvider.GPS, FILTER_TIME, GPS_TIME, NET_TIME, this, false);
          lastProvider = KalmanLocationManager.KALMAN_PROVIDER;
        } else {
          kalmanLocationManager.removeUpdates(this);
          Criteria criteria = new Criteria();
          criteria.setAccuracy(Criteria.ACCURACY_FINE);
          String provider = locationManager.getBestProvider(criteria, true);
          if (provider == null) {
            lastProvider = null;
            locationManager.removeUpdates(this);
            return;
          } else if (provider.equals(lastProvider)) {
            if (showMsgEverytime) {
            }
            return;
          }
          locationManager.removeUpdates(this);
          lastProvider = provider;
          locationManager.requestLocationUpdates(provider, 1000, 1, this);
        }
        locationListenerStatus = PermissionStatus.Enabled;
      }
      catch (SecurityException ex)
      {
        logUser("Location_Service not allowed by user!");
      }
    }

    /**
     * inject and inflate activity map content to map activity context and bring it to front
     */
    private void customMapView() {
        inclusionViewGroup = (ViewGroup) findViewById(R.id.custom_map_view_layout);
        View inflate = LayoutInflater.from(this).inflate(R.layout.activity_map_content, null);
        inclusionViewGroup.addView(inflate);

        //inclusionViewGroup.getParent().bringChildToFront(inclusionViewGroup);
        new SetStatusBarColor().setSystemBarColor(findViewById(R.id.statusBarBackgroundMap),
                getResources().getColor(R.color.my_primary), this);
        mapActions = new MapActions(this);
    }

    /**
     * check if GPS enabled and if not send user to the GSP settings
     */
    private void checkGpsAvailability() {
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            Dialog.showGpsSelector(this);
        }
    }

    /**
     * Updates the users location based on the location
     *
     * @param location Location
     */
    private void updateCurrentLocation(Location location) {
        if (location != null) {
            mCurrentLocation = location;
        } else if (mLastLocation != null && mCurrentLocation == null) {
            mCurrentLocation = mLastLocation;
        }
        if (mCurrentLocation != null) {
            GeoPoint mcLatLong = new GeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            if (Tracking.getTracking(getApplicationContext()).isTracking()) {
                MapHandler.getMapHandler().addTrackPoint(this, mcLatLong);
                Tracking.getTracking(getApplicationContext()).addPoint(mCurrentLocation, mapActions.getAppSettings());
            }
            if (NaviEngine.getNaviEngine().isNavigating())
            {
              NaviEngine.getNaviEngine().updatePosition(this, mCurrentLocation);
            }
//            if(mapActions.map_status)MapActivity._mapActivity.fragment.showCurrentPosition();
            MapHandler.getMapHandler().setCustomPoint(this, mcLatLong);
            mapActions.showPositionBtn.setImageResource(R.drawable.ic_my_location_white_24dp);
        } else {
            mapActions.showPositionBtn.setImageResource(R.drawable.ic_location_searching_white_24dp);
        }
    }
    
    public MapActions getMapActions() { return mapActions; }

    @Override public void onBackPressed() {
        boolean back = mapActions.homeBackKeyPressed();
        if (back) {
            moveTaskToBack(true);
        }
    }

    @Override protected void onStart() {
        super.onStart();
        Log.e("onStart","onStart");
    }

    @Override public void onResume() {
        super.onResume();
        ensureLocationListener(true);
        ensureLastLocationInit();
        Log.e("onResume","onResume");
    }

    @Override protected void onPause() {
        super.onPause();
        Log.e("onPause","onPause");
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.e("onLowMemory","onLowMemory");
    }
    @Override protected void onStop() {
        super.onStop();
        Log.e("stop","stop");
        // Remove location updates is not needed for tracking
        if (!Tracking.getTracking(getApplicationContext()).isTracking()) {
          locationManager.removeUpdates(this);
          kalmanLocationManager.removeUpdates(this);
          lastProvider = null;
        }
        if(offline_map_status) {
            if (mCurrentLocation != null) {
                GeoPoint geoPoint = offlineMapFragment.mapView.map().getMapPosition().getGeoPoint();
                Variable.getVariable().setLastLocation(geoPoint);
            }
            if (offlineMapFragment.mapView != null)
                Variable.getVariable().setLastZoomLevel(offlineMapFragment.mapView.map().getMapPosition().getZoomLevel());
            Variable.getVariable().saveVariables(Variable.VarType.Base);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {

            Double lat = data.getDoubleExtra("lat",0.0);
            Double lng = data.getDoubleExtra("lng",0.0);
            if(add_to_pos != -2){
                allPoints.set(add_to_pos + 1, new GHPoint(lat,lng));
            }
            else {
                if (allPoints.get(allPoints.size() - 1).isValid())
                    allPoints.add(new GHPoint(lat,lng));
                else
                    allPoints.set(allPoints.size() - 1, new GHPoint(lat, lng));
            }
            multiAddressRecyclerViewAdapter.notifyDataSetChanged();
            Boolean isStart = false;
            String pos_str = lat + ", " + lng;
            if(add_to_pos==-1)isStart = true;
            if(offline_map_status)getMapActions().doSelectCurrentPos(new GeoPoint(lat,lng), pos_str, isStart);
            if(mapActions.map_status) {
                onlineMapFragment.paintMarkers();
                if (onlineMapFragment.isValidRoute()) onlineMapFragment.fetchRoute();
            }
        }
        add_to_pos = -2;
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        Log.e("onDestroy","onDestroy");

        mapAlive = false;
        locationManager.removeUpdates(this);
        kalmanLocationManager.removeUpdates(this);
        lastProvider = null;
        if (MapHandler.getMapHandler().getHopper() != null) MapHandler.getMapHandler().getHopper().close();
        MapHandler.getMapHandler().setHopper(null);
        Navigator.getNavigator().setOn(false);
        MapHandler.reset();
        Destination.getDestination().setStartPoint(null, null);
        Destination.getDestination().setEndPoint(null, null);
        System.gc();
    }

    /**
     * @return my currentLocation
     */
    public static Location getmCurrentLocation() {
        return mCurrentLocation;
    }

    private void ensureLastLocationInit()
    {
      if (mLastLocation != null) { return; }
      try
      {
        Location lonet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (lonet != null) { mLastLocation = lonet; return; }
      }
      catch (SecurityException|IllegalArgumentException e)
      {
        log("NET-Location is not supported: " + e.getMessage());
      }
      try
      {
        Location logps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);        
        if (logps != null) { mLastLocation = logps; return; }
      }
      catch (SecurityException|IllegalArgumentException e)
      {
        log("GPS-Location is not supported: " + e.getMessage());
      }
    }

    /**
     * Called when the location has changed.
     * <p/>
     * <p> There are no restrictions on the use of the supplied Location object.
     *
     * @param location The new location, as a Location object.
     */
    @Override public void onLocationChanged(Location location) {
        updateCurrentLocation(location);
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override public void onProviderEnabled(String provider) {
        logUser("LocationService is turned on!!");
    }

    @Override public void onProviderDisabled(String provider) {
        logUser("LocationService is turned off!!");
    }
    
    /** Map was startet and until now not stopped! **/
    public static boolean isMapAlive() { return mapAlive; }
    public static void isMapAlive_preFinish() { mapAlive = false; }

    /**
     * send message to logcat
     *
     * @param str
     */
    private void log(String str) {
        Log.i(this.getClass().getName(), str);
    }
    
    private void logUser(String str) {
      Log.i(this.getClass().getName(), str);
      try
      {
        Toast.makeText(getBaseContext(), str, Toast.LENGTH_SHORT).show();
      }
      catch (Exception e) { e.printStackTrace(); }
    }
}
