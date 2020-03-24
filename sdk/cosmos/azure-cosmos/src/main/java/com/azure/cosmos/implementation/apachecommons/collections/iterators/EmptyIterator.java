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

package com.azure.cosmos.implementation.apachecommons.collections.iterators;

import com.azure.cosmos.implementation.apachecommons.collections.ResettableIterator;

import java.util.Iterator;

public class EmptyIterator<E> extends AbstractEmptyIterator<E> implements ResettableIterator<E> {
    /**
     * Singleton instance of the iterator.
     */
    @SuppressWarnings("rawtypes")
    public static final ResettableIterator RESETTABLE_INSTANCE = new EmptyIterator<>();

    /**
     * Singleton instance of the iterator.
     */
    @SuppressWarnings("rawtypes")
    public static final Iterator INSTANCE = RESETTABLE_INSTANCE;

    /**
     * Get a typed resettable empty iterator instance.
     * @param <E> the element type
     * @return ResettableIterator&lt;E&gt;
     */
    @SuppressWarnings("unchecked")
    public static <E> ResettableIterator<E> resettableEmptyIterator() {
        return RESETTABLE_INSTANCE;
    }

    /**
     * Get a typed empty iterator instance.
     * @param <E> the element type
     * @return Iterator&lt;E&gt;
     */
    @SuppressWarnings("unchecked")
    public static <E> Iterator<E> emptyIterator() {
        return INSTANCE;
    }

    /**
     * Constructor.
     */
    protected EmptyIterator() {
        super();
    }
}
