// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */

package com.azure.json.implementation.jackson.core;

import com.azure.json.implementation.jackson.core.exc.StreamReadException;
import com.azure.json.implementation.jackson.core.util.RequestPayload;

/**
 * Exception type for parsing problems, used when non-well-formed content
 * (content that does not conform to JSON syntax as per specification)
 * is encountered.
 */
public class JsonParseException extends StreamReadException {
    private static final long serialVersionUID = 2L; // 2.7

    /**
     * Constructor that uses current parsing location as location, and
     * sets processor (accessible via {@code #getProcessor()}) to
     * specified parser.
     *
     * @param p Parser in use when encountering issue reported
     * @param msg Base exception message to use
     *
     * @since 2.7
     */
    public JsonParseException(JsonParser p, String msg) {
        this(p, msg, _currentLocation(p), null);
    }

    // @since 2.7
    public JsonParseException(JsonParser p, String msg, Throwable rootCause) {
        this(p, msg, _currentLocation(p), rootCause);
    }

    // @since 2.7
    public JsonParseException(JsonParser p, String msg, JsonLocation loc) {
        this(p, msg, loc, null);
    }

    // Canonical constructor
    // @since 2.7
    public JsonParseException(JsonParser p, String msg, JsonLocation loc, Throwable rootCause) {
        super(p, msg, loc, rootCause);
    }

    /**
     * Fluent method that may be used to assign payload to this exception,
     * to let recipient access it for diagnostics purposes.
     *<p>
     * NOTE: `this` instance is modified and no new instance is constructed.
     *
     * @param payload Payload to assign to this exception
     *
     * @return This exception instance to allow call chaining
     *
     * @since 2.8
     */
    @Override
    public JsonParseException withRequestPayload(RequestPayload payload) {
        _requestPayload = payload;
        return this;
    }

    // NOTE: overloaded in 2.10 just to retain binary compatibility with 2.9 (remove from 3.0)
    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
