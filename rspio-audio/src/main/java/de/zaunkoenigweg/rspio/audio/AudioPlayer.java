package de.zaunkoenigweg.rspio.audio;

import java.nio.file.Path;

public interface AudioPlayer {

	public AudioTrack track(Path audioFile);
	
	public void stopAll(); 
	
}
