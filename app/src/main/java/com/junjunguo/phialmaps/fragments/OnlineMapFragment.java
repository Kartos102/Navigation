package com.junjunguo.phialmaps.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.graphhopper.util.shapes.GHPoint;
import com.junjunguo.phialmaps.R;
import com.junjunguo.phialmaps.activities.MainActivity;
import com.junjunguo.phialmaps.activities.MapActivity;
import com.junjunguo.phialmaps.activities.SimplifiedCallback;
import com.junjunguo.phialmaps.map.Navigator;
import com.junjunguo.phialmaps.util.SetStatusBarColor;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.exceptions.InvalidLatLngBoundsException;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.ui.v5.route.OnRouteSelectionChangeListener;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.utils.LocaleUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;


public class OnlineMapFragment extends Fragment {
    public MapView mapView;
    public MapboxMap _mapboxMap;
    private NavigationMapRoute mapRoute;
    private int current_zoom_level = 15;
    private DirectionsRoute route;
    private static final int CAMERA_ANIMATION_DURATION = 1000;
    private static final int DEFAULT_CAMERA_ZOOM = 15;
    private MapboxMap.OnMapClickListener clickListener;
    private LocaleUtils localeUtils;
    private final int[] padding = new int[]{50, 50, 50, 50};
    private Marker cur_marker;
    public OnlineMapFragment() {
    }

    public static OnlineMapFragment newInstance() {
        OnlineMapFragment fragment = new OnlineMapFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Mapbox.getInstance(this.getActivity(), getString(R.string.mapbox_access_token));
        View rootView = inflater.inflate(R.layout.fragment_online_map, container, false);
        mapView = (MapView) rootView.findViewById(R.id.online_map);
        mapView.setStyleUrl(getString(R.string.map_view_styleUrl));
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                _mapboxMap = mapboxMap;
                mapRoute = new NavigationMapRoute(mapView, _mapboxMap);
                mapRoute.setOnRouteSelectionChangeListener(new OnRouteSelectionChangeListener() {
                    @Override
                    public void onNewPrimaryRouteSelected(DirectionsRoute directionsRoute) {
                        route = directionsRoute;
                    }
                });
                showCurrentPosition();
                paintMarkers();
                if (isValidRoute()) fetchRoute();
            }
        });
        this.localeUtils = new LocaleUtils();
        new SetStatusBarColor().setSystemBarColor(rootView.findViewById(R.id.statusBarBackgroundMap2),
                getResources().getColor(R.color.my_primary), getActivity());
        return rootView;
    }
    public void setMapTouchListener() {
        MapboxMap.OnMapClickListener r0 = new MapboxMap.OnMapClickListener() {
            public void onMapClick(LatLng point) {
                MarkerOptions markerOptions = new MarkerOptions();
                LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
                MapActivity mapActivity = MapActivity._mapActivity;
                mapActivity.allPoints.set(mapActivity.add_to_pos + 1, new GHPoint(Double.parseDouble(String.format("%.6f", new Object[]{Double.valueOf(point.getLatitude())})), Double.parseDouble(String.format("%.6f", new Object[]{Double.valueOf(point.getLongitude())}))));
                MapActivity._mapActivity.multiAddressRecyclerViewAdapter.notifyDataSetChanged();
                markerOptions.position(latLng);
                markerOptions.icon(IconFactory.getInstance(OnlineMapFragment.this.getContext()).fromResource(R.drawable.ic_location_end_36dp));
                OnlineMapFragment.this._mapboxMap.addMarker(markerOptions);
                OnlineMapFragment.this.getActivity().findViewById(R.id.map_sidebar_layout).setVisibility(View.VISIBLE);
                OnlineMapFragment.this._mapboxMap.removeOnMapClickListener(OnlineMapFragment.this.clickListener);
//                if(MapActivity._mapActivity.offline_map_status)MapActivity._mapActivity.getMapActions().doSelectCurrentPos(new GeoPoint(point.getLatitude(), point.getLongitude()), String.format("%.6f", new Object[]{Double.valueOf(point.getLatitude())}) + ", " + String.format("%.6f", new Object[]{Double.valueOf(point.getLongitude())}), false);
                if (OnlineMapFragment.this.isValidRoute()) {
                    OnlineMapFragment.this.fetchRoute();
                }
            }
        };
        this.clickListener = r0;
        this._mapboxMap.addOnMapClickListener(r0);
    }
    public void paintMarkers() {
        _mapboxMap.clear();
        for (int i = 0; i < MapActivity._mapActivity.allPoints.size(); i++) {
            if (i == 0 && MapActivity._mapActivity.allPoints.get(i).isValid()) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(MapActivity._mapActivity.allPoints.get(i).lat, MapActivity._mapActivity.allPoints.get(i).lon));
                markerOptions.icon(IconFactory.getInstance(getContext()).fromResource(R.drawable.ic_location_start_36dp));
                this._mapboxMap.addMarker(markerOptions);
//                if(MapActivity._mapActivity.offline_map_status && MapHandler.getMapHandler().startMarker==null)MapActivity._mapActivity.getMapActions().doSelectCurrentPos(new GeoPoint(MapActivity._mapActivity.allPoints.get(i).lat, MapActivity._mapActivity.allPoints.get(i).lon), String.format("%.6f", new Object[]{Double.valueOf(MapActivity._mapActivity.allPoints.get(i).lat)}) + ", " + String.format("%.6f", new Object[]{Double.valueOf(MapActivity._mapActivity.allPoints.get(i).lon)}), true);
            } else if (MapActivity._mapActivity.allPoints.get(i).isValid()) {
                MarkerOptions markerOptions2 = new MarkerOptions();
                markerOptions2.position(new LatLng(MapActivity._mapActivity.allPoints.get(i).lat, MapActivity._mapActivity.allPoints.get(i).lon));
                markerOptions2.icon(IconFactory.getInstance(getContext()).fromResource(R.drawable.ic_location_end_36dp));
                this._mapboxMap.addMarker(markerOptions2);
//                if(MapActivity._mapActivity.offline_map_status && MapHandler.getMapHandler().endMarker==null)MapActivity._mapActivity.getMapActions().doSelectCurrentPos(new GeoPoint(MapActivity._mapActivity.allPoints.get(i).lat, MapActivity._mapActivity.allPoints.get(i).lon), String.format("%.6f", new Object[]{Double.valueOf(MapActivity._mapActivity.allPoints.get(i).lat)}) + ", " + String.format("%.6f", new Object[]{Double.valueOf(MapActivity._mapActivity.allPoints.get(i).lon)}), false);
            }
        }
        MarkerOptions markerOptions = new MarkerOptions();
        LatLng latLng = new LatLng(MapActivity.getmCurrentLocation().getLatitude(), MapActivity.getmCurrentLocation().getLongitude());
        markerOptions.position(latLng);
        markerOptions.icon(IconFactory.getInstance(getContext()).fromResource(R.drawable.ic_my_location_dark_36dp));
        cur_marker = _mapboxMap.addMarker(markerOptions);
    }

    public void showCurrentPosition() {
        if(_mapboxMap==null || MapActivity.getmCurrentLocation()==null)return;
        if(cur_marker!=null)_mapboxMap.removeMarker(cur_marker);
        animateCamera(new LatLng(MapActivity.getmCurrentLocation().getLatitude(), MapActivity.getmCurrentLocation().getLongitude()));
        MarkerOptions markerOptions = new MarkerOptions();
        LatLng latLng = new LatLng(MapActivity.getmCurrentLocation().getLatitude(), MapActivity.getmCurrentLocation().getLongitude());
        markerOptions.position(latLng);
        markerOptions.icon(IconFactory.getInstance(getContext()).fromResource(R.drawable.ic_my_location_dark_36dp));
        cur_marker = _mapboxMap.addMarker(markerOptions);
    }

    private void animateCamera(LatLng point) {
        if(_mapboxMap!=null)this._mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15.0d), 1000);
    }
    public boolean isValidRoute(){
        route = null;
        mapRoute.removeRoute();
        return MapActivity._mapActivity.isValidRoute();
    }
    public void fetchRoute(){
        NavigationRoute.Builder builder = NavigationRoute.builder(getActivity())
                .accessToken("pk." + getString(R.string.gh_key))
                .baseUrl(getString(R.string.base_url))
                .user("gh")
                .alternatives(true);
        for (int i = 0; i < MapActivity._mapActivity.allPoints.size(); i++) {
            Point p = Point.fromLngLat(MapActivity._mapActivity.allPoints.get(i).lon, MapActivity._mapActivity.allPoints.get(i).lat);
            if (i == 0) {
                builder.origin(p);
            } else if (i < MapActivity._mapActivity.allPoints.size() - 1) {
                builder.addWaypoint(p);
            } else {
                builder.destination(p);
            }
        }
        String dir_pro = DirectionsCriteria.PROFILE_WALKING;
        if(Navigator.getNavigator().getTravelModeArrayIndex()==1){
            dir_pro = DirectionsCriteria.PROFILE_CYCLING;
        }else if(Navigator.getNavigator().getTravelModeArrayIndex()==2){
            dir_pro = DirectionsCriteria.PROFILE_DRIVING;
        }
        builder.language(localeUtils.inferDeviceLocale(getContext())).voiceUnits(localeUtils.getUnitTypeForDeviceLocale(getContext())).profile(dir_pro);
        builder.build().getRoute(new SimplifiedCallback() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                if (validRouteResponse(response)) {
                    Log.d("routesss",""+response.body().routes().get(0));
                    MapActivity._mapActivity.findViewById(R.id.btn_nav_start).setVisibility(View.VISIBLE);
                    MapActivity._mapActivity.findViewById(R.id.btn_nav_stop).setVisibility(View.VISIBLE);
                    route = response.body().routes().get(0);
                    mapRoute.addRoutes(response.body().routes());
                    boundCameraToRoute();
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                super.onFailure(call, throwable);
                //Snackbar.make(mapView, R.string.error_calculating_route, Snackbar.LENGTH_LONG).show();
            }
        });
    }
    public void addStartMarker() {
        MarkerOptions markerOptions = new MarkerOptions();
        LatLng latLng = new LatLng(MapActivity.getmCurrentLocation().getLatitude(), MapActivity.getmCurrentLocation().getLongitude());
        MapActivity._mapActivity.allPoints.set(0, new GHPoint(MapActivity.getmCurrentLocation().getLatitude(), MapActivity.getmCurrentLocation().getLongitude()));
        markerOptions.position(latLng);
        markerOptions.icon(IconFactory.getInstance(getContext()).fromResource(R.drawable.ic_location_start_36dp));
        _mapboxMap.addMarker(markerOptions);
        if (isValidRoute()) fetchRoute();
    }
    public void _launchNavigationWithRoute() {
        if(route==null)return;
        String dir_pro = DirectionsCriteria.PROFILE_WALKING;
        if(Navigator.getNavigator().getTravelModeArrayIndex()==1){
            dir_pro = DirectionsCriteria.PROFILE_CYCLING;
        }else if(Navigator.getNavigator().getTravelModeArrayIndex()==2){
            dir_pro = DirectionsCriteria.PROFILE_DRIVING;
        }
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("Phial Map", MODE_PRIVATE).edit();
        editor.putBoolean("reloadFlag", true);
        String point_str = "";
        for(GHPoint point : MapActivity._mapActivity.allPoints){
            if(point_str!="")point_str += ",";
            point_str += point.lat + ":" + point.lon;
        }
        editor.putString("allPoints",point_str);
        editor.apply();
        NavigationLauncherOptions.Builder optionsBuilder = NavigationLauncherOptions.builder()
                .shouldSimulateRoute(false)
                .directionsProfile(dir_pro)
                .waynameChipEnabled(false);

        optionsBuilder.directionsRoute(route);
        NavigationLauncher.startNavigation(getActivity(), optionsBuilder.build());
    }

    public void changeMapZoom(boolean status){
        if(status)current_zoom_level++;
        else current_zoom_level--;
        _mapboxMap.setCameraPosition(new CameraPosition.Builder()
                .zoom(current_zoom_level)
                .build());
    }
    private void boundCameraToRoute() {
        if (route != null) {
            List<Point> routeCoords = LineString.fromPolyline(route.geometry(),
                    Constants.PRECISION_6).coordinates();
            List<LatLng> bboxPoints = new ArrayList<>();
            for (Point point : routeCoords) {
                bboxPoints.add(new LatLng(point.latitude(), point.longitude()));
            }
            if (bboxPoints.size() > 1) {
                try {
                    LatLngBounds bounds = new LatLngBounds.Builder().includes(bboxPoints).build();
                    // left, top, right, bottom
                    animateCameraBbox(bounds, CAMERA_ANIMATION_DURATION, padding);
                } catch (InvalidLatLngBoundsException exception) {
                    Toast.makeText(getActivity(), R.string.error_valid_route_not_found, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private void animateCameraBbox(LatLngBounds bounds, int animationTime, int[] padding) {
        CameraPosition position = _mapboxMap.getCameraForLatLngBounds(bounds, padding);
        _mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), animationTime);
    }
    private boolean validRouteResponse(Response<DirectionsResponse> response) {
        return response.body() != null && !response.body().routes().isEmpty();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }
    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mapView.onDestroy();
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
        //mapView.onPause();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //mapView.onSaveInstanceState(outState);
    }
}