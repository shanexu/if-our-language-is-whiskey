package org.xusheng.ioliw.haxl;

import java.util.function.Function;

public interface Functor<WITNESS> {
    <A,B> Higher<WITNESS,B> fmap(Function<? super A, ? extends B> f, Higher<WITNESS, A> fa);
}
