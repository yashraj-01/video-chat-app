package com.example.videochatdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class CallActivity extends AppCompatActivity {

    private static final String LOG_TAG = "LOG_TAG";
    private static final String YOUR_TOKEN = "00615b44db1bf054b1f8fc3d4450967cdf3IACwvKEyDbLOR0tK1oWKAyoYc8Y6Qn3NWKucbISDvo+KCbhZIhAAAAAAEAAQz9mbfDhnXwEAAQB8OGdf";

    ImageView start_call, end_call;

    private RtcEngine mRtcEngine;

    private IRtcEngineEventHandler mRtcEventHandler;

    private void onRemoteUserLeft() {
        removeVideo(R.id.remote_video_view_container);
    }

    private void removeVideo(int containerID) {
        FrameLayout videoContainer = findViewById(containerID);
        videoContainer.removeAllViews();
    }

    private void initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine();
        setupLocalVideo();
        joinChannel();
    }

    private void joinChannel() {
        mRtcEngine.joinChannel(YOUR_TOKEN, "demoChannel1", "Extra Optional Data", 0);
    }

    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    private void setupLocalVideo() {
        FrameLayout local_video_view_container = findViewById(R.id.local_video_view_container);
        SurfaceView videoSurface = RtcEngine.CreateRendererView(getBaseContext());
        videoSurface.setZOrderMediaOverlay(true);
        local_video_view_container.addView(videoSurface);
        mRtcEngine.setupLocalVideo(new VideoCanvas(videoSurface, VideoCanvas.RENDER_MODE_FIT, 0));
    }

    private void setupRemoteVideoStream(int uid) {
        FrameLayout remote_video_view_container = findViewById(R.id.remote_video_view_container);
        SurfaceView videoSurface = RtcEngine.CreateRendererView(getBaseContext());
        remote_video_view_container.addView(videoSurface);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(videoSurface, VideoCanvas.RENDER_MODE_FIT, uid));
        videoSurface.setTag(uid);
        mRtcEngine.setRemoteSubscribeFallbackOption(io.agora.rtc.Constants.STREAM_FALLBACK_OPTION_AUDIO_ONLY);
    }

    private void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
            Log.i("RTCEngine","Connected");
        } catch (Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));

            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
        setupSession();
    }

    private void setupSession() {
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);

        mRtcEngine.enableVideo();

        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(VideoEncoderConfiguration.VD_1280x720, VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }

    public void startCallClicked(View view){

    }

    public void endCallClicked(View view){
        leaveChannel();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        start_call = findViewById(R.id.start_call);
        end_call = findViewById(R.id.end_call);

        mRtcEventHandler = new IRtcEngineEventHandler() {
            @Override
            public void onJoinChannelSuccess(String s, final int i, int i1) {
                super.onJoinChannelSuccess(s, i, i1);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("agora","Join channel success, uid: " + (i & 0xFFFFFFFFL));
                    }
                });
            }

            @Override
            public void onFirstRemoteVideoDecoded(final int i, int i1, int i2, int i3) {
                super.onFirstRemoteVideoDecoded(i, i1, i2, i3);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("agora","First remote video decoded, uid: " + (i & 0xFFFFFFFFL));
                        setupRemoteVideoStream(i);
                    }
                });
            }

            @Override
            public void onUserOffline(final int i, int i1) {
                super.onUserOffline(i, i1);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("agora","User offline, uid: " + (i & 0xFFFFFFFFL));
                        onRemoteUserLeft();
                    }
                });
            }
        };

        initAgoraEngineAndJoinChannel();
    }
}