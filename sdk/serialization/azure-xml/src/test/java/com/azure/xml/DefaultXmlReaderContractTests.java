// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import com.azure.xml.contract.XmlReaderContractTests;

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
