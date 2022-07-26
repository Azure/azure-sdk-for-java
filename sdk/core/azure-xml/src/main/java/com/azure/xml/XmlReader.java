// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import javax.xml.namespace.QName;
import java.io.Closeable;
import java.util.Base64;
import java.util.function.Function;

/**
 * Reads an XML encoded value as a stream of tokens.
 */
public abstract class XmlReader implements Closeable {
    /**
     * Gets the {@link XmlToken} that the reader points to currently.
     * <p>
     * Returns {@link XmlToken#START_DOCUMENT} if the reader hasn't begun reading the XML stream. Returns
     * {@link XmlToken#END_DOCUMENT} if the reader has completed reading the XML stream.
     *
     * @return The {@link XmlToken} that the reader points to currently.
     */
    public abstract XmlToken currentToken();

    /**
     * Iterates to and returns the next {@link XmlToken#START_ELEMENT} or {@link XmlToken#END_ELEMENT} in the XML
     * stream.
     * <p>
     * Returns {@link XmlToken#END_DOCUMENT} if iterating to the next element token completes reading of the XML
     * stream.
     *
     * @return The next {@link XmlToken#START_ELEMENT} or {@link XmlToken#END_ELEMENT} in the XML stream, or
     * {@link XmlToken#END_DOCUMENT} if reading completes.
     */
    public abstract XmlToken nextElement();

    /**
     * Gets the {@link QName} for the current XML element.
     *
     * @return The {@link QName} for the current XML element.
     */
    public abstract QName getElementName();

    /**
     * Gets the string value for the attribute in the XML element.
     * <p>
     * Null is returned if the attribute doesn't exist in the XML element.
     *
     * @param namespaceUri Attribute namespace, may be null.
     * @param localName Attribute local name.
     * @return The string value for the attribute in the XML element, or null if the attribute doesn't exist.
     * @throws IllegalStateException If {@link #currentToken()} isn't {@link XmlToken#START_ELEMENT}.
     */
    public abstract String getAttributeStringValue(String namespaceUri, String localName);

    /**
     * Gets the binary value for the attribute in the XML element.
     *
     * @param namespaceUri Attribute namespace, may be null.
     * @param localName Attribute local name.
     * @return The binary value for the attribute in the XML element.
     * @throws IllegalStateException If {@link #currentToken()} isn't {@link XmlToken#START_ELEMENT}.
     */
    public final byte[] getAttributeBinaryValue(String namespaceUri, String localName) {
        String value = getAttributeStringValue(namespaceUri, localName);
        return (value == null || value.isEmpty()) ? null : Base64.getDecoder().decode(value);
    }

    /**
     * Gets the boolean value for the attribute in the XML element.
     *
     * @param namespaceUri Attribute namespace, may be null.
     * @param localName Attribute local name.
     * @return The boolean value for the attribute in the XML element.
     * @throws IllegalStateException If {@link #currentToken()} isn't {@link XmlToken#START_ELEMENT}.
     */
    public final boolean getAttributeBooleanValue(String namespaceUri, String localName) {
        return Boolean.parseBoolean(getAttributeStringValue(namespaceUri, localName));
    }

    /**
     * Gets the double value for the attribute in the XML element.
     *
     * @param namespaceUri Attribute namespace, may be null.
     * @param localName Attribute local name.
     * @return The double value for the attribute in the XML element.
     * @throws IllegalStateException If {@link #currentToken()} isn't {@link XmlToken#START_ELEMENT}.
     */
    public final double getAttributeDoubleValue(String namespaceUri, String localName) {
        return Double.parseDouble(getAttributeStringValue(namespaceUri, localName));
    }

    /**
     * Gets the float value for the attribute in the XML element.
     *
     * @param namespaceUri Attribute namespace, may be null.
     * @param localName Attribute local name.
     * @return The float value for the attribute in the XML element.
     * @throws IllegalStateException If {@link #currentToken()} isn't {@link XmlToken#START_ELEMENT}.
     */
    public final float getAttributeFloatValue(String namespaceUri, String localName) {
        return Float.parseFloat(getAttributeStringValue(namespaceUri, localName));
    }

    /**
     * Gets the int value for the attribute in the XML element.
     *
     * @param namespaceUri Attribute namespace, may be null.
     * @param localName Attribute local name.
     * @return The int value for the attribute in the XML element.
     * @throws IllegalStateException If {@link #currentToken()} isn't {@link XmlToken#START_ELEMENT}.
     */
    public final int getAttributeIntValue(String namespaceUri, String localName) {
        return Integer.parseInt(getAttributeStringValue(namespaceUri, localName));
    }

    /**
     * Gets the long value for the attribute in the XML element.
     *
     * @param namespaceUri Attribute namespace, may be null.
     * @param localName Attribute local name.
     * @return The long value for the attribute in the XML element.
     * @throws IllegalStateException If {@link #currentToken()} isn't {@link XmlToken#START_ELEMENT}.
     */
    public final long getAttributeLongValue(String namespaceUri, String localName) {
        return Long.parseLong(getAttributeStringValue(namespaceUri, localName));
    }

    /**
     * Gets the nullable value for the attribute in the XML element.
     * <p>
     * If the attribute doesn't have a value or doesn't exist null will be returned, otherwise the attribute
     * {@link #getAttributeStringValue(String, String)} is passed to the converter.
     *
     * @param namespaceUri Attribute namespace, may be null.
     * @param localName Attribute local name.
     * @param converter Function that converts the attribute text value to the nullable type.
     * @param <T> Type of the attribute.
     * @return The converted text value, or null if the attribute didn't have a value.
     * @throws IllegalStateException If {@link #currentToken()} isn't {@link XmlToken#START_ELEMENT}.
     */
    public final <T> T getAttributeNullableValue(String namespaceUri, String localName, Function<String, T> converter) {
        String textValue = getAttributeStringValue(namespaceUri, localName);

        return textValue == null ? null : converter.apply(textValue);
    }

    /**
     * Gets the string value for the current element.
     *
     * @return The string value for the current element.
     */
    public abstract String getElementStringValue();

    /**
     * Gets the binary value for the current element.
     *
     * @return The binary value for the current element.
     */
    public final byte[] getElementBinaryValue() {
        String value = getElementStringValue();
        return (value == null || value.isEmpty()) ? null : Base64.getDecoder().decode(value);
    }

    /**
     * Gets the boolean value for the current element.
     *
     * @return The boolean value for the current element.
     */
    public final boolean getElementBooleanValue() {
        return Boolean.parseBoolean(getElementStringValue());
    }

    /**
     * Gets the double value for the current element.
     *
     * @return The double value for the current element.
     */
    public final double getElementDoubleValue() {
        return Double.parseDouble(getElementStringValue());
    }

    /**
     * Gets the float value for the current element.
     *
     * @return The float value for the current element.
     */
    public final float getElementFloatValue() {
        return Float.parseFloat(getElementStringValue());
    }

    /**
     * Gets the int value for the current element.
     *
     * @return The int value for the current element.
     */
    public final int getElementIntValue() {
        return Integer.parseInt(getElementStringValue());
    }

    /**
     * Gets the long value for the current element.
     *
     * @return The long value for the current element.
     */
    public final long getElementLongValue() {
        return Long.parseLong(getElementStringValue());
    }

    /**
     * Gets the nullable value for the current element.
     * <p>
     * If the current element doesn't have a value null will be returned, otherwise the element
     * {@link #getElementStringValue() text value} is passed to the converter.
     *
     * @param converter Function that converts the element text value to the nullable type.
     * @param <T> Type of the element.
     * @return The converted text value, or null if the element didn't have a value.
     */
    public final <T> T getElementNullableValue(Function<String, T> converter) {
        String textValue = getElementStringValue();

        return textValue == null ? null : converter.apply(textValue);
    }
}
