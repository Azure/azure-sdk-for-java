// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.exc;

import com.typespec.json.implementation.jackson.core.*;
import com.typespec.json.implementation.jackson.core.util.RequestPayload;

/**
 * Exception type for read-side problems that are not direct decoding ("parsing")
 * problems (those would be reported as {@link com.typespec.json.implementation.jackson.core.JsonParseException}s),
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
     * sets processor (accessible via {@link #getProcessor()}) to
     * specified parser.
     *
     * @param p Parser in use at the point where failure occurred
     * @param msg Exception mesage to use
     * @param inputType Shape of input that failed to coerce
     * @param targetType Target type of failed coercion
     */
    public InputCoercionException(JsonParser p, String msg,
            JsonToken inputType, Class<?> targetType) {
        super(p, msg);
        _inputType = inputType;
        _targetType = targetType;
    }

    /**
     * Fluent method that may be used to assign originating {@link JsonParser},
     * to be accessed using {@link #getProcessor()}.
     *<p>
     * NOTE: `this` instance is modified and no new instance is constructed.
     */
    @Override
    public InputCoercionException withParser(JsonParser p) {
        _processor = p;
        return this;
    }

    @Override
    public InputCoercionException withRequestPayload(RequestPayload p) {
        _requestPayload = p;
        return this;
    }

    /**
     * Accessor for getting information about input type (in form of token, giving "shape"
     * of input) for which coercion failed.
     *
     * @return "Shape" of input for which coercion failed, as {@link JsonToken}
     */
    public JsonToken getInputType() {
        return _inputType;
    }

    /**
     * Accessor for getting information about target type (in form of Java {@link java.lang.Class})
     * for which coercion failed.
     *
     * @return Target type of failed conversion
     */
    public Class<?> getTargetType() {
        return _targetType;
    }
}
