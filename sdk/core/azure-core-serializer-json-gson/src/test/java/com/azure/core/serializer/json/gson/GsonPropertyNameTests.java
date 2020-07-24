// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.google.gson.annotations.SerializedName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GsonPropertyNameTests {
    private static GsonPropertyNameSerializer serializer;

    @BeforeAll
    public static void setup() {
        serializer = new GsonPropertyNameSerializer();
    }

    @Test
    public void testPropertyNameOnFieldName() {
        Field f = mock(Field.class);
        String fieldName = "fieldName";

        when(f.isAnnotationPresent(SerializedName.class)).thenReturn(false);

        when(f.getName()).thenReturn(fieldName);

        StepVerifier.create(serializer.getSerializerMemberName(f))
            .assertNext(actual -> assertEquals(fieldName, actual))
            .verifyComplete();
    }


    @Test
    public void testPropertyNameOnFieldAnnotation() {
        Field f = mock(Field.class);
        String expectValue = "hasAnnotation";

        when(f.isAnnotationPresent(SerializedName.class)).thenReturn(true);
        SerializedName annotation = mock(SerializedName.class);
        when(f.getDeclaredAnnotation(SerializedName.class)).thenReturn(annotation);
        when(annotation.value()).thenReturn(expectValue);

        StepVerifier.create(serializer.getSerializerMemberName(f))
            .assertNext(actual -> assertEquals(expectValue, actual))
            .verifyComplete();
    }

    @Test
    public void testPropertyNameOnFieldAnnotationWithEmptyValue() {
        Field f = mock(Field.class);
        String fieldName = "fieldName";

        when(f.isAnnotationPresent(SerializedName.class)).thenReturn(true);
        SerializedName annotation = mock(SerializedName.class);
        when(f.getDeclaredAnnotation(SerializedName.class)).thenReturn(annotation);
        when(annotation.value()).thenReturn("");
        when(f.getName()).thenReturn(fieldName);

        StepVerifier.create(serializer.getSerializerMemberName(f))
            .assertNext(actual -> assertEquals(fieldName, actual))
            .verifyComplete();
    }


    @Test
    public void testPropertyNameOnFieldAnnotationWithNullValue() {
        Field f = mock(Field.class);
        String fieldName = "fieldName";

        when(f.isAnnotationPresent(SerializedName.class)).thenReturn(true);
        SerializedName annotation = mock(SerializedName.class);
        when(f.getDeclaredAnnotation(SerializedName.class)).thenReturn(annotation);
        when(annotation.value()).thenReturn(null);
        when(annotation.value()).thenReturn(fieldName);

        StepVerifier.create(serializer.getSerializerMemberName(f))
            .assertNext(actual -> assertEquals(fieldName, actual))
            .verifyComplete();
    }

    @Test
    public void testPropertyNameOnMethodName() {
        Method m = mock(Method.class);
        String methodName = "methodName";

        when(m.isAnnotationPresent(SerializedName.class)).thenReturn(false);

        when(m.getName()).thenReturn(methodName);

        StepVerifier.create(serializer.getSerializerMemberName(m))
            .assertNext(actual -> assertEquals(methodName, actual))
            .verifyComplete();
    }
}
