package com.junjunguo.phialmaps.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.junjunguo.phialmaps.R;
import com.junjunguo.phialmaps.fragments.SearchPlaceRecyclerViewAdapter;
import com.junjunguo.phialmaps.model.Place;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.utils.HttpConnection;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private RecyclerView listView;
    private ImageButton btn_back;
    private ImageButton btn_clear;
    private TextView txt_search;
    private List<Place> placeDatas;
    SearchPlaceRecyclerViewAdapter searchPlaceRecyclerViewAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        listView = (RecyclerView) findViewById(R.id.placeListView);
        txt_search = (TextView)findViewById(R.id.edit_search);
        txt_search.setText("");
        txt_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getPlaces(txt_search.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_back = (ImageButton)findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED,returnIntent);
                finish();
            }
        });

        btn_clear = (ImageButton)findViewById(R.id.btn_clear);
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txt_search.setText("");
                placeDatas.clear();
                searchPlaceRecyclerViewAdapter.notifyDataSetChanged();
            }
        });

        placeDatas = new ArrayList<>();
        searchPlaceRecyclerViewAdapter = new SearchPlaceRecyclerViewAdapter(this,R.layout.row_place_item,placeDatas);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(searchPlaceRecyclerViewAdapter);
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }
    private void getPlaces(String url) {
        HttpConnection connection = new HttpConnection();
        connection.doGet("https://api.spectrumtracking.com/v1/api-interface/autoCompleteNetTool/" + url);
        String result = connection.getContentAsString();
        try {
            JSONObject obj = new JSONObject(result);
            JSONArray placeItemList = obj.getJSONArray("results");
            placeDatas.clear();
            for (int i = 0; i < placeItemList.length(); i++) {
                JSONObject place = placeItemList.getJSONObject(i);
                placeDatas.add(i,new Place(place.getString("name"), place.getString("address"), place.getDouble("latitude"), place.getDouble("longitude")));
            }
            searchPlaceRecyclerViewAdapter.notifyDataSetChanged();
        } catch (Throwable t) {
        }
    }
}