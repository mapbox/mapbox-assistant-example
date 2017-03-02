package com.mapbox.mapboxintegratedassistant;

import com.mapbox.mapboxintegratedassistant.model.BotResponse;
import com.mapbox.mapboxintegratedassistant.model.ChatObject;
import com.mapbox.mapboxintegratedassistant.model.ChatRequest;
import com.mapbox.mapboxintegratedassistant.model.InputObject;

import java.util.ArrayList;

import ai.api.AIDataService;
import ai.api.model.AIRequest;

public class MiaPresenter implements MiaContract.Presenter {

    private MiaContract.View view;
    private ArrayList<ChatObject> chatObjects;

    public MiaPresenter(ArrayList<ChatObject> chatObjects) {
        this.chatObjects = chatObjects;
    }

    @Override
    public void attachView(MiaContract.View view) {
        this.view = view;
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
        chatObjects.add(input);
        view.notifyAdapterItemInserted(chatObjects.size() - 1);

        // Scroll down to bottom of list when new object is added
        view.scrollChatDown();

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
        chatObjects.add(response);
        view.notifyAdapterItemInserted(chatObjects.size() - 1);
        view.scrollChatDown();
    }
}
