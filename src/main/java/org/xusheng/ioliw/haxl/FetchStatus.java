package org.xusheng.ioliw.haxl;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface FetchStatus<A> {
    class NotFetched<A> implements FetchStatus<A> {}

    @AllArgsConstructor
    @Getter
    class FetchSuccess<A> implements FetchStatus<A> {
        private final A value;
    }
}

