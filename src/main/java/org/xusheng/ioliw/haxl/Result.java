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

    class Blocked<R, A> implements Result<A> {
        private List<BlockedRequest<R>> requests;
        private Fetch<R, A> fetch;

        public Blocked(List<BlockedRequest<R>> requests, Fetch<R, A> fetch) {
            this.requests = requests;
            this.fetch = fetch;
        }

        public List<BlockedRequest<R>> getRequests() {
            return requests;
        }

        public Fetch<R, A> getFetch() {
            return fetch;
        }
    }
}
