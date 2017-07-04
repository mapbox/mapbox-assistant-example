package com.mapbox.alexa;

import com.mapbox.services.api.staticimage.v1.MapboxStaticImage;
import com.mapbox.services.api.staticimage.v1.models.StaticMarkerAnnotation;
import com.mapbox.services.api.staticimage.v1.models.StaticPolylineAnnotation;
import com.mapbox.services.commons.models.Position;

/**
 * Create static maps for cards.
 */
public class ImageComponent {

    // 720w x 480h
    private static final int smallWidth = 720;
    private static final int smallHeight = 480;

    // 1200w x 800h
    private static final int largeWidth = 1200;
    private static final int largeHeight = 800;

    // https://www.mapbox.com/base/styling/color/
    private static final String COLOR_GREEN = "56b881";
    private static final String COLOR_RED = "e55e5e";
    private static final String COLOR_BLUE = "3887be";

    public static String getWelcomeMap(boolean isSmall) {
        MapboxStaticImage client = new MapboxStaticImage.Builder()
                .setAccessToken(Constants.MAPBOX_ACCESS_TOKEN)
                .setWidth(isSmall ? smallWidth : largeWidth)
                .setHeight(isSmall ? smallHeight : largeHeight)
                .setStyleId(com.mapbox.services.Constants.MAPBOX_STYLE_SATELLITE)
                .setLat(0.0).setLon(0.0)
                .setZoom(0)
                .build();
        return client.getUrl().toString();
    }

    public static String getLocationMap(Position position, boolean isSmall) {
        StaticMarkerAnnotation marker = new StaticMarkerAnnotation.Builder()
                .setName(com.mapbox.services.Constants.PIN_LARGE)
                .setPosition(position)
                .setColor(COLOR_RED)
                .build();
        MapboxStaticImage client = new MapboxStaticImage.Builder()
                .setAccessToken(Constants.MAPBOX_ACCESS_TOKEN)
                .setWidth(isSmall ? smallWidth : largeWidth)
                .setHeight(isSmall ? smallHeight : largeHeight)
                .setStyleId(com.mapbox.services.Constants.MAPBOX_STYLE_STREETS)
                .setPosition(position)
                .setStaticMarkerAnnotations(marker)
                .setZoom(15)
                .build();
        return client.getUrl().toString();
    }

    public static String getRouteMap(Position origin, Position destination, String geometry, boolean isSmall) {
        StaticMarkerAnnotation markerOrigin = new StaticMarkerAnnotation.Builder()
                .setName(com.mapbox.services.Constants.PIN_LARGE)
                .setPosition(origin)
                .setColor(COLOR_GREEN)
                .build();
        StaticMarkerAnnotation markerDestination = new StaticMarkerAnnotation.Builder()
                .setName(com.mapbox.services.Constants.PIN_LARGE)
                .setPosition(destination)
                .setColor(COLOR_RED)
                .build();
        StaticPolylineAnnotation route = new StaticPolylineAnnotation.Builder()
                .setPolyline(geometry)
                .setStrokeColor(COLOR_BLUE)
                .setStrokeOpacity(1)
                .setStrokeWidth(5)
                .build();
        MapboxStaticImage client = new MapboxStaticImage.Builder()
                .setAccessToken(Constants.MAPBOX_ACCESS_TOKEN)
                .setWidth(isSmall ? smallWidth : largeWidth)
                .setHeight(isSmall ? smallHeight : largeHeight)
                .setStyleId(com.mapbox.services.Constants.MAPBOX_STYLE_TRAFFIC_DAY)
                .setAuto(true)
                .setStaticMarkerAnnotations(markerOrigin, markerDestination)
                .setStaticPolylineAnnotations(route)
                .setZoom(15)
                .build();
        return client.getUrl().toString();
    }
}
