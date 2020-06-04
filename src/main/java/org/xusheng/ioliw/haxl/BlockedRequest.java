package org.xusheng.ioliw.haxl;

public class BlockedRequest<R> {
    private final Request request;
    private final IORef<FetchStatus<R>> ref;

    public BlockedRequest(Request request, IORef<FetchStatus<R>> ref) {
        this.request = request;
        this.ref = ref;
    }

    public Request getRequest() {
        return request;
    }

    public IORef<FetchStatus<R>> getRef() {
        return ref;
    }
}
