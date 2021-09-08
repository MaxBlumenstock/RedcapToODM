package de.imi.odmtoolbox.convert.helper.lambda.interfaces;

@FunctionalInterface
public interface ThrowingOptionalFunction<T, R, E extends Exception> {
    R accept(T t) throws E;
}

