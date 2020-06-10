package org.xusheng.ioliw.tco;

import java.util.stream.Stream;

import static org.xusheng.ioliw.tco.TailCalls.call;
import static org.xusheng.ioliw.tco.TailCalls.done;

public class Trampoline {
    private static TailCall<Boolean> evenRec(final int number) {
        if (number == 0) {
            return done(true);
        }
        return call(() -> oddRec(number - 1));
    }

    private static TailCall<Boolean> oddRec(final int number) {
        if (number == 0) {
            return done(false);
        }
        return call(() -> evenRec(number - 1));
    }

    private static boolean even(final int number) {
        return evenRec(number).invoke();
    }

    public static void main(String[] args) {
        System.out.println(even(100000));
    }
}

@FunctionalInterface
interface TailCall<T> {

    TailCall<T> apply();

    default boolean isComplete() {
        return false;
    }

    default T result() {
        throw new Error("not implemented");
    }

    default T invoke() {
        return Stream.iterate(this, TailCall::apply)
            .filter(TailCall::isComplete)
            .findFirst()
            .get()
            .result();
    }
}

class TailCalls {
    public static <T> TailCall<T> call(final TailCall<T> nextCall) {
        return nextCall;
    }

    public static <T> TailCall<T> done(final T value) {
        return new TailCall<T>() {
            @Override
            public boolean isComplete() {
                return true;
            }

            @Override
            public T result() {
                return value;
            }

            @Override
            public TailCall<T> apply() {
                throw new Error("not implemented");
            }
        };
    }
}
