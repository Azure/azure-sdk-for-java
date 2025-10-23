// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/*
 * Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */

package com.azure.json.implementation.jackson.core;

import com.azure.json.implementation.jackson.core.exc.InputCoercionException;
import com.azure.json.implementation.jackson.core.io.ContentReference;
import com.azure.json.implementation.jackson.core.io.IOContext;
import com.azure.json.implementation.jackson.core.io.JsonEOFException;
import com.azure.json.implementation.jackson.core.io.NumberInput;
import com.azure.json.implementation.jackson.core.json.JsonReadContext;
import com.azure.json.implementation.jackson.core.util.ByteArrayBuilder;
import com.azure.json.implementation.jackson.core.util.TextBuffer;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * Base class that defines public API for reading JSON content.
 * Instances are created using factory methods of
 * a {@link JsonFactory} instance.
 *
 * @author Tatu Saloranta
 */
@SuppressWarnings("cast")
public abstract class JsonParser implements Closeable {
    // Control chars:
    protected final static int INT_TAB = '\t';
    protected final static int INT_LF = '\n';
    protected final static int INT_CR = '\r';
    protected final static int INT_SPACE = 0x0020;

    protected final static int INT_RBRACKET = ']';
    protected final static int INT_RCURLY = '}';
    protected final static int INT_QUOTE = '"';
    protected final static int INT_BACKSLASH = '\\';
    protected final static int INT_SLASH = '/';
    protected final static int INT_ASTERISK = '*';
    protected final static int INT_COLON = ':';
    protected final static int INT_COMMA = ',';
    protected final static int INT_HASH = '#';

    // Number chars
    protected final static int INT_0 = '0';
    protected final static int INT_9 = '9';
    protected final static int INT_MINUS = '-';
    protected final static int INT_PLUS = '+';

    protected final static int INT_PERIOD = '.';
    protected final static int INT_e = 'e';
    protected final static int INT_E = 'E';

    protected final static char CHAR_NULL = '\0';

    /**
     * @since 2.9
     */
    protected final static byte[] NO_BYTES = new byte[0];

    /*
     * /**********************************************************
     * /* Constants and fields of former 'JsonNumericParserBase'
     * /**********************************************************
     */

    protected final static int NR_UNKNOWN = 0;

    // First, integer types

    protected final static int NR_INT = 0x0001;
    protected final static int NR_LONG = 0x0002;
    protected final static int NR_BIGINT = 0x0004;

    // And then floating point types

    protected final static int NR_DOUBLE = 0x008;
    protected final static int NR_BIGDECIMAL = 0x0010;

    /**
     * NOTE! Not used by JSON implementation but used by many of binary codecs
     *
     * @since 2.9
     */
    protected final static int NR_FLOAT = 0x020;

    // Also, we need some numeric constants

    protected final static BigInteger BI_MIN_INT = BigInteger.valueOf(Integer.MIN_VALUE);
    protected final static BigInteger BI_MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);

    protected final static BigInteger BI_MIN_LONG = BigInteger.valueOf(Long.MIN_VALUE);
    protected final static BigInteger BI_MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);

    protected final static BigDecimal BD_MIN_LONG = new BigDecimal(BI_MIN_LONG);
    protected final static BigDecimal BD_MAX_LONG = new BigDecimal(BI_MAX_LONG);

    protected final static BigDecimal BD_MIN_INT = new BigDecimal(BI_MIN_INT);
    protected final static BigDecimal BD_MAX_INT = new BigDecimal(BI_MAX_INT);

    protected final static long MIN_INT_L = Integer.MIN_VALUE;
    protected final static long MAX_INT_L = Integer.MAX_VALUE;

    // These are not very accurate, but have to do... (for bounds checks)

    protected final static double MIN_LONG_D = (double) Long.MIN_VALUE;
    protected final static double MAX_LONG_D = (double) Long.MAX_VALUE;

    protected final static double MIN_INT_D = Integer.MIN_VALUE;
    protected final static double MAX_INT_D = Integer.MAX_VALUE;

    /*
     * /**********************************************************
     * /* Misc other constants
     * /**********************************************************
     */

    /**
     * Maximum number of characters to include in token reported
     * as part of error messages.
     *
     * @since 2.9
     */
    protected final static int MAX_ERROR_TOKEN_LENGTH = 256;

    /**
     * Enumeration that defines all on/off features for parsers.
     */
    public enum Feature {
        // // // Support for non-standard data format constructs

        /**
         * Feature that determines whether parser will allow use
         * of Java/C++ style comments (both '/'+'*' and
         * '//' varieties) within parsed content or not.
         *<p>
         * Since JSON specification does not mention comments as legal
         * construct,
         * this is a non-standard feature; however, in the wild
         * this is extensively used. As such, feature is
         * <b>disabled by default</b> for parsers and must be
         * explicitly enabled.
         *<p>
         * NOTE: while not technically deprecated, since 2.10 recommended to use
         * {@link com.azure.json.implementation.jackson.core.json.JsonReadFeature#ALLOW_JAVA_COMMENTS} instead.
         */
        ALLOW_COMMENTS(false),

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
         *  <li>"Infinity" (for positive infinity)
         *  <li>"-Infinity" (for negative infinity)
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
     * /**********************************************************
     * /* Minimal generally useful state
     * /**********************************************************
     */

    /**
     * Last token retrieved via {@link #nextToken}, if any.
     * Null before the first call to <code>nextToken()</code>,
     * as well as if token has been explicitly cleared
     */
    protected JsonToken _currToken;

    /*
     * /**********************************************************
     * /* Minimal configuration state
     * /**********************************************************
     */

    /**
     * Bit flag composed of bits that indicate which
     * {@link Feature}s
     * are enabled.
     */
    protected int _features;

    /*
     * /**********************************************************
     * /* Generic I/O state
     * /**********************************************************
     */

    /**
     * I/O context for this reader. It handles buffer allocation
     * for the reader.
     */
    final protected IOContext _ioContext;

    /**
     * Flag that indicates whether parser is closed or not. Gets
     * set when parser is either closed by explicit call
     * ({@link #close}) or when end-of-input is reached.
     */
    protected boolean _closed;

    /*
     * /**********************************************************
     * /* Current input data
     * /**********************************************************
     */

    // Note: type of actual buffer depends on sub-class, can't include

    /**
     * Pointer to next available character in buffer
     */
    protected int _inputPtr;

    /**
     * Index of character after last available one in the buffer.
     */
    protected int _inputEnd;

    /*
     * /**********************************************************
     * /* Current input location information
     * /**********************************************************
     */

    /**
     * Number of characters/bytes that were contained in previous blocks
     * (blocks that were already processed prior to the current buffer).
     */
    protected long _currInputProcessed;

    /**
     * Current row location of current point in input buffer, starting
     * from 1, if available.
     */
    protected int _currInputRow = 1;

    /**
     * Current index of the first character of the current row in input
     * buffer. Needed to calculate column position, if necessary; benefit
     * of not having column itself is that this only has to be updated
     * once per line.
     */
    protected int _currInputRowStart;

    /*
     * /**********************************************************
     * /* Information about starting location of event
     * /* Reader is pointing to; updated on-demand
     * /**********************************************************
     */

    // // // Location info at point when current token was started

    /**
     * Total number of bytes/characters read before start of current token.
     * For big (gigabyte-sized) sizes are possible, needs to be long,
     * unlike pointers and sizes related to in-memory buffers.
     */
    protected long _tokenInputTotal;

    /**
     * Input row on which current token starts, 1-based
     */
    protected int _tokenInputRow = 1;

    /**
     * Column on input row that current token starts; 0-based (although
     * in the end it'll be converted to 1-based)
     */
    protected int _tokenInputCol;

    /*
     * /**********************************************************
     * /* Parsing state
     * /**********************************************************
     */

    /**
     * Information about parser context, context in which
     * the next token is to be parsed (root, array, object).
     */
    protected JsonReadContext _parsingContext;

    /**
     * Secondary token related to the next token after current one;
     * used if its type is known. This may be value token that
     * follows FIELD_NAME, for example.
     */
    protected JsonToken _nextToken;

    /*
     * /**********************************************************
     * /* Buffer(s) for local name(s) and text content
     * /**********************************************************
     */

    /**
     * Buffer that contains contents of String values, including
     * field names if necessary (name split across boundary,
     * contains escape sequence, or access needed to char array)
     */
    protected final TextBuffer _textBuffer;

    /**
     * Flag set to indicate whether the field name is available
     * from the name copy buffer or not (in addition to its String
     * representation  being available via read context)
     */
    protected boolean _nameCopied;

    /**
     * ByteArrayBuilder is needed if 'getBinaryValue' is called. If so,
     * we better reuse it for remainder of content.
     */
    protected ByteArrayBuilder _byteArrayBuilder;

    /**
     * We will hold on to decoded binary data, for duration of
     * current event, so that multiple calls to
     * {@link #getBinaryValue} will not need to decode data more
     * than once.
     */
    protected byte[] _binaryValue;

    // Numeric value holders: multiple fields used for
    // for efficiency

    /**
     * Bitfield that indicates which numeric representations
     * have been calculated for the current type
     */
    protected int _numTypesValid = NR_UNKNOWN;

    // First primitives

    protected int _numberInt;

    protected long _numberLong;

    protected double _numberDouble;

    // And then object types

    protected BigInteger _numberBigInt;

    protected BigDecimal _numberBigDecimal;

    // And then other information about value itself

    /**
     * Flag that indicates whether numeric value has a negative
     * value. That is, whether its textual representation starts
     * with minus character.
     */
    protected boolean _numberNegative;

    /**
     * Length of integer part of the number, in characters
     */
    protected int _intLength;

    /**
     * Length of the fractional part (not including decimal
     * point or exponent), in characters.
     * Not used for  pure integer values.
     */
    protected int _fractLength;

    /**
     * Length of the exponent part of the number, if any, not
     * including 'e' marker or sign, just digits.
     * Not used for  pure integer values.
     */
    protected int _expLength;

    /*
     * /**********************************************************
     * /* Construction, configuration, initialization
     * /**********************************************************
     */

    protected JsonParser(IOContext ctxt, int features) {
        _ioContext = ctxt;
        _textBuffer = ctxt.constructTextBuffer();
        _features = features;
        _parsingContext = JsonReadContext.createRootContext();
    }

    /*
     * /**********************************************************
     * /* Closeable implementation
     * /**********************************************************
     */

    /**
     * Closes the parser so that no further iteration or data access
     * can be made; will also close the underlying input source
     * if parser either <b>owns</b> the input source.
     * Whether parser owns the input source depends on factory
     * method that was used to construct instance (so check
     * {@link JsonFactory} for details,
     * but the general
     * idea is that if caller passes in closable resource (such
     * as {@link InputStream} or {@link Reader}) parser does NOT
     * own the source; but if it passes a reference (such as
     * {@link File} or {@link java.net.URL} and creates
     * stream or reader it does own them.
     *
     * @throws IOException if there is either an underlying I/O problem
     */
    @Override
    public final void close() throws IOException {
        if (!_closed) {
            // 19-Jan-2018, tatu: as per [core#440] need to ensure no more data assumed available
            _inputPtr = Math.max(_inputPtr, _inputEnd);
            _closed = true;
            try {
                _closeInput();
            } finally {
                // as per [JACKSON-324], do in finally block
                // Also, internal buffer(s) can now be released as well
                _releaseBuffers();
            }
        }
    }

    /*
     * /**********************************************************
     * /* Public API, simple location, context accessors
     * /**********************************************************
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
    public final JsonReadContext getParsingContext() {
        return _parsingContext;
    }

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
     *
     * @since 2.13
     */
    public JsonLocation currentLocation() {
        int col = _inputPtr - _currInputRowStart + 1; // 1-based
        return new JsonLocation(_contentReference(), -1L, _currInputProcessed + _inputPtr, // bytes, chars
            _currInputRow, col);
    }

    /*
     * /***************************************************
     * /* Public API, configuration
     * /***************************************************
     */

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
            _features |= f.getMask();
        else
            _features &= ~f.getMask();
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
     * /**********************************************************
     * /* Public API, traversal
     * /**********************************************************
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

    /**
     * Method that will skip all child tokens of an array or
     * object token that the parser currently points to,
     * iff stream points to
     * {@link JsonToken#START_OBJECT} or {@link JsonToken#START_ARRAY}.
     * If not, it will do nothing.
     * After skipping, stream will point to <b>matching</b>
     * {@link JsonToken#END_OBJECT} or {@link JsonToken#END_ARRAY}
     * (possibly skipping nested pairs of START/END OBJECT/ARRAY tokens
     * as well as value tokens).
     * The idea is that after calling this method, application
     * will call {@link #nextToken} to point to the next
     * available token, if any.
     *
     * @return This parser, to allow call chaining
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public final JsonParser skipChildren() throws IOException {
        if (_currToken != JsonToken.START_OBJECT && _currToken != JsonToken.START_ARRAY) {
            return this;
        }
        int open = 1;

        // Since proper matching of start/end markers is handled
        // by nextToken(), we'll just count nesting levels here
        while (true) {
            JsonToken t = nextToken();
            if (t == null) {
                _handleEOF();
                /*
                 * given constraints, above should never return;
                 * however, FindBugs doesn't know about it and
                 * complains... so let's add dummy break here
                 */
                return this;
            }
            if (t.isStructStart()) {
                ++open;
            } else if (t.isStructEnd()) {
                if (--open == 0) {
                    return this;
                }
                // 23-May-2018, tatu: [core#463] Need to consider non-blocking case...
            } else if (t == JsonToken.NOT_AVAILABLE) {
                // Nothing much we can do except to either return `null` (which seems wrong),
                // or, what we actually do, signal error
                _reportError("Not enough content available for `skipChildren()`: non-blocking parser? (%s)",
                    getClass().getName());
            }
        }
    }

    /**
     * Method sub-classes need to implement for verifying that end-of-content
     * is acceptable at current input position.
     *
     * @throws JsonParseException If end-of-content is not acceptable (for example,
     *   missing end-object or end-array tokens)
     */
    protected final void _handleEOF() throws JsonParseException {
        if (!_parsingContext.inRoot()) {
            String marker = _parsingContext.inArray() ? "Array" : "Object";
            _reportInvalidEOF(String.format(": expected close marker for %s (start marker at %s)", marker,
                _parsingContext.startLocation(_contentReference())), null);
        }
    }

    /**
     * @return If no exception is thrown, {@code -1} which is used as marked for "end-of-input"
     *
     * @throws JsonParseException If check on {@code _handleEOF()} fails; usually because
     *    the current context is not root context (missing end markers in content)
     *
     * @since 2.4
     */
    protected final int _eofAsNextChar() throws JsonParseException {
        _handleEOF();
        return -1;
    }

    /*
     * /**********************************************************
     * /* Public API, simple token id/type access
     * /**********************************************************
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
    public final JsonToken currentToken() {
        return _currToken;
    }

    /*
     * /**********************************************************
     * /* Public API, access to token information, text
     * /**********************************************************
     */

    /**
     * Method that can be called to get the name associated with
     * the current token: for {@link JsonToken#FIELD_NAME}s it will
     * be the same as what {@link #getText} returns;
     * for field values it will be preceding field name;
     * and for others (array values, root-level values) null.
     *
     * @return Name of the current field in the parsing context
     * @since 2.10
     */
    public final String currentName() {
        // [JACKSON-395]: start markers require information from parent
        if (_currToken == JsonToken.START_OBJECT || _currToken == JsonToken.START_ARRAY) {
            JsonReadContext parent = _parsingContext.getParent();
            if (parent != null) {
                return parent.getCurrentName();
            }
        }
        return _parsingContext.getCurrentName();
    }

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
     *   {@link JsonParseException} for decoding problems
     */
    public abstract String getText() throws IOException;

    /*
     * /**********************************************************
     * /* Public API, access to token information, numeric
     * /**********************************************************
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
    public final int getIntValue() throws IOException {
        if ((_numTypesValid & NR_INT) == 0) {
            if (_numTypesValid == NR_UNKNOWN) { // not parsed at all
                return _parseIntValue();
            }
            if ((_numTypesValid & NR_INT) == 0) { // wasn't an int natively?
                convertNumberToInt(); // let's make it so, if possible
            }
        }
        return _numberInt;
    }

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
    public final long getLongValue() throws IOException {
        if ((_numTypesValid & NR_LONG) == 0) {
            if (_numTypesValid == NR_UNKNOWN) {
                _parseNumericValue(NR_LONG);
            }
            if ((_numTypesValid & NR_LONG) == 0) {
                convertNumberToLong();
            }
        }
        return _numberLong;
    }

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
    public final float getFloatValue() throws IOException {
        double value = getDoubleValue();
        /*
         * 22-Jan-2009, tatu: Bounds/range checks would be tricky
         * here, so let's not bother even trying...
         */
        /*
         * if (value < -Float.MAX_VALUE || value > MAX_FLOAT_D) {
         * _reportError("Numeric value ("+getText()+") out of range of Java float");
         * }
         */
        return (float) value;
    }

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
    public final double getDoubleValue() throws IOException {
        if ((_numTypesValid & NR_DOUBLE) == 0) {
            if (_numTypesValid == NR_UNKNOWN) {
                _parseNumericValue(NR_DOUBLE);
            }
            if ((_numTypesValid & NR_DOUBLE) == 0) {
                convertNumberToDouble();
            }
        }
        return _numberDouble;
    }

    /*
     * /**********************************************************
     * /* Public API, access to token information, other
     * /**********************************************************
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
    public final boolean getBooleanValue() throws IOException {
        JsonToken t = currentToken();
        if (t == JsonToken.VALUE_TRUE)
            return true;
        if (t == JsonToken.VALUE_FALSE)
            return false;
        throw new JsonParseException(this, String.format("Current token (%s) not of boolean type", t));
    }

    /*
     * /**********************************************************
     * /* Public API, access to token information, binary
     * /**********************************************************
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
     * @return Decoded binary data
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract byte[] getBinaryValue() throws IOException;

    /*
     * /**********************************************************
     * /* Public API, access to token information, coercion/conversion
     * /**********************************************************
     */

    /**
     * Method that will try to convert value of current token to a
     * {@link String}.
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
     * {@link String}.
     * JSON Strings map naturally; scalar values get converted to
     * their textual representation.
     * If representation can not be converted to a String value (including structured types
     * like Objects and Arrays and {@code null} token), specified default value
     * will be returned; no exceptions are thrown.
     *
     * @param defaultValue Default value to return if conversion to {@code String} is not possible
     *
     * @return {@link String} value current token is converted to, if possible; {@code def} otherwise
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     *
     * @since 2.1
     */
    public String getValueAsString(String defaultValue) throws IOException {
        if (_currToken == JsonToken.VALUE_STRING) {
            return getText();
        }
        if (_currToken == JsonToken.FIELD_NAME) {
            return currentName();
        }
        if (_currToken == null || _currToken == JsonToken.VALUE_NULL || !_currToken.isScalarValue()) {
            return defaultValue;
        }
        return getText();
    }

    /*
     * /**********************************************************
     * /* Internal methods
     * /**********************************************************
     */

    /**
     * Helper method for constructing {@link JsonParseException}s
     * based on current state of the parser
     *
     * @param msg Base exception message to construct exception with
     *
     * @return {@link JsonParseException} constructed
     */
    protected final JsonParseException _constructError(String msg) {
        return new JsonParseException(this, msg);
    }

    /*
     * /**********************************************************
     * /* Base64 decoding
     * /**********************************************************
     */

    /**
     * Helper method that can be used for base64 decoding in cases where
     * encoded content has already been read as a String.
     *
     * @param str String to decode
     * @param builder Builder used to buffer binary content decoded
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    protected final void _decodeBase64(String str, ByteArrayBuilder builder) throws IOException {
        try {
            Base64Variants.getDefaultVariant().decode(str, builder);
        } catch (IllegalArgumentException e) {
            _reportError(e.getMessage());
        }
    }

    /*
     * /**********************************************************
     * /* Abstract methods for sub-classes to implement
     * /**********************************************************
     */

    protected abstract void _closeInput() throws IOException;

    /*
     * /**********************************************************
     * /* Low-level reading, other
     * /**********************************************************
     */

    /**
     * Method called to release internal buffers owned by the base
     * reader. This may be called along with {@link #_closeInput} (for
     * example, when explicitly closing this reader instance), or
     * separately (if need be).
     *
     * @throws IOException Not thrown by base implementation but could be thrown
     *   by sub-classes
     */
    protected void _releaseBuffers() throws IOException {
        _textBuffer.releaseBuffers();
    }

    /*
     * /**********************************************************
     * /* Internal/package methods: shared/reusable builders
     * /**********************************************************
     */

    public final ByteArrayBuilder _getByteArrayBuilder() {
        if (_byteArrayBuilder == null) {
            _byteArrayBuilder = new ByteArrayBuilder();
        } else {
            _byteArrayBuilder.reset();
        }
        return _byteArrayBuilder;
    }

    /*
     * /**********************************************************
     * /* Methods from former JsonNumericParserBase
     * /**********************************************************
     */

    // // // Life-cycle of number-parsing

    protected final JsonToken reset(boolean negative, int intLen, int fractLen, int expLen) {
        if (fractLen < 1 && expLen < 1) { // integer
            return resetInt(negative, intLen);
        }
        return resetFloat(negative, intLen, fractLen, expLen);
    }

    protected final JsonToken resetInt(boolean negative, int intLen) {
        _numberNegative = negative;
        _intLength = intLen;
        _fractLength = 0;
        _expLength = 0;
        _numTypesValid = NR_UNKNOWN; // to force parsing
        return JsonToken.VALUE_NUMBER_INT;
    }

    protected final JsonToken resetFloat(boolean negative, int intLen, int fractLen, int expLen) {
        _numberNegative = negative;
        _intLength = intLen;
        _fractLength = fractLen;
        _expLength = expLen;
        _numTypesValid = NR_UNKNOWN; // to force parsing
        return JsonToken.VALUE_NUMBER_FLOAT;
    }

    protected final JsonToken resetAsNaN(String valueStr, double value) {
        _textBuffer.resetWithString(valueStr);
        _numberDouble = value;
        _numTypesValid = NR_DOUBLE;
        return JsonToken.VALUE_NUMBER_FLOAT;
    }

    /*
     * /**********************************************************
     * /* Conversion from textual to numeric representation
     * /**********************************************************
     */

    /**
     * Method that will parse actual numeric value out of a syntactically
     * valid number value. Type it will parse into depends on whether
     * it is a floating point number, as well as its magnitude: smallest
     * legal type (of ones available) is used for efficiency.
     *
     * @param expType Numeric type that we will immediately need, if any;
     *   mostly necessary to optimize handling of floating point numbers
     *
     * @throws IOException If there are problems reading content
     * @throws JsonParseException If there are problems decoding number value
     */
    private void _parseNumericValue(int expType) throws IOException {
        // 12-Jun-2020, tatu: Sanity check to prevent more cryptic error for this case.
        // (note: could alternatively see if TextBuffer has aggregated contents, avoid
        // exception -- but that might be more confusing)
        if (_closed) {
            _reportError("Internal error: _parseNumericValue called when parser instance closed");
        }

        // Int or float?
        if (_currToken == JsonToken.VALUE_NUMBER_INT) {
            final int len = _intLength;
            // First: optimization for simple int
            if (len <= 9) {
                _numberInt = _textBuffer.contentsAsInt(_numberNegative);
                _numTypesValid = NR_INT;
                return;
            }
            if (len <= 18) { // definitely fits AND is easy to parse using 2 int parse calls
                long l = _textBuffer.contentsAsLong(_numberNegative);
                // Might still fit in int, need to check
                if (len == 10) {
                    if (_numberNegative) {
                        if (l >= MIN_INT_L) {
                            _numberInt = (int) l;
                            _numTypesValid = NR_INT;
                            return;
                        }
                    } else {
                        if (l <= MAX_INT_L) {
                            _numberInt = (int) l;
                            _numTypesValid = NR_INT;
                            return;
                        }
                    }
                }
                _numberLong = l;
                _numTypesValid = NR_LONG;
                return;
            }
            _parseSlowInt(expType);
            return;
        }
        if (_currToken == JsonToken.VALUE_NUMBER_FLOAT) {
            _parseSlowFloat(expType);
            return;
        }
        _reportError("Current token (%s) not numeric, can not use numeric value accessors", _currToken);
    }

    // @since 2.6
    private int _parseIntValue() throws IOException {
        // 12-Jun-2020, tatu: Sanity check to prevent more cryptic error for this case.
        // (note: could alternatively see if TextBuffer has aggregated contents, avoid
        // exception -- but that might be more confusing)
        if (_closed) {
            _reportError("Internal error: _parseNumericValue called when parser instance closed");
        }
        // Inlined variant of: _parseNumericValue(NR_INT)
        if (_currToken == JsonToken.VALUE_NUMBER_INT) {
            if (_intLength <= 9) {
                int i = _textBuffer.contentsAsInt(_numberNegative);
                _numberInt = i;
                _numTypesValid = NR_INT;
                return i;
            }
        }
        // if not optimizable, use more generic
        _parseNumericValue(NR_INT);
        if ((_numTypesValid & NR_INT) == 0) {
            convertNumberToInt();
        }
        return _numberInt;
    }

    private void _parseSlowFloat(int expType) throws IOException {
        /*
         * Nope: floating point. Here we need to be careful to get
         * optimal parsing strategy: choice is between accurate but
         * slow (BigDecimal) and lossy but fast (Double). For now
         * let's only use BD when explicitly requested -- it can
         * still be constructed correctly at any point since we do
         * retain textual representation
         */
        try {
            if (expType == NR_BIGDECIMAL) {
                _numberBigDecimal = _textBuffer.contentsAsDecimal();
                _numTypesValid = NR_BIGDECIMAL;
            } else {
                // Otherwise double has to do
                _numberDouble = _textBuffer.contentsAsDouble();
                _numTypesValid = NR_DOUBLE;
            }
        } catch (NumberFormatException nex) {
            // Can this ever occur? Due to overflow, maybe?
            _wrapError("Malformed numeric value (" + _longNumberDesc(_textBuffer.contentsAsString()) + ")", nex);
        }
    }

    private void _parseSlowInt(int expType) throws IOException {
        String numStr = _textBuffer.contentsAsString();
        try {
            int len = _intLength;
            char[] buf = _textBuffer.getTextBuffer();
            int offset = _textBuffer.getTextOffset();
            if (_numberNegative) {
                ++offset;
            }
            // Some long cases still...
            if (NumberInput.inLongRange(buf, offset, len, _numberNegative)) {
                // Probably faster to construct a String, call parse, than to use BigInteger
                _numberLong = Long.parseLong(numStr);
                _numTypesValid = NR_LONG;
            } else {
                // 16-Oct-2018, tatu: Need to catch "too big" early due to [jackson-core#488]
                if ((expType == NR_INT) || (expType == NR_LONG)) {
                    _reportTooLongIntegral(expType, numStr);
                }
                if ((expType == NR_DOUBLE) || (expType == NR_FLOAT)) {
                    _numberDouble = NumberInput.parseDouble(numStr);
                    _numTypesValid = NR_DOUBLE;
                } else {
                    // nope, need the heavy guns... (rare case)
                    _numberBigInt = new BigInteger(numStr);
                    _numTypesValid = NR_BIGINT;
                }
            }
        } catch (NumberFormatException nex) {
            // Can this ever occur? Due to overflow, maybe?
            _wrapError("Malformed numeric value (" + _longNumberDesc(numStr) + ")", nex);
        }
    }

    // @since 2.9.8
    private void _reportTooLongIntegral(int expType, String rawNum) throws IOException {
        if (expType == NR_INT) {
            reportOverflowInt(rawNum);
        } else {
            reportOverflowLong(rawNum);
        }
    }

    /*
     * /**********************************************************
     * /* Numeric conversions
     * /**********************************************************
     */

    private void convertNumberToInt() throws IOException {
        // First, converting from long ought to be easy
        if ((_numTypesValid & NR_LONG) != 0) {
            // Let's verify it's lossless conversion by simple roundtrip
            int result = (int) _numberLong;
            if (((long) result) != _numberLong) {
                reportOverflowInt(getText(), currentToken());
            }
            _numberInt = result;
        } else if ((_numTypesValid & NR_BIGINT) != 0) {
            if (BI_MIN_INT.compareTo(_numberBigInt) > 0 || BI_MAX_INT.compareTo(_numberBigInt) < 0) {
                reportOverflowInt();
            }
            _numberInt = _numberBigInt.intValue();
        } else if ((_numTypesValid & NR_DOUBLE) != 0) {
            // Need to check boundaries
            if (_numberDouble < MIN_INT_D || _numberDouble > MAX_INT_D) {
                reportOverflowInt();
            }
            _numberInt = (int) _numberDouble;
        } else if ((_numTypesValid & NR_BIGDECIMAL) != 0) {
            if (BD_MIN_INT.compareTo(_numberBigDecimal) > 0 || BD_MAX_INT.compareTo(_numberBigDecimal) < 0) {
                reportOverflowInt();
            }
            _numberInt = _numberBigDecimal.intValue();
        } else {
            _throwInternal();
        }
        _numTypesValid |= NR_INT;
    }

    private void convertNumberToLong() throws IOException {
        if ((_numTypesValid & NR_INT) != 0) {
            _numberLong = _numberInt;
        } else if ((_numTypesValid & NR_BIGINT) != 0) {
            if (BI_MIN_LONG.compareTo(_numberBigInt) > 0 || BI_MAX_LONG.compareTo(_numberBigInt) < 0) {
                reportOverflowLong();
            }
            _numberLong = _numberBigInt.longValue();
        } else if ((_numTypesValid & NR_DOUBLE) != 0) {
            // Need to check boundaries
            if (_numberDouble < MIN_LONG_D || _numberDouble > MAX_LONG_D) {
                reportOverflowLong();
            }
            _numberLong = (long) _numberDouble;
        } else if ((_numTypesValid & NR_BIGDECIMAL) != 0) {
            if (BD_MIN_LONG.compareTo(_numberBigDecimal) > 0 || BD_MAX_LONG.compareTo(_numberBigDecimal) < 0) {
                reportOverflowLong();
            }
            _numberLong = _numberBigDecimal.longValue();
        } else {
            _throwInternal();
        }
        _numTypesValid |= NR_LONG;
    }

    private void convertNumberToDouble() {
        /*
         * 05-Aug-2008, tatus: Important note: this MUST start with
         * more accurate representations, since we don't know which
         * value is the original one (others get generated when
         * requested)
         */

        if ((_numTypesValid & NR_BIGDECIMAL) != 0) {
            _numberDouble = _numberBigDecimal.doubleValue();
        } else if ((_numTypesValid & NR_BIGINT) != 0) {
            _numberDouble = _numberBigInt.doubleValue();
        } else if ((_numTypesValid & NR_LONG) != 0) {
            _numberDouble = (double) _numberLong;
        } else if ((_numTypesValid & NR_INT) != 0) {
            _numberDouble = _numberInt;
        } else {
            _throwInternal();
        }
        _numTypesValid |= NR_DOUBLE;
    }

    /*
     * /**********************************************************
     * /* Error reporting
     * /**********************************************************
     */

    protected final void reportUnexpectedNumberChar(int ch, String comment) throws JsonParseException {
        String msg = "Unexpected character (" + _getCharDesc(ch) + ") in numeric value";
        if (comment != null) {
            msg += ": " + comment;
        }
        _reportError(msg);
    }

    /**
     * Method called to throw an exception for input token that looks like a number
     * based on first character(s), but is not valid according to rules of format.
     * In case of JSON this also includes invalid forms like positive sign and
     * leading zeroes.
     *
     * @throws JsonParseException Exception that describes problem with number validity
     */
    protected final void reportInvalidNumber() throws JsonParseException {
        _reportError("Invalid numeric value: Leading zeroes not allowed");
    }

    /**
     * Method called to throw an exception for integral (not floating point) input
     * token with value outside of Java signed 32-bit range when requested as {@code int}.
     * Result will be {@link InputCoercionException} being thrown.
     *
     * @throws JsonParseException Exception that describes problem with number range validity
     */
    private void reportOverflowInt() throws IOException {
        reportOverflowInt(getText());
    }

    // @since 2.10
    private void reportOverflowInt(String numDesc) throws IOException {
        reportOverflowInt(numDesc, currentToken());
    }

    // @since 2.10
    private void reportOverflowInt(String numDesc, JsonToken inputType) throws IOException {
        String msg = String.format("Numeric value (%s) out of range of int (%d - %s)",
            _longIntegerDesc(numDesc), Integer.MIN_VALUE, Integer.MAX_VALUE);
        throw new InputCoercionException(this, msg, inputType, Integer.TYPE);
    }

    /**
     * Method called to throw an exception for integral (not floating point) input
     * token with value outside of Java signed 64-bit range when requested as {@code long}.
     * Result will be {@link InputCoercionException} being thrown.
     *
     * @throws JsonParseException Exception that describes problem with number range validity
     */
    private void reportOverflowLong() throws IOException {
        reportOverflowLong(getText());
    }

    // @since 2.10
    private void reportOverflowLong(String numDesc) throws IOException {
        String msg = String.format("Numeric value (%s) out of range of long (%d - %s)",
            _longIntegerDesc(numDesc), Long.MIN_VALUE, Long.MAX_VALUE);
        throw new InputCoercionException(this, msg, _currToken, Long.TYPE);
    }

    // @since 2.9.8
    private String _longIntegerDesc(String rawNum) {
        int rawLen = rawNum.length();
        if (rawLen < 1000) {
            return rawNum;
        }
        if (rawNum.startsWith("-")) {
            rawLen -= 1;
        }
        return String.format("[Integer with %d digits]", rawLen);
    }

    // @since 2.9.8
    private String _longNumberDesc(String rawNum) {
        int rawLen = rawNum.length();
        if (rawLen < 1000) {
            return rawNum;
        }
        if (rawNum.startsWith("-")) {
            rawLen -= 1;
        }
        return String.format("[number with %d characters]", rawLen);
    }

    protected void _reportUnexpectedChar(int ch, String comment) throws JsonParseException {
        if (ch < 0) { // sanity check
            _reportInvalidEOF();
        }
        String msg = String.format("Unexpected character (%s)", _getCharDesc(ch));
        if (comment != null) {
            msg += ": " + comment;
        }
        _reportError(msg);
    }

    protected void _reportInvalidEOF() throws JsonParseException {
        _reportInvalidEOF(" in " + _currToken, _currToken);
    }

    // @since 2.8
    protected void _reportInvalidEOFInValue(JsonToken type) throws JsonParseException {
        String msg;
        if (type == JsonToken.VALUE_STRING) {
            msg = " in a String value";
        } else if ((type == JsonToken.VALUE_NUMBER_INT) || (type == JsonToken.VALUE_NUMBER_FLOAT)) {
            msg = " in a Number value";
        } else {
            msg = " in a value";
        }
        _reportInvalidEOF(msg, type);
    }

    // @since 2.8
    protected void _reportInvalidEOF(String msg, JsonToken currToken) throws JsonParseException {
        throw new JsonEOFException(this, currToken, "Unexpected end-of-input" + msg);
    }

    protected void _reportMissingRootWS(int ch) throws JsonParseException {
        _reportUnexpectedChar(ch, "Expected space separating root-level values");
    }

    protected void _throwInvalidSpace(int i) throws JsonParseException {
        char c = (char) i;
        String msg = "Illegal character (" + _getCharDesc(c)
            + "): only regular white space (\\r, \\n, \\t) is allowed between tokens";
        _reportError(msg);
    }

    /*
     * /**********************************************************
     * /* Error reporting, generic
     * /**********************************************************
     */

    private static String _getCharDesc(int ch) {
        char c = (char) ch;
        if (Character.isISOControl(c)) {
            return "(CTRL-CHAR, code " + ch + ")";
        }
        if (ch > 255) {
            return "'" + c + "' (code " + ch + " / 0x" + Integer.toHexString(ch) + ")";
        }
        return "'" + c + "' (code " + ch + ")";
    }

    protected final void _reportError(String msg) throws JsonParseException {
        throw _constructError(msg);
    }

    // @since 2.9
    protected final void _reportError(String msg, Object arg) throws JsonParseException {
        throw _constructError(String.format(msg, arg));
    }

    // @since 2.9
    protected final void _reportError(Object arg1, Object arg2) throws JsonParseException {
        throw _constructError(String.format("Unrecognized token '%s': was expecting %s", arg1, arg2));
    }

    private void _wrapError(String msg, Throwable t) throws JsonParseException {
        throw _constructError(msg, t);
    }

    private static void _throwInternal() {
        throw new RuntimeException("Internal error: this code path should never get executed");
    }

    private JsonParseException _constructError(String msg, Throwable t) {
        return new JsonParseException(this, msg, t);
    }

    protected void _reportMismatchedEndMarker(int actCh, char expCh) throws JsonParseException {
        JsonReadContext ctxt = getParsingContext();
        _reportError(String.format("Unexpected close marker '%s': expected '%c' (for %s starting at %s)", (char) actCh,
            expCh, ctxt.typeDesc(), ctxt.startLocation(_contentReference())));
    }

    protected char _handleUnrecognizedCharacterEscape(char ch) throws JsonProcessingException {
        _reportError("Unrecognized character escape " + _getCharDesc(ch));
        return ch;
    }

    /**
     * Method called to report a problem with unquoted control character..
     *
     * @param i Invalid control character
     * @param ctxtDesc Addition description of context to use in exception message
     *
     * @throws JsonParseException explaining the problem
     */
    protected void _throwUnquotedSpace(int i, String ctxtDesc) throws JsonParseException {
        // JACKSON-208; possible to allow unquoted control chars:
        if (i > INT_SPACE) {
            char c = (char) i;
            String msg = "Illegal unquoted character (" + _getCharDesc(c)
                + "): has to be escaped using backslash to be included in " + ctxtDesc;
            _reportError(msg);
        }
    }

    /**
     * @return Description to use as "valid JSON values" in an exception message about
     *    invalid (unrecognized) JSON value: called when parser finds something that
     *    does not look like a value or separator.
     *
     * @since 2.10
     */
    @SuppressWarnings("deprecation")
    protected String _validJsonValueList() {
        if (isEnabled(Feature.ALLOW_NON_NUMERIC_NUMBERS)) {
            return "(JSON String, Number (or 'NaN'/'Infinity'/'+Infinity'/'-Infinity'), Array, Object or token 'null', 'true' or 'false')";
        }
        return "(JSON String, Number, Array, Object or token 'null', 'true' or 'false')";
    }

    /*
     * /**********************************************************
     * /* Base64 handling support
     * /**********************************************************
     */

    /**
     * Method that sub-classes must implement to support escaped sequences
     * in base64-encoded sections.
     * Sub-classes that do not need base64 support can leave this as is
     *
     * @return Character decoded, if any
     *
     * @throws IOException If escape decoding fails
     */
    protected char _decodeEscaped() throws IOException {
        throw new UnsupportedOperationException();
    }

    protected final int _decodeBase64Escape(Base64Variant b64variant, int ch, int index) throws IOException {
        // 17-May-2011, tatu: As per [JACKSON-xxx], need to handle escaped chars
        if (ch != '\\') {
            throw reportInvalidBase64Char(b64variant, ch, index);
        }
        int unescaped = _decodeEscaped();
        // if white space, skip if first triplet; otherwise errors
        if (unescaped <= INT_SPACE) {
            if (index == 0) { // whitespace only allowed to be skipped between triplets
                return -1;
            }
        }
        // otherwise try to find actual triplet value
        int bits = b64variant.decodeBase64Char(unescaped);
        if (bits < 0) {
            if (bits != Base64Variant.BASE64_VALUE_PADDING) {
                throw reportInvalidBase64Char(b64variant, unescaped, index);
            }
        }
        return bits;
    }

    protected final int _decodeBase64Escape(Base64Variant b64variant, char ch, int index) throws IOException {
        if (ch != '\\') {
            throw reportInvalidBase64Char(b64variant, ch, index);
        }
        char unescaped = _decodeEscaped();
        // if white space, skip if first triplet; otherwise errors
        if (unescaped <= INT_SPACE) {
            if (index == 0) { // whitespace only allowed to be skipped between triplets
                return -1;
            }
        }
        // otherwise try to find actual triplet value
        int bits = b64variant.decodeBase64Char(unescaped);
        if (bits < 0) {
            // second check since padding can only be 3rd or 4th byte (index #2 or #3)
            if ((bits != Base64Variant.BASE64_VALUE_PADDING) || (index < 2)) {
                throw reportInvalidBase64Char(b64variant, unescaped, index);
            }
        }
        return bits;
    }

    protected IllegalArgumentException reportInvalidBase64Char(Base64Variant b64variant, int ch, int bindex)
        throws IllegalArgumentException {
        return reportInvalidBase64Char(b64variant, ch, bindex, null);
    }

    /*
     * @param bindex Relative index within base64 character unit; between 0
     * and 3 (as unit has exactly 4 characters)
     */
    protected IllegalArgumentException reportInvalidBase64Char(Base64Variant b64variant, int ch, int bindex, String msg)
        throws IllegalArgumentException {
        String base;
        if (ch <= INT_SPACE) {
            base = String.format(
                "Illegal white space character (code 0x%s) as character #%d of 4-char base64 unit: can only used between units",
                Integer.toHexString(ch), (bindex + 1));
        } else if (b64variant.usesPaddingChar(ch)) {
            base = "Unexpected padding character ('=') as character #" + (bindex + 1)
                + " of 4-char base64 unit: padding only legal as 3rd or 4th character";
        } else if (!Character.isDefined(ch) || Character.isISOControl(ch)) {
            // Not sure if we can really get here... ? (most illegal xml chars are caught at lower level)
            base = "Illegal character (code 0x" + Integer.toHexString(ch) + ") in base64 content";
        } else {
            base = "Illegal character '" + ((char) ch) + "' (code 0x" + Integer.toHexString(ch) + ") in base64 content";
        }
        if (msg != null) {
            base = base + ": " + msg;
        }
        return new IllegalArgumentException(base);
    }

    // since 2.9.8
    protected void _handleBase64MissingPadding(Base64Variant b64variant) throws IOException {
        _reportError(b64variant.missingPaddingMessage());
    }

    /*
     * /**********************************************************
     * /* Internal/package methods: other
     * /**********************************************************
     */

    /**
     * Helper method used to encapsulate logic of including (or not) of
     * "content reference" when constructing {@link JsonLocation} instances.
     *
     * @return Source reference object, if any; {@code null} if none
     *
     * @since 2.13
     */
    protected ContentReference _contentReference() {
        return _ioContext.contentReference();
    }

    protected static int[] growArrayBy(int[] arr, int more) {
        if (arr == null) {
            return new int[more];
        }
        return Arrays.copyOf(arr, arr.length + more);
    }

    /*
     * /**********************************************************
     * /* Stuff that was abstract and required before 2.8, but that
     * /* is not mandatory in 2.8 or above.
     * /**********************************************************
     */

    // Can't declare as deprecated, for now, but shouldn't be needed
    protected void _finishString() throws IOException {
    }
}
