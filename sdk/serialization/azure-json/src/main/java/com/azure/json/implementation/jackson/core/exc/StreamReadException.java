// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.exc;

import com.azure.json.implementation.jackson.core.*;

/**
 * Intermediate base class for all read-side streaming processing problems, including
 * parsing and input value coercion problems.
 *<p>
 * Added in 2.10 to eventually replace {@link JsonParseException}.
 *
 * @since 2.10
 */
public abstract class StreamReadException extends JsonProcessingException {
    final static long serialVersionUID = 2L;

    protected transient JsonParser _processor;

    protected StreamReadException(JsonParser p, String msg) {
        super(msg, (p == null) ? null : p.currentLocation());
        _processor = p;
    }

    protected StreamReadException(JsonParser p, String msg, Throwable root) {
        super(msg, (p == null) ? null : p.currentLocation(), root);
        _processor = p;
    }
}
