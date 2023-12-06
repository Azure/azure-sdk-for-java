// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.models;

import java.util.Collection;
import java.util.Objects;

/**
 * Base implementation for expandable, single string enums.
 *
 * @param <T> a specific expandable enum type
 */
public abstract class ExpandableStringEnum<T extends ExpandableStringEnum<T>> {
    private final String name;
    private final Class<T> clazz;

    protected ExpandableStringEnum(String name, Class<T> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

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
    public String toString() {
        return this.name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.clazz, this.name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (clazz == null || !clazz.isAssignableFrom(obj.getClass())) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (this.name == null) {
            return ((ExpandableStringEnum<T>) obj).name == null;
        } else {
            return this.name.equals(((ExpandableStringEnum<T>) obj).name);
        }
    }
}
