package com.yl.source.threadpool;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SerialExecutorTest {

    @Test
    void shouldExecuteTasksInSubmittedOrder() {
        Executor delegate = mock(Executor.class);
        SerialExecutor serialExecutor = new SerialExecutor(delegate);

        StringBuilder result = new StringBuilder();

        serialExecutor.execute(() -> result.append("A"));
        serialExecutor.execute(() -> result.append("B"));
        serialExecutor.execute(() -> result.append("C"));

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(delegate, times(1)).execute(captor.capture());

        // 驱动第一段任务执行，后续任务会由 scheduleNext 继续调度
        Runnable first = captor.getValue();
        first.run();

        verify(delegate, times(2)).execute(captor.capture());
        Runnable second = captor.getAllValues().get(1);
        second.run();

        verify(delegate, times(3)).execute(captor.capture());
        Runnable third = captor.getAllValues().get(2);
        third.run();

        assertEquals("ABC", result.toString());
    }
}
