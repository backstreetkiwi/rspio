package de.zaunkoenigweg.rspio.core.component;

import java.time.Duration;
import java.time.Instant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.pi4j.io.gpio.Pin;

/**
 * Release-Button for Raspberry Pi GPIO Port.
 * 
 * A Release-Button is considered to be pushed when connected to GND and released when connected to 3v3.
 * This requires a hardware(!) pull-up resistor connected to 3v3.
 * 
 * A Release-Button is neither aware of its state nor does it fire
 * events when being pushed.
 * 
 * @author mail@nikolaus-winter.de
 *
 */
public class ReleaseButton extends AbstractDigitalInputComponent {
    
    private final static Log LOG = LogFactory.getLog(ReleaseButton.class);

    private Action action;
    
    private Duration minimumPushDuration;
    
    private Instant pushedInstant;
    
    public ReleaseButton(String name, Pin pin) {
        this(name, pin, null);
    }
    
    public ReleaseButton(String name, Pin pin, Duration minimumPushDuration) {
        super(name, pin);
        this.minimumPushDuration = minimumPushDuration;
    }
    
    @Override
    public String toString() {
        return String.format("ReleaseButton '%s' (Pin %s)", this.getName(), this.getPin());
    }

    @Override
    public void changedToHigh(Blockable blockable) {
        if(pushedInstant!=null) {
            if(pushedInstant.plus(minimumPushDuration).isAfter(Instant.now())) {
                LOG.info(String.format("ReleaseButton for Pin %s released too early.", this.getPin()));
                return;
            }
        }
        if(action!=null) {
            action.run(blockable);
        }
    }

    @Override
    public void changedToLow(Blockable blockable) {
        LOG.info(String.format("ReleaseButton for Pin %s pushed.", this.getPin()));
        this.pushedInstant = Instant.now();
    }

    /**
     * Sets action to be executed when released.
     * 
     * This action should not block the calling thread too long.
     * If execution time is considered long, the action should spawn 
     * a new thread of execution.
     * 
     * @param action action to be executed when button is released
     */
    public void setAction(Action action) {
        this.action = action;
    }

}
