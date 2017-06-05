package com.novoda.noplayer.exoplayer.forwarder;

import com.novoda.noplayer.Player;
import com.novoda.noplayer.PlayerListenersHolder;
import com.novoda.noplayer.listeners.ErrorListeners;
import com.novoda.noplayer.listeners.InfoListeners;

public class ExoPlayerForwarderFactory {

    public ExoPlayerForwarder create(PlayerListenersHolder listenersHolder, Player player) {
        EventListener exoPlayerEventListener = new EventListener();
        MediaSourceEventListener mediaSourceEventListener = new MediaSourceEventListener();
        ExoPlayerVideoRendererEventListener videoRendererEventListener = new ExoPlayerVideoRendererEventListener();
        ExoPlayerExtractorMediaSourceListener extractorMediaSourceListener = new ExoPlayerExtractorMediaSourceListener();

        exoPlayerEventListener.add(new OnPrepareForwarder(listenersHolder.getPreparedListeners(), player));
        exoPlayerEventListener.add(new OnCompletionForwarder(listenersHolder.getCompletionListeners()));

        ErrorListeners errorListeners = listenersHolder.getErrorListeners();
        exoPlayerEventListener.add(new PlayerOnErrorForwarder(player, errorListeners));
        extractorMediaSourceListener.add(new MediaSourceOnErrorForwarder(player, errorListeners));
        exoPlayerEventListener.add(new BufferStateForwarder(listenersHolder.getBufferStateListeners()));
        videoRendererEventListener.add(new VideoSizeChangedForwarder(listenersHolder.getVideoSizeChangedListeners()));
        mediaSourceEventListener.add(new BitrateForwarder(listenersHolder.getBitrateChangedListeners()));

        InfoListeners infoListeners = listenersHolder.getInfoListeners();
        exoPlayerEventListener.add(new EventInfoForwarder(infoListeners));
        mediaSourceEventListener.add(new MediaSourceInfoForwarder(infoListeners));
        videoRendererEventListener.add(new VideoRendererInfoForwarder(infoListeners));
        extractorMediaSourceListener.add(new ExtractorInfoForwarder(infoListeners));

        return new ExoPlayerForwarder(exoPlayerEventListener, mediaSourceEventListener, videoRendererEventListener, extractorMediaSourceListener);
    }
}
