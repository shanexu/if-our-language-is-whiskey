package org.xusheng.ioliw.haxl;

public interface FetchStatus<A> {
    class NotFetched<A> implements FetchStatus<A> {}
    class FetchSuccess<A> implements FetchStatus<A> {
        private final A value;

        public FetchSuccess(A value) {
            this.value = value;
        }

        public A getValue() {
            return value;
        }
    }
}

