package org.xusheng.ioliw.haxl;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public interface Result<A> {
    @AllArgsConstructor
    @Getter
    class Done<A> implements Result<A> {
        private final A value;
    }

    @AllArgsConstructor
    @Getter
    class Blocked<ID, R, A> implements Result<A> {
        private final List<BlockedRequest<ID, R>> requests;
        private final Fetch<A> fetch;
    }
}
