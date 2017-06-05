package com.novoda.noplayer.exoplayer;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceHolder;

import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.novoda.noplayer.ContentType;
import com.novoda.noplayer.Heart;
import com.novoda.noplayer.LoadTimeout;
import com.novoda.noplayer.Player;
import com.novoda.noplayer.PlayerAudioTrack;
import com.novoda.noplayer.PlayerListenersHolder;
import com.novoda.noplayer.PlayerState;
import com.novoda.noplayer.PlayerView;
import com.novoda.noplayer.SurfaceHolderRequester;
import com.novoda.noplayer.SystemClock;
import com.novoda.noplayer.Timeout;
import com.novoda.noplayer.VideoDuration;
import com.novoda.noplayer.VideoPosition;
import com.novoda.noplayer.exoplayer.forwarder.ExoPlayerForwarder;
import com.novoda.noplayer.exoplayer.forwarder.ExoPlayerForwarderFactory;
import com.novoda.noplayer.exoplayer.mediasource.ExoPlayerAudioTrackSelector;
import com.novoda.noplayer.exoplayer.mediasource.ExoPlayerTrackSelector;
import com.novoda.noplayer.exoplayer.mediasource.MediaSourceFactory;
import com.novoda.noplayer.player.PlayerInformation;

import java.util.List;

public class ExoPlayerTwoImpl implements Player {

    private final ExoPlayerFacade exoPlayerFacade;
    private final PlayerListenersHolder listenersHolder;
    private final Heart heart;
    private final LoadTimeout loadTimeout;
    private final ExoPlayerForwarderFactory forwarderFactory;

    private SurfaceHolderRequester surfaceHolderRequester;

    private int videoWidth;
    private int videoHeight;

    public static ExoPlayerTwoImpl newInstance(Context context) {
        DefaultDataSourceFactory defaultDataSourceFactory = new DefaultDataSourceFactory(context, "user-agent");
        Handler handler = new Handler(Looper.getMainLooper());
        MediaSourceFactory mediaSourceFactory = new MediaSourceFactory(defaultDataSourceFactory, handler);
        Heart heart = Heart.newInstance();

        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        ExoPlayerTrackSelector exoPlayerTrackSelector = new ExoPlayerTrackSelector(trackSelector);
        FixedTrackSelection.Factory trackSelectionFactory = new FixedTrackSelection.Factory();
        ExoPlayerAudioTrackSelector exoPlayerAudioTrackSelector = new ExoPlayerAudioTrackSelector(exoPlayerTrackSelector, trackSelectionFactory);

        ExoPlayerCreator exoPlayerCreator = new ExoPlayerCreator(context, trackSelector);

        LoadTimeout loadTimeout = new LoadTimeout(new SystemClock(), new Handler(Looper.getMainLooper()));
        PlayerListenersHolder listenersHolder = new PlayerListenersHolder();
        ExoPlayerForwarderFactory forwarderFactory = new ExoPlayerForwarderFactory();

        ExoPlayerFacade exoPlayerFacade = new ExoPlayerFacade(mediaSourceFactory, exoPlayerAudioTrackSelector, exoPlayerCreator);

        return new ExoPlayerTwoImpl(
                exoPlayerFacade,
                listenersHolder,
                loadTimeout,
                heart,
                forwarderFactory
        );
    }

    ExoPlayerTwoImpl(ExoPlayerFacade exoPlayerFacade,
                     PlayerListenersHolder listenersHolder,
                     LoadTimeout loadTimeoutParam,
                     Heart heart,
                     ExoPlayerForwarderFactory forwarderFactory) {
        this.exoPlayerFacade = exoPlayerFacade;
        this.listenersHolder = listenersHolder;
        this.loadTimeout = loadTimeoutParam;
        this.heart = heart;
        this.forwarderFactory = forwarderFactory;
    }

    @Override
    public boolean isPlaying() {
        return exoPlayerFacade.isPlaying();
    }

    @Override
    public int getVideoWidth() {
        return videoWidth;
    }

    @Override
    public int getVideoHeight() {
        return videoHeight;
    }

    @Override
    public VideoPosition getPlayheadPosition() {
        return exoPlayerFacade.getPlayheadPosition();
    }

    @Override
    public VideoDuration getMediaDuration() {
        return exoPlayerFacade.getMediaDuration();
    }

    @Override
    public int getBufferPercentage() {
        return exoPlayerFacade.getBufferPercentage();
    }

    @Override
    public void play(VideoPosition position) {
        seekTo(position);
        play();
    }

    @Override
    public void seekTo(VideoPosition position) {
        exoPlayerFacade.seekTo(position);
    }

    @Override
    public void play() {
        heart.startBeatingHeart();
        surfaceHolderRequester.requestSurfaceHolder(new SurfaceHolderRequester.Callback() {
            @Override
            public void onSurfaceHolderReady(SurfaceHolder surfaceHolder) {
                exoPlayerFacade.play(surfaceHolder);
                listenersHolder.getStateChangedListeners().onVideoPlaying();
            }
        });
    }

    @Override
    public void pause() {
        exoPlayerFacade.pause();
        listenersHolder.getStateChangedListeners().onVideoPaused();
        if (heart.isBeating()) {
            heart.stopBeatingHeart();
            heart.forceBeat();
        }
    }

    @Override
    public void reset() {
        listenersHolder.getPlayerReleaseListener().onPlayerPreRelease(this);
        loadTimeout.cancel();
        heart.stopBeatingHeart();
        exoPlayerFacade.reset();
        listenersHolder.getStateChangedListeners().onVideoReleased();
    }

    @Override
    public void stop() {
        exoPlayerFacade.stop();
    }

    @Override
    public void loadVideoWithTimeout(PlayerView playerView,
                                     Uri uri,
                                     ContentType contentType,
                                     Timeout timeout,
                                     LoadTimeoutCallback loadTimeoutCallback) {
        loadTimeout.start(timeout, loadTimeoutCallback);
        loadVideo(playerView, uri, contentType);
    }

    @Override
    public void loadVideo(PlayerView playerView, Uri uri, ContentType contentType) {
        attach(listenersHolder, playerView);
        ExoPlayerForwarder forwarder = forwarderFactory.create(listenersHolder, this);
        exoPlayerFacade.loadVideo(uri, contentType, forwarder);
    }

    private void attach(PlayerListenersHolder listenersHolder, PlayerView playerView) {
        heart.bind(new Heart.Heartbeat<>(listenersHolder.getHeartbeatCallbacks(), this));

        listenersHolder.addPreparedListener(new PreparedListener() {
            @Override
            public void onPrepared(PlayerState playerState) {
                loadTimeout.cancel();
            }
        });
        listenersHolder.addErrorListener(new ErrorListener() {
            @Override
            public void onError(Player player, PlayerError error) {
                loadTimeout.cancel();
            }
        });
        listenersHolder.addVideoSizeChangedListener(new VideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                videoWidth = width;
                videoHeight = height;
            }
        });

        listenersHolder.addStateChangedListener(playerView.getStateChangedListener());
        listenersHolder.addVideoSizeChangedListener(playerView.getVideoSizeChangedListener());

        surfaceHolderRequester = playerView.getSurfaceHolderRequester();
    }

    @Override
    public PlayerInformation getPlayerInformation() {
        return new ExoPlayerInformation();
    }

    @Override
    public void selectAudioTrack(PlayerAudioTrack audioTrack) {
        exoPlayerFacade.selectAudioTrack(audioTrack);
    }

    @Override
    public List<PlayerAudioTrack> getAudioTracks() {
        return exoPlayerFacade.getAudioTracks();
    }

    @Override
    public PlayerListenersHolder getListenersHolder() {
        return listenersHolder;
    }
}
