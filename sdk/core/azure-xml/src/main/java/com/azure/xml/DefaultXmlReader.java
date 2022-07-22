// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

/**
 * Default {@link XmlWriter} implementation based on {@link XMLStreamReader}.
 */
public final class DefaultXmlReader extends XmlReader {
    private static final XMLInputFactory XML_INPUT_FACTORY;

    static {
        XML_INPUT_FACTORY = XMLInputFactory.newFactory();
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
     */
    public static XmlReader fromBytes(byte[] xml) {
        return fromInputStream(new ByteArrayInputStream(xml));
    }

    /**
     * Creates an {@link XMLStreamReader}-based {@link XmlReader} that parses the passed {@code xml}.
     *
     * @param xml The XML to parse.
     * @return A new {@link XmlReader} instance.
     */
    public static XmlReader fromString(String xml) {
        return fromReader(new StringReader(xml));
    }

    /**
     * Creates an {@link XMLStreamReader}-based {@link XmlReader} that parses the passed {@code xml}.
     *
     * @param xml The XML to parse.
     * @return A new {@link XmlReader} instance.
     * @throws RuntimeException If an {@link XmlReader} cannot be instantiated.
     */
    public static XmlReader fromInputStream(InputStream xml) {
        try {
            return new DefaultXmlReader(XML_INPUT_FACTORY.createXMLStreamReader(xml));
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates an {@link XMLStreamReader}-based {@link XmlReader} that parses the passed {@code xml}.
     *
     * @param xml The XML to parse.
     * @return A new {@link XmlReader} instance.
     * @throws RuntimeException If an {@link XmlReader} cannot be instantiated.
     */
    public static XmlReader fromReader(Reader xml) {
        try {
            return new DefaultXmlReader(XML_INPUT_FACTORY.createXMLStreamReader(xml));
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
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
    public XmlToken nextElement() {
        try {
            XmlToken next = convertEventToToken(reader.next());
            while (next != XmlToken.START_ELEMENT && next != XmlToken.END_ELEMENT && next != XmlToken.END_DOCUMENT) {
                next = convertEventToToken(reader.next());
            }

            currentToken = next;
            return currentToken;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public QName getElementName() {
        return reader.getName();
    }

    @Override
    public String getElementStringValue() {
        try {
            return reader.getElementText();
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getAttributeStringValue(String namespaceUri, String localName) {
        return reader.getAttributeValue(namespaceUri, localName);
    }

    @Override
    public void close() throws IOException {
        try {
            reader.close();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    private static XmlToken convertEventToToken(int event) {
        switch (event) {
            case 1:
                return XmlToken.START_ELEMENT;
            case 2:
                return XmlToken.END_ELEMENT;
            case 3:
                return XmlToken.PROCESSING_INSTRUCTION;
            case 4:
                return XmlToken.CHARACTERS;
            case 5:
                return XmlToken.COMMENT;
            case 6:
                return XmlToken.SPACE;
            case 7:
                return XmlToken.START_DOCUMENT;
            case 8:
                return XmlToken.END_DOCUMENT;
            case 9:
                return XmlToken.ENTITY_REFERENCE;
            case 10:
                return XmlToken.ATTRIBUTE;
            case 11:
                return XmlToken.DTD;
            case 12:
                return XmlToken.CDATA;
            case 13:
                return XmlToken.NAMESPACE;
            case 14:
                return XmlToken.NOTATION_DECLARATION;
            case 15:
                return XmlToken.ENTITY_DECLARATION;

            default:
                throw new IllegalStateException("Unknown XmlToken: " + event);
        }
    }
}
