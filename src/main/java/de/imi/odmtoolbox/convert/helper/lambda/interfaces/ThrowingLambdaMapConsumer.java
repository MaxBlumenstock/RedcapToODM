package de.imi.odmtoolbox.convert.helper.lambda.interfaces;

@FunctionalInterface
public interface ThrowingLambdaMapConsumer<K,V, E extends Exception> {
    void accept(K k, V v) throws E;
}
