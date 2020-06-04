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

public class Fetch<A> {
    private final IO<Result<A>> unFetch;

    public IO<Result<A>> getUnFetch() {
        return unFetch;
    }

    public Fetch(IO<Result<A>> unFetch) {
        this.unFetch = unFetch;
    }

    public static <A> Fetch<A> pure(A a) {
        return ret(a);
    }

    // <$>, fmap
    public static <A, B> Fetch<B> fmap(Function<A, B> f, Fetch<A> x) {
        return new Fetch<>(IO.fmap(r -> {
            if (r instanceof Done) {
                return new Done<>(f.apply(((Done<A>) r).getValue()));
            }
            if (r instanceof Blocked) {
                Blocked<A> blocked = (Blocked<A>) r;
                return new Blocked<>(blocked.getRequests(), fmap(f, blocked.getFetch()));
            }
            throw new RuntimeException("1");
        }, x.unFetch));
    }

    // <*>
    public static <A, B> Fetch<B> ap(Fetch<Function<A, B>> f, Fetch<A> x) {
        return new Fetch<>(IO.bind(f.unFetch, f_ -> IO.bind(x.unFetch, x_ -> {
            if (f_ instanceof Done && x_ instanceof Done) {
                Function<A, B> g = ((Done<Function<A, B>>) f_).getValue();
                A y = ((Done<A>) x_).getValue();
                return IO.ret(new Done<>(g.apply(y)));
            }
            if (f_ instanceof Done && x_ instanceof Blocked) {
                Function<A, B> g = ((Done<Function<A, B>>) f_).getValue();
                Blocked<A> blocked = (Blocked<A>) x_;
                List<BlockedRequest> br = blocked.getRequests();
                Fetch<A> c = blocked.getFetch();
                return IO.ret(new Blocked<>(br, fmap(g, c)));
            }
            if (f_ instanceof Blocked && x_ instanceof Done) {
                Blocked<Function<A, B>> blocked = (Blocked<Function<A, B>>) f_;
                List<BlockedRequest> br = blocked.getRequests();
                Fetch<Function<A, B>> c = blocked.getFetch();
                A y = ((Done<A>) x_).getValue();
                return IO.ret(new Blocked<>(br, ap(c, ret(y))));
            }
            if (f_ instanceof Blocked && x_ instanceof Blocked) {
                Blocked<Function<A, B>> blocked1 = (Blocked<Function<A, B>>) f_;
                Blocked<A> blocked2 = (Blocked<A>) x_;
                List<BlockedRequest> br1 = blocked1.getRequests();
                Fetch<Function<A, B>> c = blocked1.getFetch();
                List<BlockedRequest> br2 = blocked2.getRequests();
                Fetch<A> d = blocked2.getFetch();
                List<BlockedRequest> br = ListUtils.concat(br1, br2);
                return IO.ret(new Blocked<>(br, ap(c, d)));
            }
            throw new RuntimeException("neither Blocked nor done");
        })));
    }

    // return
    public static <A> Fetch<A> ret(A a) {
        return new Fetch<>(IO.ret(new Done<>(a)));
    }

    // >>=
    public static <A, B> Fetch<B> bind(Fetch<A> m, Function<A, Fetch<B>> k) {
        return new Fetch<>(IO.bind(m.unFetch, r -> {
            if (r instanceof Done) {
                return k.apply(((Done<A>) r).getValue()).unFetch;
            }
            if (r instanceof Blocked) {
                Blocked<A> blocked = (Blocked<A>) r;
                List<BlockedRequest> br = blocked.getRequests();
                Fetch<A> c = blocked.getFetch();
                return IO.ret(new Blocked<>(br, Fetch.bind(c, k)));
            }
            throw new RuntimeException("2");
        }));
    }

    // >>
    public static <A, B> Fetch<B> bind(Fetch<A> a, Fetch<B> b) {
        return bind(a, k -> b);
    }

    public static <A> Fetch<A> dataFetch(Request r) {
        return new Fetch<>(IO.bind(IORef.newIORef((FetchStatus<A>) new NotFetched<A>()), box -> {
            BlockedRequest br = new BlockedRequest<>(r, box);
            Fetch<A> cont = new Fetch<>(IO.bind(IORef.readIORef(box), x -> {
                if (x instanceof FetchSuccess) {
                    return IO.ret(new Done<>(((FetchSuccess<A>) x).getValue()));
                }
                throw new RuntimeException("3");
            }));
            return IO.ret(new Blocked<>(ListUtils.of(br) , cont));
        }));
    }

    // fetch remote resource
    public static IO<Void> fetch(List<BlockedRequest> brs) {
        if (brs.isEmpty()) {
            return IO.ret(null);
        }

        if (brs.size() == 1) {
            BlockedRequest first = brs.get(0);
            return IO.bind(getUser(first.getRequest()), user -> IORef.writeIORef(first.getRef(), new FetchSuccess<>(user)));
        }

        return IO.bind(
            getUsers(brs.stream().map(BlockedRequest::getRequest).collect(Collectors.toList())),
            users -> IO.mapM_((BlockedRequest br) -> {
                Request r = br.getRequest();
                IORef ref = br.getRef();
                return IORef.writeIORef(ref, new FetchSuccess<>(users.get(r.getId())));
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

    public static <A> IO<A> runFetch(Fetch<A> f) {
        return IO.bind(f.getUnFetch(), r -> {
            if (r instanceof Done) {
                return IO.ret(((Done<A>) r).getValue());
            }
            if (r instanceof Blocked) {
                Blocked<A> blocked = (Blocked<A>) r;
                List<BlockedRequest> br = blocked.getRequests();
                Fetch<A> cont = blocked.getFetch();
                return IO.bind(fetch(br), x -> runFetch(cont));
            }
            throw new RuntimeException("4");
        });
    }

    public static <A, B> Fetch<List<B>> mapM(Function<A, Fetch<B>> f, List<A> l) {
        BiFunction<A, Fetch<List<B>>, Fetch<List<B>>> cons_f = (x, ys) -> liftA2(
            ListUtils::cons,
            f.apply(x),
            ys
        );
        return ListUtils.foldr(cons_f, pure(ListUtils.empty()), l);
    }

    public static <A, B, C> Fetch<C> liftA2(BiFunction<A, B, C> f, Fetch<A> a, Fetch<B> b) {
        Function<A, Function<B, C>> a2fbc = x -> (y -> f.apply(x, y));
        return ap(fmap(a2fbc, a), b);
    }
}
