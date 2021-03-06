package de.zaunkoenigweg.rspio.core.omx;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AudioTrack {

    private final static Log LOG = LogFactory.getLog(AudioTrack.class);

    Path audioFile;
    AudioPlayer audioPlayer;
    Process omxProcess;
    PrintWriter omxInputWriter;
    State state = State.NOT_YET_STARTED;

    public AudioTrack(Path audioFile, AudioPlayer audioPlayer) {
        super();
        this.audioFile = audioFile;
        this.audioPlayer = audioPlayer;
    }
    
    private void autoPauseAfter(Duration duration) {
        new Thread(() -> {
            Instant start = Instant.now();
            Instant now = Instant.now();
            while(Duration.between(start, now).compareTo(duration)<0 && state==State.PLAYING) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    LOG.trace(String.format("audio track '%s': autopause was interrupted.", audioFile));
                    return;
                }
                now = Instant.now();
            }
            if(state==State.PLAYING) {
                LOG.trace(String.format("audio track '%s': autopause after %s.", audioFile, duration));
                pause();
            } else {
                LOG.trace(String.format("audio track '%s': autopause after %s interrupted after %s because track is no longer played back.", audioFile, duration, Duration.between(start, now)));
            }
        }).start();
    }

    public State play() {
        return play(null);
    }

    public State play(Duration duration) {
        synchronized (state) {
            switch (state) {
            case NOT_YET_STARTED:
                state = startOmxPlayer() ? State.PLAYING : State.NOT_YET_STARTED;
                if(state==State.PLAYING) {
                    LOG.trace(String.format("audio track '%s': playback started.", audioFile));
                    audioPlayer.playing();
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
                state = send('p') ? State.PLAYING : State.PAUSED;
                if(state==State.PLAYING) {
                    LOG.trace(String.format("audio track '%s': playback resumed.", audioFile));
                    audioPlayer.playing();
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

    public State pause() {
        synchronized (state) {
            switch (state) {
            case NOT_YET_STARTED:
                LOG.trace(String.format("audio track '%s': call of pause() has no effect: playback has not yet started.", audioFile));
                break; 
            case PLAYING:
                state = send('p') ? State.PAUSED : State.PLAYING;
                if(state==State.PAUSED) {
                    audioPlayer.stopped();
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

    public State resume() {
        synchronized (state) {
            switch (state) {
            case NOT_YET_STARTED:
                LOG.trace(String.format("audio track '%s': call of resume() has no effect: playback has not yet started.", audioFile));
                break; 
            case PLAYING:
                LOG.trace(String.format("audio track '%s': call of resume() has no effect: playback is running.", audioFile));
                break;
            case PAUSED:
                state = send('p') ? State.PLAYING : State.PAUSED;
                if(state==State.PLAYING) {
                    LOG.trace(String.format("audio track '%s': resumed.", audioFile));
                    audioPlayer.playing();
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

    public State stop() {
        synchronized (state) {
            switch (state) {
            case NOT_YET_STARTED:
                LOG.trace(String.format("audio track '%s': call of stop() has no effect: playback has not yet started.", audioFile));
                break; 
            case PLAYING:
            case PAUSED:
                if(send('q')) {
                    state = State.TERMINATED;
                }
                if(state==State.TERMINATED) {
                    LOG.trace(String.format("audio track '%s': playback stopped.", audioFile));
                    audioPlayer.stopped();
                    audioPlayer.unregister(this);
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
                    audioPlayer.register(this);
                    omxProcess.waitFor();
                    if(state!=State.TERMINATED) {
                        synchronized (state) {
                            state = State.TERMINATED;
                            audioPlayer.stopped();
                            audioPlayer.unregister(this);
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

    public static enum State {
        NOT_YET_STARTED, PLAYING, PAUSED, TERMINATED;
    }
    
    @Override
    public String toString() {
        return this.audioFile.toString();
    }

    public State getState() {
        return state;
    }

}
