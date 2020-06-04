package org.xusheng.ioliw.haxl;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.xusheng.ioliw.haxl.IOUtils.*;

public class IO<T> {
    private final Supplier<T> value;

    private IO(Supplier<T> value) {
        this.value = value;
    }

    public static <T> IO<T> ret(T t) {
        return pure(t);
    }

    public static <T> IO<T> pure(T t) {
        return new IO<>(() -> t);
    }

    public static <T> IO<T> of(Supplier<T> s) {
        return new IO<>(s);
    }

    public <B> IO<B> map(Function<T, B> f) {
        return fmap(f, this);
    }

    public static <A, B> IO<B> fmap(Function<A, B> f, IO<A> fa) {
        return new IO<>(() -> f.apply(fa.value.get()));
    }

    public static <A, B> IO<B> ap(IO<Function<A, B>> f, IO<A> x) {
        return new IO<>(() -> f.value.get().apply(x.value.get()));
    }

    public <B> IO<B> bind(Function<T, IO<B>> func) {
        return bind(this, func);
    }

    public static <A, B> IO<B> bind(IO<A> ma, Function<A, IO<B>> func) {
        return new IO<>(() -> func.apply(ma.value.get()).value.get());
    }

    public <B> IO<B> bind(IO<B> mb) {
        return bind(this, mb);
    }

    public static <A, B> IO<B> bind(IO<A> ma, IO<B> mb) {
        return bind(ma, a -> mb);
    }

    public static <A, B, C> IO<C> liftA2(BiFunction<A, B, C> f, IO<A> a, IO<B> b) {
        Function<A, Function<B, C>> a2fbc = x -> (y -> f.apply(x, y));
        return ap(fmap(a2fbc, a), b);
    }

    public static <A, B> IO<List<B>> mapM(Function<A, IO<B>> f, List<A> l) {
        BiFunction<A, IO<List<B>>, IO<List<B>>> cons_f = (x, ys) -> liftA2(
            ListUtils::cons,
            f.apply(x),
            ys
        );
        return ListUtils.foldr(cons_f, pure(ListUtils.empty()), l);
    }

    public static <A, B> IO<Void> mapM_(Function<A, IO<B>> f, List<A> l) {
        BiFunction<A, IO<Void>, IO<Void>> cons_f = (x, k) -> IO.bind(f.apply(x), k);
        return ListUtils.foldr(cons_f, pure(null), l);
    }

    public T runIO() {
        return runIO(this);
    }

    public static <T> T runIO(IO<T> m) {
        return m.value.get();
    }

    public static void main(String[] args) {
        printf("what's your name? ")
            .bind(readLine()).bind(name -> printf("Hello, %s\n", name))
            .bind(printf("How old are you? "))
            .bind(readInt())
            .bind(age -> printf("You are %d years old.\n", age))
            .runIO();
    }
}

