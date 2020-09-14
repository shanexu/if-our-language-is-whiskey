package org.xusheng.ioliw.tco;

import static org.xusheng.ioliw.tco.Trampoline.done;
import static org.xusheng.ioliw.tco.Trampoline.more;

public class UseTrampoline2 {
    public static long fibonacciNaive(long n) {
        if (n == 0) {
            return 0;
        }
        if (n == 1) {
            return 1;
        }
        return fibonacciNaive(n - 1) + fibonacciNaive(n - 2);
    }

    public static Trampoline<Long> fibonacci(long step, long a, long b) {
        if (step == 0) {
            return done(a);
        }
        return more(() -> fibonacci(step - 1, b, a + b));
    }

    public static void main(String[] args) {
        System.out.println(fibonacci(45, 0, 1).get());
        System.out.println(fibonacciNaive(45));
    }
}
