package org.xusheng.ioliw.haxl2;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.xusheng.ioliw.haxl2.Trampoline.done;
import static org.xusheng.ioliw.haxl2.Trampoline.more;

public class IOUtils {

    public static IO<Void> printf(PrintStream s, String format, Object... args) {
        return IO.of(more(() -> {
            s.printf(format, args);
            return done(null);
        }));
    }

    public static IO<Void> printf(String format, Object... args) {
        return printf(System.out, format, args);
    }

    public static IO<String> readLine(InputStream s) {
        return IO.of(more(() -> done(new Scanner(s).nextLine())));
    }

    public static IO<String> readLine() {
        return readLine(System.in);
    }

    public static IO<Integer> readInt(InputStream s) {
        return readLine(s).map(Integer::parseInt);
    }

    public static IO<Integer> readInt() {
        return readInt(System.in);
    }

    public static IO<Long> readLong(InputStream s) {
        return readLine(s).map(Long::parseLong);
    }

    public static IO<Long> readLong() {
        return readLong(System.in);
    }

    public static IO<Float> readFloat(InputStream s) {
        return readLine(s).map(Float::parseFloat);
    }

    public static IO<Float> readFloat() {
        return readFloat(System.in);
    }

    public static IO<Double> readDouble(InputStream s) {
        return readLine(s).map(Double::parseDouble);
    }

    public static IO<Double> readDouble() {
        return readDouble(System.in);
    }

    public static void main(String[] args) {
//        printf("what's your name? ")
//            .bind(readLine())
//            .bind(name -> printf("Hello, %s\n", name))
//            .bind(printf("How old are you? "))
//            .bind(readInt())
//            .bind(age -> printf("You are %d years old.\n", age))
//            .runIO();
//        printf("What's your name? ")
//            .bind(readLine())
//            .bind(name -> printf("Hello, %s\n", name))
//            .bind(printf("How old are you? ")).runIO();

        more(() -> {
            System.out.println("What's your name?");
            return done(null);
        })
            .flatMap(x -> {
                System.out.println("get name");
                return done("shane");
            })
            .flatMap(name -> {
                System.out.printf("Hello, %s\n", name);
                return done(null);
            })
            .flatMap(x -> {
                System.out.println("How old are you?");
                return done(null);
            })
            .runT();
//        IO.of(more(() -> {
//            System.out.println("What's your name?");
//            return done(null);
//        })).bind(x -> new IO<>(done(new Scanner(System.in).nextLine())))
//            .bind(x -> {
//                System.out.println("How old are you?");
//                return new IO<>(done(null));
//            }).runIO();

    }
}
