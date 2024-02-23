// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml.contract;

import com.azure.xml.XmlWriter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.xml.stream.XMLStreamException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the contract of {@link XmlWriter}.
 * <p>
 * All implementations of {@link XmlWriter} must create a subclass of this test class and pass all tests as they're
 * written to be considered an acceptable implementation.
 * <p>
 * Each test will only create a single instance of {@link XmlWriter} to simplify usage of {@link #getXmlWriter()} and
 * {@link #getXmlWriterContents()}.
 */
public abstract class XmlWriterContractTests {
    /**
     * Creates an instance of {@link XmlWriter} that will be used by a test.
     *
     * @return The {@link XmlWriter} that a test will use.
     */
    public abstract XmlWriter getXmlWriter();

    /**
     * Converts the content written to a {@link XmlWriter} during testing to a string representation.
     *
     * @return The contents of a {@link XmlWriter} converted to a string.
     */
    public abstract String getXmlWriterContents();

    @ParameterizedTest
    @MethodSource("basicElementOperationsSupplier")
    public void basicElementOperations(XMLStreamExceptionConsumer<XmlWriter> operation, String expectedXml) {
        assertDoesNotThrow(() -> operation.accept(getXmlWriter()));

        assertEquals(expectedXml, getXmlWriterContents());
    }

    private static Stream<Arguments> basicElementOperationsSupplier() {
        return Stream.of(
            // Element start and end.
            Arguments.of(createXmlConsumer(xmlWriter -> xmlWriter.writeStartElement("test").writeEndElement()),
                "<test></test>"),

            // Value handling.

            // Binary
            Arguments.of(createXmlConsumer(xmlWriter -> xmlWriter.writeBinaryElement("test", null)), ""),
            Arguments.of(createXmlConsumer(xmlWriter -> xmlWriter.writeBinaryElement("test", new byte[0])),
                "<test></test>"),
            Arguments.of(
                createXmlConsumer(
                    xmlWriter -> xmlWriter.writeBinaryElement("test", "Hello".getBytes(StandardCharsets.UTF_8))),
                "<test>" + Base64.getEncoder().encodeToString("Hello".getBytes(StandardCharsets.UTF_8)) + "</test>"),

            // Boolean
            Arguments.of(createXmlConsumer(xmlWriter -> xmlWriter.writeBooleanElement("test", null)), ""),
            Arguments.of(createXmlConsumer(xmlWriter -> xmlWriter.writeBooleanElement("test", false)),
                "<test>false</test>"),
            Arguments.of(createXmlConsumer(xmlWriter -> xmlWriter.writeBooleanElement("test", true)),
                "<test>true</test>"),

            // Double
            Arguments.of(createXmlConsumer(xmlWriter -> xmlWriter.writeDoubleElement("test", -42D)),
                "<test>-42.0</test>"),
            Arguments.of(createXmlConsumer(xmlWriter -> xmlWriter.writeDoubleElement("test", -42.0D)),
                "<test>-42.0</test>"),
            Arguments.of(createXmlConsumer(xmlWriter -> xmlWriter.writeDoubleElement("test", 42D)),
                "<test>42.0</test>"),
            Arguments.of(createXmlConsumer(xmlWriter -> xmlWriter.writeDoubleElement("test", 42.0D)),
                "<test>42.0</test>"),

            // Float
            Arguments.of(createXmlConsumer(xmlWriter -> xmlWriter.writeFloatElement("test", -42F)),
                "<test>-42.0</test>"),
            Arguments.of(createXmlConsumer(xmlWriter -> xmlWriter.writeFloatElement("test", -42.0F)),
                "<test>-42.0</test>"),
            Arguments.of(createXmlConsumer(xmlWriter -> xmlWriter.writeFloatElement("test", 42F)), "<test>42.0</test>"),
            Arguments.of(createXmlConsumer(xmlWriter -> xmlWriter.writeFloatElement("test", 42.0F)),
                "<test>42.0</test>"),

            // Integer
            Arguments.of(createXmlConsumer(xmlWriter -> xmlWriter.writeIntElement("test", -42)), "<test>-42</test>"),
            Arguments.of(createXmlConsumer(xmlWriter -> xmlWriter.writeIntElement("test", 42)), "<test>42</test>"),

            // Long
            Arguments.of(createXmlConsumer(xmlWriter -> xmlWriter.writeLongElement("test", -42)), "<test>-42</test>"),
            Arguments.of(createXmlConsumer(xmlWriter -> xmlWriter.writeLongElement("test", 42)), "<test>42</test>"),

            // String
            Arguments.of(createXmlConsumer(xmlWriter -> xmlWriter.writeStringElement("test", null)), ""),
            Arguments.of(createXmlConsumer(xmlWriter -> xmlWriter.writeStringElement("test", "")), "<test></test>"),
            Arguments.of(createXmlConsumer(xmlWriter -> xmlWriter.writeStringElement("test", "hello")),
                "<test>hello</test>"),

            // Field name and value.
            // Binary
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeBinary(null)), "<test></test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeBinary(new byte[0])), "<test></test>"),
            Arguments.of(
                createXmlElementConsumer(xmlWriter -> xmlWriter.writeBinary("Hello".getBytes(StandardCharsets.UTF_8))),
                "<test>" + Base64.getEncoder().encodeToString("Hello".getBytes(StandardCharsets.UTF_8)) + "</test>"),

            // Boolean
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeBoolean(null)), "<test></test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeBoolean(false)), "<test>false</test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeBoolean(true)), "<test>true</test>"),

            // Double
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeDouble(-42D)), "<test>-42.0</test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeDouble(-42.0D)), "<test>-42.0</test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeDouble(42D)), "<test>42.0</test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeDouble(42.0D)), "<test>42.0</test>"),

            // Float
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeFloat(-42F)), "<test>-42.0</test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeFloat(-42.0F)), "<test>-42.0</test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeFloat(42F)), "<test>42.0</test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeFloat(42.0F)), "<test>42.0</test>"),

            // Integer
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeInt(-42)), "<test>-42</test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeInt(42)), "<test>42</test>"),

            // Long
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeLong(-42)), "<test>-42</test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeLong(42)), "<test>42</test>"),

            // String
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeString(null)), "<test></test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeString("")), "<test></test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeString("hello")), "<test>hello</test>"),

            // CData
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeCDataString(null)), "<test></test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeCDataString("")),
                "<test><![CDATA[]]></test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeCDataString("hello")),
                "<test><![CDATA[hello]]></test>"));
    }

    @ParameterizedTest
    @MethodSource("basicAttributeOperationsSupplier")
    public void basicAttributeOperations(XMLStreamExceptionConsumer<XmlWriter> operation, String expectedXml) {
        assertDoesNotThrow(() -> operation.accept(getXmlWriter()));

        assertEquals(expectedXml, getXmlWriterContents());
    }

    private static Stream<Arguments> basicAttributeOperationsSupplier() {
        return Stream.of(
            // Binary
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeBinaryAttribute("test", null)),
                "<test></test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeBinaryAttribute("test", new byte[0])),
                "<test test=\"\"></test>"),
            Arguments.of(
                createXmlElementConsumer(
                    xmlWriter -> xmlWriter.writeBinaryAttribute("test", "Hello".getBytes(StandardCharsets.UTF_8))),
                "<test test=\"" + Base64.getEncoder().encodeToString("Hello".getBytes(StandardCharsets.UTF_8))
                    + "\"></test>"),

            // Boolean
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeBooleanAttribute("test", null)),
                "<test></test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeBooleanAttribute("test", false)),
                "<test test=\"false\"></test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeBooleanAttribute("test", true)),
                "<test test=\"true\"></test>"),

            // Double
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeDoubleAttribute("test", -42D)),
                "<test test=\"-42.0\"></test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeDoubleAttribute("test", -42.0D)),
                "<test test=\"-42.0\"></test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeDoubleAttribute("test", 42D)),
                "<test test=\"42.0\"></test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeDoubleAttribute("test", 42.0D)),
                "<test test=\"42.0\"></test>"),

            // Float
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeFloatAttribute("test", -42F)),
                "<test test=\"-42.0\"></test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeFloatAttribute("test", -42.0F)),
                "<test test=\"-42.0\"></test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeFloatAttribute("test", 42F)),
                "<test test=\"42.0\"></test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeFloatAttribute("test", 42.0F)),
                "<test test=\"42.0\"></test>"),

            // Integer
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeIntAttribute("test", -42)),
                "<test test=\"-42\"></test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeIntAttribute("test", 42)),
                "<test test=\"42\"></test>"),

            // Long
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeLongAttribute("test", -42)),
                "<test test=\"-42\"></test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeLongAttribute("test", 42)),
                "<test test=\"42\"></test>"),

            // String
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeStringAttribute("test", null)),
                "<test></test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeStringAttribute("test", "")),
                "<test test=\"\"></test>"),
            Arguments.of(createXmlElementConsumer(xmlWriter -> xmlWriter.writeStringAttribute("test", "hello")),
                "<test test=\"hello\"></test>"));
    }

    private static XMLStreamExceptionConsumer<XmlWriter>
        createXmlConsumer(XMLStreamExceptionFunction<XmlWriter, XmlWriter> consumptionFunction) {
        return xmlWriter -> consumptionFunction.apply(xmlWriter).flush();
    }

    private static XMLStreamExceptionConsumer<XmlWriter>
        createXmlElementConsumer(XMLStreamExceptionFunction<XmlWriter, XmlWriter> consumptionFunction) {
        return xmlWriter -> consumptionFunction.apply(xmlWriter.writeStartElement("test")).writeEndElement().flush();
    }

    private interface XMLStreamExceptionConsumer<T> {
        void accept(T t) throws XMLStreamException;
    }

    private interface XMLStreamExceptionFunction<T, R> {
        R apply(T t) throws XMLStreamException;
    }
}
