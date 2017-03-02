package com.mapbox.mapboxintegratedassistant.model;

public class BotResponse extends ChatObject {

    @Override
    public int getType() {
        return ChatObject.RESPONSE_OBJECT;
    }
}
