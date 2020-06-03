package org.xusheng.ioliw.haxl;

import java.util.function.Function;

public interface Applicative<WITNESS> {
    <A> Higher<WITNESS, A> pure(A a);
    <A, B> Higher<WITNESS, B> amap(Higher<WITNESS, Function<A, B>> f, Higher<WITNESS, A> fa);
}
