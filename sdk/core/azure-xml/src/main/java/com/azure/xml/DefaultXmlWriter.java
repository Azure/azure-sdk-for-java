// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Default {@link XmlWriter} implementation based on {@link XMLStreamWriter}.
 */
public final class DefaultXmlWriter extends XmlWriter {
    private final XMLStreamWriter writer;

    /**
     * Creates an instance of {@link XmlWriter} that writes to the provided {@link OutputStream}.
     *
     * @param outputStream The {@link OutputStream} where content will be written.
     * @return A new instance of {@link XmlWriter}.
     * @throws RuntimeException If an {@link XmlWriter} cannot be instantiated.
     */
    public static XmlWriter fromOutputStream(OutputStream outputStream) {
        try {
            return new DefaultXmlWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream));
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    private DefaultXmlWriter(XMLStreamWriter writer) {
        this.writer = writer;
    }

    @Override
    public XmlWriter writeStartDocument() {
        return writeStartDocument("1.0", "utf-8");
    }

    @Override
    public XmlWriter writeStartDocument(String version, String encoding) {
        try {
            writer.writeStartDocument(encoding, version);
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XmlWriter writeStartElement(String localName) {
        try {
            writer.writeStartElement(localName);
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XmlWriter writeStartElement(String prefix, String namespaceUri, String localName) {
        try {
            writer.writeStartElement(prefix, localName, getActualNamespaceUri(namespaceUri, prefix));
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XmlWriter writeStartSelfClosingElement(String localName) {
        try {
            writer.writeEmptyElement(localName);
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XmlWriter writeStartSelfClosingElement(String prefix, String namespaceUri, String localName) {
        try {
            writer.writeEmptyElement(prefix, localName, getActualNamespaceUri(namespaceUri, prefix));
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XmlWriter writeEndElement() {
        try {
            writer.writeEndElement();
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XmlWriter writeStringAttribute(String localName, String value) {
        try {
            writer.writeAttribute(localName, value);
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XmlWriter writeStringAttribute(String prefix, String namespaceUri, String localName, String value) {
        try {
            writer.writeAttribute(prefix, getActualNamespaceUri(namespaceUri, prefix), localName, value);
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XmlWriter writeString(String value) {
        try {
            writer.writeCharacters(value);
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XmlWriter writeCDataString(String value) {
        try {
            writer.writeCData(value);
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XmlWriter flush() {
        try {
            writer.flush();
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            writer.close();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    /*
     * Helper function that either returns the passed namespaceUri if it is non-null or retrieves the namespace URI
     * based on the passed prefix.
     */
    private String getActualNamespaceUri(String namespaceUri, String prefix) {
        return namespaceUri == null ? writer.getNamespaceContext().getNamespaceURI(prefix) : namespaceUri;
    }
}
