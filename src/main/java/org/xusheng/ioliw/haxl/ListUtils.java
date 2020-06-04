package org.xusheng.ioliw.haxl;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListUtils {

    public static <A, Z> Z foldr(BiFunction<A, Z, Z> k, Z z, List<A> l) {
        Function<List<A>, Z> go = new Function<List<A>, Z>() {
            @Override
            public Z apply(List<A> t) {
                if (t.isEmpty()) {
                    return z;
                }
                Tuple2<A, List<A>> tuple2 = ListUtils.splitAtHead(t);
                A y = tuple2.v1();
                List<A> ys = tuple2.v2();
                return k.apply(y, this.apply(ys));
            }
        };
        return go.apply(l);
    }

    public static <A> List<A> concat(List<A>... ls) {
        if (ls.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.stream(ls).filter(Objects::nonNull).flatMap(Collection::stream).collect(Collectors.toList());
    }

    public static <A> List<A> cons(A a, List<A> l) {
        return Stream.concat(Stream.of(a), l.stream()).collect(Collectors.toList());
    }


    public static <A> List<A> of(A... as) {
        return Arrays.stream(as).collect(Collectors.toList());
    }

    public static <A> List<A> empty() {
        return Collections.emptyList();
    }

    public static <A>Tuple2<A, List<A>> splitAtHead(List<A> l) {
        return Tuple.tuple(l.get(0), l.stream().skip(1).collect(Collectors.toList())) ;
    }
}
