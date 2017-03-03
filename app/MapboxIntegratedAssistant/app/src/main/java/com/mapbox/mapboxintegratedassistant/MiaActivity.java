package com.mapbox.mapboxintegratedassistant;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
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
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationSource;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;
import com.mapbox.services.commons.models.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;

public class MiaActivity extends AppCompatActivity implements MiaContract.View, PermissionsListener {

    private static final int SPEECH_INPUT_CODE = 100;

    private MapView mapView;
    private MapboxMap map;

    private LocationEngine locationEngine;
    private LocationEngineListener locationEngineListener;
    private PermissionsManager permissionsManager;

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
            }
        });

        // Get the location engine object for later use.
        locationEngine = LocationSource.getLocationEngine(this);
        locationEngine.activate();

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
                revealChatLayout();
                searchBtn.hide();
            }
        });
        ImageView clearChatBtn = (ImageView) findViewById(R.id.iv_clear_chat);
        clearChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideChatLayout();
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

    private void revealChatLayout() {
        // Get the center for the clipping circle
        int cx = chatLayout.getWidth() / 2;
        int cy = chatLayout.getHeight() / 2;

        // Get the final radius for the clipping circle
        float finalRadius = (float) Math.hypot(cx, cy);

        // Create the animator for this view (the start radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(chatLayout, cx, cy, 0, finalRadius);

        // Make the view visible and start the animation
        chatLayout.setVisibility(View.VISIBLE);
        anim.start();

        // Scroll the chat so the keyboard doesn't hide any messages
        scrollChatDown();
    }

    @Override
    public void hideChatLayout() {
        // Get the center for the clipping circle
        int cx = chatLayout.getWidth() / 2;
        int cy = chatLayout.getHeight() / 2;

        // Get the initial radius for the clipping circle
        float initialRadius = (float) Math.hypot(cx, cy);

        // Create the animation (the final radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(chatLayout, cx, cy, initialRadius, 0);

        // Make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                chatLayout.setVisibility(View.INVISIBLE);
                // Hide the keyboard if visible and show the search button again
                hideSoftKeyboard();
                searchBtn.show();
            }
        });

        // Start the animation
        anim.start();
    }

    @Override
    public void hideChatLayoutWithDelay(final int delayMs) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideChatLayout();
                    }
                }, delayMs);
            }
        });
    }

    @Override
    public void clearMap() {
        if (map.getAnnotations().size() > 0) {
            map.clear();
        }
        map.setMyLocationEnabled(false);
    }

    @Override
    public void animateToRouteBounds(Position origin, Position destination) {

        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                .include(new LatLng(origin.getLatitude(), origin.getLongitude())) // Origin
                .include(new LatLng(destination.getLatitude(), destination.getLongitude())) // Destination
                .build();

        map.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 200), 3500);
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
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(chatLayout.getWindowToken(), 0);
    }

    @Override
    public void scrollChatDown() {
        chatList.scrollToPosition(chatAdapter.getItemCount() - 1);
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
    public void showCurrentLocation() {
        toggleGps(true);
    }

    private void toggleGps(boolean enableGps) {
        if (enableGps) {
            // Check if user has granted location permission
            permissionsManager = new PermissionsManager(this);
            if (!PermissionsManager.areLocationPermissionsGranted(this)) {
                permissionsManager.requestLocationPermissions(this);
            } else {
                enableLocation(true);
            }
        } else {
            enableLocation(false);
        }
    }

    private void enableLocation(boolean enabled) {
        if (enabled) {
            // If we have the last location of the user, we can move the camera to that position.
            Location lastLocation = locationEngine.getLastLocation();
            if (lastLocation != null) {
                map.easeCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 16), 3500);
            }

            locationEngineListener = new LocationEngineListener() {
                @Override
                public void onConnected() {
                    // No action needed here.
                }

                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        // Move the map camera to where the user location is and then remove the
                        // listener so the camera isn't constantly updating when the user location
                        // changes. When the user disables and then enables the location again, this
                        // listener is registered again and will adjust the camera once again.
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 16));
                        locationEngine.removeLocationEngineListener(this);
                    }
                }
            };
            locationEngine.addLocationEngineListener(locationEngineListener);
        }
        // Enable or disable the location layer on the map
        map.setMyLocationEnabled(enabled);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "This app needs location permissions in order to show its functionality.",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocation(true);
        } else {
            Toast.makeText(this, "You didn't grant location permissions.",
                    Toast.LENGTH_LONG).show();
            finish();
        }
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
        // Ensure no memory leak occurs if we register the location listener but the call hasn't
        // been made yet.
        if (locationEngineListener != null) {
            locationEngine.removeLocationEngineListener(locationEngineListener);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
