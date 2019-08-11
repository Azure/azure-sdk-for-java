// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.common;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for Document Respose conversions
 */
public class DocumentResponseConversions {

    /**
     * Convert a Linked HashMap object to Map object
     *
     * @param linkedMapObject object to convert
     * @return Map<String, Object>
     */
    public static Map<String, Object> convertLinkedHashMapToMap(Object linkedMapObject) {


        LinkedHashMap<String, Object> linkedMap = (LinkedHashMap<String, Object>) linkedMapObject;

        Set<Map.Entry<String, Object>> entries = linkedMap.entrySet();

        Map<String, Object> convertedMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : entries) {
            Object value = entry.getValue();

            if (value instanceof LinkedHashMap) {
                value = convertLinkedHashMapToMap(entry.getValue());
            }
            if (value instanceof ArrayList) {
                value = convertArray((ArrayList) value);

            }

            convertedMap.put(entry.getKey(), value);
        }

        return convertedMap;

    }

    /**
     * Drop fields that shouldn't be in the returned object
     *
     * @param map
     * @return Map<String, Object>
     */
    public static Map<String, Object> dropUnnecessaryFields(Map<String, Object> map){
        map.remove("@odata.context");
        return map;
    }

    /**
     * Convert Array Object elements
     * @param array
     * @return ArrayList<Object>
     */
    private static ArrayList<Object> convertArray(ArrayList array) {
        ArrayList<Object> convertedArray = new ArrayList<>();
        for (Object arrayValue : array) {
            if (arrayValue instanceof LinkedHashMap)
                convertedArray.add(convertLinkedHashMapToMap(arrayValue));
            else {
                convertedArray.add(arrayValue);
            }
        }
        return convertedArray;
    }

    /**
     * Map exceptions to be more informative
     * @param throwable
     * @return Throwable
     */
    public static Throwable exceptionMapper(Throwable throwable) {

        if (throwable instanceof HttpResponseException && throwable.getMessage().equals("Status code 404, (empty body)")) {
            return new ResourceNotFoundException("Document not found", ((HttpResponseException) throwable).response());
        }

        return throwable;
    }


}
