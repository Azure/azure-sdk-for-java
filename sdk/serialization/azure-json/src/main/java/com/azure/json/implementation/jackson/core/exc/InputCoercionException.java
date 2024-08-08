// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.exc;

import com.azure.json.implementation.jackson.core.*;
import com.azure.json.implementation.jackson.core.util.RequestPayload;

/**
 * Exception type for read-side problems that are not direct decoding ("parsing")
 * problems (those would be reported as {@link com.azure.json.implementation.jackson.core.JsonParseException}s),
 * but rather result from failed attempts to convert specific Java value out of valid
 * but incompatible input value. One example is numeric coercions where target number type's
 * range does not allow mapping of too large/too small input value.
 *
 * @since 2.10
 */
public class InputCoercionException extends StreamReadException {
    private static final long serialVersionUID = 1L;

    /**
     * Input token that represents input value that failed to coerce.
     */
    protected final JsonToken _inputType;

    /**
     * Target type that input value failed to coerce to.
     */
    protected final Class<?> _targetType;

    /**
     * Constructor that uses current parsing location as location, and
     * sets processor (accessible via {@code #getProcessor()}) to
     * specified parser.
     *
     * @param p Parser in use at the point where failure occurred
     * @param msg Exception mesage to use
     * @param inputType Shape of input that failed to coerce
     * @param targetType Target type of failed coercion
     */
    public InputCoercionException(JsonParser p, String msg, JsonToken inputType, Class<?> targetType) {
        super(p, msg);
        _inputType = inputType;
        _targetType = targetType;
    }

    @Override
    public InputCoercionException withRequestPayload(RequestPayload p) {
        _requestPayload = p;
        return this;
    }

}
