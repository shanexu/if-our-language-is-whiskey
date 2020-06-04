package org.xusheng.ioliw.haxl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.xusheng.ioliw.haxl.Request.User;

public class main {
    public static void main(String[] args) {
        List<Request> l = ListUtils.of(new Request(1L), new Request(2L));
        Fetch<User, List<User>> batchFetch = Fetch.mapM(Fetch::dataFetch, l);
        Fetch<User, List<User>> seqFetch = Fetch.bind(
            Fetch.dataFetch(new Request(1L)),
            (User u1) -> Fetch.fmap((User u2) -> ListUtils.of(u1, u2), Fetch.dataFetch(new Request(u1.getId() + 1)))
        );

        System.out.printf("batchFetch %dms\n", measure(() -> IO.runIO(Fetch.runFetch(batchFetch, main::getUser, main::getUsers))) / 1000000);
        System.out.printf("seqFetch %dms\n", measure(() -> IO.runIO(Fetch.runFetch(seqFetch, main::getUser, main::getUsers))) / 1000000);
    }

    private static long measure(Callable<?> callable) {
        long start = System.nanoTime();
        try {
            System.out.println(callable.call());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return System.nanoTime() - start;
    }

    private static User getUser(Request request) {
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new User(request.getId(), "user" + request.getId());
    }

    private static Map<Long, User> getUsers(List<Request> requests) {
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return requests.stream().map(r -> new User(r.getId(), "user" + r.getId())).collect(Collectors.toMap(User::getId, Function.identity()));
    }
}
