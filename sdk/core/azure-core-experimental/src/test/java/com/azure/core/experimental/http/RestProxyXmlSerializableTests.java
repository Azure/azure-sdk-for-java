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
import com.azure.xml.XmlReader;
import com.azure.xml.XmlSerializable;
import com.azure.xml.XmlToken;
import com.azure.xml.XmlWriter;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import javax.xml.stream.XMLStreamException;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests that {@link XmlSerializable} is supported by {@code RestProxy} when {@code azure-xml} is included as a
 * dependency.
 */
public class RestProxyXmlSerializableTests {
    public static final class SimpleXmlSerializable implements XmlSerializable<SimpleXmlSerializable> {
        private final boolean aBooleanAsAttribute;
        private final int anInt;
        private final double aDecimalAsAttribute;
        private final String aString;

        public SimpleXmlSerializable(boolean aBooleanAsAttribute, int anInt, double aDecimalAsAttribute,
            String aString) {
            this.aBooleanAsAttribute = aBooleanAsAttribute;
            this.anInt = anInt;
            this.aDecimalAsAttribute = aDecimalAsAttribute;
            this.aString = aString;
        }

        @Override
        public XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException {
            xmlWriter.writeStartElement("SimpleXml");

            xmlWriter.writeBooleanAttribute("aBoolean", aBooleanAsAttribute);
            xmlWriter.writeDoubleAttribute("aDecimal", aDecimalAsAttribute);

            xmlWriter.writeIntElement("anInt", anInt);
            xmlWriter.writeStringElement("aString", aString);

            return xmlWriter.writeEndElement();
        }

        public static SimpleXmlSerializable fromXml(XmlReader xmlReader) throws XMLStreamException {
            return xmlReader.readObject("SimpleXml", reader -> {
                boolean aBooleanAsAttribute = xmlReader.getBooleanAttribute(null, "aBoolean");
                double aDecimalAsAttribute = xmlReader.getDoubleAttribute(null, "aDecimal");
                int anInt = 0;
                boolean foundAnInt = false;
                String aString = null;
                boolean foundAString = false;

                while (xmlReader.nextElement() != XmlToken.END_ELEMENT) {
                    String elementName = xmlReader.getElementName().getLocalPart();
                    if ("anInt".equals(elementName)) {
                        anInt = xmlReader.getIntElement();
                        foundAnInt = true;
                    } else if ("aString".equals(elementName)) {
                        aString = xmlReader.getStringElement();
                        foundAString = true;
                    } else {
                        xmlReader.skipElement();
                    }
                }

                if (foundAnInt && foundAString) {
                    return new SimpleXmlSerializable(aBooleanAsAttribute, anInt, aDecimalAsAttribute, aString);
                }

                throw new IllegalStateException("Missing required elements.");
            });
        }
    }

    @Host("http://localhost")
    @ServiceInterface(name = "XmlSerializable")
    public interface SimpleXmlSerializableProxy {
        @Put("sendXmlSerializable")
        @ExpectedResponses({200})
        void sendXmlSerializable(@BodyParam("application/xml") SimpleXmlSerializable simpleXmlSerializable);

        @Get("getXmlSerializable")
        @ExpectedResponses({200})
        SimpleXmlSerializable getXmlSerializable();

        @Get("getInvalidXmlSerializable")
        @ExpectedResponses({200})
        SimpleXmlSerializable getInvalidXmlSerializable();
    }

    @Test
    public void sendXmlSerializableRequest() {
        SimpleXmlSerializable xmlSerializable = new SimpleXmlSerializable(true, 10, 10.D, "10");
        String singleQuoteXmlDeclaration = "<?xml version='1.0' encoding='utf-8'?>";
        String doubleQuoteXmlDeclaration = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
        String expectedBody
            = "<SimpleXml aBoolean=\"true\" aDecimal=\"10.0\"><anInt>10</anInt><aString>10</aString></SimpleXml>";

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> {
                String body = request.getBodyAsBinaryData().toString();
                if (body.startsWith(singleQuoteXmlDeclaration)) {
                    assertEquals(singleQuoteXmlDeclaration + expectedBody, body);
                } else {
                    assertEquals(doubleQuoteXmlDeclaration + expectedBody, body);
                }
                return Mono.just(new MockHttpResponse(request, 200, null, SerializerEncoding.XML));
            })
            .build();

        SimpleXmlSerializableProxy proxy = RestProxy.create(SimpleXmlSerializableProxy.class, pipeline);
        proxy.sendXmlSerializable(xmlSerializable);
    }

    @Test
    public void receiveXmlSerializableResponse() {
        String response = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<SimpleXml aBoolean=\"true\" aDecimal=\"10.0\">"
            + "<anInt>10</anInt>"
            + "<aString>10</aString>"
            + "</SimpleXml>";

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 200,
                response.getBytes(StandardCharsets.UTF_8), SerializerEncoding.XML)))
            .build();

        SimpleXmlSerializableProxy proxy = RestProxy.create(SimpleXmlSerializableProxy.class, pipeline);

        SimpleXmlSerializable xmlSerializable = proxy.getXmlSerializable();

        assertEquals(true, xmlSerializable.aBooleanAsAttribute);
        assertEquals(10, xmlSerializable.anInt);
        assertEquals(10.0D, xmlSerializable.aDecimalAsAttribute);
        assertEquals("10", xmlSerializable.aString);
    }

    @Test
    public void invalidXmlSerializableResponse() {
        String response = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<SimpleXml aBoolean=\"true\" aDecimal=\"10.0\"></SimpleXml>";

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 200,
                response.getBytes(StandardCharsets.UTF_8), SerializerEncoding.XML)))
            .build();

        SimpleXmlSerializableProxy proxy = RestProxy.create(SimpleXmlSerializableProxy.class, pipeline);
        assertThrows(HttpResponseException.class, proxy::getInvalidXmlSerializable);
    }
}
