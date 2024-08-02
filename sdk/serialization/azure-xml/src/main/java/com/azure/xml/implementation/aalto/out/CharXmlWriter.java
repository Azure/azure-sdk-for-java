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

package com.azure.xml.implementation.aalto.out;

import java.io.*;

import javax.xml.stream.*;

import com.azure.xml.implementation.stax2.ri.typed.AsciiValueEncoder;

import com.azure.xml.implementation.aalto.impl.ErrorConsts;
import com.azure.xml.implementation.aalto.io.UTF8Writer;
import com.azure.xml.implementation.aalto.util.XmlCharTypes;

import static com.azure.xml.implementation.aalto.out.OutputCharTypes.*;

/**
 * This is the generic implementation of {@link XmlWriter}, used if
 * the destination is a character based, like a {@link java.io.Writer}.
 */
public final class CharXmlWriter extends XmlWriter {
    /**
     * This value determines a threshold to choose how much data do
     * we want to buffer at minimum, before output. This is done since
     * actual underlying writer may have significant per-call overhead,
     * and if so, it is much cheaper to coalesce content. But on the other
     * hand, this extra buffering has overhead of its own, so we'll try
     * to find a sweet spot.
     */
    final static int DEFAULT_SMALL_SIZE = 200;

    /**
     * And this value determines size of the intermediate copy buffer
     * to use.
     */
    final static int DEFAULT_FULL_BUFFER_SIZE = 1000;

    /*
    ////////////////////////////////////////////////
    // Output state, buffering
    ////////////////////////////////////////////////
     */

    /**
     * This is the threshold used to check what is considered a "small"
     * write; small writes will be buffered until resulting size will
     * be above the threshold.
     */
    protected final int mSmallWriteSize;

    /**
     * Actual Writer to use for outputting buffered data as appropriate.
     * During active usage, remains as the writer initially set; set to
     * null when this writer is closed.
     */
    protected Writer _out;

    protected char[] _outputBuffer;

    protected int _outputPtr;

    protected final int _outputBufferLen;

    /*
    ////////////////////////////////////////////////
    // Encoding/escaping configuration
    ////////////////////////////////////////////////
     */

    /**
     * First Unicode character (one with lowest value) after (and including)
     * which character entities have to be used.
     */
    private final int mEncHighChar;

    /**
     * First Unicode character that is NOT to be checked against static
     * validity table. Usually the size of check table, but lower for
     * some encodings (like ascii)
     */
    private final int mTableCheckEnd;

    /*
    ////////////////////////////////////////////////
    // Validation
    ////////////////////////////////////////////////
     */

    /**
     * Validation tables used for verifying validity (and need for quoting)
     */
    final XmlCharTypes mCharTypes;

    /*
    ////////////////////////////////////////////////
    // Life-cycle
    ////////////////////////////////////////////////
     */

    public CharXmlWriter(WriterConfig cfg, Writer out) {
        super(cfg);
        _out = out;
        _outputBuffer = cfg.allocFullCBuffer(DEFAULT_FULL_BUFFER_SIZE);
        _outputBufferLen = _outputBuffer.length;
        mSmallWriteSize = DEFAULT_SMALL_SIZE;
        _outputPtr = 0;

        /* Note: let's actually exclude some illegal and potentially illegal
         * chars from unicode-based encoders (specifically, 0xFFFE/0xFFFF
         * which are illegal; and surrogates, which either need to be validated
         * or combined). We can do some validity checks 'for free' (or at
         * least more cheaply) this way.
         */
        // But can we really handle surrogates this way?
        int bitsize = guessEncodingBitSize(cfg);

        //mEncHighChar = ((bitsize < 16) ? (1 << bitsize) : SURR1_FIRST);
        mEncHighChar = ((bitsize < 16) ? (1 << bitsize) : 0xFFFE);
        mTableCheckEnd = Math.min(256, mEncHighChar);
        /* Hmmh... Latin1 is the closest match, for table checks... unless
         * well, we have ascii (etc)
         */
        mCharTypes = (bitsize < 8) ? OutputCharTypes.getAsciiCharTypes() : OutputCharTypes.getLatin1CharTypes();
    }

    @Override
    protected int getOutputPtr() {
        return _outputPtr;
    }

    @Override
    public int getHighestEncodable() {
        return mEncHighChar;
    }

    /*
    ///////////////////////////////////////////////////////
    // WNameFactory implementation
    ///////////////////////////////////////////////////////
     */

    @Override
    public WName constructName(String localName) {
        return new CharWName(localName);
    }

    @Override
    public WName constructName(String prefix, String localName) {
        return new CharWName(prefix, localName);
    }

    /*
    ////////////////////////////////////////////////
    // Low-level (pass-through) methods
    ////////////////////////////////////////////////
     */

    @Override
    public void _releaseBuffers() {
        super._releaseBuffers();
        if (_outputBuffer != null) {
            _config.freeFullCBuffer(_outputBuffer);
            _outputBuffer = null;
        }
    }

    @Override
    public void _closeTarget(boolean doClose) throws IOException {
        if (_out != null) { // just in case it's called multiple times
            /* 27-Dec-2008, tatu: There is a good reason for adding
             *   the second check... but I'll be damned if I rememeber
             *   what exactly it was right now.
             */
            if (doClose || (_out instanceof UTF8Writer)) {
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

    @Override
    public void writeRaw(char[] cbuf, int offset, int len) throws IOException {
        if (_out == null) {
            return;
        }

        // First; is the new request small or not? If yes, needs to be buffered
        if (len < mSmallWriteSize) { // yup
            // Does it fit in with current buffer? If not, need to flush first
            if ((_outputPtr + len) > _outputBufferLen) {
                flushBuffer();
            }
            System.arraycopy(cbuf, offset, _outputBuffer, _outputPtr, len);
            _outputPtr += len;
            return;
        }

        // Ok, not a small request. But buffer may have existing content?
        int ptr = _outputPtr;
        if (ptr > 0) {
            // If it's a small chunk, need to fill enough before flushing
            if (ptr < mSmallWriteSize) {
                /* Also, if we are to copy any stuff, let's make sure
                 * that we either copy it all in one chunk, or copy
                 * enough for non-small chunk, flush, and output remaining
                 * non-small chink (former possible if chunk we were requested
                 * to output is only slightly over 'small' size)
                 */
                int needed = (mSmallWriteSize - ptr);

                // Just need minimal copy:
                System.arraycopy(cbuf, offset, _outputBuffer, ptr, needed);
                _outputPtr = ptr + needed;
                len -= needed;
                offset += needed;
            }
            flushBuffer();
        }

        // And then we'll just write whatever we have left:
        _out.write(cbuf, offset, len);
    }

    @Override
    public void writeRaw(String str, int offset, int len) throws IOException {
        if (_out == null) {
            return;
        }

        // First; is the new request small or not? If yes, needs to be buffered
        if (len < mSmallWriteSize) { // yup
            // Does it fit in with current buffer? If not, need to flush first
            if ((_outputPtr + len) >= _outputBufferLen) {
                flushBuffer();
            }
            str.getChars(offset, offset + len, _outputBuffer, _outputPtr);
            _outputPtr += len;
            return;
        }

        // Ok, not a small request. But buffer may have existing content?
        int ptr = _outputPtr;
        if (ptr > 0) {
            // If it's a small chunk, need to fill enough before flushing
            if (ptr < mSmallWriteSize) {
                /* Also, if we are to copy any stuff, let's make sure
                 * that we either copy it all in one chunk, or copy
                 * enough for non-small chunk, flush, and output remaining
                 * non-small chunk (former possible if chunk we were requested
                 * to output is only slightly over 'small' size)
                 */
                int needed = (mSmallWriteSize - ptr);

                // Just need minimal copy:
                str.getChars(offset, offset + needed, _outputBuffer, ptr);
                _outputPtr = ptr + needed;
                len -= needed;
                offset += needed;
            }
            flushBuffer();
        }

        // And then we'll just write whatever we have left:
        _out.write(str, offset, len);
    }

    /*
    ////////////////////////////////////////////////
    // "Trusted" low-level output methods
    ////////////////////////////////////////////////
     */

    public final void writeCDataStart() throws IOException {
        fastWriteRaw("<![CDATA[");
    }

    public final void writeCDataEnd() throws IOException {
        fastWriteRaw("]]>");
    }

    public final void writeCommentStart() throws IOException {
        fastWriteRaw("<!--");
    }

    public final void writeCommentEnd() throws IOException {
        fastWriteRaw("-->");
    }

    /*
    ////////////////////////////////////////////////
    // Higher-level output methods, text output
    ////////////////////////////////////////////////
     */

    /**
     * @return -1 to indicate succesful write, or index of the problematic
     *   character in input (first ']' from "]]&gt;" sequence, in non-fixing
     *   mode)
     */
    @Override
    public int writeCData(String data) throws IOException, XMLStreamException {
        writeCDataStart();
        /* Ok, let's just copy into a temporary buffer. While copying
         * to the output buffer would be faster, it gets pretty
         * complicated; so let's not bother (yet?)
         */
        int len = data.length();
        int offset = 0;

        while (len > 0) {
            char[] buf = _copyBuffer;
            final int blen = buf.length;
            int len2 = (len < blen) ? len : blen;
            data.getChars(offset, offset + len2, buf, 0);
            int cix = writeCDataContents(buf, 0, len2);
            if (cix >= 0) {
                return (offset + cix);
            }
            offset += len2;
            len -= len2;
        }
        writeCDataEnd();
        return -1;
    }

    @Override
    public int writeCData(char[] cbuf, int offset, int len) throws IOException, XMLStreamException {
        writeCDataStart();
        int ix = writeCDataContents(cbuf, offset, len);
        if (ix < 0) { // means it went ok, so can close
            writeCDataEnd();
        }
        return -1;
    }

    private int writeCDataContents(char[] cbuf, int offset, int len) throws IOException, XMLStreamException {
        len += offset;

        final int start = offset;

        main_loop: while (offset < len) {
            final int[] charTypes = mCharTypes.OTHER_CHARS;
            final int limit = mTableCheckEnd;

            inner_loop: while (true) {
                char ch = cbuf[offset];
                if (ch >= limit) {
                    break inner_loop;
                }
                if (charTypes[ch] != XmlCharTypes.CT_OK) {
                    break;
                }
                if (_outputPtr >= _outputBufferLen) {
                    flushBuffer();
                }
                _outputBuffer[_outputPtr++] = ch;
                if (++offset >= len) {
                    break main_loop;
                }
            }

            // Ok, so what did we hit?
            int ch = (int) cbuf[offset++];
            if (ch < limit) {
                switch (charTypes[ch]) {
                    case CT_INVALID:
                        reportInvalidChar(ch);
                    case CT_WS_CR:
                    case CT_WS_LF: // !!! TBI: line count
                        break;

                    case CT_OUTPUT_MUST_QUOTE:
                        reportFailedEscaping("CDATA block", ch);
                    case CT_GT: // part of "]]>"?
                        if ((offset - start) >= 3 && cbuf[offset - 2] == ']' && cbuf[offset - 3] == ']') {
                            --offset; // let's push it back
                            // And restart CDATA block...
                            writeCDataEnd();
                            writeCDataStart();
                        }
                        break;
                }
            } else {
                // Problem if it's out of range (like 8-bit char for ascii)
                if (ch >= mEncHighChar) { // problem!
                    reportFailedEscaping("CDATA block", ch);
                }
            }

            if (_outputPtr >= _outputBufferLen) {
                flushBuffer();
            }
            _outputBuffer[_outputPtr++] = (char) ch;
        }
        return -1;
    }

    @Override
    public void writeCharacters(String text) throws IOException, XMLStreamException {
        if (_out == null) {
            return;
        }

        int len = text.length();
        int offset = 0;

        while (len > 0) {
            char[] buf = _copyBuffer;
            final int blen = buf.length;
            int len2 = (len < blen) ? len : blen;
            text.getChars(offset, offset + len2, buf, 0);
            writeCharacters(buf, 0, len2);
            offset += len2;
            len -= len2;
        }
    }

    @Override
    public void writeCharacters(char[] cbuf, int offset, int len) throws IOException, XMLStreamException {
        if (_out == null) {
            return;
        }

        len += offset; // will now mark the end, not length

        main_loop: while (offset < len) {
            final int[] charTypes = mCharTypes.TEXT_CHARS;
            final int limit = mTableCheckEnd;

            inner_loop: while (true) {
                char ch = cbuf[offset];
                if (ch >= limit) {
                    break inner_loop;
                }
                if (charTypes[ch] != XmlCharTypes.CT_OK) {
                    break;
                }
                if (_outputPtr >= _outputBufferLen) {
                    flushBuffer();
                }
                _outputBuffer[_outputPtr++] = ch;
                if (++offset >= len) {
                    break main_loop;
                }
            }

            // Ok, so what did we hit?
            int ch = (int) cbuf[offset++];
            if (ch < limit) {
                switch (charTypes[ch]) {
                    case CT_INVALID:
                        reportInvalidChar(ch);
                    case CT_RBRACKET: // may need to quote as well...
                        // Let's not quote if known not to be followed by '>'
                        if (offset < len && cbuf[offset] != '>') {
                            break;
                        }
                        // let's fall down, to quote
                    case CT_OUTPUT_MUST_QUOTE:
                    case CT_LT:
                    case CT_AMP:
                        writeAsEntity(ch);
                        continue main_loop;

                    case CT_WS_CR:
                        // !!! TBI: line count
                        // Also, CR to be quoted?
                        if (_config.willEscapeCR()) {
                            writeAsEntity(ch);
                            continue main_loop;
                        }
                        break;

                    case CT_WS_LF:
                        // !!! TBI: line count
                    default:
                        break;
                }
            } else if (ch >= mEncHighChar) {
                writeAsEntity(ch);
                continue main_loop;
            }
            if (_outputPtr >= _outputBufferLen) {
                flushBuffer();
            }
            _outputBuffer[_outputPtr++] = (char) ch;
        }
    }

    @Override
    public void writeSpace(String data) throws IOException, XMLStreamException {
        if (_out == null) {
            return;
        }

        int len = data.length();
        int offset = 0;

        // !!! TODO: could just copy straight to output buffer

        while (len > 0) {
            char[] buf = _copyBuffer;
            final int blen = buf.length;
            int len2 = (len < blen) ? len : blen;
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

        len += offset; // will now mark the end, not length

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
            _outputBuffer[_outputPtr++] = ch;
        }
    }

    /**
     * Method that will try to output the content as specified. If
     * the content passed in has embedded "--" in it, it will either
     * add an intervening space between consequtive hyphens (if content
     * fixing is enabled), or return the offset of the first hyphen in
     * multi-hyphen sequence.
     */
    @Override
    public int writeComment(String data) throws IOException, XMLStreamException {
        if (_out == null) {
            return -1;
        }

        writeCommentStart();
        /* Ok, let's just copy into a temporary buffer. While copying
         * to the output buffer would be faster, it gets pretty
         * complicated; so let's not bother (yet?)
         */
        int len = data.length();
        int offset = 0;
        int cix = -1;

        while (len > 0) {
            char[] buf = _copyBuffer;
            int blen = buf.length;

            // Can write all the rest?
            if (blen > len) {
                blen = len;
            }
            // Nope, can only do part
            data.getChars(offset, offset + blen, buf, 0);
            cix = writeCommentContents(buf, 0, blen, false);
            if (cix >= 0) {
                break;
            }
            offset += blen;
            len -= blen;
        }
        if (cix >= 0) {
            return (offset + cix);
        }
        writeCommentEnd();
        return -1;
    }

    /**
     * Note: the only way to fix comment contents is to inject a space
     * to split up consequtive '--' (or '-' that ends a comment).
     */
    private int writeCommentContents(char[] cbuf, int offset, int len, boolean last)
        throws IOException, XMLStreamException {
        len += offset;

        main_loop: while (offset < len) {
            final int[] charTypes = mCharTypes.OTHER_CHARS;
            final int limit = mTableCheckEnd;

            inner_loop: while (true) {
                char ch = cbuf[offset];
                if (ch >= limit) {
                    break inner_loop;
                }
                if (charTypes[ch] != XmlCharTypes.CT_OK) {
                    break;
                }
                if (_outputPtr >= _outputBufferLen) {
                    flushBuffer();
                }
                _outputBuffer[_outputPtr++] = ch;
                if (++offset >= len) {
                    break main_loop;
                }
            }

            // Ok, so what did we hit?
            int ch = (int) cbuf[offset++];
            if (ch < limit) {
                switch (charTypes[ch]) {
                    case CT_INVALID:
                        reportInvalidChar(ch);
                    case CT_WS_CR:
                    case CT_WS_LF: // !!! TBI: line count
                        break;

                    case CT_OUTPUT_MUST_QUOTE:
                        reportFailedEscaping("comment", ch);
                    case CT_HYPHEN: // part of "--", or last char "-"?
                        /* If so, must be able to fix by appending an
                         * extra space...
                         */
                        if (offset == len || cbuf[offset] == '-') {
                            if (!_config.willFixContent()) {
                                return offset - 1; // points to the 'offending' char
                            }
                            if (_outputPtr >= _outputBufferLen) {
                                flushBuffer();
                            }
                            _outputBuffer[_outputPtr++] = ' ';
                        }
                        break;
                }
            } else {
                // Problem if it's out of range (like 8-bit char for ascii)
                if (ch >= mEncHighChar) { // problem!
                    reportFailedEscaping("comment", ch);
                }
            }
            if (_outputPtr >= _outputBufferLen) {
                flushBuffer();
            }
            _outputBuffer[_outputPtr++] = (char) ch;
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
        fastWriteRaw("<!DOCTYPE ");
        /* Since the root name isn't reall namespace aware in
         * any mode (could require it to have at most one colon,
         * but besides that), let's claim there's no need to check
         */
        writeName(rootName);
        if (systemId != null) {
            if (publicId != null) {
                fastWriteRaw(" PUBLIC \"");
                // !!! TBI: verify public id
                fastWriteRaw(publicId);
                fastWriteRaw("\" \"");
            } else {
                fastWriteRaw(" SYSTEM \"");
            }
            // !!! TBI: verify system id
            fastWriteRaw(systemId);
            fastWriteRaw('"');
        }
        // Hmmh. Should we output empty internal subset?
        if (internalSubset != null && internalSubset.length() > 0) {
            fastWriteRaw(' ', '[');
            // !!! TBI: verify validity
            fastWriteRaw(internalSubset);
            fastWriteRaw(']');
        }
        fastWriteRaw('>');
    }

    @Override
    public void writeEntityReference(WName name) throws IOException, XMLStreamException {
        fastWriteRaw('&');
        writeName(name);
        fastWriteRaw(';');
    }

    @Override
    public void writeXmlDeclaration(String version, String encoding, String standalone)
        throws IOException, XMLStreamException {
        fastWriteRaw("<?xml version='");
        // !!! TBI: check validity
        fastWriteRaw(version);
        fastWriteRaw('\'');

        if (encoding != null && encoding.length() > 0) {
            fastWriteRaw(" encoding='");
            // !!! TBI: check validity
            fastWriteRaw(encoding);
            fastWriteRaw('\'');
        }
        if (standalone != null) {
            fastWriteRaw(" standalone='");
            // !!! TBI: check validity
            fastWriteRaw(standalone);
            fastWriteRaw('\'');
        }
        fastWriteRaw('?', '>');
    }

    @Override
    public int writePI(WName target, String data) throws IOException, XMLStreamException {
        fastWriteRaw('<', '?');
        writeName(target);

        if (data != null && data.length() > 0) {
            int len = data.length();
            int offset = 0;
            int cix = -1;

            fastWriteRaw(' ');

            // !!! TODO: copy straight to output buffer
            while (len > 0) {
                char[] buf = _copyBuffer;
                int blen = buf.length;

                // Can write all the rest?
                if (blen > len) {
                    blen = len;
                }
                data.getChars(offset, offset + blen, buf, 0);
                cix = writePIContents(buf, 0, blen);
                if (cix >= 0) {
                    break;
                }
                offset += blen;
                len -= blen;
            }
            if (cix >= 0) {
                return offset + cix;
            }
        }
        fastWriteRaw('?', '>');
        return -1;
    }

    private int writePIContents(char[] cbuf, int offset, int len) throws IOException, XMLStreamException {
        len += offset;

        main_loop: while (offset < len) {
            final int[] charTypes = mCharTypes.OTHER_CHARS;
            final int limit = mTableCheckEnd;

            inner_loop: while (true) {
                char ch = cbuf[offset];
                if (ch >= limit) {
                    break inner_loop;
                }
                if (charTypes[ch] != XmlCharTypes.CT_OK) {
                    break;
                }
                if (_outputPtr >= _outputBufferLen) {
                    flushBuffer();
                }
                _outputBuffer[_outputPtr++] = ch;
                if (++offset >= len) {
                    break main_loop;
                }
            }

            // Ok, so what did we hit?
            int ch = (int) cbuf[offset++];
            if (ch < limit) {
                switch (charTypes[ch]) {
                    case CT_INVALID:
                        reportInvalidChar(ch);
                    case CT_WS_CR:
                    case CT_WS_LF: // !!! TBI: line count
                        break;

                    case CT_OUTPUT_MUST_QUOTE:
                        reportFailedEscaping("processing instruction", ch);
                    case CT_QMARK: // part of "?>"?
                        if (offset < len && cbuf[offset] == '>') { // no way to fix, alas
                            return offset;
                        }
                        break;
                }
            } else {
                // Problem if it's out of range (like 8-bit char for ascii)
                if (ch >= mEncHighChar) { // problem!
                    reportFailedEscaping("processing instruction", ch);
                }
            }
            if (_outputPtr >= _outputBufferLen) {
                flushBuffer();
            }
            _outputBuffer[_outputPtr++] = (char) ch;
        }
        return -1;
    }

    /*
    ////////////////////////////////////////////////////
    // Write methods, elements
    ////////////////////////////////////////////////////
     */

    @Override
    public void writeStartTagStart(WName name) throws IOException, XMLStreamException {
        int ptr = _outputPtr;
        int len = name.serializedLength();
        if ((ptr + len + 1) > _outputBufferLen) {
            if (_out == null) {
                return;
            }
            flushBuffer();
            // Very unlikely, but possible:
            if (len >= _outputBufferLen) {
                _out.write('<');
                name.writeChars(_out);
                return;
            }
            ptr = _outputPtr;
        }
        char[] buf = _outputBuffer;
        buf[ptr++] = '<';
        name.appendChars(buf, ptr);
        _outputPtr = ptr + len;
    }

    @Override
    public void writeStartTagEnd() throws IOException, XMLStreamException {
        fastWriteRaw('>');
    }

    @Override
    public void writeStartTagEmptyEnd() throws IOException {
        int ptr = _outputPtr;
        if ((ptr + 2) > _outputBufferLen) {
            if (_out == null) {
                return;
            }
            flushBuffer();
            ptr = _outputPtr;
        }
        char[] buf = _outputBuffer;
        buf[ptr++] = '/';
        buf[ptr++] = '>';
        _outputPtr = ptr;
    }

    @Override
    public void writeEndTag(WName name) throws IOException, XMLStreamException {
        int ptr = _outputPtr;
        int len = name.serializedLength();
        if ((ptr + len + 3) > _outputBufferLen) {
            flushBuffer();
            // name longer than the buffer? can write it straight out
            if ((len + 3) > _outputBufferLen) {
                _out.write('<');
                _out.write('/');
                name.writeChars(_out);
                _outputBuffer[_outputPtr++] = '>';
                return;
            }
            ptr = _outputPtr;
        }
        char[] buf = _outputBuffer;
        buf[ptr++] = '<';
        buf[ptr++] = '/';
        name.appendChars(buf, ptr);
        ptr += len;
        buf[ptr++] = '>';
        _outputPtr = ptr;
    }

    /*
    ////////////////////////////////////////////////////
    // Write methods, attributes/ns
    ////////////////////////////////////////////////////
     */

    @Override
    public void writeAttribute(WName name, String value) throws IOException, XMLStreamException {
        if (_out == null) {
            return;
        }
        fastWriteRaw(' ');
        writeName(name);
        fastWriteRaw('=', '"');
        int len = (value == null) ? 0 : value.length();
        if (len > 0) {
            writeAttrValue(value, len);
        }
        fastWriteRaw('"');
    }

    @Override
    public void writeAttribute(WName name, char[] value, int offset, int vlen) throws IOException, XMLStreamException {
        if (_out == null) {
            return;
        }
        fastWriteRaw(' ');
        writeName(name);
        fastWriteRaw('=', '"');

        if (vlen > 0) {
            writeAttrValue(value, offset, vlen);
        }
        fastWriteRaw('"');
    }

    private final void writeAttrValue(String value, int len) throws IOException, XMLStreamException {
        int offset = 0;

        while (len > 0) {
            char[] buf = _copyBuffer;
            final int blen = buf.length;
            int len2 = (len < blen) ? len : blen;
            value.getChars(offset, offset + len2, buf, 0);
            writeAttrValue(buf, 0, len2);
            offset += len2;
            len -= len2;
        }
    }

    private final void writeAttrValue(char[] cbuf, int offset, int len) throws IOException, XMLStreamException {
        if (_out == null) {
            return;
        }
        // Fast or slow?
        if ((_outputPtr + len) > _outputBufferLen) { // slow
            writeSplitAttrValue(cbuf, offset, len);
            return;
        }

        // Nope, fast loop:
        len += offset; // will now mark the end, not length

        main_loop: while (offset < len) {
            final int[] charTypes = mCharTypes.ATTR_CHARS;
            final int limit = mTableCheckEnd;

            inner_loop: while (true) {
                char ch = cbuf[offset];
                if (ch >= limit) {
                    break inner_loop;
                }
                if (charTypes[ch] != XmlCharTypes.CT_OK) {
                    break;
                }
                _outputBuffer[_outputPtr++] = ch;
                if (++offset >= len) {
                    break main_loop;
                }
            }

            // Ok, so what did we hit?
            char ch = cbuf[offset++];
            if (ch < limit) {
                switch (charTypes[ch]) {
                    case CT_INVALID:
                        reportInvalidChar(ch);
                    case CT_OUTPUT_MUST_QUOTE:
                    case CT_ATTR_QUOTE:
                    case CT_LT:
                    case CT_AMP:
                        break;

                    case CT_WS_CR:
                    case CT_WS_LF:
                        // !!! TBI: line count
                        /* Note: Both CR and LF always needs quoting within
                         * attribute value; no point in disabling that.
                         */
                        break;

                    default:
                        _outputBuffer[_outputPtr++] = ch;
                        continue main_loop;
                }
            } else if (ch < mEncHighChar) {
                _outputBuffer[_outputPtr++] = ch;
                continue main_loop;
            }
            writeAsEntity(ch);

            /* Invariant regarding output buffer length might not hold
             * any more? (due to escaping)
             */
            if ((len - offset) >= (_outputBufferLen - _outputPtr)) {
                flushBuffer();
            }
        }
    }

    private final void writeSplitAttrValue(char[] cbuf, int offset, int len) throws IOException, XMLStreamException {
        len += offset; // will now mark the end, not length

        main_loop: while (offset < len) {
            final int[] charTypes = mCharTypes.ATTR_CHARS;
            final int limit = mTableCheckEnd;

            inner_loop: while (true) {
                char ch = cbuf[offset];
                if (ch >= limit) {
                    break inner_loop;
                }
                if (charTypes[ch] != XmlCharTypes.CT_OK) {
                    break;
                }
                if (_outputPtr >= _outputBufferLen) {
                    flushBuffer();
                }
                _outputBuffer[_outputPtr++] = ch;
                if (++offset >= len) {
                    break main_loop;
                }
            }

            // Ok, so what did we hit?
            int ch = (int) cbuf[offset++];
            if (ch < limit) {
                switch (charTypes[ch]) {
                    case CT_INVALID:
                        reportInvalidChar(ch);
                    case CT_OUTPUT_MUST_QUOTE:
                    case CT_ATTR_QUOTE:
                    case CT_LT:
                    case CT_AMP:
                        writeAsEntity(ch);
                        continue main_loop;

                    case CT_WS_CR:
                    case CT_WS_LF:
                        // !!! TBI: line count
                        /* Note: Both CR and LF always needs quoting within
                         * attribute value; no point in disabling that.
                         */
                        writeAsEntity(ch);
                        continue main_loop;

                    default:
                        break;
                }
            } else if (ch >= mEncHighChar) {
                writeAsEntity(ch);
                continue main_loop;
            }
            if (_outputPtr >= _outputBufferLen) {
                flushBuffer();
            }
            _outputBuffer[_outputPtr++] = (char) ch;
        }
    }

    /*
    //////////////////////////////////////////////////
    // Write methods, typed content
    //////////////////////////////////////////////////
     */

    @Override
    public void writeTypedValue(AsciiValueEncoder enc) throws IOException, XMLStreamException {
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

    /*
    ////////////////////////////////////////////////////
    // Write methods, attributes, Typed
    ////////////////////////////////////////////////////
     */

    @Override
    public final void writeAttribute(WName name, AsciiValueEncoder enc) throws IOException, XMLStreamException {
        if (_out == null) {
            return;
        }
        fastWriteRaw(' ');
        writeName(name);
        fastWriteRaw('=', '"');
        writeTypedValue(enc);
        fastWriteRaw('"');
    }

    /*
    ////////////////////////////////////////////////////
    // Internal methods, buffering
    ////////////////////////////////////////////////////
     */

    private final void flushBuffer() throws IOException {
        if (_outputPtr > 0 && _out != null) {
            int ptr = _outputPtr;
            // Need to update location info, to keep it in sync
            _locPastChars += ptr;
            _locRowStartOffset -= ptr;
            _outputPtr = 0;
            _out.write(_outputBuffer, 0, ptr);
        }
    }

    /*
    ////////////////////////////////////////////////////
    // Internal methods, low-level write
    ////////////////////////////////////////////////////
     */

    protected final void writeName(WName name) throws IOException {
        int ptr = _outputPtr;
        int len = name.serializedLength();
        if ((ptr + len) > _outputBufferLen) {
            flushBuffer();
            // name longer than the buffer? can write it straight out
            if (len >= _outputBufferLen) {
                name.writeChars(_out);
                return;
            }
            ptr = _outputPtr;
        }
        name.appendChars(_outputBuffer, ptr);
        _outputPtr += len;
    }

    private final void fastWriteRaw(char c) throws IOException {
        if (_outputPtr >= _outputBufferLen) {
            if (_out == null) {
                return;
            }
            flushBuffer();
        }
        _outputBuffer[_outputPtr++] = c;
    }

    private final void fastWriteRaw(char c1, char c2) throws IOException {
        if ((_outputPtr + 1) >= _outputBufferLen) {
            if (_out == null) {
                return;
            }
            flushBuffer();
        }
        _outputBuffer[_outputPtr++] = c1;
        _outputBuffer[_outputPtr++] = c2;
    }

    private final void fastWriteRaw(String str) throws IOException {
        int len = str.length();
        int ptr = _outputPtr;
        if ((ptr + len) >= _outputBufferLen) {
            if (_out == null) {
                return;
            }
            /* It's even possible that String is longer than the buffer (not
             * likely, possible). If so, let's just call the full
             * method:
             */
            if (len > _outputBufferLen) {
                writeRaw(str, 0, str.length());
                return;
            }
            flushBuffer();
            ptr = _outputPtr;
        }
        str.getChars(0, len, _outputBuffer, ptr);
        _outputPtr = ptr + len;
    }

    /*
    ////////////////////////////////////////////////////
    // Internal methods, content verification/fixing
    ////////////////////////////////////////////////////
     */

    /**
     * @return Index at which a problem was found, if any; -1 if there's
     *   no problem.
     */
    protected int verifyCDataContent(String content) {
        if (content != null && content.length() >= 3) {
            int ix = content.indexOf(']');
            if (ix >= 0) {
                return content.indexOf("]]>", ix);
            }
        }
        return -1;
    }

    protected int verifyCDataContent(char[] c, int start, int end) {
        if (c != null) {
            start += 2;
            /* Let's do simple optimization for search...
             * (bayer-moore search algorithm)
             */
            while (start < end) {
                char ch = c[start];
                if (ch == ']') {
                    ++start; // let's just move by one in this case
                    continue;
                }
                if (ch == '>') { // match?
                    if (c[start - 1] == ']' && c[start - 2] == ']') {
                        return start - 2;
                    }
                }
                start += 2;
            }
        }
        return -1;
    }

    protected int verifyCommentContent(String content) {
        int ix = content.indexOf('-');
        if (ix >= 0) {
            /* actually, it's illegal to just end with '-' too, since 
             * that would cause invalid end marker '--->'
             */
            if (ix < (content.length() - 1)) {
                ix = content.indexOf("--", ix);
            }
        }
        return ix;
    }

    protected void writeSegmentedCData(String content, int index) throws IOException {
        /* It's actually fairly easy, just split "]]>" into 2 pieces;
         * for each ']]>'; first one containing "]]", second one ">"
         * (as long as necessary)
         */
        int start = 0;
        while (index >= 0) {
            fastWriteRaw("<![CDATA[");
            writeRaw(content, start, (index + 2) - start);
            fastWriteRaw("]]>");
            start = index + 2;
            index = content.indexOf("]]>", start);
        }
        // Ok, then the last segment
        fastWriteRaw("<![CDATA[");
        writeRaw(content, start, content.length() - start);
        fastWriteRaw("]]>");
    }

    protected void writeSegmentedCData(char[] c, int start, int len, int index) throws IOException {
        int end = start + len;
        while (index >= 0) {
            fastWriteRaw("<![CDATA[");
            writeRaw(c, start, (index + 2) - start);
            fastWriteRaw("]]>");
            start = index + 2;
            index = verifyCDataContent(c, start, end);
        }
        // Ok, then the last segment
        fastWriteRaw("<![CDATA[");
        writeRaw(c, start, end - start);
        fastWriteRaw("]]>");
    }

    protected void writeSegmentedComment(String content, int index) throws IOException {
        int len = content.length();
        // First the special case (last char is hyphen):
        if (index == (len - 1)) {
            fastWriteRaw("<!--");
            writeRaw(content, 0, content.length());
            // we just need to inject one space in there
            fastWriteRaw(" -->");
            return;
        }

        /* Fixing comments is more difficult than that of CDATA segments';
         * this because CDATA can still contain embedded ']]'s, but
         * comment neither allows '--' nor ending with '-->'; which means
         * that it's impossible to just split segments. Instead we'll do
         * something more intrusive, and embed single spaces between all
         * '--' character pairs... it's intrusive, but comments are not
         * supposed to contain any data, so that should be fine (plus
         * at least result is valid, unlike contents as is)
         */
        fastWriteRaw("<!--");
        int start = 0;
        while (index >= 0) {
            // first, content prior to '--' and the first hyphen
            writeRaw(content, start, (index + 1) - start);
            // and an obligatory trailing space to split double-hyphen
            fastWriteRaw(' ');
            // still need to handle rest of consequtive double'-'s if any
            start = index + 1;
            index = content.indexOf("--", start);
        }
        // Ok, then the last segment
        writeRaw(content, start, len - start);
        // ends with a hyphen? that needs to be fixed, too
        if (content.charAt(len - 1) == '-') {
            fastWriteRaw(' ');
        }
        fastWriteRaw("-->");
    }

    protected final void writeAsEntity(int c) throws IOException {
        // Quickie check to avoid 

        char[] buf = _outputBuffer;
        int ptr = _outputPtr;
        if ((ptr + 10) >= buf.length) { // &#x [up to 6 hex digits] ;
            flushBuffer();
            ptr = _outputPtr;
        }
        buf[ptr++] = '&';

        // Can use more optimal notation for 8-bit ascii stuff:
        if (c < 256) {
            /* Also; although not really mandatory, let's also
             * use pre-defined entities where possible.
             */
            if (c == '&') {
                buf[ptr++] = 'a';
                buf[ptr++] = 'm';
                buf[ptr++] = 'p';
            } else if (c == '<') {
                buf[ptr++] = 'l';
                buf[ptr++] = 't';
            } else if (c == '>') {
                buf[ptr++] = 'g';
                buf[ptr++] = 't';
            } else if (c == '\'') {
                buf[ptr++] = 'a';
                buf[ptr++] = 'p';
                buf[ptr++] = 'o';
                buf[ptr++] = 's';
            } else if (c == '"') {
                buf[ptr++] = 'q';
                buf[ptr++] = 'u';
                buf[ptr++] = 'o';
                buf[ptr++] = 't';
            } else {
                buf[ptr++] = '#';;
                buf[ptr++] = 'x';;
                // Can use shortest quoting for tab, cr, lf:
                if (c >= 16) {
                    int digit = (c >> 4);
                    buf[ptr++] = (char) ((digit < 10) ? ('0' + digit) : (('a' - 10) + digit));
                    c &= 0xF;
                }
                buf[ptr++] = (char) ((c < 10) ? ('0' + c) : (('a' - 10) + c));
            }
        } else {
            buf[ptr++] = '#';
            buf[ptr++] = 'x';

            // Ok, let's write the shortest possible sequence then:
            int shift = 20;
            int origPtr = ptr;

            do {
                int digit = (c >> shift) & 0xF;
                if (digit > 0 || (ptr != origPtr)) {
                    buf[ptr++] = (char) ((digit < 10) ? ('0' + digit) : (('a' - 10) + digit));
                }
                shift -= 4;
            } while (shift > 0);
            c &= 0xF;
            buf[ptr++] = (char) ((c < 10) ? ('0' + c) : (('a' - 10) + c));
        }
        buf[ptr++] = ';';
        _outputPtr = ptr;
    }
}
