package org.xusheng.ioliw.haxl;

import java.util.function.Function;
import java.util.function.Supplier;

public class IO<T> implements Higher<IO.µ, T> {
    public static final µ I = new µ();

    private IO(Supplier<T> value) {
        this.value = value;
    }

    private final Supplier<T> value;

    public Supplier<T> getValue() {
        return value;
    }

    public static <R> IO<R> of(Supplier<R> s) {
        return new IO<>(s);
    }

    public static <T> IO<T> narrowK(Higher<IO.µ, T> h) {
        return (IO<T>) h;
    }

    public static <T> T runIO(Higher<IO.µ, T> h) {
        return narrowK(h).getValue().get();
    }

    public static class µ implements Functor<IO.µ>, Applicative<IO.µ>, Monad<IO.µ> {
        private µ() {
        }

        @Override
        public <A, B> Higher<IO.µ, B> fmap(Function<? super A, ? extends B> f, Higher<IO.µ, A> fa) {
            return IO.of(() -> f.apply(((IO<A>) fa).getValue().get()));
        }

        @Override
        public <A> Higher<µ, A> pure(A a) {
            return IO.of(() -> a);
        }

        @Override
        public <A, B> Higher<µ, B> amap(Higher<µ, Function<A, B>> f, Higher<µ, A> fa) {
            return IO.of(() -> ((IO<Function<A, B>>) f).getValue().get().apply(((IO<A>) fa).getValue().get()));
        }

        @Override
        public <A, B> Higher<µ, B> bind(Higher<µ, A> ma, Function<A, Higher<µ, B>> f) {
            return IO.of(() -> narrowK(f.apply(narrowK(ma).getValue().get())).getValue().get());
        }
    }
}
