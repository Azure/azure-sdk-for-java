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

package com.azure.cosmos.implementation.apachecommons.collections;

import java.util.Collection;
import java.util.Iterator;

import com.azure.cosmos.implementation.apachecommons.collections.list.Unmodifiable;
import com.azure.cosmos.implementation.apachecommons.collections.list.iterators.UnmodifiableIterator;

public final class UnmodifiableCollection<E>
    extends AbstractCollectionDecorator<E>
    implements Unmodifiable {
    /** Serialization version */
    private static final long serialVersionUID = -1742294389933370464L;

    /**
     * Factory method to create an unmodifiable collection.
     * <p>
     * If the collection passed in is already unmodifiable, it is returned.
     *
     * @param <T> the type of the elements in the collection
     * @param coll  the collection to decorate, must not be null
     * @return an unmodifiable collection
     * @throws NullPointerException if collection is null
     */
    public static <T> Collection<T> unmodifiableCollection(final Collection<? extends T> coll) {
        if (coll instanceof Unmodifiable) {
            @SuppressWarnings("unchecked") // safe to upcast
            final Collection<T> tmpColl = (Collection<T>) coll;
            return tmpColl;
        }
        return new UnmodifiableCollection<>(coll);
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param coll  the collection to decorate, must not be null
     * @throws NullPointerException if collection is null
     */
    @SuppressWarnings("unchecked") // safe to upcast
    private UnmodifiableCollection(final Collection<? extends E> coll) {
        super((Collection<E>) coll);
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
