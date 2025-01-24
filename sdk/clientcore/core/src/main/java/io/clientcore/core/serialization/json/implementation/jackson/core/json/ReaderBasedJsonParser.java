// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package io.clientcore.core.serialization.json.implementation.jackson.core.json;

import io.clientcore.core.serialization.json.implementation.jackson.core.Base64Variant;
import io.clientcore.core.serialization.json.implementation.jackson.core.Base64Variants;
import io.clientcore.core.serialization.json.implementation.jackson.core.JsonParseException;
import io.clientcore.core.serialization.json.implementation.jackson.core.JsonParser;
import io.clientcore.core.serialization.json.implementation.jackson.core.JsonToken;
import io.clientcore.core.serialization.json.implementation.jackson.core.base.ParserMinimalBase;
import io.clientcore.core.serialization.json.implementation.jackson.core.io.CharTypes;
import io.clientcore.core.serialization.json.implementation.jackson.core.io.IOContext;
import io.clientcore.core.serialization.json.implementation.jackson.core.sym.CharsToNameCanonicalizer;
import io.clientcore.core.serialization.json.implementation.jackson.core.util.ByteArrayBuilder;
import io.clientcore.core.serialization.json.implementation.jackson.core.util.TextBuffer;

import java.io.IOException;
import java.io.Reader;

/**
 * This is a concrete implementation of {@link JsonParser}, which is
 * based on a {@link Reader} to handle low-level character
 * conversion tasks.
 */
@SuppressWarnings("fallthrough")
public class ReaderBasedJsonParser extends ParserMinimalBase {
    // Latin1 encoding is not supported, but we do use 8-bit subset for
    // pre-processing task, to simplify first pass, keep it fast.
    protected final static int[] _icLatin1 = CharTypes.getInputCodeLatin1();

    /*
     * /**********************************************************
     * /* Input configuration
     * /**********************************************************
     */

    /**
     * Reader that can be used for reading more content, if one
     * buffer from input source, but in some cases pre-loaded buffer
     * is handed to the parser.
     */
    protected Reader _reader;

    /**
     * Current buffer from which data is read; generally data is read into
     * buffer from input source.
     */
    protected char[] _inputBuffer;

    /**
     * Flag that indicates whether the input buffer is recycable (and
     * needs to be returned to recycler once we are done) or not.
     *<p>
     * If it is not, it also means that parser can NOT modify underlying
     * buffer.
     */
    protected boolean _bufferRecyclable;

    /*
     * /**********************************************************
     * /* Configuration
     * /**********************************************************
     */

    final protected CharsToNameCanonicalizer _symbols;

    final protected int _hashSeed;

    /*
     * /**********************************************************
     * /* Parsing state
     * /**********************************************************
     */

    /**
     * Value of {@link #_inputPtr} at the time when the first character of
     * name token was read. Used for calculating token location when requested;
     * combined with {@link #_currInputProcessed}, may be updated appropriately
     * as needed.
     *
     * @since 2.7
     */
    protected long _nameStartOffset;

    /**
     * @since 2.7
     */
    protected int _nameStartRow;

    /**
     * @since 2.7
     */
    protected int _nameStartCol;

    /*
     * /**********************************************************
     * /* Life-cycle
     * /**********************************************************
     */

    /**
     * Constructor called when input comes as a {@link Reader}, and buffer allocation
     * can be done using default mechanism.
     *
     * @param ctxt I/O context to use
     * @param features Standard stream read features enabled
     * @param r Reader used for reading actual content, if any; {@code null} if none
     * @param st Name canonicalizer to use
     */
    public ReaderBasedJsonParser(IOContext ctxt, int features, Reader r, CharsToNameCanonicalizer st) {
        super(ctxt, features);
        _reader = r;
        _inputBuffer = ctxt.allocTokenBuffer();
        _inputPtr = 0;
        _inputEnd = 0;
        _symbols = st;
        _hashSeed = st.hashSeed();
        _bufferRecyclable = true;
    }

    /*
     * /**********************************************************
     * /* Base method defs, overrides
     * /**********************************************************
     */

    protected char getNextChar(String eofMsg, JsonToken forToken) throws IOException {
        if (_inputPtr >= _inputEnd) {
            if (!_loadMore()) {
                _reportInvalidEOF(eofMsg, forToken);
            }
        }
        return _inputBuffer[_inputPtr++];
    }

    @Override
    protected void _closeInput() {
        if (_reader != null) {
            _reader = null;
        }
    }

    /**
     * Method called to release internal buffers owned by the base
     * reader. This may be called along with {@link #_closeInput} (for
     * example, when explicitly closing this reader instance), or
     * separately (if need be).
     */
    @Override
    protected void _releaseBuffers() throws IOException {
        super._releaseBuffers();
        // merge new symbols, if any
        _symbols.release();
        // and release buffers, if they are recyclable ones
        if (_bufferRecyclable) {
            char[] buf = _inputBuffer;
            if (buf != null) {
                _inputBuffer = null;
                _ioContext.releaseTokenBuffer(buf);
            }
        }
    }

    /*
     * /**********************************************************
     * /* Low-level access, supporting
     * /**********************************************************
     */

    protected void _loadMoreGuaranteed() throws IOException {
        if (!_loadMore()) {
            _reportInvalidEOF();
        }
    }

    protected boolean _loadMore() throws IOException {
        if (_reader != null) {
            int count = _reader.read(_inputBuffer, 0, _inputBuffer.length);
            if (count > 0) {
                final int bufSize = _inputEnd;
                _currInputProcessed += bufSize;
                _currInputRowStart -= bufSize;

                // 26-Nov-2015, tatu: Since name-offset requires it too, must offset
                // this increase to avoid "moving" name-offset, resulting most likely
                // in negative value, which is fine as combine value remains unchanged.
                _nameStartOffset -= bufSize;

                _inputPtr = 0;
                _inputEnd = count;

                return true;
            }
            // End of input
            _closeInput();
            // Should never return 0, so let's fail
            if (count == 0) {
                throw new IOException("Reader returned 0 characters when trying to read " + _inputEnd);
            }
        }
        return false;
    }

    /*
     * /**********************************************************
     * /* Public API, data access
     * /**********************************************************
     */

    /**
     * Method for accessing textual representation of the current event;
     * if no current event (before first call to {@link #nextToken}, or
     * after encountering end-of-input), returns null.
     * Method can be called for any event.
     */
    @Override
    public final String getText() throws IOException {
        if (_currToken == JsonToken.VALUE_STRING) {
            if (_tokenIncomplete) {
                _tokenIncomplete = false;
                _finishString(); // only strings can be incomplete
            }
            return _textBuffer.contentsAsString();
        }
        return _getText2(_currToken);
    }

    // // // Let's override default impls for improved performance

    // @since 2.1
    @Override
    public final String getValueAsString() throws IOException {
        if (_currToken == JsonToken.VALUE_STRING) {
            if (_tokenIncomplete) {
                _tokenIncomplete = false;
                _finishString(); // only strings can be incomplete
            }
            return _textBuffer.contentsAsString();
        }
        if (_currToken == JsonToken.FIELD_NAME) {
            return getCurrentName();
        }
        if (_currToken == null || _currToken == JsonToken.VALUE_NULL || !_currToken.isScalarValue()) {
            return null;
        }
        return getText();
    }

    @Override
    public byte[] getBinaryValue() throws IOException {
        if ((_currToken == JsonToken.VALUE_EMBEDDED_OBJECT) && (_binaryValue != null)) {
            return _binaryValue;
        }
        if (_currToken != JsonToken.VALUE_STRING) {
            _reportError("Current token (" + _currToken
                + ") not VALUE_STRING or VALUE_EMBEDDED_OBJECT, can not access as binary");
        }
        // To ensure that we won't see inconsistent data, better clear up state
        if (_tokenIncomplete) {
            try {
                _binaryValue = _decodeBase64();
            } catch (IllegalArgumentException iae) {
                throw _constructError("Failed to decode VALUE_STRING as base64 (" + Base64Variants.getDefaultVariant()
                    + "): " + iae.getMessage());
            }
            // let's clear incomplete only now; allows for accessing other textual content in error cases
            _tokenIncomplete = false;
        } else { // may actually require conversion...
            if (_binaryValue == null) {
                ByteArrayBuilder builder = _getByteArrayBuilder();
                _decodeBase64(getText(), builder, Base64Variants.getDefaultVariant());
                _binaryValue = builder.toByteArray();
            }
        }
        return _binaryValue;
    }

    /*
     * /**********************************************************
     * /* Public API, traversal
     * /**********************************************************
     */

    /**
     * @return Next token from the stream, if any found, or null
     *   to indicate end-of-input
     */
    @Override
    public final JsonToken nextToken() throws IOException {
        /*
         * First: field names are special -- we will always tokenize
         * (part of) value along with field name to simplify
         * state handling. If so, can and need to use secondary token:
         */
        if (_currToken == JsonToken.FIELD_NAME) {
            return _nextAfterName();
        }
        // But if we didn't already have a name, and (partially?) decode number,
        // need to ensure no numeric information is leaked
        _numTypesValid = NR_UNKNOWN;
        if (_tokenIncomplete) {
            _skipString(); // only strings can be partial
        }
        int i = _skipWSOrEnd();
        if (i < 0) { // end-of-input
            // Should actually close/release things
            // like input source, symbol table and recyclable buffers now.
            close();
            return (_currToken = null);
        }
        // clear any data retained so far
        _binaryValue = null;

        // Closing scope?
        if (i == INT_RBRACKET || i == INT_RCURLY) {
            _closeScope(i);
            return _currToken;
        }

        // Nope: do we then expect a comma?
        if (_parsingContext.expectComma()) {
            i = _skipComma(i);
        }

        /*
         * And should we now have a name? Always true for Object contexts, since
         * the intermediate 'expect-value' state is never retained.
         */
        boolean inObject = _parsingContext.inObject();
        if (inObject) {
            // First, field name itself:
            _updateNameLocation();
            String name = (i == INT_QUOTE) ? _parseName() : _handleOddName(i);
            _parsingContext.setCurrentName(name);
            _currToken = JsonToken.FIELD_NAME;
            i = _skipColon();
        }
        _updateLocation();

        // Ok: we must have a value... what is it?

        JsonToken t;

        switch (i) {
            case '"':
                _tokenIncomplete = true;
                t = JsonToken.VALUE_STRING;
                break;

            case '[':
                if (!inObject) {
                    _parsingContext = _parsingContext.createChildArrayContext(_tokenInputRow, _tokenInputCol);
                }
                t = JsonToken.START_ARRAY;
                break;

            case '{':
                if (!inObject) {
                    _parsingContext = _parsingContext.createChildObjectContext(_tokenInputRow, _tokenInputCol);
                }
                t = JsonToken.START_OBJECT;
                break;

            case '}':
                // Error: } is not valid at this point; valid closers have
                // been handled earlier
                _reportUnexpectedChar(i, "expected a value");
            case 't':
                _matchTrue();
                t = JsonToken.VALUE_TRUE;
                break;

            case 'f':
                _matchFalse();
                t = JsonToken.VALUE_FALSE;
                break;

            case 'n':
                _matchNull();
                t = JsonToken.VALUE_NULL;
                break;

            case '-':
                /*
                 * Should we have separate handling for plus? Although
                 * it is not allowed per se, it may be erroneously used,
                 * and could be indicate by a more specific error message.
                 */
                t = _parseNegNumber();
                break;

            case '.': // [core#61]]
                t = _parseFloatThatStartsWithPeriod();
                break;

            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                t = _parsePosNumber(i);
                break;

            default:
                t = _handleOddValue(i);
                break;
        }

        if (inObject) {
            _nextToken = t;
            return _currToken;
        }
        _currToken = t;
        return t;
    }

    /*
     * /**********************************************************
     * /* Internal methods, number parsing
     * /**********************************************************
     */

    // @since 2.11, [core#611]
    protected final JsonToken _parseFloatThatStartsWithPeriod() throws IOException {
        return _handleOddValue('.');
    }

    /**
     * Initial parsing method for number values. It needs to be able
     * to parse enough input to be able to determine whether the
     * value is to be considered a simple integer value, or a more
     * generic decimal value: latter of which needs to be expressed
     * as a floating point number. The basic rule is that if the number
     * has no fractional or exponential part, it is an integer; otherwise
     * a floating point number.
     *<p>
     * Because much of input has to be processed in any case, no partial
     * parsing is done: all input text will be stored for further
     * processing. However, actual numeric value conversion will be
     * deferred, since it is usually the most complicated and costliest
     * part of processing.
     *
     * @param ch The first non-null digit character of the number to parse
     *
     * @return Type of token decoded, usually {@link JsonToken#VALUE_NUMBER_INT}
     *    or {@link JsonToken#VALUE_NUMBER_FLOAT}
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    protected final JsonToken _parsePosNumber(int ch) throws IOException {
        /*
         * Although we will always be complete with respect to textual
         * representation (that is, all characters will be parsed),
         * actual conversion to a number is deferred. Thus, need to
         * note that no representations are valid yet
         */
        int ptr = _inputPtr;
        int startPtr = ptr - 1; // to include digit already read
        final int inputLen = _inputEnd;

        // One special case, leading zero(es):
        if (ch == INT_0) {
            return _parseNumber2(false, startPtr);
        }

        /*
         * First, let's see if the whole number is contained within
         * the input buffer unsplit. This should be the common case;
         * and to simplify processing, we will just reparse contents
         * in the alternative case (number split on buffer boundary)
         */

        int intLen = 1; // already got one

        // First let's get the obligatory integer part:
        while (true) {
            if (ptr >= inputLen) {
                _inputPtr = startPtr;
                return _parseNumber2(false, startPtr);
            }
            ch = _inputBuffer[ptr++];
            if (ch < INT_0 || ch > INT_9) {
                break;
            }
            ++intLen;
        }
        if (ch == INT_PERIOD || ch == INT_e || ch == INT_E) {
            _inputPtr = ptr;
            return _parseFloat(ch, startPtr, ptr, false, intLen);
        }
        // Got it all: let's add to text buffer for parsing, access
        --ptr; // need to push back following separator
        _inputPtr = ptr;
        // As per #105, need separating space between root values; check here
        if (_parsingContext.inRoot()) {
            _verifyRootSpace(ch);
        }
        int len = ptr - startPtr;
        _textBuffer.resetWithShared(_inputBuffer, startPtr, len);
        return resetInt(false, intLen);
    }

    private JsonToken _parseFloat(int ch, int startPtr, int ptr, boolean neg, int intLen) throws IOException {
        final int inputLen = _inputEnd;
        int fractLen = 0;

        // And then see if we get other parts
        if (ch == '.') { // yes, fraction
            while (true) {
                if (ptr >= inputLen) {
                    return _parseNumber2(neg, startPtr);
                }
                ch = _inputBuffer[ptr++];
                if (ch < INT_0 || ch > INT_9) {
                    break;
                }
                ++fractLen;
            }
            // must be followed by sequence of ints, one minimum
            if (fractLen == 0) {
                reportUnexpectedNumberChar(ch, "Decimal point not followed by a digit");
            }
        }
        int expLen = 0;
        if (ch == 'e' || ch == 'E') { // and/or exponent
            if (ptr >= inputLen) {
                _inputPtr = startPtr;
                return _parseNumber2(neg, startPtr);
            }
            // Sign indicator?
            ch = _inputBuffer[ptr++];
            if (ch == INT_MINUS || ch == INT_PLUS) { // yup, skip for now
                if (ptr >= inputLen) {
                    _inputPtr = startPtr;
                    return _parseNumber2(neg, startPtr);
                }
                ch = _inputBuffer[ptr++];
            }
            while (ch <= INT_9 && ch >= INT_0) {
                ++expLen;
                if (ptr >= inputLen) {
                    _inputPtr = startPtr;
                    return _parseNumber2(neg, startPtr);
                }
                ch = _inputBuffer[ptr++];
            }
            // must be followed by sequence of ints, one minimum
            if (expLen == 0) {
                reportUnexpectedNumberChar(ch, "Exponent indicator not followed by a digit");
            }
        }
        --ptr; // need to push back following separator
        _inputPtr = ptr;
        // As per #105, need separating space between root values; check here
        if (_parsingContext.inRoot()) {
            _verifyRootSpace(ch);
        }
        int len = ptr - startPtr;
        _textBuffer.resetWithShared(_inputBuffer, startPtr, len);
        // And there we have it!
        return resetFloat(neg, intLen, fractLen, expLen);
    }

    protected final JsonToken _parseNegNumber() throws IOException {
        int ptr = _inputPtr;
        int startPtr = ptr - 1; // to include sign/digit already read
        final int inputLen = _inputEnd;

        if (ptr >= inputLen) {
            return _parseNumber2(true, startPtr);
        }
        int ch = _inputBuffer[ptr++];
        // First check: must have a digit to follow minus sign
        if (ch > INT_9 || ch < INT_0) {
            _inputPtr = ptr;
            return _handleInvalidNumberStart(ch, true);
        }
        // One special case, leading zero(es):
        if (ch == INT_0) {
            return _parseNumber2(true, startPtr);
        }
        int intLen = 1; // already got one

        // First let's get the obligatory integer part:
        while (true) {
            if (ptr >= inputLen) {
                return _parseNumber2(true, startPtr);
            }
            ch = _inputBuffer[ptr++];
            if (ch < INT_0 || ch > INT_9) {
                break;
            }
            ++intLen;
        }

        if (ch == INT_PERIOD || ch == INT_e || ch == INT_E) {
            _inputPtr = ptr;
            return _parseFloat(ch, startPtr, ptr, true, intLen);
        }
        --ptr;
        _inputPtr = ptr;
        if (_parsingContext.inRoot()) {
            _verifyRootSpace(ch);
        }
        int len = ptr - startPtr;
        _textBuffer.resetWithShared(_inputBuffer, startPtr, len);
        return resetInt(true, intLen);
    }

    /**
     * Method called to parse a number, when the primary parse
     * method has failed to parse it, due to it being split on
     * buffer boundary. As a result code is very similar, except
     * that it has to explicitly copy contents to the text buffer
     * instead of just sharing the main input buffer.
     *
     * @param neg Whether number being decoded is negative or not
     * @param startPtr Offset in input buffer for the next character of content
     *
     * @return Type of token decoded, usually {@link JsonToken#VALUE_NUMBER_INT}
     *    or {@link JsonToken#VALUE_NUMBER_FLOAT}
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    private JsonToken _parseNumber2(boolean neg, int startPtr) throws IOException {
        _inputPtr = neg ? (startPtr + 1) : startPtr;
        char[] outBuf = _textBuffer.emptyAndGetCurrentSegment();
        int outPtr = 0;

        // Need to prepend sign?
        if (neg) {
            outBuf[outPtr++] = '-';
        }

        // This is the place to do leading-zero check(s) too:
        int intLen = 0;
        char c = (_inputPtr < _inputEnd)
            ? _inputBuffer[_inputPtr++]
            : getNextChar("No digit following minus sign", JsonToken.VALUE_NUMBER_INT);
        if (c == '0') {
            c = _verifyNoLeadingZeroes();
        }
        boolean eof = false;

        // Ok, first the obligatory integer part:
        while (c >= '0' && c <= '9') {
            ++intLen;
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            outBuf[outPtr++] = c;
            if (_inputPtr >= _inputEnd && !_loadMore()) {
                // EOF is legal for main level int values
                c = CHAR_NULL;
                eof = true;
                break;
            }
            c = _inputBuffer[_inputPtr++];
        }
        // Also, integer part is not optional
        if (intLen == 0) {
            return _handleInvalidNumberStart(c, neg);
        }

        int fractLen = 0;
        // And then see if we get other parts
        if (c == '.') { // yes, fraction
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            outBuf[outPtr++] = c;

            while (true) {
                if (_inputPtr >= _inputEnd && !_loadMore()) {
                    eof = true;
                    break;
                }
                c = _inputBuffer[_inputPtr++];
                if (c < INT_0 || c > INT_9) {
                    break;
                }
                ++fractLen;
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outBuf[outPtr++] = c;
            }
            // must be followed by sequence of ints, one minimum
            if (fractLen == 0) {
                reportUnexpectedNumberChar(c, "Decimal point not followed by a digit");
            }
        }

        int expLen = 0;
        if (c == 'e' || c == 'E') { // exponent?
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            outBuf[outPtr++] = c;
            // Not optional, can require that we get one more char
            c = (_inputPtr < _inputEnd)
                ? _inputBuffer[_inputPtr++]
                : getNextChar("expected a digit for number exponent", null);
            // Sign indicator?
            if (c == '-' || c == '+') {
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outBuf[outPtr++] = c;
                // Likewise, non optional:
                c = (_inputPtr < _inputEnd)
                    ? _inputBuffer[_inputPtr++]
                    : getNextChar("expected a digit for number exponent", null);
            }

            while (c <= INT_9 && c >= INT_0) {
                ++expLen;
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outBuf[outPtr++] = c;
                if (_inputPtr >= _inputEnd && !_loadMore()) {
                    eof = true;
                    break;
                }
                c = _inputBuffer[_inputPtr++];
            }
            // must be followed by sequence of ints, one minimum
            if (expLen == 0) {
                reportUnexpectedNumberChar(c, "Exponent indicator not followed by a digit");
            }
        }

        // Ok; unless we hit end-of-input, need to push last char read back
        if (!eof) {
            --_inputPtr;
            if (_parsingContext.inRoot()) {
                _verifyRootSpace(c);
            }
        }
        _textBuffer.setCurrentLength(outPtr);
        // And there we have it!
        return reset(neg, intLen, fractLen, expLen);
    }

    // Method called when we have seen one zero, and want to ensure
    // it is not followed by another
    private char _verifyNoLeadingZeroes() throws IOException {
        // Fast case first:
        if (_inputPtr < _inputEnd) {
            char ch = _inputBuffer[_inputPtr];
            // if not followed by a number (probably '.'); return zero as is, to be included
            if (ch < '0' || ch > '9') {
                return '0';
            }
        }
        // and offline the less common case
        return _verifyNLZ2();
    }

    private char _verifyNLZ2() throws IOException {
        if (_inputPtr >= _inputEnd && !_loadMore()) {
            return '0';
        }
        char ch = _inputBuffer[_inputPtr];
        if (ch < '0' || ch > '9') {
            return '0';
        }
        reportInvalidNumber();
        return ch;
    }

    // Method called if expected numeric value (due to leading sign) does not
    // look like a number
    protected JsonToken _handleInvalidNumberStart(int ch, boolean negative) throws IOException {
        if (ch == 'I') {
            if (_inputPtr >= _inputEnd) {
                if (!_loadMore()) {
                    _reportInvalidEOFInValue();
                }
            }
            ch = _inputBuffer[_inputPtr++];
            if (ch == 'N') {
                String match = negative ? "-INF" : "+INF";
                _matchToken(match, 3);
                if (Feature.ALLOW_NON_NUMERIC_NUMBERS.enabledIn(_features)) {
                    return resetAsNaN(match, negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
                }
                _reportError(
                    "Non-standard token '" + match + "': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
            } else if (ch == 'n') {
                String match = negative ? "-Infinity" : "+Infinity";
                _matchToken(match, 3);
                if (Feature.ALLOW_NON_NUMERIC_NUMBERS.enabledIn(_features)) {
                    return resetAsNaN(match, negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
                }
                _reportError(
                    "Non-standard token '" + match + "': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
            }
        }
        reportUnexpectedNumberChar(ch, "expected digit (0-9) to follow minus sign, for valid numeric value");
        return null;
    }

    /**
     * Method called to ensure that a root-value is followed by a space
     * token.
     *<p>
     * NOTE: caller MUST ensure there is at least one character available;
     * and that input pointer is AT given char (not past)
     *
     * @param ch First character of likely white space to skip
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems (invalid white space)
     */
    private void _verifyRootSpace(int ch) throws IOException {
        // caller had pushed it back, before calling; reset
        ++_inputPtr;
        switch (ch) {
            case ' ':
            case '\t':
                return;

            case '\r':
                _skipCR();
                return;

            case '\n':
                ++_currInputRow;
                _currInputRowStart = _inputPtr;
                return;
        }
        _reportUnexpectedChar(ch, "Expected space separating root-level values");
    }

    /*
     * /**********************************************************
     * /* Internal methods, secondary parsing
     * /**********************************************************
     */

    protected final String _parseName() throws IOException {
        // First: let's try to see if we have a simple name: one that does
        // not cross input buffer boundary, and does not contain escape sequences.
        int ptr = _inputPtr;
        int hash = _hashSeed;
        final int[] codes = _icLatin1;

        while (ptr < _inputEnd) {
            int ch = _inputBuffer[ptr];
            if (ch < codes.length && codes[ch] != 0) {
                if (ch == '"') {
                    int start = _inputPtr;
                    _inputPtr = ptr + 1; // to skip the quote
                    return _symbols.findSymbol(_inputBuffer, start, ptr - start, hash);
                }
                break;
            }
            hash = (hash * CharsToNameCanonicalizer.HASH_MULT) + ch;
            ++ptr;
        }
        int start = _inputPtr;
        _inputPtr = ptr;
        return _parseName2(start, hash);
    }

    private String _parseName2(int startPtr, int hash) throws IOException {
        _textBuffer.resetWithShared(_inputBuffer, startPtr, (_inputPtr - startPtr));

        /*
         * Output pointers; calls will also ensure that the buffer is
         * not shared and has room for at least one more char.
         */
        char[] outBuf = _textBuffer.getCurrentSegment();
        int outPtr = _textBuffer.getCurrentSegmentSize();

        while (true) {
            if (_inputPtr >= _inputEnd) {
                if (!_loadMore()) {
                    _reportInvalidEOF(" in field name", JsonToken.FIELD_NAME);
                }
            }
            char c = _inputBuffer[_inputPtr++];
            int i = c;
            if (i <= INT_BACKSLASH) {
                if (i == INT_BACKSLASH) {
                    /*
                     * Although chars outside of BMP are to be escaped as
                     * an UTF-16 surrogate pair, does that affect decoding?
                     * For now let's assume it does not.
                     */
                    c = _decodeEscaped();
                } else if (i <= ParserMinimalBase.INT_QUOTE) {
                    if (i == ParserMinimalBase.INT_QUOTE) {
                        break;
                    }
                    if (i < INT_SPACE) {
                        _throwUnquotedSpace(i, "name");
                    }
                }
            }
            hash = (hash * CharsToNameCanonicalizer.HASH_MULT) + c;
            // Ok, let's add char to output:
            outBuf[outPtr++] = c;

            // Need more room?
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
        }
        _textBuffer.setCurrentLength(outPtr);
        {
            TextBuffer tb = _textBuffer;
            char[] buf = tb.getTextBuffer();
            int start = tb.getTextOffset();
            int len = tb.size();
            return _symbols.findSymbol(buf, start, len, hash);
        }
    }

    /**
     * Method called when we see non-white space character other
     * than double quote, when expecting a field name.
     * In standard mode will just throw an expection; but
     * in non-standard modes may be able to parse name.
     *
     * @param i First undecoded character of possible "odd name" to decode
     *
     * @return Name decoded, if allowed and successful
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems (invalid name)
     */
    protected String _handleOddName(int i) throws IOException {
        _reportUnexpectedChar(i, "was expecting double-quote to start field name");
        return null; // Never reached as _reportUnexpectedChar throws an exception
    }

    /**
     * Method for handling cases where first non-space character
     * of an expected value token is not legal for standard JSON content.
     *
     * @param i First undecoded character of possible "odd value" to decode
     *
     * @return Type of value decoded, if allowed and successful
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems (invalid white space)
     */
    protected JsonToken _handleOddValue(int i) throws IOException {
        // Most likely an error, unless we are to allow single-quote-strings
        switch (i) {
            case '\'':
                break;

            case ']':
                /*
                 * 28-Mar-2016: [core#116]: If Feature.ALLOW_MISSING_VALUES is enabled
                 * we may allow "missing values", that is, encountering a trailing
                 * comma or closing marker where value would be expected
                 */
                if (!_parsingContext.inArray()) {
                    break;
                }
                // fall through
            case ',':
                break;

            case 'N':
                _matchToken("NaN", 1);
                if (Feature.ALLOW_NON_NUMERIC_NUMBERS.enabledIn(_features)) {
                    return resetAsNaN("NaN", Double.NaN);
                }
                _reportError("Non-standard token 'NaN': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
                break;

            case 'I':
                _matchToken("Infinity", 1);
                if (Feature.ALLOW_NON_NUMERIC_NUMBERS.enabledIn(_features)) {
                    return resetAsNaN("Infinity", Double.POSITIVE_INFINITY);
                }
                _reportError(
                    "Non-standard token 'Infinity': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
                break;

            case '+': // note: '-' is taken as number
                if (_inputPtr >= _inputEnd) {
                    if (!_loadMore()) {
                        _reportInvalidEOFInValue();
                    }
                }
                return _handleInvalidNumberStart(_inputBuffer[_inputPtr++], false);
        }
        // [core#77] Try to decode most likely token
        if (Character.isJavaIdentifierStart(i)) {
            _reportInvalidToken("" + ((char) i));
        }
        // but if it doesn't look like a token:
        _reportUnexpectedChar(i, "expected a valid value " + _validJsonValueList());
        return null;
    }

    @Override
    protected final void _finishString() throws IOException {
        /*
         * First: let's try to see if we have simple String value: one
         * that does not cross input buffer boundary, and does not
         * contain escape sequences.
         */
        int ptr = _inputPtr;
        final int inputLen = _inputEnd;

        if (ptr < inputLen) {
            final int[] codes = _icLatin1;
            final int maxCode = codes.length;

            do {
                int ch = _inputBuffer[ptr];
                if (ch < maxCode && codes[ch] != 0) {
                    if (ch == '"') {
                        _textBuffer.resetWithShared(_inputBuffer, _inputPtr, (ptr - _inputPtr));
                        _inputPtr = ptr + 1;
                        // Yes, we got it all
                        return;
                    }
                    break;
                }
                ++ptr;
            } while (ptr < inputLen);
        }

        // Either ran out of input, or bumped into an escape sequence...
        _textBuffer.resetWithCopy(_inputBuffer, _inputPtr, (ptr - _inputPtr));
        _inputPtr = ptr;
        _finishString2();
    }

    protected void _finishString2() throws IOException {
        char[] outBuf = _textBuffer.getCurrentSegment();
        int outPtr = _textBuffer.getCurrentSegmentSize();
        final int[] codes = _icLatin1;
        final int maxCode = codes.length;

        while (true) {
            if (_inputPtr >= _inputEnd) {
                if (!_loadMore()) {
                    _reportInvalidEOF(": was expecting closing quote for a string value", JsonToken.VALUE_STRING);
                }
            }
            char c = _inputBuffer[_inputPtr++];
            int i = c;
            if (i < maxCode && codes[i] != 0) {
                if (i == INT_QUOTE) {
                    break;
                } else if (i == INT_BACKSLASH) {
                    /*
                     * Although chars outside of BMP are to be escaped as
                     * an UTF-16 surrogate pair, does that affect decoding?
                     * For now let's assume it does not.
                     */
                    c = _decodeEscaped();
                } else if (i < INT_SPACE) {
                    _throwUnquotedSpace(i, "string value");
                } // anything else?
            }
            // Need more room?
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            // Ok, let's add char to output:
            outBuf[outPtr++] = c;
        }
        _textBuffer.setCurrentLength(outPtr);
    }

    /**
     * Method called to skim through rest of unparsed String value,
     * if it is not needed. This can be done bit faster if contents
     * need not be stored for future access.
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems (invalid white space)
     */
    protected final void _skipString() throws IOException {
        _tokenIncomplete = false;

        int inPtr = _inputPtr;
        int inLen = _inputEnd;
        char[] inBuf = _inputBuffer;

        while (true) {
            if (inPtr >= inLen) {
                _inputPtr = inPtr;
                if (!_loadMore()) {
                    _reportInvalidEOF(": was expecting closing quote for a string value", JsonToken.VALUE_STRING);
                }
                inPtr = _inputPtr;
                inLen = _inputEnd;
            }
            int i = inBuf[inPtr++];
            if (i <= INT_BACKSLASH) {
                if (i == INT_BACKSLASH) {
                    // Although chars outside of BMP are to be escaped as an UTF-16 surrogate pair,
                    // does that affect decoding? For now let's assume it does not.
                    _inputPtr = inPtr;
                    /* c = */ _decodeEscaped();
                    inPtr = _inputPtr;
                    inLen = _inputEnd;
                } else if (i <= INT_QUOTE) {
                    if (i == INT_QUOTE) {
                        _inputPtr = inPtr;
                        break;
                    }
                    if (i < INT_SPACE) {
                        _inputPtr = inPtr;
                        _throwUnquotedSpace(i, "string value");
                    }
                }
            }
        }
    }

    /*
     * /**********************************************************
     * /* Internal methods, other parsing
     * /**********************************************************
     */

    // We actually need to check the character value here
    // (to see if we have \n following \r).
    protected final void _skipCR() throws IOException {
        if (_inputPtr < _inputEnd || _loadMore()) {
            if (_inputBuffer[_inputPtr] == '\n') {
                ++_inputPtr;
            }
        }
        ++_currInputRow;
        _currInputRowStart = _inputPtr;
    }

    private int _skipColon() throws IOException {
        if ((_inputPtr + 4) >= _inputEnd) {
            return _skipColon2(false);
        }
        char c = _inputBuffer[_inputPtr];
        if (c == ':') { // common case, no leading space
            int i = _inputBuffer[++_inputPtr];
            if (i > INT_SPACE) { // nor trailing
                if (i == INT_SLASH || i == INT_HASH) {
                    return _skipColon2(true);
                }
                ++_inputPtr;
                return i;
            }
            if (i == INT_SPACE || i == INT_TAB) {
                i = _inputBuffer[++_inputPtr];
                if (i > INT_SPACE) {
                    if (i == INT_SLASH || i == INT_HASH) {
                        return _skipColon2(true);
                    }
                    ++_inputPtr;
                    return i;
                }
            }
            return _skipColon2(true); // true -> skipped colon
        }
        if (c == ' ' || c == '\t') {
            c = _inputBuffer[++_inputPtr];
        }
        if (c == ':') {
            int i = _inputBuffer[++_inputPtr];
            if (i > INT_SPACE) {
                if (i == INT_SLASH || i == INT_HASH) {
                    return _skipColon2(true);
                }
                ++_inputPtr;
                return i;
            }
            if (i == INT_SPACE || i == INT_TAB) {
                i = _inputBuffer[++_inputPtr];
                if (i > INT_SPACE) {
                    if (i == INT_SLASH || i == INT_HASH) {
                        return _skipColon2(true);
                    }
                    ++_inputPtr;
                    return i;
                }
            }
            return _skipColon2(true);
        }
        return _skipColon2(false);
    }

    private int _skipColon2(boolean gotColon) throws IOException {
        while (_inputPtr < _inputEnd || _loadMore()) {
            int i = _inputBuffer[_inputPtr++];
            if (i > INT_SPACE) {
                if (i == INT_SLASH) {
                    _skipComment();
                    continue;
                }
                if (gotColon) {
                    return i;
                }
                if (i != INT_COLON) {
                    _reportUnexpectedChar(i, "was expecting a colon to separate field name and value");
                }
                gotColon = true;
                continue;
            }
            if (i < INT_SPACE) {
                if (i == INT_LF) {
                    ++_currInputRow;
                    _currInputRowStart = _inputPtr;
                } else if (i == INT_CR) {
                    _skipCR();
                } else if (i != INT_TAB) {
                    _throwInvalidSpace(i);
                }
            }
        }
        _reportInvalidEOF(" within/between " + _parsingContext.typeDesc() + " entries", null);
        return -1;
    }

    // Primary loop: no reloading, comment handling
    private int _skipComma(int i) throws IOException {
        if (i != INT_COMMA) {
            _reportUnexpectedChar(i, "was expecting comma to separate " + _parsingContext.typeDesc() + " entries");
        }
        while (_inputPtr < _inputEnd) {
            i = _inputBuffer[_inputPtr++];
            if (i > INT_SPACE) {
                if (i == INT_SLASH || i == INT_HASH) {
                    --_inputPtr;
                    return _skipAfterComma2();
                }
                return i;
            }
            if (i < INT_SPACE) {
                if (i == INT_LF) {
                    ++_currInputRow;
                    _currInputRowStart = _inputPtr;
                } else if (i == INT_CR) {
                    _skipCR();
                } else if (i != INT_TAB) {
                    _throwInvalidSpace(i);
                }
            }
        }
        return _skipAfterComma2();
    }

    private int _skipAfterComma2() throws IOException {
        while (_inputPtr < _inputEnd || _loadMore()) {
            int i = _inputBuffer[_inputPtr++];
            if (i > INT_SPACE) {
                if (i == INT_SLASH) {
                    _skipComment();
                    continue;
                }
                return i;
            }
            if (i < INT_SPACE) {
                if (i == INT_LF) {
                    ++_currInputRow;
                    _currInputRowStart = _inputPtr;
                } else if (i == INT_CR) {
                    _skipCR();
                } else if (i != INT_TAB) {
                    _throwInvalidSpace(i);
                }
            }
        }
        throw _constructError("Unexpected end-of-input within/between " + _parsingContext.typeDesc() + " entries");
    }

    private int _skipWSOrEnd() throws IOException {
        // Let's handle first character separately since it is likely that
        // it is either non-whitespace; or we have longer run of white space
        if (_inputPtr >= _inputEnd) {
            if (!_loadMore()) {
                return _eofAsNextChar();
            }
        }
        int i = _inputBuffer[_inputPtr++];
        if (i > INT_SPACE) {
            if (i == INT_SLASH || i == INT_HASH) {
                --_inputPtr;
                return _skipWSOrEnd2();
            }
            return i;
        }
        if (i != INT_SPACE) {
            if (i == INT_LF) {
                ++_currInputRow;
                _currInputRowStart = _inputPtr;
            } else if (i == INT_CR) {
                _skipCR();
            } else if (i != INT_TAB) {
                _throwInvalidSpace(i);
            }
        }

        while (_inputPtr < _inputEnd) {
            i = _inputBuffer[_inputPtr++];
            if (i > INT_SPACE) {
                if (i == INT_SLASH || i == INT_HASH) {
                    --_inputPtr;
                    return _skipWSOrEnd2();
                }
                return i;
            }
            if (i != INT_SPACE) {
                if (i == INT_LF) {
                    ++_currInputRow;
                    _currInputRowStart = _inputPtr;
                } else if (i == INT_CR) {
                    _skipCR();
                } else if (i != INT_TAB) {
                    _throwInvalidSpace(i);
                }
            }
        }
        return _skipWSOrEnd2();
    }

    private int _skipWSOrEnd2() throws IOException {
        while (true) {
            if (_inputPtr >= _inputEnd) {
                if (!_loadMore()) { // We ran out of input...
                    return _eofAsNextChar();
                }
            }
            int i = _inputBuffer[_inputPtr++];
            if (i > INT_SPACE) {
                if (i == INT_SLASH) {
                    _skipComment();
                    continue;
                }
                return i;
            } else if (i != INT_SPACE) {
                if (i == INT_LF) {
                    ++_currInputRow;
                    _currInputRowStart = _inputPtr;
                } else if (i == INT_CR) {
                    _skipCR();
                } else if (i != INT_TAB) {
                    _throwInvalidSpace(i);
                }
            }
        }
    }

    private void _skipComment() throws IOException {
        if (!Feature.ALLOW_COMMENTS.enabledIn(_features)) {
            _reportUnexpectedChar('/',
                "maybe a (non-standard) comment? (not recognized as one since Feature 'ALLOW_COMMENTS' not enabled for parser)");
        }
        // First: check which comment (if either) it is:
        if (_inputPtr >= _inputEnd && !_loadMore()) {
            _reportInvalidEOF(" in a comment", null);
        }
        char c = _inputBuffer[_inputPtr++];
        if (c == '/') {
            _skipLine();
        } else if (c == '*') {
            _skipCComment();
        } else {
            _reportUnexpectedChar(c, "was expecting either '*' or '/' for a comment");
        }
    }

    private void _skipCComment() throws IOException {
        // Ok: need the matching '*/'
        while ((_inputPtr < _inputEnd) || _loadMore()) {
            int i = _inputBuffer[_inputPtr++];
            if (i <= '*') {
                if (i == '*') { // end?
                    if ((_inputPtr >= _inputEnd) && !_loadMore()) {
                        break;
                    }
                    if (_inputBuffer[_inputPtr] == INT_SLASH) {
                        ++_inputPtr;
                        return;
                    }
                    continue;
                }
                if (i < INT_SPACE) {
                    if (i == INT_LF) {
                        ++_currInputRow;
                        _currInputRowStart = _inputPtr;
                    } else if (i == INT_CR) {
                        _skipCR();
                    } else if (i != INT_TAB) {
                        _throwInvalidSpace(i);
                    }
                }
            }
        }
        _reportInvalidEOF(" in a comment", null);
    }

    private void _skipLine() throws IOException {
        // Ok: need to find EOF or linefeed
        while ((_inputPtr < _inputEnd) || _loadMore()) {
            int i = _inputBuffer[_inputPtr++];
            if (i < INT_SPACE) {
                if (i == INT_LF) {
                    ++_currInputRow;
                    _currInputRowStart = _inputPtr;
                    break;
                } else if (i == INT_CR) {
                    _skipCR();
                    break;
                } else if (i != INT_TAB) {
                    _throwInvalidSpace(i);
                }
            }
        }
    }

    @Override
    protected char _decodeEscaped() throws IOException {
        if (_inputPtr >= _inputEnd) {
            if (!_loadMore()) {
                _reportInvalidEOF(" in character escape sequence", JsonToken.VALUE_STRING);
            }
        }
        char c = _inputBuffer[_inputPtr++];

        switch ((int) c) {
            // First, ones that are mapped
            case 'b':
                return '\b';

            case 't':
                return '\t';

            case 'n':
                return '\n';

            case 'f':
                return '\f';

            case 'r':
                return '\r';

            // And these are to be returned as they are
            case '"':
            case '/':
            case '\\':
                return c;

            case 'u': // and finally hex-escaped
                break;

            default:
                throw _constructError("Unrecognized character escape " + _getCharDesc(c));
        }

        // Ok, a hex escape. Need 4 characters
        int value = 0;
        for (int i = 0; i < 4; ++i) {
            if (_inputPtr >= _inputEnd) {
                if (!_loadMore()) {
                    _reportInvalidEOF(" in character escape sequence", JsonToken.VALUE_STRING);
                }
            }
            int ch = _inputBuffer[_inputPtr++];
            int digit = CharTypes.charToHex(ch);
            if (digit < 0) {
                _reportUnexpectedChar(ch, "expected a hex-digit for character escape sequence");
            }
            value = (value << 4) | digit;
        }
        return (char) value;
    }

    private void _matchTrue() throws IOException {
        int ptr = _inputPtr;
        if ((ptr + 3) < _inputEnd) {
            final char[] b = _inputBuffer;
            if (b[ptr] == 'r' && b[++ptr] == 'u' && b[++ptr] == 'e') {
                char c = b[++ptr];
                if (c < '0' || c == ']' || c == '}') { // expected/allowed chars
                    _inputPtr = ptr;
                    return;
                }
            }
        }
        // buffer boundary, or problem, offline
        _matchToken("true", 1);
    }

    private void _matchFalse() throws IOException {
        int ptr = _inputPtr;
        if ((ptr + 4) < _inputEnd) {
            final char[] b = _inputBuffer;
            if (b[ptr] == 'a' && b[++ptr] == 'l' && b[++ptr] == 's' && b[++ptr] == 'e') {
                char c = b[++ptr];
                if (c < '0' || c == ']' || c == '}') { // expected/allowed chars
                    _inputPtr = ptr;
                    return;
                }
            }
        }
        // buffer boundary, or problem, offline
        _matchToken("false", 1);
    }

    private void _matchNull() throws IOException {
        int ptr = _inputPtr;
        if ((ptr + 3) < _inputEnd) {
            final char[] b = _inputBuffer;
            if (b[ptr] == 'u' && b[++ptr] == 'l' && b[++ptr] == 'l') {
                char c = b[++ptr];
                if (c < '0' || c == ']' || c == '}') { // expected/allowed chars
                    _inputPtr = ptr;
                    return;
                }
            }
        }
        // buffer boundary, or problem, offline
        _matchToken("null", 1);
    }

    // Helper method for checking whether input matches expected token
    protected final void _matchToken(String matchStr, int i) throws IOException {
        final int len = matchStr.length();
        if ((_inputPtr + len) >= _inputEnd) {
            _matchToken2(matchStr, i);
            return;
        }

        do {
            if (_inputBuffer[_inputPtr] != matchStr.charAt(i)) {
                _reportInvalidToken(matchStr.substring(0, i));
            }
            ++_inputPtr;
        } while (++i < len);
        int ch = _inputBuffer[_inputPtr];
        if (ch >= '0' && ch != ']' && ch != '}') { // expected/allowed chars
            _checkMatchEnd(matchStr, i, ch);
        }
    }

    private void _matchToken2(String matchStr, int i) throws IOException {
        final int len = matchStr.length();
        do {
            if (((_inputPtr >= _inputEnd) && !_loadMore()) || (_inputBuffer[_inputPtr] != matchStr.charAt(i))) {
                _reportInvalidToken(matchStr.substring(0, i));
            }
            ++_inputPtr;
        } while (++i < len);

        // but let's also ensure we either get EOF, or non-alphanum char...
        if (_inputPtr >= _inputEnd && !_loadMore()) {
            return;
        }
        int ch = _inputBuffer[_inputPtr];
        if (ch >= '0' && ch != ']' && ch != '}') { // expected/allowed chars
            _checkMatchEnd(matchStr, i, ch);
        }
    }

    private void _checkMatchEnd(String matchStr, int i, int c) throws IOException {
        // but actually only alphanums are problematic
        char ch = (char) c;
        if (Character.isJavaIdentifierPart(ch)) {
            _reportInvalidToken(matchStr.substring(0, i));
        }
    }

    /**
     * Efficient handling for incremental parsing of base64-encoded
     * textual content.
     *
     * @return Fully decoded value of base64 content
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems (invalid content)
     */
    @SuppressWarnings("resource")
    protected byte[] _decodeBase64() throws IOException {
        Base64Variant b64variant = Base64Variants.getDefaultVariant();
        ByteArrayBuilder builder = _getByteArrayBuilder();

        // main_loop:
        while (true) {
            // first, we'll skip preceding white space, if any
            char ch;
            do {
                if (_inputPtr >= _inputEnd) {
                    _loadMoreGuaranteed();
                }
                ch = _inputBuffer[_inputPtr++];
            } while (ch <= INT_SPACE);
            int bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
                if (ch == '"') { // reached the end, fair and square?
                    return builder.toByteArray();
                }
                bits = _decodeBase64Escape(b64variant, ch, 0);
                if (bits < 0) { // white space to skip
                    continue;
                }
            }
            int decodedData = bits;

            // then second base64 char; can't get padding yet, nor ws

            if (_inputPtr >= _inputEnd) {
                _loadMoreGuaranteed();
            }
            ch = _inputBuffer[_inputPtr++];
            bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
                bits = _decodeBase64Escape(b64variant, ch, 1);
            }
            decodedData = (decodedData << 6) | bits;

            // third base64 char; can be padding, but not ws
            if (_inputPtr >= _inputEnd) {
                _loadMoreGuaranteed();
            }
            ch = _inputBuffer[_inputPtr++];
            bits = b64variant.decodeBase64Char(ch);

            // First branch: can get padding (-> 1 byte)
            if (bits < 0) {
                if (bits != Base64Variant.BASE64_VALUE_PADDING) {
                    // as per [JACKSON-631], could also just be 'missing' padding
                    if (ch == '"') {
                        decodedData >>= 4;
                        builder.append(decodedData);
                        if (b64variant.usesPadding()) {
                            --_inputPtr; // to keep parser state bit more consistent
                            _handleBase64MissingPadding();
                        }
                        return builder.toByteArray();
                    }
                    bits = _decodeBase64Escape(b64variant, ch, 2);
                }
                if (bits == Base64Variant.BASE64_VALUE_PADDING) {
                    // Ok, must get more padding chars, then
                    if (_inputPtr >= _inputEnd) {
                        _loadMoreGuaranteed();
                    }
                    ch = _inputBuffer[_inputPtr++];
                    if (!b64variant.usesPaddingChar(ch)) {
                        if (_decodeBase64Escape(b64variant, ch, 3) != Base64Variant.BASE64_VALUE_PADDING) {
                            throw reportInvalidBase64Char(b64variant, ch, 3,
                                "expected padding character '" + b64variant.getPaddingChar() + "'");
                        }
                    }
                    // Got 12 bits, only need 8, need to shift
                    decodedData >>= 4;
                    builder.append(decodedData);
                    continue;
                }
                // otherwise we got escaped other char, to be processed below
            }
            // Nope, 2 or 3 bytes
            decodedData = (decodedData << 6) | bits;
            // fourth and last base64 char; can be padding, but not ws
            if (_inputPtr >= _inputEnd) {
                _loadMoreGuaranteed();
            }
            ch = _inputBuffer[_inputPtr++];
            bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
                if (bits != Base64Variant.BASE64_VALUE_PADDING) {
                    // as per [JACKSON-631], could also just be 'missing' padding
                    if (ch == '"') {
                        decodedData >>= 2;
                        builder.appendTwoBytes(decodedData);
                        if (b64variant.usesPadding()) {
                            --_inputPtr; // to keep parser state bit more consistent
                            _handleBase64MissingPadding();
                        }
                        return builder.toByteArray();
                    }
                    bits = _decodeBase64Escape(b64variant, ch, 3);
                }
                if (bits == Base64Variant.BASE64_VALUE_PADDING) {
                    // With padding we only get 2 bytes; but we have
                    // to shift it a bit so it is identical to triplet
                    // case with partial output.
                    // 3 chars gives 3x6 == 18 bits, of which 2 are
                    // dummies, need to discard:
                    decodedData >>= 2;
                    builder.appendTwoBytes(decodedData);
                    continue;
                }
                // otherwise we got escaped other char, to be processed below
            }
            // otherwise, our triplet is now complete
            decodedData = (decodedData << 6) | bits;
            builder.appendThreeBytes(decodedData);
        }
    }

    /*
     * /**********************************************************
     * /* Binary access
     * /**********************************************************
     */

    /*
     * /**********************************************************
     * /* Internal methods, location updating (refactored in 2.7)
     * /**********************************************************
     */

    // @since 2.7
    private void _updateLocation() {
        int ptr = _inputPtr;
        _tokenInputRow = _currInputRow;
        _tokenInputCol = ptr - _currInputRowStart;
    }

    // @since 2.7
    private void _updateNameLocation() {
        int ptr = _inputPtr;
        _nameStartOffset = ptr;
        _nameStartRow = _currInputRow;
        _nameStartCol = ptr - _currInputRowStart;
    }

    /*
     * /**********************************************************
     * /* Error reporting
     * /**********************************************************
     */

    protected void _reportInvalidToken(String matchedPart) throws IOException {
        /*
         * Let's just try to find what appears to be the token, using
         * regular Java identifier character rules. It's just a heuristic,
         * nothing fancy here.
         */
        StringBuilder sb = new StringBuilder(matchedPart);
        while ((_inputPtr < _inputEnd) || _loadMore()) {
            char c = _inputBuffer[_inputPtr];
            if (!Character.isJavaIdentifierPart(c)) {
                break;
            }
            ++_inputPtr;
            sb.append(c);
            if (sb.length() >= MAX_ERROR_TOKEN_LENGTH) {
                sb.append("...");
                break;
            }
        }
        throw _constructError(String.format("Unrecognized token '%s': was expecting %s", sb, _validJsonValueList()));
    }

    /*
     * /**********************************************************
     * /* Internal methods, other
     * /**********************************************************
     */

    private void _closeScope(int i) throws JsonParseException {
        if (i == INT_RBRACKET) {
            _updateLocation();
            if (!_parsingContext.inArray()) {
                _reportMismatchedEndMarker(i, '}');
            }
            _parsingContext = _parsingContext.clearAndGetParent();
            _currToken = JsonToken.END_ARRAY;
        }
        if (i == INT_RCURLY) {
            _updateLocation();
            if (!_parsingContext.inObject()) {
                _reportMismatchedEndMarker(i, ']');
            }
            _parsingContext = _parsingContext.clearAndGetParent();
            _currToken = JsonToken.END_OBJECT;
        }
    }
}
