package org.xusheng.ioliw.haxl2;

import lombok.AllArgsConstructor;

import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Trampoline<A> {

    Trampoline<A> resume();

    default A runT() {
        Trampoline<A> t = this;
        while (!(t instanceof Done)) {
            t = t.resume();
        }
        return ((Done<A>) t).result;
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

        @Override
        public Trampoline<A> resume() {
            return this;
        }
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
            throw new RuntimeException("unhandled sub type " + this.sub.getClass());
        }
    }

    static <B, A> Trampoline<A> flatMap(Trampoline<B> sub, Function<B, Trampoline<A>> k) {
        return new FlatMap<>(sub, k);
    }

    public static void main(String[] args) {
        more(() -> {
            System.out.print("What's your name? ");
            return done(null);
        })
            .flatMap(v -> done(new Scanner(System.in).nextLine()))
            .flatMap(name -> {
                System.out.printf("Hello, %s!\n", name);
                return done(null);
            })
            .runT();
    }
}
