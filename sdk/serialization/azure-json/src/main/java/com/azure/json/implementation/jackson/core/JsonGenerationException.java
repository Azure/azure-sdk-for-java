// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/*
 * Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */

package com.azure.json.implementation.jackson.core;

import com.azure.json.implementation.jackson.core.exc.StreamWriteException;

/**
 * Exception type for exceptions during JSON writing, such as trying
 * to output  content in wrong context (non-matching end-array or end-object,
 * for example).
 */
public class JsonGenerationException extends StreamWriteException {
    private final static long serialVersionUID = 123; // eclipse complains otherwise

    @Deprecated // since 2.7
    public JsonGenerationException(Throwable rootCause) {
        super(rootCause, null);
    }

    @Deprecated // since 2.7
    public JsonGenerationException(String msg) {
        super(msg, null);
    }

    @Deprecated // since 2.7
    public JsonGenerationException(String msg, Throwable rootCause) {
        super(msg, rootCause, null);
    }

    // @since 2.7
    public JsonGenerationException(String msg, JsonGenerator g) {
        super(msg, g);
        _processor = g;
    }

    // NOTE: overloaded in 2.13 just to retain binary compatibility with 2.12 (remove from 3.0)
    @Override
    public JsonGenerator getProcessor() {
        return _processor;
    }
}
