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

package com.azure.cosmos.implementation.apachecommons.collections.list;

import com.azure.cosmos.implementation.apachecommons.collections.list.iterators.UnmodifiableIterator;
import com.azure.cosmos.implementation.apachecommons.collections.list.iterators.UnmodifiableListIterator;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public final class UnmodifiableList<E>
    extends AbstractSerializableListDecorator<E>
    implements Unmodifiable {
    /** Serialization version */
    private static final long serialVersionUID = -5965906429914500171L;

    /**
     * Factory method to create an unmodifiable list.
     *
     * @param <E> the type of the elements in the list
     * @param list  the list to decorate, must not be null
     * @return a new unmodifiable list
     * @throws NullPointerException if list is null
     */
    public static <E> List<E> unmodifiableList(final List<? extends E> list) {
        if (list instanceof Unmodifiable) {
            @SuppressWarnings("unchecked") // safe to upcast
            final List<E> tmpList = (List<E>) list;
            return tmpList;
        }
        return new UnmodifiableList<>(list);
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param list  the list to decorate, must not be null
     * @throws NullPointerException if list is null
     */
    @SuppressWarnings("unchecked") // safe to upcast
    public UnmodifiableList(final List<? extends E> list) {
        super((List<E>) list);
    }

    @Override
    public Iterator<E> iterator() {
        return UnmodifiableIterator.unmodifiableIterator(decorated().iterator());
    }

    @Override
    public boolean add(final Object object) {
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

    @Override
    public ListIterator<E> listIterator() {
        return UnmodifiableListIterator.umodifiableListIterator(decorated().listIterator());
    }

    @Override
    public ListIterator<E> listIterator(final int index) {
        return UnmodifiableListIterator.umodifiableListIterator(decorated().listIterator(index));
    }

    @Override
    public void add(final int index, final E object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends E> coll) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E remove(final int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E set(final int index, final E object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<E> subList(final int fromIndex, final int toIndex) {
        final List<E> sub = decorated().subList(fromIndex, toIndex);
        return new UnmodifiableList<>(sub);
    }
}
