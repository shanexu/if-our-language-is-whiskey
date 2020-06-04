package org.xusheng.ioliw.haxl;

public class IORef<A> {

    private A value;

    private IORef(A value) {
        this.value = value;
    }

    private IORef() {

    }

    public static <A> IO<IORef<A>> newIORef(A a) {
        return IO.ret(new IORef<>(a));
    }

    public static <A> IO<A> readIORef(IORef<A> ref) {
        return IO.of(() -> ref.value);
    }

    public static <A> IO<Void> writeIORef(IORef<A> ref, A a) {
        return IO.of(() -> {
            ref.value = a;
            return null;
        });
    }
}
