package com.mapbox.alexa;

import com.amazon.speech.slu.Slot;
import com.mapbox.services.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.services.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.commons.utils.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

import java.util.Locale;

/**
 * Created by antonio on 2/1/17.
 */
public class AddressUtils {

    private static final Logger log = LoggerFactory.getLogger(AddressUtils.class);

    private static String getSafeValue(Slot slot) {
        try {
            return slot.getValue();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getAddressFromSlot(Slot postalAddress, Slot city, Slot landmark) {
        String landmarkValue = getSafeValue(landmark);
        if (!TextUtils.isEmpty(landmarkValue)) {
            return landmarkValue;
        }

        String addressValue = getSafeValue(postalAddress);
        String cityValue = getSafeValue(city);
        if (!TextUtils.isEmpty(addressValue) && !TextUtils.isEmpty(cityValue)) {
            return String.format(Locale.US, "%s, %s", addressValue, cityValue);
        } else if (!TextUtils.isEmpty(addressValue)) {
            return addressValue;
        } else {
            return cityValue;
        }
    }

    public static Position getCoordinatesFromAddress(String address, Position proximity) {
        try {
            log.info(String.format("Looking for '%s'.", address));
            MapboxGeocoding.Builder builder = new MapboxGeocoding.Builder()
                    .setAccessToken(Constants.MAPBOX_ACCESS_TOKEN)
                    .setMode(GeocodingCriteria.MODE_PLACES)
                    .setCountry("US")
                    .setLimit(1)
                    .setLocation(address);

            if (proximity != null) {
                // Add support for bias
                builder.setProximity(proximity);
            }

            Response<GeocodingResponse> response = builder.build().executeCall();
            double[] coordinates = response.body().getFeatures().get(0).getCenter();
            log.info(String.format("Found at '%f,%f'.", coordinates[0], coordinates[1]));
            return Position.fromCoordinates(coordinates);
        } catch (Exception e) {
            log.error("Geocoding failed.", e);
        }

        return null;
    }

}
