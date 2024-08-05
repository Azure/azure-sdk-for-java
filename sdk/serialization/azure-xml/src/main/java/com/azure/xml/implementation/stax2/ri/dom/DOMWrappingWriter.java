// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
/* Stax2 API extension for Streaming Api for Xml processing (StAX).
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

package com.azure.xml.implementation.stax2.ri.dom;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.*;

import org.w3c.dom.*;

import com.azure.xml.implementation.stax2.XMLStreamLocation2;
import com.azure.xml.implementation.stax2.XMLStreamReader2;
import com.azure.xml.implementation.stax2.XMLStreamWriter2;
import com.azure.xml.implementation.stax2.ri.typed.SimpleValueEncoder;
import com.azure.xml.implementation.stax2.typed.Base64Variant;
import com.azure.xml.implementation.stax2.typed.Base64Variants;
import com.azure.xml.implementation.stax2.validation.*;

/**
 * This is an adapter class that partially implements {@link XMLStreamWriter}
 * as a facade on top of  a DOM document or Node, allowing one
 * to basically construct DOM trees via Stax API.
 * It is meant to serve as basis for a full implementation.
 *<p>
 * Note that the implementation is only to be used with
 * <code>javax.xml.transform.dom.DOMResult</code>. It can however be
 * used for both full documents, and single element root fragments,
 * depending on what node is passed as the argument.
 *<p>
 * One more implementation note: much code is identical to one
 * used by {@link com.azure.xml.implementation.stax2.ri.Stax2WriterAdapter}.
 * Alas it is hard to reuse it without cut'n pasting.
 *
 * @author Tatu Saloranta
 *
 * @since 3.0
 */
public abstract class DOMWrappingWriter implements XMLStreamWriter2 {
    // // Constants to use as defaults for "writeStartDocument"

    final static String DEFAULT_OUTPUT_ENCODING = "UTF-8";

    final static String DEFAULT_XML_VERSION = "1.0";

    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */

    protected final boolean mNsAware;

    protected final boolean mNsRepairing;

    /**
     * This member variable is to keep information about encoding
     * that seems to be used for the document (or fragment) to output,
     * if known.
     */
    protected String mEncoding = null;

    /**
     * If we are being given info about existing bindings, it'll come
     * as a NamespaceContext.
     */
    protected NamespaceContext mNsContext;

    /*
    /**********************************************************************
    /* State
    /**********************************************************************
     */

    /**
     * We need a reference to the document hosting nodes to
     * be able to create new nodes
     */
    protected final Document mDocument;

    /*
    /**********************************************************************
    /* Helper objects
    /**********************************************************************
     */

    /**
     * Encoding of typed values is used the standard encoder
     * included in RI.
     */
    protected SimpleValueEncoder mValueEncoder;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    protected DOMWrappingWriter(Node treeRoot, boolean nsAware, boolean nsRepairing) throws XMLStreamException {
        if (treeRoot == null) {
            throw new IllegalArgumentException("Can not pass null Node for constructing a DOM-based XMLStreamWriter");
        }
        mNsAware = nsAware;
        mNsRepairing = nsRepairing;

        /* Ok; we need a document node; or an element node; or a document
         * fragment node.
         */
        switch (treeRoot.getNodeType()) {
            case Node.DOCUMENT_NODE: // fine
                mDocument = (Document) treeRoot;

                /* Should try to find encoding, version and stand-alone
                 * settings... but is there a standard way of doing that?
                 */
                break;

            case Node.ELEMENT_NODE: // can make sub-tree... ok
                mDocument = treeRoot.getOwnerDocument();
                break;

            case Node.DOCUMENT_FRAGMENT_NODE: // as with element...
                mDocument = treeRoot.getOwnerDocument();
                // Above types are fine
                break;

            default: // other Nodes not usable
                throw new XMLStreamException(
                    "Can not create an XMLStreamWriter for a DOM node of type " + treeRoot.getClass());
        }
        if (mDocument == null) {
            throw new XMLStreamException("Can not create an XMLStreamWriter for given node (of type "
                + treeRoot.getClass() + "): did not have owner document");
        }
    }

    /*
    /**********************************************************************
    /* Partial XMLStreamWriter API (Stax 1.0) impl
    /**********************************************************************
     */

    @Override
    public void close() {
        // NOP
    }

    @Override
    public void flush() {
        // NOP
    }

    @Override
    public abstract NamespaceContext getNamespaceContext();

    @Override
    public abstract String getPrefix(String uri);

    @Override
    public abstract Object getProperty(String name);

    @Override
    public abstract void setDefaultNamespace(String uri);

    @Override
    public void setNamespaceContext(NamespaceContext context) {
        mNsContext = context;
    }

    @Override
    public abstract void setPrefix(String prefix, String uri) throws XMLStreamException;

    @Override
    public abstract void writeAttribute(String localName, String value) throws XMLStreamException;

    @Override
    public abstract void writeAttribute(String nsURI, String localName, String value) throws XMLStreamException;

    @Override
    public abstract void writeAttribute(String prefix, String nsURI, String localName, String value)
        throws XMLStreamException;

    @Override
    public void writeCData(String data) throws XMLStreamException {
        appendLeaf(mDocument.createCDATASection(data));
    }

    @Override
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        writeCharacters(new String(text, start, len));
    }

    @Override
    public void writeCharacters(String text) throws XMLStreamException {
        appendLeaf(mDocument.createTextNode(text));
    }

    @Override
    public void writeComment(String data) throws XMLStreamException {
        appendLeaf(mDocument.createComment(data));
    }

    @Override
    public abstract void writeDefaultNamespace(String nsURI) throws XMLStreamException;

    @Override
    public void writeDTD(String dtd) throws XMLStreamException {
        /* Would need to parse contents, not easy to do via DOM
         * in any case.
         */
        reportUnsupported("writeDTD()");
    }

    @Override
    public abstract void writeEmptyElement(String localName) throws XMLStreamException;

    @Override
    public abstract void writeEmptyElement(String nsURI, String localName) throws XMLStreamException;

    @Override
    public abstract void writeEmptyElement(String prefix, String localName, String nsURI) throws XMLStreamException;

    @Override
    public abstract void writeEndDocument() throws XMLStreamException;

    @Override
    public void writeEntityRef(String name) {
        appendLeaf(mDocument.createEntityReference(name));
    }

    @Override
    public void writeProcessingInstruction(String target) {
        writeProcessingInstruction(target, null);
    }

    @Override
    public void writeProcessingInstruction(String target, String data) {
        appendLeaf(mDocument.createProcessingInstruction(target, data));
    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        // Note: while these defaults are not very intuitive, they
        // are what Stax 1.0 specification clearly mandates:
        writeStartDocument(DEFAULT_OUTPUT_ENCODING, DEFAULT_XML_VERSION);
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
        writeStartDocument(null, version);
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        // Is there anything here we can or should do? No?
        mEncoding = encoding;
    }

    /*
    /**********************************************************************
    /* XMLStreamWriter2 API (Stax2 v3.0): additional accessors
    /**********************************************************************
     */

    @Override
    public XMLStreamLocation2 getLocation() {
        // !!! TBI
        return null;
    }

    @Override
    public String getEncoding() {
        return mEncoding;
    }

    @Override
    public abstract boolean isPropertySupported(String name);

    @Override
    public abstract boolean setProperty(String name, Object value);

    /*
    /**********************************************************************
    /* XMLStreamWriter2 API (Stax2 v2.0): extended write methods
    /**********************************************************************
     */

    @Override
    public void writeCData(char[] text, int start, int len) throws XMLStreamException {
        writeCData(new String(text, start, len));
    }

    @Override
    public abstract void writeDTD(String rootName, String systemId, String publicId, String internalSubset)
        throws XMLStreamException;

    //public void writeDTD(String rootName, String systemId, String publicId, String internalSubset)

    @Override
    public void writeSpace(char[] text, int start, int len) throws XMLStreamException {
        writeSpace(new String(text, start, len));
    }

    @Override
    public void writeSpace(String text) throws XMLStreamException {
        /* This won't work all that well, given there's no way to
         * prevent quoting/escaping. But let's do what we can, since
         * the alternative (throwing an exception) doesn't seem
         * especially tempting choice.
         */
        writeCharacters(text);
    }

    @Override
    public void writeStartDocument(String version, String encoding, boolean standAlone) throws XMLStreamException {
        writeStartDocument(encoding, version);
    }

    /*
    /**********************************************************************
    /* XMLStreamWriter2 API (Stax2 v2.0): validation
    /**********************************************************************
     */

    @Override
    public XMLValidator validateAgainst(XMLValidationSchema schema) throws XMLStreamException {
        // !!! TBI
        return null;
    }

    @Override
    public XMLValidator stopValidatingAgainst(XMLValidationSchema schema) throws XMLStreamException {
        // !!! TBI
        return null;
    }

    @Override
    public XMLValidator stopValidatingAgainst(XMLValidator validator) throws XMLStreamException {
        // !!! TBI
        return null;
    }

    @Override
    public ValidationProblemHandler setValidationProblemHandler(ValidationProblemHandler h) {
        // !!! TBI
        return null;
    }

    /*
    /**********************************************************************
    /* Stax2, pass-through methods
    /**********************************************************************
     */

    @Override
    public void writeRaw(String text) throws XMLStreamException {
        reportUnsupported("writeRaw()");
    }

    @Override
    public void writeRaw(String text, int start, int offset) throws XMLStreamException {
        reportUnsupported("writeRaw()");
    }

    @Override
    public void writeRaw(char[] text, int offset, int length) throws XMLStreamException {
        reportUnsupported("writeRaw()");
    }

    @Override
    public void copyEventFromReader(XMLStreamReader2 r, boolean preserveEventData) {
        // !!! TBI
    }

    /*
    /**********************************************************************
    /* Stax2, output handling
    /**********************************************************************
     */

    /*
    /**********************************************************************
    /* TypedXMLStreamWriter2 implementation (Typed Access API, Stax v3.0)
    /**********************************************************************
     */

    // // // Typed element content write methods

    @Override
    public void writeBoolean(boolean value) throws XMLStreamException {
        writeCharacters(value ? "true" : "false");
    }

    @Override
    public void writeInt(int value) throws XMLStreamException {
        writeCharacters(String.valueOf(value));
    }

    @Override
    public void writeLong(long value) throws XMLStreamException {
        writeCharacters(String.valueOf(value));
    }

    @Override
    public void writeFloat(float value) throws XMLStreamException {
        writeCharacters(String.valueOf(value));
    }

    @Override
    public void writeDouble(double value) throws XMLStreamException {
        writeCharacters(String.valueOf(value));
    }

    @Override
    public void writeInteger(BigInteger value) throws XMLStreamException {
        writeCharacters(value.toString());
    }

    @Override
    public void writeDecimal(BigDecimal value) throws XMLStreamException {
        writeCharacters(value.toString());
    }

    @Override
    public void writeQName(QName name) throws XMLStreamException {
        writeCharacters(serializeQNameValue(name));
    }

    @Override
    public void writeIntArray(int[] value, int from, int length) throws XMLStreamException {
        // true -> start with space, to allow for multiple consecutive
        // to be written
        writeCharacters(getValueEncoder().encodeAsString(value, from, length));
    }

    @Override
    public void writeLongArray(long[] value, int from, int length) throws XMLStreamException {
        // true -> start with space, for multiple segments
        writeCharacters(getValueEncoder().encodeAsString(value, from, length));
    }

    @Override
    public void writeFloatArray(float[] value, int from, int length) throws XMLStreamException {
        // true -> start with space, for multiple segments
        writeCharacters(getValueEncoder().encodeAsString(value, from, length));
    }

    @Override
    public void writeDoubleArray(double[] value, int from, int length) throws XMLStreamException {
        // true -> start with space, for multiple segments
        writeCharacters(getValueEncoder().encodeAsString(value, from, length));
    }

    @Override
    public void writeBinary(byte[] value, int from, int length) throws XMLStreamException {
        writeBinary(Base64Variants.getDefaultVariant(), value, from, length);
    }

    @Override
    public void writeBinary(Base64Variant v, byte[] value, int from, int length) throws XMLStreamException {
        writeCharacters(getValueEncoder().encodeAsString(v, value, from, length));
    }

    // // // Typed attribute value write methods

    @Override
    public void writeBooleanAttribute(String prefix, String nsURI, String localName, boolean value)
        throws XMLStreamException {
        writeAttribute(prefix, nsURI, localName, value ? "true" : "false");
    }

    @Override
    public void writeIntAttribute(String prefix, String nsURI, String localName, int value) throws XMLStreamException {
        writeAttribute(prefix, nsURI, localName, String.valueOf(value));
    }

    @Override
    public void writeLongAttribute(String prefix, String nsURI, String localName, long value)
        throws XMLStreamException {
        writeAttribute(prefix, nsURI, localName, String.valueOf(value));
    }

    @Override
    public void writeFloatAttribute(String prefix, String nsURI, String localName, float value)
        throws XMLStreamException {
        writeAttribute(prefix, nsURI, localName, String.valueOf(value));
    }

    @Override
    public void writeDoubleAttribute(String prefix, String nsURI, String localName, double value)
        throws XMLStreamException {
        writeAttribute(prefix, nsURI, localName, String.valueOf(value));
    }

    @Override
    public void writeIntegerAttribute(String prefix, String nsURI, String localName, BigInteger value)
        throws XMLStreamException {
        writeAttribute(prefix, nsURI, localName, value.toString());
    }

    @Override
    public void writeDecimalAttribute(String prefix, String nsURI, String localName, BigDecimal value)
        throws XMLStreamException {
        writeAttribute(prefix, nsURI, localName, value.toString());
    }

    @Override
    public void writeQNameAttribute(String prefix, String nsURI, String localName, QName name)
        throws XMLStreamException {
        writeAttribute(prefix, nsURI, localName, serializeQNameValue(name));
    }

    @Override
    public void writeIntArrayAttribute(String prefix, String nsURI, String localName, int[] value)
        throws XMLStreamException {
        writeAttribute(prefix, nsURI, localName, getValueEncoder().encodeAsString(value, 0, value.length));
    }

    @Override
    public void writeLongArrayAttribute(String prefix, String nsURI, String localName, long[] value)
        throws XMLStreamException {
        writeAttribute(prefix, nsURI, localName, getValueEncoder().encodeAsString(value, 0, value.length));
    }

    @Override
    public void writeFloatArrayAttribute(String prefix, String nsURI, String localName, float[] value)
        throws XMLStreamException {
        writeAttribute(prefix, nsURI, localName, getValueEncoder().encodeAsString(value, 0, value.length));
    }

    @Override
    public void writeDoubleArrayAttribute(String prefix, String nsURI, String localName, double[] value)
        throws XMLStreamException {
        writeAttribute(prefix, nsURI, localName, getValueEncoder().encodeAsString(value, 0, value.length));
    }

    @Override
    public void writeBinaryAttribute(String prefix, String nsURI, String localName, byte[] value)
        throws XMLStreamException {
        writeBinaryAttribute(Base64Variants.getDefaultVariant(), prefix, nsURI, localName, value);
    }

    @Override
    public void writeBinaryAttribute(Base64Variant v, String prefix, String nsURI, String localName, byte[] value)
        throws XMLStreamException {
        writeAttribute(prefix, nsURI, localName, getValueEncoder().encodeAsString(v, value, 0, value.length));
    }

    /*
    /**********************************************************************
    /* Abstract methods for sub-classes to implement
    /**********************************************************************
     */

    protected abstract void appendLeaf(Node n) throws IllegalStateException;

    /*
    /**********************************************************************
    /* Shared package methods
    /**********************************************************************
     */

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

    /*
    /**********************************************************************
    /* Package methods, basic output problem reporting
    /**********************************************************************
     */

    protected static void throwOutputError(String msg) throws XMLStreamException {
        throw new XMLStreamException(msg);
    }

    protected void reportUnsupported(String operName) {
        throw new UnsupportedOperationException(operName + " can not be used with DOM-backed writer");
    }
}
