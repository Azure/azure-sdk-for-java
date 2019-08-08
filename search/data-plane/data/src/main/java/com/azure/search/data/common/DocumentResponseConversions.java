package com.azure.search.data.common;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class DocumentResponseConversions {

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
