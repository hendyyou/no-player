package com.novoda.noplayer.exoplayer.mediasource;

import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;

import static com.google.android.exoplayer2.C.TRACK_TYPE_AUDIO;
import static com.google.android.exoplayer2.C.TRACK_TYPE_TEXT;
import static com.google.android.exoplayer2.C.TRACK_TYPE_VIDEO;

public class ExoPlayerTrackSelector {

    private final DefaultTrackSelector trackSelector;

    public ExoPlayerTrackSelector(DefaultTrackSelector trackSelector) {
        this.trackSelector = trackSelector;
    }

    TrackGroupArray getTextTrackGroups() {
        return trackInfo().getTrackGroups(TRACK_TYPE_VIDEO);
    }

    TrackGroupArray getAudioTrackGroups() {
        return trackInfo().getTrackGroups(TRACK_TYPE_AUDIO);
    }

    private MappingTrackSelector.MappedTrackInfo trackInfo() {
        MappingTrackSelector.MappedTrackInfo trackInfo = trackSelector.getCurrentMappedTrackInfo();

        if (trackInfo == null) {
            throw new NullPointerException("Track info is not available.");
        }
        return trackInfo;
    }

    void setAudioSelectionOverride(TrackGroupArray trackGroups, MappingTrackSelector.SelectionOverride selectionOverride) {
        trackSelector.setSelectionOverride(TRACK_TYPE_AUDIO, trackGroups, selectionOverride);
    }

    void setTextSelectionOverride(TrackGroupArray trackGroups, MappingTrackSelector.SelectionOverride selectionOverride) {
        trackSelector.setSelectionOverride(TRACK_TYPE_VIDEO, trackGroups, selectionOverride);
    }

    boolean supportsTrackSwitching(TrackGroupArray trackGroups, int groupIndex) {
        return trackGroups.get(groupIndex).length > 0
                && trackInfo().getAdaptiveSupport(TRACK_TYPE_AUDIO, groupIndex, false) != RendererCapabilities.ADAPTIVE_NOT_SUPPORTED;
    }
}
