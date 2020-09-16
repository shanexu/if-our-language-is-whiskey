package org.xusheng.ioliw.haxl2;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.xusheng.ioliw.haxl.ListUtils;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.xusheng.ioliw.haxl2.Trampoline.done;
import static org.xusheng.ioliw.haxl2.Trampoline.more;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IO<T> {
    private final Trampoline<T> value;

    private IO(Supplier<T> value) {
        this(more(() -> done(value.get())));
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
        return new IO<>(fa.value.map(f));
    }

    public static <A, B> IO<B> ap(IO<Function<A, B>> m1, IO<A> m2) {
        return m1.bind(x1 -> m2.bind(x2 -> ret(x1.apply(x2))));
    }

    public <B> IO<B> bind(Function<T, IO<B>> func) {
        return bind(this, func);
    }

    public static <A, B> IO<B> bind(IO<A> ma, Function<A, IO<B>> func) {
        return new IO<>(ma.value.flatMap(x -> func.apply(x).value));
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
        return m.value.runT();
    }
}
