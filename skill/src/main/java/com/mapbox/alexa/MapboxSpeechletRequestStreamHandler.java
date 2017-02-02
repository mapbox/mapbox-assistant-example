package com.mapbox.alexa;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * Handler for AWS Lambda requests (com.mapbox.alexa.MapboxSpeechletRequestStreamHandler)
 */
public class MapboxSpeechletRequestStreamHandler  extends SpeechletRequestStreamHandler {

    private static final Set<String> supportedApplicationIds = new HashSet<>();

    static {
        supportedApplicationIds.add(Constants.APPLICATION_ID);
    }

    public MapboxSpeechletRequestStreamHandler() {
        super(new MapboxSpeechlet(), supportedApplicationIds);
    }

}
