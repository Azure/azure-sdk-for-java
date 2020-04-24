/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/*
 * Portions Copyright (c) Microsoft Corporation
 */

package com.azure.cosmos.implementation.guava25.collect;

import static com.azure.cosmos.implementation.guava25.collect.BoundType.CLOSED;
import static com.azure.cosmos.implementation.guava25.collect.BoundType.OPEN;

import com.azure.cosmos.implementation.guava25.collect.Multiset.Entry;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.SortedSet;


/**
 * Provides static utility methods for creating and working with {@link SortedMultiset} instances.
 *
 * @author Louis Wasserman
 */
final class SortedMultisets {
  private SortedMultisets() {}

  /** A skeleton implementation for {@link SortedMultiset#elementSet}. */
  static class ElementSet<E> extends Multisets.ElementSet<E> implements SortedSet<E> {
    private final SortedMultiset<E> multiset;

    ElementSet(SortedMultiset<E> multiset) {
      this.multiset = multiset;
    }

    @Override
    final SortedMultiset<E> multiset() {
      return multiset;
    }

    @Override
    public Iterator<E> iterator() {
      return Multisets.elementIterator(multiset().entrySet().iterator());
    }

    @Override
    public Comparator<? super E> comparator() {
      return multiset().comparator();
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
      return multiset().subMultiset(fromElement, CLOSED, toElement, OPEN).elementSet();
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
      return multiset().headMultiset(toElement, OPEN).elementSet();
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
      return multiset().tailMultiset(fromElement, CLOSED).elementSet();
    }

    @Override
    public E first() {
      return getElementOrThrow(multiset().firstEntry());
    }

    @Override
    public E last() {
      return getElementOrThrow(multiset().lastEntry());
    }
  }

  /** A skeleton navigable implementation for {@link SortedMultiset#elementSet}. */
  static class NavigableElementSet<E> extends ElementSet<E> implements NavigableSet<E> {
    NavigableElementSet(SortedMultiset<E> multiset) {
      super(multiset);
    }

    @Override
    public E lower(E e) {
      return getElementOrNull(multiset().headMultiset(e, OPEN).lastEntry());
    }

    @Override
    public E floor(E e) {
      return getElementOrNull(multiset().headMultiset(e, CLOSED).lastEntry());
    }

    @Override
    public E ceiling(E e) {
      return getElementOrNull(multiset().tailMultiset(e, CLOSED).firstEntry());
    }

    @Override
    public E higher(E e) {
      return getElementOrNull(multiset().tailMultiset(e, OPEN).firstEntry());
    }

    @Override
    public NavigableSet<E> descendingSet() {
      return new NavigableElementSet<E>(multiset().descendingMultiset());
    }

    @Override
    public Iterator<E> descendingIterator() {
      return descendingSet().iterator();
    }

    @Override
    public E pollFirst() {
      return getElementOrNull(multiset().pollFirstEntry());
    }

    @Override
    public E pollLast() {
      return getElementOrNull(multiset().pollLastEntry());
    }

    @Override
    public NavigableSet<E> subSet(
        E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
      return new NavigableElementSet<E>(
          multiset()
              .subMultiset(
                  fromElement, BoundType.forBoolean(fromInclusive),
                  toElement, BoundType.forBoolean(toInclusive)));
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
      return new NavigableElementSet<E>(
          multiset().headMultiset(toElement, BoundType.forBoolean(inclusive)));
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
      return new NavigableElementSet<E>(
          multiset().tailMultiset(fromElement, BoundType.forBoolean(inclusive)));
    }
  }

  private static <E> E getElementOrThrow(Entry<E> entry) {
    if (entry == null) {
      throw new NoSuchElementException();
    }
    return entry.getElement();
  }

  private static <E> E getElementOrNull(Entry<E> entry) {
    return (entry == null) ? null : entry.getElement();
  }
}
