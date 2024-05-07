// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TypeReferenceTests {

    @Test
    public void createGenericTypeReference() {
        final TypeReference<HashMap<String, Object>> typeReference = new TypeReference<HashMap<String, Object>>() {
        };
        final Map<String, Object> expectedJavaType = new HashMap<String, Object>() {
        };
        assertEquals(expectedJavaType.getClass().getGenericSuperclass(), typeReference.getJavaType());
        assertEquals(HashMap.class, typeReference.getJavaClass());
    }

    @Test
    public void createFactoryInstance() {
        TypeReference<Integer> typeReference = TypeReference.createInstance(int.class);
        assertEquals(int.class, typeReference.getJavaType());
        assertEquals(int.class, typeReference.getJavaClass());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void createTypeReferenceWithoutType() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> new TypeReference() {
        });
        assertEquals("Type constructed without type information.", thrown.getMessage());
    }
}
