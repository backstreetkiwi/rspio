package de.zaunkoenigweg.rspio.core.mock;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinAnalog;
import com.pi4j.io.gpio.GpioPinAnalogInput;
import com.pi4j.io.gpio.GpioPinAnalogOutput;
import com.pi4j.io.gpio.GpioPinDigital;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalMultipurpose;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinInput;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.GpioPinShutdown;
import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListener;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.gpio.trigger.GpioTrigger;

/**
 * Mocks a @link {@link GpioController} of Pi4J.
 * 
 * This mock randomly fires events.
 * 
 * @author mail@nikolaus-winter.de
 */
public class GpioControllerMock implements GpioController {

    private final static Log LOG = LogFactory.getLog(GpioControllerMock.class);

    /**
     * Holds state of the GPIO pins.
     */
    private final Map<Pin, PinState> pinStates = new HashMap<>();

    /**
     * Holds listeners for GPIO pin changes.
     */
    private final Map<Pin, GpioPinListenerDigital> pinListener = new HashMap<>();

    /**
     * Thread that produces random events.
     */
    private Thread eventGenerator;
    
    /**
     * Flag: EventGenerator should stop.
     */
    private boolean stopped = false;
    
    /**
     * Random number generator.
     */
    private final Random random = new Random();
    
    public GpioControllerMock(long initialWaitingTime, long timeBetweenEvents, int numberOfEvents) {
        eventGenerator = new Thread(() -> {
            try {
                LOG.info("EventGenerator started.");
                LOG.info(String.format("EventGenerator sleeping for %d millis to wait for initialization to finish.", initialWaitingTime));
                Thread.sleep(initialWaitingTime);
                int i=numberOfEvents;
                while(!stopped && i-->0) {
                    fireRandomEvent();
                    Thread.sleep(timeBetweenEvents);
                }
                LOG.info("EventGenerator stopped.");
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
        eventGenerator.start();
    }
    
    @Override
    public GpioPinDigitalInput provisionDigitalInputPin(Pin pin) {
        synchronized (pinStates) {
            pinStates.put(pin, PinState.HIGH);
        }
        return new GpioPortDigitalInputMock((listener) -> {
            synchronized (pinListener) {
                this.pinListener.put(pin, listener);
            }
        });
    }
    
    @Override
    public void shutdown() {
        this.stopped = true;
        LOG.info("shutdown requested");
    }

    private void fireRandomEvent() {
        Optional<Pin> randomPin = pinStates.keySet()
                                           .stream()
                                           .skip(random.nextInt(pinStates.size()))
                                           .findFirst();
        Pin pin = randomPin.get();
        PinState newState = PinState.getInverseState(pinStates.get(pin));
        pinStates.put(pin, newState);
        GpioPinDigitalStateChangeEvent event = new GpioPinDigitalStateChangeEvent(this, null, newState);
        pinListener.get(pin).handleGpioPinDigitalStateChangeEvent(event);
        LOG.info(String.format("Fired event to %s: new state: %s", pin, newState));
    }
    
    
    // ------------------------------------------------------
    // unsupported operations
    // ------------------------------------------------------

    @Override
    public void export(PinMode mode, PinState defaultState, GpioPin... pin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void export(PinMode mode, GpioPin... pin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isExported(GpioPin... pin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unexport(Pin... pin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void unexport(GpioPin... pin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void unexportAll() {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setMode(PinMode mode, GpioPin... pin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public PinMode getMode(GpioPin pin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMode(PinMode mode, GpioPin... pin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPullResistance(PinPullResistance resistance, GpioPin... pin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public PinPullResistance getPullResistance(GpioPin pin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPullResistance(PinPullResistance resistance, GpioPin... pin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void high(GpioPinDigitalOutput... pin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isHigh(GpioPinDigital... pin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void low(GpioPinDigitalOutput... pin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isLow(GpioPinDigital... pin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setState(PinState state, GpioPinDigitalOutput... pin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setState(boolean state, GpioPinDigitalOutput... pin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isState(PinState state, GpioPinDigital... pin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PinState getState(GpioPinDigital pin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void toggle(GpioPinDigitalOutput... pin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void pulse(long milliseconds, GpioPinDigitalOutput... pin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setValue(double value, GpioPinAnalogOutput... pin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public double getValue(GpioPinAnalog pin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addListener(GpioPinListener listener, GpioPinInput... pin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void addListener(GpioPinListener[] listeners, GpioPinInput... pin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void removeListener(GpioPinListener listener, GpioPinInput... pin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void removeListener(GpioPinListener[] listeners, GpioPinInput... pin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void removeAllListeners() {
        throw new UnsupportedOperationException();

    }

    @Override
    public void addTrigger(GpioTrigger trigger, GpioPinInput... pin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void addTrigger(GpioTrigger[] triggers, GpioPinInput... pin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void removeTrigger(GpioTrigger trigger, GpioPinInput... pin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void removeTrigger(GpioTrigger[] triggers, GpioPinInput... pin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void removeAllTriggers() {
        throw new UnsupportedOperationException();

    }

    @Override
    public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(GpioProvider provider, Pin pin, String name, PinMode mode, PinPullResistance resistance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(GpioProvider provider, Pin pin, PinMode mode, PinPullResistance resistance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(GpioProvider provider, Pin pin, String name, PinMode mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(GpioProvider provider, Pin pin, PinMode mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(Pin pin, String name, PinMode mode, PinPullResistance resistance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(Pin pin, PinMode mode, PinPullResistance resistance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(Pin pin, String name, PinMode mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(Pin pin, PinMode mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinDigitalInput provisionDigitalInputPin(GpioProvider provider, Pin pin, String name, PinPullResistance resistance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinDigitalInput provisionDigitalInputPin(GpioProvider provider, Pin pin, PinPullResistance resistance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinDigitalInput provisionDigitalInputPin(GpioProvider provider, Pin pin, String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinDigitalInput provisionDigitalInputPin(GpioProvider provider, Pin pin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinDigitalInput provisionDigitalInputPin(Pin pin, String name, PinPullResistance resistance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinDigitalInput provisionDigitalInputPin(Pin pin, PinPullResistance resistance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinDigitalInput provisionDigitalInputPin(Pin pin, String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinDigitalOutput provisionDigitalOutputPin(GpioProvider provider, Pin pin, String name, PinState defaultState) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinDigitalOutput provisionDigitalOutputPin(GpioProvider provider, Pin pin, PinState defaultState) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinDigitalOutput provisionDigitalOutputPin(GpioProvider provider, Pin pin, String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinDigitalOutput provisionDigitalOutputPin(GpioProvider provider, Pin pin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinDigitalOutput provisionDigitalOutputPin(Pin pin, String name, PinState defaultState) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinDigitalOutput provisionDigitalOutputPin(Pin pin, PinState defaultState) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinDigitalOutput provisionDigitalOutputPin(Pin pin, String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinDigitalOutput provisionDigitalOutputPin(Pin pin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinAnalogInput provisionAnalogInputPin(GpioProvider provider, Pin pin, String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinAnalogInput provisionAnalogInputPin(GpioProvider provider, Pin pin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinAnalogInput provisionAnalogInputPin(Pin pin, String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinAnalogInput provisionAnalogInputPin(Pin pin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinAnalogOutput provisionAnalogOutputPin(GpioProvider provider, Pin pin, String name, double defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinAnalogOutput provisionAnalogOutputPin(GpioProvider provider, Pin pin, double defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinAnalogOutput provisionAnalogOutputPin(GpioProvider provider, Pin pin, String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinAnalogOutput provisionAnalogOutputPin(GpioProvider provider, Pin pin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinAnalogOutput provisionAnalogOutputPin(Pin pin, String name, double defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinAnalogOutput provisionAnalogOutputPin(Pin pin, double defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinAnalogOutput provisionAnalogOutputPin(Pin pin, String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinAnalogOutput provisionAnalogOutputPin(Pin pin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinPwmOutput provisionPwmOutputPin(GpioProvider provider, Pin pin, String name, int defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinPwmOutput provisionPwmOutputPin(GpioProvider provider, Pin pin, int defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinPwmOutput provisionPwmOutputPin(GpioProvider provider, Pin pin, String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinPwmOutput provisionPwmOutputPin(GpioProvider provider, Pin pin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinPwmOutput provisionPwmOutputPin(Pin pin, String name, int defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinPwmOutput provisionPwmOutputPin(Pin pin, int defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinPwmOutput provisionPwmOutputPin(Pin pin, String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinPwmOutput provisionPwmOutputPin(Pin pin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinPwmOutput provisionSoftPwmOutputPin(GpioProvider provider, Pin pin, String name, int defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinPwmOutput provisionSoftPwmOutputPin(GpioProvider provider, Pin pin, int defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinPwmOutput provisionSoftPwmOutputPin(GpioProvider provider, Pin pin, String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinPwmOutput provisionSoftPwmOutputPin(GpioProvider provider, Pin pin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinPwmOutput provisionSoftPwmOutputPin(Pin pin, String name, int defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinPwmOutput provisionSoftPwmOutputPin(Pin pin, int defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinPwmOutput provisionSoftPwmOutputPin(Pin pin, String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPinPwmOutput provisionSoftPwmOutputPin(Pin pin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPin provisionPin(GpioProvider provider, Pin pin, String name, PinMode mode, PinState defaultState) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPin provisionPin(GpioProvider provider, Pin pin, String name, PinMode mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPin provisionPin(GpioProvider provider, Pin pin, PinMode mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPin provisionPin(Pin pin, String name, PinMode mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPin provisionPin(Pin pin, PinMode mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setShutdownOptions(GpioPinShutdown options, GpioPin... pin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setShutdownOptions(Boolean unexport, GpioPin... pin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setShutdownOptions(Boolean unexport, PinState state, GpioPin... pin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setShutdownOptions(Boolean unexport, PinState state, PinPullResistance resistance, GpioPin... pin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setShutdownOptions(Boolean unexport, PinState state, PinPullResistance resistance, PinMode mode, GpioPin... pin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public Collection<GpioPin> getProvisionedPins() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPin getProvisionedPin(Pin pin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioPin getProvisionedPin(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unprovisionPin(GpioPin... pin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isShutdown() {
        throw new UnsupportedOperationException();
    }

}
