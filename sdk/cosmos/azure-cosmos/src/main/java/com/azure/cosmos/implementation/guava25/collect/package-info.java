/*
 * Copyright (C) 2007 The Guava Authors
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

/**
 * This package contains generic collection interfaces and implementations, and other utilities for
 * working with collections. It is a part of the open-source <a
 * href="http://github.com/google/guava">Guava</a> library.
 *
 * <h2>Collection Types</h2>
 *
 * <dl>
 *   <dt>{@link com.azure.cosmos.implementation.guava25.collect.BiMap}
 *   <dd>An extension of {@link java.util.Map} that guarantees the uniqueness of its values as well
 *       as that of its keys. This is sometimes called an "invertible map," since the restriction on
 *       values enables it to support an {@linkplain com.azure.cosmos.implementation.guava25.collect.BiMap#inverse inverse
 *       view} -- which is another instance of {@code BiMap}.
 *   <dt>{@link com.azure.cosmos.implementation.guava25.collect.Multiset}
 *   <dd>An extension of {@link java.util.Collection} that may contain duplicate values like a
 *       {@link java.util.List}, yet has order-independent equality like a {@link java.util.Set}.
 *       One typical use for a multiset is to represent a histogram.
 *   <dt>{@link com.azure.cosmos.implementation.guava25.collect.Multimap}
 *   <dd>A new type, which is similar to {@link java.util.Map}, but may contain multiple entries
 *       with the same key. Some behaviors of {@link com.azure.cosmos.implementation.guava25.collect.Multimap} are left
 *       unspecified and are provided only by the subtypes mentioned below.
 *   <dt>{@link com.azure.cosmos.implementation.guava25.collect.ListMultimap}
 *   <dd>An extension of {@link com.azure.cosmos.implementation.guava25.collect.Multimap} which permits duplicate entries,
 *       supports random access of values for a particular key, and has <i>partially order-dependent
 *       equality</i> as defined by {@link com.azure.cosmos.implementation.guava25.collect.ListMultimap#equals(Object)}.
 *       {@code ListMultimap} takes its name from the fact that the {@linkplain
 *       com.azure.cosmos.implementation.guava25.collect.ListMultimap#get collection of values} associated with a given
 *       key fulfills the {@link java.util.List} contract.
 *   <dt>{@link com.azure.cosmos.implementation.guava25.collect.SetMultimap}
 *   <dd>An extension of {@link com.azure.cosmos.implementation.guava25.collect.Multimap} which has order-independent
 *       equality and does not allow duplicate entries; that is, while a key may appear twice in a
 *       {@code SetMultimap}, each must map to a different value. {@code SetMultimap} takes its name
 *       from the fact that the {@linkplain com.azure.cosmos.implementation.guava25.collect.SetMultimap#get collection of
 *       values} associated with a given key fulfills the {@link java.util.Set} contract.
 *   <dt>{@link com.azure.cosmos.implementation.guava25.collect.SortedSetMultimap}
 *   <dd>An extension of {@link com.azure.cosmos.implementation.guava25.collect.SetMultimap} for which the {@linkplain
 *       com.azure.cosmos.implementation.guava25.collect.SortedSetMultimap#get collection values} associated with a given
 *       key is a {@link java.util.SortedSet}.
 *   <dt>{@link com.azure.cosmos.implementation.guava25.collect.Table}
 *   <dd>A new type, which is similar to {@link java.util.Map}, but which indexes its values by an
 *       ordered pair of keys, a row key and column key.
 *   <dt>{@link com.azure.cosmos.implementation.guava25.collect.ClassToInstanceMap}
 *   <dd>An extension of {@link java.util.Map} that associates a raw type with an instance of that
 *       type.
 * </dl>
 *
 * <h2>Collection Implementations</h2>
 *
 * <h3>of {@link java.util.List}</h3>
 *
 * <ul>
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ImmutableList}
 * </ul>
 *
 * <h3>of {@link java.util.Set}</h3>
 *
 * <ul>
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ImmutableSet}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ImmutableSortedSet}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ContiguousSet} (see {@code Range})
 * </ul>
 *
 * <h3>of {@link java.util.Map}</h3>
 *
 * <ul>
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ImmutableMap}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ImmutableSortedMap}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.MapMaker}
 * </ul>
 *
 * <h3>of {@link com.azure.cosmos.implementation.guava25.collect.BiMap}</h3>
 *
 * <ul>
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ImmutableBiMap}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.HashBiMap}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.EnumBiMap}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.EnumHashBiMap}
 * </ul>
 *
 * <h3>of {@link com.azure.cosmos.implementation.guava25.collect.Multiset}</h3>
 *
 * <ul>
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ImmutableMultiset}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.HashMultiset}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.LinkedHashMultiset}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.TreeMultiset}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.EnumMultiset}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ConcurrentHashMultiset}
 * </ul>
 *
 * <h3>of {@link com.azure.cosmos.implementation.guava25.collect.Multimap}</h3>
 *
 * <ul>
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ImmutableMultimap}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ImmutableListMultimap}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ImmutableSetMultimap}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ArrayListMultimap}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.HashMultimap}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.TreeMultimap}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.LinkedHashMultimap}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.LinkedListMultimap}
 * </ul>
 *
 * <h3>of {@link com.azure.cosmos.implementation.guava25.collect.Table}</h3>
 *
 * <ul>
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ImmutableTable}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ArrayTable}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.HashBasedTable}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.TreeBasedTable}
 * </ul>
 *
 * <h3>of {@link com.azure.cosmos.implementation.guava25.collect.ClassToInstanceMap}</h3>
 *
 * <ul>
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ImmutableClassToInstanceMap}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.MutableClassToInstanceMap}
 * </ul>
 *
 * <h2>Classes of static utility methods</h2>
 *
 * <ul>
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.Collections2}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.Iterators}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.Iterables}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.Lists}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.Maps}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.Queues}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.Sets}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.Multisets}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.Multimaps}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.Tables}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ObjectArrays}
 * </ul>
 *
 * <h2>Comparison</h2>
 *
 * <ul>
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.Ordering}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ComparisonChain}
 * </ul>
 *
 * <h2>Abstract implementations</h2>
 *
 * <ul>
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.AbstractIterator}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.AbstractSequentialIterator}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ImmutableCollection}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.UnmodifiableIterator}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.UnmodifiableListIterator}
 * </ul>
 *
 * <h2>Ranges</h2>
 *
 * <ul>
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.Range}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.RangeMap}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.DiscreteDomain}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ContiguousSet}
 * </ul>
 *
 * <h2>Other</h2>
 *
 * <ul>
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.Interner}, {@link com.azure.cosmos.implementation.guava25.collect.Interners}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.MapDifference}, {@link
 *       com.azure.cosmos.implementation.guava25.collect.SortedMapDifference}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.MinMaxPriorityQueue}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.PeekingIterator}
 * </ul>
 *
 * <h2>Forwarding collections</h2>
 *
 * <ul>
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ForwardingCollection}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ForwardingConcurrentMap}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ForwardingIterator}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ForwardingList}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ForwardingListIterator}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ForwardingListMultimap}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ForwardingMap}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ForwardingMapEntry}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ForwardingMultimap}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ForwardingMultiset}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ForwardingNavigableMap}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ForwardingNavigableSet}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ForwardingObject}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ForwardingQueue}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ForwardingSet}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ForwardingSetMultimap}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ForwardingSortedMap}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ForwardingSortedMultiset}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ForwardingSortedSet}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ForwardingSortedSetMultimap}
 *   <li>{@link com.azure.cosmos.implementation.guava25.collect.ForwardingTable}
 * </ul>
 */
/*
 * Portions Copyright (c) Microsoft Corporation
 */

package com.azure.cosmos.implementation.guava25.collect;
