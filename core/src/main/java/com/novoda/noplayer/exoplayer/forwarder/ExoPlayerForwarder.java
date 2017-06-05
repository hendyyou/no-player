package com.novoda.noplayer.exoplayer.forwarder;

public class ExoPlayerForwarder {

    private final EventListener exoPlayerEventListener;
    private final MediaSourceEventListener mediaSourceEventListener;
    private final ExoPlayerVideoRendererEventListener videoRendererEventListener;
    private final ExoPlayerExtractorMediaSourceListener extractorMediaSourceListener;

    ExoPlayerForwarder(EventListener exoPlayerEventListener,
                       MediaSourceEventListener mediaSourceEventListener,
                       ExoPlayerVideoRendererEventListener videoRendererEventListener,
                       ExoPlayerExtractorMediaSourceListener extractorMediaSourceListener) {
        this.exoPlayerEventListener = exoPlayerEventListener;
        this.mediaSourceEventListener = mediaSourceEventListener;
        this.videoRendererEventListener = videoRendererEventListener;
        this.extractorMediaSourceListener = extractorMediaSourceListener;
    }

    public EventListener exoPlayerEventListener() {
        return exoPlayerEventListener;
    }

    public MediaSourceEventListener mediaSourceEventListener() {
        return mediaSourceEventListener;
    }

    public ExoPlayerVideoRendererEventListener videoRendererEventListener() {
        return videoRendererEventListener;
    }

    public ExoPlayerExtractorMediaSourceListener extractorMediaSourceListener() {
        return extractorMediaSourceListener;
    }
}
