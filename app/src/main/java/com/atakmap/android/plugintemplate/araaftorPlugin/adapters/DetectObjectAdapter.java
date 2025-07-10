package com.atakmap.android.plugintemplate.araaftorPlugin.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.atakmap.android.plugintemplate.araaftorPlugin.DetectObject;
import com.atakmap.android.plugintemplate.plugin.R;

import java.util.List;

public class DetectObjectAdapter extends RecyclerView.Adapter<DetectObjectAdapter.ViewHolder> {

    private List<DetectObject> itemList;
    private Context context;

    public void setItemList(List<DetectObject> itemList) {
        this.itemList = itemList;
    }

    private OnItemClickListener listener;
    public DetectObjectAdapter(List<DetectObject> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.button_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DetectObject currentItem = itemList.get(position);
        holder.itemTimestamp.setText(android.text.format.DateFormat.format("dd/MM/yy kk:mm:ss", currentItem.timestamp));
        int leftImageRes = context.getResources().getIdentifier(currentItem.imageName, "drawable", context.getPackageName());
        holder.itemImage.setImageResource(leftImageRes);
        holder.itemName.setText(currentItem.name);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentItem);
            }
        });
    }

        @Override
        public int getItemCount() {
            return itemList.size();
        }

    public interface OnItemClickListener {
        void onItemClick(DetectObject item);
    }

        public static class ViewHolder extends RecyclerView.ViewHolder {

            TextView itemName, itemTimestamp;
            ImageView itemImage;
            public ViewHolder(View itemView) {
                super(itemView);
                itemName = itemView.findViewById(R.id.nameText);
                itemTimestamp = itemView.findViewById(R.id.timeText);
                itemImage = itemView.findViewById(R.id.item_image);
            }
        }

}
