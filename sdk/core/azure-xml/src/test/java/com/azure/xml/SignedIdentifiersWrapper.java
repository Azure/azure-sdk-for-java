// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import javax.xml.namespace.QName;
import java.util.ArrayList;
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
        if (xmlReader.currentToken() != XmlToken.START_ELEMENT) {
            // Since SignedIdentifiersWrapper only cares about XML elements use nextElement()
            xmlReader.nextElement();
        }

        if (xmlReader.currentToken() != XmlToken.START_ELEMENT) {
            throw new IllegalStateException("Illegal start of XML deserialization. "
                + "Expected 'XmlToken.START_ELEMENT' but it was: 'XmlToken." + xmlReader.currentToken() + "'.");
        }

        QName elementQName = xmlReader.getElementName();
        String elementName = elementQName.toString();
        if (!"SignedIdentifiers".equals(elementName)) {
            throw new IllegalStateException("Expected XML element to be 'SignedIdentifiers' but it was: "
                + "'" + elementName + "'.");
        }

        List<SignedIdentifier> signedIdentifiers = null;

        while (xmlReader.nextElement() != XmlToken.END_ELEMENT) {
            SignedIdentifier signedIdentifier = SignedIdentifier.fromXml(xmlReader);

            if (signedIdentifiers == null) {
                signedIdentifiers = new ArrayList<>();
            }

            signedIdentifiers.add(signedIdentifier);
        }

        return new SignedIdentifiersWrapper(signedIdentifiers);
    }
}
