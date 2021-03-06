package com.novoda.noplayer;

public class PlayerAudioTrack {

    private final String trackId;
    private final String language;
    private final String mimeType;
    private final int numberOfChannels;
    private final int frequency;

    public PlayerAudioTrack(String trackId, String language, String mimeType, int numberOfChannels, int frequency) {
        this.trackId = trackId;
        this.language = language;
        this.mimeType = mimeType;
        this.numberOfChannels = numberOfChannels;
        this.frequency = frequency;
    }

    public String trackId() {
        return trackId;
    }

    public String language() {
        return language;
    }

    public String mimeType() {
        return mimeType;
    }

    public int numberOfChannels() {
        return numberOfChannels;
    }

    public int frequency() {
        return frequency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PlayerAudioTrack that = (PlayerAudioTrack) o;

        if (numberOfChannels != that.numberOfChannels) {
            return false;
        }
        if (frequency != that.frequency) {
            return false;
        }
        if (trackId != null ? !trackId.equals(that.trackId) : that.trackId != null) {
            return false;
        }
        if (language != null ? !language.equals(that.language) : that.language != null) {
            return false;
        }
        return mimeType != null ? mimeType.equals(that.mimeType) : that.mimeType == null;

    }

    @Override
    public int hashCode() {
        int result = trackId != null ? trackId.hashCode() : 0;
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + (mimeType != null ? mimeType.hashCode() : 0);
        result = 31 * result + numberOfChannels;
        result = 31 * result + frequency;
        return result;
    }
}
