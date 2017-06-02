package com.novoda.noplayer;

import android.view.View;

import com.google.android.exoplayer2.ui.SubtitleView;

public interface PlayerView {

    View getContainerView();

    SurfaceHolderRequester getSurfaceHolderRequester();

    Player.VideoSizeChangedListener getVideoSizeChangedListener();

    Player.StateChangedListener getStateChangedListener();

    SubtitleView getSubtitleView();

    void showSubtitles();

    void hideSubtitles();
}
