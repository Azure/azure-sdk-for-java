// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JacksonPropertyNameTests {
    private static JacksonPropertyNameSerializer serializer;

    @BeforeAll
    public static void setup() {
        serializer = new JacksonPropertyNameSerializer();
    }

    @Test
    public void testPropertyNameOnFieldName() throws NoSuchFieldException {
        String fieldName = "fieldName";
        Field f = mock(Field.class);

        when(f.isAnnotationPresent(JsonProperty.class)).thenReturn(false);

        when(f.getName()).thenReturn(fieldName);

        StepVerifier.create(serializer.getSerializerMemberName(f))
            .assertNext(actual -> assertEquals(fieldName, actual))
            .verifyComplete();
    }

//
//    @Test
//    public void testPropertyNameOnFieldAnnotation() {
//        Field f = Mockito.spy(Field.class);
//        // Field f = mock(Field.class);
//        String expectValue = "hasAnnotation";
//
//        when(f.isAnnotationPresent(JsonProperty.class)).thenReturn(true);
//        JsonProperty annotation = mock(JsonProperty.class);
//        when(f.getDeclaredAnnotation(JsonProperty.class)).thenReturn(annotation);
//        when(annotation.value()).thenReturn(expectValue);
//
//        StepVerifier.create(serializer.getSerializerMemberName(f))
//            .assertNext(actual -> assertEquals(expectValue, actual))
//            .verifyComplete();
//    }
//
//    @Test
//    public void testPropertyNameOnFieldAnnotationWithEmptyValue() {
//        Field f = Mockito.spy(Field.class);
//        //Field f = mock(Field.class);
//        String fieldName = "fieldName";
//
//        when(f.isAnnotationPresent(JsonProperty.class)).thenReturn(true);
//        JsonProperty annotation = mock(JsonProperty.class);
//        when(f.getDeclaredAnnotation(JsonProperty.class)).thenReturn(annotation);
//        when(annotation.value()).thenReturn("");
//        when(f.getName()).thenReturn(fieldName);
//
//        StepVerifier.create(serializer.getSerializerMemberName(f))
//            .assertNext(actual -> assertEquals(fieldName, actual))
//            .verifyComplete();
//    }
//
//
//    @Test
//    public void testPropertyNameOnFieldAnnotationWithNullValue() {
//        Field f = Mockito.spy(Field.class);
//        //Field f = mock(Field.class);
//        String fieldName = "fieldName";
//
//        when(f.isAnnotationPresent(JsonProperty.class)).thenReturn(true);
//        JsonProperty annotation = mock(JsonProperty.class);
//        when(f.getDeclaredAnnotation(JsonProperty.class)).thenReturn(annotation);
//        when(annotation.value()).thenReturn(null);
//        when(annotation.value()).thenReturn(fieldName);
//
//        StepVerifier.create(serializer.getSerializerMemberName(f))
//            .assertNext(actual -> assertEquals(fieldName, actual))
//            .verifyComplete();
//    }
//
//    @Test
//    public void testPropertyNameOnMethodName() {
//        Method m = Mockito.spy(Method.class);
//        //Method m = mock(Method.class);
//        String methodName = "methodName";
//
//        when(m.isAnnotationPresent(JsonProperty.class)).thenReturn(false);
//
//        when(m.getName()).thenReturn(methodName);
//
//        StepVerifier.create(serializer.getSerializerMemberName(m))
//            .assertNext(actual -> assertEquals(methodName, actual))
//            .verifyComplete();
//    }
//
//    @Test
//    public void testPropertyNameOnMethodAnnotation() {
//        Method m = Mockito.spy(Method.class);
//        //Method m = mock(Method.class);
//        String expectValue = "hasAnnotation";
//
//        when(m.isAnnotationPresent(JsonProperty.class)).thenReturn(true);
//        JsonProperty annotation = mock(JsonProperty.class);
//        when(m.getDeclaredAnnotation(JsonProperty.class)).thenReturn(annotation);
//        when(annotation.value()).thenReturn(expectValue);
//
//        StepVerifier.create(serializer.getSerializerMemberName(m))
//            .assertNext(actual -> assertEquals(expectValue, actual))
//            .verifyComplete();
//    }
//
//    @Test
//    public void testPropertyNameOnMethodAnnotationWithEmptyValue() {
//        Method m = Mockito.spy(Method.class);
//        //Method m = mock(Method.class);
//        String methodName = "methodName";
//
//        when(m.isAnnotationPresent(JsonProperty.class)).thenReturn(true);
//        JsonProperty annotation = mock(JsonProperty.class);
//        when(m.getDeclaredAnnotation(JsonProperty.class)).thenReturn(annotation);
//        when(annotation.value()).thenReturn("");
//        when(m.getName()).thenReturn(methodName);
//        StepVerifier.create(serializer.getSerializerMemberName(m))
//            .assertNext(actual -> assertEquals(methodName, actual))
//            .verifyComplete();
//    }
//
//    @Test
//    public void testPropertyNameOnMethodAnnotationWithNullValue() {
//        Method m = Mockito.spy(Method.class);
//        //Method m = mock(Method.class);
//        String methodName = "methodName";
//        when(m.isAnnotationPresent(JsonProperty.class)).thenReturn(true);
//        JsonProperty annotation = mock(JsonProperty.class);
//        when(m.getDeclaredAnnotation(JsonProperty.class)).thenReturn(annotation);
//        when(annotation.value()).thenReturn(null);
//        when(m.getName()).thenReturn(methodName);
//        StepVerifier.create(serializer.getSerializerMemberName(m))
//            .assertNext(actual -> assertEquals(methodName, actual))
//            .verifyComplete();
//    }
}
