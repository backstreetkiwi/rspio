package de.zaunkoenigweg.rspio.demo;

import java.util.function.Supplier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.RaspiPin;

import de.zaunkoenigweg.rspio.core.component.PushButton;
import de.zaunkoenigweg.rspio.core.input.InputController;
import de.zaunkoenigweg.rspio.core.mock.GpioControllerMock;

@Configuration
public class SpringContext {

    @Bean
    public InputController inputController() {
        return new InputController();
    }

    @Bean
    public Supplier<GpioController> gpioControllerSupplier() {
        return () -> new GpioControllerMock(2000, 1000, 100);
    }

    @Bean
    public PushButton pushButtonRed() {
        PushButton pushButtonRed = new PushButton("RED Arcade Button", RaspiPin.GPIO_13);
        pushButtonRed.setAction((blockable -> {
            new Thread(() -> {
                System.out.printf("RED (START)%n");
                for (int i = 1; i < 6; i++) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.printf("RED (%d)%n", i);
                }
                blockable.unblock();
            }).start();
            blockable.block();
        }));
        return pushButtonRed;
    }
}
