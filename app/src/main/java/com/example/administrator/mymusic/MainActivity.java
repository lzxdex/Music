package com.example.administrator.mymusic;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView,play;
    private MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.image);
        play = findViewById(R.id.play);
        getPermission();
        loadBingPic();
        initMediaPlayer();
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                }else {
                    mediaPlayer.pause();
                }

            }
        });
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
//        AssetManager assetManager = MainActivity.this.getAssets();
//        AssetFileDescriptor fileDesc;
        File file = new File(Environment.getExternalStorageDirectory(),"aa.mp3");
        try {
//            fileDesc = assetManager.openFd("aa.mp3");
//            mediaPlayer.setDataSource(fileDesc.getFileDescriptor(),fileDesc.getStartOffset(),
//                    fileDesc.getLength());

            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getPermission(){
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.INTERNET},1);
        }
    }
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
}
