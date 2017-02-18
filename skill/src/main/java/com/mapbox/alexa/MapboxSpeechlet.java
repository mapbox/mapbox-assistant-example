package com.mapbox.alexa;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for speechlet requests
 */
public class MapboxSpeechlet implements Speechlet {

    private static final Logger log = LoggerFactory.getLogger(MapboxSpeechlet.class);

    @Override
    public void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {
        log.info("onSessionStarted: requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
    }

    @Override
    public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
        log.info("onLaunch: requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
        return new IntentManager(session.getUser().getUserId()).getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
        log.info("onIntent: requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;
        IntentManager intentManager = new IntentManager(session.getUser().getUserId());
        if (Constants.INTENT_HELP.equals(intentName)) {
            return intentManager.getHelpResponse();
        } else if (Constants.INTENT_HELLO.equals(intentName)) {
            return intentManager.getHelloResponse();
        } else if (Constants.INTENT_HOME_ADDRESS.equals(intentName)) {
            return intentManager.getHomeAddressResponse(
                    intent.getSlot("PostalAddress"), intent.getSlot("City"));
        } else if (Constants.INTENT_OFFICE_ADDRESS.equals(intentName)) {
            return intentManager.getOfficeAddressResponse(
                    intent.getSlot("PostalAddress"), intent.getSlot("City"));
        } else if (Constants.INTENT_COMMUTE.equals(intentName)) {
            return intentManager.getCommuteResponse();
        } else if (Constants.INTENT_DIRECTIONS.equals(intentName)) {
            return intentManager.getDirectionsResponse(
                    intent.getSlot("PostalAddress"), intent.getSlot("City"),
                    intent.getSlot("Landmark"));
        } else if (Constants.INTENT_PLACES.equals(intentName)) {
            return intentManager.getPlacesResponse();
        } else if (Constants.INTENT_BLOG.equals(intentName)) {
            return intentManager.getBlogResponse();
        } else {
            throw new SpeechletException("Invalid Intent.");
        }
    }

    @Override
    public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {
        log.info("onSessionEnded: requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
    }
}
