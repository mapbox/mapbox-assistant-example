package com.mapbox.alexa;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.google.gson.Gson;
import com.mapbox.services.commons.models.Position;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * Store user location on S3
 */
public class StorageManager {

    private final static String BUCKET_NAME = "com-mapbox-alexa";

    private String userId;
    private AmazonS3 s3;

    public StorageManager(String userId) {
        this.userId = userId;
        s3 = AmazonS3ClientBuilder.defaultClient();
    }

    public Position getHomeAddress() {
        return getPositionFromKey(getHomeKey());
    }

    public void setHomeAddress(Position homeAddress) {
        // Workaround to avoid custom Gson serialization
        Position withAltitude = Position.fromCoordinates(homeAddress.getLongitude(), homeAddress.getLatitude(), 0.0);
        s3.putObject(BUCKET_NAME, getHomeKey(), new Gson().toJson(withAltitude));
    }

    public Position getOfficeAddress() {
        return getPositionFromKey(getOfficeKey());
    }

    public void setOfficeAddress(Position officeAddress) {
        // Workaround to avoid custom Gson serialization
        Position withAltitude = Position.fromCoordinates(officeAddress.getLongitude(), officeAddress.getLatitude(), 0.0);
        s3.putObject(BUCKET_NAME, getOfficeKey(), new Gson().toJson(withAltitude));
    }

    private String getHomeKey() {
        return String.format(Locale.US, "%s-home.json", userId);
    }

    private String getOfficeKey() {
        return String.format(Locale.US, "%s-office.json", userId);
    }

    private Position getPositionFromKey(String key) {
        try {
            S3Object object = s3.getObject(new GetObjectRequest(BUCKET_NAME, key));
            BufferedReader reader = new BufferedReader(new InputStreamReader(object.getObjectContent()));
            StringBuilder sb = new StringBuilder();
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                sb.append(line);
            }
            return new Gson().fromJson(sb.toString(), Position.class);
        } catch (Exception e) {
            return null;
        }
    }
}
