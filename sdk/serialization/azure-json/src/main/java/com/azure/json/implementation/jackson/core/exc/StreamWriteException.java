// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.exc;

import com.azure.json.implementation.jackson.core.*;

/**
 * Intermediate base class for all read-side streaming processing problems, including
 * parsing and input value coercion problems.
 *<p>
 * Added in 2.13 to eventually replace {@link JsonGenerationException}.
 *
 * @since 2.13
 */
public abstract class StreamWriteException extends JsonProcessingException {
    private final static long serialVersionUID = 2L;

    protected transient JsonGenerator _processor;

    protected StreamWriteException(Throwable rootCause, JsonGenerator g) {
        super(rootCause);
        _processor = g;
    }

    protected StreamWriteException(String msg, JsonGenerator g) {
        super(msg, (JsonLocation) null);
        _processor = g;
    }

    protected StreamWriteException(String msg, Throwable rootCause, JsonGenerator g) {
        super(msg, null, rootCause);
        _processor = g;
    }

    @Override
    public JsonGenerator getProcessor() {
        return _processor;
    }
}
