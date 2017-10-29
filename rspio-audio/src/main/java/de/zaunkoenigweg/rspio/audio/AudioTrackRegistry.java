package de.zaunkoenigweg.rspio.audio;

public interface AudioTrackRegistry {

	public void register(AudioTrack track);
	public void unregister(AudioTrack track);
}
