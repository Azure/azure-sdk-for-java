// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import static com.azure.xml.AzureXmlTestUtils.getRootElementName;

public class ResponseAuthor implements XmlSerializable<ResponseAuthor> {
    private String name;

    /**
     * Get the name property: The Service Bus namespace.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name property: The Service Bus namespace.
     *
     * @param name the name value to set.
     * @return the ResponseAuthor object itself.
     */
    public ResponseAuthor setName(String name) {
        this.name = name;
        return this;
    }

    // BEGIN: com.azure.xml.XmlSerializable.toXml#XmlWriter
    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException {
        // Pass null as the rootElementName to use the default root element name.
        // Overall, toXml(XmlWriter) is just convenience for toXml(XmlWriter, null).
        return toXml(xmlWriter, null);
    }
    // END: com.azure.xml.XmlSerializable.toXml#XmlWriter

    // BEGIN: com.azure.xml.XmlSerializable.toXml#XmlWriter-String
    @Override
    public XmlWriter toXml(XmlWriter xmlWriter, String rootElementName) throws XMLStreamException {
        // The call to XmlSerializable.toXml handles writing the XML start document
        // (<?xml version="1.0" encoding="UTF-8">).
        // Write the start of the XML element.
        xmlWriter.writeStartElement(getRootElementName(rootElementName, "author"));

        // Namespace and attribute writing happens after wiring the start of the element. The element start isn't
        // finished until end element or starting another element is called.
        xmlWriter.writeNamespace("http://www.w3.org/2005/Atom");

        // Convenience method that writes an entire element with a single API call. This is used when the element
        // doesn't have any attributes, namespaces, or child elements.
        xmlWriter.writeStringElement("name", name);

        // Finish writing the XML element. No need to flush as the caller will handle that.
        return xmlWriter.writeEndElement();
    }
    // END: com.azure.xml.XmlSerializable.toXml#XmlWriter-String

    // BEGIN: com.azure.xml.XmlSerializable.fromXml#XmlReader
    public static ResponseAuthor fromXml(XmlReader xmlReader) throws XMLStreamException {
        // Pass null as the rootElementName to use the default root element name.
        // Overall, fromXml(XmlReader) is just convenience for fromXml(XmlReader, null).
        return fromXml(xmlReader, null);
    }
    // END: com.azure.xml.XmlSerializable.fromXml#XmlReader

    // BEGIN: com.azure.xml.XmlSerializable.fromXml#XmlReader-String
    public static ResponseAuthor fromXml(XmlReader xmlReader, String rootElementName) throws XMLStreamException {
        // Use XmlReader.readObject as a convenience method for checking that the XmlReader has begun reading, the
        // current XmlToken is START_ELEMENT, and the element name matches the expected element name (this can just be
        // matching on the element name or if there is a namespace the namespace qualified element name).
        //
        // The following is the equivalent of:
        // - XmlReader.currentToken() == XmlToken.START_ELEMENT
        // - XmlReader.getElementName().getNamespaceURI().equals("http://www.w3.org/2005/Atom")
        // - XmlReader.getElementName().getLocalPart().equals(getRootElementName(rootElementName, "author"))
        //
        // If XmlReader.readObject(String, ReadValueCallback) was used instead, the namespace check would be omitted.
        //
        // The ReadValueCallback is where the actual deserialization of the object occurs. When the ReadValueCallback is
        // called, the XmlReader is positioned at the start of the element that the object is being deserialized from
        // (in this case the "author" element).
        return xmlReader.readObject("http://www.w3.org/2005/Atom", getRootElementName(rootElementName, "author"),
            reader -> {
                ResponseAuthor author = new ResponseAuthor();

                while (xmlReader.nextElement() != XmlToken.END_ELEMENT) {
                    QName qName = xmlReader.getElementName();
                    String localPart = qName.getLocalPart();
                    String namespaceUri = qName.getNamespaceURI();

                    if ("name".equals(localPart) && "http://www.w3.org/2005/Atom".equals(namespaceUri)) {
                        author.name = xmlReader.getStringElement();
                    }
                }

                return author;
            });
    }
    // END: com.azure.xml.XmlSerializable.fromXml#XmlReader-String
}
