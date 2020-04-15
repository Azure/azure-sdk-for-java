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

package com.azure.cosmos.implementation.apachecommons.collections.comparators;

import java.io.Serializable;
import java.util.Comparator;

public class ComparableComparator<E extends Comparable<? super E>> implements Comparator<E>, Serializable {
    /** Serialization version. */
    private static final long serialVersionUID = 7476918961260943496L;

    /** The singleton instance. */
    @SuppressWarnings("rawtypes")
    public static final ComparableComparator INSTANCE = new ComparableComparator();

    /**
     * Gets the singleton instance of a ComparableComparator.
     * <p>
     * Developers are encouraged to use the comparator returned from this method
     * instead of constructing a new instance to reduce allocation and GC overhead
     * when multiple comparable comparators may be used in the same VM.
     *
     * @param <E>  the element type
     * @return the singleton ComparableComparator
     */
    @SuppressWarnings("unchecked")
    public static <E extends Comparable<? super E>> ComparableComparator<E> comparableComparator() {
        return INSTANCE;
    }

    /**
     * Constructor whose use should be avoided.
     * <p>
     * Please use the {@link #comparableComparator()} method whenever possible.
     */
    public ComparableComparator() {
        super();
    }

    /**
     * Compare the two {@link Comparable Comparable} arguments.
     * This method is equivalent to:
     * <pre>((Comparable)obj1).compareTo(obj2)</pre>
     *
     * @param obj1  the first object to compare
     * @param obj2  the second object to compare
     * @return negative if obj1 is less, positive if greater, zero if equal
     * @throws NullPointerException if <i>obj1</i> is <code>null</code>,
     *         or when <code>((Comparable)obj1).compareTo(obj2)</code> does
     * @throws ClassCastException if <i>obj1</i> is not a <code>Comparable</code>,
     *         or when <code>((Comparable)obj1).compareTo(obj2)</code> does
     */
    @Override
    public int compare(final E obj1, final E obj2) {
        return obj1.compareTo(obj2);
    }

    /**
     * Implement a hash code for this comparator that is consistent with
     * {@link #equals(Object) equals}.
     *
     * @return a hash code for this comparator.
     */
    @Override
    public int hashCode() {
        return "ComparableComparator".hashCode();
    }

    /**
     * Returns {@code true} iff <i>that</i> Object is is a {@link Comparator Comparator}
     * whose ordering is known to be equivalent to mine.
     * <p>
     * This implementation returns {@code true} iff
     * <code><i>object</i>.{@link Object#getClass() getClass()}</code> equals
     * <code>this.getClass()</code>. Subclasses may want to override this behavior to remain
     * consistent with the {@link Comparator#equals(Object)} contract.
     *
     * @param object  the object to compare with
     * @return {@code true} if equal
     */
    @Override
    public boolean equals(final Object object) {
        return this == object ||
            null != object && object.getClass().equals(this.getClass());
    }
}

