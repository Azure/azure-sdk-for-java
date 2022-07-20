// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import java.util.List;

public class SignedIdentifiersWrapper implements XmlSerializable<SignedIdentifiersWrapper> {
    private final List<SignedIdentifier> signedIdentifiers;

    public SignedIdentifiersWrapper(List<SignedIdentifier> signedIdentifiers) {
        this.signedIdentifiers = signedIdentifiers;
    }

    public List<SignedIdentifier> items() {
        return signedIdentifiers;
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) {
        xmlWriter.writeStartElement("SignedIdentifiers");

        if (signedIdentifiers != null) {
            signedIdentifiers.forEach(xmlWriter::writeXml);
        }

        return xmlWriter.writeEndElement();
    }

    public static SignedIdentifiersWrapper fromXml(XmlReader xmlReader) {
        if (xmlReader.currentToken() == XmlToken.START_ELEMENT) {

        }
        return null;
    }
}
