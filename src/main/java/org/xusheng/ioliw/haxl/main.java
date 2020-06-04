package org.xusheng.ioliw.haxl;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jooq.lambda.Sneaky.runnable;

public class main {
    public static void main(String[] args) {
        List<Request<String>> l = ListUtils.of(new Request<>("A"), new Request<>("B"));
        Fetch<List<Node>> batchFetch = Fetch.mapM(Fetch::dataFetch, l);
        Fetch<List<Node>> seqFetch = Fetch.bind(
            Fetch.dataFetch(new Request<>("A")),
            (Node u1) -> Fetch.fmap((Node u2) -> ListUtils.of(u1, u2), Fetch.dataFetch(new Request<>("B")))
        );

        DataSource<String, Node> ds = new DataSource<String, Node>() {

             private final Map<String, Node> nodeDatabase =
                 ImmutableMap.<String, Node>builder()
                     .put("A", new Node("A"))
                     .put("B", new Node("B"))
                     .put("C", new Node("C"))
                     .put("D", new Node("D"))
                     .put("E", new Node("E"))
                     .put("F", new Node("F"))
                     .build();

            @Override
            public Node fetch(String id) {
                System.out.println(String.format("--> [%d] One Node %s", Thread.currentThread().getId(), id));
                runnable(() -> Thread.sleep(2000L)).run();
                System.out.println(String.format("<-- [%d] One Node %s", Thread.currentThread().getId(), id));
                return nodeDatabase.get(id);
            }

            @Override
            public Map<String, Node> batch(List<String> ids) {
                System.out.println(String.format("--> [%d] Batch Nodes %s", Thread.currentThread().getId(), ids));
                runnable(() -> Thread.sleep(3000L)).run();
                System.out.println(String.format("<-- [%d] Batch Nodes %s", Thread.currentThread().getId(), ids));
                return ids.stream().distinct().map(nodeDatabase::get).collect(Collectors.toMap(Node::getNodeName, Function.identity()));
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
        private final String nodeName;

        public Node(String nodeName) {
            this.nodeName = nodeName;
        }

        public String getNodeName() {
            return nodeName;
        }

        @Override
        public String toString() {
            return "Node{" +
                "nodeName='" + nodeName + '\'' +
                '}';
        }
    }


}
