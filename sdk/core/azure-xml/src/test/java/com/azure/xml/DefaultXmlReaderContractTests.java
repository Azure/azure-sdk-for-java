// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import com.azure.xml.contract.XmlReaderContractTests;

/**
 * Tests {@link DefaultXmlReader} against the contract required by {@link XmlReader}.
 */
public final class DefaultXmlReaderContractTests extends XmlReaderContractTests {
    @Override
    protected XmlReader getXmlReader(String xml) {
        return DefaultXmlReader.fromString(xml);
    }
}
