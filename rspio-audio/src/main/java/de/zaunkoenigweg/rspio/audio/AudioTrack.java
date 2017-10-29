package de.zaunkoenigweg.rspio.audio;

import java.time.Duration;

public interface AudioTrack {
    public PlaybackState play();
    public PlaybackState play(Duration duration);
    public PlaybackState pause();
    public PlaybackState resume();
    public PlaybackState stop();
	public PlaybackState getState();
}
