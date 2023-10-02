// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.json;

import java.io.*;
import java.util.Arrays;

import com.typespec.json.implementation.jackson.core.*;
import com.typespec.json.implementation.jackson.core.base.ParserBase;
import com.typespec.json.implementation.jackson.core.io.CharTypes;
import com.typespec.json.implementation.jackson.core.io.IOContext;
import com.typespec.json.implementation.jackson.core.sym.ByteQuadsCanonicalizer;
import com.typespec.json.implementation.jackson.core.util.*;

import static com.typespec.json.implementation.jackson.core.JsonTokenId.*;

/**
 * This is a concrete implementation of {@link JsonParser}, which is
 * based on a {@link java.io.DataInput} as the input source.
 *<p>
 * Due to limitations in look-ahead (basically there's none), as well
 * as overhead of reading content mostly byte-by-byte,
 * there are some
 * minor differences from regular streaming parsing. Specifically:
 *<ul>
 * <li>Input location offsets not being tracked, as offsets would need to
 *   be updated for each read from all over the place. If caller wants
 *   this information, it has to track this with {@link DataInput}.
 *   This also affects column number, so the only location information
 *   available is the row (line) number (but even that is approximate in
 *   case of two-byte linefeeds -- it should work with single CR or LF tho)
 *  </li>
 * <li>No white space validation:
 *    checks are simplified NOT to check for control characters.
 *  </li>
 * </ul>
 *
 * @since 2.8
 */
@SuppressWarnings("fallthrough")
public class UTF8DataInputJsonParser
    extends ParserBase
{
    final static byte BYTE_LF = (byte) '\n';

    @SuppressWarnings("deprecation")
    private final static int FEAT_MASK_TRAILING_COMMA = Feature.ALLOW_TRAILING_COMMA.getMask();
    @SuppressWarnings("deprecation")
    private final static int FEAT_MASK_LEADING_ZEROS = Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask();
    @SuppressWarnings("deprecation")
    private final static int FEAT_MASK_NON_NUM_NUMBERS = Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask();
    @SuppressWarnings("deprecation")
    private final static int FEAT_MASK_ALLOW_MISSING = Feature.ALLOW_MISSING_VALUES.getMask();
    private final static int FEAT_MASK_ALLOW_SINGLE_QUOTES = Feature.ALLOW_SINGLE_QUOTES.getMask();
    private final static int FEAT_MASK_ALLOW_UNQUOTED_NAMES = Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask();
    private final static int FEAT_MASK_ALLOW_JAVA_COMMENTS = Feature.ALLOW_COMMENTS.getMask();
    private final static int FEAT_MASK_ALLOW_YAML_COMMENTS = Feature.ALLOW_YAML_COMMENTS.getMask();

    // This is the main input-code lookup table, fetched eagerly
    private final static int[] _icUTF8 = CharTypes.getInputCodeUtf8();

    // Latin1 encoding is not supported, but we do use 8-bit subset for
    // pre-processing task, to simplify first pass, keep it fast.
    protected final static int[] _icLatin1 = CharTypes.getInputCodeLatin1();

    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    /**
     * Codec used for data binding when (if) requested; typically full
     * <code>ObjectMapper</code>, but that abstract is not part of core
     * package.
     */
    protected ObjectCodec _objectCodec;

    /**
     * Symbol table that contains field names encountered so far
     */
    final protected ByteQuadsCanonicalizer _symbols;

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

    /*
    /**********************************************************
    /* Current input data
    /**********************************************************
     */

    protected DataInput _inputData;

    /**
     * Sometimes we need buffering for just a single byte we read but
     * have to "push back"
     */
    protected int _nextByte = -1;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    public UTF8DataInputJsonParser(IOContext ctxt, int features, DataInput inputData,
            ObjectCodec codec, ByteQuadsCanonicalizer sym,
            int firstByte)
    {
        super(ctxt, features);
        _objectCodec = codec;
        _symbols = sym;
        _inputData = inputData;
        _nextByte = firstByte;
    }

    @Override
    public ObjectCodec getCodec() {
        return _objectCodec;
    }

    @Override
    public void setCodec(ObjectCodec c) {
        _objectCodec = c;
    }

    @Override // @since 2.12
    public JacksonFeatureSet<StreamReadCapability> getReadCapabilities() {
        return JSON_READ_CAPABILITIES;
    }

    /*
    /**********************************************************
    /* Overrides for life-cycle
    /**********************************************************
     */

    @Override
    public int releaseBuffered(OutputStream out) throws IOException {
        return 0;
    }

    @Override
    public Object getInputSource() {
        return _inputData;
    }

    /*
    /**********************************************************
    /* Overrides, low-level reading
    /**********************************************************
     */

    @Override
    protected void _closeInput() throws IOException { }

    /**
     * Method called to release internal buffers owned by the base
     * reader. This may be called along with {@link #_closeInput} (for
     * example, when explicitly closing this reader instance), or
     * separately (if need be).
     */
    @Override
    protected void _releaseBuffers() throws IOException
    {
        super._releaseBuffers();
        // Merge found symbols, if any:
        _symbols.release();
    }

    /*
    /**********************************************************
    /* Public API, data access
    /**********************************************************
     */

    @Override
    public String getText() throws IOException
    {
        if (_currToken == JsonToken.VALUE_STRING) {
            if (_tokenIncomplete) {
                _tokenIncomplete = false;
                return _finishAndReturnString(); // only strings can be incomplete
            }
            return _textBuffer.contentsAsString();
        }
        return _getText2(_currToken);
    }

    @Override
    public int getText(Writer writer) throws IOException
    {
        JsonToken t = _currToken;
        if (t == JsonToken.VALUE_STRING) {
            if (_tokenIncomplete) {
                _tokenIncomplete = false;
                _finishString(); // only strings can be incomplete
            }
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
            char[] ch = t.asCharArray();
            writer.write(ch);
            return ch.length;
        }
        return 0;
    }

    // // // Let's override default impls for improved performance
    @Override
    public String getValueAsString() throws IOException
    {
        if (_currToken == JsonToken.VALUE_STRING) {
            if (_tokenIncomplete) {
                _tokenIncomplete = false;
                return _finishAndReturnString(); // only strings can be incomplete
            }
            return _textBuffer.contentsAsString();
        }
        if (_currToken == JsonToken.FIELD_NAME) {
            return getCurrentName();
        }
        return super.getValueAsString(null);
    }

    @Override
    public String getValueAsString(String defValue) throws IOException
    {
        if (_currToken == JsonToken.VALUE_STRING) {
            if (_tokenIncomplete) {
                _tokenIncomplete = false;
                return _finishAndReturnString(); // only strings can be incomplete
            }
            return _textBuffer.contentsAsString();
        }
        if (_currToken == JsonToken.FIELD_NAME) {
            return getCurrentName();
        }
        return super.getValueAsString(defValue);
    }

    @Override
    public int getValueAsInt() throws IOException
    {
        JsonToken t = _currToken;
        if ((t == JsonToken.VALUE_NUMBER_INT) || (t == JsonToken.VALUE_NUMBER_FLOAT)) {
            // inlined 'getIntValue()'
            if ((_numTypesValid & NR_INT) == 0) {
                if (_numTypesValid == NR_UNKNOWN) {
                    return _parseIntValue();
                }
                if ((_numTypesValid & NR_INT) == 0) {
                    convertNumberToInt();
                }
            }
            return _numberInt;
        }
        return super.getValueAsInt(0);
    }

    @Override
    public int getValueAsInt(int defValue) throws IOException
    {
        JsonToken t = _currToken;
        if ((t == JsonToken.VALUE_NUMBER_INT) || (t == JsonToken.VALUE_NUMBER_FLOAT)) {
            // inlined 'getIntValue()'
            if ((_numTypesValid & NR_INT) == 0) {
                if (_numTypesValid == NR_UNKNOWN) {
                    return _parseIntValue();
                }
                if ((_numTypesValid & NR_INT) == 0) {
                    convertNumberToInt();
                }
            }
            return _numberInt;
        }
        return super.getValueAsInt(defValue);
    }

    protected final String _getText2(JsonToken t)
    {
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
                if (_tokenIncomplete) {
                    _tokenIncomplete = false;
                    _finishString(); // only strings can be incomplete
                }
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
        if (_currToken == JsonToken.VALUE_STRING) {
            if (_tokenIncomplete) {
                _tokenIncomplete = false;
                _finishString(); // only strings can be incomplete
            }
            return _textBuffer.size();
        }
        if (_currToken == JsonToken.FIELD_NAME) {
            return _parsingContext.getCurrentName().length();
        }
        if (_currToken != null) { // null only before/after document
            if (_currToken.isNumeric()) {
                return _textBuffer.size();
            }
            return _currToken.asCharArray().length;
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
                if (_tokenIncomplete) {
                    _tokenIncomplete = false;
                    _finishString(); // only strings can be incomplete
                }
                // fall through
            case ID_NUMBER_INT:
            case ID_NUMBER_FLOAT:
                return _textBuffer.getTextOffset();
            default:
            }
        }
        return 0;
    }

    @Override
    public byte[] getBinaryValue(Base64Variant b64variant) throws IOException
    {
        if (_currToken != JsonToken.VALUE_STRING &&
                (_currToken != JsonToken.VALUE_EMBEDDED_OBJECT || _binaryValue == null)) {
            _reportError("Current token ("+_currToken+") not VALUE_STRING or VALUE_EMBEDDED_OBJECT, can not access as binary");
        }
        /* To ensure that we won't see inconsistent data, better clear up
         * state...
         */
        if (_tokenIncomplete) {
            try {
                _binaryValue = _decodeBase64(b64variant);
            } catch (IllegalArgumentException iae) {
                throw _constructError("Failed to decode VALUE_STRING as base64 ("+b64variant+"): "+iae.getMessage());
            }
            /* let's clear incomplete only now; allows for accessing other
             * textual content in error cases
             */
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

    @Override
    public int readBinaryValue(Base64Variant b64variant, OutputStream out) throws IOException
    {
        // if we have already read the token, just use whatever we may have
        if (!_tokenIncomplete || _currToken != JsonToken.VALUE_STRING) {
            byte[] b = getBinaryValue(b64variant);
            out.write(b);
            return b.length;
        }
        // otherwise do "real" incremental parsing...
        byte[] buf = _ioContext.allocBase64Buffer();
        try {
            return _readBinary(b64variant, out, buf);
        } finally {
            _ioContext.releaseBase64Buffer(buf);
        }
    }

    protected int _readBinary(Base64Variant b64variant, OutputStream out,
                              byte[] buffer) throws IOException
    {
        int outputPtr = 0;
        final int outputEnd = buffer.length - 3;
        int outputCount = 0;

        while (true) {
            // first, we'll skip preceding white space, if any
            int ch;
            do {
                ch = _inputData.readUnsignedByte();
            } while (ch <= INT_SPACE);
            int bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) { // reached the end, fair and square?
                if (ch == INT_QUOTE) {
                    break;
                }
                bits = _decodeBase64Escape(b64variant, ch, 0);
                if (bits < 0) { // white space to skip
                    continue;
                }
            }

            // enough room? If not, flush
            if (outputPtr > outputEnd) {
                outputCount += outputPtr;
                out.write(buffer, 0, outputPtr);
                outputPtr = 0;
            }

            int decodedData = bits;

            // then second base64 char; can't get padding yet, nor ws
            ch = _inputData.readUnsignedByte();
            bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
                bits = _decodeBase64Escape(b64variant, ch, 1);
            }
            decodedData = (decodedData << 6) | bits;

            // third base64 char; can be padding, but not ws
            ch = _inputData.readUnsignedByte();
            bits = b64variant.decodeBase64Char(ch);

            // First branch: can get padding (-> 1 byte)
            if (bits < 0) {
                if (bits != Base64Variant.BASE64_VALUE_PADDING) {
                    // could also just be 'missing'  padding
                    if (ch == INT_QUOTE) {
                        decodedData >>= 4;
                        buffer[outputPtr++] = (byte) decodedData;
                        if (b64variant.usesPadding()) {
                            _handleBase64MissingPadding(b64variant);
                        }
                        break;
                    }
                    bits = _decodeBase64Escape(b64variant, ch, 2);
                }
                if (bits == Base64Variant.BASE64_VALUE_PADDING) {
                    // Ok, must get padding
                    ch = _inputData.readUnsignedByte();
                    if (!b64variant.usesPaddingChar(ch)) {
                        if ((ch != INT_BACKSLASH)
                                || _decodeBase64Escape(b64variant, ch, 3) != Base64Variant.BASE64_VALUE_PADDING) {
                            throw reportInvalidBase64Char(b64variant, ch, 3, "expected padding character '"+b64variant.getPaddingChar()+"'");
                        }
                    }
                    // Got 12 bits, only need 8, need to shift
                    decodedData >>= 4;
                    buffer[outputPtr++] = (byte) decodedData;
                    continue;
                }
            }
            // Nope, 2 or 3 bytes
            decodedData = (decodedData << 6) | bits;
            // fourth and last base64 char; can be padding, but not ws
            ch = _inputData.readUnsignedByte();
            bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
                if (bits != Base64Variant.BASE64_VALUE_PADDING) {
                    // could also just be 'missing'  padding
                    if (ch == INT_QUOTE) {
                        decodedData >>= 2;
                        buffer[outputPtr++] = (byte) (decodedData >> 8);
                        buffer[outputPtr++] = (byte) decodedData;
                        if (b64variant.usesPadding()) {
                            _handleBase64MissingPadding(b64variant);
                        }
                        break;
                    }
                    bits = _decodeBase64Escape(b64variant, ch, 3);
                }
                if (bits == Base64Variant.BASE64_VALUE_PADDING) {
                    /* With padding we only get 2 bytes; but we have
                     * to shift it a bit so it is identical to triplet
                     * case with partial output.
                     * 3 chars gives 3x6 == 18 bits, of which 2 are
                     * dummies, need to discard:
                     */
                    decodedData >>= 2;
                    buffer[outputPtr++] = (byte) (decodedData >> 8);
                    buffer[outputPtr++] = (byte) decodedData;
                    continue;
                }
            }
            // otherwise, our triplet is now complete
            decodedData = (decodedData << 6) | bits;
            buffer[outputPtr++] = (byte) (decodedData >> 16);
            buffer[outputPtr++] = (byte) (decodedData >> 8);
            buffer[outputPtr++] = (byte) decodedData;
        }
        _tokenIncomplete = false;
        if (outputPtr > 0) {
            outputCount += outputPtr;
            out.write(buffer, 0, outputPtr);
        }
        return outputCount;
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
    public JsonToken nextToken() throws IOException
    {
        if (_closed) {
            return null;
        }
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
        _tokenInputRow = _currInputRow;

        // Closing scope?
        if (i == INT_RBRACKET || i == INT_RCURLY) {
            _closeScope(i);
            return _currToken;
        }

        // Nope: do we then expect a comma?
        if (_parsingContext.expectComma()) {
            if (i != INT_COMMA) {
                _reportUnexpectedChar(i, "was expecting comma to separate "+_parsingContext.typeDesc()+" entries");
            }
            i = _skipWS();

            // Was that a trailing comma?
            if ((_features & FEAT_MASK_TRAILING_COMMA) != 0) {
                if (i == INT_RBRACKET || i == INT_RCURLY) {
                    _closeScope(i);
                    return _currToken;
                }
            }
        }

        /* And should we now have a name? Always true for
         * Object contexts, since the intermediate 'expect-value'
         * state is never retained.
         */
        if (!_parsingContext.inObject()) {
            return _nextTokenNotInObject(i);
        }
        // So first parse the field name itself:
        String n = _parseName(i);
        _parsingContext.setCurrentName(n);
        _currToken = JsonToken.FIELD_NAME;

        i = _skipColon();

        // Ok: we must have a value... what is it? Strings are very common, check first:
        if (i == INT_QUOTE) {
            _tokenIncomplete = true;
            _nextToken = JsonToken.VALUE_STRING;
            return _currToken;
        }
        JsonToken t;

        switch (i) {
        case '-':
            t = _parseNegNumber();
            break;

            // Should we have separate handling for plus? Although
            // it is not allowed per se, it may be erroneously used,
            // and could be indicate by a more specific error message.
        case '.': // as per [core#611]
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
        case 'f':
            _matchToken("false", 1);
             t = JsonToken.VALUE_FALSE;
            break;
        case 'n':
            _matchToken("null", 1);
            t = JsonToken.VALUE_NULL;
            break;
        case 't':
            _matchToken("true", 1);
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

    private final JsonToken _nextTokenNotInObject(int i) throws IOException
    {
        if (i == INT_QUOTE) {
            _tokenIncomplete = true;
            return (_currToken = JsonToken.VALUE_STRING);
        }
        switch (i) {
        case '[':
            _parsingContext = _parsingContext.createChildArrayContext(_tokenInputRow, _tokenInputCol);
            return (_currToken = JsonToken.START_ARRAY);
        case '{':
            _parsingContext = _parsingContext.createChildObjectContext(_tokenInputRow, _tokenInputCol);
            return (_currToken = JsonToken.START_OBJECT);
        case 't':
            _matchToken("true", 1);
            return (_currToken = JsonToken.VALUE_TRUE);
        case 'f':
            _matchToken("false", 1);
            return (_currToken = JsonToken.VALUE_FALSE);
        case 'n':
            _matchToken("null", 1);
            return (_currToken = JsonToken.VALUE_NULL);
        case '-':
            return (_currToken = _parseNegNumber());
            // Should we have separate handling for plus? Although it is not allowed
            // per se, it may be erroneously used, and could be indicated by a more
            // specific error message.
        case '.': // as per [core#611]
            return (_currToken = _parseFloatThatStartsWithPeriod());
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
            return (_currToken = _parsePosNumber(i));
        }
        return (_currToken = _handleUnexpectedValue(i));
    }

    private final JsonToken _nextAfterName()
    {
        _nameCopied = false; // need to invalidate if it was copied
        JsonToken t = _nextToken;
        _nextToken = null;

        // Also: may need to start new context?
        if (t == JsonToken.START_ARRAY) {
            _parsingContext = _parsingContext.createChildArrayContext(_tokenInputRow, _tokenInputCol);
        } else if (t == JsonToken.START_OBJECT) {
            _parsingContext = _parsingContext.createChildObjectContext(_tokenInputRow, _tokenInputCol);
        }
        return (_currToken = t);
    }

    @Override
    public void finishToken() throws IOException {
        if (_tokenIncomplete) {
            _tokenIncomplete = false;
            _finishString(); // only strings can be incomplete
        }
    }

    /*
    /**********************************************************
    /* Public API, traversal, nextXxxValue/nextFieldName
    /**********************************************************
     */

    // Can not implement without look-ahead...
//    public boolean nextFieldName(SerializableString str) throws IOException

    @Override
    public String nextFieldName() throws IOException
    {
        // // // Note: this is almost a verbatim copy of nextToken()

        _numTypesValid = NR_UNKNOWN;
        if (_currToken == JsonToken.FIELD_NAME) {
            _nextAfterName();
            return null;
        }
        if (_tokenIncomplete) {
            _skipString();
        }
        int i = _skipWS();
        _binaryValue = null;
        _tokenInputRow = _currInputRow;

        if (i == INT_RBRACKET || i == INT_RCURLY) {
            _closeScope(i);
            return null;
        }

        // Nope: do we then expect a comma?
        if (_parsingContext.expectComma()) {
            if (i != INT_COMMA) {
                _reportUnexpectedChar(i, "was expecting comma to separate "+_parsingContext.typeDesc()+" entries");
            }
            i = _skipWS();

            // Was that a trailing comma?
            if ((_features & FEAT_MASK_TRAILING_COMMA) != 0) {
                if (i == INT_RBRACKET || i == INT_RCURLY) {
                    _closeScope(i);
                    return null;
                }
            }

        }
        if (!_parsingContext.inObject()) {
            _nextTokenNotInObject(i);
            return null;
        }

        final String nameStr = _parseName(i);
        _parsingContext.setCurrentName(nameStr);
        _currToken = JsonToken.FIELD_NAME;

        i = _skipColon();
        if (i == INT_QUOTE) {
            _tokenIncomplete = true;
            _nextToken = JsonToken.VALUE_STRING;
            return nameStr;
        }
        JsonToken t;
        switch (i) {
        case '-':
            t = _parseNegNumber();
            break;
        case '.': // as per [core#611]
            t = _parseFloatThatStartsWithPeriod();
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
        case 'f':
            _matchToken("false", 1);
             t = JsonToken.VALUE_FALSE;
            break;
        case 'n':
            _matchToken("null", 1);
            t = JsonToken.VALUE_NULL;
            break;
        case 't':
            _matchToken("true", 1);
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
        return nameStr;
    }

    @Override
    public String nextTextValue() throws IOException
    {
        // two distinct cases; either got name and we know next type, or 'other'
        if (_currToken == JsonToken.FIELD_NAME) { // mostly copied from '_nextAfterName'
            _nameCopied = false;
            JsonToken t = _nextToken;
            _nextToken = null;
            _currToken = t;
            if (t == JsonToken.VALUE_STRING) {
                if (_tokenIncomplete) {
                    _tokenIncomplete = false;
                    return _finishAndReturnString();
                }
                return _textBuffer.contentsAsString();
            }
            if (t == JsonToken.START_ARRAY) {
                _parsingContext = _parsingContext.createChildArrayContext(_tokenInputRow, _tokenInputCol);
            } else if (t == JsonToken.START_OBJECT) {
                _parsingContext = _parsingContext.createChildObjectContext(_tokenInputRow, _tokenInputCol);
            }
            return null;
        }
        return (nextToken() == JsonToken.VALUE_STRING) ? getText() : null;
    }

    @Override
    public int nextIntValue(int defaultValue) throws IOException
    {
        // two distinct cases; either got name and we know next type, or 'other'
        if (_currToken == JsonToken.FIELD_NAME) { // mostly copied from '_nextAfterName'
            _nameCopied = false;
            JsonToken t = _nextToken;
            _nextToken = null;
            _currToken = t;
            if (t == JsonToken.VALUE_NUMBER_INT) {
                return getIntValue();
            }
            if (t == JsonToken.START_ARRAY) {
                _parsingContext = _parsingContext.createChildArrayContext(_tokenInputRow, _tokenInputCol);
            } else if (t == JsonToken.START_OBJECT) {
                _parsingContext = _parsingContext.createChildObjectContext(_tokenInputRow, _tokenInputCol);
            }
            return defaultValue;
        }
        return (nextToken() == JsonToken.VALUE_NUMBER_INT) ? getIntValue() : defaultValue;
    }

    @Override
    public long nextLongValue(long defaultValue) throws IOException
    {
        // two distinct cases; either got name and we know next type, or 'other'
        if (_currToken == JsonToken.FIELD_NAME) { // mostly copied from '_nextAfterName'
            _nameCopied = false;
            JsonToken t = _nextToken;
            _nextToken = null;
            _currToken = t;
            if (t == JsonToken.VALUE_NUMBER_INT) {
                return getLongValue();
            }
            if (t == JsonToken.START_ARRAY) {
                _parsingContext = _parsingContext.createChildArrayContext(_tokenInputRow, _tokenInputCol);
            } else if (t == JsonToken.START_OBJECT) {
                _parsingContext = _parsingContext.createChildObjectContext(_tokenInputRow, _tokenInputCol);
            }
            return defaultValue;
        }
        return (nextToken() == JsonToken.VALUE_NUMBER_INT) ? getLongValue() : defaultValue;
    }

    @Override
    public Boolean nextBooleanValue() throws IOException
    {
        // two distinct cases; either got name and we know next type, or 'other'
        if (_currToken == JsonToken.FIELD_NAME) { // mostly copied from '_nextAfterName'
            _nameCopied = false;
            JsonToken t = _nextToken;
            _nextToken = null;
            _currToken = t;
            if (t == JsonToken.VALUE_TRUE) {
                return Boolean.TRUE;
            }
            if (t == JsonToken.VALUE_FALSE) {
                return Boolean.FALSE;
            }
            if (t == JsonToken.START_ARRAY) {
                _parsingContext = _parsingContext.createChildArrayContext(_tokenInputRow, _tokenInputCol);
            } else if (t == JsonToken.START_OBJECT) {
                _parsingContext = _parsingContext.createChildObjectContext(_tokenInputRow, _tokenInputCol);
            }
            return null;
        }

        JsonToken t = nextToken();
        if (t == JsonToken.VALUE_TRUE) {
            return Boolean.TRUE;
        }
        if (t == JsonToken.VALUE_FALSE) {
            return Boolean.FALSE;
        }
        return null;
    }

    /*
    /**********************************************************
    /* Internal methods, number parsing
    /**********************************************************
     */

    // @since 2.11, [core#611]
    protected final JsonToken _parseFloatThatStartsWithPeriod() throws IOException
    {
        // [core#611]: allow optionally leading decimal point
        if (!isEnabled(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS.mappedFeature())) {
            return _handleUnexpectedValue(INT_PERIOD);
        }
        char[] outBuf = _textBuffer.emptyAndGetCurrentSegment();
        return _parseFloat(outBuf, 0, INT_PERIOD, false, 0);
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
    protected JsonToken _parsePosNumber(int c) throws IOException
    {
        char[] outBuf = _textBuffer.emptyAndGetCurrentSegment();
        int outPtr;

        // One special case: if first char is 0, must not be followed by a digit.
        // Gets bit tricky as we only want to retain 0 if it's the full value
        if (c == INT_0) {
            c = _handleLeadingZeroes();
            if (c <= INT_9 && c >= INT_0) { // skip if followed by digit
                outPtr = 0;
            } else {
                outBuf[0] = '0';
                outPtr = 1;
            }
        } else {
            outBuf[0] = (char) c;
            c = _inputData.readUnsignedByte();
            outPtr = 1;
        }
        int intLen = outPtr;

        // With this, we have a nice and tight loop:
        while (c <= INT_9 && c >= INT_0) {
            ++intLen;
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            outBuf[outPtr++] = (char) c;
            c = _inputData.readUnsignedByte();
        }
        if (c == '.' || c == 'e' || c == 'E') {
            return _parseFloat(outBuf, outPtr, c, false, intLen);
        }
        _textBuffer.setCurrentLength(outPtr);
        // As per [core#105], need separating space between root values; check here
        if (_parsingContext.inRoot()) {
            _verifyRootSpace();
        } else {
            _nextByte = c;
        }
        // And there we have it!
        return resetInt(false, intLen);
    }

    protected JsonToken _parseNegNumber() throws IOException
    {
        char[] outBuf = _textBuffer.emptyAndGetCurrentSegment();
        int outPtr = 0;

        // Need to prepend sign?
        outBuf[outPtr++] = '-';
        int c = _inputData.readUnsignedByte();
        outBuf[outPtr++] = (char) c;
        // Note: must be followed by a digit
        if (c <= INT_0) {
            // One special case: if first char is 0 need to check no leading zeroes
            if (c == INT_0) {
                c = _handleLeadingZeroes();
            } else {
                return _handleInvalidNumberStart(c, true);
            }
        } else {
            if (c > INT_9) {
                return _handleInvalidNumberStart(c, true);
            }
            c = _inputData.readUnsignedByte();
        }
        // Ok: we can first just add digit we saw first:
        int intLen = 1;

        // With this, we have a nice and tight loop:
        while (c <= INT_9 && c >= INT_0) {
            ++intLen;
            outBuf[outPtr++] = (char) c;
            c = _inputData.readUnsignedByte();
        }
        if (c == '.' || c == 'e' || c == 'E') {
            return _parseFloat(outBuf, outPtr, c, true, intLen);
        }
        _textBuffer.setCurrentLength(outPtr);
        // As per [core#105], need separating space between root values; check here
        _nextByte = c;
        if (_parsingContext.inRoot()) {
            _verifyRootSpace();
        }
        // And there we have it!
        return resetInt(true, intLen);
    }

    /**
     * Method called when we have seen one zero, and want to ensure
     * it is not followed by another, or, if leading zeroes allowed,
     * skipped redundant ones.
     *
     * @return Character immediately following zeroes
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    private final int _handleLeadingZeroes() throws IOException
    {
        int ch = _inputData.readUnsignedByte();
        // if not followed by a number (probably '.'); return zero as is, to be included
        if (ch < INT_0 || ch > INT_9) {
            return ch;
        }
        // we may want to allow leading zeroes them, after all...
        if ((_features & FEAT_MASK_LEADING_ZEROS) == 0) {
            reportInvalidNumber("Leading zeroes not allowed");
        }
        // if so, just need to skip either all zeroes (if followed by number); or all but one (if non-number)
        while (ch == INT_0) {
            ch = _inputData.readUnsignedByte();
        }
        return ch;
    }

    private final JsonToken _parseFloat(char[] outBuf, int outPtr, int c,
            boolean negative, int integerPartLength) throws IOException
    {
        int fractLen = 0;

        // And then see if we get other parts
        if (c == INT_PERIOD) { // yes, fraction
            outBuf[outPtr++] = (char) c;

            fract_loop:
            while (true) {
                c = _inputData.readUnsignedByte();
                if (c < INT_0 || c > INT_9) {
                    break fract_loop;
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
                reportUnexpectedNumberChar(c, "Decimal point not followed by a digit");
            }
        }

        int expLen = 0;
        if (c == INT_e || c == INT_E) { // exponent?
            if (outPtr >= outBuf.length) {
                outBuf = _textBuffer.finishCurrentSegment();
                outPtr = 0;
            }
            outBuf[outPtr++] = (char) c;
            c = _inputData.readUnsignedByte();
            // Sign indicator?
            if (c == '-' || c == '+') {
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outBuf[outPtr++] = (char) c;
                c = _inputData.readUnsignedByte();
            }
            while (c <= INT_9 && c >= INT_0) {
                ++expLen;
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                }
                outBuf[outPtr++] = (char) c;
                c = _inputData.readUnsignedByte();
            }
            // must be followed by sequence of ints, one minimum
            if (expLen == 0) {
                reportUnexpectedNumberChar(c, "Exponent indicator not followed by a digit");
            }
        }

        // Ok; unless we hit end-of-input, need to push last char read back
        // As per #105, need separating space between root values; check here
        _nextByte = c;
        if (_parsingContext.inRoot()) {
            _verifyRootSpace();
        }
        _textBuffer.setCurrentLength(outPtr);

        // And there we have it!
        return resetFloat(negative, integerPartLength, fractLen, expLen);
    }

    /*
     * Method called to ensure that a root-value is followed by a space token,
     * if possible.
     *<p>
     * NOTE: with {@link DataInput} source, not really feasible, up-front.
     * If we did want, we could rearrange things to require space before
     * next read, but initially let's just do nothing.
     */
    private final void _verifyRootSpace() throws IOException
    {
        int ch = _nextByte;
        if (ch <= INT_SPACE) {
            _nextByte = -1;
            if (ch == INT_CR || ch == INT_LF) {
                ++_currInputRow;
            }
            return;
        }
        _reportMissingRootWS(ch);
    }

    /*
    /**********************************************************
    /* Internal methods, secondary parsing
    /**********************************************************
     */

    protected final String _parseName(int i) throws IOException
    {
        if (i != INT_QUOTE) {
            return _handleOddName(i);
        }
        // If so, can also unroll loops nicely
        /* 25-Nov-2008, tatu: This may seem weird, but here we do
         *   NOT want to worry about UTF-8 decoding. Rather, we'll
         *   assume that part is ok (if not it will get caught
         *   later on), and just handle quotes and backslashes here.
         */
        final int[] codes = _icLatin1;

        int q = _inputData.readUnsignedByte();

        if (codes[q] == 0) {
            i = _inputData.readUnsignedByte();
            if (codes[i] == 0) {
                q = (q << 8) | i;
                i = _inputData.readUnsignedByte();
                if (codes[i] == 0) {
                    q = (q << 8) | i;
                    i = _inputData.readUnsignedByte();
                    if (codes[i] == 0) {
                        q = (q << 8) | i;
                        i = _inputData.readUnsignedByte();
                        if (codes[i] == 0) {
                            _quad1 = q;
                            return _parseMediumName(i);
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

    private final String _parseMediumName(int q2) throws IOException
    {
        final int[] codes = _icLatin1;

        // Ok, got 5 name bytes so far
        int i = _inputData.readUnsignedByte();
        if (codes[i] != 0) {
            if (i == INT_QUOTE) { // 5 bytes
                return findName(_quad1, q2, 1);
            }
            return parseName(_quad1, q2, i, 1); // quoting or invalid char
        }
        q2 = (q2 << 8) | i;
        i = _inputData.readUnsignedByte();
        if (codes[i] != 0) {
            if (i == INT_QUOTE) { // 6 bytes
                return findName(_quad1, q2, 2);
            }
            return parseName(_quad1, q2, i, 2);
        }
        q2 = (q2 << 8) | i;
        i = _inputData.readUnsignedByte();
        if (codes[i] != 0) {
            if (i == INT_QUOTE) { // 7 bytes
                return findName(_quad1, q2, 3);
            }
            return parseName(_quad1, q2, i, 3);
        }
        q2 = (q2 << 8) | i;
        i = _inputData.readUnsignedByte();
        if (codes[i] != 0) {
            if (i == INT_QUOTE) { // 8 bytes
                return findName(_quad1, q2, 4);
            }
            return parseName(_quad1, q2, i, 4);
        }
        return _parseMediumName2(i, q2);
    }

    private final String _parseMediumName2(int q3, final int q2) throws IOException
    {
        final int[] codes = _icLatin1;

        // Got 9 name bytes so far
        int i = _inputData.readUnsignedByte();
        if (codes[i] != 0) {
            if (i == INT_QUOTE) { // 9 bytes
                return findName(_quad1, q2, q3, 1);
            }
            return parseName(_quad1, q2, q3, i, 1);
        }
        q3 = (q3 << 8) | i;
        i = _inputData.readUnsignedByte();
        if (codes[i] != 0) {
            if (i == INT_QUOTE) { // 10 bytes
                return findName(_quad1, q2, q3, 2);
            }
            return parseName(_quad1, q2, q3, i, 2);
        }
        q3 = (q3 << 8) | i;
        i = _inputData.readUnsignedByte();
        if (codes[i] != 0) {
            if (i == INT_QUOTE) { // 11 bytes
                return findName(_quad1, q2, q3, 3);
            }
            return parseName(_quad1, q2, q3, i, 3);
        }
        q3 = (q3 << 8) | i;
        i = _inputData.readUnsignedByte();
        if (codes[i] != 0) {
            if (i == INT_QUOTE) { // 12 bytes
                return findName(_quad1, q2, q3, 4);
            }
            return parseName(_quad1, q2, q3, i, 4);
        }
        return _parseLongName(i, q2, q3);
    }

    private final String _parseLongName(int q, final int q2, int q3) throws IOException
    {
        _quadBuffer[0] = _quad1;
        _quadBuffer[1] = q2;
        _quadBuffer[2] = q3;

        // As explained above, will ignore UTF-8 encoding at this point
        final int[] codes = _icLatin1;
        int qlen = 3;

        while (true) {
            int i = _inputData.readUnsignedByte();
            if (codes[i] != 0) {
                if (i == INT_QUOTE) {
                    return findName(_quadBuffer, qlen, q, 1);
                }
                return parseEscapedName(_quadBuffer, qlen, q, i, 1);
            }

            q = (q << 8) | i;
            i = _inputData.readUnsignedByte();
            if (codes[i] != 0) {
                if (i == INT_QUOTE) {
                    return findName(_quadBuffer, qlen, q, 2);
                }
                return parseEscapedName(_quadBuffer, qlen, q, i, 2);
            }

            q = (q << 8) | i;
            i = _inputData.readUnsignedByte();
            if (codes[i] != 0) {
                if (i == INT_QUOTE) {
                    return findName(_quadBuffer, qlen, q, 3);
                }
                return parseEscapedName(_quadBuffer, qlen, q, i, 3);
            }

            q = (q << 8) | i;
            i = _inputData.readUnsignedByte();
            if (codes[i] != 0) {
                if (i == INT_QUOTE) {
                    return findName(_quadBuffer, qlen, q, 4);
                }
                return parseEscapedName(_quadBuffer, qlen, q, i, 4);
            }

            // Nope, no end in sight. Need to grow quad array etc
            if (qlen >= _quadBuffer.length) {
                _quadBuffer = _growArrayBy(_quadBuffer, qlen);
            }
            _quadBuffer[qlen++] = q;
            q = i;
        }
    }

    private final String parseName(int q1, int ch, int lastQuadBytes) throws IOException {
        return parseEscapedName(_quadBuffer, 0, q1, ch, lastQuadBytes);
    }

    private final String parseName(int q1, int q2, int ch, int lastQuadBytes) throws IOException {
        _quadBuffer[0] = q1;
        return parseEscapedName(_quadBuffer, 1, q2, ch, lastQuadBytes);
    }

    private final String parseName(int q1, int q2, int q3, int ch, int lastQuadBytes) throws IOException {
        _quadBuffer[0] = q1;
        _quadBuffer[1] = q2;
        return parseEscapedName(_quadBuffer, 2, q3, ch, lastQuadBytes);
    }

    /* Slower parsing method which is generally branched to when
     * an escape sequence is detected (or alternatively for long
     * names, one crossing input buffer boundary).
     * Needs to be able to handle more exceptional cases, gets slower,
     * and hence is offlined to a separate method.
     */
    protected final String parseEscapedName(int[] quads, int qlen, int currQuad, int ch,
            int currQuadBytes) throws IOException
    {
        /* 25-Nov-2008, tatu: This may seem weird, but here we do not want to worry about
         *   UTF-8 decoding yet. Rather, we'll assume that part is ok (if not it will get
         *   caught later on), and just handle quotes and backslashes here.
         */
        final int[] codes = _icLatin1;

        while (true) {
            if (codes[ch] != 0) {
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
                /* May need to UTF-8 (re-)encode it, if it's
                 * beyond 7-bit ascii. Gets pretty messy.
                 * If this happens often, may want to use different name
                 * canonicalization to avoid these hits.
                 */
                if (ch > 127) {
                    // Ok, we'll need room for first byte right away
                    if (currQuadBytes >= 4) {
                        if (qlen >= quads.length) {
                            _quadBuffer = quads = _growArrayBy(quads, quads.length);
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
                                _quadBuffer = quads = _growArrayBy(quads, quads.length);
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
                    _quadBuffer = quads = _growArrayBy(quads, quads.length);
                }
                quads[qlen++] = currQuad;
                currQuad = ch;
                currQuadBytes = 1;
            }
            ch = _inputData.readUnsignedByte();
        }

        if (currQuadBytes > 0) {
            if (qlen >= quads.length) {
                _quadBuffer = quads = _growArrayBy(quads, quads.length);
            }
            quads[qlen++] = pad(currQuad, currQuadBytes);
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
    protected String _handleOddName(int ch) throws IOException
    {
        if (ch == '\'' && (_features & FEAT_MASK_ALLOW_SINGLE_QUOTES) != 0) {
            return _parseAposName();
        }
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
            _reportUnexpectedChar(ch, "was expecting either valid name character (for unquoted name) or double-quote (for quoted) to start field name");
        }

        /* Ok, now; instead of ultra-optimizing parsing here (as with
         * regular JSON names), let's just use the generic "slow"
         * variant. Can measure its impact later on if need be
         */
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
                    _quadBuffer = quads = _growArrayBy(quads, quads.length);
                }
                quads[qlen++] = currQuad;
                currQuad = ch;
                currQuadBytes = 1;
            }
            ch = _inputData.readUnsignedByte();
            if (codes[ch] != 0) {
                break;
            }
        }
        // Note: we must "push back" character read here for future consumption
        _nextByte = ch;
        if (currQuadBytes > 0) {
            if (qlen >= quads.length) {
                _quadBuffer = quads = _growArrayBy(quads, quads.length);
            }
            quads[qlen++] = currQuad;
        }
        String name = _symbols.findName(quads, qlen);
        if (name == null) {
            name = addName(quads, qlen, currQuadBytes);
        }
        return name;
    }

    /* Parsing to allow optional use of non-standard single quotes.
     * Plenty of duplicated code;
     * main reason being to try to avoid slowing down fast path
     * for valid JSON -- more alternatives, more code, generally
     * bit slower execution.
     */
    protected String _parseAposName() throws IOException
    {
        int ch = _inputData.readUnsignedByte();
        if (ch == '\'') { // special case, ''
            return "";
        }
        int[] quads = _quadBuffer;
        int qlen = 0;
        int currQuad = 0;
        int currQuadBytes = 0;

        // Copied from parseEscapedFieldName, with minor mods:

        final int[] codes = _icLatin1;

        while (true) {
            if (ch == '\'') {
                break;
            }
            // additional check to skip handling of double-quotes
            if (ch != '"' && codes[ch] != 0) {
                if (ch != '\\') {
                    // Unquoted white space?
                    // As per [JACKSON-208], call can now return:
                    _throwUnquotedSpace(ch, "name");
                } else {
                    // Nope, escape sequence
                    ch = _decodeEscaped();
                }
                /* May need to UTF-8 (re-)encode it, if it's  beyond
                 * 7-bit ASCII. Gets pretty messy. If this happens often, may want
                 * to use different name canonicalization to avoid these hits.
                 */
                if (ch > 127) {
                    // Ok, we'll need room for first byte right away
                    if (currQuadBytes >= 4) {
                        if (qlen >= quads.length) {
                            _quadBuffer = quads = _growArrayBy(quads, quads.length);
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
                                _quadBuffer = quads = _growArrayBy(quads, quads.length);
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
                    _quadBuffer = quads = _growArrayBy(quads, quads.length);
                }
                quads[qlen++] = currQuad;
                currQuad = ch;
                currQuadBytes = 1;
            }
            ch = _inputData.readUnsignedByte();
        }

        if (currQuadBytes > 0) {
            if (qlen >= quads.length) {
                _quadBuffer = quads = _growArrayBy(quads, quads.length);
            }
            quads[qlen++] = pad(currQuad, currQuadBytes);
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

    private final String findName(int q1, int lastQuadBytes) throws JsonParseException
    {
        q1 = pad(q1, lastQuadBytes);
        // Usually we'll find it from the canonical symbol table already
        String name = _symbols.findName(q1);
        if (name != null) {
            return name;
        }
        // If not, more work. We'll need add stuff to buffer
        _quadBuffer[0] = q1;
        return addName(_quadBuffer, 1, lastQuadBytes);
    }

    private final String findName(int q1, int q2, int lastQuadBytes) throws JsonParseException
    {
        q2 = pad(q2, lastQuadBytes);
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

    private final String findName(int q1, int q2, int q3, int lastQuadBytes) throws JsonParseException
    {
        q3 = pad(q3, lastQuadBytes);
        String name = _symbols.findName(q1, q2, q3);
        if (name != null) {
            return name;
        }
        int[] quads = _quadBuffer;
        quads[0] = q1;
        quads[1] = q2;
        quads[2] = pad(q3, lastQuadBytes);
        return addName(quads, 3, lastQuadBytes);
    }

    private final String findName(int[] quads, int qlen, int lastQuad, int lastQuadBytes) throws JsonParseException
    {
        if (qlen >= quads.length) {
            _quadBuffer = quads = _growArrayBy(quads, quads.length);
        }
        quads[qlen++] = pad(lastQuad, lastQuadBytes);
        String name = _symbols.findName(quads, qlen);
        if (name == null) {
            return addName(quads, qlen, lastQuadBytes);
        }
        return name;
    }

    /**
     * This is the main workhorse method used when we take a symbol
     * table miss. It needs to demultiplex individual bytes, decode
     * multi-byte chars (if any), and then construct Name instance
     * and add it to the symbol table.
     */
    private final String addName(int[] quads, int qlen, int lastQuadBytes) throws JsonParseException
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

    /*
    /**********************************************************
    /* Internal methods, String value parsing
    /**********************************************************
     */

    @Override
    protected void _finishString() throws IOException
    {
        int outPtr = 0;
        char[] outBuf = _textBuffer.emptyAndGetCurrentSegment();
        final int[] codes = _icUTF8;
        final int outEnd = outBuf.length;

        do {
            int c = _inputData.readUnsignedByte();
            if (codes[c] != 0) {
                if (c == INT_QUOTE) {
                    _textBuffer.setCurrentLength(outPtr);
                    return;
                }
                _finishString2(outBuf, outPtr, c);
                return;
            }
            outBuf[outPtr++] = (char) c;
        } while (outPtr < outEnd);
        _finishString2(outBuf, outPtr, _inputData.readUnsignedByte());
    }

    private String _finishAndReturnString() throws IOException
    {
        int outPtr = 0;
        char[] outBuf = _textBuffer.emptyAndGetCurrentSegment();
        final int[] codes = _icUTF8;
        final int outEnd = outBuf.length;

        do {
            int c = _inputData.readUnsignedByte();
            if (codes[c] != 0) {
                if (c == INT_QUOTE) {
                    return _textBuffer.setCurrentAndReturn(outPtr);
                }
                _finishString2(outBuf, outPtr, c);
                return _textBuffer.contentsAsString();
            }
            outBuf[outPtr++] = (char) c;
        } while (outPtr < outEnd);
        _finishString2(outBuf, outPtr, _inputData.readUnsignedByte());
        return _textBuffer.contentsAsString();
    }

    private final void _finishString2(char[] outBuf, int outPtr, int c)
        throws IOException
    {
        // Here we do want to do full decoding, hence:
        final int[] codes = _icUTF8;
        int outEnd = outBuf.length;

        main_loop:
        for (;; c = _inputData.readUnsignedByte()) {
            // Then the tight ASCII non-funny-char loop:
            while (codes[c] == 0) {
                if (outPtr >= outEnd) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                    outEnd = outBuf.length;
                }
                outBuf[outPtr++] = (char) c;
                c = _inputData.readUnsignedByte();
            }
            // Ok: end marker, escape or multi-byte?
            if (c == INT_QUOTE) {
                break main_loop;
            }
            switch (codes[c]) {
            case 1: // backslash
                c = _decodeEscaped();
                break;
            case 2: // 2-byte UTF
                c = _decodeUtf8_2(c);
                break;
            case 3: // 3-byte UTF
                c = _decodeUtf8_3(c);
                break;
            case 4: // 4-byte UTF
                c = _decodeUtf8_4(c);
                // Let's add first part right away:
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                    outEnd = outBuf.length;
                }
                outBuf[outPtr++] = (char) (0xD800 | (c >> 10));
                c = 0xDC00 | (c & 0x3FF);
                // And let the other char output down below
                break;
            default:
                if (c < INT_SPACE) {
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
                outEnd = outBuf.length;
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
     *   {@link JsonParseException} for decoding problems
     */
    protected void _skipString() throws IOException
    {
        _tokenIncomplete = false;

        // Need to be fully UTF-8 aware here:
        final int[] codes = _icUTF8;

        main_loop:
        while (true) {
            int c;

            ascii_loop:
            while (true) {
                c = _inputData.readUnsignedByte();
                if (codes[c] != 0) {
                    break ascii_loop;
                }
            }
            // Ok: end marker, escape or multi-byte?
            if (c == INT_QUOTE) {
                break main_loop;
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
     * @param c First undecoded character of unexpected (but possibly ultimate accepted) value
     *
     * @return Token that was successfully decoded (if successful)
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    protected JsonToken _handleUnexpectedValue(int c)
        throws IOException
    {
        // Most likely an error, unless we are to allow single-quote-strings
        switch (c) {
        case ']':
            if (!_parsingContext.inArray()) {
                break;
            }
            // fall through
        case ',':
            /* !!! TODO: 08-May-2016, tatu: To support `Feature.ALLOW_MISSING_VALUES` would
             *    need handling here...
             */
            // 11-May-2020, tatu: [core#616] No commas in root level
            if (!_parsingContext.inRoot()) {
                if ((_features & FEAT_MASK_ALLOW_MISSING) != 0) {
//               _inputPtr--;
                    _nextByte = c;
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
            _reportError("Non-standard token 'NaN': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
            break;
        case 'I':
            _matchToken("Infinity", 1);
            if ((_features & FEAT_MASK_NON_NUM_NUMBERS) != 0) {
                return resetAsNaN("Infinity", Double.POSITIVE_INFINITY);
            }
            _reportError("Non-standard token 'Infinity': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
            break;
        case '+': // note: '-' is taken as number
            return _handleInvalidNumberStart(_inputData.readUnsignedByte(), false);
        }
        // [core#77] Try to decode most likely token
        if (Character.isJavaIdentifierStart(c)) {
            _reportInvalidToken(c, ""+((char) c), _validJsonTokenList());
        }
        // but if it doesn't look like a token:
        _reportUnexpectedChar(c, "expected a valid value "+_validJsonValueList());
        return null;
    }

    protected JsonToken _handleApos() throws IOException
    {
        int c = 0;
        // Otherwise almost verbatim copy of _finishString()
        int outPtr = 0;
        char[] outBuf = _textBuffer.emptyAndGetCurrentSegment();

        // Here we do want to do full decoding, hence:
        final int[] codes = _icUTF8;

        main_loop:
        while (true) {
            // Then the tight ascii non-funny-char loop:
            ascii_loop:
            while (true) {
                int outEnd = outBuf.length;
                if (outPtr >= outBuf.length) {
                    outBuf = _textBuffer.finishCurrentSegment();
                    outPtr = 0;
                    outEnd = outBuf.length;
                }
                do {
                    c = _inputData.readUnsignedByte();
                    if (c == '\'') {
                        break main_loop;
                    }
                    if ((codes[c] != 0)
                        // 13-Oct-2021, tatu: [core#721] Alas, regular quote is included as
                        //    special, need to ignore here
                            && (c != INT_QUOTE)) {
                        break ascii_loop;
                    }
                    outBuf[outPtr++] = (char) c;
                } while (outPtr < outEnd);
            }
            switch (codes[c]) {
            case 1: // backslash
                c = _decodeEscaped();
                break;
            case 2: // 2-byte UTF
                c = _decodeUtf8_2(c);
                break;
            case 3: // 3-byte UTF
                c = _decodeUtf8_3(c);
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
     * Method called if expected numeric value (due to leading sign) does not
     * look like a number
     */
    protected JsonToken _handleInvalidNumberStart(int ch, boolean neg)
        throws IOException
    {
        while (ch == 'I') {
            ch = _inputData.readUnsignedByte();
            String match;
            if (ch == 'N') {
                match = neg ? "-INF" :"+INF";
            } else if (ch == 'n') {
                match = neg ? "-Infinity" :"+Infinity";
            } else {
                break;
            }
            _matchToken(match, 3);
            if ((_features & FEAT_MASK_NON_NUM_NUMBERS) != 0) {
                return resetAsNaN(match, neg ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
            }
            _reportError("Non-standard token '"+match+"': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
        }
        reportUnexpectedNumberChar(ch, "expected digit (0-9) to follow minus sign, for valid numeric value");
        return null;
    }

    protected final void _matchToken(String matchStr, int i) throws IOException
    {
        final int len = matchStr.length();
        do {
            int ch = _inputData.readUnsignedByte();
            if (ch != matchStr.charAt(i)) {
                _reportInvalidToken(ch, matchStr.substring(0, i));
            }
        } while (++i < len);

        int ch = _inputData.readUnsignedByte();
        if (ch >= '0' && ch != ']' && ch != '}') { // expected/allowed chars
            _checkMatchEnd(matchStr, i, ch);
        }
        _nextByte = ch;
    }

    private final void _checkMatchEnd(String matchStr, int i, int ch) throws IOException {
        // but actually only alphanums are problematic
        char c = (char) _decodeCharForError(ch);
        if (Character.isJavaIdentifierPart(c)) {
            _reportInvalidToken(c, matchStr.substring(0, i));
        }
    }

    /*
    /**********************************************************
    /* Internal methods, ws skipping, escape/unescape
    /**********************************************************
     */

    private final int _skipWS() throws IOException
    {
        int i = _nextByte;
        if (i < 0) {
            i = _inputData.readUnsignedByte();
        } else {
            _nextByte = -1;
        }
        while (true) {
            if (i > INT_SPACE) {
                if (i == INT_SLASH || i == INT_HASH) {
                    return _skipWSComment(i);
                }
                return i;
            } else {
                // 06-May-2016, tatu: Could verify validity of WS, but for now why bother.
                //   ... but line number is useful thingy
                if (i == INT_CR || i == INT_LF) {
                    ++_currInputRow;
                }
            }
            i = _inputData.readUnsignedByte();
        }
    }

    /**
     * Alternative to {@link #_skipWS} that handles possible {@link EOFException}
     * caused by trying to read past the end of {@link InputData}.
     *
     * @since 2.9
     */
    private final int _skipWSOrEnd() throws IOException
    {
        int i = _nextByte;
        if (i < 0) {
            try {
                i = _inputData.readUnsignedByte();
            } catch (EOFException e) {
                return _eofAsNextChar();
            }
        } else {
            _nextByte = -1;
        }
        while (true) {
            if (i > INT_SPACE) {
                if (i == INT_SLASH || i == INT_HASH) {
                    return _skipWSComment(i);
                }
                return i;
            } else {
                // 06-May-2016, tatu: Could verify validity of WS, but for now why bother.
                //   ... but line number is useful thingy
                if (i == INT_CR || i == INT_LF) {
                    ++_currInputRow;
                }
            }
            try {
                i = _inputData.readUnsignedByte();
            } catch (EOFException e) {
                return _eofAsNextChar();
            }
        }
    }

    private final int _skipWSComment(int i) throws IOException
    {
        while (true) {
            if (i > INT_SPACE) {
                if (i == INT_SLASH) {
                    _skipComment();
                } else if (i == INT_HASH) {
                    if (!_skipYAMLComment()) {
                        return i;
                    }
                } else {
                    return i;
                }
            } else {
                // 06-May-2016, tatu: Could verify validity of WS, but for now why bother.
                //   ... but line number is useful thingy
                if (i == INT_CR || i == INT_LF) {
                    ++_currInputRow;
                }
                /*
                if ((i != INT_SPACE) && (i != INT_LF) && (i != INT_CR)) {
                    _throwInvalidSpace(i);
                }
                */
            }
            i = _inputData.readUnsignedByte();
        }
    }

    private final int _skipColon() throws IOException
    {
        int i = _nextByte;
        if (i < 0) {
            i = _inputData.readUnsignedByte();
        } else {
            _nextByte = -1;
        }
        // Fast path: colon with optional single-space/tab before and/or after:
        if (i == INT_COLON) { // common case, no leading space
            i = _inputData.readUnsignedByte();
            if (i > INT_SPACE) { // nor trailing
                if (i == INT_SLASH || i == INT_HASH) {
                    return _skipColon2(i, true);
                }
                return i;
            }
            if (i == INT_SPACE || i == INT_TAB) {
                i = _inputData.readUnsignedByte();
                if (i > INT_SPACE) {
                    if (i == INT_SLASH || i == INT_HASH) {
                        return _skipColon2(i, true);
                    }
                    return i;
                }
            }
            return _skipColon2(i, true); // true -> skipped colon
        }
        if (i == INT_SPACE || i == INT_TAB) {
            i = _inputData.readUnsignedByte();
        }
        if (i == INT_COLON) {
            i = _inputData.readUnsignedByte();
            if (i > INT_SPACE) {
                if (i == INT_SLASH || i == INT_HASH) {
                    return _skipColon2(i, true);
                }
                return i;
            }
            if (i == INT_SPACE || i == INT_TAB) {
                i = _inputData.readUnsignedByte();
                if (i > INT_SPACE) {
                    if (i == INT_SLASH || i == INT_HASH) {
                        return _skipColon2(i, true);
                    }
                    return i;
                }
            }
            return _skipColon2(i, true);
        }
        return _skipColon2(i, false);
    }

    private final int _skipColon2(int i, boolean gotColon) throws IOException
    {
        for (;; i = _inputData.readUnsignedByte()) {
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
            } else {
                // 06-May-2016, tatu: Could verify validity of WS, but for now why bother.
                //   ... but line number is useful thingy
                if (i == INT_CR || i == INT_LF) {
                    ++_currInputRow;
                }
            }
        }
    }

    private final void _skipComment() throws IOException
    {
        if ((_features & FEAT_MASK_ALLOW_JAVA_COMMENTS) == 0) {
            _reportUnexpectedChar('/', "maybe a (non-standard) comment? (not recognized as one since Feature 'ALLOW_COMMENTS' not enabled for parser)");
        }
        int c = _inputData.readUnsignedByte();
        if (c == '/') {
            _skipLine();
        } else if (c == '*') {
            _skipCComment();
        } else {
            _reportUnexpectedChar(c, "was expecting either '*' or '/' for a comment");
        }
    }

    private final void _skipCComment() throws IOException
    {
        // Need to be UTF-8 aware here to decode content (for skipping)
        final int[] codes = CharTypes.getInputCodeComment();
        int i = _inputData.readUnsignedByte();

        // Ok: need the matching '*/'
        main_loop:
        while (true) {
            int code = codes[i];
            if (code != 0) {
                switch (code) {
                case '*':
                    i = _inputData.readUnsignedByte();
                    if (i == INT_SLASH) {
                        return;
                    }
                    continue main_loop;
                case INT_LF:
                case INT_CR:
                    ++_currInputRow;
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
            i = _inputData.readUnsignedByte();
        }
    }

    private final boolean _skipYAMLComment() throws IOException
    {
        if ((_features & FEAT_MASK_ALLOW_YAML_COMMENTS) == 0) {
            return false;
        }
        _skipLine();
        return true;
    }

    /**
     * Method for skipping contents of an input line; usually for CPP
     * and YAML style comments.
     */
    private final void _skipLine() throws IOException
    {
        // Ok: need to find EOF or linefeed
        final int[] codes = CharTypes.getInputCodeComment();
        while (true) {
            int i = _inputData.readUnsignedByte();
            int code = codes[i];
            if (code != 0) {
                switch (code) {
                case INT_LF:
                case INT_CR:
                    ++_currInputRow;
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
    protected char _decodeEscaped() throws IOException
    {
        int c = _inputData.readUnsignedByte();

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
            int ch = _inputData.readUnsignedByte();
            int digit = CharTypes.charToHex(ch);
            if (digit < 0) {
                _reportUnexpectedChar(ch, "expected a hex-digit for character escape sequence");
            }
            value = (value << 4) | digit;
        }
        return (char) value;
    }

    protected int _decodeCharForError(int firstByte) throws IOException
    {
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

            int d = _inputData.readUnsignedByte();
            if ((d & 0xC0) != 0x080) {
                _reportInvalidOther(d & 0xFF);
            }
            c = (c << 6) | (d & 0x3F);

            if (needed > 1) { // needed == 1 means 2 bytes total
                d = _inputData.readUnsignedByte(); // 3rd byte
                if ((d & 0xC0) != 0x080) {
                    _reportInvalidOther(d & 0xFF);
                }
                c = (c << 6) | (d & 0x3F);
                if (needed > 2) { // 4 bytes? (need surrogates)
                    d = _inputData.readUnsignedByte();
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

    private final int _decodeUtf8_2(int c) throws IOException
    {
        int d = _inputData.readUnsignedByte();
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF);
        }
        return ((c & 0x1F) << 6) | (d & 0x3F);
    }

    private final int _decodeUtf8_3(int c1) throws IOException
    {
        c1 &= 0x0F;
        int d = _inputData.readUnsignedByte();
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF);
        }
        int c = (c1 << 6) | (d & 0x3F);
        d = _inputData.readUnsignedByte();
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF);
        }
        c = (c << 6) | (d & 0x3F);
        return c;
    }

    /**
     * @return Character value <b>minus 0x10000</c>; this so that caller
     *    can readily expand it to actual surrogates
     */
    private final int _decodeUtf8_4(int c) throws IOException
    {
        int d = _inputData.readUnsignedByte();
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF);
        }
        c = ((c & 0x07) << 6) | (d & 0x3F);
        d = _inputData.readUnsignedByte();
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF);
        }
        c = (c << 6) | (d & 0x3F);
        d = _inputData.readUnsignedByte();
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF);
        }

        /* note: won't change it to negative here, since caller
         * already knows it'll need a surrogate
         */
        return ((c << 6) | (d & 0x3F)) - 0x10000;
    }

    private final void _skipUtf8_2() throws IOException
    {
        int c = _inputData.readUnsignedByte();
        if ((c & 0xC0) != 0x080) {
            _reportInvalidOther(c & 0xFF);
        }
    }

    /* Alas, can't heavily optimize skipping, since we still have to
     * do validity checks...
     */
    private final void _skipUtf8_3() throws IOException
    {
        //c &= 0x0F;
        int c = _inputData.readUnsignedByte();
        if ((c & 0xC0) != 0x080) {
            _reportInvalidOther(c & 0xFF);
        }
        c = _inputData.readUnsignedByte();
        if ((c & 0xC0) != 0x080) {
            _reportInvalidOther(c & 0xFF);
        }
    }

    private final void _skipUtf8_4() throws IOException
    {
        int d = _inputData.readUnsignedByte();
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF);
        }
        d = _inputData.readUnsignedByte();
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF);
        }
        d = _inputData.readUnsignedByte();
        if ((d & 0xC0) != 0x080) {
            _reportInvalidOther(d & 0xFF);
        }
    }

    /*
    /**********************************************************
    /* Internal methods, error reporting
    /**********************************************************
     */

    protected void _reportInvalidToken(int ch, String matchedPart) throws IOException
     {
         _reportInvalidToken(ch, matchedPart, _validJsonTokenList());
     }

    protected void _reportInvalidToken(int ch, String matchedPart, String msg)
        throws IOException
     {
         StringBuilder sb = new StringBuilder(matchedPart);

         /* Let's just try to find what appears to be the token, using
          * regular Java identifier character rules. It's just a heuristic,
          * nothing fancy here (nor fast).
          */
         while (true) {
             char c = (char) _decodeCharForError(ch);
             if (!Character.isJavaIdentifierPart(c)) {
                 break;
             }
             sb.append(c);
             ch = _inputData.readUnsignedByte();
         }
         _reportError("Unrecognized token '"+sb.toString()+"': was expecting "+msg);
     }

    protected void _reportInvalidChar(int c)
        throws JsonParseException
    {
        // Either invalid WS or illegal UTF-8 start char
        if (c < INT_SPACE) {
            _throwInvalidSpace(c);
        }
        _reportInvalidInitial(c);
    }

    protected void _reportInvalidInitial(int mask)
        throws JsonParseException
    {
        _reportError("Invalid UTF-8 start byte 0x"+Integer.toHexString(mask));
    }

    private void _reportInvalidOther(int mask)
        throws JsonParseException
    {
        _reportError("Invalid UTF-8 middle byte 0x"+Integer.toHexString(mask));
    }

    private static int[] _growArrayBy(int[] arr, int more)
    {
        if (arr == null) {
            return new int[more];
        }
        return Arrays.copyOf(arr, arr.length + more);
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
    protected final byte[] _decodeBase64(Base64Variant b64variant) throws IOException
    {
        ByteArrayBuilder builder = _getByteArrayBuilder();

        //main_loop:
        while (true) {
            // first, we'll skip preceding white space, if any
            int ch;
            do {
                ch = _inputData.readUnsignedByte();
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
            ch = _inputData.readUnsignedByte();
            bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
                bits = _decodeBase64Escape(b64variant, ch, 1);
            }
            decodedData = (decodedData << 6) | bits;
            // third base64 char; can be padding, but not ws
            ch = _inputData.readUnsignedByte();
            bits = b64variant.decodeBase64Char(ch);

            // First branch: can get padding (-> 1 byte)
            if (bits < 0) {
                if (bits != Base64Variant.BASE64_VALUE_PADDING) {
                    // could also just be 'missing'  padding
                    if (ch == INT_QUOTE) {
                        decodedData >>= 4;
                        builder.append(decodedData);
                        if (b64variant.usesPadding()) {
                            _handleBase64MissingPadding(b64variant);
                        }
                        return builder.toByteArray();
                    }
                    bits = _decodeBase64Escape(b64variant, ch, 2);
                }
                if (bits == Base64Variant.BASE64_VALUE_PADDING) {
                    ch = _inputData.readUnsignedByte();
                    if (!b64variant.usesPaddingChar(ch)) {
                        if ((ch != INT_BACKSLASH)
                                || _decodeBase64Escape(b64variant, ch, 3) != Base64Variant.BASE64_VALUE_PADDING) {
                            throw reportInvalidBase64Char(b64variant, ch, 3, "expected padding character '"+b64variant.getPaddingChar()+"'");
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
            ch = _inputData.readUnsignedByte();
            bits = b64variant.decodeBase64Char(ch);
            if (bits < 0) {
                if (bits != Base64Variant.BASE64_VALUE_PADDING) {
                    // could also just be 'missing'  padding
                    if (ch == INT_QUOTE) {
                        decodedData >>= 2;
                        builder.appendTwoBytes(decodedData);
                        if (b64variant.usesPadding()) {
                            _handleBase64MissingPadding(b64variant);
                        }
                        return builder.toByteArray();
                    }
                    bits = _decodeBase64Escape(b64variant, ch, 3);
                }
                if (bits == Base64Variant.BASE64_VALUE_PADDING) {
                    /* With padding we only get 2 bytes; but we have
                     * to shift it a bit so it is identical to triplet
                     * case with partial output.
                     * 3 chars gives 3x6 == 18 bits, of which 2 are
                     * dummies, need to discard:
                     */
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
    public JsonLocation getTokenLocation() {
        // 03-Jan-2020, tatu: Should probably track this, similar to how
        //   streaming parsers do it, but... not done yet

//        if (_currToken == JsonToken.FIELD_NAME) {
//           return new JsonLocation(_getSourceReference(),
//                    -1L, -1L, _nameStartRow, _nameStartCol);
//        }

        // No column tracking since we do not have pointers, DataInput has no offset

        return new JsonLocation(_contentReference(), -1L, -1L, _tokenInputRow, -1);
    }

    @Override
    public JsonLocation getCurrentLocation() {
        // No column tracking since we do not have pointers, DataInput has no offset
        final int col = -1;
        return new JsonLocation(_contentReference(), -1L, -1L,
                _currInputRow, col);
    }

    /*
    /**********************************************************
    /* Internal methods, other
    /**********************************************************
     */

    private void _closeScope(int i) throws JsonParseException {
        if (i == INT_RBRACKET) {
            if (!_parsingContext.inArray()) {
                _reportMismatchedEndMarker(i, '}');
            }
            _parsingContext = _parsingContext.clearAndGetParent();
            _currToken = JsonToken.END_ARRAY;
        }
        if (i == INT_RCURLY) {
            if (!_parsingContext.inObject()) {
                _reportMismatchedEndMarker(i, ']');
            }
            _parsingContext = _parsingContext.clearAndGetParent();
            _currToken = JsonToken.END_OBJECT;
        }
    }

    /**
     * Helper method needed to fix [Issue#148], masking of 0x00 character
     */
    private final static int pad(int q, int bytes) {
        return (bytes == 4) ? q : (q | (-1 << (bytes << 3)));
    }
}
