// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.base;

import com.azure.json.implementation.jackson.core.Base64Variant;
import com.azure.json.implementation.jackson.core.JsonLocation;
import com.azure.json.implementation.jackson.core.JsonParseException;
import com.azure.json.implementation.jackson.core.JsonParser;
import com.azure.json.implementation.jackson.core.JsonStreamContext;
import com.azure.json.implementation.jackson.core.JsonToken;
import com.azure.json.implementation.jackson.core.exc.InputCoercionException;
import com.azure.json.implementation.jackson.core.io.JsonEOFException;
import com.azure.json.implementation.jackson.core.util.ByteArrayBuilder;

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
    protected final static int INT_APOS = '\'';
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

    protected final static char CHAR_NULL = '\0';

    /**
     * @since 2.9
     */
    protected final static byte[] NO_BYTES = new byte[0];

    /*
    /**********************************************************
    /* Constants and fields of former 'JsonNumericParserBase'
    /**********************************************************
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

    protected final static double MIN_LONG_D = Long.MIN_VALUE;
    protected final static double MAX_LONG_D = Long.MAX_VALUE;

    protected final static double MIN_INT_D = Integer.MIN_VALUE;
    protected final static double MAX_INT_D = Integer.MAX_VALUE;

    /*
    /**********************************************************
    /* Minimal generally useful state
    /**********************************************************
     */

    /**
     * Last token retrieved via {@link #nextToken}, if any.
     * Null before the first call to <code>nextToken()</code>,
     * as well as if token has been explicitly cleared
     */
    protected JsonToken _currToken;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    protected ParserMinimalBase(int features) {
        super(features);
    }

    // NOTE: had base impl in 2.3 and before; but shouldn't
    // public abstract Version version();

    /*
    /**********************************************************
    /* JsonParser impl
    /**********************************************************
     */

    @Override
    public abstract JsonToken nextToken() throws IOException;

    @Override
    public JsonToken currentToken() {
        return _currToken;
    }

    /**
     * Method sub-classes need to implement for verifying that end-of-content
     * is acceptable at current input position.
     *
     * @throws JsonParseException If end-of-content is not acceptable (for example,
     *   missing end-object or end-array tokens)
     */
    protected abstract void _handleEOF() throws JsonParseException;

    //public JsonToken getCurrentToken()
    //public boolean hasCurrentToken()

    @Override
    public abstract void close() throws IOException;

    @Override
    public abstract JsonStreamContext getParsingContext();

    /*
    /**********************************************************
    /* Public API, access to token information, text
    /**********************************************************
     */

    @Override
    public abstract String getText() throws IOException;

    /*
    /**********************************************************
    /* Public API, access to token information, binary
    /**********************************************************
     */

    @Override
    public abstract byte[] getBinaryValue(Base64Variant b64variant) throws IOException;

    /*
    /**********************************************************
    /* Public API, access with conversion/coercion
    /**********************************************************
     */

    @Override
    public String getValueAsString() throws IOException {
        // sub-classes tend to override so...
        return getValueAsString(null);
    }

    @Override
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
    /**********************************************************
    /* Base64 decoding
    /**********************************************************
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
    /**********************************************************
    /* Error reporting
    /**********************************************************
     */

    // @since 2.10
    protected void _reportInputCoercion(String msg, JsonToken inputType, Class<?> targetType)
        throws InputCoercionException {
        throw new InputCoercionException(this, msg, inputType, targetType);
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

    /**
     * Method called to throw an exception for input token that looks like a number
     * based on first character(s), but is not valid according to rules of format.
     * In case of JSON this also includes invalid forms like positive sign and
     * leading zeroes.
     *
     * @param msg Base exception message to use
     *
     * @throws JsonParseException Exception that describes problem with number validity
     */
    protected void reportInvalidNumber(String msg) throws JsonParseException {
        throw _constructReadException("Invalid numeric value: " + msg);
    }

    protected void _reportMissingRootWS(int ch) throws JsonParseException {
        _reportUnexpectedChar(ch, "Expected space separating root-level values");
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
        reportOverflowInt(numDesc, currentToken());
    }

    // @since 2.10
    protected void reportOverflowInt(String numDesc, JsonToken inputType) throws IOException {
        _reportInputCoercion(String.format("Numeric value (%s) out of range of int (%d - %s)",
            _longIntegerDesc(numDesc), Integer.MIN_VALUE, Integer.MAX_VALUE), inputType, Integer.TYPE);
    }

    /**
     * Method called to throw an exception for integral (not floating point) input
     * token with value outside of Java signed 64-bit range when requested as {@code long}.
     * Result will be {@link InputCoercionException} being thrown.
     *
     * @throws JsonParseException Exception that describes problem with number range validity
     */
    protected void reportOverflowLong() throws IOException {
        reportOverflowLong(getText());
    }

    // @since 2.10
    protected void reportOverflowLong(String numDesc) throws IOException {
        reportOverflowLong(numDesc, currentToken());
    }

    // @since 2.10
    protected void reportOverflowLong(String numDesc, JsonToken inputType) throws IOException {
        _reportInputCoercion(String.format("Numeric value (%s) out of range of long (%d - %s)",
            _longIntegerDesc(numDesc), Long.MIN_VALUE, Long.MAX_VALUE), inputType, Long.TYPE);
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
        throw _constructReadException(msg, _currentLocationMinusOne());
    }

    /**
     * @since 2.14
     *
     * @param ch Character that was unexpected
     * @param comment Additional failure comment to add, if any
     *
     * @throws JsonParseException
     */
    protected <T> T _reportUnexpectedNumberChar(int ch, String comment) throws JsonParseException {
        String msg = String.format("Unexpected character (%s) in numeric value", _getCharDesc(ch));
        if (comment != null) {
            msg += ": " + comment;
        }
        throw _constructReadException(msg, _currentLocationMinusOne());
    }

    protected void _throwInvalidSpace(int i) throws JsonParseException {
        char c = (char) i;
        String msg = "Illegal character (" + _getCharDesc(c)
            + "): only regular white space (\\r, \\n, \\t) is allowed between tokens";
        throw _constructReadException(msg);
    }

    /*
    /**********************************************************
    /* Error reporting, generic
    /**********************************************************
     */

    /**
     * Factory method used to provide location for cases where we must read
     * and consume a single "wrong" character (to possibly allow error recovery),
     * but need to report accurate location for that character: if so, the
     * current location is past location we want, and location we want will be
     * "one location earlier".
     *<p>
     * Default implementation simply returns {@link #currentLocation()}
     *
     * @since 2.17
     *
     * @return Same as {@link #currentLocation()} except offset by -1
     */
    protected JsonLocation _currentLocationMinusOne() {
        return currentLocation();
    }

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
        throw _constructReadException(msg);
    }

    // @since 2.9
    protected final void _reportError(String msg, Object arg) throws JsonParseException {
        throw _constructReadException(msg, arg);
    }

    // @since 2.9
    protected final void _reportError(Object arg1, Object arg2) throws JsonParseException {
        throw _constructReadException("Unrecognized token '%s': was expecting %s", arg1, arg2);
    }

    protected final void _throwInternal() {
        throw new RuntimeException("Internal error: this code path should never get executed");
    }

    protected final void _wrapError(String msg, Throwable t) throws JsonParseException {
        throw _constructReadException(msg, t);
    }
}
