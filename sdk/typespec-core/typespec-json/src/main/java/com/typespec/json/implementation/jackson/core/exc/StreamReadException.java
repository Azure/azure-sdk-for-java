// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.exc;

import com.typespec.json.implementation.jackson.core.*;
import com.typespec.json.implementation.jackson.core.util.RequestPayload;

/**
 * Intermediate base class for all read-side streaming processing problems, including
 * parsing and input value coercion problems.
 *<p>
 * Added in 2.10 to eventually replace {@link com.typespec.json.implementation.jackson.core.JsonParseException}.
 *
 * @since 2.10
 */
public abstract class StreamReadException
    extends JsonProcessingException
{
    final static long serialVersionUID = 2L;

    protected transient JsonParser _processor;

    /**
     * Optional payload that can be assigned to pass along for error reporting
     * or handling purposes. Core streaming parser implementations DO NOT
     * initialize this; it is up to using applications and frameworks to
     * populate it.
     */
    protected RequestPayload _requestPayload;

    protected StreamReadException(JsonParser p, String msg) {
        super(msg, (p == null) ? null : p.getCurrentLocation());
        _processor = p;
    }

    protected StreamReadException(JsonParser p, String msg, Throwable root) {
        super(msg, (p == null) ? null : p.getCurrentLocation(), root);
        _processor = p;
    }

    protected StreamReadException(JsonParser p, String msg, JsonLocation loc) {
        super(msg, loc, null);
        _processor = p;
    }

    // @since 2.13
    protected StreamReadException(JsonParser p, String msg, JsonLocation loc,
            Throwable rootCause) {
        super(msg, loc, rootCause);
        _processor = p;
    }

    protected StreamReadException(String msg, JsonLocation loc, Throwable rootCause) {
        super(msg, loc, rootCause);
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
     */
    public abstract StreamReadException withParser(JsonParser p);

    /**
     * Fluent method that may be used to assign payload to this exception,
     * to let recipient access it for diagnostics purposes.
     *<p>
     * NOTE: `this` instance is modified and no new instance is constructed.
     *
     * @param payload Payload to assign to this exception
     *
     * @return This exception instance to allow call chaining
     */
    public abstract StreamReadException withRequestPayload(RequestPayload payload);
    
    @Override
    public JsonParser getProcessor() {
        return _processor;
    }

    /**
     * Method that may be called to find payload that was being parsed, if
     * one was specified for parser that threw this Exception.
     *
     * @return request body, if payload was specified; `null` otherwise
     */
    public RequestPayload getRequestPayload() {
        return _requestPayload;
    }

    /**
     * The method returns the String representation of the request payload if
     * one was specified for parser that threw this Exception.
     * 
     * @return request body as String, if payload was specified; `null` otherwise
     */
    public String getRequestPayloadAsString() {
        return (_requestPayload != null) ? _requestPayload.toString() : null;
    }

    /**
     * Overriding the getMessage() to include the request body
     */
    @Override 
    public String getMessage() {
        String msg = super.getMessage();
        if (_requestPayload != null) {
            msg +=  "\nRequest payload : " + _requestPayload.toString();
        }
        return msg;
    }
}
