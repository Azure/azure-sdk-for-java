// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.serialization.xml;

import javax.xml.stream.XMLStreamException;

/**
 * Indicates that the implementing class can be serialized to and deserialized from XML.
 * <p>
 * Since deserialization needs to work without an instance of the class, implementing this interface it's assumed the
 * class has static methods {@code #fromXml(XmlReader)} and {@code #fromXml(XmlReader, String)} that deserializes an
 * instance of that class. The contract for reading XML from {@link XmlReader} is that the initial state of the reader
 * on call will either be a null {@link XmlToken} or be {@link XmlToken#START_ELEMENT} for the object. So, for objects
 * calling out to other {@link XmlSerializable} objects for deserialization, they'll pass the reader pointing to the
 * token after the {@link XmlToken#START_ELEMENT}. This way objects reading XML will be self-encapsulated for reading
 * properly formatted XML. And, if an error occurs during deserialization an {@link IllegalStateException} should be
 * thrown.
 *
 * @param <T> The type of the object that is XML serializable.
 *
 * @see io.clientcore.core.serialization.xml
 * @see XmlReader
 * @see XmlWriter
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
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed io.clientcore.core.serialization.xml.XmlSerializable.toXml#XmlWriter -->
     * <pre>
     * &#64;Override
     * public XmlWriter toXml&#40;XmlWriter xmlWriter&#41; throws XMLStreamException &#123;
     *     &#47;&#47; Pass null as the rootElementName to use the default root element name.
     *     &#47;&#47; Overall, toXml&#40;XmlWriter&#41; is just convenience for toXml&#40;XmlWriter, null&#41;.
     *     return toXml&#40;xmlWriter, null&#41;;
     * &#125;
     * </pre>
     * <!-- end io.clientcore.core.serialization.xml.XmlSerializable.toXml#XmlWriter -->
     *
     * @param xmlWriter The {@link XmlWriter} being written to.
     * @return The {@link XmlWriter} where the XML was written for chaining.
     * @throws XMLStreamException If the object fails to be written to the {@code xmlWriter}.
     */
    default XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException {
        return toXml(xmlWriter, null);
    }

    /**
     * Writes the object to the passed {@link XmlWriter}.
     * <p>
     * The contract for writing XML to {@link XmlWriter} is that the object being written will handle opening and
     * closing its own XML object. So, for objects calling out to other {@link XmlSerializable} objects for
     * serialization, they'll pass the {@link XmlWriter} to the other {@link XmlSerializable} object. This way objects
     * writing XML will be self-encapsulated for writing properly formatted XML.
     * <p>
     * This differs from {@link #toXml(XmlWriter)} in that it allows the root element name to be overridden. This is
     * useful when the model can serialize using different root element names.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed io.clientcore.core.serialization.xml.XmlSerializable.toXml#XmlWriter-String -->
     * <pre>
     * &#64;Override
     * public XmlWriter toXml&#40;XmlWriter xmlWriter, String rootElementName&#41; throws XMLStreamException &#123;
     *     &#47;&#47; The call to XmlSerializable.toXml handles writing the XML start document
     *     &#47;&#47; &#40;&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;&gt;&#41;.
     *     &#47;&#47; Write the start of the XML element.
     *     xmlWriter.writeStartElement&#40;getRootElementName&#40;rootElementName, &quot;author&quot;&#41;&#41;;
     *
     *     &#47;&#47; Namespace and attribute writing happens after wiring the start of the element. The element start isn't
     *     &#47;&#47; finished until end element or starting another element is called.
     *     xmlWriter.writeNamespace&#40;&quot;http:&#47;&#47;www.w3.org&#47;2005&#47;Atom&quot;&#41;;
     *
     *     &#47;&#47; Convenience method that writes an entire element with a single API call. This is used when the element
     *     &#47;&#47; doesn't have any attributes, namespaces, or child elements.
     *     xmlWriter.writeStringElement&#40;&quot;name&quot;, name&#41;;
     *
     *     &#47;&#47; Finish writing the XML element. No need to flush as the caller will handle that.
     *     return xmlWriter.writeEndElement&#40;&#41;;
     * &#125;
     * </pre>
     * <!-- end io.clientcore.core.serialization.xml.XmlSerializable.toXml#XmlWriter-String -->
     *
     * @param xmlWriter The {@link XmlWriter} being written to.
     * @param rootElementName Optional root element name to override the default defined by the model. Used to support
     * cases where the model can serialize using different root element names.
     * @return The {@link XmlWriter} where the XML was written for chaining.
     * @throws XMLStreamException If the object fails to be written to the {@code xmlWriter}.
     */
    XmlWriter toXml(XmlWriter xmlWriter, String rootElementName) throws XMLStreamException;
}
