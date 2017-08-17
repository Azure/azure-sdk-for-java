package com.microsoft.rest.v2;

import com.microsoft.rest.v2.annotations.Host;
import org.junit.Test;

import static org.junit.Assert.*;

public class SwaggerInterfaceParserTests {

    interface TestInterface1 {
    }

    @Host("https://management.azure.com")
    interface TestInterface2 {
    }

    @Test
    public void hostWithNoHostAnnotation() {
        final SwaggerInterfaceParser interfaceParser = new SwaggerInterfaceParser(TestInterface1.class);
        assertEquals(null, interfaceParser.host());
    }

    @Test
    public void hostWithHostAnnotation() {
        final SwaggerInterfaceParser interfaceParser = new SwaggerInterfaceParser(TestInterface2.class);
        assertEquals("https://management.azure.com", interfaceParser.host());
    }

    @Test
    public void getMethodProxyDetails() {
        final SwaggerInterfaceParser interfaceParser = new SwaggerInterfaceParser(TestInterface1.class);
        final SwaggerMethodProxyDetails methodDetails = interfaceParser.getMethodProxyDetails("mockMethodName");
        assertNotNull(methodDetails);
        assertEquals("com.microsoft.rest.v2.SwaggerInterfaceParserTests.TestInterface1.mockMethodName", methodDetails.fullyQualifiedMethodName());
        final SwaggerMethodProxyDetails methodDetails2 = interfaceParser.getMethodProxyDetails("mockMethodName");
        assertSame(methodDetails, methodDetails2);
    }
}
