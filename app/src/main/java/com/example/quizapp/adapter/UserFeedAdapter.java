package com.example.quizapp.adapter;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.quizapp.R;
import com.example.quizapp.models.response.PostListItem;

import java.util.ArrayList;
import java.util.List;

public class UserFeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private List<PostListItem> postList;




    public UserFeedAdapter(List<PostListItem> postList) {
        this.postList=postList;
    }


    @Override
    public int getItemViewType(int position) {
        String type = postList.get(position).getType();
        if(type.equalsIgnoreCase("image"))
            return 1;
        else if(type.equalsIgnoreCase("video"))
            return 2;
        else
            return 0;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
//        if (postList.get(i).getType().equalsIgnoreCase("video")){
//            System.out.println("video type");
//            return new VideoViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_card_text, viewGroup, false));
//
//
//        } else if (postList.get(i).getType().equalsIgnoreCase("image")) {
//            System.out.println("Image type");
//            return new ImageViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_card_image_layout, viewGroup, false));
//
//        }
//        else
//        {   System.out.println("Text type");
//            return new TextViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_card_text, viewGroup, false));
//
//        }
        if(viewType==1){
            return new ImageViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_card_image_layout, viewGroup, false));
        }else if(viewType==2){
            return new VideoViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_card_text, viewGroup, false));
        }else{
            return new TextViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_card_text, viewGroup, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {


        if (viewHolder.getItemViewType() == 1)

        {  ((ImageViewHolder) viewHolder).bind(postList.get(position));

        }
        else if (viewHolder.getItemViewType() == 2 )
        {
            ((VideoViewHolder) viewHolder).bind(postList.get(position));


        }
        else {
            ((TextViewHolder) viewHolder).bind(postList.get(position));

        }



    }

    @Override
    public int getItemCount() {
        return postList.size();
    }



    class TextViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView userDp;
        TextView username;


        public TextViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.PostText);
            userDp=itemView.findViewById(R.id.userdp1);
            username=itemView.findViewById(R.id.username1);
        }
        public void bind(final PostListItem postListItem){
            this.textView.setText(String.valueOf("Post Text "+postListItem.getDescription()));
           this.userDp.setImageResource(R.drawable.bunny);
            this.username.setText("USERNAME" + postListItem.getUserId());






     } }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView userDp;
        TextView username;


        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.PostImage);
            userDp=itemView.findViewById(R.id.userdp2);
            username=itemView.findViewById(R.id.username2);
        }
        public void bind(final PostListItem postListItem){

            Glide.with(imageView.getContext()).load(postListItem.getUrl()).into(imageView);
            this.username.setText("USERNAME" + postListItem.getUserId());
            this.userDp.setImageResource(R.drawable.bunny);




    } }
    class VideoViewHolder extends RecyclerView.ViewHolder {
       VideoView videoView;
        ImageView userDp;
        TextView username;


        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.videoView);
            userDp=itemView.findViewById(R.id.userdp3);
            username=itemView.findViewById(R.id.username3);
        }
        public void bind(final PostListItem postListItem){

            this.userDp.setImageResource(R.drawable.bunny);
            this.username.setText("USERNAME" + postListItem.getUserId());

            Uri uri = Uri.parse(postListItem.getUrl());
            videoView.setVideoURI(uri);
            videoView.requestFocus();

            videoView.start();

        }

     }
    }

