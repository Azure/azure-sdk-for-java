// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.ServiceInterface;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class SwaggerInterfaceParserTests {

    interface TestInterface1 {
        String testMethod1();
    }

    @Host("https://management.azure.com")
    interface TestInterface2 {
    }

    @Host("https://management.azure.com")
    @ServiceInterface(name = "myService")
    interface TestInterface3 {
    }

    @Test
    public void hostWithNoHostAnnotation() {
        assertThrows(MissingRequiredAnnotationException.class, () -> new SwaggerInterfaceParser(TestInterface1.class, null));
    }

    @Test
    public void hostWithNoServiceNameAnnotation() {
        assertThrows(MissingRequiredAnnotationException.class, () -> new SwaggerInterfaceParser(TestInterface2.class, null));
    }

    @Test
    public void hostWithHostAnnotation() {
        final SwaggerInterfaceParser interfaceParser = new SwaggerInterfaceParser(TestInterface3.class, null);
        assertEquals("https://management.azure.com", interfaceParser.getHost());
        assertEquals("myService", interfaceParser.getServiceName());
    }

    @Host("https://azure.com")
    @ServiceInterface(name = "myService")
    interface TestInterface4 {
        @Get("my/url/path")
        @ExpectedResponses({200})
        void testMethod4();
    }

    @Test
    public void methodParser() {
        final SwaggerInterfaceParser interfaceParser = new SwaggerInterfaceParser(TestInterface4.class, null);
        final Method testMethod3 = TestInterface4.class.getDeclaredMethods()[0];
        assertEquals("testMethod4", testMethod3.getName());

        final SwaggerMethodParser methodParser = interfaceParser.getMethodParser(testMethod3);
        assertNotNull(methodParser);
        assertEquals("com.azure.core.http.rest.SwaggerInterfaceParserTests$TestInterface4.testMethod4", methodParser.getFullyQualifiedMethodName());

        final SwaggerMethodParser methodDetails2 = interfaceParser.getMethodParser(testMethod3);
        assertSame(methodParser, methodDetails2);
    }
}
