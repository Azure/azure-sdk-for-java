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

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.*;

import com.azure.xml.implementation.stax2.*;
import com.azure.xml.implementation.stax2.ri.typed.SimpleValueEncoder;
import com.azure.xml.implementation.stax2.typed.Base64Variant;
import com.azure.xml.implementation.stax2.typed.Base64Variants;
// Not from Stax 1.0, but Stax2 does provide it:
import com.azure.xml.implementation.stax2.util.StreamWriterDelegate;
import com.azure.xml.implementation.stax2.validation.ValidationProblemHandler;
import com.azure.xml.implementation.stax2.validation.XMLValidationSchema;
import com.azure.xml.implementation.stax2.validation.XMLValidator;

/**
 * This adapter implements parts of {@link XMLStreamWriter2}, the
 * extended stream writer defined by Stax2 extension, by wrapping
 * a vanilla Stax 1.0 {@link XMLStreamReader} implementation.
 *<p>
 * Note: the implementation is incomplete as-is, since not all
 * features needed are accessible via basic Stax 1.0 interface.
 * As such, two main use cases for this wrapper are:
 *<ul>
 * <li>Serve as convenient base class for a complete implementation,
 *    which can use native accessors provided by the wrapped Stax
 *    implementation
 *  </li>
 * <li>To be used for tasks that make limited use of Stax2 API, such
 *   that missing parts are not needed
 *  </li>
 * </ul>
 */
public class Stax2WriterAdapter extends StreamWriterDelegate implements XMLStreamWriter2 /* From Stax2 */
    , XMLStreamConstants {
    /**
     * Encoding we have determined to be used, according to method
     * calls (write start document etc.)
     */
    protected String mEncoding;

    protected SimpleValueEncoder mValueEncoder;

    protected final boolean mNsRepairing;

    /*
    ///////////////////////////////////////////////////////////////////////
    // Life-cycle methods
    ///////////////////////////////////////////////////////////////////////
     */

    protected Stax2WriterAdapter(XMLStreamWriter sw) {
        super(sw);
        mDelegate = sw;
        Object value = sw.getProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES);
        mNsRepairing = (value instanceof Boolean) && (Boolean) value;
    }

    /**
     * Method that should be used to add dynamic support for
     * {@link XMLStreamWriter2}. Method will check whether the
     * stream reader passed happens to be a {@link XMLStreamWriter2};
     * and if it is, return it properly cast. If not, it will create
     * necessary wrapper to support features needed by StaxMate,
     * using vanilla Stax 1.0 interface.
     */
    public static XMLStreamWriter2 wrapIfNecessary(XMLStreamWriter sw) {
        if (sw instanceof XMLStreamWriter2) {
            return (XMLStreamWriter2) sw;
        }
        return new Stax2WriterAdapter(sw);
    }

    /*
    ///////////////////////////////////////////////////////////////////////
    // TypedXMLStreamWriter2 implementation
    // (Typed Access API, Stax v3.0)
    ///////////////////////////////////////////////////////////////////////
     */

    // // // Typed element content write methods

    @Override
    public void writeBoolean(boolean b) throws XMLStreamException {
        mDelegate.writeCharacters(b ? "true" : "false");
    }

    @Override
    public void writeInt(int value) throws XMLStreamException {
        mDelegate.writeCharacters(String.valueOf(value));
    }

    @Override
    public void writeLong(long value) throws XMLStreamException {
        mDelegate.writeCharacters(String.valueOf(value));
    }

    @Override
    public void writeFloat(float value) throws XMLStreamException {
        mDelegate.writeCharacters(String.valueOf(value));
    }

    @Override
    public void writeDouble(double value) throws XMLStreamException {
        mDelegate.writeCharacters(String.valueOf(value));
    }

    @Override
    public void writeInteger(BigInteger value) throws XMLStreamException {
        mDelegate.writeCharacters(value.toString());
    }

    @Override
    public void writeDecimal(BigDecimal value) throws XMLStreamException {
        mDelegate.writeCharacters(value.toString());
    }

    @Override
    public void writeQName(QName name) throws XMLStreamException {
        mDelegate.writeCharacters(serializeQNameValue(name));
    }

    @Override
    public void writeIntArray(int[] value, int from, int length) throws XMLStreamException {
        mDelegate.writeCharacters(getValueEncoder().encodeAsString(value, from, length));
    }

    @Override
    public void writeLongArray(long[] value, int from, int length) throws XMLStreamException {
        mDelegate.writeCharacters(getValueEncoder().encodeAsString(value, from, length));
    }

    @Override
    public void writeFloatArray(float[] value, int from, int length) throws XMLStreamException {
        mDelegate.writeCharacters(getValueEncoder().encodeAsString(value, from, length));
    }

    @Override
    public void writeDoubleArray(double[] value, int from, int length) throws XMLStreamException {
        mDelegate.writeCharacters(getValueEncoder().encodeAsString(value, from, length));
    }

    @Override
    public void writeBinary(Base64Variant v, byte[] value, int from, int length) throws XMLStreamException {
        mDelegate.writeCharacters(getValueEncoder().encodeAsString(v, value, from, length));
    }

    @Override
    public void writeBinary(byte[] value, int from, int length) throws XMLStreamException {
        writeBinary(Base64Variants.getDefaultVariant(), value, from, length);
    }

    // // // Typed attribute value write methods

    @Override
    public void writeBooleanAttribute(String prefix, String nsURI, String localName, boolean b)
        throws XMLStreamException {
        mDelegate.writeAttribute(prefix, nsURI, localName, b ? "true" : "false");
    }

    @Override
    public void writeIntAttribute(String prefix, String nsURI, String localName, int value) throws XMLStreamException {
        mDelegate.writeAttribute(prefix, nsURI, localName, String.valueOf(value));
    }

    @Override
    public void writeLongAttribute(String prefix, String nsURI, String localName, long value)
        throws XMLStreamException {
        mDelegate.writeAttribute(prefix, nsURI, localName, String.valueOf(value));
    }

    @Override
    public void writeFloatAttribute(String prefix, String nsURI, String localName, float value)
        throws XMLStreamException {
        mDelegate.writeAttribute(prefix, nsURI, localName, String.valueOf(value));
    }

    @Override
    public void writeDoubleAttribute(String prefix, String nsURI, String localName, double value)
        throws XMLStreamException {
        mDelegate.writeAttribute(prefix, nsURI, localName, String.valueOf(value));
    }

    @Override
    public void writeIntegerAttribute(String prefix, String nsURI, String localName, BigInteger value)
        throws XMLStreamException {
        mDelegate.writeAttribute(prefix, nsURI, localName, value.toString());
    }

    @Override
    public void writeDecimalAttribute(String prefix, String nsURI, String localName, BigDecimal value)
        throws XMLStreamException {
        mDelegate.writeAttribute(prefix, nsURI, localName, value.toString());
    }

    @Override
    public void writeQNameAttribute(String prefix, String nsURI, String localName, QName name)
        throws XMLStreamException {
        mDelegate.writeAttribute(prefix, nsURI, localName, serializeQNameValue(name));
    }

    @Override
    public void writeIntArrayAttribute(String prefix, String nsURI, String localName, int[] value)
        throws XMLStreamException {
        mDelegate.writeAttribute(prefix, nsURI, localName, getValueEncoder().encodeAsString(value, 0, value.length));
    }

    @Override
    public void writeLongArrayAttribute(String prefix, String nsURI, String localName, long[] value)
        throws XMLStreamException {
        mDelegate.writeAttribute(prefix, nsURI, localName, getValueEncoder().encodeAsString(value, 0, value.length));
    }

    @Override
    public void writeFloatArrayAttribute(String prefix, String nsURI, String localName, float[] value)
        throws XMLStreamException {
        mDelegate.writeAttribute(prefix, nsURI, localName, getValueEncoder().encodeAsString(value, 0, value.length));
    }

    @Override
    public void writeDoubleArrayAttribute(String prefix, String nsURI, String localName, double[] value)
        throws XMLStreamException {
        mDelegate.writeAttribute(prefix, nsURI, localName, getValueEncoder().encodeAsString(value, 0, value.length));
    }

    @Override
    public void writeBinaryAttribute(String prefix, String nsURI, String localName, byte[] value)
        throws XMLStreamException {
        writeBinaryAttribute(Base64Variants.getDefaultVariant(), prefix, nsURI, localName, value);
    }

    @Override
    public void writeBinaryAttribute(Base64Variant v, String prefix, String nsURI, String localName, byte[] value)
        throws XMLStreamException {
        mDelegate.writeAttribute(prefix, nsURI, localName, getValueEncoder().encodeAsString(v, value, 0, value.length));
    }

    /*
    ///////////////////////////////////////////////////////////////////////
    // XMLStreamWriter2 (StAX2) implementation
    ///////////////////////////////////////////////////////////////////////
     */

    @Override
    public boolean isPropertySupported(String name) {
        // No real clean way to check this, so let's just fake by
        // claiming nothing is supported
        return false;
    }

    @Override
    public boolean setProperty(String name, Object value) {
        throw new IllegalArgumentException("No settable property '" + name + "'");
    }

    @Override
    public XMLStreamLocation2 getLocation() {
        // No easy way to keep track of it, without impl support
        return null;
    }

    @Override
    public String getEncoding() {
        // We may have been able to infer it... if so:
        return mEncoding;
    }

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
    public void writeStartDocument(String version, String encoding, boolean standAlone) throws XMLStreamException {
        // No good way to do it, so let's do what we can...
        writeStartDocument(encoding, version);
    }

    /*
    ///////////////////////////////////////////////////////////////////////
    // Stax2, Pass-through methods
    ///////////////////////////////////////////////////////////////////////
    */

    @Override
    public void writeRaw(String text) throws XMLStreamException {
        writeRaw(text, 0, text.length());
    }

    @Override
    public void writeRaw(String text, int offset, int len) throws XMLStreamException {
        // There is no clean way to implement this via Stax 1.0, alas...
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void writeRaw(char[] text, int offset, int length) throws XMLStreamException {
        writeRaw(new String(text, offset, length));
    }

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
                    // no output if no real input
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
    ///////////////////////////////////////////////////////////////////////
    // Stax2, output handling
    ///////////////////////////////////////////////////////////////////////
    */

    /*
    ///////////////////////////////////////////////////////////////////////
    // Stax2, validation
    ///////////////////////////////////////////////////////////////////////
    */

    @Override
    public XMLValidator validateAgainst(XMLValidationSchema schema) throws XMLStreamException {
        // !!! TODO: try to implement?
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
    ///////////////////////////////////////////////////////////////////////
    // Helper methods
    ///////////////////////////////////////////////////////////////////////
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

    /**
     * Method called to serialize given qualified name into valid
     * String serialization, taking into account existing namespace
     * bindings.
     */
    protected String serializeQNameValue(QName name) throws XMLStreamException {
        String prefix;
        // Ok as is? In repairing mode need to ensure it's properly bound
        if (mNsRepairing) {
            String uri = name.getNamespaceURI();
            // First: let's see if a valid binding already exists:
            NamespaceContext ctxt = getNamespaceContext();
            prefix = (ctxt == null) ? null : ctxt.getPrefix(uri);
            if (prefix == null) {
                // nope: need to (try to) bind
                String origPrefix = name.getPrefix();
                if (origPrefix == null || origPrefix.isEmpty()) {
                    prefix = "";
                    /* note: could cause a namespace conflict... but
                     * there is nothing we can do with just stax1 stream
                     * writer
                     */
                    writeDefaultNamespace(uri);
                } else {
                    prefix = origPrefix;
                    writeNamespace(prefix, uri);
                }
            }
        } else { // in non-repairing, good as is
            prefix = name.getPrefix();
        }
        String local = name.getLocalPart();
        if (prefix == null || prefix.isEmpty()) {
            return local;
        }

        // Not efficient... but should be ok
        return prefix + ":" + local;
    }

    protected SimpleValueEncoder getValueEncoder() {
        if (mValueEncoder == null) {
            mValueEncoder = new SimpleValueEncoder();
        }
        return mValueEncoder;
    }
}
