package de.zaunkoenigweg.rspio.audio;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AudioTrackTemplate implements AudioTrack {

    private final static Log LOG = LogFactory.getLog(AudioTrackTemplate.class);

    protected Path audioFile;
    protected PlaybackIndicator playbackIndicator;
    protected AudioTrackRegistry runningTracks;
    
    protected PlaybackState state = PlaybackState.NOT_YET_STARTED;

    protected AudioTrackTemplate(Path audioFile, PlaybackIndicator playbackIndicator, AudioTrackRegistry runningTracks) {
        super();
        this.audioFile = audioFile;
        this.playbackIndicator = playbackIndicator;
        this.runningTracks = runningTracks;
    }
    
    protected abstract boolean initAndPlayImpl();
    protected abstract boolean pauseImpl();
    protected abstract boolean resumeImpl();
    protected abstract boolean stopAndTerminateImpl();
    
    private void autoPauseAfter(Duration duration) {
        new Thread(() -> {
            Instant start = Instant.now();
            Instant now = Instant.now();
            while(Duration.between(start, now).compareTo(duration)<0 && state==PlaybackState.PLAYING) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    LOG.trace(String.format("audio track '%s': autopause was interrupted.", audioFile));
                    return;
                }
                now = Instant.now();
            }
            if(state==PlaybackState.PLAYING) {
                LOG.trace(String.format("audio track '%s': autopause after %s.", audioFile, duration));
                pause();
            } else {
                LOG.trace(String.format("audio track '%s': autopause after %s interrupted after %s because track is no longer played back.", audioFile, duration, Duration.between(start, now)));
            }
        }).start();
    }

    @Override
    public PlaybackState play() {
        return play(null);
    }

    @Override
    public PlaybackState play(Duration duration) {
        synchronized (state) {
            switch (state) {
            case NOT_YET_STARTED:
                state = initAndPlayImpl() ? PlaybackState.PLAYING : PlaybackState.NOT_YET_STARTED;
                if(state==PlaybackState.PLAYING) {
                    LOG.trace(String.format("audio track '%s': playback started.", audioFile));
                    playbackIndicator.playing();
                } else {
                    LOG.trace(String.format("audio track '%s': playback could not be started.", audioFile));
                }
                if(duration!=null) {
                    autoPauseAfter(duration);
                }
                break;
            case PLAYING:
                LOG.trace(String.format("audio track '%s': unnecessary call of play(): track is currently being played.", audioFile));
                break;
            case PAUSED:
                state = resumeImpl() ? PlaybackState.PLAYING : PlaybackState.PAUSED;
                if(state==PlaybackState.PLAYING) {
                    LOG.trace(String.format("audio track '%s': playback resumed.", audioFile));
                    playbackIndicator.playing();
                } else {
                    LOG.trace(String.format("audio track '%s': playback could not be resumed.", audioFile));
                }
                if(duration!=null) {
                    autoPauseAfter(duration);
                }
                break;
            case TERMINATED:
                LOG.trace(String.format("audio track '%s': unnecessary call of play(): playback has been completed.", audioFile));
                break;
            }
            return state;
        }
    }

    @Override
    public PlaybackState pause() {
        synchronized (state) {
            switch (state) {
            case NOT_YET_STARTED:
                LOG.trace(String.format("audio track '%s': call of pause() has no effect: playback has not yet started.", audioFile));
                break; 
            case PLAYING:
                state = pauseImpl() ? PlaybackState.PAUSED : PlaybackState.PLAYING;
                if(state==PlaybackState.PAUSED) {
                	playbackIndicator.stopped();
                    LOG.trace(String.format("audio track '%s': paused.", audioFile));
                } else {
                    LOG.trace(String.format("audio track '%s': playback could not be paused.", audioFile));
                }
                break;
            case PAUSED:
                LOG.trace(String.format("audio track '%s': call of pause() has no effect: playback is paused.", audioFile));
                break;
            case TERMINATED:
                LOG.trace(String.format("audio track '%s': unnecessary call of pause(): playback has been completed.", audioFile));
                break;
            }
            return state;
        }
    }

    @Override
    public PlaybackState resume() {
        synchronized (state) {
            switch (state) {
            case NOT_YET_STARTED:
                LOG.trace(String.format("audio track '%s': call of resume() has no effect: playback has not yet started.", audioFile));
                break; 
            case PLAYING:
                LOG.trace(String.format("audio track '%s': call of resume() has no effect: playback is running.", audioFile));
                break;
            case PAUSED:
                state = resumeImpl() ? PlaybackState.PLAYING : PlaybackState.PAUSED;
                if(state==PlaybackState.PLAYING) {
                    LOG.trace(String.format("audio track '%s': resumed.", audioFile));
                    playbackIndicator.playing();
                } else {
                    LOG.trace(String.format("audio track '%s': playback could not be resumed.", audioFile));
                }
                break;
            case TERMINATED:
                LOG.trace(String.format("audio track '%s': unnecessary call of resume(): playback has been completed.", audioFile));
                break;
            }
            return state;
        }
    }

    @Override
    public PlaybackState stop() {
        synchronized (state) {
            switch (state) {
            case NOT_YET_STARTED:
                LOG.trace(String.format("audio track '%s': call of stop() has no effect: playback has not yet started.", audioFile));
                break; 
            case PLAYING:
            case PAUSED:
                if(stopAndTerminateImpl()) {
                    state = PlaybackState.TERMINATED;
                }
                if(state==PlaybackState.TERMINATED) {
                    LOG.trace(String.format("audio track '%s': playback stopped.", audioFile));
                    playbackIndicator.stopped();
                    runningTracks.unregister(this);
                } else {
                    LOG.trace(String.format("audio track '%s': playback could not be stopped.", audioFile));
                }
                break;
            case TERMINATED:
                LOG.trace(String.format("audio track '%s': unnecessary call of stop(): playback has been completed.", audioFile));
                break;
            }
            return state;
        }
    }

    @Override
    public String toString() {
        return this.audioFile.toString();
    }

    @Override
    public PlaybackState getState() {
        return state;
    }

}
