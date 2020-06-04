package org.xusheng.ioliw.haxl;

public class Request<ID> {

    private final ID id;

    public ID getId() {
        return id;
    }

    public Request(ID id) {
        this.id = id;
    }

}
