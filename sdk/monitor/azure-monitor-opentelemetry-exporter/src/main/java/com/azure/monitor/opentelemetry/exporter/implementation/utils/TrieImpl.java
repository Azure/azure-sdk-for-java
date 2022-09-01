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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// copied from io.opentelemetry.javaagent.instrumentation.api.util.TrieImpl
public final class TrieImpl<V> implements Trie<V> {

  private final Node<V> root;

  private TrieImpl(Node<V> root) {
    this.root = root;
  }

  @Override
  public V getOrDefault(CharSequence str, V defaultValue) {
    Node<V> node = root;
    V lastMatchedValue = defaultValue;

    for (int i = 0; i < str.length(); ++i) {
      char c = str.charAt(i);
      Node<V> next = node.getNext(c);
      if (next == null) {
        return lastMatchedValue;
      }
      node = next;
      // next node matched, use its value if it's defined
      lastMatchedValue = next.value != null ? next.value : lastMatchedValue;
    }

    return lastMatchedValue;
  }

  static final class Node<V> {
    final char[] chars;
    final Node<V>[] children;
    final V value;

    Node(char[] chars, Node<V>[] children, V value) {
      this.chars = chars;
      this.children = children;
      this.value = value;
    }

    @Nullable
    Node<V> getNext(char c) {
      int index = Arrays.binarySearch(chars, c);
      if (index < 0) {
        return null;
      }
      return children[index];
    }
  }

  static final class BuilderImpl<V> implements Builder<V> {

    private final NodeBuilder<V> root = new NodeBuilder<>();

    @Override
    public Builder<V> put(CharSequence str, V value) {
      put(str, value, root, 0);
      return this;
    }

    private void put(CharSequence str, V value, NodeBuilder<V> node, int i) {
      if (str.length() == i) {
        node.value = value;
        return;
      }
      char c = str.charAt(i);
      NodeBuilder<V> next = node.children.computeIfAbsent(c, k -> new NodeBuilder<>());
      put(str, value, next, i + 1);
    }

    @Override
    public Trie<V> build() {
      return new TrieImpl<>(root.build());
    }
  }

  static final class NodeBuilder<V> {
    final Map<Character, NodeBuilder<V>> children = new HashMap<>();
    V value;

    Node<V> build() {
      int size = children.size();
      char[] chars = new char[size];
      Node<V>[] nodes = new Node[size];

      int i = 0;
      Iterator<Map.Entry<Character, NodeBuilder<V>>> it =
          this.children.entrySet().stream().sorted(Map.Entry.comparingByKey()).iterator();
      while (it.hasNext()) {
        Map.Entry<Character, NodeBuilder<V>> e = it.next();
        chars[i] = e.getKey();
        nodes[i++] = e.getValue().build();
      }

      return new Node<>(chars, nodes, value);
    }
  }
}
