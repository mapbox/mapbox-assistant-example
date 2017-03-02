package com.mapbox.mapboxintegratedassistant;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxintegratedassistant.model.BotResponse;
import com.mapbox.mapboxintegratedassistant.model.ChatObject;
import com.mapbox.mapboxintegratedassistant.view.ChatAdapter;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.Locale;

import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;

public class MiaActivity extends AppCompatActivity implements MiaContract.View {

    private static final int SPEECH_INPUT_CODE = 100;

    private MapView mapView;
    private MapboxMap map;

    private MiaPresenter presenter;
    private ChatAdapter chatAdapter;

    private AIDataService aiDataService;

    private RelativeLayout chatLayout;
    private RecyclerView chatList;
    private FloatingActionButton searchBtn;
    private EditText searchBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up Mapbox account
        Mapbox.getInstance(this, MiaConstants.MAPBOX_ACCESS_TOKEN);
        setContentView(R.layout.mia_layout);

        // Find the MapView and send a toast when it's ready
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
                showMapReadyToast();
            }
        });

        // Set up connection with API.AI
        setupAIConfiguration();

        // Set up list of chat objects for the RecyclerView
        ArrayList<ChatObject> chatObjects = new ArrayList<>();
        BotResponse greeting = new BotResponse();
        greeting.setText(getString(R.string.mia_greeting));
        chatObjects.add(greeting);

        // Create the presenter and give it the list of objects
        presenter = new MiaPresenter(chatObjects);
        presenter.attachView(this); // Attach view for UI manipulation

        chatLayout = (RelativeLayout) findViewById(R.id.rl_chat_layout);
        chatList = (RecyclerView) findViewById(R.id.rv_chat);
        setupChatView(chatObjects);

        searchBtn = (FloatingActionButton) findViewById(R.id.search_btn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chatLayout.setVisibility(View.VISIBLE);
                searchBtn.hide();
            }
        });
        ImageView clearChatBtn = (ImageView) findViewById(R.id.iv_clear_chat);
        clearChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chatLayout.setVisibility(View.GONE);
                hideSoftKeyboard();
                searchBtn.show();
            }
        });
        ImageView micBtn = (ImageView) findViewById(R.id.iv_microphone);
        micBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beginListening();
            }
        });
        searchBox = (EditText) findViewById(R.id.et_chat_box);
        searchBox.setOnEditorActionListener(searchBoxListener);
    }

    private void setupAIConfiguration() {
        final AIConfiguration aiConfig = new AIConfiguration(MiaConstants.APIAI_ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiDataService = new AIDataService(this, aiConfig);
    }

    private void setupChatView(ArrayList<ChatObject> chatObjects) {
        chatAdapter = new ChatAdapter(chatObjects);
        chatList.setAdapter(chatAdapter);
        chatList.setItemAnimator(new DefaultItemAnimator());
        chatList.setLayoutManager(new LinearLayoutManager(this));
    }

    private EditText.OnEditorActionListener searchBoxListener = new EditText.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                presenter.sendQueryRequest(aiDataService, textView.getText().toString());
                searchBox.getText().clear();
                return true;
            }
            return false;
        }
    };

    private void showMapReadyToast() {
        Toast.makeText(this, "Map is ready!", Toast.LENGTH_SHORT).show();
    }

    private void beginListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.ask_me));
        try {
            startActivityForResult(intent, SPEECH_INPUT_CODE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    R.string.speech_not_supported,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(chatLayout.getWindowToken(), 0);
    }

    @Override
    public void scrollChatDown() {
        chatList.scrollToPosition(chatAdapter.getItemCount() - 1);
    }

    @Override
    public void hideChatLayout() {
        chatLayout.setVisibility(View.GONE);
        searchBtn.show();
    }

    // Will fire after text is interpreted from the microphone
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SPEECH_INPUT_CODE: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    presenter.sendQueryRequest(aiDataService, result.get(0));
                    Toast.makeText(this, result.get(0), Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    public void addMarkerToMap(MarkerOptions markerOptions) {
        map.addMarker(markerOptions);
    }

    @Override
    public void addPolylineToMap(PolylineOptions polylineOptions) {
        map.addPolyline(polylineOptions);
    }

    @Override
    public void notifyAdapterItemInserted(int position) {
        chatAdapter.notifyItemInserted(position);
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
