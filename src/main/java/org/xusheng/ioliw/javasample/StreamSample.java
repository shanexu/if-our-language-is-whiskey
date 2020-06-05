package org.xusheng.ioliw.javasample;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StreamSample {
    public static void main(String[] args) {
        final List<String> friends =
            Arrays.asList("Brian", "Nate", "Neal", "Raju", "Sara", "Scott");

        friends.stream()
            .map(String::toUpperCase)
            .map(name -> name + " ")
            .forEach(System.out::println);

        final List<String> startsWithN = friends.stream()
            .filter(name -> name.startsWith("N")).collect(Collectors.toList());

        final Optional<String> aLongName = friends.stream()
            .reduce((name1, name2) -> name1.length() >= name2.length() ? name1 : name2);
        aLongName.ifPresent(name ->
            System.out.println(String.format("A longest name: %s", name)));

        friends.stream().reduce(0, (total, name) -> name.length() + total, Integer::sum);
    }
}
