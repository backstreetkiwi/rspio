package de.zaunkoenigweg.rspio.core.input;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import de.zaunkoenigweg.rspio.core.component.DigitalInputComponent;
import de.zaunkoenigweg.rspio.core.component.InputComponent;

/**
 * Controls all input of RSPIO
 * 
 * @author mail@nikolaus-winter.de
 */
public class InputController {

    private final static Log LOG = LogFactory.getLog(InputController.class);

    /**
     * Registered input components.
     */
    private Map<Pin, InputComponent> inputComponents = new HashMap<>();
    
    /**
     * Flag: Has an error occured during registration?
     */
    private boolean errorsDuringRegistration = false;
    
    /**
     * Flag: Is this controller running?
     */
    private boolean running = false;
 
    /**
     * Supplies GPIO Controller (Pi4J)
     */
    @Autowired
    private Supplier<GpioController> gpioControllerSupplier;
    
    /**
     * GPIO Controller (obtained from {@link #gpioControllerSupplier}
     */
    private GpioController gpioController;
    
    /**
     * Event Dispatcher
     */
    private EventDispatcher eventDispatcher;

    /**
     * Starts this GPIO controller.
     * @return Has the controller been started properly?
     */
    synchronized public boolean start() {
        if (this.errorsDuringRegistration) {
            LOG.error("RSPIO InputController could not be started due to errors during registration of InputComponents.");
            return false;
        }
        
        this.eventDispatcher = new EventDispatcher();

        gpioController = gpioControllerSupplier.get();
        this.inputComponents.forEach((pin, inputComponent) -> {
            GpioPinDigitalInput digitalInput = gpioController.provisionDigitalInputPin(pin);
            digitalInput.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
            digitalInput.addListener(createGpioPinListener(inputComponent));
        });
        running = true;

        LOG.info(String.format("RSPIO InputController started with %d input components.", inputComponents.size()));
        return true;
    }

    /**
     * Stopps this GPIO controller.
     * @return Has the controller been stopped properly?
     */
    synchronized public boolean stop() {
        if (!this.running) {
            LOG.error("RSPIO InputController could not be shut down. It is not running.");
            return false;
        }

        eventDispatcher.stop();
        gpioController.shutdown();
        running = false;

        LOG.info(String.format("RSPIO InputController with %d input components shut down.", inputComponents.size()));
        return true;
    }

    /**
     * Register input component.
     * @param inputComponent component to register
     * @return Has the component been registered properly?
     */
    public boolean register(InputComponent inputComponent) {
        if (inputComponent == null) {
            LOG.warn("InputComponent 'null' could not be registered.");
            return false;
        }
        synchronized (this.inputComponents) {
            if (this.running) {
                LOG.warn(String.format("InputComponent '%s' could not be registered. InputController already started.", inputComponent));
                return false;
            }
            if (this.inputComponents.containsKey(inputComponent.getPin())) {
                String errorMessage = String.format("InputComponent '%s' could not be registered. Pin %s is in use.", inputComponent, inputComponent.getPin());
                LOG.error(errorMessage);
                this.errorsDuringRegistration = true;
                return false;
            }
            inputComponents.put(inputComponent.getPin(), inputComponent);
            LOG.info(String.format("RSPIO InputController %s registered.", inputComponent));
            return true;
        }
    }

    private GpioPinListenerDigital createGpioPinListener(final InputComponent inputComponent) {
        return (event) -> eventDispatcher.add((DigitalInputComponent)inputComponent, event);
    }

}
