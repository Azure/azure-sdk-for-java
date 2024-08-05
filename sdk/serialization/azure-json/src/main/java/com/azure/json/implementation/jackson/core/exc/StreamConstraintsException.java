// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.exc;

import com.azure.json.implementation.jackson.core.JsonLocation;
import com.azure.json.implementation.jackson.core.JsonProcessingException;

/**
 * Exception type used to indicate violations of stream constraints
 * (for example {@link com.azure.json.implementation.jackson.core.StreamReadConstraints})
 * when reading or writing content.
 *
 * @since 2.15
 */
public class StreamConstraintsException extends JsonProcessingException {
    private final static long serialVersionUID = 2L;

    public StreamConstraintsException(String msg) {
        super(msg);
    }

    public StreamConstraintsException(String msg, JsonLocation loc) {
        super(msg, loc);
    }
}
