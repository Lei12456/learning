package com.yl.hash;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HashMapChainingTest {

    @Test
    void shouldPutGetAndRemoveSuccessfully() {
        HashMapChaining map = new HashMapChaining();

        map.put(1, "A");
        map.put(5, "B");

        assertEquals("A", map.get(1));
        assertEquals("B", map.get(5));

        map.remove(1);
        assertNull(map.get(1));
        assertEquals("B", map.get(5));
    }

    @Test
    void shouldOverrideValueWhenKeyExists() {
        HashMapChaining map = new HashMapChaining();

        map.put(100, "OLD");
        map.put(100, "NEW");

        assertEquals("NEW", map.get(100));
    }

    @Test
    void shouldExpandWhenLoadFactorExceeded() {
        HashMapChaining map = new HashMapChaining();

        map.put(1, "A");
        map.put(2, "B");
        map.put(3, "C");
        map.put(4, "D");

        // 插入后应仍可正确读取，说明扩容和 rehash 正常
        assertEquals("A", map.get(1));
        assertEquals("B", map.get(2));
        assertEquals("C", map.get(3));
        assertEquals("D", map.get(4));
    }
}
