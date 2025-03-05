// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package io.clientcore.core.serialization.json.implementation.jackson.core.base;

import io.clientcore.core.serialization.json.implementation.jackson.core.Base64Variant;
import io.clientcore.core.serialization.json.implementation.jackson.core.Base64Variants;
import io.clientcore.core.serialization.json.implementation.jackson.core.JsonLocation;
import io.clientcore.core.serialization.json.implementation.jackson.core.JsonParseException;
import io.clientcore.core.serialization.json.implementation.jackson.core.JsonParser;
import io.clientcore.core.serialization.json.implementation.jackson.core.JsonToken;
import io.clientcore.core.serialization.json.implementation.jackson.core.exc.InputCoercionException;
import io.clientcore.core.serialization.json.implementation.jackson.core.io.ContentReference;
import io.clientcore.core.serialization.json.implementation.jackson.core.io.IOContext;
import io.clientcore.core.serialization.json.implementation.jackson.core.io.JsonEOFException;
import io.clientcore.core.serialization.json.implementation.jackson.core.io.NumberInput;
import io.clientcore.core.serialization.json.implementation.jackson.core.json.JsonReadContext;
import io.clientcore.core.serialization.json.implementation.jackson.core.util.ByteArrayBuilder;
import io.clientcore.core.serialization.json.implementation.jackson.core.util.TextBuffer;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Intermediate base class used by all Jackson {@link JsonParser}
 * implementations, but does not add any additional fields that depend
 * on particular method of obtaining input.
 *<p>
 * Note that 'minimal' here mostly refers to minimal number of fields
 * (size) and functionality that is specific to certain types
 * of parser implementations; but not necessarily to number of methods.
 */
public abstract class ParserMinimalBase extends JsonParser {
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

    /**
     * Flag that indicates that the current token has not yet
     * been fully processed, and needs to be finished for
     * some access (or skipped to obtain the next token)
     */
    protected boolean _tokenIncomplete;

    protected ParserMinimalBase(IOContext ctxt, int features) {
        super(features);
        _ioContext = ctxt;
        _textBuffer = ctxt.constructTextBuffer();
        _parsingContext = JsonReadContext.createRootContext();
    }

    /*
     * /**********************************************************
     * /* JsonParser impl
     * /**********************************************************
     */

    @Override
    public JsonToken getCurrentToken() {
        return _currToken;
    }

    @Override
    public JsonParser skipChildren() throws IOException {
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
                throw _constructError(
                    String.format("Not enough content available for `skipChildren()`: non-blocking parser? (%s)",
                        getClass().getName()));
            }
        }
    }

    /**
     * Method called when an EOF is encountered between tokens.
     * If so, it may be a legitimate EOF, but only iff there
     * is no open non-root context.
     */
    protected void _handleEOF() throws JsonParseException {
        if (!_parsingContext.inRoot()) {
            String marker = _parsingContext.inArray() ? "Array" : "Object";
            _reportInvalidEOF(String.format(": expected close marker for %s (start marker at %s)", marker,
                _parsingContext.startLocation(_contentReference())), null);
        }
    }

    /**
     * Method that can be called to get the name associated with
     * the current event.
     */
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
            }
        }
    }

    @Override
    public JsonReadContext getParsingContext() {
        return _parsingContext;
    }

    /**
     * Method that returns location of the last processed character;
     * usually for error reporting purposes
     */
    @Override
    public JsonLocation getCurrentLocation() {
        int col = _inputPtr - _currInputRowStart + 1; // 1-based
        return new JsonLocation(_contentReference(), -1L, _currInputProcessed + _inputPtr, // bytes, chars
            _currInputRow, col);
    }

    /*
     * /**********************************************************
     * /* Numeric accessors of public API
     * /**********************************************************
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
        return _numberDouble;
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
    protected void _parseNumericValue(int expType) throws IOException {
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
        throw _constructError(
            String.format("Current token (%s) not numeric, can not use numeric value accessors", _currToken));
    }

    // @since 2.6
    protected int _parseIntValue() throws IOException {
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
                    _numberDouble = Double.parseDouble(numStr);
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
    protected void _reportTooLongIntegral(int expType, String rawNum) throws IOException {
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

    protected void convertNumberToInt() throws IOException {
        // First, converting from long ought to be easy
        if ((_numTypesValid & NR_LONG) != 0) {
            // Let's verify it's lossless conversion by simple roundtrip
            int result = (int) _numberLong;
            if (((long) result) != _numberLong) {
                reportOverflowInt(getText());
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
            throw new RuntimeException("Internal error: this code path should never get executed");
        }
        _numTypesValid |= NR_INT;
    }

    protected void convertNumberToLong() throws IOException {
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
            throw new RuntimeException("Internal error: this code path should never get executed");
        }
        _numTypesValid |= NR_LONG;
    }

    protected void convertNumberToDouble() {
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
            throw new RuntimeException("Internal error: this code path should never get executed");
        }
        _numTypesValid |= NR_DOUBLE;
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
     * @param b64variant Base64 variant expected in content
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    protected void _decodeBase64(String str, ByteArrayBuilder builder, Base64Variant b64variant) throws IOException {
        try {
            b64variant.decode(str, builder);
        } catch (IllegalArgumentException e) {
            _reportError(e.getMessage());
        }
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
    protected void _handleBase64MissingPadding() throws IOException {
        _reportError(Base64Variants.getDefaultVariant().missingPaddingMessage());
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
     * /* Internal/package methods: shared/reusable builders
     * /**********************************************************
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
     * /* Error reporting
     * /**********************************************************
     */

    protected void reportUnexpectedNumberChar(int ch, String comment) throws JsonParseException {
        String msg = String.format("Unexpected character (%s) in numeric value", _getCharDesc(ch));
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
    protected void reportInvalidNumber() throws JsonParseException {
        _reportError("Invalid numeric value: Leading zeroes not allowed");
    }

    /**
     * Method called to throw an exception for integral (not floating point) input
     * token with value outside of Java signed 32-bit range when requested as {@code int}.
     * Result will be {@link InputCoercionException} being thrown.
     *
     * @throws JsonParseException Exception that describes problem with number range validity
     */
    protected void reportOverflowInt() throws IOException {
        reportOverflowInt(getText());
    }

    // @since 2.10
    protected void reportOverflowInt(String numDesc) throws IOException {
        throw new InputCoercionException(this, String.format("Numeric value (%s) out of range of int (%d - %s)",
            _longIntegerDesc(numDesc), Integer.MIN_VALUE, Integer.MAX_VALUE), _currToken, Integer.TYPE);
    }

    /**
     * Method called to throw an exception for integral (not floating point) input
     * token with value outside of Java signed 64-bit range when requested as {@code long}.
     * Result will be {@link InputCoercionException} being thrown.
     *
     * @throws JsonParseException Exception that describes problem with number range validity
     */
    protected void reportOverflowLong() throws IOException {
        throw new InputCoercionException(this, String.format("Numeric value (%s) out of range of long (%d - %s)",
            _longIntegerDesc(getText()), Long.MIN_VALUE, Long.MAX_VALUE), _currToken, Long.TYPE);
    }

    // @since 2.10
    protected void reportOverflowLong(String numDesc) throws IOException {
        throw new InputCoercionException(this, String.format("Numeric value (%s) out of range of long (%d - %s)",
            _longIntegerDesc(numDesc), Long.MIN_VALUE, Long.MAX_VALUE), _currToken, Long.TYPE);
    }

    // @since 2.9.8
    protected String _longIntegerDesc(String rawNum) {
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
    protected String _longNumberDesc(String rawNum) {
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
    protected void _reportInvalidEOFInValue() throws JsonParseException {
        _reportInvalidEOF(" in a Number value", JsonToken.VALUE_NUMBER_INT);
    }

    // @since 2.8
    protected void _reportInvalidEOF(String msg, JsonToken currToken) throws JsonParseException {
        throw new JsonEOFException(this, currToken, "Unexpected end-of-input" + msg);
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

    protected static String _getCharDesc(int ch) {
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

    protected final void _wrapError(String msg, Throwable t) throws JsonParseException {
        throw new JsonParseException(this, msg, t);
    }

    protected void _reportMismatchedEndMarker(int actCh, char expCh) throws JsonParseException {
        JsonReadContext ctxt = getParsingContext();
        _reportError(String.format("Unexpected close marker '%s': expected '%c' (for %s starting at %s)", (char) actCh,
            expCh, ctxt.typeDesc(), ctxt.startLocation(_contentReference())));
    }

    /**
     * Method called to report a problem with unquoted control character.
     *
     * @param i Invalid control character
     * @param ctxtDesc Addition description of context to use in exception message
     *
     * @throws JsonParseException explaining the problem
     */
    protected void _throwUnquotedSpace(int i, String ctxtDesc) throws JsonParseException {
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
    protected String _validJsonValueList() {
        if (Feature.ALLOW_NON_NUMERIC_NUMBERS.enabledIn(_features)) {
            return "(JSON String, Number (or 'NaN'/'Infinity'/'+Infinity'/'-Infinity'), Array, Object or token 'null', 'true' or 'false')";
        }
        return "(JSON String, Number, Array, Object or token 'null', 'true' or 'false')";
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

    // Can't declare as deprecated, for now, but shouldn't be needed
    protected void _finishString() throws IOException {
    }

    protected final String _getText2(JsonToken t) {
        if (t == null) {
            return null;
        }
        switch (t) {
            case FIELD_NAME:
                return _parsingContext.getCurrentName();

            case VALUE_STRING:
                // fall through
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
                return _textBuffer.contentsAsString();

            default:
                return t.asString();
        }
    }

    protected final JsonToken _nextAfterName() {
        JsonToken t = _nextToken;
        _nextToken = null;

        // !!! 16-Nov-2015, tatu: TODO: fix [databind#37], copy next location to current here

        // Also: may need to start new context?
        if (t == JsonToken.START_ARRAY) {
            _parsingContext = _parsingContext.createChildArrayContext(_tokenInputRow, _tokenInputCol);
        } else if (t == JsonToken.START_OBJECT) {
            _parsingContext = _parsingContext.createChildObjectContext(_tokenInputRow, _tokenInputCol);
        }
        return (_currToken = t);
    }
}
