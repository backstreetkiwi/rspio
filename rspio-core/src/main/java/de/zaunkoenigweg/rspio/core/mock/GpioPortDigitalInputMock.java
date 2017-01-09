package de.zaunkoenigweg.rspio.core.mock;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinShutdown;
import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinListener;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.gpio.trigger.GpioTrigger;

/**
 * Mocks a @link {@link GpioPinDigitalInput} of Pi4J.
 * 
 * This mock must/can be provided with a function (Consumer of {@link GpioPinListenerDigital}) that
 * will be called once a listener is set. 
 * 
 * @author mail@nikolaus-winter.de
 */
public class GpioPortDigitalInputMock implements GpioPinDigitalInput {

    private Consumer<GpioPinListenerDigital> registerListener;
    
    public GpioPortDigitalInputMock(Consumer<GpioPinListenerDigital> registerListener) {
        this.registerListener = registerListener;
    }

    /**
     * {@inheritDoc}
     * 
     * This mock supports only one listener!
     */
    @Override
    public void addListener(GpioPinListener... listener) {
        if(listener.length!=1) {
            return;
        }
        if(listener[0] instanceof GpioPinListenerDigital) {
            registerListener.accept((GpioPinListenerDigital) listener[0]);
        }
    }

    // ------------------------------------------------------
    // unsupported operations
    // ------------------------------------------------------

    @Override
    public boolean isHigh() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLow() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PinState getState() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isState(PinState state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GpioProvider getProvider() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Pin getPin() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTag(Object tag) {
        throw new UnsupportedOperationException();

    }

    @Override
    public Object getTag() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setProperty(String key, String value) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean hasProperty(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getProperty(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getProperties() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeProperty(String key) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void clearProperties() {
        throw new UnsupportedOperationException();

    }

    @Override
    public void export(PinMode mode) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void export(PinMode mode, PinState defaultState) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void unexport() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isExported() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMode(PinMode mode) {
        throw new UnsupportedOperationException();

    }

    @Override
    public PinMode getMode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMode(PinMode mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPullResistance(PinPullResistance resistance) {
        throw new UnsupportedOperationException();

    }

    @Override
    public PinPullResistance getPullResistance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPullResistance(PinPullResistance resistance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<GpioPinListener> getListeners() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addListener(List<? extends GpioPinListener> listeners) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean hasListener(GpioPinListener... listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeListener(GpioPinListener... listener) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void removeListener(List<? extends GpioPinListener> listeners) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void removeAllListeners() {
        throw new UnsupportedOperationException();

    }

    @Override
    public GpioPinShutdown getShutdownOptions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setShutdownOptions(GpioPinShutdown options) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setShutdownOptions(Boolean unexport) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setShutdownOptions(Boolean unexport, PinState state) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setShutdownOptions(Boolean unexport, PinState state, PinPullResistance resistance) {
        // ignored, no shutdown executed as this is a mock
    }

    @Override
    public void setShutdownOptions(Boolean unexport, PinState state, PinPullResistance resistance, PinMode mode) {
        throw new UnsupportedOperationException();

    }

    @Override
    public Collection<GpioTrigger> getTriggers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addTrigger(GpioTrigger... trigger) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void addTrigger(List<? extends GpioTrigger> triggers) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void removeTrigger(GpioTrigger... trigger) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void removeTrigger(List<? extends GpioTrigger> triggers) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void removeAllTriggers() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean hasDebounce(PinState state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getDebounce(PinState state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDebounce(int debounce) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setDebounce(int debounce, PinState... state) {
        throw new UnsupportedOperationException();

    }

}
