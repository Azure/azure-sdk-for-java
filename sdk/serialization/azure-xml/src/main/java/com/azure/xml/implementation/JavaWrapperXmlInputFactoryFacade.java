// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.xml.implementation;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.Reader;

/**
 * Implementation of {@link XmlInputFactoryFacade} that wraps an underlying Java XML input factory.
 */
public final class JavaWrapperXmlInputFactoryFacade implements XmlInputFactoryFacade {
    private final XMLInputFactory xmlInputFactory;

    /**
     * Creates an instance of {@link JavaWrapperXmlInputFactoryFacade} that wraps the provided Java XML input factory.
     *
     * @param xmlInputFactory The Java XML input factory to wrap.
     */
    public JavaWrapperXmlInputFactoryFacade(XMLInputFactory xmlInputFactory) {
        this.xmlInputFactory = xmlInputFactory;
    }

    @Override
    public void setProperty(String name, Object value) {
        xmlInputFactory.setProperty(name, value);
    }

    @Override
    public XMLStreamReader createXMLStreamReader(InputStream stream) throws XMLStreamException {
        return xmlInputFactory.createXMLStreamReader(stream);
    }

    @Override
    public XMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException {
        return xmlInputFactory.createXMLStreamReader(reader);
    }
}
