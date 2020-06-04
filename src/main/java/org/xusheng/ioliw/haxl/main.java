package org.xusheng.ioliw.haxl;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jooq.lambda.Sneaky.runnable;

public class main {
    public static void main(String[] args) {
        List<Request<Long>> l = ListUtils.of(new Request<>(1L), new Request<>(2L));
        Fetch<Node, List<Node>> batchFetch = Fetch.mapM(Fetch::dataFetch, l);
        Fetch<Node, List<Node>> seqFetch = Fetch.bind(
            Fetch.dataFetch(new Request<>(1L)),
            (Node u1) -> Fetch.fmap((Node u2) -> ListUtils.of(u1, u2), Fetch.dataFetch(new Request(u1.getId() + 1)))
        );

        DataSource<Long, Node> ds = new DataSource<Long, Node>() {

             private final Map<Long, Node> users = ImmutableMap.of(
                 1L, new Node(1L, "user1"),
                 2L, new Node(2L, "user2"),
                 3L, new Node(3L, "user3")
             );

            @Override
            public Node fetch(Long id) {
                System.out.println(String.format("--> [%d] One Node %s", Thread.currentThread().getId(), id));
                runnable(() -> Thread.sleep(2000L)).run();
                System.out.println(String.format("<-- [%d] One Node %s", Thread.currentThread().getId(), id));
                return users.get(id);
            }

            @Override
            public Map<Long, Node> batch(List<Long> ids) {
                System.out.println(String.format("--> [%d] Batch Nodes %s", Thread.currentThread().getId(), ids));
                runnable(() -> Thread.sleep(3000L)).run();
                System.out.println(String.format("<-- [%d] Batch Nodes %s", Thread.currentThread().getId(), ids));
                return ids.stream().distinct().map(users::get).collect(Collectors.toMap(Node::getId, Function.identity()));
            }
        };

        System.out.printf("batchFetch %dms\n", measure(() -> IO.runIO(Fetch.runFetch(batchFetch, ds))) / 1000000);
        System.out.printf("seqFetch %dms\n", measure(() -> IO.runIO(Fetch.runFetch(seqFetch, ds))) / 1000000);
    }

    private static long measure(Runnable block) {
        long start = System.nanoTime();
        block.run();
        return System.nanoTime() - start;
    }

    public static class Node {
        private final Long id;
        private final String username;


        public Node(Long id, String username) {
            this.id = id;
            this.username = username;
        }

        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        @Override
        public String toString() {
            return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
        }
    }
}
