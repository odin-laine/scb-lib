package scb.cache;

public interface Cache<K, V> {
    V get(K key);
}