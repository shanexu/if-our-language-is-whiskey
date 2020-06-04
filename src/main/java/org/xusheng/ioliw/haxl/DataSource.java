package org.xusheng.ioliw.haxl;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface DataSource<ID, DATA> {
    DATA fetch(ID id);
    default Map<ID, DATA> batch(List<ID> ids) {
        return ids.stream().distinct().map(id -> Tuple.tuple(id, fetch(id))).collect(Collectors.toMap(Tuple2::v1, Tuple2::v2));
    }
}
