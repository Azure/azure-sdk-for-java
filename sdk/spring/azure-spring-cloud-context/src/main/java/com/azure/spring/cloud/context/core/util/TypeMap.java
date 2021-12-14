// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Mapping an object's type to the object.
 */
public class TypeMap {

    private final Map<Class<?>, Object> map = new HashMap<>();

    /**
     * Puts a new type and instance into the TypeMap.
     *
     * @param type The type.
     * @param instance The instance.
     * @param <T> The type.
     */
    public <T> void put(Class<T> type, T instance) {
        map.put(Objects.requireNonNull(type), instance);
    }

    /**
     * Gets an instance from the TypeMap given the type.
     *
     * @param type The type.
     * @param <T> The type.
     * @return The instance.
     */
    public <T> T get(Class<T> type) {
        return type.cast(map.get(type));
    }
}
