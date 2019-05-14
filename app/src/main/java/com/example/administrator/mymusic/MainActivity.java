package com.example.administrator.mymusic;

import android.Manifest;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView,play;
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private int minutes,seconds;
    private TextView tv_max,tv_min;
    private String time = "";
    private Message msg;
    private MyServiceMusic.MyBinder musicControl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        getPermission();
        loadBingPic();
        initMediaPlayer();
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setMax(getMusicLength());
                getMax();
                if (!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                    play.setImageResource(android.R.drawable.ic_media_pause);
                }else {
                    mediaPlayer.pause();
                    play.setImageResource(android.R.drawable.ic_media_play);
                }
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser == true){
                    mediaPlayer.seekTo(progress);
                    msg = new Message();
                    msg.obj = time;
                    msg.what = 0;
                    handler.sendMessage(msg);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    seekBar.setProgress(getCurrentProgress());
                    setMin();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }
    class MyConn implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicControl = (MyServiceMusic.MyBinder) service;
            seekBar.setMax(musicControl.getDuration());
            seekBar.setProgress(musicControl.getCurrenPostion());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case  0:
                    tv_min.setText((String) msg.obj);
                    break;
            }
        }
    };
    /**
     * 获取文件的时间长度  分钟：秒
     */
    public void getMax(){
        minutes = (int) (getMusicLength() % (1000 * 60 * 60)) / (1000 * 60);
        seconds = (int) ((getMusicLength() % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        String start = "";
        if (minutes<10){
            start = "0"+minutes;
        }else {
            start = minutes+"";
        }
        String end = "";
        if (seconds<10){
            end = "0"+seconds;
        }else {
            end = seconds+"";
        }
        tv_max.setText(start+":"+end);
    }

    public void setMin(){
        minutes = (int) (getCurrentProgress() % (1000 * 60 * 60)) / (1000 * 60);
        seconds = (int) ((getCurrentProgress() % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        String start = "";
        if (minutes<10){
            start = "0"+minutes;
        }else {
            start = minutes+"";
        }
        String end = "";
        if (seconds<10){
            end = "0"+seconds;
        }else {
            end = seconds+"";
        }
        time = start+":"+end;
        msg = new Message();
        msg.obj = time;
        msg.what = 0;
        handler.sendMessage(msg);
    }

    /**
     * 初始化控件
     */
    private void init() {
        imageView = findViewById(R.id.image);
        play = findViewById(R.id.play);
        seekBar = findViewById(R.id.seekBar);
        tv_max = findViewById(R.id.max);
        tv_min = findViewById(R.id.min);
    }

    /**
     * 初始化音乐播放器
     */
    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        AssetManager assetManager = MainActivity.this.getAssets();
        AssetFileDescriptor fileDesc;
        //File file = new File(Environment.getExternalStorageDirectory(),"aa.mp3");
        try {
            fileDesc = assetManager.openFd("aa.mp3");
            mediaPlayer.setDataSource(fileDesc.getFileDescriptor(),fileDesc.getStartOffset(),
                    fileDesc.getLength());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取权限
     */
    public void getPermission(){
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.INTERNET},1);
        }
    }

    /**
     * 获取必应每日一图
     */
    private void loadBingPic(){
        String requsetBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requsetBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences
                        (MainActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(MainActivity.this).load(bingPic).into(imageView);
                    }
                });
            }
        });
    }

    /**
     * 返回音乐总长度
     * @return -1 系统错误
     */
    public int getMusicLength(){
        if (mediaPlayer != null){
            return mediaPlayer.getDuration();
        }
        return -1;
    }

    /**
     * 获取当前进度
     * @return -1 系统错误
     */
    public int getCurrentProgress(){
        if (mediaPlayer != null){
            return mediaPlayer.getCurrentPosition();
        }
        return -1;
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (musicControl != null){
//            //handler.sendEmptyMessage(musicControl.getCurrenPostion());
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
