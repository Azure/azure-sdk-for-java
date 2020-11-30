/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

/*
 * Portions Copyright (c) Microsoft Corporation
 */

package com.azure.cosmos.implementation.guava25.collect;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;


import com.azure.cosmos.implementation.guava25.base.Objects;
import com.azure.cosmos.implementation.guava25.collect.Multisets.ImmutableEntry;
import com.azure.cosmos.implementation.guava25.primitives.Ints;
import java.util.Arrays;
import java.util.Collection;


/**
 * Implementation of {@link ImmutableMultiset} with zero or more elements.
 *
 * @author Jared Levy
 * @author Louis Wasserman
 */
@SuppressWarnings("serial") // uses writeReplace(), not default serialization
class RegularImmutableMultiset<E> extends ImmutableMultiset<E> {
  static final ImmutableMultiset<Object> EMPTY = create(ImmutableList.<Entry<Object>>of());

  @SuppressWarnings({"unchecked", "rawtypes"})
  static <E> ImmutableMultiset<E> create(Collection<? extends Entry<? extends E>> entries) {
    int distinct = entries.size();
    @SuppressWarnings({"unchecked", "rawtypes"})
    Multisets.ImmutableEntry<E>[] entryArray = new Multisets.ImmutableEntry[distinct];
    if (distinct == 0) {
      return new RegularImmutableMultiset<>(entryArray, null, 0, 0, ImmutableSet.of());
    }
    int tableSize = Hashing.closedTableSize(distinct, MAX_LOAD_FACTOR);
    int mask = tableSize - 1;
    @SuppressWarnings({"unchecked", "rawtypes"})
    Multisets.ImmutableEntry<E>[] hashTable = new Multisets.ImmutableEntry[tableSize];

    int index = 0;
    int hashCode = 0;
    long size = 0;
    for (Entry<? extends E> entry : entries) {
      E element = checkNotNull(entry.getElement());
      int count = entry.getCount();
      int hash = element.hashCode();
      int bucket = Hashing.smear(hash) & mask;
      Multisets.ImmutableEntry<E> bucketHead = hashTable[bucket];
      Multisets.ImmutableEntry<E> newEntry;
      if (bucketHead == null) {
        boolean canReuseEntry =
            entry instanceof Multisets.ImmutableEntry && !(entry instanceof NonTerminalEntry);
        newEntry =
            canReuseEntry
                ? (Multisets.ImmutableEntry<E>) entry
                : new Multisets.ImmutableEntry<E>(element, count);
      } else {
        newEntry = new NonTerminalEntry<E>(element, count, bucketHead);
      }
      hashCode += hash ^ count;
      entryArray[index++] = newEntry;
      hashTable[bucket] = newEntry;
      size += count;
    }

    return hashFloodingDetected(hashTable)
        ? JdkBackedImmutableMultiset.create(ImmutableList.asImmutableList(entryArray))
        : new RegularImmutableMultiset<E>(
            entryArray, hashTable, Ints.saturatedCast(size), hashCode, null);
  }

  private static boolean hashFloodingDetected(Multisets.ImmutableEntry<?>[] hashTable) {
    for (int i = 0; i < hashTable.length; i++) {
      int bucketLength = 0;
      for (Multisets.ImmutableEntry<?> entry = hashTable[i];
          entry != null;
          entry = entry.nextInBucket()) {
        bucketLength++;
        if (bucketLength > MAX_HASH_BUCKET_LENGTH) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Closed addressing tends to perform well even with high load factors. Being conservative here
   * ensures that the table is still likely to be relatively sparse (hence it misses fast) while
   * saving space.
   */
   static final double MAX_LOAD_FACTOR = 1.0;

  /**
   * Maximum allowed false positive probability of detecting a hash flooding attack given random
   * input.
   */
   static final double HASH_FLOODING_FPP = 0.001;

  /**
   * Maximum allowed length of a hash table bucket before falling back to a j.u.HashMap based
   * implementation. Experimentally determined.
   */
   static final int MAX_HASH_BUCKET_LENGTH = 9;

  private final transient Multisets.ImmutableEntry<E>[] entries;
  private final transient Multisets.ImmutableEntry<E>[] hashTable;
  private final transient int size;
  private final transient int hashCode;

  private transient ImmutableSet<E> elementSet;

  private RegularImmutableMultiset(
      ImmutableEntry<E>[] entries,
      ImmutableEntry<E>[] hashTable,
      int size,
      int hashCode,
      ImmutableSet<E> elementSet) {
    this.entries = entries;
    this.hashTable = hashTable;
    this.size = size;
    this.hashCode = hashCode;
    this.elementSet = elementSet;
  }

  private static final class NonTerminalEntry<E> extends Multisets.ImmutableEntry<E> {
    private final Multisets.ImmutableEntry<E> nextInBucket;

    NonTerminalEntry(E element, int count, ImmutableEntry<E> nextInBucket) {
      super(element, count);
      this.nextInBucket = nextInBucket;
    }

    @Override
    public ImmutableEntry<E> nextInBucket() {
      return nextInBucket;
    }
  }

  @Override
  boolean isPartialView() {
    return false;
  }

  @Override
  public int count(Object element) {
    Multisets.ImmutableEntry<E>[] hashTable = this.hashTable;
    if (element == null || hashTable == null) {
      return 0;
    }
    int hash = Hashing.smearedHash(element);
    int mask = hashTable.length - 1;
    for (Multisets.ImmutableEntry<E> entry = hashTable[hash & mask];
        entry != null;
        entry = entry.nextInBucket()) {
      if (Objects.equal(element, entry.getElement())) {
        return entry.getCount();
      }
    }
    return 0;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public ImmutableSet<E> elementSet() {
    ImmutableSet<E> result = elementSet;
    return (result == null) ? elementSet = new ElementSet<E>(Arrays.asList(entries), this) : result;
  }

  @Override
  Entry<E> getEntry(int index) {
    return entries[index];
  }

  @Override
  public int hashCode() {
    return hashCode;
  }
}
