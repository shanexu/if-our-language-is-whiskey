package org.xusheng.ioliw.haxl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jooq.lambda.Sneaky.runnable;

public class Main {
    public static void main(String[] args) {
        Map<String, List<String>> deps = ImmutableMap.of(
            "A", ImmutableList.of("B", "C"),
            "B", ImmutableList.of("D", "E"),
            "C", ImmutableList.of("E", "F")
        );

        IO.runIO(Fetch.runFetch(getGraph("A", deps), ds));
    }

    private static final DataSource<String, Node> ds = new DataSource<String, Node>() {

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
            runnable(() -> Thread.sleep(1000L)).run();
            System.out.println(String.format("<-- [%d] One Node %s", Thread.currentThread().getId(), id));
            return nodeDatabase.get(id);
        }

        @Override
        public Map<String, Node> batch(List<String> ids) {
            System.out.println(String.format("--> [%d] Batch Nodes %s", Thread.currentThread().getId(), ids));
            runnable(() -> Thread.sleep(2000L)).run();
            System.out.println(String.format("<-- [%d] Batch Nodes %s", Thread.currentThread().getId(), ids));
            return ids.stream().distinct().map(nodeDatabase::get).collect(Collectors.toMap(Node::getNodeName, Function.identity()));
        }
    };

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

    public static Fetch<Node> getNode(String id) {
        return Fetch.dataFetch(new Request<>(id));
    }

    public static Fetch<Node> getGraph(String id, Map<String, List<String>> deps) {
        List<String> ids = deps.get(id);
        if (ids == null) {
            return getNode(id);
        }
        return Fetch.mapM(i -> getGraph(i, deps), ids).bind(getNode(id));
    }
}
