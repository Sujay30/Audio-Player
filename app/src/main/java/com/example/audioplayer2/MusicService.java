package com.example.audioplayer2;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.audioplayer.R;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static com.example.audioplayer2.ApplicationClass.ACTION_NEXT;
import static com.example.audioplayer2.ApplicationClass.ACTION_PLAY;
import static com.example.audioplayer2.ApplicationClass.ACTION_PREVIOUS;
import static com.example.audioplayer2.ApplicationClass.CHANNEL_ID1;
import static com.example.audioplayer2.ApplicationClass.CHANNEL_ID2;
import static com.example.audioplayer2.PlayerActivity.ListSongs;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    IBinder mBinder = new MyBinder();
    MediaPlayer mediaPlayer;
    ArrayList<MusicFiles>musicFiles = new ArrayList<>();
    Uri uri;
    int position = -1;
    ActionPlaying actionPlaying;
    MediaSessionCompat mediaSessionCompat;


    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat =  new MediaSessionCompat(getBaseContext(),"My Audio");


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Bind", "Method");
        return mBinder;
    }



    public class MyBinder extends Binder{
        MusicService getService(){
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition",-1);
        String actionName = intent.getStringExtra("ActionName");
        if(myPosition != -1)
        {
            playMedia(myPosition);
        }
        if(actionName != null)
        {
            switch(actionName){
                case "playPause":
                    Toast.makeText(this,
                            "PlayPause",Toast.LENGTH_SHORT).show();
                    if(actionPlaying != null)
                    {
                        Log.e("Inside","Action");
                        actionPlaying.playPauseBtnClicked();
                    }
                    break;
                case "next":
                    Toast.makeText(this,
                            "Next",Toast.LENGTH_SHORT).show();
                    if(actionPlaying != null)
                    {
                        Log.e("Inside","Action");
                        actionPlaying.nextBtnClicked();
                    }
                    break;
                case "previous":
                    Toast.makeText(this,
                            "Previous",Toast.LENGTH_SHORT).show();
                    if(actionPlaying != null)
                    {
                        Log.e("Inside","Action");
                        actionPlaying.previousBtnClicked();
                    }
                    break;
            }
        }
        playMedia(position);
        return START_STICKY;
    }

    private void playMedia(int StartPosition) {
        musicFiles = ListSongs;
        position = StartPosition;
        if(mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            if (musicFiles != null)
            {
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        }
        else
        {
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }
    void start() {
        mediaPlayer.start();
    }
    boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }
    void stop(){
        mediaPlayer.stop();
    }
    void release(){
        mediaPlayer.release();
    }
    int getDuration(){
       return mediaPlayer.getDuration();
    }
    void seekTo(int position){
        mediaPlayer.seekTo(position);
    }
    int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }
    void createMediaPlayer(int positionInner){
        position = positionInner;
        uri = Uri.parse(musicFiles.get(position).getPath());
        mediaPlayer = MediaPlayer.create(getBaseContext(),uri);
    }
    void pause() {
        mediaPlayer.pause();
    }
    void OnCompleted(){
        mediaPlayer.setOnCompletionListener(this);
    }
    @Override
    public void onCompletion(MediaPlayer mp) {
        if (actionPlaying != null)
        {
            actionPlaying.nextBtnClicked();
            if(mediaPlayer != null)
            {
                createMediaPlayer(position);
                mediaPlayer.start();
                OnCompleted();
            }
        }

    }
    void setCallBack(ActionPlaying actionPlaying){
        this.actionPlaying = actionPlaying;
    }

    void showNotification(int playPauseBtn) {
        Intent intent = new Intent(this, PlayerActivity.class);
        PendingIntent contentIntent = PendingIntent.getBroadcast(this, 0
                , intent, 0);
        Intent prevIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent
                .getBroadcast(this, 0
                        , prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent
                .getBroadcast(this, 0
                        , pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent nextIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_NEXT);

        PendingIntent nextPending = PendingIntent
                .getBroadcast(this, 0
                        , nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        byte[] picture = null;
        picture = getAlbumArt(musicFiles.get(position).getPath());
        Bitmap thumb = null;
        if (picture != null) {
            thumb = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        } else {
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.song_background);
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID2)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .addAction((R.drawable.ic_skip_previous), "Previous", prevPending)
                .addAction(playPauseBtn, "Pause", pausePending)
                .addAction((R.drawable.ic_skip_next), "Previous", nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
        startForeground(2,notification);
    }
    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;

    }

    }



