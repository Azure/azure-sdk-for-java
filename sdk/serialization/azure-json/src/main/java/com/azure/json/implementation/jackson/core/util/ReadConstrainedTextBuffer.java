// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.util;

import com.azure.json.implementation.jackson.core.StreamReadConstraints;
import com.azure.json.implementation.jackson.core.exc.StreamConstraintsException;

public final class ReadConstrainedTextBuffer extends TextBuffer {

    private final StreamReadConstraints _streamReadConstraints;

    public ReadConstrainedTextBuffer(StreamReadConstraints streamReadConstraints, BufferRecycler bufferRecycler) {
        super(bufferRecycler);
        _streamReadConstraints = streamReadConstraints;
    }

    /*
    /**********************************************************************
    /* Convenience methods for validation
    /**********************************************************************
     */

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateStringLength(int length) throws StreamConstraintsException {
        _streamReadConstraints.validateStringLength(length);
    }
}
