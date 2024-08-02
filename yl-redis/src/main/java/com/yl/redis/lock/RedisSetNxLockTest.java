package com.yl.redis.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
class RedisSetNxLockTest {

    @Autowired
    private RedisSetNxLock redisSetNxLock;

    @MockBean
    private RedisTemplate<String, String> redisTemplate;

    @MockBean
    private ValueOperations<String, String> valueOperations;

    private static final String KEY = "testLockKey";
    private static final String VALUE = "testLockValue";

    @BeforeEach
    public void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void lockWhenKeyNotPresentShouldReturnTrue() {
        when(valueOperations.setIfAbsent(eq(KEY), eq(VALUE), eq(1000L), eq(TimeUnit.MILLISECONDS))).thenReturn(true);

        boolean result = redisSetNxLock.lock(KEY, VALUE, 1000);

        assertTrue(result);
        verify(valueOperations).setIfAbsent(eq(KEY), eq(VALUE), eq(1000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void lockWhenKeyPresentShouldReturnFalse() {
        when(valueOperations.setIfAbsent(eq(KEY), eq(VALUE), eq(1000L), eq(TimeUnit.MILLISECONDS))).thenReturn(false);

        boolean result = redisSetNxLock.lock(KEY, VALUE, 1000);

        assertFalse(result);
        verify(valueOperations).setIfAbsent(eq(KEY), eq(VALUE), eq(1000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void unlockWithCorrectValueShouldReturnTrue() {
        when(valueOperations.get(eq(KEY))).thenReturn(VALUE);
        doNothing().when(redisTemplate).delete(eq(KEY));

        boolean result = redisSetNxLock.unlock(KEY, VALUE);

        assertTrue(result);
        verify(valueOperations).get(eq(KEY));
        verify(redisTemplate).delete(eq(KEY));
    }

    @Test
    void unlockWithIncorrectValueShouldReturnFalse() {
        when(valueOperations.get(eq(KEY))).thenReturn("anotherValue");

        boolean result = redisSetNxLock.unlock(KEY, VALUE);

        assertFalse(result);
        verify(valueOperations).get(eq(KEY));
        verify(redisTemplate, never()).delete(anyString());
    }
}
