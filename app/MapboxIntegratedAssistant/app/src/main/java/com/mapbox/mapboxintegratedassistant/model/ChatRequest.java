package com.mapbox.mapboxintegratedassistant.model;

import android.os.AsyncTask;

import com.mapbox.mapboxintegratedassistant.MiaConstants;
import com.mapbox.mapboxintegratedassistant.MiaPresenter;

import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Fulfillment;
import ai.api.model.Result;

public class ChatRequest extends AsyncTask<AIRequest, Void, AIResponse> {

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
            e.printStackTrace();
        }
        return null;
    }
    @Override
    protected void onPostExecute(AIResponse aiResponse) {
        if (aiResponse != null) {
            final Result result = aiResponse.getResult();
            final Fulfillment fulfillment = result.getFulfillment();

            // Use the display text for the response
            presenter.onQueryResponseReceived(fulfillment.getSpeech());

            if (!result.isActionIncomplete()) {
                // Completed an action - go through available actions to work with
                switch (result.getAction()) {
                    case MiaConstants.ACTION_SHOW_ROUTE_OD:
                        presenter.drawRouteOriginDestination(result);
                        break;
                    case MiaConstants.ACTION_SHOW_CURRENT_LOCATION:
                        presenter.showCurrentLocation();
                        break;
                }
            }
        }
    }
}
