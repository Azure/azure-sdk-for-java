// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */

package com.azure.json.implementation.jackson.core;

import com.azure.json.implementation.jackson.core.exc.InputCoercionException;
import com.azure.json.implementation.jackson.core.util.RequestPayload;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Base class that defines public API for reading JSON content.
 * Instances are created using factory methods of
 * a {@link JsonFactory} instance.
 *
 * @author Tatu Saloranta
 */
public abstract class JsonParser implements Closeable {

    /**
     * Enumeration that defines all on/off features for parsers.
     */
    public enum Feature {

        // // // Support for non-standard data format constructs

        /**
         * Feature that allows parser to recognize set of
         * "Not-a-Number" (NaN) tokens as legal floating number
         * values (similar to how many other data formats and
         * programming language source code allows it).
         * Specific subset contains values that
         * <a href="http://www.w3.org/TR/xmlschema-2/">XML Schema</a>
         * (see section 3.2.4.1, Lexical Representation)
         * allows (tokens are quoted contents, not including quotes):
         *<ul>
         *  <li>"INF" (for positive infinity), as well as alias of "Infinity"
         *  <li>"-INF" (for negative infinity), alias "-Infinity"
         *  <li>"NaN" (for other not-a-numbers, like result of division by zero)
         *</ul>
         *<p>
         * Since JSON specification does not allow use of such values,
         * this is a non-standard feature, and as such disabled by default.
          *
          * @deprecated Since 2.10 use {@link com.azure.json.implementation.jackson.core.json.JsonReadFeature#ALLOW_NON_NUMERIC_NUMBERS} instead
          */
        @Deprecated
        ALLOW_NON_NUMERIC_NUMBERS(false);

        /**
         * Whether feature is enabled or disabled by default.
         */
        private final boolean _defaultState;

        private final int _mask;

        /**
         * Method that calculates bit set (flags) of all features that
         * are enabled by default.
         *
         * @return Bit mask of all features that are enabled by default
         */
        public static int collectDefaults() {
            int flags = 0;
            for (Feature f : values()) {
                if (f.enabledByDefault()) {
                    flags |= f.getMask();
                }
            }
            return flags;
        }

        Feature(boolean defaultState) {
            _mask = (1 << ordinal());
            _defaultState = defaultState;
        }

        public boolean enabledByDefault() {
            return _defaultState;
        }

        public boolean enabledIn(int flags) {
            return (flags & _mask) != 0;
        }

        public int getMask() {
            return _mask;
        }
    }

    /*
    /**********************************************************
    /* Minimal configuration state
    /**********************************************************
     */

    /**
     * Bit flag composed of bits that indicate which
     * {@link com.azure.json.implementation.jackson.core.JsonParser.Feature}s
     * are enabled.
     */
    protected int _features;

    /**
     * Optional container that holds the request payload which will be displayed on JSON parsing error.
     *
     * @since 2.8
     */
    protected transient RequestPayload _requestPayload;

    /*
    /**********************************************************
    /* Construction, configuration, initialization
    /**********************************************************
     */

    protected JsonParser() {
        // @since 2.14 do use sane defaults
        _features = JsonFactory.DEFAULT_PARSER_FEATURE_FLAGS;
    }

    protected JsonParser(int features) {
        _features = features;
    }

    /*
    /**********************************************************************
    /* Constraints violation checking (2.15)
    /**********************************************************************
     */

    /**
     * Get the constraints to apply when performing streaming reads.
     *
     * @return Read constraints used by this parser
     *
     * @since 2.15
     */
    public StreamReadConstraints streamReadConstraints() {
        return StreamReadConstraints.defaults();
    }

    /*
    /**********************************************************
    /* Closeable implementation
    /**********************************************************
     */

    /**
     * Closes the parser so that no further iteration or data access
     * can be made; will also close the underlying input source
     * if parser either <b>owns</b> the input source, or feature
     * {@code Feature#AUTO_CLOSE_SOURCE} is enabled.
     * Whether parser owns the input source depends on factory
     * method that was used to construct instance (so check
     * {@link com.azure.json.implementation.jackson.core.JsonFactory} for details,
     * but the general
     * idea is that if caller passes in closable resource (such
     * as {@link InputStream} or {@link Reader}) parser does NOT
     * own the source; but if it passes a reference (such as
     * {@link java.io.File} or {@link java.net.URL} and creates
     * stream or reader it does own them.
     *
     * @throws IOException if there is either an underlying I/O problem
     */
    @Override
    public abstract void close() throws IOException;

    /*
    /**********************************************************
    /* Public API, simple location, context accessors
    /**********************************************************
     */

    /**
     * Method that can be used to access current parsing context reader
     * is in. There are 3 different types: root, array and object contexts,
     * with slightly different available information. Contexts are
     * hierarchically nested, and can be used for example for figuring
     * out part of the input document that correspond to specific
     * array or object (for highlighting purposes, or error reporting).
     * Contexts can also be used for simple xpath-like matching of
     * input, if so desired.
     *
     * @return Stream input context ({@link JsonStreamContext}) associated with this parser
     */
    public abstract JsonStreamContext getParsingContext();

    /**
     * Method that returns location of the last processed input unit (character
     * or byte) from the input;
     * usually for error reporting purposes.
     *<p>
     * Note that the location is not guaranteed to be accurate (although most
     * implementation will try their best): some implementations may only
     * report specific boundary locations (start or end locations of tokens)
     * and others only return {@link JsonLocation#NA} due to not having access
     * to input location information (when delegating actual decoding work
     * to other library)
     *
     * @return Location of the last processed input unit (byte or character)
     */
    public abstract JsonLocation currentLocation();

    /*
    /***************************************************
    /* Public API, configuration
    /***************************************************
     */

    /**
     * Method for enabling specified parser feature
     * (check {@link Feature} for list of features)
     *
     * @param f Feature to enable
     *
     * @return This parser, to allow call chaining
     */
    public JsonParser enable(Feature f) {
        _features |= f.getMask();
        return this;
    }

    /**
     * Method for disabling specified  feature
     * (check {@link Feature} for list of features)
     *
     * @param f Feature to disable
     *
     * @return This parser, to allow call chaining
     */
    public JsonParser disable(Feature f) {
        _features &= ~f.getMask();
        return this;
    }

    /**
     * Method for enabling or disabling specified feature
     * (check {@link Feature} for list of features)
     *
     * @param f Feature to enable or disable
     * @param state Whether to enable feature ({@code true}) or disable ({@code false})
     *
     * @return This parser, to allow call chaining
     */
    public JsonParser configure(Feature f, boolean state) {
        if (state)
            enable(f);
        else
            disable(f);
        return this;
    }

    /**
     * Method for checking whether specified {@link Feature} is enabled.
     *
     * @param f Feature to check
     *
     * @return {@code True} if feature is enabled; {@code false} otherwise
     */
    public boolean isEnabled(Feature f) {
        return f.enabledIn(_features);
    }

    /*
    /**********************************************************
    /* Public API, traversal
    /**********************************************************
     */

    /**
     * Main iteration method, which will advance stream enough
     * to determine type of the next token, if any. If none
     * remaining (stream has no content other than possible
     * white space before ending), null will be returned.
     *
     * @return Next token from the stream, if any found, or null
     *   to indicate end-of-input
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract JsonToken nextToken() throws IOException;

    /*
    /**********************************************************
    /* Public API, simple token id/type access
    /**********************************************************
     */

    /**
     * Accessor to find which token parser currently points to, if any;
     * null will be returned if none.
     * If return value is non-null, data associated with the token
     * is available via other accessor methods.
     *
     * @return Type of the token this parser currently points to,
     *   if any: null before any tokens have been read, and
     *   after end-of-input has been encountered, as well as
     *   if the current token has been explicitly cleared.
     *
     * @since 2.8
     */
    public abstract JsonToken currentToken();

    /*
    /**********************************************************
    /* Public API, access to token information, text
    /**********************************************************
     */

    /**
     * Method that can be called to get the name associated with
     * the current token: for {@link JsonToken#FIELD_NAME}s it will
     * be the same as what {@link #getText} returns;
     * for field values it will be preceding field name;
     * and for others (array values, root-level values) null.
     *
     * @return Name of the current field in the parsing context
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     *
     * @since 2.10
     */
    public abstract String currentName() throws IOException;

    /**
     * Method for accessing textual representation of the current token;
     * if no current token (before first call to {@link #nextToken}, or
     * after encountering end-of-input), returns null.
     * Method can be called for any token type.
     *
     * @return Textual value associated with the current token (one returned
     *   by {@link #nextToken()} or other iteration methods)
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems, including if the text is too large
     */
    public abstract String getText() throws IOException;

    /*
    /**********************************************************
    /* Public API, access to token information, numeric
    /**********************************************************
     */

    /**
     * Numeric accessor that can be called when the current
     * token is of type {@link JsonToken#VALUE_NUMBER_INT} and
     * it can be expressed as a value of Java int primitive type.
     * It can also be called for {@link JsonToken#VALUE_NUMBER_FLOAT};
     * if so, it is equivalent to calling {@link #getDoubleValue}
     * and then casting; except for possible overflow/underflow
     * exception.
     *<p>
     * Note: if the resulting integer value falls outside range of
     * Java {@code int}, a {@link InputCoercionException}
     * may be thrown to indicate numeric overflow/underflow.
     *
     * @return Current number value as {@code int} (if numeric token within
     *   Java 32-bit signed {@code int} range); otherwise exception thrown
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract int getIntValue() throws IOException;

    /**
     * Numeric accessor that can be called when the current
     * token is of type {@link JsonToken#VALUE_NUMBER_INT} and
     * it can be expressed as a Java long primitive type.
     * It can also be called for {@link JsonToken#VALUE_NUMBER_FLOAT};
     * if so, it is equivalent to calling {@link #getDoubleValue}
     * and then casting to int; except for possible overflow/underflow
     * exception.
     *<p>
     * Note: if the token is an integer, but its value falls
     * outside of range of Java long, a {@link InputCoercionException}
     * may be thrown to indicate numeric overflow/underflow.
     *
     * @return Current number value as {@code long} (if numeric token within
     *   Java 32-bit signed {@code long} range); otherwise exception thrown
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract long getLongValue() throws IOException;

    /**
     * Numeric accessor that can be called when the current
     * token is of type {@link JsonToken#VALUE_NUMBER_FLOAT} and
     * it can be expressed as a Java float primitive type.
     * It can also be called for {@link JsonToken#VALUE_NUMBER_INT};
     * if so, it is equivalent to calling {@link #getLongValue}
     * and then casting; except for possible overflow/underflow
     * exception.
     *<p>
     * Note: if the value falls
     * outside of range of Java float, a {@link InputCoercionException}
     * will be thrown to indicate numeric overflow/underflow.
     *
     * @return Current number value as {@code float} (if numeric token within
     *   Java {@code float} range); otherwise exception thrown
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract float getFloatValue() throws IOException;

    /**
     * Numeric accessor that can be called when the current
     * token is of type {@link JsonToken#VALUE_NUMBER_FLOAT} and
     * it can be expressed as a Java double primitive type.
     * It can also be called for {@link JsonToken#VALUE_NUMBER_INT};
     * if so, it is equivalent to calling {@link #getLongValue}
     * and then casting; except for possible overflow/underflow
     * exception.
     *<p>
     * Note: if the value falls
     * outside of range of Java double, a {@link InputCoercionException}
     * will be thrown to indicate numeric overflow/underflow.
     *
     * @return Current number value as {@code double} (if numeric token within
     *   Java {@code double} range); otherwise exception thrown
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract double getDoubleValue() throws IOException;

    /*
    /**********************************************************
    /* Public API, access to token information, other
    /**********************************************************
     */

    /**
     * Convenience accessor that can be called when the current
     * token is {@link JsonToken#VALUE_TRUE} or
     * {@link JsonToken#VALUE_FALSE}, to return matching {@code boolean}
     * value.
     * If the current token is of some other type, {@link JsonParseException}
     * will be thrown
     *
     * @return {@code True} if current token is {@code JsonToken.VALUE_TRUE},
     *   {@code false} if current token is {@code JsonToken.VALUE_FALSE};
     *   otherwise throws {@link JsonParseException}
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public boolean getBooleanValue() throws IOException {
        JsonToken t = currentToken();
        if (t == JsonToken.VALUE_TRUE)
            return true;
        if (t == JsonToken.VALUE_FALSE)
            return false;
        throw new JsonParseException(this, String.format("Current token (%s) not of boolean type", t))
            .withRequestPayload(_requestPayload);
    }

    /*
    /**********************************************************
    /* Public API, access to token information, binary
    /**********************************************************
     */

    /**
     * Method that can be used to read (and consume -- results
     * may not be accessible using other methods after the call)
     * base64-encoded binary data
     * included in the current textual JSON value.
     * It works similar to getting String value via {@link #getText}
     * and decoding result (except for decoding part),
     * but should be significantly more performant.
     *<p>
     * Note that non-decoded textual contents of the current token
     * are not guaranteed to be accessible after this method
     * is called. Current implementation, for example, clears up
     * textual content during decoding.
     * Decoded binary content, however, will be retained until
     * parser is advanced to the next event.
     *
     * @param bv Expected variant of base64 encoded
     *   content (see {@link Base64Variants} for definitions
     *   of "standard" variants).
     *
     * @return Decoded binary data
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract byte[] getBinaryValue(Base64Variant bv) throws IOException;

    /**
     * Convenience alternative to {@link #getBinaryValue(Base64Variant)}
     * that defaults to using
     * {@link Base64Variants#getDefaultVariant} as the default encoding.
     *
     * @return Decoded binary data
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public byte[] getBinaryValue() throws IOException {
        return getBinaryValue(Base64Variants.getDefaultVariant());
    }

    /*
    /**********************************************************
    /* Public API, access to token information, coercion/conversion
    /**********************************************************
     */

    /**
     * Method that will try to convert value of current token to a
     * {@link java.lang.String}.
     * JSON Strings map naturally; scalar values get converted to
     * their textual representation.
     * If representation can not be converted to a String value (including structured types
     * like Objects and Arrays and {@code null} token), default value of
     * <b>null</b> will be returned; no exceptions are thrown.
     *
     * @return {@link String} value current token is converted to, if possible; {@code null} otherwise
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     *
     * @since 2.1
     */
    public String getValueAsString() throws IOException {
        return getValueAsString(null);
    }

    /**
     * Method that will try to convert value of current token to a
     * {@link java.lang.String}.
     * JSON Strings map naturally; scalar values get converted to
     * their textual representation.
     * If representation can not be converted to a String value (including structured types
     * like Objects and Arrays and {@code null} token), specified default value
     * will be returned; no exceptions are thrown.
     *
     * @param def Default value to return if conversion to {@code String} is not possible
     *
     * @return {@link String} value current token is converted to, if possible; {@code def} otherwise
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     *
     * @since 2.1
     */
    public abstract String getValueAsString(String def) throws IOException;

    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */

    /**
     * Helper method for constructing {@link JsonParseException}s
     * based on current state of the parser
     *
     * @param msg Base exception message to construct exception with
     *
     * @return {@link JsonParseException} constructed
     */
    protected JsonParseException _constructError(String msg) {
        return new JsonParseException(this, msg).withRequestPayload(_requestPayload);
    }

    /**
     * Helper method for constructing {@link JsonParseException}
     * based on current state of the parser.
     *
     * @param msg Base exception message to construct exception with
     *
     * @return Read exception (of type {@link JsonParseException}) constructed
     *
     * @since 2.13
     */
    protected JsonParseException _constructReadException(String msg) {
        // In 3.0 will be actual `StreamReadException`...
        return _constructError(msg);
    }

    protected JsonParseException _constructReadException(String msg, Object arg) {
        return _constructReadException(String.format(msg, arg));
    }

    protected JsonParseException _constructReadException(Object arg1, Object arg2) {
        return _constructReadException(String.format("Unrecognized token '%s': was expecting %s", arg1, arg2));
    }

    /**
     * Helper method for constructing {@link JsonParseException}
     * based on current state of the parser and indicating that the given
     * {@link Throwable} is the root cause.
     *
     * @param msg Base exception message to construct exception with
     * @param t Root cause to assign
     *
     * @return Read exception (of type {@link JsonParseException}) constructed
     *
     * @since 2.13
     */
    protected JsonParseException _constructReadException(String msg, Throwable t) {
        JsonParseException e = new JsonParseException(this, msg, t);
        if (_requestPayload != null) {
            e = e.withRequestPayload(_requestPayload);
        }
        return e;
    }

    /**
     * Helper method for constructing {@link JsonParseException}
     * based on current state of the parser, except for specified
     * {@link JsonLocation} for problem location (which may not be
     * the exact current location)
     *
     * @param msg Base exception message to construct exception with
     * @param loc Error location to report
     *
     * @return Read exception (of type {@link JsonParseException}) constructed
     *
     * @since 2.13
     */
    protected JsonParseException _constructReadException(String msg, JsonLocation loc) {
        JsonParseException e = new JsonParseException(this, msg, loc);
        if (_requestPayload != null) {
            e = e.withRequestPayload(_requestPayload);
        }
        return e;
    }
}
