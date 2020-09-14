package org.xusheng.ioliw.tco;

import static org.xusheng.ioliw.tco.Trampoline.done;
import static org.xusheng.ioliw.tco.Trampoline.more;

public class UseTrampoline {
    public static Trampoline<Boolean> evenRec(int n) {
        if (n == 0) {
            return done(true);
        }
        return more(() -> oddRec(n - 1));
    }

    public static Trampoline<Boolean> oddRec(int n) {
        if (n == 0) {
            return done(false);
        }
        return more(() -> evenRec(n - 1));
    }

    public static void main(String[] args) {
        System.out.println(evenRec(100000).get());
    }
}
