// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TrieTest {
    @Test
    void shouldMatchExactString() {
        Trie<Integer> trie =
            Trie.<Integer>newBuilder().put("abc", 0).put("abcd", 10).put("abcde", 20).build();

        assertThat(trie.getOrNull("ab")).isNull();
        assertEquals(0, trie.getOrNull("abc"));
        assertEquals(10, trie.getOrNull("abcd"));
        assertEquals(20, trie.getOrNull("abcde"));
    }

    @Test
    void shouldReturnLastMatchedValue() {
        Trie<Integer> trie =
            Trie.<Integer>newBuilder().put("abc", 0).put("abcde", 10).put("abcdfgh", 20).build();

        assertThat(trie.getOrNull("ababababa")).isNull();
        assertEquals(0, trie.getOrNull("abcd"));
        assertEquals(10, trie.getOrNull("abcdefgh"));
        assertEquals(20, trie.getOrNull("abcdfghjkl"));
    }

    @Test
    void shouldOverwritePreviousValue() {
        Trie<Integer> trie = Trie.<Integer>newBuilder().put("abc", 0).put("abc", 12).build();

        assertEquals(12, trie.getOrNull("abc"));
    }

    @Test
    void shouldReturnDefaultValueWhenNotMatched() {
        Trie<Integer> trie = Trie.<Integer>newBuilder().put("abc", 42).build();

        assertEquals(-1, trie.getOrDefault("acdc", -1));
    }
}
