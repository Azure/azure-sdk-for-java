// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.common;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for Document Respose conversions
 */
public class DocumentResponseConversions {

    private static final String ODATA_CONTEXT = "@odata.context";
    /**
     * Convert a Linked HashMap object to Map object
     * @param linkedMapObject object to convert
     * @return Map<String, Object>
     */
    public static Map<String, Object> convertLinkedHashMapToMap(Object linkedMapObject) {
        /** This SuppressWarnings is for the checkstyle
            it is used because api return type can be anything and therefore is an Object
            in our case we know and we use it only when the return type is LinkedHashMap
         **/
        @SuppressWarnings (value = "unchecked")
        LinkedHashMap<String, Object> linkedMap = (LinkedHashMap<String, Object>) linkedMapObject;
        Set<Map.Entry<String, Object>> entries = linkedMap.entrySet();

        Map<String, Object> convertedMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : entries) {
            convertedMap.put(entry.getKey(), entry.getValue());
        }

        return convertedMap;
    }

    /**
     * Drop fields that shouldn't be in the returned object
     * @param map the map to drop items from
     * @return the new map
     */
    public static Map<String, Object> dropUnnecessaryFields(Map<String, Object> map) {
        map.remove(ODATA_CONTEXT);

        return map;
    }
}
