package scb.cache;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class CacheImplTest {

    public final static class CacheImplWithSize<K, V> extends CacheImpl<K, V> {

        public CacheImplWithSize(Function<K, V> function) {
            super(function);
        }

        public int size() {
            return map.size();
        }
    }

    @Test
    public void nullFunction_WhenInstantiation_ThenException() {
        assertThrows(AssertionError.class, () -> new CacheImpl<>(null));
    }

    @Test
    public void addSomeDataToCache_WhenGetData_ThenIsEqualWithCacheElement() {
        Function<String, String> function = Function.identity();
        Cache<String, String> cache = new CacheImpl<>(function);
        assertEquals("1", cache.get("1"));
        assertEquals("2", cache.get("2"));
        assertEquals("3", cache.get("3"));
    }

    @Test
    public void addSomeIncorrectDataToCache_ThenException() {
        Function<String, String> function = s -> null;
        Cache<String, String> cache = new CacheImpl<>(function);
        assertThrows(UnsupportedOperationException.class, () -> cache.get(null));
        assertThrows(UnsupportedOperationException.class, () -> cache.get("3"));
    }

    @Test
    public void addUnsupportedFunctionDataToCache_ThenException() {
        Function<String, String> function = Mockito.mock(Function.class);
        Mockito.when(function.apply(Mockito.any(String.class))).thenThrow(NullPointerException.class);
        Cache<String, String> cache = new CacheImpl<>(function);
        assertThrows(UnsupportedOperationException.class, () -> cache.get("3"));
    }

    @Test
    public void runMultiThreadTask_WhenGetDataInConcurrentToCache_ThenNoDataLost() throws Exception {
        Function<Integer, String> function = Objects::toString;
        final int size = 50;
        final ExecutorService executorService = Executors.newFixedThreadPool(5);
        CacheImplWithSize<Integer, String> cache = new CacheImplWithSize<>(function);
        CountDownLatch countDownLatch = new CountDownLatch(size);
        try {
            IntStream.range(0, size).<Runnable>mapToObj(key -> () -> {
                cache.get(key);
                countDownLatch.countDown();
            }).forEach(executorService::submit);
            countDownLatch.await();
        } finally {
            executorService.shutdown();
        }
        assertEquals(cache.size(), size);
        IntStream.range(0, size).forEach(i -> assertEquals(String.valueOf(i), cache.get(i)));
    }

    @Test
    public void runMultiThreadTask_WhenGetIdenticalDataInConcurrentToCache_ThenUniqueFunctionCall() throws Exception {
        Function<Integer, String> function = Mockito.mock(Function.class);
        Mockito.when(function.apply(Mockito.any(Integer.class))).thenReturn("Unique");
        final int size = 50;
        final ExecutorService executorService = Executors.newFixedThreadPool(5);
        CacheImplWithSize<Integer, String> cache = new CacheImplWithSize<>(function);
        CountDownLatch countDownLatch = new CountDownLatch(size);
        try {
            IntStream.range(0, size).<Runnable>mapToObj(key -> () -> {
                cache.get(0);
                countDownLatch.countDown();
            }).forEach(executorService::submit);
            countDownLatch.await();
        } finally {
            executorService.shutdown();
        }
        assertEquals(cache.size(), 1);
        Mockito.verify(function, Mockito.only()).apply(Mockito.any(Integer.class));
    }
}
