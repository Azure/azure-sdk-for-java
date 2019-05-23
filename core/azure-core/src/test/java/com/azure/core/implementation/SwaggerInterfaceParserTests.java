// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.annotations.ExpectedResponses;
import com.azure.core.annotations.GET;
import com.azure.core.annotations.Host;
import com.azure.core.annotations.Service;
import com.azure.core.implementation.exception.MissingRequiredAnnotationException;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class SwaggerInterfaceParserTests {

    interface TestInterface1 {
        String testMethod1();
    }

    @Host("https://management.azure.com")
    interface TestInterface2 {
    }

    @Host("https://management.azure.com")
    @Service("myService")
    interface TestInterface3 {
    }

    @Test(expected = MissingRequiredAnnotationException.class)
    public void hostWithNoHostAnnotation() {
        new SwaggerInterfaceParser(TestInterface1.class, null);
    }

    @Test(expected = MissingRequiredAnnotationException.class)
    public void hostWithNoServiceNameAnnotation() {
        new SwaggerInterfaceParser(TestInterface2.class, null);
    }

    @Test
    public void hostWithHostAnnotation() {
        final SwaggerInterfaceParser interfaceParser = new SwaggerInterfaceParser(TestInterface3.class, null);
        assertEquals("https://management.azure.com", interfaceParser.host());
        assertEquals("myService", interfaceParser.service());
    }

    @Host("https://azure.com")
    @Service("myService")
    interface TestInterface4 {
        @GET("my/url/path")
        @ExpectedResponses({200})
        void testMethod4();
    }

    @Test
    public void methodParser() {
        final SwaggerInterfaceParser interfaceParser = new SwaggerInterfaceParser(TestInterface4.class, null);
        final Method testMethod3 = TestInterface4.class.getDeclaredMethods()[0];
        assertEquals("testMethod4", testMethod3.getName());

        final SwaggerMethodParser methodParser = interfaceParser.methodParser(testMethod3);
        assertNotNull(methodParser);
        assertEquals("com.azure.core.implementation.SwaggerInterfaceParserTests$TestInterface4.testMethod4", methodParser.fullyQualifiedMethodName());

        final SwaggerMethodParser methodDetails2 = interfaceParser.methodParser(testMethod3);
        assertSame(methodParser, methodDetails2);
    }
}
