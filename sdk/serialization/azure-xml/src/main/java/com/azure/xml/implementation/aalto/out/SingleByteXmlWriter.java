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

import com.azure.xml.implementation.aalto.util.XmlCharTypes;

/**
 * This is the common base class for writers that output to byte-backed
 * output sources, and use Ascii or ISO-8859-1 (Latin1) encoding.
 */
public abstract class SingleByteXmlWriter extends ByteXmlWriter {
    public SingleByteXmlWriter(WriterConfig cfg, OutputStream out, XmlCharTypes charTypes) {
        super(cfg, out, charTypes);
    }

    /*
    /**********************************************************************
    /* Abstract methods for sub-classes
    /**********************************************************************
     */

    @Override
    public abstract int getHighestEncodable();

    @Override
    public abstract void writeRaw(char[] cbuf, int offset, int len) throws IOException, XMLStreamException;

    /*
    /**********************************************************************
    /* Internal methods, low-level writes
    /**********************************************************************
     */

    @Override
    final protected void output2ByteChar(int ch) throws IOException, XMLStreamException {
        reportFailedEscaping("content", ch);
    }

    /**
     * With single byte encodings, there's no way to express these
     * characters without character entities. So, this always leads
     * to an exception
     */
    @Override
    final protected int outputStrictMultiByteChar(int ch, char[] cbuf, int inputOffset, int inputLen)
        throws IOException, XMLStreamException {
        reportFailedEscaping("content", ch);
        return 0; // never gets this far
    }

    /**
     * This can be done, although only by using character entities.
     */
    @Override
    final protected int outputMultiByteChar(int ch, char[] cbuf, int inputOffset, int inputLen)
        throws IOException, XMLStreamException {
        if (ch >= SURR1_FIRST) { // surrogate?
            if (ch <= SURR2_LAST) { // yes, outside of BMP
                // Do we have second part?
                if (inputOffset >= inputLen) { // nope... have to note down
                    _surrogate = ch;
                } else {
                    int ch2 = cbuf[inputOffset++];
                    outputSurrogates(ch, ch2);
                }
                return inputOffset;
            } else if (ch >= 0xFFFE) { // 0xFFFE, 0xFFFF are invalid
                reportInvalidChar(ch);
            }
        }
        writeAsEntity(ch);
        return inputOffset;
    }

    @Override
    protected final void outputSurrogates(int surr1, int surr2) throws IOException, XMLStreamException {
        writeAsEntity(calcSurrogate(surr1, surr2, " in content"));
    }
}
