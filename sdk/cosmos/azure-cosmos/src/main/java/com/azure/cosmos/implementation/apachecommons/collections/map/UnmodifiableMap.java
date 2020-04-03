/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

package com.azure.cosmos.implementation.apachecommons.collections.map;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.azure.cosmos.implementation.apachecommons.collections.IterableMap;
import com.azure.cosmos.implementation.apachecommons.collections.MapIterator;
import com.azure.cosmos.implementation.apachecommons.collections.UnmodifiableCollection;
import com.azure.cosmos.implementation.apachecommons.collections.iterators.EntrySetMapIterator;
import com.azure.cosmos.implementation.apachecommons.collections.iterators.UnmodifiableMapIterator;
import com.azure.cosmos.implementation.apachecommons.collections.list.Unmodifiable;
import com.azure.cosmos.implementation.apachecommons.collections.set.UnmodifiableSet;

public final class UnmodifiableMap<K, V>
    extends AbstractMapDecorator<K, V>
    implements Unmodifiable, Serializable {
    /** Serialization version */
    private static final long serialVersionUID = -1132159183705203881L;

    /**
     * Factory method to create an unmodifiable map.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param map  the map to decorate, must not be null
     * @return a new unmodifiable map
     * @throws NullPointerException if map is null
     */
    public static <K, V> Map<K, V> unmodifiableMap(final Map<? extends K, ? extends V> map) {
        if (map instanceof Unmodifiable) {
            @SuppressWarnings("unchecked") // safe to upcast
            final Map<K, V> tmpMap = (Map<K, V>) map;
            return tmpMap;
        }
        return new UnmodifiableMap<>(map);
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map  the map to decorate, must not be null
     * @throws NullPointerException if map is null
     */
    @SuppressWarnings("unchecked") // safe to upcast
    private UnmodifiableMap(final Map<? extends K, ? extends V> map) {
        super((Map<K, V>) map);
    }

    /**
     * Write the map out using a custom routine.
     *
     * @param out  the output stream
     * @throws IOException if an error occurs while writing to the stream
     */
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(map);
    }

    /**
     * Read the map in using a custom routine.
     *
     * @param in  the input stream
     * @throws IOException if an error occurs while reading from the stream
     * @throws ClassNotFoundException if an object read from the stream can not be loaded
     */
    @SuppressWarnings("unchecked")
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        map = (Map<K, V>) in.readObject();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V put(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> mapToCopy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MapIterator<K, V> mapIterator() {
        if (map instanceof IterableMap) {
            final MapIterator<K, V> it = ((IterableMap<K, V>) map).mapIterator();
            return UnmodifiableMapIterator.unmodifiableMapIterator(it);
        }
        final MapIterator<K, V> it = new EntrySetMapIterator<>(map);
        return UnmodifiableMapIterator.unmodifiableMapIterator(it);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        final Set<Map.Entry<K, V>> set = super.entrySet();
        return UnmodifiableEntrySet.unmodifiableEntrySet(set);
    }

    @Override
    public Set<K> keySet() {
        final Set<K> set = super.keySet();
        return UnmodifiableSet.unmodifiableSet(set);
    }

    @Override
    public Collection<V> values() {
        final Collection<V> coll = super.values();
        return UnmodifiableCollection.unmodifiableCollection(coll);
    }
}
