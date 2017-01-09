package de.zaunkoenigweg.rspio.core.component;

import com.pi4j.io.gpio.Pin;

/**
 * Push-Button for Raspberry Pi GPIO Port.
 * 
 * A Push-Button is considered to be pushed when connected to GND.
 * This requires a hardware(!) pull-up resistor connected to 3v3.
 * 
 * A Push-Button is neither aware of its state nor does it fire
 * events for release.
 * 
 * @author mail@nikolaus-winter.de
 *
 */
public class PushButton extends AbstractDigitalInputComponent {
    
    private Action action;
    
    public PushButton(String name, Pin pin) {
        super(name, pin);
    }
    
    @Override
    public String toString() {
        return String.format("PushButton '%s' (Pin %s)", this.getName(), this.getPin());
    }

    @Override
    public void changedToHigh(Blockable blockable) {
        // A Push-Button ignores this one
    }

    @Override
    public void changedToLow(Blockable blockable) {
        if(action!=null) {
            action.run(blockable);
        }
    }

    /**
     * Sets action to be executed when pushed.
     * 
     * This action should not block the calling thread too long.
     * If execution time is considered long, the action should spawn 
     * a new thread of execution.
     * 
     * @param action action to be executed when button is pushed
     */
    public void setAction(Action action) {
        this.action = action;
    }

}
