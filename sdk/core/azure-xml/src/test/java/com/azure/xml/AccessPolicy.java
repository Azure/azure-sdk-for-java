// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

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
}
