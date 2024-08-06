// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
/* Stax2 API extension for Streaming Api for Xml processing (StAX).
 *
 * Copyright (c) 2006- Tatu Saloranta, tatu.saloranta@iki.fi
 *
 * Licensed under the License specified in file LICENSE, included with
 * the source code.
 * You may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.azure.xml.implementation.stax2.ri;

import javax.xml.stream.*;

import com.azure.xml.implementation.stax2.*;

/**
 * This is a partial base implementation of {@link XMLStreamWriter2},
 * the extended stream writer that is part of Stax2.
 */
public abstract class Stax2WriterImpl implements XMLStreamWriter2 /* From Stax2 */
    , XMLStreamConstants {
    /*
    ///////////////////////////////////////////////////////////
    // Life-cycle methods
    ///////////////////////////////////////////////////////////
     */

    protected Stax2WriterImpl() {
    }

    /*
    ///////////////////////////////////////////////////////////
    // XMLStreamWriter2 (StAX2) implementation
    ///////////////////////////////////////////////////////////
     */

    @Override
    public abstract Location getLocation();

    @Override
    public void writeSpace(String text) throws XMLStreamException {
        /* Hmmh. Two choices: either try to write as regular characters,
         * or output as is via raw calls. Latter would be safer, if we
         * had access to it; former may escape incorrectly.
         * While this may not be optimal, let's try former
         */
        writeRaw(text);
    }

    @Override
    public void writeSpace(char[] text, int offset, int length) throws XMLStreamException {
        // See comments above...
        writeRaw(text, offset, length);
    }

    /*
    ///////////////////////////////////////////////////////////
    // Stax2, Pass-through methods
    ///////////////////////////////////////////////////////////
    */

    @Override
    public void writeRaw(String text) throws XMLStreamException {
        writeRaw(text, 0, text.length());
    }

    @Override
    public abstract void writeRaw(String text, int offset, int len) throws XMLStreamException;

    @Override
    public abstract void writeRaw(char[] text, int offset, int length) throws XMLStreamException;

    /*
    ///////////////////////////////////////////////////////////
    // Stax2, validation
    ///////////////////////////////////////////////////////////
    */

    /*
    ///////////////////////////////////////////////////////////
    // Helper methods
    ///////////////////////////////////////////////////////////
    */

}
