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
    private final XMLStreamReader reader;

    private XmlToken currentToken = null;
    private int attributeIndex = -1;

    public static XmlReader fromBytes(byte[] xml) {
        return fromInputStream(new ByteArrayInputStream(xml));
    }

    public static XmlReader fromString(String xml) {
        return fromReader(new StringReader(xml));
    }

    public static XmlReader fromInputStream(InputStream inputStream) {
        try {
            return new DefaultXmlReader(XMLInputFactory.newFactory().createXMLStreamReader(inputStream));
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public static XmlReader fromReader(Reader reader) {
        try {
            return new DefaultXmlReader(XMLInputFactory.newFactory().createXMLStreamReader(reader));
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    private DefaultXmlReader(XMLStreamReader reader) {
        this.reader = reader;
    }

    @Override
    public XmlToken currentToken() {
        return currentToken;
    }

    @Override
    public XmlToken nextToken() {
        try {
            currentToken = convertEventToToken(reader);

            if (currentToken == XmlToken.ATTRIBUTE) {
                attributeIndex = attributeIndex == -1 ? 0 : attributeIndex + 1;
            } else {
                attributeIndex = -1;
            }

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
    public QName getAttributeName() {
        return reader.getAttributeName(attributeIndex);
    }

    @Override
    public void close() throws IOException {
        try {
            reader.close();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    private static XmlToken convertEventToToken(XMLStreamReader reader) throws XMLStreamException {
        int event = reader.next();

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
