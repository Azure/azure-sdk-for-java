// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

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
     * param xmlWriter Where the object's XML will be written.
     *
     * @return The {@link XmlWriter} where the JSON was written for chaining.
     */
    XmlWriter toXml(XmlWriter xmlWriter);

    /**
     * Reads an XML stream into an object.
     * <p>
     * Implementations of {@link XmlSerializable} must define this method, otherwise an
     * {@link UnsupportedOperationException} will be thrown.
     *
     * @param xmlReader The {@link XmlReader} being read.
     * @param <T> The type of the object.
     * @return The object that the XML stream represented, may return null.
     */
    static <T extends XmlSerializable<T>> T fromXml(XmlReader xmlReader) {
        throw new UnsupportedOperationException("Implementation of XmlSerializable must define this factory method.");
    }
}
