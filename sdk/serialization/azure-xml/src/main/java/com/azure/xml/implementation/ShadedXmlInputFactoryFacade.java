// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.xml.implementation;

import com.azure.xml.implementation.aalto.stax.InputFactoryImpl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.Reader;

/**
 * Implementation of {@link XmlInputFactoryFacade} that wraps an {@link InputFactoryImpl} instance.
 */
public final class ShadedXmlInputFactoryFacade implements XmlInputFactoryFacade {
    private final InputFactoryImpl inputFactory;

    /**
     * Creates a new instance of {@link ShadedXmlInputFactoryFacade}.
     *
     * @param inputFactory The {@link InputFactoryImpl} to wrap.
     */
    public ShadedXmlInputFactoryFacade(InputFactoryImpl inputFactory) {
        this.inputFactory = inputFactory;
    }

    @Override
    public void setProperty(String name, Object value) {
        inputFactory.setProperty(name, value);
    }

    @Override
    public XMLStreamReader createXMLStreamReader(InputStream stream) throws XMLStreamException {
        return inputFactory.createXMLStreamReader(stream);
    }

    @Override
    public XMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException {
        return inputFactory.createXMLStreamReader(reader);
    }
}
