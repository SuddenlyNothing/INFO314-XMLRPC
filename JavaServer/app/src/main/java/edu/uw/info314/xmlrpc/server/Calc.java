package edu.uw.info314.xmlrpc.server;

public class Calc {
    public static int add(Integer... args) {
        int result = 0;
        for (int arg : args) { result += arg; }
        return result;
    }
    public static int subtract(int lhs, int rhs) { return lhs - rhs; }
    public static int multiply(Integer... args) {
        int result = 1;
        for (int arg : args) { result *= arg; }
        return result;
    }
    public static int divide(int lhs, int rhs) { return lhs / rhs; }
    public static int modulo(int lhs, int rhs) { return lhs % rhs; }
}
