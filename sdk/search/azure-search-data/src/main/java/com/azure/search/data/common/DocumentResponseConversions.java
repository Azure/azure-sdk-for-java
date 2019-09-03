// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.common;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.search.data.customization.Document;

import java.util.ArrayList;
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
     * @return {@link Map}{@code <}{@link String}{@code ,}{@link Object}{@code >}
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
     * Convert Array Object elements
     * @param array which elements will be converted
     * @return {@link ArrayList}{@code <}{@link Object}{@code >}
     */
    private static ArrayList<Object> convertArray(ArrayList array) {
        ArrayList<Object> convertedArray = new ArrayList<>();
        for (Object arrayValue : array) {
            if (arrayValue instanceof LinkedHashMap) {
                convertedArray.add(convertLinkedHashMapToMap(arrayValue));
            } else {
                convertedArray.add(arrayValue);
            }
        }
        return convertedArray;
    }

    /**
     * Map exceptions to be more informative
     * @param throwable to convert
     * @return Throwable
     */
    public static Throwable exceptionMapper(Throwable throwable) {

        if (throwable instanceof HttpResponseException && throwable.getMessage().equals("Status code 404, (empty body)")) {
            return new ResourceNotFoundException("Document not found", ((HttpResponseException) throwable).response());
        }

        return throwable;
    }

    /**
     * Drop fields that shouldn't be in the returned object
     * @param document document object
     * @return document without the redundant fields
     */
    public static Document cleanupDocument(Document document) {
        document.remove(ODATA_CONTEXT);
        return document;
    }
}
