// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.async;

import javax.xml.stream.XMLStreamException;

import com.azure.xml.implementation.aalto.AsyncInputFeeder;
import com.azure.xml.implementation.aalto.AsyncXMLStreamReader;
import com.azure.xml.implementation.aalto.stax.StreamReaderImpl;

/**
 * Implementation of {@link AsyncXMLStreamReader}.
 */
public class AsyncStreamReaderImpl<F extends AsyncInputFeeder> extends StreamReaderImpl
    implements AsyncXMLStreamReader<F> {
    protected final AsyncByteScanner _asyncScanner;

    public AsyncStreamReaderImpl(AsyncByteScanner scanner) {
        super(scanner);
        _asyncScanner = scanner;
        _currToken = EVENT_INCOMPLETE;
    }

    /*
    /**********************************************************************
    /* AsyncXMLStreamReader implementation
    /**********************************************************************
     */

    @SuppressWarnings("unchecked")
    @Override
    public F getInputFeeder() {
        return (F) _asyncScanner;
    }

    /*
    /**********************************************************************
    /* Overrides
    /**********************************************************************
     */

    @Override
    protected void _reportNonTextEvent(int type) throws XMLStreamException {
        // for Async parser
        if (type == EVENT_INCOMPLETE) {
            throwWfe("Can not use text-aggregating methods with non-blocking parser, as they (may) require blocking");
        }
        super._reportNonTextEvent(type);
    }
}
