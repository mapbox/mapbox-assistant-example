package com.mapbox.mapboxintegratedassistant;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxintegratedassistant.model.ChatObject;
import com.mapbox.mapboxintegratedassistant.view.BaseViewHolder;
import com.mapbox.mapboxintegratedassistant.view.InputViewHolder;
import com.mapbox.mapboxintegratedassistant.view.ResponseViewHolder;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private ArrayList<ChatObject> chatObjects;

    public ChatAdapter(ArrayList<ChatObject> chatObjects) {
        this.chatObjects = chatObjects;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Create the ViewHolder based on the viewType
        if (viewType == ChatObject.INPUT_OBJECT) {
            View itemView = inflater.inflate(R.layout.user_input_layout, parent, false);
            return new InputViewHolder(itemView);
        } else if (viewType == ChatObject.RESPONSE_OBJECT){
            View itemView = inflater.inflate(R.layout.chat_response_layout, parent, false);
            return new ResponseViewHolder(itemView);
        } else {
            // No view type found - default to regular response
            View itemView = inflater.inflate(R.layout.chat_response_layout, parent, false);
            return new ResponseViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.onBindView(chatObjects.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        return chatObjects.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return chatObjects.size();
    }
}
