package com.yl.lock.baseThread;

import java.util.Map;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName ImmutableExample.java
 * @Description 测试 不可变集合
 * @createTime 2023年11月09日 11:00:00
 */
public class ImmutableExample {

    public static void main(String[] args) {
        //获取一个不可变的集合
        Map<String, Integer> unmodifiableMap = Map.of();
        unmodifiableMap.put("a",123);
    }
}
