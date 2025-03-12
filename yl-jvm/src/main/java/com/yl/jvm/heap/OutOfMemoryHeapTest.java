package com.yl.jvm.heap;

import java.nio.ByteBuffer;

public class OutOfMemoryHeapTest {

    public static void main(String[] args) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);

        byteBuffer.put((byte) 'a');
        byteBuffer.put((byte) 'b');

        byteBuffer.flip();

        while (byteBuffer.hasRemaining()) {
            System.out.println(byteBuffer.get());
        }

        byteBuffer = null;

        System.gc();

        System.out.println("GC completed!");
    }

}
