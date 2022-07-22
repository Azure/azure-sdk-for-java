// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import javax.xml.namespace.QName;

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

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) {
        xmlWriter.writeStartElement("author");
        xmlWriter.writeNamespace("http://www.w3.org/2005/Atom");
        xmlWriter.writeStringElement("name", name);

        return xmlWriter.writeEndElement().flush();
    }

    public static ResponseAuthor fromXml(XmlReader xmlReader) {
        if (xmlReader.currentToken() != XmlToken.START_ELEMENT) {
            // Since ResponseAuthor only cares about XML elements use nextElement()
            xmlReader.nextElement();
        }

        if (xmlReader.currentToken() != XmlToken.START_ELEMENT) {
            throw new IllegalStateException("Illegal start of XML deserialization. "
                + "Expected 'XmlToken.START_ELEMENT' but it was: 'XmlToken." + xmlReader.currentToken() + "'.");
        }

        QName qName = xmlReader.getElementName();
        if (!"author".equals(qName.getLocalPart())
            || !"http://www.w3.org/2005/Atom".equals(qName.getNamespaceURI())) {
            throw new IllegalStateException("Expected XML element to be 'author' in namespace "
                + "'http://www.w3.org/2005/Atom' but it was: "
                + "{'" + qName.getNamespaceURI() + "'}'" + qName.getLocalPart() + "'.");
        }

        String name = null;

        while (xmlReader.nextElement() != XmlToken.END_ELEMENT) {
            qName = xmlReader.getElementName();
            String localPart = qName.getLocalPart();
            String namespaceUri = qName.getNamespaceURI();

            if ("name".equals(localPart) && "http://www.w3.org/2005/Atom".equals(namespaceUri)) {
                name = xmlReader.getElementStringValue();
            }
        }

        return new ResponseAuthor().setName(name);
    }
}
