package de.zaunkoenigweg.rspio.audio.dummy;

import java.nio.file.Path;

import de.zaunkoenigweg.rspio.audio.AudioPlayerTemplate;
import de.zaunkoenigweg.rspio.audio.AudioTrack;
import de.zaunkoenigweg.rspio.audio.PlaybackIndicator;

public class DummyAudioPlayer extends AudioPlayerTemplate {

	public DummyAudioPlayer() {
		super(PlaybackIndicator.ignoring());
	}
	
	public DummyAudioPlayer(PlaybackIndicator playbackIndicator) {
		super(playbackIndicator);
	}
	
	@Override
	public AudioTrack track(Path audioFile) {
		return new DummyAudioTrack(audioFile, playbackIndicator, runningTracks);
	}

}
