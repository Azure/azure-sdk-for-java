// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import javax.xml.namespace.QName;
import java.io.Closeable;
import java.util.Base64;
import java.util.Objects;
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
    public abstract String getStringAttribute(String namespaceUri, String localName);

    /**
     * Gets the binary value for the attribute in the XML element.
     *
     * @param namespaceUri Attribute namespace, may be null.
     * @param localName Attribute local name.
     * @return The binary value for the attribute in the XML element.
     * @throws IllegalStateException If {@link #currentToken()} isn't {@link XmlToken#START_ELEMENT}.
     */
    public final byte[] getBinaryAttribute(String namespaceUri, String localName) {
        String value = getStringAttribute(namespaceUri, localName);
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
    public final boolean getBooleanAttribute(String namespaceUri, String localName) {
        return Boolean.parseBoolean(getStringAttribute(namespaceUri, localName));
    }

    /**
     * Gets the double value for the attribute in the XML element.
     *
     * @param namespaceUri Attribute namespace, may be null.
     * @param localName Attribute local name.
     * @return The double value for the attribute in the XML element.
     * @throws IllegalStateException If {@link #currentToken()} isn't {@link XmlToken#START_ELEMENT}.
     */
    public final double getDoubleAttribute(String namespaceUri, String localName) {
        return Double.parseDouble(getStringAttribute(namespaceUri, localName));
    }

    /**
     * Gets the float value for the attribute in the XML element.
     *
     * @param namespaceUri Attribute namespace, may be null.
     * @param localName Attribute local name.
     * @return The float value for the attribute in the XML element.
     * @throws IllegalStateException If {@link #currentToken()} isn't {@link XmlToken#START_ELEMENT}.
     */
    public final float getFloatAttribute(String namespaceUri, String localName) {
        return Float.parseFloat(getStringAttribute(namespaceUri, localName));
    }

    /**
     * Gets the int value for the attribute in the XML element.
     *
     * @param namespaceUri Attribute namespace, may be null.
     * @param localName Attribute local name.
     * @return The int value for the attribute in the XML element.
     * @throws IllegalStateException If {@link #currentToken()} isn't {@link XmlToken#START_ELEMENT}.
     */
    public final int getIntAttribute(String namespaceUri, String localName) {
        return Integer.parseInt(getStringAttribute(namespaceUri, localName));
    }

    /**
     * Gets the long value for the attribute in the XML element.
     *
     * @param namespaceUri Attribute namespace, may be null.
     * @param localName Attribute local name.
     * @return The long value for the attribute in the XML element.
     * @throws IllegalStateException If {@link #currentToken()} isn't {@link XmlToken#START_ELEMENT}.
     */
    public final long getLongAttribute(String namespaceUri, String localName) {
        return Long.parseLong(getStringAttribute(namespaceUri, localName));
    }

    /**
     * Gets the nullable value for the attribute in the XML element.
     * <p>
     * If the attribute doesn't have a value or doesn't exist null will be returned, otherwise the attribute
     * {@link #getStringAttribute(String, String)} is passed to the converter.
     *
     * @param namespaceUri Attribute namespace, may be null.
     * @param localName Attribute local name.
     * @param converter Function that converts the attribute text value to the nullable type.
     * @param <T> Type of the attribute.
     * @return The converted text value, or null if the attribute didn't have a value.
     * @throws IllegalStateException If {@link #currentToken()} isn't {@link XmlToken#START_ELEMENT}.
     */
    public final <T> T getNullableAttribute(String namespaceUri, String localName, Function<String, T> converter) {
        String textValue = getStringAttribute(namespaceUri, localName);

        return textValue == null ? null : converter.apply(textValue);
    }

    /**
     * Gets the string value for the current element.
     *
     * @return The string value for the current element.
     */
    public abstract String getStringElement();

    /**
     * Gets the binary value for the current element.
     *
     * @return The binary value for the current element.
     */
    public final byte[] getBinaryElement() {
        String value = getStringElement();
        return (value == null || value.isEmpty()) ? null : Base64.getDecoder().decode(value);
    }

    /**
     * Gets the boolean value for the current element.
     *
     * @return The boolean value for the current element.
     */
    public final boolean getBooleanElement() {
        return Boolean.parseBoolean(getStringElement());
    }

    /**
     * Gets the double value for the current element.
     *
     * @return The double value for the current element.
     */
    public final double getDoubleElement() {
        return Double.parseDouble(getStringElement());
    }

    /**
     * Gets the float value for the current element.
     *
     * @return The float value for the current element.
     */
    public final float getFloatElement() {
        return Float.parseFloat(getStringElement());
    }

    /**
     * Gets the int value for the current element.
     *
     * @return The int value for the current element.
     */
    public final int getIntElement() {
        return Integer.parseInt(getStringElement());
    }

    /**
     * Gets the long value for the current element.
     *
     * @return The long value for the current element.
     */
    public final long getLongElement() {
        return Long.parseLong(getStringElement());
    }

    /**
     * Gets the nullable value for the current element.
     * <p>
     * If the current element doesn't have a value null will be returned, otherwise the element
     * {@link #getStringElement() text value} is passed to the converter.
     *
     * @param converter Function that converts the element text value to the nullable type.
     * @param <T> Type of the element.
     * @return The converted text value, or null if the element didn't have a value.
     */
    public final <T> T getNullableElement(Function<String, T> converter) {
        String textValue = getStringElement();

        return textValue == null ? null : converter.apply(textValue);
    }

    /**
     * Reads an object from the XML stream.
     * <p>
     * Validates that the {@link XmlReader} is currently pointing to an {@link XmlToken#START_ELEMENT} which has the
     * qualifying name specified by the {@code startTagName}.
     *
     * @param localName The expecting starting local name for the object.
     * @param converter The function that reads the object.
     * @param <T> Type of the object.
     * @return An instance of the expected object,
     * @throws IllegalStateException If the starting tag isn't {@link XmlToken#START_ELEMENT} or the tag doesn't match
     * the expected {@code startTagName}
     */
    public final <T> T readObject(String localName, Function<XmlReader, T> converter) {
        return readObject(new QName(null, localName), converter);
    }

    /**
     * Reads an object from the XML stream.
     * <p>
     * Validates that the {@link XmlReader} is currently pointing to an {@link XmlToken#START_ELEMENT} which has the
     * qualifying name specified by the {@code startTagName}.
     *
     * @param namespaceUri The expecting namespace for the object.
     * @param localName The expecting starting local name for the object.
     * @param converter The function that reads the object.
     * @param <T> Type of the object.
     * @return An instance of the expected object,
     * @throws IllegalStateException If the starting tag isn't {@link XmlToken#START_ELEMENT} or the tag doesn't match
     * the expected {@code startTagName}
     */
    public final <T> T readObject(String namespaceUri, String localName, Function<XmlReader, T> converter) {
        return readObject(new QName(namespaceUri, localName), converter);
    }

    private <T> T readObject(QName startTagName, Function<XmlReader, T> converter) {
        if (currentToken() != XmlToken.START_ELEMENT) {
            nextElement();
        }

        if (currentToken() != XmlToken.START_ELEMENT) {
            throw new IllegalStateException("Illegal start of XML deserialization. "
                + "Expected 'XmlToken.START_ELEMENT' but it was: 'XmlToken." + currentToken() + "'.");
        }

        QName tagName = getElementName();
        if (!Objects.equals(startTagName, tagName)) {
            throw new IllegalStateException("Expected XML element to be '" + startTagName + "' but it was: "
                + tagName + "'.");
        }

        return converter.apply(this);
    }
}
