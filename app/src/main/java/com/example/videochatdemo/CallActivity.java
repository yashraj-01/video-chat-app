package com.example.videochatdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class CallActivity extends AppCompatActivity {

    private static final String TAG = "AGORA";
    RelativeLayout remote_video_view_container;
    FrameLayout local_video_view_container;
    ImageView btn_end_call, btn_switch_camera, btn_mute;

    private RtcEngine mRtcEngine;
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("agora","Join channel success, uid: " + (uid & 0xFFFFFFFFL));
                }
            });
        }

        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("agora","First remote video decoded, uid: " + (uid & 0xFFFFFFFFL));
                    setupRemoteVideo(uid);
                }
            });
        }

        @Override
        public void onUserOffline(final int uid, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("agora","User offline, uid: " + (uid & 0xFFFFFFFFL));
                    onRemoteUserLeft();
                }
            });
        }
    };

    private VideoCanvas localVideoCanvas;
    private VideoCanvas remoteVideoCanvas;

    private String TOKEN, CHANNEL_NAME;

    private boolean mMuted = false;

    // Initialize the RtcEngine object.
    private void initializeEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void setupLocalVideo() {

        // Enable the video module.
        mRtcEngine.enableVideo();

        // Create a SurfaceView object.
        SurfaceView mLocalView;

        mLocalView = RtcEngine.CreateRendererView(getBaseContext());
        mLocalView.setZOrderMediaOverlay(true);
        local_video_view_container.addView(mLocalView);
        // Set the local video view.
        localVideoCanvas = new VideoCanvas(mLocalView, VideoCanvas.RENDER_MODE_HIDDEN, 0);
        mRtcEngine.setupLocalVideo(localVideoCanvas);
    }

    private void joinChannel() {

        // Join a channel with a token.
        mRtcEngine.joinChannel(TOKEN, CHANNEL_NAME, "Extra Optional Data", 0);
    }

    private void setupVideoConfig() {
        // In simple use cases, we only need to enable video capturing
        // and rendering once at the initialization step.
        // Note: audio recording and playing is enabled by default.
        mRtcEngine.enableVideo();

        // Please go to this page for detailed explanation
        // https://docs.agora.io/en/Video/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_rtc_engine.html#af5f4de754e2c1f493096641c5c5c1d8f
        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }

    private void initEngineAndJoinChannel() {
        // This is our usual steps for joining
        // a channel and starting a call.
        initializeEngine();
        setupVideoConfig();
        setupLocalVideo();
        joinChannel();
    }

    private void setupRemoteVideo(int uid){
        // Create a SurfaceView object.
        SurfaceView mRemoteView;

        mRemoteView = RtcEngine.CreateRendererView(getBaseContext());
        remote_video_view_container.addView(mRemoteView);
        // Set the remote video view.
        remoteVideoCanvas = new VideoCanvas(mRemoteView, VideoCanvas.RENDER_MODE_HIDDEN, uid);
        mRtcEngine.setupRemoteVideo(remoteVideoCanvas);
    }

    private void onRemoteUserLeft(){

    }

    private void leaveChannel(){
        // Leave the current channel.
        mRtcEngine.leaveChannel();
    }

    public void onEndCallClicked(View view){
        endCall();
    }

    private void endCall(){
        removeFromParent(localVideoCanvas);
        localVideoCanvas = null;
        removeFromParent(remoteVideoCanvas);
        remoteVideoCanvas = null;
        leaveChannel();
    }

    private ViewGroup removeFromParent(VideoCanvas canvas) {
        if (canvas != null) {
            ViewParent parent = canvas.view.getParent();
            if (parent != null) {
                ViewGroup group = (ViewGroup) parent;
                group.removeView(canvas.view);
                return group;
            }
        }
        return null;
    }

    private void switchView(VideoCanvas canvas) {
        ViewGroup parent = removeFromParent(canvas);
        if (parent == local_video_view_container) {
            if (canvas.view instanceof SurfaceView) {
                ((SurfaceView) canvas.view).setZOrderMediaOverlay(false);
            }
            remote_video_view_container.addView(canvas.view);
        } else if (parent == remote_video_view_container) {
            if (canvas.view instanceof SurfaceView) {
                ((SurfaceView) canvas.view).setZOrderMediaOverlay(true);
            }
            local_video_view_container.addView(canvas.view);
        }
    }

    public void onLocalContainerClicked(View view){
        switchView(localVideoCanvas);
        switchView(remoteVideoCanvas);
    }

    public void onSwitchCameraClicked(View view){
        // Switches between front and rear cameras.
        mRtcEngine.switchCamera();
    }

    public void onLocalAudioMuteClicked(View view){
        mMuted = !mMuted;
        // Stops/Resumes sending the local audio stream.
        mRtcEngine.muteLocalAudioStream(mMuted);
        int res = mMuted ? R.drawable.ic_baseline_mic_24 : R.drawable.ic_baseline_mic_off_24;
        btn_mute.setImageResource(res);
    }

    public void findViews(){
        remote_video_view_container = findViewById(R.id.remote_video_view_container);
        local_video_view_container = findViewById(R.id.local_video_view_container);
        btn_end_call = findViewById(R.id.btn_end_call);
        btn_mute = findViewById(R.id.btn_mute);
        btn_switch_camera = findViewById(R.id.btn_switch_camera);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        findViews();

        Intent intent = getIntent();
        TOKEN = intent.getStringExtra("token");
        CHANNEL_NAME = intent.getStringExtra("channelName");

        initEngineAndJoinChannel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        leaveChannel();
        RtcEngine.destroy();

    }
}