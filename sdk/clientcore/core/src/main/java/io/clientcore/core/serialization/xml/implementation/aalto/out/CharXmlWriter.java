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

package io.clientcore.core.serialization.xml.implementation.aalto.out;

import io.clientcore.core.serialization.xml.implementation.aalto.impl.ErrorConsts;
import io.clientcore.core.serialization.xml.implementation.aalto.impl.StreamExceptionBase;
import io.clientcore.core.serialization.xml.implementation.aalto.util.XmlCharTypes;
import io.clientcore.core.serialization.xml.implementation.aalto.util.XmlChars;
import io.clientcore.core.serialization.xml.implementation.aalto.util.XmlConsts;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;

import static io.clientcore.core.serialization.xml.implementation.aalto.out.OutputCharTypes.CT_OUTPUT_MUST_QUOTE;
import static io.clientcore.core.serialization.xml.implementation.aalto.util.XmlCharTypes.CT_AMP;
import static io.clientcore.core.serialization.xml.implementation.aalto.util.XmlCharTypes.CT_ATTR_QUOTE;
import static io.clientcore.core.serialization.xml.implementation.aalto.util.XmlCharTypes.CT_GT;
import static io.clientcore.core.serialization.xml.implementation.aalto.util.XmlCharTypes.CT_INVALID;
import static io.clientcore.core.serialization.xml.implementation.aalto.util.XmlCharTypes.CT_LT;
import static io.clientcore.core.serialization.xml.implementation.aalto.util.XmlCharTypes.CT_RBRACKET;
import static io.clientcore.core.serialization.xml.implementation.aalto.util.XmlCharTypes.CT_WS_CR;
import static io.clientcore.core.serialization.xml.implementation.aalto.util.XmlCharTypes.CT_WS_LF;

/**
 * This is the generic implementation of a simple XML writer, that
 * outputs XML content using a {@link Writer}.
 */
@SuppressWarnings("fallthrough")
public final class CharXmlWriter {
    private final static int SURR1_FIRST = 0xD800;
    private final static int SURR2_LAST = 0xDFFF;

    /**
     * And this value determines size of the intermediate copy buffer
     * to use.
     */
    final static int DEFAULT_FULL_BUFFER_SIZE = 1000;

    /*
    /**********************************************************************
    /* Output location info
    /**********************************************************************
     */

    /**
     * Number of characters output prior to currently buffered output
     */
    private int _locPastChars = 0;

    /**
     * Offset of the first character on this line. May be negative, if
     * the offset was in a buffer that has been flushed out.
     */
    private int _locRowStartOffset = 0;

    /*
    ////////////////////////////////////////////////
    // Output state, buffering
    ////////////////////////////////////////////////
     */

    /**
     * Actual Writer to use for outputting buffered data as appropriate.
     * During active usage, remains as the writer initially set; set to
     * null when this writer is closed.
     */
    private final Writer _out;

    private final char[] _outputBuffer;

    private int _outputPtr;

    private final int _outputBufferLen;

    /*
    ////////////////////////////////////////////////
    // Encoding/escaping configuration
    ////////////////////////////////////////////////
     */

    /**
     * First Unicode character (one with lowest value) after (and including)
     * which character entities have to be used.
     */
    private final int mEncHighChar = 0xFFFE;

    /**
     * First Unicode character that is NOT to be checked against static
     * validity table. Usually the size of check table, but lower for
     * some encodings (like ascii)
     */
    private final int mTableCheckEnd = 256;

    /*
    ////////////////////////////////////////////////
    // Validation
    ////////////////////////////////////////////////
     */

    /**
     * Validation tables used for verifying validity (and need for quoting)
     */
    final XmlCharTypes mCharTypes = OutputCharTypes.getLatin1CharTypes();

    /*
    ////////////////////////////////////////////////
    // Life-cycle
    ////////////////////////////////////////////////
     */

    public CharXmlWriter(Writer out) {
        _out = out;
        _outputBuffer = new char[DEFAULT_FULL_BUFFER_SIZE];
        _outputBufferLen = _outputBuffer.length;
        _outputPtr = 0;
    }

    private int getOutputPtr() {
        return _outputPtr;
    }

    /*
    ///////////////////////////////////////////////////////
    // WNameFactory implementation
    ///////////////////////////////////////////////////////
     */

    public WName constructName(String localName) {
        return new WName(localName);
    }

    public WName constructName(String prefix, String localName) {
        return new WName(prefix, localName);
    }

    /*
    ////////////////////////////////////////////////
    // Low-level (pass-through) methods
    ////////////////////////////////////////////////
     */

    /**
     * Method called to flush the buffer(s), and close the output
     * sink (stream or writer).
     */
    public void close() throws IOException {
        flush();
    }

    public void flush() throws IOException {
        flushBuffer();
        _out.flush();
    }

    public void writeRaw(String str, int offset, int len) throws IOException {
        // First; is the new request small or not? If yes, needs to be buffered
        if (len < _outputBufferLen) { // yup
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
            if (ptr < _outputBufferLen) {
                /* Also, if we are to copy any stuff, let's make sure
                 * that we either copy it all in one chunk, or copy
                 * enough for non-small chunk, flush, and output remaining
                 * non-small chunk (former possible if chunk we were requested
                 * to output is only slightly over 'small' size)
                 */
                int needed = (_outputBufferLen - ptr);

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

    public void writeCDataStart() throws IOException {
        fastWriteRaw("<![CDATA[");
    }

    public void writeCDataEnd() throws IOException {
        fastWriteRaw("]]>");
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
    public int writeCData(String data) throws IOException, XMLStreamException {
        writeCDataStart();
        /* Ok, let's just copy into a temporary buffer. While copying
         * to the output buffer would be faster, it gets pretty
         * complicated; so let's not bother (yet?)
         */
        int len = data.length();
        int offset = 0;

        while (len > 0) {
            int len2 = Math.min(len, _outputBufferLen - _outputPtr);
            int cix = writeCDataContents(data, offset, len2);
            if (cix >= 0) {
                return (offset + cix);
            }
            offset += len2;
            len -= len2;
        }
        writeCDataEnd();
        return -1;
    }

    private int writeCDataContents(String data, int offset, int len) throws IOException, XMLStreamException {
        len += offset; // will now mark the end, not length

        final int start = offset;

        main_loop: while (offset < len) {
            final int[] charTypes = mCharTypes.OTHER_CHARS;
            final int limit = mTableCheckEnd;

            while (true) {
                char ch = data.charAt(offset);
                if (ch >= limit) {
                    break;
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
            int ch = data.charAt(offset++);
            if (ch < limit) {
                switch (charTypes[ch]) {
                    case CT_INVALID:
                        reportInvalidChar(ch);
                    case CT_WS_CR:
                    case CT_WS_LF: // !!! TBI: line count
                        break;

                    case CT_OUTPUT_MUST_QUOTE:
                        reportFailedEscaping(ch);
                    case CT_GT: // part of "]]>"?
                        if ((offset - start) >= 3 && data.charAt(offset - 2) == ']' && data.charAt(offset - 3) == ']') {
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
                    reportFailedEscaping(ch);
                }
            }

            if (_outputPtr >= _outputBufferLen) {
                flushBuffer();
            }
            _outputBuffer[_outputPtr++] = (char) ch;
        }
        return -1;
    }

    public void writeCharacters(String text) throws IOException, XMLStreamException {
        int len = text.length();
        int offset = 0;

        while (len > 0) {
            int len2 = Math.min(len, _outputBufferLen - _outputPtr);
            writeCharacters(text, offset, len2);
            offset += len2;
            len -= len2;
        }
    }

    public void writeCharacters(CharSequence text, int offset, int len) throws IOException, XMLStreamException {
        len += offset; // will now mark the end, not length

        main_loop: while (offset < len) {
            final int[] charTypes = mCharTypes.TEXT_CHARS;
            final int limit = mTableCheckEnd;

            while (true) {
                char ch = text.charAt(offset);
                if (ch >= limit) {
                    break;
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
            int ch = text.charAt(offset++);
            if (ch < limit) {
                switch (charTypes[ch]) {
                    case CT_INVALID:
                        reportInvalidChar(ch);
                    case CT_RBRACKET: // may need to quote as well...
                        // Let's not quote if known not to be followed by '>'
                        if (offset < len && text.charAt(offset) != '>') {
                            break;
                        }
                        // let's fall down, to quote
                    case CT_OUTPUT_MUST_QUOTE:
                    case CT_LT:
                    case CT_AMP:
                        writeAsEntity(ch);
                        continue;

                    case CT_WS_CR:
                        // !!! TBI: line count
                        // Also, CR to be quoted?
                        writeAsEntity(ch);
                        continue;

                    case CT_WS_LF:
                        // !!! TBI: line count
                    default:
                        break;
                }
            } else if (ch >= mEncHighChar) {
                writeAsEntity(ch);
                continue;
            }
            if (_outputPtr >= _outputBufferLen) {
                flushBuffer();
            }
            _outputBuffer[_outputPtr++] = (char) ch;
        }
    }

    public void writeSpace(String data) throws IOException, XMLStreamException {
        int len = data.length();
        int offset = 0;

        while (len > 0) {
            int len2 = Math.min(len, _outputBufferLen - _outputPtr);
            writeSpace(data, offset, len2);
            offset += len2;
            len -= len2;
        }
    }

    public void writeSpace(CharSequence data, int offset, int len) throws IOException, XMLStreamException {
        len += offset; // will now mark the end, not length

        while (offset < len) {
            char ch = data.charAt(offset++);
            if (ch > 0x0020) {
                throwOutputError(MessageFormat.format(ErrorConsts.WERR_SPACE_CONTENT, (int) ch, offset - 1));
            }
            if (_outputPtr >= _outputBufferLen) {
                flushBuffer();
            }
            _outputBuffer[_outputPtr++] = ch;
        }
    }

    public void writeXmlDeclaration() throws IOException {
        fastWriteRaw("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    }

    /*
    ////////////////////////////////////////////////////
    // Write methods, elements
    ////////////////////////////////////////////////////
     */

    /**
     * Note: can throw XMLStreamException, if name checking is enabled,
     * and name is invalid (name check has to be in this writer, not
     * caller, since it depends not only on xml limitations, but also
     * on encoding limitations)
     */
    public void writeStartTagStart(WName name) throws IOException {
        int ptr = _outputPtr;
        int len = name.serializedLength();
        if ((ptr + len + 1) > _outputBufferLen) {
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

    public void writeStartTagEnd() throws IOException {
        fastWriteRaw('>');
    }

    public void writeStartTagEmptyEnd() throws IOException {
        int ptr = _outputPtr;
        if ((ptr + 2) > _outputBufferLen) {
            flushBuffer();
            ptr = _outputPtr;
        }
        char[] buf = _outputBuffer;
        buf[ptr++] = '/';
        buf[ptr++] = '>';
        _outputPtr = ptr;
    }

    public void writeEndTag(WName name) throws IOException {
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

    /**
     * Note: can throw XMLStreamException, if name checking is enabled,
     * and name is invalid (name check has to be in this writer, not
     * caller, since it depends not only on xml limitations, but also
     * on encoding limitations)
     */
    public void writeAttribute(WName name, String value) throws IOException, XMLStreamException {
        fastWriteRaw(' ');
        writeName(name);
        fastWriteRaw();
        int len = (value == null) ? 0 : value.length();
        if (len > 0) {
            writeAttrValue(value, len);
        }
        fastWriteRaw('"');
    }

    private void writeAttrValue(String value, int len) throws IOException, XMLStreamException {
        int offset = 0;

        while (len > 0) {
            int len2 = Math.min(len, _outputBufferLen - _outputPtr);
            writeAttrValue(value, offset, len2);
            offset += len2;
            len -= len2;
        }
    }

    private void writeAttrValue(String value, int offset, int len) throws IOException, XMLStreamException {
        len += offset; // will now mark the end, not length

        // Nope, fast loop:
        main_loop: while (offset < len) {
            final int[] charTypes = mCharTypes.ATTR_CHARS;
            final int limit = mTableCheckEnd;

            while (true) {
                char ch = value.charAt(offset);
                if (ch >= limit) {
                    break;
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
            char ch = value.charAt(offset++);
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
                        continue;
                }
            } else if (ch < mEncHighChar) {
                _outputBuffer[_outputPtr++] = ch;
                continue;
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

    /*
    /**********************************************************************
    /* Location information
    /**********************************************************************
     */

    public int getRow() {
        return 1;
    }

    public int getColumn() {
        return (getOutputPtr() - _locRowStartOffset) + 1;
    }

    public int getAbsOffset() {
        return _locPastChars + getOutputPtr();
    }

    /*
    ////////////////////////////////////////////////////
    // Internal methods, buffering
    ////////////////////////////////////////////////////
     */

    private void flushBuffer() throws IOException {
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

    private void writeName(WName name) throws IOException {
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

    private void fastWriteRaw(char c) throws IOException {
        if (_outputPtr >= _outputBufferLen) {
            flushBuffer();
        }
        _outputBuffer[_outputPtr++] = c;
    }

    private void fastWriteRaw() throws IOException {
        if ((_outputPtr + 1) >= _outputBufferLen) {
            flushBuffer();
        }
        _outputBuffer[_outputPtr++] = '=';
        _outputBuffer[_outputPtr++] = '"';
    }

    private void fastWriteRaw(String str) throws IOException {
        int len = str.length();
        int ptr = _outputPtr;
        if ((ptr + len) >= _outputBufferLen) {
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

    private void writeAsEntity(int c) throws IOException {
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
                buf[ptr++] = '#';
                buf[ptr++] = 'x';
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

    private void reportFailedEscaping(int ch) throws XMLStreamException {
        // Quick separation of high-range invalid chars:
        if (ch == 0xFFFE || ch == 0xFFFF || (ch >= SURR1_FIRST && ch <= SURR2_LAST)) {
            reportInvalidChar(ch);
        }
        // One more check: is it only escapable in xml 1.1?
        if (ch < 0x0020) {
            reportInvalidChar(ch);
        }
        throwOutputError(MessageFormat.format(ErrorConsts.WERR_NO_ESCAPING, "CDATA block", ch));
    }

    private void reportInvalidChar(int c) throws XMLStreamException {
        // First, let's flush any output we may have, to make debugging easier
        try {
            flush();
        } catch (IOException ioe) {
            throw new StreamExceptionBase(ioe);
        }

        if (c == 0) {
            throwOutputError("Invalid null character in text to output");
        }
        if (c < ' ' || (c >= 0x7F && c <= 0x9F)) {
            throwOutputError("Invalid white space character (0x" + Integer.toHexString(c) + ") in text to output");
        }
        if (c > XmlConsts.MAX_UNICODE_CHAR) {
            throwOutputError("Illegal unicode character point (0x" + Integer.toHexString(c)
                + ") to output; max is 0x10FFFF as per RFC 3629");
        }
        /* Surrogate pair in non-quotable (not text or attribute value)
         * content, and non-unicode encoding (ISO-8859-x, Ascii)?
         */
        if (c >= SURR1_FIRST && c <= SURR2_LAST) {
            throwOutputError(
                "Illegal surrogate pair -- can only be output via character entities (for current encoding), which are not allowed in this content");
        }
        // Just something that the encoding can not express natively?

        throwOutputError("Invalid XML character " + XmlChars.getCharDesc(c) + " in text to output");
    }

    private void throwOutputError(String msg) throws XMLStreamException {
        // First, let's flush any output we may have, to make debugging easier
        try {
            flush();
        } catch (IOException ioe) {
            throw new StreamExceptionBase(ioe);
        }
        throw new XMLStreamException(msg);
    }
}
