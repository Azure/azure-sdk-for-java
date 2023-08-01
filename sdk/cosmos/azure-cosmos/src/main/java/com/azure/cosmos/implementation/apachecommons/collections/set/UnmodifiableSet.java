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

package com.azure.cosmos.implementation.apachecommons.collections.set;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.azure.cosmos.implementation.apachecommons.collections.list.Unmodifiable;
import com.azure.cosmos.implementation.apachecommons.collections.list.iterators.UnmodifiableIterator;

public final class UnmodifiableSet<E>
    extends AbstractSerializableSetDecorator<E>
    implements Unmodifiable {
    /** Serialization version */
    private static final long serialVersionUID = 2106511754030535764L;

    /**
     * Factory method to create an unmodifiable set.
     *
     * @param <E> the element type
     * @param set  the set to decorate, must not be null
     * @return a new unmodifiable set
     * @throws NullPointerException if set is null
     */
    public static <E> Set<E> unmodifiableSet(final Set<? extends E> set) {
        if (set instanceof Unmodifiable) {
            @SuppressWarnings("unchecked") // safe to upcast
            final Set<E> tmpSet = (Set<E>) set;
            return tmpSet;
        }
        return new UnmodifiableSet<>(set);
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param set  the set to decorate, must not be null
     * @throws NullPointerException if set is null
     */
    @SuppressWarnings("unchecked") // safe to upcast
    private UnmodifiableSet(final Set<? extends E> set) {
        super((Set<E>) set);
    }

    @Override
    public Iterator<E> iterator() {
        return UnmodifiableIterator.unmodifiableIterator(decorated().iterator());
    }

    @Override
    public boolean add(final E object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(final Collection<? extends E> coll) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(final Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(final Collection<?> coll) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(final Collection<?> coll) {
        throw new UnsupportedOperationException();
    }
}
