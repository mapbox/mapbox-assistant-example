package com.mapbox.alexa;

import com.mapbox.services.commons.models.Position;
import com.mapbox.services.commons.utils.TextUtils;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the image component.
 */
public class ImageComponentTest {

    private final static String BASE_URL = "https://api.mapbox.com/styles/v1";

    @Test
    public void getWelcomeMapIsNotEmpty() {
        assertFalse(TextUtils.isEmpty(ImageComponent.getWelcomeMap(true)));
        assertTrue(ImageComponent.getWelcomeMap(true).startsWith(BASE_URL));
        assertFalse(TextUtils.isEmpty(ImageComponent.getWelcomeMap(false)));
        assertTrue(ImageComponent.getWelcomeMap(false).startsWith(BASE_URL));
    }

    @Test
    public void getLocationMapIsNotEmpty() {
        Position whiteHouse = Position.fromCoordinates(-77.0365, 38.8977);
        assertTrue(ImageComponent.getLocationMap(whiteHouse, true).startsWith(BASE_URL));
        assertTrue(ImageComponent.getLocationMap(whiteHouse, false).startsWith(BASE_URL));
    }

    @Test
    public void getRouteMapIsNotEmpty() {
        // From https://www.mapbox.com/api-playground/#/directions
        String geometry = "}jllF~_euMm@C?xC}B@?l@e@FItAaAhCaAH?ZqD`BaH@CpCe@f@ye@zTDZ_Bv@";

        Position whiteHouse = Position.fromCoordinates(-77.0365, 38.8977);
        Position dupontCircle = Position.fromCoordinates(-77.04341, 38.90962);
        assertTrue(ImageComponent.getRouteMap(whiteHouse, dupontCircle, geometry, true).startsWith(BASE_URL));
        assertTrue(ImageComponent.getRouteMap(whiteHouse, dupontCircle, geometry, false).startsWith(BASE_URL));
    }
}
