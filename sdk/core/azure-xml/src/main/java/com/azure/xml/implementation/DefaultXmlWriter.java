// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml.implementation;

import com.azure.xml.XmlWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;

/**
 * Default {@link XmlWriter} implementation based on {@link XMLStreamWriter}.
 */
public final class DefaultXmlWriter extends XmlWriter {
    private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newFactory();

    private final XMLStreamWriter writer;

    /**
     * Creates an instance of {@link XmlWriter} that writes to the provided {@link OutputStream}.
     *
     * @param outputStream The {@link OutputStream} where content will be written.
     * @return A new instance of {@link XmlWriter}.
     * @throws RuntimeException If an {@link XmlWriter} cannot be instantiated.
     */
    public static XmlWriter toStream(OutputStream outputStream) {
        try {
            return new DefaultXmlWriter(XML_OUTPUT_FACTORY.createXMLStreamWriter(
                new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)));
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Creates an instance of {@link XmlWriter} that writes to the provided {@link Writer}.
     *
     * @param writer The {@link Writer} where content will be written.
     * @return A new instance of {@link XmlWriter}.
     * @throws RuntimeException If an {@link XmlWriter} cannot be instantiated.
     */
    public static XmlWriter toWriter(Writer writer) {
        try {
            return new DefaultXmlWriter(XML_OUTPUT_FACTORY.createXMLStreamWriter(writer));
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    private DefaultXmlWriter(XMLStreamWriter writer) {
        this.writer = writer;
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
    public XmlWriter writeStartElement(String namespaceUri, String localName) {
        try {
            if (namespaceUri == null) {
                writer.writeStartElement(localName);
            } else {
                writer.writeStartElement(namespaceUri, localName);
            }
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XmlWriter writeStartSelfClosingElement(String namespaceUri, String localName) {
        try {
            if (namespaceUri == null) {
                writer.writeEmptyElement(localName);
            } else {
                writer.writeEmptyElement(namespaceUri, localName);
            }
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XmlWriter writeNamespace(String namespaceUri) {
        if (Objects.equals(writer.getNamespaceContext().getNamespaceURI(DEFAULT_NS_PREFIX), namespaceUri)) {
            return this;
        }

        try {
            writer.setDefaultNamespace(namespaceUri);
            writer.writeDefaultNamespace(namespaceUri);
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
    public XmlWriter writeStringAttribute(String namespaceUri, String localName, String value) {
        if (value == null) {
            return this;
        }

        try {
            if (namespaceUri == null) {
                writer.writeAttribute(localName, value);
            } else {
                writer.writeAttribute(namespaceUri, localName, value);
            }
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XmlWriter writeString(String value) {
        if (value == null) {
            return this;
        }

        try {
            writer.writeCharacters(value);
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XmlWriter writeCDataString(String value) {
        if (value == null) {
            return this;
        }

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
}
