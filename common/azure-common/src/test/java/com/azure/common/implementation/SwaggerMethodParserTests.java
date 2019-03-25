package com.azure.common.implementation;

import com.azure.common.implementation.exception.MissingRequiredAnnotationException;
import com.azure.common.MyRestException;
import com.azure.common.http.rest.RestException;
import com.azure.common.annotations.ExpectedResponses;
import com.azure.common.annotations.PATCH;
import com.azure.common.annotations.UnexpectedResponseExceptionType;
import com.azure.common.entities.HttpBinJSON;
import com.azure.common.http.HttpMethod;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

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
        assertEquals(RestException.class, methodParser.exceptionType());
        assertEquals(Object.class, methodParser.exceptionBodyType());
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
        assertEquals(MyRestException.class, methodParser.exceptionType());
        assertEquals(HttpBinJSON.class, methodParser.exceptionBodyType());
        assertEquals(false, methodParser.headers(null).iterator().hasNext());
        assertEquals("https", methodParser.scheme(null));
        assertEquals("raw.host.com", methodParser.host(null));
    }
}
