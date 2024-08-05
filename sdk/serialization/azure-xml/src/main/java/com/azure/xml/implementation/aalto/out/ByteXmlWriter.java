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

package com.azure.xml.implementation.aalto.out;

import java.io.*;

import javax.xml.stream.*;

import com.azure.xml.implementation.stax2.ri.typed.AsciiValueEncoder;

import com.azure.xml.implementation.aalto.impl.ErrorConsts;
import com.azure.xml.implementation.aalto.util.XmlCharTypes;
import com.azure.xml.implementation.aalto.util.XmlChars;
import com.azure.xml.implementation.aalto.util.XmlConsts;

import static com.azure.xml.implementation.aalto.out.OutputCharTypes.*;

/**
 * This abstract base class (partial implementation of {@link XmlWriter})
 * is used if the destination is byte-based {@link java.io.OutputStream}.
 *<p>
 * Further, all existing implementations are for encodings that
 * are 7-bit ascii compatible. This is important since this means
 * that marker and separator characters are identical independent
 * of actual encoding. This would not hold if support for encodings
 * like EBCDIC were supported using this class.
 */
@SuppressWarnings("fallthrough")
public abstract class ByteXmlWriter extends XmlWriter {
    /**
     * And this value determines size of the intermediate copy buffer
     * to use.
     */
    final static int DEFAULT_FULL_BUFFER_SIZE = 4000;

    /**
     * Let's try avoid short writes, since some output streams have
     * high per-call penalty (like network streams).
     */
    final static int SMALL_WRITE = 250;

    final static byte BYTE_SPACE = (byte) ' ';
    final static byte BYTE_COLON = (byte) ':';
    final static byte BYTE_SEMICOLON = (byte) ';';
    final static byte BYTE_RBRACKET = (byte) ']';
    final static byte BYTE_QMARK = (byte) '?';
    final static byte BYTE_EQ = (byte) '=';
    final static byte BYTE_SLASH = (byte) '/';
    final static byte BYTE_HASH = (byte) '#';
    final static byte BYTE_HYPHEN = (byte) '-';

    final static byte BYTE_LT = (byte) '<';
    final static byte BYTE_GT = (byte) '>';
    final static byte BYTE_AMP = (byte) '&';
    final static byte BYTE_QUOT = (byte) '"';

    final static byte BYTE_A = (byte) 'a';
    final static byte BYTE_G = (byte) 'g';
    final static byte BYTE_L = (byte) 'l';
    final static byte BYTE_M = (byte) 'm';
    final static byte BYTE_O = (byte) 'o';
    final static byte BYTE_P = (byte) 'p';
    final static byte BYTE_Q = (byte) 'q';
    final static byte BYTE_S = (byte) 's';
    final static byte BYTE_T = (byte) 't';
    final static byte BYTE_U = (byte) 'u';
    final static byte BYTE_X = (byte) 'x';

    final static byte[] BYTES_CDATA_START = getAscii("<![CDATA[");
    final static byte[] BYTES_CDATA_END = getAscii("]]>");
    final static byte[] BYTES_COMMENT_START = getAscii("<!--");
    final static byte[] BYTES_COMMENT_END = getAscii("-->");

    final static byte[] BYTES_XMLDECL_START = getAscii("<?xml version=\"");
    final static byte[] BYTES_XMLDECL_ENCODING = getAscii(" encoding=\"");
    final static byte[] BYTES_XMLDECL_STANDALONE = getAscii(" standalone=\"");

    /*
    /**********************************************************************
    /* Output state, buffering
    /**********************************************************************
     */

    /**
     * Actual Writer to use for outputting buffered data as appropriate.
     * During active usage, remains as the writer initially set; set to
     * null when this writer is closed.
     */
    protected OutputStream _out;

    protected byte[] _outputBuffer;

    protected int _outputPtr;

    protected final int _outputBufferLen;

    /**
     * In case a split surrogate pair is output (which can occur for only
     * some of the methods, possibly depending on encoding),
     * the first part is temporarily stored within this member variable.
     */
    protected int _surrogate = 0;

    /*
    /**********************************************************************
    /* Validation
    /**********************************************************************
     */

    /**
     * Validation tables used for verifying validity (and need for quoting)
     */
    final protected XmlCharTypes _charTypes;

    /*
    /**********************************************************************
    /* Life cycle
    /**********************************************************************
     */

    protected ByteXmlWriter(WriterConfig cfg, OutputStream out, XmlCharTypes charTypes) {
        super(cfg);
        _out = out;
        _outputBuffer = cfg.allocFullBBuffer(DEFAULT_FULL_BUFFER_SIZE);
        _outputBufferLen = _outputBuffer.length;
        _outputPtr = 0;
        _charTypes = charTypes;
    }

    @Override
    protected final int getOutputPtr() {
        return _outputPtr;
    }

    /*
    /**********************************************************************
    /* WNameFactory
    /**********************************************************************
     */

    @Override
    public final WName constructName(String localName) throws XMLStreamException {
        verifyNameComponent(localName);
        return doConstructName(localName);
    }

    @Override
    public WName constructName(String prefix, String localName) throws XMLStreamException {
        verifyNameComponent(prefix);
        verifyNameComponent(localName);
        return doConstructName(prefix, localName);
    }

    protected abstract WName doConstructName(String localName) throws XMLStreamException;

    protected abstract WName doConstructName(String prefix, String localName) throws XMLStreamException;

    /**
     * Method used to verify that a name component (prefix, local name)
     * is a legal as per xml 1.0 specification.
     *
     * @throws IllegalArgumentException If name component contains
     *  an invalid (non-name; or for the initial characters,
     *  non-name-first) character.
     */
    protected void verifyNameComponent(String part) throws XMLStreamException {
        if (part == null || part.isEmpty()) {
            reportNwfName(ErrorConsts.WERR_NAME_EMPTY);
        }
        int ch = part.charAt(0);
        int len = part.length();
        int i;

        // First, special case: surrogates...
        if (ch >= SURR1_FIRST && ch <= SURR2_LAST) {
            // Can't start with surr2...
            if (ch >= SURR2_FIRST) {
                reportNwfName("Illegal surrogate pairing in name: first character (" + XmlChars.getCharDesc(ch)
                    + ") not valid surrogate first character");
            }
            // Unpaired? Not good either
            if (len < 2) {
                reportNwfName("Illegal surrogate pairing in name: incomplete surrogate (missing second half)");
            }
            // Otherwise let's decode code point for verification
            ch = calcSurrogate(ch, part.charAt(1), " in name");
            i = 2; // and skip second half of surrogate pair
        } else {
            i = 1;
        }
        if (!XmlChars.is10NameStartChar(ch)) {
            reportNwfName("Invalid name start character " + XmlChars.getCharDesc(ch) + " (name \"" + part + "\")");
        }
        // Also, names can not use entities, must be natively expressable
        final int lastValid = getHighestEncodable();
        if (ch > lastValid) {
            reportNwfName("Illegal name start character " + XmlChars.getCharDesc(ch) + " (name \"" + part
                + "\"): can not be expressed using effective encoding (" + _config.getActualEncoding() + ")");
        }

        for (; i < len; ++i) {
            ch = part.charAt(i);
            if (ch >= SURR1_FIRST && ch <= SURR2_LAST) {
                // Can't start with surr2...
                if (ch >= SURR2_FIRST) {
                    reportNwfName("Illegal surrogate pairing in name: character at #" + i + " ("
                        + XmlChars.getCharDesc(ch) + ") not valid surrogate first character");
                }
                // Unpaired? Not good either
                ++i;
                if (i >= len) {
                    reportNwfName("Illegal surrogate pairing in name: name ends with incomplete surrogate pair");
                }
                // Otherwise let's decode code point for verification
                ch = calcSurrogate(ch, part.charAt(i), " in name");
            }
            if (ch > lastValid) {
                reportNwfName("Illegal name character " + XmlChars.getCharDesc(ch) + " (name \"" + part + "\", index #"
                    + i + "): can not be expressed using effective encoding (" + _config.getActualEncoding() + ")");
            }
            if (!XmlChars.is10NameChar(ch)) {
                reportNwfName(
                    "Invalid name character " + XmlChars.getCharDesc(ch) + ") in name (\"" + part + "\"), index #" + i);
            }
        }
    }

    /*
    /**********************************************************************
    /* Abstract methods
    /**********************************************************************
     */

    /**
     * Method called to output a composite character, result of
     * combining 2 surrogate characters.
     */
    abstract protected void outputSurrogates(int surr1, int surr2) throws IOException, XMLStreamException;

    abstract protected void output2ByteChar(int ch) throws IOException, XMLStreamException;

    /**
     * Method called to output a character beyond basic 1- or 2-byte
     * encoding (code 0x0800 and above); possibly using character
     * entities, if necessary
     */
    abstract protected int outputMultiByteChar(int ch, char[] cbuf, int inputOffset, int inputLen)
        throws IOException, XMLStreamException;

    /*
    /**********************************************************************
    /* Low-level (pass-through) methods
    /**********************************************************************
     */

    @Override
    public void _releaseBuffers() {
        super._releaseBuffers();
        if (_outputBuffer != null) {
            _config.freeFullBBuffer(_outputBuffer);
            _outputBuffer = null;
        }
        if (_copyBuffer != null) {
            _config.freeFullCBuffer(_copyBuffer);
            _copyBuffer = null;
        }
    }

    @Override
    public void _closeTarget(boolean doClose) throws IOException {
        if (_out != null) { // just in case it's called multiple times
            if (doClose) {
                _out.close();
                _out = null;
            }
        }
    }

    @Override
    public final void flush() throws IOException {
        if (_out != null) {
            flushBuffer();
            _out.flush();
        }
    }

    /*
    /**********************************************************************
    /* Write methods, raw
    /**********************************************************************
     */

    @Override
    public final void writeRaw(String text, int offset, int len) throws IOException, XMLStreamException {
        while (len > 0) {
            char[] buf = _copyBuffer;
            final int blen = buf.length;
            final int len2 = Math.min(len, blen);
            text.getChars(offset, offset + len2, buf, 0);
            writeRaw(buf, 0, len2);
            offset += len2;
            len -= len2;
        }
    }

    /**
     * This method is heavily encoding-dependant, so it needs
     * to be deferred to sub-classes
     */
    @Override
    public abstract void writeRaw(char[] cbuf, int offset, int len) throws IOException, XMLStreamException;

    /*
    /**********************************************************************
    /* Write methods, elements
    /**********************************************************************
     */

    @Override
    public final void writeStartTagStart(WName name) throws IOException {
        if (_surrogate != 0) {
            throwUnpairedSurrogate();
        }
        int ptr = _outputPtr;
        if ((ptr + name.serializedLength() + 1) > _outputBufferLen) {
            writeName(BYTE_LT, name); // let's offline slow case
            return;
        }
        byte[] bbuf = _outputBuffer;
        bbuf[ptr++] = BYTE_LT;
        ptr += name.appendBytes(bbuf, ptr);
        _outputPtr = ptr;
    }

    @Override
    public final void writeStartTagEnd() throws IOException {
        // inlined writeRaw(), gets called so often
        if (_surrogate != 0) {
            throwUnpairedSurrogate();
        }
        if (_outputPtr >= _outputBufferLen) {
            flushBuffer();
        }
        _outputBuffer[_outputPtr++] = BYTE_GT;
    }

    @Override
    public void writeStartTagEmptyEnd() throws IOException {
        int ptr = _outputPtr;
        if ((ptr + 2) > _outputBufferLen) {
            flushBuffer();
            ptr = _outputPtr;
        }
        byte[] bbuf = _outputBuffer;
        bbuf[ptr++] = BYTE_SLASH;
        bbuf[ptr++] = BYTE_GT;
        _outputPtr = ptr;
    }

    @Override
    public final void writeEndTag(WName name) throws IOException {
        if (_surrogate != 0) {
            throwUnpairedSurrogate();
        }
        int ptr = _outputPtr;
        int len = name.serializedLength();
        if ((ptr + len + 3) > _outputBufferLen) {
            flushBuffer();
            // name longer than the buffer? can write it straight out
            if ((len + 3) > _outputBufferLen) {
                _out.write(BYTE_LT);
                _out.write(BYTE_SLASH);
                name.writeBytes(_out);
                // Last byte will fit in buffer ok though
                _outputBuffer[_outputPtr++] = BYTE_GT;
                return;
            }
            ptr = _outputPtr;
        }
        byte[] bbuf = _outputBuffer;
        bbuf[ptr++] = BYTE_LT;
        bbuf[ptr++] = BYTE_SLASH;
        ptr += name.appendBytes(bbuf, ptr);
        bbuf[ptr++] = BYTE_GT;
        _outputPtr = ptr;
    }

    /*
    /**********************************************************************
    /* Write methods, attributes
    /**********************************************************************
     */

    @Override
    public final void writeAttribute(WName name, String value) throws IOException, XMLStreamException {
        int vlen = value.length();
        // Let's off-line rare case:
        if (vlen > _copyBufferLen) {
            writeLongAttribute(name, value, vlen);
            return;
        }
        char[] cbuf = _copyBuffer;
        if (vlen > 0) {
            value.getChars(0, vlen, cbuf, 0);
        }
        writeAttribute(name, cbuf, 0, vlen);
    }

    @Override
    public final void writeAttribute(WName name, char[] vbuf, int offset, int vlen)
        throws IOException, XMLStreamException {
        if (_surrogate != 0) {
            throwUnpairedSurrogate();
        }

        // Enough room?
        int ptr = _outputPtr;
        byte[] bbuf = _outputBuffer;

        if ((ptr + name.serializedLength()) >= _outputBufferLen) {
            writeName(BYTE_SPACE, name);
            ptr = _outputPtr;
        } else {
            bbuf[ptr++] = BYTE_SPACE;
            ptr += name.appendBytes(bbuf, ptr);
        }

        // And then the value
        if ((ptr + 3 + vlen) > _outputBufferLen) { // won't fit
            _outputPtr = ptr;
            flushBuffer();
            bbuf[_outputPtr++] = BYTE_EQ;
            bbuf[_outputPtr++] = BYTE_QUOT;
            if ((_outputPtr + vlen + 1) > _outputBufferLen) {
                writeAttrValue(vbuf, offset, vlen);
                writeRaw(BYTE_QUOT);
                return;
            }
            ptr = _outputPtr;
        } else {
            bbuf[ptr++] = BYTE_EQ;
            bbuf[ptr++] = BYTE_QUOT;
        }
        if (vlen > 0) {
            ptr = fastWriteAttrValue(vbuf, offset, vlen, bbuf, ptr);
        }
        bbuf[ptr++] = BYTE_QUOT;
        _outputPtr = ptr;
    }

    /**
     * Method called to copy given attribute value, when it's known that
     * it will completely fit in the output buffer without further checks
     */
    protected final int fastWriteAttrValue(char[] vbuf, int offset, int len, byte[] bbuf, int ptr)
        throws IOException, XMLStreamException {
        len += offset; // now marks the end

        main_loop: while (offset < len) {
            final int[] charTypes = _charTypes.ATTR_CHARS;

            while (true) {
                int ch = vbuf[offset];
                if (ch >= OutputCharTypes.MAIN_TABLE_SIZE) {
                    break;
                }
                if (charTypes[ch] != XmlCharTypes.CT_OK) {
                    // Here we do want to quote linefeed, too
                    break;
                }
                bbuf[ptr++] = (byte) ch;
                if (++offset >= len) {
                    break main_loop;
                }
            }

            _outputPtr = ptr;
            // Ok, so what did we hit? Invalid, or quotable?
            int ch = vbuf[offset++];
            if (ch < OutputCharTypes.MAIN_TABLE_SIZE) {
                switch (charTypes[ch]) {
                    case CT_INVALID:
                        reportInvalidChar(ch);
                        break;

                    case CT_MULTIBYTE_2:
                        output2ByteChar(ch);
                        break;

                    default:
                        writeAsEntity(ch);
                }
            } else {
                offset = outputMultiByteChar(ch, vbuf, offset, len);
            }

            /* Ok, need to mess with buffers a bit: plus, it's possible
             * that we may even need to flush the buffer as the guarantee
             * for fitting may not necessarily hold (but it will after
             * flushing)
             * Still enough room? (also for following quote -- caller
             * relies on that -- that's why >=, not >)
             */
            if ((len - offset) >= (_outputBufferLen - _outputPtr)) {
                flushBuffer();
            }
            ptr = _outputPtr;
        }

        return ptr;
    }

    protected final void writeAttrValue(char[] vbuf, int offset, int len) throws IOException, XMLStreamException {
        if (_surrogate != 0) {
            outputSurrogates(_surrogate, vbuf[offset]);
            //           reset the temporary surrogate storage
            _surrogate = 0;
            ++offset;
            --len;
        }

        len += offset; // now marks the end

        main_loop: while (offset < len) {
            final int[] charTypes = _charTypes.ATTR_CHARS;

            while (true) {
                int ch = vbuf[offset];
                if (ch >= OutputCharTypes.MAIN_TABLE_SIZE) {
                    break;
                }
                if (charTypes[ch] != XmlCharTypes.CT_OK) {
                    break;
                }
                if (_outputPtr >= _outputBufferLen) {
                    flushBuffer();
                }
                _outputBuffer[_outputPtr++] = (byte) ch;
                if (++offset >= len) {
                    break main_loop;
                }
            }

            // Ok, so what did we hit?
            int ch = vbuf[offset++];
            if (ch < OutputCharTypes.MAIN_TABLE_SIZE) {
                switch (charTypes[ch]) {
                    case CT_INVALID:
                        reportInvalidChar(ch);
                    case CT_MULTIBYTE_2:
                        output2ByteChar(ch);
                        break;

                    default:
                        writeAsEntity(ch);
                        break;
                }
            } else {
                offset = outputMultiByteChar(ch, vbuf, offset, len);
            }
        }
    }

    protected final void writeLongAttribute(WName name, String value, int vlen) throws IOException, XMLStreamException {
        writeRaw(BYTE_SPACE);
        int nlen = name.serializedLength();
        if ((_outputPtr + nlen) > _outputBufferLen) {
            flushBuffer();
            if (nlen > _outputBufferLen) {
                name.writeBytes(_out);
            } else {
                _outputPtr += name.appendBytes(_outputBuffer, _outputPtr);
            }
        } else {
            _outputPtr += name.appendBytes(_outputBuffer, _outputPtr);
        }
        writeRaw(BYTE_EQ, BYTE_QUOT);
        int offset = 0;
        while (vlen > 0) {
            char[] buf = _copyBuffer;
            final int blen = buf.length;
            int len2 = Math.min(vlen, blen);
            value.getChars(offset, offset + len2, buf, 0);
            writeAttrValue(buf, 0, len2);
            offset += len2;
            vlen -= len2;
        }
        writeRaw(BYTE_QUOT);
    }

    /*
    /**********************************************************************
    /* Write methods, names
    /**********************************************************************
     */

    protected final void writeName(WName name) throws IOException {
        int ptr = _outputPtr;
        int len = name.serializedLength();
        if ((ptr + len) > _outputBufferLen) {
            flushBuffer();
            // name longer than the buffer? can write it straight out
            if (len >= _outputBufferLen) {
                name.writeBytes(_out);
                return;
            }
            ptr = _outputPtr;
        }
        ptr += name.appendBytes(_outputBuffer, ptr);
        _outputPtr = ptr;
    }

    protected final void writeName(byte preChar, WName name) throws IOException {
        flushBuffer();
        // name longer than the buffer? Need to write it straight out
        int len = name.serializedLength();
        if (len >= _outputBufferLen) {
            _out.write(preChar);
            name.writeBytes(_out);
            return;
        }
        int ptr = _outputPtr;
        byte[] buf = _outputBuffer;
        buf[ptr++] = preChar;
        ptr += name.appendBytes(buf, ptr);
        _outputPtr = ptr;
    }

    private void writeAttrNameEqQ(WName name) throws IOException {
        if (_surrogate != 0) {
            throwUnpairedSurrogate();
        }
        // Enough room for ' attr="' part?
        int nlen = name.serializedLength();
        int ptr = _outputPtr;
        if ((ptr + nlen + 3) >= _outputBufferLen) {
            flushBuffer();
            ptr = _outputPtr;
            // Still won't fit in buffer? Let's output pieces separately
            if ((ptr + nlen + 3) >= _outputBufferLen) {
                writeName(BYTE_SPACE, name);
                writeRaw(BYTE_EQ);
                writeRaw(BYTE_QUOT);
                return;
            }
        }
        byte[] bbuf = _outputBuffer;
        bbuf[ptr++] = BYTE_SPACE;
        ptr += name.appendBytes(bbuf, ptr);
        bbuf[ptr++] = BYTE_EQ;
        bbuf[ptr++] = BYTE_QUOT;
        _outputPtr = ptr;
    }

    /*
    /**********************************************************************
    /* Write methods, textual content
    /**********************************************************************
     */

    /**
     * @return -1 to indicate succesful write, or index of the problematic
     *   character in input (first ']' from "]]&gt;" sequence, in non-fixing
     *   mode)
     */
    @Override
    public int writeCData(String data) throws IOException, XMLStreamException {
        writeCDataStart(); // will check surrogates
        int len = data.length();
        int offset = 0;
        while (len > 0) {
            char[] buf = _copyBuffer;
            int blen = buf.length;

            // Can write all the rest?
            if (blen > len) {
                blen = len;
            }
            // Nope, can only do part
            data.getChars(offset, offset + blen, buf, 0);
            int cix = writeCDataContents(buf, 0, blen);
            if (cix >= 0) {
                return offset + cix;
            }
            offset += blen;
            len -= blen;
        }
        writeCDataEnd(); // will check surrogates
        return -1;
    }

    @Override
    public int writeCData(char[] cbuf, int offset, int len) throws IOException, XMLStreamException {
        writeCDataStart(); // will check surrogates
        int ix = writeCDataContents(cbuf, offset, len);
        if (ix < 0) {
            writeCDataEnd(); // will check surrogates
        }
        return ix;
    }

    protected int writeCDataContents(char[] cbuf, int offset, int len) throws IOException, XMLStreamException {
        if (_surrogate != 0) {
            outputSurrogates(_surrogate, cbuf[offset]);
            // reset the temporary surrogate storage
            _surrogate = 0;
            ++offset;
            --len;
        }

        // Unlike with writeCharacters() and fastWriteName(), let's not
        // worry about split buffers here: this is unlikely to become
        // performance bottleneck. This allows keeping it simple; and
        // should it matter, we could start doing fast version here as well.
        len += offset; // now marks the end

        main_loop: while (offset < len) {
            final int[] charTypes = _charTypes.OTHER_CHARS;

            while (true) {
                int ch = cbuf[offset];
                if (ch >= OutputCharTypes.MAIN_TABLE_SIZE) {
                    break;
                }
                if (charTypes[ch] != XmlCharTypes.CT_OK) {
                    break;
                }
                if (_outputPtr >= _outputBufferLen) {
                    flushBuffer();
                }
                _outputBuffer[_outputPtr++] = (byte) ch;
                if (++offset >= len) {
                    break main_loop;
                }
            }

            // Ok, so what did we hit?
            int ch = cbuf[offset++];
            if (ch < OutputCharTypes.MAIN_TABLE_SIZE) {
                switch (charTypes[ch]) {
                    case CT_INVALID:
                        reportInvalidChar(ch);
                    case CT_WS_CR: // No way to escape within CDATA
                    case CT_WS_LF:
                        ++_locRowNr;
                        break;

                    case CT_OUTPUT_MUST_QUOTE: // == MULTIBYTE_N value
                        reportFailedEscaping("CDATA", ch);
                    case CT_MULTIBYTE_2:
                        // To off-line or not?
                        output2ByteChar(ch);
                        continue;

                    case CT_RBRACKET:
                        /* !!! TBI: Need to split CData? Can do, but what about
                         *   content split around buffer boundary?
                         */
                        if (offset < len && cbuf[offset] == ']') {
                            if ((offset + 1) < len && cbuf[offset + 1] == '>') {
                                // Ok, need to output ']]' first, then end
                                offset += 2;
                                writeRaw(BYTE_RBRACKET, BYTE_RBRACKET);
                                writeCDataEnd();
                                // Then new start, and '>'
                                writeCDataStart();
                                writeRaw(BYTE_GT);
                            }
                            continue;
                        }
                        break;

                    default: // Everything else should be outputtable as is
                        break;
                }
                if (_outputPtr >= _outputBufferLen) {
                    flushBuffer();
                }
                _outputBuffer[_outputPtr++] = (byte) ch;
            } else { // beyond 2-byte encodables; 3-byte, surrogates?
                offset = outputMultiByteChar(ch, cbuf, offset, len);
            }
        }
        return -1;
    }

    @Override
    public final void writeCharacters(String text) throws IOException, XMLStreamException {
        final int len = text.length();

        // Not so common case, let's offline:
        if (len > _copyBufferLen) {
            longWriteCharacters(text);
            return;
        }
        if (len > 0) {
            char[] buf = _copyBuffer;
            text.getChars(0, len, buf, 0);
            writeCharacters(buf, 0, len);
        }
    }

    private void longWriteCharacters(String text) throws IOException, XMLStreamException {
        int offset = 0;
        int len = text.length();
        char[] buf = _copyBuffer;

        do {
            final int blen = buf.length;
            int len2 = Math.min(len, blen);
            text.getChars(offset, offset + len2, buf, 0);
            writeCharacters(buf, 0, len2);
            offset += len2;
            len -= len2;
        } while (len > 0);
    }

    @Override
    public final void writeCharacters(char[] cbuf, int offset, int len) throws IOException, XMLStreamException {
        if (_surrogate != 0) {
            outputSurrogates(_surrogate, cbuf[offset]);
            //           reset the temporary surrogate storage
            _surrogate = 0;
            ++offset;
            --len;
        }

        // Ok, let's offline (what's sure to be) slow case first:
        // (with multi-byte chars, others may be, too).
        int ptr = _outputPtr;
        if ((ptr + len) > _outputBufferLen) {
            writeSplitCharacters(cbuf, offset, len);
            return;
        }
        len += offset; // now marks the end

        main_loop: while (offset < len) {
            final int[] charTypes = _charTypes.TEXT_CHARS;

            while (true) {
                int ch = cbuf[offset];
                if (ch >= OutputCharTypes.MAIN_TABLE_SIZE) {
                    break;
                }
                if (charTypes[ch] != XmlCharTypes.CT_OK) {
                    // This may look weird, but profiling showed that handling of LFs
                    // for indentation has measurable effect; plus, that checking it
                    // here will not slow down inner loop either
                    if (ch != '\n') {
                        break;
                    }
                    ++_locRowNr;
                }
                _outputBuffer[ptr++] = (byte) ch;
                if (++offset >= len) {
                    break main_loop;
                }
            }
            // Ok, so what did we hit?
            int ch = cbuf[offset++];
            if (ch < OutputCharTypes.MAIN_TABLE_SIZE) {
                switch (charTypes[ch]) {
                    case CT_INVALID:
                        reportInvalidChar(ch);
                    case CT_WS_CR:
                        // !!! TBI: line count
                        // Also, CR to be quoted?
                        if (_config.willEscapeCR()) {
                            _outputPtr = ptr;
                            writeAsEntity(ch);
                            break;
                        }
                        _outputBuffer[ptr++] = (byte) ch;
                        ++_locRowNr;
                        continue;

                    case CT_WS_LF: // never occurs (handled in loop), but don't want to leave gaps
                        break;

                    case CT_OUTPUT_MUST_QUOTE: // == MULTIBYTE_N value
                    case CT_LT:
                    case CT_AMP:
                        _outputPtr = ptr;
                        writeAsEntity(ch);
                        break;

                    case CT_MULTIBYTE_2:
                        // To off-line or not?
                        _outputPtr = ptr;
                        output2ByteChar(ch);
                        break;

                    case CT_RBRACKET: // may need to quote as well...
                        // Let's not quote if known not to be followed by '>'
                        if (offset >= len || cbuf[offset] == '>') {
                            _outputPtr = ptr;
                            writeAsEntity(ch);
                            break;
                        }
                        //  fall through
                    default:
                        _outputBuffer[ptr++] = (byte) ch;
                        continue;
                }
            } else { // beyond 2-byte encodables; 3-byte, surrogates?
                _outputPtr = ptr;
                offset = outputMultiByteChar(ch, cbuf, offset, len);
            }

            /* At this point, it's not guaranteed any more that we'll
             * be able to fit all output into buffer without checks.
             * Let's verify: in the worst case, we'll just flush
             * whatever we had, to gain more room.
             */
            if ((len - offset) >= (_outputBufferLen - _outputPtr)) {
                flushBuffer();
            }
            ptr = _outputPtr;
        }
        _outputPtr = ptr;
    }

    /**
     * This method is called when it is possible that the output
     * may cross the output buffer boundary. Because of this, code
     * has to add more boundary checks.
     */
    private void writeSplitCharacters(char[] cbuf, int offset, int len) throws IOException, XMLStreamException {
        // Note: caller handled surrogate already

        len += offset; // now marks the end

        main_loop: while (offset < len) {
            final int[] charTypes = _charTypes.TEXT_CHARS;

            while (true) {
                int ch = cbuf[offset];
                if (ch >= OutputCharTypes.MAIN_TABLE_SIZE) {
                    break;
                }
                if (charTypes[ch] != XmlCharTypes.CT_OK) {
                    if (ch != '\n') {
                        break;
                    }
                    ++_locRowNr;
                }
                if (_outputPtr >= _outputBufferLen) {
                    flushBuffer();
                }
                _outputBuffer[_outputPtr++] = (byte) ch;
                if (++offset >= len) {
                    break main_loop;
                }
            }

            // Ok, so what did we hit?
            int ch = cbuf[offset++];
            if (ch < OutputCharTypes.MAIN_TABLE_SIZE) {
                switch (charTypes[ch]) {
                    case CT_INVALID:
                        reportInvalidChar(ch);
                    case CT_WS_CR:
                        // !!! TBI: line count
                        // Also, CR to be quoted?
                        if (_config.willEscapeCR()) {
                            writeAsEntity(ch);
                            continue;
                        }
                        ++_locRowNr;
                        break;

                    case CT_WS_LF: // can not occur, handled above, but let's keep sequence
                        break;

                    case CT_OUTPUT_MUST_QUOTE:
                    case CT_LT:
                    case CT_AMP:
                        writeAsEntity(ch);
                        continue;

                    case CT_MULTIBYTE_2: // 3, 4 and N can never occur
                        // To off-line or not?
                        output2ByteChar(ch);
                        continue;

                    case CT_RBRACKET: // may need to quote as well...
                        // Let's not quote if known not to be followed by '>'
                        if (offset >= len || cbuf[offset] == '>') {
                            writeAsEntity(ch);
                            continue;
                        }
                        break;

                    default:
                        break;
                }
            } else { // beyond 2-byte encodables; 3-byte, surrogates?
                offset = outputMultiByteChar(ch, cbuf, offset, len);
                continue;
            }

            if (_outputPtr >= _outputBufferLen) {
                flushBuffer();
            }
            _outputBuffer[_outputPtr++] = (byte) ch;
        }
    }

    /*
    /**********************************************************************
    /* Write methods, typed (element) content
    /**********************************************************************
     */

    @Override
    public void writeTypedValue(AsciiValueEncoder enc) throws IOException {
        if (_surrogate != 0) {
            throwUnpairedSurrogate();
        }
        int free = _outputBufferLen - _outputPtr;
        if (enc.bufferNeedsFlush(free)) {
            flush();
        }
        while (true) {
            _outputPtr = enc.encodeMore(_outputBuffer, _outputPtr, _outputBufferLen);
            if (enc.isCompleted()) {
                break;
            }
            flushBuffer();
        }
    }

    @Override
    public final void writeAttribute(WName name, AsciiValueEncoder enc) throws IOException, XMLStreamException {
        writeAttrNameEqQ(name);

        // (inlined writeTypedVAlue()...)

        int free = _outputBufferLen - _outputPtr;
        if (enc.bufferNeedsFlush(free)) {
            flush();
        }
        while (true) {
            _outputPtr = enc.encodeMore(_outputBuffer, _outputPtr, _outputBufferLen);
            if (enc.isCompleted()) {
                break;
            }
            flushBuffer();
        }

        // (end of inlined writeTypedVAlue()...)

        if (_outputPtr >= _outputBufferLen) {
            flushBuffer();
        }
        _outputBuffer[_outputPtr++] = BYTE_QUOT;
    }

    /*
    /**********************************************************************
    /* Write methods, other
    /**********************************************************************
     */

    /**
     * Method that will try to output the content as specified. If
     * the content passed in has embedded "--" in it, it will either
     * add an intervening space between consequtive hyphens (if content
     * fixing is enabled), or return the offset of the first hyphen in
     * multi-hyphen sequence.
     */
    @Override
    public int writeComment(String data) throws IOException, XMLStreamException {
        writeCommentStart();

        int len = data.length();
        int offset = 0;
        while (len > 0) {
            char[] buf = _copyBuffer;
            final int blen = buf.length;
            int len2 = Math.min(len, blen);
            // Nope, can only do part
            data.getChars(offset, offset + len2, buf, 0);
            int cix = writeCommentContents(buf, 0, len2);
            if (cix >= 0) {
                return offset + cix;
            }
            offset += blen;
            len -= blen;
        }
        writeCommentEnd();
        return -1;
    }

    /**
     * Note: the only way to fix comment contents is to inject a space
     * to split up consecutive '--' (or '-' that ends a comment).
     */
    protected int writeCommentContents(char[] cbuf, int offset, int len) throws IOException, XMLStreamException {
        if (_surrogate != 0) {
            outputSurrogates(_surrogate, cbuf[offset]);
            // reset the temporary surrogate storage
            _surrogate = 0;
            ++offset;
            --len;
        }

        // Unlike with writeCharacters() and fastWriteName(), let's not
        // worry about split buffers here: this is unlikely to become
        // performance bottleneck. This allows keeping it simple; and
        // should it matter, we could start doing fast version here as well.
        len += offset; // now marks the end

        main_loop: while (offset < len) {
            final int[] charTypes = _charTypes.OTHER_CHARS;

            while (true) {
                int ch = cbuf[offset];
                if (ch >= OutputCharTypes.MAIN_TABLE_SIZE) {
                    break;
                }
                if (charTypes[ch] != XmlCharTypes.CT_OK) {
                    break;
                }
                if (_outputPtr >= _outputBufferLen) {
                    flushBuffer();
                }
                _outputBuffer[_outputPtr++] = (byte) ch;
                if (++offset >= len) {
                    break main_loop;
                }
            }

            // Ok, so what did we hit?
            int ch = cbuf[offset++];
            if (ch < OutputCharTypes.MAIN_TABLE_SIZE) {
                switch (charTypes[ch]) {
                    case CT_INVALID:
                        reportInvalidChar(ch);
                    case CT_WS_CR: // No way to escape within CDATA
                    case CT_WS_LF:
                        ++_locRowNr;
                        break;

                    case CT_OUTPUT_MUST_QUOTE: // == MULTIBYTE_N value
                        reportFailedEscaping("comment", ch);
                    case CT_MULTIBYTE_2:
                        // To off-line or not?
                        output2ByteChar(ch);
                        continue;

                    case CT_HYPHEN:
                        // No need if followed by non hyphen
                        if (offset < len && cbuf[offset] != '-') {
                            break;
                        }
                        // Two hyphens, or hyphen at end; must append a space
                        writeRaw(BYTE_HYPHEN, BYTE_SPACE);
                        continue;

                    default: // Everything else should be outputtable as is
                        break;
                }
                if (_outputPtr >= _outputBufferLen) {
                    flushBuffer();
                }
                _outputBuffer[_outputPtr++] = (byte) ch;
            } else { // beyond 2-byte encodables; 3-byte, surrogates?
                offset = outputMultiByteChar(ch, cbuf, offset, len);
            }
        }
        return -1;
    }

    @Override
    public void writeDTD(String data) throws IOException, XMLStreamException {
        // !!! TBI: Check for char validity, similar to other methods?
        writeRaw(data, 0, data.length());
    }

    @Override
    public void writeDTD(WName rootName, String systemId, String publicId, String internalSubset)
        throws IOException, XMLStreamException {
        // !!! TBI
        //if (true) throw new RuntimeException("DTD not implemented yet");
    }

    protected int writePIData(char[] cbuf, int offset, int len) throws IOException, XMLStreamException {
        if (_surrogate != 0) {
            outputSurrogates(_surrogate, cbuf[offset]);
            // reset the temporary surrogate storage
            _surrogate = 0;
            ++offset;
            --len;
        }

        // Unlike with writeCharacters() and fastWriteName(), let's not
        // worry about split buffers here: this is unlikely to become
        // performance bottleneck. This allows keeping it simple; and
        // should it matter, we could start doing fast version here as well.
        len += offset; // now marks the end

        main_loop: while (offset < len) {
            final int[] charTypes = _charTypes.OTHER_CHARS;

            while (true) {
                int ch = cbuf[offset];
                if (ch >= OutputCharTypes.MAIN_TABLE_SIZE) {
                    break;
                }
                if (charTypes[ch] != XmlCharTypes.CT_OK) {
                    break;
                }
                if (_outputPtr >= _outputBufferLen) {
                    flushBuffer();
                }
                _outputBuffer[_outputPtr++] = (byte) ch;
                if (++offset >= len) {
                    break main_loop;
                }
            }

            // Ok, so what did we hit?
            int ch = cbuf[offset++];
            if (ch < OutputCharTypes.MAIN_TABLE_SIZE) {
                switch (charTypes[ch]) {
                    case CT_INVALID:
                        reportInvalidChar(ch);
                    case CT_WS_CR: // No way to escape within CDATA
                    case CT_WS_LF:
                        ++_locRowNr;
                        break;

                    case CT_OUTPUT_MUST_QUOTE: // == MULTIBYTE_N value
                        reportFailedEscaping("processing instruction", ch);
                    case CT_MULTIBYTE_2:
                        // To off-line or not?
                        output2ByteChar(ch);
                        continue;

                    case CT_QMARK:
                        // Problem, if we have '?>'
                        if (offset < len && cbuf[offset] == '>') {
                            return offset;
                        }
                        break;

                    default: // Everything else should be outputtable as is
                        break;
                }
                if (_outputPtr >= _outputBufferLen) {
                    flushBuffer();
                }
                _outputBuffer[_outputPtr++] = (byte) ch;
            } else { // beyond 2-byte encodables; 3-byte, surrogates?
                offset = outputMultiByteChar(ch, cbuf, offset, len);
            }
        }
        return -1;
    }

    @Override
    public void writeEntityReference(WName name) throws IOException {
        writeRaw(BYTE_AMP); // will check surrogates
        writeName(name);
        writeRaw(BYTE_SEMICOLON);
    }

    @Override
    public int writePI(WName target, String data) throws IOException, XMLStreamException {
        writeRaw(BYTE_LT, BYTE_QMARK);
        writeName(target);
        if (data != null) {
            // Need to split etc
            writeRaw(BYTE_SPACE);

            int len = data.length();
            int offset = 0;
            while (len > 0) {
                char[] buf = _copyBuffer;
                int blen = buf.length;

                // Can write all the rest?
                if (blen > len) {
                    blen = len;
                }
                // Nope, can only do part
                data.getChars(offset, offset + blen, buf, 0);
                int cix = writePIData(buf, 0, blen);
                if (cix >= 0) {
                    return offset + cix;
                }
                offset += blen;
                len -= blen;
            }
        }
        writeRaw(BYTE_QMARK, BYTE_GT);
        return -1;
    }

    @Override
    public final void writeSpace(String data) throws IOException, XMLStreamException {
        int len = data.length();
        int offset = 0;

        while (len > 0) {
            char[] buf = _copyBuffer;
            final int blen = buf.length;
            int len2 = Math.min(len, blen);
            data.getChars(offset, offset + len2, buf, 0);
            writeSpace(buf, 0, len2);
            offset += len2;
            len -= len2;
        }
    }

    @Override
    public void writeSpace(char[] cbuf, int offset, int len) throws IOException, XMLStreamException {
        if (_out == null) {
            return;
        }
        if (_surrogate != 0) { // can this actually happen?
            reportNwfContent(ErrorConsts.WERR_SPACE_CONTENT, _surrogate, offset - 1);
        }

        len += offset; // now marks the end
        while (offset < len) {
            char ch = cbuf[offset++];
            if (ch > 0x0020) {
                if (!_config.isXml11() || (ch != 0x0085 && ch != 0x2028)) {
                    reportNwfContent(ErrorConsts.WERR_SPACE_CONTENT, (int) ch, offset - 1);
                }
            }
            if (_outputPtr >= _outputBufferLen) {
                flushBuffer();
            }
            // !!! Line counts?
            _outputBuffer[_outputPtr++] = (byte) ch;
        }
    }

    @Override
    public void writeXmlDeclaration(String version, String encoding, String standalone)
        throws IOException, XMLStreamException {
        writeRaw(BYTES_XMLDECL_START); // will check surrogates
        // !!! TBI: check validity
        writeRaw(version, 0, version.length());
        writeRaw(BYTE_QUOT);

        if (encoding != null && !encoding.isEmpty()) {
            writeRaw(BYTES_XMLDECL_ENCODING);
            // !!! TBI: check validity
            writeRaw(encoding, 0, encoding.length());
            writeRaw(BYTE_QUOT);
        }
        if (standalone != null) {
            writeRaw(BYTES_XMLDECL_STANDALONE);
            // !!! TBI: check validity
            writeRaw(standalone, 0, standalone.length());
            writeRaw(BYTE_QUOT);
        }
        writeRaw(BYTE_QMARK, BYTE_GT);
    }

    /*
    /**********************************************************************
    /* Shared helper output methods
    /**********************************************************************
     */

    protected final void writeCDataStart() throws IOException {
        writeRaw(BYTES_CDATA_START);
    }

    protected final void writeCDataEnd() throws IOException {
        writeRaw(BYTES_CDATA_END);
    }

    protected final void writeCommentStart() throws IOException {
        writeRaw(BYTES_COMMENT_START);
    }

    protected final void writeCommentEnd() throws IOException {
        writeRaw(BYTES_COMMENT_END);
    }

    /*
    /**********************************************************************
    /* Write methods, raw (unprocessed) output
    /**********************************************************************
     */

    protected final void writeRaw(byte b) throws IOException {
        if (_surrogate != 0) {
            throwUnpairedSurrogate();
        }
        if (_outputPtr >= _outputBufferLen) {
            flushBuffer();
        }
        _outputBuffer[_outputPtr++] = b;
    }

    protected final void writeRaw(byte b1, byte b2) throws IOException {
        if (_surrogate != 0) {
            throwUnpairedSurrogate();
        }
        if ((_outputPtr + 1) >= _outputBufferLen) {
            flushBuffer();
        }
        _outputBuffer[_outputPtr++] = b1;
        _outputBuffer[_outputPtr++] = b2;
    }

    protected final void writeRaw(byte[] buf) throws IOException {
        writeRaw(buf, 0, buf.length);
    }

    protected final void writeRaw(byte[] buf, int offset, int len) throws IOException {
        if (_surrogate != 0) {
            throwUnpairedSurrogate();
        }

        int ptr = _outputPtr;
        // Common case: fits right in the buffer
        if ((ptr + len) <= _outputBufferLen) {
            System.arraycopy(buf, offset, _outputBuffer, ptr, len);
            _outputPtr += len;
            return;
        }

        // If not, should we just flush + write?
        if (ptr > 0) {
            flush();
            ptr = _outputPtr;
        }
        if (len < SMALL_WRITE) {
            System.arraycopy(buf, offset, _outputBuffer, ptr, len);
            _outputPtr += len;
        } else {
            _out.write(buf, offset, len);
        }
    }

    /*
    /**********************************************************************
    /* Internal methods, problem reporting
    /**********************************************************************
     */

    protected final void throwUnpairedSurrogate() throws IOException {
        int surr = _surrogate;
        _surrogate = 0;
        throwUnpairedSurrogate(surr);
    }

    protected final void throwUnpairedSurrogate(int code) throws IOException {
        // Let's flush to make debugging easier
        flush();
        throw new IOException("Unpaired surrogate character (0x" + Integer.toHexString(code) + ")");
    }

    /*
    /**********************************************************************
    /* Helper methods for sub-classes
    /**********************************************************************
     */

    protected final void flushBuffer() throws IOException {
        if ((_outputPtr > 0) && (_out != null)) {
            int ptr = _outputPtr;
            // Need to update location info, to keep it in sync
            _locPastChars += ptr;
            _locRowStartOffset -= ptr;
            _outputPtr = 0;
            _out.write(_outputBuffer, 0, ptr);
        }
    }

    protected final void writeAsEntity(int c) throws IOException {
        // Quickie check to avoid

        byte[] buf = _outputBuffer;
        int ptr = _outputPtr;
        if ((ptr + 10) >= buf.length) { // &#x [up to 6 hex digits] ;
            flushBuffer();
            ptr = _outputPtr;
        }
        buf[ptr++] = BYTE_AMP;

        // Can use more optimal notation for 8-bit ascii stuff:
        if (c < 256) {
            /* Also; although not really mandatory, let's also
             * use pre-defined entities where possible.
             */
            if (c == '&') {
                buf[ptr++] = BYTE_A;
                buf[ptr++] = BYTE_M;
                buf[ptr++] = BYTE_P;
            } else if (c == '<') {
                buf[ptr++] = BYTE_L;
                buf[ptr++] = BYTE_T;
            } else if (c == '>') {
                buf[ptr++] = BYTE_G;
                buf[ptr++] = BYTE_T;
            } else if (c == '\'') {
                buf[ptr++] = BYTE_A;
                buf[ptr++] = BYTE_P;
                buf[ptr++] = BYTE_O;
                buf[ptr++] = BYTE_S;
            } else if (c == '"') {
                buf[ptr++] = BYTE_Q;
                buf[ptr++] = BYTE_U;
                buf[ptr++] = BYTE_O;
                buf[ptr++] = BYTE_T;
            } else {
                buf[ptr++] = BYTE_HASH;
                buf[ptr++] = BYTE_X;
                // Can use shortest quoting for tab, cr, lf:
                if (c >= 16) {
                    int digit = (c >> 4);
                    buf[ptr++] = (byte) ((digit < 10) ? ('0' + digit) : (('a' - 10) + digit));
                    c &= 0xF;
                }
                buf[ptr++] = (byte) ((c < 10) ? ('0' + c) : (('a' - 10) + c));
            }
        } else {
            buf[ptr++] = BYTE_HASH;
            buf[ptr++] = BYTE_X;

            // Ok, let's write the shortest possible sequence then:
            int shift = 20;
            int origPtr = ptr;

            do {
                int digit = (c >> shift) & 0xF;
                if (digit > 0 || (ptr != origPtr)) {
                    buf[ptr++] = (byte) ((digit < 10) ? ('0' + digit) : (('a' - 10) + digit));
                }
                shift -= 4;
            } while (shift > 0);
            c &= 0xF;
            buf[ptr++] = (byte) ((c < 10) ? ('0' + c) : (('a' - 10) + c));
        }
        buf[ptr++] = ';';
        _outputPtr = ptr;
    }

    protected final int calcSurrogate(int surr1, int surr2, String context) throws XMLStreamException {
        // First is known to be valid, but how about the other?
        if (surr2 < SURR2_FIRST || surr2 > SURR2_LAST) {
            String msg = "Incomplete surrogate pair" + context + ": first char 0x" + Integer.toHexString(surr1)
                + ", second 0x" + Integer.toHexString(surr2);
            reportNwfContent(msg);
        }
        int c = 0x10000 + ((surr1 - SURR1_FIRST) << 10) + (surr2 - SURR2_FIRST);
        if (c > XmlConsts.MAX_UNICODE_CHAR) { // illegal, as per RFC 3629
            reportInvalidChar(c);
        }
        return c;
    }

    /*
    /**********************************************************************
    /* Internal helper methods
    /**********************************************************************
     */

    protected static byte[] getAscii(String str) {
        int len = str.length();
        byte[] result = new byte[len];
        getAscii(str, result, 0);
        return result;
    }

    protected static void getAscii(String str, byte[] result, int offset) {
        int len = str.length();
        for (int i = 0; i < len; ++i) {
            result[offset + i] = (byte) str.charAt(i);
        }
    }
}
