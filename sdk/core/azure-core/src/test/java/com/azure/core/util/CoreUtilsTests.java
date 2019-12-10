// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.azure.core.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CoreUtilsTests {
    @Test
    public void findFirstOfTypeEmptyArgs() {
        assertNull(CoreUtils.findFirstOfType(null, Integer.class));
    }

    @Test
    public void findFirstOfTypeWithOneOfType() {
        int expected = 1;
        Object[] args = { "string", expected };
        int actual = CoreUtils.findFirstOfType(args, Integer.class);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void findFirstOfTypeWithMultipleOfType() {
        int expected = 1;
        Object[] args = { "string", expected, 10 };
        int actual = CoreUtils.findFirstOfType(args, Integer.class);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void findFirstOfTypeWithNoneOfType() {
        Object[] args = { "string", "anotherString" };
        assertNull(CoreUtils.findFirstOfType(args, Integer.class));
    }

    @Test
    public void testProperties() {
        assertNotNull(CoreUtils.getProperties("azure-core.properties").get("version"));
        assertNotNull(CoreUtils.getProperties("azure-core.properties").get("name"));
        assertTrue(CoreUtils.getProperties("azure-core.properties").get("version")
            .matches("\\d.\\d.\\d([-a-zA-Z0-9.])*"));
    }

    @Test
    public void testMissingProperties() {
        assertNotNull(CoreUtils.getProperties("foo.properties"));
        assertTrue(CoreUtils.getProperties("foo.properties").isEmpty());
        assertNull(CoreUtils.getProperties("azure-core.properties").get("foo"));
    }

    @Test
    public void testPrettyPrintFormatJsonOrXmlWithJsonContent() {
        String plainJsonContent = "{\"error\":{\"code\":\"MethodNotAllowed\",\"message\":\"HTTP POST not allowed\"}}";
        String prettyJsonContent = "{\r\n"
            + "  \"error\" : {\r\n"
            + "    \"code\" : \"MethodNotAllowed\",\r\n"
            + "    \"message\" : \"HTTP POST not allowed\"\r\n"
            + "  }\r\n"
            + "}";
        assertEquals(CoreUtils.printPrettyFormatJsonOrXml(plainJsonContent,
            ContentType.APPLICATION_JSON), prettyJsonContent);
    }

    @Test
    public void testPrettyPrintFormatJsonOrXmlWithXmlContent() {
        String plainXmlContent = "<error><errorCode>InvalidRequest</errorCode><message>This is wrong.</message></error>";
        String prettyXmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><error>\r\n"
            + "    <errorCode>InvalidRequest</errorCode>\r\n"
            + "    <message>This is wrong.</message>\r\n"
            + "</error>\r\n";
        assertEquals(CoreUtils.printPrettyFormatJsonOrXml(plainXmlContent,
            ContentType.APPLICATION_XML), prettyXmlContent);
    }

    @Test
    public void testPrettyPrintFormatJsonOrXmlWithInvalidJsonContent() {
        String plainJsonContent = "{\"error\":\"code\":\"MethodNotAllowed\",\"message\":\"HTTP POST not allowed\"}}";
        assertEquals(CoreUtils.printPrettyFormatJsonOrXml(plainJsonContent,
            ContentType.APPLICATION_JSON), plainJsonContent);
    }

    @Test
    public void testPrettyPrintFormatJsonOrXmlWithInvalidXmlContent() {
        String plainJsonContent = "{\"error\":\"code\":\"MethodNotAllowed\",\"message\":\"HTTP POST not allowed\"}}";
        assertEquals(CoreUtils.printPrettyFormatJsonOrXml(plainJsonContent,
            ContentType.APPLICATION_JSON), plainJsonContent);
    }

    @Test
    public void testPrettyPrintFormatJsonOrXmlWithNullContent() {
        assertNull(CoreUtils.printPrettyFormatJsonOrXml(null, ContentType.APPLICATION_JSON));
    }

    @Test
    public void testPrettyPrintFormatJsonOrXmlWithNullContentType() {
        String content = "some content";
        assertEquals(CoreUtils.printPrettyFormatJsonOrXml(content, null), content);
    }

    @Test
    public void testPrettyPrintFormatJsonOrXmlWithOtherType() {
        String content = "some content";
        assertEquals(CoreUtils.printPrettyFormatJsonOrXml(content, "application/abc"), content);
    }
}
