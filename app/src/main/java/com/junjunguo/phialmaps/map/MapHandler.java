package com.junjunguo.phialmaps.map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.oscim.android.MapView;
import org.oscim.android.canvas.AndroidGraphics;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.event.Gesture;
import org.oscim.event.GestureListener;
import org.oscim.event.MotionEvent;
import org.oscim.layers.Layer;
import org.oscim.layers.vector.PathLayer;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.layers.vector.geometries.Style;
import org.oscim.map.Layers;
import org.oscim.theme.ExternalRenderTheme;
import org.oscim.tiling.source.mapfile.MapFileTileSource;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.android.GHAsyncTask;
import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.util.StopWatch;
import com.graphhopper.util.Parameters.Algorithms;
import com.graphhopper.util.Parameters.Routing;
import com.graphhopper.util.PointList;

import com.graphhopper.util.shapes.GHPoint;
import com.junjunguo.phialmaps.R;
import com.junjunguo.phialmaps.activities.MapActivity;
import com.junjunguo.phialmaps.activities.ShowLocationActivity;
import com.junjunguo.phialmaps.model.listeners.MapHandlerListener;
import com.junjunguo.phialmaps.navigator.NaviEngine;
import com.junjunguo.phialmaps.util.TargetDirComputer;
import com.junjunguo.phialmaps.util.Variable;


public class MapHandler
{
  private static MapHandler mapHandler;
  private volatile boolean prepareInProgress = false;
  private volatile boolean calcPathActive = false;
  MapPosition tmpPos = new MapPosition();
  public GeoPoint startMarker;
  public GeoPoint endMarker;
  private boolean needLocation = false;
  public MapView mapView;
  private ItemizedLayer itemizedLayer;
  private ItemizedLayer customLayer;
  public PathLayer pathLayer;
  private PathLayer polylineTrack;
  private GraphHopper hopper;
  private MapHandlerListener mapHandlerListener;
  private String currentArea;
  File mapsFolder;
  FloatingActionButton naviCenterBtn;
  PointList trackingPointList = new PointList();
  private int customIcon = R.drawable.ic_my_location_dark_36dp;
  private MapFileTileSource tileSource;
  /**
   * need to know if path calculating status change; this will trigger MapActions function
   */
  private boolean needPathCal;
  
  public static MapHandler getMapHandler()
  {
    if (mapHandler == null)
    {
      reset();
    }
    return mapHandler;
  }

   /**
    * reset class, build a new instance
    */
  public static void reset()
  {
    mapHandler = new MapHandler();
  }

  private MapHandler()
  {
    setCalculatePath(false,false);
    startMarker = null;
    endMarker = null;
    needLocation = false;
    needPathCal = false;
  }

  public void init(MapView mapView, String currentArea, File mapsFolder)
  {
    this.mapView = mapView;
    this.currentArea = currentArea;
    this.mapsFolder = mapsFolder; // path/to/map/area-gh/
      this.mapHandlerListener = MapActivity._mapActivity.getMapActions();
  }
  
  public MapFileTileSource getTileSource() { return tileSource; }

  /**
   * load map to mapView
   *
   * @param areaFolder
   */
  public void loadMap(File areaFolder, MapActivity activity)
  {
      File s = new File(activity.getCacheDir()+"/resource");
      if(!s.exists())copyAssetFolder(activity.getApplicationContext(), "resource",activity.getCacheDir()+"/resource");
    // Map events receiver
    mapView.map().layers().add(new MapEventsReceiver(mapView.map()));

    // Map file source
    tileSource = new MapFileTileSource();
    tileSource.setMapFile(new File(areaFolder, currentArea + ".map").getAbsolutePath());
    VectorTileLayer l = mapView.map().setBaseMap(tileSource);
    int mapStyleIndex = Variable.getVariable().getMapStyleIndex();
    switch (mapStyleIndex){
        case 0:mapView.map().setTheme(new ExternalRenderTheme(getXmlPath(activity, "default.xml")));break;
        case 1:mapView.map().setTheme(new ExternalRenderTheme(getXmlPath(activity, "bright.xml")));break;
        case 2:mapView.map().setTheme(new ExternalRenderTheme(getXmlPath(activity, "gray.xml")));break;
        case 3:mapView.map().setTheme(new ExternalRenderTheme(getXmlPath(activity, "dark.xml")));break;
    }
    mapView.map().layers().add(new BuildingLayer(mapView.map(), l));
    mapView.map().layers().add(new LabelLayer(mapView.map(), l));
    
    // Markers layer
    itemizedLayer = new ItemizedLayer(mapView.map(), (MarkerSymbol) null);
    mapView.map().layers().add(itemizedLayer);
    customLayer = new ItemizedLayer(mapView.map(), (MarkerSymbol) null);
    mapView.map().layers().add(customLayer);

    // Map position
    GeoPoint mapCenter = tileSource.getMapInfo().boundingBox.getCenterPoint();
    mapView.map().setMapPosition(mapCenter.getLatitude(), mapCenter.getLongitude(), 1 << 17);

//    ViewGroup.LayoutParams params =
//        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//    activity.addContentView(mapView, params);
    
    loadGraphStorage(activity);
  }
  public String getXmlPath(MapActivity activity, String name){
      File f = new File( activity.getCacheDir()+"/" + name);
      if(!f.exists()) {
          try {
              InputStream is = activity.getAssets().open(name);
              int size = is.available();
              byte[] buffer = new byte[size];
              is.read(buffer);
              is.close();

              FileOutputStream fos = new FileOutputStream(f);
              fos.write(buffer);
              fos.close();
          } catch (Exception e) {
              throw new RuntimeException(e);
          }
      }
      return f.getPath();
  }
    public static boolean copyAssetFolder(Context context, String srcName, String dstName) {
        try {
            boolean result = true;
            String fileList[] = context.getAssets().list(srcName);
            if (fileList == null) return false;

            if (fileList.length == 0) {
                result = copyAssetFile(context, srcName, dstName);
            } else {
                File file = new File(dstName);
                result = file.mkdirs();
                for (String filename : fileList) {
                    result &= copyAssetFolder(context, srcName + File.separator + filename, dstName + File.separator + filename);
                }
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean copyAssetFile(Context context, String srcName, String dstName) {
        try {
            InputStream in = context.getAssets().open(srcName);
            File outFile = new File(dstName);
            OutputStream out = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
  void loadGraphStorage(final MapActivity activity) {
      //logUser(activity, "loading graph (" + Constants.VERSION + ") ... ");
      new GHAsyncTask<Void, Void, Path>() {
          protected Path saveDoInBackground(Void... v) throws Exception {
              GraphHopper tmpHopp = new GraphHopper().forMobile();
              // Why is "shortest" missing in default config? Add!
              tmpHopp.getCHFactoryDecorator().addCHProfileAsString("shortest");
              tmpHopp.load(new File(mapsFolder, currentArea).getAbsolutePath() + "-gh");
              log("found graph " + tmpHopp.getGraphHopperStorage().toString() + ", nodes:" + tmpHopp.getGraphHopperStorage().getNodes());
              hopper = tmpHopp;
              return null;
          }

          protected void onPostExecute(Path o) {
              if (hasError()) {
                  //logUser(activity, "An error happened while creating graph:"
                          //+ getErrorMessage());
              } else {
                  //logUser(activity, "Finished loading graph.");
              }

              GeoPoint g = ShowLocationActivity.locationGeoPoint;
              String lss = ShowLocationActivity.locationSearchString;
              if (g != null)
              {
                activity.getMapActions().onPressLocationEndPoint(g);
                ShowLocationActivity.locationGeoPoint = null;
              }
              else if (lss != null)
              {
                activity.getMapActions().startGeocodeActivity(null, null, false, false);
              }
              prepareInProgress = false;
          }
      }.execute();
  }
  
  public AllEdgesIterator getAllEdges()
  {
    if (hopper==null) { return null; }
    if (hopper.getGraphHopperStorage()==null) { return null; }
    return hopper.getGraphHopperStorage().getAllEdges();
  }

  /**
   * center the LatLong point in the map and zoom map to zoomLevel
   *
   * @param latLong
   * @param zoomLevel (if 0 use current zoomlevel)
   */
  public void centerPointOnMap(GeoPoint latLong, int zoomLevel, float bearing, float tilt)
  {
      if(mapView==null)return;
    if (zoomLevel == 0)
    {
      zoomLevel = mapView.map().getMapPosition().zoomLevel;
    }
    double scale = 1 << zoomLevel;
    tmpPos.setPosition(latLong);
    tmpPos.setScale(scale);
    tmpPos.setBearing(bearing);
    tmpPos.setTilt(tilt);
    mapView.map().animator().animateTo(300, tmpPos);
  }
  
  public void resetTilt(float tilt)
  {
      mapView.map().setMapPosition(mapView.map().getMapPosition().setTilt(tilt));
  }

  /**
   * @return
   */
  public boolean isNeedLocation()
  {
    return needLocation;
  }

  /**
   * set in need a location from screen point (touch)
   *
   * @param needLocation
   */
  public void setNeedLocation(boolean needLocation)
  {
    this.needLocation = needLocation;
  }

  public void removeMarker(Activity activity, List<GHPoint> Points){
      if (pathLayer!=null) { pathLayer.clearPath(); }
      itemizedLayer.removeAllItems();
      int valid_count = 0;
      if (Points.get(0).isValid())
      {
          startMarker = new GeoPoint(Points.get(0).lat,Points.get(0).lon);
          itemizedLayer.addItem(createMarkerItem(activity, startMarker, R.drawable.ic_location_start_36dp, 0.5f, 1.0f));
          valid_count++;
      }
      if(Points.size()>=2) {
          for (int i=1;i<Points.size();i++) {
              if (Points.get(i).isValid()) {
                  endMarker = new GeoPoint(Points.get(i).lat,Points.get(i).lon);
                  itemizedLayer.addItem(createMarkerItem(activity, endMarker, R.drawable.ic_location_end_36dp, 0.5f, 1.0f));
                  valid_count++;
              }
          }
          if(valid_count>=2)recalcPath(activity);
      }
      mapView.map().updateMap(true);
  }
  public boolean setStartEndPoint(Activity activity, GeoPoint p, boolean isStart, boolean recalculate)
  {
    boolean result = false;
    boolean refreshBoth = false;
    if (startMarker!=null && endMarker!=null && p!=null) { refreshBoth = true; }
      
    if (!isStart)
    {
      endMarker = p;
    }

    // remove routing layers
    if ((startMarker==null || endMarker==null) || refreshBoth)
    {
      if (pathLayer!=null) { pathLayer.clearPath(); }
      //itemizedLayer.removeAllItems();
    }
    if (startMarker==null && p!= null)
    {
        startMarker = p;
      itemizedLayer.addItem(createMarkerItem(activity, startMarker, R.drawable.ic_location_start_36dp, 0.5f, 1.0f));
    }
    if (endMarker!=null)
    {
      itemizedLayer.addItem(createMarkerItem(activity, endMarker, R.drawable.ic_location_end_36dp, 0.5f, 1.0f));
    }
    if (startMarker!=null && endMarker!=null && recalculate)
    {
        //logUser(activity, endMarker.getLatitude()+":");
      recalcPath(activity);
      result = true;
    }
    mapView.map().updateMap(true);
    return result;
  }
  
  public void recalcPath(Activity activity)
  {
    setCalculatePath(true, true);
    calcPath(startMarker.getLatitude(), startMarker.getLongitude(), endMarker.getLatitude(), endMarker.getLongitude(), activity, false);
  }

  /** Set the custom Point for current location, or null to delete.
   *  Sets the offset to center. **/
  public void setCustomPoint(Activity activity, GeoPoint p)
  {
    if (customLayer==null) { return; } // Not loaded yet.
    customLayer.removeAllItems();
    if (p!=null)
    {
      customLayer.addItem(createMarkerItem(activity, p,customIcon, 0.5f, 0.5f));
      mapView.map().updateMap(true);
    }
  }
  
  public void setCustomPointIcon(Context appContext, int customIcon)
  {
    this.customIcon = customIcon;
    if (customLayer.getItemList().size() > 0)
    { // RefreshIcon
      MarkerItem curSymbol = (MarkerItem) customLayer.getItemList().get(0);
      MarkerSymbol marker = createMarkerItem(appContext, new GeoPoint(0,0), customIcon, 0.5f, 0.5f).getMarker();
      curSymbol.setMarker(marker);
    }
  }

  private MarkerItem createMarkerItem(Context appContext, GeoPoint p, int resource, float offsetX, float offsetY) {
//      Drawable drawable = activity.getDrawable(resource); // Since API21
      Drawable drawable = ContextCompat.getDrawable(appContext, resource);
      Bitmap bitmap = AndroidGraphics.drawableToBitmap(drawable);
      MarkerSymbol markerSymbol = new MarkerSymbol(bitmap, offsetX, offsetY);
      MarkerItem markerItem = new MarkerItem("", "", p);
      markerItem.setMarker(markerSymbol);
      return markerItem;
  }

    /**
     * @return true if already loaded
     */
    boolean isReady() {
      return !prepareInProgress;
    }

    /**
     * start tracking : reset polylineTrack & trackingPointList & remove polylineTrack if exist
     */
    public void startTrack(Activity activity) {
        if (polylineTrack != null) {
            removeLayer(mapView.map().layers(), polylineTrack);
        }
        polylineTrack = null;
        trackingPointList.clear();
        if (polylineTrack != null) { polylineTrack.clearPath(); }
        polylineTrack = updatePathLayer(activity, polylineTrack, trackingPointList, 0x990083ff, 6);
        NaviEngine.getNaviEngine().startDebugSimulator(activity, true);
    }

    /**
     * add a tracking point
     *
     * @param point
     */
    public void addTrackPoint(Activity activity, GeoPoint point) {
      trackingPointList.add(point.getLatitude(), point.getLongitude());
      updatePathLayer(activity, polylineTrack, trackingPointList, 0x990083ff, 6);
      mapView.map().updateMap(true);
    }
    
  /**
   * remove a layer from map layers
   *
   * @param layers
   * @param layer
   */
  public static void removeLayer(Layers layers, Layer layer)
  {
    if (layers != null && layer != null && layers.contains(layer))
    {
      layers.remove(layer);
    }
  }

    public boolean isCalculatingPath() {
        return calcPathActive;
    }

    public void setCalculatePath(boolean calcPathActive, boolean callListener) {
        this.calcPathActive = calcPathActive;
        if (mapHandlerListener != null && needPathCal && callListener) mapHandlerListener.pathCalculating(calcPathActive);
    }

    public void setNeedPathCal(boolean needPathCal) {
        this.needPathCal = needPathCal;
    }

    /**
     * Get the hopper object, that may be null while loading map.
     * @return GraphHopper object
     */
    public GraphHopper getHopper() {
        return hopper;
    }

    /**
     * assign a new GraphHopper
     *
     * @param hopper
     */
    public void setHopper(GraphHopper hopper) {
        this.hopper = hopper;
    }

    /**
     * only tell on object
     *
     * @param mapHandlerListener
     */
    public void setMapHandlerListener(MapHandlerListener mapHandlerListener) {
        this.mapHandlerListener = mapHandlerListener;
    }

    public void calcPath(final double fromLat, final double fromLon,
                         final double toLat, final double toLon, final Activity activity, boolean auto_flag) {
        setCalculatePath(true, false);
        final List<GHPoint> _points = ((MapActivity)activity).allPoints;
        if(_points.get(0).lat!=fromLat || _points.get(0).lon!=fromLon){
            _points.set(0, new GHPoint(fromLat,fromLon));
        }
//        _points.add(0,new GHPoint(fromLat,fromLon));
        log("calculating path ...");
        new AsyncTask<Void, Void, GHResponse>() {
            float time;

            @Override
            protected GHResponse doInBackground(Void... v) {
              StopWatch sw = new StopWatch().start();
              GHRequest req = new GHRequest(_points).
                      setAlgorithm(Algorithms.DIJKSTRA_BI);
              req.getHints().put(Routing.INSTRUCTIONS, Variable.getVariable().getDirectionsON());
              req.setVehicle(Variable.getVariable().getTravelMode().toString().toLowerCase());
              req.setWeighting(Variable.getVariable().getWeighting());
              if (Variable.getVariable().isShowingSpeedLimits() || Variable.getVariable().isSpeakingSpeedLimits())
              {
                  req.getPathDetails().add(com.graphhopper.routing.profiles.MaxSpeed.KEY);
                  req.getPathDetails().add(com.graphhopper.util.Parameters.Details.AVERAGE_SPEED);
              }
              GHResponse resp = null;
              if (hopper != null)
              {
                resp = hopper.route(req);
              }
              if (resp==null || resp.hasErrors())
              {
                NaviEngine.getNaviEngine().setDirectTargetDir(true);
                Throwable error;
                if (resp != null) { error = resp.getErrors().get(0); }
                else { error = new NullPointerException("Hopper is null!!!"); }
                log("Multible errors, first: " + error);
                resp = TargetDirComputer.getInstance().createTargetdirResponseFromArray(_points);
              }
              else
              {
                NaviEngine.getNaviEngine().setDirectTargetDir(false);
              }
              time = sw.stop().getSeconds();
              return resp;
            }


            @Override
            protected void onPostExecute(GHResponse ghResp) {
                if (!ghResp.hasErrors()) {
                    PathWrapper resp = ghResp.getBest();
                    log("from:" + fromLat + "," + fromLon + " to:" + toLat + ","
                            + toLon + " found path with distance:" + resp.getDistance()
                            / 1000f + ", nodes:" + resp.getPoints().getSize() + ", time:"
                            + time + " " + resp.getDebugInfo());
                   // logUser(activity, "the route is " + (int) (resp.getDistance() / 100) / 10f
                        //    + "km long, time:" + resp.getTime() / 60000f + "min.");

                    int sWidth = 6;
                    pathLayer = updatePathLayer(activity, pathLayer, resp.getPoints(), 0x990083ff, sWidth);
                    mapView.map().updateMap(true);
                    ((MapActivity)activity).resp = resp;
                    ((MapActivity)activity).getMapActions().btn_nav_start.setVisibility(View.VISIBLE);
                    ((MapActivity)activity).getMapActions().btn_nav_stop.setVisibility(View.VISIBLE);
                    if (Variable.getVariable().isDirectionsON()) {
                      if(auto_flag)Navigator.getNavigator().setGhResponse(resp);
                    }
                } else {
                   // logUser(activity, "Multible errors: " + ghResp.getErrors().size());
                    log("Multible errors, first: " + ghResp.getErrors().get(0));
                }
                if (NaviEngine.getNaviEngine().isNavigating())
                {
                    //logUser(activity,"123123");
                  setCalculatePath(false, false);
                }
                else
                {
                    if(auto_flag)setCalculatePath(false, true);
                    //logUser(activity,"89789789");
                  try
                  {
                    activity.findViewById(R.id.map_nav_settings_path_finding).setVisibility(View.GONE);
                    activity.findViewById(R.id.nav_settings_layout).setVisibility(View.VISIBLE);
                  }
                  catch (Exception e) { e.printStackTrace(); }
                }
            }
        }.execute();
    }
    
  private PathLayer updatePathLayer(Activity activity, PathLayer ref, PointList pointList, int color, int strokeWidth) {
      if (ref==null) {
          ref = createPathLayer(activity, color, strokeWidth);
          mapView.map().layers().add(ref);
      }//else Toast.makeText(activity.getApplicationContext(),"sfsdfsdf", Toast.LENGTH_LONG).show();
      List<GeoPoint> geoPoints = new ArrayList<>();
      //TODO: Search for a more efficient way
      for (int i = 0; i < pointList.getSize(); i++)
          geoPoints.add(new GeoPoint(pointList.getLatitude(i), pointList.getLongitude(i)));
      ref.setPoints(geoPoints);
      return ref;
  }
  
  public void joinPathLayerToPos(double lat, double lon)
  {
    try
    {
      List<GeoPoint> geoPoints = new ArrayList<>();
      geoPoints.add(new GeoPoint(lat,lon));
      geoPoints.add(pathLayer.getPoints().get(1));
      pathLayer.setPoints(geoPoints);
    }
    catch (Exception e) { log("Error: " + e); }
  }
    
  private PathLayer createPathLayer(Activity activity, int color, int strokeWidth)
  {
      Style style = Style.builder()
        .fixed(true)
        .generalization(Style.GENERALIZATION_SMALL)
        .strokeColor(color)
        .strokeWidth(strokeWidth * activity.getResources().getDisplayMetrics().density)
        .build();
      PathLayer newPathLayer = new PathLayer(mapView.map(), style);
      return newPathLayer;
  }

  public void showNaviCenterBtn(boolean visible)
  {
    if (visible)
    {
      naviCenterBtn.setVisibility(View.VISIBLE);
    }
    else
    {
      naviCenterBtn.setVisibility(View.INVISIBLE);
    }
  }

  public void setNaviCenterBtn(final FloatingActionButton naviCenterBtn)
  {
    this.naviCenterBtn = naviCenterBtn;
  }
    
  class MapEventsReceiver extends Layer implements GestureListener
  {

      MapEventsReceiver(org.oscim.map.Map map) {
          super(map);
      }

      @Override
      public boolean onGesture(Gesture g, MotionEvent e) {
          if (g instanceof Gesture.Tap) {
              GeoPoint p = mMap.viewport().fromScreenPoint(e.getX(), e.getY());
              if (mapHandlerListener!=null && needLocation)
              {
                mapHandlerListener.onPressLocation(p);
              }
          }
          return false;
      }
  }
  
  private void logUser(Activity activity, String str)
  {
    log(str);
    try
    {
      Toast.makeText(activity, str, Toast.LENGTH_LONG).show();
    }
    catch (Exception e) { e.printStackTrace(); }
  }
  
  private void log(String str)
  {
    Log.i(MapHandler.class.getName(), str);
  }
}

