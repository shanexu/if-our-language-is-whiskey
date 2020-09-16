package org.xusheng.ioliw.haxl2;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Trampoline<A> {

    default Trampoline<A> jump() {
        if (this instanceof Done) {
            return this;
        }
        if (this instanceof More) {
            return ((More<A>) this).k.get();
        }
        if (this instanceof FlatMap) {
            Trampoline<Object> a = ((FlatMap<Object, A>) this).sub;
            Function<Object, Trampoline<A>> f = ((FlatMap<Object, A>) this).k;
            if (a instanceof Done) {
                return f.apply(((Done<Object>) a).result);
            }
            if (a instanceof More) {
                return new FlatMap<>(((More<Object>) a).k.get(), f);
            }
            if (a instanceof FlatMap) {
                Trampoline<Object> b = ((FlatMap<Object, Object>) a).sub;
                Function<Object, Trampoline<Object>> g =
                    ((FlatMap<Object, Object>) a).k;
                return new FlatMap<>(b, x -> new FlatMap<>(g.apply(x), f));
            }
            throw new RuntimeException("unhandled sub type " + a.getClass());
        }
        throw new RuntimeException("unhandled type " + this.getClass());
    }

    default <B> Trampoline<B> flatMap(Function<A, Trampoline<B>> f) {
        return new FlatMap<>(this, f);
    }

    default <B> Trampoline<B> map(Function<A, B> f) {
        return new FlatMap<>(this, a -> done(f.apply(a)));
    }

    default A runT() {
        Trampoline<A> t = this;
        while (!(t instanceof Done)) {
            t = t.jump();
        }
        return ((Done<A>) t).result;
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    class Done<A> implements Trampoline<A> {
        private final A result;
    }

    static <T> Trampoline<T> done(T t) {
        return new Done<>(t);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    class More<A> implements Trampoline<A> {
        private final Supplier<Trampoline<A>> k;
    }

    static <T> Trampoline<T> more(Supplier<Trampoline<T>> k) {
        return new More<>(k);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    class FlatMap<B, A> implements Trampoline<A> {
        private final Trampoline<B> sub;
        private final Function<B, Trampoline<A>> k;
    }

    static <B, A> Trampoline<A> flatMap(Trampoline<B> sub, Function<B, Trampoline<A>> k) {
        return new FlatMap<>(sub, k);
    }

    static void main(String[] args) {
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
