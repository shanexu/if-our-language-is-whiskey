package org.xusheng.ioliw.haxl2;

import lombok.AllArgsConstructor;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface Trampoline<A> {

    default A runT() {
        Optional<Done<A>> first = Stream.iterate(this, Trampoline::resume).filter(t -> t instanceof Done).map(t -> (Done<A>)t).findFirst();
        return first.get().result;
    }

    default Trampoline<A> resume() {
        return this;
    }

    default <B> Trampoline<B> flatMap(Function<A, Trampoline<B>> f) {
        return new FlatMap<>(this, f);
    }

    @AllArgsConstructor
    class Done<A> implements Trampoline<A> {
        private final A result;
    }

    @AllArgsConstructor
    class More<A> implements Trampoline<A> {
        private final Supplier<Trampoline<A>> k;

        @Override
        public Trampoline<A> resume() {
            return k.get();
        }
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
                if (s.sub instanceof Done) {
                    return new FlatMap<>(s.k.apply(((Done<Object>) s.sub).result), k);
                }
                if (s.sub instanceof More) {
                    s = new FlatMap<>(((More<Object>) s.sub).k.get(), s.k);
                    return new FlatMap<>(s, k);
                }
                if (s.sub instanceof FlatMap) {
                    FlatMap<Object, Object> ss = (FlatMap<Object, Object>) s.sub;
                    Trampoline<Object> b = ss.sub;
                    Function<Object, Trampoline<Object>> g = ss.k;
                    return new FlatMap<>(b, x -> new FlatMap<>(g.apply(x), (Function<Object, Trampoline<A>>) k));
                }
            }
            throw new RuntimeException();
        }
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
