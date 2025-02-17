// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

/**
 * Interface for expandable enums.
 *
 * @param <T> The type of objects to be listed in the expandable enum.
 */
@FunctionalInterface
public interface ExpandableEnum<T> {
    /**
     * Returns the value represented by this expandable enum instance.
     *
     * @return The value represented by this expandable enum instance.
     */
    T getValue();
}
