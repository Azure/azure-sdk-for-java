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
import java.util.Collections;

public class CollectionUtils {
    /**
     * An empty unmodifiable collection.
     * The JDK provides empty Set and List implementations which could be used for
     * this purpose. However they could be cast to Set or List which might be
     * undesirable. This implementation only implements Collection.
     */
    @SuppressWarnings("rawtypes") // we deliberately use the raw type here
    public static final Collection EMPTY_COLLECTION = Collections.emptyList();


    private CollectionUtils() {}

    /**
     * Returns the immutable EMPTY_COLLECTION with generic type safety.
     *
     * @see #EMPTY_COLLECTION
     * @param <T> the element type
     * @return immutable empty collection
     */
    @SuppressWarnings("unchecked") // OK, empty collection is compatible with any type
    public static <T> Collection<T> emptyCollection() {
        return EMPTY_COLLECTION;
    }


    /**
     * Returns an immutable empty collection if the argument is <code>null</code>,
     * or the argument itself otherwise.
     *
     * @param <T> the element type
     * @param collection the collection, possibly <code>null</code>
     * @return an empty collection if the argument is <code>null</code>
     */
    public static <T> Collection<T> emptyIfNull(final Collection<T> collection) {
        return collection == null ? CollectionUtils.<T>emptyCollection() : collection;
    }
}
