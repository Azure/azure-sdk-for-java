// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml.implementation;

import com.azure.xml.XmlReader;
import com.azure.xml.XmlToken;
import com.azure.xml.XmlWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Objects;

/**
 * Default {@link XmlWriter} implementation based on {@link XMLStreamReader}.
 */
public final class DefaultXmlReader extends XmlReader {
    private static final XMLInputFactory XML_INPUT_FACTORY;

    static {
        XML_INPUT_FACTORY = XMLInputFactory.newInstance();
        XML_INPUT_FACTORY.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        XML_INPUT_FACTORY.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    }

    private final XMLStreamReader reader;

    private XmlToken currentToken;

    /**
     * Creates an {@link XMLStreamReader}-based {@link XmlReader} that parses the passed {@code xml}.
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
     *
     * @param xml The XML to parse.
     * @return A new {@link XmlReader} instance.
     * @throws NullPointerException If {@code xml} is null.
     * @throws XMLStreamException If an {@link XmlReader} cannot be instantiated.
     */
    public static XmlReader fromStream(InputStream xml) throws XMLStreamException {
        Objects.requireNonNull(xml, "'xml' cannot be null.");
        return new DefaultXmlReader(XML_INPUT_FACTORY.createXMLStreamReader(xml));
    }

    /**
     * Creates an {@link XmlReader} that parses the passed {@code xml}.
     *
     * @param xml The XML to parse.
     * @return A new {@link XmlReader} instance.
     * @throws NullPointerException If {@code xml} is null.
     * @throws XMLStreamException If an {@link XmlReader} cannot be instantiated.
     */
    public static XmlReader fromReader(Reader xml) throws XMLStreamException {
        Objects.requireNonNull(xml, "'xml' cannot be null.");
        return new DefaultXmlReader(XML_INPUT_FACTORY.createXMLStreamReader(xml));
    }

    private DefaultXmlReader(XMLStreamReader reader) {
        this.reader = reader;
        this.currentToken = convertEventToToken(reader.getEventType());
    }

    @Override
    public XmlToken currentToken() {
        return currentToken;
    }

    @Override
    public XmlToken nextElement() throws XMLStreamException {
        int next = reader.next();
        while (next != XMLStreamConstants.START_ELEMENT
            && next != XMLStreamConstants.END_ELEMENT
            && next != XMLStreamConstants.END_DOCUMENT) {
            next = reader.next();
        }

        currentToken = convertEventToToken(next);
        return currentToken;
    }

    @Override
    public QName getElementName() {
        return reader.getName();
    }

    @Override
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
            return null;
        } else if (readCount == 1) {
            return firstRead;
        } else {
            StringBuilder finalText = new StringBuilder(stringBufferSize);
            for (int i = 0; i < readCount; i++) {
                finalText.append(buffer[i]);
            }

            return finalText.toString();
        }
    }

    @Override
    public String getStringAttribute(String namespaceUri, String localName) {
        String value = reader.getAttributeValue(namespaceUri, localName);

        // Treat empty string as null.
        return "".equals(value) ? null : value;
    }

    @Override
    public void close() throws XMLStreamException {
        reader.close();
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
