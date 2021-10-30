package com.funkyfunctor.scalabot;

import java.util.function.Consumer;

/**
 * Using this interface to help with the Java-Scala interaction
 * @param <E> Type of the event we want to register our handler with
 */
public interface JavaEventHandler<E> {
    Consumer<E> getConsumer();

    Class<E> getClazz();
}
