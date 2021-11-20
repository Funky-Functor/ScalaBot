package com.funkyfunctor.scalabot.twitch;

import com.github.philippheuer.events4j.core.EventManager;

public class EventHandlerRegistrar {
    public static <E> void registerEventHandler(EventManager manager, JavaEventHandler<E> eventHandler) {
        manager.onEvent(eventHandler.associatedClass(), eventHandler.consumer());
    }
}
