package de.imi.odmtoolbox.convert.helper.lambda;

import de.imi.odmtoolbox.convert.helper.lambda.interfaces.ThrowingLambdaConsumer;
import de.imi.odmtoolbox.convert.helper.lambda.interfaces.ThrowingLambdaMapConsumer;
import de.imi.odmtoolbox.convert.helper.lambda.interfaces.ThrowingOptionalFunction;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class LambdaHelper {
    public static <T> Consumer<T> throwingConsumerWrapper(ThrowingLambdaConsumer<T, Exception> throwingConsumer) {

        return i -> {
            try {
                throwingConsumer.accept(i);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    public static <K,V> BiConsumer<K, V> throwingBiConsumerWrapper(ThrowingLambdaMapConsumer<K,V, Exception> throwingMapConsumer) {

        return (k,v) -> {
            try {
                throwingMapConsumer.accept(k,v);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    public static <T, R> Function<T, R> throwingOptionalFunctionrWrapper(ThrowingOptionalFunction<T, R, Exception> throwingOptionalFunction) {
        return t -> {
            try {
                return throwingOptionalFunction.accept(t);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }
}
