// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.base;

import com.azure.json.implementation.jackson.core.Base64Variant;
import com.azure.json.implementation.jackson.core.JsonLocation;
import com.azure.json.implementation.jackson.core.JsonParseException;
import com.azure.json.implementation.jackson.core.JsonParser;
import com.azure.json.implementation.jackson.core.JsonProcessingException;
import com.azure.json.implementation.jackson.core.JsonToken;
import com.azure.json.implementation.jackson.core.StreamReadConstraints;
import com.azure.json.implementation.jackson.core.StreamReadFeature;
import com.azure.json.implementation.jackson.core.exc.StreamConstraintsException;
import com.azure.json.implementation.jackson.core.io.ContentReference;
import com.azure.json.implementation.jackson.core.io.IOContext;
import com.azure.json.implementation.jackson.core.io.NumberInput;
import com.azure.json.implementation.jackson.core.json.JsonReadContext;
import com.azure.json.implementation.jackson.core.util.ByteArrayBuilder;
import com.azure.json.implementation.jackson.core.util.TextBuffer;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * Intermediate base class used by all Jackson {@link JsonParser}
 * implementations. Contains most common things that are independent
 * of actual underlying input source.
 */
public abstract class ParserBase extends ParserMinimalBase {

    /*
    /**********************************************************
    /* Generic I/O state
    /**********************************************************
     */

    /**
     * I/O context for this reader. It handles buffer allocation
     * for the reader.
     */
    protected final IOContext _ioContext;

    /**
     * @since 2.15
     */
    protected final StreamReadConstraints _streamReadConstraints;

    /**
     * Flag that indicates whether parser is closed or not. Gets
     * set when parser is either closed by explicit call
     * ({@link #close}) or when end-of-input is reached.
     */
    protected boolean _closed;

    /*
    /**********************************************************
    /* Current input data
    /**********************************************************
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
    /**********************************************************
    /* Current input location information
    /**********************************************************
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
    /**********************************************************
    /* Information about starting location of event
    /* Reader is pointing to; updated on-demand
    /**********************************************************
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
    /**********************************************************
    /* Parsing state
    /**********************************************************
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
    /**********************************************************
    /* Buffer(s) for local name(s) and text content
    /**********************************************************
     */

    /**
     * Buffer that contains contents of String values, including
     * field names if necessary (name split across boundary,
     * contains escape sequence, or access needed to char array)
     */
    protected final TextBuffer _textBuffer;

    /**
     * Temporary buffer that is needed if field name is accessed
     * using {@code #getTextCharacters} method (instead of String
     * returning alternatives)
     */
    protected char[] _nameCopyBuffer;

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

    protected float _numberFloat;

    protected double _numberDouble;

    // And then object types

    protected BigInteger _numberBigInt;

    protected BigDecimal _numberBigDecimal;

    /**
     * Textual number representation captured from input in cases lazy-parsing
     * is desired.
     *
     * @since 2.14
     */
    protected String _numberString;

    /**
     * Marker for explicit "Not a Number" (NaN) values that may be read
     * by some formats: this includes positive and negative infinity,
     * as well as "NaN" result for some arithmetic operations.
     *<p>
     * In case of JSON, such values can only be handled with non-standard
     * processing: for some other formats they can be passed normally.
     *<p>
     * NOTE: this marker is NOT set in case of value overflow/underflow for
     * {@code double} or {@code float} values.
     *
     * @since 2.17
     */
    protected boolean _numberIsNaN;

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
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    protected ParserBase(IOContext ctxt, int features) {
        super(features);
        _ioContext = ctxt;
        final StreamReadConstraints streamReadConstraints = ctxt.streamReadConstraints();
        _streamReadConstraints
            = streamReadConstraints == null ? StreamReadConstraints.defaults() : streamReadConstraints;
        _textBuffer = ctxt.constructReadConstrainedTextBuffer();
        _parsingContext = JsonReadContext.createRootContext();
    }

    /**
     * Method that can be called to get the name associated with
     * the current event.
     */
    @Deprecated // since 2.17
    @Override
    public String getCurrentName() {
        // [JACKSON-395]: start markers require information from parent
        if (_currToken == JsonToken.START_OBJECT || _currToken == JsonToken.START_ARRAY) {
            JsonReadContext parent = _parsingContext.getParent();
            if (parent != null) {
                return parent.getCurrentName();
            }
        }
        return _parsingContext.getCurrentName();
    }

    @Override
    public void close() throws IOException {
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
                _ioContext.close();
            }
        }
    }

    @Override
    public boolean isClosed() {
        return _closed;
    }

    @Override
    public JsonReadContext getParsingContext() {
        return _parsingContext;
    }

    /**
     * Method that return the <b>starting</b> location of the current
     * token; that is, position of the first character from input
     * that starts the current token.
     */
    @Override
    @Deprecated // since 2.17
    public JsonLocation getTokenLocation() {
        return new JsonLocation(_contentReference(), -1L, getTokenCharacterOffset(), // bytes, chars
            getTokenLineNr(), getTokenColumnNr());
    }

    /**
     * Method that returns location of the last processed character;
     * usually for error reporting purposes
     */
    @Override
    @Deprecated // since 2.17
    public JsonLocation getCurrentLocation() {
        int col = _inputPtr - _currInputRowStart + 1; // 1-based
        return new JsonLocation(_contentReference(), -1L, _currInputProcessed + _inputPtr, // bytes, chars
            _currInputRow, col);
    }

    /*
    /**********************************************************
    /* Public API, access to token information, text and similar
    /**********************************************************
     */

    @SuppressWarnings("resource")
    @Override // since 2.7
    public byte[] getBinaryValue(Base64Variant variant) throws IOException {
        if (_binaryValue == null) {
            if (_currToken != JsonToken.VALUE_STRING) {
                _reportError("Current token (" + _currToken + ") not VALUE_STRING, can not access as binary");
            }
            ByteArrayBuilder builder = _getByteArrayBuilder();
            _decodeBase64(getText(), builder, variant);
            _binaryValue = builder.toByteArray();
        }
        return _binaryValue;
    }

    /*
    /**********************************************************
    /* Public low-level accessors
    /**********************************************************
     */

    public long getTokenCharacterOffset() {
        return _tokenInputTotal;
    }

    public int getTokenLineNr() {
        return _tokenInputRow;
    }

    public int getTokenColumnNr() {
        // note: value of -1 means "not available"; otherwise convert from 0-based to 1-based
        int col = _tokenInputCol;
        return (col < 0) ? col : (col + 1);
    }

    /*
    /**********************************************************
    /* Abstract methods for sub-classes to implement
    /**********************************************************
     */

    protected abstract void _closeInput() throws IOException;

    /*
    /**********************************************************
    /* Low-level reading, other
    /**********************************************************
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
        char[] buf = _nameCopyBuffer;
        if (buf != null) {
            _nameCopyBuffer = null;
            _ioContext.releaseNameCopyBuffer(buf);
        }
    }

    /**
     * Method called when an EOF is encountered between tokens.
     * If so, it may be a legitimate EOF, but only iff there
     * is no open non-root context.
     */
    @Override
    protected void _handleEOF() throws JsonParseException {
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
    /**********************************************************
    /* Internal/package methods: shared/reusable builders
    /**********************************************************
     */

    public ByteArrayBuilder _getByteArrayBuilder() {
        if (_byteArrayBuilder == null) {
            _byteArrayBuilder = new ByteArrayBuilder();
        } else {
            _byteArrayBuilder.reset();
        }
        return _byteArrayBuilder;
    }

    /*
    /**********************************************************
    /* Methods from former JsonNumericParserBase
    /**********************************************************
     */

    // // // Life-cycle of number-parsing

    protected final JsonToken reset(boolean negative, int intLen, int fractLen, int expLen) throws IOException {
        if (fractLen < 1 && expLen < 1) { // integer
            return resetInt(negative, intLen);
        }
        return resetFloat(negative, intLen, fractLen, expLen);
    }

    protected final JsonToken resetInt(boolean negative, int intLen) throws IOException {
        // May throw StreamConstraintsException:
        _streamReadConstraints.validateIntegerLength(intLen);
        _numberNegative = negative;
        _numberIsNaN = false;
        _intLength = intLen;
        _fractLength = 0;
        _expLength = 0;
        _numTypesValid = NR_UNKNOWN; // to force decoding
        return JsonToken.VALUE_NUMBER_INT;
    }

    protected final JsonToken resetFloat(boolean negative, int intLen, int fractLen, int expLen) throws IOException {
        // May throw StreamConstraintsException:
        _streamReadConstraints.validateFPLength(intLen + fractLen + expLen);
        _numberNegative = negative;
        _numberIsNaN = false;
        _intLength = intLen;
        _fractLength = fractLen;
        _expLength = expLen;
        _numTypesValid = NR_UNKNOWN; // to force decoding
        return JsonToken.VALUE_NUMBER_FLOAT;
    }

    protected final JsonToken resetAsNaN(String valueStr, double value) throws IOException {
        _textBuffer.resetWithString(valueStr);
        _numberDouble = value;
        _numTypesValid = NR_DOUBLE;
        _numberIsNaN = true;
        return JsonToken.VALUE_NUMBER_FLOAT;
    }

    /*
    /**********************************************************
    /* Numeric accessors of public API
    /**********************************************************
     */

    @Override
    public int getIntValue() throws IOException {
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

    @Override
    public long getLongValue() throws IOException {
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

    @Override
    public float getFloatValue() throws IOException {
        /* 22-Jan-2009, tatu: Bounds/range checks would be tricky
         *   here, so let's not bother even trying...
         */
        /*
        if (value < -Float.MAX_VALUE || value > MAX_FLOAT_D) {
            _reportError("Numeric value ("+getText()+") out of range of Java float");
        }
        */
        if ((_numTypesValid & NR_FLOAT) == 0) {
            if (_numTypesValid == NR_UNKNOWN) {
                _parseNumericValue(NR_FLOAT);
            }
            if ((_numTypesValid & NR_FLOAT) == 0) {
                convertNumberToFloat();
            }
        }
        return _getNumberFloat();
    }

    @Override
    public double getDoubleValue() throws IOException {
        if ((_numTypesValid & NR_DOUBLE) == 0) {
            if (_numTypesValid == NR_UNKNOWN) {
                _parseNumericValue(NR_DOUBLE);
            }
            if ((_numTypesValid & NR_DOUBLE) == 0) {
                convertNumberToDouble();
            }
        }
        return _getNumberDouble();
    }

    @Override // @since 2.15
    public StreamReadConstraints streamReadConstraints() {
        return _streamReadConstraints;
    }

    /*
    /**********************************************************
    /* Conversion from textual to numeric representation
    /**********************************************************
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
    protected void _parseNumericValue(int expType) throws IOException {
        // 12-Jun-2020, tatu: Sanity check to prevent more cryptic error for this case.
        //    (note: could alternatively see if TextBuffer has aggregated contents, avoid
        //    exception -- but that might be more confusing)
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
            // For [core#865]: handle remaining 19-char cases as well
            if (len == 19) {
                char[] buf = _textBuffer.getTextBuffer();
                int offset = _textBuffer.getTextOffset();
                if (_numberNegative) {
                    ++offset;
                }
                if (NumberInput.inLongRange(buf, offset, len, _numberNegative)) {
                    _numberLong = NumberInput.parseLong19(buf, offset, _numberNegative);
                    _numTypesValid = NR_LONG;
                    return;
                }
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
    protected int _parseIntValue() throws IOException {
        // 12-Jun-2020, tatu: Sanity check to prevent more cryptic error for this case.
        //    (note: could alternatively see if TextBuffer has aggregated contents, avoid
        //    exception -- but that might be more confusing)
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
        /* Nope: floating point. Here we need to be careful to get
         * optimal parsing strategy: choice is between accurate but
         * slow (BigDecimal) and lossy but fast (Double). For now
         * let's only use BD when explicitly requested -- it can
         * still be constructed correctly at any point since we do
         * retain textual representation
         */
        if (expType == NR_BIGDECIMAL) {
            // 04-Dec-2022, tatu: Let's defer actual decoding until it is certain
            //    value is actually needed.
            _numberBigDecimal = null;
            _numberString = _textBuffer.contentsAsString();
            _numTypesValid = NR_BIGDECIMAL;
        } else if (expType == NR_FLOAT) {
            _numberFloat = 0.0f;
            _numberString = _textBuffer.contentsAsString();
            _numTypesValid = NR_FLOAT;
        } else {
            // Otherwise double has to do
            // 04-Dec-2022, tatu: We can get all kinds of values here, NR_DOUBLE
            //    but also NR_INT or even NR_UNKNOWN. Shouldn't we try further
            //    deferring some typing?
            _numberDouble = 0.0;
            _numberString = _textBuffer.contentsAsString();
            _numTypesValid = NR_DOUBLE;
        }
    }

    private void _parseSlowInt(int expType) throws IOException {
        final String numStr = _textBuffer.contentsAsString();
        // 16-Oct-2018, tatu: Need to catch "too big" early due to [jackson-core#488]
        if ((expType == NR_INT) || (expType == NR_LONG)) {
            _reportTooLongIntegral(expType, numStr);
        }
        if ((expType == NR_DOUBLE) || (expType == NR_FLOAT)) {
            _numberString = numStr;
            _numTypesValid = NR_DOUBLE;
        } else {
            // nope, need the heavy guns... (rare case) - since Jackson v2.14, BigInteger parsing is lazy
            _numberBigInt = null;
            _numberString = numStr;
            _numTypesValid = NR_BIGINT;
        }
    }

    // @since 2.9.8
    protected void _reportTooLongIntegral(int expType, String rawNum) throws IOException {
        if (expType == NR_INT) {
            reportOverflowInt(rawNum);
        } else {
            reportOverflowLong(rawNum);
        }
    }

    /*
    /**********************************************************
    /* Numeric conversions
    /**********************************************************
     */

    protected void convertNumberToInt() throws IOException {
        // First, converting from long ought to be easy
        if ((_numTypesValid & NR_LONG) != 0) {
            // Let's verify its lossless conversion by simple roundtrip
            int result = (int) _numberLong;
            if (result != _numberLong) {
                reportOverflowInt(getText(), currentToken());
            }
            _numberInt = result;
        } else if ((_numTypesValid & NR_BIGINT) != 0) {
            final BigInteger bigInteger = _getBigInteger();
            if (BI_MIN_INT.compareTo(bigInteger) > 0 || BI_MAX_INT.compareTo(bigInteger) < 0) {
                reportOverflowInt();
            }
            _numberInt = bigInteger.intValue();
        } else if ((_numTypesValid & NR_DOUBLE) != 0) {
            // Need to check boundaries
            final double d = _getNumberDouble();
            if (d < MIN_INT_D || d > MAX_INT_D) {
                reportOverflowInt();
            }
            _numberInt = (int) d;
        } else if ((_numTypesValid & NR_BIGDECIMAL) != 0) {
            final BigDecimal bigDecimal = _getBigDecimal();
            if (BD_MIN_INT.compareTo(bigDecimal) > 0 || BD_MAX_INT.compareTo(bigDecimal) < 0) {
                reportOverflowInt();
            }
            _numberInt = bigDecimal.intValue();
        } else {
            _throwInternal();
        }
        _numTypesValid |= NR_INT;
    }

    protected void convertNumberToLong() throws IOException {
        if ((_numTypesValid & NR_INT) != 0) {
            _numberLong = _numberInt;
        } else if ((_numTypesValid & NR_BIGINT) != 0) {
            final BigInteger bigInteger = _getBigInteger();
            if (BI_MIN_LONG.compareTo(bigInteger) > 0 || BI_MAX_LONG.compareTo(bigInteger) < 0) {
                reportOverflowLong();
            }
            _numberLong = bigInteger.longValue();
        } else if ((_numTypesValid & NR_DOUBLE) != 0) {
            // Need to check boundaries
            final double d = _getNumberDouble();
            if (d < MIN_LONG_D || d > MAX_LONG_D) {
                reportOverflowLong();
            }
            _numberLong = (long) d;
        } else if ((_numTypesValid & NR_BIGDECIMAL) != 0) {
            final BigDecimal bigDecimal = _getBigDecimal();
            if (BD_MIN_LONG.compareTo(bigDecimal) > 0 || BD_MAX_LONG.compareTo(bigDecimal) < 0) {
                reportOverflowLong();
            }
            _numberLong = bigDecimal.longValue();
        } else {
            _throwInternal();
        }
        _numTypesValid |= NR_LONG;
    }

    protected void convertNumberToDouble() throws IOException {
        /* 05-Aug-2008, tatus: Important note: this MUST start with
         *   more accurate representations, since we don't know which
         *   value is the original one (others get generated when
         *   requested)
         */

        if ((_numTypesValid & NR_BIGDECIMAL) != 0) {
            if (_numberString != null) {
                _numberDouble = _getNumberDouble();
            } else {
                _numberDouble = _getBigDecimal().doubleValue();
            }
        } else if ((_numTypesValid & NR_BIGINT) != 0) {
            if (_numberString != null) {
                _numberDouble = _getNumberDouble();
            } else {
                _numberDouble = _getBigInteger().doubleValue();
            }
        } else if ((_numTypesValid & NR_LONG) != 0) {
            _numberDouble = _numberLong;
        } else if ((_numTypesValid & NR_INT) != 0) {
            _numberDouble = _numberInt;
        } else if ((_numTypesValid & NR_FLOAT) != 0) {
            if (_numberString != null) {
                _numberDouble = _getNumberDouble();
            } else {
                _numberDouble = _getNumberFloat();
            }
        } else {
            _throwInternal();
        }
        _numTypesValid |= NR_DOUBLE;
    }

    protected void convertNumberToFloat() throws IOException {
        /* 05-Aug-2008, tatus: Important note: this MUST start with
         *   more accurate representations, since we don't know which
         *   value is the original one (others get generated when
         *   requested)
         */

        if ((_numTypesValid & NR_BIGDECIMAL) != 0) {
            if (_numberString != null) {
                _numberFloat = _getNumberFloat();
            } else {
                _numberFloat = _getBigDecimal().floatValue();
            }
        } else if ((_numTypesValid & NR_BIGINT) != 0) {
            if (_numberString != null) {
                _numberFloat = _getNumberFloat();
            } else {
                _numberFloat = _getBigInteger().floatValue();
            }
        } else if ((_numTypesValid & NR_LONG) != 0) {
            _numberFloat = _numberLong;
        } else if ((_numTypesValid & NR_INT) != 0) {
            _numberFloat = _numberInt;
        } else if ((_numTypesValid & NR_DOUBLE) != 0) {
            if (_numberString != null) {
                _numberFloat = _getNumberFloat();
            } else {
                _numberFloat = (float) _getNumberDouble();
            }
        } else {
            _throwInternal();
        }
        _numTypesValid |= NR_FLOAT;
    }

    /**
     * Internal accessor that needs to be used for accessing number value of type
     * {@link BigInteger} which -- as of 2.14 -- is typically lazily parsed.
     *
     * @return {@link BigInteger} value of the current token
     *
     * @since 2.14
     */
    protected BigInteger _getBigInteger() throws JsonParseException {
        if (_numberBigInt != null) {
            return _numberBigInt;
        } else if (_numberString == null) {
            throw new IllegalStateException("cannot get BigInteger from current parser state");
        }
        try {
            // NOTE! Length of number string has been validated earlier
            _numberBigInt
                = NumberInput.parseBigInteger(_numberString, isEnabled(StreamReadFeature.USE_FAST_BIG_NUMBER_PARSER));
        } catch (NumberFormatException nex) {
            _wrapError("Malformed numeric value (" + _longNumberDesc(_numberString) + ")", nex);
        }
        _numberString = null;
        return _numberBigInt;
    }

    /**
     * Internal accessor that needs to be used for accessing number value of type
     * {@link BigDecimal} which -- as of 2.14 -- is typically lazily parsed.
     *
     * @return {@link BigDecimal} value of the current token
     *
     * @since 2.14
     */
    protected BigDecimal _getBigDecimal() throws JsonParseException {
        if (_numberBigDecimal != null) {
            return _numberBigDecimal;
        } else if (_numberString == null) {
            throw new IllegalStateException("cannot get BigDecimal from current parser state");
        }
        try {
            // NOTE! Length of number string has been validated earlier
            _numberBigDecimal
                = NumberInput.parseBigDecimal(_numberString, isEnabled(StreamReadFeature.USE_FAST_BIG_NUMBER_PARSER));
        } catch (NumberFormatException nex) {
            _wrapError("Malformed numeric value (" + _longNumberDesc(_numberString) + ")", nex);
        }
        _numberString = null;
        return _numberBigDecimal;
    }

    /**
     * Internal accessor that needs to be used for accessing number value of type
     * {@code double} which -- as of 2.15 -- will be lazily parsed.
     *
     * @return {@code double} value of the current token
     *
     * @since 2.15
     */
    protected double _getNumberDouble() throws JsonParseException {
        if (_numberString != null) {
            try {
                _numberDouble
                    = NumberInput.parseDouble(_numberString, isEnabled(StreamReadFeature.USE_FAST_DOUBLE_PARSER));
            } catch (NumberFormatException nex) {
                _wrapError("Malformed numeric value (" + _longNumberDesc(_numberString) + ")", nex);
            }
            _numberString = null;
        }
        return _numberDouble;
    }

    /**
     * Internal accessor that needs to be used for accessing number value of type
     * {@code float} which -- as of 2.15 -- will be lazily parsed.
     *
     * @return {@code float} value of the current token
     *
     * @since 2.15
     */
    protected float _getNumberFloat() throws JsonParseException {
        if (_numberString != null) {
            try {
                _numberFloat
                    = NumberInput.parseFloat(_numberString, isEnabled(StreamReadFeature.USE_FAST_DOUBLE_PARSER));
            } catch (NumberFormatException nex) {
                _wrapError("Malformed numeric value (" + _longNumberDesc(_numberString) + ")", nex);
            }
            _numberString = null;
        }
        return _numberFloat;
    }

    /*
    /**********************************************************
    /* Internal/package methods: Context handling (2.15)
    /**********************************************************
     */

    // @since 2.15
    protected void createChildArrayContext(final int lineNr, final int colNr) throws IOException {
        _parsingContext = _parsingContext.createChildArrayContext(lineNr, colNr);
        _streamReadConstraints.validateNestingDepth(_parsingContext.getNestingDepth());
    }

    // @since 2.15
    protected void createChildObjectContext(final int lineNr, final int colNr) throws IOException {
        _parsingContext = _parsingContext.createChildObjectContext(lineNr, colNr);
        _streamReadConstraints.validateNestingDepth(_parsingContext.getNestingDepth());
    }

    /*
    /**********************************************************
    /* Internal/package methods: Error reporting
    /**********************************************************
     */

    @SuppressWarnings("deprecation")
    protected char _handleUnrecognizedCharacterEscape(char ch) throws JsonProcessingException {
        // as per [JACKSON-300]
        if (isEnabled(Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)) {
            return ch;
        }
        // and [JACKSON-548]
        if (ch == '\'' && isEnabled(Feature.ALLOW_SINGLE_QUOTES)) {
            return ch;
        }
        throw _constructReadException("Unrecognized character escape " + _getCharDesc(ch), _currentLocationMinusOne());
    }

    protected void _reportMismatchedEndMarker(int actCh, char expCh) throws JsonParseException {
        final JsonReadContext ctxt = getParsingContext();
        final String msg = String.format("Unexpected close marker '%s': expected '%c' (for %s starting at %s)",
            (char) actCh, expCh, ctxt.typeDesc(), ctxt.startLocation(_contentReference()));
        throw _constructReadException(msg, _currentLocationMinusOne());
    }

    /**
     * Method called to report a problem with unquoted control character.
     * Note: it is possible to suppress some instances of
     * exception by enabling
     * {@link com.azure.json.implementation.jackson.core.json.JsonReadFeature#ALLOW_UNESCAPED_CONTROL_CHARS}.
     *
     * @param i Invalid control character
     * @param ctxtDesc Addition description of context to use in exception message
     *
     * @throws JsonParseException explaining the problem
     */
    @SuppressWarnings("deprecation")
    protected void _throwUnquotedSpace(int i, String ctxtDesc) throws JsonParseException {
        // JACKSON-208; possible to allow unquoted control chars:
        if (!isEnabled(Feature.ALLOW_UNQUOTED_CONTROL_CHARS) || i > INT_SPACE) {
            char c = (char) i;
            String msg = "Illegal unquoted character (" + _getCharDesc(c)
                + "): has to be escaped using backslash to be included in " + ctxtDesc;
            throw _constructReadException(msg, _currentLocationMinusOne());
        }
    }

    /**
     * @return Description to use as "valid tokens" in an exception message about
     *    invalid (unrecognized) JSON token: called when parser finds something that
     *    looks like unquoted textual token
     *
     * @since 2.10
     */
    protected String _validJsonTokenList() {
        return _validJsonValueList();
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
            return "(JSON String, Number (or 'NaN'/'+INF'/'-INF'), Array, Object or token 'null', 'true' or 'false')";
        }
        return "(JSON String, Number, Array, Object or token 'null', 'true' or 'false')";
    }

    /*
    /**********************************************************
    /* Base64 handling support
    /**********************************************************
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
     *  and 3 (as unit has exactly 4 characters)
     */
    protected IllegalArgumentException reportInvalidBase64Char(Base64Variant b64variant, int ch, int bindex, String msg)
        throws IllegalArgumentException {
        String base;
        if (ch <= INT_SPACE) {
            base = String.format(
                "Illegal white space character (code 0x%s) as character #%d of 4-char base64 unit: can only used between units",
                Integer.toHexString(ch), (bindex + 1));
        } else if (b64variant.usesPaddingChar(ch)) {
            base = "Unexpected padding character ('" + b64variant.getPaddingChar() + "') as character #" + (bindex + 1)
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
    /**********************************************************
    /* Internal/package methods: other
    /**********************************************************
     */

    /**
     * @return Source reference
     * @since 2.9
     * @deprecated Since 2.13, use {@link #_contentReference()} instead.
     */
    @Deprecated
    protected Object _getSourceReference() {
        if (JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION.enabledIn(_features)) {
            return _ioContext.contentReference().getRawContent();
        }
        return null;
    }

    /**
     * Helper method used to encapsulate logic of including (or not) of
     * "content reference" when constructing {@link JsonLocation} instances.
     *
     * @return ContentReference object to use.
     *
     * @since 2.13
     */
    protected ContentReference _contentReference() {
        if (JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION.enabledIn(_features)) {
            return _ioContext.contentReference();
        }
        return _contentReferenceRedacted();
    }

    /**
     * Helper method used to encapsulate logic of providing
     * "content reference" when constructing {@link JsonLocation} instances
     * and source information is <b>NOT</b> to be included
     * ({@code StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION} disabled).
     *<p>
     * Default implementation will simply return {@link ContentReference#redacted()}.
     *
     * @return ContentReference object to use when source is not to be included
     *
     * @since 2.16
     */
    protected ContentReference _contentReferenceRedacted() {
        return ContentReference.redacted();
    }

    /* Helper method called by name-decoding methods that require storage
     * for "quads" (4-byte units encode as ints), when existing buffer
     * is full.
     */
    protected static int[] growArrayBy(int[] arr, int more) throws IllegalArgumentException {
        if (arr == null) {
            return new int[more];
        }
        final int len = arr.length + more;
        if (len < 0) {
            throw new IllegalArgumentException("Unable to grow array to longer than `Integer.MAX_VALUE`");
        }
        return Arrays.copyOf(arr, len);
    }

    /* Helper method to call to expand "quad" buffer for name decoding
     *
     * @since 2.16
     */
    protected int[] _growNameDecodeBuffer(int[] arr, int more) throws StreamConstraintsException {
        // the following check will fail if the array is already bigger than is allowed for names
        _streamReadConstraints.validateNameLength(arr.length << 2);
        return growArrayBy(arr, more);
    }

    /*
    /**********************************************************
    /* Stuff that was abstract and required before 2.8, but that
    /* is not mandatory in 2.8 or above.
    /**********************************************************
     */

    @Deprecated // since 2.8
    protected void loadMoreGuaranteed() throws IOException {
        if (!loadMore()) {
            _reportInvalidEOF();
        }
    }

    @Deprecated // since 2.8
    protected boolean loadMore() {
        return false;
    }

    // Can't declare as deprecated, for now, but shouldn't be needed
    protected void _finishString() throws IOException {
    }
}
