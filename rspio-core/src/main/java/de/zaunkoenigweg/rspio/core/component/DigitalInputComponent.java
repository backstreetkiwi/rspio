package de.zaunkoenigweg.rspio.core.component;

/**
 * Component that registers digital input from GPIO ports.
 * 
 * @author mail@nikolaus-winter.de
 */
public interface DigitalInputComponent extends InputComponent {

    /**
     * Event handler that gets fired if input is changed to HIGH.
     * 
     * This method <u>must</u> terminate as fast as possible, as is called by
     * the event handling thread of RSPIO.
     * 
     * Therefore any expensive operations must be performed in a parallel thread of execution.
     * 
     * Any blocking {@link Blockable#unblock()} made through provided {@link Blockable} must be performed in this method,
     * whereas unblocking {@link Blockable#unblock()} can be made at any time later.
     * 
     * @param blockable can be used to block/unblock further event processing.
     */
    public void changedToHigh(Blockable blockable);

    /**
     * Event handler that gets fired if input is changed to LOW.
     * 
     * This method <u>must</u> terminate as fast as possible, as is called by
     * the event handling thread of RSPIO.
     * 
     * Therefore any expensive operations must be performed in a parallel thread of execution.
     * 
     * Any blocking {@link Blockable#unblock()} made through provided {@link Blockable} must be performed in this method,
     * whereas unblocking {@link Blockable#unblock()} can be made at any time later.
     * 
     * @param blockable can be used to block/unblock further event processing.
     */
    public void changedToLow(Blockable blockable);
}
