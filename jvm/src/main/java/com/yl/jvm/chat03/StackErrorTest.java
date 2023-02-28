package com.yl.jvm.chat03;

public class StackErrorTest {
    private static int count = 1;

    public static void main(String[] args) {
        System.out.println(count);
        count ++;
        main(args);
    }
}
