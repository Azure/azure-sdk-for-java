// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.xml;

import javax.xml.stream.XMLStreamException;

/**
 * A callback used when processing an XML element.
 */
@FunctionalInterface
public interface XmlProcessElementCallback {
    /**
     * Processes an XML element.
     *
     * @param namespaceUri The namespace URI of the element being processed.
     * @param localName The local name of the element being processed.
     * @param reader The {@link XmlReader} processing the element.
     * @throws XMLStreamException If an XML stream error occurs during processing of the element.
     */
    void process(String namespaceUri, String localName, XmlReader reader) throws XMLStreamException;
}
