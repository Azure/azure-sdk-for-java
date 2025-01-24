// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.serialization.xml;

import io.clientcore.core.serialization.xml.contract.XmlReaderContractTests;

import javax.xml.stream.XMLStreamException;

/**
 * Tests {@link XmlReader} against the contract required by {@link XmlReader}.
 */
public final class DefaultXmlReaderContractTests extends XmlReaderContractTests {
    @Override
    protected XmlReader getXmlReader(String xml) throws XMLStreamException {
        return XmlReader.fromString(xml);
    }
}
