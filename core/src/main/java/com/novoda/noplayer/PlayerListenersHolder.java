package com.novoda.noplayer;

import com.novoda.noplayer.listeners.BitrateChangedListeners;
import com.novoda.noplayer.listeners.BufferStateListeners;
import com.novoda.noplayer.listeners.CompletionListeners;
import com.novoda.noplayer.listeners.ErrorListeners;
import com.novoda.noplayer.listeners.PreparedListeners;
import com.novoda.noplayer.listeners.StateChangedListeners;
import com.novoda.noplayer.listeners.VideoSizeChangedListeners;

public class PlayerListenersHolder implements PlayerListeners {

    private final ErrorListeners errorListeners;
    private final PreparedListeners preparedListeners;
    private final BufferStateListeners bufferStateListeners;
    private final CompletionListeners completionListeners;
    private final StateChangedListeners stateChangedListeners;
    private final BitrateChangedListeners bitrateChangedListeners;
    private final VideoSizeChangedListeners videoSizeChangedListeners;

    private final HeartbeatCallbacks<Player> heartbeatCallbacks;

    private Player.PreReleaseListener playerReleaseListener = Player.PreReleaseListener.NULL_IMPL;

    public PlayerListenersHolder() {
        errorListeners = new ErrorListeners();
        preparedListeners = new PreparedListeners();
        bufferStateListeners = new BufferStateListeners();
        completionListeners = new CompletionListeners();
        stateChangedListeners = new StateChangedListeners();
        bitrateChangedListeners = new BitrateChangedListeners();
        heartbeatCallbacks = new HeartbeatCallbacks<>();
        videoSizeChangedListeners = new VideoSizeChangedListeners();
    }

    @Override
    public void addErrorListener(Player.ErrorListener errorListener) {
        errorListeners.add(errorListener);
    }

    @Override
    public void removeErrorListener(Player.ErrorListener errorListener) {
        errorListeners.remove(errorListener);
    }

    @Override
    public void addPreparedListener(Player.PreparedListener preparedListener) {
        preparedListeners.add(preparedListener);
    }

    @Override
    public void removePreparedListener(Player.PreparedListener preparedListener) {
        preparedListeners.remove(preparedListener);
    }

    @Override
    public void addBufferStateListener(Player.BufferStateListener bufferStateListener) {
        bufferStateListeners.add(bufferStateListener);
    }

    @Override
    public void removeBufferStateListener(Player.BufferStateListener bufferStateListener) {
        bufferStateListeners.remove(bufferStateListener);
    }

    @Override
    public void addCompletionListener(Player.CompletionListener completionListener) {
        completionListeners.add(completionListener);
    }

    @Override
    public void removeCompletionListener(Player.CompletionListener completionListener) {
        completionListeners.remove(completionListener);
    }

    @Override
    public void addStateChangedListener(Player.StateChangedListener stateChangedListener) {
        stateChangedListeners.add(stateChangedListener);
    }

    @Override
    public void removeStateChangedListener(Player.StateChangedListener stateChangedListener) {
        stateChangedListeners.remove(stateChangedListener);
    }

    @Override
    public void addBitrateChangedListener(Player.BitrateChangedListener bitrateChangedListener) {
        bitrateChangedListeners.add(bitrateChangedListener);
    }

    @Override
    public void removeBitrateChangedListener(Player.BitrateChangedListener bitrateChangedListener) {
        bitrateChangedListeners.remove(bitrateChangedListener);
    }

    @Override
    public void setPreReleaseListener(Player.PreReleaseListener playerReleaseListener) {
        this.playerReleaseListener = playerReleaseListener;
    }

    @Override
    public void addHeartbeatCallback(Heart.Heartbeat.Callback<Player> callback) {
        heartbeatCallbacks.registerCallback(callback);
    }

    @Override
    public void removeHeartbeatCallback(Heart.Heartbeat.Callback<Player> callback) {
        heartbeatCallbacks.unregisterCallback(callback);
    }

    @Override
    public void addVideoSizeChangedListener(Player.VideoSizeChangedListener videoSizeChangedListener) {
        videoSizeChangedListeners.add(videoSizeChangedListener);
    }

    @Override
    public void removeVideoSizeChangedListener(Player.VideoSizeChangedListener videoSizeChangedListener) {
        videoSizeChangedListeners.remove(videoSizeChangedListener);
    }

    protected final ErrorListeners getErrorListeners() {
        return errorListeners;
    }

    protected final PreparedListeners getPreparedListeners() {
        return preparedListeners;
    }

    protected final BufferStateListeners getBufferStateListeners() {
        return bufferStateListeners;
    }

    protected final CompletionListeners getCompletionListeners() {
        return completionListeners;
    }

    protected final StateChangedListeners getStateChangedListeners() {
        return stateChangedListeners;
    }

    protected final BitrateChangedListeners getBitrateChangedListeners() {
        return bitrateChangedListeners;
    }

    protected final Player.PreReleaseListener getPlayerReleaseListener() {
        return playerReleaseListener;
    }

    protected final HeartbeatCallbacks<Player> getHeartbeatCallbacks() {
        return heartbeatCallbacks;
    }

    protected final VideoSizeChangedListeners getVideoSizeChangedListeners() {
        return videoSizeChangedListeners;
    }
}
