// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Mapping an object's type to the object.
 */
public class TypeMap {

    private final Map<Class<?>, Object> map = new HashMap<>();

    public <T> void put(Class<T> type, T instance) {
        map.put(Objects.requireNonNull(type), instance);
    }

    public <T> T get(Class<T> type) {
        return type.cast(map.get(type));
    }
}
