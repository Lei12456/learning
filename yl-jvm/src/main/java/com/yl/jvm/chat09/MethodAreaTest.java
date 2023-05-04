package com.yl.jvm.chat09;

public class MethodAreaTest {
    public static void main(String[] args) {
        System.out.println("start.....");
        try {
            Thread.sleep(10000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
