package com.example.exoplayer;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private ArrayList<VideoObject> videoList;
    private Context aContext;
    private int gridLayout;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView durationTextView;
        TextView dateTextView;
        TextView sizeTextView;
        ImageView thumbnailsImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.video_name_text_view);
            durationTextView = itemView.findViewById(R.id.video_duration_text_view);
            dateTextView = itemView.findViewById(R.id.video_date_text_view);
            thumbnailsImageView = itemView.findViewById(R.id.video_thumbnail_image_view);
            sizeTextView = itemView.findViewById(R.id.video_size_text_view);
        }
    }

    public RecyclerAdapter(ArrayList<VideoObject> videoList, Context context, int layout) {
        this.videoList = videoList;
        this.aContext = context;
        this.gridLayout = layout;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View currentView = (View) LayoutInflater.from(parent.getContext()).inflate(gridLayout, parent, false);
        final ViewHolder holder = new ViewHolder(currentView);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        VideoObject currentVideo = videoList.get(position);
        holder.nameTextView.setText(currentVideo.getName());
        holder.nameTextView.setSelected(true);
        holder.dateTextView.setText(currentVideo.getDate());
        //nếu đang ở chế độ gridView 1 cột thì nhóm hai String thời lượng và kích cỡ của video làm một String
        if (gridLayout == R.layout.video_item_layout) {
            holder.durationTextView.setText(currentVideo.getDuration() + "  |  " + currentVideo.getSize());
        } else if (gridLayout == R.layout.video_item_layout_grid) {
            holder.durationTextView.setText(currentVideo.getDuration());
        }

        //lấy ra đường dẫn của video
        String path = currentVideo.getPath();
        //load ảnh thumbnail cho video item, sử dụng thư viện Glide
        GlideApp.with(aContext).load(path).centerCrop().into(holder.thumbnailsImageView);

        final int POS = position; //vị trí của item
        //set event onClick cho từng item, do recyclerView đã bỏ hỗ trợ onItemClick
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent videoIntent = new Intent(aContext, PlayerActivity.class);
                //truyền đường dẫn của video
                String clickedVideoPath = videoList.get(POS).getPath();
                videoIntent.putExtra("video path", clickedVideoPath);
                //truyền tên video
                String clickedVideoName = videoList.get(POS).getName();
                videoIntent.putExtra("video name", clickedVideoName);

                aContext.startActivity(videoIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }
}
