// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

import static com.azure.xml.AzureXmlTestUtils.getRootElementName;

public class SignedIdentifiersWrapper implements XmlSerializable<SignedIdentifiersWrapper> {
    private final List<SignedIdentifier> signedIdentifiers;

    public SignedIdentifiersWrapper(List<SignedIdentifier> signedIdentifiers) {
        this.signedIdentifiers = signedIdentifiers;
    }

    public List<SignedIdentifier> items() {
        return signedIdentifiers;
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException {
        return toXml(xmlWriter, null);
    }

    public XmlWriter toXml(XmlWriter xmlWriter, String rootElementName) throws XMLStreamException {
        xmlWriter.writeStartElement(getRootElementName(rootElementName, "SignedIdentifiers"));

        if (signedIdentifiers != null) {
            for (SignedIdentifier signedIdentifier : signedIdentifiers) {
                xmlWriter.writeXml(signedIdentifier);
            }
        }

        return xmlWriter.writeEndElement();
    }

    public static SignedIdentifiersWrapper fromXml(XmlReader xmlReader) throws XMLStreamException {
        return fromXml(xmlReader, null);
    }

    public static SignedIdentifiersWrapper fromXml(XmlReader xmlReader, String rootElementName)
        throws XMLStreamException {
        return xmlReader.readObject(getRootElementName(rootElementName, "SignedIdentifiers"), reader -> {
            List<SignedIdentifier> signedIdentifiers = null;

            while (xmlReader.nextElement() != XmlToken.END_ELEMENT) {
                SignedIdentifier signedIdentifier = SignedIdentifier.fromXml(xmlReader);

                if (signedIdentifiers == null) {
                    signedIdentifiers = new ArrayList<>();
                }

                signedIdentifiers.add(signedIdentifier);
            }

            return new SignedIdentifiersWrapper(signedIdentifiers);
        });
    }
}
