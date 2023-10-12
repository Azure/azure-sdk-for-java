// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import javax.xml.stream.XMLStreamException;

/**
 * Indicates that the implementing class can be serialized to and deserialized from XML.
 * <p>
 * Since deserialization needs to work without an instance of the class, implementing this interface it's assumed the
 * class has a static method {@code fromXml(XmlReader)} that deserializes an instance of that class. The contract for
 * reading XML... TODO (alzimmer): finish this javadoc
 *
 * @param <T> The type of the object that is XML serializable.
 */
public interface XmlSerializable<T extends XmlSerializable<T>> {
    /**
     * Writes the object to the passed {@link XmlWriter}.
     * <p>
     * The contract for writing XML to {@link XmlWriter} is that the object being written will handle opening and
     * closing its own XML object. So, for objects calling out to other {@link XmlSerializable} objects for
     * serialization, they'll pass the {@link XmlWriter} to the other {@link XmlSerializable} object. This way objects
     * writing XML will be self-encapsulated for writing properly formatted XML.
     *
     * @param xmlWriter The {@link XmlWriter} being written to.
     * @return The {@link XmlWriter} where the JSON was written for chaining.
     * @throws XMLStreamException If the object fails to be written to the {@code xmlWriter}.
     */
    XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException;

    /**
     * Writes the object to the passed {@link XmlWriter}.
     * <p>
     * The contract for writing XML to {@link XmlWriter} is that the object being written will handle opening and
     * closing its own XML object. So, for objects calling out to other {@link XmlSerializable} objects for
     * serialization, they'll pass the {@link XmlWriter} to the other {@link XmlSerializable} object. This way objects
     * writing XML will be self-encapsulated for writing properly formatted XML.
     *
     * @param xmlWriter The {@link XmlWriter} being written to.
     * @param rootElementName Optional root element name to override the default defined by the model. Used to support
     * cases where the model can serialize using different root element names.
     * @return The {@link XmlWriter} where the JSON was written for chaining.
     * @throws XMLStreamException If the object fails to be written to the {@code xmlWriter}.
     */
    XmlWriter toXml(XmlWriter xmlWriter, String rootElementName) throws XMLStreamException;

    /**
     * Reads an XML stream into an object.
     * <p>
     * Implementations of {@link XmlSerializable} must define this method, otherwise an
     * {@link UnsupportedOperationException} will be thrown.
     *
     * @param xmlReader The {@link XmlReader} being read.
     * @param <T> The type of the object.
     * @return The object that the XML stream represented, may return null.
     * @throws XMLStreamException If an object fails to be read from the {@code xmlReader}.
     */
    static <T extends XmlSerializable<T>> T fromXml(XmlReader xmlReader) throws XMLStreamException {
        throw new UnsupportedOperationException("Implementation of XmlSerializable must define this factory method.");
    }

    /**
     * Reads an XML stream into an object.
     * <p>
     * Implementations of {@link XmlSerializable} must define this method, otherwise an
     * {@link UnsupportedOperationException} will be thrown.
     *
     * @param xmlReader The {@link XmlReader} being read.
     * @param rootElementName Optional root element name to override the default defined by the model. Used to support
     * cases where the model can deserialize from different root element names.
     * @param <T> The type of the object.
     * @return The object that the XML stream represented, may return null.
     * @throws XMLStreamException If an object fails to be read from the {@code xmlReader}.
     */
    static <T extends XmlSerializable<T>> T fromXml(XmlReader xmlReader, String rootElementName)
        throws XMLStreamException {
        throw new UnsupportedOperationException("Implementation of XmlSerializable must define this factory method.");
    }
}
