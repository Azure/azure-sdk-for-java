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

import com.azure.xml.implementation.aalto.UncheckedStreamException;
import com.azure.xml.implementation.aalto.impl.ErrorConsts;
import com.azure.xml.implementation.aalto.impl.StreamExceptionBase;
import com.azure.xml.implementation.aalto.in.InputBootstrapper;
import com.azure.xml.implementation.aalto.in.PName;
import com.azure.xml.implementation.aalto.in.ReaderConfig;
import com.azure.xml.implementation.aalto.in.XmlScanner;
import com.azure.xml.implementation.aalto.util.TextAccumulator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Collections;
import java.util.Objects;

/**
 * Basic backend-independent {@link XMLStreamReader} implementation.
 * While the read implements Stax API, most of real work is delegated
 * to input (and thereby, encoding) specific backend implementations.
 */
public final class StreamReaderImpl implements XMLStreamReader {
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
    private final XmlScanner _scanner;

    // // // Config flags:

    private final boolean _cfgCoalesceText;

    /*
    /**********************************************************************
    /* Current state
    /**********************************************************************
     */

    private int _currToken;

    /**
     * Main parsing/tokenization state (STATE_xxx)
     */
    private int _parseState;

    /**
     * Prefixed name associated with the current event, if any.
     */
    private PName _currName;

    /**
     * If the current event is <code>START_ELEMENT</code>, number
     * of attributes the start element has. Otherwise undefined.
     * Updated by reader, to make index checks for other attribute
     * access methods simpler.
     */
    private int _attrCount;

    /*
    /**********************************************************************
    /* Collected other information
    /**********************************************************************
     */

    /**
     * Prefixed root-name DOCTYPE declaration gave us, if any (note: also
     * serves as a marker to know if we have seen DOCTYPE yet)
     */
    private PName _dtdRootName;

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
    public String getCharacterEncodingScheme() {
        return _scanner.getConfig().getXmlDeclEncoding();
    }

    /**
     * As per Stax (1.0) specs, needs to return whatever parser determined
     * the encoding was, if it was able to figure it out. If not (there are
     * cases where this can not be found; specifically when being passed a
     * {@link java.io.Reader}), it should return null.
     */
    @Override
    public String getEncoding() {
        return _scanner.getConfig().getActualEncoding();
    }

    @Override
    public String getVersion() {
        return _scanner.getConfig().getXmlDeclVersion();
    }

    @Override
    public boolean isStandalone() {
        return (_scanner.getConfig().getXmlDeclStandalone() == ReaderConfig.STANDALONE_YES);
    }

    @Override
    public boolean standaloneSet() {
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
    public int getAttributeCount() {
        if (_currToken != START_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_STELEM);
        }
        return _attrCount;
    }

    @Override
    public String getAttributeLocalName(int index) {
        if (_currToken != START_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_STELEM);
        }
        if (index >= _attrCount || index < 0) {
            reportInvalidAttrIndex(index);
        }
        return _scanner.getAttrLocalName(index);
    }

    @Override
    public QName getAttributeName(int index) {
        if (_currToken != START_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_STELEM);
        }
        if (index >= _attrCount || index < 0) {
            reportInvalidAttrIndex(index);
        }
        return _scanner.getAttrQName(index);
    }

    @Override
    public String getAttributeNamespace(int index) {
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
    public String getAttributePrefix(int index) {
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
    public String getAttributeType(int index) {
        if (_currToken != START_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_STELEM);
        }
        if (index >= _attrCount || index < 0) {
            reportInvalidAttrIndex(index);
        }
        return "CDATA";
    }

    @Override
    public String getAttributeValue(int index) {
        if (_currToken != START_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_STELEM);
        }
        if (index >= _attrCount || index < 0) {
            reportInvalidAttrIndex(index);
        }
        return _scanner.getAttrValue(index);
    }

    @Override
    public String getAttributeValue(String nsURI, String localName) {
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
    public String getElementText() throws XMLStreamException {
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
    public int getEventType() {
        /* Only complication -- multi-part coalesced text is to be reported
         * as CHARACTERS always, never as CDATA (StAX specs).
         */
        if (_currToken == CDATA) {
            if (_cfgCoalesceText) {
                return CHARACTERS;
            }
        }
        return _currToken;
    }

    @Override
    public String getLocalName() {
        // Note: for this we need not (yet) finish reading element
        if (_currToken == START_ELEMENT || _currToken == END_ELEMENT || _currToken == ENTITY_REFERENCE) {
            return _currName.getLocalName();
        }
        throw new IllegalStateException("Current state not START_ELEMENT, END_ELEMENT or ENTITY_REFERENCE");
    }

    // // // getLocation() defined in StreamScanner

    @Override
    public QName getName() {
        if (_currToken != START_ELEMENT && _currToken != END_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_ELEM);
        }
        return _scanner.getQName();
    }

    // // // Namespace access

    @Override
    public NamespaceContext getNamespaceContext() {
        /* Unlike other getNamespaceXxx methods, this is available
         * for all events.
         * Note that the context is "live", ie. remains active (but not
         * static) even through calls to next(). StAX compliant apps
         * should not count on this behaviour, however.
         */
        return _scanner;
    }

    @Override
    public int getNamespaceCount() {
        if (_currToken != START_ELEMENT && _currToken != END_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_ELEM);
        }
        return _scanner.getNsCount();
    }

    @Override
    public String getNamespacePrefix(int index) {
        if (_currToken != START_ELEMENT && _currToken != END_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_ELEM);
        }
        String p = _scanner.getNamespacePrefix(index);
        return (p == null) ? "" : p;
    }

    @Override
    public String getNamespaceURI() {
        if (_currToken != START_ELEMENT && _currToken != END_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_ELEM);
        }
        String uri = _scanner.getNamespaceURI();
        return (uri == null) ? "" : uri;
    }

    @Override
    public String getNamespaceURI(int index) {
        if (_currToken != START_ELEMENT && _currToken != END_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_ELEM);
        }
        String uri = _scanner.getNamespaceURI(index);
        return (uri == null) ? "" : uri;
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if (_currToken != START_ELEMENT && _currToken != END_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_ELEM);
        }
        /* Note: null means 'no such prefix bound', whereas "" means
         * "no namespace" (only applicable for prefix "")
         */
        return _scanner.getNamespaceURI(prefix);
    }

    @Override
    public String getPIData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPITarget() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPrefix() {
        if (_currToken != START_ELEMENT && _currToken != END_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_ELEM);
        }
        // Note: need to return "" for "no prefix", not null, so:
        String p = _currName.getPrefix();
        return (p == null) ? "" : p;
    }

    @Override
    public String getText() {
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
    public char[] getTextCharacters() {
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
    public int getTextCharacters(int srcStart, char[] target, int targetStart, int len) {
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
    public int getTextLength() {
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
    public int getTextStart() {
        if (((1 << _currToken) & MASK_GET_TEXT_XXX) == 0) {
            throwNotTextXxx();
        }
        /* Scanner always stores text from the beginning of its
         * buffers...
         */
        return 0;
    }

    @Override
    public boolean hasName() {
        return (_currToken == START_ELEMENT) || (_currToken == END_ELEMENT);
    }

    @Override
    public boolean hasNext() {
        return (_currToken != END_DOCUMENT);
    }

    @Override
    public boolean hasText() {
        return (((1 << _currToken) & MASK_GET_TEXT) != 0);
    }

    @Override
    public boolean isAttributeSpecified(int index) {
        // No need to check for ATTRIBUTE since we never return that...
        if (_currToken != START_ELEMENT) {
            throw new IllegalStateException(ErrorConsts.ERR_STATE_NOT_STELEM);
        }
        return true;
    }

    @Override
    public boolean isCharacters() {
        return (getEventType() == CHARACTERS);
    }

    @Override
    public boolean isEndElement() {
        return (_currToken == END_ELEMENT);
    }

    @Override
    public boolean isStartElement() {
        return (_currToken == START_ELEMENT);
    }

    @Override
    public boolean isWhiteSpace() {
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
    public void require(int type, String nsUri, String localName) throws XMLStreamException {
        int curr = _currToken;

        /* There are some special cases; specifically, CDATA
         * is sometimes reported as CHARACTERS. Let's be lenient by
         * allowing both 'real' and reported types, for now.
         */
        if (curr != type) {
            if (curr == CDATA) {
                if (_cfgCoalesceText) {
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
            if (!Objects.equals(n, localName)) {
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
    public int next() throws XMLStreamException {
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
                if (_cfgCoalesceText) {
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
    public int nextTag() throws XMLStreamException {
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
    public void close() throws XMLStreamException {
        _closeScanner();
    }

    @Override
    public Location getLocation() {
        return _scanner.getStartLocation();
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
    private void _reportNonTextEvent(int type) throws XMLStreamException {
        throwWfe("Expected a text token, got " + ErrorConsts.tokenTypeDesc(type) + ".");
    }

    private Location getLastCharLocation() {
        // !!! TBI
        return _scanner.getCurrentLocation();
    }

    private int handlePrologEoi(boolean isProlog) throws XMLStreamException {
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
    private void handleTreeEoi() throws XMLStreamException {
        _currToken = END_DOCUMENT;
        // !!! Should indicate open tree etc.
        throwUnexpectedEOI(ErrorConsts.SUFFIX_IN_TREE);
    }

    /**
     * Throws generic parse error with specified message and current parsing
     * location.
     */
    private void throwWfe(String msg) throws XMLStreamException {
        throw new StreamExceptionBase(msg, getLastCharLocation());
    }

    private void throwNotTextual() {
        throw new IllegalStateException("Not a textual event (" + ErrorConsts.tokenTypeDesc(_currToken) + ")");
    }

    private void throwNotTextXxx() {
        throw new IllegalStateException(
            "getTextXxx() methods can not be called on " + ErrorConsts.tokenTypeDesc(_currToken));
    }

    private void throwUnexpectedEOI(String msg) throws XMLStreamException {
        throwWfe("Unexpected End-of-input" + msg);
    }

    private void reportInvalidAttrIndex(int index) {
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
    private void _closeScanner() throws XMLStreamException {
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
    public String toString() {
        return "[Aalto stream reader, scanner: " + _scanner + "]";
    }
}
