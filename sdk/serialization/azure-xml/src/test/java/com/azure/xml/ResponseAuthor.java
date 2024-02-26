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

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException {
        return toXml(xmlWriter, null);
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter, String rootElementName) throws XMLStreamException {
        xmlWriter.writeStartElement(getRootElementName(rootElementName, "author"));
        xmlWriter.writeNamespace("http://www.w3.org/2005/Atom");
        xmlWriter.writeStringElement("name", name);

        return xmlWriter.writeEndElement().flush();
    }

    public static ResponseAuthor fromXml(XmlReader xmlReader) throws XMLStreamException {
        return fromXml(xmlReader, null);
    }

    public static ResponseAuthor fromXml(XmlReader xmlReader, String rootElementName) throws XMLStreamException {
        return xmlReader.readObject("http://www.w3.org/2005/Atom", getRootElementName(rootElementName, "author"),
            reader -> {
                String name = null;

                while (xmlReader.nextElement() != XmlToken.END_ELEMENT) {
                    QName qName = xmlReader.getElementName();
                    String localPart = qName.getLocalPart();
                    String namespaceUri = qName.getNamespaceURI();

                    if ("name".equals(localPart) && "http://www.w3.org/2005/Atom".equals(namespaceUri)) {
                        name = xmlReader.getStringElement();
                    }
                }

                return new ResponseAuthor().setName(name);
            });
    }
}
