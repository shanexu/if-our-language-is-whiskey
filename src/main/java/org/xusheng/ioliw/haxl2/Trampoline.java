package org.xusheng.ioliw.haxl2;

import lombok.AllArgsConstructor;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Trampoline<A> {

    default A runT() {
        Trampoline<A> t = this;
        while (!(t instanceof Done)) {
            t = t.resume();
        }
        return ((Done<A>) t).result;
    }

    default Trampoline<A> resume() {
        return this;
    }

    default <B> Trampoline<B> flatMap(Function<A, Trampoline<B>> f) {
        return new FlatMap<>(this, f);
    }

    default <B> Trampoline<B> map(Function<A, B> f) {
        return new FlatMap<>(this, a -> done(f.apply(a)));
    }

    @AllArgsConstructor
    class Done<A> implements Trampoline<A> {
        private final A result;
    }

    static <T> Trampoline<T> done(T t) {
        return new Done<>(t);
    }

    @AllArgsConstructor
    class More<A> implements Trampoline<A> {
        private final Supplier<Trampoline<A>> k;

        @Override
        public Trampoline<A> resume() {
            return k.get();
        }
    }

    static <T> Trampoline<T> more(Supplier<Trampoline<T>> k) {
        return new More<>(k);
    }

    @AllArgsConstructor
    class FlatMap<B, A> implements Trampoline<A> {
        private final Trampoline<B> sub;
        private final Function<B, Trampoline<A>> k;

        @Override
        public Trampoline<A> resume() {
            if (sub instanceof Done) {
                return k.apply(((Done<B>) sub).result);
            }
            if (sub instanceof More) {
                return new FlatMap<>(((More<B>) sub).k.get(), k);
            }
            if (sub instanceof FlatMap) {
                FlatMap<Object, B> s = (FlatMap<Object, B>) sub;
                Trampoline<Object> b = s.sub;
                Function<Object, Trampoline<B>> g = s.k;
                return new FlatMap<>(b, x -> new FlatMap<>(g.apply(x), k));
            }
            throw new RuntimeException();
        }
    }

    static <B, T> Trampoline<T> flatMap(Trampoline<B> sub, Function<B, Trampoline<T>> k) {
        return new FlatMap<>(sub, k);
    }

    static void main(String[] args) {
        Trampoline<Void> m = new More<>(() -> {
            System.out.println("begin");
            return new Done<>(null);
        });
        for (int i = 0; i < 1000000; i++) {
            m = m.flatMap(x -> new Done<>(null));
        }
        m = m.flatMap(x -> {
            System.out.println("end");
            return new Done<>(null);
        });
        m.runT();
    }
}
