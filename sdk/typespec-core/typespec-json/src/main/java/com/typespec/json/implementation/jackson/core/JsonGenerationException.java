// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */

package com.typespec.json.implementation.jackson.core;

import com.typespec.json.implementation.jackson.core.exc.StreamWriteException;

/**
 * Exception type for exceptions during JSON writing, such as trying
 * to output  content in wrong context (non-matching end-array or end-object,
 * for example).
 */
public class JsonGenerationException
    extends StreamWriteException
{
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
    public JsonGenerationException(Throwable rootCause, JsonGenerator g) {
        super(rootCause, g);
    }

    // @since 2.7
    public JsonGenerationException(String msg, JsonGenerator g) {
        super(msg, g);
        _processor = g;
    }
    
    // @since 2.7
    public JsonGenerationException(String msg, Throwable rootCause, JsonGenerator g) {
        super(msg, rootCause, g);
        _processor = g;
    }

    /**
     * Fluent method that may be used to assign originating {@link JsonGenerator},
     * to be accessed using {@link #getProcessor()}.
     *
     * @param g Generator to assign
     *
     * @return This exception instance (to allow call chaining)
     *
     * @since 2.7
     */
    @Override
    public JsonGenerationException withGenerator(JsonGenerator g) {
        _processor = g;
        return this;
    }

    // NOTE: overloaded in 2.13 just to retain binary compatibility with 2.12 (remove from 3.0)
    @Override
    public JsonGenerator getProcessor() { return _processor; }
}
