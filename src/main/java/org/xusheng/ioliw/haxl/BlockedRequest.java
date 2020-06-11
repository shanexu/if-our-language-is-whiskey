package org.xusheng.ioliw.haxl;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BlockedRequest<ID, DATA> {
    private final Request<ID> request;
    private final IORef<FetchStatus<DATA>> ref;
}
