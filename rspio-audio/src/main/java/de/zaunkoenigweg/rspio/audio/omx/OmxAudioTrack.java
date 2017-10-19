package de.zaunkoenigweg.rspio.audio.omx;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.zaunkoenigweg.rspio.audio.AudioTrack;
import de.zaunkoenigweg.rspio.audio.AudioTrackRegistry;
import de.zaunkoenigweg.rspio.audio.PlaybackIndicator;
import de.zaunkoenigweg.rspio.audio.PlaybackState;

public class OmxAudioTrack implements AudioTrack {

    private final static Log LOG = LogFactory.getLog(OmxAudioTrack.class);

    Path audioFile;
    PlaybackIndicator playbackIndicator;
    AudioTrackRegistry runningTracks;
    Process omxProcess;
    PrintWriter omxInputWriter;
    PlaybackState state = PlaybackState.NOT_YET_STARTED;

    public OmxAudioTrack(Path audioFile, AudioTrackRegistry runningTracks) {
        this(audioFile, PlaybackIndicator.ignoring(), runningTracks);
    }
    
    public OmxAudioTrack(Path audioFile, PlaybackIndicator playbackIndicator, AudioTrackRegistry runningTracks) {
        super();
        this.audioFile = audioFile;
        this.playbackIndicator = playbackIndicator;
        this.runningTracks = runningTracks;
    }
    
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
                state = startOmxPlayer() ? PlaybackState.PLAYING : PlaybackState.NOT_YET_STARTED;
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
                state = send('p') ? PlaybackState.PLAYING : PlaybackState.PAUSED;
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
                state = send('p') ? PlaybackState.PAUSED : PlaybackState.PLAYING;
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
                state = send('p') ? PlaybackState.PLAYING : PlaybackState.PAUSED;
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
                if(send('q')) {
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

    private boolean startOmxPlayer() {
        try {
            omxProcess = new ProcessBuilder("bash", "-c", "omxplayer " + StringUtils.replace(this.audioFile.toString(), " ", "\\ ")).start();
            omxInputWriter = new PrintWriter(omxProcess.getOutputStream());
            new Thread(() -> {
                try {
                    runningTracks.register(this);
                    omxProcess.waitFor();
                    if(state!=PlaybackState.TERMINATED) {
                        synchronized (state) {
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
    
    private boolean send(char signal) {
        try {
            omxInputWriter.print(signal);
            omxInputWriter.flush();
            return true;
        } catch (Exception e) {
            LOG.error(String.format("Error sending signal '%s' to process of track '%s'.", signal, audioFile), e);
            return false;
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
