// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml.contract;

import com.azure.xml.XmlReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    @MethodSource("basicOperationsSupplier")
    public <T> void basicOperations(String json, T expectedValue, Function<XmlReader, T> function) {
        XmlReader reader = getXmlReader(json);
        reader.nextElement(); // Initialize the XmlReader for reading.

        T actualValue = assertDoesNotThrow(() -> function.apply(reader));

        assertEquals(expectedValue, actualValue);
    }

    private static Stream<Arguments> basicOperationsSupplier() {
        return Stream.of(
            // Value handling.

            // Boolean
            Arguments.of("<test>false</test>", false, createXmlConsumer(XmlReader::getElementBooleanValue)),
            Arguments.of("<test>true</test>", true, createXmlConsumer(XmlReader::getElementBooleanValue)),
            //Arguments.of("<test>null</test>", null, createXmlConsumer(JsonReader::getBooleanNullableValue)),

            // Double
            Arguments.of("<test>-42.0</test>", -42D, createXmlConsumer(XmlReader::getElementDoubleValue)),
            Arguments.of("<test>-42</test>", -42D, createXmlConsumer(XmlReader::getElementDoubleValue)),
            Arguments.of("<test>42.0</test>", 42D, createXmlConsumer(XmlReader::getElementDoubleValue)),
            Arguments.of("<test>42</test>", 42D, createXmlConsumer(XmlReader::getElementDoubleValue)),
            //Arguments.of("<test>null</test>", null, createXmlConsumer(JsonReader::getDoubleNullableValue)),

            // Float
            Arguments.of("<test>-42.0</test>", -42F, createXmlConsumer(XmlReader::getElementFloatValue)),
            Arguments.of("<test>-42</test>", -42F, createXmlConsumer(XmlReader::getElementFloatValue)),
            Arguments.of("<test>42.0</test>", 42F, createXmlConsumer(XmlReader::getElementFloatValue)),
            Arguments.of("<test>42</test>", 42F, createXmlConsumer(XmlReader::getElementFloatValue)),
            //Arguments.of("<test>null</test>", null, createXmlConsumer(JsonReader::getFloatNullableValue)),

            // Integer
            Arguments.of("<test>-42</test>", -42, createXmlConsumer(XmlReader::getElementIntValue)),
            Arguments.of("<test>42</test>", 42, createXmlConsumer(XmlReader::getElementIntValue)),
            //Arguments.of("<test>null</test>", null, createXmlConsumer(JsonReader::getIntegerNullableValue)),

            // Long
            Arguments.of("<test>-42</test>", -42L, createXmlConsumer(XmlReader::getElementLongValue)),
            Arguments.of("<test>42</test>", 42L, createXmlConsumer(XmlReader::getElementLongValue)),
            //Arguments.of("<test>null</test>", null, createXmlConsumer(JsonReader::getLongNullableValue)),

            // String
            Arguments.of("<test/>", null, createXmlConsumer(XmlReader::getElementStringValue)),
            Arguments.of("<test></test>", "", createXmlConsumer(XmlReader::getElementStringValue)),
            Arguments.of("<test>hello</test>", "hello", createXmlConsumer(XmlReader::getElementStringValue))
        );
    }

    private static <T> Function<XmlReader, T> createXmlConsumer(Function<XmlReader, T> func) {
        return func;
    }

    @Test
    public void selfClosingTagAlwaysIsNull() {
        XmlReader xmlReader = getXmlReader("<test/>");

        xmlReader.nextElement(); // Initialize the XmlReader for reading.

        assertNull(xmlReader.getElementStringValue());
    }
}
