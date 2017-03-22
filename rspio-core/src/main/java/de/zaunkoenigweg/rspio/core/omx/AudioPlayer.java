package de.zaunkoenigweg.rspio.core.omx;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

public class AudioPlayer {

    private final static Log LOG = LogFactory.getLog(AudioPlayer.class);
    
    // TODO use RSPIO instead of GpioController itself...
    @Autowired
    private Supplier<GpioController> gpioControllerSupplier;

    private Pin playbackLedPin;
   
    private GpioPinDigitalOutput playbackLed;
    
    private Set<AudioTrack> playingTracks = new HashSet<>();
    
    public AudioPlayer() {
    }

    @PostConstruct
    public void init() {
        if(playbackLedPin==null) {
            String errorMessage = "AudioPlayer could not be initialized: Playback LED pin is missing.";
            LOG.error(errorMessage);
            throw new BeanCreationException(errorMessage);
        }
        playbackLed = this.gpioControllerSupplier.get().provisionDigitalOutputPin(this.playbackLedPin, "Playback", PinState.LOW);
        playbackLed.setShutdownOptions(true, PinState.LOW);
    }

    public void playing() {
        playbackLed.high();
    }
    
    public void stopped() {
        playbackLed.low();
    }
    
    public void register(AudioTrack track) {
        synchronized (playingTracks) {
            this.playingTracks.add(track);
        }
    }
    
    public void unregister(AudioTrack track) {
        synchronized (playingTracks) {
            this.playingTracks.remove(track);
        }
    }
    
    public void stopAll() {
        List<AudioTrack> list = this.playingTracks.stream().collect(Collectors.toList());
        list.stream().forEach(AudioTrack::stop);
    }
    
    public void setPlaybackLedPin(Pin playbackLedPin) {
        this.playbackLedPin = playbackLedPin;
    }
}
