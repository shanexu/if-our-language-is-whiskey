package org.xusheng.ioliw.sampels;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.Data;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReduceSamples {

    public static class test1 {
        public <T> void test(List<T> listOfT) {
            T result = null;
            for (int i = 0; i < listOfT.size(); i++) {
                T t = listOfT.get(i);
                if (i == 0) {
                    result = t;
                    continue;
                }
                result = accumulator(t, result);
            }

            IntStream.of(1, 2, 3).sum();
        }

        public <T> T accumulator(T t1, T t2) {
            return null;
        }
    }

    public static class test2 {

        public <T> void test(List<T> listOfT, T identity) {
            T result = identity;
            for (T t : listOfT) {
                result = accumulator(t, result);
            }
        }

        public <T> T accumulator(T t1, T t2) {
            return null;
        }
    }

    public static class test3 {
        public <T, U> void test(U identity, List<T> listOfT) {
            U result = identity;
            for (T t : listOfT) {
                U partialResult = accumulator(result, t);
                result = combiner(result, partialResult);
            }
        }

        public <T, U> U accumulator(U u, T t) {
            return null;
        }

        public <U> U combiner(U u1, U u2) {
            return null;
        }

        @Data
        public static class User {
            private Long id;
            private String username;
        }

        public void sample() {
            List<User> users = new LinkedList<>();

            Map<Long, User> userMap = Maps.newHashMap();
            for (User user : users) {
                userMap.put(user.getId(), user);
            }

            userMap = users.stream().reduce(
                Collections.emptyMap(),
                (Map<Long, User> acc, User user) -> {
                   acc.put(user.getId(), user);
                   return acc;
                }, (acc1, acc2) -> {
                    acc1.putAll(acc2);
                    return acc1;
                });

            userMap = users.stream().reduce(
                Collections.emptyMap(),
                (Map<Long, User> acc, User user) -> {
                    HashMap<Long, User> newAcc = Maps.newHashMap(acc);
                    newAcc.put(user.getId(), user);
                    return newAcc;
                }, (acc1, acc2) -> {
                    HashMap<Long, User> newAcc = Maps.newHashMap(acc1);
                    newAcc.putAll(acc2);
                    return newAcc;
                });

            userMap = users.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        }
    }

}
