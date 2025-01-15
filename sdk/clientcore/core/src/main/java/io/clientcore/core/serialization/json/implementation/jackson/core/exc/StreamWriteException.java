// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package io.clientcore.core.serialization.json.implementation.jackson.core.exc;

import io.clientcore.core.serialization.json.implementation.jackson.core.JsonGenerator;
import io.clientcore.core.serialization.json.implementation.jackson.core.JsonProcessingException;

/**
 * Intermediate base class for all read-side streaming processing problems, including
 * parsing and input value coercion problems.
 *
 * @since 2.13
 */
public abstract class StreamWriteException extends JsonProcessingException {
    private final static long serialVersionUID = 2L;

    protected transient JsonGenerator _processor;

    protected StreamWriteException(String msg, JsonGenerator g) {
        super(msg, null);
        _processor = g;
    }

    @Override
    public JsonGenerator getProcessor() {
        return _processor;
    }
}
