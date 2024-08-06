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
import java.util.Objects;

import javax.xml.stream.XMLStreamException;

import com.azure.xml.implementation.aalto.impl.ErrorConsts;
import com.azure.xml.implementation.aalto.util.DataUtil;
import com.azure.xml.implementation.aalto.util.XmlCharTypes;
import com.azure.xml.implementation.aalto.util.XmlChars;

/**
 * Scanner for tokenizing XML content from a byte stream encoding using
 * UTF-8 encoding, or something suitably close it for decoding purposes
 * (including ISO-Latin1 and US-ASCII).
 */
@SuppressWarnings("fallthrough")
public final class Utf8Scanner extends StreamScanner {
    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    public Utf8Scanner(ReaderConfig cfg, InputStream in, byte[] buffer, int ptr, int last) {
        super(cfg, in, buffer, ptr, last);
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

    @Override
    protected int handleStartElement(byte b) throws XMLStreamException {
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
    @Override
    protected int handleEntityInText() throws XMLStreamException {
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
        if (false) {
            reportInputProblem("General entity reference (&" + pname
                + ";) encountered in attribute value, in non-entity-expanding mode: no way to handle it");
        }
        return 0;
    }

    /*
    /**********************************************************************
    /* Internal methods, name parsing:
    /**********************************************************************
     */

    /**
     * Parsing of public ids is bit more complicated than that of system
     * ids, since white space is to be coalesced.
     */
    @Override
    protected String parsePublicId(byte quoteChar) throws XMLStreamException {
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

    @Override
    protected String parseSystemId(byte quoteChar) throws XMLStreamException {
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

    /*
    private final int skipMultiByteChar(int c, int ptr)
        throws XMLStreamException
    {
        int needed;
    
        // Ok; if we end here, we got multi-byte combination
        if ((c & 0xE0) == 0xC0) { // 2 bytes (0x0080 - 0x07FF)
            needed = 1;
        } else if ((c & 0xF0) == 0xE0) { // 3 bytes (0x0800 - 0xFFFF)
            needed = 2;
        } else if ((c & 0xF8) == 0xF0) {
            // 4 bytes; double-char with surrogates and all...
            needed = 3;
        } else {
            reportInvalidInitial(c & 0xFF);
            needed = 1; // never gets here
        }
    
        if (ptr >= _inputEnd) {
            loadMoreGuaranteed();
            ptr = _inputPtr;
        }
        c = (int) _inputBuffer[ptr++];
    
        if ((c & 0xC0) != 0x080) {
            reportInvalidOther(c & 0xFF, ptr);
        }
    
        if (needed > 1) { // needed == 1 means 2 bytes total
            if (ptr >= _inputEnd) {
                loadMoreGuaranteed();
                ptr = _inputPtr;
            }
            c = (int) _inputBuffer[ptr++];
    
            if ((c & 0xC0) != 0x080) {
                reportInvalidOther(c & 0xFF, ptr);
            }
            if (needed > 2) { // 4 bytes? (need surrogates)
                if (ptr >= _inputEnd) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                }
                c = (int) _inputBuffer[ptr++];
    
                if ((c & 0xC0) != 0x080) {
                    reportInvalidOther(c & 0xFF, ptr);
                }
            }
        }
        return ptr;
    }
    
    private final int skipMultiByteChar(int c, int type, int ptr)
        throws XMLStreamException
    {
        type -= XmlCharTypes.CT_MULTIBYTE_N; // number of more bytes needed
    
        if (ptr >= _inputEnd) {
            loadMoreGuaranteed();
            ptr = _inputPtr;
        }
        c = (int) _inputBuffer[ptr++];
        if ((c & 0xC0) != 0x080) {
            reportInvalidOther(c & 0xFF, ptr);
        }
        if (type > 1) { // needed == 1 means 2 bytes total
            if (ptr >= _inputEnd) {
                loadMoreGuaranteed();
                ptr = _inputPtr;
            }
            c = (int) _inputBuffer[ptr++];
            if ((c & 0xC0) != 0x080) {
                reportInvalidOther(c & 0xFF, ptr);
            }
            if (type > 2) { // 4 bytes? (need surrogates)
                if (ptr >= _inputEnd) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                }
                c = (int) _inputBuffer[ptr++];
                if ((c & 0xC0) != 0x080) {
                    reportInvalidOther(c & 0xFF, ptr);
                }
            }
        }
        return ptr;
    }
    */

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
    @Override
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
}
