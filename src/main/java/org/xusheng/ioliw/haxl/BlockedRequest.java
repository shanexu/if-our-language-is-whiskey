package org.xusheng.ioliw.haxl;

public class BlockedRequest<A> {
    private final Request request;
    private final IORef<FetchStatus<A>> ref;

    public BlockedRequest(Request request, IORef<FetchStatus<A>> ref) {
        this.request = request;
        this.ref = ref;
    }

    public Request getRequest() {
        return request;
    }

    public IORef<FetchStatus<A>> getRef() {
        return ref;
    }
}
