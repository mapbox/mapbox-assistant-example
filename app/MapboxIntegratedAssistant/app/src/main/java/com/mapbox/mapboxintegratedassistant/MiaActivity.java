package com.mapbox.mapboxintegratedassistant;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.mapbox.mapboxintegratedassistant.model.BotResponse;
import com.mapbox.mapboxintegratedassistant.model.ChatObject;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.ArrayList;

public class MiaActivity extends AppCompatActivity {

    private MapView mapView;

    private RelativeLayout chatLayout;
    private RecyclerView chatList;
    private FloatingActionButton searchBtn;
    private ImageView clearChatBtn;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up Mapbox account
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.mia_layout);

        // Find the mapview and send a toast when it's ready
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                showMapReadyToast();
            }
        });

        // Set up list of chat objects for the RecyclerView
        ArrayList<ChatObject> chatObjects = new ArrayList<>();
        BotResponse greeting = new BotResponse();
        greeting.setText("Hi, I'm Mapbox's assistant! How can I help you?");
        chatObjects.add(greeting);

        chatLayout = (RelativeLayout) findViewById(R.id.rl_chat_layout);
        chatList = (RecyclerView) findViewById(R.id.rv_chat);
        chatList.setAdapter(new ChatAdapter(chatObjects));
        chatList.setItemAnimator(new DefaultItemAnimator());
        chatList.setLayoutManager(new LinearLayoutManager(this));

        searchBtn = (FloatingActionButton) findViewById(R.id.search_btn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chatLayout.setVisibility(View.VISIBLE);
                searchBtn.hide();
            }
        });
        clearChatBtn = (ImageView) findViewById(R.id.iv_clear_chat);
        clearChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chatLayout.setVisibility(View.GONE);
                searchBtn.show();
            }
        });
        searchEditText = (EditText) findViewById(R.id.et_chat_box);

    }

    private void showMapReadyToast() {
        Toast.makeText(this, "Map is ready!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
