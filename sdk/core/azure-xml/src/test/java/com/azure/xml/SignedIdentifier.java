// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import javax.xml.namespace.QName;

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
    public XmlWriter toXml(XmlWriter xmlWriter) {
        return xmlWriter.writeStartElement("SignedIdentifier")
            .writeStringElement("Id", id)
            .writeXml(accessPolicy)
            .writeEndElement();
    }

    public static SignedIdentifier fromXml(XmlReader xmlReader) {
        if (xmlReader.currentToken() != XmlToken.START_ELEMENT) {
            // Since SignedIdentifier only cares about XML elements use nextElement()
            xmlReader.nextElement();
        }

        if (xmlReader.currentToken() != XmlToken.START_ELEMENT) {
            throw new IllegalStateException("Illegal start of XML deserialization. "
                + "Expected 'XmlToken.START_ELEMENT' but it was: 'XmlToken." + xmlReader.currentToken() + "'.");
        }

        QName elementQName = xmlReader.getElementName();
        String elementName = elementQName.toString();
        if (!"SignedIdentifier".equals(elementName)) {
            throw new IllegalStateException("Expected XML element to be 'SignedIdentifier' but it was: "
                + "'" + elementName + "'.");
        }

        String id = null;
        boolean idFound = false;
        AccessPolicy accessPolicy = null;
        boolean accessPolicyFound = false;
        while (xmlReader.nextElement() != XmlToken.END_ELEMENT) {
            elementQName = xmlReader.getElementName();
            elementName = elementQName.toString();

            if ("Id".equals(elementName)) {
                id = xmlReader.getElementStringValue();
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
    }
}
