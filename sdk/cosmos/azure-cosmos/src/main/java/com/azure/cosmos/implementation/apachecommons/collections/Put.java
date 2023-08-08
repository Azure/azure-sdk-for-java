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


import java.util.Map;

public interface Put<K, V> {
    /**
     * @see Map#clear()
     */
    void clear();

    /**
     * Note that the return type is Object, rather than V as in the Map interface.
     * See the class Javadoc for further info.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <code>key</code>, or
     *         <code>null</code> if there was no mapping for <code>key</code>.
     *         (A <code>null</code> return can also indicate that the map
     *         previously associated <code>null</code> with <code>key</code>,
     *         if the implementation supports <code>null</code> values.)
     * @see Map#put(Object, Object)
     */
    Object put(K key, V value);

    /**
     * @param t mappings to be stored in this map
     * @see Map#putAll(Map)
     */
    void putAll(Map<? extends K, ? extends V> t);
}

