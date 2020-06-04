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

    class Blocked<A> implements Result<A> {
        private List<BlockedRequest> requests;
        private Fetch<A> fetch;

        public Blocked(List<BlockedRequest> requests, Fetch<A> fetch) {
            this.requests = requests;
            this.fetch = fetch;
        }

        public List<BlockedRequest> getRequests() {
            return requests;
        }

        public Fetch<A> getFetch() {
            return fetch;
        }
    }
}
