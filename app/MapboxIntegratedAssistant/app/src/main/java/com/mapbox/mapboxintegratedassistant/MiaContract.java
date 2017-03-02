package com.mapbox.mapboxintegratedassistant;

import ai.api.AIDataService;

public interface MiaContract {

    interface View {
        /**
         * Used to let the adapter know we updated our list of chat objects
         * @param position - position where object was added in the stack
         */
        void notifyAdapterItemInserted(int position);

        void hideSoftKeyboard();

        /**
         * Will scroll chat to bottom if it currently is not
         */
        void scrollChatDown();
    }

    interface Presenter {

        void attachView(MiaContract.View view);

        void sendQueryRequest(AIDataService service, String queryText);

        void onQueryResponseReceived(String responseText);
    }
}
