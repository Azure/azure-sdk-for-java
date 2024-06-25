// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import com.azure.core.v2.annotation.ExpectedResponses;
import com.azure.core.v2.annotation.Get;
import com.azure.core.v2.annotation.Host;
import com.azure.core.v2.annotation.ServiceInterface;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class SwaggerInterfaceParserTests {

    interface TestInterface1 {
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
        assertThrows(MissingRequiredAnnotationException.class,
            () -> SwaggerInterfaceParser.getInstance(TestInterface1.class));
    }

    @Test
    public void hostWithNoServiceNameAnnotation() {
        assertThrows(MissingRequiredAnnotationException.class,
            () -> SwaggerInterfaceParser.getInstance(TestInterface2.class));
    }

    @Test
    public void hostWithHostAnnotation() {
        final SwaggerInterfaceParser interfaceParser = SwaggerInterfaceParser.getInstance(TestInterface3.class);
        assertEquals("https://management.azure.com", interfaceParser.getHost());
        assertEquals("myService", interfaceParser.getServiceName());
    }

    @Host("https://azure.com")
    @ServiceInterface(name = "myService")
    interface TestInterface4 {
        @Get("my/url/path")
        @ExpectedResponses({ 200 })
        void testMethod4();
    }

    @Test
    public void methodParser() throws NoSuchMethodException {
        final SwaggerInterfaceParser interfaceParser = SwaggerInterfaceParser.getInstance(TestInterface4.class);
        final Method testMethod4 = TestInterface4.class.getDeclaredMethod("testMethod4");
        assertEquals("testMethod4", testMethod4.getName());

        final SwaggerMethodParser methodParser = interfaceParser.getMethodParser(testMethod4);
        assertNotNull(methodParser);
        assertEquals("com.azure.core.implementation.http.rest.SwaggerInterfaceParserTests$TestInterface4.testMethod4",
            methodParser.getFullyQualifiedMethodName());

        final SwaggerMethodParser methodDetails2 = interfaceParser.getMethodParser(testMethod4);
        assertSame(methodParser, methodDetails2);
    }
}
