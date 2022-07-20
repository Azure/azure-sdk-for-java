// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

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
}
