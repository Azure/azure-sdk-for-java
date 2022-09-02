/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import javax.annotation.Nullable;

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
