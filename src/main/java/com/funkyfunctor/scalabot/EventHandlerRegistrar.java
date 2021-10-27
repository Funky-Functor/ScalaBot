package com.funkyfunctor.scalabot;

import com.github.philippheuer.events4j.core.EventManager;

public class EventHandlerRegistrar {
    public static <E> void registerHandlerToEventManager(EventManager eventManager, JavaEventHandler<E> eventHandler) {
        eventManager.onEvent(eventHandler.getClazz(), eventHandler.getConsumer());
    }
}
