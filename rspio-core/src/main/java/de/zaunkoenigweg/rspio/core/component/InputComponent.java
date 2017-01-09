package de.zaunkoenigweg.rspio.core.component;

import com.pi4j.io.gpio.Pin;

/**
 * Component that registers input from GPIO ports.
 * 
 * @author mail@nikolaus-winter.de
 */
public interface InputComponent extends Component {

    /**
     * GPIO pin to which this input component is assigned to
     * @return GPIO pin
     */
    public Pin getPin();

    /**
     * Gets the name of this component.
     * @return Name of this component
     */
    public String getName();
}
