// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TypeMap {

    private Map<Class<?>, Object> map = new HashMap<>();

    public <T> void put(Class<T> type, T instance) {
        map.put(Objects.requireNonNull(type), instance);
    }

    public <T> T get(Class<T> type) {
        return type.cast(map.get(type));
    }
}
