package org.xusheng.ioliw.haxl;

public class BlockedRequest<ID, DATA> {
    private final Request<ID> request;
    private final IORef<FetchStatus<DATA>> ref;

    public BlockedRequest(Request<ID> request, IORef<FetchStatus<DATA>> ref) {
        this.request = request;
        this.ref = ref;
    }

    public Request<ID> getRequest() {
        return request;
    }

    public IORef<FetchStatus<DATA>> getRef() {
        return ref;
    }
}
