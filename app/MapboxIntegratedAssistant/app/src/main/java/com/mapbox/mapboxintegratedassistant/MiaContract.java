package com.mapbox.mapboxintegratedassistant;

import android.support.annotation.NonNull;

import com.mapbox.mapboxintegratedassistant.model.ChatObject;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.services.commons.models.Position;

import ai.api.AIDataService;
import ai.api.model.Result;

interface MiaContract {

    interface View {
        /**
         * Used to let the adapter know we updated our list of chat objects
         * @param position - position where object was added in the stack
         */
        void notifyAdapterItemInserted(int position);

        void hideSoftKeyboard();

        /**
         * Starts hide animation to collapse the chat layout
         * Also hides soft keyboard and shows search FAB
         */
        void hideChatLayout();

        void hideChatLayoutWithDelay(int delayMs);

        /**
         * Add a marker on the map given provided MarkerOptions
         * @param markerOptions - MarkerOptions object from Mapbox SDK
         */
        void addMarkerToMap(MarkerOptions markerOptions);

        /**
         * Used to draw line on map given the options provided
         * @param polylineOptions - PolylineOptions needed to display the line
         */
        void addPolylineToMap(PolylineOptions polylineOptions);

        /**
         * Will scroll chat to bottom if it currently is not
         */
        void scrollChatDown();

        /**
         * Called to animate the map to the route the user requested
         * @param origin - Position origin object
         * @param destination - Position destination
         */
        void animateToRouteBounds(Position origin, Position destination);

        /**
         * Clears the map off all annotations
         * Called before we draw a new route
         */
        void clearMap();

        void showCurrentLocation();

        void announceResponse(String responseText);
    }

    interface Presenter {

        void attachView(MiaContract.View view);

        /**
         * Will add object to ArrayList of ChatObjects
         * as well as notify the adapter an item was added
         * @param object - object to be added
         */
        void addChatObject(ChatObject object);

        /**
         * Used to send requests to API.AI
         * Can come from either search box or microphone
         * @param queryText - text entered or interpreted
         */
        void sendQueryRequest(AIDataService service, @NonNull String queryText);

        /**
         * Called after async task to API.AI is done executing and
         * we have a response from their service
         * @param responseText - text to be displayed in the chat view
         */
        void onQueryResponseReceived(String responseText);

        /**
         * Use this to call Mapbox geocoder to resolve a String place to given coordinates
         * from the return Position object
         * @param location - String location of type place
         * @param entity - For now, to decide whether to populate origin or destination
         */
        void setMapPositionFromLocation(String location, String entity);

        /**
         * Called when we receive a complete show.route.origin.destination action
         * @param result - extract the Strings origin and destination from Result
         */
        void drawRouteOriginDestination(Result result);

        /**
         * Will fire Mapbox code for showing current user location'
         * Includes detecting permissions and moving the camera to the location itself
         */
        void showCurrentLocation();
    }
}
