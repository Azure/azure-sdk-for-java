// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.base;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.typespec.json.implementation.jackson.core.*;
import com.typespec.json.implementation.jackson.core.exc.InputCoercionException;
import com.typespec.json.implementation.jackson.core.io.JsonEOFException;
import com.typespec.json.implementation.jackson.core.io.NumberInput;
import com.typespec.json.implementation.jackson.core.util.ByteArrayBuilder;
import com.typespec.json.implementation.jackson.core.util.VersionUtil;

import static com.typespec.json.implementation.jackson.core.JsonTokenId.*;

/**
 * Intermediate base class used by all Jackson {@link JsonParser}
 * implementations, but does not add any additional fields that depend
 * on particular method of obtaining input.
 *<p>
 * Note that 'minimal' here mostly refers to minimal number of fields
 * (size) and functionality that is specific to certain types
 * of parser implementations; but not necessarily to number of methods.
 */
public abstract class ParserMinimalBase extends JsonParser
{
    // Control chars:
    protected final static int INT_TAB = '\t';
    protected final static int INT_LF = '\n';
    protected final static int INT_CR = '\r';
    protected final static int INT_SPACE = 0x0020;

    // Markup
    protected final static int INT_LBRACKET = '[';
    protected final static int INT_RBRACKET = ']';
    protected final static int INT_LCURLY = '{';
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
    protected final static int INT_E = 'E';

    protected final static char CHAR_NULL = '\0';

    /**
     * @since 2.9
     */
    protected final static byte[] NO_BYTES = new byte[0];

    /**
     * @since 2.9
     */
    protected final static int[] NO_INTS = new int[0];
    
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

    protected final static long MIN_INT_L = (long) Integer.MIN_VALUE;
    protected final static long MAX_INT_L = (long) Integer.MAX_VALUE;

    // These are not very accurate, but have to do... (for bounds checks)

    protected final static double MIN_LONG_D = (double) Long.MIN_VALUE;
    protected final static double MAX_LONG_D = (double) Long.MAX_VALUE;

    protected final static double MIN_INT_D = (double) Integer.MIN_VALUE;
    protected final static double MAX_INT_D = (double) Integer.MAX_VALUE;

    /*
    /**********************************************************
    /* Misc other constants
    /**********************************************************
     */

    /**
     * Maximum number of characters to include in token reported
     * as part of error messages.
     *
     * @since 2.9
     */
    protected final static int MAX_ERROR_TOKEN_LENGTH = 256;

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

    /**
     * Last cleared token, if any: that is, value that was in
     * effect when {@link #clearCurrentToken} was called.
     */
    protected JsonToken _lastClearedToken;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    protected ParserMinimalBase() { }
    protected ParserMinimalBase(int features) { super(features); }

    // NOTE: had base impl in 2.3 and before; but shouldn't
    // public abstract Version version();

    /*
    /**********************************************************
    /* Configuration overrides if any
    /**********************************************************
     */

    // from base class:

    //public void enableFeature(Feature f)
    //public void disableFeature(Feature f)
    //public void setFeature(Feature f, boolean state)
    //public boolean isFeatureEnabled(Feature f)

    /*
    /**********************************************************
    /* JsonParser impl
    /**********************************************************
     */

    @Override public abstract JsonToken nextToken() throws IOException;

    @Override public JsonToken currentToken() { return _currToken; }
    @Override public int currentTokenId() {
        final JsonToken t = _currToken;
        return (t == null) ? JsonTokenId.ID_NO_TOKEN : t.id();
    }

    @Override public JsonToken getCurrentToken() { return _currToken; }

    @Deprecated
    @Override public int getCurrentTokenId() {
        final JsonToken t = _currToken;
        return (t == null) ? JsonTokenId.ID_NO_TOKEN : t.id();
    }

    @Override public boolean hasCurrentToken() { return _currToken != null; }
    @Override public boolean hasTokenId(int id) {
        final JsonToken t = _currToken;
        if (t == null) {
            return (JsonTokenId.ID_NO_TOKEN == id);
        }
        return t.id() == id;
    }

    @Override public boolean hasToken(JsonToken t) {
        return (_currToken == t);
    }
    
    @Override public boolean isExpectedStartArrayToken() { return _currToken == JsonToken.START_ARRAY; }
    @Override public boolean isExpectedStartObjectToken() { return _currToken == JsonToken.START_OBJECT; }
    @Override public boolean isExpectedNumberIntToken() { return _currToken == JsonToken.VALUE_NUMBER_INT; }

    @Override
    public JsonToken nextValue() throws IOException {
        // Implementation should be as trivial as follows; only needs to change if
        // we are to skip other tokens (for example, if comments were exposed as tokens)
        JsonToken t = nextToken();
        if (t == JsonToken.FIELD_NAME) {
            t = nextToken();
        }
        return t;
    }

    @Override
    public JsonParser skipChildren() throws IOException
    {
        if (_currToken != JsonToken.START_OBJECT
            && _currToken != JsonToken.START_ARRAY) {
            return this;
        }
        int open = 1;

        // Since proper matching of start/end markers is handled
        // by nextToken(), we'll just count nesting levels here
        while (true) {
            JsonToken t = nextToken();
            if (t == null) {
                _handleEOF();
                /* given constraints, above should never return;
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
    protected abstract void _handleEOF() throws JsonParseException;

    //public JsonToken getCurrentToken()
    //public boolean hasCurrentToken()

    @Override public abstract String getCurrentName() throws IOException;
    @Override public abstract void close() throws IOException;
    @Override public abstract boolean isClosed();

    @Override public abstract JsonStreamContext getParsingContext();

//    public abstract JsonLocation getTokenLocation();

//   public abstract JsonLocation getCurrentLocation();

    /*
    /**********************************************************
    /* Public API, token state overrides
    /**********************************************************
     */

    @Override public void clearCurrentToken() {
        if (_currToken != null) {
            _lastClearedToken = _currToken;
            _currToken = null;
        }
    }

    @Override public JsonToken getLastClearedToken() { return _lastClearedToken; }

    @Override public abstract void overrideCurrentName(String name);
    
    /*
    /**********************************************************
    /* Public API, access to token information, text
    /**********************************************************
     */

    @Override public abstract String getText() throws IOException;
    @Override public abstract char[] getTextCharacters() throws IOException;
    @Override public abstract boolean hasTextCharacters();
    @Override public abstract int getTextLength() throws IOException;
    @Override public abstract int getTextOffset() throws IOException;  

    /*
    /**********************************************************
    /* Public API, access to token information, binary
    /**********************************************************
     */

    @Override public abstract byte[] getBinaryValue(Base64Variant b64variant) throws IOException;

    /*
    /**********************************************************
    /* Public API, access with conversion/coercion
    /**********************************************************
     */

    @Override
    public boolean getValueAsBoolean(boolean defaultValue) throws IOException
    {
        JsonToken t = _currToken;
        if (t != null) {
            switch (t.id()) {
            case ID_STRING:
                String str = getText().trim();
                if ("true".equals(str)) {
                    return true;
                }
                if ("false".equals(str)) {
                    return false;
                }
                if (_hasTextualNull(str)) {
                    return false;
                }
                break;
            case ID_NUMBER_INT:
                return getIntValue() != 0;
            case ID_TRUE:
                return true;
            case ID_FALSE:
            case ID_NULL:
                return false;
            case ID_EMBEDDED_OBJECT:
                Object value = getEmbeddedObject();
                if (value instanceof Boolean) {
                    return (Boolean) value;
                }
                break;
            default:
            }
        }
        return defaultValue;
    }

    @Override
    public int getValueAsInt() throws IOException
    {
        JsonToken t = _currToken;
        if ((t == JsonToken.VALUE_NUMBER_INT) || (t == JsonToken.VALUE_NUMBER_FLOAT)) {
            return getIntValue();
        }
        return getValueAsInt(0);
    }

    @Override
    public int getValueAsInt(int defaultValue) throws IOException
    {
        JsonToken t = _currToken;
        if ((t == JsonToken.VALUE_NUMBER_INT) || (t == JsonToken.VALUE_NUMBER_FLOAT)) {
            return getIntValue();
        }
        if (t != null) {
            switch (t.id()) {
            case ID_STRING:
                String str = getText();
                if (_hasTextualNull(str)) {
                    return 0;
                }
                return NumberInput.parseAsInt(str, defaultValue);
            case ID_TRUE:
                return 1;
            case ID_FALSE:
                return 0;
            case ID_NULL:
                return 0;
            case ID_EMBEDDED_OBJECT:
                Object value = getEmbeddedObject();
                if (value instanceof Number) {
                    return ((Number) value).intValue();
                }
            }
        }
        return defaultValue;
    }

    @Override
    public long getValueAsLong() throws IOException
    {
        JsonToken t = _currToken;
        if ((t == JsonToken.VALUE_NUMBER_INT) || (t == JsonToken.VALUE_NUMBER_FLOAT)) {
            return getLongValue();
        }
        return getValueAsLong(0L);
    }
    
    @Override
    public long getValueAsLong(long defaultValue) throws IOException
    {
        JsonToken t = _currToken;
        if ((t == JsonToken.VALUE_NUMBER_INT) || (t == JsonToken.VALUE_NUMBER_FLOAT)) {
            return getLongValue();
        }
        if (t != null) {
            switch (t.id()) {
            case ID_STRING:
                String str = getText();
                if (_hasTextualNull(str)) {
                    return 0L;
                }
                return NumberInput.parseAsLong(str, defaultValue);
            case ID_TRUE:
                return 1L;
            case ID_FALSE:
            case ID_NULL:
                return 0L;
            case ID_EMBEDDED_OBJECT:
                Object value = getEmbeddedObject();
                if (value instanceof Number) {
                    return ((Number) value).longValue();
                }
            }
        }
        return defaultValue;
    }

    @Override
    public double getValueAsDouble(double defaultValue) throws IOException
    {
        JsonToken t = _currToken;
        if (t != null) {
            switch (t.id()) {
            case ID_STRING:
                String str = getText();
                if (_hasTextualNull(str)) {
                    return 0L;
                }
                return NumberInput.parseAsDouble(str, defaultValue);
            case ID_NUMBER_INT:
            case ID_NUMBER_FLOAT:
                return getDoubleValue();
            case ID_TRUE:
                return 1.0;
            case ID_FALSE:
            case ID_NULL:
                return 0.0;
            case ID_EMBEDDED_OBJECT:
                Object value = this.getEmbeddedObject();
                if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                }
            }
        }
        return defaultValue;
    }

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
            return getCurrentName();
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
    protected void _decodeBase64(String str, ByteArrayBuilder builder, Base64Variant b64variant) throws IOException
    {
        try {
            b64variant.decode(str, builder);
        } catch (IllegalArgumentException e) {
            _reportError(e.getMessage());
        }
    }

    /*
    /**********************************************************
    /* Coercion helper methods (overridable)
    /**********************************************************
     */

    /**
     * Helper method used to determine whether we are currently pointing to
     * a String value of "null" (NOT a null token); and, if so, that parser
     * is to recognize and return it similar to if it was real null token.
     *<p>
     * Default implementation accepts exact string {@code "null"} and nothing else
     *
     * @param value String value to check
     *
     * @return True if given value contains "null equivalent" String value (for
     *   content this parser handles).
     *
     * @since 2.3
     */
    protected boolean _hasTextualNull(String value) { return "null".equals(value); }

    /*
    /**********************************************************
    /* Error reporting
    /**********************************************************
     */

    protected void reportUnexpectedNumberChar(int ch, String comment) throws JsonParseException {
        String msg = String.format("Unexpected character (%s) in numeric value", _getCharDesc(ch));
        if (comment != null) {
            msg += ": "+comment;
        }
        _reportError(msg);
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
        _reportError("Invalid numeric value: "+msg);
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
                _longIntegerDesc(numDesc), Integer.MIN_VALUE, Integer.MAX_VALUE),
                inputType, Integer.TYPE);
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
                _longIntegerDesc(numDesc), Long.MIN_VALUE, Long.MAX_VALUE),
                inputType, Long.TYPE);
    }

    // @since 2.10
    protected void _reportInputCoercion(String msg, JsonToken inputType, Class<?> targetType)
            throws InputCoercionException {
        throw new InputCoercionException(this, msg, inputType, targetType);
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

    protected void _reportUnexpectedChar(int ch, String comment) throws JsonParseException
    {
        if (ch < 0) { // sanity check
            _reportInvalidEOF();
        }
        String msg = String.format("Unexpected character (%s)", _getCharDesc(ch));
        if (comment != null) {
            msg += ": "+comment;
        }
        _reportError(msg);
    }

    protected void _reportInvalidEOF() throws JsonParseException {
        _reportInvalidEOF(" in "+_currToken, _currToken);
    }

    // @since 2.8
    protected void _reportInvalidEOFInValue(JsonToken type) throws JsonParseException {
        String msg;
        if (type == JsonToken.VALUE_STRING) {
            msg = " in a String value";
        } else if ((type == JsonToken.VALUE_NUMBER_INT)
                || (type == JsonToken.VALUE_NUMBER_FLOAT)) {
            msg = " in a Number value";
        } else {
            msg = " in a value";
        }
        _reportInvalidEOF(msg, type);
    }

    // @since 2.8
    protected void _reportInvalidEOF(String msg, JsonToken currToken) throws JsonParseException {
        throw new JsonEOFException(this, currToken, "Unexpected end-of-input"+msg);
    }

    /**
     * @deprecated Since 2.8 use {@link #_reportInvalidEOF(String, JsonToken)} instead
     *
     * @throws JsonParseException Exception that describes problem with end-of-content within value
     */
    @Deprecated // since 2.8
    protected void _reportInvalidEOFInValue() throws JsonParseException {
        _reportInvalidEOF(" in a value");
    }
    
    /**
     * @param msg Addition message snippet to append to base exception message
     * @deprecated Since 2.8 use {@link #_reportInvalidEOF(String, JsonToken)} instead
     *
     * @throws JsonParseException Exception that describes problem with end-of-content within value
     */
    @Deprecated // since 2.8
    protected void _reportInvalidEOF(String msg) throws JsonParseException {
        throw new JsonEOFException(this, null, "Unexpected end-of-input"+msg);
    }
    
    protected void _reportMissingRootWS(int ch) throws JsonParseException {
        _reportUnexpectedChar(ch, "Expected space separating root-level values");
    }
    
    protected void _throwInvalidSpace(int i) throws JsonParseException {
        char c = (char) i;
        String msg = "Illegal character ("+_getCharDesc(c)+"): only regular white space (\\r, \\n, \\t) is allowed between tokens";
        _reportError(msg);
    }

    /*
    /**********************************************************
    /* Error reporting, generic
    /**********************************************************
     */

    protected final static String _getCharDesc(int ch)
    {
        char c = (char) ch;
        if (Character.isISOControl(c)) {
            return "(CTRL-CHAR, code "+ch+")";
        }
        if (ch > 255) {
            return "'"+c+"' (code "+ch+" / 0x"+Integer.toHexString(ch)+")";
        }
        return "'"+c+"' (code "+ch+")";
    }

    protected final void _reportError(String msg) throws JsonParseException {
        throw _constructError(msg);
    }

    // @since 2.9
    protected final void _reportError(String msg, Object arg) throws JsonParseException {
        throw _constructError(String.format(msg, arg));
    }

    // @since 2.9
    protected final void _reportError(String msg, Object arg1, Object arg2) throws JsonParseException {
        throw _constructError(String.format(msg, arg1, arg2));
    }

    protected final void _wrapError(String msg, Throwable t) throws JsonParseException {
        throw _constructError(msg, t);
    }

    protected final void _throwInternal() {
        VersionUtil.throwInternal();
    }

    protected final JsonParseException _constructError(String msg, Throwable t) {
        return new JsonParseException(this, msg, t);
    }

    @Deprecated // since 2.11
    protected static byte[] _asciiBytes(String str) {
        byte[] b = new byte[str.length()];
        for (int i = 0, len = str.length(); i < len; ++i) {
            b[i] = (byte) str.charAt(i);
        }
        return b;
    }

    @Deprecated // since 2.11
    protected static String _ascii(byte[] b) {
        try {
            return new String(b, "US-ASCII");
        } catch (IOException e) { // never occurs
            throw new RuntimeException(e);
        }
    }
}
