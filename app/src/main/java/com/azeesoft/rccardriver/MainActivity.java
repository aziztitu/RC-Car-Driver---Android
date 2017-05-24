package com.azeesoft.rccardriver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.rtp.RtpStream;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspServer;
import net.majorkernelpanic.streaming.video.VideoQuality;

import com.azeesoft.rccardriver.tools.screen.ScreenManager;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    enum ACTIVITY_INTENT_EXTRAS {A, B}

    public final static String ACTIVITY_INTENT_ACTION = "com.azeesoft.rccardriver.action.ACTIVITY_INTENT_ACTION";
    public final static String ACTIVITY_INTENT_EXTRA_NAME = "com.azeesoft.rccardriver.extra.ACTIVITY_INTENT_EXTRA_NAME";

    private SurfaceView streamSurfaceView;
    private Session libStreamSession;
    private RtspServer rtspServer;

    private final BroadcastReceiver mainActivityBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action.equals(ACTIVITY_INTENT_ACTION)) {
                    ACTIVITY_INTENT_EXTRAS action_extra = (ACTIVITY_INTENT_EXTRAS) intent.getSerializableExtra(ACTIVITY_INTENT_EXTRA_NAME);
                    switch (action_extra) {
                        //TODO: Implement Actions
                        case A:

                            break;
                        case B:

                            break;
                        default:

                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        startService(new Intent(this, MainService.class));

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTIVITY_INTENT_ACTION);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.registerReceiver(mainActivityBroadcastReceiver, filter);

        streamSurfaceView = (SurfaceView) findViewById(R.id.streamSurfaceView);

        libStreamSession = SessionBuilder.getInstance()
                .setCallback(new Session.Callback() {
                    @Override
                    public void onBitrateUpdate(long bitrate) {

                    }

                    @Override
                    public void onSessionError(int reason, int streamType, Exception e) {

                    }

                    @Override
                    public void onPreviewStarted() {

                    }

                    @Override
                    public void onSessionConfigured() {
                        libStreamSession.start();
                    }

                    @Override
                    public void onSessionStarted() {

                    }

                    @Override
                    public void onSessionStopped() {
                        libStreamSession.stop();
                    }
                })
                .setSurfaceView(streamSurfaceView)
                .setCamera(Camera.CameraInfo.CAMERA_FACING_FRONT)
                .setPreviewOrientation(0)
                .setContext(this)
                .setAudioEncoder(SessionBuilder.AUDIO_AAC)
                .setAudioQuality(new AudioQuality(16000, 32000))
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                .setVideoQuality(new VideoQuality(320, 240, 15, 300000))
                .build();

     /*   libStreamSession.startPreview();
        try {
            libStreamSession.syncStart();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();

//        connectToDevice();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1010: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.unregisterReceiver(mainActivityBroadcastReceiver);
        super.onDestroy();
    }


    public void startStreamServer(View v){
        rtspServer = new RtspServer();
        rtspServer.addCallbackListener(new RtspServer.CallbackListener() {
            @Override
            public void onError(RtspServer server, Exception e, int error) {

            }

            @Override
            public void onMessage(RtspServer server, int message) {
                if (message == RtspServer.MESSAGE_STREAMING_STARTED){
//                    Toast.makeText(MainActivity.this, "Streaming Started", Toast.LENGTH_LONG).show();
                }
            }
        });
        rtspServer.start();

//        startService(new Intent(this, RtspServer.class));

        Toast.makeText(this, "Stream Session started!",Toast.LENGTH_LONG).show();
    }

    public void stopStreamServer(View v){
        if(libStreamSession.isStreaming()){
            libStreamSession.stop();
        }

        rtspServer.stop();
    }


    public void screenOff(View v) {
        Log.d("Screen", "Turning off...");
        ScreenManager.turnScreenOff(this);
    }

    public void showHideSettings(View v){
        ScrollView settingsScrollView = (ScrollView) findViewById(R.id.settingsScrollView);
        if(settingsScrollView.getVisibility()!=View.GONE) {
            settingsScrollView.setVisibility(View.GONE);
        }else{
            settingsScrollView.setVisibility(View.VISIBLE);
        }
    }

    public void sendSimpleIntentToService(MainService.SERVICE_INTENT_EXTRAS param){
        Intent intent = new Intent(this, MainService.class);
        intent.setAction(MainService.SERVICE_INTENT_ACTION);
        intent.putExtra(MainService.SERVICE_INTENT_EXTRA_NAME, param);
        MainService.executeAction(intent);
    }

    public void resetConnections(View v) {
        sendSimpleIntentToService(MainService.SERVICE_INTENT_EXTRAS.RESET_CONNECTIONS);
    }

    public void connectToRCCar(View v) {
        sendSimpleIntentToService(MainService.SERVICE_INTENT_EXTRAS.CONNECT_TO_RC_CAR);
    }

    public void startWifiServer(View v){
        sendSimpleIntentToService(MainService.SERVICE_INTENT_EXTRAS.START_WIFI_SERVER);
    }
}
