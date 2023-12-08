// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.models;

import java.util.Collection;

/**
 * Base implementation for expandable, single string enums.
 *
 * @param <T> a specific expandable enum type
 */
public abstract class ExpandableStringEnum<T extends ExpandableStringEnum<T>> {
    /**
     * Creates an instance of the specific expandable string enum from a String.
     *
     * @param name The value to create the instance from.
     * @param <T> the class of the expandable string enum.
     * @return The expandable string enum instance.
     * @throws RuntimeException wrapping implementation class constructor exception (if any is thrown).
     */
    public static <T extends ExpandableStringEnum<T>> T fromString(String name) {
        throw new UnsupportedOperationException("Subtypes must override this method.");
    }

    /**
     * Gets a collection of all known values to an expandable string enum type.
     *
     * @param <T> the class of the expandable string enum.
     * @return A collection of all known values for the given {@code clazz}.
     */
    public static <T extends ExpandableStringEnum<T>> Collection<T> values() {
        throw new UnsupportedOperationException("Subtypes must override this method.");
    }

    @Override
    public abstract String toString();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);
}
