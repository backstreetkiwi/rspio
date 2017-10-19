package de.zaunkoenigweg.rspio.audio;

import java.util.HashSet;
import java.util.Set;

public class AudioTrackSet implements AudioTrackRegistry {

    private Set<AudioTrack> tracks = new HashSet<>();

	@Override
    public void register(AudioTrack track) {
        synchronized (tracks) {
            this.tracks.add(track);
        }
    }
    
	@Override
    public void unregister(AudioTrack track) {
        synchronized (tracks) {
            this.tracks.remove(track);
        }
    }
	
    public void stopAll() {
        synchronized (tracks) {
        	this.tracks.stream().forEach(AudioTrack::stop);
        	this.tracks.clear();
        }
    }
}
