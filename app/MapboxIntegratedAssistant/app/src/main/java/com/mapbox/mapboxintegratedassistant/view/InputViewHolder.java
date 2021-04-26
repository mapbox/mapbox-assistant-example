package com.mapbox.mapboxintegratedassistant.view;

import android.view.View;
import android.widget.TextView;

import com.mapbox.mapboxintegratedassistant.R;
import com.mapbox.mapboxintegratedassistant.model.ChatObject;

class InputViewHolder extends BaseViewHolder {

    private TextView tvInputText;

    InputViewHolder(View itemView) {
        super(itemView);
        this.tvInputText = (TextView) itemView.findViewById(R.id.tv_input_text);
    }

    @Override
    public void onBindView(ChatObject object) {
        this.tvInputText.setText(object.getText());
    }
}
