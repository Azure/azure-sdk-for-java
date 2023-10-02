// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */

package com.typespec.json.implementation.jackson.core;

import com.typespec.json.implementation.jackson.core.exc.StreamReadException;
import com.typespec.json.implementation.jackson.core.util.RequestPayload;

/**
 * Exception type for parsing problems, used when non-well-formed content
 * (content that does not conform to JSON syntax as per specification)
 * is encountered.
 */
public class JsonParseException
    extends StreamReadException
{
    private static final long serialVersionUID = 2L; // 2.7

    @Deprecated // since 2.7
    public JsonParseException(String msg, JsonLocation loc) {
        super(msg, loc, null);
    }

    @Deprecated // since 2.7
    public JsonParseException(String msg, JsonLocation loc, Throwable root) {
        super(msg, loc, root);
    }

    /**
     * Constructor that uses current parsing location as location, and
     * sets processor (accessible via {@link #getProcessor()}) to
     * specified parser.
     *
     * @param p Parser in use when encountering issue reported
     * @param msg Base exception message to use
     *
     * @since 2.7
     */
    public JsonParseException(JsonParser p, String msg) {
        super(p, msg);
    }

    // @since 2.7
    public JsonParseException(JsonParser p, String msg, Throwable root) {
        super(p, msg, root);
    }

    // @since 2.7
    public JsonParseException(JsonParser p, String msg, JsonLocation loc) {
        super(p, msg, loc);
    }

    // @since 2.7
    public JsonParseException(JsonParser p, String msg, JsonLocation loc, Throwable root) {
        super(msg, loc, root);
    }

    /**
     * Fluent method that may be used to assign originating {@link JsonParser},
     * to be accessed using {@link #getProcessor()}.
     *<p>
     * NOTE: `this` instance is modified and no new instance is constructed.
     *
     * @param p Parser instance to assign to this exception
     *
     * @return This exception instance to allow call chaining
     *
     * @since 2.7
     */
    @Override
    public JsonParseException withParser(JsonParser p) {
        _processor = p;
        return this;
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
    public JsonParser getProcessor() {
        return super.getProcessor();
    }

    // NOTE: overloaded in 2.10 just to retain binary compatibility with 2.9 (remove from 3.0)
    @Override
    public RequestPayload getRequestPayload() {
        return super.getRequestPayload();
    }

    // NOTE: overloaded in 2.10 just to retain binary compatibility with 2.9 (remove from 3.0)
    @Override
    public String getRequestPayloadAsString() {
        return super.getRequestPayloadAsString();
    }
    
    // NOTE: overloaded in 2.10 just to retain binary compatibility with 2.9 (remove from 3.0)
    @Override 
    public String getMessage() {
        return super.getMessage();
    }
}
