// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import javax.xml.stream.XMLStreamException;

import static com.azure.xml.AzureXmlTestUtils.getRootElementName;

public class SignedIdentifier implements XmlSerializable<SignedIdentifier> {
    private String id;
    private AccessPolicy accessPolicy;

    public String getId() {
        return id;
    }

    public SignedIdentifier setId(String id) {
        this.id = id;
        return this;
    }

    public AccessPolicy getAccessPolicy() {
        return accessPolicy;
    }

    public SignedIdentifier setAccessPolicy(AccessPolicy accessPolicy) {
        this.accessPolicy = accessPolicy;
        return this;
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException {
        return toXml(xmlWriter, null);
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter, String rootElementName) throws XMLStreamException {
        return xmlWriter.writeStartElement(getRootElementName(rootElementName, "SignedIdentifier"))
            .writeStringElement("Id", id)
            .writeXml(accessPolicy)
            .writeEndElement();
    }

    public static SignedIdentifier fromXml(XmlReader xmlReader) throws XMLStreamException {
        return fromXml(xmlReader, null);
    }

    public static SignedIdentifier fromXml(XmlReader xmlReader, String rootElementName) throws XMLStreamException {
        return xmlReader.readObject(getRootElementName(rootElementName, "SignedIdentifier"), reader -> {
            String id = null;
            boolean idFound = false;
            AccessPolicy accessPolicy = null;
            boolean accessPolicyFound = false;
            while (xmlReader.nextElement() != XmlToken.END_ELEMENT) {
                String elementName = xmlReader.getElementName().getLocalPart();

                if ("Id".equals(elementName)) {
                    id = xmlReader.getStringElement();
                    idFound = true;
                } else if ("AccessPolicy".equals(elementName)) {
                    accessPolicy = AccessPolicy.fromXml(xmlReader);
                    accessPolicyFound = true;
                }
            }

            if (idFound && accessPolicyFound) {
                return new SignedIdentifier().setId(id).setAccessPolicy(accessPolicy);
            }

            StringBuilder errorMessageBuilder = new StringBuilder("Missing required property/properties: ");
            boolean firstMissingProperty = true;
            if (!idFound) {
                errorMessageBuilder.append("'Id'");
                firstMissingProperty = false;
            }

            if (!accessPolicyFound) {
                if (!firstMissingProperty) {
                    errorMessageBuilder.append(" and ");
                }
                errorMessageBuilder.append("'AccessPolicy'");
            }

            throw new IllegalStateException(errorMessageBuilder.toString());
        });
    }
}
