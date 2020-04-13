package com.martin.utils;

public class checkUtils {

    public static void checkArgument(int argument, String e) {
        if (argument <= 0) {
            throw new RuntimeException(e);
        }
    }
}
