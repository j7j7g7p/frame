package com.base.utils;

public class ThreadUtils {


    public static void doNothing(int delay) {
        try {
            Thread.sleep(delay);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static long id() {
        long curThreadId = Thread.currentThread().getId();
        return curThreadId;
    }
}
