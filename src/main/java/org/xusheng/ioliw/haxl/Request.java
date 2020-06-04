package org.xusheng.ioliw.haxl;

import java.util.Objects;

public class Request<ID> {

    private final ID id;

    public ID getId() {
        return id;
    }

    public Request(ID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request<?> request = (Request<?>) o;
        return id.equals(request.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
