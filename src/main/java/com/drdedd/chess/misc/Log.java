package com.drdedd.chess.misc;

public class Log {
    public static void printTime(String task, long time) {
        String s1 = String.format("Time taken for %s : %s (%,3d ns)", task, MiscMethods.formatNanoseconds(time), time);
        String decor = "=".repeat(s1.length());
        System.out.printf("%s%n%s%n%s%n", decor, s1, decor);
    }

    public static void printTime(String TAG, String message, long time, int s) {
        printTime(TAG + "." + message + " Size: " + s, time);
    }

    public static void d(String TAG, String message) {
        System.out.printf("%s.%s%n", TAG, message);
    }

    public static void e(String TAG, String message, Throwable e) {
        System.err.printf("%s.%s%n", TAG, message);
        if (e != null) e.printStackTrace(System.err);
    }
}