// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import javax.xml.stream.XMLStreamException;
import java.time.OffsetDateTime;

import static com.azure.xml.AzureXmlTestUtils.getRootElementName;

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
    public XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException {
        return toXml(xmlWriter, null);
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter, String rootElementName) throws XMLStreamException {
        return xmlWriter.writeStartElement(getRootElementName(rootElementName, "AccessPolicy"))
            .writeStringElement("Start", startsOn == null ? null : startsOn.toString())
            .writeStringElement("Expiry", expiresOn == null ? null : expiresOn.toString())
            .writeStringElement("Permission", permissions)
            .writeEndElement();
    }

    public static AccessPolicy fromXml(XmlReader xmlReader) throws XMLStreamException {
        return fromXml(xmlReader, null);
    }

    public static AccessPolicy fromXml(XmlReader xmlReader, String rootElementName) throws XMLStreamException {
        return xmlReader.readObject(getRootElementName(rootElementName, "AccessPolicy"), reader -> {
            OffsetDateTime startsOn = null;
            OffsetDateTime expiresOn = null;
            String permissions = null;

            while (xmlReader.nextElement() != XmlToken.END_ELEMENT) {
                String elementName = xmlReader.getElementName().getLocalPart();

                if ("Start".equals(elementName)) {
                    startsOn = OffsetDateTime.parse(xmlReader.getStringElement());
                } else if ("Expiry".equals(elementName)) {
                    expiresOn = OffsetDateTime.parse(xmlReader.getStringElement());
                } else if ("Permission".equals(elementName)) {
                    permissions = xmlReader.getStringElement();
                }
            }

            return new AccessPolicy().setStartsOn(startsOn).setExpiresOn(expiresOn).setPermissions(permissions);
        });
    }
}
