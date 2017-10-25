package de.zaunkoenigweg.rspio.audio.omx;

import java.nio.file.Path;

import de.zaunkoenigweg.rspio.audio.AudioPlayerTemplate;
import de.zaunkoenigweg.rspio.audio.AudioTrack;
import de.zaunkoenigweg.rspio.audio.PlaybackIndicator;

public class OmxAudioPlayer extends AudioPlayerTemplate {

	public OmxAudioPlayer() {
		super(PlaybackIndicator.ignoring());
	}
	
	public OmxAudioPlayer(PlaybackIndicator playbackIndicator) {
		super(playbackIndicator);
	}
	
	@Override
	public AudioTrack track(Path audioFile) {
		return new OmxAudioTrack(audioFile, playbackIndicator, runningTracks);
	}

}
