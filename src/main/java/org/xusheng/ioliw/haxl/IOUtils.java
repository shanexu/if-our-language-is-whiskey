package org.xusheng.ioliw.haxl;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class IOUtils {
    public static Higher<IO.µ, Void> printf(PrintStream s, String format, Object... args) {
        return IO.of(() -> {
            s.printf(format, args);
            return null;
        });
    }

    public static Higher<IO.µ, Void> printf(String format, Object... args) {
        return printf(System.out, format, args);
    }

    public static Higher<IO.µ, String> readLine(InputStream s) {
        return IO.of(() -> new Scanner(s).nextLine());
    }

    public static Higher<IO.µ, String> readLine() {
        return readLine(System.in);
    }

    public static Higher<IO.µ, Integer> readInt(InputStream s) {
        return IO.I.fmap(Integer::parseInt, readLine(s));
    }

    public static Higher<IO.µ, Integer> readInt() {
        return readInt(System.in);
    }

    public static Higher<IO.µ, Long> readLong(InputStream s) {
        return IO.I.fmap(Long::parseLong, readLine(s));
    }

    public static Higher<IO.µ, Long> readLong() {
        return readLong(System.in);
    }

    public static Higher<IO.µ, Float> readFloat(InputStream s) {
        return IO.I.fmap(Float::parseFloat, readLine(s));
    }

    public static Higher<IO.µ, Float> readFloat() {
        return readFloat(System.in);
    }

    public static Higher<IO.µ, Double> readDouble(InputStream s) {
        return IO.I.fmap(Double::parseDouble, readLine(s));
    }

    public static Higher<IO.µ, Double> readDouble() {
        return readDouble(System.in);
    }

    public static void main(String[] args) {
        Higher<IO.µ, String> nameIO = IO.I.bind(printf("what's your name? "), readLine());
        Higher<IO.µ, Integer> ageIO = IO.I.bind(printf("How old are you? "), readInt());
        Higher<IO.µ, Void> mainIO = IO.I.bind(
            IO.I.bind(nameIO, name -> printf("Hello, %s!\n", name)),
            IO.I.bind(ageIO, age -> printf("You are %d years old.\n", age))
        );
        IO.runIO(mainIO);
    }
}
