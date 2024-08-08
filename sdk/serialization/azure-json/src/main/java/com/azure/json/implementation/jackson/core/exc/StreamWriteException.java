// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.exc;

import com.azure.json.implementation.jackson.core.*;

/**
 * Intermediate base class for all read-side streaming processing problems, including
 * parsing and input value coercion problems.
 *<p>
 * Added in 2.13 to eventually replace {@link com.azure.json.implementation.jackson.core.JsonGenerationException}.
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
}
