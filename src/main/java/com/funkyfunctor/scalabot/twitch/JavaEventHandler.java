package com.funkyfunctor.scalabot.twitch;

import java.util.function.Consumer;

public interface JavaEventHandler<E> {
    public Class<E> associatedClass();

    public Consumer<E> consumer();
}
