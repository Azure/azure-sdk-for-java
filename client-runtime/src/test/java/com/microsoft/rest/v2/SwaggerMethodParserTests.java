package com.microsoft.rest.v2;

import com.microsoft.rest.v2.annotations.ExpectedResponses;
import com.microsoft.rest.v2.annotations.PATCH;
import com.microsoft.rest.v2.annotations.UnexpectedResponseExceptionType;
import com.microsoft.rest.v2.entities.HttpBinJSON;
import org.junit.Test;

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

        new SwaggerMethodParser(testMethod1, "https://raw.host.com");
    }

    interface TestInterface2 {
        @PATCH("my/rest/api/path")
        @ExpectedResponses({200})
        void testMethod2();
    }

    @Test
    public void withOnlyExpectedResponse() {
        final Method testMethod2 = TestInterface2.class.getDeclaredMethods()[0];
        assertEquals("testMethod2", testMethod2.getName());

        final SwaggerMethodParser methodParser = new SwaggerMethodParser(testMethod2, "https://raw.host.com");
        assertEquals("com.microsoft.rest.v2.SwaggerMethodParserTests$TestInterface2.testMethod2", methodParser.fullyQualifiedMethodName());
        assertEquals("PATCH", methodParser.httpMethod());
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
    public void withExpectedResponseAndUnexpectedResponseExceptionType() {
        final Method testMethod3 = TestInterface3.class.getDeclaredMethods()[0];
        assertEquals("testMethod3", testMethod3.getName());

        final SwaggerMethodParser methodParser = new SwaggerMethodParser(testMethod3, "https://raw.host.com");
        assertEquals("com.microsoft.rest.v2.SwaggerMethodParserTests$TestInterface3.testMethod3", methodParser.fullyQualifiedMethodName());
        assertEquals("PATCH", methodParser.httpMethod());
        assertArrayEquals(new int[] { 200 }, methodParser.expectedStatusCodes());
        assertEquals(MyRestException.class, methodParser.exceptionType());
        assertEquals(HttpBinJSON.class, methodParser.exceptionBodyType());
        assertEquals(false, methodParser.headers(null).iterator().hasNext());
        assertEquals("https", methodParser.scheme(null));
        assertEquals("raw.host.com", methodParser.host(null));
    }
}
