package org.xusheng.ioliw.haxl;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class Request<ID> {
    private final ID id;
}
