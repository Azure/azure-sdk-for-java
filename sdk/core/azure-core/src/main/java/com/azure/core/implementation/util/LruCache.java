// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.util;

import com.azure.core.util.logging.ClientLogger;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Least recently used cache that will retain the most recently used key-values until a cache size limit is reached.
 * <p>
 * The least recently used cache will eject values that have been added or accessed least recently, offering the ability
 * to limit the size of the cache while still receiving the benefit of retaining the most recently used values. The
 * cache does have limitations where it is possible for a most commonly used value to be ejected due to a streak of less
 * commonly but more recently used values being added, which won't be covered in this implementation.
 */
public final class LruCache<Key, Value> {
    private static final ClientLogger LOGGER = new ClientLogger(LruCache.class);

    private final int maxCacheSize;

    // Maintains the key-value pairs in the cache.
    private final Map<Key, Node<Key, Value>> cache;

    private final ReentrantLock lock = new ReentrantLock();

    private Node<Key, Value> head;
    private Node<Key, Value> tail;

    /**
     * Creates an {@link LruCache} instance that caches the {@code maxCacheSize} most recently used values.
     *
     * @param maxCacheSize The number of values that can be cached at any time.
     * @throws IllegalArgumentException If {@code maxCacheSize} is less than or equal to 0.
     */
    public LruCache(int maxCacheSize) {
        if (maxCacheSize <= 0) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'cacheSizeLimit' cannot be less than or equal to 0."));
        }

        this.maxCacheSize = maxCacheSize;

        this.cache = new ConcurrentHashMap<>();
        this.head = null;
        this.tail = null;
    }

    /**
     * Gets the value associated to the {@code key} from the cache.
     * <p>
     * If the cache doesn't contain a value associated to the {@code key} null will be returned.
     *
     * @param key The key to get the associated value in the cache.
     * @return The value associated to the key or null if there is no value associated to the key.
     * @throws NullPointerException If {@code key} is null.
     */
    public Value get(Key key) {
        Objects.requireNonNull(key, "'key' cannot be null.");

        lock.lock();
        try {
            Node<Key, Value> node = getNode(key);

            return node == null ? null : node.value;
        } finally {
            lock.unlock();
        }
    }

    private Node<Key, Value> getNode(Key key) {
        Node<Key, Value> node = cache.get(key);

        updateLeastRecentlyUsed(node);

        return node;
    }

    private void updateLeastRecentlyUsed(Node<Key, Value> node) {
        // Only update the least recently used if the node isn't null and isn't the only node in the cache.
        if (node == null || cache.size() == 1) {
            return;
        }

        Node<Key, Value> previousNode = node.previous;
        Node<Key, Value> nextNode = node.next;

        // If the head node is null the node becomes the head node.
        if (head == null) {
            head = node;
        } else {
            // Otherwise, check if this node is the head node.
            if (head == node) {
                // Update the head to the nextNode.
                head = nextNode;

                // Head node doesn't have a previous node.
                head.previous = null;
            }
        }

        // If the tail node is not null, the current tail's next becomes this node.
        if (tail != null) {
            tail.next = node;

            // And this node's previous becomes the tail.
            node.previous = tail;
        }

        // And this node always becomes the new tail.
        tail = node;

        // And tail node's don't have a next.
        node.next = null;

        // Finally, unlink the node from where it was linked before.
        if (previousNode != null) {
            previousNode.next = nextNode;
        }

        if (nextNode != null) {
            nextNode.previous = previousNode;
        }
    }

    /**
     * Puts the key-value pair into the LRU cache.
     * <p>
     * If the key already exists in the cache the value associated with it will be overwritten and the key will become
     * the most recently used key.
     * <p>
     * If the key-value pair will increase the cache size above the allowed limit the least recently used key-value pair
     * will be ejected from the cache and the passed key-value pair will be added.
     *
     * @param key The key of the value being added to the cache.
     * @param value The value being added to the cache.
     * @return The previous value associated with the {@code key}, if no value was previously associated null.
     * @throws NullPointerException If {@code key} is null.
     */
    public Value put(Key key, Value value) {
        Objects.requireNonNull(key, "'key' cannot be null.");

        lock.lock();
        try {
            Node<Key, Value> node = getNode(key);

            // If a node with the key already exists update it.
            if (node != null) {
                Value previousValue = node.value;
                node.value = value;
                return previousValue;
            }

            // Otherwise, put a new node into the cache.
            putNode(key, value);

            return null;
        } finally {
            lock.unlock();
        }
    }

    private Node<Key, Value> putNode(Key key, Value value) {
        Node<Key, Value> node = new Node<>(key, value);
        cache.put(key, node);

        if (head == null) {
            head = node;
        }

        node.previous = tail;

        if (tail == null) {
            tail = node;
        }

        removeLeastRecentlyUsed();

        return node;
    }

    /**
     * Gets or computes the value for the {@code key}.
     * <p>
     * If the {@code key} exists in the cache the value associated with it is return and the key becomes the most
     * recently used key.
     * <p>
     * If the {@code key} doesn't exist, {@code compute} is called with the passed key and the key-value pair is added
     * into the cache. If the new key-value causes the cache to exceed its allowed size the least recently used
     * key-value pair is ejected from the cache.
     *
     * @param key The key to get the associated value in the cache.
     * @param compute The function that will supply the value for the key if the key doesn't exist in the cache.
     * @return Either the found value associated with the key in the cache or the value returned by {@code compute} when
     * supplied with the {@code key}.
     * @throws NullPointerException If {@code key} or {@code compute} is null.
     */
    public Value computeIfAbsent(Key key, Function<Key, Value> compute) {
        Objects.requireNonNull(key, "'key' cannot be null.");
        Objects.requireNonNull(compute, "'compute' cannot be null.");

        lock.lock();
        try {
            Node<Key, Value> node = getNode(key);
            if (node != null) {
                return node.value;
            }

            return putNode(key, compute.apply(key)).value;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes the key-value pair associated with the {@code key} from the cache.
     *
     * @param key The {@code key} to remove from the cache.
     * @return The value associated with the {@code key}, if the {@code key} doesn't exist in the cache null.
     * @throws NullPointerException If {@code key} is null.
     */
    public Value remove(Key key) {
        Objects.requireNonNull(key, "'key' cannot be null.");

        lock.lock();
        try {
            Node<Key, Value> node = cache.get(key);

            // If the node didn't exist in the cache return.
            if (node == null) {
                return null;
            }

            // If the node was the head node, update the head node to the next node.
            if (node == head) {
                head = node.next;
            }

            // If the node was the tail node, update the tail node to the previous node.
            if (node == tail) {
                tail = node.previous;
            }

            // Unlink the node from the list.
            if (node.next != null) {
                node.next.previous = node.previous;
            }

            if (node.previous != null) {
                node.previous.next = node.next;
            }

            return node.value;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Clears all values from the cache.
     */
    public void clear() {
        lock.lock();
        try {
            cache.clear();

            Node<Key, Value> toUnlink = head;
            do {
                if (toUnlink.previous != null) {
                    toUnlink.previous.next = null;
                }

                toUnlink.previous = null;
            } while ((toUnlink = toUnlink.next) != null);

            head = null;
            tail = null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Gets the current number of key-value pairs in the cache.
     *
     * @return The number of key-value pairs in the cache.
     */
    public int size() {
        return cache.size();
    }

    // Complains that head may produce a NullPointerException.
    // This will never occur if the cache size is greater than maxCacheSize as that will always be > 1.
    @SuppressWarnings("ConstantConditions")
    private void removeLeastRecentlyUsed() {
        while (cache.size() > maxCacheSize) {
            Node<Key, Value> toRemove = head;

            if (head.next != null) {
                head.next.previous = null;
            }

            head = head.next;

            cache.remove(toRemove.key);
        }
    }

    /*
     * Represents a doubly-linked list node.
     */
    private static final class Node<Key, Value> {
        // Node key is the only constant for its lifetime.
        private final Key key;

        Node<Key, Value> previous;
        Node<Key, Value> next;
        Value value;

        Node(Key key, Value value) {
            this.key = key;
            this.value = value;
        }
    }
}
