package com.azure.core.util;

/**
 * Interface for expandable enums.
 *
 * @param <T> The type of objects to be listed in the expandable enum.
 */
public interface ExpandableEnum<T> {
    /**
     * Returns the value represented by this expandable enum instance.
     *
     * @return The value represented by this expandable enum instance.
     */
    T getValue();
}
