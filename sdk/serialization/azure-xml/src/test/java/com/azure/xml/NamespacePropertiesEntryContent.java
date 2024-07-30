// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import static com.azure.xml.AzureXmlTestUtils.getRootElementName;

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
    public XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException {
        return toXml(xmlWriter, null);
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter, String rootElementName) throws XMLStreamException {
        xmlWriter.writeStartElement(getRootElementName(rootElementName, "content"));
        xmlWriter.writeNamespace("http://www.w3.org/2005/Atom");
        xmlWriter.writeStringAttribute("type", type);

        xmlWriter.writeXml(namespaceProperties);

        return xmlWriter.writeEndElement().flush();
    }

    public static NamespacePropertiesEntryContent fromXml(XmlReader xmlReader) throws XMLStreamException {
        return fromXml(xmlReader, null);
    }

    public static NamespacePropertiesEntryContent fromXml(XmlReader xmlReader, String rootElementName)
        throws XMLStreamException {
        return xmlReader.readObject("http://www.w3.org/2005/Atom", getRootElementName(rootElementName, "content"),
            reader -> {
                String type = xmlReader.getStringAttribute(null, "type");
                NamespaceProperties namespaceProperties = null;

                while (xmlReader.nextElement() != XmlToken.END_ELEMENT) {
                    // BEGIN: com.azure.xml.XmlReader.getElementName
                    QName qName = xmlReader.getElementName();
                    String localPart = qName.getLocalPart(); // The name of the XML element.
                    String namespaceUri = qName.getNamespaceURI(); // The namespace of the XML element.
                    // END: com.azure.xml.XmlReader.getElementName

                    if ("NamespaceInfo".equals(localPart)
                        && "http://schemas.microsoft.com/netservices/2010/10/servicebus/connect".equals(namespaceUri)) {
                        namespaceProperties = NamespaceProperties.fromXml(xmlReader);
                    }
                }

                return new NamespacePropertiesEntryContent().setType(type).setNamespaceProperties(namespaceProperties);
            });
    }
}
