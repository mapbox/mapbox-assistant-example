package com.mapbox.alexa;

import com.amazon.speech.slu.Slot;
import com.mapbox.services.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.services.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.services.commons.models.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

/**
 * Created by antonio on 2/1/17.
 */
public class AddressUtils {

    private static final Logger log = LoggerFactory.getLogger(AddressUtils.class);

    public static String getAddressFromSlot(Slot slot) {
        return slot.getValue();
    }

    public static Position getCoordinatesFromAddress(String address) {
        try {
            MapboxGeocoding client = new MapboxGeocoding.Builder()
                    .setAccessToken(Constants.MAPBOX_ACCESS_TOKEN)
                    .setMode(GeocodingCriteria.MODE_PLACES)
                    .setCountry("US")
                    .setGeocodingType(GeocodingCriteria.TYPE_ADDRESS)
                    .setLimit(1)
                    .setLocation(address)
                    .build();
            Response<GeocodingResponse> response = client.executeCall();
            double[] coordinates = response.body().getFeatures().get(0).getCenter();
            log.info(String.format("Address '%s' is at %f, %f", address, coordinates[0], coordinates[1]));
            return Position.fromCoordinates(coordinates);
        } catch (Exception e) {
            log.error("Geocoding failed.", e);
        }

        return null;
    }

}
