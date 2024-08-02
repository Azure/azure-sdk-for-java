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

/**
 * This is the generic implementation of {@link XmlWriter}, used if
 * the destination is byte-based {@link java.io.OutputStream}, and
 * encoding is 7-bit (US) Ascii.
 */
public final class AsciiXmlWriter extends SingleByteXmlWriter {
    final static int LAST_VALID_CHAR = 0x7F;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    public AsciiXmlWriter(WriterConfig cfg, OutputStream out) {
        super(cfg, out, OutputCharTypes.getAsciiCharTypes());
    }

    @Override
    public int getHighestEncodable() {
        return LAST_VALID_CHAR;
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

        while (offset < len) {
            char ch = cbuf[offset++];
            if (ch > LAST_VALID_CHAR) {
                reportFailedEscaping("raw content", ch);
            }
            if (_outputPtr >= _outputBufferLen) {
                flushBuffer();
            }
            _outputBuffer[_outputPtr++] = (byte) ch;
        }
    }

    @Override
    protected WName doConstructName(String localName) throws XMLStreamException {
        return new ByteWName(localName, getAscii(localName));
    }

    @Override
    protected WName doConstructName(String prefix, String localName) throws XMLStreamException {
        int plen = prefix.length();
        byte[] pname = new byte[plen + 1 + localName.length()];
        getAscii(prefix, pname, 0);
        pname[plen] = BYTE_COLON;
        getAscii(localName, pname, plen + 1);
        return new ByteWName(prefix, localName, pname);
    }
}
