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
