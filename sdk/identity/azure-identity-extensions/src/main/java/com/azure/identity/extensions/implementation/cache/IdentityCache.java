package com.azure.identity.extensions.implementation.cache;

public interface IdentityCache<K, V> {

    void put(K key, V value);

    V get(K key);

    void remove(K key);

}
