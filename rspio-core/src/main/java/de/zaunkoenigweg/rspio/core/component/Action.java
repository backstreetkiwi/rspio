package de.zaunkoenigweg.rspio.core.component;

/**
 * Action to perform when InputComponent is triggered.
 * 
 * @author mail@nikolaus-winter.de
 */
@FunctionalInterface
public interface Action {

    /**
     * Runs this action. 
     * 
     * This method <u>must</u> terminate immediately. Any expensive operations must be handled in different threads of execution.
     * 
     * @param blockable to block/unblock further execution
     */
    void run(Blockable blockable);
}
