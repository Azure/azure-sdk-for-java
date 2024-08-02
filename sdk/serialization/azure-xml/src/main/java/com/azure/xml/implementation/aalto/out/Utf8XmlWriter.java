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

import com.azure.xml.implementation.aalto.impl.IoStreamException;
import com.azure.xml.implementation.aalto.util.XmlConsts;

/**
 * This is the generic implementation of {@link XmlWriter}, used if
 * the destination is byte-based {@link java.io.OutputStream}, and
 * encoding is UTF-8.
 */
public final class Utf8XmlWriter extends ByteXmlWriter {
    public Utf8XmlWriter(WriterConfig cfg, OutputStream out) {
        super(cfg, out, OutputCharTypes.getUtf8CharTypes());
    }

    /*
    /**********************************************************************
    /* Abstract method implementations
    /**********************************************************************
     */

    @Override
    public int getHighestEncodable() {
        return XmlConsts.MAX_UNICODE_CHAR;
    }

    @Override
    public void writeRaw(char[] cbuf, int offset, int len) throws IOException, XMLStreamException {
        if (_out == null || len == 0) {
            return;
        }
        if (_surrogate != 0) {
            outputSurrogates(_surrogate, cbuf[offset]);
            ++offset;
            --len;
        }

        len += offset; // now marks the end

        // !!! TODO: combine input+output length checks into just one

        main_loop: while (offset < len) {
            inner_loop: while (true) {
                int ch = (int) cbuf[offset];
                if (ch >= 0x80) {
                    break inner_loop;
                }
                // !!! TODO: fast writes
                if (_outputPtr >= _outputBufferLen) {
                    flushBuffer();
                }
                _outputBuffer[_outputPtr++] = (byte) ch;
                if (++offset >= len) {
                    break main_loop;
                }
            }
            char ch = cbuf[offset++];
            if (ch < 0x800) { // 2-byte?
                output2ByteChar(ch);
                continue;
            }
            offset = outputMultiByteChar(ch, cbuf, offset, len);
        }
    }

    @Override
    protected WName doConstructName(String localName) throws XMLStreamException {
        // !!! TODO: optimize:
        try {
            byte[] b = localName.getBytes("UTF-8");
            return new ByteWName(localName, b);
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
        }
    }

    @Override
    protected WName doConstructName(String prefix, String localName) throws XMLStreamException {
        // !!! TODO: optimize:
        try {
            byte[] b = (prefix + ":" + localName).getBytes("UTF-8");
            return new ByteWName(prefix, localName, b);
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
        }
    }

    /*
    /**********************************************************************
    /* Internal methods, low-level write
    /**********************************************************************
     */

    @Override
    protected final void outputSurrogates(int surr1, int surr2) throws IOException, XMLStreamException {
        int c = calcSurrogate(surr1, surr2, " in content");
        if ((_outputPtr + 4) > _outputBufferLen) {
            flushBuffer();
        }
        _outputBuffer[_outputPtr++] = (byte) (0xf0 | (c >> 18));
        _outputBuffer[_outputPtr++] = (byte) (0x80 | ((c >> 12) & 0x3f));
        _outputBuffer[_outputPtr++] = (byte) (0x80 | ((c >> 6) & 0x3f));
        _outputBuffer[_outputPtr++] = (byte) (0x80 | (c & 0x3f));
    }

    @Override
    final protected void output2ByteChar(int ch) throws IOException, XMLStreamException {
        if ((_outputPtr + 2) > _outputBufferLen) {
            flushBuffer();
        }
        byte[] bbuf = _outputBuffer;
        bbuf[_outputPtr++] = (byte) (0xc0 | (ch >> 6));
        bbuf[_outputPtr++] = (byte) (0x80 | (ch & 0x3f));
    }

    /**
     * Method called to output a character that is beyond range of
     * 1- and 2-byte UTF-8 encodings. This means it's either invalid
     * character, or needs to be encoded using 3- or 4-byte encoding.
     *
     * @param inputOffset Input pointer after character has been handled;
     *   either same as one passed in, or one more if a surrogate character
     *   was succesfully handled
     */
    @Override
    final protected int outputMultiByteChar(int ch, char[] cbuf, int inputOffset, int inputLen)
        throws IOException, XMLStreamException {
        if (ch >= SURR1_FIRST) {
            if (ch <= SURR2_LAST) { // yes, outside of BMP
                // Do we have second part?
                if (inputOffset >= inputLen) { // nope... have to note down
                    _surrogate = ch;
                } else {
                    outputSurrogates(ch, cbuf[inputOffset]);
                    ++inputOffset;
                }
                return inputOffset;
            }
            // Nope... but may be invalid
            if (ch >= 0xFFFE) { // 0xFFFE, 0xFFFF are invalid
                reportInvalidChar(ch);
            }
        }
        if ((_outputPtr + 3) > _outputBufferLen) {
            flushBuffer();
        }
        byte[] bbuf = _outputBuffer;
        bbuf[_outputPtr++] = (byte) (0xe0 | (ch >> 12));
        bbuf[_outputPtr++] = (byte) (0x80 | ((ch >> 6) & 0x3f));
        bbuf[_outputPtr++] = (byte) (0x80 | (ch & 0x3f));
        return inputOffset;
    }

    @Override
    final protected int outputStrictMultiByteChar(int ch, char[] cbuf, int inputOffset, int inputLen)
        throws IOException, XMLStreamException {
        if (ch >= SURR1_FIRST) {
            if (ch <= SURR2_LAST) { // yes, outside of BMP
                // Do we have second part?
                if (inputOffset >= inputLen) { // nope... have to note down
                    _surrogate = ch;
                } else {
                    outputSurrogates(ch, cbuf[inputOffset]);
                    ++inputOffset;
                }
                return inputOffset;
            }
            // Nope... but may be invalid
            if (ch >= 0xFFFE) { // 0xFFFE, 0xFFFF are invalid
                reportInvalidChar(ch);
            }
        }
        if ((_outputPtr + 3) > _outputBufferLen) {
            flushBuffer();
        }
        byte[] bbuf = _outputBuffer;
        bbuf[_outputPtr++] = (byte) (0xe0 | (ch >> 12));
        bbuf[_outputPtr++] = (byte) (0x80 | ((ch >> 6) & 0x3f));
        bbuf[_outputPtr++] = (byte) (0x80 | (ch & 0x3f));
        return inputOffset;
    }
}
