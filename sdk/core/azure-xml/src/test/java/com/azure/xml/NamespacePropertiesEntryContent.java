// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import javax.xml.namespace.QName;

public class NamespacePropertiesEntryContent implements XmlSerializable<NamespacePropertiesEntryContent> {
    private String type;
    private NamespaceProperties namespaceProperties;

    /**
     * Get the type property: Type of content in namespace info response.
     *
     * @return the type value.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Set the type property: Type of content in namespace info response.
     *
     * @param type the type value to set.
     * @return the NamespacePropertiesEntryContent object itself.
     */
    public NamespacePropertiesEntryContent setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Get the namespaceProperties property: The metadata related to a Service Bus namespace.
     *
     * @return the namespaceProperties value.
     */
    public NamespaceProperties getNamespaceProperties() {
        return this.namespaceProperties;
    }

    /**
     * Set the namespaceProperties property: The metadata related to a Service Bus namespace.
     *
     * @param namespaceProperties the namespaceProperties value to set.
     * @return the NamespacePropertiesEntryContent object itself.
     */
    public NamespacePropertiesEntryContent setNamespaceProperties(NamespaceProperties namespaceProperties) {
        this.namespaceProperties = namespaceProperties;
        return this;
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) {
        xmlWriter.writeStartElement("content");
        xmlWriter.writeNamespace("http://www.w3.org/2005/Atom");
        xmlWriter.writeStringAttribute("type", type);

        xmlWriter.writeXml(namespaceProperties);

        return xmlWriter.writeEndElement().flush();
    }

    public static NamespacePropertiesEntryContent fromXml(XmlReader xmlReader) {
        if (xmlReader.currentToken() != XmlToken.START_ELEMENT) {
            xmlReader.nextElement();
        }

        if (xmlReader.currentToken() != XmlToken.START_ELEMENT) {
            throw new IllegalStateException("Illegal start of XML deserialization. "
                + "Expected 'XmlToken.START_ELEMENT' but it was: 'XmlToken." + xmlReader.currentToken() + "'.");
        }

        QName qName = xmlReader.getElementName();
        if (!"content".equals(qName.getLocalPart())
            || !"http://www.w3.org/2005/Atom".equals(qName.getNamespaceURI())) {
            throw new IllegalStateException("Expected XML element to be 'content' in namespace "
                + "'http://www.w3.org/2005/Atom' but it was: "
                + "{'" + qName.getNamespaceURI() + "'}'" + qName.getLocalPart() + "'.");
        }

        String type = xmlReader.getAttributeStringValue(null, "type");
        NamespaceProperties namespaceProperties = null;

        while (xmlReader.nextElement() != XmlToken.END_ELEMENT) {
            qName = xmlReader.getElementName();
            String localPart = qName.getLocalPart();
            String namespaceUri = qName.getNamespaceURI();

            if ("NamespaceInfo".equals(localPart)
                && "http://schemas.microsoft.com/netservices/2010/10/servicebus/connect".equals(namespaceUri)) {
                namespaceProperties = NamespaceProperties.fromXml(xmlReader);
            }
        }

        return new NamespacePropertiesEntryContent().setType(type).setNamespaceProperties(namespaceProperties);
    }
}
