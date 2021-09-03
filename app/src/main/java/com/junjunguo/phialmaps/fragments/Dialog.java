package com.junjunguo.phialmaps.fragments;

import com.junjunguo.phialmaps.R;
import com.junjunguo.phialmaps.activities.MapActivity;
import com.junjunguo.phialmaps.map.MapHandler;
import com.junjunguo.phialmaps.util.Variable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.provider.Settings;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.oscim.theme.ExternalRenderTheme;

public class Dialog
{
  
  public static void showAutoSelectMapSelector(Activity activity)
  {
    AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
    builder1.setTitle(R.string.autoselect_map);
    builder1.setCancelable(true);
    final CheckBox cb = new CheckBox(activity.getBaseContext());
    cb.setChecked(Variable.getVariable().getAutoSelectMap());
    cb.setText(R.string.autoselect_map_text);
    builder1.setView(cb);
    OnClickListener listener = new OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int buttonNr)
      {
        Variable.getVariable().setAutoSelectMap(cb.isChecked());
        Variable.getVariable().saveVariables(Variable.VarType.Base);
      }
    };
    builder1.setPositiveButton(R.string.ok, listener);
    AlertDialog alert11 = builder1.create();
    alert11.show();
  }
  
  public static void showGpsSelector(final Activity activity)
  {
    AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
    builder1.setTitle(R.string.autoselect_map);
    builder1.setCancelable(true);
    builder1.setTitle(R.string.gps_is_off);
    OnClickListener listener = new OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int buttonNr)
      {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        activity.startActivity(intent);
      }
    };
    builder1.setPositiveButton(R.string.gps_settings, listener);
    AlertDialog alert11 = builder1.create();
    alert11.show();
  }
  public static void showMapStyleSelector(Activity activity){
    AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
    builder1.setTitle("Switch Map Style");
    builder1.setCancelable(true);

    final RadioButton rb1 = new RadioButton(activity.getBaseContext());
    rb1.setText("Default");

    final RadioButton rb2 = new RadioButton(activity.getBaseContext());
    rb2.setText("Bright");

    final RadioButton rb3 = new RadioButton(activity.getBaseContext());
    rb3.setText("Gray");

    final RadioButton rb4 = new RadioButton(activity.getBaseContext());
    rb4.setText("Dark");

    final RadioGroup rg = new RadioGroup(activity.getBaseContext());
    rg.addView(rb1);
    rg.addView(rb2);
    rg.addView(rb3);
    rg.addView(rb4);
    int mapStyleIndex = Variable.getVariable().getMapStyleIndex();
    switch (mapStyleIndex){
      case 0: rg.check(rb1.getId());break;
      case 1: rg.check(rb2.getId());break;
      case 2: rg.check(rb3.getId());break;
      case 3: rg.check(rb4.getId());break;
    }

    builder1.setView(rg);
    OnClickListener listener = new OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int buttonNr)
      {
        int mapStyleIndex = 0;
        if(rb1.isChecked()) {
          mapStyleIndex = 0;
          if(MapHandler.getMapHandler().mapView!=null)MapHandler.getMapHandler().mapView.map().setTheme(new ExternalRenderTheme(MapHandler.getMapHandler().getXmlPath((MapActivity) activity, "default.xml")));
        }
        else if(rb2.isChecked()) {
          mapStyleIndex = 1;
          if(MapHandler.getMapHandler().mapView!=null)MapHandler.getMapHandler().mapView.map().setTheme(new ExternalRenderTheme(MapHandler.getMapHandler().getXmlPath((MapActivity) activity, "bright.xml")));
        }
        else if(rb3.isChecked()) {
          mapStyleIndex = 2;
          if(MapHandler.getMapHandler().mapView!=null)MapHandler.getMapHandler().mapView.map().setTheme(new ExternalRenderTheme(MapHandler.getMapHandler().getXmlPath((MapActivity) activity, "gray.xml")));
        }
        else if(rb4.isChecked()) {
          mapStyleIndex = 3;
          if(MapHandler.getMapHandler().mapView!=null)MapHandler.getMapHandler().mapView.map().setTheme(new ExternalRenderTheme(MapHandler.getMapHandler().getXmlPath((MapActivity) activity, "dark.xml")));
        }
        Variable.getVariable().setMapStyleIndex(mapStyleIndex);
        Variable.getVariable().saveVariables(Variable.VarType.Base);
      }
    };
    builder1.setPositiveButton(R.string.ok, listener);
    AlertDialog alert11 = builder1.create();
    alert11.show();
  }
  public static void showUnitTypeSelector(Activity activity)
  {
    AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
    builder1.setTitle(R.string.units);
    builder1.setCancelable(true);
    
    final RadioButton rb1 = new RadioButton(activity.getBaseContext());
    rb1.setText(R.string.units_metric);

    final RadioButton rb2 = new RadioButton(activity.getBaseContext());
    rb2.setText(R.string.units_imperal);
    
    final RadioGroup rg = new RadioGroup(activity.getBaseContext());
    rg.addView(rb1);
    rg.addView(rb2);
    rg.check(Variable.getVariable().isImperalUnit() ? rb2.getId() : rb1.getId());
    
    builder1.setView(rg);
    OnClickListener listener = new OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int buttonNr)
      {
        Variable.getVariable().setImperalUnit(rb2.isChecked());
        Variable.getVariable().saveVariables(Variable.VarType.Base);
      }
    };
    builder1.setPositiveButton(R.string.ok, listener);
    AlertDialog alert11 = builder1.create();
    alert11.show();
  }
}
