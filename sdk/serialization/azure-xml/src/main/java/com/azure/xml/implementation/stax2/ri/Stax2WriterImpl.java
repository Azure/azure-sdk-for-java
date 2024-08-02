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
import com.azure.xml.implementation.stax2.validation.*;

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
    public boolean isPropertySupported(String name) {
        /* No base properties (or should we have something for impl
         * name, version?)
         */
        return false;
    }

    @Override
    public boolean setProperty(String name, Object value) {
        throw new IllegalArgumentException("No settable property '" + name + "'");
    }

    @Override
    public abstract XMLStreamLocation2 getLocation();

    @Override
    public abstract String getEncoding();

    @Override
    public void writeCData(char[] text, int start, int len) throws XMLStreamException {
        writeCData(new String(text, start, len));
    }

    @Override
    public void writeDTD(String rootName, String systemId, String publicId, String internalSubset)
        throws XMLStreamException {
        /* This may or may not work... depending on how well underlying
         * implementation follows stax 1.0 spec (it should work)
         */
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE");
        sb.append(rootName);
        if (systemId != null) {
            if (publicId != null) {
                sb.append(" PUBLIC \"");
                sb.append(publicId);
                sb.append("\" \"");
            } else {
                sb.append(" SYSTEM \"");
            }
            sb.append(systemId);
            sb.append('"');
        }
        // Hmmh. Should we output empty internal subset?
        if (internalSubset != null && !internalSubset.isEmpty()) {
            sb.append(" [");
            sb.append(internalSubset);
            sb.append(']');
        }
        sb.append('>');
        writeDTD(sb.toString());
    }

    @Override
    public void writeFullEndElement() throws XMLStreamException {
        /* This should work with base Stax 1.0 implementations.
         * Sub-classes are, however, encouraged to implement it
         * more directly, if possible.
         */
        writeCharacters("");
        writeEndElement();
    }

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

    @Override
    public abstract void writeStartDocument(String version, String encoding, boolean standAlone)
        throws XMLStreamException;

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

    @Override
    public void copyEventFromReader(XMLStreamReader2 sr, boolean preserveEventData) throws XMLStreamException {
        switch (sr.getEventType()) {
            case START_DOCUMENT: {
                String version = sr.getVersion();
                /* No real declaration? If so, we don't want to output
                 * anything, to replicate as closely as possible the
                 * source document
                 */
                if (version == null || version.isEmpty()) {
                    ; // no output if no real input
                } else {
                    if (sr.standaloneSet()) {
                        writeStartDocument(sr.getVersion(), sr.getCharacterEncodingScheme(), sr.isStandalone());
                    } else {
                        writeStartDocument(sr.getCharacterEncodingScheme(), sr.getVersion());
                    }
                }
            }
                return;

            case END_DOCUMENT:
                writeEndDocument();
                return;

            // Element start/end events:
            case START_ELEMENT:
                /* Start element is bit trickier to output since there
                 * may be differences between repairing/non-repairing
                 * writers. But let's try a generic handling here.
                 */
                copyStartElement(sr);
                return;

            case END_ELEMENT:
                writeEndElement();
                return;

            case SPACE:
                writeSpace(sr.getTextCharacters(), sr.getTextStart(), sr.getTextLength());
                return;

            case CDATA:
                writeCData(sr.getTextCharacters(), sr.getTextStart(), sr.getTextLength());
                return;

            case CHARACTERS:
                writeCharacters(sr.getTextCharacters(), sr.getTextStart(), sr.getTextLength());
                return;

            case COMMENT:
                writeComment(sr.getText());
                return;

            case PROCESSING_INSTRUCTION:
                writeProcessingInstruction(sr.getPITarget(), sr.getPIData());
                return;

            case DTD: {
                DTDInfo info = sr.getDTDInfo();
                if (info == null) {
                    /* Hmmmh. Can this happen for non-DTD-aware readers?
                     * And if so, what should we do?
                     */
                    throw new XMLStreamException(
                        "Current state DOCTYPE, but not DTDInfo Object returned -- reader doesn't support DTDs?");
                }
                writeDTD(info.getDTDRootName(), info.getDTDSystemId(), info.getDTDPublicId(),
                    info.getDTDInternalSubset());
            }
                return;

            case ENTITY_REFERENCE:
                writeEntityRef(sr.getLocalName());
                return;

            case ATTRIBUTE:
            case NAMESPACE:
            case ENTITY_DECLARATION:
            case NOTATION_DECLARATION:
                // Let's just fall back to throw the exception
        }
        throw new XMLStreamException("Unrecognized event type (" + sr.getEventType() + "); not sure how to copy");
    }

    /*
    ///////////////////////////////////////////////////////////
    // Stax2, validation
    ///////////////////////////////////////////////////////////
    */

    @Override
    public XMLValidator validateAgainst(XMLValidationSchema schema) throws XMLStreamException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public XMLValidator stopValidatingAgainst(XMLValidationSchema schema) throws XMLStreamException {
        return null;
    }

    @Override
    public XMLValidator stopValidatingAgainst(XMLValidator validator) throws XMLStreamException {
        return null;
    }

    @Override
    public ValidationProblemHandler setValidationProblemHandler(ValidationProblemHandler h) {
        /* Not a real problem: although we can't do anything with it
         * (without real validator integration)
         */
        return null;
    }

    /*
    ///////////////////////////////////////////////////////////
    // Helper methods
    ///////////////////////////////////////////////////////////
    */

    /**
     * Basic implementation of copy operation. It is likely that
     * sub-classes can implement more efficient copy operations: if so,
     * they should do so.
     */
    protected void copyStartElement(XMLStreamReader sr) throws XMLStreamException {
        // Any namespace declarations/bindings?
        int nsCount = sr.getNamespaceCount();
        if (nsCount > 0) { // yup, got some...
            /* First, need to (or at least, should?) add prefix bindings:
             * (may not be 100% required, but probably a good thing to do,
             * just so that app code has access to prefixes then)
             */
            for (int i = 0; i < nsCount; ++i) {
                String prefix = sr.getNamespacePrefix(i);
                String uri = sr.getNamespaceURI(i);
                if (prefix == null || prefix.isEmpty()) { // default NS
                    setDefaultNamespace(uri);
                } else {
                    setPrefix(prefix, uri);
                }
            }
        }
        writeStartElement(sr.getPrefix(), sr.getLocalName(), sr.getNamespaceURI());

        if (nsCount > 0) {
            // And then output actual namespace declarations:
            for (int i = 0; i < nsCount; ++i) {
                String prefix = sr.getNamespacePrefix(i);
                String uri = sr.getNamespaceURI(i);

                if (prefix == null || prefix.isEmpty()) { // default NS
                    writeDefaultNamespace(uri);
                } else {
                    writeNamespace(prefix, uri);
                }
            }
        }

        /* And then let's just output attributes. But should we copy the
         * implicit attributes (created via attribute defaulting?)
         */
        int attrCount = sr.getAttributeCount();
        if (attrCount > 0) {
            for (int i = 0; i < attrCount; ++i) {
                writeAttribute(sr.getAttributePrefix(i), sr.getAttributeNamespace(i), sr.getAttributeLocalName(i),
                    sr.getAttributeValue(i));
            }
        }
    }
}
