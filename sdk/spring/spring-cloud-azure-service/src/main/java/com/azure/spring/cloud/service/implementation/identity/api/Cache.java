package com.azure.spring.cloud.service.implementation.identity.api;

public interface Cache<K, V> {

    void put(K key, V value);

    V get(K key);


}
