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

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.*;

import com.azure.xml.implementation.stax2.*;
import com.azure.xml.implementation.stax2.typed.Base64Variant;
import com.azure.xml.implementation.stax2.typed.Base64Variants;
import com.azure.xml.implementation.stax2.typed.TypedArrayDecoder;
import com.azure.xml.implementation.stax2.typed.TypedValueDecoder;
import com.azure.xml.implementation.stax2.typed.TypedXMLStreamException;
import com.azure.xml.implementation.stax2.validation.*;

import com.azure.xml.implementation.stax2.ri.typed.ValueDecoderFactory;

/**
 * This is a partial base implementation of {@link XMLStreamReader2},
 * the extended stream reader that is part of Stax2.
 */
public abstract class Stax2ReaderImpl implements XMLStreamReader2 /* From Stax2 */
    , AttributeInfo, DTDInfo, LocationInfo {
    /**
     * Factory used for constructing decoders we need for typed access
     */
    protected ValueDecoderFactory _decoderFactory;

    /*
    ////////////////////////////////////////////////////
    // Life-cycle methods
    ////////////////////////////////////////////////////
     */

    protected Stax2ReaderImpl() {
    }

    /*
    ////////////////////////////////////////////////////
    // XMLStreamReader2 (StAX2) implementation
    ////////////////////////////////////////////////////
     */

    // // // StAX2, per-reader configuration

    @Deprecated
    @Override
    public Object getFeature(String name) {
        // No features defined
        return null;
    }

    @Deprecated
    @Override
    public void setFeature(String name, Object value) {
        // No features defined
    }

    // NOTE: getProperty() defined in Stax 1.0 interface

    @Override
    public boolean isPropertySupported(String name) {
        /* No way to cleanly implement this using just Stax 1.0
         * interface, so let's be conservative and decline any knowledge
         * of properties...
         */
        return false;
    }

    @Override
    public boolean setProperty(String name, Object value) {
        return false; // could throw an exception too
    }

    // // // StAX2, additional traversal methods

    @Override
    public void skipElement() throws XMLStreamException {
        if (getEventType() != START_ELEMENT) {
            throwNotStartElem();
        }
        int nesting = 1; // need one more end elements than start elements

        while (true) {
            int type = next();
            if (type == START_ELEMENT) {
                ++nesting;
            } else if (type == END_ELEMENT) {
                if (--nesting == 0) {
                    break;
                }
            }
        }
    }

    // // // StAX2, additional attribute access

    @Override
    public AttributeInfo getAttributeInfo() throws XMLStreamException {
        if (getEventType() != START_ELEMENT) {
            throwNotStartElem();
        }
        return this;
    }

    // // // StAX2, Additional DTD access

    @Override
    public DTDInfo getDTDInfo() throws XMLStreamException {
        if (getEventType() != DTD) {
            return null;
        }
        return this;
    }

    // // // StAX2, Additional location information

    /**
     * Location information is always accessible, for this reader.
     */
    @Override
    public final LocationInfo getLocationInfo() {
        return this;
    }

    // // // StAX2, Pass-through text accessors

    @Override
    public int getText(Writer w, boolean preserveContents) throws IOException, XMLStreamException {
        char[] cbuf = getTextCharacters();
        int start = getTextStart();
        int len = getTextLength();

        if (len > 0) {
            w.write(cbuf, start, len);
        }
        return len;
    }

    // // // StAX 2, Other accessors

    /**
     * @return Number of open elements in the stack; 0 when parser is in
     *  prolog/epilog, 1 inside root element and so on.
     */
    @Override
    public abstract int getDepth();

    @Override
    public abstract boolean isEmptyElement() throws XMLStreamException;

    @Override
    public abstract NamespaceContext getNonTransientNamespaceContext();

    @Override
    public String getPrefixedName() {
        switch (getEventType()) {
            case START_ELEMENT:
            case END_ELEMENT: {
                String prefix = getPrefix();
                String ln = getLocalName();

                if (prefix == null) {
                    return ln;
                }
                return prefix + ':' + ln;
            }

            case ENTITY_REFERENCE:
                return getLocalName();

            case PROCESSING_INSTRUCTION:
                return getPITarget();

            case DTD:
                return getDTDRootName();

        }
        throw new IllegalStateException(
            "Current state not START_ELEMENT, END_ELEMENT, ENTITY_REFERENCE, PROCESSING_INSTRUCTION or DTD");
    }

    @Override
    public void closeCompletely() throws XMLStreamException {
        // As usual, Stax 1.0 offers no generic way of doing just this.
        // But let's at least call the lame basic close()
        close();
    }

    /*
    ////////////////////////////////////////////////////
    // AttributeInfo implementation (StAX 2)
    ////////////////////////////////////////////////////
     */

    // Already part of XMLStreamReader
    //public int getAttributeCount();

    @Override
    public int findAttributeIndex(String nsURI, String localName) {
        // !!! TBI
        return -1;
    }

    @Override
    public int getIdAttributeIndex() {
        // !!! TBI
        return -1;
    }

    @Override
    public int getNotationAttributeIndex() {
        // !!! TBI
        return -1;
    }

    /*
    ////////////////////////////////////////////////////
    // DTDInfo implementation (StAX 2)
    ////////////////////////////////////////////////////
     */

    @Override
    public Object getProcessedDTD() {
        return null;
    }

    @Override
    public String getDTDRootName() {
        return null;
    }

    @Override
    public String getDTDPublicId() {
        return null;
    }

    @Override
    public String getDTDSystemId() {
        return null;
    }

    /**
     * @return Internal subset portion of the DOCTYPE declaration, if any;
     *   empty String if none
     */
    @Override
    public String getDTDInternalSubset() {
        return null;
    }

    // // StAX2, v2.0

    @Override
    public DTDValidationSchema getProcessedDTDSchema() {
        return null;
    }

    /*
    ////////////////////////////////////////////////////
    // LocationInfo implementation (StAX 2)
    ////////////////////////////////////////////////////
     */

    // // // First, the "raw" offset accessors:

    @Override
    public long getStartingByteOffset() {
        return -1L;
    }

    @Override
    public long getStartingCharOffset() {
        return 0;
    }

    @Override
    public long getEndingByteOffset() throws XMLStreamException {
        return -1;
    }

    @Override
    public long getEndingCharOffset() throws XMLStreamException {
        return -1;
    }

    // // // and then the object-based access methods:

    @Override
    public abstract XMLStreamLocation2 getStartLocation();

    @Override
    public abstract XMLStreamLocation2 getCurrentLocation();

    @Override
    public abstract XMLStreamLocation2 getEndLocation() throws XMLStreamException;

    /*
    ////////////////////////////////////////////////////
    // Stax2 validation
    ////////////////////////////////////////////////////
     */

    @Override
    public XMLValidator validateAgainst(XMLValidationSchema schema) throws XMLStreamException {
        throwUnsupported();
        return null;
    }

    @Override
    public XMLValidator stopValidatingAgainst(XMLValidationSchema schema) throws XMLStreamException {
        throwUnsupported();
        return null;
    }

    @Override
    public XMLValidator stopValidatingAgainst(XMLValidator validator) throws XMLStreamException {
        throwUnsupported();
        return null;
    }

    @Override
    public abstract ValidationProblemHandler setValidationProblemHandler(ValidationProblemHandler h);

    /*
    ////////////////////////////////////////////////////////
    // TypedXMLStreamReader, scalar elements
    ////////////////////////////////////////////////////////
     */

    @Override
    public boolean getElementAsBoolean() throws XMLStreamException {
        ValueDecoderFactory.BooleanDecoder dec = _decoderFactory().getBooleanDecoder();
        getElementAs(dec);
        return dec.getValue();
    }

    @Override
    public int getElementAsInt() throws XMLStreamException {
        ValueDecoderFactory.IntDecoder dec = _decoderFactory().getIntDecoder();
        getElementAs(dec);
        return dec.getValue();
    }

    @Override
    public long getElementAsLong() throws XMLStreamException {
        ValueDecoderFactory.LongDecoder dec = _decoderFactory().getLongDecoder();
        getElementAs(dec);
        return dec.getValue();
    }

    @Override
    public float getElementAsFloat() throws XMLStreamException {
        ValueDecoderFactory.FloatDecoder dec = _decoderFactory().getFloatDecoder();
        getElementAs(dec);
        return dec.getValue();
    }

    @Override
    public double getElementAsDouble() throws XMLStreamException {
        ValueDecoderFactory.DoubleDecoder dec = _decoderFactory().getDoubleDecoder();
        getElementAs(dec);
        return dec.getValue();
    }

    @Override
    public BigInteger getElementAsInteger() throws XMLStreamException {
        ValueDecoderFactory.IntegerDecoder dec = _decoderFactory().getIntegerDecoder();
        getElementAs(dec);
        return dec.getValue();
    }

    @Override
    public BigDecimal getElementAsDecimal() throws XMLStreamException {
        ValueDecoderFactory.DecimalDecoder dec = _decoderFactory().getDecimalDecoder();
        getElementAs(dec);
        return dec.getValue();
    }

    @Override
    public QName getElementAsQName() throws XMLStreamException {
        ValueDecoderFactory.QNameDecoder dec = _decoderFactory().getQNameDecoder(getNamespaceContext());
        getElementAs(dec);
        // !!! Should we try to verify validity of name chars?
        return dec.getValue();
    }

    @Override
    public byte[] getElementAsBinary() throws XMLStreamException {
        return getElementAsBinary(Base64Variants.getDefaultVariant());
    }

    // !!! TODO: copy code from Stax2ReaderAdapter?
    @Override
    public abstract byte[] getElementAsBinary(Base64Variant v) throws XMLStreamException;

    @Override
    public void getElementAs(TypedValueDecoder tvd) throws XMLStreamException {
        String value = getElementText();
        try {
            tvd.decode(value);
        } catch (IllegalArgumentException iae) {
            throw _constructTypeException(iae, value);
        }
    }

    /*
    ////////////////////////////////////////////////////////
    // TypedXMLStreamReader2 implementation, array elements
    ////////////////////////////////////////////////////////
     */

    @Override
    public int readElementAsIntArray(int[] value, int from, int length) throws XMLStreamException {
        return readElementAsArray(_decoderFactory().getIntArrayDecoder(value, from, length));
    }

    @Override
    public int readElementAsLongArray(long[] value, int from, int length) throws XMLStreamException {
        return readElementAsArray(_decoderFactory().getLongArrayDecoder(value, from, length));
    }

    @Override
    public int readElementAsFloatArray(float[] value, int from, int length) throws XMLStreamException {
        return readElementAsArray(_decoderFactory().getFloatArrayDecoder(value, from, length));
    }

    @Override
    public int readElementAsDoubleArray(double[] value, int from, int length) throws XMLStreamException {
        return readElementAsArray(_decoderFactory().getDoubleArrayDecoder(value, from, length));
    }

    /**
     * Actual implementation needs to implement tokenization and state
     * keeping.
     *<p>
     * !!! TODO: should be possible to implement completely
     */
    @Override
    public abstract int readElementAsArray(TypedArrayDecoder dec) throws XMLStreamException;

    /*
    ////////////////////////////////////////////////////////
    // TypedXMLStreamReader2 implementation, binary data
    ////////////////////////////////////////////////////////
     */

    @Override
    public int readElementAsBinary(byte[] resultBuffer, int offset, int maxLength) throws XMLStreamException {
        return readElementAsBinary(Base64Variants.getDefaultVariant(), resultBuffer, offset, maxLength);
    }

    public abstract int readElementAsBinary(Base64Variant b64variant, byte[] resultBuffer, int offset, int maxLength)
        throws XMLStreamException;

    /*
    ///////////////////////////////////////////////////////////
    // TypedXMLStreamReader2 implementation, scalar attributes
    ///////////////////////////////////////////////////////////
     */

    @Override
    public abstract int getAttributeIndex(String namespaceURI, String localName);

    @Override
    public boolean getAttributeAsBoolean(int index) throws XMLStreamException {
        ValueDecoderFactory.BooleanDecoder dec = _decoderFactory().getBooleanDecoder();
        getAttributeAs(index, dec);
        return dec.getValue();
    }

    @Override
    public int getAttributeAsInt(int index) throws XMLStreamException {
        ValueDecoderFactory.IntDecoder dec = _decoderFactory().getIntDecoder();
        getAttributeAs(index, dec);
        return dec.getValue();
    }

    @Override
    public long getAttributeAsLong(int index) throws XMLStreamException {
        ValueDecoderFactory.LongDecoder dec = _decoderFactory().getLongDecoder();
        getAttributeAs(index, dec);
        return dec.getValue();
    }

    @Override
    public float getAttributeAsFloat(int index) throws XMLStreamException {
        ValueDecoderFactory.FloatDecoder dec = _decoderFactory().getFloatDecoder();
        getAttributeAs(index, dec);
        return dec.getValue();
    }

    @Override
    public double getAttributeAsDouble(int index) throws XMLStreamException {
        ValueDecoderFactory.DoubleDecoder dec = _decoderFactory().getDoubleDecoder();
        getAttributeAs(index, dec);
        return dec.getValue();
    }

    @Override
    public BigInteger getAttributeAsInteger(int index) throws XMLStreamException {
        ValueDecoderFactory.IntegerDecoder dec = _decoderFactory().getIntegerDecoder();
        getAttributeAs(index, dec);
        return dec.getValue();
    }

    @Override
    public BigDecimal getAttributeAsDecimal(int index) throws XMLStreamException {
        ValueDecoderFactory.DecimalDecoder dec = _decoderFactory().getDecimalDecoder();
        getAttributeAs(index, dec);
        return dec.getValue();
    }

    @Override
    public QName getAttributeAsQName(int index) throws XMLStreamException {
        ValueDecoderFactory.QNameDecoder dec = _decoderFactory().getQNameDecoder(getNamespaceContext());
        getAttributeAs(index, dec);
        // !!! Should we try to verify validity of name chars?
        return dec.getValue();
    }

    @Override
    public void getAttributeAs(int index, TypedValueDecoder tvd) throws XMLStreamException {
        String value = getAttributeValue(index);
        try {
            tvd.decode(value);
        } catch (IllegalArgumentException iae) {
            throw _constructTypeException(iae, value);
        }
    }

    @Override
    public int[] getAttributeAsIntArray(int index) throws XMLStreamException {
        ValueDecoderFactory.IntArrayDecoder dec = _decoderFactory().getIntArrayDecoder();
        getAttributeAsArray(index, dec);
        return dec.getValues();
    }

    @Override
    public long[] getAttributeAsLongArray(int index) throws XMLStreamException {
        ValueDecoderFactory.LongArrayDecoder dec = _decoderFactory().getLongArrayDecoder();
        getAttributeAsArray(index, dec);
        return dec.getValues();
    }

    @Override
    public float[] getAttributeAsFloatArray(int index) throws XMLStreamException {
        ValueDecoderFactory.FloatArrayDecoder dec = _decoderFactory().getFloatArrayDecoder();
        getAttributeAsArray(index, dec);
        return dec.getValues();
    }

    @Override
    public double[] getAttributeAsDoubleArray(int index) throws XMLStreamException {
        ValueDecoderFactory.DoubleArrayDecoder dec = _decoderFactory().getDoubleArrayDecoder();
        getAttributeAsArray(index, dec);
        return dec.getValues();
    }

    /**
     * Actual implementation needs to implement tokenization.
     *<p>
     * !!! TODO: should be possible to implement completely
     */
    @Override
    public abstract int getAttributeAsArray(int index, TypedArrayDecoder tad) throws XMLStreamException;

    @Override
    public byte[] getAttributeAsBinary(int index) throws XMLStreamException {
        return getAttributeAsBinary(Base64Variants.getDefaultVariant(), index);
    }

    public abstract byte[] getAttributeAsBinary(Base64Variant v, int index) throws XMLStreamException;

    /*
    ////////////////////////////////////////////////////
    // Package methods
    ////////////////////////////////////////////////////
     */

    protected ValueDecoderFactory _decoderFactory() {
        if (_decoderFactory == null) {
            _decoderFactory = new ValueDecoderFactory();
        }
        return _decoderFactory;
    }

    protected TypedXMLStreamException _constructTypeException(IllegalArgumentException iae, String lexicalValue) {
        return new TypedXMLStreamException(lexicalValue, iae.getMessage(), getStartLocation(), iae);
    }

    /*
    ////////////////////////////////////////////////////
    // Internal methods
    ////////////////////////////////////////////////////
     */

    protected void throwUnsupported() throws XMLStreamException {
        throw new XMLStreamException("Unsupported method");
    }

    protected void throwNotStartElem() {
        throw new IllegalStateException("Current state not START_ELEMENT");
    }
}
