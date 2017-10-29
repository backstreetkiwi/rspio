package de.zaunkoenigweg.rspio.audio.dummy;

import java.nio.file.Path;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.zaunkoenigweg.rspio.audio.AudioTrackRegistry;
import de.zaunkoenigweg.rspio.audio.AudioTrackTemplate;
import de.zaunkoenigweg.rspio.audio.PlaybackIndicator;
import de.zaunkoenigweg.rspio.audio.PlaybackState;

public class DummyAudioTrack extends AudioTrackTemplate {

    private final static Log LOG = LogFactory.getLog(DummyAudioTrack.class);
    private final static long FRAME_LENGTH = 100;
    
    private long remainingFrames = 0;
    
    public DummyAudioTrack(Path audioFile, AudioTrackRegistry runningTracks) {
        this(audioFile, PlaybackIndicator.ignoring(), runningTracks);
    }
    
    public DummyAudioTrack(Path audioFile, PlaybackIndicator playbackIndicator, AudioTrackRegistry runningTracks) {
        super(audioFile, playbackIndicator, runningTracks);
        int duration = 180 + (new Random()).nextInt(121);
        remainingFrames = 1000 * duration / FRAME_LENGTH;
    }
    
    @Override
    protected boolean initAndPlayImpl() {
        try {
            new Thread(() -> {
                try {
                    runningTracks.register(this);
                	LOG.info(String.format("START of '%s'", this.audioFile.toAbsolutePath().toString()));
                    while(state!=PlaybackState.TERMINATED && remainingFrames>0) {
                    	if(state==PlaybackState.PLAYING) {
                    		remainingFrames--;
                    	}
                    	Thread.sleep(FRAME_LENGTH);
                    }
                    synchronized (state) {
	                    if(state!=PlaybackState.TERMINATED) {
	                    	LOG.info(String.format("STOP (completed) of '%s'", this.audioFile.toAbsolutePath().toString()));
                            state = PlaybackState.TERMINATED;
                            playbackIndicator.stopped();
                            runningTracks.unregister(this);
	                    }
                    }
                } catch (InterruptedException e) {
                    LOG.error(String.format("Playback of audio track '%s': Thread interrupted.", audioFile), e);
                }
            }).start();
            return true;
        } catch (Exception e) {
            LOG.error(String.format("Playback of audio track '%s' could not be started.", audioFile), e);
            return false;
        }
    }
    
    @Override
    protected boolean pauseImpl() {
    	LOG.info(String.format("PAUSE of '%s'", this.audioFile.toAbsolutePath().toString()));
    	return true;
    }
    
    @Override
    protected boolean resumeImpl() {
    	LOG.info(String.format("RESUME of '%s'", this.audioFile.toAbsolutePath().toString()));
    	return true;
    }
    
    @Override
    protected boolean stopAndTerminateImpl() {
    	LOG.info(String.format("STOP (terminated) of '%s'", this.audioFile.toAbsolutePath().toString()));
    	return true;
    }

}
