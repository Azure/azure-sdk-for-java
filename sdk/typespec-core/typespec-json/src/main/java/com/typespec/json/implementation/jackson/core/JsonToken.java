// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */

package com.typespec.json.implementation.jackson.core;

/**
 * Enumeration for basic token types used for returning results
 * of parsing JSON content.
 */
public enum JsonToken
{
    /* Some notes on implementation:
     *
     * - Entries are to be ordered such that start/end array/object
     *   markers come first, then field name marker (if any), and
     *   finally scalar value tokens. This is assumed by some
     *   typing checks.
     */

    /**
     * NOT_AVAILABLE can be returned if {@link JsonParser}
     * implementation can not currently return the requested
     * token (usually next one), or even if any will be
     * available, but that may be able to determine this in
     * future. This is the case with non-blocking parsers --
     * they can not block to wait for more data to parse and
     * must return something.
     */
    NOT_AVAILABLE(null, JsonTokenId.ID_NOT_AVAILABLE),

    /**
     * START_OBJECT is returned when encountering '{'
     * which signals starting of an Object value.
     */
    START_OBJECT("{", JsonTokenId.ID_START_OBJECT),
        
    /**
     * END_OBJECT is returned when encountering '}'
     * which signals ending of an Object value
     */
    END_OBJECT("}", JsonTokenId.ID_END_OBJECT),

    /**
     * START_ARRAY is returned when encountering '['
     * which signals starting of an Array value
     */
    START_ARRAY("[", JsonTokenId.ID_START_ARRAY),

    /**
     * END_ARRAY is returned when encountering ']'
     * which signals ending of an Array value
     */
    END_ARRAY("]", JsonTokenId.ID_END_ARRAY),
        
    /**
     * FIELD_NAME is returned when a String token is encountered
     * as a field name (same lexical value, different function)
     */
    FIELD_NAME(null, JsonTokenId.ID_FIELD_NAME),
    
    /**
     * Placeholder token returned when the input source has a concept
     * of embedded Object that are not accessible as usual structure
     * (of starting with {@link #START_OBJECT}, having values, ending with
     * {@link #END_OBJECT}), but as "raw" objects.
     *<p>
     * Note: this token is never returned by regular JSON readers, but
     * only by readers that expose other kinds of source (like
     * <code>JsonNode</code>-based JSON trees, Maps, Lists and such).
     */
    VALUE_EMBEDDED_OBJECT(null, JsonTokenId.ID_EMBEDDED_OBJECT),

    /**
     * VALUE_STRING is returned when a String token is encountered
     * in value context (array element, field value, or root-level
     * stand-alone value)
     */
    VALUE_STRING(null, JsonTokenId.ID_STRING),

    /**
     * VALUE_NUMBER_INT is returned when an integer numeric token is
     * encountered in value context: that is, a number that does
     * not have floating point or exponent marker in it (consists
     * only of an optional sign, followed by one or more digits;
     * or, for binary formats, is indicated as integral number
     * by internal representation).
     */
    VALUE_NUMBER_INT(null, JsonTokenId.ID_NUMBER_INT),

    /**
     * VALUE_NUMBER_FLOAT is returned when a numeric token other
     * than integer is encountered: that is, a number that does
     * have floating point or exponent marker in it, in addition
     * to one or more digits (or, for non-textual formats,
     * has internal floating-point representation).
     */
    VALUE_NUMBER_FLOAT(null, JsonTokenId.ID_NUMBER_FLOAT),
    
    /**
     * VALUE_TRUE is returned when encountering literal "true" in
     * value context
     */
    VALUE_TRUE("true", JsonTokenId.ID_TRUE),

    /**
     * VALUE_FALSE is returned when encountering literal "false" in
     * value context
     */
    VALUE_FALSE("false", JsonTokenId.ID_FALSE),

    /**
     * VALUE_NULL is returned when encountering literal "null" in
     * value context
     */
    VALUE_NULL("null", JsonTokenId.ID_NULL),
        ;
    
    final String _serialized;

    final char[] _serializedChars;

    final byte[] _serializedBytes;

    final int _id;
    
    final boolean _isStructStart, _isStructEnd;

    final boolean _isNumber;

    final boolean _isBoolean;

    final boolean _isScalar;

    /**
     * @param token representation for this token, if there is a
     *   single static representation; null otherwise
     * @param id Numeric id from {@link JsonTokenId}
     */
    JsonToken(String token, int id)
    {
        if (token == null) {
            _serialized = null;
            _serializedChars = null;
            _serializedBytes = null;
        } else {
            _serialized = token;
            _serializedChars = token.toCharArray();
            // It's all in ascii, can just case...
            int len = _serializedChars.length;
            _serializedBytes = new byte[len];
            for (int i = 0; i < len; ++i) {
                _serializedBytes[i] = (byte) _serializedChars[i];
            }
        }
        _id = id;
        
        _isBoolean = (id == JsonTokenId.ID_FALSE || id == JsonTokenId.ID_TRUE);
        _isNumber = (id == JsonTokenId.ID_NUMBER_INT || id == JsonTokenId.ID_NUMBER_FLOAT);

        _isStructStart = (id == JsonTokenId.ID_START_OBJECT || id == JsonTokenId.ID_START_ARRAY);
        _isStructEnd = (id == JsonTokenId.ID_END_OBJECT || id == JsonTokenId.ID_END_ARRAY);

        _isScalar = !_isStructStart && !_isStructEnd
                && (id != JsonTokenId.ID_FIELD_NAME)
                && (id != JsonTokenId.ID_NOT_AVAILABLE);
    }

    public final int id() { return _id; }
    
    public final String asString() { return _serialized; }
    public final char[] asCharArray() { return _serializedChars; }
    public final byte[] asByteArray() { return _serializedBytes; }

    /**
     * @return {@code True} if this token is {@code VALUE_NUMBER_INT} or {@code VALUE_NUMBER_FLOAT},
     *   {@code false} otherwise
     */
    public final boolean isNumeric() { return _isNumber; }

    /**
     * Accessor that is functionally equivalent to:
     * <code>
     *    this == JsonToken.START_OBJECT || this == JsonToken.START_ARRAY
     * </code>
     *
     * @return {@code True} if this token is {@code START_OBJECT} or {@code START_ARRAY},
     *   {@code false} otherwise
     *
     * @since 2.3
     */
    public final boolean isStructStart() { return _isStructStart; }

    /**
     * Accessor that is functionally equivalent to:
     * <code>
     *    this == JsonToken.END_OBJECT || this == JsonToken.END_ARRAY
     * </code>
     *
     * @return {@code True} if this token is {@code END_OBJECT} or {@code END_ARRAY},
     *   {@code false} otherwise
     * 
     * @since 2.3
     */
    public final boolean isStructEnd() { return _isStructEnd; }

    /**
     * Method that can be used to check whether this token represents
     * a valid non-structured value. This means all {@code VALUE_xxx} tokens;
     * excluding {@code START_xxx} and {@code END_xxx} tokens as well
     * {@code FIELD_NAME}.
     *
     * @return {@code True} if this token is a scalar value token (one of
     *   {@code VALUE_xxx} tokens), {@code false} otherwise
     */
    public final boolean isScalarValue() { return _isScalar; }

    /**
     * @return {@code True} if this token is {@code VALUE_TRUE} or {@code VALUE_FALSE},
     *   {@code false} otherwise
     */
    public final boolean isBoolean() { return _isBoolean; }
}
