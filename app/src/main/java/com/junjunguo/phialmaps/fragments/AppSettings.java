package com.junjunguo.phialmaps.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import androidx.appcompat.app.AlertDialog;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.junjunguo.phialmaps.R;
import com.junjunguo.phialmaps.activities.ExportActivity;
import com.junjunguo.phialmaps.activities.MainActivity;
import com.junjunguo.phialmaps.activities.Analytics;
import com.junjunguo.phialmaps.activities.MapActivity;
import com.junjunguo.phialmaps.map.Tracking;
import com.junjunguo.phialmaps.util.IO;
import com.junjunguo.phialmaps.util.UnitCalculator;
import com.junjunguo.phialmaps.util.Variable;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * This file is part of Phial Map
 * <p>
 * Created by GuoJunjun <junjunguo.com> on July 01, 2015.
 */
public class AppSettings {
    public enum SettType {Default, Navi};
    private Activity activity;
    private ViewGroup appSettingsVP, trackingAnalyticsVP, trackingLayoutVP, naviLayoutVP, changeMapItemVP, useVoiceVP, directoryVP, autoVP, exportVP, unitTypeVP, mapStyleVP;
    private TextView tvspeed, tvdistance, tvdisunit;


    public AppSettings (Activity activity) {
        this.activity = activity;
        appSettingsVP = (ViewGroup) activity.findViewById(R.id.app_settings_layout);
        naviLayoutVP = (ViewGroup) activity.findViewById(R.id.app_settings_navigation_layout);
        changeMapItemVP = (ViewGroup) activity.findViewById(R.id.app_settings_change_map);
        useVoiceVP = (ViewGroup)activity.findViewById(R.id.app_settings_use_voice);
        directoryVP = (ViewGroup)activity.findViewById(R.id.app_settings_map_directory);
        autoVP = (ViewGroup)activity.findViewById(R.id.app_settings_map_auto);
        exportVP = (ViewGroup)activity.findViewById(R.id.app_settings_export);
        mapStyleVP = (ViewGroup)activity.findViewById(R.id.app_settings_map_style);
        unitTypeVP = (ViewGroup)activity.findViewById(R.id.app_settings_unit_type);
        useVoiceVP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VoiceDialog.showTtsVoiceSelector(activity);
            }
        });
        directoryVP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final File oldFile = Variable.getVariable().getMapsFolder();
                IO.showRootfolderSelector(activity, false, new Runnable()
                {
                    @Override public void run()
                    {
                        if (!oldFile.equals(Variable.getVariable().getMapsFolder()))
                        {
                            copyFavourites(oldFile);
                        }
                    }
                });
            }
        });
        autoVP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog.showAutoSelectMapSelector(activity);
            }
        });
        exportVP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startActivity(new Intent(activity, ExportActivity.class));
            }
        });
        unitTypeVP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog.showUnitTypeSelector(activity);
            }
        });
        mapStyleVP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog.showMapStyleSelector(activity);
            }
        });
    }
    protected void copyFavourites(File oldMapDir)
    {
        String newFolder = Variable.getVariable().getMapsFolder().getParent();
        String oldFolder = oldMapDir.getParent();
        File oldFavourites = new File(oldFolder, "Favourites.properties");
        File newFavourites = new File(newFolder, "Favourites.properties");
        if (!oldFavourites.isFile()) { return; }
        String dataFavourites = IO.readFromFile(oldFavourites, "\n");
        if (dataFavourites == null)
        {
            log("Error, cannot transfer favourites!");
            return;
        }
        if (dataFavourites.isEmpty()) { return; }
        boolean success = IO.writeToFile(dataFavourites, newFavourites, false);
        if (success) { oldFavourites.delete(); }
    }
    public class CollapseAnimation extends TranslateAnimation implements TranslateAnimation.AnimationListener{

        private LinearLayout slidingLayout;
        int panelWidth;
        public CollapseAnimation(LinearLayout layout, int width, int fromXType, float fromXValue, int toXType,
                                 float toXValue, int fromYType, float fromYValue, int toYType, float toYValue) {

            super(fromXType, fromXValue, toXType, toXValue, fromYType, fromYValue, toYType, toYValue);

            //Initialize
            slidingLayout = layout;
            panelWidth = width;
            setDuration(400);
            setFillAfter( false );
            setInterpolator(new AccelerateDecelerateInterpolator());
            setAnimationListener(this);

            //Clear left and right margins

            slidingLayout.startAnimation(this);
            //slidingLayout.setBackgroundColor();
            //slidingLayout.setBackgroundColor(R.string.white);

        }
        @Override
        public void onAnimationEnd(Animation arg0) {
            // TODO Auto-generated method stub
            appSettingsVP.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onAnimationRepeat(Animation arg0) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onAnimationStart(Animation arg0) {
            // TODO Auto-generated method stub

        }

    }
    public class ExpandAnimation extends TranslateAnimation implements Animation.AnimationListener{
        private LinearLayout slidingLayout;
        int panelWidth;
        public ExpandAnimation(LinearLayout layout, int width, int fromXType, float fromXValue, int toXType,
                               float toXValue, int fromYType, float fromYValue, int toYType, float toYValue) {

            super(fromXType, fromXValue, toXType, toXValue, fromYType, fromYValue, toYType, toYValue);

            //Initialize
            slidingLayout = layout;
            panelWidth = width;
            setDuration(400);
            setFillAfter( false );
            setInterpolator(new AccelerateDecelerateInterpolator());
            setAnimationListener(this);
            slidingLayout.startAnimation(this);
            //slidingLayout.setBackgroundColor(panelWidth);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            slidingLayout.clearAnimation();
            slidingLayout.requestLayout();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onAnimationStart(Animation animation) {
            // TODO Auto-generated method stub

        }

    }
    public void showAppSettings(final ViewGroup calledFromVP, SettType settType)
    {
        initClearBtn(appSettingsVP, calledFromVP);
//        if (settType == SettType.Default)
//        {
          changeMapItemVP.setVisibility(View.VISIBLE);
         // naviLayoutVP.setVisibility(View.GONE);
          chooseMapBtn(appSettingsVP);
//        }
//        else
//        {
          naviLayoutVP.setVisibility(View.VISIBLE);
         // changeMapItemVP.setVisibility(View.GONE);
         // trackingLayoutVP.setVisibility(View.GONE);
          alternateRoute();
          naviDirections();
//        }
        appSettingsVP.setVisibility(View.VISIBLE);
        calledFromVP.setVisibility(View.INVISIBLE);
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int panelWidth = (int) ((metrics.widthPixels)*0.45);
        new ExpandAnimation((LinearLayout) activity.findViewById(R.id.app_setting_main), panelWidth,
                Animation.RELATIVE_TO_SELF, -1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, 0, 0.0f, 0, 0.0f);

    }


    public ViewGroup getAppSettingsVP() {
        return appSettingsVP;
    }
    
    private Tracking getTracking()
    {
      return Tracking.getTracking(activity.getApplicationContext());
    }

    /**
     * init and implement directions checkbox
     */
    private void naviDirections() {
        Switch cb = (Switch) activity.findViewById(R.id.app_settings_directions_cb);
        final Switch cb_voice = (Switch) activity.findViewById(R.id.app_settings_voice);
        final Switch cb_light = (Switch) activity.findViewById(R.id.app_settings_light);
        final Switch cb_smooth = (Switch) activity.findViewById(R.id.app_settings_smooth);
        final Switch cb_showspeed = (Switch) activity.findViewById(R.id.app_settings_showspeed);
        final Switch cb_speakspeed = (Switch) activity.findViewById(R.id.app_settings_speakspeed);
        final TextView txt_voice = (TextView) activity.findViewById(R.id.txt_voice);
        final TextView txt_light = (TextView) activity.findViewById(R.id.txt_light);
        final TextView txt_smooth = (TextView) activity.findViewById(R.id.txt_smooth);
        final TextView txt_showspeed = (TextView) activity.findViewById(R.id.txt_showspeed);
        final TextView txt_speakspeed = (TextView) activity.findViewById(R.id.txt_speakspeed);
        cb.setChecked(Variable.getVariable().isDirectionsON());
        cb_voice.setChecked(Variable.getVariable().isVoiceON());
        cb_light.setChecked(Variable.getVariable().isLightSensorON());
        cb_smooth.setChecked(Variable.getVariable().isSmoothON());
        cb_showspeed.setChecked(Variable.getVariable().isShowingSpeedLimits());
        cb_speakspeed.setChecked(Variable.getVariable().isSpeakingSpeedLimits());
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Variable.getVariable().setDirectionsON(isChecked);
                cb_voice.setEnabled(isChecked);
                cb_light.setEnabled(isChecked);
                cb_smooth.setEnabled(isChecked);
                cb_showspeed.setEnabled(isChecked);
                cb_speakspeed.setEnabled(isChecked);
                txt_voice.setEnabled(isChecked);
                txt_light.setEnabled(isChecked);
                txt_smooth.setEnabled(isChecked);
                txt_showspeed.setEnabled(isChecked);
                txt_speakspeed.setEnabled(isChecked);
            }
        });
        cb_voice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
              Variable.getVariable().setVoiceON(isChecked);
          }
        });
        cb_light.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
              Variable.getVariable().setLightSensorON(isChecked);
          }
        });
        cb_smooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Variable.getVariable().setSmoothON(isChecked);
                ((MapActivity)activity).ensureLocationListener(false);
            }
        });
        cb_showspeed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Variable.getVariable().setShowSpeedLimits(isChecked);
            }
        });
        cb_speakspeed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Variable.getVariable().setSpeakSpeedLimits(isChecked);
            }
        });
        if (!Variable.getVariable().isDirectionsON())
        {
          cb_voice.setEnabled(false);
          cb_light.setEnabled(false);
          cb_smooth.setEnabled(false);
          cb_showspeed.setEnabled(false);
          cb_speakspeed.setEnabled(false);
          txt_voice.setEnabled(false);
          txt_light.setEnabled(false);
          txt_smooth.setEnabled(false);
          txt_showspeed.setEnabled(false);
          txt_speakspeed.setEnabled(false);
        }
    }
    
    /**
     * init and set alternate route radio button option
     */
    private void alternateRoute() {
        RadioGroup rg = (RadioGroup) activity.findViewById(R.id.app_settings_weighting_rbtngroup);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.app_settings_fastest_rbtn:
                        Variable.getVariable().setWeighting("fastest");
                        break;
                    case R.id.app_settings_shortest_rbtn:
                        Variable.getVariable().setWeighting("shortest");
                        break;
                }
            }
        });
        RadioButton rbf, rbs;
        rbf = (RadioButton) activity.findViewById(R.id.app_settings_fastest_rbtn);
        rbs = (RadioButton) activity.findViewById(R.id.app_settings_shortest_rbtn);
        if (Variable.getVariable().getWeighting().equalsIgnoreCase("fastest")) {
            rbf.setChecked(true);
        } else {
            rbs.setChecked(true);
        }
    }

    
    private void showLoadDialog(Context context)
    {
      android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.Builder(context);
      builder1.setTitle(R.string.tracking_load);
      builder1.setCancelable(true);
      final String items[] = Variable.getVariable().getTrackingFolder().list();
      OnClickListener listener = new OnClickListener()
      {
        @Override
        public void onClick(DialogInterface dialog, int buttonNr)
        {
          String selection = items[buttonNr];
          File gpxFile = new File(Variable.getVariable().getTrackingFolder(), selection);
          getTracking().loadData(activity, gpxFile, AppSettings.this);
        }
      };
      builder1.setItems(items, listener);
      android.app.AlertDialog alert11 = builder1.create();
      alert11.show();
    }

    private void confirmWindow() {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        final EditText edittext = new EditText(activity);
        builder.setTitle(activity.getResources().getString(R.string.dialog_stop_save_tracking));
        builder.setMessage("path: " + Variable.getVariable().getTrackingFolder().getAbsolutePath() + "/");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String formattedDate = df.format(System.currentTimeMillis());
        edittext.setText(formattedDate);
        builder.setView(edittext);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        //        builder.setView(inflater.inflate(R.layout.dialog_tracking_exit, null));
        // Add action buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int id) {
                // save file
                getTracking().saveAsGPX(edittext.getText().toString());
                getTracking().stopTracking(AppSettings.this);
            }
        }).setNeutralButton(R.string.stop, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                getTracking().stopTracking(AppSettings.this);
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        //        ((EditText) ((AlertDialog) dialog).findViewById(R.id.dialog_tracking_exit_et)).setText(formattedDate);
        dialog.show();
    }

    /**
     * dynamic show start or stop tracking
     */



    /**
     * actions to preform when tracking analytics item (btn) is clicked
     */
    private void trackingAnalyticsBtn() {
        trackingAnalyticsVP.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        trackingAnalyticsVP
                                .setBackgroundColor(activity.getResources().getColor(R.color.my_primary_light));
                        return true;
                    case MotionEvent.ACTION_UP:
                        trackingAnalyticsVP.setBackgroundColor(activity.getResources().getColor(R.color.my_icons));
                        openAnalyticsActivity(true);
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * update speed and distance at analytics item
     *
     * @param speed
     * @param distance
     */
    public void updateAnalytics(double speed, double distance) {
        if (tvdistance==null || tvdisunit==null || tvspeed==null) { return; }
        if (distance < 1000) {
            tvdistance.setText(String.valueOf(Math.round(distance)));
            tvdisunit.setText(UnitCalculator.getUnit(false));
        } else {
            tvdistance.setText(String.format("%.1f", distance / 1000));
            tvdisunit.setText(UnitCalculator.getUnit(true));
        }
        tvspeed.setText(String.format("%.1f", speed));
    }


    /**
     * move to select and load map view
     *
     * @param appSettingsVP
     */
    private void chooseMapBtn(final ViewGroup appSettingsVP) {
        changeMapItemVP.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        changeMapItemVP.setBackgroundColor(activity.getResources().getColor(R.color.my_primary_light));
                        return true;
                    case MotionEvent.ACTION_UP:
                        changeMapItemVP.setBackgroundColor(activity.getResources().getColor(R.color.my_icons));
                        // Variable.getVariable().setAutoLoad(false); // close auto load from
                        // main activity
                        MapActivity.isMapAlive_preFinish();
                        activity.finish();
                        startMainActivity();
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * init clear btn
     */
    private void initClearBtn(final ViewGroup appSettingsVP, final ViewGroup calledFromVP) {
        ImageButton appsettingsClearBtn = (ImageButton) activity.findViewById(R.id.app_settings_clear_btn);
        appsettingsClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                activity.findViewById(R.id.map_search_address).setVisibility(View.VISIBLE);
                calledFromVP.setVisibility(View.VISIBLE);
                new CollapseAnimation((LinearLayout)activity.findViewById(R.id.app_setting_main),0,
                        TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
                        TranslateAnimation.RELATIVE_TO_SELF, -1.0f, 0, 0.0f, 0, 0.0f);
            }
        });
        activity.findViewById(R.id.setting_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.findViewById(R.id.map_search_address).setVisibility(View.VISIBLE);
                calledFromVP.setVisibility(View.VISIBLE);
                new CollapseAnimation((LinearLayout)activity.findViewById(R.id.app_setting_main),0,
                        TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
                        TranslateAnimation.RELATIVE_TO_SELF, -1.0f, 0, 0.0f, 0, 0.0f);
            }
        });
    }

    /**
     * move to main activity
     */
    private void startMainActivity() {
        //        if (Tracking.getTracking().isTracking()) {
        //            Toast.makeText(activity, "You need to stop your tracking first!", Toast.LENGTH_LONG).show();
        //        } else {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra("com.junjunguo.pocketmaps.activities.MapActivity.SELECTNEWMAP", true);
        activity.startActivity(intent);
        //        activity.finish();
        //        }
    }

    /**
     * open analytics activity
     */

    public void openAnalyticsActivity(boolean startTimer) {
        Analytics.startTimer = startTimer;
        Intent intent = new Intent(activity, Analytics.class);
        activity.startActivity(intent);
        //        activity.finish();
    }

    /**
     * send message to logcat
     *
     * @param str
     */
    private void log(String str) {
        Log.i(this.getClass().getName(), str);
    }

    /**
     * send message to logcat and Toast it on screen
     *
     * @param str: message
     */
    private void logToast(String str) {
        log(str);
        Toast.makeText(activity, str, Toast.LENGTH_SHORT).show();
    }
}
