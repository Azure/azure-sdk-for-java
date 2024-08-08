// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.json;

import java.io.*;

import com.azure.json.implementation.jackson.core.*;
import com.azure.json.implementation.jackson.core.exc.StreamConstraintsException;
import com.azure.json.implementation.jackson.core.io.CharTypes;
import com.azure.json.implementation.jackson.core.io.IOContext;
import com.azure.json.implementation.jackson.core.sym.ByteQuadsCanonicalizer;
import com.azure.json.implementation.jackson.core.util.*;

import static com.azure.json.implementation.jackson.core.JsonTokenId.*;

/**
 * This is a concrete implementation of {@link JsonParser}, which is
 * based on a {@link java.io.InputStream} as the input source.
 */
@SuppressWarnings("fallthrough")
public class UTF8StreamJsonParser extends JsonParserBase {
    protected final static byte BYTE_LF = (byte) '\n';

    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    /**
     * Symbol table that contains field names encountered so far
     */
    protected final ByteQuadsCanonicalizer _symbols;

    /*
    /**********************************************************
    /* Parsing state
    /**********************************************************
     */

    /**
     * Temporary buffer used for name parsing.
     */
    protected int[] _quadBuffer = new int[16];

    /**
     * Flag that indicates that the current token has not yet
     * been fully processed, and needs to be finished for
     * some access (or skipped to obtain the next token)
     */
    protected boolean _tokenIncomplete;

    /**
     * Temporary storage for partially parsed name bytes.
     */
    private int _quad1;

    /**
     * Value of {@link #_inputPtr} at the time when the first character of
     * name token was read. Used for calculating token location when requested;
     * combined with {@link #_currInputProcessed}, may be updated appropriately
     * as needed.
     *
     * @since 2.7
     */
    protected int _nameStartOffset;

    /**
     * @since 2.7
     */
    protected int _nameStartRow;

    /**
     * @since 2.7
     */
    protected int _nameStartCol;

    /*
    /**********************************************************
    /* Input buffering (from former 'StreamBasedParserBase')
    /**********************************************************
     */

    protected InputStream _inputStream;

    /*
    /**********************************************************
    /* Current input data
    /**********************************************************
     */

    /**
     * Current buffer from which data is read; generally data is read into
     * buffer from input source, but in some cases pre-loaded buffer
     * is handed to the parser.
     */
    protected byte[] _inputBuffer;

    /**
     * Flag that indicates whether the input buffer is recycable (and
     * needs to be returned to recycler once we are done) or not.
     *<p>
     * If it is not, it also means that parser can NOT modify underlying
     * buffer.
     */
    protected boolean _bufferRecyclable;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    /**
     * Constructor called when caller wants to provide input buffer directly
     * (or needs to, in case of bootstrapping having read some of contents)
     * and it may or may not be recyclable use standard recycle context.
     *
     * @param ctxt I/O context to use
     * @param features Standard stream read features enabled
     * @param in InputStream used for reading actual content, if any; {@code null} if none
     * @param sym Name canonicalizer to use
     * @param inputBuffer Input buffer to read initial content from (before Reader)
     * @param start Pointer in {@code inputBuffer} that has the first content character to decode
     * @param end Pointer past the last content character in {@code inputBuffer}
     * @param bytesPreProcessed Number of bytes that have been consumed already (by bootstrapping)
     * @param bufferRecyclable Whether {@code inputBuffer} passed is managed by Jackson core
     *    (and thereby needs recycling)
     */
    public UTF8StreamJsonParser(IOContext ctxt, int features, InputStream in, ByteQuadsCanonicalizer sym,
        byte[] inputBuffer, int start, int end, int bytesPreProcessed, boolean bufferRecyclable) {
        super(ctxt, features);
        _inputStream = in;
        _symbols = sym;
        _inputBuffer = inputBuffer;
        _inputPtr = start;
        _inputEnd = end;
        _currInputRowStart = start - bytesPreProcessed;
        // If we have offset, need to omit that from byte offset, so:
        _currInputProcessed = -start + bytesPreProcessed;
        _bufferRecyclable = bufferRecyclable;
    }

    /*
    /**********************************************************
    /* Overrides, low-level reading
    /**********************************************************
     */

    protected final boolean _loadMore() throws IOException {
        if (_inputStream != null) {
            int space = _inputBuffer.length;
            if (space == 0) { // only occurs when we've been closed
                return false;
            }

            final int bufSize = _inputEnd;
            _currInputProcessed += bufSize;
            _currInputRowStart -= bufSize;
            // 06-Sep-2023, tatu: [core#1046] Enforce max doc length limit
            streamReadConstraints().validateDocumentLength(_currInputProcessed);

            int count = _inputStream.read(_inputBuffer, 0, space);
            if (count > 0) {

                // 26-Nov-2015, tatu: Since name-offset requires it too, must offset
                //   this increase to avoid "moving" name-offset, resulting most likely
                //   in negative value, which is fine as combine value remains unchanged.
                _nameStartOffset -= bufSize;

                _inputPtr = 0;
                _inputEnd = count;

                return true;
            }
            _inputPtr = _inputEnd = 0;
            // End of input
            _closeInput();
            // Should never return 0, so let's fail
            if (count == 0) {
                throw new IOException(
                    "InputStream.read() returned 0 characters when trying to read " + _inputBuffer.length + " bytes");
            }
        }
        return false;
    }

    @Override
    protected void _closeInput() throws IOException {
        // We are not to call close() on the underlying InputStream
        // unless we "own" it, or auto-closing feature is enabled.
        if (_inputStream != null) {
            if (_ioContext.isResourceManaged() || isEnabled(Feature.AUTO_CLOSE_SOURCE)) {
                _inputStream.close();
            }
            _inputStream = null;
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
        // Merge found symbols, if any:
        _symbols.release();
        if (_bufferRecyclable) {
            byte[] buf = _inputBuffer;
            if (buf != null) {
                // Let's not set it to null; this way should get slightly more meaningful
                // error messages in case someone closes parser indirectly, without realizing.
                if (buf != NO_BYTES) {
                    _inputBuffer = NO_BYTES;
                    _ioContext.releaseReadIOBuffer(buf);
                }
            }
        }
    }

    /*
    /**********************************************************
    /* Public API, data access
    /**********************************************************
     */

    @Override
    public String getText() throws IOException {
        if (_currToken == JsonToken.VALUE_STRING) {
            if (_tokenIncomplete) {
                _tokenIncomplete = false;
                return _finishAndReturnString(); // only strings can be incomplete
            }
            return _textBuffer.contentsAsString();
        }
        return _getText2(_currToken);
    }

    // // // Let's override default impls for improved performance

    // @since 2.1
    @Override
    public String getValueAsString() throws IOException {
        if (_currToken == JsonToken.VALUE_STRING) {
            if (_tokenIncomplete) {
                _tokenIncomplete = false;
                return _finishAndReturnString(); // only strings can be incomplete
            }
            return _textBuffer.contentsAsString();
        }
        if (_currToken == JsonToken.FIELD_NAME) {
            return currentName();
        }
        return super.getValueAsString(null);
    }

    // @since 2.1
    @Override
    public String getValueAsString(String defValue) throws IOException {
        if (_currToken == JsonToken.VALUE_STRING) {
            if (_tokenIncomplete) {
                _tokenIncomplete = false;
                return _finishAndReturnString(); // only strings can be incomplete
            }
            return _textBuffer.contentsAsString();
        }
        if (_currToken == JsonToken.FIELD_NAME) {
            return currentName();
        }
        return super.getValueAsString(defValue);
    }

    protected final String _getText2(JsonToken t) throws IOException {
        if (t == null) {
            return null;
        }
        switch (t.id()) {
            case ID_FIELD_NAME:
                return _parsingContext.getCurrentName();

            case ID_STRING:
                // fall through
            case ID_NUMBER_INT:
            case ID_NUMBER_FLOAT:
                return _textBuffer.contentsAsString();

            default:
                return t.asString();
        }
    }

    @Override
    public byte[] getBinaryValue(Base64Variant b64variant) throws IOException {
        if (_currToken != JsonToken.VALUE_STRING
            && (_currToken != JsonToken.VALUE_EMBEDDED_OBJECT || _binaryValue == null)) {
            _reportError("Current token (" + _currToken
                + ") not VALUE_STRING or VALUE_EMBEDDED_OBJECT, can not access as binary");
        }
        // To ensure that we won't see inconsistent data, better clear up state...
        if (_tokenIncomplete) {
            try {
                _binaryValue = _decodeBase64(b64variant);
            } catch (IllegalArgumentException iae) {
                throw _constructError(
                    "Failed to decode VALUE_STRING as base64 (" + b64variant + "): " + iae.getMessage());
            }
            // let's clear incomplete only now; allows for accessing other textual content in error cases
            _tokenIncomplete = false;
        } else { // may actually require conversion...
            if (_binaryValue == null) {
                @SuppressWarnings("resource")
                ByteArrayBuilder builder = _getByteArrayBuilder();
                _decodeBase64(getText(), builder, b64variant);
                _binaryValue = builder.toByteArray();
            }
        }
        return _binaryValue;
    }

    /*
    /**********************************************************
    /* Public API, traversal, basic
    /**********************************************************
     */

    /**
     * @return Next token from the stream, if any found, or null
     *   to indicate end-of-input
     */
    @Override
    public JsonToken nextToken() throws IOException {
        /* First: field names are special -- we will always tokenize
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
            // Close/release things like input source, symbol table and recyclable buffers
            close();
            return (_currToken = null);
        }
        // clear any data retained so far
        _binaryValue = null;

        // Closing scope?
        if (i == INT_RBRACKET) {
            _closeArrayScope();
            return (_currToken = JsonToken.END_ARRAY);
        }
        if (i == INT_RCURLY) {
            _closeObjectScope();
            return (_currToken = JsonToken.END_OBJECT);
        }

        // Nope: do we then expect a comma?
        if (_parsingContext.expectComma()) {
            if (i != INT_COMMA) {
                _reportUnexpectedChar(i, "was expecting comma to separate " + _parsingContext.typeDesc() + " entries");
            }
            i = _skipWS();
            // Was that a trailing comma?
            if ((_features & FEAT_MASK_TRAILING_COMMA) != 0) {
                if ((i == INT_RBRACKET) || (i == INT_RCURLY)) {
                    return _closeScope(i);
                }
            }
        }

        // And should we now have a name? Always true for Object contexts
        // since the intermediate 'expect-value' state is never retained.
        if (!_parsingContext.inObject()) {
            _updateLocation();
            return _nextTokenNotInObject(i);
        }
        // So first parse the field name itself:
        _updateNameLocation();
        String n = _parseName(i);
        _parsingContext.setCurrentName(n);
        _currToken = JsonToken.FIELD_NAME;

        i = _skipColon();
        _updateLocation();

        // Ok: we must have a value... what is it? Strings are very common, check first:
        if (i == INT_QUOTE) {
            _tokenIncomplete = true;
            _nextToken = JsonToken.VALUE_STRING;
            return _currToken;
        }
        JsonToken t;

        switch (i) {
            case '-':
                t = _parseSignedNumber(true);
                break;

            case '+':
                if (isEnabled(JsonReadFeature.ALLOW_LEADING_PLUS_SIGN_FOR_NUMBERS.mappedFeature())) {
                    t = _parseSignedNumber(false);
                } else {
                    t = _handleUnexpectedValue(i);
                }
                break;

            case '.': // [core#611]:
                t = _parseFloatThatStartsWithPeriod(false);
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
                t = _parseUnsignedNumber(i);
                break;

            case 'f':
                _matchFalse();
                t = JsonToken.VALUE_FALSE;
                break;

            case 'n':
                _matchNull();
                t = JsonToken.VALUE_NULL;
                break;

            case 't':
                _matchTrue();
                t = JsonToken.VALUE_TRUE;
                break;

            case '[':
                t = JsonToken.START_ARRAY;
                break;

            case '{':
                t = JsonToken.START_OBJECT;
                break;

            default:
                t = _handleUnexpectedValue(i);
        }
        _nextToken = t;
        return _currToken;
    }

    private JsonToken _nextTokenNotInObject(int i) throws IOException {
        if (i == INT_QUOTE) {
            _tokenIncomplete = true;
            return (_currToken = JsonToken.VALUE_STRING);
        }
        switch (i) {
            case '[':
                createChildArrayContext(_tokenInputRow, _tokenInputCol);
                return (_currToken = JsonToken.START_ARRAY);

            case '{':
                createChildObjectContext(_tokenInputRow, _tokenInputCol);
                return (_currToken = JsonToken.START_OBJECT);

            case 't':
                _matchTrue();
                return (_currToken = JsonToken.VALUE_TRUE);

            case 'f':
                _matchFalse();
                return (_currToken = JsonToken.VALUE_FALSE);

            case 'n':
                _matchNull();
                return (_currToken = JsonToken.VALUE_NULL);

            case '-':
                return (_currToken = _parseSignedNumber(true));

            case '+':
                if (!isEnabled(JsonReadFeature.ALLOW_LEADING_PLUS_SIGN_FOR_NUMBERS.mappedFeature())) {
                    return (_currToken = _handleUnexpectedValue(i));
                }
                return (_currToken = _parseSignedNumber(false));

            case '.': // [core#611]:
                return (_currToken = _parseFloatThatStartsWithPeriod(false));

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
                return (_currToken = _parseUnsignedNumber(i));
        }
        return (_currToken = _handleUnexpectedValue(i));
    }

    private JsonToken _nextAfterName() throws IOException {
        _nameCopied = false; // need to invalidate if it was copied
        JsonToken t = _nextToken;
        _nextToken = null;

        // !!! 16-Nov-2015, tatu: TODO: fix [databind#37], copy next location to current here

        // Also: may need to start new context?
        if (t == JsonToken.START_ARRAY) {
            createChildArrayContext(_tokenInputRow, _tokenInputCol);
        } else if (t == JsonToken.START_OBJECT) {
            createChildObjectContext(_tokenInputRow, _tokenInputCol);
        }
        return (_currToken = t);
    }

    /*
    /**********************************************************
    /* Internal methods, number parsing
    /**********************************************************
     */

    protected final JsonToken _parseFloatThatStartsWithPeriod(final boolean neg) throws IOException {
        // [core#611]: allow optionally leading decimal point
        if (!isEnabled(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS.mappedFeature())) {
            return _handleUnexpectedValue(INT_PERIOD);
        }
        final char[] outBuf = _textBuffer.emptyAndGetCurrentSegment();
        int outPtr = 0;
        // 27-Jun-2022, tatu: [core#784] would add plus here too but not yet
        if (neg) {
            outBuf[outPtr++] = '-';
        }
        return _parseFloat(outBuf, outPtr, INT_PERIOD, neg, 0);
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
     * @param c The first non-null digit character of the number to parse
     *
     * @return Type of token decoded, usually {@link JsonToken#VALUE_NUMBER_INT}
     *    or {@link JsonToken#VALUE_NUMBER_FLOAT}
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    protected JsonToken _parseUnsignedNumber(int c) throws IOException {
        char[] outBuf = _textBuffer.emptyAndGetCurrentSegment();
        // One special case: if first char is 0, must not be followed by a digit
        if (c == INT_0) {
            c = _verifyNoLeadingZeroes();
        }
        // Ok: we can first just add digit we saw first:
        outBuf[0] = (char) c;
        int intLen = 1;
        int outPtr = 1;
        // And then figure out how far we can read without further checks
        // for either input or output
        final int end = Math.min(_inputEnd, _inputPtr + outBuf.length - 1); // 1 == outPtr
        // With this, we have a nice and tight loop:
        while (true) {
            if (_inputPtr >= end) { // split across boundary, offline
                return _parseNumber2(outBuf, outPtr, false, intLen);
            }
            c = _inputBuffer[_inputPtr++] & 0xFF;
            if (c < INT_0 || c > INT_9) {
                break;
            }
            ++intLen;
            outBuf[outPtr++] = (char) c;
        }
        if (c == INT_PERIOD || (c | 0x20) == INT_e) { // ~ '.eE'
            return _parseFloat(outBuf, outPtr, c, false, intLen);
        }
        --_inputPtr; // to push back trailing char (comma etc)
        _textBuffer.setCurrentLength(outPtr);
        // As per #105, need separating space between root values; check here
        if (_parsingContext.inRoot()) {
            _verifyRootSpace(c);
        }
        // And there we have it!
        return resetInt(false, intLen);
    }

    private JsonToken _parseSignedNumber(boolean negative) throws IOException {
        char[] outBuf = _textBuffer.emptyAndGetCurrentSegment();
        int outPtr = 0;

        if (negative) {
            // Need to prepend sign?
            outBuf[outPtr++] = '-';
        }
        // Must have something after sign too
        if (_inputPtr >= _inputEnd) {
            _loadMoreGuaranteed();
        }
        int c = _inputBuffer[_inputPtr++] & 0xFF;
        // Note: must be followed by a digit
        if (c <= INT_0) {
            // One special case: if first char is 0, must not be followed by a digit
            if (c != INT_0) {
                if (c == INT_PERIOD) {
                    return _parseFloatThatStartsWithPeriod(negative);
                }
                return _handleInvalidNumberStart(c, negative);
            }
            c = _verifyNoLeadingZeroes();
        } else if (c > INT_9) {
            return _handleInvalidNumberStart(c, negative);
        }

        // Ok: we can first just add digit we saw first:
        outBuf[outPtr++] = (char) c;
        int intLen = 1;

        // And then figure out how far we can read without further checks
        // for either input or output
        final int end = Math.min(_inputEnd, _inputPtr + outBuf.length - outPtr);
        // With this, we have a nice and tight loop:
        while (true) {
            if (_inputPtr >= end) {
                // Long enough to be split across boundary, so:
                return _parseNumber2(outBuf, outPtr, negative, intLen);
            }
            c = _inputBuffer[_inputPtr++] & 0xFF;
            if (c < INT_0 || c > INT_9) {
                break;
            }
            ++intLen;
            outBuf[outPtr++] = (char) c;
        }
        if (c == INT_PERIOD || (c | 0x20) == INT_e) { // ~ '.eE'
            return _parseFloat(outBuf, outPtr, c, negative, intLen);
        }

        --_inputPtr; // to push back trailing char (comma etc)
        _textBuffer.setCurrentLength(outPtr);
        // As per #105, need separating space between root values; check here
        if (_parsingContext.inRoot()) {
            _verifyRootSpace(c);
        }

        // And there we have it!
        return resetInt(negative, intLen);
    }

    // Method called to handle parsing when input is split across buffer boundary
    // (or output is longer than segment used to store it)
    private JsonToken _parseNumber2(char[] outBuf, int outPtr, boolean negative, int intPartLength) throws IOException {
        // Ok, parse the rest
        while (true) {
            if (_inputPtr >= _inputEnd && !_loadMore()) {
                _textBuffer.setCurrentLength(outPtr);
                return resetInt(negative, intPartLength);
            }
            int c = _inputBuffer[_inputPtr++] & 0xFF;
            if (c > INT_9 || c < INT_0) {
                if (c == INT_PERIOD || (c | 0x20) == INT_e) { // ~ '.eE'
                    return _parseFloat(outBuf, outPtr, c, negative, intPartLength);
                }
                break;
            }
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            outBuf[outPtr++] = (char) c;
            ++intPartLength;
        }
        --_inputPtr; // to push back trailing char (comma etc)
        _textBuffer.setCurrentLength(outPtr);
        // As per #105, need separating space between root values; check here
        if (_parsingContext.inRoot()) {
            _verifyRootSpace(_inputBuffer[_inputPtr] & 0xFF);
        }

        // And there we have it!
        return resetInt(negative, intPartLength);

    }

    // Method called when we have seen one zero, and want to ensure
    // it is not followed by another
    private int _verifyNoLeadingZeroes() throws IOException {
        // Ok to have plain "0"
        if (_inputPtr >= _inputEnd && !_loadMore()) {
            return INT_0;
        }
        int ch = _inputBuffer[_inputPtr] & 0xFF;
        // if not followed by a number (probably '.'); return zero as is, to be included
        if (ch < INT_0 || ch > INT_9) {
            return INT_0;
        }
        // [JACKSON-358]: we may want to allow them, after all...
        if ((_features & FEAT_MASK_LEADING_ZEROS) == 0) {
            reportInvalidNumber("Leading zeroes not allowed");
        }
        // if so, just need to skip either all zeroes (if followed by number); or all but one (if non-number)
        ++_inputPtr; // Leading zero to be skipped
        if (ch == INT_0) {
            while (_inputPtr < _inputEnd || _loadMore()) {
                ch = _inputBuffer[_inputPtr] & 0xFF;
                if (ch < INT_0 || ch > INT_9) { // followed by non-number; retain one zero
                    return INT_0;
                }
                ++_inputPtr; // skip previous zeroes
                if (ch != INT_0) { // followed by other number; return
                    break;
                }
            }
        }
        return ch;
    }

    private JsonToken _parseFloat(char[] outBuf, int outPtr, int c, boolean negative, int integerPartLength)
        throws IOException {
        int fractLen = 0;
        boolean eof = false;

        // And then see if we get other parts
        if (c == INT_PERIOD) { // yes, fraction
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            outBuf[outPtr++] = (char) c;

            while (true) {
                if (_inputPtr >= _inputEnd && !_loadMore()) {
                    eof = true;
                    break;
                }
                c = _inputBuffer[_inputPtr++] & 0xFF;
                if (c < INT_0 || c > INT_9) {
                    break;
                }
                ++fractLen;
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outBuf[outPtr++] = (char) c;
            }
            // must be followed by sequence of ints, one minimum
            if (fractLen == 0) {
                if (!isEnabled(JsonReadFeature.ALLOW_TRAILING_DECIMAL_POINT_FOR_NUMBERS.mappedFeature())) {
                    _reportUnexpectedNumberChar(c, "Decimal point not followed by a digit");
                }
            }
        }

        int expLen = 0;
        if ((c | 0x20) == INT_e) { // ~ 'eE' exponent?
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            outBuf[outPtr++] = (char) c;
            // Not optional, can require that we get one more char
            if (_inputPtr >= _inputEnd) {
                _loadMoreGuaranteed();
            }
            c = _inputBuffer[_inputPtr++] & 0xFF;
            // Sign indicator?
            if (c == '-' || c == '+') {
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outBuf[outPtr++] = (char) c;
                // Likewise, non optional:
                if (_inputPtr >= _inputEnd) {
                    _loadMoreGuaranteed();
                }
                c = _inputBuffer[_inputPtr++] & 0xFF;
            }

            while (c >= INT_0 && c <= INT_9) {
                ++expLen;
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outBuf[outPtr++] = (char) c;
                if (_inputPtr >= _inputEnd && !_loadMore()) {
                    eof = true;
                    break;
                }
                c = _inputBuffer[_inputPtr++] & 0xFF;
            }
            // must be followed by sequence of ints, one minimum
            if (expLen == 0) {
                _reportUnexpectedNumberChar(c, "Exponent indicator not followed by a digit");
            }
        }

        // Ok; unless we hit end-of-input, need to push last char read back
        if (!eof) {
            --_inputPtr;
            // As per [core#105], need separating space between root values; check here
            if (_parsingContext.inRoot()) {
                _verifyRootSpace(c);
            }
        }
        _textBuffer.setCurrentLength(outPtr);

        // And there we have it!
        return resetFloat(negative, integerPartLength, fractLen, expLen);
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
        // TODO? Handle UTF-8 char decoding for error reporting
        switch (ch) {
            case ' ':
            case '\t':
                return;

            case '\r':
                // 29-Oct-2022, tatu: [core#834] While issue is only relevant for char-backed
                //   sources, let's unify handling to keep behavior uniform.
                // _skipCR();
                --_inputPtr;
                return;

            case '\n':
                ++_currInputRow;
                _currInputRowStart = _inputPtr;
                return;
        }
        _reportMissingRootWS(ch);
    }

    /*
    /**********************************************************
    /* Internal methods, secondary parsing
    /**********************************************************
     */

    protected final String _parseName(int i) throws IOException {
        if (i != INT_QUOTE) {
            return _handleOddName(i);
        }
        // First: can we optimize out bounds checks?
        if ((_inputPtr + 13) > _inputEnd) { // Need up to 12 chars, plus one trailing (quote)
            return slowParseName();
        }

        // If so, can also unroll loops nicely
        /* 25-Nov-2008, tatu: This may seem weird, but here we do
         *   NOT want to worry about UTF-8 decoding. Rather, we'll
         *   assume that part is ok (if not it will get caught
         *   later on), and just handle quotes and backslashes here.
         */
        final byte[] input = _inputBuffer;
        final int[] codes = INPUT_CODES_LATIN1;

        int q = input[_inputPtr++] & 0xFF;

        if (codes[q] == 0) {
            i = input[_inputPtr++] & 0xFF;
            if (codes[i] == 0) {
                q = (q << 8) | i;
                i = input[_inputPtr++] & 0xFF;
                if (codes[i] == 0) {
                    q = (q << 8) | i;
                    i = input[_inputPtr++] & 0xFF;
                    if (codes[i] == 0) {
                        q = (q << 8) | i;
                        i = input[_inputPtr++] & 0xFF;
                        if (codes[i] == 0) {
                            _quad1 = q;
                            return parseMediumName(i);
                        }
                        if (i == INT_QUOTE) { // 4 byte/char case or broken
                            return findName(q, 4);
                        }
                        return parseName(q, i, 4);
                    }
                    if (i == INT_QUOTE) { // 3 byte/char case or broken
                        return findName(q, 3);
                    }
                    return parseName(q, i, 3);
                }
                if (i == INT_QUOTE) { // 2 byte/char case or broken
                    return findName(q, 2);
                }
                return parseName(q, i, 2);
            }
            if (i == INT_QUOTE) { // one byte/char case or broken
                return findName(q, 1);
            }
            return parseName(q, i, 1);
        }
        if (q == INT_QUOTE) { // special case, ""
            return "";
        }
        return parseName(0, q, 0); // quoting or invalid char
    }

    protected final String parseMediumName(int q2) throws IOException {
        final byte[] input = _inputBuffer;
        final int[] codes = INPUT_CODES_LATIN1;

        // Ok, got 5 name bytes so far
        int i = input[_inputPtr++] & 0xFF;
        if (codes[i] != 0) {
            if (i == INT_QUOTE) { // 5 bytes
                return findName(_quad1, q2, 1);
            }
            return parseName(_quad1, q2, i, 1); // quoting or invalid char
        }
        q2 = (q2 << 8) | i;
        i = input[_inputPtr++] & 0xFF;
        if (codes[i] != 0) {
            if (i == INT_QUOTE) { // 6 bytes
                return findName(_quad1, q2, 2);
            }
            return parseName(_quad1, q2, i, 2);
        }
        q2 = (q2 << 8) | i;
        i = input[_inputPtr++] & 0xFF;
        if (codes[i] != 0) {
            if (i == INT_QUOTE) { // 7 bytes
                return findName(_quad1, q2, 3);
            }
            return parseName(_quad1, q2, i, 3);
        }
        q2 = (q2 << 8) | i;
        i = input[_inputPtr++] & 0xFF;
        if (codes[i] != 0) {
            if (i == INT_QUOTE) { // 8 bytes
                return findName(_quad1, q2, 4);
            }
            return parseName(_quad1, q2, i, 4);
        }
        return parseMediumName2(i, q2);
    }

    // @since 2.6
    protected final String parseMediumName2(int q3, final int q2) throws IOException {
        final byte[] input = _inputBuffer;
        final int[] codes = INPUT_CODES_LATIN1;

        // Got 9 name bytes so far
        int i = input[_inputPtr++] & 0xFF;
        if (codes[i] != 0) {
            if (i == INT_QUOTE) { // 9 bytes
                return findName(_quad1, q2, q3, 1);
            }
            return parseName(_quad1, q2, q3, i, 1);
        }
        q3 = (q3 << 8) | i;
        i = input[_inputPtr++] & 0xFF;
        if (codes[i] != 0) {
            if (i == INT_QUOTE) { // 10 bytes
                return findName(_quad1, q2, q3, 2);
            }
            return parseName(_quad1, q2, q3, i, 2);
        }
        q3 = (q3 << 8) | i;
        i = input[_inputPtr++] & 0xFF;
        if (codes[i] != 0) {
            if (i == INT_QUOTE) { // 11 bytes
                return findName(_quad1, q2, q3, 3);
            }
            return parseName(_quad1, q2, q3, i, 3);
        }
        q3 = (q3 << 8) | i;
        i = input[_inputPtr++] & 0xFF;
        if (codes[i] != 0) {
            if (i == INT_QUOTE) { // 12 bytes
                return findName(_quad1, q2, q3, 4);
            }
            return parseName(_quad1, q2, q3, i, 4);
        }
        return parseLongName(i, q2, q3);
    }

    protected final String parseLongName(int q, final int q2, int q3) throws IOException {
        _quadBuffer[0] = _quad1;
        _quadBuffer[1] = q2;
        _quadBuffer[2] = q3;

        // As explained above, will ignore UTF-8 encoding at this point
        final byte[] input = _inputBuffer;
        final int[] codes = INPUT_CODES_LATIN1;
        int qlen = 3;

        while ((_inputPtr + 4) <= _inputEnd) {
            int i = input[_inputPtr++] & 0xFF;
            if (codes[i] != 0) {
                if (i == INT_QUOTE) {
                    return findName(_quadBuffer, qlen, q, 1);
                }
                return parseEscapedName(_quadBuffer, qlen, q, i, 1);
            }

            q = (q << 8) | i;
            i = input[_inputPtr++] & 0xFF;
            if (codes[i] != 0) {
                if (i == INT_QUOTE) {
                    return findName(_quadBuffer, qlen, q, 2);
                }
                return parseEscapedName(_quadBuffer, qlen, q, i, 2);
            }

            q = (q << 8) | i;
            i = input[_inputPtr++] & 0xFF;
            if (codes[i] != 0) {
                if (i == INT_QUOTE) {
                    return findName(_quadBuffer, qlen, q, 3);
                }
                return parseEscapedName(_quadBuffer, qlen, q, i, 3);
            }

            q = (q << 8) | i;
            i = input[_inputPtr++] & 0xFF;
            if (codes[i] != 0) {
                if (i == INT_QUOTE) {
                    return findName(_quadBuffer, qlen, q, 4);
                }
                return parseEscapedName(_quadBuffer, qlen, q, i, 4);
            }

            // Nope, no end in sight. Need to grow quad array etc
            if (qlen >= _quadBuffer.length) {
                _quadBuffer = _growNameDecodeBuffer(_quadBuffer, qlen);
            }
            _quadBuffer[qlen++] = q;
            q = i;
        }

        /* Let's offline if we hit buffer boundary (otherwise would
         * need to [try to] align input, which is bit complicated
         * and may not always be possible)
         */
        return parseEscapedName(_quadBuffer, qlen, 0, q, 0);
    }

    // Method called when not even first 8 bytes are guaranteed
    // to come consecutively. Happens rarely, so this is offlined;
    // plus we'll also do full checks for escaping etc.
    protected String slowParseName() throws IOException {
        if (_inputPtr >= _inputEnd) {
            if (!_loadMore()) {
                _reportInvalidEOF(": was expecting closing '\"' for name", JsonToken.FIELD_NAME);
            }
        }
        int i = _inputBuffer[_inputPtr++] & 0xFF;
        if (i == INT_QUOTE) { // special case, ""
            return "";
        }
        return parseEscapedName(_quadBuffer, 0, 0, i, 0);
    }

    private String parseName(int q1, int ch, int lastQuadBytes) throws IOException {
        return parseEscapedName(_quadBuffer, 0, q1, ch, lastQuadBytes);
    }

    private String parseName(int q1, int q2, int ch, int lastQuadBytes) throws IOException {
        _quadBuffer[0] = q1;
        return parseEscapedName(_quadBuffer, 1, q2, ch, lastQuadBytes);
    }

    private String parseName(int q1, int q2, int q3, int ch, int lastQuadBytes) throws IOException {
        _quadBuffer[0] = q1;
        _quadBuffer[1] = q2;
        return parseEscapedName(_quadBuffer, 2, q3, ch, lastQuadBytes);
    }

    // Slower parsing method which is generally branched to when an escape
    // sequence is detected (or alternatively for long names, one crossing
    // input buffer boundary). Needs to be able to handle more exceptional
    // cases, gets slower, and hence is offlined to a separate method.
    protected final String parseEscapedName(int[] quads, int qlen, int currQuad, int ch, int currQuadBytes)
        throws IOException {
        // This may seem weird, but here we do not want to worry about
        // UTF-8 decoding yet. Rather, we'll assume that part is ok (if not it will get
        // caught later on), and just handle quotes and backslashes here.

        while (true) {
            if (INPUT_CODES_LATIN1[ch] != 0) {
                if (ch == INT_QUOTE) { // we are done
                    break;
                }
                // Unquoted white space?
                if (ch != INT_BACKSLASH) {
                    // As per [JACKSON-208], call can now return:
                    _throwUnquotedSpace(ch, "name");
                } else {
                    // Nope, escape sequence
                    ch = _decodeEscaped();
                }
                // Oh crap. May need to UTF-8 (re-)encode it, if it's beyond
                // 7-bit ASCII. Gets pretty messy. If this happens often, may
                // want to use different name canonicalization to avoid these hits.
                if (ch > 127) {
                    // Ok, we'll need room for first byte right away
                    if (currQuadBytes >= 4) {
                        if (qlen >= quads.length) {
                            _quadBuffer = quads = _growNameDecodeBuffer(quads, quads.length);
                        }
                        quads[qlen++] = currQuad;
                        currQuad = 0;
                        currQuadBytes = 0;
                    }
                    if (ch < 0x800) { // 2-byte
                        currQuad = (currQuad << 8) | (0xc0 | (ch >> 6));
                        ++currQuadBytes;
                        // Second byte gets output below:
                    } else { // 3 bytes; no need to worry about surrogates here
                        currQuad = (currQuad << 8) | (0xe0 | (ch >> 12));
                        ++currQuadBytes;
                        // need room for middle byte?
                        if (currQuadBytes >= 4) {
                            if (qlen >= quads.length) {
                                _quadBuffer = quads = _growNameDecodeBuffer(quads, quads.length);
                            }
                            quads[qlen++] = currQuad;
                            currQuad = 0;
                            currQuadBytes = 0;
                        }
                        currQuad = (currQuad << 8) | (0x80 | ((ch >> 6) & 0x3f));
                        ++currQuadBytes;
                    }
                    // And same last byte in both cases, gets output below:
                    ch = 0x80 | (ch & 0x3f);
                }
            }
            // Ok, we have one more byte to add at any rate:
            if (currQuadBytes < 4) {
                ++currQuadBytes;
                currQuad = (currQuad << 8) | ch;
            } else {
                if (qlen >= quads.length) {
                    _quadBuffer = quads = _growNameDecodeBuffer(quads, quads.length);
                }
                quads[qlen++] = currQuad;
                currQuad = ch;
                currQuadBytes = 1;
            }
            if (_inputPtr >= _inputEnd) {
                if (!_loadMore()) {
                    _reportInvalidEOF(" in field name", JsonToken.FIELD_NAME);
                }
            }
            ch = _inputBuffer[_inputPtr++] & 0xFF;
        }

        if (currQuadBytes > 0) {
            if (qlen >= quads.length) {
                _quadBuffer = quads = _growNameDecodeBuffer(quads, quads.length);
            }
            quads[qlen++] = _padLastQuad(currQuad, currQuadBytes);
        }
        String name = _symbols.findName(quads, qlen);
        if (name == null) {
            name = addName(quads, qlen, currQuadBytes);
        }
        return name;
    }

    /**
     * Method called when we see non-white space character other
     * than double quote, when expecting a field name.
     * In standard mode will just throw an exception; but
     * in non-standard modes may be able to parse name.
     *
     * @param ch First undecoded character of possible "odd name" to decode
     *
     * @return Name decoded, if allowed and successful
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems (invalid name)
     */
    protected String _handleOddName(int ch) throws IOException {
        // First: may allow single quotes
        if (ch == INT_APOS && (_features & FEAT_MASK_ALLOW_SINGLE_QUOTES) != 0) {
            return _parseAposName();
        }
        // Allow unquoted names if feature enabled:
        if ((_features & FEAT_MASK_ALLOW_UNQUOTED_NAMES) == 0) {
            char c = (char) _decodeCharForError(ch);
            _reportUnexpectedChar(c, "was expecting double-quote to start field name");
        }
        /* Also: note that although we use a different table here,
         * it does NOT handle UTF-8 decoding. It'll just pass those
         * high-bit codes as acceptable for later decoding.
         */
        final int[] codes = CharTypes.getInputCodeUtf8JsNames();
        // Also: must start with a valid character...
        if (codes[ch] != 0) {
            _reportUnexpectedChar(ch,
                "was expecting either valid name character (for unquoted name) or double-quote (for quoted) to start field name");
        }

        // Ok, now; instead of ultra-optimizing parsing here (as with regular
        // JSON names), let's just use the generic "slow" variant.
        // Can measure its impact later on if need be.
        int[] quads = _quadBuffer;
        int qlen = 0;
        int currQuad = 0;
        int currQuadBytes = 0;

        while (true) {
            // Ok, we have one more byte to add at any rate:
            if (currQuadBytes < 4) {
                ++currQuadBytes;
                currQuad = (currQuad << 8) | ch;
            } else {
                if (qlen >= quads.length) {
                    _quadBuffer = quads = _growNameDecodeBuffer(quads, quads.length);
                }
                quads[qlen++] = currQuad;
                currQuad = ch;
                currQuadBytes = 1;
            }
            if (_inputPtr >= _inputEnd) {
                if (!_loadMore()) {
                    _reportInvalidEOF(" in field name", JsonToken.FIELD_NAME);
                }
            }
            ch = _inputBuffer[_inputPtr] & 0xFF;
            if (codes[ch] != 0) {
                break;
            }
            ++_inputPtr;
        }

        if (currQuadBytes > 0) {
            if (qlen >= quads.length) {
                _quadBuffer = quads = _growNameDecodeBuffer(quads, quads.length);
            }
            quads[qlen++] = currQuad;
        }
        String name = _symbols.findName(quads, qlen);
        if (name == null) {
            name = addName(quads, qlen, currQuadBytes);
        }
        return name;
    }

    // Parsing to support apostrope-quoted names. Plenty of duplicated code;
    // main reason being to try to avoid slowing down fast path
    // for valid JSON -- more alternatives, more code, generally
    // bit slower execution.
    protected String _parseAposName() throws IOException {
        if (_inputPtr >= _inputEnd) {
            if (!_loadMore()) {
                _reportInvalidEOF(": was expecting closing ''' for field name", JsonToken.FIELD_NAME);
            }
        }
        int ch = _inputBuffer[_inputPtr++] & 0xFF;
        if (ch == INT_APOS) { // special case, ''
            return "";
        }
        int[] quads = _quadBuffer;
        int qlen = 0;
        int currQuad = 0;
        int currQuadBytes = 0;

        // Copied from parseEscapedFieldName, with minor mods:

        while (ch != INT_APOS) {
            // additional check to skip handling of double-quotes
            if ((INPUT_CODES_LATIN1[ch] != 0) && (ch != INT_QUOTE)) {
                if (ch != '\\') {
                    // Unquoted white space?
                    // As per [JACKSON-208], call can now return:
                    _throwUnquotedSpace(ch, "name");
                } else {
                    // Nope, escape sequence
                    ch = _decodeEscaped();
                }
                // as per main code, inefficient but will have to do
                if (ch > 127) {
                    // Ok, we'll need room for first byte right away
                    if (currQuadBytes >= 4) {
                        if (qlen >= quads.length) {
                            _quadBuffer = quads = _growNameDecodeBuffer(quads, quads.length);
                        }
                        quads[qlen++] = currQuad;
                        currQuad = 0;
                        currQuadBytes = 0;
                    }
                    if (ch < 0x800) { // 2-byte
                        currQuad = (currQuad << 8) | (0xc0 | (ch >> 6));
                        ++currQuadBytes;
                        // Second byte gets output below:
                    } else { // 3 bytes; no need to worry about surrogates here
                        currQuad = (currQuad << 8) | (0xe0 | (ch >> 12));
                        ++currQuadBytes;
                        // need room for middle byte?
                        if (currQuadBytes >= 4) {
                            if (qlen >= quads.length) {
                                _quadBuffer = quads = _growNameDecodeBuffer(quads, quads.length);
                            }
                            quads[qlen++] = currQuad;
                            currQuad = 0;
                            currQuadBytes = 0;
                        }
                        currQuad = (currQuad << 8) | (0x80 | ((ch >> 6) & 0x3f));
                        ++currQuadBytes;
                    }
                    // And same last byte in both cases, gets output below:
                    ch = 0x80 | (ch & 0x3f);
                }
            }
            // Ok, we have one more byte to add at any rate:
            if (currQuadBytes < 4) {
                ++currQuadBytes;
                currQuad = (currQuad << 8) | ch;
            } else {
                if (qlen >= quads.length) {
                    _quadBuffer = quads = _growNameDecodeBuffer(quads, quads.length);
                }
                quads[qlen++] = currQuad;
                currQuad = ch;
                currQuadBytes = 1;
            }
            if (_inputPtr >= _inputEnd) {
                if (!_loadMore()) {
                    _reportInvalidEOF(" in field name", JsonToken.FIELD_NAME);
                }
            }
            ch = _inputBuffer[_inputPtr++] & 0xFF;
        }

        if (currQuadBytes > 0) {
            if (qlen >= quads.length) {
                _quadBuffer = quads = _growNameDecodeBuffer(quads, quads.length);
            }
            quads[qlen++] = _padLastQuad(currQuad, currQuadBytes);
        }
        String name = _symbols.findName(quads, qlen);
        if (name == null) {
            name = addName(quads, qlen, currQuadBytes);
        }
        return name;
    }

    /*
    /**********************************************************
    /* Internal methods, symbol (name) handling
    /**********************************************************
     */

    private String findName(int q1, int lastQuadBytes) throws JsonParseException, StreamConstraintsException {
        q1 = _padLastQuad(q1, lastQuadBytes);
        // Usually we'll find it from the canonical symbol table already
        String name = _symbols.findName(q1);
        if (name != null) {
            return name;
        }
        // If not, more work. We'll need add stuff to buffer
        _quadBuffer[0] = q1;
        return addName(_quadBuffer, 1, lastQuadBytes);
    }

    private String findName(int q1, int q2, int lastQuadBytes) throws JsonParseException, StreamConstraintsException {
        q2 = _padLastQuad(q2, lastQuadBytes);
        // Usually we'll find it from the canonical symbol table already
        String name = _symbols.findName(q1, q2);
        if (name != null) {
            return name;
        }
        // If not, more work. We'll need add stuff to buffer
        _quadBuffer[0] = q1;
        _quadBuffer[1] = q2;
        return addName(_quadBuffer, 2, lastQuadBytes);
    }

    private String findName(int q1, int q2, int q3, int lastQuadBytes)
        throws JsonParseException, StreamConstraintsException {
        q3 = _padLastQuad(q3, lastQuadBytes);
        String name = _symbols.findName(q1, q2, q3);
        if (name != null) {
            return name;
        }
        int[] quads = _quadBuffer;
        quads[0] = q1;
        quads[1] = q2;
        quads[2] = _padLastQuad(q3, lastQuadBytes);
        return addName(quads, 3, lastQuadBytes);
    }

    private String findName(int[] quads, int qlen, int lastQuad, int lastQuadBytes)
        throws JsonParseException, StreamConstraintsException {
        if (qlen >= quads.length) {
            _quadBuffer = quads = _growNameDecodeBuffer(quads, quads.length);
        }
        quads[qlen++] = _padLastQuad(lastQuad, lastQuadBytes);
        String name = _symbols.findName(quads, qlen);
        if (name == null) {
            return addName(quads, qlen, lastQuadBytes);
        }
        return name;
    }

    /* This is the main workhorse method used when we take a symbol
     * table miss. It needs to demultiplex individual bytes, decode
     * multi-byte chars (if any), and then construct Name instance
     * and add it to the symbol table.
     */
    private String addName(int[] quads, int qlen, int lastQuadBytes)
        throws JsonParseException, StreamConstraintsException {
        /* Ok: must decode UTF-8 chars. No other validation is
         * needed, since unescaping has been done earlier as necessary
         * (as well as error reporting for unescaped control chars)
         */
        // 4 bytes per quad, except last one maybe less
        final int byteLen = (qlen << 2) - 4 + lastQuadBytes;
        _streamReadConstraints.validateNameLength(byteLen);

        /* And last one is not correctly aligned (leading zero bytes instead
         * need to shift a bit, instead of trailing). Only need to shift it
         * for UTF-8 decoding; need revert for storage (since key will not
         * be aligned, to optimize lookup speed)
         */
        int lastQuad;

        if (lastQuadBytes < 4) {
            lastQuad = quads[qlen - 1];
            // 8/16/24 bit left shift
            quads[qlen - 1] = (lastQuad << ((4 - lastQuadBytes) << 3));
        } else {
            lastQuad = 0;
        }

        // Need some working space, TextBuffer works well:
        char[] cbuf = _textBuffer.emptyAndGetCurrentSegment();
        int cix = 0;

        for (int ix = 0; ix < byteLen;) {
            int ch = quads[ix >> 2]; // current quad, need to shift+mask
            int byteIx = (ix & 3);
            ch = (ch >> ((3 - byteIx) << 3)) & 0xFF;
            ++ix;

            if (ch > 127) { // multi-byte
                int needed;
                if ((ch & 0xE0) == 0xC0) { // 2 bytes (0x0080 - 0x07FF)
                    ch &= 0x1F;
                    needed = 1;
                } else if ((ch & 0xF0) == 0xE0) { // 3 bytes (0x0800 - 0xFFFF)
                    ch &= 0x0F;
                    needed = 2;
                } else if ((ch & 0xF8) == 0xF0) { // 4 bytes; double-char with surrogates and all...
                    ch &= 0x07;
                    needed = 3;
                } else { // 5- and 6-byte chars not valid json chars
                    _reportInvalidInitial(ch);
                    needed = ch = 1; // never really gets this far
                }
                if ((ix + needed) > byteLen) {
                    _reportInvalidEOF(" in field name", JsonToken.FIELD_NAME);
                }

                // Ok, always need at least one more:
                int ch2 = quads[ix >> 2]; // current quad, need to shift+mask
                byteIx = (ix & 3);
                ch2 = (ch2 >> ((3 - byteIx) << 3));
                ++ix;

                if ((ch2 & 0xC0) != 0x080) {
                    _reportInvalidOther(ch2);
                }
                ch = (ch << 6) | (ch2 & 0x3F);
                if (needed > 1) {
                    ch2 = quads[ix >> 2];
                    byteIx = (ix & 3);
                    ch2 = (ch2 >> ((3 - byteIx) << 3));
                    ++ix;

                    if ((ch2 & 0xC0) != 0x080) {
                        _reportInvalidOther(ch2);
                    }
                    ch = (ch << 6) | (ch2 & 0x3F);
                    if (needed > 2) { // 4 bytes? (need surrogates on output)
                        ch2 = quads[ix >> 2];
                        byteIx = (ix & 3);
                        ch2 = (ch2 >> ((3 - byteIx) << 3));
                        ++ix;
                        if ((ch2 & 0xC0) != 0x080) {
                            _reportInvalidOther(ch2 & 0xFF);
                        }
                        ch = (ch << 6) | (ch2 & 0x3F);
                    }
                }
                if (needed > 2) { // surrogate pair? once again, let's output one here, one later on
                    ch -= 0x10000; // to normalize it starting with 0x0
                    if (cix >= cbuf.length) {
                        cbuf = _textBuffer.expandCurrentSegment();
                    }
                    cbuf[cix++] = (char) (0xD800 + (ch >> 10));
                    ch = 0xDC00 | (ch & 0x03FF);
                }
            }
            if (cix >= cbuf.length) {
                cbuf = _textBuffer.expandCurrentSegment();
            }
            cbuf[cix++] = (char) ch;
        }

        // Ok. Now we have the character array, and can construct the String
        String baseName = new String(cbuf, 0, cix);
        // And finally, un-align if necessary
        if (lastQuadBytes < 4) {
            quads[qlen - 1] = lastQuad;
        }
        return _symbols.addName(baseName, quads, qlen);
    }

    // Helper method needed to fix [jackson-core#148], masking of 0x00 character
    private static int _padLastQuad(int q, int bytes) {
        return (bytes == 4) ? q : (q | (-1 << (bytes << 3)));
    }

    /*
    /**********************************************************
    /* Internal methods, String value parsing
    /**********************************************************
     */

    protected void _loadMoreGuaranteed() throws IOException {
        if (!_loadMore()) {
            _reportInvalidEOF();
        }
    }

    @Override
    protected void _finishString() throws IOException {
        // First, single tight loop for ASCII content, not split across input buffer boundary:
        int ptr = _inputPtr;
        if (ptr >= _inputEnd) {
            _loadMoreGuaranteed();
            ptr = _inputPtr;
        }
        int outPtr = 0;
        char[] outBuf = _textBuffer.emptyAndGetCurrentSegment();

        final int max = Math.min(_inputEnd, (ptr + outBuf.length));
        final byte[] inputBuffer = _inputBuffer;
        while (ptr < max) {
            int c = inputBuffer[ptr] & 0xFF;
            if (INPUT_CODES_UTF8[c] != 0) {
                if (c == INT_QUOTE) {
                    _inputPtr = ptr + 1;
                    _textBuffer.setCurrentLength(outPtr);
                    return;
                }
                break;
            }
            ++ptr;
            outBuf[outPtr++] = (char) c;
        }
        _inputPtr = ptr;
        _finishString2(outBuf, outPtr);
    }

    // @since 2.6
    protected String _finishAndReturnString() throws IOException {
        // First, single tight loop for ASCII content, not split across input buffer boundary:
        int ptr = _inputPtr;
        if (ptr >= _inputEnd) {
            _loadMoreGuaranteed();
            ptr = _inputPtr;
        }
        int outPtr = 0;
        char[] outBuf = _textBuffer.emptyAndGetCurrentSegment();

        final int max = Math.min(_inputEnd, (ptr + outBuf.length));
        final byte[] inputBuffer = _inputBuffer;
        while (ptr < max) {
            int c = inputBuffer[ptr] & 0xFF;
            if (INPUT_CODES_UTF8[c] != 0) {
                if (c == INT_QUOTE) {
                    _inputPtr = ptr + 1;
                    return _textBuffer.setCurrentAndReturn(outPtr);
                }
                break;
            }
            ++ptr;
            outBuf[outPtr++] = (char) c;
        }
        _inputPtr = ptr;
        _finishString2(outBuf, outPtr);
        return _textBuffer.contentsAsString();
    }

    private void _finishString2(char[] outBuf, int outPtr) throws IOException {
        int c;

        // Here we do want to do full decoding, hence:
        final int[] codes = INPUT_CODES_UTF8;
        final byte[] inputBuffer = _inputBuffer;

        while (true) {
            // Then the tight ASCII non-funny-char loop:
            ascii_loop: while (true) {
                int ptr = _inputPtr;
                if (ptr >= _inputEnd) {
                    _loadMoreGuaranteed();
                    ptr = _inputPtr;
                }
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                final int max = Math.min(_inputEnd, (ptr + (outBuf.length - outPtr)));
                while (ptr < max) {
                    c = inputBuffer[ptr++] & 0xFF;
                    if (codes[c] != 0) {
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                    outBuf[outPtr++] = (char) c;
                }
                _inputPtr = ptr;
            }
            // Ok: end marker, escape or multi-byte?
            if (c == INT_QUOTE) {
                break;
            }

            switch (codes[c]) {
                case 1: // backslash
                    c = _decodeEscaped();
                    break;

                case 2: // 2-byte UTF
                    c = _decodeUtf8_2(c);
                    break;

                case 3: // 3-byte UTF
                    if ((_inputEnd - _inputPtr) >= 2) {
                        c = _decodeUtf8_3fast(c);
                    } else {
                        c = _decodeUtf8_3(c);
                    }
                    break;

                case 4: // 4-byte UTF
                    c = _decodeUtf8_4(c);
                    // Let's add first part right away:
                    outBuf[outPtr++] = (char) (0xD800 | (c >> 10));
                    if (outPtr >= outBuf.length) {
                        outBuf = _textBuffer.finishCurrentSegment();
                        outPtr = 0;
                    }
                    c = 0xDC00 | (c & 0x3FF);
                    // And let the other char output down below
                    break;

                default:
                    if (c < INT_SPACE) {
                        // As per [JACKSON-208], call can now return:
                        _throwUnquotedSpace(c, "string value");
                    } else {
                        // Is this good enough error message?
                        _reportInvalidChar(c);
                    }
            }
            // Need more room?
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            // Ok, let's add char to output:
            outBuf[outPtr++] = (char) c;
        }
        _textBuffer.setCurrentLength(outPtr);
    }

    /**
     * Method called to skim through rest of unparsed String value,
     * if it is not needed. This can be done bit faster if contents
     * need not be stored for future access.
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems (invalid String value)
     */
    protected void _skipString() throws IOException {
        _tokenIncomplete = false;

        // Need to be fully UTF-8 aware here:
        final int[] codes = INPUT_CODES_UTF8;
        final byte[] inputBuffer = _inputBuffer;

        while (true) {
            int c;

            ascii_loop: while (true) {
                int ptr = _inputPtr;
                int max = _inputEnd;
                if (ptr >= max) {
                    _loadMoreGuaranteed();
                    ptr = _inputPtr;
                    max = _inputEnd;
                }
                while (ptr < max) {
                    c = inputBuffer[ptr++] & 0xFF;
                    if (codes[c] != 0) {
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                }
                _inputPtr = ptr;
            }
            // Ok: end marker, escape or multi-byte?
            if (c == INT_QUOTE) {
                break;
            }

            switch (codes[c]) {
                case 1: // backslash
                    _decodeEscaped();
                    break;

                case 2: // 2-byte UTF
                    _skipUtf8_2();
                    break;

                case 3: // 3-byte UTF
                    _skipUtf8_3();
                    break;

                case 4: // 4-byte UTF
                    _skipUtf8_4();
                    break;

                default:
                    if (c < INT_SPACE) {
                        _throwUnquotedSpace(c, "string value");
                    } else {
                        // Is this good enough error message?
                        _reportInvalidChar(c);
                    }
            }
        }
    }

    /**
     * Method for handling cases where first non-space character
     * of an expected value token is not legal for standard JSON content.
     *
     * @param c First undecoded character of possible "odd value" to decode
     *
     * @return Type of value decoded, if allowed and successful
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems (invalid white space)
     */
    protected JsonToken _handleUnexpectedValue(int c) throws IOException {
        // Most likely an error, unless we are to allow single-quote-strings
        switch (c) {
            /* This check proceeds only if `Feature.ALLOW_MISSING_VALUES` is enabled;
             * it is for missing values. In case of missing values in an array the next token
             * will be either ',' or ']'. This case, decrements the already incremented _inputPtr
             * in the buffer in case of comma (`,`) so that the existing flow goes back to checking
             * the next token which will be comma again and  it parsing continues.
             * Also the case returns NULL as current token in case of ',' or ']'.
             */
            case ']':
                if (!_parsingContext.inArray()) {
                    break;
                }
                // fall through
            case ',':
                // 28-Mar-2016: [core#116]: If Feature.ALLOW_MISSING_VALUES is enabled
                //   we may allow "missing values", that is, encountering a trailing
                //   comma or closing marker where value would be expected
                // 11-May-2020, tatu: [core#616] No commas in root level
                if (!_parsingContext.inRoot()) {
                    if ((_features & FEAT_MASK_ALLOW_MISSING) != 0) {
                        --_inputPtr;
                        return JsonToken.VALUE_NULL;
                    }
                }
                // fall through
            case '}':
                // Error: neither is valid at this point; valid closers have
                // been handled earlier
                _reportUnexpectedChar(c, "expected a value");
            case '\'':
                if ((_features & FEAT_MASK_ALLOW_SINGLE_QUOTES) != 0) {
                    return _handleApos();
                }
                break;

            case 'N':
                _matchToken("NaN", 1);
                if ((_features & FEAT_MASK_NON_NUM_NUMBERS) != 0) {
                    return resetAsNaN("NaN", Double.NaN);
                }
                _reportError("Non-standard token 'NaN': enable `JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS` to allow");
                break;

            case 'I':
                _matchToken("Infinity", 1);
                if ((_features & FEAT_MASK_NON_NUM_NUMBERS) != 0) {
                    return resetAsNaN("Infinity", Double.POSITIVE_INFINITY);
                }
                _reportError(
                    "Non-standard token 'Infinity': enable `JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS` to allow");
                break;

            case '+': // note: '-' is taken as number
                if (_inputPtr >= _inputEnd) {
                    if (!_loadMore()) {
                        _reportInvalidEOFInValue(JsonToken.VALUE_NUMBER_INT);
                    }
                }
                return _handleInvalidNumberStart(_inputBuffer[_inputPtr++] & 0xFF, false);
        }
        // [core#77] Try to decode most likely token
        if (Character.isJavaIdentifierStart(c)) {
            _reportInvalidToken("" + ((char) c), _validJsonTokenList());
        }
        // but if it doesn't look like a token:
        _reportUnexpectedChar(c, "expected a valid value " + _validJsonValueList());
        return null;
    }

    protected JsonToken _handleApos() throws IOException {
        int c;
        // Otherwise almost verbatim copy of _finishString()
        int outPtr = 0;
        char[] outBuf = _textBuffer.emptyAndGetCurrentSegment();

        // Here we do want to do full decoding, hence:
        final int[] codes = INPUT_CODES_UTF8;
        final byte[] inputBuffer = _inputBuffer;

        main_loop: while (true) {
            // Then the tight ascii non-funny-char loop:
            ascii_loop: while (true) {
                if (_inputPtr >= _inputEnd) {
                    _loadMoreGuaranteed();
                }
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                int max = _inputEnd;
                {
                    int max2 = _inputPtr + (outBuf.length - outPtr);
                    if (max2 < max) {
                        max = max2;
                    }
                }
                while (_inputPtr < max) {
                    c = inputBuffer[_inputPtr++] & 0xFF;
                    if (c == INT_APOS) {
                        break main_loop;
                    }
                    if ((codes[c] != 0)
                        // 13-Oct-2021, tatu: [core#721] Alas, regular quote is included as
                        //    special, need to ignore here
                        && (c != INT_QUOTE)) {
                        break ascii_loop;
                    }
                    outBuf[outPtr++] = (char) c;
                }
            }

            switch (codes[c]) {
                case 1: // backslash
                    c = _decodeEscaped();
                    break;

                case 2: // 2-byte UTF
                    c = _decodeUtf8_2(c);
                    break;

                case 3: // 3-byte UTF
                    if ((_inputEnd - _inputPtr) >= 2) {
                        c = _decodeUtf8_3fast(c);
                    } else {
                        c = _decodeUtf8_3(c);
                    }
                    break;

                case 4: // 4-byte UTF
                    c = _decodeUtf8_4(c);
                    // Let's add first part right away:
                    outBuf[outPtr++] = (char) (0xD800 | (c >> 10));
                    if (outPtr >= outBuf.length) {
                        outBuf = _textBuffer.finishCurrentSegment();
                        outPtr = 0;
                    }
                    c = 0xDC00 | (c & 0x3FF);
                    // And let the other char output down below
                    break;

                default:
                    if (c < INT_SPACE) {
                        _throwUnquotedSpace(c, "string value");
                    }
                    // Is this good enough error message?
                    _reportInvalidChar(c);
            }
            // Need more room?
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            // Ok, let's add char to output:
            outBuf[outPtr++] = (char) c;
        }
        _textBuffer.setCurrentLength(outPtr);

        return JsonToken.VALUE_STRING;
    }

    /*
    /**********************************************************
    /* Internal methods, well-known token decoding
    /**********************************************************
     */

    protected JsonToken _handleInvalidNumberStart(int ch, final boolean neg) throws IOException {
        while (ch == 'I') {
            if (_inputPtr >= _inputEnd) {
                if (!_loadMore()) {
                    _reportInvalidEOFInValue(JsonToken.VALUE_NUMBER_FLOAT); // possibly?
                }
            }
            ch = _inputBuffer[_inputPtr++];
            String match;
            if (ch == 'N') {
                match = neg ? "-INF" : "+INF";
            } else if (ch == 'n') {
                match = neg ? "-Infinity" : "+Infinity";
            } else {
                break;
            }
            _matchToken(match, 3);
            if ((_features & FEAT_MASK_NON_NUM_NUMBERS) != 0) {
                return resetAsNaN(match, neg ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
            }
            _reportError("Non-standard token '%s': enable `JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS` to allow", match);
        }
        if (!isEnabled(JsonReadFeature.ALLOW_LEADING_PLUS_SIGN_FOR_NUMBERS.mappedFeature()) && !neg) {
            _reportUnexpectedNumberChar('+',
                "JSON spec does not allow numbers to have plus signs: enable `JsonReadFeature.ALLOW_LEADING_PLUS_SIGN_FOR_NUMBERS` to allow");
        }
        final String message = neg
            ? "expected digit (0-9) to follow minus sign, for valid numeric value"
            : "expected digit (0-9) for valid numeric value";
        _reportUnexpectedNumberChar(ch, message);
        return null;
    }

    // NOTE: first character already decoded
    protected final void _matchTrue() throws IOException {
        int ptr = _inputPtr;
        if ((ptr + 3) < _inputEnd) {
            byte[] buf = _inputBuffer;
            if ((buf[ptr++] == 'r') && (buf[ptr++] == 'u') && (buf[ptr++] == 'e')) {
                int ch = buf[ptr] & 0xFF;
                if (ch < INT_0 || (ch | 0x20) == INT_RCURLY) { //  < '0' || ~ '}]' expected/allowed chars
                    _inputPtr = ptr;
                    return;
                }
            }
        }
        _matchToken2("true", 1);
    }

    protected final void _matchFalse() throws IOException {
        int ptr = _inputPtr;
        if ((ptr + 4) < _inputEnd) {
            byte[] buf = _inputBuffer;
            if ((buf[ptr++] == 'a') && (buf[ptr++] == 'l') && (buf[ptr++] == 's') && (buf[ptr++] == 'e')) {
                int ch = buf[ptr] & 0xFF;
                if (ch < INT_0 || (ch | 0x20) == INT_RCURLY) { //  < '0' || ~ '}]' expected/allowed chars
                    _inputPtr = ptr;
                    return;
                }
            }
        }
        _matchToken2("false", 1);
    }

    protected final void _matchNull() throws IOException {
        int ptr = _inputPtr;
        if ((ptr + 3) < _inputEnd) {
            byte[] buf = _inputBuffer;
            if ((buf[ptr++] == 'u') && (buf[ptr++] == 'l') && (buf[ptr++] == 'l')) {
                int ch = buf[ptr] & 0xFF;
                if (ch < INT_0 || (ch | 0x20) == INT_RCURLY) { //  < '0' || ~ '}]' expected/allowed chars
                    _inputPtr = ptr;
                    return;
                }
            }
        }
        _matchToken2("null", 1);
    }

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

        int ch = _inputBuffer[_inputPtr] & 0xFF;
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
        int ch = _inputBuffer[_inputPtr] & 0xFF;
        if (ch >= '0' && ch != ']' && ch != '}') { // expected/allowed chars
            _checkMatchEnd(matchStr, i, ch);
        }
    }

    private void _checkMatchEnd(String matchStr, int i, int ch) throws IOException {
        // but actually only alphanums are problematic
        char c = (char) _decodeCharForError(ch);
        if (Character.isJavaIdentifierPart(c)) {
            _reportInvalidToken(matchStr.substring(0, i));
        }
    }

    /*
    /**********************************************************
    /* Internal methods, ws skipping, escape/unescape
    /**********************************************************
     */

    private int _skipWS() throws IOException {
        while (_inputPtr < _inputEnd) {
            int i = _inputBuffer[_inputPtr++] & 0xFF;
            if (i > INT_SPACE) {
                if (i == INT_SLASH || i == INT_HASH) {
                    --_inputPtr;
                    return _skipWS2();
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
        return _skipWS2();
    }

    private int _skipWS2() throws IOException {
        while (_inputPtr < _inputEnd || _loadMore()) {
            int i = _inputBuffer[_inputPtr++] & 0xFF;
            if (i > INT_SPACE) {
                if (i == INT_SLASH) {
                    _skipComment();
                    continue;
                }
                if (i == INT_HASH) {
                    if (_skipYAMLComment()) {
                        continue;
                    }
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
        int i = _inputBuffer[_inputPtr++] & 0xFF;
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
            i = _inputBuffer[_inputPtr++] & 0xFF;
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
        while ((_inputPtr < _inputEnd) || _loadMore()) {
            int i = _inputBuffer[_inputPtr++] & 0xFF;
            if (i > INT_SPACE) {
                if (i == INT_SLASH) {
                    _skipComment();
                    continue;
                }
                if (i == INT_HASH) {
                    if (_skipYAMLComment()) {
                        continue;
                    }
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
        // We ran out of input...
        return _eofAsNextChar();
    }

    private int _skipColon() throws IOException {
        if ((_inputPtr + 4) >= _inputEnd) {
            return _skipColon2(false);
        }
        // Fast path: colon with optional single-space/tab before and/or after:
        int i = _inputBuffer[_inputPtr];
        if (i == INT_COLON) { // common case, no leading space
            i = _inputBuffer[++_inputPtr];
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
        if (i == INT_SPACE || i == INT_TAB) {
            i = _inputBuffer[++_inputPtr];
        }
        if (i == INT_COLON) {
            i = _inputBuffer[++_inputPtr];
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
            int i = _inputBuffer[_inputPtr++] & 0xFF;

            if (i > INT_SPACE) {
                if (i == INT_SLASH) {
                    _skipComment();
                    continue;
                }
                if (i == INT_HASH) {
                    if (_skipYAMLComment()) {
                        continue;
                    }
                }
                if (gotColon) {
                    return i;
                }
                if (i != INT_COLON) {
                    _reportUnexpectedChar(i, "was expecting a colon to separate field name and value");
                }
                gotColon = true;
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
        _reportInvalidEOF(" within/between " + _parsingContext.typeDesc() + " entries", null);
        return -1;
    }

    private void _skipComment() throws IOException {
        if ((_features & FEAT_MASK_ALLOW_JAVA_COMMENTS) == 0) {
            _reportUnexpectedChar('/',
                "maybe a (non-standard) comment? (not recognized as one since Feature 'ALLOW_COMMENTS' not enabled for parser)");
        }
        // First: check which comment (if either) it is:
        if (_inputPtr >= _inputEnd && !_loadMore()) {
            _reportInvalidEOF(" in a comment", null);
        }
        int c = _inputBuffer[_inputPtr++] & 0xFF;
        if (c == INT_SLASH) {
            _skipLine();
        } else if (c == INT_ASTERISK) {
            _skipCComment();
        } else {
            _reportUnexpectedChar(c, "was expecting either '*' or '/' for a comment");
        }
    }

    private void _skipCComment() throws IOException {
        // Need to be UTF-8 aware here to decode content (for skipping)
        final int[] codes = CharTypes.getInputCodeComment();

        // Ok: need the matching '*/'
        main_loop: while ((_inputPtr < _inputEnd) || _loadMore()) {
            int i = _inputBuffer[_inputPtr++] & 0xFF;
            int code = codes[i];
            if (code != 0) {
                switch (code) {
                    case '*':
                        if (_inputPtr >= _inputEnd && !_loadMore()) {
                            break main_loop;
                        }
                        if (_inputBuffer[_inputPtr] == INT_SLASH) {
                            ++_inputPtr;
                            return;
                        }
                        break;

                    case INT_LF:
                        ++_currInputRow;
                        _currInputRowStart = _inputPtr;
                        break;

                    case INT_CR:
                        _skipCR();
                        break;

                    case 2: // 2-byte UTF
                        _skipUtf8_2();
                        break;

                    case 3: // 3-byte UTF
                        _skipUtf8_3();
                        break;

                    case 4: // 4-byte UTF
                        _skipUtf8_4();
                        break;

                    default: // e.g. -1
                        // Is this good enough error message?
                        _reportInvalidChar(i);
                }
            }
        }
        _reportInvalidEOF(" in a comment", null);
    }

    private boolean _skipYAMLComment() throws IOException {
        if ((_features & FEAT_MASK_ALLOW_YAML_COMMENTS) == 0) {
            return false;
        }
        _skipLine();
        return true;
    }

    // Method for skipping contents of an input line; usually for CPP
    // and YAML style comments.
    private void _skipLine() throws IOException {
        // Ok: need to find EOF or linefeed
        final int[] codes = CharTypes.getInputCodeComment();
        while ((_inputPtr < _inputEnd) || _loadMore()) {
            int i = _inputBuffer[_inputPtr++] & 0xFF;
            int code = codes[i];
            if (code != 0) {
                switch (code) {
                    case INT_LF:
                        ++_currInputRow;
                        _currInputRowStart = _inputPtr;
                        return;

                    case INT_CR:
                        _skipCR();
                        return;

                    case '*': // nop for these comments
                        break;

                    case 2: // 2-byte UTF
                        _skipUtf8_2();
                        break;

                    case 3: // 3-byte UTF
                        _skipUtf8_3();
                        break;

                    case 4: // 4-byte UTF
                        _skipUtf8_4();
                        break;

                    default: // e.g. -1
                        if (code < 0) {
                            // Is this good enough error message?
                            _reportInvalidChar(i);
                        }
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
        int c = _inputBuffer[_inputPtr++];

        switch (c) {
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
                return (char) c;

            case 'u': // and finally hex-escaped
                break;

            default:
                return _handleUnrecognizedCharacterEscape((char) _decodeCharForError(c));
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
                _reportUnexpectedChar(ch & 0xFF, "expected a hex-digit for character escape sequence");
            }
            value = (value << 4) | digit;
        }
        return (char) value;
    }

    protected int _decodeCharForError(int firstByte) throws IOException {
        int c = firstByte & 0xFF;
        if (c > 0x7F) { // if >= 0, is ascii and fine as is
            int needed;

            // Ok; if we end here, we got multi-byte combination
            if ((c & 0xE0) == 0xC0) { // 2 bytes (0x0080 - 0x07FF)
                c &= 0x1F;
                needed = 1;
            } else if ((c & 0xF0) == 0xE0) { // 3 bytes (0x0800 - 0xFFFF)
                c &= 0x0F;
                needed = 2;
            } else if ((c & 0xF8) == 0xF0) {
                // 4 bytes; double-char with surrogates and all...
                c &= 0x07;
                needed = 3;
            } else {
                _reportInvalidInitial(c & 0xFF);
                needed = 1; // never gets here
            }

            int d = nextByte();
            if ((d & 0xC0) != 0x080) {
                _reportInvalidOther(d & 0xFF);
            }
            c = (c << 6) | (d & 0x3F);

            if (needed > 1) { // needed == 1 means 2 bytes total
                d = nextByte(); // 3rd byte
                if ((d & 0xC0) != 0x080) {
                    _reportInvalidOther(d & 0xFF);
                }
                c = (c << 6) | (d & 0x3F);
                if (needed > 2) { // 4 bytes? (need surrogates)
                    d = nextByte();
                    if ((d & 0xC0) != 0x080) {
                        _reportInvalidOther(d & 0xFF);
                    }
                    c = (c << 6) | (d & 0x3F);
                }
            }
        }
        return c;
    }

    /*
    /**********************************************************
    /* Internal methods,UTF8 decoding
    /**********************************************************
     */

    private int _decodeUtf8_2(int c) throws IOException {
        if (_inputPtr >= _inputEnd) {
            _loadMoreGuaranteed();
        }
        int d = _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF, _inputPtr);
        }
        return ((c & 0x1F) << 6) | (d & 0x3F);
    }

    private int _decodeUtf8_3(int c1) throws IOException {
        if (_inputPtr >= _inputEnd) {
            _loadMoreGuaranteed();
        }
        c1 &= 0x0F;
        int d = _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF, _inputPtr);
        }
        int c = (c1 << 6) | (d & 0x3F);
        if (_inputPtr >= _inputEnd) {
            _loadMoreGuaranteed();
        }
        d = _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF, _inputPtr);
        }
        c = (c << 6) | (d & 0x3F);
        return c;
    }

    private int _decodeUtf8_3fast(int c1) throws IOException {
        c1 &= 0x0F;
        int d = _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF, _inputPtr);
        }
        int c = (c1 << 6) | (d & 0x3F);
        d = _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF, _inputPtr);
        }
        c = (c << 6) | (d & 0x3F);
        return c;
    }

    // @return Character value <b>minus 0x10000</c>; this so that caller
    //    can readily expand it to actual surrogates
    private int _decodeUtf8_4(int c) throws IOException {
        if (_inputPtr >= _inputEnd) {
            _loadMoreGuaranteed();
        }
        int d = _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF, _inputPtr);
        }
        c = ((c & 0x07) << 6) | (d & 0x3F);

        if (_inputPtr >= _inputEnd) {
            _loadMoreGuaranteed();
        }
        d = _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF, _inputPtr);
        }
        c = (c << 6) | (d & 0x3F);
        if (_inputPtr >= _inputEnd) {
            _loadMoreGuaranteed();
        }
        d = _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF, _inputPtr);
        }

        /* note: won't change it to negative here, since caller
         * already knows it'll need a surrogate
         */
        return ((c << 6) | (d & 0x3F)) - 0x10000;
    }

    private void _skipUtf8_2() throws IOException {
        if (_inputPtr >= _inputEnd) {
            _loadMoreGuaranteed();
        }
        int c = _inputBuffer[_inputPtr++];
        if ((c & 0xC0) != 0x080) {
            _reportInvalidOther(c & 0xFF, _inputPtr);
        }
    }

    /* Alas, can't heavily optimize skipping, since we still have to
     * do validity checks...
     */
    private void _skipUtf8_3() throws IOException {
        if (_inputPtr >= _inputEnd) {
            _loadMoreGuaranteed();
        }
        //c &= 0x0F;
        int c = _inputBuffer[_inputPtr++];
        if ((c & 0xC0) != 0x080) {
            _reportInvalidOther(c & 0xFF, _inputPtr);
        }
        if (_inputPtr >= _inputEnd) {
            _loadMoreGuaranteed();
        }
        c = _inputBuffer[_inputPtr++];
        if ((c & 0xC0) != 0x080) {
            _reportInvalidOther(c & 0xFF, _inputPtr);
        }
    }

    private void _skipUtf8_4() throws IOException {
        if (_inputPtr >= _inputEnd) {
            _loadMoreGuaranteed();
        }
        int d = _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF, _inputPtr);
        }
        if (_inputPtr >= _inputEnd) {
            _loadMoreGuaranteed();
        }
        d = _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF, _inputPtr);
        }
        if (_inputPtr >= _inputEnd) {
            _loadMoreGuaranteed();
        }
        d = _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF, _inputPtr);
        }
    }

    /*
    /**********************************************************
    /* Internal methods, input loading
    /**********************************************************
     */

    // We actually need to check the character value here
    // (to see if we have \n following \r).
    protected final void _skipCR() throws IOException {
        if (_inputPtr < _inputEnd || _loadMore()) {
            if (_inputBuffer[_inputPtr] == BYTE_LF) {
                ++_inputPtr;
            }
        }
        ++_currInputRow;
        _currInputRowStart = _inputPtr;
    }

    private int nextByte() throws IOException {
        if (_inputPtr >= _inputEnd) {
            _loadMoreGuaranteed();
        }
        return _inputBuffer[_inputPtr++] & 0xFF;
    }

    /*
    /**********************************************************
    /* Internal methods, error reporting
    /**********************************************************
     */

    protected void _reportInvalidToken(String matchedPart) throws IOException {
        _reportInvalidToken(matchedPart, _validJsonTokenList());
    }

    protected void _reportInvalidToken(String matchedPart, String msg) throws IOException {
        /* Let's just try to find what appears to be the token, using
         * regular Java identifier character rules. It's just a heuristic,
         * nothing fancy here (nor fast).
         */
        StringBuilder sb = new StringBuilder(matchedPart);
        while ((_inputPtr < _inputEnd) || _loadMore()) {
            int i = _inputBuffer[_inputPtr++];
            char c = (char) _decodeCharForError(i);
            if (!Character.isJavaIdentifierPart(c)) {
                // 11-Jan-2016, tatu: note: we will fully consume the character,
                //   included or not, so if recovery was possible, it'd be off-by-one...
                // 04-Apr-2021, tatu: ... and the reason we can't do much about it is
                //   because it may be multi-byte UTF-8 character (and even if saved
                //   offset, on buffer boundary it would not work, still)
                break;
            }
            sb.append(c);
            if (sb.length() >= _ioContext.errorReportConfiguration().getMaxErrorTokenLength()) {
                sb.append("...");
                break;
            }
        }
        _reportError(sb, msg);
    }

    protected void _reportInvalidChar(int c) throws JsonParseException {
        // Either invalid WS or illegal UTF-8 start char
        if (c < INT_SPACE) {
            _throwInvalidSpace(c);
        }
        _reportInvalidInitial(c);
    }

    protected void _reportInvalidInitial(int mask) throws JsonParseException {
        _reportError("Invalid UTF-8 start byte 0x" + Integer.toHexString(mask));
    }

    protected void _reportInvalidOther(int mask) throws JsonParseException {
        _reportError("Invalid UTF-8 middle byte 0x" + Integer.toHexString(mask));
    }

    protected void _reportInvalidOther(int mask, int ptr) throws JsonParseException {
        _inputPtr = ptr;
        _reportInvalidOther(mask);
    }

    /*
    /**********************************************************
    /* Internal methods, binary access
    /**********************************************************
     */

    /**
     * Efficient handling for incremental parsing of base64-encoded
     * textual content.
     *
     * @param b64variant Type of base64 encoding expected in context
     *
     * @return Fully decoded value of base64 content
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems (invalid content)
     */
    @SuppressWarnings("resource")
    protected final byte[] _decodeBase64(Base64Variant b64variant) throws IOException {
        ByteArrayBuilder builder = _getByteArrayBuilder();

        while (true) {
            // first, we'll skip preceding white space, if any
            int ch;
            do {
                if (_inputPtr >= _inputEnd) {
                    _loadMoreGuaranteed();
                }
                ch = _inputBuffer[_inputPtr++] & 0xFF;
            } while (ch <= INT_SPACE);
            int bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) { // reached the end, fair and square?
                if (ch == INT_QUOTE) {
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
            ch = _inputBuffer[_inputPtr++] & 0xFF;
            bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
                bits = _decodeBase64Escape(b64variant, ch, 1);
            }
            decodedData = (decodedData << 6) | bits;

            // third base64 char; can be padding, but not ws
            if (_inputPtr >= _inputEnd) {
                _loadMoreGuaranteed();
            }
            ch = _inputBuffer[_inputPtr++] & 0xFF;
            bits = b64variant.decodeBase64Char(ch);

            // First branch: can get padding (-> 1 byte)
            if (bits < 0) {
                if (bits != Base64Variant.BASE64_VALUE_PADDING) {
                    // could also just be 'missing'  padding
                    if (ch == INT_QUOTE) {
                        decodedData >>= 4;
                        builder.append(decodedData);
                        if (b64variant.requiresPaddingOnRead()) {
                            --_inputPtr; // to keep parser state bit more consistent
                            _handleBase64MissingPadding(b64variant);
                        }
                        return builder.toByteArray();
                    }
                    bits = _decodeBase64Escape(b64variant, ch, 2);
                }
                if (bits == Base64Variant.BASE64_VALUE_PADDING) {
                    // Ok, must get padding
                    if (_inputPtr >= _inputEnd) {
                        _loadMoreGuaranteed();
                    }
                    ch = _inputBuffer[_inputPtr++] & 0xFF;
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
            }
            // Nope, 2 or 3 bytes
            decodedData = (decodedData << 6) | bits;
            // fourth and last base64 char; can be padding, but not ws
            if (_inputPtr >= _inputEnd) {
                _loadMoreGuaranteed();
            }
            ch = _inputBuffer[_inputPtr++] & 0xFF;
            bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
                if (bits != Base64Variant.BASE64_VALUE_PADDING) {
                    // could also just be 'missing'  padding
                    if (ch == INT_QUOTE) {
                        decodedData >>= 2;
                        builder.appendTwoBytes(decodedData);
                        if (b64variant.requiresPaddingOnRead()) {
                            --_inputPtr; // to keep parser state bit more consistent
                            _handleBase64MissingPadding(b64variant);
                        }
                        return builder.toByteArray();
                    }
                    bits = _decodeBase64Escape(b64variant, ch, 3);
                }
                if (bits == Base64Variant.BASE64_VALUE_PADDING) {
                    // With padding we only get 2 bytes; but we have to shift it
                    // a bit so it is identical to triplet case with partial output.
                    // 3 chars gives 3x6 == 18 bits, of which 2 are dummies, need to discard:
                    decodedData >>= 2;
                    builder.appendTwoBytes(decodedData);
                    continue;
                }
            }
            // otherwise, our triplet is now complete
            decodedData = (decodedData << 6) | bits;
            builder.appendThreeBytes(decodedData);
        }
    }

    /*
    /**********************************************************
    /* Improved location updating (refactored in 2.7)
    /**********************************************************
     */

    @Override
    public JsonLocation currentLocation() {
        int col = _inputPtr - _currInputRowStart + 1; // 1-based
        return new JsonLocation(_contentReference(), _currInputProcessed + _inputPtr, -1L, // bytes, chars
            _currInputRow, col);
    }

    @Override // @since 2.17
    protected JsonLocation _currentLocationMinusOne() {
        final int prevInputPtr = _inputPtr - 1;
        final int col = prevInputPtr - _currInputRowStart + 1; // 1-based
        return new JsonLocation(_contentReference(), _currInputProcessed + prevInputPtr, -1L, // bytes, chars
            _currInputRow, col);
    }

    @Override
    public JsonLocation currentTokenLocation() {
        if (_currToken == JsonToken.FIELD_NAME) {
            long total = _currInputProcessed + (_nameStartOffset - 1);
            return new JsonLocation(_contentReference(), total, -1L, _nameStartRow, _nameStartCol);
        }
        return new JsonLocation(_contentReference(), _tokenInputTotal - 1, -1L, _tokenInputRow, _tokenInputCol);
    }

    // @since 2.7
    private void _updateLocation() {
        _tokenInputRow = _currInputRow;
        final int ptr = _inputPtr;
        _tokenInputTotal = _currInputProcessed + ptr;
        _tokenInputCol = ptr - _currInputRowStart;
    }

    // @since 2.7
    private void _updateNameLocation() {
        _nameStartRow = _currInputRow;
        final int ptr = _inputPtr;
        _nameStartOffset = ptr;
        _nameStartCol = ptr - _currInputRowStart;
    }

    /*
    /**********************************************************
    /* Internal methods, other
    /**********************************************************
     */

    private JsonToken _closeScope(int i) throws JsonParseException {
        if (i == INT_RCURLY) {
            _closeObjectScope();
            return (_currToken = JsonToken.END_OBJECT);
        }
        _closeArrayScope();
        return (_currToken = JsonToken.END_ARRAY);
    }

    private void _closeArrayScope() throws JsonParseException {
        _updateLocation();
        if (!_parsingContext.inArray()) {
            _reportMismatchedEndMarker(']', '}');
        }
        _parsingContext = _parsingContext.clearAndGetParent();
    }

    private void _closeObjectScope() throws JsonParseException {
        _updateLocation();
        if (!_parsingContext.inObject()) {
            _reportMismatchedEndMarker('}', ']');
        }
        _parsingContext = _parsingContext.clearAndGetParent();
    }
}
