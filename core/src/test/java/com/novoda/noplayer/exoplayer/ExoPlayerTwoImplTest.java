package com.novoda.noplayer.exoplayer;

import android.net.Uri;
import android.view.SurfaceHolder;

import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.novoda.noplayer.ContentType;
import com.novoda.noplayer.Heart;
import com.novoda.noplayer.LoadTimeout;
import com.novoda.noplayer.Player;
import com.novoda.noplayer.Player.StateChangedListener;
import com.novoda.noplayer.PlayerAudioTrack;
import com.novoda.noplayer.PlayerListenersHolder;
import com.novoda.noplayer.PlayerView;
import com.novoda.noplayer.SurfaceHolderRequester;
import com.novoda.noplayer.Timeout;
import com.novoda.noplayer.VideoDuration;
import com.novoda.noplayer.VideoPosition;
import com.novoda.noplayer.exoplayer.forwarder.ExoPlayerForwarder;
import com.novoda.noplayer.exoplayer.forwarder.ExoPlayerForwarderFactory;
import com.novoda.noplayer.exoplayer.mediasource.ExoPlayerAudioTrackSelector;
import com.novoda.noplayer.exoplayer.mediasource.MediaSourceFactory;
import com.novoda.noplayer.listeners.BitrateChangedListeners;
import com.novoda.noplayer.listeners.BufferStateListeners;
import com.novoda.noplayer.listeners.CompletionListeners;
import com.novoda.noplayer.listeners.ErrorListeners;
import com.novoda.noplayer.listeners.InfoListeners;
import com.novoda.noplayer.listeners.PreparedListeners;
import com.novoda.noplayer.listeners.StateChangedListeners;
import com.novoda.noplayer.listeners.VideoSizeChangedListeners;
import com.novoda.noplayer.player.PlayerInformation;
import com.novoda.noplayer.player.PlayerType;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(Enclosed.class)
public class ExoPlayerTwoImplTest {

    private static final long TWO_MINUTES_IN_MILLIS = 120000;
    private static final long TEN_MINUTES_IN_MILLIS = 600000;
    private static final long TEN_SECONDS = 10;

    private static final int WIDTH = 120;
    private static final int HEIGHT = 160;
    private static final int TEN_PERCENT = 10;
    private static final int ANY_ROTATION_DEGREES = 30;
    private static final int ANY_PIXEL_WIDTH_HEIGHT = 75;

    private static final boolean IS_PLAYING = true;
    private static final boolean PLAY_WHEN_READY = true;
    private static final boolean DO_NOT_PLAY_WHEN_READY = false;
    private static final boolean IS_BEATING = true;
    private static final boolean IS_NOT_BEATING = false;
    private static final boolean RESET_POSITION = true;
    private static final boolean DO_NOT_RESET_STATE = false;

    private static final ContentType ANY_CONTENT_TYPE = ContentType.DASH;
    private static final Timeout ANY_TIMEOUT = Timeout.fromSeconds(TEN_SECONDS);
    private static final Player.LoadTimeoutCallback ANY_LOAD_TIMEOUT_CALLBACK = new Player.LoadTimeoutCallback() {
        @Override
        public void onLoadTimeout() {

        }
    };
    private static final int INDEX_INTERNAL_VIDEO_SIZE_CHANGED_LISTENER = 0;

    public static class GivenVideoNotLoaded extends Base {

        @Test
        public void givenMediaSource_whenLoadingVideo_thenBindsHeart() {
            givenMediaSource();

            player.loadVideo(playerView, uri, ANY_CONTENT_TYPE);

            verify(listenersHolder).getHeartbeatCallbacks();
            verify(heart).bind(any(Heart.Heartbeat.class));
        }

        @Test
        public void givenVideoLoaded_whenVideoIsPrepared_thenCancelsTimeout() {
            givenMediaSource();

            player.loadVideo(playerView, uri, ANY_CONTENT_TYPE);

            ArgumentCaptor<Player.PreparedListener> argumentCaptor = ArgumentCaptor.forClass(Player.PreparedListener.class);

            verify(listenersHolder).addPreparedListener(argumentCaptor.capture());
            Player.PreparedListener preparedListener = argumentCaptor.getValue();
            preparedListener.onPrepared(player);

            verify(loadTimeout).cancel();
        }

        @Test
        public void givenVideoLoaded_whenVideoHasError_thenCancelsTimeout() {
            givenMediaSource();

            player.loadVideo(playerView, uri, ANY_CONTENT_TYPE);

            ArgumentCaptor<Player.ErrorListener> argumentCaptor = ArgumentCaptor.forClass(Player.ErrorListener.class);

            verify(listenersHolder).addErrorListener(argumentCaptor.capture());
            Player.ErrorListener errorListener = argumentCaptor.getValue();
            errorListener.onError(player, mock(Player.PlayerError.class));

            verify(loadTimeout).cancel();
        }

        @Test
        public void givenVideoLoaded_whenVideoSizeChanges_thenPlayerVideoWidthAndHeightMatches() {
            givenMediaSource();
            player.loadVideo(playerView, uri, ANY_CONTENT_TYPE);

            ArgumentCaptor<Player.VideoSizeChangedListener> argumentCaptor = ArgumentCaptor.forClass(Player.VideoSizeChangedListener.class);
            verify(listenersHolder, times(2)).addVideoSizeChangedListener(argumentCaptor.capture());

            Player.VideoSizeChangedListener videoSizeChangedListener = argumentCaptor.getAllValues().get(INDEX_INTERNAL_VIDEO_SIZE_CHANGED_LISTENER);
            videoSizeChangedListener.onVideoSizeChanged(WIDTH, HEIGHT, ANY_ROTATION_DEGREES, ANY_PIXEL_WIDTH_HEIGHT);

            int actualWidth = player.getVideoWidth();
            int actualHeight = player.getVideoHeight();

            assertThat(actualWidth).isEqualTo(WIDTH);
            assertThat(actualHeight).isEqualTo(HEIGHT);
        }

        @Test
        public void givenMediaSource_whenLoadingVideo_thenListenersHolderHasPlayerViewVideoSizeChangedListenerBound() {
            givenMediaSource();

            player.loadVideo(playerView, uri, ANY_CONTENT_TYPE);

            ArgumentCaptor<Player.VideoSizeChangedListener> argumentCaptor = ArgumentCaptor.forClass(Player.VideoSizeChangedListener.class);
            verify(listenersHolder, times(2)).addVideoSizeChangedListener(argumentCaptor.capture());
            Player.VideoSizeChangedListener videoSizeChangedListener = argumentCaptor.getAllValues().get(1);
            assertThat(videoSizeChangedListener).isSameAs(playerView.getVideoSizeChangedListener());
        }

        @Test
        public void whenResetting_thenNotifiesReleaseListenersOfPlayerPreRelease() {
            player.reset();

            verify(preReleaseListener).onPlayerPreRelease(player);
        }

        @Test
        public void whenResetting_thenNotifiesStateStateListenersThatVideoHasReleased() {
            player.reset();

            verify(stateChangedListeners).onVideoReleased();
        }

        @Test
        public void whenResetting_thenCancelsLoadTimeout() {
            player.reset();

            verify(loadTimeout).cancel();
        }

        @Test
        public void whenResetting_thenStopsBeatingHeart() {
            player.reset();

            verify(heart).stopBeatingHeart();
        }

        @Test
        public void whenResetting_thenReleasesUnderlyingPlayer() {
            player.reset();

            verify(internalExoPlayer, never()).release();
        }

        @Test
        public void whenLoadingVideo_thenAddsPlayerEventListener() {
            player.loadVideo(playerView, uri, ANY_CONTENT_TYPE);

            verify(internalExoPlayer).addListener(exoPlayerForwarder.exoPlayerEventListener());
        }

        @Test
        public void whenLoadingVideo_thenSetsVideoDebugListener() {
            player.loadVideo(playerView, uri, ANY_CONTENT_TYPE);

            verify(internalExoPlayer).setVideoDebugListener(exoPlayerForwarder.videoRendererEventListener());
        }

        @Test
        public void givenMediaSource_whenLoadingVideo_thenPreparesInternalExoPlayer() {
            MediaSource mediaSource = givenMediaSource();

            player.loadVideo(playerView, uri, ANY_CONTENT_TYPE);

            verify(internalExoPlayer).prepare(mediaSource, RESET_POSITION, DO_NOT_RESET_STATE);
        }

        @Test
        public void whenLoadingVideoWithTimeout_thenAddsPlayerEventListener() {
            player.loadVideoWithTimeout(playerView, uri, ANY_CONTENT_TYPE, ANY_TIMEOUT, ANY_LOAD_TIMEOUT_CALLBACK);

            verify(internalExoPlayer).addListener(exoPlayerForwarder.exoPlayerEventListener());
        }

        @Test
        public void givenMediaSource_whenLoadingVideoWithTimeout_thenSetsVideoDebugListener() {
            givenMediaSource();

            player.loadVideoWithTimeout(playerView, uri, ANY_CONTENT_TYPE, ANY_TIMEOUT, ANY_LOAD_TIMEOUT_CALLBACK);

            verify(internalExoPlayer).setVideoDebugListener(exoPlayerForwarder.videoRendererEventListener());
        }

        @Test
        public void givenMediaSource_whenLoadingVideoWithTimeout_thenPreparesInternalExoPlayer() {
            MediaSource mediaSource = givenMediaSource();

            player.loadVideoWithTimeout(playerView, uri, ANY_CONTENT_TYPE, ANY_TIMEOUT, ANY_LOAD_TIMEOUT_CALLBACK);

            verify(internalExoPlayer).prepare(mediaSource, RESET_POSITION, DO_NOT_RESET_STATE);
        }

        @Test
        public void whenGettingPlayerInformation_thenReturnsPlayerInformation() {
            PlayerInformation playerInformation = player.getPlayerInformation();

            assertThat(playerInformation.getPlayerType()).isEqualTo(PlayerType.EXO_PLAYER);
            assertThat(playerInformation.getVersion()).isEqualTo(ExoPlayerLibraryInfo.VERSION);
        }

        @Test
        public void givenMediaSource_whenLoadingVideo_thenAddsStateChangedListenerToListenersHolder() {
            givenMediaSource();

            player.loadVideo(playerView, uri, ANY_CONTENT_TYPE);

            verify(listenersHolder).addStateChangedListener(playerView.getStateChangedListener());
        }

        @Test
        public void givenMediaSource_whenLoadingVideo_thenAddsVideoSizeChangedListenerToListenersHolder() {
            givenMediaSource();

            player.loadVideo(playerView, uri, ANY_CONTENT_TYPE);

            verify(listenersHolder).addVideoSizeChangedListener(playerView.getVideoSizeChangedListener());
        }

        @Test
        public void whenSelectingAudioTrack_thenDelegatesToTrackSelector() {
            PlayerAudioTrack audioTrack = mock(PlayerAudioTrack.class);

            player.selectAudioTrack(audioTrack);

            verify(trackSelector).selectAudioTrack(audioTrack);
        }

        @Test
        public void whenGettingAudioTracks_thenDelegatesToTrackSelector() {
            player.getAudioTracks();

            verify(trackSelector).getAudioTracks();
        }

        @Test
        public void whenQueryingIsPlaying_thenReturnsFalse() {

            boolean isPlaying = player.isPlaying();

            assertThat(isPlaying).isFalse();
        }
    }

    public static class GivenVideoIsLoaded extends Base {

        @Override
        public void setUp() {
            super.setUp();
            givenPlayerIsLoaded();
        }

        private void givenPlayerIsLoaded() {
            givenMediaSource();
            player.loadVideo(playerView, uri, ANY_CONTENT_TYPE);
        }

        @Test
        public void whenResetting_thenReleasesUnderlyingPlayer() {
            player.reset();

            verify(internalExoPlayer).release();
        }

        @Test
        public void whenStartingPlayback_thenStartsBeatingHeart() {

            player.play();

            verify(heart).startBeatingHeart();
        }

        @Test
        public void whenPausing_thenSetsPlayWhenReadyToFalse() {
            player.pause();

            verify(internalExoPlayer).setPlayWhenReady(DO_NOT_PLAY_WHEN_READY);
        }

        @Test
        public void whenPausing_thenNotifiesStateListenersThatVideoIsPaused() {
            player.pause();

            verify(stateChangedListeners).onVideoPaused();
        }

        @Test
        public void givenHeartIsBeating_whenPausing_thenStopsBeatingHeart() {
            given(heart.isBeating()).willReturn(IS_BEATING);

            player.pause();

            verify(heart).stopBeatingHeart();
        }

        @Test
        public void givenHeartIsBeating_whenPausing_thenForcesHeartBeat() {
            given(heart.isBeating()).willReturn(IS_BEATING);

            player.pause();

            verify(heart).forceBeat();
        }

        @Test
        public void givenHeartIsNotBeating_whenPausing_thenDoesNotStopBeatingHeart() {
            given(heart.isBeating()).willReturn(IS_NOT_BEATING);

            player.pause();

            verify(heart, never()).stopBeatingHeart();
        }

        @Test
        public void givenHeartIsNotBeating_whenPausing_thenDoesNotForceHeartBeat() {
            given(heart.isBeating()).willReturn(IS_NOT_BEATING);

            player.pause();

            verify(heart, never()).forceBeat();
        }

        @Test
        public void whenSeeking_thenSeeksToPosition() {
            VideoPosition videoPosition = VideoPosition.fromMillis(TWO_MINUTES_IN_MILLIS);

            player.seekTo(videoPosition);

            verify(internalExoPlayer).seekTo(videoPosition.inMillis());
        }

        @Test
        public void whenStopping_thenStopsPlayer() {
            player.stop();

            verify(internalExoPlayer).stop();
        }

        @Test
        public void whenStartingPlayback_andSurfaceHolderIsReady_thenClearsAndSetsVideoSurfaceHolder() {
            player.play();

            InOrder inOrder = inOrder(internalExoPlayer);
            inOrder.verify(internalExoPlayer).clearVideoSurfaceHolder(surfaceHolder);
            inOrder.verify(internalExoPlayer).setVideoSurfaceHolder(surfaceHolder);
        }

        @Test
        public void whenStartingPlay_thenSetsPlayWhenReadyToTrue() {

            player.play();

            verify(internalExoPlayer).setPlayWhenReady(PLAY_WHEN_READY);
        }

        @Test
        public void whenStartingPlay_thenNotifiesStateListenersThatVideoIsPlaying() {

            player.play();

            verify(stateChangedListeners).onVideoPlaying();
        }

        @Test
        public void whenStartingPlayAtVideoPosition_thenSeeksToPosition() {

            player.play(VideoPosition.fromMillis(TWO_MINUTES_IN_MILLIS));

            verify(internalExoPlayer).seekTo(VideoPosition.fromMillis(TWO_MINUTES_IN_MILLIS).inMillis());
        }

        @Test
        public void whenStartingPlayAtVideoPosition_thenStartsBeatingHeart() {

            player.play(VideoPosition.fromMillis(TWO_MINUTES_IN_MILLIS));

            verify(heart).startBeatingHeart();
        }

        @Test
        public void whenStartingPlayAtVideoPosition_thenSetsPlayWhenReadyToTrue() {

            player.play(VideoPosition.fromMillis(TWO_MINUTES_IN_MILLIS));

            verify(internalExoPlayer).setPlayWhenReady(PLAY_WHEN_READY);
        }

        @Test
        public void whenStartingPlayAtVideoPosition_thenNotifiesStateListenersThatVideoIsPlaying() {

            player.play(VideoPosition.fromMillis(TWO_MINUTES_IN_MILLIS));

            verify(stateChangedListeners).onVideoPlaying();
        }

        @Test
        public void givenExoPlayerIsReadyToPlay_whenQueryingIsPlaying_thenReturnsTrue() {
            given(internalExoPlayer.getPlayWhenReady()).willReturn(IS_PLAYING);

            boolean isPlaying = player.isPlaying();

            assertThat(isPlaying).isTrue();
        }

        @Test
        public void whenGettingPlayheadPosition_thenReturnsCurrentPosition() {
            given(internalExoPlayer.getCurrentPosition()).willReturn(TWO_MINUTES_IN_MILLIS);

            VideoPosition playheadPosition = player.getPlayheadPosition();

            assertThat(playheadPosition).isEqualTo(VideoPosition.fromMillis(TWO_MINUTES_IN_MILLIS));
        }

        @Test
        public void whenGettingMediaDuration_thenReturnsDuration() {
            given(internalExoPlayer.getDuration()).willReturn(TEN_MINUTES_IN_MILLIS);

            VideoDuration videoDuration = player.getMediaDuration();

            assertThat(videoDuration).isEqualTo(VideoDuration.fromMillis(TEN_MINUTES_IN_MILLIS));
        }

        @Test
        public void whenGettingBufferPercentage_thenReturnsBufferPercentage() {
            given(internalExoPlayer.getBufferedPercentage()).willReturn(TEN_PERCENT);

            int bufferPercentage = player.getBufferPercentage();

            assertThat(bufferPercentage).isEqualTo(TEN_PERCENT);
        }
    }

    public abstract static class Base {

        @Rule
        public MockitoRule mockitoRule = MockitoJUnit.rule();

        @Mock
        SimpleExoPlayer internalExoPlayer;
        @Mock
        MediaSourceFactory mediaSourceFactory;
        @Mock
        ExoPlayerForwarder exoPlayerForwarder;
        @Mock
        LoadTimeout loadTimeout;
        @Mock
        ExoPlayerAudioTrackSelector trackSelector;
        @Mock
        Heart heart;
        @Mock
        Uri uri;
        @Mock
        PlayerView playerView;
        @Mock
        SurfaceHolderRequester surfaceHolderRequester;
        @Mock
        SurfaceHolder surfaceHolder;
        @Mock
        StateChangedListener stateChangeListener;
        @Mock
        Player.VideoSizeChangedListener videoSizeChangedListener;
        @Mock
        PlayerListenersHolder listenersHolder;
        @Mock
        ErrorListeners errorListeners;
        @Mock
        PreparedListeners preparedListeners;
        @Mock
        BufferStateListeners bufferStateListeners;
        @Mock
        CompletionListeners completionListeners;
        @Mock
        StateChangedListeners stateChangedListeners;
        @Mock
        InfoListeners infoListeners;
        @Mock
        VideoSizeChangedListeners videoSizeChangedListeners;
        @Mock
        BitrateChangedListeners bitrateChangedListeners;
        @Mock
        Player.PreReleaseListener preReleaseListener;

        Player player;

        @Before
        public void setUp() {
            given(playerView.getSurfaceHolderRequester()).willReturn(surfaceHolderRequester);
            given(playerView.getStateChangedListener()).willReturn(stateChangeListener);
            given(playerView.getVideoSizeChangedListener()).willReturn(videoSizeChangedListener);
            doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    SurfaceHolderRequester.Callback callback = invocation.getArgument(0);
                    callback.onSurfaceHolderReady(surfaceHolder);
                    return null;
                }
            }).when(surfaceHolderRequester).requestSurfaceHolder(any(SurfaceHolderRequester.Callback.class));

            given(listenersHolder.getErrorListeners()).willReturn(errorListeners);
            given(listenersHolder.getPreparedListeners()).willReturn(preparedListeners);
            given(listenersHolder.getBufferStateListeners()).willReturn(bufferStateListeners);
            given(listenersHolder.getCompletionListeners()).willReturn(completionListeners);
            given(listenersHolder.getStateChangedListeners()).willReturn(stateChangedListeners);
            given(listenersHolder.getInfoListeners()).willReturn(infoListeners);
            given(listenersHolder.getVideoSizeChangedListeners()).willReturn(videoSizeChangedListeners);
            given(listenersHolder.getBitrateChangedListeners()).willReturn(bitrateChangedListeners);
            given(listenersHolder.getPlayerReleaseListener()).willReturn(preReleaseListener);

            ExoPlayerForwarderFactory forwarderFactory = mock(ExoPlayerForwarderFactory.class);
            ExoPlayerCreator exoPlayerCreator = mock(ExoPlayerCreator.class);
            given(exoPlayerCreator.create()).willReturn(internalExoPlayer);
            player = new ExoPlayerTwoImpl(
                    exoPlayerCreator,
                    listenersHolder,
                    mediaSourceFactory,
                    loadTimeout,
                    trackSelector,
                    heart,
                    forwarderFactory
            );
            given(forwarderFactory.create(listenersHolder, player)).willReturn(exoPlayerForwarder);
        }

        MediaSource givenMediaSource() {
            MediaSource mediaSource = mock(MediaSource.class);
            given(
                    mediaSourceFactory.create(
                            ANY_CONTENT_TYPE,
                            uri,
                            exoPlayerForwarder.extractorMediaSourceListener(),
                            exoPlayerForwarder.mediaSourceEventListener()
                    )
            ).willReturn(mediaSource);

            return mediaSource;
        }
    }
}
