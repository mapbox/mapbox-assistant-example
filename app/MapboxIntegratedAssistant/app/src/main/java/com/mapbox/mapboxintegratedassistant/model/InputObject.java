package com.mapbox.mapboxintegratedassistant.model;

public class InputObject extends ChatObject {

    @Override
    public int getType() {
        return ChatObject.INPUT_OBJECT;
    }
}
