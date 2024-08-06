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

import java.io.*;

import javax.xml.stream.XMLStreamException;

import com.azure.xml.implementation.aalto.impl.ErrorConsts;
import com.azure.xml.implementation.aalto.impl.IoStreamException;
import com.azure.xml.implementation.aalto.util.DataUtil;
import com.azure.xml.implementation.aalto.util.TextBuilder;
import com.azure.xml.implementation.aalto.util.XmlCharTypes;

/**
 * Base class for various byte stream based scanners (generally one
 * for each type of encoding supported).
 */
public abstract class StreamScanner extends ByteBasedScanner {
    /*
    /**********************************************************************
    /* Configuration, input, buffering
    /**********************************************************************
     */

    /**
     * Underlying InputStream to use for reading content.
     */
    protected InputStream _in;

    protected byte[] _inputBuffer;

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
    protected final XmlCharTypes _charTypes;

    /**
     * For now, symbol table contains prefixed names. In future it is
     * possible that they may be split into prefixes and local names?
     */
    protected final ByteBasedPNameTable _symbols;

    /**
     * This buffer is used for name parsing. Will be expanded if/as
     * needed; 32 ints can hold names 128 ascii chars long.
     */
    protected int[] _quadBuffer = new int[32];

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
    /* Abstract methods for sub-classes to implement
    /**********************************************************************
     */

    protected abstract int handleEntityInText() throws XMLStreamException;

    protected abstract String parsePublicId(byte quoteChar) throws XMLStreamException;

    protected abstract String parseSystemId(byte quoteChar) throws XMLStreamException;

    /*
    /**********************************************************************
    /* Implementation of parsing API
    /**********************************************************************
     */

    @Override
    public final int nextFromProlog(boolean isProlog) throws XMLStreamException {
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
    public final int nextFromTree() throws XMLStreamException {
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
        if (_cfgLazyParsing) {
            _tokenIncomplete = true;
        } else {
            finishCharacters();
        }
        return (_currToken = CHARACTERS);
    }

    /**
     * Helper method used to isolate things that need to be (re)set in
     * cases where
     */
    protected int _nextEntity() {
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
                if (_cfgLazyParsing) {
                    _tokenIncomplete = true;
                } else {
                    finishComment();
                }
                return (_currToken = COMMENT);
            }
        } else if (b == BYTE_D) { // DOCTYPE?
            if (isProlog) { // no DOCTYPE in epilog
                handleDtdStart();
                // incomplete flag is set by handleDtdStart
                if (!_cfgLazyParsing) {
                    if (_tokenIncomplete) {
                        finishDTD(true); // must copy contents, may be needed
                        _tokenIncomplete = false;
                    }
                }
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
            if (_cfgLazyParsing) {
                _tokenIncomplete = true;
            } else {
                finishComment();
            }
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
            if (_cfgLazyParsing) {
                _tokenIncomplete = true;
            } else {
                finishCData();
            }
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
            if (_cfgLazyParsing) {
                _tokenIncomplete = true;
            } else {
                finishPI();
            }
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
    protected final int handleCharEntity() throws XMLStreamException {
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
    protected abstract int handleStartElement(byte b) throws XMLStreamException;

    /**
     * Note that this method is currently also shareable for all Ascii-based
     * encodings, and at least between UTF-8 and ISO-Latin1. The reason is
     * that since we already know exact bytes that need to be matched,
     * there's no danger of getting invalid encodings or such.
     * So, for now, let's leave this method here in the base class.
     */
    protected final int handleEndElement() throws XMLStreamException {
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

    /* 28-Oct-2006, tatus: This is the old (slow) implementation. I'll
     *   leave it here, since it's known to work, so in case new impl
     *   has problems, one can refer to the old impl
     */
    /*
    protected final int handleEndElement2()
        throws XMLStreamException
    {
        --_depth;
        _currToken = END_ELEMENT;
        // Ok, at this point we have seen '/', need the name
        _tokenName = _currElem.getName();
    
        int i2;
        int qix = 0;
    
        while (true) {
            int q;
            int expQuad = _tokenName.getQuad(qix);
    
            // First byte of a quad:
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            i2 = _inputBuffer[_inputPtr++] & 0xFF;
            if (i2 < 65) {
                // Ok; "_" (45), "." (46) and "0"-"9"/":" (48 - 57/58) still name chars
                if (i2 < 45 || i2 > 58 || i2 == 47) {
                    if (0 != expQuad || _tokenName.sizeInQuads() != qix) {
                        reportUnexpectedEndTag(_tokenName.getPrefixedName());
                    }
                    break;
                }
            }
            q = i2;
            ++qix; // since this started a new quad
    
            // second byte
            //i2 = (int) ((_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne()) & 0xFF;
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            i2 = _inputBuffer[_inputPtr++] & 0xFF;
            if (i2 < 65) {
                if (i2 < 45 || i2 > 58 || i2 == 47) {
                    if (q != expQuad || _tokenName.sizeInQuads() != qix) {
                        reportUnexpectedEndTag(_tokenName.getPrefixedName());
                    }
                    break;
                }
            }
            q = (q << 8) | i2;
    
            // third byte
            //i2 = (int) ((_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne()) & 0xFF;
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            i2 = _inputBuffer[_inputPtr++] & 0xFF;
            if (i2 < 65) {
                if (i2 < 45 || i2 > 58 || i2 == 47) { // 2 (ascii) char name?
                    if (q != expQuad || _tokenName.sizeInQuads() != qix) {
                        reportUnexpectedEndTag(_tokenName.getPrefixedName());
                    }
                    break;
                }
            }
            q = (q << 8) | i2;
    
            // fourth byte
            //i2 = (int) ((_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne()) & 0xFF;
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            i2 = _inputBuffer[_inputPtr++] & 0xFF;
            if (i2 < 65) {
                if (i2 < 45 || i2 > 58 || i2 == 47) { // 2 (ascii) char name?
                    if (q != expQuad || _tokenName.sizeInQuads() != qix) {
                        reportUnexpectedEndTag(_tokenName.getPrefixedName());
                    }
                    break;
                }
            }
            q = (q << 8) | i2;
    
            // Full quad, ok; need to compare now:
            if (q != expQuad) {
                // Let's just fall through, then; will throw exception
                reportUnexpectedEndTag(_tokenName.getPrefixedName());
            }
        }
    
        // Note: i2 still holds the last byte read (except if we detected
        // a mismatch; but that caused an exception above)
    
        // Trailing space?
        while (i2 <= INT_SPACE) {
            if (i2 == INT_LF) {
                markLF();
            } else if (i2 == INT_CR) {
                byte b = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne();
                if (b != BYTE_LF) {
                    markLF(_inputPtr-1);
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
            throwUnexpectedChar(decodeCharForError((byte)i2), " expected space or closing '>'");
        }
        return END_ELEMENT;
    }
    */

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
    protected final PName parsePName(byte b) throws XMLStreamException {
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

    protected PName parsePNameMedium(int i2, int q1) throws XMLStreamException {
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

    protected final PName parsePNameLong(int q, int[] quads) throws XMLStreamException {
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

    protected final PName parsePNameSlow(byte b) throws XMLStreamException {
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

    protected final PName addPName(int hash, int[] quads, int qlen, int lastQuadBytes) throws XMLStreamException {
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
    protected byte skipInternalWs(boolean reqd, String msg) throws XMLStreamException {
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
    protected final int checkInTreeIndentation(int c) throws XMLStreamException {
        if (c == INT_CR) {
            // First a degenerate case, a lone \r:
            if (_inputPtr >= _inputEnd && !loadMore()) {
                _textBuilder.resetWithIndentation(0, CHAR_SPACE);
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
                    _textBuilder.resetWithIndentation(0, CHAR_SPACE);
                    return -1;
                }
            }
            char[] outBuf = _textBuilder.resetWithEmpty();
            outBuf[0] = CHAR_LF;
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
        outBuf[0] = CHAR_LF;
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
    protected final int checkPrologIndentation(int c) throws XMLStreamException {
        if (c == INT_CR) {
            // First a degenerate case, a lone \r:
            if (_inputPtr >= _inputEnd && !loadMore()) {
                _textBuilder.resetWithIndentation(0, CHAR_SPACE);
                return -1;
            }
            if (_inputBuffer[_inputPtr] == BYTE_LF) {
                ++_inputPtr;
            }
        }
        markLF();
        // Ok, indentation char?
        if (_inputPtr >= _inputEnd && !loadMore()) {
            _textBuilder.resetWithIndentation(0, CHAR_SPACE);
            return -1;
        }
        byte b = _inputBuffer[_inputPtr]; // won't advance past the char yet
        if (b != BYTE_SPACE && b != BYTE_TAB) {
            // If lt, it's still indentation ok:
            if (b == BYTE_LT) { // need
                _textBuilder.resetWithIndentation(0, CHAR_SPACE);
                return -1;
            }
            // Nope... something else
            char[] outBuf = _textBuilder.resetWithEmpty();
            outBuf[0] = CHAR_LF;
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
                outBuf[0] = CHAR_LF;
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
    protected final boolean loadMore() throws XMLStreamException {
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
            throw new IoStreamException(ioe);
        }
    }

    protected final byte nextByte() throws XMLStreamException {
        if (_inputPtr >= _inputEnd) {
            if (!loadMore()) {
                reportInputProblem(
                    "Unexpected end-of-input when trying to parse " + ErrorConsts.tokenTypeDesc(_currToken));
            }
        }
        return _inputBuffer[_inputPtr++];
    }

    protected final byte loadOne() throws XMLStreamException {
        if (!loadMore()) {
            reportInputProblem("Unexpected end-of-input when trying to parse " + ErrorConsts.tokenTypeDesc(_currToken));
        }
        return _inputBuffer[_inputPtr++];
    }

    protected final byte loadOne(int type) throws XMLStreamException {
        if (!loadMore()) {
            reportInputProblem("Unexpected end-of-input when trying to parse " + ErrorConsts.tokenTypeDesc(type));
        }
        return _inputBuffer[_inputPtr++];
    }

    protected final boolean loadAndRetain() throws XMLStreamException {
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
            throw new IoStreamException(ioe);
        }
    }
}
