package com.microsoft.rest.v2;

import com.microsoft.rest.v2.annotations.Host;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Iterator;

import static org.junit.Assert.*;

public class SwaggerMethodParserTests {

    interface TestInterface1 {
        void testMethod1();
    }

    @Test
    public void withNoAnnotations() {
        final Method testMethod1 = TestInterface1.class.getDeclaredMethods()[0];
        assertEquals("testMethod1", testMethod1.getName());

        final SwaggerMethodParser methodParser = new SwaggerMethodParser(testMethod1, "https://raw.host.com");
        assertEquals("com.microsoft.rest.v2.SwaggerMethodParserTests.TestInterface1.testMethod1", methodParser.fullyQualifiedMethodName());
        assertEquals(null, methodParser.httpMethod());
        assertEquals(false, methodParser.headers(null).iterator().hasNext());
        assertEquals("https", methodParser.scheme(null));
        assertEquals("raw.host.com", methodParser.host(null));
    }
}
