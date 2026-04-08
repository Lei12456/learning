package com.yl.limiter;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class TokenBucketLimiterTest {

    @Test
    void shouldBeLimitedWhenNoTokenAvailable() {
        TokenBucketLimiter limiter = new TokenBucketLimiter();
        limiter.capacity = 2;
        limiter.rate = 1;
        limiter.tokens = new AtomicInteger(0);
        limiter.lastTime = System.currentTimeMillis();

        boolean limited = limiter.isLimited(1L, 1);

        assertTrue(limited);
    }

    @Test
    void shouldPassWhenEnoughTokenAvailable() {
        TokenBucketLimiter limiter = new TokenBucketLimiter();
        limiter.capacity = 2;
        limiter.rate = 1;
        limiter.tokens = new AtomicInteger(2);
        limiter.lastTime = System.currentTimeMillis();

        boolean limited = limiter.isLimited(2L, 1);

        assertFalse(limited);
        assertEquals(1, limiter.tokens.get());
    }
}
