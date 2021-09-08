package de.imi.odmtoolbox.convert.helper.lambda.interfaces;

@FunctionalInterface
public interface ThrowingLambdaConsumer<T, E extends Exception> {
    void accept(T t) throws E;
}

