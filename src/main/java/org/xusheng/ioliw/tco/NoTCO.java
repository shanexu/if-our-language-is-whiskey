package org.xusheng.ioliw.tco;

public class NoTCO {
    private static boolean even(int n) {
        if (n == 0) {
            return true;
        }
        return odd(n - 1);
    }

    private static boolean odd(int n) {
        if (n == 0) {
            return false;
        }
        return even(n - 1);
    }

    public static void main(String[] args) {
        System.out.println(even(100000));
    }
}
