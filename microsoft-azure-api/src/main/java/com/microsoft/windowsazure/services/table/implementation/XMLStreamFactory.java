package com.microsoft.windowsazure.services.table.implementation;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public interface XMLStreamFactory {
    XMLStreamWriter getWriter(OutputStream stream);

    XMLStreamReader getReader(InputStream stream);
}
