// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.util;

import java.util.HashMap;
import java.util.Map;

public final class Utility {
    /**
     * The utility of converting key of the map from object to string.
     *
     * @param sourceMap The source map to convert
     * @return The map which has String as a key.
     */
    public static Map<String, Object> convertMaps(Map<Object, Object> sourceMap) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry: sourceMap.entrySet()) {
            result.put((String) entry.getKey(), entry.getValue());
        }
        return result;
    }

    private Utility() {
    }
}
