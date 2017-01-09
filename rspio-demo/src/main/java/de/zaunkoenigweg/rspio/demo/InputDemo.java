package de.zaunkoenigweg.rspio.demo;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import de.zaunkoenigweg.rspio.core.input.InputController;

public class InputDemo {

    public static void main(String[] args) {
        InputController inputController = null;
        AbstractApplicationContext springContext = null;
        try {
            springContext = new AnnotationConfigApplicationContext(SpringContext.class);
            inputController = springContext.getBean(InputController.class);
            boolean inputControllerStarted = inputController.start();
            if(!inputControllerStarted) {
                return;
            }
            Thread.sleep(120000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            inputController.stop();
            springContext.close();
        }
        
    }
    
}
