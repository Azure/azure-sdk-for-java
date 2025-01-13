// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.serialization.xml;

import javax.xml.stream.XMLStreamException;

/**
 * A callback used when processing an XML element.
 */
@FunctionalInterface
public interface XmlElementConsumer {
    /**
     * Consume an XML element.
     *
     * @param namespaceUri The namespace URI of the element being processed.
     * @param localName The local name of the element being processed.
     * @param reader The {@link XmlReader} processing the element.
     * @throws XMLStreamException If an XML stream error occurs during processing of the element.
     */
    void consume(String namespaceUri, String localName, XmlReader reader) throws XMLStreamException;
}
