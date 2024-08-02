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

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import com.azure.xml.implementation.stax2.XMLStreamLocation2;

import com.azure.xml.implementation.aalto.impl.LocationImpl;
import com.azure.xml.implementation.aalto.util.DataUtil;
import com.azure.xml.implementation.aalto.util.XmlCharTypes;
import com.azure.xml.implementation.aalto.util.XmlChars;

/**
 * Intermediate base class used by different byte-backed scanners.
 * Specifically, used as a base by both blocking (stream) and
 * non-blocking (async) byte-based scanners (as opposed to Reader-backed,
 * character-based scanners)
 */
public abstract class ByteBasedScanner extends XmlScanner {
    /*
    /**********************************************************************
    /* Byte constants
    /**********************************************************************
     */

    // White-space:

    final protected static byte BYTE_NULL = (byte) 0;
    final protected static byte BYTE_SPACE = (byte) ' ';
    final protected static byte BYTE_LF = (byte) '\n';
    final protected static byte BYTE_CR = (byte) '\r';
    final protected static byte BYTE_TAB = (byte) 9;

    final protected static byte BYTE_LT = (byte) '<';
    final protected static byte BYTE_GT = (byte) '>';
    final protected static byte BYTE_AMP = (byte) '&';
    final protected static byte BYTE_HASH = (byte) '#';
    final protected static byte BYTE_EXCL = (byte) '!';
    final protected static byte BYTE_HYPHEN = (byte) '-';
    final protected static byte BYTE_QMARK = (byte) '?';
    final protected static byte BYTE_SLASH = (byte) '/';
    final protected static byte BYTE_EQ = (byte) '=';
    final protected static byte BYTE_QUOT = (byte) '"';
    final protected static byte BYTE_APOS = (byte) '\'';
    final protected static byte BYTE_LBRACKET = (byte) '[';
    final protected static byte BYTE_RBRACKET = (byte) ']';
    final protected static byte BYTE_SEMICOLON = (byte) ';';

    final protected static byte BYTE_a = (byte) 'a';
    final protected static byte BYTE_g = (byte) 'g';
    final protected static byte BYTE_l = (byte) 'l';
    final protected static byte BYTE_m = (byte) 'm';
    final protected static byte BYTE_o = (byte) 'o';
    final protected static byte BYTE_p = (byte) 'p';
    final protected static byte BYTE_q = (byte) 'q';
    final protected static byte BYTE_s = (byte) 's';
    final protected static byte BYTE_t = (byte) 't';
    final protected static byte BYTE_u = (byte) 'u';
    final protected static byte BYTE_x = (byte) 'x';

    final protected static byte BYTE_A = (byte) 'A';
    final protected static byte BYTE_C = (byte) 'C';
    final protected static byte BYTE_D = (byte) 'D';
    final protected static byte BYTE_P = (byte) 'P';
    final protected static byte BYTE_S = (byte) 'S';
    final protected static byte BYTE_T = (byte) 'T';

    /*
    /**********************************************************************
    /* Input buffering
    /**********************************************************************
     */

    /**
     * Pointer to the next unread byte in the input buffer.
     */
    protected int _inputPtr;

    /**
     * Pointer to the first byte <b>after</b> the end of valid content.
     * This may point beyond of the physical buffer array.
     */
    protected int _inputEnd;

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
    protected int _tmpChar = INT_NULL;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    protected ByteBasedScanner(ReaderConfig cfg) {
        super(cfg);
        _pastBytesOrChars = 0; // should it be passed by caller?
        _rowStartOffset = 0; // should probably be passed by caller...
    }

    //    @Override protected abstract void _releaseBuffers();

    @Override
    protected abstract void _closeSource() throws IOException;

    /*
    /**********************************************************************
    /* Location handling
    /**********************************************************************
     */

    @Override
    public XMLStreamLocation2 getCurrentLocation() {
        return LocationImpl.fromZeroBased(_config.getPublicId(), _config.getSystemId(), _pastBytesOrChars + _inputPtr,
            _currRow, _inputPtr - _rowStartOffset);
    }

    @Override
    public int getCurrentColumnNr() {
        return _inputPtr - _rowStartOffset;
    }

    @Override
    public long getStartingByteOffset() {
        return _startRawOffset;
    }

    @Override
    public long getStartingCharOffset() {
        // N/A for this type
        return -1L;
    }

    @Override
    public long getEndingByteOffset() throws XMLStreamException {
        // Have to complete the token to know the ending location...
        if (_tokenIncomplete) {
            finishToken();
        }
        return _pastBytesOrChars + _inputPtr;
    }

    @Override
    public long getEndingCharOffset() throws XMLStreamException {
        // N/A for this type
        return -1L;
    }

    protected final void markLF(int offset) {
        _rowStartOffset = offset;
        ++_currRow;
    }

    protected final void markLF() {
        _rowStartOffset = _inputPtr;
        ++_currRow;
    }

    protected final void setStartLocation() {
        _startRawOffset = _pastBytesOrChars + _inputPtr;
        _startRow = _currRow;
        _startColumn = _inputPtr - _rowStartOffset;
    }

    /*
    /**********************************************************************
    /* Abstract methods for sub-classes to implement
    /**********************************************************************
     */

    /**
     * Method called by methods when encountering a byte that
     * can not be part of a valid character in the current context.
     * Should return the actual decoded character for error reporting
     * purposes.
     */
    protected abstract int decodeCharForError(byte b) throws XMLStreamException;

    /*
    /**********************************************************************
    /* And then shared functionality for sub-classes
    /**********************************************************************
     */

    /**
     * Conceptually, this method really does NOT belong here. However,
     * currently it is quite hard to refactor it, so it'll have to
     * stay here until better place is found
     */
    protected final PName addUTFPName(ByteBasedPNameTable symbols, XmlCharTypes charTypes, int hash, int[] quads,
        int qlen, int lastQuadBytes) throws XMLStreamException {
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
                    reportEofInName(cbuf, 0);
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
                        reportEofInName(cbuf, cix);
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

    protected void reportInvalidInitial(int mask) throws XMLStreamException {
        reportInputProblem("Invalid UTF-8 start byte 0x" + Integer.toHexString(mask));
    }

    protected void reportInvalidOther(int mask) throws XMLStreamException {
        reportInputProblem("Invalid UTF-8 middle byte 0x" + Integer.toHexString(mask));
    }
}
