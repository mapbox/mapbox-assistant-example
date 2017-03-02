package com.mapbox.mapboxintegratedassistant.model;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonElement;
import com.mapbox.mapboxintegratedassistant.MiaPresenter;

import java.util.HashMap;
import java.util.Map;

import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Fulfillment;
import ai.api.model.Result;

public class ChatRequest extends AsyncTask<AIRequest, Void, AIResponse> {

    private final String LOG_TAG = ChatRequest.class.getSimpleName();

    private AIDataService service;
    private MiaPresenter presenter;

    public ChatRequest(AIDataService service, MiaPresenter presenter) {
        this.service = service;
        this.presenter = presenter;
    }

    @Override
    protected AIResponse doInBackground(AIRequest... requests) {
        final AIRequest request = requests[0];
        try {
            return service.request(request);
        } catch (AIServiceException e) {

        }
        return null;
    }
    @Override
    protected void onPostExecute(AIResponse aiResponse) {
        if (aiResponse != null) {
            final Result result = aiResponse.getResult();
            final Fulfillment fulfillment = result.getFulfillment();

            // TODO - remove logs
            final HashMap<String, JsonElement> params = result.getParameters();
            if (params != null && !params.isEmpty()) {
                Log.i(LOG_TAG, "Parameters: ");
                for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                    Log.i(LOG_TAG, String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
                }
            }

            // Use the display text for the response
            presenter.onQueryResponseReceived(fulfillment.getSpeech());
        }
    }
}
