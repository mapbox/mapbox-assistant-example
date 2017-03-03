package com.mapbox.mapboxintegratedassistant;

import android.graphics.Color;
import android.util.Log;

import com.google.gson.JsonElement;
import com.mapbox.mapboxintegratedassistant.model.BotResponse;
import com.mapbox.mapboxintegratedassistant.model.ChatObject;
import com.mapbox.mapboxintegratedassistant.model.ChatRequest;
import com.mapbox.mapboxintegratedassistant.model.InputObject;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.services.Constants;
import com.mapbox.services.commons.ServicesException;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.directions.v5.DirectionsCriteria;
import com.mapbox.services.directions.v5.MapboxDirections;
import com.mapbox.services.directions.v5.models.DirectionsResponse;
import com.mapbox.services.directions.v5.models.DirectionsRoute;
import com.mapbox.services.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.geocoding.v5.MapboxGeocoding;
import com.mapbox.services.geocoding.v5.models.CarmenFeature;
import com.mapbox.services.geocoding.v5.models.GeocodingResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.api.AIDataService;
import ai.api.model.AIRequest;
import ai.api.model.Result;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MiaPresenter implements MiaContract.Presenter {

    private static final String LOG_TAG = MiaPresenter.class.getSimpleName();

    private MiaContract.View view;
    private ArrayList<ChatObject> chatObjects;

    private Position origin;
    private Position destination;
    private DirectionsRoute currentRoute;

    MiaPresenter(ArrayList<ChatObject> chatObjects) {
        this.chatObjects = chatObjects;
    }

    @Override
    public void attachView(MiaContract.View view) {
        this.view = view;
    }

    @Override
    public void addChatObject(ChatObject object) {
        chatObjects.add(object);
        view.notifyAdapterItemInserted(chatObjects.size() - 1);
        // Scroll down to bottom of list when new object is added
        view.scrollChatDown();
    }

    /**
     * Used to send requests to API.AI
     * Can come from either search box or microphone
     * @param queryText - text entered or interpreted
     */
    @Override
    public void sendQueryRequest(AIDataService service, String queryText) {
        // Add an input object to show the query in the chat
        InputObject input = new InputObject();
        input.setText(queryText);
        addChatObject(input);

        // Send the request to API.AI here
        AIRequest request = new AIRequest();
        request.setQuery(queryText);
        new ChatRequest(service, this).execute(request);
    }

    /**
     * Called after async task to API.AI is done executing and
     * we have a response from their service
     * @param responseText - text to be displayed in the chat view
     */
    @Override
    public void onQueryResponseReceived(String responseText) {
        // Add an response object to the chat
        BotResponse response = new BotResponse();
        response.setText(responseText);
        addChatObject(response);
    }

    /**
     * Use this to call Mapbox geocoder to resolve a String place to given coordinates
     * from the return Position object
     * @param location - String location of type place
     * @param entity - For now, to decide whether to populate origin or destination
     */
    @Override
    public void setMapPositionFromLocation(final String location, final String entity) {
        try {
            MapboxGeocoding client = new MapboxGeocoding.Builder()
                    .setAccessToken(MiaConstants.MAPBOX_ACCESS_TOKEN)
                    .setMode(GeocodingCriteria.MODE_PLACES)
                    .setGeocodingTypes(new String[]{GeocodingCriteria.TYPE_PLACE})
                    .setLocation(location)
                    .build();

            client.enqueueCall(new Callback<GeocodingResponse>() {
                @Override
                public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                    List<CarmenFeature> results = response.body().getFeatures();
                    if (results.size() > 0) {
                        // Log the first results position.
                        Position position = results.get(0).asPosition();
                        Log.d(LOG_TAG, "onResponse: " + position.toString());

                        // Add a marker to the map based on the position
                        view.addMarkerToMap(new MarkerOptions()
                                .position(new LatLng(position.getLatitude(), position.getLongitude()))
                                .setTitle(location));

                        // Switch on the entity to populate origin and destination Positions
                        switch (entity) {
                            case MiaConstants.ENTITY_ORIGIN:
                                origin = position;
                                break;
                            case MiaConstants.ENTITY_DESTINATION:
                                destination = position;
                                break;
                        }

                        // Once we have both origin and destination, draw the route
                        if (origin != null && destination != null) {
                            try {
                                getRoute(origin, destination);
                                view.animateToRouteBounds(origin, destination);
                            } catch (ServicesException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        // No result for your request were found.
                        Log.d(LOG_TAG, "onResponse: No result found");
                    }
                }

                @Override
                public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            BotResponse response = new BotResponse();
            response.setText("Sorry, I'm have trouble finding directions right now.");
            addChatObject(response);
        }
    }

    /**
     * Called when we receive a complete show.route.origin.destination action
     * @param result - extract the Strings origin and destination from Result
     */
    @Override
    public void drawRouteOriginDestination(Result result) {
        // Hide the chat layout - done talking to the bot at this point
        view.hideChatLayoutWithDelay(1500);

        // Clear current route data / map annotations
        origin = null;
        destination = null;
        view.clearMap();

        final HashMap<String, JsonElement> params = result.getParameters();
        if (params != null && !params.isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                // Check each key to extract the origin and destination entities
                switch (entry.getKey()) {
                    case MiaConstants.ENTITY_ORIGIN:
                        setMapPositionFromLocation(entry.getValue().toString(),
                                MiaConstants.ENTITY_ORIGIN);
                        break;
                    case MiaConstants.ENTITY_DESTINATION:
                        setMapPositionFromLocation(entry.getValue().toString(),
                                MiaConstants.ENTITY_DESTINATION);
                        break;
                }
            }
        }
    }

    private void getRoute(Position origin, Position destination) throws ServicesException {
        try {
            MapboxDirections client = new MapboxDirections.Builder()
                    .setOrigin(origin)
                    .setDestination(destination)
                    .setProfile(DirectionsCriteria.PROFILE_DRIVING)
                    .setAccessToken(MiaConstants.MAPBOX_ACCESS_TOKEN)
                    .build();

            client.enqueueCall(new Callback<DirectionsResponse>() {
                @Override
                public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                    // You can get the generic HTTP info about the response
                    Log.d(LOG_TAG, "Response code: " + response.code());
                    if (response.body() == null) {
                        Log.e(LOG_TAG, "No routes found, make sure you set the right user and access token.");
                        return;
                    } else if (response.body().getRoutes().size() < 1) {
                        Log.e(LOG_TAG, "No routes found");
                        return;
                    }

                    // Print some info about the route
                    currentRoute = response.body().getRoutes().get(0);
                    Log.d(LOG_TAG, "Distance: " + currentRoute.getDistance());

                    // Draw the route on the map
                    drawRoute(currentRoute);
                }

                @Override
                public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                    Log.e(LOG_TAG, "Error: " + throwable.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            BotResponse response = new BotResponse();
            response.setText("Sorry, I'm have trouble finding directions right now.");
            addChatObject(response);
        }

    }

    private void drawRoute(DirectionsRoute route) {
        // Convert LineString coordinates into LatLng[]
        LineString lineString = LineString.fromPolyline(route.getGeometry(), Constants.OSRM_PRECISION_V5);
        List<Position> coordinates = lineString.getCoordinates();
        LatLng[] points = new LatLng[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            points[i] = new LatLng(
                    coordinates.get(i).getLatitude(),
                    coordinates.get(i).getLongitude());
        }

        // Draw Points on MapView
        view.addPolylineToMap(new PolylineOptions()
                .add(points)
                .color(Color.parseColor("#009688"))
                .width(5));
    }

    @Override
    public void showCurrentLocation() {
        // Hide the chat layout
        view.hideChatLayoutWithDelay(1500);
        view.showCurrentLocation();
    }
}
