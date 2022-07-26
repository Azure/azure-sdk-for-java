// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml.contract;

import com.azure.xml.XmlReader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the contract of {@link XmlReader}.
 * <p>
 * All implementations of {@link XmlReader} must create a subclass of this test class and pass all tests as they're
 * written to be considered an acceptable implementation.
 * <p>
 * Each test will only create a single instance of {@link XmlReader} to simplify usage of {@link #getXmlReader(String)}.
 */
public abstract class XmlReaderContractTests {
    /**
     * Creates an instance of {@link XmlReader} that will be used by a test.
     *
     * @param xml The XML to be read.
     * @return The {@link XmlReader} that a test will use.
     */
    protected abstract XmlReader getXmlReader(String xml);

    @ParameterizedTest
    @MethodSource("basicElementOperationsSupplier")
    public <T> void basicElementOperations(String xml, T expectedValue, Function<XmlReader, T> function) {
        XmlReader reader = getXmlReader(xml);
        reader.nextElement(); // Initialize the XmlReader for reading.

        T actualValue = assertDoesNotThrow(() -> function.apply(reader));

        assertEquals(expectedValue, actualValue);
    }

    private static Stream<Arguments> basicElementOperationsSupplier() {
        return Stream.of(
            // Value handling.

            // Boolean
            Arguments.of("<test>false</test>", false, createXmlConsumer(XmlReader::getElementBooleanValue)),
            Arguments.of("<test>true</test>", true, createXmlConsumer(XmlReader::getElementBooleanValue)),
            Arguments.of("<test></test>", null, createXmlConsumer(xmlReader ->
                xmlReader.getElementNullableValue(Boolean::parseBoolean))),
            Arguments.of("<test/>", null, createXmlConsumer(xmlReader ->
                xmlReader.getElementNullableValue(Boolean::parseBoolean))),

            // Double
            Arguments.of("<test>-42.0</test>", -42D, createXmlConsumer(XmlReader::getElementDoubleValue)),
            Arguments.of("<test>-42</test>", -42D, createXmlConsumer(XmlReader::getElementDoubleValue)),
            Arguments.of("<test>42.0</test>", 42D, createXmlConsumer(XmlReader::getElementDoubleValue)),
            Arguments.of("<test>42</test>", 42D, createXmlConsumer(XmlReader::getElementDoubleValue)),
            Arguments.of("<test></test>", null, createXmlConsumer(xmlReader ->
                xmlReader.getElementNullableValue(Double::parseDouble))),
            Arguments.of("<test/>", null, createXmlConsumer(xmlReader ->
                xmlReader.getElementNullableValue(Double::parseDouble))),

            // Float
            Arguments.of("<test>-42.0</test>", -42F, createXmlConsumer(XmlReader::getElementFloatValue)),
            Arguments.of("<test>-42</test>", -42F, createXmlConsumer(XmlReader::getElementFloatValue)),
            Arguments.of("<test>42.0</test>", 42F, createXmlConsumer(XmlReader::getElementFloatValue)),
            Arguments.of("<test>42</test>", 42F, createXmlConsumer(XmlReader::getElementFloatValue)),
            Arguments.of("<test></test>", null, createXmlConsumer(xmlReader ->
                xmlReader.getElementNullableValue(Float::parseFloat))),
            Arguments.of("<test/>", null, createXmlConsumer(xmlReader ->
                xmlReader.getElementNullableValue(Float::parseFloat))),

            // Integer
            Arguments.of("<test>-42</test>", -42, createXmlConsumer(XmlReader::getElementIntValue)),
            Arguments.of("<test>42</test>", 42, createXmlConsumer(XmlReader::getElementIntValue)),
            Arguments.of("<test></test>", null, createXmlConsumer(xmlReader ->
                xmlReader.getElementNullableValue(Integer::parseInt))),
            Arguments.of("<test/>", null, createXmlConsumer(xmlReader ->
                xmlReader.getElementNullableValue(Integer::parseInt))),

            // Long
            Arguments.of("<test>-42</test>", -42L, createXmlConsumer(XmlReader::getElementLongValue)),
            Arguments.of("<test>42</test>", 42L, createXmlConsumer(XmlReader::getElementLongValue)),
            Arguments.of("<test></test>", null, createXmlConsumer(xmlReader ->
                xmlReader.getElementNullableValue(Long::parseLong))),
            Arguments.of("<test/>", null, createXmlConsumer(xmlReader ->
                xmlReader.getElementNullableValue(Long::parseLong))),

            // String
            Arguments.of("<test/>", null, createXmlConsumer(XmlReader::getElementStringValue)),
            Arguments.of("<test></test>", null, createXmlConsumer(XmlReader::getElementStringValue)),
            Arguments.of("<test>hello</test>", "hello", createXmlConsumer(XmlReader::getElementStringValue))
        );
    }

    // Byte arrays can't use Object.equals as they'll be compared by memory location instead of value equality.
    @ParameterizedTest
    @MethodSource("binaryElementOperationsSupplier")
    public void binaryElementOperations(String xml, byte[] expectedValue, Function<XmlReader, byte[]> function) {
        XmlReader reader = getXmlReader(xml);
        reader.nextElement(); // Initialize the XmlReader for reading.

        byte[] actualValue = assertDoesNotThrow(() -> function.apply(reader));

        assertArrayEquals(expectedValue, actualValue);
    }

    private static Stream<Arguments> binaryElementOperationsSupplier() {
        return Stream.of(
            // Binary
            Arguments.of("<test/>", null, createXmlConsumer(XmlReader::getElementBinaryValue)),
            Arguments.of("<test></test>", null, createXmlConsumer(XmlReader::getElementBinaryValue)),
            Arguments.of("<test>" + Base64.getEncoder().encodeToString("Hello".getBytes(StandardCharsets.UTF_8)) + "</test>",
                "Hello".getBytes(StandardCharsets.UTF_8), createXmlConsumer(XmlReader::getElementBinaryValue))
        );
    }

    @ParameterizedTest
    @MethodSource("basicAttributeOperationsSupplier")
    public <T> void basicAttributeOperations(String json, T expectedValue, Function<XmlReader, T> function) {
        XmlReader reader = getXmlReader(json);
        reader.nextElement(); // Initialize the XmlReader for reading.

        T actualValue = assertDoesNotThrow(() -> function.apply(reader));

        assertEquals(expectedValue, actualValue);
    }

    private static Stream<Arguments> basicAttributeOperationsSupplier() {
        return Stream.of(
            // Value handling.

            // Boolean
            Arguments.of("<test test=\"false\"></test>", false, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeBooleanValue(null, "test"))),
            Arguments.of("<test test=\"true\"></test>", true, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeBooleanValue(null, "test"))),
            Arguments.of("<test test=\"false\"/>", false, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeBooleanValue(null, "test"))),
            Arguments.of("<test test=\"true\"/>", true, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeBooleanValue(null, "test"))),
            Arguments.of("<test></test>", null, createXmlConsumer(xmlReader ->
                xmlReader.getAttributeNullableValue(null, "test", Boolean::parseBoolean))),
            Arguments.of("<test/>", null, createXmlConsumer(xmlReader ->
                xmlReader.getAttributeNullableValue(null, "test", Boolean::parseBoolean))),
            Arguments.of("<test test=\"\"></test>", null, createXmlConsumer(xmlReader ->
                xmlReader.getAttributeNullableValue(null, "test", Boolean::parseBoolean))),
            Arguments.of("<test test=\"\"/>", null, createXmlConsumer(xmlReader ->
                xmlReader.getAttributeNullableValue(null, "test", Boolean::parseBoolean))),

            // Double
            Arguments.of("<test test=\"-42.0\"></test>", -42D, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeDoubleValue(null, "test"))),
            Arguments.of("<test test=\"-42\"></test>", -42D, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeDoubleValue(null, "test"))),
            Arguments.of("<test test=\"-42.0\"/>", -42D, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeDoubleValue(null, "test"))),
            Arguments.of("<test test=\"-42\"/>", -42D, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeDoubleValue(null, "test"))),
            Arguments.of("<test test=\"42.0\"></test>", 42D, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeDoubleValue(null, "test"))),
            Arguments.of("<test test=\"42\"></test>", 42D, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeDoubleValue(null, "test"))),
            Arguments.of("<test test=\"42.0\"/>", 42D, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeDoubleValue(null, "test"))),
            Arguments.of("<test test=\"42\"/>", 42D, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeDoubleValue(null, "test"))),
            Arguments.of("<test></test>", null, createXmlConsumer(xmlReader ->
                xmlReader.getAttributeNullableValue(null, "test", Double::parseDouble))),
            Arguments.of("<test/>", null, createXmlConsumer(xmlReader ->
                xmlReader.getAttributeNullableValue(null, "test", Double::parseDouble))),
            Arguments.of("<test test=\"\"></test>", null, createXmlConsumer(xmlReader ->
                xmlReader.getAttributeNullableValue(null, "test", Double::parseDouble))),
            Arguments.of("<test test=\"\"/>", null, createXmlConsumer(xmlReader ->
                xmlReader.getAttributeNullableValue(null, "test", Double::parseDouble))),

            // Float
            Arguments.of("<test test=\"-42.0\"></test>", -42F, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeFloatValue(null, "test"))),
            Arguments.of("<test test=\"-42\"></test>", -42F, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeFloatValue(null, "test"))),
            Arguments.of("<test test=\"-42.0\"/>", -42F, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeFloatValue(null, "test"))),
            Arguments.of("<test test=\"-42\"/>", -42F, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeFloatValue(null, "test"))),
            Arguments.of("<test test=\"42.0\"></test>", 42F, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeFloatValue(null, "test"))),
            Arguments.of("<test test=\"42\"></test>", 42F, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeFloatValue(null, "test"))),
            Arguments.of("<test test=\"42.0\"/>", 42F, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeFloatValue(null, "test"))),
            Arguments.of("<test test=\"42\"/>", 42F, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeFloatValue(null, "test"))),
            Arguments.of("<test></test>", null, createXmlConsumer(xmlReader ->
                xmlReader.getAttributeNullableValue(null, "test", Float::parseFloat))),
            Arguments.of("<test/>", null, createXmlConsumer(xmlReader ->
                xmlReader.getAttributeNullableValue(null, "test", Float::parseFloat))),
            Arguments.of("<test test=\"\"></test>", null, createXmlConsumer(xmlReader ->
                xmlReader.getAttributeNullableValue(null, "test", Float::parseFloat))),
            Arguments.of("<test test=\"\"/>", null, createXmlConsumer(xmlReader ->
                xmlReader.getAttributeNullableValue(null, "test", Float::parseFloat))),

            // Integer
            Arguments.of("<test test=\"-42\"></test>", -42, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeIntValue(null, "test"))),
            Arguments.of("<test test=\"-42\"/>", -42, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeIntValue(null, "test"))),
            Arguments.of("<test test=\"42\"></test>", 42, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeIntValue(null, "test"))),
            Arguments.of("<test test=\"42\"/>", 42, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeIntValue(null, "test"))),
            Arguments.of("<test></test>", null, createXmlConsumer(xmlReader ->
                xmlReader.getAttributeNullableValue(null, "test", Integer::parseInt))),
            Arguments.of("<test/>", null, createXmlConsumer(xmlReader ->
                xmlReader.getAttributeNullableValue(null, "test", Integer::parseInt))),
            Arguments.of("<test test=\"\"></test>", null, createXmlConsumer(xmlReader ->
                xmlReader.getAttributeNullableValue(null, "test", Integer::parseInt))),
            Arguments.of("<test test=\"\"/>", null, createXmlConsumer(xmlReader ->
                xmlReader.getAttributeNullableValue(null, "test", Integer::parseInt))),

            // Long
            Arguments.of("<test test=\"-42\"></test>", -42L, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeLongValue(null, "test"))),
            Arguments.of("<test test=\"-42\"/>", -42L, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeLongValue(null, "test"))),
            Arguments.of("<test test=\"42\"></test>", 42L, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeLongValue(null, "test"))),
            Arguments.of("<test test=\"42\"/>", 42L, createXmlConsumer(
                xmlReader -> xmlReader.getAttributeLongValue(null, "test"))),
            Arguments.of("<test></test>", null, createXmlConsumer(xmlReader ->
                xmlReader.getAttributeNullableValue(null, "test", Long::parseLong))),
            Arguments.of("<test/>", null, createXmlConsumer(xmlReader ->
                xmlReader.getAttributeNullableValue(null, "test", Long::parseLong))),
            Arguments.of("<test test=\"\"></test>", null, createXmlConsumer(xmlReader ->
                xmlReader.getAttributeNullableValue(null, "test", Long::parseLong))),
            Arguments.of("<test test=\"\"/>", null, createXmlConsumer(xmlReader ->
                xmlReader.getAttributeNullableValue(null, "test", Long::parseLong))),

            // String
            Arguments.of("<test></test>", null,
                createXmlConsumer(xmlReader -> xmlReader.getAttributeStringValue(null, "test"))),
            Arguments.of("<test/>", null,
                createXmlConsumer(xmlReader -> xmlReader.getAttributeStringValue(null, "test"))),
            Arguments.of("<test test=\"\"></test>", null,
                createXmlConsumer(xmlReader -> xmlReader.getAttributeStringValue(null, "test"))),
            Arguments.of("<test test=\"\"/>", null,
                createXmlConsumer(xmlReader -> xmlReader.getAttributeStringValue(null, "test"))),
            Arguments.of("<test test=\"hello\"></test>", "hello",
                createXmlConsumer(xmlReader -> xmlReader.getAttributeStringValue(null, "test"))),
            Arguments.of("<test test=\"hello\"/>", "hello",
                createXmlConsumer(xmlReader -> xmlReader.getAttributeStringValue(null, "test"))),
            Arguments.of("<test test=\"hello\"></test>", "hello",
                createXmlConsumer(xmlReader -> xmlReader.getAttributeStringValue(null, "test"))),
            Arguments.of("<test test=\"hello\"/>", "hello",
                createXmlConsumer(xmlReader -> xmlReader.getAttributeStringValue(null, "test")))
        );
    }

    // Byte arrays can't use Object.equals as they'll be compared by memory location instead of value equality.
    @ParameterizedTest
    @MethodSource("binaryAttributeOperationsSupplier")
    public void binaryAttributeOperations(String xml, byte[] expectedValue, Function<XmlReader, byte[]> function) {
        XmlReader reader = getXmlReader(xml);
        reader.nextElement(); // Initialize the XmlReader for reading.

        byte[] actualValue = assertDoesNotThrow(() -> function.apply(reader));

        assertArrayEquals(expectedValue, actualValue);
    }

    private static Stream<Arguments> binaryAttributeOperationsSupplier() {
        return Stream.of(
            // Binary
            Arguments.of("<test/>", null,
                createXmlConsumer(xmlReader -> xmlReader.getAttributeBinaryValue(null, "test"))),
            Arguments.of("<test></test>", null,
                createXmlConsumer(xmlReader -> xmlReader.getAttributeBinaryValue(null, "test"))),
            Arguments.of("<test test=\"\"/>", null,
                createXmlConsumer(xmlReader -> xmlReader.getAttributeBinaryValue(null, "test"))),
            Arguments.of("<test test=\"\"></test>", null,
                createXmlConsumer(xmlReader -> xmlReader.getAttributeBinaryValue(null, "test"))),
            Arguments.of("<test test=\"" + Base64.getEncoder().encodeToString("Hello".getBytes(StandardCharsets.UTF_8)) + "\"></test>",
                "Hello".getBytes(StandardCharsets.UTF_8),
                createXmlConsumer(xmlReader -> xmlReader.getAttributeBinaryValue(null, "test"))),
            Arguments.of("<test test=\"" + Base64.getEncoder().encodeToString("Hello".getBytes(StandardCharsets.UTF_8)) + "\"/>",
                "Hello".getBytes(StandardCharsets.UTF_8),
                createXmlConsumer(xmlReader -> xmlReader.getAttributeBinaryValue(null, "test")))
        );
    }

    private static <T> Function<XmlReader, T> createXmlConsumer(Function<XmlReader, T> func) {
        return func;
    }
}
