package com.mapbox.mapboxintegratedassistant.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.mapbox.mapboxintegratedassistant.model.ChatObject;

public abstract class BaseViewHolder extends RecyclerView.ViewHolder {

    BaseViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void onBindView(ChatObject object);
}
