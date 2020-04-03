/*
 * Copyright (C) 2012 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Portions Copyright (c) Microsoft Corporation
 */

package com.azure.cosmos.implementation.guava25.collect;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;


import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


/**
 * CompactLinkedHashMap is an implementation of a Map with insertion or LRU iteration order,
 * maintained with a doubly linked list through the entries. All optional operations (put and
 * remove) are supported. Null keys and values are supported.
 *
 * <p>{@code containsKey(k)}, {@code put(k, v)} and {@code remove(k)} are all (expected and
 * amortized) constant time operations. Expected in the hashtable sense (depends on the hash
 * function doing a good job of distributing the elements to the buckets to a distribution not far
 * from uniform), and amortized since some operations can trigger a hash table resize.
 *
 * <p>As compared with {@link java.util.LinkedHashMap}, this structure places significantly reduced
 * load on the garbage collector by only using a constant number of internal objects.
 *
 * <p>This class should not be assumed to be universally superior to {@code
 * java.util.LinkedHashMap}. Generally speaking, this class reduces object allocation and memory
 * consumption at the price of moderately increased constant factors of CPU. Only use this class
 * when there is a specific reason to prioritize memory over CPU.
 *
 * @author Louis Wasserman
 */
class CompactLinkedHashMap<K, V> extends CompactHashMap<K, V> {
  // TODO(lowasser): implement removeEldestEntry so this can be used as a drop-in replacement

  /**
   * Creates an empty {@code CompactLinkedHashMap} instance.
   */
  public static <K, V> CompactLinkedHashMap<K, V> create() {
    return new CompactLinkedHashMap<>();
  }

  /**
   * Creates a {@code CompactLinkedHashMap} instance, with a high enough "initial capacity"
   * that it <i>should</i> hold {@code expectedSize} elements without growth.
   *
   * @param expectedSize the number of elements you expect to add to the returned set
   * @return a new, empty {@code CompactLinkedHashMap} with enough capacity to hold {@code
   *         expectedSize} elements without resizing
   * @throws IllegalArgumentException if {@code expectedSize} is negative
   */
  public static <K, V> CompactLinkedHashMap<K, V> createWithExpectedSize(int expectedSize) {
    return new CompactLinkedHashMap<>(expectedSize);
  }

  private static final int ENDPOINT = -2;

  /**
   * Contains the link pointers corresponding with the entries, in the range of [0, size()). The
   * high 32 bits of each long is the "prev" pointer, whereas the low 32 bits is the "succ" pointer
   * (pointing to the next entry in the linked list). The pointers in [size(), entries.length) are
   * all "null" (UNSET).
   *
   * A node with "prev" pointer equal to {@code ENDPOINT} is the first node in the linked list,
   * and a node with "next" pointer equal to {@code ENDPOINT} is the last node.
   */
   transient long[] links;

  /**
   * Pointer to the first node in the linked list, or {@code ENDPOINT} if there are no entries.
   */
  private transient int firstEntry;

  /**
   * Pointer to the last node in the linked list, or {@code ENDPOINT} if there are no entries.
   */
  private transient int lastEntry;

  private final boolean accessOrder;

  CompactLinkedHashMap() {
    this(DEFAULT_SIZE);
  }

  CompactLinkedHashMap(int expectedSize) {
    this(expectedSize, DEFAULT_LOAD_FACTOR, false);
  }

  CompactLinkedHashMap(int expectedSize, float loadFactor, boolean accessOrder) {
    super(expectedSize, loadFactor);
    this.accessOrder = accessOrder;
  }

  @Override
  void init(int expectedSize, float loadFactor) {
    super.init(expectedSize, loadFactor);
    firstEntry = ENDPOINT;
    lastEntry = ENDPOINT;
    links = new long[expectedSize];
    Arrays.fill(links, UNSET);
  }

  private int getPredecessor(int entry) {
    return (int) (links[entry] >>> 32);
  }

  @Override
  int getSuccessor(int entry) {
    return (int) links[entry];
  }

  private void setSuccessor(int entry, int succ) {
    long succMask = (~0L) >>> 32;
    links[entry] = (links[entry] & ~succMask) | (succ & succMask);
  }

  private void setPredecessor(int entry, int pred) {
    long predMask = (~0L) << 32;
    links[entry] = (links[entry] & ~predMask) | ((long) pred << 32);
  }

  private void setSucceeds(int pred, int succ) {
    if (pred == ENDPOINT) {
      firstEntry = succ;
    } else {
      setSuccessor(pred, succ);
    }
    if (succ == ENDPOINT) {
      lastEntry = pred;
    } else {
      setPredecessor(succ, pred);
    }
  }

  @Override
  void insertEntry(int entryIndex, K key, V value, int hash) {
    super.insertEntry(entryIndex, key, value, hash);
    setSucceeds(lastEntry, entryIndex);
    setSucceeds(entryIndex, ENDPOINT);
  }

  @Override
  void accessEntry(int index) {
    if (accessOrder) {
      // delete from previous position...
      setSucceeds(getPredecessor(index), getSuccessor(index));
      // ...and insert at the end.
      setSucceeds(lastEntry, index);
      setSucceeds(index, ENDPOINT);
      modCount++;
    }
  }

  @Override
  void moveLastEntry(int dstIndex) {
    int srcIndex = size() - 1;
    setSucceeds(getPredecessor(dstIndex), getSuccessor(dstIndex));
    if (dstIndex < srcIndex) {
      setSucceeds(getPredecessor(srcIndex), dstIndex);
      setSucceeds(dstIndex, getSuccessor(srcIndex));
    }
    super.moveLastEntry(dstIndex);
  }

  @Override
  void resizeEntries(int newCapacity) {
    super.resizeEntries(newCapacity);
    links = Arrays.copyOf(links, newCapacity);
  }

  @Override
  int firstEntryIndex() {
    return firstEntry;
  }

  @Override
  int adjustAfterRemove(int indexBeforeRemove, int indexRemoved) {
    return (indexBeforeRemove >= size()) ? indexRemoved : indexBeforeRemove;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public void forEach(BiConsumer<? super K, ? super V> action) {
    checkNotNull(action);
    for (int i = firstEntry; i != ENDPOINT; i = getSuccessor(i)) {
      action.accept((K) keys[i], (V) values[i]);
    }
  }

  @Override
  Set<Entry<K, V>> createEntrySet() {
    class EntrySetImpl extends EntrySetView {
      @Override
      public Spliterator<Entry<K, V>> spliterator() {
        return Spliterators.spliterator(this, Spliterator.ORDERED | Spliterator.DISTINCT);
      }
    }
    return new EntrySetImpl();
  }

  @Override
  Set<K> createKeySet() {
    class KeySetImpl extends KeySetView {
      @Override
      public Object[] toArray() {
        return ObjectArrays.toArrayImpl(this);
      }

      @Override
      public <T> T[] toArray(T[] a) {
        return ObjectArrays.toArrayImpl(this, a);
      }

      @Override
      public Spliterator<K> spliterator() {
        return Spliterators.spliterator(this, Spliterator.ORDERED | Spliterator.DISTINCT);
      }

      @SuppressWarnings({"unchecked", "rawtypes"})
      @Override
      public void forEach(Consumer<? super K> action) {
        checkNotNull(action);
        for (int i = firstEntry; i != ENDPOINT; i = getSuccessor(i)) {
          action.accept((K) keys[i]);
        }
      }
    }
    return new KeySetImpl();
  }

  @Override
  Collection<V> createValues() {
    class ValuesImpl extends ValuesView {
      @Override
      public Object[] toArray() {
        return ObjectArrays.toArrayImpl(this);
      }

      @Override
      public <T> T[] toArray(T[] a) {
        return ObjectArrays.toArrayImpl(this, a);
      }

      @Override
      public Spliterator<V> spliterator() {
        return Spliterators.spliterator(this, Spliterator.ORDERED);
      }

      @SuppressWarnings({"unchecked", "rawtypes"})
      @Override
      public void forEach(Consumer<? super V> action) {
        checkNotNull(action);
        for (int i = firstEntry; i != ENDPOINT; i = getSuccessor(i)) {
          action.accept((V) values[i]);
        }
      }
    }
    return new ValuesImpl();
  }

  @Override
  public void clear() {
    super.clear();
    this.firstEntry = ENDPOINT;
    this.lastEntry = ENDPOINT;
  }
}
