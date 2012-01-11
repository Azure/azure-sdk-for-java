package com.microsoft.windowsazure.services.table.implementation;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public class DefaultXMLStreamFactory implements XMLStreamFactory {
    private final XMLOutputFactory xmlOutputFactory;
    private final XMLInputFactory xmlInputFactory;

    public DefaultXMLStreamFactory() {
        this.xmlOutputFactory = XMLOutputFactory.newInstance();
        this.xmlInputFactory = XMLInputFactory.newInstance();
    }

    @Override
    public XMLStreamWriter getWriter(OutputStream stream) {
        try {
            return xmlOutputFactory.createXMLStreamWriter(stream, "UTF-8");
        }
        catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XMLStreamReader getReader(InputStream stream) {
        try {
            return xmlInputFactory.createXMLStreamReader(stream);
        }
        catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }
}
