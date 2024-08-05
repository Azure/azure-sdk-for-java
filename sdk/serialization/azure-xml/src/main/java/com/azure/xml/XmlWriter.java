// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import com.azure.xml.implementation.aalto.stax.OutputFactoryImpl;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;

/**
 * Writes an XML encoded value to a stream.
 */
@SuppressWarnings("resource")
public final class XmlWriter implements AutoCloseable {
    private static final XMLOutputFactory XML_OUTPUT_FACTORY;

    static {
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        if ("com.sun.xml.internal.stream.XMLOutputFactoryImpl".equals(xmlOutputFactory.getClass().getName())) {
            xmlOutputFactory = new OutputFactoryImpl();
        }

        XML_OUTPUT_FACTORY = xmlOutputFactory;
    }

    private final XMLStreamWriter writer;

    /**
     * Creates an instance of {@link XmlWriter} that writes to the provided {@link OutputStream}.
     * <p>
     * This uses the {@link XMLStreamWriter} implementation provided by the default
     * {@link XMLOutputFactory#newInstance()}. If you need to provide a custom implementation of
     * {@link XMLStreamWriter} use {@link #fromXmlStreamWriter(XMLStreamWriter)}.
     *
     * @param xml The {@link OutputStream} where content will be written.
     * @return A new instance of {@link XmlWriter}.
     * @throws NullPointerException If {@code xml} is null.
     * @throws XMLStreamException If an {@link XmlWriter} cannot be instantiated.
     */
    public static XmlWriter toStream(OutputStream xml) throws XMLStreamException {
        Objects.requireNonNull(xml, "'xml' cannot be null.");
        return new XmlWriter(
            XML_OUTPUT_FACTORY.createXMLStreamWriter(new OutputStreamWriter(xml, StandardCharsets.UTF_8)));
    }

    /**
     * Creates an instance of {@link XmlWriter} that writes to the provided {@link Writer}.
     * <p>
     * This uses the {@link XMLStreamWriter} implementation provided by the default
     * {@link XMLOutputFactory#newInstance()}. If you need to provide a custom implementation of
     * {@link XMLStreamWriter} use {@link #fromXmlStreamWriter(XMLStreamWriter)}.
     *
     * @param xml The {@link Writer} where content will be written.
     * @return A new instance of {@link XmlWriter}.
     * @throws NullPointerException If {@code xml} is null.
     * @throws XMLStreamException If an {@link XmlWriter} cannot be instantiated.
     */
    public static XmlWriter toWriter(Writer xml) throws XMLStreamException {
        Objects.requireNonNull(xml, "'xml' cannot be null.");
        return new XmlWriter(XML_OUTPUT_FACTORY.createXMLStreamWriter(xml));
    }

    /**
     * Creates an instance of {@link XmlWriter} that writes to the provided {@link XMLStreamWriter}.
     *
     * @param writer The {@link XMLStreamWriter} where content will be written.
     * @return A new instance of {@link XmlWriter}.
     * @throws NullPointerException If {@code writer} is null.
     */
    public static XmlWriter fromXmlStreamWriter(XMLStreamWriter writer) {
        return new XmlWriter(writer);
    }

    /**
     * Creates an instance of {@link XmlWriter}.
     *
     * @param writer The {@link XMLStreamWriter} where content will be written.
     */
    private XmlWriter(XMLStreamWriter writer) {
        this.writer = Objects.requireNonNull(writer, "'writer' cannot be null.");
    }

    /**
     * Writes the XML document start ({@code <?xml version="1.0" encoding="UTF-8">}).
     * <p>
     * This uses the default version and encoding which are {@code 1.0} and {@code UTF-8} respectively. If a different
     * version or encoding is required use {@link #writeStartDocument(String, String)} which allows for specifying
     * those values.
     *
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML start document cannot be written.
     */
    public XmlWriter writeStartDocument() throws XMLStreamException {
        return writeStartDocument("1.0", "UTF-8");
    }

    /**
     * Writes the XML document start ({@code <?xml version="1.0" encoding="utf-8">}).
     * <p>
     * Both {@code version} and {@code encoding} are optional and if they aren't passed their default values will be
     * used. For {@code version} the default is {@code 1.0} and for {@code encoding} the default is {@code UTF-8}.
     *
     * @param version XML document version.
     * @param encoding XML document encoding.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML start document cannot be written.
     */
    public XmlWriter writeStartDocument(String version, String encoding) throws XMLStreamException {
        writer.writeStartDocument(encoding, version);
        return this;
    }

    /**
     * Begins an XML element start ({@code <tag}).
     * <p>
     * This call doesn't close ({@code >}) the XML element start but instead defers it until a call to begin another
     * element or to write the body of the element. This also requires an explicit call to {@link #writeEndElement()} to
     * end the XML element's body.
     * <p>
     * Calls to write attributes won't close the XML element.
     *
     * @param localName Name of the element.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML element start cannot be written.
     */
    public XmlWriter writeStartElement(String localName) throws XMLStreamException {
        return writeStartElement(null, localName);
    }

    /**
     * Begins an XML element start that has a prefix ({@code <prefix:tag}).
     * <p>
     * This call doesn't close ({@code >}) the XML element start but instead defers it until a call to begin another
     * element or to write the body of the element. This also requires an explicit call to {@link #writeEndElement()} to
     * end the XML element's body.
     * <p>
     * Calls to write attributes won't close the XML element.
     * <p>
     * If {@code prefix} is null this will behave the same as {@link #writeStartElement(String)}.
     *
     * @param namespaceUri Namespace URI to bind the prefix to, if null the default namespace is used.
     * @param localName Name of the element.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML element start cannot be written.
     */
    public XmlWriter writeStartElement(String namespaceUri, String localName) throws XMLStreamException {
        if (namespaceUri == null) {
            writer.writeStartElement(localName);
        } else {
            writer.writeStartElement(namespaceUri, localName);
        }
        return this;
    }

    /**
     * Begins an XML element start that will be self-closing ({@code <tag/>}).
     * <p>
     * This call doesn't close ({@code />}) the XML element start but instead defers it until a call to begin another
     * element. If there is an attempt to write the body of the element after beginning a self-closing element an
     * {@link IllegalStateException} will be thrown as self-closing elements do not have a body.
     * <p>
     * Calls to write attributes won't close the XML element.
     *
     * @param localName Name of the element.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML element start cannot be written.
     */
    public XmlWriter writeStartSelfClosingElement(String localName) throws XMLStreamException {
        return writeStartSelfClosingElement(null, localName);
    }

    /**
     * Begins an XML element start that has a prefix that will be self-closing ({@code <prefix:tag/>}).
     * <p>
     * This call doesn't close ({@code />}) the XML element start but instead defers it until a call to begin another
     * element. If there is an attempt to write the body of the element after beginning a self-closing element an
     * {@link IllegalStateException} will be thrown as self-closing elements do not have a body.
     * <p>
     * Calls to write attributes won't close the XML element.
     * <p>
     * If {@code prefix} is null this will behave the same as {@link #writeStartSelfClosingElement(String)}.
     *
     * @param namespaceUri Namespace URI to bind the prefix to, if null the default namespace is used.
     * @param localName Name of the element.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML element start cannot be written.
     */
    public XmlWriter writeStartSelfClosingElement(String namespaceUri, String localName) throws XMLStreamException {
        if (namespaceUri == null) {
            writer.writeEmptyElement(localName);
        } else {
            writer.writeEmptyElement(namespaceUri, localName);
        }
        return this;
    }

    /**
     * Ends the current XML element by writing the closing tag ({@code </tag>}).
     * <p>
     * This call will determine the XML element tag name and prefix, if there is one, to close the current XML element
     * scope.
     *
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML element end cannot be written.
     */
    public XmlWriter writeEndElement() throws XMLStreamException {
        writer.writeEndElement();
        return this;
    }

    /**
     * Writes a default XML namespace.
     *
     * @param namespaceUri Namespace URI to bind as the default namespace.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML namespace cannot be written.
     */
    public XmlWriter writeNamespace(String namespaceUri) throws XMLStreamException {
        if (Objects.equals(writer.getNamespaceContext().getNamespaceURI(DEFAULT_NS_PREFIX), namespaceUri)) {
            return this;
        }

        writer.setDefaultNamespace(namespaceUri);
        writer.writeDefaultNamespace(namespaceUri);
        return this;
    }

    /**
     * Writes an XML namespace with a specified prefix.
     * <p>
     * If the {@code namespacePrefix} is null or {@code xmlns} calling this method is equivalent to
     * {@link #writeNamespace(String)}.
     *
     * @param namespacePrefix Prefix that the namespace binds.
     * @param namespaceUri Namespace URI to bind to the prefix.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML namespace cannot be written.
     */
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

    /**
     * Writes a String attribute ({@code attribute="value"}).
     *
     * @param localName Name of the attribute.
     * @param value Value of the attribute.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML attribute cannot be written.
     */
    public XmlWriter writeStringAttribute(String localName, String value) throws XMLStreamException {
        return writeStringAttribute(null, localName, value);
    }

    /**
     * Writes a String attribute that has a prefix ({@code prefix:attribute="value"}).
     *
     * @param namespaceUri Namespace URI to bind the prefix to, if null the default namespace is used.
     * @param localName Name of the attribute.
     * @param value Value of the attribute.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML attribute cannot be written.
     */
    public XmlWriter writeStringAttribute(String namespaceUri, String localName, String value)
        throws XMLStreamException {
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

    /**
     * Writes a binary attribute as a base64 string ({@code attribute="value"}).
     * <p>
     * If the {@code value} is null this is a no-op.
     *
     * @param localName Name of the attribute.
     * @param value Binary value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML attribute cannot be written.
     */
    public XmlWriter writeBinaryAttribute(String localName, byte[] value) throws XMLStreamException {
        return writeBinaryAttribute(null, localName, value);
    }

    /**
     * Writes a binary attribute as a base64 string that has a prefix ({@code prefix:attribute="value"}).
     * <p>
     * If the {@code value} is null this is a no-op.
     *
     * @param namespaceUri Namespace URI to bind the prefix to, if null the default namespace is used.
     * @param localName Name of the attribute.
     * @param value Binary value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML attribute cannot be written.
     */
    public XmlWriter writeBinaryAttribute(String namespaceUri, String localName, byte[] value)
        throws XMLStreamException {
        if (value == null) {
            return this;
        }

        return writeStringAttribute(namespaceUri, localName, convertBytesToString(value));
    }

    /**
     * Writes a boolean attribute ({@code attribute="true"}).
     *
     * @param localName Name of the attribute.
     * @param value boolean value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML attribute cannot be written.
     */
    public XmlWriter writeBooleanAttribute(String localName, boolean value) throws XMLStreamException {
        return writeStringAttribute(localName, String.valueOf(value));
    }

    /**
     * Writes a boolean attribute that has a prefix ({@code prefix:attribute="true"}).
     *
     * @param namespaceUri Namespace URI to bind the prefix to, if null the default namespace is used.
     * @param localName Name of the attribute.
     * @param value boolean value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML attribute cannot be written.
     */
    public XmlWriter writeBooleanAttribute(String namespaceUri, String localName, boolean value)
        throws XMLStreamException {
        return writeStringAttribute(namespaceUri, localName, String.valueOf(value));
    }

    /**
     * Writes a nullable boolean attribute ({@code attribute="false"}).
     * <p>
     * If the {@code value} is null this is a no-op.
     *
     * @param localName Name of the attribute.
     * @param value Boolean value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML attribute cannot be written.
     */
    public XmlWriter writeBooleanAttribute(String localName, Boolean value) throws XMLStreamException {
        return writeBooleanAttribute(null, localName, value);
    }

    /**
     * Writes a nullable boolean attribute that has a prefix ({@code prefix:attribute="false"}).
     * <p>
     * If the {@code value} is null this is a no-op.
     *
     * @param namespaceUri Namespace URI to bind the prefix to, if null the default namespace is used.
     * @param localName Name of the attribute.
     * @param value Boolean value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML attribute cannot be written.
     */
    public XmlWriter writeBooleanAttribute(String namespaceUri, String localName, Boolean value)
        throws XMLStreamException {
        if (value == null) {
            return this;
        }

        return writeStringAttribute(namespaceUri, localName, String.valueOf(value));
    }

    /**
     * Writes a double attribute ({@code attribute="3.14"}).
     *
     * @param localName Name of the attribute.
     * @param value double value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML attribute cannot be written.
     */
    public XmlWriter writeDoubleAttribute(String localName, double value) throws XMLStreamException {
        return writeStringAttribute(localName, String.valueOf(value));
    }

    /**
     * Writes a double attribute that has a prefix ({@code prefix:attribute="3.14"}).
     *
     * @param namespaceUri Namespace URI to bind the prefix to, if null the default namespace is used.
     * @param localName Name of the attribute.
     * @param value double value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML attribute cannot be written.
     */
    public XmlWriter writeDoubleAttribute(String namespaceUri, String localName, double value)
        throws XMLStreamException {
        return writeStringAttribute(namespaceUri, localName, String.valueOf(value));
    }

    /**
     * Writes a float attribute ({@code attribute="2.7"}).
     *
     * @param localName Name of the attribute.
     * @param value float value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML attribute cannot be written.
     */
    public XmlWriter writeFloatAttribute(String localName, float value) throws XMLStreamException {
        return writeStringAttribute(localName, String.valueOf(value));
    }

    /**
     * Writes a float attribute that has a prefix ({@code prefix:attribute="2.7"}).
     *
     * @param namespaceUri Namespace URI to bind the prefix to, if null the default namespace is used.
     * @param localName Name of the attribute.
     * @param value float value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML attribute cannot be written.
     */
    public XmlWriter writeFloatAttribute(String namespaceUri, String localName, float value) throws XMLStreamException {
        return writeStringAttribute(namespaceUri, localName, String.valueOf(value));
    }

    /**
     * Writes an int attribute ({@code attribute="10"}).
     *
     * @param localName Name of the attribute.
     * @param value int value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML attribute cannot be written.
     */
    public XmlWriter writeIntAttribute(String localName, int value) throws XMLStreamException {
        return writeStringAttribute(localName, String.valueOf(value));
    }

    /**
     * Writes an int attribute that has a prefix ({@code prefix:attribute="10"}).
     *
     * @param namespaceUri Namespace URI to bind the prefix to, if null the default namespace is used.
     * @param localName Name of the attribute.
     * @param value int value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML attribute cannot be written.
     */
    public XmlWriter writeIntAttribute(String namespaceUri, String localName, int value) throws XMLStreamException {
        return writeStringAttribute(namespaceUri, localName, String.valueOf(value));
    }

    /**
     * Writes a long attribute ({@code attribute="100000000000"}).
     *
     * @param localName Name of the attribute.
     * @param value long value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML attribute cannot be written.
     */
    public XmlWriter writeLongAttribute(String localName, long value) throws XMLStreamException {
        return writeStringAttribute(localName, String.valueOf(value));
    }

    /**
     * Writes a long attribute that has a prefix ({@code prefix:attribute="100000000000"}).
     *
     * @param namespaceUri Namespace URI to bind the prefix to, if null the default namespace is used.
     * @param localName Name of the attribute.
     * @param value long value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML attribute cannot be written.
     */
    public XmlWriter writeLongAttribute(String namespaceUri, String localName, long value) throws XMLStreamException {
        return writeStringAttribute(namespaceUri, localName, String.valueOf(value));
    }

    /**
     * Writes a nullable number attribute ({@code attribute="number"}).
     *
     * @param localName Name of the attribute.
     * @param value Number value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML attribute cannot be written.
     */
    public XmlWriter writeNumberAttribute(String localName, Number value) throws XMLStreamException {
        return writeNumberAttribute(null, localName, value);
    }

    /**
     * Writes a nullable number attribute that has a prefix ({@code prefix:attribute="number"}).
     *
     * @param namespaceUri Namespace URI to bind the prefix to, if null the default namespace is used.
     * @param localName Name of the attribute.
     * @param value Number value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML attribute cannot be written.
     */
    public XmlWriter writeNumberAttribute(String namespaceUri, String localName, Number value)
        throws XMLStreamException {
        if (value == null) {
            return this;
        }

        return writeStringAttribute(namespaceUri, localName, String.valueOf(value));
    }

    /**
     * Writes a binary element as a base64 string ({@code <tag>value</tag}).
     * <p>
     * If the {@code value} is null this is a no-op.
     *
     * @param localName Name of the element.
     * @param value Binary value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML element and value cannot be written.
     */
    public XmlWriter writeBinaryElement(String localName, byte[] value) throws XMLStreamException {
        return writeBinaryElement(null, localName, value);
    }

    /**
     * Writes a binary element as a base64 string that has a prefix ({@code <tag>value</tag}).
     * <p>
     * If the {@code value} is null this is a no-op.
     *
     * @param namespaceUri Namespace URI to bind the prefix to, if null the default namespace is used.
     * @param localName Name of the element.
     * @param value Binary value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML element and value cannot be written.
     */
    public XmlWriter writeBinaryElement(String namespaceUri, String localName, byte[] value) throws XMLStreamException {
        if (value == null) {
            return this;
        }

        return writeStartElement(namespaceUri, localName).writeBinary(value).writeEndElement();
    }

    /**
     * Writes a boolean element ({@code <tag>true</tag}).
     *
     * @param localName Name of the element.
     * @param value boolean value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML element and value cannot be written.
     */
    public XmlWriter writeBooleanElement(String localName, boolean value) throws XMLStreamException {
        return writeBooleanElement(null, localName, value);
    }

    /**
     * Writes a boolean element that has a prefix ({@code <tag>true</tag}).
     *
     * @param namespaceUri Namespace URI to bind the prefix to, if null the default namespace is used.
     * @param localName Name of the element.
     * @param value boolean value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML element and value cannot be written.
     */
    public XmlWriter writeBooleanElement(String namespaceUri, String localName, boolean value)
        throws XMLStreamException {
        return writeStartElement(namespaceUri, localName).writeBoolean(value).writeEndElement();
    }

    /**
     * Writes a nullable boolean element ({@code <tag>true</tag}).
     * <p>
     * If the {@code value} is null this is a no-op.
     *
     * @param localName Name of the element.
     * @param value Boolean value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML element and value cannot be written.
     */
    public XmlWriter writeBooleanElement(String localName, Boolean value) throws XMLStreamException {
        return writeBooleanElement(null, localName, value);
    }

    /**
     * Writes a nullable boolean element that has a prefix ({@code <tag>true</tag}).
     * <p>
     * If the {@code value} is null this is a no-op.
     *
     * @param namespaceUri Namespace URI to bind the prefix to, if null the default namespace is used.
     * @param localName Name of the element.
     * @param value Boolean value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML element and value cannot be written.
     */
    public XmlWriter writeBooleanElement(String namespaceUri, String localName, Boolean value)
        throws XMLStreamException {
        if (value == null) {
            return this;
        }

        return writeStartElement(namespaceUri, localName).writeBoolean(value.booleanValue()).writeEndElement();
    }

    /**
     * Writes a double element ({@code <tag>3.14</tag}).
     *
     * @param localName Name of the element.
     * @param value double value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML element and value cannot be written.
     */
    public XmlWriter writeDoubleElement(String localName, double value) throws XMLStreamException {
        return writeDoubleElement(null, localName, value);
    }

    /**
     * Writes a double element that has a prefix ({@code <tag>3.14</tag}).
     *
     * @param namespaceUri Namespace URI to bind the prefix to, if null the default namespace is used.
     * @param localName Name of the element.
     * @param value double value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML element and value cannot be written.
     */
    public XmlWriter writeDoubleElement(String namespaceUri, String localName, double value) throws XMLStreamException {
        return writeStartElement(namespaceUri, localName).writeDouble(value).writeEndElement();
    }

    /**
     * Writes a float element ({@code <tag>2.7</tag}).
     *
     * @param localName Name of the element.
     * @param value float value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML element and value cannot be written.
     */
    public XmlWriter writeFloatElement(String localName, float value) throws XMLStreamException {
        return writeFloatElement(null, localName, value);
    }

    /**
     * Writes a float element that has a prefix ({@code <tag>2.7</tag}).
     *
     * @param namespaceUri Namespace URI to bind the prefix to, if null the default namespace is used.
     * @param localName Name of the element.
     * @param value float value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML element and value cannot be written.
     */
    public XmlWriter writeFloatElement(String namespaceUri, String localName, float value) throws XMLStreamException {
        return writeStartElement(namespaceUri, localName).writeFloat(value).writeEndElement();
    }

    /**
     * Writes an int element ({@code <tag>10</tag}).
     *
     * @param localName Name of the element.
     * @param value int value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML element and value cannot be written.
     */
    public XmlWriter writeIntElement(String localName, int value) throws XMLStreamException {
        return writeIntElement(null, localName, value);
    }

    /**
     * Writes an int element that has a prefix ({@code <tag>10</tag}).
     *
     * @param namespaceUri Namespace URI to bind the prefix to, if null the default namespace is used.
     * @param localName Name of the element.
     * @param value int value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML element and value cannot be written.
     */
    public XmlWriter writeIntElement(String namespaceUri, String localName, int value) throws XMLStreamException {
        return writeStartElement(namespaceUri, localName).writeInt(value).writeEndElement();
    }

    /**
     * Writes a long element ({@code <tag>100000000000</tag}).
     *
     * @param localName Name of the element
     * @param value long value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML element and value cannot be written.
     */
    public XmlWriter writeLongElement(String localName, long value) throws XMLStreamException {
        return writeLongElement(null, localName, value);
    }

    /**
     * Writes a long element that has a prefix ({@code <tag>100000000000</tag}).
     *
     * @param namespaceUri Namespace URI to bind the prefix to, if null the default namespace is used.
     * @param localName Name of the element.
     * @param value long value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML element and value cannot be written.
     */
    public XmlWriter writeLongElement(String namespaceUri, String localName, long value) throws XMLStreamException {
        return writeStartElement(namespaceUri, localName).writeLong(value).writeEndElement();
    }

    /**
     * Writes a nullable number element ({@code <tag>number</tag}).
     * <p>
     * If the {@code value} is null this is a no-op.
     *
     * @param localName Name of the element.
     * @param value Number value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML element and value cannot be written.
     */
    public XmlWriter writeNumberElement(String localName, Number value) throws XMLStreamException {
        return writeNumberElement(null, localName, value);
    }

    /**
     * Writes a nullable number element that has a prefix ({@code <tag>number</tag}).
     * <p>
     * If the {@code value} is null this is a no-op.
     *
     * @param namespaceUri Namespace URI to bind the prefix to, if null the default namespace is used.
     * @param localName Name of the element.
     * @param value Number value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML element and value cannot be written.
     */
    public XmlWriter writeNumberElement(String namespaceUri, String localName, Number value) throws XMLStreamException {
        if (value == null) {
            return this;
        }

        return writeStartElement(namespaceUri, localName).writeNumber(value).writeEndElement();
    }

    /**
     * Writes a string element ({@code <tag>string</tag}).
     * <p>
     * If the {@code value} is null this is a no-op.
     *
     * @param localName Name of the element.
     * @param value String value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML element and value cannot be written.
     */
    public XmlWriter writeStringElement(String localName, String value) throws XMLStreamException {
        return writeStringElement(null, localName, value);
    }

    /**
     * Writes a string element that has a prefix ({@code <tag>string</tag}).
     * <p>
     * If the {@code value} is null this is a no-op.
     *
     * @param namespaceUri Namespace URI to bind the prefix to, if null the default namespace is used.
     * @param localName Name of the element.
     * @param value String value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML element and value cannot be written.
     */
    public XmlWriter writeStringElement(String namespaceUri, String localName, String value) throws XMLStreamException {
        if (value == null) {
            return this;
        }

        return writeStartElement(namespaceUri, localName).writeString(value).writeEndElement();
    }

    /**
     * Writes an {@link XmlSerializable} object.
     * <p>
     * If the {@code value} is null this is a no-op.
     *
     * @param value {@link XmlSerializable} object to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML object cannot be written.
     */
    public XmlWriter writeXml(XmlSerializable<?> value) throws XMLStreamException {
        return writeXml(value, null);
    }

    /**
     * Writes an {@link XmlSerializable} object.
     * <p>
     * If the {@code value} is null this is a no-op.
     * <p>
     * If {@code rootElementName} is null this is the same as calling {@link #writeXml(XmlSerializable)}.
     *
     * @param value {@link XmlSerializable} object to write.
     * @param rootElementName Override of the XML element name defined by the object.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML object cannot be written.
     */
    public XmlWriter writeXml(XmlSerializable<?> value, String rootElementName) throws XMLStreamException {
        return value == null ? this : value.toXml(this, rootElementName);
    }

    /**
     * Writes a binary value as a base64 string.
     * <p>
     * If the {@code value} is null this is a no-op.
     *
     * @param value Binary value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML value cannot be written.
     */
    public XmlWriter writeBinary(byte[] value) throws XMLStreamException {
        return value == null ? this : writeString(convertBytesToString(value));
    }

    /**
     * Writes a boolean value.
     *
     * @param value boolean value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML value cannot be written.
     */
    public XmlWriter writeBoolean(boolean value) throws XMLStreamException {
        return writeString(String.valueOf(value));
    }

    /**
     * Writes a nullable boolean value.
     * <p>
     * If the {@code value} is null this is a no-op.
     *
     * @param value Boolean value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML value cannot be written.
     */
    public XmlWriter writeBoolean(Boolean value) throws XMLStreamException {
        return value == null ? this : writeString(String.valueOf(value));
    }

    /**
     * Writes a double value.
     *
     * @param value double value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML value cannot be written.
     */
    public XmlWriter writeDouble(double value) throws XMLStreamException {
        return writeString(String.valueOf(value));
    }

    /**
     * Writes a float value.
     *
     * @param value float value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML value cannot be written.
     */
    public XmlWriter writeFloat(float value) throws XMLStreamException {
        return writeString(String.valueOf(value));
    }

    /**
     * Writes an int value.
     *
     * @param value int value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML value cannot be written.
     */
    public XmlWriter writeInt(int value) throws XMLStreamException {
        return writeString(String.valueOf(value));
    }

    /**
     * Writes a long value.
     *
     * @param value long value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML value cannot be written.
     */
    public XmlWriter writeLong(long value) throws XMLStreamException {
        return writeString(String.valueOf(value));
    }

    /**
     * Writes a nullable number.
     * <p>
     * If {@code value} is null this is a no-op.
     *
     * @param value Number value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML value cannot be written.
     */
    public XmlWriter writeNumber(Number value) throws XMLStreamException {
        return value == null ? this : writeString(String.valueOf(value));
    }

    /**
     * Writes a value directly into an XML element ({@code <tag>value</tag>}).
     * <p>
     * This doesn't write the XML element start tag or end tag.
     * <p>
     * {@link #writeCDataString(String)} is a convenience API if an XML CData value needs to be written.
     *
     * @param value Value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML value cannot be written.
     */
    public XmlWriter writeString(String value) throws XMLStreamException {
        if (value == null) {
            return this;
        }

        writer.writeCharacters(value);
        return this;
    }

    /**
     * Writes a CData value directly into an XML element ({@code <tag><![CDATA[value]]></tag>}).
     * <p>
     * This doesn't write the XML element start tag or end tag.
     * <p>
     * This API is a convenience over {@link #writeString(String)} for CData values, it is possible to use
     * {@link #writeString(String)} instead of this API.
     *
     * @param value CData value to write.
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the XML CData value cannot be written.
     */
    public XmlWriter writeCDataString(String value) throws XMLStreamException {
        if (value == null) {
            return this;
        }

        writer.writeCData(value);
        return this;
    }

    /**
     * Flushes any un-flushed content that has been written to the {@link XmlWriter}.
     *
     * @return The updated XmlWriter object.
     * @throws XMLStreamException If the un-flushed XML content could not be flushed.
     */
    public XmlWriter flush() throws XMLStreamException {
        writer.flush();
        return this;
    }

    /**
     * Closes the XML stream.
     * <p>
     * During closing the implementation of {@link XmlWriter} must flush any un-flushed content.
     *
     * @throws XMLStreamException If the underlying content store fails to close.
     */
    @Override
    public void close() throws XMLStreamException {
        writer.flush();
        writer.close();
    }

    private static String convertBytesToString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        return Base64.getEncoder().encodeToString(bytes);
    }
}
