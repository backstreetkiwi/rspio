package de.zaunkoenigweg.rspio.audio.omx;

import java.io.PrintWriter;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.zaunkoenigweg.rspio.audio.AudioTrackRegistry;
import de.zaunkoenigweg.rspio.audio.AudioTrackTemplate;
import de.zaunkoenigweg.rspio.audio.PlaybackIndicator;
import de.zaunkoenigweg.rspio.audio.PlaybackState;

public class OmxAudioTrack extends AudioTrackTemplate {

    private final static Log LOG = LogFactory.getLog(OmxAudioTrack.class);

    Process omxProcess;
    PrintWriter omxInputWriter;

    public OmxAudioTrack(Path audioFile, AudioTrackRegistry runningTracks) {
        this(audioFile, PlaybackIndicator.ignoring(), runningTracks);
    }
    
    public OmxAudioTrack(Path audioFile, PlaybackIndicator playbackIndicator, AudioTrackRegistry runningTracks) {
        super(audioFile, playbackIndicator, runningTracks);
    }
    
    @Override
    protected boolean initAndPlayImpl() {
        try {
            omxProcess = new ProcessBuilder("bash", "-c", "omxplayer " + StringUtils.replace(this.audioFile.toString(), " ", "\\ ")).start();
            omxInputWriter = new PrintWriter(omxProcess.getOutputStream());
            new Thread(() -> {
                try {
                    runningTracks.register(this);
                    omxProcess.waitFor();
                    synchronized (state) {
                    	if(state!=PlaybackState.TERMINATED) {
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
    	return send('p');
    }
    
    @Override
    protected boolean resumeImpl() {
    	return send('p');
    }
    
    @Override
    protected boolean stopAndTerminateImpl() {
    	return send('q');
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

}
