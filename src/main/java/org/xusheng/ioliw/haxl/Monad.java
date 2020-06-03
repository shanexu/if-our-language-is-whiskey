package org.xusheng.ioliw.haxl;

import java.util.function.Function;

public interface Monad<WITNESS> extends Applicative<WITNESS> {
    default <A> Higher<WITNESS, A> ret(A a) {
        return pure(a);
    }
    <A, B> Higher<WITNESS, B> bind(Higher<WITNESS, A> ma, Function<A, Higher<WITNESS, B>> f);
    default <A, B> Higher<WITNESS, B> bind(Higher<WITNESS, A> ma, Higher<WITNESS, B> mb) {
        return bind(ma, a -> mb);
    }
}
