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

package com.azure.cosmos.implementation.apachecommons.lang;

import java.io.Serializable;
import java.util.Comparator;

public final class Range<T> implements Serializable {

    /**
     * Serialization version.
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /**
     * The ordering scheme used in this range.
     */
    private final Comparator<T> comparator;
    /**
     * The minimum value in this range (inclusive).
     */
    private final T minimum;
    /**
     * The maximum value in this range (inclusive).
     */
    private final T maximum;
    /**
     * Cached output hashCode (class is immutable).
     */
    private transient int hashCode;
    /**
     * Cached output toString (class is immutable).
     */
    private transient String toString;

    /**
     * <p>Obtains a range using the specified element as both the minimum
     * and maximum in this range.</p>
     *
     * <p>The range uses the natural ordering of the elements to determine where
     * values lie in the range.</p>
     *
     * @param <T> the type of the elements in this range
     * @param element  the value to use for this range, not null
     * @return the range object, not null
     * @throws IllegalArgumentException if the element is null
     * @throws ClassCastException if the element is not {@code Comparable}
     */
    public static <T extends Comparable<T>> Range<T> is(final T element) {
        return between(element, element, null);
    }

    /**
     * <p>Obtains a range using the specified element as both the minimum
     * and maximum in this range.</p>
     *
     * <p>The range uses the specified {@code Comparator} to determine where
     * values lie in the range.</p>
     *
     * @param <T> the type of the elements in this range
     * @param element  the value to use for this range, must not be {@code null}
     * @param comparator  the comparator to be used, null for natural ordering
     * @return the range object, not null
     * @throws IllegalArgumentException if the element is null
     * @throws ClassCastException if using natural ordering and the elements are not {@code Comparable}
     */
    public static <T> Range<T> is(final T element, final Comparator<T> comparator) {
        return between(element, element, comparator);
    }

    /**
     * <p>Obtains a range with the specified minimum and maximum values (both inclusive).</p>
     *
     * <p>The range uses the natural ordering of the elements to determine where
     * values lie in the range.</p>
     *
     * <p>The arguments may be passed in the order (min,max) or (max,min).
     * The getMinimum and getMaximum methods will return the correct values.</p>
     *
     * @param <T> the type of the elements in this range
     * @param fromInclusive  the first value that defines the edge of the range, inclusive
     * @param toInclusive  the second value that defines the edge of the range, inclusive
     * @return the range object, not null
     * @throws IllegalArgumentException if either element is null
     * @throws ClassCastException if the elements are not {@code Comparable}
     */
    public static <T extends Comparable<T>> Range<T> between(final T fromInclusive, final T toInclusive) {
        return between(fromInclusive, toInclusive, null);
    }

    /**
     * <p>Obtains a range with the specified minimum and maximum values (both inclusive).</p>
     *
     * <p>The range uses the specified {@code Comparator} to determine where
     * values lie in the range.</p>
     *
     * <p>The arguments may be passed in the order (min,max) or (max,min).
     * The getMinimum and getMaximum methods will return the correct values.</p>
     *
     * @param <T> the type of the elements in this range
     * @param fromInclusive  the first value that defines the edge of the range, inclusive
     * @param toInclusive  the second value that defines the edge of the range, inclusive
     * @param comparator  the comparator to be used, null for natural ordering
     * @return the range object, not null
     * @throws IllegalArgumentException if either element is null
     * @throws ClassCastException if using natural ordering and the elements are not {@code Comparable}
     */
    public static <T> Range<T> between(final T fromInclusive, final T toInclusive, final Comparator<T> comparator) {
        return new Range<>(fromInclusive, toInclusive, comparator);
    }

    /**
     * Creates an instance.
     *
     * @param element1  the first element, not null
     * @param element2  the second element, not null
     * @param comp  the comparator to be used, null for natural ordering
     */
    @SuppressWarnings("unchecked")
    private Range(final T element1, final T element2, final Comparator<T> comp) {
        if (element1 == null || element2 == null) {
            throw new IllegalArgumentException("Elements in a range must not be null: element1=" +
                element1 + ", element2=" + element2);
        }
        if (comp == null) {
            this.comparator = ComparableComparator.INSTANCE;
        } else {
            this.comparator = comp;
        }
        if (this.comparator.compare(element1, element2) < 1) {
            this.minimum = element1;
            this.maximum = element2;
        } else {
            this.minimum = element2;
            this.maximum = element1;
        }
    }

    /**
     * <p>Gets the minimum value in this range.</p>
     *
     * @return the minimum value in this range, not null
     */
    public T getMinimum() {
        return minimum;
    }

    /**
     * <p>Gets the maximum value in this range.</p>
     *
     * @return the maximum value in this range, not null
     */
    public T getMaximum() {
        return maximum;
    }

    /**
     * <p>Gets the comparator being used to determine if objects are within the range.</p>
     *
     * <p>Natural ordering uses an internal comparator implementation, thus this
     * method never returns null.</p>
     *
     * @return the comparator being used, not null
     */
    public Comparator<T> getComparator() {
        return comparator;
    }

    /**
     * <p>Checks whether the specified element occurs within this range.</p>
     *
     * @param element  the element to check for, null returns false
     * @return true if the specified element occurs within this range
     */
    public boolean contains(final T element) {
        if (element == null) {
            return false;
        }
        return comparator.compare(element, minimum) > -1 && comparator.compare(element, maximum) < 1;
    }

    /**
     * <p>Compares this range to another object to test if they are equal.</p>.
     *
     * <p>To be equal, the minimum and maximum values must be equal, which
     * ignores any differences in the comparator.</p>
     *
     * @param obj the reference object with which to compare
     * @return true if this object is equal
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || obj.getClass() != getClass()) {
            return false;
        } else {
            @SuppressWarnings("unchecked") // OK because we checked the class above
            final
            Range<T> range = (Range<T>) obj;
            return minimum.equals(range.minimum) &&
                maximum.equals(range.maximum);
        }
    }

    /**
     * <p>Gets a suitable hash code for the range.</p>
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        int result = hashCode;
        if (hashCode == 0) {
            result = 17;
            result = 37 * result + getClass().hashCode();
            result = 37 * result + minimum.hashCode();
            result = 37 * result + maximum.hashCode();
            hashCode = result;
        }
        return result;
    }

    /**
     * <p>Gets the range as a {@code String}.</p>
     *
     * <p>The format of the String is '[<i>min</i>..<i>max</i>]'.</p>
     *
     * @return the {@code String} representation of this range
     */
    @Override
    public String toString() {
        if (toString == null) {
            toString = "[" + minimum + ".." + maximum + "]";
        }
        return toString;
    }

    /**
     * <p>Formats the receiver using the given format.</p>
     *
     * <p>This uses {@link java.util.Formattable} to perform the formatting. Three variables may
     * be used to embed the minimum, maximum and comparator.
     * Use {@code %1$s} for the minimum element, {@code %2$s} for the maximum element
     * and {@code %3$s} for the comparator.
     * The default format used by {@code toString()} is {@code [%1$s..%2$s]}.</p>
     *
     * @param format  the format string, optionally containing {@code %1$s}, {@code %2$s} and  {@code %3$s}, not null
     * @return the formatted string, not null
     */
    public String toString(final String format) {
        return String.format(format, minimum, maximum, comparator);
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings({"rawtypes", "unchecked"})
    private enum ComparableComparator implements Comparator {
        INSTANCE;
        /**
         * Comparable based compare implementation.
         *
         * @param obj1 left hand side of comparison
         * @param obj2 right hand side of comparison
         * @return negative, 0, positive comparison value
         */
        @Override
        public int compare(final Object obj1, final Object obj2) {
            return ((Comparable) obj1).compareTo(obj2);
        }
    }
}

