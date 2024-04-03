// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Base64;
import java.util.Objects;

/**
 * Reads an XML encoded value as a stream of tokens.
 */
public final class XmlReader implements AutoCloseable {
    private static final XMLInputFactory XML_INPUT_FACTORY;

    static {
        XML_INPUT_FACTORY = XMLInputFactory.newInstance();
        XML_INPUT_FACTORY.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        XML_INPUT_FACTORY.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    }

    private final XMLStreamReader reader;

    private XmlToken currentToken;
    private boolean needToReadElementString = true;
    private String currentElementString;

    /**
     * Creates an {@link XMLStreamReader}-based {@link XmlReader} that parses the passed {@code xml}.
     * <p>
     * This uses the {@link XMLStreamReader} implementation provided by the default
     * {@link XMLInputFactory#newInstance()}. If you need to provide a custom implementation of
     * {@link XMLStreamReader} use {@link #fromXmlStreamReader(XMLStreamReader)}.
     *
     * @param xml The XML to parse.
     * @return A new {@link XmlReader} instance.
     * @throws NullPointerException If {@code xml} is null.
     * @throws XMLStreamException If an {@link XmlReader} cannot be instantiated.
     */
    public static XmlReader fromBytes(byte[] xml) throws XMLStreamException {
        Objects.requireNonNull(xml, "'xml' cannot be null.");
        return fromStream(new ByteArrayInputStream(xml));
    }

    /**
     * Creates an {@link XmlReader} that parses the passed {@code xml}.
     * <p>
     * This uses the {@link XMLStreamReader} implementation provided by the default
     * {@link XMLInputFactory#newInstance()}. If you need to provide a custom implementation of
     * {@link XMLStreamReader} use {@link #fromXmlStreamReader(XMLStreamReader)}.
     *
     * @param xml The XML to parse.
     * @return A new {@link XmlReader} instance.
     * @throws NullPointerException If {@code xml} is null.
     * @throws XMLStreamException If an {@link XmlReader} cannot be instantiated.
     */
    public static XmlReader fromString(String xml) throws XMLStreamException {
        Objects.requireNonNull(xml, "'xml' cannot be null.");
        return fromReader(new StringReader(xml));
    }

    /**
     * Creates an {@link XmlReader} that parses the passed {@code xml}.
     * <p>
     * This uses the {@link XMLStreamReader} implementation provided by the default
     * {@link XMLInputFactory#newInstance()}. If you need to provide a custom implementation of
     * {@link XMLStreamReader} use {@link #fromXmlStreamReader(XMLStreamReader)}.
     *
     * @param xml The XML to parse.
     * @return A new {@link XmlReader} instance.
     * @throws NullPointerException If {@code xml} is null.
     * @throws XMLStreamException If an {@link XmlReader} cannot be instantiated.
     */
    public static XmlReader fromStream(InputStream xml) throws XMLStreamException {
        Objects.requireNonNull(xml, "'xml' cannot be null.");
        return new XmlReader(XML_INPUT_FACTORY.createXMLStreamReader(xml));
    }

    /**
     * Creates an {@link XmlReader} that parses the passed {@code xml}.
     * <p>
     * This uses the {@link XMLStreamReader} implementation provided by the default
     * {@link XMLInputFactory#newInstance()}. If you need to provide a custom implementation of
     * {@link XMLStreamReader} use {@link #fromXmlStreamReader(XMLStreamReader)}.
     *
     * @param xml The XML to parse.
     * @return A new {@link XmlReader} instance.
     * @throws NullPointerException If {@code xml} is null.
     * @throws XMLStreamException If an {@link XmlReader} cannot be instantiated.
     */
    public static XmlReader fromReader(Reader xml) throws XMLStreamException {
        Objects.requireNonNull(xml, "'xml' cannot be null.");
        return new XmlReader(XML_INPUT_FACTORY.createXMLStreamReader(xml));
    }

    /**
     * Creates an {@link XmlReader} that parses the passed {@code xml}.
     * <p>
     * This uses the provided {@link XMLStreamReader} implementation to parse the XML.
     *
     * @param reader The {@link XMLStreamReader} to parse the XML.
     * @return A new {@link XmlReader} instance.
     * @throws NullPointerException If {@code reader} is null.
     */
    public static XmlReader fromXmlStreamReader(XMLStreamReader reader) {
        return new XmlReader(reader);
    }

    /**
     * Creates an instance of {@link XmlReader}.
     */
    private XmlReader(XMLStreamReader reader) {
        this.reader = Objects.requireNonNull(reader, "'reader' cannot be null.");
        this.currentToken = convertEventToToken(reader.getEventType());
    }

    /**
     * Gets the {@link XmlToken} that the reader points to currently.
     * <p>
     * Returns {@link XmlToken#START_DOCUMENT} if the reader hasn't begun reading the XML stream. Returns
     * {@link XmlToken#END_DOCUMENT} if the reader has completed reading the XML stream.
     *
     * @return The {@link XmlToken} that the reader points to currently.
     */
    public XmlToken currentToken() {
        return currentToken;
    }

    /**
     * Iterates to and returns the next {@link XmlToken#START_ELEMENT} or {@link XmlToken#END_ELEMENT} in the XML
     * stream.
     * <p>
     * Returns {@link XmlToken#END_DOCUMENT} if iterating to the next element token completes reading of the XML
     * stream.
     *
     * @return The next {@link XmlToken#START_ELEMENT} or {@link XmlToken#END_ELEMENT} in the XML stream, or
     * {@link XmlToken#END_DOCUMENT} if reading completes.
     * @throws XMLStreamException If the next element cannot be determined.
     */
    public XmlToken nextElement() throws XMLStreamException {
        int next = reader.next();
        while (next != XMLStreamConstants.START_ELEMENT
            && next != XMLStreamConstants.END_ELEMENT
            && next != XMLStreamConstants.END_DOCUMENT) {
            next = reader.next();
        }

        currentToken = convertEventToToken(next);
        needToReadElementString = true;
        currentElementString = null;
        return currentToken;
    }

    /**
     * Closes the XML stream.
     *
     * @throws XMLStreamException If the underlying content store fails to close.
     */
    @Override
    public void close() throws XMLStreamException {
        reader.close();
    }

    /**
     * Gets the {@link QName} for the current XML element.
     *
     * @return The {@link QName} for the current XML element.
     * @throws IllegalStateException If the {@link #currentToken()} {@link XmlToken#START_ELEMENT} or
     * {@link XmlToken#END_ELEMENT}.
     */
    public QName getElementName() {
        return reader.getName();
    }

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
    public String getStringAttribute(String namespaceUri, String localName) {
        String value = reader.getAttributeValue(namespaceUri, localName);

        // Treat empty string as null.
        return "".equals(value) ? null : value;
    }

    /**
     * Gets the binary value for the attribute in the XML element.
     *
     * @param namespaceUri Attribute namespace, may be null.
     * @param localName Attribute local name.
     * @return The binary value for the attribute in the XML element.
     */
    public byte[] getBinaryAttribute(String namespaceUri, String localName) {
        String value = getStringAttribute(namespaceUri, localName);
        return (value == null || value.isEmpty()) ? null : Base64.getDecoder().decode(value);
    }

    /**
     * Gets the boolean value for the attribute in the XML element.
     *
     * @param namespaceUri Attribute namespace, may be null.
     * @param localName Attribute local name.
     * @return The boolean value for the attribute in the XML element.
     */
    public boolean getBooleanAttribute(String namespaceUri, String localName) {
        return Boolean.parseBoolean(getStringAttribute(namespaceUri, localName));
    }

    /**
     * Gets the double value for the attribute in the XML element.
     *
     * @param namespaceUri Attribute namespace, may be null.
     * @param localName Attribute local name.
     * @return The double value for the attribute in the XML element.
     */
    public double getDoubleAttribute(String namespaceUri, String localName) {
        return Double.parseDouble(getStringAttribute(namespaceUri, localName));
    }

    /**
     * Gets the float value for the attribute in the XML element.
     *
     * @param namespaceUri Attribute namespace, may be null.
     * @param localName Attribute local name.
     * @return The float value for the attribute in the XML element.
     */
    public float getFloatAttribute(String namespaceUri, String localName) {
        return Float.parseFloat(getStringAttribute(namespaceUri, localName));
    }

    /**
     * Gets the int value for the attribute in the XML element.
     *
     * @param namespaceUri Attribute namespace, may be null.
     * @param localName Attribute local name.
     * @return The int value for the attribute in the XML element.
     */
    public int getIntAttribute(String namespaceUri, String localName) {
        return Integer.parseInt(getStringAttribute(namespaceUri, localName));
    }

    /**
     * Gets the long value for the attribute in the XML element.
     *
     * @param namespaceUri Attribute namespace, may be null.
     * @param localName Attribute local name.
     * @return The long value for the attribute in the XML element.
     */
    public long getLongAttribute(String namespaceUri, String localName) {
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
     * @throws XMLStreamException If the nullable attribute cannot be read.
     */
    public <T> T getNullableAttribute(String namespaceUri, String localName, ReadValueCallback<String, T> converter)
        throws XMLStreamException {
        String textValue = getStringAttribute(namespaceUri, localName);

        if (textValue == null) {
            return null;
        }

        return converter.read(textValue);
    }

    /**
     * Gets the string value for the current element.
     *
     * @return The string value for the current element.
     * @throws XMLStreamException If the String element cannot be read.
     */
    public String getStringElement() throws XMLStreamException {
        // The default getElementText implementation in the JDK uses an internal buffer as the API handles merging
        // multiple text states, characters, CDATA, space, and entity reference, into a single String. This
        // generally results in overhead as most cases will only have a single read performed but that read will
        // be buffered into the buffer and then returned as-is. So instead, a custom implementation will be used
        // where a small String buffer will be used to contain the intermediate reads and when the terminal state
        // is reached if only a single read was performed the String can be return, and if the unlikely multiple
        // read scenario is triggered those Strings can be concatenated.
        //
        // This logic continues to work even if the underlying XMLStreamReader implementation, such as the one
        // used in Jackson XML through Woodstox, handles this already.
        if (!needToReadElementString) {
            return currentElementString;
        }

        int readCount = 0;
        String firstRead = null;
        String[] buffer = null;
        int stringBufferSize = 0;
        int nextEvent = reader.next();

        // Continue reading until the next event is the end of the element or an exception state.
        while (nextEvent != XMLStreamConstants.END_ELEMENT) {
            if (nextEvent == XMLStreamConstants.CHARACTERS
                || nextEvent == XMLStreamConstants.CDATA
                || nextEvent == XMLStreamConstants.SPACE
                || nextEvent == XMLStreamConstants.ENTITY_REFERENCE) {
                readCount++;
                if (readCount == 1) {
                    firstRead = reader.getText();
                    stringBufferSize = firstRead.length();
                } else {
                    if (readCount == 2) {
                        buffer = new String[4];
                        buffer[0] = firstRead;
                    }

                    if (readCount > buffer.length - 1) {
                        String[] newBuffer = new String[buffer.length * 2];
                        System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
                        buffer = newBuffer;
                    }

                    String readText = reader.getText();
                    buffer[readCount - 1] = readText;
                    stringBufferSize += readText.length();
                }
            } else if (nextEvent != XMLStreamConstants.PROCESSING_INSTRUCTION
                && nextEvent != XMLStreamConstants.COMMENT) {
                // Processing instructions and comments are ignored but anything else is unexpected.
                throw new XMLStreamException("Unexpected event type while reading element value " + nextEvent);
            }

            nextEvent = reader.next();
        }

        if (readCount == 0) {
            currentElementString = null;
        } else if (readCount == 1) {
            currentElementString = firstRead;
        } else {
            StringBuilder finalText = new StringBuilder(stringBufferSize);
            for (int i = 0; i < readCount; i++) {
                finalText.append(buffer[i]);
            }

            currentElementString = finalText.toString();
        }

        needToReadElementString = false;
        return currentElementString;
    }

    /**
     * Gets the binary value for the current element.
     *
     * @return The binary value for the current element.
     * @throws XMLStreamException If the binary element cannot be read.
     */
    public byte[] getBinaryElement() throws XMLStreamException {
        String value = getStringElement();
        return (value == null || value.isEmpty()) ? null : Base64.getDecoder().decode(value);
    }

    /**
     * Gets the boolean value for the current element.
     *
     * @return The boolean value for the current element.
     * @throws XMLStreamException If the boolean element cannot be read.
     */
    public boolean getBooleanElement() throws XMLStreamException {
        return Boolean.parseBoolean(getStringElement());
    }

    /**
     * Gets the double value for the current element.
     *
     * @return The double value for the current element.
     * @throws XMLStreamException If the double element cannot be read.
     */
    public double getDoubleElement() throws XMLStreamException {
        return Double.parseDouble(getStringElement());
    }

    /**
     * Gets the float value for the current element.
     *
     * @return The float value for the current element.
     * @throws XMLStreamException If the float element cannot be read.
     */
    public float getFloatElement() throws XMLStreamException {
        return Float.parseFloat(getStringElement());
    }

    /**
     * Gets the int value for the current element.
     *
     * @return The int value for the current element.
     * @throws XMLStreamException If the int element cannot be read.
     */
    public int getIntElement() throws XMLStreamException {
        return Integer.parseInt(getStringElement());
    }

    /**
     * Gets the long value for the current element.
     *
     * @return The long value for the current element.
     * @throws XMLStreamException If the long element cannot be read.
     */
    public long getLongElement() throws XMLStreamException {
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
     * @throws XMLStreamException If the nullable element cannot be read.
     */
    public <T> T getNullableElement(ReadValueCallback<String, T> converter) throws XMLStreamException {
        String textValue = getStringElement();

        if (textValue == null) {
            return null;
        }

        return converter.read(textValue);
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
     * @throws XMLStreamException If the object cannot be read.
     */
    public <T> T readObject(String localName, ReadValueCallback<XmlReader, T> converter) throws XMLStreamException {
        return readObject(null, localName, converter);
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
     * @throws XMLStreamException If the object cannot be read.
     */
    public <T> T readObject(String namespaceUri, String localName, ReadValueCallback<XmlReader, T> converter)
        throws XMLStreamException {
        return readObject(new QName(namespaceUri, localName), converter);
    }

    private <T> T readObject(QName startTagName, ReadValueCallback<XmlReader, T> converter) throws XMLStreamException {
        if (currentToken() != XmlToken.START_ELEMENT) {
            nextElement();
        }

        if (currentToken() != XmlToken.START_ELEMENT) {
            throw new IllegalStateException("Illegal start of XML deserialization. "
                + "Expected 'XmlToken.START_ELEMENT' but it was: 'XmlToken." + currentToken() + "'.");
        }

        QName tagName = getElementName();
        if (!Objects.equals(startTagName, tagName)) {
            throw new IllegalStateException(
                "Expected XML element to be '" + startTagName + "' but it was: " + tagName + "'.");
        }

        return converter.read(this);
    }

    /**
     * Skips the current XML element.
     * <p>
     * If the {@link #currentToken()} isn't an {@link XmlToken#START_ELEMENT} this is a no-op.
     * <p>
     * This reads the XML stream until the matching {@link XmlToken#END_ELEMENT} is found for the current
     * {@link XmlToken#START_ELEMENT}.
     * @throws XMLStreamException If skipping the element fails.
     */
    public void skipElement() throws XMLStreamException {
        XmlToken currentToken = currentToken();
        if (currentToken != XmlToken.START_ELEMENT) {
            return;
        }

        int depth = 1;
        while (depth > 0) {
            currentToken = nextElement();

            if (currentToken == XmlToken.START_ELEMENT) {
                depth++;
            } else if (currentToken == XmlToken.END_ELEMENT) {
                depth--;
            } else {
                // Should never get into this state if the XML token stream is properly formatted XML.
                // But if this happens, just break until a better strategy can be determined.
                break;
            }
        }
    }

    private static XmlToken convertEventToToken(int event) {
        switch (event) {
            case 1:
                return XmlToken.START_ELEMENT;

            case 2:
                return XmlToken.END_ELEMENT;

            case 7:
                return XmlToken.START_DOCUMENT;

            case 8:
                return XmlToken.END_DOCUMENT;

            default:
                throw new IllegalStateException("Unknown/unsupported XMLStreamConstants: " + event);
        }
    }
}
