package com.example.quizapp.utils;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.quizapp.R;
import com.example.quizapp.api.AppController;
import com.example.quizapp.api.IConnectAPI;
import com.example.quizapp.models.DynamicQuestionDTO;
import com.example.quizapp.models.request.dynamicSubmitRequest.Request;
import com.example.quizapp.models.request.dynamicSubmitRequest.SubmitContest;
import com.example.quizapp.models.request.dynamicSubmitRequest.SubmitQuestion;
import com.example.quizapp.models.Response.DynamicResponse;
import com.example.quizapp.models.Response.GetQuestionWinner;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DynamicContestPage extends AppCompatActivity {

    private static final String DYNAMIC_CONTEST_DB_NODE = "Dynamic_Contest";
    TextView ques_tv;
    ImageView ques_iv;
    VideoView ques_vv;
    TextView ques_text;
    CheckBox cb1,cb2,cb3,cb4,cb5;
    Button b_submit_ques;
    long timer_halt=10000;
    long timer_ques;
    boolean run_ques=false;
    boolean run_halt=false;
    CountDownTimer countDownTimerQues;
    CountDownTimer countDownTimerHalt;
    View ques,halt;
    Integer currentQuestion = 1;
    TextView tv_halt_timer;
    TextView tv_ques_timer;
    String contestId="dbbfb173-f399-4619-93ad-448066cf6e99";
    IConnectAPI iConnectAPI;
    DynamicQuestionDTO dynamicQuestionDTO;
    String userId="Nitin";
    Button b_submit_contest;
    TextView tv_winner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_contest_page);

        ques_iv=findViewById(R.id.ques_iv);
        ques_vv=findViewById(R.id.ques_vv);
        ques_tv=findViewById(R.id.ques_tv);
        cb1=findViewById(R.id.checkBox);
        cb2=findViewById(R.id.checkBox2);
        cb3=findViewById(R.id.checkBox3);
        cb4=findViewById(R.id.checkBox4);
        cb5=findViewById(R.id.checkBox5);
        ques_text=findViewById(R.id.text);
        tv_ques_timer=findViewById(R.id.tv_ques_timer);
        tv_halt_timer=findViewById(R.id.tv_halt_timer);
        b_submit_ques =findViewById(R.id.submit);
        ques=findViewById(R.id.ques_view);
        halt=findViewById(R.id.halt_view);
        b_submit_contest =findViewById(R.id.b_submit);
        tv_winner=findViewById(R.id.tv_winner);

        iConnectAPI= AppController.dynamic_contest_retrofit.create(IConnectAPI.class);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        b_submit_ques.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit_ans();

            }
        });

        b_submit_contest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitContest();
            }
        });
        currentQuestionListener();
//        getQuestionFromFirebase(currentQuestion);

    }

    private void submitContest() {
        SubmitContest submitContest=new SubmitContest("",userId);
        iConnectAPI.submitDynamicContest(contestId,submitContest).enqueue(new Callback<DynamicResponse>() {
            @Override
            public void onResponse(Call<DynamicResponse> call, Response<DynamicResponse> response) {
                Log.d("Contest Submit : ",response.body().toString());
            }

            @Override
            public void onFailure(Call<DynamicResponse> call, Throwable t) {

            }
        });
    }

    private void currentQuestionListener() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(DYNAMIC_CONTEST_DB_NODE)
                .child(contestId).child("currentQuestion");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentQuestion = dataSnapshot.getValue(Integer.class);
                    getQuestionFromFirebase(currentQuestion);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void submit_ans() {

        String optionIds = getSelectedOptions();
        Request request=new Request(contestId,0,dynamicQuestionDTO.getQuestionId(),optionIds,"",0,0,"",userId);
        SubmitQuestion submitQuestion=new SubmitQuestion(request,userId);
        iConnectAPI.submitDynamicQuestion(contestId,submitQuestion).enqueue(new Callback<DynamicResponse>() {
            @Override
            public void onResponse(Call<DynamicResponse> call, Response<DynamicResponse> response) {
                if(response.body().getStatus().equals("success")) {
                    b_submit_ques.setVisibility(View.GONE);
                    showSnackBar("Your answer is submitted");
                }
                //Log.d("Submit Dynamic : ",response.body().toString());
            }

            @Override
            public void onFailure(Call<DynamicResponse> call, Throwable t) {

            }
        });
    }

    private String getSelectedOptions() {
        StringBuilder res=new StringBuilder();
        if(cb1.isChecked()){
            res.append(dynamicQuestionDTO.getOptionDTOList().get(0).getOptionId()+",");
        }
        if(cb2.isChecked()){
            res.append(dynamicQuestionDTO.getOptionDTOList().get(1).getOptionId()+",");
        }
        if(cb3.isChecked()){
            res.append(dynamicQuestionDTO.getOptionDTOList().get(2).getOptionId()+",");
        }
        if(cb4.isChecked()){
            res.append(dynamicQuestionDTO.getOptionDTOList().get(3).getOptionId()+",");
        }
        return res.toString();
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            startStopHalt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getQuestionFromFirebase(Integer questionSequence) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Dynamic_Contest")
                .child(contestId).child("questions").child(""+questionSequence);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dynamicQuestionDTO = dataSnapshot.getValue(DynamicQuestionDTO.class);
                if(dynamicQuestionDTO!=null) {
                    Log.d("DYNAMIC_QUESTION_UPDATE", dynamicQuestionDTO.toString());
                    try {

                        updateUI(dynamicQuestionDTO);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    Log.d("DYNAMIC_QUESTION_UPDATE", "NULL COMING");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void updateUI(DynamicQuestionDTO dynamicQuestionDTO) throws IOException {
        if(null==dynamicQuestionDTO)
        {

            ques.setVisibility(View.GONE);
            halt.setVisibility(View.VISIBLE);

            stopQuesTimer();
        }
        else
        {

            halt.setVisibility(View.GONE);
            ques.setVisibility(View.VISIBLE);
            b_submit_ques.setVisibility(View.VISIBLE);

            cb1.setChecked(false);
            cb2.setChecked(false);
            cb3.setChecked(false);
            cb4.setChecked(false);
            cb5.setChecked(false);

            stopHaltTimer();
            timer_ques=dynamicQuestionDTO.getDuration()*1000;

            if(dynamicQuestionDTO.getQuestionType().equals("Text"))
            {
                ques_vv.setVisibility(View.GONE);
                ques_iv.setVisibility(View.GONE);
                ques_tv.setVisibility(View.VISIBLE);
                ques_tv.setText(dynamicQuestionDTO.getQuestionName());


            }
            else if(dynamicQuestionDTO.getQuestionType().equals("Image")) {
                ques_vv.setVisibility(View. GONE);
                ques_tv.setVisibility(View.GONE);
                ques_iv.setVisibility(View.VISIBLE);
                String img2 = dynamicQuestionDTO.getQuestionContent();
//            String img2="https://static.standard.co.uk/s3fs-public/thumbnails/image/2018/12/17/09/lionelmessi1712.jpg";
                // Log.d("Image:",img2);
                //Toast.makeText(ContestPage.this, "" + img2, Toast.LENGTH_SHORT).show();
                Glide.with(this).load(img2).apply(new RequestOptions().override(500,500)).into(ques_iv);
            }
            else if(dynamicQuestionDTO.getQuestionType().equals("Video")){
                ques_iv.setVisibility(View.GONE);
                ques_tv.setVisibility(View.GONE);
                ques_vv.setVisibility(View.VISIBLE);
                if (ques_vv != null)
                {  ques_vv.setVideoURI(Uri.parse(dynamicQuestionDTO.getQuestionContent()));
                    ques_vv.requestFocus();
                    ques_vv.start();
                }
            }
            else if(dynamicQuestionDTO.getQuestionType().equals("Audio"))
            {
                ques_iv.setVisibility(View.GONE);
                ques_vv.setVisibility(View.GONE);
                ques_tv.setVisibility(View.VISIBLE);
                ques_tv.setText("Listen to audio...");
                Uri myUri = Uri.parse(dynamicQuestionDTO.getQuestionContent());

                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(getApplicationContext(), myUri);
                mediaPlayer.prepare();
                mediaPlayer.start();

            }

            try {
                cb1.setText(dynamicQuestionDTO.getOptionDTOList().get(0).getOptionContent());
                cb2.setText(dynamicQuestionDTO.getOptionDTOList().get(1).getOptionContent());
                cb3.setText(dynamicQuestionDTO.getOptionDTOList().get(2).getOptionContent());
                cb4.setText(dynamicQuestionDTO.getOptionDTOList().get(3).getOptionContent());
            }catch (Exception e){
                Log.e("Option error",e.getMessage());
            }
            if(dynamicQuestionDTO.getOptionDTOList().size()==5)
                cb5.setText(dynamicQuestionDTO.getOptionDTOList().get(4).getOptionContent());
            else
                cb5.setVisibility(View.GONE);

            startStopQuestion();
        }
    }

    public void startStopQuestion() throws IOException {
        if(run_ques)
            stopQuesTimer();
        startQuesTimer();

    }

    public void startStopHalt() throws IOException {
        if(run_halt)
            stopHaltTimer();
        startHaltTimer();

    }

    public void startQuesTimer(){

        countDownTimerQues = new CountDownTimer(timer_ques,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timer_ques=millisUntilFinished;
                updateQuestionTimer();
            }

            @Override
            public void onFinish() {
                if(dynamicQuestionDTO.getLast())
                {
                    try {
                        updateUI(null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    b_submit_contest.setVisibility(View.VISIBLE);
                }
                else {
                    timer_halt = 10000;
                    try {
                        startStopHalt();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ques.setVisibility(View.GONE);
                    halt.setVisibility(View.VISIBLE);
                    getWinnerQuestion();
                }
            }
        }.start();

        run_ques=true;
    }

    private void getWinnerQuestion() {
        iConnectAPI.getQuestionWinner(contestId,dynamicQuestionDTO.getQuestionId()).enqueue(new Callback<GetQuestionWinner>() {
            @Override
            public void onResponse(Call<GetQuestionWinner> call, Response<GetQuestionWinner> response) {
                if(null==response)
                    tv_winner.setText(response.body().getErrorMessage());
                else
                    tv_winner.setText(response.body().getResponse());
            }

            @Override
            public void onFailure(Call<GetQuestionWinner> call, Throwable t) {

            }
        });
    }

    public void startHaltTimer(){

        countDownTimerHalt = new CountDownTimer(timer_halt,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timer_halt=millisUntilFinished;
                updateHaltTimer();
            }

            @Override
            public void onFinish() {
                tv_halt_timer.setText("Admin is sleeping..."+(new String(Character.toChars(0x1F634))));
                updateCurrentQuestion();
            }
        }.start();

        run_halt=true;
    }

    private void updateCurrentQuestion() {
        final DatabaseReference currentQuestionRef = FirebaseDatabase.getInstance().getReference().child("Dynamic_Contest")
                .child(contestId).child("currentQuestion");
        currentQuestionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer currentQuestion  = dataSnapshot.getValue(Integer.class);
                if(dynamicQuestionDTO!=null && currentQuestion == dynamicQuestionDTO.getQuestionSequence()){
                    currentQuestionRef.setValue(currentQuestion + 1 );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void stopQuesTimer(){
        countDownTimerQues.cancel();

        run_ques=false;
    }

    public void stopHaltTimer(){
        countDownTimerHalt.cancel();

        run_halt=false;
    }

    public void updateQuestionTimer(){
        int minutes=(int)timer_ques/60000;
        int seconds=(int)timer_ques%60000/1000;

        String timeLeftText=""+minutes+":";
        if(seconds<10) timeLeftText+="0";
        timeLeftText+=seconds;

        tv_ques_timer.setText(timeLeftText);

    }

    public void updateHaltTimer(){
        int minutes=(int)timer_halt/60000;
        int seconds=(int)timer_halt%60000/1000;

        String timeLeftText=""+minutes+":";
        if(seconds<10) timeLeftText+="0";
        timeLeftText+=seconds;

        tv_halt_timer.setText(timeLeftText);
    }

    public void showSnackBar(String msg){
        Snackbar.make(findViewById(R.id.layoutDynamicContest),msg,Snackbar.LENGTH_LONG).show();
    }

}
