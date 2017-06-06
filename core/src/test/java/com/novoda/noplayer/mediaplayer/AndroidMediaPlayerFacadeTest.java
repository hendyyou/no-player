package com.novoda.noplayer.mediaplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.SurfaceHolder;

import com.novoda.noplayer.PlayerAudioTrack;
import com.novoda.noplayer.SurfaceHolderRequester;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

import static com.novoda.notils.logger.simple.Log.setShowLogs;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

public class AndroidMediaPlayerFacadeTest {

    private static final int ANY_DURATION = 12000;
    private static final int ANY_POSITION = 60;
    private static final int ANY_WIDTH = 100;
    private static final int ANY_HEIGHT = 50;
    private static final int ANY_ERROR_WHAT = -1;
    private static final int ANY_ERROR_EXTRA = 404;
    private static final int TEN_PERCENT = 10;
    private static final int MINUS_ONE = -1;
    private static final int ZERO = 0;
    private static final int TEN_SECONDS_IN_MILLIS = 10000;

    private static final boolean SCREEN_ON = true;
    private static final boolean IS_IN_PLAYBACK_STATE = true;
    private static final boolean IS_NOT_IN_PLAYBACK_STATE = false;
    private static final boolean IS_PLAYING = true;
    private static final boolean IS_NOT_PLAYING = false;

    private static final Map<String, String> NO_HEADERS = null;
    private static final Uri ANY_URI = mock(Uri.class);
    private static final PlayerAudioTrack PLAYER_AUDIO_TRACK = new PlayerAudioTrack(0, 0, "id", "english", ".mp4", 1, 120);
    private static final List<PlayerAudioTrack> AUDIO_TRACKS = Collections.singletonList(PLAYER_AUDIO_TRACK);

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private Context context;
    @Mock
    private AndroidMediaPlayerAudioTrackSelector trackSelector;
    @Mock
    private PlaybackStateChecker playbackStateChecker;
    @Mock
    private MediaPlayer mediaPlayer;
    @Mock
    private AudioManager audioManager;
    @Mock
    private SurfaceHolderRequester surfaceHolderRequester;
    @Mock
    private SurfaceHolder surfaceHolder;
    @Mock
    private MediaPlayer.OnPreparedListener preparedListener;
    @Mock
    private MediaPlayer.OnVideoSizeChangedListener videoSizeChangedListener;
    @Mock
    private MediaPlayer.OnErrorListener errorListener;
    @Mock
    private MediaPlayer.OnCompletionListener completionListener;

    private AndroidMediaPlayerFacade facade;

    @Before
    public void setUp() {
        setShowLogs(false);

        facade = new AndroidMediaPlayerFacade(
                context,
                audioManager,
                trackSelector,
                playbackStateChecker
        ) {
            @Override
            protected MediaPlayer createMediaPlayer() {
                return mediaPlayer;
            }
        };

        given(playbackStateChecker.isInPlaybackState(eq(mediaPlayer), any(PlaybackStateChecker.PlaybackState.class))).willReturn(IS_IN_PLAYBACK_STATE);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                SurfaceHolderRequester.Callback callback = invocation.getArgument(0);
                callback.onSurfaceHolderReady(surfaceHolder);
                return null;
            }
        }).when(surfaceHolderRequester).requestSurfaceHolder(any(SurfaceHolderRequester.Callback.class));

        facade.setSurfaceHolderRequester(surfaceHolderRequester);
        facade.setOnPreparedListener(preparedListener);
        facade.setOnSizeChangedListener(videoSizeChangedListener);
        facade.setOnCompletionListener(completionListener);
        facade.setOnErrorListener(errorListener);
    }

    @Ignore("We should forward to listeners / make logs testable / throw exception.")
    @Test
    public void givenNoBoundSurfaceHolderRequester_whenPreparing_thenLogsNotAttachedWarning() {

    }

    @Test
    public void whenPreparing_thenRequestsAudioFocus() {
        givenMediaPlayerIsPrepared();

        verify(audioManager).requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    @Test
    public void whenPreparing_thenDoesNotReleaseMediaPlayer() {
        givenMediaPlayerIsPrepared();

        verify(mediaPlayer, never()).reset();
        verify(mediaPlayer, never()).release();
    }

    @Test
    public void whenPreparingMultipleTimes_thenReleasesMediaPlayer() {
        facade.prepareVideo(ANY_URI);
        facade.prepareVideo(ANY_URI);

        verify(mediaPlayer).reset();
        verify(mediaPlayer).release();
    }

    @Test
    public void whenPreparing_thenSetsDataSource() throws IOException {
        givenMediaPlayerIsPrepared();

        verify(mediaPlayer).setDataSource(context, ANY_URI, NO_HEADERS);
    }

    @Test
    public void whenPreparing_thenSetsDisplay() {
        givenMediaPlayerIsPrepared();

        verify(mediaPlayer).setDisplay(surfaceHolder);
    }

    @Test
    public void whenPreparing_thenSetsStreamMusicAudioStreamType() {
        givenMediaPlayerIsPrepared();

        verify(mediaPlayer).setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Test
    public void whenPreparing_thenSetsScreenOnWhilePlayerToTrue() {
        givenMediaPlayerIsPrepared();

        verify(mediaPlayer).setScreenOnWhilePlaying(SCREEN_ON);
    }

    @Test
    public void whenPreparing_thenPreparesMediaPlayerAsyncronously() {
        givenMediaPlayerIsPrepared();

        verify(mediaPlayer).prepareAsync();
    }

    @Ignore("We should forward to listeners / make logs testable / throw exception.")
    @Test
    public void givenException_whenPreparing_thenLogsCreationError() {

    }

    @Test
    public void givenBoundPreparedListener_andMediaPlayerIsPrepared_whenPrepared_thenForwardsOnPrepared() {
        facade.prepareVideo(ANY_URI);
        ArgumentCaptor<MediaPlayer.OnPreparedListener> argumentCaptor = ArgumentCaptor.forClass(MediaPlayer.OnPreparedListener.class);
        verify(mediaPlayer).setOnPreparedListener(argumentCaptor.capture());
        argumentCaptor.getValue().onPrepared(mediaPlayer);

        verify(preparedListener).onPrepared(mediaPlayer);
    }

    @Ignore("Should we throw when a listener is not attached?")
    @Test
    public void givenNoBoundPreparedListener_andMediaPlayerIsPrepared_whenPrepared_thenDoesNotForwardOnPrepared() {

    }

    @Test
    public void givenBoundVideoSizeChangedListener_andMediaPlayerOnPrepared_whenVideoSizeChanges_thenForwardsSizeChanges() {
        givenMediaPlayerIsPrepared();

        ArgumentCaptor<MediaPlayer.OnVideoSizeChangedListener> argumentCaptor = ArgumentCaptor.forClass(MediaPlayer.OnVideoSizeChangedListener.class);
        verify(mediaPlayer).setOnVideoSizeChangedListener(argumentCaptor.capture());
        argumentCaptor.getValue().onVideoSizeChanged(mediaPlayer, ANY_WIDTH, ANY_HEIGHT);

        verify(videoSizeChangedListener).onVideoSizeChanged(eq(mediaPlayer), eq(ANY_WIDTH), eq(ANY_HEIGHT));
    }

    @Ignore("Should we throw when a listener is not attached?")
    @Test
    public void givenNoBoundVideoSizeChangedListener_andMediaPlayerIsPrepared_whenVideoSizeChanged_thenDoesNotForwardOnSizeChanges() {

    }

    @Test
    public void givenBoundCompletionListener_andMediaPlayerIsPrepared_whenCompleted_thenForwardsCompleted() {
        givenMediaPlayerIsPrepared();

        ArgumentCaptor<MediaPlayer.OnCompletionListener> argumentCaptor = ArgumentCaptor.forClass(MediaPlayer.OnCompletionListener.class);
        verify(mediaPlayer).setOnCompletionListener(argumentCaptor.capture());
        argumentCaptor.getValue().onCompletion(mediaPlayer);

        verify(completionListener).onCompletion(mediaPlayer);
    }

    @Ignore("Should we throw when a listener is not attached?")
    @Test
    public void givenNoBoundCompletionListener_andMediaPlayerIsPrepared_whenCompleted_thenDoesNotForwardCompleted() {

    }

    @Test
    public void givenBoundErrorListener_andMediaPlayerIsPrepared_whenErroring_thenForwardsError() {
        givenMediaPlayerIsPrepared();

        ArgumentCaptor<MediaPlayer.OnErrorListener> argumentCaptor = ArgumentCaptor.forClass(MediaPlayer.OnErrorListener.class);
        verify(mediaPlayer).setOnErrorListener(argumentCaptor.capture());
        argumentCaptor.getValue().onError(mediaPlayer, ANY_ERROR_WHAT, ANY_ERROR_EXTRA);

        verify(errorListener).onError(mediaPlayer, ANY_ERROR_WHAT, ANY_ERROR_EXTRA);
    }

    @Ignore("Should we throw when a listener is not attached?")
    @Test
    public void givenNoBoundErrorListener_andMediaPlayerIsPrepared_whenErroring_thenDoesNotForwardError() {

    }

    @Test
    public void givenBoundBufferListener_andMediaPlayerIsPrepared_whenBuffering_thenBufferPercentageIsUpdated() {
        givenMediaPlayerIsPrepared();

        ArgumentCaptor<MediaPlayer.OnBufferingUpdateListener> argumentCaptor = ArgumentCaptor.forClass(MediaPlayer.OnBufferingUpdateListener.class);
        verify(mediaPlayer).setOnBufferingUpdateListener(argumentCaptor.capture());
        argumentCaptor.getValue().onBufferingUpdate(mediaPlayer, TEN_PERCENT);

        int bufferPercentage = facade.getBufferPercentage();
        assertThat(bufferPercentage).isEqualTo(TEN_PERCENT);
    }

    @Test
    public void givenMediaPlayerIsPrepared_whenReleasing_thenReleasesMediaPlayer() {
        givenMediaPlayerIsPrepared();

        facade.release();

        verify(mediaPlayer).reset();
        verify(mediaPlayer).release();
    }

    @Ignore("We should forward to listeners / make logs testable / throw exception.")
    @Test
    public void givenNoBoundSurfaceHolderRequester_whenStarting_thenLogsNotAttachedWarning() {

    }

    @Test
    public void givenMediaPlayerIsPrepared_whenStarting_thenSetsDisplay() {
        givenMediaPlayerIsPrepared();
        reset(mediaPlayer);

        facade.start();

        verify(mediaPlayer).setDisplay(surfaceHolder);
    }

    @Test
    public void givenMediaPlayerIsNotPrepared_whenStarting_thenNeverSetsDisplay() {
        facade.start();

        verify(mediaPlayer, never()).setDisplay(surfaceHolder);
    }

    @Test
    public void givenMediaPlayerIsPrepared_whenStarting_thenStartsMediaPlayer() {
        givenMediaPlayerIsPrepared();
        reset(mediaPlayer);

        facade.start();

        verify(mediaPlayer).start();
    }

    @Test
    public void givenMediaPlayerIsNotPrepared_whenStarting_thenNeverStartsMediaPlayer() {
        facade.start();

        verify(mediaPlayer, never()).start();
    }

    @Test
    public void givenMediaPlayerIsPlaying_whenPausing_thenPausesMediaPlayer() {
        givenMediaPlayerIsPrepared();
        given(playbackStateChecker.isPlaying(eq(mediaPlayer), any(PlaybackStateChecker.PlaybackState.class)))
                .willReturn(IS_PLAYING);

        facade.pause();

        verify(mediaPlayer).pause();
    }

    @Test
    public void givenMediaPlayerIsNotPlaying_whenPausing_thenDoesNotPausesMediaPlayer() {
        givenMediaPlayerIsPrepared();
        given(playbackStateChecker.isPlaying(eq(mediaPlayer), any(PlaybackStateChecker.PlaybackState.class)))
                .willReturn(IS_NOT_PLAYING);

        facade.pause();

        verify(mediaPlayer, never()).pause();
    }

    @Test
    public void givenMediaPlayerIsInPlaybackState_whenGettingDuration_thenReturnsDuration() {
        givenMediaPlayerIsPrepared();
        given(playbackStateChecker.isInPlaybackState(eq(mediaPlayer), any(PlaybackStateChecker.PlaybackState.class)))
                .willReturn(IS_IN_PLAYBACK_STATE);
        given(mediaPlayer.getDuration()).willReturn(ANY_DURATION);

        int duration = facade.getDuration();

        assertThat(duration).isEqualTo(ANY_DURATION);
    }

    @Test
    public void givenMediaPlayerIsNotInPlaybackState_whenGettingDuration_thenReturnsMinusOne() {
        given(playbackStateChecker.isInPlaybackState(eq(mediaPlayer), any(PlaybackStateChecker.PlaybackState.class)))
                .willReturn(IS_NOT_IN_PLAYBACK_STATE);

        int duration = facade.getDuration();

        assertThat(duration).isEqualTo(MINUS_ONE);
    }

    @Test
    public void givenMediaPlayerIsInPlaybackState_whenGettingPosition_thenReturnsPosition() {
        givenMediaPlayerIsPrepared();
        given(playbackStateChecker.isInPlaybackState(eq(mediaPlayer), any(PlaybackStateChecker.PlaybackState.class)))
                .willReturn(IS_IN_PLAYBACK_STATE);
        given(mediaPlayer.getCurrentPosition()).willReturn(ANY_POSITION);

        int currentPosition = facade.getCurrentPosition();

        assertThat(currentPosition).isEqualTo(ANY_POSITION);
    }

    @Test
    public void givenMediaPlayerIsNotInPlaybackState_whenGettingPosition_thenReturnsZero() {
        given(playbackStateChecker.isInPlaybackState(eq(mediaPlayer), any(PlaybackStateChecker.PlaybackState.class)))
                .willReturn(IS_NOT_IN_PLAYBACK_STATE);

        int currentPosition = facade.getCurrentPosition();

        assertThat(currentPosition).isEqualTo(ZERO);
    }

    @Test
    public void givenMediaPlayerIsInPlaybackState_whenSeeking_thenSeeksToPosition() {
        givenMediaPlayerIsPrepared();
        given(playbackStateChecker.isInPlaybackState(eq(mediaPlayer), any(PlaybackStateChecker.PlaybackState.class)))
                .willReturn(IS_IN_PLAYBACK_STATE);

        facade.seekTo(TEN_SECONDS_IN_MILLIS);

        verify(mediaPlayer).seekTo(TEN_SECONDS_IN_MILLIS);
    }

    @Test
    public void givenMediaPlayerIsNotInPlaybackState_whenSeeking_thenDoesNotSeek() {
        given(playbackStateChecker.isInPlaybackState(eq(mediaPlayer), any(PlaybackStateChecker.PlaybackState.class)))
                .willReturn(IS_NOT_IN_PLAYBACK_STATE);

        facade.seekTo(TEN_SECONDS_IN_MILLIS);

        verify(mediaPlayer, never()).seekTo(TEN_SECONDS_IN_MILLIS);
    }

    @Test
    public void whenCheckingIsPlaying_thenDelegatesToPlaystateChecker() {
        givenMediaPlayerIsPrepared();
        given(playbackStateChecker.isPlaying(eq(mediaPlayer), any(PlaybackStateChecker.PlaybackState.class)))
                .willReturn(IS_PLAYING);

        boolean playing = facade.isPlaying();

        assertThat(playing).isTrue();
    }

    @Test
    public void givenNoMediaPlayer_whenGettingBufferPercentage_thenReturnsZero() {
        int bufferPercentage = facade.getBufferPercentage();

        assertThat(bufferPercentage).isEqualTo(ZERO);
    }

    @Test
    public void givenMediaPlayerIsPrepared_whenStopping_thenStopsMediaPlayer() {
        givenMediaPlayerIsPrepared();

        facade.stop();

        verify(mediaPlayer).stop();
    }

    @Test
    public void givenNoMediaPlayer_whenStopping_thenDoesNotStop() {
        facade.stop();

        verify(mediaPlayer, never()).stop();
    }

    @Test
    public void whenGettingAudioTracks_thenDelegatesToTrackSelector() {
        givenMediaPlayerIsPrepared();
        given(trackSelector.getAudioTracks(mediaPlayer)).willReturn(AUDIO_TRACKS);

        List<PlayerAudioTrack> audioTracks = facade.getAudioTracks();

        assertThat(audioTracks).isEqualTo(AUDIO_TRACKS);
    }

    @Test
    public void whenSelectingAudioTrack_thenDelegatesToTrackSelector() {
        givenMediaPlayerIsPrepared();
        PlayerAudioTrack audioTrack = mock(PlayerAudioTrack.class);

        facade.selectAudioTrack(audioTrack);

        verify(trackSelector).selectAudioTrack(mediaPlayer, audioTrack);
    }

    @Test
    public void whenSettingOnSeekCompleteListener_thenSetsOnSeekCompleteListener() {
        givenMediaPlayerIsPrepared();

        MediaPlayer.OnSeekCompleteListener onSeekCompleteListener = mock(MediaPlayer.OnSeekCompleteListener.class);
        facade.setOnSeekCompleteListener(onSeekCompleteListener);

        verify(mediaPlayer).setOnSeekCompleteListener(onSeekCompleteListener);
    }

    private void givenMediaPlayerIsPrepared() {
        facade.prepareVideo(ANY_URI);
        ArgumentCaptor<MediaPlayer.OnPreparedListener> argumentCaptor = ArgumentCaptor.forClass(MediaPlayer.OnPreparedListener.class);
        verify(mediaPlayer).setOnPreparedListener(argumentCaptor.capture());
        argumentCaptor.getValue().onPrepared(mediaPlayer);
    }
}