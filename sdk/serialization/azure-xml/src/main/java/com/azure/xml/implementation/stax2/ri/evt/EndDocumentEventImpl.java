// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.ri.evt;

import java.io.Writer;

import javax.xml.stream.*;
import javax.xml.stream.events.EndDocument;

import com.azure.xml.implementation.stax2.XMLStreamWriter2;

public class EndDocumentEventImpl extends BaseEventImpl implements EndDocument {
    public EndDocumentEventImpl(Location loc) {
        super(loc);
    }

    /*
    ///////////////////////////////////////////
    // Implementation of abstract base methods
    ///////////////////////////////////////////
     */

    @Override
    public int getEventType() {
        return END_DOCUMENT;
    }

    @Override
    public boolean isEndDocument() {
        return true;
    }

    @Override
    public void writeAsEncodedUnicode(Writer w) throws XMLStreamException {
        // Nothing to output
    }

    @Override
    public void writeUsing(XMLStreamWriter2 w) throws XMLStreamException {
        w.writeEndDocument();
    }

    /*
    ///////////////////////////////////////////
    // Standard method impl
    ///////////////////////////////////////////
     */

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o == null)
            return false;
        return (o instanceof EndDocument);
    }

    @Override
    public int hashCode() {
        return END_DOCUMENT;
    }
}
