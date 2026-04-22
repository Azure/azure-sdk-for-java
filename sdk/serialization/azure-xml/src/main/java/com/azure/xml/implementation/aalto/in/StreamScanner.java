// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
/* Aalto XML processor
 *
 * Copyright (c) 2006- Tatu Saloranta, tatu.saloranta@iki.fi
 *
 * Licensed under the License specified in the file LICENSE which is
 * included with the source code.
 * You may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.azure.xml.implementation.aalto.in;

import com.azure.xml.implementation.aalto.impl.ErrorConsts;
import com.azure.xml.implementation.aalto.impl.LocationImpl;
import com.azure.xml.implementation.aalto.impl.StreamExceptionBase;
import com.azure.xml.implementation.aalto.util.DataUtil;
import com.azure.xml.implementation.aalto.util.TextBuilder;
import com.azure.xml.implementation.aalto.util.XmlCharTypes;
import com.azure.xml.implementation.aalto.util.XmlChars;
import com.azure.xml.implementation.aalto.util.XmlConsts;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Base class for various byte stream based scanners (generally one
 * for each type of encoding supported).
 */
@SuppressWarnings("fallthrough")
public final class StreamScanner extends XmlScanner {
    /*
    /**********************************************************************
    /* Byte constants
    /**********************************************************************
     */

    // White-space:

    private final static byte BYTE_SPACE = (byte) ' ';
    private final static byte BYTE_LF = (byte) '\n';
    private final static byte BYTE_CR = (byte) '\r';
    private final static byte BYTE_TAB = (byte) 9;

    private final static byte BYTE_LT = (byte) '<';
    private final static byte BYTE_GT = (byte) '>';
    private final static byte BYTE_AMP = (byte) '&';
    private final static byte BYTE_HASH = (byte) '#';
    private final static byte BYTE_EXCL = (byte) '!';
    private final static byte BYTE_HYPHEN = (byte) '-';
    private final static byte BYTE_QMARK = (byte) '?';
    private final static byte BYTE_SLASH = (byte) '/';
    private final static byte BYTE_LBRACKET = (byte) '[';
    private final static byte BYTE_RBRACKET = (byte) ']';
    private final static byte BYTE_SEMICOLON = (byte) ';';

    private final static byte BYTE_a = (byte) 'a';
    private final static byte BYTE_g = (byte) 'g';
    private final static byte BYTE_l = (byte) 'l';
    private final static byte BYTE_m = (byte) 'm';
    private final static byte BYTE_o = (byte) 'o';
    private final static byte BYTE_p = (byte) 'p';
    private final static byte BYTE_q = (byte) 'q';
    private final static byte BYTE_s = (byte) 's';
    private final static byte BYTE_t = (byte) 't';
    private final static byte BYTE_u = (byte) 'u';
    private final static byte BYTE_x = (byte) 'x';

    private final static byte BYTE_D = (byte) 'D';
    private final static byte BYTE_P = (byte) 'P';
    private final static byte BYTE_S = (byte) 'S';

    /*
    /**********************************************************************
    /* Input buffering
    /**********************************************************************
     */

    /**
     * Pointer to the next unread byte in the input buffer.
     */
    private int _inputPtr;

    /**
     * Pointer to the first byte <b>after</b> the end of valid content.
     * This may point beyond of the physical buffer array.
     */
    private int _inputEnd;

    /*
    /**********************************************************************
    /* Parsing state
    /**********************************************************************
     */

    /**
     * Storage location for a single character that can not be easily
     * pushed back (for example, multi-byte char; or char entity
     * expansion). Negative, if from entity expansion; positive if
     * a singular char.
     */
    private int _tmpChar = INT_NULL;

    /*
    /**********************************************************************
    /* Configuration, input, buffering
    /**********************************************************************
     */

    /**
     * Underlying InputStream to use for reading content.
     */
    private InputStream _in;

    private byte[] _inputBuffer;

    /*
    /**********************************************************************
    /* Character, name decoding
    /**********************************************************************
     */

    /**
     * This is a simple container object that is used to access the
     * decoding tables for characters. Indirection is needed since
     * we actually support multiple utf-8 compatible encodings, not
     * just utf-8 itself.
     */
    private final XmlCharTypes _charTypes;

    /**
     * For now, symbol table contains prefixed names. In future it is
     * possible that they may be split into prefixes and local names?
     */
    private final ByteBasedPNameTable _symbols;

    /**
     * This buffer is used for name parsing. Will be expanded if/as
     * needed; 32 ints can hold names 128 ascii chars long.
     */
    private int[] _quadBuffer = new int[32];

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    public StreamScanner(ReaderConfig cfg, InputStream in, byte[] buffer, int ptr, int last) {
        super(cfg);
        _charTypes = cfg.getCharTypes();
        _symbols = cfg.getBBSymbols();

        _in = in;
        _inputBuffer = buffer;
        _inputPtr = ptr;
        _inputEnd = last;
        _pastBytesOrChars = 0; // should it be passed by caller?
        _rowStartOffset = 0; // should probably be passed by caller...
    }

    @Override
    protected void _releaseBuffers() {
        super._releaseBuffers();
        if (_symbols.maybeDirty()) {
            _config.updateBBSymbols(_symbols);
        }
        // Note: if we have block input (_in == null), the buffer we use is
        // not owned by scanner, can't recycle.
        // Also note that this method will always get called before _closeSource()
        // so that _in won't be cleared before we  have a chance to see it.
        if (_in != null) {
            if (_inputBuffer != null) {
                _config.freeFullBBuffer(_inputBuffer);
                _inputBuffer = null;
            }
        }
    }

    @Override
    protected void _closeSource() throws IOException {
        if (_in != null) {
            _in.close();
            _in = null;
        }
    }

    /*
    /**********************************************************************
    /* Location handling
    /**********************************************************************
     */

    @Override
    public Location getCurrentLocation() {
        return LocationImpl.fromZeroBased(_pastBytesOrChars + _inputPtr, _currRow, _inputPtr - _rowStartOffset);
    }

    private void markLF(int offset) {
        _rowStartOffset = offset;
        ++_currRow;
    }

    private void markLF() {
        _rowStartOffset = _inputPtr;
        ++_currRow;
    }

    private void setStartLocation() {
        _startRawOffset = _pastBytesOrChars + _inputPtr;
        _startRow = _currRow;
        _startColumn = _inputPtr - _rowStartOffset;
    }

    /**
     * Method called when an ampersand is encounter in text segment.
     * Method needs to determine whether it is a pre-defined or character
     * entity (in which case it will be expanded into a single char or
     * surrogate pair), or a general
     * entity (in which case it will most likely be returned as
     * ENTITY_REFERENCE event)
     *
     * @return 0 if a general parsed entity encountered; integer
     * value of a (valid) XML content character otherwise
     */
    private int handleEntityInText() throws XMLStreamException {
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        byte b = _inputBuffer[_inputPtr++];
        if (b == BYTE_HASH) {
            return handleCharEntity();
        }
        String start;
        if (b == BYTE_a) { // amp or apos?
            b = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne();
            if (b == BYTE_m) { // amp?
                b = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne();
                if (b == BYTE_p) {
                    b = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne();
                    if (b == BYTE_SEMICOLON) {
                        return INT_AMP;
                    }
                    start = "amp";
                } else {
                    start = "am";
                }
            } else if (b == BYTE_p) { // apos?
                b = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne();
                if (b == BYTE_o) {
                    b = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne();
                    if (b == BYTE_s) {
                        b = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne();
                        if (b == BYTE_SEMICOLON) {
                            return INT_APOS;
                        }
                        start = "apos";
                    } else {
                        start = "apo";
                    }
                } else {
                    start = "ap";
                }
            } else {
                start = "a";
            }
        } else if (b == BYTE_l) { // lt?
            b = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne();
            if (b == BYTE_t) {
                b = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne();
                if (b == BYTE_SEMICOLON) {
                    return INT_LT;
                }
                start = "lt";
            } else {
                start = "l";
            }
        } else if (b == BYTE_g) { // gt?
            b = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne();
            if (b == BYTE_t) {
                b = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne();
                if (b == BYTE_SEMICOLON) {
                    return INT_GT;
                }
                start = "gt";
            } else {
                start = "g";
            }
        } else if (b == BYTE_q) { // quot?
            b = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne();
            if (b == BYTE_u) {
                b = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne();
                if (b == BYTE_o) {
                    b = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne();
                    if (b == BYTE_t) {
                        b = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne();
                        if (b == BYTE_SEMICOLON) {
                            return INT_QUOTE;
                        }
                        start = "quot";
                    } else {
                        start = "quo";
                    }
                } else {
                    start = "qu";
                }
            } else {
                start = "q";
            }
        } else {
            start = "";
        }

        final int[] TYPES = _charTypes.NAME_CHARS;

        /* All righty: we have the beginning of the name, plus the first
         * byte too. So let's see what we can do with it.
         */
        char[] cbuf = _nameBuffer;
        int cix = 0;
        for (int len = start.length(); cix < len; ++cix) {
            cbuf[cix] = start.charAt(cix);
        }
        //int colon = -1;
        while (b != BYTE_SEMICOLON) {
            boolean ok;
            int c = (int) b & 0xFF;

            // Has to be a valid name start char though:
            switch (TYPES[c]) {
                case XmlCharTypes.CT_NAME_NONE:
                case XmlCharTypes.CT_NAME_COLON: // not ok for entities?
                case XmlCharTypes.CT_NAME_NONFIRST:
                    ok = (cix > 0);
                    break;

                case XmlCharTypes.CT_NAME_ANY:
                    ok = true;
                    break;

                case InputCharTypes.CT_INPUT_NAME_MB_2:
                    c = decodeUtf8_2(c);
                    ok = XmlChars.is10NameStartChar(c);
                    break;

                case InputCharTypes.CT_INPUT_NAME_MB_3:
                    c = decodeUtf8_3(c);
                    ok = XmlChars.is10NameStartChar(c);
                    break;

                case InputCharTypes.CT_INPUT_NAME_MB_4:
                    c = decodeUtf8_4(c);
                    ok = XmlChars.is10NameStartChar(c);
                    if (ok) {
                        if (cix >= cbuf.length) {
                            _nameBuffer = cbuf = DataUtil.growArrayBy(cbuf, cbuf.length);
                        }
                        // Let's add first part right away:
                        c -= 0x10000;
                        cbuf[cix++] = (char) (0xD800 | (c >> 10));
                        c = 0xDC00 | (c & 0x3FF);
                    }
                    break;

                case InputCharTypes.CT_INPUT_NAME_MB_N:
                default:
                    ok = false;
                    break;
            }
            if (!ok) {
                reportInvalidNameChar(c, cix);
            }
            if (cix >= cbuf.length) {
                _nameBuffer = cbuf = DataUtil.growArrayBy(cbuf, cbuf.length);
            }
            cbuf[cix++] = (char) c;
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            b = _inputBuffer[_inputPtr++];
        }

        // Ok, let's construct a (temporary) entity name, then:
        String pname = new String(cbuf, 0, cix);
        // (note: hash is dummy... not to be compared to anything etc)
        _tokenName = new PNameC(pname, null, pname, 0);

        /* One more thing: do we actually allow entities in this mode
         * and with this event?
         */
        if (_config.willExpandEntities()) {
            reportInputProblem("General entity reference (&" + pname
                + ";) encountered in entity expanding mode: operation not (yet) implemented");
        }
        return 0;
    }

    /**
     * Parsing of public ids is bit more complicated than that of system
     * ids, since white space is to be coalesced.
     */
    private String parsePublicId(byte quoteChar) throws XMLStreamException {
        char[] outputBuffer = _nameBuffer;
        int outPtr = 0;
        final int[] TYPES = XmlCharTypes.PUBID_CHARS;
        boolean addSpace = false;

        while (true) {
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            // Easier to check without char type table, first:
            byte b = _inputBuffer[_inputPtr++];
            if (b == quoteChar) {
                break;
            }
            int c = (int) b & 0xFF;
            if (TYPES[c] != XmlCharTypes.PUBID_OK) {
                throwUnexpectedChar(c, " in public identifier");
            }

            // White space? Needs to be coalesced
            if (c <= INT_SPACE) {
                addSpace = true;
                continue;
            }
            if (addSpace) {
                if (outPtr >= outputBuffer.length) {
                    _nameBuffer = outputBuffer = DataUtil.growArrayBy(outputBuffer, outputBuffer.length);
                    outPtr = 0;
                }
                outputBuffer[outPtr++] = ' ';
                addSpace = false;
            }
            if (outPtr >= outputBuffer.length) {
                _nameBuffer = outputBuffer = DataUtil.growArrayBy(outputBuffer, outputBuffer.length);
                outPtr = 0;
            }
            outputBuffer[outPtr++] = (char) c;
        }
        return new String(outputBuffer, 0, outPtr);
    }

    private String parseSystemId(byte quoteChar) throws XMLStreamException {
        // caller has init'ed the buffer...
        char[] outputBuffer = _nameBuffer;
        int outPtr = 0;
        // attribute types are closest matches, so let's use them
        final int[] TYPES = _charTypes.ATTR_CHARS;
        //boolean spaceToAdd = false;

        main_loop: while (true) {
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            int c = (int) _inputBuffer[_inputPtr++] & 0xFF;
            if (TYPES[c] != 0) {
                switch (TYPES[c]) {
                    case XmlCharTypes.CT_INVALID:
                        handleInvalidXmlChar(c);
                    case XmlCharTypes.CT_WS_CR:
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        if (_inputBuffer[_inputPtr] == BYTE_LF) {
                            ++_inputPtr;
                        }
                        markLF();
                        c = INT_LF;
                        break;

                    case XmlCharTypes.CT_WS_LF:
                        markLF();
                        break;

                    case XmlCharTypes.CT_MULTIBYTE_2:
                        c = decodeUtf8_2(c);
                        break;

                    case XmlCharTypes.CT_MULTIBYTE_3:
                        c = decodeUtf8_3(c);
                        break;

                    case XmlCharTypes.CT_MULTIBYTE_4:
                        c = decodeUtf8_4(c);
                        if (outPtr >= outputBuffer.length) {
                            outputBuffer = _textBuilder.finishCurrentSegment();
                            outPtr = 0;
                        }
                        // Let's add first part right away:
                        outputBuffer[outPtr++] = (char) (0xD800 | (c >> 10));
                        c = 0xDC00 | (c & 0x3FF);
                        // And let the other char output down below
                        // And let the other char output down below
                        break;

                    case XmlCharTypes.CT_MULTIBYTE_N:
                        reportInvalidInitial(c);

                    case XmlCharTypes.CT_ATTR_QUOTE:
                        if (c == (int) quoteChar) {
                            break main_loop;
                        }
                }

            }

            if (outPtr >= outputBuffer.length) {
                _nameBuffer = outputBuffer = DataUtil.growArrayBy(outputBuffer, outputBuffer.length);
                outPtr = 0;
            }
            outputBuffer[outPtr++] = (char) c;
        }
        return new String(outputBuffer, 0, outPtr);
    }

    /*
    /**********************************************************************
    /* Implementation of parsing API
    /**********************************************************************
     */

    @Override
    public int nextFromProlog(boolean isProlog) throws XMLStreamException {
        if (_tokenIncomplete) { // left-overs from last thingy?
            skipToken();
        }

        // First: keep track of where event started
        setStartLocation();
        // Ok: we should get a WS or '<'. So, let's skip through WS
        while (true) {
            if (_inputPtr >= _inputEnd) {
                if (!loadMore()) {
                    setStartLocation();
                    return TOKEN_EOI;
                }
            }
            int c = _inputBuffer[_inputPtr++] & 0xFF;

            // Really should get white space or '<'...
            if (c == INT_LT) {
                break;
            }
            /* 26-Mar-2008, tatus: White space in prolog/epilog is
             *   not to be reported at all (by default at least), as
             *   it is not part of XML Infoset content. So let's
             *   just actively skip it here
             */
            if (c != INT_SPACE) {
                if (c == INT_LF) {
                    markLF();
                } else if (c == INT_CR) {
                    if (_inputPtr >= _inputEnd) {
                        if (!loadMore()) {
                            markLF();
                            setStartLocation();
                            return TOKEN_EOI;
                        }
                    }
                    if (_inputBuffer[_inputPtr] == BYTE_LF) {
                        ++_inputPtr;
                    }
                    markLF();
                } else if (c != INT_TAB) {
                    reportPrologUnexpChar(isProlog, decodeCharForError((byte) c), null);
                }
            }
        }

        // Ok, got LT:
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed(COMMENT); // not necessarily a comment of course
        }
        byte b = _inputBuffer[_inputPtr++];
        if (b == BYTE_EXCL) { // comment/DOCTYPE? (CDATA not legal)
            return handlePrologDeclStart(isProlog);
        }
        if (b == BYTE_QMARK) {
            return handlePIStart();
        }
        /* End tag not allowed if no open tree; and only one root
         * element (one root-level start tag)
         */
        if (b == BYTE_SLASH || !isProlog) {
            reportPrologUnexpElement(isProlog, b);
        }
        return handleStartElement(b);
    }

    @Override
    public int nextFromTree() throws XMLStreamException {
        if (_tokenIncomplete) { // left-overs?
            if (skipToken()) { // Figured out next event (ENTITY_REFERENCE)?
                // !!! We don't yet parse DTD, don't know real contents
                return _nextEntity();
            }
        } else { // note: START_ELEMENT/END_ELEMENT never incomplete
            if (_currToken == START_ELEMENT) {
                if (_isEmptyTag) {
                    --_depth;
                    return (_currToken = END_ELEMENT);
                }
            } else if (_currToken == END_ELEMENT) {
                _currElem = _currElem.getParent();
                // Any namespace declarations that need to be unbound?
                while (_lastNsDecl != null && _lastNsDecl.getLevel() >= _depth) {
                    _lastNsDecl = _lastNsDecl.unbind();
                }
            } else {
                // It's possible CHARACTERS entity with an entity ref:
                if (_entityPending) {
                    _entityPending = false;
                    return _nextEntity();
                }
            }
        }
        // and except for special cases, mark down actual start location of the event
        setStartLocation();

        /* Any more data? Although it'd be an error not to get any,
         * let's leave error reporting up to caller
         */
        if (_inputPtr >= _inputEnd) {
            if (!loadMore()) {
                setStartLocation();
                return TOKEN_EOI;
            }
        }
        byte b = _inputBuffer[_inputPtr];

        /* Can get pretty much any type; start/end element, comment/PI,
         * CDATA, text, entity reference...
         */
        if (b == BYTE_LT) { // root element, comment, proc instr?
            ++_inputPtr;
            b = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne(COMMENT);
            if (b == BYTE_EXCL) { // comment or CDATA
                return handleCommentOrCdataStart();
            }
            if (b == BYTE_QMARK) {
                return handlePIStart();
            }
            if (b == BYTE_SLASH) {
                return handleEndElement();
            }
            return handleStartElement(b);
        }
        if (b == BYTE_AMP) { // entity reference
            ++_inputPtr;
            /* Need to expand; should indicate either text, or an unexpanded
             * entity reference
             */
            int i = handleEntityInText();
            if (i == 0) { // general entity
                return (_currToken = ENTITY_REFERENCE);
            }
            /* Nope, a char entity; need to indicate it came from an entity.
             * Since we may want to store the char as is, too, let's negate
             * entity-based char
             */
            _tmpChar = -i;
        } else {
            /* Let's store it for future reference. May or may not be used --
             * so let's not advance input ptr quite yet.
             */
            _tmpChar = (int) b & 0xFF; // need to ensure it won't be negative
        }
        _tokenIncomplete = true;
        return (_currToken = CHARACTERS);
    }

    /**
     * Helper method used to isolate things that need to be (re)set in
     * cases where
     */
    private int _nextEntity() {
        // !!! Also, have to assume start location has been set or such
        _textBuilder.resetWithEmpty();
        // !!! TODO: handle start location?
        return (_currToken = ENTITY_REFERENCE);
    }

    /*
    /**********************************************************************
    /* Internal methods, secondary parsing
    /**********************************************************************
     */

    private int handlePrologDeclStart(boolean isProlog) throws XMLStreamException {
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        byte b = _inputBuffer[_inputPtr++];
        if (b == BYTE_HYPHEN) { // Comment?
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            b = _inputBuffer[_inputPtr++];
            if (b == BYTE_HYPHEN) {
                _tokenIncomplete = true;
                return (_currToken = COMMENT);
            }
        } else if (b == BYTE_D) { // DOCTYPE?
            if (isProlog) { // no DOCTYPE in epilog
                handleDtdStart();
                return DTD;
            }
        }

        /* error... for error recovery purposes, let's just pretend
         * like it was unfinished CHARACTERS, though.
         */
        _tokenIncomplete = true;
        _currToken = CHARACTERS;
        reportPrologUnexpChar(isProlog, decodeCharForError(b), " (expected '-' for COMMENT)");
        return _currToken; // never gets here
    }

    private void handleDtdStart() throws XMLStreamException {
        matchAsciiKeyword("DOCTYPE");

        // And then some white space and root  name
        byte b = skipInternalWs(true, "after DOCTYPE keyword, before root name");
        _tokenName = parsePName(b);
        b = skipInternalWs(false, null);

        if (b == BYTE_P) { // PUBLIC
            matchAsciiKeyword("PUBLIC");
            b = skipInternalWs(true, null);
            _publicId = parsePublicId(b);
            b = skipInternalWs(true, null);
            _systemId = parseSystemId(b);
            b = skipInternalWs(false, null);
        } else if (b == BYTE_S) { // SYSTEM
            matchAsciiKeyword("SYSTEM");
            b = skipInternalWs(true, null);
            _publicId = null;
            _systemId = parseSystemId(b);
            b = skipInternalWs(false, null);
        } else {
            _publicId = _systemId = null;
        }

        /* Ok; so, need to get either an internal subset, or the
         * end:
         */
        if (b == BYTE_GT) { // fine, we are done
            _tokenIncomplete = false;
            _currToken = DTD;
            return;
        }

        if (b != BYTE_LBRACKET) { // If not end, must have int. subset
            String msg = (_systemId != null)
                ? " (expected '[' for the internal subset, or '>' to end DOCTYPE declaration)"
                : " (expected a 'PUBLIC' or 'SYSTEM' keyword, '[' for the internal subset, or '>' to end DOCTYPE declaration)";
            reportTreeUnexpChar(decodeCharForError(b), msg);
        }

        /* Need not parse the int. subset yet, can leave as is, and then
         * either skip or parse later on
         */

        _tokenIncomplete = true;
        _currToken = DTD;
    }

    private int handleCommentOrCdataStart() throws XMLStreamException {
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        byte b = _inputBuffer[_inputPtr++];

        // Let's first see if it's a comment (simpler)
        if (b == BYTE_HYPHEN) { // Comment
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            b = _inputBuffer[_inputPtr++];
            if (b != BYTE_HYPHEN) {
                reportTreeUnexpChar(decodeCharForError(b), " (expected '-' for COMMENT)");
            }
            _tokenIncomplete = true;
            return (_currToken = COMMENT);
        }

        // If not, should be CDATA:
        if (b == BYTE_LBRACKET) { // CDATA
            _currToken = CDATA;
            for (int i = 0; i < 6; ++i) {
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                b = _inputBuffer[_inputPtr++];
                if (b != (byte) CDATA_STR.charAt(i)) {
                    int ch = decodeCharForError(b);
                    reportTreeUnexpChar(ch, " (expected '" + CDATA_STR.charAt(i) + "' for CDATA section)");
                }
            }
            _tokenIncomplete = true;
            return CDATA;
        }
        reportTreeUnexpChar(decodeCharForError(b), " (expected either '-' for COMMENT or '[CDATA[' for CDATA section)");
        return TOKEN_EOI; // never gets here
    }

    /**
     * Method called after leading '<?' has been parsed; needs to parse
     * target.
     */
    private int handlePIStart() throws XMLStreamException {
        _currToken = PROCESSING_INSTRUCTION;

        // Ok, first, need a name
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        byte b = _inputBuffer[_inputPtr++];
        _tokenName = parsePName(b);
        { // but is it "xml" (case insensitive)?
            String ln = _tokenName.getLocalName();
            if (ln.equalsIgnoreCase("xml") && _tokenName.getPrefix() == null) {
                reportInputProblem(ErrorConsts.ERR_WF_PI_XML_TARGET);
            }
        }

        /* Let's then verify that we either get a space, or closing
         * '?>': this way we'll catch some problems right away, and also
         * simplify actual processing of contents.
         */
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        int c = (int) _inputBuffer[_inputPtr++] & 0xFF;

        if (c <= INT_SPACE) {
            // Ok, let's skip the white space...
            while (true) {
                if (c == INT_LF) {
                    markLF();
                } else if (c == INT_CR) {
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    if (_inputBuffer[_inputPtr] == BYTE_LF) {
                        ++_inputPtr;
                    }
                    markLF();
                } else if (c != INT_SPACE && c != INT_TAB) {
                    throwInvalidSpace(c);
                }
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                c = (int) _inputBuffer[_inputPtr] & 0xFF;
                if (c > INT_SPACE) {
                    break;
                }
                ++_inputPtr;
            }
            // Ok, got non-space, need to push back:
            _tokenIncomplete = true;
        } else {
            if (c != INT_QMARK) {
                reportMissingPISpace(decodeCharForError((byte) c));
            }
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            b = _inputBuffer[_inputPtr++];
            if (b != BYTE_GT) {
                reportMissingPISpace(decodeCharForError(b));
            }
            _textBuilder.resetWithEmpty();
            _tokenIncomplete = false;
        }

        return PROCESSING_INSTRUCTION;
    }

    /**
     * @return Code point for the entity that expands to a valid XML
     *    content character.
     */
    private int handleCharEntity() throws XMLStreamException {
        // Hex or decimal?
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        byte b = _inputBuffer[_inputPtr++];
        int value = 0;
        if (b == BYTE_x) { // hex
            while (true) {
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                b = _inputBuffer[_inputPtr++];
                if (b == BYTE_SEMICOLON) {
                    break;
                }
                value = value << 4;
                int c = b;
                if (c <= '9' && c >= '0') {
                    value += (c - '0');
                } else if (c >= 'a' && c <= 'f') {
                    value += 10 + (c - 'a');
                } else if (c >= 'A' && c <= 'F') {
                    value += 10 + (c - 'A');
                } else {
                    throwUnexpectedChar(decodeCharForError(b), "; expected a hex digit (0-9a-fA-F)");
                }
                if (value > MAX_UNICODE_CHAR) { // Overflow?
                    reportEntityOverflow();
                }
            }
        } else { // numeric (decimal)
            while (b != BYTE_SEMICOLON) {
                int c = b;
                if (c <= '9' && c >= '0') {
                    value = (value * 10) + (c - '0');
                    if (value > MAX_UNICODE_CHAR) { // Overflow?
                        reportEntityOverflow();
                    }
                } else {
                    throwUnexpectedChar(decodeCharForError(b), "; expected a decimal number");
                }
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                b = _inputBuffer[_inputPtr++];
            }
        }
        verifyXmlChar(value);
        return value;
    }

    /**
     * Parsing of start element requires parsing of the element name
     * (and attribute names), and is thus encoding-specific.
     */
    private int handleStartElement(byte b) throws XMLStreamException {
        _currToken = START_ELEMENT;
        _currNsCount = 0;
        PName elemName = parsePName(b);

        /* Ok. Need to create a qualified name. Simplest for element
         * in default ns (no extra work -- expressed as null binding);
         * otherwise need to find binding
         */
        String prefix = elemName.getPrefix();
        boolean allBound; // flag to check 'late' bindings

        if (prefix == null) { // element in default ns
            allBound = true; // which need not be bound
        } else {
            elemName = bindName(elemName, prefix);
            allBound = elemName.isBound();
        }

        _tokenName = elemName;
        _currElem = new ElementScope(elemName, _currElem);

        // And then attribute parsing loop:
        int attrPtr = 0;

        while (true) {
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            b = _inputBuffer[_inputPtr++];
            int c = (int) b & 0xFF;
            // Intervening space to skip?
            if (c <= INT_SPACE) {
                do {
                    if (c == INT_LF) {
                        markLF();
                    } else if (c == INT_CR) {
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        if (_inputBuffer[_inputPtr] == BYTE_LF) {
                            ++_inputPtr;
                        }
                        markLF();
                    } else if (c != INT_SPACE && c != INT_TAB) {
                        throwInvalidSpace(c);
                    }
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    b = _inputBuffer[_inputPtr++];
                    c = (int) b & 0xFF;
                } while (c <= INT_SPACE);
            } else if (c != INT_SLASH && c != INT_GT) {
                c = decodeCharForError(b);
                throwUnexpectedChar(c, " expected space, or '>' or \"/>\"");
            }

            // Ok; either need to get an attribute name, or end marker:
            if (c == INT_SLASH) {
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                b = _inputBuffer[_inputPtr++];
                if (b != BYTE_GT) {
                    c = decodeCharForError(b);
                    throwUnexpectedChar(c, " expected '>'");
                }
                _isEmptyTag = true;
                break;
            } else if (c == INT_GT) {
                _isEmptyTag = false;
                break;
            } else if (c == INT_LT) {
                reportInputProblem("Unexpected '<' character in element (missing closing '>'?)");
            }

            // Ok, an attr name:
            PName attrName = parsePName(b);
            prefix = attrName.getPrefix();

            boolean isNsDecl;

            if (prefix == null) { // can be default ns decl:
                isNsDecl = (Objects.equals(attrName.getLocalName(), "xmlns"));
            } else {
                // May be a namespace decl though?
                if (prefix.equals("xmlns")) {
                    isNsDecl = true;
                } else {
                    attrName = bindName(attrName, prefix);
                    if (allBound) {
                        allBound = attrName.isBound();
                    }
                    isNsDecl = false;
                }
            }

            // Optional space to skip again
            while (true) {
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                b = _inputBuffer[_inputPtr++];
                c = (int) b & 0xFF;
                if (c > INT_SPACE) {
                    break;
                }
                if (c == INT_LF) {
                    markLF();
                } else if (c == INT_CR) {
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    if (_inputBuffer[_inputPtr] == BYTE_LF) {
                        ++_inputPtr;
                    }
                    markLF();
                } else if (c != INT_SPACE && c != INT_TAB) {
                    throwInvalidSpace(c);
                }
            }

            if (c != INT_EQ) {
                c = decodeCharForError(b);
                throwUnexpectedChar(c, " expected '='");
            }

            // Optional space to skip again
            while (true) {
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                b = _inputBuffer[_inputPtr++];
                c = (int) b & 0xFF;
                if (c > INT_SPACE) {
                    break;
                }
                if (c == INT_LF) {
                    markLF();
                } else if (c == INT_CR) {
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    if (_inputBuffer[_inputPtr] == BYTE_LF) {
                        ++_inputPtr;
                    }
                    markLF();
                } else if (c != INT_SPACE && c != INT_TAB) {
                    throwInvalidSpace(c);
                }
            }

            if (c != INT_QUOTE && c != INT_APOS) {
                c = decodeCharForError(b);
                throwUnexpectedChar(c, " Expected a quote");
            }

            /* Ok, finally: value parsing. However, ns URIs are to be handled
             * different from attribute values... let's offline URIs, since
             * they should be less common than attribute values.
             */
            if (isNsDecl) { // default ns, or explicit?
                handleNsDeclaration(attrName, b);
                ++_currNsCount;
            } else { // nope, a 'real' attribute:
                attrPtr = collectValue(attrPtr, b, attrName);
            }
        }
        {
            // Note: this call also checks attribute uniqueness
            int act = _attrCollector.finishLastValue(attrPtr);
            if (act < 0) { // error, dup attr indicated by -1
                act = _attrCollector.getCount(); // let's get correct count
                reportInputProblem(_attrCollector.getErrorMsg());
            }
            _attrCount = act;
        }
        ++_depth;

        /* Was there any prefix that wasn't bound prior to use?
         * That's legal, assuming declaration was found later on...
         * let's check
         */
        if (!allBound) {
            if (!elemName.isBound()) { // element itself unbound
                reportUnboundPrefix(_tokenName, false);
            }
            for (int i = 0, len = _attrCount; i < len; ++i) {
                PName attrName = _attrCollector.getName(i);
                if (!attrName.isBound()) {
                    reportUnboundPrefix(attrName, true);
                }
            }
        }
        return START_ELEMENT;
    }

    /**
     * This method implements the tight loop for parsing attribute
     * values. It's off-lined from the main start element method to
     * simplify main method, which makes code more maintainable
     * and possibly easier for JIT/HotSpot to optimize.
     */
    private int collectValue(int attrPtr, byte quoteByte, PName attrName) throws XMLStreamException {
        char[] attrBuffer = _attrCollector.startNewValue(attrName, attrPtr);
        final int[] TYPES = _charTypes.ATTR_CHARS;

        value_loop: while (true) {
            int c;

            ascii_loop: while (true) {
                int ptr = _inputPtr;
                if (ptr >= _inputEnd) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                }
                if (attrPtr >= attrBuffer.length) {
                    attrBuffer = _attrCollector.valueBufferFull();
                }
                int max = _inputEnd;
                {
                    int max2 = ptr + (attrBuffer.length - attrPtr);
                    if (max2 < max) {
                        max = max2;
                    }
                }
                while (ptr < max) {
                    c = (int) _inputBuffer[ptr++] & 0xFF;
                    if (TYPES[c] != 0) {
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                    attrBuffer[attrPtr++] = (char) c;
                }
                _inputPtr = ptr;
            }

            switch (TYPES[c]) {
                case XmlCharTypes.CT_INVALID:
                    handleInvalidXmlChar(c);
                case XmlCharTypes.CT_WS_CR:
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    if (_inputBuffer[_inputPtr] == BYTE_LF) {
                        ++_inputPtr;
                    }
                    // fall through
                case XmlCharTypes.CT_WS_LF:
                    markLF();
                    // fall through
                case XmlCharTypes.CT_WS_TAB:
                    // Plus, need to convert these all to simple space
                    c = INT_SPACE;
                    break;

                case XmlCharTypes.CT_MULTIBYTE_2:
                    c = decodeUtf8_2(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_3:
                    c = decodeUtf8_3(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_4:
                    c = decodeUtf8_4(c);
                    // Let's add first part right away:
                    attrBuffer[attrPtr++] = (char) (0xD800 | (c >> 10));
                    c = 0xDC00 | (c & 0x3FF);
                    if (attrPtr >= attrBuffer.length) {
                        attrBuffer = _attrCollector.valueBufferFull();
                    }
                    break;

                case XmlCharTypes.CT_MULTIBYTE_N:
                    reportInvalidInitial(c);
                case XmlCharTypes.CT_LT:
                    throwUnexpectedChar(c, "'<' not allowed in attribute value");
                case XmlCharTypes.CT_AMP:
                    c = handleEntityInText();
                    if (c == 0) { // unexpanded general entity... not good
                        reportUnexpandedEntityInAttr(false);
                    }
                    // Ok; does it need a surrogate though? (over 16 bits)
                    if ((c >> 16) != 0) {
                        c -= 0x10000;
                        attrBuffer[attrPtr++] = (char) (0xD800 | (c >> 10));
                        c = 0xDC00 | (c & 0x3FF);
                        if (attrPtr >= attrBuffer.length) {
                            attrBuffer = _attrCollector.valueBufferFull();
                        }
                    }
                    break;

                case XmlCharTypes.CT_ATTR_QUOTE:
                    if (c == (int) quoteByte) {
                        break value_loop;
                    }

                    // default:
                    // Other chars are not important here...
            }
            // We know there's room for at least one char without checking
            attrBuffer[attrPtr++] = (char) c;
        }

        return attrPtr;
    }

    /**
     * Method called from the main START_ELEMENT handling loop, to
     * parse namespace URI values.
     */
    private void handleNsDeclaration(PName name, byte quoteByte) throws XMLStreamException {
        int attrPtr = 0;
        char[] attrBuffer = _nameBuffer;

        while (true) {
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            byte b = _inputBuffer[_inputPtr++];
            if (b == quoteByte) {
                break;
            }
            int c;
            if (b == BYTE_AMP) { // entity
                c = handleEntityInText();
                if (c == 0) { // general entity; should never happen
                    reportUnexpandedEntityInAttr(true);
                }
                // Ok; does it need a surrogate though? (over 16 bits)
                if ((c >> 16) != 0) {
                    if (attrPtr >= attrBuffer.length) {
                        _nameBuffer = attrBuffer = DataUtil.growArrayBy(attrBuffer, attrBuffer.length);
                    }
                    c -= 0x10000;
                    attrBuffer[attrPtr++] = (char) (0xD800 | (c >> 10));
                    c = 0xDC00 | (c & 0x3FF);
                }
            } else if (b == BYTE_LT) { // error
                c = b;
                throwUnexpectedChar(c, "'<' not allowed in attribute value");
            } else {
                c = (int) b & 0xFF;
                if (c < INT_SPACE) {
                    if (c == INT_LF) {
                        markLF();
                    } else if (c == INT_CR) {
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        if (_inputBuffer[_inputPtr] == BYTE_LF) {
                            ++_inputPtr;
                        }
                        markLF();
                    } else if (c != INT_TAB) {
                        throwInvalidSpace(c);
                    }
                } else if (c > 0x7F) {
                    c = decodeMultiByteChar(c, _inputPtr);
                    if (c < 0) { // surrogate pair
                        c = -c;
                        // Let's add first part right away:
                        if (attrPtr >= attrBuffer.length) {
                            _nameBuffer = attrBuffer = DataUtil.growArrayBy(attrBuffer, attrBuffer.length);
                        }
                        c -= 0x10000;
                        attrBuffer[attrPtr++] = (char) (0xD800 | (c >> 10));
                        c = 0xDC00 | (c & 0x3FF);
                    }
                }
            }
            if (attrPtr >= attrBuffer.length) {
                _nameBuffer = attrBuffer = DataUtil.growArrayBy(attrBuffer, attrBuffer.length);
            }
            attrBuffer[attrPtr++] = (char) c;
        }

        /* Simple optimization: for default ns removal (or, with
         * ns 1.1, any other as well), will use empty value... no
         * need to try to intern:
         */
        if (attrPtr == 0) {
            bindNs(name, "");
        } else {
            String uri = _config.canonicalizeURI(attrBuffer, attrPtr);
            bindNs(name, uri);
        }
    }

    /**
     * Note that this method is currently also shareable for all Ascii-based
     * encodings, and at least between UTF-8 and ISO-Latin1. The reason is
     * that since we already know exact bytes that need to be matched,
     * there's no danger of getting invalid encodings or such.
     * So, for now, let's leave this method here in the base class.
     */
    private int handleEndElement() throws XMLStreamException {
        --_depth;
        _currToken = END_ELEMENT;
        // Ok, at this point we have seen '/', need the name
        _tokenName = _currElem.getName();

        int size = _tokenName.sizeInQuads();
        /* Do we need to take the slow route? Let's separate that out
         * to another method.
         * Note: we'll require max bytes for name PLUS one (for trailing
         * '>', most likely).
         */
        if ((_inputEnd - _inputPtr) < ((size << 2) + 1)) { // may need to load more
            return handleEndElementSlow(size);
        }

        int ptr = _inputPtr;
        byte[] buf = _inputBuffer;

        // First all full chunks of 4 bytes (if any)
        --size;
        for (int qix = 0; qix < size; ++qix) {
            int q = (buf[ptr] << 24) | ((buf[ptr + 1] & 0xFF) << 16) | ((buf[ptr + 2] & 0xFF) << 8)
                | ((buf[ptr + 3] & 0xFF));
            ptr += 4;
            // match?
            if (q != _tokenName.getQuad(qix)) {
                _inputPtr = ptr;
                reportUnexpectedEndTag(_tokenName.getPrefixedName());
            }
        }

        /* After which we can deal with the last entry: it's bit
         * tricky as we don't actually fully know byte length...
         */
        int lastQ = _tokenName.getQuad(size);
        int q = buf[ptr++] & 0xFF;
        if (q != lastQ) { // need second byte?
            q = (q << 8) | (buf[ptr++] & 0xFF);
            if (q != lastQ) { // need third byte?
                q = (q << 8) | (buf[ptr++] & 0xFF);
                if (q != lastQ) { // need full 4 bytes?
                    q = (q << 8) | (buf[ptr++] & 0xFF);
                    if (q != lastQ) { // still no match? failure!
                        _inputPtr = ptr;
                        reportUnexpectedEndTag(_tokenName.getPrefixedName());
                    }
                }
            }
        }
        // Trailing space?
        int i2 = _inputBuffer[ptr] & 0xFF;
        _inputPtr = ptr + 1;
        while (i2 <= INT_SPACE) {
            if (i2 == INT_LF) {
                markLF();
            } else if (i2 == INT_CR) {
                byte b = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne();
                if (b != BYTE_LF) {
                    markLF(_inputPtr - 1);
                    i2 = (int) b & 0xFF;
                    continue;
                }
                markLF();
            } else if (i2 != INT_SPACE && i2 != INT_TAB) {
                throwInvalidSpace(i2);
            }
            i2 = (int) ((_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne()) & 0xFF;
        }
        if (i2 != INT_GT) {
            throwUnexpectedChar(decodeCharForError((byte) i2), " expected space or closing '>'");
        }
        return END_ELEMENT;
    }

    private int handleEndElementSlow(int size) throws XMLStreamException {
        /* Nope, will likely cross the input boundary; need
         * to do proper checks
         */
        --size;
        for (int qix = 0; qix < size; ++qix) { // first, full chunks
            int q = 0;
            for (int i = 0; i < 4; ++i) {
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                q = (q << 8) | (_inputBuffer[_inputPtr++] & 0xFF);
            }
            // match?
            if (q != _tokenName.getQuad(qix)) {
                reportUnexpectedEndTag(_tokenName.getPrefixedName());
            }
        }

        // And then the last 1-4 bytes:
        int lastQ = _tokenName.getQuad(size);
        int q = 0;
        int i = 0;

        while (true) {
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            q = (q << 8) | (_inputBuffer[_inputPtr++] & 0xFF);
            if (q == lastQ) { // match
                break;
            }
            if (++i > 3) { // no match, error
                reportUnexpectedEndTag(_tokenName.getPrefixedName());
                break; // never gets here
            }
        }

        // Trailing space?
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        int i2 = _inputBuffer[_inputPtr++];
        while (i2 <= INT_SPACE) {
            if (i2 == INT_LF) {
                markLF();
            } else if (i2 == INT_CR) {
                byte b = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne();
                if (b != BYTE_LF) {
                    markLF(_inputPtr - 1);
                    i2 = (int) b & 0xFF;
                    continue;
                }
                markLF();
            } else if (i2 != INT_SPACE && i2 != INT_TAB) {
                throwInvalidSpace(i2);
            }
            i2 = (int) ((_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne()) & 0xFF;
        }
        if (i2 != INT_GT) {
            throwUnexpectedChar(decodeCharForError((byte) i2), " expected space or closing '>'");
        }
        return END_ELEMENT;
    }

    /*
    /**********************************************************************
    /* Common name/entity parsing
    /**********************************************************************
     */

    /**
     * This method can (for now?) be shared between all Ascii-based
     * encodings, since it only does coarse validity checking -- real
     * checks are done in different method.
     *<p>
     * Some notes about assumption implementation makes:
     *<ul>
     * <li>Well-formed xml content can not end with a name: as such,
     *    end-of-input is an error and we can throw an exception
     *  </li>
     * </ul>
     */
    private PName parsePName(byte b) throws XMLStreamException {
        // First: can we optimize out bounds checks?
        if ((_inputEnd - _inputPtr) < 8) { // got 1 byte, but need 7, plus one trailing
            return parsePNameSlow(b);
        }
        // If so, can also unroll loops nicely

        int q = b & 0xFF;
        // Let's do just quick sanity check first; a thorough check will be
        // done later on if necessary, now we'll just do the very cheap
        // check to catch extra spaces etc.
        if (q < INT_A) { // lowest acceptable start char, except for ':' that would be allowed in non-ns mode
            throwUnexpectedChar(q, "; expected a name start character");
        }

        int i2 = _inputBuffer[_inputPtr++] & 0xFF;
        // For other bytes beyond first we have to do bit more complicated
        // check, to reliably find out where name ends. Still can do quite
        // simple checks though
        if (i2 < 65) {
            // Ok; "_" (45), "." (46) and "0"-"9"/":" (48 - 57/58) still name chars
            if (i2 < 45 || i2 > 58 || i2 == 47) {
                return findPName(q, 1);
            }
        }
        q = (q << 8) | i2;
        i2 = (int) _inputBuffer[_inputPtr++] & 0xFF;
        if (i2 < 65) {
            if (i2 < 45 || i2 > 58 || i2 == 47) { // 2 (ascii) char name?
                return findPName(q, 2);
            }
        }
        q = (q << 8) | i2;
        i2 = (int) _inputBuffer[_inputPtr++] & 0xFF;
        if (i2 < 65) {
            if (i2 < 45 || i2 > 58 || i2 == 47) { // 3 (ascii) char name?
                return findPName(q, 3);
            }
        }
        q = (q << 8) | i2;
        i2 = (int) _inputBuffer[_inputPtr++] & 0xFF;
        if (i2 < 65) {
            if (i2 < 45 || i2 > 58 || i2 == 47) { // 4 (ascii) char name?
                return findPName(q, 4);
            }
        }

        // Longer, let's offline:
        return parsePNameMedium(i2, q);
    }

    private PName parsePNameMedium(int i2, int q1) throws XMLStreamException {
        // Ok, so far so good; one quad, one byte. Then the second
        int q2 = i2;
        i2 = _inputBuffer[_inputPtr++] & 0xFF;
        if (i2 < 65) {
            // Ok; "_" (45), "." (46) and "0"-"9"/":" (48 - 57/58) still name chars
            if (i2 < 45 || i2 > 58 || i2 == 47) {
                return findPName(q1, q2, 1);
            }
        }

        q2 = (q2 << 8) | i2;
        i2 = (int) _inputBuffer[_inputPtr++] & 0xFF;
        if (i2 < 65) {
            if (i2 < 45 || i2 > 58 || i2 == 47) { // 2 (ascii) char name?
                return findPName(q1, q2, 2);
            }
        }
        q2 = (q2 << 8) | i2;
        i2 = (int) _inputBuffer[_inputPtr++] & 0xFF;
        if (i2 < 65) {
            if (i2 < 45 || i2 > 58 || i2 == 47) { // 3 (ascii) char name?
                return findPName(q1, q2, 3);
            }
        }
        q2 = (q2 << 8) | i2;
        i2 = (int) _inputBuffer[_inputPtr++] & 0xFF;
        if (i2 < 65) {
            if (i2 < 45 || i2 > 58 || i2 == 47) { // 4 (ascii) char name?
                return findPName(q1, q2, 4);
            }
        }

        // Ok, no, longer loop. Let's offline
        int[] quads = _quadBuffer;
        quads[0] = q1;
        quads[1] = q2;
        return parsePNameLong(i2, quads);
    }

    private PName parsePNameLong(int q, int[] quads) throws XMLStreamException {
        int qix = 2;
        while (true) {
            // Second byte of a new quad
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            int i2 = _inputBuffer[_inputPtr++] & 0xFF;
            if (i2 < 65) {
                if (i2 < 45 || i2 > 58 || i2 == 47) {
                    // End of name, a single ascii char?
                    return findPName(q, quads, qix, 1);
                }
            }
            // 3rd byte:
            q = (q << 8) | i2;
            i2 = (int) ((_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne()) & 0xFF;
            if (i2 < 65) {
                if (i2 < 45 || i2 > 58 || i2 == 47) { // 2 (ascii) char name?
                    return findPName(q, quads, qix, 2);
                }
            }
            // 4th byte:
            q = (q << 8) | i2;
            i2 = (int) ((_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne()) & 0xFF;
            if (i2 < 65) {
                if (i2 < 45 || i2 > 58 || i2 == 47) { // 2 (ascii) char name?
                    return findPName(q, quads, qix, 3);
                }
            }
            q = (q << 8) | i2;
            i2 = (int) ((_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne()) & 0xFF;
            if (i2 < 65) {
                if (i2 < 45 || i2 > 58 || i2 == 47) { // 2 (ascii) char name?
                    return findPName(q, quads, qix, 4);
                }
            }
            if (qix >= quads.length) { // let's just double?
                _quadBuffer = quads = DataUtil.growArrayBy(quads, quads.length);
            }
            quads[qix] = q;
            ++qix;
            q = i2;
        }
    }

    private PName parsePNameSlow(byte b) throws XMLStreamException {
        int q = b & 0xFF;

        // Let's do just quick sanity check first; a thorough check will be
        // done later on if necessary, now we'll just do the very cheap
        // check to catch extra spaces etc.
        if (q < INT_A) { // lowest acceptable start char, except for ':' that would be allowed in non-ns mode
            throwUnexpectedChar(q, "; expected a name start character");
        }

        int[] quads = _quadBuffer;
        int qix = 0;
        // Let's optimize a bit for shorter PNames...
        int firstQuad = 0;

        while (true) {
            // Second byte
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            int i2 = _inputBuffer[_inputPtr++] & 0xFF;
            // For other bytes beyond first we have to do bit more complicated
            // check, to reliably find out where name ends. Still can do quite
            // simple checks though
            if (i2 < 65) {
                // Ok; "_" (45), "." (46) and "0"-"9"/":" (48 - 57/58) still name chars
                if (i2 < 45 || i2 > 58 || i2 == 47) {
                    // End of name, a single ascii char?
                    return findPName(q, 1, firstQuad, qix, quads);
                }
            }

            // 3rd byte:
            q = (q << 8) | i2;
            i2 = (int) ((_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne()) & 0xFF;
            if (i2 < 65) {
                if (i2 < 45 || i2 > 58 || i2 == 47) { // 2 (ascii) char name?
                    return findPName(q, 2, firstQuad, qix, quads);
                }
            }

            // 4th byte:
            q = (q << 8) | i2;
            i2 = (int) ((_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne()) & 0xFF;
            if (i2 < 65) {
                if (i2 < 45 || i2 > 58 || i2 == 47) { // 3 (ascii) char name?
                    return findPName(q, 3, firstQuad, qix, quads);
                }
            }
            q = (q << 8) | i2;

            // Ok; one more full quad gotten... but just to squeeze bit
            // more mileage out of it, was this the end?
            i2 = (int) ((_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne()) & 0xFF;
            if (i2 < 65) {
                if (i2 < 45 || i2 > 58 || i2 == 47) { // 4 (ascii) char name?
                    return findPName(q, 4, firstQuad, qix, quads);
                }
            }

            // Nope; didn't end. May need to store the quad in temporary
            // buffer and continue
            if (qix == 0) { // not yet, was the first quad
                firstQuad = q;
            } else if (qix == 1) { // second quad, need to init buffer
                quads[0] = firstQuad;
                quads[1] = q;
            } else { // 3rd or after... need to make sure there's room
                if (qix >= quads.length) { // let's just double?
                    _quadBuffer = quads = DataUtil.growArrayBy(quads, quads.length);
                }
                quads[qix] = q;
            }
            ++qix;
            q = i2;
        }
    }

    /**
     * Method called to process a sequence of bytes that is likely to
     * be a PName. At this point we encountered an end marker, and
     * may either hit a formerly seen well-formed PName; an as-of-yet
     * unseen well-formed PName; or a non-well-formed sequence (containing
     * one or more non-name chars without any valid end markers).
     *
     * @param onlyQuad Word with 1 to 4 bytes that make up PName
     * @param lastByteCount Number of actual bytes contained in onlyQuad; 0 to 3.
     */
    private PName findPName(int onlyQuad, int lastByteCount) throws XMLStreamException {
        // First, need to push back the byte read but not used:
        --_inputPtr;
        int hash = ByteBasedPNameTable.calcHash(onlyQuad);
        PName name = _symbols.findSymbol(hash, onlyQuad, 0);
        if (name == null) {
            // Let's simplify things a bit, and just use array based one then:
            _quadBuffer[0] = onlyQuad;
            name = addPName(hash, _quadBuffer, 1, lastByteCount);
        }
        return name;
    }

    /**
     * Method called to process a sequence of bytes that is likely to
     * be a PName. At this point we encountered an end marker, and
     * may either hit a formerly seen well-formed PName; an as-of-yet
     * unseen well-formed PName; or a non-well-formed sequence (containing
     * one or more non-name chars without any valid end markers).
     *
     * @param firstQuad First 1 to 4 bytes of the PName
     * @param secondQuad Word with last 1 to 4 bytes of the PName
     * @param lastByteCount Number of bytes contained in secondQuad; 0 to 3.
     */
    private PName findPName(int firstQuad, int secondQuad, int lastByteCount) throws XMLStreamException {
        // First, need to push back the byte read but not used:
        --_inputPtr;
        int hash = ByteBasedPNameTable.calcHash(firstQuad, secondQuad);
        PName name = _symbols.findSymbol(hash, firstQuad, secondQuad);
        if (name == null) {
            // Let's just use array, then
            _quadBuffer[0] = firstQuad;
            _quadBuffer[1] = secondQuad;
            name = addPName(hash, _quadBuffer, 2, lastByteCount);
        }
        return name;
    }

    /**
     * Method called to process a sequence of bytes that is likely to
     * be a PName. At this point we encountered an end marker, and
     * may either hit a formerly seen well-formed PName; an as-of-yet
     * unseen well-formed PName; or a non-well-formed sequence (containing
     * one or more non-name chars without any valid end markers).
     *
     * @param lastQuad Word with last 0 to 3 bytes of the PName; not included
     *   in the quad array
     * @param quads Array that contains all the quads, except for the
     *    last one, for names with more than 8 bytes (i.e. more than
     *    2 quads)
     * @param qlen Number of quads in the array, except if less than 2
     *    (in which case only firstQuad and lastQuad are used)
     * @param lastByteCount Number of bytes contained in lastQuad; 0 to 3.
     */
    private PName findPName(int lastQuad, int[] quads, int qlen, int lastByteCount) throws XMLStreamException {
        // First, need to push back the byte read but not used:
        --_inputPtr;
        /* Nope, long (3 quads or more). At this point, the last quad is
         * not yet in the array, let's add:
         */
        if (qlen >= quads.length) { // let's just double?
            _quadBuffer = quads = DataUtil.growArrayBy(quads, quads.length);
        }
        quads[qlen++] = lastQuad;
        int hash = ByteBasedPNameTable.calcHash(quads, qlen);
        PName name = _symbols.findSymbol(hash, quads, qlen);
        if (name == null) {
            name = addPName(hash, quads, qlen, lastByteCount);
        }
        return name;
    }

    /**
     * Method called to process a sequence of bytes that is likely to
     * be a PName. At this point we encountered an end marker, and
     * may either hit a formerly seen well-formed PName; an as-of-yet
     * unseen well-formed PName; or a non-well-formed sequence (containing
     * one or more non-name chars without any valid end markers).
     *
     * @param lastQuad Word with last 0 to 3 bytes of the PName; not included
     *   in the quad array
     * @param lastByteCount Number of bytes contained in lastQuad; 0 to 3.
     * @param firstQuad First 1 to 4 bytes of the PName (4 if length
     *    at least 4 bytes; less only if not).
     * @param qlen Number of quads in the array, except if less than 2
     *    (in which case only firstQuad and lastQuad are used)
     * @param quads Array that contains all the quads, except for the
     *    last one, for names with more than 8 bytes (i.e. more than
     *    2 quads)
     */
    private PName findPName(int lastQuad, int lastByteCount, int firstQuad, int qlen, int[] quads)
        throws XMLStreamException {
        // Separate handling for short names:
        if (qlen <= 1) {
            if (qlen == 0) { // 4-bytes or less; only has 'lastQuad' defined
                return findPName(lastQuad, lastByteCount);
            }
            return findPName(firstQuad, lastQuad, lastByteCount);
        }
        return findPName(lastQuad, quads, qlen, lastByteCount);
    }

    private PName addPName(int hash, int[] quads, int qlen, int lastQuadBytes) throws XMLStreamException {
        return addUTFPName(_symbols, _charTypes, hash, quads, qlen, lastQuadBytes);
    }

    /*
    /**********************************************************************
    /* Other parsing helper methods
    /**********************************************************************
     */

    /**
     * @return First byte following skipped white space
     */
    private byte skipInternalWs(boolean reqd, String msg) throws XMLStreamException {
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        byte b = _inputBuffer[_inputPtr++];
        int c = b & 0xFF;
        if (c > INT_SPACE) {
            if (!reqd) {
                return b;
            }
            reportTreeUnexpChar(decodeCharForError(b), " (expected white space " + msg + ")");
        }
        do {
            // But let's first handle the space we already got:
            if (b == BYTE_LF) {
                markLF();
            } else if (b == BYTE_CR) {
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                if (_inputBuffer[_inputPtr] == BYTE_LF) {
                    ++_inputPtr;
                }
                markLF();
            } else if (b != BYTE_SPACE && b != BYTE_TAB) {
                throwInvalidSpace(b);
            }

            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            b = _inputBuffer[_inputPtr++];
        } while ((b & 0xFF) <= INT_SPACE);

        return b;
    }

    private void matchAsciiKeyword(String keyw) throws XMLStreamException {
        for (int i = 1, len = keyw.length(); i < len; ++i) {
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            byte b = _inputBuffer[_inputPtr++];
            if (b != (byte) keyw.charAt(i)) {
                reportTreeUnexpChar(decodeCharForError(b),
                    " (expected '" + keyw.charAt(i) + "' for " + keyw + " keyword)");
            }
        }
    }

    /**
     *<p>
     * Note: consequtive white space is only considered indentation,
     * if the following token seems like a tag (start/end). This so
     * that if a CDATA section follows, it can be coalesced in
     * coalescing mode. Although we could check if coalescing mode is
     * enabled, this should seldom have significant effect either way,
     * so it removes one possible source of problems in coalescing mode.
     *
     * @return -1, if indentation was handled; offset in the output
     *    buffer, if not
     */
    private int checkInTreeIndentation(int c) throws XMLStreamException {
        if (c == INT_CR) {
            // First a degenerate case, a lone \r:
            if (_inputPtr >= _inputEnd && !loadMore()) {
                _textBuilder.resetWithIndentation(0, XmlConsts.CHAR_SPACE);
                return -1;
            }
            if (_inputBuffer[_inputPtr] == BYTE_LF) {
                ++_inputPtr;
            }
        }
        markLF();
        // Then need an indentation char (or start/end tag):
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        byte b = _inputBuffer[_inputPtr];
        if (b != BYTE_SPACE && b != BYTE_TAB) {
            // May still be indentation, if it's lt + non-exclamation mark
            if (b == BYTE_LT) {
                if ((_inputPtr + 1) < _inputEnd && _inputBuffer[_inputPtr + 1] != BYTE_EXCL) {
                    _textBuilder.resetWithIndentation(0, XmlConsts.CHAR_SPACE);
                    return -1;
                }
            }
            char[] outBuf = _textBuilder.resetWithEmpty();
            outBuf[0] = XmlConsts.CHAR_LF;
            _textBuilder.setCurrentLength(1);
            return 1;
        }
        // So how many do we get?
        ++_inputPtr;
        int count = 1;
        int max = (b == BYTE_SPACE) ? TextBuilder.MAX_INDENT_SPACES : TextBuilder.MAX_INDENT_TABS;
        while (count <= max) {
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            byte b2 = _inputBuffer[_inputPtr];
            if (b2 != b) {
                // Has to be followed by a start/end tag...
                if (b2 == BYTE_LT && (_inputPtr + 1) < _inputEnd && _inputBuffer[_inputPtr + 1] != BYTE_EXCL) {
                    _textBuilder.resetWithIndentation(count, (char) b);
                    return -1;
                }
                break;
            }
            ++_inputPtr;
            ++count;
        }
        // Nope, hit something else, or too long: need to just copy the stuff
        // we know buffer has enough room either way
        char[] outBuf = _textBuilder.resetWithEmpty();
        outBuf[0] = XmlConsts.CHAR_LF;
        char ind = (char) b;
        for (int i = 1; i <= count; ++i) {
            outBuf[i] = ind;
        }
        count += 1; // to account for leading lf
        _textBuilder.setCurrentLength(count);
        return count;
    }

    /**
     * @return -1, if indentation was handled; offset in the output
     *    buffer, if not
     */
    private int checkPrologIndentation(int c) throws XMLStreamException {
        if (c == INT_CR) {
            // First a degenerate case, a lone \r:
            if (_inputPtr >= _inputEnd && !loadMore()) {
                _textBuilder.resetWithIndentation(0, XmlConsts.CHAR_SPACE);
                return -1;
            }
            if (_inputBuffer[_inputPtr] == BYTE_LF) {
                ++_inputPtr;
            }
        }
        markLF();
        // Ok, indentation char?
        if (_inputPtr >= _inputEnd && !loadMore()) {
            _textBuilder.resetWithIndentation(0, XmlConsts.CHAR_SPACE);
            return -1;
        }
        byte b = _inputBuffer[_inputPtr]; // won't advance past the char yet
        if (b != BYTE_SPACE && b != BYTE_TAB) {
            // If lt, it's still indentation ok:
            if (b == BYTE_LT) { // need
                _textBuilder.resetWithIndentation(0, XmlConsts.CHAR_SPACE);
                return -1;
            }
            // Nope... something else
            char[] outBuf = _textBuilder.resetWithEmpty();
            outBuf[0] = XmlConsts.CHAR_LF;
            _textBuilder.setCurrentLength(1);
            return 1;
        }
        // So how many do we get?
        ++_inputPtr;
        int count = 1;
        int max = (b == BYTE_SPACE) ? TextBuilder.MAX_INDENT_SPACES : TextBuilder.MAX_INDENT_TABS;
        while (true) {
            if (_inputPtr >= _inputEnd && !loadMore()) {
                break;
            }
            if (_inputBuffer[_inputPtr] != b) {
                break;
            }
            ++_inputPtr;
            ++count;
            if (count >= max) { // ok, can't share... but can build it still
                // we know buffer has enough room
                char[] outBuf = _textBuilder.resetWithEmpty();
                outBuf[0] = XmlConsts.CHAR_LF;
                char ind = (char) b;
                for (int i = 1; i <= count; ++i) {
                    outBuf[i] = ind;
                }
                count += 1; // to account for leading lf
                _textBuilder.setCurrentLength(count);
                return count;
            }
        }
        // Ok, gotcha?
        _textBuilder.resetWithIndentation(count, (char) b);
        return -1;
    }

    /*
    /**********************************************************************
    /* Methods for sub-classes, reading data
    /**********************************************************************
     */

    @Override
    protected boolean loadMore() throws XMLStreamException {
        // First, let's update offsets:
        _pastBytesOrChars += _inputEnd;
        _rowStartOffset -= _inputEnd;
        _inputPtr = 0;

        // If it's a block source, there's no input stream, or any more data:
        if (_in == null) {
            _inputEnd = 0;
            return false;
        }

        try {
            int count = _in.read(_inputBuffer, 0, _inputBuffer.length);
            if (count < 1) {
                _inputEnd = 0;
                if (count == 0) {
                    /* Sanity check; should never happen with correctly written
                     * InputStreams...
                     */
                    reportInputProblem(
                        "InputStream returned 0 bytes, even when asked to read up to " + _inputBuffer.length);
                }
                return false;
            }
            _inputEnd = count;
            return true;
        } catch (IOException ioe) {
            throw new StreamExceptionBase(ioe);
        }
    }

    /*
    /**********************************************************************
    /* Content skipping
    /**********************************************************************
     */

    @Override
    protected boolean skipCharacters() throws XMLStreamException {
        final int[] TYPES = _charTypes.TEXT_CHARS;
        final byte[] inputBuffer = _inputBuffer;

        while (true) {
            int c;

            // Then the tight ascii non-funny-char loop:
            ascii_loop: while (true) {
                int ptr = _inputPtr;
                int max = _inputEnd;
                if (ptr >= max) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                    max = _inputEnd;
                }
                while (ptr < max) {
                    c = (int) inputBuffer[ptr++] & 0xFF;
                    if (TYPES[c] != 0) {
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                }
                _inputPtr = ptr;
            }

            switch (TYPES[c]) {
                case XmlCharTypes.CT_INVALID:
                    handleInvalidXmlChar(c);
                case XmlCharTypes.CT_WS_CR:
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    if (inputBuffer[_inputPtr] == BYTE_LF) {
                        ++_inputPtr;
                    }
                    markLF();
                    break;

                case XmlCharTypes.CT_WS_LF:
                    markLF();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_2:
                    skipUtf8_2();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_3:
                    skipUtf8_3(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_4:
                    skipUtf8_4();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_N:
                    reportInvalidInitial(c);
                case XmlCharTypes.CT_LT:
                    --_inputPtr;
                    return false;

                case XmlCharTypes.CT_AMP:
                    c = handleEntityInText();
                    if (c == 0) { // unexpandable general parsed entity
                        return true;
                    }
                    break;

                case XmlCharTypes.CT_RBRACKET: // ']]>'?
                {
                    // Let's then just count number of brackets --
                    // in case they are not followed by '>'
                    int count = 1;
                    byte b;
                    while (true) {
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        b = inputBuffer[_inputPtr];
                        if (b != BYTE_RBRACKET) {
                            break;
                        }
                        ++_inputPtr; // to skip past bracket
                        ++count;
                    }
                    if (b == BYTE_GT && count > 1) {
                        reportIllegalCDataEnd();
                    }
                }
                    break;

                // default:
                // Other types are not important here...
            }
        }
    }

    @Override
    protected void skipComment() throws XMLStreamException {
        final int[] TYPES = _charTypes.OTHER_CHARS;
        final byte[] inputBuffer = _inputBuffer;

        while (true) {
            int c;

            // Then the tight ascii non-funny-char loop:
            ascii_loop: while (true) {
                int ptr = _inputPtr;
                int max = _inputEnd;
                if (ptr >= max) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                    max = _inputEnd;
                }
                while (ptr < max) {
                    c = (int) inputBuffer[ptr++] & 0xFF;
                    if (TYPES[c] != 0) {
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                }
                _inputPtr = ptr;
            }

            switch (TYPES[c]) {
                case XmlCharTypes.CT_INVALID:
                    handleInvalidXmlChar(c);
                case XmlCharTypes.CT_WS_CR:
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    if (inputBuffer[_inputPtr] == BYTE_LF) {
                        ++_inputPtr;
                    }
                    markLF();
                    break;

                case XmlCharTypes.CT_WS_LF:
                    markLF();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_2:
                    skipUtf8_2();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_3:
                    skipUtf8_3(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_4:
                    skipUtf8_4();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_N:
                    reportInvalidInitial(c);
                case XmlCharTypes.CT_HYPHEN: // '-->'?
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    if (_inputBuffer[_inputPtr] == BYTE_HYPHEN) { // ok, must be end then
                        ++_inputPtr;
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        if (_inputBuffer[_inputPtr++] != BYTE_GT) {
                            reportDoubleHyphenInComments();
                        }
                        return;
                    }
                    break;

                // default:
                // Other types are not important here...
            }
        }
    }

    @Override
    protected void skipCData() throws XMLStreamException {
        final int[] TYPES = _charTypes.OTHER_CHARS;
        final byte[] inputBuffer = _inputBuffer;

        while (true) {
            int c;

            // Then the tight ascii non-funny-char loop:
            ascii_loop: while (true) {
                int ptr = _inputPtr;
                int max = _inputEnd;
                if (ptr >= max) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                    max = _inputEnd;
                }
                while (ptr < max) {
                    c = (int) inputBuffer[ptr++] & 0xFF;
                    if (TYPES[c] != 0) {
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                }
                _inputPtr = ptr;
            }

            switch (TYPES[c]) {
                case XmlCharTypes.CT_INVALID:
                    handleInvalidXmlChar(c);
                case XmlCharTypes.CT_WS_CR:
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    if (inputBuffer[_inputPtr] == BYTE_LF) {
                        ++_inputPtr;
                    }
                    markLF();
                    break;

                case XmlCharTypes.CT_WS_LF:
                    markLF();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_2:
                    skipUtf8_2();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_3:
                    skipUtf8_3(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_4:
                    skipUtf8_4();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_N:
                    reportInvalidInitial(c);
                case XmlCharTypes.CT_RBRACKET: // ']]>'?
                {
                    // end is nigh?
                    int count = 0;
                    byte b;

                    do {
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        ++count;
                        b = _inputBuffer[_inputPtr++];
                    } while (b == BYTE_RBRACKET);

                    if (b == BYTE_GT) {
                        if (count > 1) { // gotcha
                            return;
                        }
                        // can still skip plain ']>'...
                    } else {
                        --_inputPtr; // need to push back last char
                    }
                }
                    break;

                // default:
                // Other types are not important here...
            }
        }
    }

    @Override
    protected void skipPI() throws XMLStreamException {
        final int[] TYPES = _charTypes.OTHER_CHARS;
        final byte[] inputBuffer = _inputBuffer;

        while (true) {
            int c;

            // Then the tight ascii non-funny-char loop:
            ascii_loop: while (true) {
                int ptr = _inputPtr;
                int max = _inputEnd;
                if (ptr >= max) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                    max = _inputEnd;
                }
                while (ptr < max) {
                    c = (int) inputBuffer[ptr++] & 0xFF;
                    if (TYPES[c] != 0) {
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                }
                _inputPtr = ptr;
            }

            switch (TYPES[c]) {
                case XmlCharTypes.CT_INVALID:
                    handleInvalidXmlChar(c);
                case XmlCharTypes.CT_WS_CR:
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    if (inputBuffer[_inputPtr] == BYTE_LF) {
                        ++_inputPtr;
                    }
                    markLF();
                    break;

                case XmlCharTypes.CT_WS_LF:
                    markLF();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_2:
                    skipUtf8_2();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_3:
                    skipUtf8_3(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_4:
                    skipUtf8_4();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_N:
                    reportInvalidInitial(c);
                case XmlCharTypes.CT_QMARK: // '?>'?
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    if (_inputBuffer[_inputPtr] == BYTE_GT) {
                        ++_inputPtr;
                        return;
                    }
                    break;

                // default:
                // Other types are not important here...
            }
        }
    }

    @Override
    protected void skipSpace() throws XMLStreamException {
        // mTmpChar has a space, but it's been checked, can ignore
        int ptr = _inputPtr;

        while (true) {
            if (ptr >= _inputEnd) {
                if (!loadMore()) {
                    break;
                }
                ptr = _inputPtr;
            }
            int c = (int) _inputBuffer[ptr] & 0xFF;
            if (c > INT_SPACE) { // !!! TODO: xml 1.1 ws
                break;
            }
            ++ptr;

            if (c == INT_LF) {
                markLF(ptr);
            } else if (c == INT_CR) {
                if (ptr >= _inputEnd) {
                    if (!loadMore()) {
                        break;
                    }
                    ptr = _inputPtr;
                }
                if (_inputBuffer[ptr] == BYTE_LF) {
                    ++ptr;
                }
                markLF(ptr);
            } else if (c != INT_SPACE && c != INT_TAB) {
                _inputPtr = ptr;
                throwInvalidSpace(c);
            }
        }

        _inputPtr = ptr;
    }

    private void skipUtf8_2() throws XMLStreamException {
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        int c = _inputBuffer[_inputPtr++];
        if ((c & 0xC0) != 0x080) {
            reportInvalidOther(c & 0xFF, _inputPtr);
        }
    }

    /* Alas, can't heavily optimize skipping, since we still have to
     * do validity checks...
     */
    private void skipUtf8_3(int c) throws XMLStreamException {
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        c &= 0x0F;
        if (c >= 0xD) { // have to check
            c <<= 6;
            int d = _inputBuffer[_inputPtr++];
            if ((d & 0xC0) != 0x080) {
                reportInvalidOther(d & 0xFF, _inputPtr);
            }
            c |= (d & 0x3F);
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            d = _inputBuffer[_inputPtr++];
            if ((d & 0xC0) != 0x080) {
                reportInvalidOther(d & 0xFF, _inputPtr);
            }
            c = (c << 6) | (d & 0x3F);
            // 0xD800-0xDFFF, 0xFFFE-0xFFFF illegal
            if (c >= 0xD800) { // surrogates illegal, as well as 0xFFFE/0xFFFF
                if (c < 0xE000 || c >= 0xFFFE) {
                    handleInvalidXmlChar(c);
                }
            }
        } else { // no checks, can discard
            c = _inputBuffer[_inputPtr++];
            if ((c & 0xC0) != 0x080) {
                reportInvalidOther(c & 0xFF, _inputPtr);
            }
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            c = _inputBuffer[_inputPtr++];
            if ((c & 0xC0) != 0x080) {
                reportInvalidOther(c & 0xFF, _inputPtr);
            }
        }
    }

    private void skipUtf8_4() throws XMLStreamException {
        if ((_inputPtr + 4) > _inputEnd) {
            skipUtf8_4Slow();
            return;
        }
        int d = _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            reportInvalidOther(d & 0xFF, _inputPtr);
        }
        d = _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            reportInvalidOther(d & 0xFF, _inputPtr);
        }
        d = _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            reportInvalidOther(d & 0xFF, _inputPtr);
        }
    }

    private void skipUtf8_4Slow() throws XMLStreamException {
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        int d = _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            reportInvalidOther(d & 0xFF, _inputPtr);
        }
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        d = _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            reportInvalidOther(d & 0xFF, _inputPtr);
        }
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        d = _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            reportInvalidOther(d & 0xFF, _inputPtr);
        }
    }

    /*
    /**********************************************************************
    /* Content parsing
    /**********************************************************************
     */

    @Override
    protected void finishCData() throws XMLStreamException {
        final int[] TYPES = _charTypes.OTHER_CHARS;
        final byte[] inputBuffer = _inputBuffer;
        char[] outputBuffer = _textBuilder.resetWithEmpty();
        int outPtr = 0;

        /* At this point, space (if any) has been skipped, and we are
         * to parse and store the contents
         */
        main_loop: while (true) {
            int c;
            // Then the tight ascii non-funny-char loop:
            ascii_loop: while (true) {
                int ptr = _inputPtr;
                if (ptr >= _inputEnd) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                }
                if (outPtr >= outputBuffer.length) {
                    outputBuffer = _textBuilder.finishCurrentSegment();
                    outPtr = 0;
                }
                int max = _inputEnd;
                {
                    int max2 = ptr + (outputBuffer.length - outPtr);
                    if (max2 < max) {
                        max = max2;
                    }
                }
                while (ptr < max) {
                    c = (int) inputBuffer[ptr++] & 0xFF;
                    if (TYPES[c] != 0) {
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                    outputBuffer[outPtr++] = (char) c;
                }
                _inputPtr = ptr;
            }
            // And then exceptions:
            switch (TYPES[c]) {
                case XmlCharTypes.CT_INVALID:
                    handleInvalidXmlChar(c);
                case XmlCharTypes.CT_WS_CR:
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    if (inputBuffer[_inputPtr] == BYTE_LF) {
                        ++_inputPtr;
                    }
                    markLF();
                    c = INT_LF;
                    break;

                case XmlCharTypes.CT_WS_LF:
                    markLF();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_2:
                    c = decodeUtf8_2(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_3:
                    c = decodeUtf8_3(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_4:
                    c = decodeUtf8_4(c);
                    // Let's add first part right away:
                    outputBuffer[outPtr++] = (char) (0xD800 | (c >> 10));
                    if (outPtr >= outputBuffer.length) {
                        outputBuffer = _textBuilder.finishCurrentSegment();
                        outPtr = 0;
                    }
                    c = 0xDC00 | (c & 0x3FF);
                    // And let the other char output down below
                    break;

                case XmlCharTypes.CT_MULTIBYTE_N:
                    reportInvalidInitial(c);
                case XmlCharTypes.CT_RBRACKET: // close ']]>' marker?
                    /* Ok: let's just parse all consequtive right brackets,
                     * and see if followed by greater-than char. This because
                     * we can only push back at most one char at a time, and
                     * thus can't easily just check a subset
                     */
                    int count = 0; // ignoring first one
                    byte b;

                    do {
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        b = _inputBuffer[_inputPtr];
                        if (b != BYTE_RBRACKET) {
                            break;
                        }
                        ++_inputPtr;
                        ++count;
                    } while (true);

                    // Was the marker found?
                    boolean ok = (b == BYTE_GT && count >= 1);
                    if (ok) {
                        --count;
                    }
                    // Brackets to copy to output?
                    for (; count > 0; --count) {
                        outputBuffer[outPtr++] = ']';
                        if (outPtr >= outputBuffer.length) {
                            outputBuffer = _textBuilder.finishCurrentSegment();
                            outPtr = 0;
                        }
                    }
                    if (ok) {
                        ++_inputPtr; // to consume '>'
                        break main_loop;
                    }
                    break;
            }
            // Ok, can output the char; there's room for one char at least
            outputBuffer[outPtr++] = (char) c;
        }

        _textBuilder.setCurrentLength(outPtr);
        /* 03-Feb-2009, tatu: To support coalescing mode, may need to
         *   do some extra work
         */
        if (_cfgCoalescing && !_entityPending) {
            finishCoalescedText();
        }
    }

    @Override
    protected void finishCharacters() throws XMLStreamException {
        int outPtr;
        int c;
        char[] outputBuffer;

        // Ok, so what was the first char / entity?
        c = _tmpChar;
        if (c < 0) { // from entity; can just copy as is
            c = -c;
            outputBuffer = _textBuilder.resetWithEmpty();
            outPtr = 0;
            if ((c >> 16) != 0) { // surrogate pair?
                c -= 0x10000;
                /* Note: after resetting the buffer, it's known to have
                 * space for more than 2 chars we need to add
                 */
                outputBuffer[outPtr++] = (char) (0xD800 | (c >> 10));
                c = 0xDC00 | (c & 0x3FF);
            }
            outputBuffer[outPtr++] = (char) c;
        } else { // white space that we are interested in?
            if (c == INT_CR || c == INT_LF) {
                ++_inputPtr; // wasn't advanced yet, in this case
                outPtr = checkInTreeIndentation(c);
                if (outPtr < 0) {
                    return;
                }
                // Above call also initializes the text builder appropriately
                outputBuffer = _textBuilder.getBufferWithoutReset();
            } else {
                outputBuffer = _textBuilder.resetWithEmpty();
                outPtr = 0;
            }
        }

        final int[] TYPES = _charTypes.TEXT_CHARS;
        final byte[] inputBuffer = _inputBuffer;

        main_loop: while (true) {
            // Then the tight ascii non-funny-char loop:
            ascii_loop: while (true) {
                int ptr = _inputPtr;
                if (ptr >= _inputEnd) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                }
                if (outPtr >= outputBuffer.length) {
                    outputBuffer = _textBuilder.finishCurrentSegment();
                    outPtr = 0;
                }
                int max = _inputEnd;
                {
                    int max2 = ptr + (outputBuffer.length - outPtr);
                    if (max2 < max) {
                        max = max2;
                    }
                }
                while (ptr < max) {
                    c = (int) inputBuffer[ptr++] & 0xFF;
                    if (TYPES[c] != 0) {
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                    outputBuffer[outPtr++] = (char) c;
                }
                _inputPtr = ptr;
            }
            // And then fallback for funny chars / UTF-8 multibytes:
            switch (TYPES[c]) {
                case XmlCharTypes.CT_INVALID:
                    handleInvalidXmlChar(c);
                case XmlCharTypes.CT_WS_CR:
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    if (inputBuffer[_inputPtr] == BYTE_LF) {
                        ++_inputPtr;
                    }
                    markLF();
                    c = INT_LF;
                    break;

                case XmlCharTypes.CT_WS_LF:
                    markLF();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_2:
                    c = decodeUtf8_2(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_3:
                    if ((_inputEnd - _inputPtr) >= 2) {
                        c = decodeUtf8_3fast(c);
                    } else {
                        c = decodeUtf8_3(c);
                    }
                    break;

                case XmlCharTypes.CT_MULTIBYTE_4:
                    c = decodeUtf8_4(c);
                    // Let's add first part right away:
                    outputBuffer[outPtr++] = (char) (0xD800 | (c >> 10));
                    if (outPtr >= outputBuffer.length) {
                        outputBuffer = _textBuilder.finishCurrentSegment();
                        outPtr = 0;
                    }
                    c = 0xDC00 | (c & 0x3FF);
                    // And let the other char output down below
                    break;

                case XmlCharTypes.CT_MULTIBYTE_N:
                    reportInvalidInitial(c);
                case XmlCharTypes.CT_LT:
                    --_inputPtr;
                    break main_loop;

                case XmlCharTypes.CT_AMP:
                    c = handleEntityInText();
                    if (c == 0) { // unexpandable general parsed entity
                        // _inputPtr set by entity expansion method
                        _entityPending = true;
                        break main_loop;
                    }
                    // Ok; does it need a surrogate though? (over 16 bits)
                    if ((c >> 16) != 0) {
                        c -= 0x10000;
                        outputBuffer[outPtr++] = (char) (0xD800 | (c >> 10));
                        // Need to ensure room for one more char
                        if (outPtr >= outputBuffer.length) {
                            outputBuffer = _textBuilder.finishCurrentSegment();
                            outPtr = 0;
                        }
                        c = 0xDC00 | (c & 0x3FF);
                    }
                    break;

                case XmlCharTypes.CT_RBRACKET: // ']]>'?
                {
                    // Let's then just count number of brackets --
                    // in case they are not followed by '>'
                    int count = 1;
                    byte b;
                    while (true) {
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        b = inputBuffer[_inputPtr];
                        if (b != BYTE_RBRACKET) {
                            break;
                        }
                        ++_inputPtr; // to skip past bracket
                        ++count;
                    }
                    if (b == BYTE_GT && count > 1) {
                        reportIllegalCDataEnd();
                    }
                    // Nope. Need to output all brackets, then; except
                    // for one that can be left for normal output
                    while (count > 1) {
                        outputBuffer[outPtr++] = ']';
                        // Need to ensure room for one more char
                        if (outPtr >= outputBuffer.length) {
                            outputBuffer = _textBuilder.finishCurrentSegment();
                            outPtr = 0;
                        }
                        --count;
                    }
                }
                    // Can just output the first ']' along normal output
                    break;

                // default:
                // Other types are not important here...
            }
            // We know there's room for one more:
            outputBuffer[outPtr++] = (char) c;
        }

        _textBuilder.setCurrentLength(outPtr);

        /* 03-Feb-2009, tatu: To support coalescing mode, may need to
         *   do some extra work
         */
        if (_cfgCoalescing && !_entityPending) {
            finishCoalescedText();
        }
    }

    @Override
    protected void finishComment() throws XMLStreamException {
        final int[] TYPES = _charTypes.OTHER_CHARS;
        final byte[] inputBuffer = _inputBuffer;
        char[] outputBuffer = _textBuilder.resetWithEmpty();
        int outPtr = 0;

        main_loop: while (true) {
            int c;
            // Then the tight ascii non-funny-char loop:
            ascii_loop: while (true) {
                int ptr = _inputPtr;
                if (ptr >= _inputEnd) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                }
                if (outPtr >= outputBuffer.length) {
                    outputBuffer = _textBuilder.finishCurrentSegment();
                    outPtr = 0;
                }
                int max = _inputEnd;
                {
                    int max2 = ptr + (outputBuffer.length - outPtr);
                    if (max2 < max) {
                        max = max2;
                    }
                }
                while (ptr < max) {
                    c = (int) inputBuffer[ptr++] & 0xFF;
                    if (TYPES[c] != 0) {
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                    outputBuffer[outPtr++] = (char) c;
                }
                _inputPtr = ptr;
            }

            switch (TYPES[c]) {
                case XmlCharTypes.CT_INVALID:
                    handleInvalidXmlChar(c);
                case XmlCharTypes.CT_WS_CR:
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    if (inputBuffer[_inputPtr] == BYTE_LF) {
                        ++_inputPtr;
                    }
                    markLF();
                    c = INT_LF;
                    break;

                case XmlCharTypes.CT_WS_LF:
                    markLF();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_2:
                    c = decodeUtf8_2(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_3:
                    c = decodeUtf8_3(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_4:
                    c = decodeUtf8_4(c);
                    // Let's add first part right away:
                    outputBuffer[outPtr++] = (char) (0xD800 | (c >> 10));
                    if (outPtr >= outputBuffer.length) {
                        outputBuffer = _textBuilder.finishCurrentSegment();
                        outPtr = 0;
                    }
                    c = 0xDC00 | (c & 0x3FF);
                    // And let the other char output down below
                    break;

                case XmlCharTypes.CT_MULTIBYTE_N:
                    reportInvalidInitial(c);
                case XmlCharTypes.CT_HYPHEN: // '-->'?
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    if (_inputBuffer[_inputPtr] == BYTE_HYPHEN) { // ok, must be end then
                        ++_inputPtr;
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        if (_inputBuffer[_inputPtr++] != BYTE_GT) {
                            reportDoubleHyphenInComments();
                        }
                        break main_loop;
                    }
                    break;
                // default:
                // Other types are not important here...
            }

            // Ok, can output the char (we know there's room for one more)
            outputBuffer[outPtr++] = (char) c;
        }
        _textBuilder.setCurrentLength(outPtr);
    }

    /**
     * When this method gets called we know that we have an internal subset,
     * and that the opening '[' has already been read.
     */
    @Override
    protected void finishDTD(boolean copyContents) throws XMLStreamException {
        char[] outputBuffer = copyContents ? _textBuilder.resetWithEmpty() : null;
        int outPtr = 0;

        final int[] TYPES = _charTypes.DTD_CHARS;
        boolean inDecl = false; // in declaration/directive?
        int quoteChar = 0; // inside quoted string?

        main_loop: while (true) {
            int c;

            /* First we'll have a quickie loop for speeding through
             * uneventful chars...
             */
            ascii_loop: while (true) {
                int ptr = _inputPtr;
                if (ptr >= _inputEnd) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                }
                int max = _inputEnd;
                if (outputBuffer != null) {
                    if (outPtr >= outputBuffer.length) {
                        outputBuffer = _textBuilder.finishCurrentSegment();
                        outPtr = 0;
                    }
                    {
                        int max2 = ptr + (outputBuffer.length - outPtr);
                        if (max2 < max) {
                            max = max2;
                        }
                    }
                }
                while (ptr < max) {
                    c = (int) _inputBuffer[ptr++] & 0xFF;
                    if (TYPES[c] != 0) {
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                    if (outputBuffer != null) {
                        outputBuffer[outPtr++] = (char) c;
                    }
                }
                _inputPtr = ptr;
            }

            switch (TYPES[c]) {

                // First, common types

                case XmlCharTypes.CT_INVALID:
                    handleInvalidXmlChar(c);
                case XmlCharTypes.CT_WS_CR:
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    if (_inputBuffer[_inputPtr] == BYTE_LF) {
                        ++_inputPtr;
                    }
                    markLF();
                    c = INT_LF;
                    break;

                case XmlCharTypes.CT_WS_LF:
                    markLF();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_2:
                    c = decodeUtf8_2(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_3:
                    c = decodeUtf8_3(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_4:
                    c = decodeUtf8_4(c);
                    if (outputBuffer != null) {
                        // Let's add first part right away:
                        outputBuffer[outPtr++] = (char) (0xD800 | (c >> 10));
                        c = 0xDC00 | (c & 0x3FF);
                        if (outPtr >= outputBuffer.length) {
                            outputBuffer = _textBuilder.finishCurrentSegment();
                            outPtr = 0;
                        }
                        // And let the other char output down below
                    }
                    break;

                case XmlCharTypes.CT_MULTIBYTE_N:
                    reportInvalidInitial(c);

                    // Then DTD-specific types:

                case XmlCharTypes.CT_DTD_QUOTE: // apos or quot
                    if (quoteChar == 0) {
                        quoteChar = c;
                    } else {
                        if (quoteChar == c) {
                            quoteChar = 0;
                        }
                    }
                    break;

                case XmlCharTypes.CT_DTD_LT:
                    if (!inDecl) {
                        inDecl = true;
                    }
                    break;

                case XmlCharTypes.CT_DTD_GT:
                    if (quoteChar == 0) {
                        inDecl = false;
                    }
                    break;

                case XmlCharTypes.CT_DTD_RBRACKET:
                    if (!inDecl && quoteChar == 0) {
                        break main_loop;
                    }
                    break;

                // default:
                // Other types are not important here...
            }

            if (outputBuffer != null) { // will have room for one more
                outputBuffer[outPtr++] = (char) c;
            }
        }
        if (outputBuffer != null) {
            _textBuilder.setCurrentLength(outPtr);
        }

        // but still need to match the '>'...
        byte b = skipInternalWs(false, null);
        if (b != BYTE_GT) {
            throwUnexpectedChar(decodeCharForError(b), " expected '>' after the internal subset");
        }
    }

    @Override
    protected void finishPI() throws XMLStreamException {
        final int[] TYPES = _charTypes.OTHER_CHARS;
        final byte[] inputBuffer = _inputBuffer;
        char[] outputBuffer = _textBuilder.resetWithEmpty();
        int outPtr = 0;

        /* At this point, space (if any) has been skipped, and we are
         * to parse and store the contents
         */
        main_loop: while (true) {
            int c;
            // Then the tight ascii non-funny-char loop:
            ascii_loop: while (true) {
                int ptr = _inputPtr;
                if (ptr >= _inputEnd) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                }
                if (outPtr >= outputBuffer.length) {
                    outputBuffer = _textBuilder.finishCurrentSegment();
                    outPtr = 0;
                }
                int max = _inputEnd;
                {
                    int max2 = ptr + (outputBuffer.length - outPtr);
                    if (max2 < max) {
                        max = max2;
                    }
                }
                while (ptr < max) {
                    c = (int) inputBuffer[ptr++] & 0xFF;
                    if (TYPES[c] != 0) {
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                    outputBuffer[outPtr++] = (char) c;
                }
                _inputPtr = ptr;
            }
            // And then exceptions:
            switch (TYPES[c]) {
                case XmlCharTypes.CT_INVALID:
                    handleInvalidXmlChar(c);
                case XmlCharTypes.CT_WS_CR:
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    if (inputBuffer[_inputPtr] == BYTE_LF) {
                        ++_inputPtr;
                    }
                    markLF();
                    c = INT_LF;
                    break;

                case XmlCharTypes.CT_WS_LF:
                    markLF();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_2:
                    c = decodeUtf8_2(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_3:
                    c = decodeUtf8_3(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_4:
                    c = decodeUtf8_4(c);
                    // Let's add first part right away:
                    outputBuffer[outPtr++] = (char) (0xD800 | (c >> 10));
                    if (outPtr >= outputBuffer.length) {
                        outputBuffer = _textBuilder.finishCurrentSegment();
                        outPtr = 0;
                    }
                    c = 0xDC00 | (c & 0x3FF);
                    // And let the other char output down below
                    break;

                case XmlCharTypes.CT_MULTIBYTE_N:
                    reportInvalidInitial(c);
                case XmlCharTypes.CT_QMARK:
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    if (_inputBuffer[_inputPtr] == BYTE_GT) { // ok, the end!
                        ++_inputPtr;
                        break main_loop;
                    }
                    // Not end mark, just need to reprocess the second char
                    // default:
                    // Other types are not important here...
            }
            // Ok, can output the char (we know there's room for one more)
            outputBuffer[outPtr++] = (char) c;
        }
        _textBuilder.setCurrentLength(outPtr);
    }

    /**
     * Note: this method is only called in cases where it is known
     * that only space chars are legal. Thus, encountering a non-space
     * is an error (WFC or VC). However, an end-of-input is ok.
     */
    @Override
    protected void finishSpace() throws XMLStreamException {
        /* Ok: so, mTmpChar contains first space char. If it looks
         * like indentation, we can probably optimize a bit...
         */
        int tmp = _tmpChar;
        char[] outputBuffer;
        int outPtr;

        if (tmp == BYTE_CR || tmp == BYTE_LF) {
            outPtr = checkPrologIndentation(tmp);
            if (outPtr < 0) {
                return;
            }
            // Above call also initializes the text builder appropriately
            outputBuffer = _textBuilder.getBufferWithoutReset();
        } else {
            outputBuffer = _textBuilder.resetWithEmpty();
            outputBuffer[0] = (char) tmp;
            outPtr = 1;
        }

        int ptr = _inputPtr;

        while (true) {
            if (ptr >= _inputEnd) {
                if (!loadMore()) {
                    break;
                }
                ptr = _inputPtr;
            }
            int c = (int) _inputBuffer[ptr] & 0xFF;
            // !!! TODO: check for xml 1.1 whitespace?
            if (c > INT_SPACE) {
                break;
            }
            ++ptr;

            if (c == INT_LF) {
                markLF(ptr);
            } else if (c == INT_CR) {
                if (ptr >= _inputEnd) {
                    if (!loadMore()) { // still need to output the lf
                        if (outPtr >= outputBuffer.length) {
                            outputBuffer = _textBuilder.finishCurrentSegment();
                            outPtr = 0;
                        }
                        outputBuffer[outPtr++] = '\n';
                        break;
                    }
                    ptr = _inputPtr;
                }
                if (_inputBuffer[ptr] == BYTE_LF) {
                    ++ptr;
                }
                markLF(ptr);
                c = INT_LF; // need to convert to canonical lf
            } else if (c != INT_SPACE && c != INT_TAB) {
                _inputPtr = ptr;
                throwInvalidSpace(c);
            }

            // Ok, can output the char
            if (outPtr >= outputBuffer.length) {
                outputBuffer = _textBuilder.finishCurrentSegment();
                outPtr = 0;
            }
            outputBuffer[outPtr++] = (char) c;
        }

        _inputPtr = ptr;
        _textBuilder.setCurrentLength(outPtr);
    }

    /*
    /**********************************************************************
    /* 2nd level parsing/skipping for coalesced text
    /**********************************************************************
     */

    /**
     * Method that gets called after a primary text segment (of type
     * CHARACTERS or CDATA, not applicable to SPACE) has been read in
     * text buffer. Method has to see if the following event would
     * be textual as well, and if so, read it (and any other following
     * textual segments).
     */
    private void finishCoalescedText() throws XMLStreamException {
        while (true) {
            // no matter what, will need (and can get) one char
            if (_inputPtr >= _inputEnd) {
                if (!loadMore()) { // most likely an error, will be handled later on
                    return;
                }
            }

            if (_inputBuffer[_inputPtr] == BYTE_LT) { // markup of some kind
                /* In worst case, need 3 chars ("<![") all in all to know
                 * if we are getting a CDATA section
                 */
                if ((_inputPtr + 3) >= _inputEnd) {
                    if (!loadAndRetain()) {
                        // probably an error, but will be handled later
                        return;
                    }
                }
                if (_inputBuffer[_inputPtr + 1] != BYTE_EXCL || _inputBuffer[_inputPtr + 2] != BYTE_LBRACKET) {
                    // can't be CDATA, we are done here
                    return;
                }
                // but let's verify it still:
                _inputPtr += 3;
                for (int i = 0; i < 6; ++i) {
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    byte b = _inputBuffer[_inputPtr++];
                    if (b != (byte) CDATA_STR.charAt(i)) {
                        int ch = decodeCharForError(b);
                        reportTreeUnexpChar(ch, " (expected '" + CDATA_STR.charAt(i) + "' for CDATA section)");
                    }
                }
                finishCoalescedCData();
            } else { // textual (or entity, error etc)
                finishCoalescedCharacters();
                if (_entityPending) {
                    break;
                }
            }
        }
    }

    // note: code mostly copied from 'finishCharacters', just simplified
    // in some places
    private void finishCoalescedCharacters() throws XMLStreamException {
        // first char can't be from (char) entity (wrt finishCharacters)

        final int[] TYPES = _charTypes.TEXT_CHARS;
        final byte[] inputBuffer = _inputBuffer;

        char[] outputBuffer = _textBuilder.getBufferWithoutReset();
        int outPtr = _textBuilder.getCurrentLength();

        int c;

        main_loop: while (true) {
            // Then the tight ascii non-funny-char loop:
            ascii_loop: while (true) {
                int ptr = _inputPtr;
                if (ptr >= _inputEnd) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                }
                if (outPtr >= outputBuffer.length) {
                    outputBuffer = _textBuilder.finishCurrentSegment();
                    outPtr = 0;
                }
                int max = _inputEnd;
                {
                    int max2 = ptr + (outputBuffer.length - outPtr);
                    if (max2 < max) {
                        max = max2;
                    }
                }
                while (ptr < max) {
                    c = (int) inputBuffer[ptr++] & 0xFF;
                    if (TYPES[c] != 0) {
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                    outputBuffer[outPtr++] = (char) c;
                }
                _inputPtr = ptr;
            }
            // And then fallback for funny chars / UTF-8 multibytes:
            switch (TYPES[c]) {
                case XmlCharTypes.CT_INVALID:
                    handleInvalidXmlChar(c);
                case XmlCharTypes.CT_WS_CR:
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    if (inputBuffer[_inputPtr] == BYTE_LF) {
                        ++_inputPtr;
                    }
                    markLF();
                    c = INT_LF;
                    break;

                case XmlCharTypes.CT_WS_LF:
                    markLF();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_2:
                    c = decodeUtf8_2(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_3:
                    if ((_inputEnd - _inputPtr) >= 2) {
                        c = decodeUtf8_3fast(c);
                    } else {
                        c = decodeUtf8_3(c);
                    }
                    break;

                case XmlCharTypes.CT_MULTIBYTE_4:
                    c = decodeUtf8_4(c);
                    // Let's add first part right away:
                    outputBuffer[outPtr++] = (char) (0xD800 | (c >> 10));
                    if (outPtr >= outputBuffer.length) {
                        outputBuffer = _textBuilder.finishCurrentSegment();
                        outPtr = 0;
                    }
                    c = 0xDC00 | (c & 0x3FF);
                    // And let the other char output down below
                    break;

                case XmlCharTypes.CT_MULTIBYTE_N:
                    reportInvalidInitial(c);
                case XmlCharTypes.CT_LT:
                    --_inputPtr;
                    break main_loop;

                case XmlCharTypes.CT_AMP:
                    c = handleEntityInText();
                    if (c == 0) { // unexpandable general parsed entity
                        // _inputPtr set by entity expansion method
                        _entityPending = true;
                        break main_loop;
                    }
                    // Ok; does it need a surrogate though? (over 16 bits)
                    if ((c >> 16) != 0) {
                        c -= 0x10000;
                        outputBuffer[outPtr++] = (char) (0xD800 | (c >> 10));
                        // Need to ensure room for one more char
                        if (outPtr >= outputBuffer.length) {
                            outputBuffer = _textBuilder.finishCurrentSegment();
                            outPtr = 0;
                        }
                        c = 0xDC00 | (c & 0x3FF);
                    }
                    break;

                case XmlCharTypes.CT_RBRACKET: // ']]>'?
                {
                    // Let's then just count number of brackets --
                    // in case they are not followed by '>'
                    int count = 1;
                    byte b;
                    while (true) {
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        b = inputBuffer[_inputPtr];
                        if (b != BYTE_RBRACKET) {
                            break;
                        }
                        ++_inputPtr; // to skip past bracket
                        ++count;
                    }
                    if (b == BYTE_GT && count > 1) {
                        reportIllegalCDataEnd();
                    }
                    // Nope. Need to output all brackets, then; except
                    // for one that can be left for normal output
                    while (count > 1) {
                        outputBuffer[outPtr++] = ']';
                        // Need to ensure room for one more char
                        if (outPtr >= outputBuffer.length) {
                            outputBuffer = _textBuilder.finishCurrentSegment();
                            outPtr = 0;
                        }
                        --count;
                    }
                }
                    // Can just output the first ']' along normal output
                    break;

                // default:
                // Other types are not important here...
            }
            // We know there's room for one more:
            outputBuffer[outPtr++] = (char) c;
        }

        _textBuilder.setCurrentLength(outPtr);
    }

    // note: code mostly copied from 'finishCharacters', just simplified
    // in some places
    private void finishCoalescedCData() throws XMLStreamException {
        final int[] TYPES = _charTypes.OTHER_CHARS;
        final byte[] inputBuffer = _inputBuffer;

        char[] outputBuffer = _textBuilder.getBufferWithoutReset();
        int outPtr = _textBuilder.getCurrentLength();

        /* At this point, space (if any) has been skipped, and we are
         * to parse and store the contents
         */
        main_loop: while (true) {
            int c;
            // Then the tight ascii non-funny-char loop:
            ascii_loop: while (true) {
                int ptr = _inputPtr;
                if (ptr >= _inputEnd) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                }
                if (outPtr >= outputBuffer.length) {
                    outputBuffer = _textBuilder.finishCurrentSegment();
                    outPtr = 0;
                }
                int max = _inputEnd;
                {
                    int max2 = ptr + (outputBuffer.length - outPtr);
                    if (max2 < max) {
                        max = max2;
                    }
                }
                while (ptr < max) {
                    c = (int) inputBuffer[ptr++] & 0xFF;
                    if (TYPES[c] != 0) {
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                    outputBuffer[outPtr++] = (char) c;
                }
                _inputPtr = ptr;
            }
            // And then exceptions:
            switch (TYPES[c]) {
                case XmlCharTypes.CT_INVALID:
                    handleInvalidXmlChar(c);
                case XmlCharTypes.CT_WS_CR:
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    if (inputBuffer[_inputPtr] == BYTE_LF) {
                        ++_inputPtr;
                    }
                    markLF();
                    c = INT_LF;
                    break;

                case XmlCharTypes.CT_WS_LF:
                    markLF();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_2:
                    c = decodeUtf8_2(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_3:
                    c = decodeUtf8_3(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_4:
                    c = decodeUtf8_4(c);
                    // Let's add first part right away:
                    outputBuffer[outPtr++] = (char) (0xD800 | (c >> 10));
                    if (outPtr >= outputBuffer.length) {
                        outputBuffer = _textBuilder.finishCurrentSegment();
                        outPtr = 0;
                    }
                    c = 0xDC00 | (c & 0x3FF);
                    // And let the other char output down below
                    break;

                case XmlCharTypes.CT_MULTIBYTE_N:
                    reportInvalidInitial(c);
                case XmlCharTypes.CT_RBRACKET: // close ']]>' marker?
                    /* Ok: let's just parse all consequtive right brackets,
                     * and see if followed by greater-than char. This because
                     * we can only push back at most one char at a time, and
                     * thus can't easily just check a subset
                     */
                    int count = 0; // ignoring first one
                    byte b;

                    do {
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        b = _inputBuffer[_inputPtr];
                        if (b != BYTE_RBRACKET) {
                            break;
                        }
                        ++_inputPtr;
                        ++count;
                    } while (true);

                    // Was the marker found?
                    boolean ok = (b == BYTE_GT && count >= 1);
                    if (ok) {
                        --count;
                    }
                    // Brackets to copy to output?
                    for (; count > 0; --count) {
                        outputBuffer[outPtr++] = ']';
                        if (outPtr >= outputBuffer.length) {
                            outputBuffer = _textBuilder.finishCurrentSegment();
                            outPtr = 0;
                        }
                    }
                    if (ok) {
                        ++_inputPtr; // to consume '>'
                        break main_loop;
                    }
                    break;
            }
            // Ok, can output the char; there's room for one char at least
            outputBuffer[outPtr++] = (char) c;
        }

        _textBuilder.setCurrentLength(outPtr);
    }

    /**
     * Method that gets called after a primary text segment (of type
     * CHARACTERS or CDATA, not applicable to SPACE) has been skipped.
     * Method has to see if the following event would
     * be textual as well, and if so, skip it (and any other following
     * textual segments).
     *
     * @return True if we encountered an unexpandable entity
     */
    @Override
    protected boolean skipCoalescedText() throws XMLStreamException {
        while (true) {
            // no matter what, will need (and can get) one char
            if (_inputPtr >= _inputEnd) {
                if (!loadMore()) { // most likely an error, will be handled later on
                    return false;
                }
            }

            if (_inputBuffer[_inputPtr] == BYTE_LT) { // markup of some kind
                /* In worst case, need 3 chars ("<![") all in all to know
                 * if we are getting a CDATA section
                 */
                if ((_inputPtr + 3) >= _inputEnd) {
                    if (!loadAndRetain()) { // probably an error, but will be handled later
                        return false;
                    }
                }
                if (_inputBuffer[_inputPtr + 1] != BYTE_EXCL || _inputBuffer[_inputPtr + 2] != BYTE_LBRACKET) {
                    // can't be CDATA, we are done here
                    return false;
                }
                // but let's verify it still:
                _inputPtr += 3;
                for (int i = 0; i < 6; ++i) {
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    byte b = _inputBuffer[_inputPtr++];
                    if (b != (byte) CDATA_STR.charAt(i)) {
                        int ch = decodeCharForError(b);
                        reportTreeUnexpChar(ch, " (expected '" + CDATA_STR.charAt(i) + "' for CDATA section)");
                    }
                }
                skipCData();
            } else { // textual (or entity, error etc)
                if (skipCharacters()) {
                    return true;
                }
            }
        }
    }

    /*
    /**********************************************************************
    /* Other methods, utf-decoding
    /**********************************************************************
     */

    /**
     * @return Either decoded character (if positive int); or negated
     *    value of a high-order char (one that needs surrogate pair)
     */
    private int decodeMultiByteChar(int c, int ptr) throws XMLStreamException {
        int needed;

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
            reportInvalidInitial(c & 0xFF);
            needed = 1; // never gets here
        }

        if (ptr >= _inputEnd) {
            loadMoreGuaranteed();
            ptr = _inputPtr;
        }
        int d = _inputBuffer[ptr++];
        if ((d & 0xC0) != 0x080) {
            reportInvalidOther(d & 0xFF, ptr);
        }
        c = (c << 6) | (d & 0x3F);

        if (needed > 1) { // needed == 1 means 2 bytes total
            if (ptr >= _inputEnd) {
                loadMoreGuaranteed();
                ptr = _inputPtr;
            }
            d = _inputBuffer[ptr++];
            if ((d & 0xC0) != 0x080) {
                reportInvalidOther(d & 0xFF, ptr);
            }
            c = (c << 6) | (d & 0x3F);
            if (needed > 2) { // 4 bytes? (need surrogates)
                if (ptr >= _inputEnd) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                }
                d = _inputBuffer[ptr++];
                if ((d & 0xC0) != 0x080) {
                    reportInvalidOther(d & 0xFF, ptr);
                }
                c = (c << 6) | (d & 0x3F);
                /* Need to signal such pair differently (to make comparison
                 * easier)
                 */
                c = -c;
            }
        }
        _inputPtr = ptr;
        return c;
    }

    private int decodeUtf8_2(int c) throws XMLStreamException {
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        int d = _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            reportInvalidOther(d & 0xFF, _inputPtr);
        }
        return ((c & 0x1F) << 6) | (d & 0x3F);
    }

    private int decodeUtf8_3(int c1) throws XMLStreamException {
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        c1 &= 0x0F;
        int d = _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            reportInvalidOther(d & 0xFF, _inputPtr);
        }
        int c = (c1 << 6) | (d & 0x3F);
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        d = _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            reportInvalidOther(d & 0xFF, _inputPtr);
        }
        c = (c << 6) | (d & 0x3F);
        if (c1 >= 0xD) { // 0xD800-0xDFFF, 0xFFFE-0xFFFF illegal
            if (c >= 0xD800) { // surrogates illegal, as well as 0xFFFE/0xFFFF
                if (c < 0xE000 || c >= 0xFFFE) {
                    c = handleInvalidXmlChar(c);
                }
            }
        }
        return c;
    }

    private int decodeUtf8_3fast(int c1) throws XMLStreamException {
        c1 &= 0x0F;
        int d = _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            reportInvalidOther(d & 0xFF, _inputPtr);
        }
        int c = (c1 << 6) | (d & 0x3F);
        d = _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            reportInvalidOther(d & 0xFF, _inputPtr);
        }
        c = (c << 6) | (d & 0x3F);
        if (c1 >= 0xD) { // 0xD800-0xDFFF, 0xFFFE-0xFFFF illegal
            if (c >= 0xD800) { // surrogates illegal, as well as 0xFFFE/0xFFFF
                if (c < 0xE000 || c >= 0xFFFE) {
                    c = handleInvalidXmlChar(c);
                }
            }
        }
        return c;
    }

    /**
     * @return Character value <b>minus 0x10000</c>; this so that caller
     *    can readily expand it to actual surrogates
     */
    private int decodeUtf8_4(int c) throws XMLStreamException {
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        int d = _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            reportInvalidOther(d & 0xFF, _inputPtr);
        }
        c = ((c & 0x07) << 6) | (d & 0x3F);

        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        d = _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            reportInvalidOther(d & 0xFF, _inputPtr);
        }
        c = (c << 6) | (d & 0x3F);
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        d = _inputBuffer[_inputPtr++];
        if ((d & 0xC0) != 0x080) {
            reportInvalidOther(d & 0xFF, _inputPtr);
        }

        /* note: won't change it to negative here, since caller
         * already knows it'll need a surrogate
         */
        return ((c << 6) | (d & 0x3F)) - 0x10000;
    }

    /*
    /**********************************************************************
    /* Internal methods, error reporting
    /**********************************************************************
     */

    /**
     * Method called called to decode a full UTF-8 characters, given
     * its first byte. Note: does not do any validity checks, since this
     * is only to be used for informational purposes (often when an error
     * has already been encountered)
     */
    public int decodeCharForError(byte b) throws XMLStreamException {
        int c = b;
        if (c >= 0) { // ascii? fine as is...
            return c;
        }
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
            reportInvalidInitial(c & 0xFF);
            needed = 1; // never gets here
        }

        int d = nextByte();
        if ((d & 0xC0) != 0x080) {
            reportInvalidOther(d & 0xFF);
        }
        c = (c << 6) | (d & 0x3F);

        if (needed > 1) { // needed == 1 means 2 bytes total
            d = nextByte(); // 3rd byte
            if ((d & 0xC0) != 0x080) {
                reportInvalidOther(d & 0xFF);
            }
            c = (c << 6) | (d & 0x3F);
            if (needed > 2) { // 4 bytes? (need surrogates)
                d = nextByte();
                if ((d & 0xC0) != 0x080) {
                    reportInvalidOther(d & 0xFF);
                }
                c = (c << 6) | (d & 0x3F);
            }
        }
        return c;
    }

    private void reportInvalidOther(int mask, int ptr) throws XMLStreamException {
        _inputPtr = ptr;
        reportInvalidOther(mask);
    }

    /*
    /**********************************************************************
    /* Internal methods, secondary parsing
    /**********************************************************************
     */

    @Override
    protected void finishToken() throws XMLStreamException {
        _tokenIncomplete = false;
        switch (_currToken) {
            case PROCESSING_INSTRUCTION:
                finishPI();
                break;

            case CHARACTERS:
                finishCharacters();
                break;

            case COMMENT:
                finishComment();
                break;

            case SPACE:
                finishSpace();
                break;

            case DTD:
                finishDTD(true); // true -> get text
                break;

            case CDATA:
                finishCData();
                break;

            default:
                ErrorConsts.throwInternalError();
        }
    }

    private byte nextByte() throws XMLStreamException {
        if (_inputPtr >= _inputEnd) {
            if (!loadMore()) {
                reportInputProblem(
                    "Unexpected end-of-input when trying to parse " + ErrorConsts.tokenTypeDesc(_currToken));
            }
        }
        return _inputBuffer[_inputPtr++];
    }

    private byte loadOne() throws XMLStreamException {
        if (!loadMore()) {
            reportInputProblem("Unexpected end-of-input when trying to parse " + ErrorConsts.tokenTypeDesc(_currToken));
        }
        return _inputBuffer[_inputPtr++];
    }

    private byte loadOne(int type) throws XMLStreamException {
        if (!loadMore()) {
            reportInputProblem("Unexpected end-of-input when trying to parse " + ErrorConsts.tokenTypeDesc(type));
        }
        return _inputBuffer[_inputPtr++];
    }

    private boolean loadAndRetain() throws XMLStreamException {
        /* first: can't move, if we were handed an immutable block
         * (alternative to handing InputStream as _in)
         */
        if (_in == null) {
            return false;
        }

        // otherwise, need to use cut'n pasted code from loadMore()...

        _pastBytesOrChars += _inputPtr;
        _rowStartOffset -= _inputPtr;

        int remaining = (_inputEnd - _inputPtr); // must be > 0
        System.arraycopy(_inputBuffer, _inputPtr, _inputBuffer, 0, remaining);
        _inputPtr = 0;
        _inputEnd = remaining; // temporarily set to cover copied stuff

        try {
            do {
                int max = _inputBuffer.length - _inputEnd;
                int count = _in.read(_inputBuffer, _inputEnd, max);
                if (count < 1) {
                    if (count == 0) {
                        // Sanity check, should never happen with non-buggy readers/stream
                        reportInputProblem("InputStream returned 0 bytes, even when asked to read up to " + max);
                    }
                    return false;
                }
                _inputEnd += count;
            } while (_inputEnd < 3);
            return true;
        } catch (IOException ioe) {
            throw new StreamExceptionBase(ioe);
        }
    }

    /**
     * Conceptually, this method really does NOT belong here. However,
     * currently it is quite hard to refactor it, so it'll have to
     * stay here until better place is found
     */
    private PName addUTFPName(ByteBasedPNameTable symbols, XmlCharTypes charTypes, int hash, int[] quads, int qlen,
        int lastQuadBytes) throws XMLStreamException {
        // 4 bytes per quad, except last one maybe less
        int byteLen = (qlen << 2) - 4 + lastQuadBytes;

        // And last one is not correctly aligned (leading zero bytes instead
        // need to shift a bit, instead of trailing). Only need to shift it
        // for UTF-8 decoding; need revert for storage (since key will not
        // be aligned, to optimize lookup speed)
        int lastQuad;

        if (lastQuadBytes < 4) {
            lastQuad = quads[qlen - 1];
            // 8/16/24 bit left shift
            quads[qlen - 1] = (lastQuad << ((4 - lastQuadBytes) << 3));
        } else {
            lastQuad = 0;
        }

        // Let's handle first char separately (different validation):
        int ch = (quads[0] >>> 24);
        boolean ok;
        int ix = 1;
        char[] cbuf = _nameBuffer;
        int cix = 0;
        final int[] TYPES = charTypes.NAME_CHARS;

        switch (TYPES[ch]) {
            case XmlCharTypes.CT_NAME_NONE:
            case XmlCharTypes.CT_NAME_COLON: // not ok as first
            case XmlCharTypes.CT_NAME_NONFIRST:
            case InputCharTypes.CT_INPUT_NAME_MB_N:
                ok = false;
                break;

            case XmlCharTypes.CT_NAME_ANY:
                ok = true;
                break;

            default: // multi-byte (UTF-8) chars:
            {
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
                    reportInvalidInitial(ch);
                    needed = ch = 1; // never really gets this far
                }
                if ((ix + needed) > byteLen) {
                    reportEofInName();
                }
                ix += needed;

                int q = quads[0];
                // Always need at least one more right away:
                int ch2 = (q >> 16) & 0xFF;
                if ((ch2 & 0xC0) != 0x080) {
                    reportInvalidOther(ch2);
                }
                ch = (ch << 6) | (ch2 & 0x3F);

                /* And then may need more. Note: here we do not do all the
                 * checks that UTF-8 text decoder might do. Reason is that
                 * name validity checking methods handle most of such checks
                 */
                if (needed > 1) {
                    ch2 = (q >> 8) & 0xFF;
                    if ((ch2 & 0xC0) != 0x080) {
                        reportInvalidOther(ch2);
                    }
                    ch = (ch << 6) | (ch2 & 0x3F);
                    if (needed > 2) { // 4 bytes? (need surrogates on output)
                        ch2 = q & 0xFF;
                        if ((ch2 & 0xC0) != 0x080) {
                            reportInvalidOther(ch2 & 0xFF);
                        }
                        ch = (ch << 6) | (ch2 & 0x3F);
                    }
                }
                ok = XmlChars.is10NameStartChar(ch);
                if (needed > 2) { // outside of basic 16-bit range? need surrogates
                    /* so, let's first output first char (high surrogate),
                     * let second be output by later code
                     */
                    ch -= 0x10000; // to normalize it starting with 0x0
                    cbuf[cix++] = (char) (0xD800 + (ch >> 10));
                    ch = (0xDC00 | (ch & 0x03FF));
                }
            }
        }

        if (!ok) { // 0 to indicate it's first char, even with surrogates
            reportInvalidNameChar(ch, 0);
        }

        cbuf[cix++] = (char) ch; // the only char, or second (low) surrogate

        /* Whoa! Tons of code for just the start char. But now we get to
         * decode the name proper, at last!
         */
        int last_colon = -1;

        while (ix < byteLen) {
            ch = quads[ix >> 2]; // current quad, need to shift+mask
            int byteIx = (ix & 3);
            ch = (ch >> ((3 - byteIx) << 3)) & 0xFF;
            ++ix;

            // Ascii?
            switch (TYPES[ch]) {
                case XmlCharTypes.CT_NAME_NONE:
                case XmlCharTypes.CT_MULTIBYTE_N:
                    ok = false;
                    break;

                case XmlCharTypes.CT_NAME_COLON: // not ok as first
                    if (last_colon >= 0) {
                        reportMultipleColonsInName();
                    }
                    last_colon = cix;
                    ok = true;
                    break;

                case XmlCharTypes.CT_NAME_NONFIRST:
                case XmlCharTypes.CT_NAME_ANY:
                    ok = true;
                    break;

                default: {
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
                        reportInvalidInitial(ch);
                        needed = ch = 1; // never really gets this far
                    }
                    if ((ix + needed) > byteLen) {
                        reportEofInName();
                    }

                    // Ok, always need at least one more:
                    int ch2 = quads[ix >> 2]; // current quad, need to shift+mask
                    byteIx = (ix & 3);
                    ch2 = (ch2 >> ((3 - byteIx) << 3));
                    ++ix;

                    if ((ch2 & 0xC0) != 0x080) {
                        reportInvalidOther(ch2);
                    }
                    ch = (ch << 6) | (ch2 & 0x3F);

                    // Once again, some of validation deferred to name char validator
                    if (needed > 1) {
                        ch2 = quads[ix >> 2];
                        byteIx = (ix & 3);
                        ch2 = (ch2 >> ((3 - byteIx) << 3));
                        ++ix;

                        if ((ch2 & 0xC0) != 0x080) {
                            reportInvalidOther(ch2);
                        }
                        ch = (ch << 6) | (ch2 & 0x3F);
                        if (needed > 2) { // 4 bytes? (need surrogates on output)
                            ch2 = quads[ix >> 2];
                            byteIx = (ix & 3);
                            ch2 = (ch2 >> ((3 - byteIx) << 3));
                            ++ix;
                            if ((ch2 & 0xC0) != 0x080) {
                                reportInvalidOther(ch2 & 0xFF);
                            }
                            ch = (ch << 6) | (ch2 & 0x3F);
                        }
                    }
                    ok = XmlChars.is10NameChar(ch);
                    if (needed > 2) { // surrogate pair? once again, let's output one here, one later on
                        ch -= 0x10000; // to normalize it starting with 0x0
                        if (cix >= cbuf.length) {
                            _nameBuffer = cbuf = DataUtil.growArrayBy(cbuf, cbuf.length);
                        }
                        cbuf[cix++] = (char) (0xD800 + (ch >> 10));
                        ch = 0xDC00 | (ch & 0x03FF);
                    }
                }
            }
            if (!ok) {
                reportInvalidNameChar(ch, cix);
            }
            if (cix >= cbuf.length) {
                _nameBuffer = cbuf = DataUtil.growArrayBy(cbuf, cbuf.length);
            }
            cbuf[cix++] = (char) ch;
        }

        /* Ok. Now we have the character array, and can construct the
         * String (as well as check proper composition of semicolons
         * for ns-aware mode...)
         */
        String baseName = new String(cbuf, 0, cix);
        // And finally, unalign if necessary
        if (lastQuadBytes < 4) {
            quads[qlen - 1] = lastQuad;
        }
        return symbols.addSymbol(hash, baseName, last_colon, quads, qlen);
    }

    /*
    /**********************************************************************
    /* Error reporting
    /**********************************************************************
     */

    private void reportInvalidInitial(int mask) throws XMLStreamException {
        reportInputProblem("Invalid UTF-8 start byte 0x" + Integer.toHexString(mask));
    }

    private void reportInvalidOther(int mask) throws XMLStreamException {
        reportInputProblem("Invalid UTF-8 middle byte 0x" + Integer.toHexString(mask));
    }
}
