// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.rest;

import io.clientcore.core.annotation.ServiceInterface;
import io.clientcore.core.http.MockHttpResponse;
import io.clientcore.core.http.RestProxy;
import io.clientcore.core.http.annotation.BodyParam;
import io.clientcore.core.http.annotation.HttpRequestInformation;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.serialization.xml.XmlSerializable;
import io.clientcore.core.util.binarydata.StringBinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests that {@link XmlSerializable} is supported by {@code RestProxy}.
 */
public class RestProxyXmlSerializableTests {

    @ServiceInterface(name = "XmlSerializable", host = "http://localhost")
    public interface SimpleXmlSerializableProxy {
        @HttpRequestInformation(method = HttpMethod.PUT, path = "sendApplicationXml", expectedStatusCodes = { 200 })
        void sendApplicationXml(@BodyParam("application/xml") SimpleXmlSerializable simpleXmlSerializable);

        @HttpRequestInformation(method = HttpMethod.PUT, path = "sendTextXml", expectedStatusCodes = { 200 })
        void sendTextXml(@BodyParam("text/xml") SimpleXmlSerializable simpleXmlSerializable);

        @HttpRequestInformation(method = HttpMethod.GET, path = "getXml", expectedStatusCodes = { 200 })
        SimpleXmlSerializable getXml();

        @HttpRequestInformation(method = HttpMethod.GET, path = "getInvalidXml", expectedStatusCodes = { 200 })
        SimpleXmlSerializable getInvalidXml();
    }

    @Test
    public void sendApplicationXml() {
        sendXmlShared(SimpleXmlSerializableProxy::sendApplicationXml);
    }

    @Test
    public void sendTextXml() {
        sendXmlShared(SimpleXmlSerializableProxy::sendTextXml);
    }

    private static void sendXmlShared(BiConsumer<SimpleXmlSerializableProxy, SimpleXmlSerializable> restCall) {
        SimpleXmlSerializable xmlSerializable = new SimpleXmlSerializable(true, 10, 10.D, "10");
        String singleQuoteXmlDeclaration = "<?xml version='1.0' encoding='UTF-8'?>";
        String doubleQuoteXmlDeclaration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        String expectedBody
            = "<SimpleXml boolean=\"true\" decimal=\"10.0\"><int>10</int><string>10</string></SimpleXml>";

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request -> {
            String body = request.getBody().toString();
            if (body.startsWith(singleQuoteXmlDeclaration)) {
                assertEquals(singleQuoteXmlDeclaration + expectedBody, body);
            } else {
                assertEquals(doubleQuoteXmlDeclaration + expectedBody, body);
            }
            return new MockHttpResponse(request, 200);
        }).build();

        SimpleXmlSerializableProxy proxy = RestProxy.create(SimpleXmlSerializableProxy.class, pipeline);
        restCall.accept(proxy, xmlSerializable);
    }

    @ParameterizedTest
    @ValueSource(strings = { "application/xml", "application/xml;charset=utf-8", "text/xml" })
    public void getXml(String contentType) {
        String response = "<?xml version=\"1.0\" encoding=\"utf-8\"?><SimpleXml boolean=\"true\" decimal=\"10.0\">"
            + "<int>10</int><string>10</string></SimpleXml>";

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> new MockHttpResponse(request, 200,
                new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, contentType), new StringBinaryData(response)))
            .build();

        SimpleXmlSerializableProxy proxy = RestProxy.create(SimpleXmlSerializableProxy.class, pipeline);

        SimpleXmlSerializable xmlSerializable = proxy.getXml();

        assertEquals(true, xmlSerializable.isABoolean());
        assertEquals(10, xmlSerializable.getAnInt());
        assertEquals(10.0D, xmlSerializable.getADecimal());
        assertEquals("10", xmlSerializable.getAString());
    }

    @ParameterizedTest
    @ValueSource(strings = { "application/xml", "application/xml;charset=utf-8", "text/xml" })
    public void getInvalidXml(String contentType) {
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<SimpleXml boolean=\"true\" decimal=\"10.0\"></SimpleXml>";

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> new MockHttpResponse(request, 200,
                new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, contentType), new StringBinaryData(response)))
            .build();

        SimpleXmlSerializableProxy proxy = RestProxy.create(SimpleXmlSerializableProxy.class, pipeline);
        assertThrows(RuntimeException.class, proxy::getInvalidXml);
    }
}
