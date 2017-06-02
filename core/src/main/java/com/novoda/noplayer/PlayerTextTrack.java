package com.novoda.noplayer;

public class PlayerTextTrack {

    private final int groupIndex;
    private final int formatIndex;

    public PlayerTextTrack(int groupIndex, int formatIndex) {
        this.groupIndex = groupIndex;
        this.formatIndex = formatIndex;
    }

    public int groupIndex() {
        return groupIndex;
    }

    public int formatIndex() {
        return formatIndex;
    }
}
