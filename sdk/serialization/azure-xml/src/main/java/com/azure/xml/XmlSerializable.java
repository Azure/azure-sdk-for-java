// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import javax.xml.stream.XMLStreamException;

/**
 * Indicates that the implementing class can be serialized to and deserialized from XML.
 * <p>
 * Since deserialization needs to work without an instance of the class, implementing this interface it's assumed the
 * class has static methods {@link #fromXml(XmlReader)} and {@link #fromXml(XmlReader, String)} that deserializes an
 * instance of that class. The contract for reading XML from {@link XmlReader} is that the initial state of the reader
 * on call will either be a null {@link XmlToken} or be {@link XmlToken#START_ELEMENT} for the object. So, for objects
 * calling out to other {@link XmlSerializable} objects for deserialization, they'll pass the reader pointing to the
 * token after the {@link XmlToken#START_ELEMENT}. This way objects reading XML will be self-encapsulated for reading
 * properly formatted XML. And, if an error occurs during deserialization an {@link IllegalStateException} should be
 * thrown.
 *
 * @param <T> The type of the object that is XML serializable.
 *
 * @see com.azure.xml
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
     * <!-- src_embed com.azure.xml.XmlSerializable.toXml#XmlWriter -->
     * <pre>
     * &#64;Override
     * public XmlWriter toXml&#40;XmlWriter xmlWriter&#41; throws XMLStreamException &#123;
     *     &#47;&#47; Pass null as the rootElementName to use the default root element name.
     *     &#47;&#47; Overall, toXml&#40;XmlWriter&#41; is just convenience for toXml&#40;XmlWriter, null&#41;.
     *     return toXml&#40;xmlWriter, null&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.xml.XmlSerializable.toXml#XmlWriter -->
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
     * <!-- src_embed com.azure.xml.XmlSerializable.toXml#XmlWriter-String -->
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
     * <!-- end com.azure.xml.XmlSerializable.toXml#XmlWriter-String -->
     *
     * @param xmlWriter The {@link XmlWriter} being written to.
     * @param rootElementName Optional root element name to override the default defined by the model. Used to support
     * cases where the model can serialize using different root element names.
     * @return The {@link XmlWriter} where the XML was written for chaining.
     * @throws XMLStreamException If the object fails to be written to the {@code xmlWriter}.
     */
    XmlWriter toXml(XmlWriter xmlWriter, String rootElementName) throws XMLStreamException;

    /**
     * Reads an XML stream into an object.
     * <p>
     * Implementations of {@link XmlSerializable} must define this method, otherwise an
     * {@link UnsupportedOperationException} will be thrown.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.xml.XmlSerializable.fromXml#XmlReader -->
     * <pre>
     * public static ResponseAuthor fromXml&#40;XmlReader xmlReader&#41; throws XMLStreamException &#123;
     *     &#47;&#47; Pass null as the rootElementName to use the default root element name.
     *     &#47;&#47; Overall, fromXml&#40;XmlReader&#41; is just convenience for fromXml&#40;XmlReader, null&#41;.
     *     return fromXml&#40;xmlReader, null&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.xml.XmlSerializable.fromXml#XmlReader -->
     *
     * @param xmlReader The {@link XmlReader} being read.
     * @param <T> The type of the object.
     * @return The object that the XML stream represented, may return null.
     * @throws XMLStreamException If an object fails to be read from the {@code xmlReader}.
     */
    static <T extends XmlSerializable<T>> T fromXml(XmlReader xmlReader) throws XMLStreamException {
        return fromXml(xmlReader, null);
    }

    /**
     * Reads an XML stream into an object.
     * <p>
     * Implementations of {@link XmlSerializable} must define this method, otherwise an
     * {@link UnsupportedOperationException} will be thrown.
     * <p>
     * This differs from {@link #fromXml(XmlReader)} in that it allows the root element name to be overridden. This is
     * useful when the model can deserialize from different root element names.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.xml.XmlSerializable.fromXml#XmlReader-String -->
     * <pre>
     * public static ResponseAuthor fromXml&#40;XmlReader xmlReader, String rootElementName&#41; throws XMLStreamException &#123;
     *     &#47;&#47; Use XmlReader.readObject as a convenience method for checking that the XmlReader has begun reading, the
     *     &#47;&#47; current XmlToken is START_ELEMENT, and the element name matches the expected element name &#40;this can just be
     *     &#47;&#47; matching on the element name or if there is a namespace the namespace qualified element name&#41;.
     *     &#47;&#47;
     *     &#47;&#47; The following is the equivalent of:
     *     &#47;&#47; - XmlReader.currentToken&#40;&#41; == XmlToken.START_ELEMENT
     *     &#47;&#47; - XmlReader.getElementName&#40;&#41;.getNamespaceURI&#40;&#41;.equals&#40;&quot;http:&#47;&#47;www.w3.org&#47;2005&#47;Atom&quot;&#41;
     *     &#47;&#47; - XmlReader.getElementName&#40;&#41;.getLocalPart&#40;&#41;.equals&#40;getRootElementName&#40;rootElementName, &quot;author&quot;&#41;&#41;
     *     &#47;&#47;
     *     &#47;&#47; If XmlReader.readObject&#40;String, ReadValueCallback&#41; was used instead, the namespace check would be omitted.
     *     &#47;&#47;
     *     &#47;&#47; The ReadValueCallback is where the actual deserialization of the object occurs. When the ReadValueCallback is
     *     &#47;&#47; called, the XmlReader is positioned at the start of the element that the object is being deserialized from
     *     &#47;&#47; &#40;in this case the &quot;author&quot; element&#41;.
     *     return xmlReader.readObject&#40;&quot;http:&#47;&#47;www.w3.org&#47;2005&#47;Atom&quot;, getRootElementName&#40;rootElementName, &quot;author&quot;&#41;,
     *         reader -&gt; &#123;
     *             ResponseAuthor author = new ResponseAuthor&#40;&#41;;
     *
     *             while &#40;xmlReader.nextElement&#40;&#41; != XmlToken.END_ELEMENT&#41; &#123;
     *                 QName qName = xmlReader.getElementName&#40;&#41;;
     *                 String localPart = qName.getLocalPart&#40;&#41;;
     *                 String namespaceUri = qName.getNamespaceURI&#40;&#41;;
     *
     *                 if &#40;&quot;name&quot;.equals&#40;localPart&#41; &amp;&amp; &quot;http:&#47;&#47;www.w3.org&#47;2005&#47;Atom&quot;.equals&#40;namespaceUri&#41;&#41; &#123;
     *                     author.name = xmlReader.getStringElement&#40;&#41;;
     *                 &#125;
     *             &#125;
     *
     *             return author;
     *         &#125;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.xml.XmlSerializable.fromXml#XmlReader-String -->
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
