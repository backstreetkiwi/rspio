package de.zaunkoenigweg.rspio.core.component;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;

import com.pi4j.io.gpio.Pin;

import de.zaunkoenigweg.rspio.core.input.InputController;

/**
 * Base class for all digital input components.
 *
 * @author mail@nikolaus-winter.de
 */
public abstract class AbstractDigitalInputComponent implements DigitalInputComponent {

    private final static Log LOG = LogFactory.getLog(AbstractDigitalInputComponent.class);
    
    private String name;
    private Pin pin;

    /**
     * This controller handles the GPIO input. This component registers itself to the controller after its construction.
     */
    @Autowired
    protected InputController inputController;
    
    /**
     * Creates a DigitalInputComponent.
     * @param name Name of this component
     * @param pin GPIO Pin this component is assigned to
     */
    public AbstractDigitalInputComponent(String name, Pin pin) {
        this.name = name;
        this.pin = pin;
    }

    /**
     * After construction this component registers itself to the InputController
     */
    @PostConstruct
    public void register() {
        if(inputController==null) {
            String errorMessage = String.format("DigitalInputComponent '%s' could not be registered. InputController is missing.", this.name);
            LOG.error(errorMessage);
            throw new BeanCreationException(errorMessage);
        }
        if(!this.inputController.register(this)) {
            String errorMessage = String.format("DigitalInputComponent '%s' could not be registered.", this.name);
            LOG.error(errorMessage);
            throw new BeanCreationException(errorMessage);
        }
        LOG.info(String.format("DigitalInputComponent '%s' registered at InputController.", this));
    }

    public Pin getPin() {
        return pin;
    }
    
    public String getName() {
        return name;
    }
}
