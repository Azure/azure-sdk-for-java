// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import javax.xml.namespace.QName;
import java.time.OffsetDateTime;

public class AccessPolicy implements XmlSerializable<AccessPolicy> {
    private OffsetDateTime startsOn;
    private OffsetDateTime expiresOn;
    private String permissions;

    public OffsetDateTime getStartsOn() {
        return startsOn;
    }

    public AccessPolicy setStartsOn(OffsetDateTime startsOn) {
        this.startsOn = startsOn;
        return this;
    }

    public OffsetDateTime getExpiresOn() {
        return expiresOn;
    }

    public AccessPolicy setExpiresOn(OffsetDateTime expiresOn) {
        this.expiresOn = expiresOn;
        return this;
    }

    public String getPermissions() {
        return permissions;
    }

    public AccessPolicy setPermissions(String permissions) {
        this.permissions = permissions;
        return this;
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) {
        return xmlWriter.writeStartElement("AccessPolicy")
            .writeStringElement("Start", startsOn == null ? null : startsOn.toString())
            .writeStringElement("Expiry", expiresOn == null ? null : expiresOn.toString())
            .writeStringElement("Permission", permissions)
            .writeEndElement();
    }

    public static AccessPolicy fromXml(XmlReader xmlReader) {
        if (xmlReader.currentToken() != XmlToken.START_ELEMENT) {
            // Since AccessPolicy only cares about XML elements use nextElement()
            xmlReader.nextElement();
        }

        if (xmlReader.currentToken() != XmlToken.START_ELEMENT) {
            throw new IllegalStateException("Illegal start of XML deserialization. "
                + "Expected 'XmlToken.START_ELEMENT' but it was: 'XmlToken." + xmlReader.currentToken() + "'.");
        }

        QName elementQName = xmlReader.getElementName();
        String elementName = elementQName.toString();
        if (!"AccessPolicy".equals(elementName)) {
            throw new IllegalStateException("Expected XML element to be 'SignedIdentifiers' but it was: "
                + "'" + elementName + "'.");
        }

        OffsetDateTime startsOn = null;
        OffsetDateTime expiresOn = null;
        String permissions = null;

        while (xmlReader.nextElement() != XmlToken.END_ELEMENT) {
            elementQName = xmlReader.getElementName();
            elementName = elementQName.toString();

            if ("Start".equals(elementName)) {
                startsOn = OffsetDateTime.parse(xmlReader.getElementStringValue());
            } else if ("Expiry".equals(elementName)) {
                expiresOn = OffsetDateTime.parse(xmlReader.getElementStringValue());
            } else if ("Permission".equals(elementName)) {
                permissions = xmlReader.getElementStringValue();
            }
        }

        return new AccessPolicy().setStartsOn(startsOn).setExpiresOn(expiresOn).setPermissions(permissions);
    }
}
