package com.example.quizapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.quizapp.R;
import com.example.quizapp.activity.NotificationActivity;
import com.example.quizapp.activity.ScrollUser;
import com.example.quizapp.adapter.FeedAdapter;
import com.example.quizapp.api.IConnectAPI;
import com.example.quizapp.response.AdvertisementResponse;
import com.example.quizapp.response.FeedsListItem;
import com.example.quizapp.response.HomeFeedResponse;
import com.example.quizapp.response.LikeDislikePost;
import com.example.quizapp.response.ResponseItem;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FeedPageFragment extends Fragment implements FeedAdapter.FeedPageCommunicator {
    int likedBoolean;
    String likedPostId;
    String userId;
    FeedAdapter feedAdapter;
    ImageButton profileButton;
    ImageButton notificationButton;
    List<FeedsListItem> homeFeedResponses=new ArrayList<>();
    List<ResponseItem> advertisementResponses = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_feed_page, container,false);
        userId="8a8a7a69-b642-4f7f-a4ef-2b43a370c3fe";
        init(view);
        profileButton= view.findViewById(R.id.profileButton);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ScrollUser.class));
            }
        });

        notificationButton=view.findViewById(R.id.notificationButton);
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity((new Intent(getActivity(), NotificationActivity.class)));
            }
        });

        return view;
    }

    private void init(final View view) {
        final RecyclerView recyclerView=view.findViewById(R.id.feedRV);
        final List<FeedsListItem> feedList=new ArrayList<>();

        OkHttpClient client = new OkHttpClient.Builder().build();
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.177.7.137:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        IConnectAPI iConnectAPI=retrofit.create(IConnectAPI.class);
        iConnectAPI.getHomeFeedResponse(userId).enqueue(new Callback<HomeFeedResponse>() {
            @Override
            public void onResponse(Call<HomeFeedResponse> call, Response<HomeFeedResponse> response) {
                System.out.println("Inside response method");
                System.out.println(response.body());
                homeFeedResponses.addAll(response.body().getFeedsList());
            }

            @Override
            public void onFailure(Call<com.example.quizapp.response.HomeFeedResponse> call, Throwable t) {
                System.out.println("fail"+t.getLocalizedMessage());

            }
        });


        recyclerView.setLayoutManager( new LinearLayoutManager(getContext()));
        feedAdapter = new FeedAdapter(homeFeedResponses, likedBoolean, userId, FeedPageFragment.this);
        recyclerView.setAdapter(feedAdapter);

    }

    @Override
    public void returnLikeStatus(int likedBoolean, String postId) {
        this.likedBoolean = likedBoolean;
        this.likedPostId = postId;
        if (likedBoolean == 1)
            likePostMethod();
        if (likedBoolean==0)
            dislikePostMethod();
    }

    public void likePostMethod() {
        System.out.println("LIKING");
        OkHttpClient client = new OkHttpClient.Builder().build();
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.177.7.137:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        IConnectAPI iConnectAPI = retrofit.create(IConnectAPI.class);
            iConnectAPI.postLike(likedPostId, userId).enqueue(new Callback<LikeDislikePost>() {
                @Override
                public void onResponse(Call<LikeDislikePost> call, Response<LikeDislikePost> response) {
                    Log.d("TSTRESPONSE",response.body().toString());
                }

                @Override
                public void onFailure(Call<LikeDislikePost> call, Throwable t) {
                    System.out.println("Couldn't Like");

                }
            });
    }

    public void dislikePostMethod(){
        OkHttpClient client = new OkHttpClient.Builder().build();
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.177.7.137:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        IConnectAPI iConnectAPI = retrofit.create(IConnectAPI.class);
        iConnectAPI.postDislike(likedPostId,userId).enqueue(new Callback<LikeDislikePost>() {
            @Override
            public void onResponse(Call<LikeDislikePost> call, Response<LikeDislikePost> response) {
                Log.d("TSTRESPONSE",response.body().toString());
                //System.out.println(response.body().getMessage());
            }
            @Override
            public void onFailure(Call<LikeDislikePost> call, Throwable t) {
            }
        });
    }
}
