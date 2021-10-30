package com.funkyfunctor.scalabot;

import com.github.philippheuer.events4j.core.EventManager;

public class EventHandlerRegistrar {
    /**
     * Method used to register an EventHandler with an EventManager. Had to do it in Java as there are otherwise problems with the Scala interop.
     * @param eventManager EventManager to register with
     * @param eventHandler JavaEventHandler we want to register with the EventManager
     * @param <E> Type of the event the JavaEventHandler takes care of
     */
    public static <E> void registerHandlerToEventManager(EventManager eventManager, JavaEventHandler<E> eventHandler) {
        eventManager.onEvent(eventHandler.getClazz(), eventHandler.getConsumer());
    }
}
