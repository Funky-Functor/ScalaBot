package com.funkyfunctor.scalabot;

import java.util.function.Consumer;

public interface JavaEventHandler<E> {
    Consumer<E> getConsumer();

    Class<E> getClazz();
}
