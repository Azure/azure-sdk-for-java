// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.common;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.implementation.http.PagedResponseBase;
import com.azure.search.data.generated.models.FacetResult;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for Document Respose conversions
 */
public class DocumentResponseConversions {

    /**
     * Convert a Linked HashMap object to Map object
     * @param linkedMapObject object to convert
     * @return Map<String, Object>
     */
    public static Map<String, Object> convertLinkedHashMapToMap(Object linkedMapObject) {
        LinkedHashMap<String, Object> linkedMap = (LinkedHashMap<String, Object>) linkedMapObject;
        Set<Map.Entry<String, Object>> entries = linkedMap.entrySet();

        Map<String, Object> convertedMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : entries) {
            convertedMap.put(entry.getKey(), entry.getValue());
        }

        return convertedMap;

    }
}
