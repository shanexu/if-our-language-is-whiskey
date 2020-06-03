package org.xusheng.ioliw.haxl;

public class IORef<A> {

    private A value;

    private IORef(A value) {
        this.value = value;
    }

    private IORef() {

    }

    public static <A> Higher<IO.µ, IORef<A>> newIORef(A a) {
        return IO.I.ret(new IORef<>(a));
    }

    public static <A> Higher<IO.µ, A> readIORef(IORef<A> ref) {
        return IO.of(() -> ref.value);
    }

    public static <A> Higher<IO.µ, Void> writeIORef(IORef<A> ref, A a) {
        return IO.of(() -> {
            ref.value = a;
            return null;
        });
    }
}
