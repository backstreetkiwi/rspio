package de.zaunkoenigweg.rspio.audio;

public abstract class AudioPlayerTemplate implements AudioPlayer {

	protected AudioTrackSet runningTracks;
	protected PlaybackIndicator playbackIndicator;
	
	protected AudioPlayerTemplate(PlaybackIndicator playbackIndicator) {
		this.runningTracks = new AudioTrackSet();
		this.playbackIndicator = playbackIndicator;
	}
	
	@Override
	public void stopAll() {
		runningTracks.stopAll();
	}

}
