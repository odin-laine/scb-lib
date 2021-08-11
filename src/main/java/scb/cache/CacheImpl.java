package scb.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * Basic ConcurrentHashMap-backed implementation without any added complexity like:
 * - Eviction by Size/Weight/Time
 * - Soft/Weak references
 * - Refresh/Reload
 * - Basic method like size()
 * This implementation doesn't support Null keys and values
 * and will wrap any exception from the third-party function
 * in an Unsupported Operation
 *
 * @param <K> Key type of your cache
 * @param <V> Value type of your cache
 */
public class CacheImpl<K, V> implements Cache<K, V> {

    protected final Map<K, V> map = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Function<K, V> function;

    public CacheImpl(Function<K, V> function) {
        assert function != null;
        this.function = function;
    }

    @Override
    public V get(K key) {
        if (key == null) {
            throw new UnsupportedOperationException("Null keys are not supported");
        }
        lock.readLock().lock();
        V v = null;
        if ((v = this.map.get(key)) == null) {
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                if ((v = this.map.get(key)) == null) {
                    try {
                        v = function.apply(key);
                    } catch (Exception e) {
                        throw new UnsupportedOperationException("Function cannot map the provided key", e);
                    }
                    if (v == null) {
                        throw new UnsupportedOperationException("Null values are not supported");
                    } else {
                        this.map.put(key, v);
                    }
                }
            } finally {
                lock.readLock().lock();
                lock.writeLock().unlock();
            }
        }
        lock.readLock().unlock();
        return v;
    }
}