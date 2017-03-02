package com.mapbox.mapboxintegratedassistant;

import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;

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

        void hideChatLayout();

        /**
         * Add a marker on the map given provided MarkerOptions
         * @param markerOptions - MarkerOptions object from Mapbox SDK
         */
        void addMarkerToMap(MarkerOptions markerOptions);

        void addPolylineToMap(PolylineOptions polylineOptions);

        /**
         * Will scroll chat to bottom if it currently is not
         */
        void scrollChatDown();
    }

    interface Presenter {

        void attachView(MiaContract.View view);

        void sendQueryRequest(AIDataService service, String queryText);

        void onQueryResponseReceived(String responseText);

        void setMapPositionFromLocation(String location, String entity);

        void drawRouteOriginDestination(Result result);
    }
}
