package org.xusheng.ioliw.haxl;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.xusheng.ioliw.haxl.FetchStatus.FetchSuccess;
import static org.xusheng.ioliw.haxl.FetchStatus.NotFetched;
import static org.xusheng.ioliw.haxl.Request.User;
import static org.xusheng.ioliw.haxl.Result.Blocked;
import static org.xusheng.ioliw.haxl.Result.Done;

public class Fetch<R, A> {
    private final IO<Result<A>> unFetch;

    public IO<Result<A>> getUnFetch() {
        return unFetch;
    }

    public Fetch(IO<Result<A>> unFetch) {
        this.unFetch = unFetch;
    }

    public static <R, A> Fetch<R, A> pure(A a) {
        return ret(a);
    }

    // <$>, fmap
    public static <R, A, B> Fetch<R, B> fmap(Function<A, B> f, Fetch<R, A> x) {
        return new Fetch<>(IO.fmap(r -> {
            if (r instanceof Done) {
                return new Done<>(f.apply(((Done<A>) r).getValue()));
            }
            if (r instanceof Blocked) {
                Blocked<R, A> blocked = (Blocked<R, A>) r;
                return new Blocked<>(blocked.getRequests(), fmap(f, blocked.getFetch()));
            }
            throw new RuntimeException("1");
        }, x.unFetch));
    }

    // <*>
    public static <R, A, B> Fetch<R, B> ap(Fetch<R, Function<A, B>> f, Fetch<R, A> x) {
        return new Fetch<>(IO.bind(f.unFetch, f_ -> IO.bind(x.unFetch, x_ -> {
            if (f_ instanceof Done && x_ instanceof Done) {
                Function<A, B> g = ((Done<Function<A, B>>) f_).getValue();
                A y = ((Done<A>) x_).getValue();
                return IO.ret(new Done<>(g.apply(y)));
            }
            if (f_ instanceof Done && x_ instanceof Blocked) {
                Function<A, B> g = ((Done<Function<A, B>>) f_).getValue();
                Blocked<R, A> blocked = (Blocked<R, A>) x_;
                List<BlockedRequest<R>> br = blocked.getRequests();
                Fetch<R, A> c = blocked.getFetch();
                return IO.ret(new Blocked<>(br, fmap(g, c)));
            }
            if (f_ instanceof Blocked && x_ instanceof Done) {
                Blocked<R, Function<A, B>> blocked = (Blocked<R, Function<A, B>>) f_;
                List<BlockedRequest<R>> br = blocked.getRequests();
                Fetch<R, Function<A, B>> c = blocked.getFetch();
                A y = ((Done<A>) x_).getValue();
                return IO.ret(new Blocked<>(br, ap(c, ret(y))));
            }
            if (f_ instanceof Blocked && x_ instanceof Blocked) {
                Blocked<R, Function<A, B>> blocked1 = (Blocked<R, Function<A, B>>) f_;
                Blocked<R, A> blocked2 = (Blocked<R, A>) x_;
                List<BlockedRequest<R>> br1 = blocked1.getRequests();
                Fetch<R, Function<A, B>> c = blocked1.getFetch();
                List<BlockedRequest<R>> br2 = blocked2.getRequests();
                Fetch<R, A> d = blocked2.getFetch();
                List<BlockedRequest<R>> br = ListUtils.concat(br1, br2);
                return IO.ret(new Blocked<>(br, ap(c, d)));
            }
            throw new RuntimeException("neither Blocked nor done");
        })));
    }

    // return
    public static <R, A> Fetch<R, A> ret(A a) {
        return new Fetch<>(IO.ret(new Done<>(a)));
    }

    // >>=
    public static <R, A, B> Fetch<R, B> bind(Fetch<R, A> m, Function<A, Fetch<R, B>> k) {
        return new Fetch<>(IO.bind(m.unFetch, r -> {
            if (r instanceof Done) {
                return k.apply(((Done<A>) r).getValue()).unFetch;
            }
            if (r instanceof Blocked) {
                Blocked<R, A> blocked = (Blocked<R, A>) r;
                List<BlockedRequest<R>> br = blocked.getRequests();
                Fetch<R, A> c = blocked.getFetch();
                return IO.ret(new Blocked<>(br, Fetch.bind(c, k)));
            }
            throw new RuntimeException("2");
        }));
    }

    // >>
    public static <R, A, B> Fetch<R, B> bind(Fetch<R, A> a, Fetch<R, B> b) {
        return bind(a, k -> b);
    }

    public static <R, A> Fetch<R, A> dataFetch(Request r) {
        return new Fetch<>(IO.bind(IORef.newIORef((FetchStatus<R>) new NotFetched<R>()), box -> {
            BlockedRequest<R> br = new BlockedRequest<>(r, box);
            Fetch<R, A> cont = new Fetch<>(IO.bind(IORef.readIORef(box), x -> {
                if (x instanceof FetchSuccess) {
                    return IO.ret(new Done<>(((FetchSuccess<A>) x).getValue()));
                }
                throw new RuntimeException("3");
            }));
            return IO.ret(new Blocked<>(ListUtils.of(br), cont));
        }));
    }

    // fetch remote resource
    public static <R> IO<Void> fetch(List<BlockedRequest<R>> brs, Function<Request, R> fetchOne, Function<List<Request>, Map<Long, R>> fetchBatch) {
        if (brs.isEmpty()) {
            return IO.ret(null);
        }

        if (brs.size() == 1) {
            BlockedRequest<R> first = brs.get(0);
            return IO.bind(IO.of(() -> fetchOne.apply(first.getRequest())), user -> IORef.writeIORef(first.getRef(), new FetchSuccess<>(user)));
        }

        return IO.bind(
            IO.of(() -> fetchBatch.apply(brs.stream().map(BlockedRequest::getRequest).collect(Collectors.toList()))),
            results -> IO.mapM_((BlockedRequest<R> br) -> {
                Request r = br.getRequest();
                IORef<FetchStatus<R>> ref = br.getRef();
                return IORef.writeIORef(ref, new FetchSuccess<>(results.get(r.getId())));
            }, brs)
        );
    }

    private static IO<User> getUser(Request request) {
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return IO.ret(new User(request.getId(), "user" + request.getId()));
    }

    private static IO<Map<Long, User>> getUsers(List<Request> requests) {
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return IO.ret(requests.stream().map(r -> new User(r.getId(), "user" + r.getId())).collect(Collectors.toMap(User::getId, Function.identity())));
    }

    public static <R, A> IO<A> runFetch(Fetch<R, A> f, Function<Request, R> fetchOne, Function<List<Request>, Map<Long, R>> fetchBatch) {
        return IO.bind(f.getUnFetch(), r -> {
            if (r instanceof Done) {
                return IO.ret(((Done<A>) r).getValue());
            }
            if (r instanceof Blocked) {
                Blocked<R, A> blocked = (Blocked<R, A>) r;
                List<BlockedRequest<R>> br = blocked.getRequests();
                Fetch<R, A> cont = blocked.getFetch();
                return IO.bind(fetch(br, fetchOne, fetchBatch), x -> runFetch(cont, fetchOne, fetchBatch));
            }
            throw new RuntimeException("4");
        });
    }

    public static <R, A, B> Fetch<R, List<B>> mapM(Function<A, Fetch<R, B>> f, List<A> l) {
        BiFunction<A, Fetch<R, List<B>>, Fetch<R, List<B>>> cons_f = (x, ys) -> liftA2(
            ListUtils::cons,
            f.apply(x),
            ys
        );
        return ListUtils.foldr(cons_f, pure(ListUtils.empty()), l);
    }

    public static <R, A, B, C> Fetch<R, C> liftA2(BiFunction<A, B, C> f, Fetch<R, A> a, Fetch<R, B> b) {
        Function<A, Function<B, C>> a2fbc = x -> (y -> f.apply(x, y));
        return ap(fmap(a2fbc, a), b);
    }
}
