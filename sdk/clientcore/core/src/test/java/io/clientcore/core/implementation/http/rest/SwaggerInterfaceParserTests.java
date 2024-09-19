// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.rest;

import io.clientcore.core.annotation.ServiceInterface;
import io.clientcore.core.http.annotation.HttpRequestInformation;
import io.clientcore.core.http.models.HttpMethod;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SwaggerInterfaceParserTests {
    interface TestInterface1 {
    }

    @ServiceInterface(name = "myService")
    interface TestInterface2 {
    }

    @ServiceInterface(name = "myService", host = "https://management.azure.com")
    interface TestInterface3 {
    }

    @Test
    public void serviceWithNoServiceInterfaceAnnotation() {
        assertThrows(MissingRequiredAnnotationException.class,
            () -> SwaggerInterfaceParser.getInstance(TestInterface1.class));
    }

    @Test
    public void serviceWithNoHostInServiceInterfaceAnnotation() {
        final SwaggerInterfaceParser interfaceParser = SwaggerInterfaceParser.getInstance(TestInterface2.class);
        assertEquals("", interfaceParser.getHost());
        assertEquals("myService", interfaceParser.getServiceName());
    }

    @Test
    public void serviceWithServiceInterfaceAnnotation() {
        final SwaggerInterfaceParser interfaceParser = SwaggerInterfaceParser.getInstance(TestInterface3.class);
        assertEquals("https://management.azure.com", interfaceParser.getHost());
        assertEquals("myService", interfaceParser.getServiceName());
    }

    @ServiceInterface(name = "myService", host = "https://azure.com")
    interface TestInterface4 {
        @HttpRequestInformation(method = HttpMethod.GET, path = "my/uri/path", expectedStatusCodes = {200})
        void testMethod4();
    }

    @Test
    public void methodParser() throws NoSuchMethodException {
        final SwaggerInterfaceParser interfaceParser = SwaggerInterfaceParser.getInstance(TestInterface4.class);
        final Method testMethod4 = TestInterface4.class.getDeclaredMethod("testMethod4");

        assertEquals("testMethod4", testMethod4.getName());

        final SwaggerMethodParser methodParser = interfaceParser.getMethodParser(testMethod4);

        assertNotNull(methodParser);
        assertEquals("io.clientcore.core.implementation.http.rest.SwaggerInterfaceParserTests$TestInterface4.testMethod4",
            methodParser.getFullyQualifiedMethodName());

        final SwaggerMethodParser methodDetails2 = interfaceParser.getMethodParser(testMethod4);

        assertSame(methodParser, methodDetails2);
    }
}
