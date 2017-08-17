package com.microsoft.rest.v2;

import com.microsoft.rest.v2.annotations.Host;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class SwaggerInterfaceParserTests {

    interface TestInterface1 {
        String testMethod1();
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
    public void methodParser() {
        final SwaggerInterfaceParser interfaceParser = new SwaggerInterfaceParser(TestInterface1.class);
        final Method testMethod1 = TestInterface1.class.getDeclaredMethods()[0];
        assertEquals("testMethod1", testMethod1.getName());

        final SwaggerMethodParser methodParser = interfaceParser.methodParser(testMethod1);
        assertNotNull(methodParser);
        assertEquals("com.microsoft.rest.v2.SwaggerInterfaceParserTests.TestInterface1.testMethod1", methodParser.fullyQualifiedMethodName());

        final SwaggerMethodParser methodDetails2 = interfaceParser.methodParser(testMethod1);
        assertSame(methodParser, methodDetails2);
    }
}
