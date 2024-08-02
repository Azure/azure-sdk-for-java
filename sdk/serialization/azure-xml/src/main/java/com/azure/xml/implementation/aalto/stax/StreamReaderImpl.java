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

package com.azure.xml.implementation.aalto.stax;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.azure.xml.implementation.stax2.*;
import com.azure.xml.implementation.stax2.typed.Base64Variant;
import com.azure.xml.implementation.stax2.typed.Base64Variants;
import com.azure.xml.implementation.stax2.typed.TypedArrayDecoder;
import com.azure.xml.implementation.stax2.typed.TypedValueDecoder;
import com.azure.xml.implementation.stax2.typed.TypedXMLStreamException;
import com.azure.xml.implementation.stax2.validation.DTDValidationSchema;
import com.azure.xml.implementation.stax2.validation.XMLValidator;
import com.azure.xml.implementation.stax2.validation.XMLValidationSchema;
import com.azure.xml.implementation.stax2.validation.ValidationProblemHandler;

import com.azure.xml.implementation.stax2.ri.Stax2Util;
import com.azure.xml.implementation.stax2.ri.typed.CharArrayBase64Decoder;
import com.azure.xml.implementation.stax2.ri.typed.ValueDecoderFactory;

import com.azure.xml.implementation.aalto.UncheckedStreamException;
import com.azure.xml.implementation.aalto.WFCException;
import com.azure.xml.implementation.aalto.impl.ErrorConsts;
import com.azure.xml.implementation.aalto.impl.IoStreamException;
import com.azure.xml.implementation.aalto.in.InputBootstrapper;
import com.azure.xml.implementation.aalto.in.PName;
import com.azure.xml.implementation.aalto.in.ReaderConfig;
import com.azure.xml.implementation.aalto.in.XmlScanner;
import com.azure.xml.implementation.aalto.util.TextAccumulator;
import com.azure.xml.implementation.aalto.util.XmlNames;

/**
 * Basic backend-independent {@link XMLStreamReader} implementation.
 * While the read implements Stax API, most of real work is delegated
 * to input (and thereby, encoding) specific backend implementations.
 */
public class StreamReaderImpl implements XMLStreamReader2, AttributeInfo, DTDInfo, LocationInfo {
    // // // Main state constants

    final static int STATE_PROLOG = 0; // Before root element
    final static int STATE_TREE = 1; // Parsing actual XML tree
    final static int STATE_EPILOG = 2; // After root element has been closed
    final static int STATE_CLOSED = 3; // After reader has been closed

    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */

    /**
     * Underlying XML scanner
     */
    protected final XmlScanner _scanner;

    // // // Config flags:

    protected final boolean _cfgCoalesceText;

    protected final boolean _cfgReportTextAsChars;

    /*
    /**********************************************************************
    /* Current state
    /**********************************************************************
     */

    protected int _currToken;

    /**
     * Main parsing/tokenization state (STATE_xxx)
     */
    protected int _parseState;

    /**
     * Prefixed name associated with the current event, if any.
     */
    protected PName _currName;

    /**
     * If the current event is <code>START_ELEMENT</code>, number
     * of attributes the start element has. Otherwise undefined.
     * Updated by reader, to make index checks for other attribute
     * access methods simpler.
     */
    protected int _attrCount;

    /**
     * Factory used for constructing decoders we need for typed access
     */
    protected ValueDecoderFactory _decoderFactory;

    /**
     * Lazily-constructed decoder object for decoding base64 encoded
     * element binary content.
     */
    protected CharArrayBase64Decoder _base64Decoder = null;

    /*
    /**********************************************************************
    /* Collected other information
    /**********************************************************************
     */

    // // // Info from XML declaration:

    //final String mXmlDeclEncoding;
    //final int mXmlDeclVersion;
    //final int mXmlDeclStandalone;

    //final String mInputEncoding;

    /**
     * Prefixed root-name DOCTYPE declaration gave us, if any (note: also
     * serves as a marker to know if we have seen DOCTYPE yet)
     */
    protected PName _dtdRootName;

    /*
    /**********************************************************************
    /* Life-cycle:
    /**********************************************************************
     */

    public StreamReaderImpl(XmlScanner scanner) {
        _scanner = scanner;
        _currToken = START_DOCUMENT;
        ReaderConfig cfg = scanner.getConfig();
        _cfgCoalesceText = cfg.willCoalesceText();
        _cfgReportTextAsChars = !cfg.willReportCData();
    }

    public static StreamReaderImpl construct(InputBootstrapper bs) throws XMLStreamException {
        return new StreamReaderImpl(bs.bootstrap());
    }

    /**
     * Should not really be public, but needed by SAX code
     */
    public XmlScanner getScanner() {
        return _scanner;
    }

    /*
    /**********************************************************************
    /* XMLStreamReader API
    /**********************************************************************
     */

    // // // Bit masks used for quick type comparisons

    /**
     * Bitmask for determining if it's ok to call 'getText'
     */
    final private static int MASK_GET_TEXT
        = (1 << CHARACTERS) | (1 << CDATA) | (1 << SPACE) | (1 << COMMENT) | (1 << DTD) | (1 << ENTITY_REFERENCE);

    /**
     * Bitmask for determining if it's ok to call 'getTextXXX' methods
     * (not including 'getText' itself)
     */
    final private static int MASK_GET_TEXT_XXX = (1 << CHARACTERS) | (1 << CDATA) | (1 << SPACE) | (1 << COMMENT);

    /**
     * This mask is used with Stax2 getText() method (one that takes
     * Writer as an argument): accepts even wider range of event types.
     */
    final private static int MASK_GET_TEXT_WITH_WRITER = (1 << CHARACTERS) | (1 << CDATA) | (1 << SPACE)
        | (1 << COMMENT) | (1 << DTD) | (1 << ENTITY_REFERENCE) | (1 << PROCESSING_INSTRUCTION);

    final private static int MASK_GET_ELEMENT_TEXT
        = (1 << CHARACTERS) | (1 << CDATA) | (1 << SPACE) | (1 << ENTITY_REFERENCE);

    final private static int MASK_TYPED_ACCESS_ARRAY = (1 << START_ELEMENT) | (1 << END_ELEMENT) // for convenience
        | (1 << CHARACTERS) | (1 << CDATA) | (1 << SPACE)
    // Not ok for PI or COMMENT? Let's assume so
    ;

    final private static int MASK_TYPED_ACCESS_BINARY = (1 << START_ELEMENT) //  note: END_ELEMENT handled separately
        | (1 << CHARACTERS) | (1 << CDATA) | (1 << SPACE);

    /*
    /**********************************************************************
    /* XMLStreamReader, document info
    /**********************************************************************
     */

    /**
     * As per Stax (1.0) specs, needs to return whatever xml declaration
     * claimed encoding is, if any; or null if no xml declaration found.
     *<p>
     * Note: method name is rather confusing (compare to {@link #getEncoding}).
     */
    @Override
    public final String getCharacterEncodingScheme() {
        return _scanner.getConfig().getXmlDeclEncoding();
    }

    /**
     * As per Stax (1.0) specs, needs to return whatever parser determined
     * the encoding was, if it was able to figure it out. If not (there are
     * cases where this can not be found; specifically when being passed a
     * {@link java.io.Reader}), it should return null.
     */
    @Override
    public final String getEncoding() {
        return _scanner.getConfig().getActualEncoding();
    }

    @Override
    public String getVersion() {
        return _scanner.getConfig().getXmlDeclVersion();
    }

    @Override
    public final boolean isStandalone() {
        return (_scanner.getConfig().getXmlDeclStandalone() == ReaderConfig.STANDALONE_YES);
    }

    @Override
    public final boolean standaloneSet() {
        return (_scanner.getConfig().getXmlDeclStandalone() != ReaderConfig.STANDALONE_UNKNOWN);
    }

    /*
    /**********************************************************************
    /* Public API, configuration
    /**********************************************************************
     */

    @Override
    public Object getProperty(String name) {
        if (name.equals("javax.xml.stream.entities")) {
            // !!! TBI
            return Collections.EMPTY_LIST;
        }
        if (name.equals("javax.xml.stream.notations")) {
            // !!! TBI
            return Collections.EMPTY_LIST;
        }
        // false -> not mandatory, unrecognized will return null
        return _scanner.getConfig().getProperty(name, false);
    }

    public ReaderConfig getConfig() {
        return _scanner.getConfig();
    }

    /*
    /**********************************************************************
    /* XMLStreamReader, current state
    /**********************************************************************
     */

    // // // Attribute access:

    @Override
    public final int getAttributeCount() {
        if (_currToken != START_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_STELEM);
        }
        return _attrCount;
    }

    @Override
    public final String getAttributeLocalName(int index) {
        if (_currToken != START_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_STELEM);
        }
        if (index >= _attrCount || index < 0) {
            reportInvalidAttrIndex(index);
        }
        return _scanner.getAttrLocalName(index);
    }

    @Override
    public final QName getAttributeName(int index) {
        if (_currToken != START_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_STELEM);
        }
        if (index >= _attrCount || index < 0) {
            reportInvalidAttrIndex(index);
        }
        return _scanner.getAttrQName(index);
    }

    @Override
    public final String getAttributeNamespace(int index) {
        if (_currToken != START_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_STELEM);
        }
        if (index >= _attrCount || index < 0) {
            reportInvalidAttrIndex(index);
        }
        String p = _scanner.getAttrNsURI(index);
        return (p == null) ? "" : p;
    }

    @Override
    public final String getAttributePrefix(int index) {
        if (_currToken != START_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_STELEM);
        }
        if (index >= _attrCount || index < 0) {
            reportInvalidAttrIndex(index);
        }
        String p = _scanner.getAttrPrefix(index);
        return (p == null) ? "" : p;
    }

    @Override
    public final String getAttributeType(int index) {
        if (_currToken != START_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_STELEM);
        }
        if (index >= _attrCount || index < 0) {
            reportInvalidAttrIndex(index);
        }
        return _scanner.getAttrType(index);
    }

    @Override
    public final String getAttributeValue(int index) {
        if (_currToken != START_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_STELEM);
        }
        if (index >= _attrCount || index < 0) {
            reportInvalidAttrIndex(index);
        }
        return _scanner.getAttrValue(index);
    }

    @Override
    public final String getAttributeValue(String nsURI, String localName) {
        if (_currToken != START_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_STELEM);
        }
        return _scanner.getAttrValue(nsURI, localName);
    }

    /**
     * From StAX specs:
     *<blockquote>
     * Reads the content of a text-only element, an exception is thrown if
     * this is not a text-only element.
     * Regardless of value of javax.xml.stream.isCoalescing this method always
     * returns coalesced content.
     *<br>Precondition: the current event is START_ELEMENT.
     *<br>Postcondition: the current event is the corresponding END_ELEMENT. 
     *</blockquote>
     */
    @Override
    public final String getElementText() throws XMLStreamException {
        if (_currToken != START_ELEMENT) {
            throwWfe(ErrorConsts.ERR_STATE_NOT_STELEM);
        }
        // First need to find a textual event
        while (true) {
            int type = next();
            if (type == END_ELEMENT) {
                return "";
            }
            if (type == COMMENT || type == PROCESSING_INSTRUCTION) {
                continue;
            }
            if (((1 << type) & MASK_GET_ELEMENT_TEXT) == 0) {
                _reportNonTextEvent(type);
            }
            break;
        }
        String text = _scanner.getText();
        // Then we'll see if end is nigh...
        TextAccumulator acc = null;
        int type;

        while ((type = next()) != END_ELEMENT) {
            if (((1 << type) & MASK_GET_ELEMENT_TEXT) != 0) {
                if (acc == null) {
                    acc = new TextAccumulator();
                    acc.addText(text);
                }
                acc.addText(getText());
                continue;
            }
            if (type != COMMENT && type != PROCESSING_INSTRUCTION) {
                _reportNonTextEvent(type);
            }
        }
        return (acc == null) ? text : acc.getAndClear();
    }

    /**
     * Returns type of the last event returned; or START_DOCUMENT before
     * any events has been explicitly returned.
     */
    @Override
    public final int getEventType() {
        /* Only complication -- multi-part coalesced text is to be reported
         * as CHARACTERS always, never as CDATA (StAX specs).
         */
        if (_currToken == CDATA) {
            if (_cfgCoalesceText || _cfgReportTextAsChars) {
                return CHARACTERS;
            }
        }
        return _currToken;
    }

    @Override
    public final String getLocalName() {
        // Note: for this we need not (yet) finish reading element
        if (_currToken == START_ELEMENT || _currToken == END_ELEMENT || _currToken == ENTITY_REFERENCE) {
            return _currName.getLocalName();
        }
        throw new IllegalStateException("Current state not START_ELEMENT, END_ELEMENT or ENTITY_REFERENCE");
    }

    // // // getLocation() defined in StreamScanner

    @Override
    public final QName getName() {
        if (_currToken != START_ELEMENT && _currToken != END_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_ELEM);
        }
        return _scanner.getQName();
    }

    // // // Namespace access

    @Override
    public final NamespaceContext getNamespaceContext() {
        /* Unlike other getNamespaceXxx methods, this is available
         * for all events.
         * Note that the context is "live", ie. remains active (but not
         * static) even through calls to next(). StAX compliant apps
         * should not count on this behaviour, however.         
         */
        return _scanner;
    }

    @Override
    public final int getNamespaceCount() {
        if (_currToken != START_ELEMENT && _currToken != END_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_ELEM);
        }
        return _scanner.getNsCount();
    }

    @Override
    public final String getNamespacePrefix(int index) {
        if (_currToken != START_ELEMENT && _currToken != END_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_ELEM);
        }
        String p = _scanner.getNamespacePrefix(index);
        return (p == null) ? "" : p;
    }

    @Override
    public final String getNamespaceURI() {
        if (_currToken != START_ELEMENT && _currToken != END_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_ELEM);
        }
        String uri = _scanner.getNamespaceURI();
        return (uri == null) ? "" : uri;
    }

    @Override
    public final String getNamespaceURI(int index) {
        if (_currToken != START_ELEMENT && _currToken != END_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_ELEM);
        }
        String uri = _scanner.getNamespaceURI(index);
        return (uri == null) ? "" : uri;
    }

    @Override
    public final String getNamespaceURI(String prefix) {
        if (_currToken != START_ELEMENT && _currToken != END_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_ELEM);
        }
        /* Note: null means 'no such prefix bound', whereas "" means
         * "no namespace" (only applicable for prefix "")
         */
        return _scanner.getNamespaceURI(prefix);
    }

    @Override
    public final String getPIData() {
        if (_currToken != PROCESSING_INSTRUCTION) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_PI);
        }
        try {
            return _scanner.getText();
        } catch (XMLStreamException sex) {
            throw UncheckedStreamException.createFrom(sex);
        }
    }

    @Override
    public final String getPITarget() {
        if (_currToken != PROCESSING_INSTRUCTION) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_PI);
        }
        return _currName.getLocalName();
    }

    @Override
    public final String getPrefix() {
        if (_currToken != START_ELEMENT && _currToken != END_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_ELEM);
        }
        // Note: need to return "" for "no prefix", not null, so:
        String p = _currName.getPrefix();
        return (p == null) ? "" : p;
    }

    @Override
    public final String getText() {
        if (((1 << _currToken) & MASK_GET_TEXT) == 0) {
            throwNotTextual(_currToken);
        }
        try {
            return _scanner.getText();
        } catch (XMLStreamException sex) {
            throw UncheckedStreamException.createFrom(sex);
        }
    }

    @Override
    public final char[] getTextCharacters() {
        if (((1 << _currToken) & MASK_GET_TEXT_XXX) == 0) {
            throwNotTextXxx(_currToken);
        }
        try {
            return _scanner.getTextCharacters();
        } catch (XMLStreamException sex) {
            throw UncheckedStreamException.createFrom(sex);
        }
    }

    @Override
    public final int getTextCharacters(int srcStart, char[] target, int targetStart, int len) {
        if (((1 << _currToken) & MASK_GET_TEXT_XXX) == 0) {
            throwNotTextXxx(_currToken);
        }
        try {
            return _scanner.getTextCharacters(srcStart, target, targetStart, len);
        } catch (XMLStreamException sex) {
            throw UncheckedStreamException.createFrom(sex);
        }
    }

    @Override
    public final int getTextLength() {
        if (((1 << _currToken) & MASK_GET_TEXT_XXX) == 0) {
            throwNotTextXxx(_currToken);
        }
        try {
            return _scanner.getTextLength();
        } catch (XMLStreamException sex) {
            throw UncheckedStreamException.createFrom(sex);
        }
    }

    @Override
    public final int getTextStart() {
        if (((1 << _currToken) & MASK_GET_TEXT_XXX) == 0) {
            throwNotTextXxx(_currToken);
        }
        /* Scanner always stores text from the beginning of its
         * buffers...
         */
        return 0;
    }

    @Override
    public final boolean hasName() {
        return (_currToken == START_ELEMENT) || (_currToken == END_ELEMENT);
    }

    @Override
    public final boolean hasNext() {
        return (_currToken != END_DOCUMENT);
    }

    @Override
    public final boolean hasText() {
        return (((1 << _currToken) & MASK_GET_TEXT) != 0);
    }

    @Override
    public final boolean isAttributeSpecified(int index) {
        // No need to check for ATTRIBUTE since we never return that...
        if (_currToken != START_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_STELEM);
        }
        return _scanner.isAttrSpecified(index);
    }

    @Override
    public final boolean isCharacters() {
        return (getEventType() == CHARACTERS);
    }

    @Override
    public final boolean isEndElement() {
        return (_currToken == END_ELEMENT);
    }

    @Override
    public final boolean isStartElement() {
        return (_currToken == START_ELEMENT);
    }

    @Override
    public final boolean isWhiteSpace() {
        if (_currToken == CHARACTERS || _currToken == CDATA) {
            try {
                return _scanner.isTextWhitespace();
            } catch (XMLStreamException sex) {
                throw UncheckedStreamException.createFrom(sex);
            }
        }
        return (_currToken == SPACE);
    }

    @Override
    public final void require(int type, String nsUri, String localName) throws XMLStreamException {
        int curr = _currToken;

        /* There are some special cases; specifically, CDATA
         * is sometimes reported as CHARACTERS. Let's be lenient by
         * allowing both 'real' and reported types, for now.
         */
        if (curr != type) {
            if (curr == CDATA) {
                if (_cfgCoalesceText || _cfgReportTextAsChars) {
                    curr = CHARACTERS;
                }
            } else if (curr == SPACE) {
                // Hmmh. Should we require it to be empty or something?
                //curr = CHARACTERS;
                // For now, let's not change the check
            }
        }

        if (type != curr) {
            throwWfe("Expected type " + ErrorConsts.tokenTypeDesc(type) + ", current type "
                + ErrorConsts.tokenTypeDesc(curr));
        }

        if (localName != null) {
            if (curr != START_ELEMENT && curr != END_ELEMENT && curr != ENTITY_REFERENCE) {
                throwWfe(
                    "Expected non-null local name, but current token not a START_ELEMENT, END_ELEMENT or ENTITY_REFERENCE (was "
                        + ErrorConsts.tokenTypeDesc(_currToken) + ")");
            }
            String n = getLocalName();
            if (n != localName && !n.equals(localName)) {
                throwWfe("Expected local name '" + localName + "'; current local name '" + n + "'.");
            }
        }
        if (nsUri != null) {
            if (curr != START_ELEMENT && curr != END_ELEMENT) {
                throwWfe("Expected non-null NS URI, but current token not a START_ELEMENT or END_ELEMENT (was "
                    + ErrorConsts.tokenTypeDesc(curr) + ")");
            }
            String uri = getNamespaceURI();
            // No namespace?
            if (nsUri.length() == 0) {
                if (uri != null && uri.length() > 0) {
                    throwWfe("Expected empty namespace, instead have '" + uri + "'.");
                }
            } else {
                if ((nsUri != uri) && !nsUri.equals(uri)) {
                    throwWfe("Expected namespace '" + nsUri + "'; have '" + uri + "'.");
                }
            }
        }
        // Ok, fine, all's good
    }

    /*
    /**********************************************************************
    /* XMLStreamReader, iterating
    /**********************************************************************
     */

    @Override
    public final int next() throws XMLStreamException {
        if (_parseState == STATE_TREE) {
            int type = _scanner.nextFromTree();
            if (type == XmlScanner.TOKEN_EOI) { // Not allowed here...
                handleTreeEoi();
            }
            _currToken = type;
            /* Special cases -- sometimes (when coalescing text, or
             * when specifically configured to do so), CDATA and SPACE are
             * to be reported as CHARACTERS, although we still will
             * internally keep track of the real type.
             */
            if (type == CDATA) {
                if (_cfgCoalesceText || _cfgReportTextAsChars) {
                    return CHARACTERS;
                }
            } else {
                _currName = _scanner.getName();
                if (type == END_ELEMENT) { // root closed?
                    if (_scanner.hasEmptyStack()) {
                        _parseState = STATE_EPILOG;
                    }
                } else if (type == START_ELEMENT) {
                    _attrCount = _scanner.getAttrCount();
                }
            }
            return type;
        }

        int type;

        if (_parseState == STATE_PROLOG) {
            type = _scanner.nextFromProlog(true);
            // Did we get the root element?
            if (type == START_ELEMENT) {
                _parseState = STATE_TREE;
                _attrCount = _scanner.getAttrCount();
            } else if (type == DTD) {
                if (_dtdRootName != null) { // dup DOCTYPEs not allowed
                    throwWfe("Duplicate DOCTYPE declaration");
                }
                _dtdRootName = _scanner.getName();
            }
        } else if (_parseState == STATE_EPILOG) {
            type = _scanner.nextFromProlog(false);
        } else {
            // == STATE_CLOSED
            //return END_DOCUMENT;
            throw new java.util.NoSuchElementException();
        }
        if (type < 0) { // end-of-input
            // Need to mark 
            return handlePrologEoi(_parseState == STATE_PROLOG);
        }
        _currName = _scanner.getName();
        return (_currToken = type);
    }

    @Override
    public final int nextTag() throws XMLStreamException {
        while (true) {
            int next = next();

            switch (next) {
                case SPACE:
                case COMMENT:
                case PROCESSING_INSTRUCTION:
                    continue;

                case CDATA:
                case CHARACTERS:
                    if (isWhiteSpace()) {
                        continue;
                    }
                    throwWfe("Received non-all-whitespace CHARACTERS or CDATA event in nextTag().");
                    break; // never gets here, but jikes complains without

                case START_ELEMENT:
                case END_ELEMENT:
                    return next;
            }
            throwWfe(
                "Received event " + ErrorConsts.tokenTypeDesc(next) + ", instead of START_ELEMENT or END_ELEMENT.");
        }
    }

    /**
     *<p>
     * Note: as per StAX 1.0 specs, this method does NOT close the underlying
     * input reader. (that is, unless the new StAX2 property
     * <code>com.azure.xml.implementation.stax2.XMLInputFactory2#P_AUTO_CLOSE_INPUT</code> is
     * set to true).
     */
    @Override
    public final void close() throws XMLStreamException {
        _closeScanner(false);
    }

    @Override
    public final Location getLocation() {
        return getStartLocation();
    }

    /*
    /**********************************************************************
    /* TypedXMLStreamReader2 implementation
    /**********************************************************************
     */

    @Override
    public final boolean getElementAsBoolean() throws XMLStreamException {
        ValueDecoderFactory.BooleanDecoder dec = _decoderFactory().getBooleanDecoder();
        getElementAs(dec);
        return dec.getValue();
    }

    @Override
    public final int getElementAsInt() throws XMLStreamException {
        ValueDecoderFactory.IntDecoder dec = _decoderFactory().getIntDecoder();
        getElementAs(dec);
        return dec.getValue();
    }

    @Override
    public final long getElementAsLong() throws XMLStreamException {
        ValueDecoderFactory.LongDecoder dec = _decoderFactory().getLongDecoder();
        getElementAs(dec);
        return dec.getValue();
    }

    @Override
    public final float getElementAsFloat() throws XMLStreamException {
        ValueDecoderFactory.FloatDecoder dec = _decoderFactory().getFloatDecoder();
        getElementAs(dec);
        return dec.getValue();
    }

    @Override
    public final double getElementAsDouble() throws XMLStreamException {
        ValueDecoderFactory.DoubleDecoder dec = _decoderFactory().getDoubleDecoder();
        getElementAs(dec);
        return dec.getValue();
    }

    @Override
    public final BigInteger getElementAsInteger() throws XMLStreamException {
        ValueDecoderFactory.IntegerDecoder dec = _decoderFactory().getIntegerDecoder();
        getElementAs(dec);
        return dec.getValue();
    }

    @Override
    public final BigDecimal getElementAsDecimal() throws XMLStreamException {
        ValueDecoderFactory.DecimalDecoder dec = _decoderFactory().getDecimalDecoder();
        getElementAs(dec);
        return dec.getValue();
    }

    @Override
    public final QName getElementAsQName() throws XMLStreamException {
        ValueDecoderFactory.QNameDecoder dec = _decoderFactory().getQNameDecoder(getNamespaceContext());
        getElementAs(dec);
        return verifyQName(dec.getValue());
    }

    @Override
    public final byte[] getElementAsBinary() throws XMLStreamException {
        return getElementAsBinary(Base64Variants.getDefaultVariant());
    }

    @Override
    public final void getElementAs(TypedValueDecoder tvd) throws XMLStreamException {
        // !!! TODO: optimize
        String value = getElementText();
        value = value.trim();
        if (value.length() == 0) {
            _handleEmptyValue(tvd);
            return;
        }
        try {
            tvd.decode(value);
        } catch (IllegalArgumentException iae) {
            throw _constructTypeException(iae, value);
        }
    }

    @Override
    public final byte[] getElementAsBinary(Base64Variant v) throws XMLStreamException {
        // note: code here is similar to Base64DecoderBase.aggregateAll(), see comments there
        Stax2Util.ByteAggregator aggr = _base64Decoder().getByteAggregator();
        byte[] buffer = aggr.startAggregation();
        while (true) {
            int offset = 0;
            int len = buffer.length;

            do {
                int readCount = readElementAsBinary(buffer, offset, len, v);
                if (readCount < 1) { // all done!
                    return aggr.aggregateAll(buffer, offset);
                }
                offset += readCount;
                len -= readCount;
            } while (len > 0);
            buffer = aggr.addFullBlock(buffer);
        }
    }

    /*
    /**********************************************************************
    /* TypedXMLStreamReader2 implementation, array elements
    /**********************************************************************
     */

    @Override
    public final int readElementAsIntArray(int[] value, int from, int length) throws XMLStreamException {
        return readElementAsArray(_decoderFactory().getIntArrayDecoder(value, from, length));
    }

    @Override
    public final int readElementAsLongArray(long[] value, int from, int length) throws XMLStreamException {
        return readElementAsArray(_decoderFactory().getLongArrayDecoder(value, from, length));
    }

    @Override
    public final int readElementAsFloatArray(float[] value, int from, int length) throws XMLStreamException {
        return readElementAsArray(_decoderFactory().getFloatArrayDecoder(value, from, length));
    }

    @Override
    public final int readElementAsDoubleArray(double[] value, int from, int length) throws XMLStreamException {
        return readElementAsArray(_decoderFactory().getDoubleArrayDecoder(value, from, length));
    }

    @Override
    public final int readElementAsArray(TypedArrayDecoder dec) throws XMLStreamException {
        int type = _currToken;
        // First things first: must be acceptable start state:
        if (((1 << type) & MASK_TYPED_ACCESS_ARRAY) == 0) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_STELEM_OR_TEXT);
        }

        // need to keep track of when we move to a new token:
        boolean reset;

        // Are we just starting (START_ELEMENT)?
        if (type == START_ELEMENT) {
            // Empty? Not common, but can short-cut handling if occurs
            if (_scanner.isEmptyTag()) {
                // might be possible to optimize, but for now this'll do:
                next();
                return -1;
            }
            // Otherwise let's just find the first text segment
            while (true) {
                type = next();
                if (type == END_ELEMENT) { // Simple, no textual content
                    return -1;
                }
                if (type == COMMENT || type == PROCESSING_INSTRUCTION) {
                    continue;
                }
                if (type == CHARACTERS || type == CDATA) {
                    break;
                }
                // otherwise just not legal (how about SPACE, unexpanded entities?)
                throw _constructUnexpectedInTyped(type);
            }
            reset = true; // yes, we'll be getting a new text segment
        } else {
            reset = false; // may have an existing text segment
        }

        int count = 0;
        while (type != END_ELEMENT) {
            /* Ok then: we will now have a valid textual type. Just need to
             * ensure current segment is completed etc.
             */
            if (type == CHARACTERS || type == CDATA || type == SPACE) {
                count += _scanner.decodeElements(dec, reset);
                if (!dec.hasRoom()) {
                    break;
                }
            } else if (type == COMMENT || type == PROCESSING_INSTRUCTION) {
                ;
            } else {
                throw _constructUnexpectedInTyped(type);
            }
            reset = true;
            type = next();
        }

        // If nothing was found, needs to be indicated via -1, not 0
        return (count > 0) ? count : -1;
    }

    /*
    /**********************************************************************
    /* TypedXMLStreamReader2 implementation, binary data
    /**********************************************************************
     */

    @Override
    public final int readElementAsBinary(byte[] resultBuffer, int offset, int maxLength) throws XMLStreamException {
        return readElementAsBinary(resultBuffer, offset, maxLength, Base64Variants.getDefaultVariant());
    }

    @Override
    public final int readElementAsBinary(byte[] resultBuffer, int offset, int maxLength, Base64Variant v)
        throws XMLStreamException {
        if (resultBuffer == null) {
            throw new IllegalArgumentException("resultBuffer is null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException(
                "Illegal offset (" + offset + "), must be [0, " + resultBuffer.length + "[");
        }
        if (maxLength < 1 || (offset + maxLength) > resultBuffer.length) {
            if (maxLength == 0) { // special case, allowed, but won't do anything
                return 0;
            }
            throw new IllegalArgumentException("Illegal maxLength (" + maxLength
                + "), has to be positive number, and offset+maxLength can not exceed" + resultBuffer.length);
        }

        final CharArrayBase64Decoder dec = _base64Decoder();
        int type = _currToken;
        // First things first: must be acceptable start state:
        if (((1 << type) & MASK_TYPED_ACCESS_BINARY) == 0) {
            if (type == END_ELEMENT) {
                // Minor complication: may have unflushed stuff (non-padded versions)
                if (!dec.hasData()) {
                    return -1;
                }
            } else {
                throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_STELEM_OR_TEXT);
            }
        } else if (type == START_ELEMENT) { // just starting (START_ELEMENT)?
            if (_scanner.isEmptyTag()) {
                // might be possible to optimize, but for now this'll do:
                next();
                return -1;
            }
            // Otherwise let's just find the first text segment
            while (true) {
                type = next();
                if (type == END_ELEMENT) { // Simple, no textual content
                    return -1;
                }
                if (type == COMMENT || type == PROCESSING_INSTRUCTION) {
                    continue;
                }
                if (type == CHARACTERS || type == CDATA) {
                    break;
                }
                // otherwise just not legal (how about SPACE, unexpanded entities?)
                throw _constructUnexpectedInTyped(type);
            }
            _scanner.resetForDecoding(v, dec, true); // true -> first segment
        }

        int totalCount = 0;

        main_loop: while (true) {
            // Ok, decode:
            int count;
            try {
                count = dec.decode(resultBuffer, offset, maxLength);
            } catch (IllegalArgumentException iae) {
                // !!! 26-Sep-2008, tatus: should try to figure out which char (etc) triggered problem to pass with typed exception
                throw _constructTypeException(iae.getMessage(), "");
            }
            offset += count;
            totalCount += count;
            maxLength -= count;

            /* And if we filled the buffer we are done. Or, an edge
             * case: reached END_ELEMENT (for non-padded variant)
             */
            if (maxLength < 1 || _currToken == END_ELEMENT) {
                break;
            }
            // Otherwise need to advance to the next event
            while (true) {
                type = next();
                if (type == COMMENT || type == PROCESSING_INSTRUCTION || type == SPACE) { // space is ignorable too
                    continue;
                }
                if (type == END_ELEMENT) {
                    /* Just need to verify we don't have partial stuff
                     * (missing one to three characters of a full quartet
                     * that encodes 1 - 3 bytes). Also: non-padding
                     * variants can be in incomplete state, from which
                     * data may need to be flushed...
                     */
                    int left = dec.endOfContent();
                    if (left < 0) { // incomplete, error
                        throw _constructTypeException("Incomplete base64 triplet at the end of decoded content", "");
                    } else if (left > 0) { // 1 or 2 more bytes of data, loop some more
                        continue main_loop;
                    }
                    // Otherwise, no more data, we are done
                    break main_loop;
                }
                _scanner.resetForDecoding(v, dec, false); // false -> not first segment
                break;
            }
        }

        // If nothing was found, needs to be indicated via -1, not 0
        return (totalCount > 0) ? totalCount : -1;
    }

    /*
    /**********************************************************************
    /* TypedXMLStreamReader2 implementation, scalar attributes
    /**********************************************************************
     */

    @Override
    public final int getAttributeIndex(String namespaceURI, String localName) {
        if (_currToken != START_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_STELEM);
        }
        // Note: this method does not check for START_ELEMENT
        return findAttributeIndex(namespaceURI, localName);
    }

    @Override
    public final boolean getAttributeAsBoolean(int index) throws XMLStreamException {
        ValueDecoderFactory.BooleanDecoder dec = _decoderFactory().getBooleanDecoder();
        getAttributeAs(index, dec);
        return dec.getValue();
    }

    @Override
    public final int getAttributeAsInt(int index) throws XMLStreamException {
        ValueDecoderFactory.IntDecoder dec = _decoderFactory().getIntDecoder();
        getAttributeAs(index, dec);
        return dec.getValue();
    }

    @Override
    public final long getAttributeAsLong(int index) throws XMLStreamException {
        ValueDecoderFactory.LongDecoder dec = _decoderFactory().getLongDecoder();
        getAttributeAs(index, dec);
        return dec.getValue();
    }

    @Override
    public final float getAttributeAsFloat(int index) throws XMLStreamException {
        ValueDecoderFactory.FloatDecoder dec = _decoderFactory().getFloatDecoder();
        getAttributeAs(index, dec);
        return dec.getValue();
    }

    @Override
    public final double getAttributeAsDouble(int index) throws XMLStreamException {
        ValueDecoderFactory.DoubleDecoder dec = _decoderFactory().getDoubleDecoder();
        getAttributeAs(index, dec);
        return dec.getValue();
    }

    @Override
    public final BigInteger getAttributeAsInteger(int index) throws XMLStreamException {
        ValueDecoderFactory.IntegerDecoder dec = _decoderFactory().getIntegerDecoder();
        getAttributeAs(index, dec);
        return dec.getValue();
    }

    @Override
    public final BigDecimal getAttributeAsDecimal(int index) throws XMLStreamException {
        ValueDecoderFactory.DecimalDecoder dec = _decoderFactory().getDecimalDecoder();
        getAttributeAs(index, dec);
        return dec.getValue();
    }

    @Override
    public final QName getAttributeAsQName(int index) throws XMLStreamException {
        ValueDecoderFactory.QNameDecoder dec = _decoderFactory().getQNameDecoder(getNamespaceContext());
        getAttributeAs(index, dec);
        return verifyQName(dec.getValue());
    }

    @Override
    public final void getAttributeAs(int index, TypedValueDecoder tvd) throws XMLStreamException {
        if (_currToken != START_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_STELEM);
        }
        try {
            _scanner.decodeAttrValue(index, tvd);
        } catch (IllegalArgumentException iae) {
            throw _constructTypeException(iae, getAttributeValue(index));
        }
    }

    @Override
    public final int[] getAttributeAsIntArray(int index) throws XMLStreamException {
        ValueDecoderFactory.IntArrayDecoder dec = _decoderFactory().getIntArrayDecoder();
        getAttributeAsArray(index, dec);
        return dec.getValues();
    }

    @Override
    public final long[] getAttributeAsLongArray(int index) throws XMLStreamException {
        ValueDecoderFactory.LongArrayDecoder dec = _decoderFactory().getLongArrayDecoder();
        getAttributeAsArray(index, dec);
        return dec.getValues();
    }

    @Override
    public final float[] getAttributeAsFloatArray(int index) throws XMLStreamException {
        ValueDecoderFactory.FloatArrayDecoder dec = _decoderFactory().getFloatArrayDecoder();
        getAttributeAsArray(index, dec);
        return dec.getValues();
    }

    @Override
    public final double[] getAttributeAsDoubleArray(int index) throws XMLStreamException {
        ValueDecoderFactory.DoubleArrayDecoder dec = _decoderFactory().getDoubleArrayDecoder();
        getAttributeAsArray(index, dec);
        return dec.getValues();
    }

    /**
     * Method that allows reading contents of an attribute as an array
     * of whitespace-separate tokens, decoded using specified decoder.
     *
     * @return Number of tokens decoded, 0 if none found
     */
    @Override
    public final int getAttributeAsArray(int index, TypedArrayDecoder tad) throws XMLStreamException {
        if (_currToken != START_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_STELEM);
        }
        return _scanner.decodeAttrValues(index, tad);
    }

    @Override
    public final byte[] getAttributeAsBinary(int index) throws XMLStreamException {
        return getAttributeAsBinary(index, Base64Variants.getDefaultVariant());
    }

    @Override
    public final byte[] getAttributeAsBinary(int index, Base64Variant v) throws XMLStreamException {
        if (_currToken != START_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_STELEM);
        }
        return _scanner.decodeAttrBinaryValue(index, v, _base64Decoder());
    }

    protected QName verifyQName(QName n) throws TypedXMLStreamException {
        String ln = n.getLocalPart();
        int ix = XmlNames.findIllegalNameChar(ln, false);
        if (ix >= 0) {
            String prefix = n.getPrefix();
            String pname = (prefix != null && prefix.length() > 0) ? (prefix + ":" + ln) : ln;
            throw _constructTypeException("Invalid local name \"" + ln + "\" (character at #" + ix + " is invalid)",
                pname);
        }
        return n;
    }

    /*
    /**********************************************************************
    /* XMLStreamReader2 (Stax2) implementation
    /**********************************************************************
     */

    // // // StAX2, per-reader configuration

    @Deprecated // in base class
    @Override
    public final Object getFeature(String name) {
        // !!! TBI
        return null;
    }

    @Deprecated // in base class
    @Override
    public final void setFeature(String name, Object value) {
        // !!! TBI
    }

    // NOTE: getProperty() defined in Stax 1.0 interface

    @Override
    public final boolean isPropertySupported(String name) {
        // !!! TBI: not all these properties are really supported
        return _scanner.getConfig().isPropertySupported(name);
    }

    /**
     * @param name Name of the property to set
     * @param value Value to set property to.
     *
     * @return True, if the specified property was <b>succesfully</b>
     *    set to specified value; false if its value was not changed
     */
    @Override
    public final boolean setProperty(String name, Object value) {
        /* Note: can not call local method, since it'll return false for
         * recognized but non-mutable properties
         */
        return _scanner.getConfig().setProperty(name, value);
    }

    // // // StAX2, additional traversal methods

    @Override
    public final void skipElement() throws XMLStreamException {
        if (_currToken != START_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_STELEM);
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
    public final AttributeInfo getAttributeInfo() throws XMLStreamException {
        if (_currToken != START_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_STELEM);
        }
        return this;
    }

    // // // StAX2, Additional DTD access

    /**
     * Since this class implements {@link DTDInfo}, method can just
     * return <code>this</code>.
     */
    @Override
    public final DTDInfo getDTDInfo() throws XMLStreamException {
        /* Let's not allow it to be accessed during other events -- that
         * way callers won't count on it being available afterwards.
         */
        if (_currToken != DTD) {
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

    /**
     * Method similar to {@link #getText()}, except
     * that it just uses provided Writer to write all textual content.
     * For further optimization, it may also be allowed to do true
     * pass-through, thus possibly avoiding one temporary copy of the
     * data.
     *<p>
     * TODO: try to optimize to allow completely streaming pass-through:
     * currently will still read all data in memory buffers before
     * outputting
     * 
     * @param w Writer to use for writing textual contents
     * @param preserveContents If true, reader has to preserve contents
     *   so that further calls to <code>getText</code> will return
     *   proper conntets. If false, reader is allowed to skip creation
     *   of such copies: this can improve performance, but it also means
     *   that further calls to <code>getText</code> is not guaranteed to
     *   return meaningful data.
     *
     * @return Number of characters written to the reader
     */
    @Override
    public final int getText(Writer w, boolean preserveContents) throws XMLStreamException {
        if (((1 << _currToken) & MASK_GET_TEXT_WITH_WRITER) == 0) {
            throwNotTextual(_currToken);
        }
        return _scanner.getText(w, preserveContents);
    }

    // // // StAX 2, Other accessors

    /**
     * @return Number of open elements in the stack; 0 when parser is in
     *  prolog/epilog, 1 inside root element and so on.
     */
    @Override
    public final int getDepth() {
        /* 20-Mar-2008, tatus: Need to modify scanner's value since
         *   it decrements depth early for END_ELEMENT
         */
        int d = _scanner.getDepth();
        if (_currToken == END_ELEMENT) {
            ++d; // to compensate for too early decrement
        }
        return d;
    }

    /**
     * @return True, if cursor points to a start or end element that is
     *    constructed from 'empty' element (ends with '/&gt;');
     *    false otherwise.
     */
    @Override
    public final boolean isEmptyElement() throws XMLStreamException {
        return (_currToken == START_ELEMENT) ? _scanner.isEmptyTag() : false;
    }

    @Override
    public final NamespaceContext getNonTransientNamespaceContext() {
        return _scanner.getNonTransientNamespaceContext();
    }

    @Override
    public final String getPrefixedName() {
        switch (_currToken) {
            case START_ELEMENT:
            case END_ELEMENT:
                return _currName.getPrefixedName();

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
    public final void closeCompletely() throws XMLStreamException {
        _closeScanner(true);
    }

    /*
    /**********************************************************************
    /* DTDInfo implementation (StAX 2)
    /**********************************************************************
     */

    /**
     *<p>
     * Note: DTD-handling sub-classes need to override this method.
     */
    @Override
    public final Object getProcessedDTD() {
        // !!! TBI
        return null;
    }

    @Override
    public final String getDTDRootName() {
        if (_currToken != DTD) {
            return null;
        }
        return (_currName == null) ? null : _currName.getPrefixedName();
    }

    @Override
    public final String getDTDPublicId() {
        return _scanner.getDTDPublicId();
    }

    @Override
    public final String getDTDSystemId() {
        return _scanner.getDTDSystemId();
    }

    /**
     * @return Internal subset portion of the DOCTYPE declaration, if any;
     *   empty String if none
     */
    @Override
    public final String getDTDInternalSubset() {
        if (_currToken != DTD) {
            return null;
        }
        try {
            return _scanner.getText();
        } catch (XMLStreamException sex) {
            throw UncheckedStreamException.createFrom(sex);
        }
    }

    // // StAX2, v2.0

    /**
     * Sub-class will override this method
     */
    @Override
    public final DTDValidationSchema getProcessedDTDSchema() {
        // !!! TBI
        return null;
    }

    /*
    /**********************************************************************
    /* LocationInfo implementation (StAX 2)
    /**********************************************************************
     */

    // // // First, the "raw" offset accessors:

    @Override
    public final long getStartingByteOffset() {
        return _scanner.getStartingByteOffset();
    }

    @Override
    public final long getStartingCharOffset() {
        return _scanner.getStartingCharOffset();
    }

    @Override
    public final long getEndingByteOffset() throws XMLStreamException {
        return _scanner.getEndingByteOffset();
    }

    @Override
    public final long getEndingCharOffset() throws XMLStreamException {
        return _scanner.getEndingCharOffset();
    }

    // // // and then the object-based access methods:

    @Override
    public final XMLStreamLocation2 getStartLocation() {
        return _scanner.getStartLocation();
    }

    @Override
    public final XMLStreamLocation2 getEndLocation() throws XMLStreamException {
        return _scanner.getEndLocation();
    }

    @Override
    public final XMLStreamLocation2 getCurrentLocation() {
        return _scanner.getCurrentLocation();
    }

    /*
    /**********************************************************************
    /* AttributeInfo implementation (StAX 2)
    /**********************************************************************
     */

    //public final int getAttributeCount();

    @Override
    public final int findAttributeIndex(String nsURI, String localName) {
        return _scanner.findAttrIndex(nsURI, localName);
    }

    @Override
    public final int getIdAttributeIndex() {
        // !!! TBI: Need dtd handling for it to work
        return -1;
    }

    @Override
    public final int getNotationAttributeIndex() {
        // !!! TBI: Need dtd handling for it to work
        return -1;
    }

    /*
    /**********************************************************************
    /* Stax2 validation
    /**********************************************************************
     */

    @Override
    public final XMLValidator validateAgainst(XMLValidationSchema schema) throws XMLStreamException {
        // !!! TBI
        return null;
    }

    @Override
    public final XMLValidator stopValidatingAgainst(XMLValidationSchema schema) throws XMLStreamException {
        // !!! TBI
        return null;
    }

    @Override
    public final XMLValidator stopValidatingAgainst(XMLValidator validator) throws XMLStreamException {
        // !!! TBI
        return null;
    }

    @Override
    public final ValidationProblemHandler setValidationProblemHandler(ValidationProblemHandler h) {
        // !!! TBI
        return null;
    }

    /*
    /**********************************************************************
    /* Internal methods, error reporting
    /**********************************************************************
     */

    /**
     * Helper method called when {@link #getElementText} (et al) method encounters
     * a token type it should not, during text coalescing
     */
    protected void _reportNonTextEvent(int type) throws XMLStreamException {
        throwWfe("Expected a text token, got " + ErrorConsts.tokenTypeDesc(type) + ".");
    }

    protected Location getLastCharLocation() {
        // !!! TBI
        return _scanner.getCurrentLocation();
    }

    protected int handlePrologEoi(boolean isProlog) throws XMLStreamException {
        // Either way, we can now close the reader
        close();

        // It's ok to get EOF from epilog but not from prolog
        if (isProlog) {
            throwUnexpectedEOI(ErrorConsts.SUFFIX_IN_PROLOG);
        }
        return END_DOCUMENT;
    }

    /**
     * Method called when hitting an end-of-input within tree, after
     * a valid token
     */
    protected void handleTreeEoi() throws XMLStreamException {
        _currToken = END_DOCUMENT;
        // !!! Should indicate open tree etc.
        throwUnexpectedEOI(ErrorConsts.SUFFIX_IN_TREE);
    }

    /**
     * Throws generic parse error with specified message and current parsing
     * location.
     */
    protected void throwWfe(String msg) throws XMLStreamException {
        throw new WFCException(msg, getLastCharLocation());
    }

    private void throwNotTextual(int type) {
        throw new IllegalStateException("Not a textual event (" + ErrorConsts.tokenTypeDesc(_currToken) + ")");
    }

    private void throwNotTextXxx(int type) {
        throw new IllegalStateException(
            "getTextXxx() methods can not be called on " + ErrorConsts.tokenTypeDesc(_currToken));
    }

    protected void throwFromIOE(IOException ioe) throws XMLStreamException {
        throw new IoStreamException(ioe);
    }

    protected void throwUnexpectedEOI(String msg) throws XMLStreamException {
        throwWfe("Unexpected End-of-input" + msg);
    }

    protected XMLStreamException _constructUnexpectedInTyped(int nextToken) {
        if (nextToken == START_ELEMENT) {
            return _constructTypeException(
                "Element content can not contain child START_ELEMENT when using Typed Access methods", null);
        }
        return _constructTypeException("Expected a text token, got " + ErrorConsts.tokenTypeDesc(nextToken), null);
    }

    /**
     * Method called to wrap or convert given conversion-fail exception
     * into a full {@link TypedXMLStreamException}.
     *
     * @param iae Problem as reported by converter
     * @param lexicalValue Lexical value (element content, attribute value)
     *    that could not be converted succesfully.
     */
    private TypedXMLStreamException _constructTypeException(IllegalArgumentException iae, String lexicalValue) {
        return new TypedXMLStreamException(lexicalValue, iae.getMessage(), getStartLocation(), iae);
    }

    private TypedXMLStreamException _constructTypeException(String msg, String lexicalValue) {
        return new TypedXMLStreamException(lexicalValue, msg, getStartLocation());
    }

    protected void reportInvalidAttrIndex(int index) {
        /* 24-Jun-2006, tatus: Stax API doesn't specify what (if anything)
         *   should be thrown. Although RI throws IndexOutOfBounds
         *   let's throw IllegalArgumentException: that's what StaxTest
         *   assumes.
         */
        throw new IllegalArgumentException(
            "Illegal attribute index, " + index + ", current START_ELEMENT has " + _attrCount + " attributes");
    }

    /*
    /**********************************************************************
    /* Internal methods, input source handling
    /**********************************************************************
     */

    /**
     * Method called to close scanner, by asking it to release resource
     * it has, and potentially also close the underlying stream.
     */
    protected void _closeScanner(boolean forceStreamClose) throws XMLStreamException {
        if (_parseState != STATE_CLOSED) {
            _parseState = STATE_CLOSED;
            if (_currToken != END_DOCUMENT) {
                _currToken = END_DOCUMENT;
            }
        }
        _scanner.close(forceStreamClose);
    }

    /*
    /**********************************************************************
    /* Internal methods, other
    /**********************************************************************
     */

    protected final ValueDecoderFactory _decoderFactory() {
        if (_decoderFactory == null) {
            _decoderFactory = new ValueDecoderFactory();
        }
        return _decoderFactory;
    }

    protected CharArrayBase64Decoder _base64Decoder() {
        if (_base64Decoder == null) {
            _base64Decoder = new CharArrayBase64Decoder();
        }
        return _base64Decoder;
    }

    /**
     * Method called to handle value that has empty String
     * as representation. This will usually either lead to an
     * exception, or parsing to the default value for the
     * type in question (null for nullable types and so on).
     */
    private void _handleEmptyValue(TypedValueDecoder dec) throws XMLStreamException {
        try { // default action is to throw an exception
            dec.handleEmptyValue();
        } catch (IllegalArgumentException iae) {
            throw _constructTypeException(iae, "");
        }
    }

    /*
    /**********************************************************************
    /* Debugging help
    /**********************************************************************
     */

    @Override
    public final String toString() {
        return "[Aalto stream reader, scanner: " + _scanner + "]";
    }
}
