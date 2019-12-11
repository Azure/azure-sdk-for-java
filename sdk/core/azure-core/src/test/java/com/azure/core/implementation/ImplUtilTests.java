// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.http.ContentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ImplUtilTests {
    @Test
    public void testPrettyPrintFormatJsonOrXmlWithJsonContent() {
        String plainJsonContent = "{\"error\":{\"code\":\"MethodNotAllowed\",\"message\":\"HTTP POST not allowed\"}}";
        String prettyJsonContent = "{" + System.lineSeparator()
            + "  \"error\" : {" + System.lineSeparator()
            + "    \"code\" : \"MethodNotAllowed\"," + System.lineSeparator()
            + "    \"message\" : \"HTTP POST not allowed\"" + System.lineSeparator()
            + "  }" + System.lineSeparator()
            + "}";
        assertEquals(ImplUtil.printPrettyFormatJsonOrXml(plainJsonContent,
            ContentType.APPLICATION_JSON), prettyJsonContent);
    }

    @Test
    public void testPrettyPrintFormatJsonOrXmlWithXmlContent() {
        String plainXmlContent = "<error><errorCode>InvalidRequest</errorCode><message>This is wrong.</message></error>";
        String prettyXmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><error>" + System.lineSeparator()
            + "    <errorCode>InvalidRequest</errorCode>" + System.lineSeparator()
            + "    <message>This is wrong.</message>" + System.lineSeparator()
            + "</error>" + System.lineSeparator();
        assertEquals(ImplUtil.printPrettyFormatJsonOrXml(plainXmlContent,
            ContentType.APPLICATION_XML), prettyXmlContent);
    }

    @Test
    public void testPrettyPrintFormatJsonOrXmlWithInvalidJsonContent() {
        String plainJsonContent = "{\"error\":\"code\":\"MethodNotAllowed\",\"message\":\"HTTP POST not allowed\"}}";
        assertEquals(ImplUtil.printPrettyFormatJsonOrXml(plainJsonContent,
            ContentType.APPLICATION_JSON), plainJsonContent);
    }

    @Test
    public void testPrettyPrintFormatJsonOrXmlWithInvalidXmlContent() {
        String plainJsonContent = "{\"error\":\"code\":\"MethodNotAllowed\",\"message\":\"HTTP POST not allowed\"}}";
        assertEquals(ImplUtil.printPrettyFormatJsonOrXml(plainJsonContent,
            ContentType.APPLICATION_JSON), plainJsonContent);
    }

    @Test
    public void testPrettyPrintFormatJsonOrXmlWithNullContent() {
        assertNull(ImplUtil.printPrettyFormatJsonOrXml(null, ContentType.APPLICATION_JSON));
    }

    @Test
    public void testPrettyPrintFormatJsonOrXmlWithNullContentType() {
        String content = "some content";
        assertEquals(ImplUtil.printPrettyFormatJsonOrXml(content, null), content);
    }

    @Test
    public void testPrettyPrintFormatJsonOrXmlWithOtherType() {
        String content = "some content";
        assertEquals(ImplUtil.printPrettyFormatJsonOrXml(content, "application/abc"), content);
    }
}
