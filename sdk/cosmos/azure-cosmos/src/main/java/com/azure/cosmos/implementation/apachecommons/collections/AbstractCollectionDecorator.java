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

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

public abstract class AbstractCollectionDecorator<E>
    implements Collection<E>, Serializable {
    /** Serialization version */
    private static final long serialVersionUID = 4124674254999113719L;

    /** The collection being decorated */
    private Collection<E> collection;

    /**
     * Constructor only used in deserialization, do not use otherwise.
     */
    protected AbstractCollectionDecorator() {
        super();
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param coll  the collection to decorate, must not be null
     * @throws NullPointerException if the collection is null
     */
    protected AbstractCollectionDecorator(final Collection<E> coll) {
        if (coll == null) {
            throw new NullPointerException("Collection must not be null.");
        }
        this.collection = coll;
    }

    /**
     * Gets the collection being decorated.
     * All access to the decorated collection goes via this method.
     *
     * @return the decorated collection
     */
    protected Collection<E> decorated() {
        return collection;
    }

    /**
     * Sets the collection being decorated.
     * <p>
     * <b>NOTE:</b> this method should only be used during deserialization
     *
     * @param coll  the decorated collection
     */
    protected void setCollection(final Collection<E> coll) {
        this.collection = coll;
    }

    @Override
    public boolean add(final E object) {
        return decorated().add(object);
    }

    @Override
    public boolean addAll(final Collection<? extends E> coll) {
        return decorated().addAll(coll);
    }

    @Override
    public void clear() {
        decorated().clear();
    }

    @Override
    public boolean contains(final Object object) {
        return decorated().contains(object);
    }

    @Override
    public boolean isEmpty() {
        return decorated().isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return decorated().iterator();
    }

    @Override
    public boolean remove(final Object object) {
        return decorated().remove(object);
    }

    @Override
    public int size() {
        return decorated().size();
    }

    @Override
    public Object[] toArray() {
        return decorated().toArray();
    }

    @Override
    public <T> T[] toArray(final T[] object) {
        return decorated().toArray(object);
    }

    @Override
    public boolean containsAll(final Collection<?> coll) {
        return decorated().containsAll(coll);
    }

    @Override
    public boolean removeAll(final Collection<?> coll) {
        return decorated().removeAll(coll);
    }

    @Override
    public boolean retainAll(final Collection<?> coll) {
        return decorated().retainAll(coll);
    }

    @Override
    public String toString() {
        return decorated().toString();
    }
}
