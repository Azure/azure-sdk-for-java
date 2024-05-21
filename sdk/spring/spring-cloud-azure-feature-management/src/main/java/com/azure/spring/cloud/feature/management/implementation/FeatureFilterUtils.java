// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.implementation;

import java.util.Collection;
import java.util.Map;

public class FeatureFilterUtils {
    /**
     * Looks at the given key in the parameters and coverts it to a list if it is currently a map. Used for updating
     * fields in the targeting filter.
     *
     * @param parameters map of generic objects
     * @param key key of object int the parameters map
     */
    public static void updateValueFromMapToList(Map<String, Object> parameters, String key) {
        Object objectMap = parameters.get(key);
        if (objectMap instanceof Map) {
            Collection<Object> toType = ((Map<String, Object>) objectMap).values();
            parameters.put(key, toType);
        }
    }
}
