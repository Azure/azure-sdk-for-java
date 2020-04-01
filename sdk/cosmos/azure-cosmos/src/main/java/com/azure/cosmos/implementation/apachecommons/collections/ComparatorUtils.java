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

import com.azure.cosmos.implementation.apachecommons.collections.comparators.ComparableComparator;

import java.util.Comparator;

public class ComparatorUtils {
    /**
     * Comparator for natural sort order.
     *
     * @see ComparableComparator#comparableComparator()
     */
    @SuppressWarnings({ "rawtypes", "unchecked" }) // explicit type needed for Java 1.5 compilation
    public static final Comparator NATURAL_COMPARATOR = ComparableComparator.<Comparable>comparableComparator();

    /**
     * Gets a comparator that uses the natural order of the objects.
     *
     * @param <E>  the object type to compare
     * @return  a comparator which uses natural order
     */
    @SuppressWarnings("unchecked")
    public static <E extends Comparable<? super E>> Comparator<E> naturalComparator() {
        return NATURAL_COMPARATOR;
    }
}
