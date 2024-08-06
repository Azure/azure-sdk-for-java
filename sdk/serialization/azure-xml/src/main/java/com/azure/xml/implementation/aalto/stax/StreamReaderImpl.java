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

import java.util.Collections;
import java.util.Objects;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.azure.xml.implementation.stax2.*;

import com.azure.xml.implementation.aalto.UncheckedStreamException;
import com.azure.xml.implementation.aalto.WFCException;
import com.azure.xml.implementation.aalto.impl.ErrorConsts;
import com.azure.xml.implementation.aalto.in.InputBootstrapper;
import com.azure.xml.implementation.aalto.in.PName;
import com.azure.xml.implementation.aalto.in.ReaderConfig;
import com.azure.xml.implementation.aalto.in.XmlScanner;
import com.azure.xml.implementation.aalto.util.TextAccumulator;

/**
 * Basic backend-independent {@link XMLStreamReader} implementation.
 * While the read implements Stax API, most of real work is delegated
 * to input (and thereby, encoding) specific backend implementations.
 */
public class StreamReaderImpl implements XMLStreamReader, LocationInfo {
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

    final private static int MASK_GET_ELEMENT_TEXT
        = (1 << CHARACTERS) | (1 << CDATA) | (1 << SPACE) | (1 << ENTITY_REFERENCE);

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
        return _scanner.getAttrType();
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
            throwNotTextual();
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
            throwNotTextXxx();
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
            throwNotTextXxx();
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
            throwNotTextXxx();
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
            throwNotTextXxx();
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
        return _scanner.isAttrSpecified();
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
            if (!Objects.equals(n, localName) && !n.equals(localName)) {
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
            if (nsUri.isEmpty()) {
                if (!uri.isEmpty()) {
                    throwWfe("Expected empty namespace, instead have '" + uri + "'.");
                }
            } else {
                if (!nsUri.equals(uri)) {
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
        _closeScanner();
    }

    @Override
    public final Location getLocation() {
        return getStartLocation();
    }

    /*
    /**********************************************************************
    /* XMLStreamReader2 (Stax2) implementation
    /**********************************************************************
     */

    // // // StAX2, additional attribute access

    // // // StAX2, Additional DTD access

    // // // StAX2, Additional location information

    // // // StAX2, Pass-through text accessors

    // // // StAX 2, Other accessors

    /*
    /**********************************************************************
    /* DTDInfo implementation (StAX 2)
    /**********************************************************************
     */

    // // StAX2, v2.0

    /*
    /**********************************************************************
    /* LocationInfo implementation (StAX 2)
    /**********************************************************************
     */

    // // // First, the "raw" offset accessors:

    // // // and then the object-based access methods:

    @Override
    public final Location getStartLocation() {
        return _scanner.getStartLocation();
    }

    /*
    /**********************************************************************
    /* AttributeInfo implementation (StAX 2)
    /**********************************************************************
     */

    //public final int getAttributeCount();

    /*
    /**********************************************************************
    /* Stax2 validation
    /**********************************************************************
     */

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

    private void throwNotTextual() {
        throw new IllegalStateException("Not a textual event (" + ErrorConsts.tokenTypeDesc(_currToken) + ")");
    }

    private void throwNotTextXxx() {
        throw new IllegalStateException(
            "getTextXxx() methods can not be called on " + ErrorConsts.tokenTypeDesc(_currToken));
    }

    protected void throwUnexpectedEOI(String msg) throws XMLStreamException {
        throwWfe("Unexpected End-of-input" + msg);
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
    protected void _closeScanner() throws XMLStreamException {
        if (_parseState != STATE_CLOSED) {
            _parseState = STATE_CLOSED;
            if (_currToken != END_DOCUMENT) {
                _currToken = END_DOCUMENT;
            }
        }
        _scanner.close(false);
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
