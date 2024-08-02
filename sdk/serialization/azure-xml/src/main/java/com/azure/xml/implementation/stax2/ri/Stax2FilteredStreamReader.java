// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.ri;

import javax.xml.stream.*;

import com.azure.xml.implementation.stax2.*;
import com.azure.xml.implementation.stax2.util.StreamReader2Delegate;

/**
 * Simple straight-forward implementation of a filtering stream reader,
 * which can fully adapt Stax2 stream reader
 * ({@link XMLStreamReader2}).
 */
public class Stax2FilteredStreamReader extends StreamReader2Delegate implements XMLStreamConstants {
    final StreamFilter mFilter;

    public Stax2FilteredStreamReader(XMLStreamReader r, StreamFilter f) {
        super(Stax2ReaderAdapter.wrapIfNecessary(r));
        mFilter = f;
    }

    /*
    //////////////////////////////////////////////////////
    // XMLStreamReader method overrides that we need
    //////////////////////////////////////////////////////
     */

    @Override
    public int next() throws XMLStreamException {
        int type;
        do {
            type = _delegate2.next();
            if (mFilter.accept(this)) {
                break;
            }
        } while (type != END_DOCUMENT);

        return type;
    }

    @Override
    public int nextTag() throws XMLStreamException {
        int type;
        // Can be implemented very much like next()
        do {
            type = _delegate2.nextTag();
        } while (!mFilter.accept(this));
        return type;
    }
}
