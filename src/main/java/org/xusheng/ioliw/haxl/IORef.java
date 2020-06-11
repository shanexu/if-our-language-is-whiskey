package org.xusheng.ioliw.haxl;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class IORef<A> {
    private A value;

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
