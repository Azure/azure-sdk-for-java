// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import reactor.util.annotation.Nullable;

/**
 * A prefix tree that maps from the longest matching prefix to a value {@code V}.
 */
// copied from io.opentelemetry.javaagent.instrumentation.api.util.Trie
public interface Trie<V> {

    /**
     * Start building a trie.
     */
    static <V> Builder<V> newBuilder() {
        return new TrieImpl.BuilderImpl<>();
    }

    /**
     * Returns the value associated with the longest matched prefix, or null if there wasn't a match.
     * For example: for a trie containing an {@code ("abc", 10)} entry {@code trie.getOrNull("abcd")}
     * will return {@code 10}.
     */
    @Nullable
    default V getOrNull(CharSequence str) {
        return getOrDefault(str, null);
    }

    /**
     * Returns the value associated with the longest matched prefix, or the {@code defaultValue} if
     * there wasn't a match. For example: for a trie containing an {@code ("abc", 10)} entry {@code
     * trie.getOrDefault("abcd", -1)} will return {@code 10}.
     */
    V getOrDefault(CharSequence str, V defaultValue);

    interface Builder<V> {

        /**
         * Associate {@code value} with the string {@code str}.
         */
        Builder<V> put(CharSequence str, V value);

        Trie<V> build();
    }
}
