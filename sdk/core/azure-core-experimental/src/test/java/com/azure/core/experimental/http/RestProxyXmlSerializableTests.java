// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.http;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.xml.XmlSerializable;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests that {@link XmlSerializable} is supported by {@code RestProxy} when {@code azure-xml} is included as a
 * dependency.
 */
public class RestProxyXmlSerializableTests {

    @Host("http://localhost")
    @ServiceInterface(name = "XmlSerializable")
    public interface SimpleXmlSerializableProxy {
        @Put("sendXmlSerializable")
        @ExpectedResponses({ 200 })
        void sendXmlSerializable(@BodyParam("application/xml") SimpleXmlSerializable simpleXmlSerializable);

        @Get("getXmlSerializable")
        @ExpectedResponses({ 200 })
        SimpleXmlSerializable getXmlSerializable();

        @Get("getInvalidXmlSerializable")
        @ExpectedResponses({ 200 })
        SimpleXmlSerializable getInvalidXmlSerializable();
    }

    @Test
    public void sendXmlSerializableRequest() {
        SimpleXmlSerializable xmlSerializable = new SimpleXmlSerializable(true, 10, 10.D, "10");
        String singleQuoteXmlDeclaration = "<?xml version='1.0' encoding='utf-8'?>";
        String doubleQuoteXmlDeclaration = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
        String expectedBody
            = "<SimpleXml boolean=\"true\" decimal=\"10.0\"><int>10</int><string>10</string></SimpleXml>";

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request -> {
            String body = request.getBodyAsBinaryData().toString();
            if (body.startsWith(singleQuoteXmlDeclaration)) {
                assertEquals(singleQuoteXmlDeclaration + expectedBody, body);
            } else {
                assertEquals(doubleQuoteXmlDeclaration + expectedBody, body);
            }
            return Mono.just(new MockHttpResponse(request, 200, null, SerializerEncoding.XML));
        }).build();

        SimpleXmlSerializableProxy proxy = RestProxy.create(SimpleXmlSerializableProxy.class, pipeline);
        proxy.sendXmlSerializable(xmlSerializable);
    }

    @Test
    public void receiveXmlSerializableResponse() {
        String response = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<SimpleXml boolean=\"true\" decimal=\"10.0\">"
            + "<int>10</int>" + "<string>10</string>" + "</SimpleXml>";

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> Mono.just(
                new MockHttpResponse(request, 200, response.getBytes(StandardCharsets.UTF_8), SerializerEncoding.XML)))
            .build();

        SimpleXmlSerializableProxy proxy = RestProxy.create(SimpleXmlSerializableProxy.class, pipeline);

        SimpleXmlSerializable xmlSerializable = proxy.getXmlSerializable();

        assertEquals(true, xmlSerializable.isABoolean());
        assertEquals(10, xmlSerializable.getAnInt());
        assertEquals(10.0D, xmlSerializable.getADecimal());
        assertEquals("10", xmlSerializable.getAString());
    }

    @Test
    public void invalidXmlSerializableResponse() {
        String response = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<SimpleXml boolean=\"true\" decimal=\"10.0\"></SimpleXml>";

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> Mono.just(
                new MockHttpResponse(request, 200, response.getBytes(StandardCharsets.UTF_8), SerializerEncoding.XML)))
            .build();

        SimpleXmlSerializableProxy proxy = RestProxy.create(SimpleXmlSerializableProxy.class, pipeline);
        assertThrows(HttpResponseException.class, proxy::getInvalidXmlSerializable);
    }
}
