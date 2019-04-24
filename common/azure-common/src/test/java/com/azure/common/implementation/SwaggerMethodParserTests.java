// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.implementation;

import com.azure.common.MyOtherRestException;
import com.azure.common.MyRestException;
import com.azure.common.annotations.ExpectedResponses;
import com.azure.common.annotations.PATCH;
import com.azure.common.annotations.UnexpectedResponseExceptionType;
import com.azure.common.entities.HttpBinJSON;
import com.azure.common.exception.ServiceRequestException;
import com.azure.common.http.HttpMethod;
import com.azure.common.implementation.exception.MissingRequiredAnnotationException;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SwaggerMethodParserTests {

    interface TestInterface1 {
        void testMethod1();
    }

    @Test(expected = MissingRequiredAnnotationException.class)
    public void withNoAnnotations() {
        final Method testMethod1 = TestInterface1.class.getDeclaredMethods()[0];
        assertEquals("testMethod1", testMethod1.getName());

        new SwaggerMethodParser(testMethod1, RestProxy.createDefaultSerializer(), "https://raw.host.com");
    }

    interface TestInterface2 {
        @PATCH("my/rest/api/path")
        @ExpectedResponses({200})
        void testMethod2();
    }

    @Test
    public void withOnlyExpectedResponse() throws IOException {
        final Method testMethod2 = TestInterface2.class.getDeclaredMethods()[0];
        assertEquals("testMethod2", testMethod2.getName());

        final SwaggerMethodParser methodParser = new SwaggerMethodParser(testMethod2, RestProxy.createDefaultSerializer(), "https://raw.host.com");
        assertEquals("com.azure.common.implementation.SwaggerMethodParserTests$TestInterface2.testMethod2", methodParser.fullyQualifiedMethodName());
        assertEquals(HttpMethod.PATCH, methodParser.httpMethod());
        assertArrayEquals(new int[] { 200 }, methodParser.expectedStatusCodes());
        assertEquals(ServiceRequestException.class, methodParser.getUnexpectedException(-1).exceptionType());
        assertEquals(Object.class, methodParser.getUnexpectedException(-1).exceptionBodyType());
        assertEquals(false, methodParser.headers(null).iterator().hasNext());
        assertEquals("https", methodParser.scheme(null));
        assertEquals("raw.host.com", methodParser.host(null));
    }

    interface TestInterface3 {
        @PATCH("my/rest/api/path")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(MyRestException.class)
        void testMethod3();
    }

    @Test
    public void withExpectedResponseAndUnexpectedResponseExceptionType() throws IOException {
        final Method testMethod3 = TestInterface3.class.getDeclaredMethods()[0];
        assertEquals("testMethod3", testMethod3.getName());

        final SwaggerMethodParser methodParser = new SwaggerMethodParser(testMethod3, RestProxy.createDefaultSerializer(), "https://raw.host.com");
        assertEquals("com.azure.common.implementation.SwaggerMethodParserTests$TestInterface3.testMethod3", methodParser.fullyQualifiedMethodName());
        assertEquals(HttpMethod.PATCH, methodParser.httpMethod());
        assertArrayEquals(new int[] { 200 }, methodParser.expectedStatusCodes());
        assertEquals(MyRestException.class, methodParser.getUnexpectedException(-1).exceptionType());
        assertEquals(HttpBinJSON.class, methodParser.getUnexpectedException(-1).exceptionBodyType());
        assertEquals(false, methodParser.headers(null).iterator().hasNext());
        assertEquals("https", methodParser.scheme(null));
        assertEquals("raw.host.com", methodParser.host(null));
    }

    interface TestInterface4 {
        @PATCH("my/rest/api/path")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {400}, value = ServiceRequestException.class)
        @UnexpectedResponseExceptionType(MyRestException.class)
        void testMethod4();
    }

    @Test
    public void withExpectedResponseAndMappedUnexpectedResponseExceptionTypeWithFallthrough() {
        final Method testMethod4 = TestInterface4.class.getDeclaredMethods()[0];
        assertEquals("testMethod4", testMethod4.getName());

        final SwaggerMethodParser methodParser = new SwaggerMethodParser(testMethod4, RestProxy.createDefaultSerializer(), "https://raw.host.com");
        assertEquals("com.azure.common.implementation.SwaggerMethodParserTests$TestInterface4.testMethod4", methodParser.fullyQualifiedMethodName());
        assertEquals(HttpMethod.PATCH, methodParser.httpMethod());
        assertArrayEquals(new int[] { 200 }, methodParser.expectedStatusCodes());
        assertEquals(ServiceRequestException.class, methodParser.getUnexpectedException(400).exceptionType());
        assertEquals(Object.class, methodParser.getUnexpectedException(400).exceptionBodyType());
        assertEquals(MyRestException.class, methodParser.getUnexpectedException(-1).exceptionType());
        assertEquals(HttpBinJSON.class, methodParser.getUnexpectedException(-1).exceptionBodyType());
        assertEquals(false, methodParser.headers(null).iterator().hasNext());
        assertEquals("https", methodParser.scheme(null));
        assertEquals("raw.host.com", methodParser.host(null));
    }

    interface TestInterface5 {
        @PATCH("my/rest/api/path")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {400}, value = MyRestException.class)
        void testMethod5();
    }

    @Test
    public void withExpectedResponseAndMappedUnexpectedResponseExceptionTypeWithoutFallthrough() {
        final Method testMethod5 = TestInterface5.class.getDeclaredMethods()[0];
        assertEquals("testMethod5", testMethod5.getName());

        final SwaggerMethodParser methodParser = new SwaggerMethodParser(testMethod5, RestProxy.createDefaultSerializer(), "https://raw.host.com");
        assertEquals("com.azure.common.implementation.SwaggerMethodParserTests$TestInterface5.testMethod5", methodParser.fullyQualifiedMethodName());
        assertEquals(HttpMethod.PATCH, methodParser.httpMethod());
        assertArrayEquals(new int[] { 200 }, methodParser.expectedStatusCodes());
        assertEquals(MyRestException.class, methodParser.getUnexpectedException(400).exceptionType());
        assertEquals(HttpBinJSON.class, methodParser.getUnexpectedException(400).exceptionBodyType());
        assertEquals(ServiceRequestException.class, methodParser.getUnexpectedException(-1).exceptionType());
        assertEquals(Object.class, methodParser.getUnexpectedException(-1).exceptionBodyType());
        assertEquals(false, methodParser.headers(null).iterator().hasNext());
        assertEquals("https", methodParser.scheme(null));
        assertEquals("raw.host.com", methodParser.host(null));
    }

    interface TestInterface6 {
        @PATCH("my/rest/api/path")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(code = {400, 401}, value = MyRestException.class)
        @UnexpectedResponseExceptionType(code = {404, 409}, value = ServiceRequestException.class)
        @UnexpectedResponseExceptionType(MyOtherRestException.class)
        void testMethod6();
    }

    @Test
    public void withExpectedResponseAndMultipleMappedUnexpectedResponseExceptionTypes() {
        final Method testMethod6 = TestInterface6.class.getDeclaredMethods()[0];
        assertEquals("testMethod6", testMethod6.getName());

        final SwaggerMethodParser methodParser = new SwaggerMethodParser(testMethod6, RestProxy.createDefaultSerializer(), "https://raw.host.com");
        assertEquals("com.azure.common.implementation.SwaggerMethodParserTests$TestInterface6.testMethod6", methodParser.fullyQualifiedMethodName());
        assertEquals(HttpMethod.PATCH, methodParser.httpMethod());
        assertArrayEquals(new int[] { 200 }, methodParser.expectedStatusCodes());
        assertEquals(MyRestException.class, methodParser.getUnexpectedException(400).exceptionType());
        assertEquals(HttpBinJSON.class, methodParser.getUnexpectedException(400).exceptionBodyType());
        assertEquals(MyRestException.class, methodParser.getUnexpectedException(401).exceptionType());
        assertEquals(HttpBinJSON.class, methodParser.getUnexpectedException(401).exceptionBodyType());
        assertEquals(ServiceRequestException.class, methodParser.getUnexpectedException(404).exceptionType());
        assertEquals(Object.class, methodParser.getUnexpectedException(404).exceptionBodyType());
        assertEquals(ServiceRequestException.class, methodParser.getUnexpectedException(409).exceptionType());
        assertEquals(Object.class, methodParser.getUnexpectedException(409).exceptionBodyType());
        assertEquals(MyOtherRestException.class, methodParser.getUnexpectedException(-1).exceptionType());
        assertEquals(HttpBinJSON.class, methodParser.getUnexpectedException(-1).exceptionBodyType());
        assertEquals(false, methodParser.headers(null).iterator().hasNext());
        assertEquals("https", methodParser.scheme(null));
        assertEquals("raw.host.com", methodParser.host(null));
    }

    interface TestInterface7 {
        @PATCH("my/rest/api/path")
        @UnexpectedResponseExceptionType(MyRestException.class)
        @UnexpectedResponseExceptionType(ServiceRequestException.class)
        void testMethod7();
    }

    @Test
    public void withInvalidUnexpectedResponseAnnotationsExceptionByDefaultAnnotation() {
        final Method testMethod7 = TestInterface7.class.getDeclaredMethods()[0];
        assertEquals("testMethod7", testMethod7.getName());

        final SwaggerMethodParser methodParser = new SwaggerMethodParser(testMethod7, RestProxy.createDefaultSerializer(), "https://raw.host.com");
        assertEquals("com.azure.common.implementation.SwaggerMethodParserTests$TestInterface7.testMethod7", methodParser.fullyQualifiedMethodName());
        assertEquals(ServiceRequestException.class, methodParser.getUnexpectedException(-1).exceptionType());
        assertEquals(Object.class, methodParser.getUnexpectedException(-1).exceptionBodyType());
        assertEquals(false, methodParser.headers(null).iterator().hasNext());
        assertEquals("https", methodParser.scheme(null));
        assertEquals("raw.host.com", methodParser.host(null));
    }

    interface TestInterface8 {
        @PATCH("my/rest/api/path")
        @UnexpectedResponseExceptionType(code = {404}, value = MyRestException.class)
        @UnexpectedResponseExceptionType(code = {404}, value = ServiceRequestException.class)
        void testMethod8();
    }

    @Test
    public void withInvalidUnexpectedResponseAnnotationsExceptionByRepeatCodes() {
        final Method testMethod8 = TestInterface8.class.getDeclaredMethods()[0];
        assertEquals("testMethod8", testMethod8.getName());

        final SwaggerMethodParser methodParser = new SwaggerMethodParser(testMethod8, RestProxy.createDefaultSerializer(), "https://raw.host.com");
        assertEquals("com.azure.common.implementation.SwaggerMethodParserTests$TestInterface8.testMethod8", methodParser.fullyQualifiedMethodName());
        assertEquals(ServiceRequestException.class, methodParser.getUnexpectedException(404).exceptionType());
        assertEquals(Object.class, methodParser.getUnexpectedException(404).exceptionBodyType());
        assertEquals(ServiceRequestException.class, methodParser.getUnexpectedException(-1).exceptionType());
        assertEquals(Object.class, methodParser.getUnexpectedException(-1).exceptionBodyType());
        assertEquals(false, methodParser.headers(null).iterator().hasNext());
        assertEquals("https", methodParser.scheme(null));
        assertEquals("raw.host.com", methodParser.host(null));
    }
}
