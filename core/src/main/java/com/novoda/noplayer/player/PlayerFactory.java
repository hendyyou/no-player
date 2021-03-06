package com.novoda.noplayer.player;

import android.content.Context;

import com.novoda.noplayer.Player;
import com.novoda.noplayer.drm.DownloadedModularDrm;
import com.novoda.noplayer.drm.DrmHandler;
import com.novoda.noplayer.drm.DrmType;
import com.novoda.noplayer.drm.StreamingModularDrm;
import com.novoda.noplayer.drm.provision.ProvisionExecutor;
import com.novoda.noplayer.exoplayer.DrmSessionCreator;
import com.novoda.noplayer.exoplayer.ExoPlayerFacade;
import com.novoda.noplayer.exoplayer.ExoPlayerImpl;
import com.novoda.noplayer.exoplayer.ProvisioningModularDrmCallback;
import com.novoda.noplayer.exoplayer.RendererFactory;
import com.novoda.noplayer.mediaplayer.AndroidMediaPlayerFacade;
import com.novoda.noplayer.mediaplayer.AndroidMediaPlayerImpl;

public class PlayerFactory {

    private final Context context;
    private final PrioritizedPlayerTypes prioritizedPlayerTypes;
    private final ExoPlayerCreator exoPlayerCreator;
    private final MediaPlayerCreator mediaPlayerCreator;

    public PlayerFactory(Context context, PrioritizedPlayerTypes prioritizedPlayerTypes) {
        this(context, prioritizedPlayerTypes, new ExoPlayerCreator(), new MediaPlayerCreator());
    }

    PlayerFactory(Context context, PrioritizedPlayerTypes prioritizedPlayerTypes, ExoPlayerCreator exoPlayerCreator, MediaPlayerCreator mediaPlayerCreator) {
        this.context = context;
        this.prioritizedPlayerTypes = prioritizedPlayerTypes;
        this.exoPlayerCreator = exoPlayerCreator;
        this.mediaPlayerCreator = mediaPlayerCreator;
    }

    public Player create() {
        return create(DrmType.NONE, DrmHandler.NO_DRM);
    }

    public Player create(DrmType drmType, DrmHandler drmHandler) {
        for (PlayerType player : prioritizedPlayerTypes) {
            if (player.supports(drmType)) {
                return createPlayerForType(player, drmType, drmHandler);
            }
        }
        throw UnableToCreatePlayerException.unhandledDrmType(drmType);
    }

    private Player createPlayerForType(PlayerType playerType, DrmType drmType, DrmHandler drmHandler) {
        switch (playerType) {
            case MEDIA_PLAYER:
                return mediaPlayerCreator.createMediaPlayer(context);
            case EXO_PLAYER:
                DrmSessionCreator drmSessionCreator = createDrmSessionCreatorFor(drmType, drmHandler);
                return exoPlayerCreator.createExoPlayer(context, drmSessionCreator);
            default:
                throw UnableToCreatePlayerException.unhandledPlayerType(playerType);
        }
    }

    private DrmSessionCreator createDrmSessionCreatorFor(DrmType drmType, DrmHandler drmHandler) {
        switch (drmType) {
            case NONE:
            case WIDEVINE_CLASSIC:
                return new NoDrmSessionCreator();
            case WIDEVINE_MODULAR_STREAM:
                ProvisionExecutor provisionExecutor = ProvisionExecutor.newInstance();
                ProvisioningModularDrmCallback mediaDrmCallback = new ProvisioningModularDrmCallback((StreamingModularDrm) drmHandler, provisionExecutor);
                return new StreamingDrmSessionCreator(mediaDrmCallback);
            case WIDEVINE_MODULAR_DOWNLOAD:
                return new LocalDrmSessionCreator((DownloadedModularDrm) drmHandler);
            default:
                throw UnableToCreatePlayerException.noDrmHandlerFor(drmType);
        }
    }

    static class UnableToCreatePlayerException extends RuntimeException {

        static UnableToCreatePlayerException unhandledDrmType(DrmType drmType) {
            return new UnableToCreatePlayerException("Unhandled DrmType: " + drmType);
        }

        static UnableToCreatePlayerException noDrmHandlerFor(DrmType drmType) {
            return new UnableToCreatePlayerException("No DrmHandler for DrmType: " + drmType);
        }

        static UnableToCreatePlayerException unhandledPlayerType(PlayerType playerType) {
            return new UnableToCreatePlayerException("Unhandled player type: " + playerType.name());
        }

        UnableToCreatePlayerException(String reason) {
            super(reason);
        }
    }

    static class ExoPlayerCreator {

        Player createExoPlayer(Context context, DrmSessionCreator drmSessionCreator) {
            RendererFactory rendererFactory = createRendererFactory(context, drmSessionCreator);
            ExoPlayerFacade exoPlayerFacade = new ExoPlayerFacade(rendererFactory);
            return new ExoPlayerImpl(exoPlayerFacade);
        }

        private RendererFactory createRendererFactory(Context context, DrmSessionCreator drmSessionCreator) {
            return new RendererFactory(context, drmSessionCreator);
        }
    }

    static class MediaPlayerCreator {

        Player createMediaPlayer(Context context) {
            AndroidMediaPlayerFacade androidMediaPlayer = new AndroidMediaPlayerFacade(context);
            return new AndroidMediaPlayerImpl(androidMediaPlayer);
        }
    }
}
