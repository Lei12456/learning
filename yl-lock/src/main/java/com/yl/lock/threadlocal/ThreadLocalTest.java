package com.yl.lock.threadlocal;

import com.google.common.collect.Lists;
import org.checkerframework.checker.units.qual.K;

import java.util.BitSet;
import java.util.List;

public class ThreadLocalTest {
    private List<String> messages = Lists.newArrayList();

    public static final ThreadLocal<ThreadLocalTest> holder = ThreadLocal.withInitial(ThreadLocalTest::new);

    public static void add(String message) {
        holder.get().messages.add(message);
    }

    public static List<String> clear() {
        List<String> messages = holder.get().messages;
        holder.remove();
        BitSet bitSet = new BitSet();
        bitSet.set(1);
        System.out.println("size: " + holder.get().messages.size());
        return messages;
    }

    public static void main(String[] args) {
        ThreadLocalTest.add("一枝花算不算浪漫");
        List<String> messages = ThreadLocalTest.clear();
        String s = messages.get(0);
        System.out.println(holder.get().messages);
        ThreadLocalTest.clear();
    }
}