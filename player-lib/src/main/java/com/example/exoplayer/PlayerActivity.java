/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.exoplayer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class PlayerActivity extends AppCompatActivity implements Player.EventListener {

    private SimpleExoPlayer player;
    private SimpleExoPlayerView playerView;
    private ImageButton backButton;
    private TextView videoTitleTextView;
    private ImageButton rotateButton;
    private SeekBar volumeSeekBar;
    private ImageButton volumeButton;
    LinearLayout volumeSeekBarLayout;

    private long playbackPosition;
    private int currentWindow;
    private boolean playWhenReady = true;

    private String videoPath;
    private String videoTitle;
    private boolean IS_PORTRAIT = true;
    private boolean IS_VOLUME_INVISIBLE = true;

    AudioManager audioManager;
    int currentVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        playerView = findViewById(R.id.video_view);

        //lấy đường dẫn và tên của video đã được bấm vào
        Intent videoIntent = getIntent();
        videoPath = videoIntent.getStringExtra("video path");
        videoTitle = videoIntent.getStringExtra("video name");

        videoTitleTextView = findViewById(R.id.video_title);
        videoTitleTextView.setText(videoTitle);
        videoTitleTextView.setSelected(true);

        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //nút để xoay màn hình
        rotateButton = findViewById(R.id.video_rotate_button);
        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //nếu màn hình đang là dọc thì chuyển thành ngang, và set trạng thái IS_POTRAIT = false và ngược lại
                if (IS_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    IS_PORTRAIT = false;
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    IS_PORTRAIT = true;
                }
            }
        });

        //layout chứa thanh kéo âm lượng, bao gồm thanh kéo,
        //biểu tượng âm lượng lớn, biểu tượng âm lượng nhỏ
        volumeSeekBarLayout = findViewById(R.id.volume_seekbar_group);
        //vào lần đầu tiên sẽ cần phải ẩn layout chứa thanh kéo
        volumeSeekBarLayout.setVisibility(View.INVISIBLE);

        volumeSeekBar = findViewById(R.id.video_volume_seek_bar);
        //set max cho thanh kéo chính là độ lớn max của âm lượng
        volumeSeekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        //gán vị trí tương ứng cho âm lượng hiện tại
        volumeSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        //hàm thay đổi mỗi lần thay đổi vị trí con trỏ của thanh kéo
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                checkVolumeStatus();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //khi toàn bộ controller ẩn thì layout thanh kéo âm lượng cũng phải ẩn theo
        playerView.setControllerVisibilityListener(new PlaybackControlView.VisibilityListener() {
            @Override
            public void onVisibilityChange(int visibility) {
                //xét xem nếu như controller ẩn thì ẩn luôn thanh kéo
                if (visibility == View.GONE) {
                    volumeSeekBarLayout.setVisibility(View.INVISIBLE);
                    IS_VOLUME_INVISIBLE = true;
                }
            }
        });

        volumeButton = findViewById(R.id.video_volume_button);
        checkVolumeStatus();
        //bấm vào nút này sẽ ẩn/hiện thanh kéo âm lượng
        volumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IS_VOLUME_INVISIBLE) {
                    volumeSeekBarLayout.setVisibility(View.VISIBLE);
                    IS_VOLUME_INVISIBLE = false;
                } else {
                    volumeSeekBarLayout.setVisibility(View.INVISIBLE);
                    IS_VOLUME_INVISIBLE = true;
                }
            }
        });

    }

    //check xem hiện tại âm lượng đang ở mức nào, từ đó thay đổi icon tương ứng
    public void checkVolumeStatus() {
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
            volumeButton.setImageResource(R.drawable.ic_mute);
        } else if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                == audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
            volumeButton.setImageResource(R.drawable.ic_speaker);
        } else {
            volumeButton.setImageResource(R.drawable.ic_speaker_medium);
        }
    }

    //từ android SDK 23 trở đi, ta có thể khởi tạo ngay từ hàm onStart()
    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    //ngược lại, từ SDK 23 trở xuống hệ thống buộc chúng ta phải đợi chương trình vào hàm onResume mới được phép khởi tạo
    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }

    //trước SDK 23, không có gì chắc chắn việc chương trình sẽ nhảy vào hàm onStop() cả,
    //vậy nên ta phải giải phóng trình chơi video ngay tại hàm onPause()
    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    //sau SDK 23, chắc chắn sẽ phải nhảy vào hàm onStop() nên ta có thể giải phóng ở đây
    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    //hàm khởi tạo trình chơi video exo pLayer
    private void initializePlayer() {
        if (player == null) {
            //sử dụng những đối mặc định:
            // DefaultRenderersFactory: kiểu render mặc định, giúp đồng bộ hóa sub, thời gian, hình ảnh, âm thanh của video
            // DefaultTrackSelector: Quản lý danh sách thứ tự chơi các video
            // DefaultLoadControl: quản lý việc buffer video (dành cho xem video online)

            player = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this),
                    new DefaultTrackSelector(), new DefaultLoadControl());
            playerView.setPlayer(player);
            player.setPlayWhenReady(playWhenReady);
            player.seekTo(currentWindow, playbackPosition);
        }
        //truyền đường dẫn video cần chơi vào, tạo thành nguồn cho trình chơi
        MediaSource mediaSource = buildMediaSource(Uri.parse(videoPath));
        player.prepare(mediaSource, true, false);
        player.addListener(this);
    }

    //giải phóng trình chơi video
    private void releasePlayer() {
        if (player != null) {
            //lưu lại vị trí của video đang chơi dở  trong trường hợp trình chơi video bị tạm thời dừng
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }

    //hàm biến đổi đường dẫn URI thành đối tượng MediaSource để ExoPlayer có thể phát được
    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource.Factory(new DefaultDataSourceFactory(PlayerActivity.this, "ua"))
                .createMediaSource(uri);
    }

    // gắn cờ để báo hệ thống biết layout cần được toàn màn hình và ẩn thanh navigation bar
    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }


    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        //sau khi chơi video xong thì nhảy lại vị trí ban đầu và đang pause
        if (playbackState == Player.STATE_ENDED) {
            player.setPlayWhenReady(false);
            player.seekTo(0);

        }

        //nếu video đã chơi xong hoặc đang pause thì phải để cho màn hình được phép tự tắt
        if (playbackState == Player.STATE_ENDED || (playbackState == Player.STATE_READY && playWhenReady == false)) {
            playerView.setKeepScreenOn(false);
            //ngược lại thì phải giữ màn hình sáng
        } else {
            playerView.setKeepScreenOn(true);
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
    }

    @Override
    public void onPositionDiscontinuity(int reason) {
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
    }

    @Override
    public void onSeekProcessed() {
        //nếu vị trí con trỏ của thanh kéo video khác 0, thì khi kéo con trỏ video sẽ tự động chạy mà không cần nhấn nút play
        if (player.getCurrentPosition() != 0) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume + 1, 0);
            volumeSeekBar.setProgress(volumeSeekBar.getProgress() + 1);
            checkVolumeStatus();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume - 1, 0);
            volumeSeekBar.setProgress(volumeSeekBar.getProgress() - 1);
            checkVolumeStatus();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
