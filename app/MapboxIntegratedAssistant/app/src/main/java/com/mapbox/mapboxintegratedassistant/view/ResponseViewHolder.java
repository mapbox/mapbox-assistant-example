package com.mapbox.mapboxintegratedassistant.view;

import android.view.View;
import android.widget.TextView;

import com.mapbox.mapboxintegratedassistant.R;
import com.mapbox.mapboxintegratedassistant.model.ChatObject;

class ResponseViewHolder extends BaseViewHolder {

    private TextView tvResponseText;

    ResponseViewHolder(View itemView) {
        super(itemView);
        this.tvResponseText = (TextView) itemView.findViewById(R.id.tv_response_text);
    }

    @Override
    public void onBindView(ChatObject object) {
        this.tvResponseText.setText(object.getText());
    }
}
