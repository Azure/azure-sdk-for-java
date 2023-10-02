// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.json.async;

import java.io.*;

import com.typespec.json.implementation.jackson.core.*;
import com.typespec.json.implementation.jackson.core.base.ParserBase;
import com.typespec.json.implementation.jackson.core.io.IOContext;
import com.typespec.json.implementation.jackson.core.json.JsonReadContext;
import com.typespec.json.implementation.jackson.core.sym.ByteQuadsCanonicalizer;
import com.typespec.json.implementation.jackson.core.util.ByteArrayBuilder;
import com.typespec.json.implementation.jackson.core.util.JacksonFeatureSet;

import static com.typespec.json.implementation.jackson.core.JsonTokenId.*;

/**
 * Intermediate base class for non-blocking JSON parsers.
 */
public abstract class NonBlockingJsonParserBase
    extends ParserBase
{
    /*
    /**********************************************************************
    /* Major state constants
    /**********************************************************************
     */

    /**
     * State right after parser has been constructed, before seeing the first byte
     * to handle possible (but optional) BOM.
     */
    protected final static int MAJOR_INITIAL = 0;

    /**
     * State right after parser a root value has been
     * finished, but next token has not yet been recognized.
     */
    protected final static int MAJOR_ROOT = 1;

    protected final static int MAJOR_OBJECT_FIELD_FIRST = 2;
    protected final static int MAJOR_OBJECT_FIELD_NEXT = 3;

    protected final static int MAJOR_OBJECT_VALUE = 4;

    protected final static int MAJOR_ARRAY_ELEMENT_FIRST = 5;
    protected final static int MAJOR_ARRAY_ELEMENT_NEXT = 6;

    /**
     * State after non-blocking input source has indicated that no more input
     * is forthcoming AND we have exhausted all the input
     */
    protected final static int MAJOR_CLOSED = 7;

    /*
    /**********************************************************************
    /* Minor state constants
    /**********************************************************************
     */

    /**
     * State in which part of (UTF-8) BOM has been detected, but not yet completely.
     */
    protected final static int MINOR_ROOT_BOM = 1;

    /**
     * State between root-level value, waiting for at least one white-space
     * character as separator
     */
    protected final static int MINOR_ROOT_NEED_SEPARATOR = 2;

    /**
     * State between root-level value, having processed at least one white-space
     * character, and expecting either more, start of a value, or end of input
     * stream.
     */
    protected final static int MINOR_ROOT_GOT_SEPARATOR = 3;

    // state before field name itself, waiting for quote (or unquoted name)
    protected final static int MINOR_FIELD_LEADING_WS = 4;
    // state before field name, expecting comma (or closing curly), then field name
    protected final static int MINOR_FIELD_LEADING_COMMA = 5;

    // State within regular (double-quoted) field name
    protected final static int MINOR_FIELD_NAME = 7;
    // State within regular (double-quoted) field name, within escape (having
    // encountered either just backslash, or backslash and 'u' and 0 - 3 hex digits,
    protected final static int MINOR_FIELD_NAME_ESCAPE = 8;

    protected final static int MINOR_FIELD_APOS_NAME = 9;
    protected final static int MINOR_FIELD_UNQUOTED_NAME = 10;

    protected final static int MINOR_VALUE_LEADING_WS = 12;
    protected final static int MINOR_VALUE_EXPECTING_COMMA = 13;
    protected final static int MINOR_VALUE_EXPECTING_COLON = 14;
    protected final static int MINOR_VALUE_WS_AFTER_COMMA = 15;

    protected final static int MINOR_VALUE_TOKEN_NULL = 16;
    protected final static int MINOR_VALUE_TOKEN_TRUE = 17;
    protected final static int MINOR_VALUE_TOKEN_FALSE = 18;

    protected final static int MINOR_VALUE_TOKEN_NON_STD = 19;

    protected final static int MINOR_NUMBER_MINUS = 23;
    protected final static int MINOR_NUMBER_ZERO = 24; // zero as first, possibly trimming multiple
    protected final static int MINOR_NUMBER_MINUSZERO = 25; // "-0" (and possibly more zeroes) receive
    protected final static int MINOR_NUMBER_INTEGER_DIGITS = 26;

    protected final static int MINOR_NUMBER_FRACTION_DIGITS = 30;
    protected final static int MINOR_NUMBER_EXPONENT_MARKER = 31;
    protected final static int MINOR_NUMBER_EXPONENT_DIGITS = 32;

    protected final static int MINOR_VALUE_STRING = 40;
    protected final static int MINOR_VALUE_STRING_ESCAPE = 41;
    protected final static int MINOR_VALUE_STRING_UTF8_2 = 42;
    protected final static int MINOR_VALUE_STRING_UTF8_3 = 43;
    protected final static int MINOR_VALUE_STRING_UTF8_4 = 44;
    protected final static int MINOR_VALUE_APOS_STRING = 45;

    /**
     * Special state at which point decoding of a non-quoted token has encountered
     * a problem; that is, either not matching fully (like "truf" instead of "true",
     * at "tru"), or not having trailing separator (or end of input), like "trueful".
     * Attempt is made, then, to decode likely full input token to report suitable
     * error.
     */
    protected final static int MINOR_VALUE_TOKEN_ERROR = 50;

    protected final static int MINOR_COMMENT_LEADING_SLASH = 51;
    protected final static int MINOR_COMMENT_CLOSING_ASTERISK = 52;
    protected final static int MINOR_COMMENT_C = 53;
    protected final static int MINOR_COMMENT_CPP = 54;
    protected final static int MINOR_COMMENT_YAML = 55;
    
    /*
    /**********************************************************************
    /* Helper objects, symbols (field names)
    /**********************************************************************
     */

    /**
     * Symbol table that contains field names encountered so far
     */
    final protected ByteQuadsCanonicalizer _symbols;

    /**
     * Temporary buffer used for name parsing.
     */
    protected int[] _quadBuffer = new int[8];

    protected int _quadLength;

    protected int _quad1;

    protected int _pending32;

    protected int _pendingBytes;

    protected int _quoted32;

    protected int _quotedDigits;

    /*
    /**********************************************************************
    /* Additional parsing state
    /**********************************************************************
     */

    /**
     * Current main decoding state within logical tree
     */
    protected int _majorState;

    /**
     * Value of {@link #_majorState} after completing a scalar value
     */
    protected int _majorStateAfterValue;

    /**
     * Additional indicator within state; contextually relevant for just that state
     */
    protected int _minorState;

    /**
     * Secondary minor state indicator used during decoding of escapes and/or
     * multi-byte Unicode characters
     */
    protected int _minorStateAfterSplit;

    /**
     * Flag that is sent when calling application indicates that there will
     * be no more input to parse.
     */
    protected boolean _endOfInput = false;

    /*
    /**********************************************************************
    /* Additional parsing state: non-standard tokens
    /**********************************************************************
     */

    protected final static int NON_STD_TOKEN_NAN = 0;

    protected final static int NON_STD_TOKEN_INFINITY = 1;
    protected final static int NON_STD_TOKEN_PLUS_INFINITY = 2;
    protected final static int NON_STD_TOKEN_MINUS_INFINITY = 3;

    protected final static String[] NON_STD_TOKENS = new String[] {
            "NaN",
            "Infinity", "+Infinity", "-Infinity",
    };

    protected final static double[] NON_STD_TOKEN_VALUES = new double[] {
            Double.NaN,
            Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
    };
    
    /**
     * When tokenizing non-standard ("odd") tokens, this is the type to consider;
     * also works as index to actual textual representation.
     */
    protected int _nonStdTokenType;

    /*
    /**********************************************************************
    /* Location tracking, additional
    /**********************************************************************
     */

    /**
     * Since we are fed content that may or may not start at zero offset, we need
     * to keep track of the first byte within that buffer, to be able to calculate
     * logical offset within input "stream"
     */
    protected int _currBufferStart = 0;
    
    /**
     * Alternate row tracker, used to keep track of position by `\r` marker
     * (whereas <code>_currInputRow</code> tracks `\n`). Used to simplify
     * tracking of linefeeds, assuming that input typically uses various
     * linefeed combinations (`\r`, `\n` or `\r\n`) consistently, in which
     * case we can simply choose max of two row candidates.
     */
    protected int _currInputRowAlt = 1;
    
    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    public NonBlockingJsonParserBase(IOContext ctxt, int parserFeatures,
            ByteQuadsCanonicalizer sym)
    {
        super(ctxt, parserFeatures);
        _symbols = sym;
        _currToken = null;
        _majorState = MAJOR_INITIAL;
        _majorStateAfterValue = MAJOR_ROOT;
    }

    @Override
    public ObjectCodec getCodec() {
        return null;
    }

    @Override
    public void setCodec(ObjectCodec c) {
        throw new UnsupportedOperationException("Can not use ObjectMapper with non-blocking parser");
    }

    @Override // since 2.9
    public boolean canParseAsync() { return true; }

    @Override // @since 2.12
    public JacksonFeatureSet<StreamReadCapability> getReadCapabilities() {
        return JSON_READ_CAPABILITIES;
    }

    /*
    /**********************************************************
    /* Test support
    /**********************************************************
     */

    protected ByteQuadsCanonicalizer symbolTableForTests() {
        return _symbols;
    }

    /*
    /**********************************************************
    /* Abstract methods from JsonParser
    /**********************************************************
     */

    @Override
    public abstract int releaseBuffered(OutputStream out) throws IOException;

    @Override
    protected void _releaseBuffers() throws IOException
    {
        super._releaseBuffers();
        // Merge found symbols, if any:
        _symbols.release();
        // any other temp buffers?
    }

    @Override
    public Object getInputSource() {
        // since input is "pushed", to traditional source...
        return null;
    }

    @Override
    protected void _closeInput() throws IOException {
        // 30-May-2017, tatu: Seems like this is the most certain way to prevent
        //    further decoding... not the optimal place, but due to inheritance
        //    hierarchy most convenient.
        _currBufferStart = 0;
        _inputEnd = 0;
    }

    /*
    /**********************************************************************
    /* Overridden methods
    /**********************************************************************
     */

    @Override
    public boolean hasTextCharacters()
    {
        if (_currToken == JsonToken.VALUE_STRING) {
            // yes; is or can be made available efficiently as char[]
            return _textBuffer.hasTextAsCharacters();
        }
        if (_currToken == JsonToken.FIELD_NAME) {
            // not necessarily; possible but:
            return _nameCopied;
        }
        // other types, no benefit from accessing as char[]
        return false;
    }

    @Override
    public JsonLocation getCurrentLocation()
    {
        int col = _inputPtr - _currInputRowStart + 1; // 1-based
        // Since we track CR and LF separately, max should gives us right answer
        int row = Math.max(_currInputRow, _currInputRowAlt);
        return new JsonLocation(_contentReference(),
                _currInputProcessed + (_inputPtr - _currBufferStart), -1L, // bytes, chars
                row, col);
    }

    @Override
    public JsonLocation getTokenLocation()
    {
        return new JsonLocation(_contentReference(),
                _tokenInputTotal, -1L, _tokenInputRow, _tokenInputCol);
    }

    /*
    /**********************************************************************
    /* Public API, access to token information, text
    /**********************************************************************
     */

    /**
     * Method for accessing textual representation of the current event;
     * if no current event (before first call to {@link #nextToken}, or
     * after encountering end-of-input), returns null.
     * Method can be called for any event.
     */
    @Override
    public String getText() throws IOException
    {
        if (_currToken == JsonToken.VALUE_STRING) {
            return _textBuffer.contentsAsString();
        }
        return _getText2(_currToken);
    }

    protected final String _getText2(JsonToken t)
    {
        if (t == null) {
            return null;
        }
        switch (t.id()) {
        case ID_NOT_AVAILABLE:
            return null;
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

    @Override // since 2.8
    public int getText(Writer writer) throws IOException
    {
        JsonToken t = _currToken;
        if (t == JsonToken.VALUE_STRING) {
            return _textBuffer.contentsToWriter(writer);
        }
        if (t == JsonToken.FIELD_NAME) {
            String n = _parsingContext.getCurrentName();
            writer.write(n);
            return n.length();
        }
        if (t != null) {
            if (t.isNumeric()) {
                return _textBuffer.contentsToWriter(writer);
            }
            if (t == JsonToken.NOT_AVAILABLE) {
                _reportError("Current token not available: can not call this method");
            }
            char[] ch = t.asCharArray();
            writer.write(ch);
            return ch.length;
        }
        return 0;
    }

    // // // Let's override default impls for improved performance
    
    // @since 2.1
    @Override
    public String getValueAsString() throws IOException
    {
        if (_currToken == JsonToken.VALUE_STRING) {
            return _textBuffer.contentsAsString();
        }
        if (_currToken == JsonToken.FIELD_NAME) {
            return getCurrentName();
        }
        return super.getValueAsString(null);
    }
    
    // @since 2.1
    @Override
    public String getValueAsString(String defValue) throws IOException
    {
        if (_currToken == JsonToken.VALUE_STRING) {
            return _textBuffer.contentsAsString();
        }
        if (_currToken == JsonToken.FIELD_NAME) {
            return getCurrentName();
        }
        return super.getValueAsString(defValue);
    }

    @Override
    public char[] getTextCharacters() throws IOException
    {
        if (_currToken != null) { // null only before/after document
            switch (_currToken.id()) {
                
            case ID_FIELD_NAME:
                if (!_nameCopied) {
                    String name = _parsingContext.getCurrentName();
                    int nameLen = name.length();
                    if (_nameCopyBuffer == null) {
                        _nameCopyBuffer = _ioContext.allocNameCopyBuffer(nameLen);
                    } else if (_nameCopyBuffer.length < nameLen) {
                        _nameCopyBuffer = new char[nameLen];
                    }
                    name.getChars(0, nameLen, _nameCopyBuffer, 0);
                    _nameCopied = true;
                }
                return _nameCopyBuffer;
    
            case ID_STRING:
                // fall through
            case ID_NUMBER_INT:
            case ID_NUMBER_FLOAT:
                return _textBuffer.getTextBuffer();
                
            default:
                return _currToken.asCharArray();
            }
        }
        return null;
    }

    @Override
    public int getTextLength() throws IOException
    {
        if (_currToken != null) { // null only before/after document
            switch (_currToken.id()) {
                
            case ID_FIELD_NAME:
                return _parsingContext.getCurrentName().length();
            case ID_STRING:
                // fall through
            case ID_NUMBER_INT:
            case ID_NUMBER_FLOAT:
                return _textBuffer.size();
                
            default:
                return _currToken.asCharArray().length;
            }
        }
        return 0;
    }

    @Override
    public int getTextOffset() throws IOException
    {
        // Most have offset of 0, only some may have other values:
        if (_currToken != null) {
            switch (_currToken.id()) {
            case ID_FIELD_NAME:
                return 0;
            case ID_STRING:
                // fall through
            case ID_NUMBER_INT:
            case ID_NUMBER_FLOAT:
                return _textBuffer.getTextOffset();
            default:
            }
        }
        return 0;
    }

    /*
    /**********************************************************************
    /* Public API, access to token information, binary
    /**********************************************************************
     */

    @Override
    public byte[] getBinaryValue(Base64Variant b64variant) throws IOException
    {
        if (_currToken != JsonToken.VALUE_STRING) {
            _reportError("Current token (%s) not VALUE_STRING or VALUE_EMBEDDED_OBJECT, can not access as binary",
                    _currToken);
        }
        if (_binaryValue == null) {
            @SuppressWarnings("resource")
            ByteArrayBuilder builder = _getByteArrayBuilder();
            _decodeBase64(getText(), builder, b64variant);
            _binaryValue = builder.toByteArray();
        }
        return _binaryValue;
    }

    @Override
    public int readBinaryValue(Base64Variant b64variant, OutputStream out) throws IOException
    {
        byte[] b = getBinaryValue(b64variant);
        out.write(b);
        return b.length;
    }

    @Override
    public Object getEmbeddedObject() throws IOException
    {
        if (_currToken == JsonToken.VALUE_EMBEDDED_OBJECT ) {
            return _binaryValue;
        }
        return null;
    }

    /*
    /**********************************************************************
    /* Handling of nested scope, state
    /**********************************************************************
     */

    protected final JsonToken _startArrayScope() throws IOException
    {
        _parsingContext = _parsingContext.createChildArrayContext(-1, -1);
        _majorState = MAJOR_ARRAY_ELEMENT_FIRST;
        _majorStateAfterValue = MAJOR_ARRAY_ELEMENT_NEXT;
        return (_currToken = JsonToken.START_ARRAY);
    }

    protected final JsonToken _startObjectScope() throws IOException
    {
        _parsingContext = _parsingContext.createChildObjectContext(-1, -1);
        _majorState = MAJOR_OBJECT_FIELD_FIRST;
        _majorStateAfterValue = MAJOR_OBJECT_FIELD_NEXT;
        return (_currToken = JsonToken.START_OBJECT);
    }

    protected final JsonToken _closeArrayScope() throws IOException
    {
        if (!_parsingContext.inArray()) {
            _reportMismatchedEndMarker(']', '}');
        }
        JsonReadContext ctxt = _parsingContext.getParent();
        _parsingContext = ctxt;
        int st;
        if (ctxt.inObject()) {
            st = MAJOR_OBJECT_FIELD_NEXT;
        } else if (ctxt.inArray()) {
            st = MAJOR_ARRAY_ELEMENT_NEXT;
        } else {
            st = MAJOR_ROOT;
        }
        _majorState = st;
        _majorStateAfterValue = st;
        return (_currToken = JsonToken.END_ARRAY);
    }

    protected final JsonToken _closeObjectScope() throws IOException
    {
        if (!_parsingContext.inObject()) {
            _reportMismatchedEndMarker('}', ']');
        }
        JsonReadContext ctxt = _parsingContext.getParent();
        _parsingContext = ctxt;
        int st;
        if (ctxt.inObject()) {
            st = MAJOR_OBJECT_FIELD_NEXT;
        } else if (ctxt.inArray()) {
            st = MAJOR_ARRAY_ELEMENT_NEXT;
        } else {
            st = MAJOR_ROOT;
        }
        _majorState = st;
        _majorStateAfterValue = st;
        return (_currToken = JsonToken.END_OBJECT);
    }

    /*
    /**********************************************************
    /* Internal methods, symbol (name) handling
    /**********************************************************
     */

    protected final String _findName(int q1, int lastQuadBytes) throws JsonParseException
    {
        q1 = _padLastQuad(q1, lastQuadBytes);
        // Usually we'll find it from the canonical symbol table already
        String name = _symbols.findName(q1);
        if (name != null) {
            return name;
        }
        // If not, more work. We'll need add stuff to buffer
        _quadBuffer[0] = q1;
        return _addName(_quadBuffer, 1, lastQuadBytes);
    }

    protected final String _findName(int q1, int q2, int lastQuadBytes) throws JsonParseException
    {
        q2 = _padLastQuad(q2, lastQuadBytes);
        // Usually we'll find it from the canonical symbol table already
        String name = _symbols.findName(q1, q2);
        if (name != null) {
            return name;
        }
        // If not, more work. We'll need add stuff to buffer
        _quadBuffer[0] = q1;
        _quadBuffer[1] = q2;
        return _addName(_quadBuffer, 2, lastQuadBytes);
    }

    protected final String _findName(int q1, int q2, int q3, int lastQuadBytes) throws JsonParseException
    {
        q3 = _padLastQuad(q3, lastQuadBytes);
        String name = _symbols.findName(q1, q2, q3);
        if (name != null) {
            return name;
        }
        int[] quads = _quadBuffer;
        quads[0] = q1;
        quads[1] = q2;
        quads[2] = _padLastQuad(q3, lastQuadBytes);
        return _addName(quads, 3, lastQuadBytes);
    }

    // This is the main workhorse method used when we take a symbol
    // table miss. It needs to demultiplex individual bytes, decode
    // multi-byte chars (if any), and then construct Name instance
    // and add it to the symbol table.
    protected final String _addName(int[] quads, int qlen, int lastQuadBytes) throws JsonParseException
    {
        /* Ok: must decode UTF-8 chars. No other validation is
         * needed, since unescaping has been done earlier as necessary
         * (as well as error reporting for unescaped control chars)
         */
        // 4 bytes per quad, except last one maybe less
        int byteLen = (qlen << 2) - 4 + lastQuadBytes;

        /* And last one is not correctly aligned (leading zero bytes instead
         * need to shift a bit, instead of trailing). Only need to shift it
         * for UTF-8 decoding; need revert for storage (since key will not
         * be aligned, to optimize lookup speed)
         */
        int lastQuad;

        if (lastQuadBytes < 4) {
            lastQuad = quads[qlen-1];
            // 8/16/24 bit left shift
            quads[qlen-1] = (lastQuad << ((4 - lastQuadBytes) << 3));
        } else {
            lastQuad = 0;
        }

        // Need some working space, TextBuffer works well:
        char[] cbuf = _textBuffer.emptyAndGetCurrentSegment();
        int cix = 0;

        for (int ix = 0; ix < byteLen; ) {
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
                } else { // 5- and 6-byte chars not valid xml chars
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
            quads[qlen-1] = lastQuad;
        }
        return _symbols.addName(baseName, quads, qlen);
    }

    // Helper method needed to fix [jackson-core#148], masking of 0x00 character
    protected final static int _padLastQuad(int q, int bytes) {
        return (bytes == 4) ? q : (q | (-1 << (bytes << 3)));
    }

    /*
    /**********************************************************************
    /* Internal methods, state changes
    /**********************************************************************
     */

    // Helper method called at point when all input has been exhausted and
    // input feeder has indicated no more input will be forthcoming.
    protected final JsonToken _eofAsNextToken() throws IOException {
        _majorState = MAJOR_CLOSED;
        if (!_parsingContext.inRoot()) {
            _handleEOF();
        }
        close();
        return (_currToken = null);
    }

    protected final JsonToken _fieldComplete(String name) throws IOException
    {
        _majorState = MAJOR_OBJECT_VALUE;
        _parsingContext.setCurrentName(name);
        return (_currToken = JsonToken.FIELD_NAME);
    }

    protected final JsonToken _valueComplete(JsonToken t) throws IOException
    {
        _majorState = _majorStateAfterValue;
        _currToken = t;
        return t;
    }

    protected final JsonToken _valueCompleteInt(int value, String asText) throws IOException
    {
        _textBuffer.resetWithString(asText);
        _intLength = asText.length();
        _numTypesValid = NR_INT; // to force parsing
        _numberInt = value;
        _majorState = _majorStateAfterValue;
        JsonToken t = JsonToken.VALUE_NUMBER_INT;
        _currToken = t;
        return t;
    }

    @SuppressWarnings("deprecation")
    protected final JsonToken _valueNonStdNumberComplete(int type) throws IOException
    {
        String tokenStr = NON_STD_TOKENS[type];
        _textBuffer.resetWithString(tokenStr);
        if (!isEnabled(Feature.ALLOW_NON_NUMERIC_NUMBERS)) {
            _reportError("Non-standard token '%s': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow",
                    tokenStr);
        }
        _intLength = 0;
        _numTypesValid = NR_DOUBLE;
        _numberDouble = NON_STD_TOKEN_VALUES[type];
        _majorState = _majorStateAfterValue;
        return (_currToken = JsonToken.VALUE_NUMBER_FLOAT);
    }

    protected final String _nonStdToken(int type) {
        return NON_STD_TOKENS[type];
    }

    /*
    /**********************************************************************
    /* Internal methods, error reporting, related
    /**********************************************************************
     */

    protected final void _updateTokenLocation()
    {
        _tokenInputRow = Math.max(_currInputRow, _currInputRowAlt);
        final int ptr = _inputPtr;
        _tokenInputCol = ptr - _currInputRowStart;
        _tokenInputTotal = _currInputProcessed + (ptr - _currBufferStart);
    }

    protected void _reportInvalidChar(int c) throws JsonParseException {
        // Either invalid WS or illegal UTF-8 start char
        if (c < INT_SPACE) {
            _throwInvalidSpace(c);
        }
        _reportInvalidInitial(c);
    }

    protected void _reportInvalidInitial(int mask) throws JsonParseException {
        _reportError("Invalid UTF-8 start byte 0x"+Integer.toHexString(mask));
    }

    protected void _reportInvalidOther(int mask, int ptr) throws JsonParseException {
        _inputPtr = ptr;
        _reportInvalidOther(mask);
    }

    protected void _reportInvalidOther(int mask) throws JsonParseException {
        _reportError("Invalid UTF-8 middle byte 0x"+Integer.toHexString(mask));
    }
}
