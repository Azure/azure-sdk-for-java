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

package com.azure.xml.implementation.aalto.async;

import java.nio.ByteBuffer;
import java.util.Objects;

import javax.xml.stream.XMLStreamException;

import com.azure.xml.implementation.aalto.*;
import com.azure.xml.implementation.aalto.in.*;
import com.azure.xml.implementation.aalto.util.DataUtil;
//import com.azure.xml.implementation.aalto.util.XmlConsts;
import com.azure.xml.implementation.aalto.util.XmlCharTypes;

/**
 * This is the base class for asynchronous (non-blocking) XML
 * scanners. Due to basic complexity of async approach, character-based
 * doesn't make much sense, so only byte-based input is supported.
 */
public class AsyncByteBufferScanner extends AsyncByteScanner implements AsyncByteBufferFeeder {
    /*
    /**********************************************************************
    /* Input buffer handling
    /**********************************************************************
     */

    /**
     * This buffer is actually provided by caller
     */
    protected ByteBuffer _inputBuffer;

    /**
     * In addition to current buffer pointer, and end pointer,
     * we will also need to know number of bytes originally
     * contained. This is needed to correctly update location
     * information when the block has been completed.
     */
    protected int _origBufferLen;

    /*
    /**********************************************************************
    /* Instance construction
    /**********************************************************************
     */

    public AsyncByteBufferScanner(ReaderConfig cfg) {
        super(cfg);
        // must start by checking if there's XML declaration...
        _state = STATE_PROLOG_INITIAL;
        _currToken = EVENT_INCOMPLETE;
    }

    @Override
    public String toString() {
        return "asyncScanner; curr=" + _currToken + " next=" + _nextEvent + ", state = " + _state;
    }

    /*
    /**********************************************************************
    /* Implementation for low-level accessors
    /**********************************************************************
     */

    @Override
    protected final byte _currentByte() throws XMLStreamException {
        return _inputBuffer.get(_inputPtr);
    }

    @Override
    protected final byte _nextByte() throws XMLStreamException {
        return _inputBuffer.get(_inputPtr++);
    }

    @Override
    protected final byte _prevByte() throws XMLStreamException {
        return _inputBuffer.get(_inputPtr - 1);
    }

    /*
    /**********************************************************************
    /* Parsing, comments
    /**********************************************************************
     */

    protected int parseCommentContents() throws XMLStreamException {
        // Left-overs from last input block?
        if (_pendingInput != 0) { // CR, multi-byte, or '-'?
            int result = handleCommentPending();
            // If there's not enough input, or if we completed, can leave
            if (result != 0) {
                return result;
            }
            // otherwise we should be good to continue
        }

        char[] outputBuffer = _textBuilder.getBufferWithoutReset();
        int outPtr = _textBuilder.getCurrentLength();

        final int[] TYPES = _charTypes.OTHER_CHARS;
        ByteBuffer inputBuffer = _inputBuffer;

        main_loop: while (true) {
            int c;
            // Then the tight ASCII non-funny-char loop:
            ascii_loop: while (true) {
                if (_inputPtr >= _inputEnd) {
                    break main_loop;
                }
                if (outPtr >= outputBuffer.length) {
                    outputBuffer = _textBuilder.finishCurrentSegment();
                    outPtr = 0;
                }
                int max = _inputEnd;
                {
                    int max2 = _inputPtr + (outputBuffer.length - outPtr);
                    if (max2 < max) {
                        max = max2;
                    }
                }
                while (_inputPtr < max) {
                    c = (int) inputBuffer.get(_inputPtr++) & 0xFF;
                    if (TYPES[c] != 0) {
                        break ascii_loop;
                    }
                    outputBuffer[outPtr++] = (char) c;
                }
            }

            switch (TYPES[c]) {
                case XmlCharTypes.CT_INVALID:
                    handleInvalidXmlChar(c);
                case XmlCharTypes.CT_WS_CR: {
                    if (_inputPtr >= _inputEnd) {
                        _pendingInput = PENDING_STATE_CR;
                        break main_loop;
                    }
                    if (inputBuffer.get(_inputPtr) == BYTE_LF) {
                        ++_inputPtr;
                    }
                    markLF();
                }
                    c = INT_LF;
                    break;

                case XmlCharTypes.CT_WS_LF:
                    markLF();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_2:
                    if (_inputPtr >= _inputEnd) {
                        _pendingInput = c;
                        break main_loop;
                    }
                    c = decodeUtf8_2(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_3:
                    if ((_inputEnd - _inputPtr) < 2) {
                        if (_inputEnd > _inputPtr) { // 2 bytes available
                            int d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                            c |= (d << 8);
                        }
                        _pendingInput = c;
                        break main_loop;
                    }
                    c = decodeUtf8_3(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_4:
                    if ((_inputEnd - _inputPtr) < 3) {
                        if (_inputEnd > _inputPtr) { // at least 2 bytes?
                            int d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                            c |= (d << 8);
                            if (_inputEnd > _inputPtr) { // 3 bytes?
                                d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                                c |= (d << 16);
                            }
                        }
                        _pendingInput = c;
                        break main_loop;
                    }
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
                        _pendingInput = PENDING_STATE_COMMENT_HYPHEN1;
                        break main_loop;
                    }
                    if (_inputBuffer.get(_inputPtr) == BYTE_HYPHEN) { // ok, must be end then
                        ++_inputPtr;
                        if (_inputPtr >= _inputEnd) {
                            _pendingInput = PENDING_STATE_COMMENT_HYPHEN2;
                            break main_loop;
                        }
                        if (_inputBuffer.get(_inputPtr++) != BYTE_GT) {
                            reportDoubleHyphenInComments();
                        }
                        _textBuilder.setCurrentLength(outPtr);
                        _state = STATE_DEFAULT;
                        _nextEvent = EVENT_INCOMPLETE;
                        return COMMENT;
                    }
                    break;
                // default:
                // Other types are not important here...
            }

            // Ok, can output the char (we know there's room for one more)
            outputBuffer[outPtr++] = (char) c;
        }

        _textBuilder.setCurrentLength(outPtr);
        return EVENT_INCOMPLETE;
    }

    /**
     * @return EVENT_INCOMPLETE, if there's not enough input to
     *   handle pending char, COMMENT, if we handled complete
     *   "--&gt;" end marker, or 0 to indicate something else
     *   was successfully handled.
     */
    protected int handleCommentPending() throws XMLStreamException {
        if (_inputPtr >= _inputEnd) {
            return EVENT_INCOMPLETE;
        }
        if (_pendingInput == PENDING_STATE_COMMENT_HYPHEN1) {
            if (_inputBuffer.get(_inputPtr) != BYTE_HYPHEN) {
                // can't be the end marker, just append '-' and go
                _pendingInput = 0;
                _textBuilder.append("-");
                return 0;
            }
            ++_inputPtr;
            _pendingInput = PENDING_STATE_COMMENT_HYPHEN2;
            if (_inputPtr >= _inputEnd) { // no more input?
                return EVENT_INCOMPLETE;
            }
            // continue
        }
        if (_pendingInput == PENDING_STATE_COMMENT_HYPHEN2) {
            _pendingInput = 0;
            byte b = _inputBuffer.get(_inputPtr++);
            if (b != BYTE_GT) {
                reportDoubleHyphenInComments();
            }
            _state = STATE_DEFAULT;
            _nextEvent = EVENT_INCOMPLETE;
            return COMMENT;
        }
        // Otherwise can use default code
        return handleAndAppendPending() ? 0 : EVENT_INCOMPLETE;
    }

    /*
    /**********************************************************************
    /* Parsing, PI
    /**********************************************************************
     */

    protected int parsePIData() throws XMLStreamException {
        // Left-overs from last input block?
        if (_pendingInput != 0) { // CR, multi-byte, '?'
            int result = handlePIPending();
            // If there's not enough input, or if we completed, can leave
            if (result != 0) {
                return result;
            }
            // otherwise we should be good to continue
        }

        char[] outputBuffer = _textBuilder.getBufferWithoutReset();
        int outPtr = _textBuilder.getCurrentLength();

        final int[] TYPES = _charTypes.OTHER_CHARS;
        ByteBuffer inputBuffer = _inputBuffer;

        main_loop: while (true) {
            int c;
            // Then the tight ASCII non-funny-char loop:
            ascii_loop: while (true) {
                if (_inputPtr >= _inputEnd) {
                    break main_loop;
                }
                if (outPtr >= outputBuffer.length) {
                    outputBuffer = _textBuilder.finishCurrentSegment();
                    outPtr = 0;
                }
                int max = _inputEnd;
                {
                    int max2 = _inputPtr + (outputBuffer.length - outPtr);
                    if (max2 < max) {
                        max = max2;
                    }
                }
                while (_inputPtr < max) {
                    c = (int) inputBuffer.get(_inputPtr++) & 0xFF;
                    if (TYPES[c] != 0) {
                        break ascii_loop;
                    }
                    outputBuffer[outPtr++] = (char) c;
                }
            }

            switch (TYPES[c]) {
                case XmlCharTypes.CT_INVALID:
                    handleInvalidXmlChar(c);
                case XmlCharTypes.CT_WS_CR: {
                    if (_inputPtr >= _inputEnd) {
                        _pendingInput = PENDING_STATE_CR;
                        break main_loop;
                    }
                    if (inputBuffer.get(_inputPtr) == BYTE_LF) {
                        ++_inputPtr;
                    }
                    markLF();
                }
                    c = INT_LF;
                    break;

                case XmlCharTypes.CT_WS_LF:
                    markLF();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_2:
                    if (_inputPtr >= _inputEnd) {
                        _pendingInput = c;
                        break main_loop;
                    }
                    c = decodeUtf8_2(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_3:
                    if ((_inputEnd - _inputPtr) < 2) {
                        if (_inputEnd > _inputPtr) { // 2 bytes available
                            int d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                            c |= (d << 8);
                        }
                        _pendingInput = c;
                        break main_loop;
                    }
                    c = decodeUtf8_3(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_4:
                    if ((_inputEnd - _inputPtr) < 3) {
                        if (_inputEnd > _inputPtr) { // at least 2 bytes?
                            int d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                            c |= (d << 8);
                            if (_inputEnd > _inputPtr) { // 3 bytes?
                                d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                                c |= (d << 16);
                            }
                        }
                        _pendingInput = c;
                        break main_loop;
                    }
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
                        _pendingInput = PENDING_STATE_PI_QMARK;
                        break main_loop;
                    }
                    if (_inputBuffer.get(_inputPtr) == BYTE_GT) { // end
                        ++_inputPtr;
                        _textBuilder.setCurrentLength(outPtr);
                        _state = STATE_DEFAULT;
                        _nextEvent = EVENT_INCOMPLETE;
                        return PROCESSING_INSTRUCTION;
                    }
                    // Not end mark, just need to reprocess the second char
                    break;
                // default:
                // Other types are not important here...
            }

            // Ok, can output the char (we know there's room for one more)
            outputBuffer[outPtr++] = (char) c;
        }
        _textBuilder.setCurrentLength(outPtr);
        return EVENT_INCOMPLETE;
    }

    /**
     * @return EVENT_INCOMPLETE, if there's not enough input to
     *   handle pending char, PROCESSING_INSTRUCTION, if we handled complete
     *   "?&gt;" end marker, or 0 to indicate something else
     *   was succesfully handled.
     */
    protected int handlePIPending() throws XMLStreamException {
        // First, the special case, end marker:
        if (_pendingInput == PENDING_STATE_PI_QMARK) {
            if (_inputPtr >= _inputEnd) {
                return EVENT_INCOMPLETE;
            }
            byte b = _inputBuffer.get(_inputPtr);
            _pendingInput = 0;
            if (b != BYTE_GT) {
                // can't be the end marker, just append '-' and go
                _textBuilder.append('?');
                return 0;
            }
            ++_inputPtr;
            _state = STATE_DEFAULT;
            _nextEvent = EVENT_INCOMPLETE;
            return PROCESSING_INSTRUCTION;
        }
        // Otherwise can use default code
        return handleAndAppendPending() ? 0 : EVENT_INCOMPLETE;
    }

    /*
    /**********************************************************************
    /* Parsing, internal DTD subset
    /**********************************************************************
     */

    @Override
    protected final boolean handleDTDInternalSubset(boolean init) throws XMLStreamException {
        char[] outputBuffer;
        int outPtr;

        if (init) { // first time around
            outputBuffer = _textBuilder.resetWithEmpty();
            outPtr = 0;
            _elemAttrQuote = 0;
            _inDtdDeclaration = false;
        } else {
            if (_pendingInput != 0) {
                if (!handleAndAppendPending()) {
                    return false;
                }
            }
            outputBuffer = _textBuilder.getBufferWithoutReset();
            outPtr = _textBuilder.getCurrentLength();
        }

        final int[] TYPES = _charTypes.DTD_CHARS;
        ByteBuffer inputBuffer = _inputBuffer;

        main_loop: while (true) {
            int c;
            // Then the tight ASCII non-funny-char loop:
            ascii_loop: while (true) {
                if (_inputPtr >= _inputEnd) {
                    break main_loop;
                }
                if (outPtr >= outputBuffer.length) {
                    outputBuffer = _textBuilder.finishCurrentSegment();
                    outPtr = 0;
                }
                int max = _inputEnd;
                {
                    int max2 = _inputPtr + (outputBuffer.length - outPtr);
                    if (max2 < max) {
                        max = max2;
                    }
                }
                while (_inputPtr < max) {
                    c = (int) inputBuffer.get(_inputPtr++) & 0xFF;
                    if (TYPES[c] != 0) {
                        break ascii_loop;
                    }
                    outputBuffer[outPtr++] = (char) c;
                }
            }

            switch (TYPES[c]) {
                case XmlCharTypes.CT_INVALID:
                    handleInvalidXmlChar(c);
                case XmlCharTypes.CT_WS_CR:
                    if (_inputPtr >= _inputEnd) {
                        _pendingInput = PENDING_STATE_CR;
                        break main_loop;
                    }
                    if (inputBuffer.get(_inputPtr) == BYTE_LF) {
                        ++_inputPtr;
                    }
                    markLF();
                    c = INT_LF;
                    break;

                case XmlCharTypes.CT_WS_LF:
                    markLF();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_2:
                    if (_inputPtr >= _inputEnd) {
                        _pendingInput = c;
                        break main_loop;
                    }
                    c = decodeUtf8_2(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_3:
                    if ((_inputEnd - _inputPtr) < 2) {
                        if (_inputEnd > _inputPtr) { // 2 bytes available
                            int d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                            c |= (d << 8);
                        }
                        _pendingInput = c;
                        break main_loop;
                    }
                    c = decodeUtf8_3(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_4:
                    if ((_inputEnd - _inputPtr) < 3) {
                        if (_inputEnd > _inputPtr) { // at least 2 bytes?
                            int d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                            c |= (d << 8);
                            if (_inputEnd > _inputPtr) { // 3 bytes?
                                d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                                c |= (d << 16);
                            }
                        }
                        _pendingInput = c;
                        break main_loop;
                    }
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

                case XmlCharTypes.CT_DTD_QUOTE: // apos or quot
                    if (_elemAttrQuote == 0) {
                        _elemAttrQuote = (byte) c;
                    } else {
                        if (_elemAttrQuote == c) {
                            _elemAttrQuote = 0;
                        }
                    }
                    break;

                case XmlCharTypes.CT_DTD_LT:
                    if (!_inDtdDeclaration) {
                        _inDtdDeclaration = true;
                    }
                    break;

                case XmlCharTypes.CT_DTD_GT:
                    if (_elemAttrQuote == 0) {
                        _inDtdDeclaration = false;
                    }
                    break;

                case XmlCharTypes.CT_DTD_RBRACKET:
                    if (!_inDtdDeclaration && _elemAttrQuote == 0) {
                        _textBuilder.setCurrentLength(outPtr);
                        return true;
                    }
                    break;
                // default:
                // Other types are not important here...
            }
            // Ok, can output the char (we know there's room for one more)
            outputBuffer[outPtr++] = (char) c;
        }
        _textBuilder.setCurrentLength(outPtr);
        return false;
    }

    /*
    /**********************************************************************
    /* Parsing, CDATA
    /**********************************************************************
     */

    protected final int parseCDataContents() throws XMLStreamException {
        // Left-overs from last input block?
        if (_pendingInput != 0) { // CR, multi-byte, or ']'?
            int result = handleCDataPending();
            // If there's not enough input, or if we completed, can leave
            if (result != 0) {
                return result;
            }
            // otherwise we should be good to continue
        }
        char[] outputBuffer = _textBuilder.getBufferWithoutReset();
        int outPtr = _textBuilder.getCurrentLength();

        final int[] TYPES = _charTypes.OTHER_CHARS;
        ByteBuffer inputBuffer = _inputBuffer;

        main_loop: while (true) {
            int c;
            // Then the tight ASCII non-funny-char loop:
            ascii_loop: while (true) {
                if (_inputPtr >= _inputEnd) {
                    break main_loop;
                }
                if (outPtr >= outputBuffer.length) {
                    outputBuffer = _textBuilder.finishCurrentSegment();
                    outPtr = 0;
                }
                int max = _inputEnd;
                {
                    int max2 = _inputPtr + (outputBuffer.length - outPtr);
                    if (max2 < max) {
                        max = max2;
                    }
                }
                while (_inputPtr < max) {
                    c = (int) inputBuffer.get(_inputPtr++) & 0xFF;
                    if (TYPES[c] != 0) {
                        break ascii_loop;
                    }
                    outputBuffer[outPtr++] = (char) c;
                }
            }

            switch (TYPES[c]) {
                case XmlCharTypes.CT_INVALID:
                    handleInvalidXmlChar(c);
                case XmlCharTypes.CT_WS_CR: {
                    if (_inputPtr >= _inputEnd) {
                        _pendingInput = PENDING_STATE_CR;
                        break main_loop;
                    }
                    if (inputBuffer.get(_inputPtr) == BYTE_LF) {
                        ++_inputPtr;
                    }
                    markLF();
                }
                    c = INT_LF;
                    break;

                case XmlCharTypes.CT_WS_LF:
                    markLF();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_2:
                    if (_inputPtr >= _inputEnd) {
                        _pendingInput = c;
                        break main_loop;
                    }
                    c = decodeUtf8_2(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_3:
                    if ((_inputEnd - _inputPtr) < 2) {
                        if (_inputEnd > _inputPtr) { // 2 bytes available
                            int d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                            c |= (d << 8);
                        }
                        _pendingInput = c;
                        break main_loop;
                    }
                    c = decodeUtf8_3(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_4:
                    if ((_inputEnd - _inputPtr) < 3) {
                        if (_inputEnd > _inputPtr) { // at least 2 bytes?
                            int d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                            c |= (d << 8);
                            if (_inputEnd > _inputPtr) { // 3 bytes?
                                d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                                c |= (d << 16);
                            }
                        }
                        _pendingInput = c;
                        break main_loop;
                    }
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
                case XmlCharTypes.CT_RBRACKET: // ']]>'?
                    if (_inputPtr >= _inputEnd) {
                        _pendingInput = PENDING_STATE_CDATA_BRACKET1;
                        break main_loop;
                    }
                    // Hmmh. This is more complex... so be it.
                    if (_inputBuffer.get(_inputPtr) == BYTE_RBRACKET) { // end might be nigh...
                        ++_inputPtr;
                        while (true) {
                            if (_inputPtr >= _inputEnd) {
                                _pendingInput = PENDING_STATE_CDATA_BRACKET2;
                                break main_loop;
                            }
                            if (_inputBuffer.get(_inputPtr) == BYTE_GT) {
                                ++_inputPtr;
                                _textBuilder.setCurrentLength(outPtr);
                                _state = STATE_DEFAULT;
                                _nextEvent = EVENT_INCOMPLETE;
                                return CDATA;
                            }
                            if (_inputBuffer.get(_inputPtr) != BYTE_RBRACKET) { // neither '>' nor ']'; push "]]" back
                                outputBuffer[outPtr++] = ']';
                                if (outPtr >= outputBuffer.length) {
                                    outputBuffer = _textBuilder.finishCurrentSegment();
                                    outPtr = 0;
                                }
                                outputBuffer[outPtr++] = ']';
                                continue main_loop;
                            }
                            // Got third bracket; push one back, keep on checking
                            ++_inputPtr;
                            outputBuffer[outPtr++] = ']';
                            if (outPtr >= outputBuffer.length) {
                                outputBuffer = _textBuilder.finishCurrentSegment();
                                outPtr = 0;
                            }
                        }
                    }
                    break;
                // default:
                // Other types are not important here...
            }

            // Ok, can output the char (we know there's room for one more)
            outputBuffer[outPtr++] = (char) c;
        }

        _textBuilder.setCurrentLength(outPtr);
        return EVENT_INCOMPLETE;
    }

    /**
     * @return EVENT_INCOMPLETE, if there's not enough input to
     *   handle pending char, CDATA, if we handled complete
     *   "]]&gt;" end marker, or 0 to indicate something else
     *   was successfully handled.
     */
    protected final int handleCDataPending() throws XMLStreamException {
        if (_pendingInput == PENDING_STATE_CDATA_BRACKET1) {
            if (_inputPtr >= _inputEnd) {
                return EVENT_INCOMPLETE;
            }
            if (_inputBuffer.get(_inputPtr) != BYTE_RBRACKET) {
                // can't be the end marker, just append ']' and go
                _textBuilder.append(']');
                return (_pendingInput = 0);
            }
            ++_inputPtr;
            _pendingInput = PENDING_STATE_CDATA_BRACKET2;
            if (_inputPtr >= _inputEnd) { // no more input?
                return EVENT_INCOMPLETE;
            }
            // continue
        }
        while (_pendingInput == PENDING_STATE_CDATA_BRACKET2) {
            if (_inputPtr >= _inputEnd) {
                return EVENT_INCOMPLETE;
            }
            byte b = _inputBuffer.get(_inputPtr++);
            if (b == BYTE_GT) {
                _pendingInput = 0;
                _state = STATE_DEFAULT;
                _nextEvent = EVENT_INCOMPLETE;
                return CDATA;
            }
            if (b != BYTE_RBRACKET) {
                --_inputPtr;
                _textBuilder.append("]]");
                return (_pendingInput = 0);
            }
            _textBuilder.append(']');
        }
        // Otherwise can use default code
        return handleAndAppendPending() ? 0 : EVENT_INCOMPLETE;
    }

    /**
     * This method gets called, if the first character of a
     * CHARACTERS event could not be fully read (multi-byte,
     * split over buffer boundary). If so, there is some
     * pending data to be handled.
     */
    protected int startCharactersPending() throws XMLStreamException {
        // First, need to have at least one more byte:
        if (_inputPtr >= _inputEnd) {
            return EVENT_INCOMPLETE;
        }

        // K. So what was the type again?
        int c = _pendingInput;
        _pendingInput = 0;

        // Possible \r\n linefeed?
        if (c == PENDING_STATE_CR) {
            if (_inputBuffer.get(_inputPtr) == BYTE_LF) {
                ++_inputPtr;
            }
            markLF();
            _textBuilder.resetWithChar(CHAR_LF);
        } else {
            // Nah, a multi-byte UTF-8 char:

            // Let's just retest the first pending byte (in LSB):
            switch (_charTypes.TEXT_CHARS[c & 0xFF]) {
                case XmlCharTypes.CT_MULTIBYTE_2:
                    // Easy: must have just one byte, did get another one:
                    _textBuilder.resetWithChar((char) decodeUtf8_2(c));
                    break;

                case XmlCharTypes.CT_MULTIBYTE_3: {
                    // Ok... so do we have one or two pending bytes?
                    int next = _inputBuffer.get(_inputPtr++) & 0xFF;
                    int c2 = (c >> 8);
                    if (c2 == 0) { // just one; need two more
                        if (_inputPtr >= _inputEnd) { // but got only one
                            _pendingInput = c | (next << 8);
                            return EVENT_INCOMPLETE;
                        }
                        int c3 = _inputBuffer.get(_inputPtr++) & 0xFF;
                        c = decodeUtf8_3(c, next, c3);
                    } else { // had two, got one, bueno:
                        c = decodeUtf8_3((c & 0xFF), c2, next);
                    }
                    _textBuilder.resetWithChar((char) c);
                }
                    break;

                case XmlCharTypes.CT_MULTIBYTE_4: {
                    int next = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                    // Only had one?
                    if ((c >> 8) == 0) { // ok, so need 3 more
                        if (_inputPtr >= _inputEnd) { // just have 1
                            _pendingInput = c | (next << 8);
                            return EVENT_INCOMPLETE;
                        }
                        int c2 = _inputBuffer.get(_inputPtr++) & 0xFF;
                        if (_inputPtr >= _inputEnd) { // almost, got 2
                            _pendingInput = c | (next << 8) | (c2 << 16);
                            return EVENT_INCOMPLETE;
                        }
                        int c3 = _inputBuffer.get(_inputPtr++) & 0xFF;
                        c = decodeUtf8_4(c, next, c2, c3);
                    } else { // had two or three
                        int c2 = (c >> 8) & 0xFF;
                        int c3 = (c >> 16);

                        if (c3 == 0) { // just two
                            if (_inputPtr >= _inputEnd) { // one short
                                _pendingInput = c | (next << 16);
                                return EVENT_INCOMPLETE;
                            }
                            c3 = _inputBuffer.get(_inputPtr++) & 0xFF;
                            c = decodeUtf8_4((c & 0xFF), c2, next, c3);
                        } else { // had three, got last
                            c = decodeUtf8_4((c & 0xFF), c2, c3, next);
                        }
                    }
                }
                    // Need a surrogate pair, have to call from here:
                    _textBuilder.resetWithSurrogate(c);
                    return (_currToken = CHARACTERS);

                default: // should never occur:
                    throwInternal();
            }
        }

        // Great, we got it. Is that enough?
        if (_cfgCoalescing && !_cfgLazyParsing) {
            // In eager coalescing mode, must read it all
            return finishCharactersCoalescing();
        }
        _currToken = CHARACTERS;
        if (_cfgLazyParsing) {
            _tokenIncomplete = true;
        } else {
            finishCharacters();
        }
        return _currToken;
    }

    /**
     * TODO: Method not yet implemented
     */
    protected final int finishCharactersCoalescing() throws XMLStreamException {
        // First things first: any pending partial multi-bytes?
        if (_pendingInput != 0) {
            if (!handleAndAppendPending()) {
                return EVENT_INCOMPLETE;
            }
        }
        throw new UnsupportedOperationException();
        // !!! TBI
        //        return 0;
    }

    /*
    /**********************************************************************
    /* Async input, methods to feed (push) content to parse
    /**********************************************************************
     */

    @Override
    public final boolean needMoreInput() {
        return (_inputPtr >= _inputEnd) && !_endOfInput;
    }

    @Override
    public void feedInput(ByteBuffer buffer) throws XMLStreamException {
        // Must not have remaining input
        if (_inputPtr < _inputEnd) {
            throw new XMLStreamException("Still have " + (_inputEnd - _inputPtr) + " unread bytes");
        }
        // and shouldn't have been marked as end-of-input
        if (_endOfInput) {
            throw new XMLStreamException("Already closed, can not feed more input");
        }
        // Time to update pointers first
        _pastBytesOrChars += _origBufferLen;
        _rowStartOffset -= _origBufferLen;

        int start = buffer.position();
        int end = buffer.limit();

        // And then update buffer settings
        _inputBuffer = buffer;
        _inputPtr = start;
        _inputEnd = end;
        _origBufferLen = end - start;
    }

    /*
    /**********************************************************************
    /* Implementation of parsing API
    /**********************************************************************
     */

    @Override
    public int nextFromTree() throws XMLStreamException {
        // Had a fully complete event? Need to reset state:
        if (_currToken != EVENT_INCOMPLETE) {
            /* First, need to handle some complications arising from
             * empty elements, and namespace binding/unbinding:
             */
            if (_currToken == START_ELEMENT) {
                if (_isEmptyTag) {
                    --_depth;
                    // Important: do NOT overwrite start location, same as with START_ELEMENT
                    return (_currToken = END_ELEMENT);
                }
            } else if (_currToken == END_ELEMENT) {
                _currElem = _currElem.getParent();
                // Any namespace declarations that need to be unbound?
                while (_lastNsDecl != null && _lastNsDecl.getLevel() >= _depth) {
                    _lastNsDecl = _lastNsDecl.unbind();
                }
            }

            // keep track of where event started
            setStartLocation();

            /* Only CHARACTERS can remain incomplete: this happens if
             * first character is decoded, but coalescing mode is NOT
             * set. Skip can not therefore block, nor will add pending
             * input. Can also occur when we have run out of input
             */
            if (_tokenIncomplete) {
                if (!skipCharacters()) { // couldn't complete skipping
                    return EVENT_INCOMPLETE;
                }
                _tokenIncomplete = false;
            }
            _currToken = _nextEvent = EVENT_INCOMPLETE;
            _state = STATE_DEFAULT;
        }

        // Don't yet know the type?
        if (_nextEvent == EVENT_INCOMPLETE) {
            if (_state == STATE_DEFAULT) {
                /* We can only have pending input for (incomplete)
                 * CHARACTERS event.
                 */
                if (_pendingInput != 0) { // CR, or multi-byte?
                    _nextEvent = CHARACTERS;
                    return startCharactersPending();
                }
                if (_inputPtr >= _inputEnd) { // nothing we can do?
                    return _currToken; // i.e. EVENT_INCOMPLETE
                }
                byte b = _inputBuffer.get(_inputPtr++);
                if (b == BYTE_LT) { // root element, comment, proc instr?
                    _state = STATE_TREE_SEEN_LT;
                } else if (b == BYTE_AMP) {
                    _state = STATE_TREE_SEEN_AMP;
                } else {
                    _nextEvent = CHARACTERS;
                    return startCharacters(b);
                }
            }

            if (_inputPtr >= _inputEnd) {
                return _currToken; // i.e. EVENT_INCOMPLETE
            }
            if (_state == STATE_TREE_SEEN_LT) {
                // Ok, so we've just seen the less-than char...
                byte b = _inputBuffer.get(_inputPtr++);
                if (b == BYTE_EXCL) { // comment or CDATA
                    _state = STATE_TREE_SEEN_EXCL;
                } else if (b == BYTE_QMARK) {
                    _nextEvent = PROCESSING_INSTRUCTION;
                    _state = STATE_DEFAULT;
                    return handlePI();
                } else if (b == BYTE_SLASH) {
                    return handleEndElementStart();
                } else {
                    // Probably start element -- need to retain first char tho
                    return handleStartElementStart(b);
                }
            } else if (_state == STATE_TREE_SEEN_AMP) {
                return handleEntityStartingToken();
            } else if (_state == STATE_TREE_NAMED_ENTITY_START) {
                return handleNamedEntityStartingToken();
            } else if (_state == STATE_TREE_NUMERIC_ENTITY_START) {
                return handleNumericEntityStartingToken();
            }

            if (_state == STATE_TREE_SEEN_EXCL) {
                if (_inputPtr >= _inputEnd) {
                    return _currToken; // i.e. EVENT_INCOMPLETE
                }
                byte b = _inputBuffer.get(_inputPtr++);
                // Comment or CDATA?
                if (b == BYTE_HYPHEN) { // Comment
                    _nextEvent = COMMENT;
                    _state = STATE_DEFAULT;
                } else if (b == BYTE_LBRACKET) { // CDATA
                    _nextEvent = CDATA;
                    _state = STATE_DEFAULT;
                } else {
                    reportTreeUnexpChar(decodeCharForError(b),
                        " (expected either '-' for COMMENT or '[CDATA[' for CDATA section)");
                }
            } else {
                throwInternal();
            }
        }

        /* We know the type; event is usually partially processed
         * and needs to be completely read.
         */
        switch (_nextEvent) {
            case START_ELEMENT:
                return handleStartElement();

            case END_ELEMENT:
                return handleEndElement();

            case PROCESSING_INSTRUCTION:
                return handlePI();

            case COMMENT:
                return handleComment();

            case CDATA:
                return handleCData();

            case CHARACTERS:
                if (!_cfgLazyParsing) {
                    // !!! TBI: how would non-lazy mode work?
                    if (_cfgCoalescing) {
                        return finishCharactersCoalescing();
                    }
                }
                if (_pendingInput != 0) { // multi-byte, or CR without LF
                    return startCharactersPending();
                }
                // Otherwise, should not get here
                throwInternal();
                //        case ENTITY_REFERENCE:
        }
        return throwInternal(); // never gets here
    }

    /*
    /**********************************************************************
    /* Second-level parsing; character content (in tree)
    /**********************************************************************
     */

    private int handleCData() throws XMLStreamException {
        if (_state == STATE_CDATA_CONTENT) {
            return parseCDataContents();
        }
        if (_inputPtr >= _inputEnd) {
            return EVENT_INCOMPLETE;
        }
        return handleCDataStartMarker(_inputBuffer.get(_inputPtr++));
    }

    private int handleCDataStartMarker(byte b) throws XMLStreamException {
        switch (_state) {
            case STATE_DEFAULT:
                if (b != BYTE_C) {
                    reportTreeUnexpChar(decodeCharForError(b), " (expected 'C' for CDATA)");
                }
                _state = STATE_CDATA_C;
                if (_inputPtr >= _inputEnd) {
                    return EVENT_INCOMPLETE;
                }
                b = _inputBuffer.get(_inputPtr++);
                // fall through
            case STATE_CDATA_C:
                if (b != BYTE_D) {
                    reportTreeUnexpChar(decodeCharForError(b), " (expected 'D' for CDATA)");
                }
                _state = STATE_CDATA_CD;
                if (_inputPtr >= _inputEnd) {
                    return EVENT_INCOMPLETE;
                }
                b = _inputBuffer.get(_inputPtr++);
                // fall through
            case STATE_CDATA_CD:
                if (b != BYTE_A) {
                    reportTreeUnexpChar(decodeCharForError(b), " (expected 'A' for CDATA)");
                }
                _state = STATE_CDATA_CDA;
                if (_inputPtr >= _inputEnd) {
                    return EVENT_INCOMPLETE;
                }
                b = _inputBuffer.get(_inputPtr++);
                // fall through
            case STATE_CDATA_CDA:
                if (b != BYTE_T) {
                    reportTreeUnexpChar(decodeCharForError(b), " (expected 'T' for CDATA)");
                }
                _state = STATE_CDATA_CDAT;
                if (_inputPtr >= _inputEnd) {
                    return EVENT_INCOMPLETE;
                }
                b = _inputBuffer.get(_inputPtr++);
                // fall through
            case STATE_CDATA_CDAT:
                if (b != BYTE_A) {
                    reportTreeUnexpChar(decodeCharForError(b), " (expected 'A' for CDATA)");
                }
                _state = STATE_CDATA_CDATA;
                if (_inputPtr >= _inputEnd) {
                    return EVENT_INCOMPLETE;
                }
                b = _inputBuffer.get(_inputPtr++);
                // fall through
            case STATE_CDATA_CDATA:
                if (b != BYTE_LBRACKET) {
                    reportTreeUnexpChar(decodeCharForError(b), " (expected '[' for CDATA)");
                }
                _textBuilder.resetWithEmpty();
                _state = STATE_CDATA_CONTENT;
                if (_inputPtr >= _inputEnd) {
                    return EVENT_INCOMPLETE;
                }
                return parseCDataContents();
        }
        return throwInternal();
    }

    /*
    /**********************************************************************
    /* Second-level parsing; other (PI, Comment)
    /**********************************************************************
     */

    @Override
    protected int handlePI() throws XMLStreamException {
        // Most common case first:
        if (_state == STATE_PI_IN_DATA) {
            return parsePIData();
        }

        main_loop: while (true) {
            if (_inputPtr >= _inputEnd) {
                return EVENT_INCOMPLETE;
            }
            switch (_state) {
                case STATE_DEFAULT:
                    _tokenName = parseNewName(_inputBuffer.get(_inputPtr++));
                    if (_tokenName == null) {
                        _state = STATE_PI_IN_TARGET;
                        return EVENT_INCOMPLETE;
                    }
                    _state = STATE_PI_AFTER_TARGET;
                    checkPITargetName(_tokenName);
                    if (_inputPtr >= _inputEnd) {
                        return EVENT_INCOMPLETE;
                    }
                    // fall through
                case STATE_PI_AFTER_TARGET:
                // Need ws or "?>"
                {
                    byte b = _inputBuffer.get(_inputPtr++);
                    if (b == BYTE_QMARK) {
                        // Quick check, can we see '>' as well? All done, if so
                        if (_inputPtr < _inputEnd && _inputBuffer.get(_inputPtr) == BYTE_GT) {
                            ++_inputPtr;
                            break main_loop; // means we are done
                        }
                        // If not (whatever reason), let's move to check state
                        _state = STATE_PI_AFTER_TARGET_QMARK;
                        break;
                    }
                    if (b == BYTE_SPACE || b == BYTE_CR || b == BYTE_LF || b == BYTE_TAB) {
                        if (!asyncSkipSpace()) { // ran out of input?
                            _state = STATE_PI_AFTER_TARGET_WS;
                            return EVENT_INCOMPLETE;
                        }
                        _textBuilder.resetWithEmpty();
                        // Quick check, perhaps we'll see end marker?
                        if ((_inputPtr + 1) < _inputEnd
                            && _inputBuffer.get(_inputPtr) == BYTE_QMARK
                            && _inputBuffer.get(_inputPtr + 1) == BYTE_GT) {
                            _inputPtr += 2;
                            break main_loop; // means we are done
                        }
                        // If not, we'll move to 'data' portion of PI
                        _state = STATE_PI_IN_DATA;
                        return parsePIData();
                    }
                    // Otherwise, it's an error
                    reportMissingPISpace(decodeCharForError(b));
                }

                // fall through
                case STATE_PI_AFTER_TARGET_WS:
                    if (!asyncSkipSpace()) { // ran out of input?
                        return EVENT_INCOMPLETE;
                    }
                    // Can just move to "data" portion right away
                    _state = STATE_PI_IN_DATA;
                    _textBuilder.resetWithEmpty();
                    return parsePIData();

                case STATE_PI_AFTER_TARGET_QMARK: {
                    // Must get '>' following '?' we saw right after name
                    byte b = _inputBuffer.get(_inputPtr++);
                    // Otherwise, it's an error
                    if (b != BYTE_GT) {
                        reportMissingPISpace(decodeCharForError(b));
                    }
                }
                    // but if it's ok, we are done
                    break main_loop;

                case STATE_PI_IN_TARGET:
                    _tokenName = parsePName();
                    if (_tokenName == null) {
                        return EVENT_INCOMPLETE;
                    }
                    checkPITargetName(_tokenName);
                    _state = STATE_PI_AFTER_TARGET;
                    break;

                default:
                    return throwInternal();
            }
        }

        _state = STATE_DEFAULT;
        _nextEvent = EVENT_INCOMPLETE;
        return PROCESSING_INSTRUCTION;
    }

    @Override
    protected final int handleComment() throws XMLStreamException {
        if (_state == STATE_COMMENT_CONTENT) {
            return parseCommentContents();
        }
        if (_inputPtr >= _inputEnd) {
            return EVENT_INCOMPLETE;
        }
        byte b = _inputBuffer.get(_inputPtr++);

        if (_state == STATE_DEFAULT) {
            if (b != BYTE_HYPHEN) {
                reportTreeUnexpChar(decodeCharForError(b), " (expected '-' for COMMENT)");
            }
            _state = STATE_COMMENT_CONTENT;
            _textBuilder.resetWithEmpty();
            return parseCommentContents();
        }
        if (_state == STATE_COMMENT_HYPHEN2) {
            // We are almost done, just need to get '>' at the end
            if (b != BYTE_GT) {
                reportDoubleHyphenInComments();
            }
            _state = STATE_DEFAULT;
            _nextEvent = EVENT_INCOMPLETE;
            return COMMENT;
        }
        return throwInternal();
    }

    /*
    /**********************************************************************
    /* Second-level parsing; helper methods
    /**********************************************************************
     */

    /**
     * Method to skip whatever space can be skipped.
     *<p>
     * NOTE: if available content ends with a CR, method will set
     * <code>_pendingInput</code> to <code>PENDING_STATE_CR</code>.
     *
     * @return True, if was able to skip through the space and find
     *   a non-space byte; false if reached end-of-buffer
     */
    @Override
    protected boolean asyncSkipSpace() throws XMLStreamException {
        while (_inputPtr < _inputEnd) {
            byte b = _inputBuffer.get(_inputPtr);
            if ((b & 0xFF) > INT_SPACE) {
                // hmmmh. Shouldn't this be handled someplace else?
                if (_pendingInput == PENDING_STATE_CR) {
                    markLF();
                    _pendingInput = 0;
                }
                return true;
            }
            ++_inputPtr;
            if (b == BYTE_LF) {
                markLF();
            } else if (b == BYTE_CR) {
                if (_inputPtr >= _inputEnd) {
                    _pendingInput = PENDING_STATE_CR;
                    break;
                }
                if (_inputBuffer.get(_inputPtr) == BYTE_LF) {
                    ++_inputPtr;
                }
                markLF();
            } else if (b != BYTE_SPACE && b != BYTE_TAB) {
                throwInvalidSpace(b);
            }
        }
        return false;
    }

    /**
     * Method called when a new token (within tree) starts with an
     * entity.
     *
     * @return Type of event to return
     */
    protected int handleEntityStartingToken() throws XMLStreamException {
        _textBuilder.resetWithEmpty();
        byte b = _inputBuffer.get(_inputPtr++); // we know one is available
        if (b == BYTE_HASH) { // numeric character entity
            _textBuilder.resetWithEmpty();
            _state = STATE_TREE_NUMERIC_ENTITY_START;
            _pendingInput = PENDING_STATE_ENT_SEEN_HASH;
            if (_inputPtr >= _inputEnd) { // but no more content to parse yet
                return EVENT_INCOMPLETE;
            }
            return handleNumericEntityStartingToken();
        }
        PName n = parseNewEntityName(b);
        // null if incomplete; non-null otherwise
        if (n == null) {
            // Not sure if it's a char entity or general one; so we don't yet know type
            _state = STATE_TREE_NAMED_ENTITY_START;
            return EVENT_INCOMPLETE;
        }
        int ch = decodeGeneralEntity(n);
        if (ch == 0) { // not a character entity
            _tokenName = n;
            return (_nextEvent = _currToken = ENTITY_REFERENCE);
        }
        // character entity; initialize buffer,
        _textBuilder.resetWithChar((char) ch);
        _nextEvent = 0;
        _currToken = CHARACTERS;
        if (_cfgLazyParsing) {
            _tokenIncomplete = true;
        } else {
            finishCharacters();
        }
        return _currToken;
    }

    /**
     * Method called when we see an entity that is starting a new token,
     * and part of its name has been decoded (but not all)
     */
    protected int handleNamedEntityStartingToken() throws XMLStreamException {
        PName n = parseEntityName();
        // null if incomplete; non-null otherwise
        if (n == null) {
            return _nextEvent; // i.e. EVENT_INCOMPLETE
        }
        int ch = decodeGeneralEntity(n);
        if (ch == 0) { // not a character entity
            _tokenName = n;
            return (_currToken = ENTITY_REFERENCE);
        }
        // character entity; initialize buffer,
        _textBuilder.resetWithChar((char) ch);
        _nextEvent = 0;
        _currToken = CHARACTERS;
        if (_cfgLazyParsing) {
            _tokenIncomplete = true;
        } else {
            finishCharacters();
        }
        return _currToken;
    }

    /**
     * Method called to handle cases where we find something other than
     * a character entity (or one of 4 pre-defined general entities that
     * act like character entities)
     */
    protected int handleNumericEntityStartingToken() throws XMLStreamException {
        if (_pendingInput == PENDING_STATE_ENT_SEEN_HASH) {
            byte b = _inputBuffer.get(_inputPtr); // we know one is available
            _entityValue = 0;
            if (b == BYTE_x) { // 'x' marks hex
                _pendingInput = PENDING_STATE_ENT_IN_HEX_DIGIT;
                if (++_inputPtr >= _inputEnd) {
                    return EVENT_INCOMPLETE;
                }
            } else { // if not 'x', must be a digit
                _pendingInput = PENDING_STATE_ENT_IN_DEC_DIGIT;
                // let's just keep byte for calculation
            }
        }
        if (_pendingInput == PENDING_STATE_ENT_IN_HEX_DIGIT) {
            if (!decodeHexEntity()) {
                return EVENT_INCOMPLETE;
            }
        } else {
            if (!decodeDecEntity()) {
                return EVENT_INCOMPLETE;
            }
        }
        // and now we have the full value
        verifyAndAppendEntityCharacter(_entityValue);
        _currToken = CHARACTERS;
        if (_cfgLazyParsing) {
            _tokenIncomplete = true;
        } else {
            finishCharacters();
        }
        _pendingInput = 0;
        return _currToken;
    }

    /**
     * @return True if entity was decoded (and value assigned to <code>_entityValue</code>;
     *    false otherwise
     */
    protected final boolean decodeHexEntity() throws XMLStreamException {
        int value = _entityValue;
        while (_inputPtr < _inputEnd) {
            byte b = _inputBuffer.get(_inputPtr++);
            if (b == BYTE_SEMICOLON) {
                _entityValue = value;
                return true;
            }
            int ch = b;
            if (ch <= INT_9 && ch >= INT_0) {
                ch -= INT_0;
            } else if (ch <= INT_F && ch >= INT_A) {
                ch = 10 + (ch - INT_A);
            } else if (ch <= INT_f && ch >= INT_a) {
                ch = 10 + (ch - INT_a);
            } else {
                throwUnexpectedChar(decodeCharForError(b), " expected a hex digit (0-9a-fA-F) for character entity");
            }
            value = (value << 4) + ch;
            if (value > MAX_UNICODE_CHAR) { // Overflow?
                _entityValue = value;
                reportEntityOverflow();
            }
        }
        _entityValue = value;
        return false;
    }

    /**
     * @return True if entity was decoded (and value assigned to <code>_entityValue</code>;
     *    false otherwise
     */
    protected final boolean decodeDecEntity() throws XMLStreamException {
        int value = _entityValue;
        while (_inputPtr < _inputEnd) {
            byte b = _inputBuffer.get(_inputPtr++);
            if (b == BYTE_SEMICOLON) {
                _entityValue = value;
                return true;
            }
            int ch = ((int) b) - INT_0;
            if (ch < 0 || ch > 9) { // invalid entity
                throwUnexpectedChar(decodeCharForError(b), " expected a digit (0 - 9) for character entity");
            }
            value = (value * 10) + ch;
            if (value > MAX_UNICODE_CHAR) { // Overflow?
                _entityValue = value;
                reportEntityOverflow();
            }
        }
        _entityValue = value;
        return false;
    }

    /**
     * Method that verifies that given named entity is followed by
     * a semi-colon (meaning next byte must be available for reading);
     * and if so, whether it is one of pre-defined general entities.
     *
     * @return Character of the expanded pre-defined general entity
     *   (if name matches one); zero if not.
     */
    protected final int decodeGeneralEntity(PName entityName) throws XMLStreamException {
        // First things first: verify that we got semicolon afterwards
        byte b = _inputBuffer.get(_inputPtr++);
        if (b != BYTE_SEMICOLON) {
            throwUnexpectedChar(decodeCharForError(b),
                " expected ';' following entity name (\"" + entityName.getPrefixedName() + "\")");
        }

        String name = entityName.getPrefixedName();
        if (Objects.equals(name, "amp")) {
            return INT_AMP;
        }
        if (Objects.equals(name, "lt")) {
            return INT_LT;
        }
        if (Objects.equals(name, "apos")) {
            return INT_APOS;
        }
        if (Objects.equals(name, "quot")) {
            return INT_QUOTE;
        }
        if (Objects.equals(name, "gt")) {
            return INT_GT;
        }
        return 0;
    }

    /**
     * Method called when {@code '<'} and (what appears to be) a name
     * start character have been seen.
     */
    @Override
    protected int handleStartElementStart(byte b) throws XMLStreamException {
        PName elemName = parseNewName(b);
        _nextEvent = START_ELEMENT;
        if (elemName == null) {
            _state = STATE_SE_ELEM_NAME;
            return EVENT_INCOMPLETE;
        }
        initStartElement(elemName);
        return handleStartElement();
    }

    @Override
    protected int handleStartElement() throws XMLStreamException {
        main_loop: while (true) {
            if (_inputPtr >= _inputEnd) {
                return EVENT_INCOMPLETE;
            }

            byte b;
            int c;

            switch (_state) {
                case STATE_SE_ELEM_NAME: {
                    PName elemName = parsePName();
                    if (elemName == null) {
                        return EVENT_INCOMPLETE;
                    }
                    initStartElement(elemName);
                }
                    if (_inputPtr >= _inputEnd) {
                        return EVENT_INCOMPLETE;
                    }
                    // Fall through to next state

                case STATE_SE_SPACE_OR_END: // obligatory space, or end
                    if (_pendingInput != 0) {
                        if (!handlePartialCR()) {
                            return EVENT_INCOMPLETE;
                        }
                        // Ok, got a space, can move on
                    } else {
                        b = _inputBuffer.get(_inputPtr++);
                        c = (int) b & 0xFF;

                        if (c <= INT_SPACE) {
                            if (c == INT_LF) {
                                markLF();
                            } else if (c == INT_CR) {
                                if (_inputPtr >= _inputEnd) {
                                    _pendingInput = PENDING_STATE_CR;
                                    return EVENT_INCOMPLETE;
                                }
                                if (_inputBuffer.get(_inputPtr) == BYTE_LF) {
                                    ++_inputPtr;
                                }
                                markLF();
                            } else if (c != INT_SPACE && c != INT_TAB) {
                                throwInvalidSpace(c);
                            }
                        } else if (c == INT_GT) { // must be '/' or '>'
                            return finishStartElement(false);
                        } else if (c == INT_SLASH) {
                            _state = STATE_SE_SEEN_SLASH;
                            continue main_loop;
                        } else {
                            throwUnexpectedChar(decodeCharForError(b), " expected space, or '>' or \"/>\"");
                        }
                    }
                    _state = STATE_SE_SPACE_OR_ATTRNAME;
                    if (_inputPtr >= _inputEnd) {
                        return EVENT_INCOMPLETE;
                    }
                    // can fall through, again:

                case STATE_SE_SPACE_OR_ATTRNAME:
                case STATE_SE_SPACE_OR_EQ:
                case STATE_SE_SPACE_OR_ATTRVALUE:
                    // Common to these states is that there may be leading space(s),
                    // so let's see if any has to be skipped
                    if (_pendingInput != 0) {
                        if (!handlePartialCR()) {
                            return EVENT_INCOMPLETE;
                        }
                        if (_inputPtr >= _inputEnd) {
                            return EVENT_INCOMPLETE;
                        }
                    }
                    b = _inputBuffer.get(_inputPtr++);
                    c = (int) b & 0xFF;

                    while (c <= INT_SPACE) {
                        if (c == INT_LF) {
                            markLF();
                        } else if (c == INT_CR) {
                            if (_inputPtr >= _inputEnd) {
                                _pendingInput = PENDING_STATE_CR;
                                return EVENT_INCOMPLETE;
                            }
                            if (_inputBuffer.get(_inputPtr) == BYTE_LF) {
                                ++_inputPtr;
                            }
                            markLF();
                        } else if (c != INT_SPACE && c != INT_TAB) {
                            throwInvalidSpace(c);
                        }
                        if (_inputPtr >= _inputEnd) {
                            return EVENT_INCOMPLETE;
                        }
                        b = _inputBuffer.get(_inputPtr++);
                        c = (int) b & 0xFF;
                    }

                    switch (_state) {
                        case STATE_SE_SPACE_OR_ATTRNAME:
                            if (b == BYTE_SLASH) {
                                _state = STATE_SE_SEEN_SLASH;
                                continue main_loop;
                            }
                            if (b == BYTE_GT) {
                                return finishStartElement(false);
                            } {
                            PName n = parseNewName(b);
                            if (n == null) {
                                _state = STATE_SE_ATTR_NAME;
                                return EVENT_INCOMPLETE;
                            }
                            _state = STATE_SE_SPACE_OR_EQ;
                            _elemAttrName = n;
                        }
                            continue main_loop;

                        case STATE_SE_SPACE_OR_EQ:
                            if (b != BYTE_EQ) {
                                throwUnexpectedChar(decodeCharForError(b), " expected '='");
                            }
                            _state = STATE_SE_SPACE_OR_ATTRVALUE;
                            continue main_loop;

                        case STATE_SE_SPACE_OR_ATTRVALUE:
                            if (b != BYTE_QUOT && b != BYTE_APOS) {
                                throwUnexpectedChar(decodeCharForError(b), " Expected a quote");
                            }
                            initAttribute(b);
                            continue main_loop;

                        default:
                            throwInternal();
                    }

                case STATE_SE_ATTR_NAME: {
                    PName n = parsePName();
                    if (n == null) {
                        return EVENT_INCOMPLETE;
                    }
                    _elemAttrName = n;
                    _state = STATE_SE_SPACE_OR_EQ;
                }
                    break;

                case STATE_SE_ATTR_VALUE_NORMAL:
                    if (!handleAttrValue()) {
                        return EVENT_INCOMPLETE;
                    }
                    _state = STATE_SE_SPACE_OR_END;
                    break;

                case STATE_SE_ATTR_VALUE_NSDECL:
                    if (!handleNsDecl()) {
                        return EVENT_INCOMPLETE;
                    }
                    _state = STATE_SE_SPACE_OR_END;
                    break;

                case STATE_SE_SEEN_SLASH: {
                    b = _inputBuffer.get(_inputPtr++);
                    if (b != BYTE_GT) {
                        throwUnexpectedChar(decodeCharForError(b), " expected '>'");
                    }
                    return finishStartElement(true);
                }

                default:
                    throwInternal();
            }
        }
    }

    private void initStartElement(PName elemName) {
        String prefix = elemName.getPrefix();
        if (prefix == null) { // element in default ns
            _elemAllNsBound = true; // which need not be bound
        } else {
            elemName = bindName(elemName, prefix);
            _elemAllNsBound = elemName.isBound();
        }
        _tokenName = elemName;
        _currElem = new ElementScope(elemName, _currElem);
        _attrCount = 0;
        _currNsCount = 0;
        _elemAttrPtr = 0;
        _state = STATE_SE_SPACE_OR_END;
    }

    private void initAttribute(byte quoteChar) {
        _elemAttrQuote = quoteChar;

        PName attrName = _elemAttrName;
        String prefix = attrName.getPrefix();
        boolean nsDecl;

        if (prefix == null) { // can be default ns decl:
            nsDecl = (Objects.equals(attrName.getLocalName(), "xmlns"));
        } else {
            // May be a namespace decl though?
            if (prefix.equals("xmlns")) {
                nsDecl = true;
            } else {
                attrName = bindName(attrName, prefix);
                if (_elemAllNsBound) {
                    _elemAllNsBound = attrName.isBound();
                }
                nsDecl = false;
            }
        }

        if (nsDecl) {
            _state = STATE_SE_ATTR_VALUE_NSDECL;
            // Ns decls use name buffer transiently
            _elemNsPtr = 0;
            ++_currNsCount;
        } else {
            _state = STATE_SE_ATTR_VALUE_NORMAL;
            // Regular attributes are appended, shouldn't reset ptr
            _attrCollector.startNewValue(attrName, _elemAttrPtr);
        }
    }

    /**
     * Method called to wrap up settings when the whole start
     * (or empty) element has been parsed.
     */
    private int finishStartElement(boolean emptyTag) throws XMLStreamException {
        _isEmptyTag = emptyTag;

        // Note: this call also checks attribute uniqueness
        int act = _attrCollector.finishLastValue(_elemAttrPtr);
        if (act < 0) { // error, dup attr indicated by -1
            act = _attrCollector.getCount(); // let's get correct count
            reportInputProblem(_attrCollector.getErrorMsg());
        }
        _attrCount = act;
        ++_depth;

        /* Was there any prefix that wasn't bound prior to use?
         * That's legal, assuming declaration was found later on...
         * let's check
         */
        if (!_elemAllNsBound) {
            if (!_tokenName.isBound()) { // element itself unbound
                reportUnboundPrefix(_tokenName, false);
            }
            for (int i = 0, len = _attrCount; i < len; ++i) {
                PName attrName = _attrCollector.getName(i);
                if (!attrName.isBound()) {
                    reportUnboundPrefix(attrName, true);
                }
            }
        }

        return (_currToken = START_ELEMENT);
    }

    private int handleEndElementStart() throws XMLStreamException {
        --_depth;
        _tokenName = _currElem.getName();

        /* Ok, perhaps we can do this quickly? This works, if we
         * are expected to have the full name (plus one more byte
         * to indicate name end) in the current buffer:
         */
        int size = _tokenName.sizeInQuads();
        if ((_inputEnd - _inputPtr) < ((size << 2) + 1)) { // may need to load more
            _nextEvent = END_ELEMENT;
            _state = STATE_DEFAULT;
            _quadCount = _currQuad = _currQuadBytes = 0;
            /* No, need to take it slow. Can not yet give up, though,
             * without reading remainder of the buffer
             */
            return handleEndElement();
        }
        ByteBuffer buf = _inputBuffer;

        // First all full chunks of 4 bytes (if any)
        --size;
        for (int qix = 0; qix < size; ++qix) {
            int ptr = _inputPtr;
            int q = (buf.get(ptr) << 24) | ((buf.get(ptr + 1) & 0xFF) << 16) | ((buf.get(ptr + 2) & 0xFF) << 8)
                | ((buf.get(ptr + 3) & 0xFF));
            _inputPtr += 4;
            // match?
            if (q != _tokenName.getQuad(qix)) {
                reportUnexpectedEndTag(_tokenName.getPrefixedName());
            }
        }

        /* After which we can deal with the last entry: it's bit
         * tricky as we don't actually fully know byte length...
         */
        int lastQ = _tokenName.getQuad(size);
        int q = buf.get(_inputPtr++) & 0xFF;
        if (q != lastQ) { // need second byte?
            q = (q << 8) | (buf.get(_inputPtr++) & 0xFF);
            if (q != lastQ) { // need third byte?
                q = (q << 8) | (buf.get(_inputPtr++) & 0xFF);
                if (q != lastQ) { // need full 4 bytes?
                    q = (q << 8) | (buf.get(_inputPtr++) & 0xFF);
                    if (q != lastQ) { // still no match? failure!
                        reportUnexpectedEndTag(_tokenName.getPrefixedName());
                    }
                }
            }
        }
        // Trailing space?
        int i2 = _inputBuffer.get(_inputPtr++) & 0xFF;
        while (i2 <= INT_SPACE) {
            if (i2 == INT_LF) {
                markLF();
            } else if (i2 == INT_CR) {
                if (_inputPtr >= _inputEnd) {
                    _pendingInput = PENDING_STATE_CR;
                    _nextEvent = END_ELEMENT;
                    _state = STATE_EE_NEED_GT;
                    return EVENT_INCOMPLETE;
                }
                if (_inputBuffer.get(_inputPtr) == BYTE_LF) {
                    ++_inputPtr;
                }
                markLF();
            } else if (i2 != INT_SPACE && i2 != INT_TAB) {
                throwInvalidSpace(i2);
            }
            if (_inputPtr >= _inputEnd) {
                _nextEvent = END_ELEMENT;
                _state = STATE_EE_NEED_GT;
                return EVENT_INCOMPLETE;
            }
            i2 = _inputBuffer.get(_inputPtr++) & 0xFF;
        }
        if (i2 != INT_GT) {
            throwUnexpectedChar(decodeCharForError((byte) i2), " expected space or closing '>'");
        }
        return (_currToken = END_ELEMENT);
    }

    /**
     * This method is "slow" version of above, used when name of
     * the end element can split input buffer boundary
     */
    private int handleEndElement() throws XMLStreamException {
        if (_state == STATE_DEFAULT) { // parsing name
            final PName elemName = _tokenName;
            final int quadSize = elemName.sizeInQuads() - 1; // need to ignore last for now
            for (; _quadCount < quadSize; ++_quadCount) { // first, full quads
                for (; _currQuadBytes < 4; ++_currQuadBytes) {
                    if (_inputPtr >= _inputEnd) {
                        return EVENT_INCOMPLETE;
                    }
                    _currQuad = (_currQuad << 8) | (_inputBuffer.get(_inputPtr++) & 0xFF);
                }
                // match?
                if (_currQuad != elemName.getQuad(_quadCount)) {
                    reportUnexpectedEndTag(elemName.getPrefixedName());
                }
                _currQuad = _currQuadBytes = 0;
            }
            // So far so good! Now need to check the last quad:
            int lastQ = elemName.getLastQuad();

            while (true) {
                if (_inputPtr >= _inputEnd) {
                    return EVENT_INCOMPLETE;
                }
                int q = (_currQuad << 8);
                q |= (_inputBuffer.get(_inputPtr++) & 0xFF);
                _currQuad = q;
                if (q == lastQ) { // match
                    break;
                }
                if (++_currQuadBytes > 3) { // no match, error
                    reportUnexpectedEndTag(elemName.getPrefixedName());
                    break; // never gets here
                }
            }
            // Bueno. How about optional space, '>'?
            _state = STATE_EE_NEED_GT;
        } else if (_state != STATE_EE_NEED_GT) {
            throwInternal();
        }

        if (_pendingInput != 0) {
            if (!handlePartialCR()) {
                return EVENT_INCOMPLETE;
            }
            // it's ignorable ws
        }

        // Trailing space?
        while (true) {
            if (_inputPtr >= _inputEnd) {
                return EVENT_INCOMPLETE;
            }
            int i2 = _inputBuffer.get(_inputPtr++) & 0xFF;
            if (i2 <= INT_SPACE) {
                if (i2 == INT_LF) {
                    markLF();
                } else if (i2 == INT_CR) {
                    if (_inputPtr >= _inputEnd) {
                        _pendingInput = PENDING_STATE_CR;
                        return EVENT_INCOMPLETE;
                    }
                    if (_inputBuffer.get(_inputPtr) == BYTE_LF) {
                        ++_inputPtr;
                    }
                    markLF();
                } else if (i2 != INT_SPACE && i2 != INT_TAB) {
                    throwInvalidSpace(i2);
                }
                continue;
            }

            if (i2 != INT_GT) {
                throwUnexpectedChar(decodeCharForError((byte) i2), " expected space or closing '>'");
            }
            // Hah, done!
            return (_currToken = END_ELEMENT);
        }
    }

    /*
    /**********************************************************************
    /* Implementation of parsing API, character events
    /**********************************************************************
     */

    @Override
    protected final int startCharacters(byte b) throws XMLStreamException {
        dummy_loop: do { // dummy loop, to allow break
            int c = (int) b & 0xFF;
            switch (_charTypes.TEXT_CHARS[c]) {
                case XmlCharTypes.CT_INVALID:
                    handleInvalidXmlChar(c);
                case XmlCharTypes.CT_WS_CR:
                    /* Note: can not have pending input when this method
                     * is called. No need to check that (could assert)
                     */
                    if (_inputPtr >= _inputEnd) { // no more input available
                        _pendingInput = PENDING_STATE_CR;
                        return EVENT_INCOMPLETE;
                    }
                    if (_inputBuffer.get(_inputPtr) == BYTE_LF) {
                        ++_inputPtr;
                    }
                    markLF();
                    c = INT_LF;
                    break;

                case XmlCharTypes.CT_WS_LF:
                    markLF();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_2:
                    if (_inputPtr >= _inputEnd) {
                        _pendingInput = c;
                        return EVENT_INCOMPLETE;
                    }
                    c = decodeUtf8_2(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_3:
                    if ((_inputEnd - _inputPtr) < 2) {
                        if (_inputEnd > _inputPtr) { // 2 bytes available
                            int d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                            c |= (d << 8);
                        }
                        _pendingInput = c;
                        return EVENT_INCOMPLETE;
                    }
                    c = decodeUtf8_3(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_4:
                    if ((_inputEnd - _inputPtr) < 3) {
                        if (_inputEnd > _inputPtr) { // at least 2 bytes?
                            int d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                            c |= (d << 8);
                            if (_inputEnd > _inputPtr) { // 3 bytes?
                                d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                                c |= (d << 16);
                            }
                        }
                        _pendingInput = c;
                        return EVENT_INCOMPLETE;
                    }
                    c = decodeUtf8_4(c);
                    // Need a surrogate pair, have to call from here:
                    _textBuilder.resetWithSurrogate(c);
                    break dummy_loop;

                case XmlCharTypes.CT_MULTIBYTE_N:
                    reportInvalidInitial(c);
                    break;

                case XmlCharTypes.CT_LT: // should never get here
                case XmlCharTypes.CT_AMP: // - "" -
                    throwInternal();
                    break;

                case XmlCharTypes.CT_RBRACKET: // ']]>'?
                    // !!! TBI: check for "]]>"

                default:
                    break;
            }

            _textBuilder.resetWithChar((char) c);
        } while (false); // dummy loop, for break

        if (_cfgCoalescing && !_cfgLazyParsing) {
            // In eager coalescing mode, must read it all
            return finishCharactersCoalescing();
        }
        _currToken = CHARACTERS;
        if (_cfgLazyParsing) {
            _tokenIncomplete = true;
        } else {
            finishCharacters();
        }
        return _currToken;
    }

    /**
     * This method only gets called in non-coalescing mode; and if so,
     * needs to parse as many characters of the current text segment
     * from the current input block as possible.
     */
    @Override
    protected final void finishCharacters() throws XMLStreamException {
        /* Now: there should not usually be any pending input (as it's
         * handled when CHARACTERS segment started, and this method
         * only gets called exactly once)... but we may want to
         * revisit this subject when (if) coalescing mode is to be
         * tackled.
         */
        if (_pendingInput != 0) {
            // !!! TBI: needs to be changed for coalescing mode
            throwInternal();
        }

        final int[] TYPES = _charTypes.TEXT_CHARS;
        final ByteBuffer inputBuffer = _inputBuffer;
        char[] outputBuffer = _textBuilder.getBufferWithoutReset();
        // Should have just one code point (one or two chars). Assert?
        int outPtr = _textBuilder.getCurrentLength();

        main_loop: while (true) {
            int c;
            // Then the tight ASCII non-funny-char loop:
            ascii_loop: while (true) {
                int ptr = _inputPtr;
                if (ptr >= _inputEnd) {
                    break main_loop;
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
                    c = (int) inputBuffer.get(ptr++) & 0xFF;
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
                case XmlCharTypes.CT_WS_CR: {
                    if (_inputPtr >= _inputEnd) {
                        _pendingInput = PENDING_STATE_CR;
                        break main_loop;
                    }
                    if (inputBuffer.get(_inputPtr) == BYTE_LF) {
                        ++_inputPtr;
                    }
                    markLF();
                }
                    c = INT_LF;
                    break;

                case XmlCharTypes.CT_WS_LF:
                    markLF();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_2:
                    if (_inputPtr >= _inputEnd) {
                        _pendingInput = c;
                        break main_loop;
                    }
                    c = decodeUtf8_2(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_3:
                    if ((_inputEnd - _inputPtr) < 2) {
                        if (_inputEnd > _inputPtr) { // 2 bytes available
                            int d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                            c |= (d << 8);
                        }
                        _pendingInput = c;
                        break main_loop;
                    }
                    c = decodeUtf8_3(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_4:
                    if ((_inputEnd - _inputPtr) < 3) {
                        if (_inputEnd > _inputPtr) { // at least 2 bytes?
                            int d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                            c |= (d << 8);
                            if (_inputEnd > _inputPtr) { // 3 bytes?
                                d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                                c |= (d << 16);
                            }
                        }
                        _pendingInput = c;
                        break main_loop;
                    }
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
                    c = handleEntityInCharacters();
                    if (c == 0) { // not a succesfully expanded char entity
                        // _inputPtr set by entity expansion method
                        --_inputPtr;
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
                /* 09-Mar-2007, tatus: This will not give 100% coverage,
                 *  for it may be split across input buffer boundary.
                 *  For now this will have to suffice though.
                 */
                {
                    // Let's then just count number of brackets --
                    // in case they are not followed by '>'
                    int count = 1;
                    byte b = BYTE_NULL;
                    while (_inputPtr < _inputEnd) {
                        b = inputBuffer.get(_inputPtr);
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
                    while (--count > 0) {
                        outputBuffer[outPtr++] = ']';
                        // Need to ensure room for one more char
                        if (outPtr >= outputBuffer.length) {
                            outputBuffer = _textBuilder.finishCurrentSegment();
                            outPtr = 0;
                        }
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

    /**
     * Method called to handle entity encountered inside
     * CHARACTERS segment, when trying to complete a non-coalescing text segment.
     *<p>
     * NOTE: unlike with generic parsing of named entities, where trailing semicolon
     * needs to be left in place, here we should just process it right away.
     *
     * @return Expanded (character) entity, if positive number; 0 if incomplete.
     */
    protected int handleEntityInCharacters() throws XMLStreamException {
        /* Thing that simplifies processing here is that handling
         * is pretty much optional: if there isn't enough data, we
         * just return 0 and are done with it.
         *
         * Also: we need at least 3 more characters for any character entity
         */
        int ptr = _inputPtr;
        if ((ptr + 3) <= _inputEnd) {
            byte b = _inputBuffer.get(ptr++);
            if (b == BYTE_HASH) { // numeric character entity
                if (_inputBuffer.get(ptr) == BYTE_x) {
                    return handleHexEntityInCharacters(ptr + 1);
                }
                return handleDecEntityInCharacters(ptr);
            }
            // general entity; maybe one of pre-defined ones
            if (b == BYTE_a) { // amp or apos?
                b = _inputBuffer.get(ptr++);
                if (b == BYTE_m) {
                    if ((ptr + 1) < _inputEnd
                        && _inputBuffer.get(ptr) == BYTE_p
                        && _inputBuffer.get(ptr + 1) == BYTE_SEMICOLON) {
                        _inputPtr = ptr + 2;
                        return INT_AMP;
                    }
                } else if (b == BYTE_p) {
                    if ((ptr + 2) < _inputEnd
                        && _inputBuffer.get(ptr) == BYTE_o
                        && _inputBuffer.get(ptr + 1) == BYTE_s
                        && _inputBuffer.get(ptr + 2) == BYTE_SEMICOLON) {
                        _inputPtr = ptr + 3;
                        return INT_APOS;
                    }
                }
            } else if (b == BYTE_g) { // gt?
                if (_inputBuffer.get(ptr) == BYTE_t && _inputBuffer.get(ptr + 1) == BYTE_SEMICOLON) {
                    _inputPtr = ptr + 2;
                    return INT_GT;
                }
            } else if (b == BYTE_l) { // lt?
                if (_inputBuffer.get(ptr) == BYTE_t && _inputBuffer.get(ptr + 1) == BYTE_SEMICOLON) {
                    _inputPtr = ptr + 2;
                    return INT_LT;
                }
            } else if (b == BYTE_q) { // quot?
                if ((ptr + 3) < _inputEnd
                    && _inputBuffer.get(ptr) == BYTE_u
                    && _inputBuffer.get(ptr + 1) == BYTE_o
                    && _inputBuffer.get(ptr + 2) == BYTE_t
                    && _inputBuffer.get(ptr + 3) == BYTE_SEMICOLON) {
                    _inputPtr = ptr + 4;
                    return INT_QUOTE;
                }
            }
        }
        // couldn't handle:
        return 0;
    }

    protected int handleDecEntityInCharacters(int ptr) throws XMLStreamException {
        byte b = _inputBuffer.get(ptr++);
        final int end = _inputEnd;
        int value = 0;
        do {
            int ch = b;
            if (ch > INT_9 || ch < INT_0) {
                throwUnexpectedChar(decodeCharForError(b), " expected a digit (0 - 9) for character entity");
            }
            value = (value * 10) + (ch - INT_0);
            if (value > MAX_UNICODE_CHAR) { // Overflow?
                reportEntityOverflow();
            }
            if (ptr >= end) {
                return 0;
            }
            b = _inputBuffer.get(ptr++);
        } while (b != BYTE_SEMICOLON);
        _inputPtr = ptr;
        verifyXmlChar(value);
        return value;
    }

    protected int handleHexEntityInCharacters(int ptr) throws XMLStreamException {
        byte b = _inputBuffer.get(ptr++);
        final int end = _inputEnd;
        int value = 0;
        do {
            int ch = b;
            if (ch <= INT_9 && ch >= INT_0) {
                ch -= INT_0;
            } else if (ch <= INT_F && ch >= INT_A) {
                ch = 10 + (ch - INT_A);
            } else if (ch <= INT_f && ch >= INT_a) {
                ch = 10 + (ch - INT_a);
            } else {
                throwUnexpectedChar(decodeCharForError(b), " expected a hex digit (0-9a-fA-F) for character entity");
            }
            value = (value << 4) + ch;
            if (value > MAX_UNICODE_CHAR) { // Overflow?
                reportEntityOverflow();
            }
            if (ptr >= end) {
                return 0;
            }
            b = _inputBuffer.get(ptr++);
        } while (b != BYTE_SEMICOLON);
        _inputPtr = ptr;
        verifyXmlChar(value);
        return value;
    }

    /**
     * Method called to handle split multi-byte character, by decoding
     * it and appending to the text buffer, if possible.
     *
     * @return True, if split character was completely handled; false
     *    if not
     */
    private boolean handleAndAppendPending() throws XMLStreamException {
        // First, need to have at least one more byte:
        if (_inputPtr >= _inputEnd) {
            return false;
        }
        int c = _pendingInput;
        _pendingInput = 0;

        // Possible \r\n linefeed?
        if (c < 0) { // markers are all negative
            if (c == PENDING_STATE_CR) {
                if (_inputBuffer.get(_inputPtr) == BYTE_LF) {
                    ++_inputPtr;
                }
                markLF();
                _textBuilder.append(CHAR_LF);
                return true;
            }
            throwInternal();
        }

        // Nah, a multi-byte UTF-8 char:
        // Let's just re-test the first pending byte (in LSB):
        switch (_charTypes.TEXT_CHARS[c & 0xFF]) {
            case XmlCharTypes.CT_MULTIBYTE_2:
                // Easy: must have just one byte, did get another one:
                _textBuilder.append((char) decodeUtf8_2(c));
                break;

            case XmlCharTypes.CT_MULTIBYTE_3: {
                // Ok... so do we have one or two pending bytes?
                int next = _inputBuffer.get(_inputPtr++) & 0xFF;
                int c2 = (c >> 8);
                if (c2 == 0) { // just one; need two more
                    if (_inputPtr >= _inputEnd) { // but got only one
                        _pendingInput = c | (next << 8);
                        return false;
                    }
                    int c3 = _inputBuffer.get(_inputPtr++) & 0xFF;
                    c = decodeUtf8_3(c, next, c3);
                } else { // had two, got one, bueno:
                    c = decodeUtf8_3((c & 0xFF), c2, next);
                }
                _textBuilder.append((char) c);
            }
                break;

            case XmlCharTypes.CT_MULTIBYTE_4: {
                int next = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                // Only had one?
                if ((c >> 8) == 0) { // ok, so need 3 more
                    if (_inputPtr >= _inputEnd) { // just have 1
                        _pendingInput = c | (next << 8);
                        return false;
                    }
                    int c2 = _inputBuffer.get(_inputPtr++) & 0xFF;
                    if (_inputPtr >= _inputEnd) { // almost, got 2
                        _pendingInput = c | (next << 8) | (c2 << 16);
                        return false;
                    }
                    int c3 = _inputBuffer.get(_inputPtr++) & 0xFF;
                    c = decodeUtf8_4(c, next, c2, c3);
                } else { // had two or three
                    int c2 = (c >> 8) & 0xFF;
                    int c3 = (c >> 16);

                    if (c3 == 0) { // just two
                        if (_inputPtr >= _inputEnd) { // one short
                            _pendingInput = c | (next << 16);
                            return false;
                        }
                        c3 = _inputBuffer.get(_inputPtr++) & 0xFF;
                        c = decodeUtf8_4((c & 0xFF), c2, next, c3);
                    } else { // had three, got last
                        c = decodeUtf8_4((c & 0xFF), c2, c3, next);
                    }
                }
            }
                // Need a surrogate pair, have to call from here:
                _textBuilder.appendSurrogate(c);
                break;

            default: // should never occur:
                throwInternal();
        }
        return true;
    }

    /*
    /**********************************************************************
    /* Implementation of parsing API, skipping remainder CHARACTERS section
    /**********************************************************************
     */

    /**
     * Method that will be called to skip all possible characters
     * from the input buffer, but without blocking. Partial
     * characters are not to be handled (not pending input
     * is to be added).
     *
     * @return True, if skipping ending with an unexpanded
     *   entity; false if not
     */
    @Override
    protected boolean skipCharacters() throws XMLStreamException {
        if (_pendingInput != 0) {
            if (!skipPending()) {
                return false;
            }
        }

        final int[] TYPES = _charTypes.TEXT_CHARS;
        final ByteBuffer inputBuffer = _inputBuffer;

        main_loop: while (true) {
            int c;

            ascii_loop: while (true) {
                int ptr = _inputPtr;
                int max = _inputEnd;
                if (ptr >= max) {
                    break main_loop;
                }
                while (ptr < max) {
                    c = (int) inputBuffer.get(ptr++) & 0xFF;
                    if (TYPES[c] != 0) {
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                }
                _inputPtr = ptr;
            }
            // And then fallback for funny chars / UTF-8 multibytes:
            switch (TYPES[c]) {
                case XmlCharTypes.CT_INVALID:
                    handleInvalidXmlChar(c);
                case XmlCharTypes.CT_WS_CR: {
                    if (_inputPtr >= _inputEnd) {
                        _pendingInput = PENDING_STATE_CR;
                        break main_loop;
                    }
                    if (inputBuffer.get(_inputPtr) == BYTE_LF) {
                        ++_inputPtr;
                    }
                    markLF();
                }
                    break;

                case XmlCharTypes.CT_WS_LF:
                    markLF();
                    break;

                case XmlCharTypes.CT_MULTIBYTE_2:
                    if (_inputPtr >= _inputEnd) {
                        _pendingInput = c;
                        break main_loop;
                    }
                    skipUtf8_2(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_3:
                    if ((_inputEnd - _inputPtr) < 2) {
                        if (_inputEnd > _inputPtr) { // 2 bytes available
                            int d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                            c |= (d << 8);
                        }
                        _pendingInput = c;
                        break main_loop;
                    }
                    decodeUtf8_3(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_4:
                    if ((_inputEnd - _inputPtr) < 3) {
                        if (_inputEnd > _inputPtr) { // at least 2 bytes?
                            int d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                            c |= (d << 8);
                            if (_inputEnd > _inputPtr) { // 3 bytes?
                                d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                                c |= (d << 16);
                            }
                        }
                        _pendingInput = c;
                        break main_loop;
                    }
                    decodeUtf8_4(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_N:
                    reportInvalidInitial(c);
                case XmlCharTypes.CT_LT:
                    --_inputPtr;
                    return true;

                case XmlCharTypes.CT_AMP:
                    c = skipEntityInCharacters();
                    if (c == 0) { // not a successfully expanded char entity
                        _pendingInput = PENDING_STATE_TEXT_AMP;
                        // but we may have input to skip nonetheless..
                        if (_inputPtr < _inputEnd) {
                            if (skipPending()) {
                                return true;
                            }
                        }
                        return false;
                    }
                    break;

                case XmlCharTypes.CT_RBRACKET: // ']]>'?
                /* !!! 09-Mar-2007, tatu: This will not give 100% coverage,
                 *  for it may be split across input buffer boundary.
                 *  For now this will have to suffice though.
                 */
                {
                    // Let's then just count number of brackets --
                    // in case they are not followed by '>'
                    int count = 1;
                    byte b = BYTE_NULL;
                    while (_inputPtr < _inputEnd) {
                        b = inputBuffer.get(_inputPtr);
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

        // Ran out of input, no entity encountered
        return false;
    }

    private boolean skipPending() throws XMLStreamException {
        // First, need to have at least one more byte:
        if (_inputPtr >= _inputEnd) {
            return false;
        }

        // Possible \r\n linefeed?
        if (_pendingInput < 0) { // markers are all negative
            while (true) {
                switch (_pendingInput) {
                    case PENDING_STATE_CR:
                        _pendingInput = 0;
                        if (_inputBuffer.get(_inputPtr) == BYTE_LF) {
                            ++_inputPtr;
                        }
                        markLF();
                        return true;

                    case PENDING_STATE_TEXT_AMP: {
                        byte b = _inputBuffer.get(_inputPtr++);
                        if (b == BYTE_HASH) {
                            _pendingInput = PENDING_STATE_TEXT_AMP_HASH;
                            break;
                        }
                        PName n = parseNewEntityName(b);
                        if (n == null) {
                            _pendingInput = PENDING_STATE_TEXT_IN_ENTITY;
                            return false;
                        }
                        int ch = decodeGeneralEntity(n);
                        if (ch == 0) {
                            _tokenName = n;
                            _nextEvent = ENTITY_REFERENCE;
                        }
                    }
                        _pendingInput = 0;
                        return true; // no matter what, we are done

                    case PENDING_STATE_TEXT_AMP_HASH:
                        _entityValue = 0;
                        if (_inputBuffer.get(_inputPtr) == BYTE_x) {
                            ++_inputPtr;
                            if (decodeHexEntity()) {
                                _pendingInput = 0;
                                return true;
                            }
                            _pendingInput = PENDING_STATE_TEXT_HEX_ENTITY;
                            return false;
                        }
                        if (decodeDecEntity()) {
                            _pendingInput = 0;
                            return true;
                        }
                        _pendingInput = PENDING_STATE_TEXT_DEC_ENTITY;
                        return false;

                    case PENDING_STATE_TEXT_DEC_ENTITY:
                        if (decodeDecEntity()) {
                            _pendingInput = 0;
                            return true;
                        }
                        return false;

                    case PENDING_STATE_TEXT_HEX_ENTITY:
                        if (decodeHexEntity()) {
                            _pendingInput = 0;
                            return true;
                        }
                        return false;

                    case PENDING_STATE_TEXT_IN_ENTITY: {
                        PName n = parseEntityName();
                        if (n == null) {
                            return false;
                        }
                        int ch = decodeGeneralEntity(n);
                        if (ch == 0) {
                            _tokenName = n;
                            _nextEvent = ENTITY_REFERENCE;
                        }
                    }
                        _pendingInput = 0;
                        return true;

                    case PENDING_STATE_TEXT_BRACKET1:
                        if (_inputBuffer.get(_inputPtr) != BYTE_RBRACKET) {
                            _pendingInput = 0;
                            return true;
                        }
                        ++_inputPtr;
                        _pendingInput = PENDING_STATE_TEXT_BRACKET2;
                        break;

                    case PENDING_STATE_TEXT_BRACKET2:
                    // may get sequence...
                    {
                        byte b = _inputBuffer.get(_inputPtr);
                        if (b == BYTE_RBRACKET) {
                            ++_inputPtr;
                            break;
                        }
                        if (b == BYTE_GT) { // problem!
                            ++_inputPtr;
                            reportInputProblem("Encountered ']]>' in text segment");
                        }
                    }
                        // nope, something else, reprocess
                        _pendingInput = 0;
                        return true;

                    default:
                        throwInternal();
                }

                if (_inputPtr >= _inputEnd) {
                    return false;
                }
            }
        }

        // Nah, a multi-byte UTF-8 char:
        // Let's just re-test the first pending byte (in LSB):
        int c = _pendingInput;
        switch (_charTypes.TEXT_CHARS[c & 0xFF]) {
            case XmlCharTypes.CT_MULTIBYTE_2:
                // Easy: must have just one byte, did get another one:
                skipUtf8_2(c);
                break;

            case XmlCharTypes.CT_MULTIBYTE_3: {
                // Ok... so do we have one or two pending bytes?
                int next = _inputBuffer.get(_inputPtr++) & 0xFF;
                int c2 = (c >> 8);
                if (c2 == 0) { // just one; need two more
                    if (_inputPtr >= _inputEnd) { // but got only one
                        _pendingInput = c | (next << 8);
                        return false;
                    }
                    int c3 = _inputBuffer.get(_inputPtr++) & 0xFF;
                    decodeUtf8_3(c, next, c3);
                } else { // had two, got one, bueno:
                    decodeUtf8_3((c & 0xFF), c2, next);
                }
            }
                break;

            case XmlCharTypes.CT_MULTIBYTE_4: {
                int next = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                // Only had one?
                if ((c >> 8) == 0) { // ok, so need 3 more
                    if (_inputPtr >= _inputEnd) { // just have 1
                        _pendingInput = c | (next << 8);
                        return false;
                    }
                    int c2 = _inputBuffer.get(_inputPtr++) & 0xFF;
                    if (_inputPtr >= _inputEnd) { // almost, got 2
                        _pendingInput = c | (next << 8) | (c2 << 16);
                        return false;
                    }
                    int c3 = _inputBuffer.get(_inputPtr++) & 0xFF;
                    decodeUtf8_4(c, next, c2, c3);
                } else { // had two or three
                    int c2 = (c >> 8) & 0xFF;
                    int c3 = (c >> 16);

                    if (c3 == 0) { // just two
                        if (_inputPtr >= _inputEnd) { // one short
                            _pendingInput = c | (next << 16);
                            return false;
                        }
                        c3 = _inputBuffer.get(_inputPtr++) & 0xFF;
                        decodeUtf8_4((c & 0xFF), c2, next, c3);
                    } else { // had three, got last
                        decodeUtf8_4((c & 0xFF), c2, c3, next);
                    }
                }
            }
                break;

            default: // should never occur:
                throwInternal();
        }
        _pendingInput = 0;
        return true;
    }

    /**
     * Method called to handle entity encountered inside
     * CHARACTERS segment, when trying to complete a non-coalescing text segment.
     *
     * @return Expanded (character) entity, if positive number; 0 if incomplete.
     */
    private int skipEntityInCharacters() throws XMLStreamException {
        /* Thing that simplifies processing here is that handling
         * is pretty much optional: if there isn't enough data, we
         * just return 0 and are done with it.
         *
         * Also: we need at least 3 more characters for any character entity
         */
        int ptr = _inputPtr;
        if ((ptr + 3) <= _inputEnd) {
            byte b = _inputBuffer.get(ptr++);
            if (b == BYTE_HASH) { // numeric character entity
                if (_inputBuffer.get(ptr) == BYTE_x) {
                    return handleHexEntityInCharacters(ptr + 1);
                }
                return handleDecEntityInCharacters(ptr);
            }
            // general entity; maybe one of pre-defined ones
            if (b == BYTE_a) { // amp or apos?
                b = _inputBuffer.get(ptr++);
                if (b == BYTE_m) {
                    if ((ptr + 1) < _inputEnd
                        && _inputBuffer.get(ptr) == BYTE_p
                        && _inputBuffer.get(ptr + 1) == BYTE_SEMICOLON) {
                        _inputPtr = ptr + 2; // NOTE: do skip semicolon as well
                        return INT_AMP;
                    }
                } else if (b == BYTE_p) {
                    if ((ptr + 2) < _inputEnd
                        && _inputBuffer.get(ptr) == BYTE_o
                        && _inputBuffer.get(ptr + 1) == BYTE_s
                        && _inputBuffer.get(ptr + 2) == BYTE_SEMICOLON) {
                        _inputPtr = ptr + 3;
                        return INT_APOS;
                    }
                }
            } else if (b == BYTE_g) { // gt?
                if (_inputBuffer.get(ptr) == BYTE_t && _inputBuffer.get(ptr + 1) == BYTE_SEMICOLON) {
                    _inputPtr = ptr + 2;
                    return INT_GT;
                }
            } else if (b == BYTE_l) { // lt?
                if (_inputBuffer.get(ptr) == BYTE_t && _inputBuffer.get(ptr + 1) == BYTE_SEMICOLON) {
                    _inputPtr = ptr + 2;
                    return INT_LT;
                }
            } else if (b == BYTE_q) { // quot?
                if ((ptr + 3) < _inputEnd
                    && _inputBuffer.get(ptr) == BYTE_u
                    && _inputBuffer.get(ptr + 1) == BYTE_o
                    && _inputBuffer.get(ptr + 2) == BYTE_t
                    && _inputBuffer.get(ptr + 3) == BYTE_SEMICOLON) {
                    _inputPtr = ptr + 4;
                    return INT_QUOTE;
                }
            }
        }
        // couldn't handle:
        return 0;
    }

    /**
     * Coalescing mode is (and will) not be implemented for non-blocking
     * parsers, so this method should never get called.
     */
    @Override
    protected boolean skipCoalescedText() throws XMLStreamException {
        throwInternal();
        return false;
    }

    /*
    /**********************************************************************
    /* Implementation of parsing API, element/attr events
    /**********************************************************************
     */

    /**
     * @return True, if the whole value was read; false if
     *   only part (due to buffer ending)
     */
    @Override
    protected boolean handleAttrValue() throws XMLStreamException {
        // First; any pending input?
        if (_pendingInput != 0) {
            if (!handleAttrValuePending()) {
                return false;
            }
            _pendingInput = 0;
        }

        char[] attrBuffer = _attrCollector.continueValue();
        final int[] TYPES = _charTypes.ATTR_CHARS;
        final int quoteChar = _elemAttrQuote;

        value_loop: while (true) {
            int c;

            ascii_loop: while (true) {
                if (_inputPtr >= _inputEnd) {
                    return false;
                }
                if (_elemAttrPtr >= attrBuffer.length) {
                    attrBuffer = _attrCollector.valueBufferFull();
                }
                int max = _inputEnd;
                {
                    int max2 = _inputPtr + (attrBuffer.length - _elemAttrPtr);
                    if (max2 < max) {
                        max = max2;
                    }
                }
                while (_inputPtr < max) {
                    c = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                    if (TYPES[c] != 0) {
                        break ascii_loop;
                    }
                    attrBuffer[_elemAttrPtr++] = (char) c;
                }
            }

            switch (TYPES[c]) {
                case XmlCharTypes.CT_INVALID:
                    handleInvalidXmlChar(c);
                case XmlCharTypes.CT_WS_CR:
                    if (_inputPtr >= _inputEnd) {
                        _pendingInput = PENDING_STATE_CR;
                        return false;
                    }
                    if (_inputBuffer.get(_inputPtr) == BYTE_LF) {
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
                    if (_inputPtr >= _inputEnd) {
                        _pendingInput = c;
                        return false;
                    }
                    c = decodeUtf8_2(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_3:
                    if ((_inputEnd - _inputPtr) < 2) {
                        if (_inputEnd > _inputPtr) { // 2 bytes available
                            int d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                            c |= (d << 8);
                        }
                        _pendingInput = c;
                        return false;
                    }
                    c = decodeUtf8_3(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_4:
                    if ((_inputEnd - _inputPtr) < 3) {
                        if (_inputEnd > _inputPtr) { // at least 2 bytes?
                            int d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                            c |= (d << 8);
                            if (_inputEnd > _inputPtr) { // 3 bytes?
                                d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                                c |= (d << 16);
                            }
                        }
                        _pendingInput = c;
                        return false;
                    }
                    c = decodeUtf8_4(c);
                    // Let's add first part right away:
                    attrBuffer[_elemAttrPtr++] = (char) (0xD800 | (c >> 10));
                    c = 0xDC00 | (c & 0x3FF);
                    if (_elemAttrPtr >= attrBuffer.length) {
                        attrBuffer = _attrCollector.valueBufferFull();
                    }
                    break;

                case XmlCharTypes.CT_MULTIBYTE_N:
                    reportInvalidInitial(c);
                case XmlCharTypes.CT_LT:
                    throwUnexpectedChar(c, "'<' not allowed in attribute value");
                case XmlCharTypes.CT_AMP:
                    c = handleEntityInAttributeValue();
                    if (c <= 0) { // general entity; should never happen
                        if (c < 0) { // end-of-input
                            return false;
                        }
                        reportUnexpandedEntityInAttr(_elemAttrName, false);
                    }
                    // Ok; does it need a surrogate though? (over 16 bits)
                    if ((c >> 16) != 0) {
                        c -= 0x10000;
                        attrBuffer[_elemAttrPtr++] = (char) (0xD800 | (c >> 10));
                        c = 0xDC00 | (c & 0x3FF);
                        if (_elemAttrPtr >= attrBuffer.length) {
                            attrBuffer = _attrCollector.valueBufferFull();
                        }
                    }
                    break;

                case XmlCharTypes.CT_ATTR_QUOTE:
                    if (c == quoteChar) {
                        break value_loop;
                    }

                    // default:
                    // Other chars are not important here...
            }
            // We know there's room for at least one char without checking
            attrBuffer[_elemAttrPtr++] = (char) c;
        }

        return true; // yeah, we're done!
    }

    /**
     * @return True if the partial information was succesfully handled;
     *    false if not
     */
    private boolean handleAttrValuePending() throws XMLStreamException {
        if (_pendingInput == PENDING_STATE_CR) {
            if (!handlePartialCR()) {
                return false;
            }
            char[] attrBuffer = _attrCollector.continueValue();
            if (_elemAttrPtr >= attrBuffer.length) {
                attrBuffer = _attrCollector.valueBufferFull();
            }
            // All LFs get converted to spaces, in attribute values
            attrBuffer[_elemAttrPtr++] = ' ';
            return true;
        }
        // otherwise must be related to entity handling within attribute value
        if (_inputPtr >= _inputEnd) {
            return false;
        }
        int ch;

        if (_pendingInput == PENDING_STATE_ATTR_VALUE_AMP) {
            byte b = _inputBuffer.get(_inputPtr++);
            if (b == BYTE_HASH) { // numeric character entity
                _pendingInput = PENDING_STATE_ATTR_VALUE_AMP_HASH;
                if (_inputPtr >= _inputEnd) {
                    return false;
                }
                if (_inputBuffer.get(_inputPtr) == BYTE_x) {
                    _pendingInput = PENDING_STATE_ATTR_VALUE_AMP_HASH_X;
                    ++_inputPtr;
                    if (_inputPtr >= _inputEnd) {
                        return false;
                    }
                    ch = handleHexEntityInAttribute(true);
                } else {
                    ch = handleDecEntityInAttribute(true);
                }
            } else {
                PName entityName = parseNewEntityName(b);
                if (entityName == null) {
                    _pendingInput = PENDING_STATE_ATTR_VALUE_ENTITY_NAME;
                    return false;
                }
                ch = decodeGeneralEntity(entityName);
                if (ch == 0) { // can't have general entities within attribute values
                    _tokenName = entityName;
                    reportUnexpandedEntityInAttr(_elemAttrName, false);
                }
            }
        } else if (_pendingInput == PENDING_STATE_ATTR_VALUE_AMP_HASH) {
            if (_inputBuffer.get(_inputPtr) == BYTE_x) {
                _pendingInput = PENDING_STATE_ATTR_VALUE_AMP_HASH_X;
                ++_inputPtr;
                if (_inputPtr >= _inputEnd) {
                    return false;
                }
                ch = handleHexEntityInAttribute(true);
            } else {
                ch = handleDecEntityInAttribute(true);
            }
        } else if (_pendingInput == PENDING_STATE_ATTR_VALUE_AMP_HASH_X) {
            ch = handleHexEntityInAttribute(true);
        } else if (_pendingInput == PENDING_STATE_ATTR_VALUE_ENTITY_NAME) {
            PName entityName = parseEntityName();
            if (entityName == null) {
                return false;
            }
            ch = decodeGeneralEntity(entityName);
            if (ch == 0) { // can't have general entities within attribute values
                _tokenName = entityName;
                reportUnexpandedEntityInAttr(_elemAttrName, false);
            }
        } else if (_pendingInput == PENDING_STATE_ATTR_VALUE_DEC_DIGIT) {
            ch = handleDecEntityInAttribute(false);
        } else if (_pendingInput == PENDING_STATE_ATTR_VALUE_HEX_DIGIT) {
            ch = handleHexEntityInAttribute(false);
        } else { // nope, split UTF-8 char
            // Nah, a multi-byte UTF-8 char. Alas, can't use shared method, as results
            // don't go in shared text buffer...
            ch = handleAttrValuePendingUTF8();
        }
        if (ch == 0) { // wasn't resolved
            return false;
        }

        char[] attrBuffer = _attrCollector.continueValue();
        // Ok; does it need a surrogate though? (over 16 bits)
        if ((ch >> 16) != 0) {
            ch -= 0x10000;
            if (_elemAttrPtr >= attrBuffer.length) {
                attrBuffer = _attrCollector.valueBufferFull();
            }
            attrBuffer[_elemAttrPtr++] = (char) (0xD800 | (ch >> 10));
            ch = 0xDC00 | (ch & 0x3FF);
        }
        if (_elemAttrPtr >= attrBuffer.length) {
            attrBuffer = _attrCollector.valueBufferFull();
        }
        attrBuffer[_elemAttrPtr++] = (char) ch;
        return true; // done it!
    }

    private int handleAttrValuePendingUTF8() throws XMLStreamException {
        // note: we know there must be at least one byte available at this point
        int c = _pendingInput;
        _pendingInput = 0;

        // Let's just re-test the first pending byte (in LSB):
        switch (_charTypes.TEXT_CHARS[c & 0xFF]) {
            case XmlCharTypes.CT_MULTIBYTE_2:
                // Easy: must have just one byte, did get another one:
                return decodeUtf8_2(c);

            case XmlCharTypes.CT_MULTIBYTE_3: {
                // Ok... so do we have one or two pending bytes?
                int next = _inputBuffer.get(_inputPtr++) & 0xFF;
                int c2 = (c >> 8);
                if (c2 == 0) { // just one; need two more
                    if (_inputPtr >= _inputEnd) { // but got only one
                        _pendingInput = c | (next << 8);
                        return 0;
                    }
                    int c3 = _inputBuffer.get(_inputPtr++) & 0xFF;
                    c = decodeUtf8_3(c, next, c3);
                } else { // had two, got one, bueno:
                    c = decodeUtf8_3((c & 0xFF), c2, next);
                }
                return c;
            }

            case XmlCharTypes.CT_MULTIBYTE_4: {
                int next = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                // Only had one?
                if ((c >> 8) == 0) { // ok, so need 3 more
                    if (_inputPtr >= _inputEnd) { // just have 1
                        _pendingInput = c | (next << 8);
                        return 0;
                    }
                    int c2 = _inputBuffer.get(_inputPtr++) & 0xFF;
                    if (_inputPtr >= _inputEnd) { // almost, got 2
                        _pendingInput = c | (next << 8) | (c2 << 16);
                        return 0;
                    }
                    int c3 = _inputBuffer.get(_inputPtr++) & 0xFF;
                    c = decodeUtf8_4(c, next, c2, c3);
                } else { // had two or three
                    int c2 = (c >> 8) & 0xFF;
                    int c3 = (c >> 16);

                    if (c3 == 0) { // just two
                        if (_inputPtr >= _inputEnd) { // one short
                            _pendingInput = c | (next << 16);
                            return 0;
                        }
                        c3 = _inputBuffer.get(_inputPtr++) & 0xFF;
                        c = decodeUtf8_4((c & 0xFF), c2, next, c3);
                    } else { // had three, got last
                        c = decodeUtf8_4((c & 0xFF), c2, c3, next);
                    }
                }
                return c;
            }

            default: // should never occur:
                throwInternal();
                return 0; // never gets here
        }
    }

    private int handleDecEntityInAttribute(boolean starting) throws XMLStreamException {
        byte b = _inputBuffer.get(_inputPtr++); // we know one is available
        if (starting) {
            int ch = b;
            if (ch < INT_0 || ch > INT_9) { // invalid entity
                throwUnexpectedChar(decodeCharForError(b), " expected a digit (0 - 9) for character entity");
            }
            _pendingInput = PENDING_STATE_ATTR_VALUE_DEC_DIGIT;
            _entityValue = ch - INT_0;
            if (_inputPtr >= _inputEnd) {
                return 0;
            }
            b = _inputBuffer.get(_inputPtr++);
        }
        while (b != BYTE_SEMICOLON) {
            int ch = ((int) b) - INT_0;
            if (ch < 0 || ch > 9) { // invalid entity
                throwUnexpectedChar(decodeCharForError(b), " expected a digit (0 - 9) for character entity");
            }
            int value = (_entityValue * 10) + ch;
            _entityValue = value;
            if (value > MAX_UNICODE_CHAR) { // Overflow?
                reportEntityOverflow();
            }
            if (_inputPtr >= _inputEnd) {
                return 0;
            }
            b = _inputBuffer.get(_inputPtr++);
        }
        verifyXmlChar(_entityValue);
        _pendingInput = 0;
        return _entityValue;
    }

    private int handleHexEntityInAttribute(boolean starting) throws XMLStreamException {
        byte b = _inputBuffer.get(_inputPtr++); // we know one is available
        if (starting) {
            int ch = b;
            if (ch <= INT_9 && ch >= INT_0) {
                ch -= INT_0;
            } else if (ch <= INT_F && ch >= INT_A) {
                ch = 10 + (ch - INT_A);
            } else if (ch <= INT_f && ch >= INT_a) {
                ch = 10 + (ch - INT_a);
            } else {
                throwUnexpectedChar(decodeCharForError(b), " expected a hex digit (0-9a-fA-F) for character entity");
            }
            _pendingInput = PENDING_STATE_ATTR_VALUE_HEX_DIGIT;
            _entityValue = ch;
            if (_inputPtr >= _inputEnd) {
                return 0;
            }
            b = _inputBuffer.get(_inputPtr++);
        }
        while (b != BYTE_SEMICOLON) {
            int ch = b;
            if (ch <= INT_9 && ch >= INT_0) {
                ch -= INT_0;
            } else if (ch <= INT_F && ch >= INT_A) {
                ch = 10 + (ch - INT_A);
            } else if (ch <= INT_f && ch >= INT_a) {
                ch = 10 + (ch - INT_a);
            } else {
                throwUnexpectedChar(decodeCharForError(b), " expected a hex digit (0-9a-fA-F) for character entity");
            }
            int value = (_entityValue << 4) + ch;
            _entityValue = value;
            if (value > MAX_UNICODE_CHAR) { // Overflow?
                reportEntityOverflow();
            }
            if (_inputPtr >= _inputEnd) {
                return 0;
            }
            b = _inputBuffer.get(_inputPtr++);
        }
        verifyXmlChar(_entityValue);
        _pendingInput = 0;
        return _entityValue;
    }

    /**
     * Method called to handle entity encountered inside attribute value.
     *
     * @return Value of expanded character entity, if processed (which must be
     *    1 or above); 0 for general entity, or -1 for "not enough input"
     */
    protected int handleEntityInAttributeValue() throws XMLStreamException {
        if (_inputPtr >= _inputEnd) {
            _pendingInput = PENDING_STATE_ATTR_VALUE_AMP;
            return -1;
        }
        byte b = _inputBuffer.get(_inputPtr++);
        if (b == BYTE_HASH) { // numeric character entity
            _pendingInput = PENDING_STATE_ATTR_VALUE_AMP_HASH;
            if (_inputPtr >= _inputEnd) {
                return -1;
            }
            int ch;
            if (_inputBuffer.get(_inputPtr) == BYTE_x) {
                _pendingInput = PENDING_STATE_ATTR_VALUE_AMP_HASH_X;
                ++_inputPtr;
                if (_inputPtr >= _inputEnd) {
                    return -1;
                }
                ch = handleHexEntityInAttribute(true);
            } else {
                ch = handleDecEntityInAttribute(true);
            }
            if (ch == 0) {
                return -1;
            }
            return ch;
        }
        PName entityName = parseNewEntityName(b);
        if (entityName == null) {
            _pendingInput = PENDING_STATE_ATTR_VALUE_ENTITY_NAME;
            return -1;
        }
        int ch = decodeGeneralEntity(entityName);
        if (ch != 0) {
            return ch;
        }
        _tokenName = entityName;
        return 0;
    }

    @Override
    protected boolean handleNsDecl() throws XMLStreamException {
        final int[] TYPES = _charTypes.ATTR_CHARS;
        char[] attrBuffer = _nameBuffer;
        final int quoteChar = _elemAttrQuote;

        // First; any pending input?
        if (_pendingInput != 0) {
            if (!handleNsValuePending()) {
                return false;
            }
            _pendingInput = 0;
        }

        value_loop: while (true) {
            int c;

            ascii_loop: while (true) {
                if (_inputPtr >= _inputEnd) {
                    return false;
                }
                if (_elemNsPtr >= attrBuffer.length) {
                    _nameBuffer = attrBuffer = DataUtil.growArrayBy(attrBuffer, attrBuffer.length);
                }
                int max = _inputEnd;
                {
                    int max2 = _inputPtr + (attrBuffer.length - _elemNsPtr);
                    if (max2 < max) {
                        max = max2;
                    }
                }
                while (_inputPtr < max) {
                    c = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                    if (TYPES[c] != 0) {
                        break ascii_loop;
                    }
                    attrBuffer[_elemNsPtr++] = (char) c;
                }
            }

            switch (TYPES[c]) {
                case XmlCharTypes.CT_INVALID:
                    handleInvalidXmlChar(c);
                case XmlCharTypes.CT_WS_CR:
                    if (_inputPtr >= _inputEnd) {
                        _pendingInput = PENDING_STATE_CR;
                        return false;
                    }
                    if (_inputBuffer.get(_inputPtr) == BYTE_LF) {
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
                    if (_inputPtr >= _inputEnd) {
                        _pendingInput = c;
                        return false;
                    }
                    c = decodeUtf8_2(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_3:
                    if ((_inputEnd - _inputPtr) < 2) {
                        if (_inputEnd > _inputPtr) { // 2 bytes available
                            int d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                            c |= (d << 8);
                        }
                        _pendingInput = c;
                        return false;
                    }
                    c = decodeUtf8_3(c);
                    break;

                case XmlCharTypes.CT_MULTIBYTE_4:
                    if ((_inputEnd - _inputPtr) < 3) {
                        if (_inputEnd > _inputPtr) { // at least 2 bytes?
                            int d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                            c |= (d << 8);
                            if (_inputEnd > _inputPtr) { // 3 bytes?
                                d = (int) _inputBuffer.get(_inputPtr++) & 0xFF;
                                c |= (d << 16);
                            }
                        }
                        _pendingInput = c;
                        return false;
                    }
                    c = decodeUtf8_4(c);
                    // Let's add first part right away:
                    attrBuffer[_elemNsPtr++] = (char) (0xD800 | (c >> 10));
                    c = 0xDC00 | (c & 0x3FF);
                    if (_elemNsPtr >= attrBuffer.length) {
                        _nameBuffer = attrBuffer = DataUtil.growArrayBy(attrBuffer, attrBuffer.length);
                    }
                    break;

                case XmlCharTypes.CT_MULTIBYTE_N:
                    reportInvalidInitial(c);
                case XmlCharTypes.CT_LT:
                    throwUnexpectedChar(c, "'<' not allowed in attribute value");
                case XmlCharTypes.CT_AMP:
                    c = handleEntityInAttributeValue();
                    if (c <= 0) { // general entity; should never happen
                        if (c < 0) { // end-of-input
                            return false;
                        }
                        reportUnexpandedEntityInAttr(_elemAttrName, true);
                    }
                    // Ok; does it need a surrogate though? (over 16 bits)
                    if ((c >> 16) != 0) {
                        c -= 0x10000;
                        attrBuffer[_elemNsPtr++] = (char) (0xD800 | (c >> 10));
                        c = 0xDC00 | (c & 0x3FF);
                        if (_elemNsPtr >= attrBuffer.length) {
                            _nameBuffer = attrBuffer = DataUtil.growArrayBy(attrBuffer, attrBuffer.length);
                        }
                    }
                    break;

                case XmlCharTypes.CT_ATTR_QUOTE:
                    if (c == quoteChar) {
                        break value_loop;
                    }

                    // default:
                    // Other chars are not important here...
            }
            // We know there's room for at least one char without checking
            attrBuffer[_elemNsPtr++] = (char) c;
        }

        /* Simple optimization: for default ns removal (or, with
         * ns 1.1, any other as well), will use empty value... no
         * need to try to intern:
         */
        int attrPtr = _elemNsPtr;
        if (attrPtr == 0) {
            bindNs(_elemAttrName, "");
        } else {
            String uri = _config.canonicalizeURI(attrBuffer, attrPtr);
            bindNs(_elemAttrName, uri);
        }
        return true;
    }

    /**
     * @return True if the partial information was succesfully handled;
     *    false if not
     */
    private boolean handleNsValuePending() throws XMLStreamException {
        if (_pendingInput == PENDING_STATE_CR) {
            if (!handlePartialCR()) {
                return false;
            }
            char[] attrBuffer = _nameBuffer;
            if (_elemNsPtr >= attrBuffer.length) {
                _nameBuffer = attrBuffer = DataUtil.growArrayBy(attrBuffer, attrBuffer.length);
            }
            // All lfs get converted to spaces, in attribute values
            attrBuffer[_elemNsPtr++] = ' ';
            return true;
        }

        // otherwise must be related to entity handling within attribute value
        if (_inputPtr >= _inputEnd) {
            return false;
        }

        int ch;

        if (_pendingInput == PENDING_STATE_ATTR_VALUE_AMP) {
            byte b = _inputBuffer.get(_inputPtr++);
            if (b == BYTE_HASH) { // numeric character entity
                _pendingInput = PENDING_STATE_ATTR_VALUE_AMP_HASH;
                if (_inputPtr >= _inputEnd) {
                    return false;
                }
                if (_inputBuffer.get(_inputPtr) == BYTE_x) {
                    _pendingInput = PENDING_STATE_ATTR_VALUE_AMP_HASH_X;
                    ++_inputPtr;
                    if (_inputPtr >= _inputEnd) {
                        return false;
                    }
                    ch = handleHexEntityInAttribute(true);
                } else {
                    ch = handleDecEntityInAttribute(true);
                }
            } else {
                PName entityName = parseNewEntityName(b);
                if (entityName == null) {
                    _pendingInput = PENDING_STATE_ATTR_VALUE_ENTITY_NAME;
                    return false;
                }
                ch = decodeGeneralEntity(entityName);
                if (ch == 0) { // can't have general entities within attribute values
                    _tokenName = entityName;
                    reportUnexpandedEntityInAttr(_elemAttrName, false);
                }
            }
        } else if (_pendingInput == PENDING_STATE_ATTR_VALUE_AMP_HASH) {
            if (_inputBuffer.get(_inputPtr) == BYTE_x) {
                _pendingInput = PENDING_STATE_ATTR_VALUE_AMP_HASH_X;
                ++_inputPtr;
                if (_inputPtr >= _inputEnd) {
                    return false;
                }
                ch = handleHexEntityInAttribute(true);
            } else {
                ch = handleDecEntityInAttribute(true);
            }
        } else if (_pendingInput == PENDING_STATE_ATTR_VALUE_AMP_HASH_X) {
            ch = handleHexEntityInAttribute(true);
        } else if (_pendingInput == PENDING_STATE_ATTR_VALUE_ENTITY_NAME) {
            PName entityName = parseEntityName();
            if (entityName == null) {
                return false;
            }
            ch = decodeGeneralEntity(entityName);
            if (ch == 0) { // can't have general entities within attribute values
                _tokenName = entityName;
                reportUnexpandedEntityInAttr(_elemAttrName, false);
            }
        } else if (_pendingInput == PENDING_STATE_ATTR_VALUE_DEC_DIGIT) {
            ch = handleDecEntityInAttribute(false);
        } else if (_pendingInput == PENDING_STATE_ATTR_VALUE_HEX_DIGIT) {
            ch = handleHexEntityInAttribute(false);
        } else {
            // 05-Aug-2012, tatu: Apparently we can end up here too...
            ch = handleAttrValuePendingUTF8();
        }
        if (ch == 0) { // wasn't resolved
            return false;
        }

        char[] attrBuffer = _nameBuffer;
        // Ok; does it need a surrogate though? (over 16 bits)
        if ((ch >> 16) != 0) {
            ch -= 0x10000;
            if (_elemNsPtr >= attrBuffer.length) {
                _nameBuffer = attrBuffer = DataUtil.growArrayBy(attrBuffer, attrBuffer.length);
            }
            attrBuffer[_elemNsPtr++] = (char) (0xD800 | (ch >> 10));
            ch = 0xDC00 | (ch & 0x3FF);
        }
        if (_elemNsPtr >= attrBuffer.length) {
            _nameBuffer = attrBuffer = DataUtil.growArrayBy(attrBuffer, attrBuffer.length);
        }
        attrBuffer[_elemNsPtr++] = (char) ch;
        return true; // done it!
    }

    /*
    /**********************************************************************
    /* Common name/entity parsing
    /**********************************************************************
     */

    @Override
    protected final PName parseNewName(byte b) throws XMLStreamException {
        int q = b & 0xFF;

        // Let's do just quick sanity check first; a thorough check will be
        // done later on if necessary, now we'll just do the very cheap
        // check to catch extra spaces etc.
        if (q < INT_A) { // lowest acceptable start char, except for ':' that would be allowed in non-ns mode
            throwUnexpectedChar(q, "; expected a name start character");
        }
        _quadCount = 0;
        _currQuad = q;
        _currQuadBytes = 1;
        return parsePName();
    }

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
    @Override
    protected final PName parsePName() throws XMLStreamException {
        int q = _currQuad;

        while (true) {
            int i;

            switch (_currQuadBytes) {
                case 0:
                    if (_inputPtr >= _inputEnd) {
                        return null; // all pointers have been set
                    }
                    q = _inputBuffer.get(_inputPtr++) & 0xFF;
                    /* Since name char validity is checked later on, we only
                     * need to be able to reliably see the end of the name...
                     * and those are simple enough so that we can just
                     * compare; lookup table won't speed things up (according
                     * to profiler)
                     */
                    if (q < 65) { // 'A'
                        // Ok; "_" (45), "." (46) and "0"-"9"/":" (48 - 57/58) still name chars
                        if (q < 45 || q > 58 || q == 47) {
                            // End of name
                            return findPName(q, 0);
                        }
                    }
                    // fall through

                case 1:
                    if (_inputPtr >= _inputEnd) { // need to store pointers
                        _currQuad = q;
                        _currQuadBytes = 1;
                        return null;
                    }
                    i = _inputBuffer.get(_inputPtr++) & 0xFF;
                    if (i < 65) { // 'A'
                        if (i < 45 || i > 58 || i == 47) {
                            return findPName(q, 1);
                        }
                    }
                    q = (q << 8) | i;
                    // fall through

                case 2:
                    if (_inputPtr >= _inputEnd) { // need to store pointers
                        _currQuad = q;
                        _currQuadBytes = 2;
                        return null;
                    }
                    i = _inputBuffer.get(_inputPtr++) & 0xFF;
                    if (i < 65) { // 'A'
                        if (i < 45 || i > 58 || i == 47) {
                            return findPName(q, 2);
                        }
                    }
                    q = (q << 8) | i;
                    // fall through

                case 3:
                    if (_inputPtr >= _inputEnd) { // need to store pointers
                        _currQuad = q;
                        _currQuadBytes = 3;
                        return null;
                    }
                    i = _inputBuffer.get(_inputPtr++) & 0xFF;
                    if (i < 65) { // 'A'
                        if (i < 45 || i > 58 || i == 47) {
                            return findPName(q, 3);
                        }
                    }
                    q = (q << 8) | i;
            }

            // If we get this far, need to add full quad into result array and update state
            if (_quadCount == 0) { // first quad
                _quadBuffer[0] = q;
                _quadCount = 1;
            } else {
                if (_quadCount >= _quadBuffer.length) { // let's just double?
                    _quadBuffer = DataUtil.growArrayBy(_quadBuffer, _quadBuffer.length);
                }
                _quadBuffer[_quadCount++] = q;
            }
            _currQuadBytes = 0;
        }
    }

    protected final PName parseNewEntityName(byte b) throws XMLStreamException {
        int q = b & 0xFF;
        if (q < INT_A) {
            throwUnexpectedChar(q, "; expected a name start character");
        }
        _quadCount = 0;
        _currQuad = q;
        _currQuadBytes = 1;
        return parseEntityName();
    }

    protected final PName parseEntityName() throws XMLStreamException {
        int q = _currQuad;

        while (true) {
            int i;

            switch (_currQuadBytes) {
                case 0:
                    if (_inputPtr >= _inputEnd) {
                        return null; // all pointers have been set
                    }
                    q = _inputBuffer.get(_inputPtr++) & 0xFF;
                    /* Since name char validity is checked later on, we only
                     * need to be able to reliably see the end of the name...
                     * and those are simple enough so that we can just
                     * compare; lookup table won't speed things up (according
                     * to profiler)
                     */
                    if (q < 65) { // 'A'
                        // Ok; "_" (45), "." (46) and "0"-"9"/":" (48 - 57/58) still name chars
                        if (q < 45 || q > 58 || q == 47) {
                            // apos, quot?
                            if (_quadCount == 1) {
                                q = _quadBuffer[0];
                                if (q == EntityNames.ENTITY_APOS_QUAD) {
                                    --_inputPtr;
                                    return EntityNames.ENTITY_APOS;
                                }
                                if (q == EntityNames.ENTITY_QUOT_QUAD) {
                                    --_inputPtr;
                                    return EntityNames.ENTITY_QUOT;
                                }
                            }
                            // Nope, generic:
                            return findPName(q, 0);
                        }
                    }
                    // fall through

                case 1:
                    if (_inputPtr >= _inputEnd) { // need to store pointers
                        _currQuad = q;
                        _currQuadBytes = 1;
                        return null;
                    }
                    i = _inputBuffer.get(_inputPtr++) & 0xFF;
                    if (i < 65) { // 'A'
                        if (i < 45 || i > 58 || i == 47) {
                            return findPName(q, 1);
                        }
                    }
                    q = (q << 8) | i;
                    // fall through

                case 2:
                    if (_inputPtr >= _inputEnd) { // need to store pointers
                        _currQuad = q;
                        _currQuadBytes = 2;
                        return null;
                    }
                    i = _inputBuffer.get(_inputPtr++) & 0xFF;
                    if (i < 65) { // 'A'
                        if (i < 45 || i > 58 || i == 47) {
                            // lt or gt?
                            if (_quadCount == 0) {
                                if (q == EntityNames.ENTITY_GT_QUAD) {
                                    --_inputPtr;
                                    return EntityNames.ENTITY_GT;
                                }
                                if (q == EntityNames.ENTITY_LT_QUAD) {
                                    --_inputPtr;
                                    return EntityNames.ENTITY_LT;
                                }
                            }
                            return findPName(q, 2);
                        }
                    }
                    q = (q << 8) | i;
                    // fall through

                case 3:
                    if (_inputPtr >= _inputEnd) { // need to store pointers
                        _currQuad = q;
                        _currQuadBytes = 3;
                        return null;
                    }
                    i = _inputBuffer.get(_inputPtr++) & 0xFF;
                    if (i < 65) { // 'A'
                        if (i < 45 || i > 58 || i == 47) {
                            // amp?
                            if (_quadCount == 0) {
                                if (q == EntityNames.ENTITY_AMP_QUAD) {
                                    --_inputPtr;
                                    return EntityNames.ENTITY_AMP;
                                }
                            }
                            return findPName(q, 3);
                        }
                    }
                    q = (q << 8) | i;
            }

            /* If we get this far, need to add full quad into
             * result array and update state
             */
            if (_quadCount == 0) { // first quad
                _quadBuffer[0] = q;
                _quadCount = 1;
            } else {
                if (_quadCount >= _quadBuffer.length) { // let's just double?
                    _quadBuffer = DataUtil.growArrayBy(_quadBuffer, _quadBuffer.length);
                }
                _quadBuffer[_quadCount++] = q;
            }
            _currQuadBytes = 0;
        }
    }

    /*
    /**********************************************************************
    /* Internal methods, LF handling
    /**********************************************************************
     */

    /**
     * Method called when there is a pending \r (from past buffer),
     * and we need to see
     *
     * @return True if the linefeed was succesfully processed (had
     *   enough input data to do that); or false if there is no
     *   data available to check this
     */
    @Override
    protected final boolean handlePartialCR() {
        // sanity check
        if (_pendingInput != PENDING_STATE_CR) {
            throwInternal();
        }
        if (_inputPtr >= _inputEnd) {
            return false;
        }
        _pendingInput = 0;
        if (_inputBuffer.get(_inputPtr) == BYTE_LF) {
            ++_inputPtr;
        }
        ++_currRow;
        _rowStartOffset = _inputPtr;
        return true;
    }

    /*
    /**********************************************************************
    /* Multi-byte char decoding
    /**********************************************************************
     */

    /**
     *<p>
     * Note: caller must guarantee enough data is available before
     * calling the method
     */
    protected final int decodeUtf8_2(int c) throws XMLStreamException {
        int d = _inputBuffer.get(_inputPtr++);
        if ((d & 0xC0) != 0x080) {
            reportInvalidOther(d & 0xFF, _inputPtr);
        }
        return ((c & 0x1F) << 6) | (d & 0x3F);
    }

    protected final void skipUtf8_2(int c) throws XMLStreamException {
        int d = _inputBuffer.get(_inputPtr++);
        if ((d & 0xC0) != 0x080) {
            reportInvalidOther(d & 0xFF, _inputPtr);
        }
    }

    /**
     *<p>
     * Note: caller must guarantee enough data is available before
     * calling the method
     */
    protected final int decodeUtf8_3(int c1) throws XMLStreamException {
        c1 &= 0x0F;
        int d = _inputBuffer.get(_inputPtr++);
        if ((d & 0xC0) != 0x080) {
            reportInvalidOther(d & 0xFF, _inputPtr);
        }
        int c = (c1 << 6) | (d & 0x3F);
        d = _inputBuffer.get(_inputPtr++);
        if ((d & 0xC0) != 0x080) {
            reportInvalidOther(d & 0xFF, _inputPtr);
        }
        c = (c << 6) | (d & 0x3F);
        if (c1 >= 0xD) { // 0xD800-0xDFFF, 0xFFFE-0xFFFF illegal
            if (c >= 0xD800) { // surrogates illegal, as well as 0xFFFE/0xFFFF
                if (c < 0xE000 || (c >= 0xFFFE && c <= 0xFFFF)) {
                    c = handleInvalidXmlChar(c);
                }
            }
        }
        return c;
    }

    protected final int decodeUtf8_3(int c1, int c2, int c3) throws XMLStreamException {
        // Note: first char is assumed to have been checked
        if ((c2 & 0xC0) != 0x080) {
            reportInvalidOther(c2 & 0xFF, _inputPtr - 1);
        }
        if ((c3 & 0xC0) != 0x080) {
            reportInvalidOther(c3 & 0xFF, _inputPtr);
        }
        int c = ((c1 & 0x0F) << 12) | ((c2 & 0x3F) << 6) | (c3 & 0x3F);
        if (c1 >= 0xD) { // 0xD800-0xDFFF, 0xFFFE-0xFFFF illegal
            if (c >= 0xD800) { // surrogates illegal, as well as 0xFFFE/0xFFFF
                if (c < 0xE000 || (c >= 0xFFFE && c <= 0xFFFF)) {
                    c = handleInvalidXmlChar(c);
                }
            }
        }
        return c;
    }

    protected final int decodeUtf8_4(int c) throws XMLStreamException {
        int d = _inputBuffer.get(_inputPtr++);
        if ((d & 0xC0) != 0x080) {
            reportInvalidOther(d & 0xFF, _inputPtr);
        }
        c = ((c & 0x07) << 6) | (d & 0x3F);
        d = _inputBuffer.get(_inputPtr++);
        if ((d & 0xC0) != 0x080) {
            reportInvalidOther(d & 0xFF, _inputPtr);
        }
        c = (c << 6) | (d & 0x3F);
        d = _inputBuffer.get(_inputPtr++);
        if ((d & 0xC0) != 0x080) {
            reportInvalidOther(d & 0xFF, _inputPtr);
        }
        /* note: won't change it to negative here, since caller
         * already knows it'll need a surrogate
         */
        return ((c << 6) | (d & 0x3F)) - 0x10000;
    }

    /**
     * @return Character value <b>minus 0x10000</b>; this so that caller
     *    can readily expand it to actual surrogates
     */
    protected final int decodeUtf8_4(int c1, int c2, int c3, int c4) throws XMLStreamException {
        /* Note: first char is assumed to have been checked,
         * (but not yet masked)
         */
        if ((c2 & 0xC0) != 0x080) {
            reportInvalidOther(c2 & 0xFF, _inputPtr - 2);
        }
        int c = ((c1 & 0x07) << 6) | (c2 & 0x3F);
        if ((c3 & 0xC0) != 0x080) {
            reportInvalidOther(c3 & 0xFF, _inputPtr - 1);
        }
        c = (c << 6) | (c3 & 0x3F);
        if ((c4 & 0xC0) != 0x080) {
            reportInvalidOther(c4 & 0xFF, _inputPtr);
        }
        return ((c << 6) | (c4 & 0x3F)) - 0x10000;
    }
}
