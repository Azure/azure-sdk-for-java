// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.test.serializers;

import io.clientcore.annotation.processor.test.implementation.SimpleXmlSerializableService;
import io.clientcore.annotation.processor.test.implementation.models.SimpleXmlSerializable;
import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.models.binarydata.StringBinaryData;
import io.clientcore.core.serialization.xml.XmlSerializable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests that {@link XmlSerializable} is supported by annotation-processor.
 */
public class XmlSerializableTests {

    @Test
    public void sendApplicationXml() {
        sendXmlShared(SimpleXmlSerializableService::sendApplicationXml);
    }

    @Test
    public void sendTextXml() {
        sendXmlShared(SimpleXmlSerializableService::sendTextXml);
    }

    private static void sendXmlShared(BiConsumer<SimpleXmlSerializableService, SimpleXmlSerializable> restCall) {
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
            return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
        }).build();

        SimpleXmlSerializableService simpleXmlSerializableServiceImpl = SimpleXmlSerializableService.getNewInstance(pipeline);
        restCall.accept(simpleXmlSerializableServiceImpl, xmlSerializable);
    }

    @ParameterizedTest
    @ValueSource(strings = { "application/xml", "application/xml;charset=utf-8", "text/xml" })
    public void getXml(String contentType) {
        String response = "<?xml version=\"1.0\" encoding=\"utf-8\"?><SimpleXml boolean=\"true\" decimal=\"10.0\">"
            + "<int>10</int><string>10</string></SimpleXml>";

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> new Response<>(request.setHeaders(new HttpHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_TYPE,
                contentType)))
                , 200,
                new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, contentType), new StringBinaryData(response)))
            .build();

        SimpleXmlSerializableService simpleXmlSerializableServiceImpl = SimpleXmlSerializableService.getNewInstance(pipeline);

        SimpleXmlSerializable xmlSerializable = simpleXmlSerializableServiceImpl.getXml(contentType);

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
            .httpClient(request -> new Response<>(request.setHeaders(new HttpHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_TYPE,
                contentType))), 200,
                new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, contentType), new StringBinaryData(response)))
            .build();

        SimpleXmlSerializableService simpleXmlSerializableServiceImpl = SimpleXmlSerializableService.getNewInstance(pipeline);
        assertThrows(RuntimeException.class, () -> simpleXmlSerializableServiceImpl.getInvalidXml(contentType));
    }
}
