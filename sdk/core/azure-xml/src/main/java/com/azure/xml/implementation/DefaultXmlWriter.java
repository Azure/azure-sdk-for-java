// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml.implementation;

import com.azure.xml.XmlWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
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
    private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newInstance();

    private final XMLStreamWriter writer;

    /**
     * Creates an instance of {@link XmlWriter} that writes to the provided {@link OutputStream}.
     *
     * @param xml The {@link OutputStream} where content will be written.
     * @return A new instance of {@link XmlWriter}.
     * @throws NullPointerException If {@code xml} is null.
     * @throws XMLStreamException If an {@link XmlWriter} cannot be instantiated.
     */
    public static XmlWriter toStream(OutputStream xml) throws XMLStreamException {
        Objects.requireNonNull(xml, "'xml' cannot be null.");
        return new DefaultXmlWriter(XML_OUTPUT_FACTORY.createXMLStreamWriter(
            new OutputStreamWriter(xml, StandardCharsets.UTF_8)));
    }

    /**
     * Creates an instance of {@link XmlWriter} that writes to the provided {@link Writer}.
     *
     * @param xml The {@link Writer} where content will be written.
     * @return A new instance of {@link XmlWriter}.
     * @throws NullPointerException If {@code xml} is null.
     * @throws XMLStreamException If an {@link XmlWriter} cannot be instantiated.
     */
    public static XmlWriter toWriter(Writer xml) throws XMLStreamException {
        Objects.requireNonNull(xml, "'xml' cannot be null.");
        return new DefaultXmlWriter(XML_OUTPUT_FACTORY.createXMLStreamWriter(xml));
    }

    private DefaultXmlWriter(XMLStreamWriter writer) {
        this.writer = writer;
    }

    @Override
    public XmlWriter writeStartDocument(String version, String encoding) throws XMLStreamException {
        writer.writeStartDocument(encoding, version);
        return this;
    }

    @Override
    public XmlWriter writeStartElement(String namespaceUri, String localName) throws XMLStreamException {
        if (namespaceUri == null) {
            writer.writeStartElement(localName);
        } else {
            writer.writeStartElement(namespaceUri, localName);
        }
        return this;
    }

    @Override
    public XmlWriter writeStartSelfClosingElement(String namespaceUri, String localName) throws XMLStreamException {
        if (namespaceUri == null) {
            writer.writeEmptyElement(localName);
        } else {
            writer.writeEmptyElement(namespaceUri, localName);
        }
        return this;
    }

    @Override
    public XmlWriter writeNamespace(String namespaceUri) throws XMLStreamException {
        if (Objects.equals(writer.getNamespaceContext().getNamespaceURI(DEFAULT_NS_PREFIX), namespaceUri)) {
            return this;
        }

        writer.setDefaultNamespace(namespaceUri);
        writer.writeDefaultNamespace(namespaceUri);
        return this;
    }

    @Override
    public XmlWriter writeNamespace(String namespacePrefix, String namespaceUri) throws XMLStreamException {
        if (namespacePrefix == null || "xmlns".equals(namespacePrefix)) {
            return writeNamespace(namespacePrefix);
        }

        if (Objects.equals(writer.getNamespaceContext().getNamespaceURI(namespacePrefix), namespaceUri)) {
            return this;
        }

        writer.writeNamespace(namespacePrefix, namespaceUri);
        return this;
    }

    @Override
    public XmlWriter writeEndElement() throws XMLStreamException {
        writer.writeEndElement();
        return this;
    }

    @Override
    public XmlWriter writeStringAttribute(String namespaceUri, String localName,
        String value) throws XMLStreamException {
        if (value == null) {
            return this;
        }

        if (namespaceUri == null) {
            writer.writeAttribute(localName, value);
        } else {
            writer.writeAttribute(namespaceUri, localName, value);
        }
        return this;
    }

    @Override
    public XmlWriter writeString(String value) throws XMLStreamException {
        if (value == null) {
            return this;
        }

        writer.writeCharacters(value);
        return this;
    }

    @Override
    public XmlWriter writeCDataString(String value) throws XMLStreamException {
        if (value == null) {
            return this;
        }

        writer.writeCData(value);
        return this;
    }

    @Override
    public XmlWriter flush() throws XMLStreamException {
        writer.flush();
        return this;
    }

    @Override
    public void close() throws XMLStreamException {
        writer.flush();
        writer.close();
    }
}
