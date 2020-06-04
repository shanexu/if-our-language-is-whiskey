package org.xusheng.ioliw.haxl;

import java.util.List;

public interface Result<A> {
    class Done<A> implements Result<A> {
        final private A value;

        public Done(A value) {
            this.value = value;
        }

        public A getValue() {
            return value;
        }
    }

    class Blocked<ID, R, A> implements Result<A> {
        private final List<BlockedRequest<ID, R>> requests;
        private final Fetch<R, A> fetch;

        public Blocked(List<BlockedRequest<ID, R>> requests, Fetch<R, A> fetch) {
            this.requests = requests;
            this.fetch = fetch;
        }

        public List<BlockedRequest<ID, R>> getRequests() {
            return requests;
        }

        public Fetch<R, A> getFetch() {
            return fetch;
        }
    }
}
