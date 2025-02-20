// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.xml.implementation;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.Reader;

/**
 * Simple interface which acts as a facade around an {@link XMLInputFactory} or {@link XMLInputFactory}-like object.
 * <p>
 * This is a simplification of the {@link XMLInputFactory} interface based on the subset of APIs used by the Azure SDK.
 */
public interface XmlInputFactoryFacade {
    /**
     * Allows the user to set specific feature/property on the underlying
     * implementation. The underlying implementation is not required to support
     * every setting of every property in the specification and may use
     * IllegalArgumentException to signal that an unsupported property may not be
     * set with the specified value.
     * <p>
     * All implementations that implement JAXP 1.5 or newer are required to
     * support the {@link javax.xml.XMLConstants#ACCESS_EXTERNAL_DTD} property.
     * <ul>
     *   <li>
     *        Access to external DTDs, external Entity References is restricted to the
     *        protocols specified by the property. If access is denied during parsing
     *        due to the restriction of this property, {@link javax.xml.stream.XMLStreamException}
     *        will be thrown by the {@link javax.xml.stream.XMLStreamReader#next()} or
     *        {@link javax.xml.stream.XMLEventReader#nextEvent()} method.
     *   </li>
     * </ul>
     *
     * @param name The name of the property (may not be null)
     * @param value The value of the property
     * @throws java.lang.IllegalArgumentException if the property is not supported
     */
    void setProperty(String name, Object value);

    /**
     * Create a new XMLStreamReader from a java.io.InputStream.
     *
     * @param stream the InputStream to read from
     * @return an instance of the {@code XMLStreamReader}
     * @throws XMLStreamException if an error occurs
     */
    XMLStreamReader createXMLStreamReader(InputStream stream) throws XMLStreamException;

    /**
     * Create a new XMLEventReader from a reader.
     *
     * @param reader the XML data to read from
     * @return an instance of the {@code XMLEventReader}
     * @throws XMLStreamException if an error occurs
     */
    XMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException;
}
