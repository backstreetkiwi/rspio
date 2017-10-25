package de.zaunkoenigweg.rspio.audio;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    		List<AudioTrack> tracksToStop = this.tracks.stream().collect(Collectors.toList());
			tracksToStop.stream().forEach(AudioTrack::stop);
        }
    }
}
