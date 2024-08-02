// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
/* Woodstox Lite ("wool") XML processor
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

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

import com.azure.xml.implementation.aalto.impl.IoStreamException;
import com.azure.xml.implementation.aalto.impl.LocationImpl;
import com.azure.xml.implementation.aalto.util.CharsetNames;

/**
 * Class that takes care of bootstrapping main document input from
 * a byte-oriented input source: usually either an <code>InputStream</code>,
 * or a block source like byte array.
 */
public final class ByteSourceBootstrapper extends InputBootstrapper {
    private final static byte BYTE_NULL = (byte) 0;

    private final static byte BYTE_CR = (byte) '\r';

    private final static byte BYTE_LF = (byte) '\n';

    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */

    /**
     * Underlying InputStream to use for reading content.
     */
    private final InputStream _in;

    /*
    /**********************************************************************
    /* Input buffering
    /**********************************************************************
     */

    private final byte[] _inputBuffer;

    private int _inputPtr;

    private int _inputLen;

    /*
    /**********************************************************************
    /* Data gathered
    /**********************************************************************
     */

    private boolean mBigEndian = true;
    private int mBytesPerChar = 0; // 0 means "dunno yet"

    private boolean mHadBOM = false;
    private boolean mByteSizeFound = false;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    private ByteSourceBootstrapper(ReaderConfig cfg, InputStream in) {
        super(cfg);
        _in = in;
        _inputBuffer = cfg.allocFullBBuffer(4000);
        _inputLen = _inputPtr = 0;
    }

    private ByteSourceBootstrapper(ReaderConfig cfg, byte[] inputBuffer, int inputStart, int inputLen) {
        super(cfg);
        _in = null;
        _inputBuffer = inputBuffer;
        _inputPtr = inputStart;
        _inputLen = (inputStart + inputLen);
        // Need to offset this, to keep location correct
        _inputProcessed = -inputStart;
    }

    public static ByteSourceBootstrapper construct(ReaderConfig cfg, InputStream in) throws XMLStreamException {
        return new ByteSourceBootstrapper(cfg, in);
    }

    public static ByteSourceBootstrapper construct(ReaderConfig cfg, byte[] inputBuffer, int inputStart, int inputLen)
        throws XMLStreamException {
        return new ByteSourceBootstrapper(cfg, inputBuffer, inputStart, inputLen);
    }

    @Override
    public XmlScanner bootstrap() throws XMLStreamException {
        try {
            return doBootstrap();
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
        } finally {
            _config.freeSmallCBuffer(mKeyword);
        }
    }

    public XmlScanner doBootstrap() throws IOException, XMLStreamException {
        String normEnc = null;

        determineStreamEncoding();
        if (hasXmlDeclaration()) { // yup, has xml decl:
            readXmlDeclaration();
            if (mFoundEncoding != null) {
                normEnc = verifyXmlEncoding(mFoundEncoding);
            }
        }

        // Now, have we figured out the encoding?

        if (normEnc == null) { // not via xml declaration
            if (mBytesPerChar == 2) { // UTF-16, BE/LE
                normEnc = mBigEndian ? CharsetNames.CS_UTF16BE : CharsetNames.CS_UTF16LE;
            } else if (mBytesPerChar == 4) { // UCS-4... ?
                /* 22-Mar-2005, TSa: JDK apparently has no way of dealing
                 *   with these encodings... not sure if and how it should
                 *   be dealt with, really. Name could be UCS-4xx... or
                 *   perhaps UTF-32xx
                 */
                normEnc = mBigEndian ? CharsetNames.CS_UTF32BE : CharsetNames.CS_UTF32LE;
            } else {
                // Ok, default has to be UTF-8, as per XML specs
                normEnc = CharsetNames.CS_UTF8;
            }
        }

        _config.setActualEncoding(normEnc);
        _config.setXmlDeclInfo(mDeclaredXmlVersion, mFoundEncoding, mStandalone);

        // Normalized, can thus use straight equality checks now
        // UTF-8 compatible (loosely speaking) ones can use same scanner
        if (normEnc.equals(CharsetNames.CS_UTF8)
            || normEnc.equals(CharsetNames.CS_ISO_LATIN1)
            || normEnc.equals(CharsetNames.CS_US_ASCII)) {
            return new Utf8Scanner(_config, _in, _inputBuffer, _inputPtr, _inputLen);
        } else if (normEnc.startsWith(CharsetNames.CS_UTF32)) {
            // Since this is such a rare encoding, we'll just create
            // a Reader, and dispatch it to reader scanner?

            // let's augment with actual endianness info
            if (normEnc.equals(CharsetNames.CS_UTF32)) {
                normEnc = mBigEndian ? CharsetNames.CS_UTF32BE : CharsetNames.CS_UTF32LE;
            }
            Reader r = new Utf32Reader(_config, _in, _inputBuffer, _inputPtr, _inputLen, mBigEndian);
            return new ReaderScanner(_config, r);
        }

        // And finally, if all else fails, we'll also fall back to
        // using JDK-provided decoders and ReaderScanner:
        InputStream in = _in;
        if (_inputPtr < _inputLen) {
            in = new MergedStream(_config, in, _inputBuffer, _inputPtr, _inputLen); // lgtm [java/input-resource-leak]
        }
        if (normEnc.equals(CharsetNames.CS_UTF16)) {
            normEnc = mBigEndian ? CharsetNames.CS_UTF16BE : CharsetNames.CS_UTF16LE;
        }
        try {
            return new ReaderScanner(_config, new InputStreamReader(in, normEnc));
        } catch (UnsupportedEncodingException usex) {
            throw new IoStreamException("Unsupported encoding: " + usex.getMessage());
        }
    }

    /*
    /**********************************************************************
    // Internal methods, main xml decl processing
    /**********************************************************************
     */

    /**
     * Method called to figure out what the physical encoding of the
     * file appears to be (in case it can be determined from BOM, or
     * xml declaration, either of which may be present)
     */
    private void determineStreamEncoding() throws IOException {
        /* Ok; first just need 4 bytes for determining bytes-per-char from
         * BOM or first char(s) of likely xml declaration:
         */
        if (ensureLoaded(4)) {
            int origPtr = _inputPtr;

            bomblock: do { // BOM/auto-detection block
                int quartet = (_inputBuffer[_inputPtr] << 24) | ((_inputBuffer[_inputPtr + 1] & 0xFF) << 16)
                    | ((_inputBuffer[_inputPtr + 2] & 0xFF) << 8) | (_inputBuffer[_inputPtr + 3] & 0xFF);

                // Handling of (usually) optional BOM (required for
                // multi-byte formats); first 32-bit charsets:
                switch (quartet) {
                    case 0x0000FEFF:
                        mBigEndian = true;
                        _inputPtr += 4;
                        mBytesPerChar = 4;
                        break bomblock;

                    case 0xFFFE0000: // UCS-4, LE?
                        mBigEndian = false;
                        _inputPtr += 4;
                        mBytesPerChar = 4;
                        break bomblock;

                    case 0x0000FFFE: // UCS-4, in-order...
                        reportWeirdUCS4("2143");
                        break bomblock;

                    case 0x0FEFF0000: // UCS-4, in-order...
                        reportWeirdUCS4("3412");
                        break bomblock;
                }

                // Ok, if not, how about 16-bit encoding BOMs?
                int msw = quartet >>> 16;
                if (msw == 0xFEFF) { // UTF-16, BE
                    _inputPtr += 2;
                    mBytesPerChar = 2;
                    mBigEndian = true;
                    break;
                }
                if (msw == 0xFFFE) { // UTF-16, LE
                    _inputPtr += 2;
                    mBytesPerChar = 2;
                    mBigEndian = false;
                    break;
                }

                // And if not, then UTF-8 BOM?
                if ((quartet >>> 8) == 0xEFBBBF) { // UTF-8
                    _inputPtr += 3;
                    mBytesPerChar = 1;
                    mBigEndian = true; // doesn't really matter
                    break;
                }

                /* And if that wasn't succesful, how about auto-detection
                 * for '<?xm' (or subset for multi-byte encodings) marker?
                 */
                // Note: none of these consume bytes... so ptr remains at 0

                switch (quartet) {
                    case 0x0000003c: // UCS-4, BE?
                        mBigEndian = true;
                        mBytesPerChar = 4;
                        break bomblock;

                    case 0x3c000000: // UCS-4, LE?
                        mBytesPerChar = 4;
                        mBigEndian = false;
                        break bomblock;

                    case 0x00003c00: // UCS-4, in-order...
                        reportWeirdUCS4("2143");
                        break bomblock;

                    case 0x003c0000: // UCS-4, in-order...
                        reportWeirdUCS4("3412");
                        break bomblock;

                    case 0x003c003f: // UTF-16, BE
                        mBytesPerChar = 2;
                        mBigEndian = true;
                        break bomblock;

                    case 0x3c003f00: // UTF-16, LE
                        mBytesPerChar = 2;
                        mBigEndian = false;
                        break bomblock;

                    case 0x3c3f786d: // UTF-8, Ascii, ISO-Latin
                        mBytesPerChar = 1;
                        mBigEndian = true; // doesn't really matter
                        break bomblock;

                    case 0x4c6fa794: // EBCDIC, not (yet?) supported...
                        reportEBCDIC();
                }

                /* Otherwise it's either single-byte doc without xml
                 * declaration, or corrupt input...
                 */
            } while (false); // BOM/auto-detection block

            mHadBOM = (_inputPtr > origPtr);

            /* Let's update location markers to ignore BOM when calculating
             * column positions (but not from raw byte offsets)
             */
            _inputRowStart = _inputPtr;
        }

        /* Hmmh. If we haven't figured it out, let's just assume
         * UTF-8 as per XML specs:
         */
        mByteSizeFound = (mBytesPerChar > 0);
        if (!mByteSizeFound) {
            mBytesPerChar = 1;
            mBigEndian = true; // doesn't matter
        }
    }

    private boolean hasXmlDeclaration() throws IOException, XMLStreamException {
        // First the common case, 1-byte encoding (Ascii/ISO-Latin/UTF-8):
        if (mBytesPerChar == 1) {
            // Need 6 chars to determine for sure...
            if (ensureLoaded(6)) {
                if (_inputBuffer[_inputPtr] == '<'
                    && _inputBuffer[_inputPtr + 1] == '?'
                    && _inputBuffer[_inputPtr + 2] == 'x'
                    && _inputBuffer[_inputPtr + 3] == 'm'
                    && _inputBuffer[_inputPtr + 4] == 'l'
                    && ((_inputBuffer[_inputPtr + 5] & 0xFF) <= CHAR_SPACE)) {

                    // Let's skip stuff so far:
                    _inputPtr += 6;
                    return true;
                }
            }
        } else { // ... and then for slower fixed-multibyte encodings:
            if (ensureLoaded(6 * mBytesPerChar)) { // 6 chars as well
                int start = _inputPtr; // if we have to 'unread' chars
                if (nextMultiByte() == '<'
                    && nextMultiByte() == '?'
                    && nextMultiByte() == 'x'
                    && nextMultiByte() == 'm'
                    && nextMultiByte() == 'l'
                    && nextMultiByte() <= CHAR_SPACE) {
                    return true;
                }
                _inputPtr = start; // push data back
            }
        }
        return false;
    }

    /**
     * @return Normalized encoding name
     */
    private String verifyXmlEncoding(String enc) throws XMLStreamException {
        enc = CharsetNames.normalize(enc);

        // Let's actually verify we got matching information:
        if (enc.equals(CharsetNames.CS_UTF8)) {
            verifyEncoding(enc, 1);
        } else if (enc.equals(CharsetNames.CS_ISO_LATIN1)) {
            verifyEncoding(enc, 1);
        } else if (enc.equals(CharsetNames.CS_US_ASCII)) {
            verifyEncoding(enc, 1);
        } else if (enc.equals(CharsetNames.CS_UTF16)) {
            // BOM should be obligatory, to know the ordering?
            // For now, let's not enforce that though.
            //if (!mHadBOM) {
            //reportMissingBOM(enc);
            //}
            verifyEncoding(enc, 2);
        } else if (enc.equals(CharsetNames.CS_UTF16LE)) {
            verifyEncoding(enc, 2, false);
        } else if (enc.equals(CharsetNames.CS_UTF16BE)) {
            verifyEncoding(enc, 2, true);

        } else if (enc.equals(CharsetNames.CS_UTF32)) {
            // Do we require a BOM here? we can live without it...
            //if (!mHadBOM) {
            //    reportMissingBOM(enc);
            //}
            verifyEncoding(enc, 4);
        } else if (enc.equals(CharsetNames.CS_UTF32LE)) {
            verifyEncoding(enc, 4, false);
        } else if (enc.equals(CharsetNames.CS_UTF32BE)) {
            verifyEncoding(enc, 4, true);
        }
        return enc;
    }

    /*
    /**********************************************************************
    /* Internal methods, loading input data
    /**********************************************************************
     */

    private boolean ensureLoaded(int minimum) throws IOException {
        /* Let's assume here buffer has enough room -- this will always
         * be true for the limited used this method gets
         */
        int gotten = (_inputLen - _inputPtr);
        while (gotten < minimum) {
            int count;

            if (_in == null) { // block source
                count = -1;
            } else {
                count = _in.read(_inputBuffer, _inputLen, _inputBuffer.length - _inputLen);
            }
            if (count < 1) {
                return false;
            }
            _inputLen += count;
            gotten += count;
        }
        return true;
    }

    private void loadMore() throws IOException, XMLStreamException {
        _inputProcessed += _inputLen;
        _inputRowStart -= _inputLen;

        _inputPtr = 0;
        if (_in == null) { // block source
            _inputLen = -1;
        } else {
            _inputLen = _in.read(_inputBuffer, 0, _inputBuffer.length);
        }
        if (_inputLen < 1) {
            reportEof();
        }
    }

    /*
    /**********************************************************************
    /* Implementations of abstract parsing methods
    /**********************************************************************
     */

    @Override
    protected void pushback() {
        _inputPtr -= mBytesPerChar;
    }

    @Override
    protected int getNext() throws IOException, XMLStreamException {
        if (mBytesPerChar > 1) {
            return nextMultiByte();
        }
        byte b = (_inputPtr < _inputLen) ? _inputBuffer[_inputPtr++] : nextByte();
        return (b & 0xFF);
    }

    @Override
    protected int getNextAfterWs(boolean reqWs) throws IOException, XMLStreamException {
        int count;

        if (mBytesPerChar > 1) { // multi-byte
            count = skipMbWs();
        } else {
            count = skipSbWs();
        }

        if (reqWs && count == 0) {
            reportUnexpectedChar(getNext(), ERR_XMLDECL_EXP_SPACE);
        }

        // inlined getNext()
        if (mBytesPerChar > 1) {
            return nextMultiByte();
        }
        byte b = (_inputPtr < _inputLen) ? _inputBuffer[_inputPtr++] : nextByte();
        return (b & 0xFF);
    }

    /**
     * @return First character that does not match expected, if any;
     *    CHAR_NULL if match succeeded
     */
    @Override
    protected int checkKeyword(String exp) throws IOException, XMLStreamException {
        if (mBytesPerChar > 1) {
            return checkMbKeyword(exp);
        }
        return checkSbKeyword(exp);
    }

    @Override
    protected int readQuotedValue(char[] kw, int quoteChar) throws IOException, XMLStreamException {
        int i = 0;
        int len = kw.length;
        boolean mb = (mBytesPerChar > 1);

        while (i < len) {
            int c;

            if (mb) {
                c = nextMultiByte();
                if (c == CHAR_CR || c == CHAR_LF) {
                    skipMbLF(c);
                    c = CHAR_LF;
                }
            } else {
                byte b = (_inputPtr < _inputLen) ? _inputBuffer[_inputPtr++] : nextByte();
                if (b == BYTE_NULL) {
                    reportNull();
                }
                if (b == BYTE_CR || b == BYTE_LF) {
                    skipSbLF(b);
                    b = BYTE_LF;
                }
                c = (b & 0xFF);
            }

            if (c == quoteChar) {
                return i;
            }
            kw[i++] = (char) c;
        }

        // If we end up this far, we ran out of buffer space... let's let
        // caller figure that out, though
        return -1;
    }

    @Override
    protected Location getLocation() {
        /* Ok; for fixed-size multi-byte encodings, need to divide numbers
         * to get character locations. For variable-length encodings the
         * good thing is that xml declaration only uses shortest codepoints,
         * ie. char count == byte count.
         */
        int total = _inputProcessed + _inputPtr;
        int col = _inputPtr - _inputRowStart;

        if (mBytesPerChar > 1) {
            total /= mBytesPerChar;
            col /= mBytesPerChar;
        }
        return LocationImpl.fromZeroBased(_config.getPublicId(), _config.getSystemId(), total, _inputRow, col);
    }

    /*
    /**********************************************************************
    /* Internal methods, single-byte access methods
    /**********************************************************************
     */

    private byte nextByte() throws IOException, XMLStreamException {
        if (_inputPtr >= _inputLen) {
            loadMore();
        }
        return _inputBuffer[_inputPtr++];
    }

    private int skipSbWs() throws IOException, XMLStreamException {
        int count = 0;

        while (true) {
            byte b = (_inputPtr < _inputLen) ? _inputBuffer[_inputPtr++] : nextByte();

            if ((b & 0xFF) > CHAR_SPACE) {
                --_inputPtr;
                break;
            }
            if (b == BYTE_CR || b == BYTE_LF) {
                skipSbLF(b);
            } else if (b == BYTE_NULL) {
                reportNull();
            }
            ++count;
        }
        return count;
    }

    private void skipSbLF(byte lfByte) throws IOException, XMLStreamException {
        if (lfByte == BYTE_CR) {
            byte b = (_inputPtr < _inputLen) ? _inputBuffer[_inputPtr++] : nextByte();
            if (b != BYTE_LF) {
                --_inputPtr; // pushback if not 2-char/byte lf
            }
        }
        ++_inputRow;
        _inputRowStart = _inputPtr;
    }

    /**
     * @return First character that does not match expected, if any;
     *    CHAR_NULL if match succeeded
     */
    private int checkSbKeyword(String expected) throws IOException, XMLStreamException {
        int len = expected.length();

        for (int ptr = 1; ptr < len; ++ptr) {
            byte b = (_inputPtr < _inputLen) ? _inputBuffer[_inputPtr++] : nextByte();

            if (b == BYTE_NULL) {
                reportNull();
            }
            if ((b & 0xFF) != expected.charAt(ptr)) {
                return (b & 0xFF);
            }
        }

        return CHAR_NULL;
    }

    /*
    /**********************************************************************
    /* Internal methods, multi-byte access/checks
    /**********************************************************************
     */

    private int nextMultiByte() throws IOException, XMLStreamException {
        byte b = (_inputPtr < _inputLen) ? _inputBuffer[_inputPtr++] : nextByte();
        byte b2 = (_inputPtr < _inputLen) ? _inputBuffer[_inputPtr++] : nextByte();
        int c;

        if (mBytesPerChar == 2) {
            if (mBigEndian) {
                c = ((b & 0xFF) << 8) | (b2 & 0xFF);
            } else {
                c = (b & 0xFF) | ((b2 & 0xFF) << 8);
            }
        } else {
            // Has to be 4 bytes
            byte b3 = (_inputPtr < _inputLen) ? _inputBuffer[_inputPtr++] : nextByte();
            byte b4 = (_inputPtr < _inputLen) ? _inputBuffer[_inputPtr++] : nextByte();

            if (mBigEndian) {
                c = (b << 24) | ((b2 & 0xFF) << 16) | ((b3 & 0xFF) << 8) | (b4 & 0xFF);
            } else {
                c = (b4 << 24) | ((b3 & 0xFF) << 16) | ((b2 & 0xFF) << 8) | (b & 0xFF);
            }
        }

        // Let's catch null chars early
        if (c == 0) {
            reportNull();
        }
        return c;
    }

    private int skipMbWs() throws IOException, XMLStreamException {
        int count = 0;

        while (true) {
            int c = nextMultiByte();

            if (c > CHAR_SPACE) {
                _inputPtr -= mBytesPerChar;
                break;
            }
            if (c == CHAR_CR || c == CHAR_LF) {
                skipMbLF(c);
            } else if (c == CHAR_NULL) {
                reportNull();
            }
            ++count;
        }
        return count;
    }

    private void skipMbLF(int lf) throws IOException, XMLStreamException {
        if (lf == CHAR_CR) {
            int c = nextMultiByte();
            if (c != CHAR_LF) {
                _inputPtr -= mBytesPerChar;
            }
        }
        ++_inputRow;
        _inputRowStart = _inputPtr;
    }

    /**
     * @return First character that does not match expected, if any;
     *    CHAR_NULL if match succeeded
     */
    private int checkMbKeyword(String expected) throws IOException, XMLStreamException {
        int len = expected.length();

        for (int ptr = 1; ptr < len; ++ptr) {
            int c = nextMultiByte();
            if (c == BYTE_NULL) {
                reportNull();
            }
            if (c != expected.charAt(ptr)) {
                return c;
            }
        }

        return CHAR_NULL;
    }

    /*
    /**********************************************************************
    /* Other private methods:
    /**********************************************************************
     */

    private void verifyEncoding(String id, int bpc) throws XMLStreamException {
        if (mByteSizeFound) {
            // Let's verify that if we matched an encoding, it's the same
            // as what was declared...
            if (bpc != mBytesPerChar) {
                reportXmlProblem("Declared encoding '" + id + "' uses " + bpc
                    + " bytes per character; but physical encoding appeared to use " + mBytesPerChar
                    + "; cannot decode");
            }
        }
    }

    private void verifyEncoding(String id, int bpc, boolean bigEndian) throws XMLStreamException {
        if (mByteSizeFound) {
            verifyEncoding(id, bpc);

            if (bigEndian != mBigEndian) {
                String bigStr = bigEndian ? "big" : "little";
                reportXmlProblem("Declared encoding '" + id + "' has different endianness (" + bigStr
                    + " endian) than what physical ordering appeared to be; cannot decode");
            }
        }
    }

    private void reportWeirdUCS4(String type) throws IOException {
        throw new CharConversionException("Unsupported UCS-4 endianness (" + type + ") detected");
    }

    private void reportEBCDIC() throws IOException {
        throw new CharConversionException("Unsupported encoding (EBCDIC)");
    }
}
