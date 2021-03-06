package com.example.quizapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quizapp.R;
import com.example.quizapp.activity.CreatePostActivity;
import com.example.quizapp.activity.NotificationActivity;
import com.example.quizapp.activity.ScrollUser;
import com.example.quizapp.activity.SearchActivity;
import com.example.quizapp.adapter.FeedAdapter;
import com.example.quizapp.api.AppController;
import com.example.quizapp.api.IConnectAPI;
import com.example.quizapp.models.Response.AdvertisementResponse;
import com.example.quizapp.models.Response.ResponseItem;
import com.example.quizapp.response.FeedsListItem;
import com.example.quizapp.response.HomeFeedResponse;
import com.example.quizapp.response.LikeDislikePost;


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
    TextView writePost;
    FeedAdapter feedAdapter;
    ImageButton profileButton;
    ImageButton notificationButton;
    LinearLayout searchBox;
    List<FeedsListItem> homeFeedResponses=new ArrayList<>();
    List<ResponseItem> advertisementResponses=new ArrayList<>();
    int count = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_feed_page, container,false);
        userId= AppController.sharedPreferences.getString("userId","");
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
        advertisementResponses=new ArrayList<>();
        writePost=view.findViewById(R.id.writePost);
        searchBox=view.findViewById(R.id.searchBox);
        searchBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(getActivity(), SearchActivity.class));
            }
        });
        writePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(getActivity(), CreatePostActivity.class));
            }
        });
        final RecyclerView recyclerView = view.findViewById(R.id.feedRV);
        final List<FeedsListItem> feedList = new ArrayList<>();
        OkHttpClient client = new OkHttpClient.Builder().build();
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.177.7.137:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        IConnectAPI iConnectAPI = retrofit.create(IConnectAPI.class);
        iConnectAPI.getHomeFeedResponse(userId).enqueue(new Callback<HomeFeedResponse>() {
            @Override
            public void onResponse(Call<HomeFeedResponse> call, Response<HomeFeedResponse> response) {
                if(null!=response.body()) {
                    if(response.body().getFeedsList().size()>0) {
                        System.out.println("Inside response method");
                        System.out.println(response.body());
                        homeFeedResponses.addAll(response.body().getFeedsList());
                        ++count;
                        setAdapter(recyclerView);
                    }else{
                        Toast.makeText(getActivity(), "No feeds available.", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getContext(), "Unable to reach server.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<com.example.quizapp.response.HomeFeedResponse> call, Throwable t) {
                System.out.println("fail" + t.getLocalizedMessage());
            }
        });

        final Retrofit retrofit2 = new Retrofit.Builder()
                .baseUrl("http://10.177.7.88:6000")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        IConnectAPI iConnectAPI2 = retrofit2.create(IConnectAPI.class);
        iConnectAPI2.getAdvertisements(AppController.sharedPreferences.getString("userId",""), 4).enqueue(new Callback<AdvertisementResponse>() {
            @Override
            public void onResponse(Call<AdvertisementResponse> call, Response<AdvertisementResponse> response) {
                if (("SUCCESS").equals(response.body().getStatus())) {

                    advertisementResponses.addAll(response.body().getResponse());
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    ++count;
                    setAdapter(recyclerView);
                } else {
                    Toast.makeText(getContext(), "Failure", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AdvertisementResponse> call, Throwable t) {
                System.out.println("fail" + t.getLocalizedMessage());
            }
        });

    }

    private void setAdapter(RecyclerView recyclerView) {
        if (count == 2) {
            feedAdapter = new FeedAdapter(homeFeedResponses, advertisementResponses, likedBoolean, userId, FeedPageFragment.this);
            recyclerView.setAdapter(feedAdapter);
        }
    }
    @Override
    public void returnLikeStatus(int likedBoolean, String postId) {
        this.likedBoolean = likedBoolean;
        this.likedPostId = postId;
        if (likedBoolean == 1)
            likePostMethod();
        if (likedBoolean == 0)
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
                Log.d("TSTRESPONSE", response.body().toString());
            }
            @Override
            public void onFailure(Call<LikeDislikePost> call, Throwable t) {
                System.out.println("Couldn't Like");
            }
        });
    }

    public void dislikePostMethod() {
        OkHttpClient client = new OkHttpClient.Builder().build();
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.177.7.137:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        IConnectAPI iConnectAPI = retrofit.create(IConnectAPI.class);
        iConnectAPI.postDislike(likedPostId, userId).enqueue(new Callback<LikeDislikePost>() {
            @Override
            public void onResponse(Call<LikeDislikePost> call, Response<LikeDislikePost> response) {
                Log.d("TSTRESPONSE", response.body().toString());
                //System.out.println(response.body().getMessage());
            }
            @Override
            public void onFailure(Call<LikeDislikePost> call, Throwable t) {
            }
        });
    }
}
