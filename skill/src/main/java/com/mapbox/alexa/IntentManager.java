package com.mapbox.alexa;

import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.*;
import com.mapbox.services.api.directions.v5.DirectionsCriteria;
import com.mapbox.services.api.directions.v5.MapboxDirections;
import com.mapbox.services.api.directions.v5.models.DirectionsResponse;
import com.mapbox.services.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.services.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.services.commons.models.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

import java.util.Locale;

/**
 * Build SpeechletResponse objects for all intents.
 */
public class IntentManager {

    private static final String CARD_TITLE = "Mapbox";

    private static final Logger log = LoggerFactory.getLogger(IntentManager.class);

    private StorageManager storageManager;

    public IntentManager(String userId) {
        storageManager = new StorageManager(userId);
    }

    public SpeechletResponse getWelcomeResponse() {
        String speechText = "Welcome to the Mapbox skill, feel free to ask for help.";

        // Create a standard card content
        StandardCard card = new StandardCard();
        card.setTitle(CARD_TITLE);
        card.setText(speechText);

        // Card image
        Image image = new Image();
        image.setSmallImageUrl(ImageComponent.getWelcomeMap(true));
        image.setLargeImageUrl(ImageComponent.getWelcomeMap(false));
        card.setImage(image);

        // Create the plain text output
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    public SpeechletResponse getHelpResponse() {
        String speechText = "You can ask for directions, commuting time, interesting places nearby, or even news.";

        // Create a standard card content
        StandardCard card = new StandardCard();
        card.setTitle(CARD_TITLE);
        card.setText(speechText);

        // Card image
        Image image = new Image();
        image.setSmallImageUrl(ImageComponent.getWelcomeMap(true));
        image.setLargeImageUrl(ImageComponent.getWelcomeMap(false));
        card.setImage(image);

        // Create the plain text output
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    public SpeechletResponse getHelloResponse() {
        String speechText = "Hey, this is Mapbox, how can I help you?";

        // Create a standard card content
        StandardCard card = new StandardCard();
        card.setTitle(CARD_TITLE);
        card.setText(speechText);

        // Card image
        Image image = new Image();
        image.setSmallImageUrl(ImageComponent.getWelcomeMap(true));
        image.setLargeImageUrl(ImageComponent.getWelcomeMap(false));
        card.setImage(image);

        // Create the plain text output
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    public SpeechletResponse getHomeAddressResponse(Slot postalAddress, Slot city) {
        String speechText;
        String cardText;
        Image image = null;

        String address = AddressUtils.getAddressFromSlot(postalAddress, city, null);

        try {
            Position position = AddressUtils.getCoordinatesFromAddress(address, null);
            storageManager.setHomeAddress(position);
            speechText = "Thank you, home address set.";
            cardText = String.format(Locale.US, "Thank you, home address set: %s", address);
            image = new Image();
            image.setSmallImageUrl(ImageComponent.getLocationMap(position, true));
            image.setLargeImageUrl(ImageComponent.getLocationMap(position, false));
        } catch (Exception e) {
            speechText = "Sorry, I couldn't find that address.";
            cardText = String.format(Locale.US, "Sorry, I couldn't find that address: %s", address);
        }

        // Create a standard card content
        StandardCard card = new StandardCard();
        card.setTitle(CARD_TITLE);
        card.setText(cardText);

        // Card image
        if (image != null) {
            card.setImage(image);
        }

        // Create the plain text output
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    public SpeechletResponse getOfficeAddressResponse(Slot postalAddress, Slot city) {
        String speechText;
        String cardText;
        Image image = null;

        String address = AddressUtils.getAddressFromSlot(postalAddress, city, null);

        try {
            Position proximity = storageManager.getHomeAddress();
            Position position = AddressUtils.getCoordinatesFromAddress(address, proximity);
            storageManager.setOfficeAddress(position);
            cardText = String.format(Locale.US, "Thank you, office address set: %s", address);
            speechText = "Thank you, office address set.";
            image = new Image();
            image.setSmallImageUrl(ImageComponent.getLocationMap(position, true));
            image.setLargeImageUrl(ImageComponent.getLocationMap(position, false));
        } catch (Exception e) {
            cardText = String.format(Locale.US, "Sorry, I couldn't find that address: %s", address);
            speechText = "Sorry, I couldn't find that address.";
        }

        // Create a standard card content
        StandardCard card = new StandardCard();
        card.setTitle(CARD_TITLE);
        card.setText(cardText);

        // Card image
        if (image != null) {
            card.setImage(image);
        }

        // Create the plain text output
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    public SpeechletResponse getCommuteResponse() {
        String speechText;
        Image image = null;

        Position origin = storageManager.getHomeAddress();
        Position destination = storageManager.getOfficeAddress();
        if (origin == null) {
            speechText = "Please set your home address first.";
        } else if (destination == null) {
            speechText = "Please set your office address first.";
        } else {
            try {
                MapboxDirections client = new MapboxDirections.Builder()
                        .setAccessToken(Constants.MAPBOX_ACCESS_TOKEN)
                        .setOrigin(origin)
                        .setDestination(destination)
                        .setProfile(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
                        .setGeometry(DirectionsCriteria.GEOMETRY_POLYLINE)
                        .build();
                Response<DirectionsResponse> response = client.executeCall();

                try {
                    // Get distance and duration
                    double distance = response.body().getRoutes().get(0).getDistance() / 1000; // km
                    double duration = response.body().getRoutes().get(0).getDuration() / 60; // min
                    speechText = String.format(Locale.US,
                            "Your commute is %.0f kilometers long, a %.0f minutes drive with current traffic.", distance, duration);
                    image = new Image();
                    String geometry = response.body().getRoutes().get(0).getGeometry();
                    image.setSmallImageUrl(ImageComponent.getRouteMap(origin, destination, geometry, true));
                    image.setLargeImageUrl(ImageComponent.getRouteMap(origin, destination, geometry, false));
                } catch (Exception e) {
                    speechText = "Sorry, I couldn't find directions from your place to the office.";
                    log.error("Route failed.", e);
                }
            } catch (Exception e) {
                speechText = "Sorry, something didn't work on our end. Please try again later.";
                log.error("Request failed.", e);
            }
        }

        // Create a standard card content
        StandardCard card = new StandardCard();
        card.setTitle(CARD_TITLE);
        card.setText(speechText);

        // Card image
        if (image != null) {
            card.setImage(image);
        }

        // Create the plain text output
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    public SpeechletResponse getDirectionsResponse(Slot postalAddress, Slot city, Slot landmark) {
        String speechText;
        Image image = null;

        Position origin = storageManager.getHomeAddress();
        if (origin == null) {
            speechText = "Please set your home address first.";
        } else {
            try {
                Position proximity = storageManager.getHomeAddress();
                String address = AddressUtils.getAddressFromSlot(postalAddress, city, landmark);
                Position position = AddressUtils.getCoordinatesFromAddress(address, proximity);
                MapboxDirections client = new MapboxDirections.Builder()
                        .setAccessToken(Constants.MAPBOX_ACCESS_TOKEN)
                        .setOrigin(origin)
                        .setDestination(position)
                        .setProfile(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
                        .setGeometry(DirectionsCriteria.GEOMETRY_POLYLINE)
                        .build();
                Response<DirectionsResponse> response = client.executeCall();

                try {
                    // Get distance and duration
                    double distance = response.body().getRoutes().get(0).getDistance() / 1000; // km
                    double duration = response.body().getRoutes().get(0).getDuration() / 60; // min
                    speechText = String.format(Locale.US,
                            "That address is a %.0f kilometers drive, %.0f minutes with current traffic.", distance, duration);
                    image = new Image();
                    String geometry = response.body().getRoutes().get(0).getGeometry();
                    image.setSmallImageUrl(ImageComponent.getRouteMap(origin, position, geometry, true));
                    image.setLargeImageUrl(ImageComponent.getRouteMap(origin, position, geometry, false));
                } catch (Exception e) {
                    speechText = "Sorry, I couldn't find directions from your location to that address.";
                    log.error("Route failed.", e);
                }
            } catch (Exception e) {
                speechText = "Sorry, I either couldn't find that address, or directions to that address.";
                log.error("Request failed.", e);
            }
        }

        // Create a standard card content
        StandardCard card = new StandardCard();
        card.setTitle(CARD_TITLE);
        card.setText(speechText);

        // Card image
        if (image != null) {
            card.setImage(image);
        }

        // Create the plain text output
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    public SpeechletResponse getPlacesResponse() {
        String speechText;
        Image image = null;

        Position origin = storageManager.getHomeAddress();
        if (origin == null) {
            speechText = "Please set your home address first.";
        } else {
            try {
                MapboxGeocoding client = new MapboxGeocoding.Builder()
                        .setAccessToken(Constants.MAPBOX_ACCESS_TOKEN)
                        .setMode(GeocodingCriteria.MODE_PLACES)
                        .setGeocodingTypes(new String[]{GeocodingCriteria.TYPE_POI, GeocodingCriteria.TYPE_POI_LANDMARK})
                        .setLimit(1)
                        .setCoordinates(origin)
                        .build();
                Response<GeocodingResponse> response = client.executeCall();

                try {
                    speechText = String.format("Have you tried %s?", response.body().getFeatures().get(0).getPlaceName());
                    Position position = response.body().getFeatures().get(0).asPosition();
                    image = new Image();
                    image.setSmallImageUrl(ImageComponent.getLocationMap(position, true));
                    image.setLargeImageUrl(ImageComponent.getLocationMap(position, false));
                } catch (Exception e) {
                    speechText = "Sorry, I couldn't find any interesting places nearby.";
                }
            } catch (Exception e) {
                speechText = "Sorry, something didn't work on our end. Please try again later.";
                log.error("Request failed.", e);
            }
        }

        // Create a standard card content
        StandardCard card = new StandardCard();
        card.setTitle(CARD_TITLE);
        card.setText(speechText);

        // Card image
        if (image != null) {
            card.setImage(image);
        }

        // Create the plain text output
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    public SpeechletResponse getBlogResponse() {
        BlogComponent blogComponent = new BlogComponent();
        String speechText = blogComponent.lastWeek(false);

        // Create a standard card content
        StandardCard card = new StandardCard();
        card.setTitle(CARD_TITLE);
        card.setText(speechText);

        // Card image
        Image image = new Image();
        image.setSmallImageUrl("https://cdn-images-1.medium.com/max/1900/1*w7UyBTg5rlFzxsCEYzsNeA@2x.png");
        image.setLargeImageUrl("https://cdn-images-1.medium.com/max/1900/1*w7UyBTg5rlFzxsCEYzsNeA@2x.png");
        card.setImage(image);

        // Create the plain text output
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }
}
