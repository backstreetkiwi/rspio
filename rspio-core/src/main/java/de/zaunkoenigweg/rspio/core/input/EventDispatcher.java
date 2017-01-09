package de.zaunkoenigweg.rspio.core.input;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;

import de.zaunkoenigweg.rspio.core.component.Blockable;
import de.zaunkoenigweg.rspio.core.component.DigitalInputComponent;

/**
 * This dispatcher holds a queue with incoming events and fires them in the right sequence.
 * It acts as a buffer between the actual GPIO events from Pi4J and RSPIO's own event processing.
 * 
 * This dispatcher is a {@link Blockable} and can therefore be instructed to block further events.
 * 
 * As of now, the dispatcher works only for {@link DigitalInputComponent}s
 * 
 * @author mail@nikolaus-winter.de
 */
public class EventDispatcher implements Blockable {

    private final static Log LOG = LogFactory.getLog(EventDispatcher.class);

    /**
     * Event Queue.
     */
    private final BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>();

    /**
     * Thread that processes/dispatches events.
     */
    private Thread dispatcherThread;

    /**
     * Flag: EventDispatcher should stop.
     */
    private boolean stopped = false;

    /**
     * Flag: EventDispatcher temporarily blocked.
     */
    private boolean blocked = false;

    /**
     * The constructor defines the actual dispatcher thread.
     */
    public EventDispatcher() {
        dispatcherThread = new Thread(() -> {
            try {
                LOG.info("EventDispatcher started.");
                Event event;
                while (!stopped) {
                    event = eventQueue.poll(1, TimeUnit.SECONDS);
                    if (event == null) {
                        continue;
                    }
                    fireEvent(event);
                }
                LOG.info("EventDispatcher stopped.");
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
        dispatcherThread.start();
    }

    /**
     * Fires the actual event. This method calls the apropriate event callbacks and hands in the dispatcher itself, acting as a {@link Blockable}.
     * 
     * @param event event
     */
    private void fireEvent(Event event) {
        switch (event.event.getState()) {
        case HIGH:
            event.component.changedToHigh(this);
            break;
        case LOW:
            event.component.changedToLow(this);
            break;
        }
    }

    /**
     * Adds event to the queue, if the latter is not blocked.
     * 
     * @param component source of this event.
     * @param event Event
     */
    public void add(DigitalInputComponent component, GpioPinDigitalStateChangeEvent event) {
        synchronized(eventQueue) {
            if(this.blocked) {
                LOG.info("Event blocked!!!" + event.getPin());
                return;
            }
            eventQueue.add(new Event(component, event));
        }
    }

    /**
     * Stop further event processing.
     */
    public void stop() {
        this.stopped = true;
        LOG.info("stop requested");
    }

    /**
     * Block all further events and deletes existing ones.
     * 
     * This method must be called from within an action listener to make sure
     * no further events are processed.
     */
    public void block() {
        synchronized (eventQueue) {
            this.blocked = true;
            eventQueue.clear();
        }
    }
    
    /**
     * Do not block any further events.
     */
    public void unblock() {
        synchronized (eventQueue) {
            this.blocked = false;
        }
    }

    /**
     * Container object for events in the event queue.  
     */
    private class Event {
        Event(DigitalInputComponent component, GpioPinDigitalStateChangeEvent event) {
            this.component = component;
            this.event = event;
        }
        DigitalInputComponent component;
        GpioPinDigitalStateChangeEvent event;
    }
    
    
    
}
