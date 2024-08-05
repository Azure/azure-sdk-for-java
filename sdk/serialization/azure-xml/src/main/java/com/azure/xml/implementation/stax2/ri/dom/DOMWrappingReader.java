// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
/* Stax2 API extension for Streaming API for XML processing (StAX).
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

import com.azure.xml.implementation.stax2.AttributeInfo;
import com.azure.xml.implementation.stax2.DTDInfo;
import com.azure.xml.implementation.stax2.LocationInfo;
import com.azure.xml.implementation.stax2.XMLStreamLocation2;
import com.azure.xml.implementation.stax2.XMLStreamReader2;
import com.azure.xml.implementation.stax2.ri.EmptyNamespaceContext;
import com.azure.xml.implementation.stax2.ri.Stax2Util;
import com.azure.xml.implementation.stax2.ri.typed.StringBase64Decoder;
import com.azure.xml.implementation.stax2.ri.typed.ValueDecoderFactory;
import com.azure.xml.implementation.stax2.typed.Base64Variant;
import com.azure.xml.implementation.stax2.typed.Base64Variants;
import com.azure.xml.implementation.stax2.typed.TypedArrayDecoder;
import com.azure.xml.implementation.stax2.typed.TypedValueDecoder;
import com.azure.xml.implementation.stax2.typed.TypedXMLStreamException;
import com.azure.xml.implementation.stax2.validation.ValidationProblemHandler;
import com.azure.xml.implementation.stax2.validation.XMLValidationSchema;
import com.azure.xml.implementation.stax2.validation.XMLValidator;
import org.w3c.dom.Attr;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * This is an adapter class that presents a DOM document as if it was
 * a regular {@link XMLStreamReader}. This is mostly useful for
 * inter-operability purposes, and should only be used when the
 * input has to come as a DOM object and the original xml content
 * is not available as a stream.
 *<p>
 * Note that the implementation is only to be used for use with
 * <code>javax.xml.transform.dom.DOMSource</code>. It can however be
 * used for both full documents, and single element root fragments,
 * depending on what node is passed as the argument.
 *<p>
 * Some notes regarding missing/incomplete functionality:
 * <ul>
 *  <li>DOM does not seem to have access to information from the XML
 *    declaration (although Document node can be viewed as representing
 *    it). Consequently, all accessors return no information (version,
 *    encoding, standalone).
 *   </li>
 *  <li>No location info is provided, since (you guessed it!) DOM
 *    does not provide that info.
 *   </li>
 *  </ul>
 */
@SuppressWarnings({ "deprecation", "fallthrough" })
public abstract class DOMWrappingReader
    implements XMLStreamReader2, AttributeInfo, DTDInfo, LocationInfo, NamespaceContext, XMLStreamConstants {
    protected final static int INT_SPACE = 0x0020;

    // // // Bit masks used for quick type comparisons

    final private static int MASK_GET_TEXT
        = (1 << CHARACTERS) | (1 << CDATA) | (1 << SPACE) | (1 << COMMENT) | (1 << DTD) | (1 << ENTITY_REFERENCE);

    final private static int MASK_GET_TEXT_XXX = (1 << CHARACTERS) | (1 << CDATA) | (1 << SPACE) | (1 << COMMENT);

    final private static int MASK_GET_ELEMENT_TEXT
        = (1 << CHARACTERS) | (1 << CDATA) | (1 << SPACE) | (1 << ENTITY_REFERENCE);

    final protected static int MASK_TYPED_ACCESS_BINARY = (1 << START_ELEMENT) //  note: END_ELEMENT handled separately
        | (1 << CHARACTERS) | (1 << CDATA) | (1 << SPACE);

    // // // Enumerated error case ids

    /**
     * Current state not START_ELEMENT, should be
     */
    protected final static int ERR_STATE_NOT_START_ELEM = 1;

    /**
     * Current state not START_ELEMENT or END_ELEMENT, should be
     */
    protected final static int ERR_STATE_NOT_ELEM = 2;

    /**
     * Current state not PROCESSING_INSTRUCTION
     */
    protected final static int ERR_STATE_NOT_PI = 3;

    /**
     * Current state not one where getText() can be used
     */
    protected final static int ERR_STATE_NOT_TEXTUAL = 4;

    /**
     * Current state not one where getTextXxx() can be used
     */
    protected final static int ERR_STATE_NOT_TEXTUAL_XXX = 5;

    protected final static int ERR_STATE_NOT_TEXTUAL_OR_ELEM = 6;

    protected final static int ERR_STATE_NO_LOCALNAME = 7;

    // // // Configuration:

    protected final String _systemId;

    protected final Node _rootNode;

    /**
     * Whether stream reader is to be namespace aware (as per property
     * {@link XMLInputFactory#IS_NAMESPACE_AWARE}) or not
     */
    protected final boolean _cfgNsAware;

    /**
     * Whether stream reader is to coalesce adjacent textual
     * (CHARACTERS, SPACE, CDATA) events (as per property
     * {@link XMLInputFactory#IS_COALESCING}) or not
     */
    protected final boolean _coalescing;

    /**
     * By default we do not force interning of names: can be
     * reset by sub-classes.
     */
    protected boolean _cfgInternNames = false;

    /**
     * By default we do not force interning of namespace URIs: can be
     * reset by sub-classes.
     */
    protected boolean _cfgInternNsURIs = false;

    // // // State:

    protected int _currEvent = START_DOCUMENT;

    /**
     * Current node is the DOM node that contains information
     * regarding the current event.
     */
    protected Node _currNode;

    protected int _depth = 0;

    /**
     * In coalescing mode, we may need to combine textual content
     * from multiple adjacent nodes. Since we shouldn't be modifying
     * the underlying DOM tree, need to accumulate it into a temporary
     * variable
     */
    protected String _coalescedText;

    /**
     * Helper object used for combining segments of text as needed
     */
    protected Stax2Util.TextBuffer _textBuffer = new Stax2Util.TextBuffer();

    // // // Attribute/namespace declaration state

    /* DOM, alas, does not distinguish between namespace declarations
     * and attributes (due to its roots prior to XML namespaces?).
     * Because of this, two lists need to be separated. Since this
     * information is often not needed, it will be lazily generated.
     */

    /**
     * Lazily instantiated List of all actual attributes for the
     * current (start) element, NOT including namespace declarations.
     * As such, elements are {@link org.w3c.dom.Attr} instances.
     *<p>
     */
    protected List<Node> _attrList = null;

    /**
     * Lazily instantiated String pairs of all namespace declarations for the
     * current (start/end) element. String pair means that for each
     * declarations there are two Strings in the list: first one is prefix
     * (empty String for the default namespace declaration), and second
     * URI it is bound to.
     */
    protected List<String> _nsDeclList = null;

    /**
     * Factory used for constructing decoders we need for typed access
     */
    protected ValueDecoderFactory _decoderFactory;

    /**
     * Lazily-constructed decoder object for decoding base64 encoded
     * binary content.
     */
    protected StringBase64Decoder _base64Decoder = null;

    /*
    /**********************************************************************
    /* Construction, configuration
    /**********************************************************************
     */

    /**
     * @param src Node that is the tree of the DOM document, or fragment.
     * @param nsAware Whether resulting reader should operate in namespace
     *   aware mode or not. Note that this should be compatible with
     *   settings for the DOM builder that produced DOM tree or fragment
     *   being operated on, otherwise results are not defined.
     * @param coalescing Whether resulting reader should coalesce adjacent
     *    text events or not
     */
    protected DOMWrappingReader(DOMSource src, boolean nsAware, boolean coalescing) throws XMLStreamException {
        Node treeRoot = src.getNode();
        if (treeRoot == null) {
            throw new IllegalArgumentException("Can not pass null Node for constructing a DOM-based XMLStreamReader");
        }
        _cfgNsAware = nsAware;
        _coalescing = coalescing;
        _systemId = src.getSystemId();

        /* Ok; we need a document node; or an element node; or a document
         * fragment node.
         */
        switch (treeRoot.getNodeType()) {
            case Node.DOCUMENT_NODE: // fine
                /* Should try to find encoding, version and stand-alone
                 * settings... but is there a standard way of doing that?
                 */
            case Node.ELEMENT_NODE: // can make sub-tree... ok
                // But should we skip START/END_DOCUMENT? For now, let's not

            case Node.DOCUMENT_FRAGMENT_NODE: // as with element...

                // Above types are fine
                break;

            default: // other Nodes not usable
                throw new XMLStreamException(
                    "Can not create an XMLStreamReader for a DOM node of type " + treeRoot.getClass());
        }
        _rootNode = _currNode = treeRoot;
    }

    protected void setInternNames(boolean state) {
        _cfgInternNames = state;
    }

    protected void setInternNsURIs(boolean state) {
        _cfgInternNsURIs = state;
    }

    /*
    /**********************************************************************
    /* Abstract methods for sub-classes to implement
    /**********************************************************************
     */

    protected abstract void throwStreamException(String msg, Location loc) throws XMLStreamException;

    /*
    /**********************************************************************
    /* XMLStreamReader, document info
    /**********************************************************************
     */

    /**
     * As per Stax (1.0) specs, needs to return whatever xml declaration
     * claimed encoding is, if any; or null if no xml declaration found.
     */
    @Override
    public String getCharacterEncodingScheme() {
        /* No standard way to figure it out from a DOM Document node;
         * have to return null
         */
        return null;
    }

    /**
     * As per Stax (1.0) specs, needs to return whatever parser determined
     * the encoding was, if it was able to figure it out. If not (there are
     * cases where this can not be found; specifically when being passed a
     * {@link java.io.Reader}), it should return null.
     */
    @Override
    public String getEncoding() {
        /* We have no information regarding underlying stream/Reader, so
         * best we can do is to see if we know xml declaration encoding.
         */
        return getCharacterEncodingScheme();
    }

    @Override
    public String getVersion() {
        /* No standard way to figure it out from a DOM Document node;
         * have to return null
         */
        return null;
    }

    @Override
    public boolean isStandalone() {
        // No standard way to figure it out from a DOM Document node;
        // have to return false
        return false;
    }

    @Override
    public boolean standaloneSet() {
        // No standard way to figure it out from a DOM Document node;
        // have to return false
        return false;
    }

    /*
    /**********************************************************************
    /* Public API, configuration
    /**********************************************************************
     */

    @Override
    public abstract Object getProperty(String name);

    // NOTE: getProperty() defined in Stax 1.0 interface

    @Override
    public abstract boolean isPropertySupported(String name);

    /**
     * @param name Name of the property to set
     * @param value Value to set property to.
     *
     * @return True, if the specified property was <b>succesfully</b>
     *    set to specified value; false if its value was not changed
     */
    @Override
    public abstract boolean setProperty(String name, Object value);

    /*
    /**********************************************************************
    /* XMLStreamReader, current state
    /**********************************************************************
     */

    // // // Attribute access:

    @Override
    public int getAttributeCount() {
        if (_currEvent != START_ELEMENT) {
            reportWrongState(ERR_STATE_NOT_START_ELEM);
        }
        if (_attrList == null) {
            _calcNsAndAttrLists(true);
        }
        return _attrList.size();
    }

    @Override
    public String getAttributeLocalName(int index) {
        if (_currEvent != START_ELEMENT) {
            reportWrongState(ERR_STATE_NOT_START_ELEM);
        }
        if (_attrList == null) {
            _calcNsAndAttrLists(true);
        }
        if (index >= _attrList.size() || index < 0) {
            handleIllegalAttrIndex(index);
            return null;
        }
        Attr attr = (Attr) _attrList.get(index);
        return _internName(_safeGetLocalName(attr));
    }

    @Override
    public QName getAttributeName(int index) {
        if (_currEvent != START_ELEMENT) {
            reportWrongState(ERR_STATE_NOT_START_ELEM);
        }
        if (_attrList == null) {
            _calcNsAndAttrLists(true);
        }
        if (index >= _attrList.size() || index < 0) {
            handleIllegalAttrIndex(index);
            return null;
        }
        Attr attr = (Attr) _attrList.get(index);
        return _constructQName(attr.getNamespaceURI(), _safeGetLocalName(attr), attr.getPrefix());
    }

    @Override
    public String getAttributeNamespace(int index) {
        if (_currEvent != START_ELEMENT) {
            reportWrongState(ERR_STATE_NOT_START_ELEM);
        }
        if (_attrList == null) {
            _calcNsAndAttrLists(true);
        }
        if (index >= _attrList.size() || index < 0) {
            handleIllegalAttrIndex(index);
            return null;
        }
        Attr attr = (Attr) _attrList.get(index);
        return _internNsURI(attr.getNamespaceURI());
    }

    @Override
    public String getAttributePrefix(int index) {
        if (_currEvent != START_ELEMENT) {
            reportWrongState(ERR_STATE_NOT_START_ELEM);
        }
        if (_attrList == null) {
            _calcNsAndAttrLists(true);
        }
        if (index >= _attrList.size() || index < 0) {
            handleIllegalAttrIndex(index);
            return null;
        }
        Attr attr = (Attr) _attrList.get(index);
        return _internName(attr.getPrefix());
    }

    @Override
    public String getAttributeType(int index) {
        if (_currEvent != START_ELEMENT) {
            reportWrongState(ERR_STATE_NOT_START_ELEM);
        }
        if (_attrList == null) {
            _calcNsAndAttrLists(true);
        }
        if (index >= _attrList.size() || index < 0) {
            handleIllegalAttrIndex(index);
            return null;
        }
        //Attr attr = (Attr) _attrList.get(index);
        // First, a special case, ID... since it's potentially most useful
        /* 26-Apr-2006, TSa: Turns out that following methods are
         *    DOM Level3, and as such not available in JDK 1.4 and prior.
         *    Thus, let's not yet use them (could use dynamic discovery
         *    for graceful downgrade)
         */
        /*
        if (attr.isId()) {
            return "ID";
        }
        TypeInfo schemaType = attr.getSchemaTypeInfo();
        return (schemaType == null) ? "CDATA" : schemaType.getTypeName();
        */
        return "CDATA";
    }

    @Override
    public String getAttributeValue(int index) {
        if (_currEvent != START_ELEMENT) {
            reportWrongState(ERR_STATE_NOT_START_ELEM);
        }
        if (_attrList == null) {
            _calcNsAndAttrLists(true);
        }
        if (index >= _attrList.size() || index < 0) {
            handleIllegalAttrIndex(index);
            return null;
        }
        Attr attr = (Attr) _attrList.get(index);
        return attr.getValue();
    }

    @Override
    public String getAttributeValue(String nsURI, String localName) {
        if (_currEvent != START_ELEMENT) {
            reportWrongState(ERR_STATE_NOT_START_ELEM);
        }
        Element elem = (Element) _currNode;
        NamedNodeMap attrs = elem.getAttributes();
        /* Hmmh. DOM javadocs claim "Per [XML Namespaces], applications
         * must use the value null as the namespaceURI parameter for methods
         * if they wish to have no namespace.".
         * Not sure how true that is, but:
         */
        if (nsURI != null && nsURI.isEmpty()) {
            nsURI = null;
        }
        Attr attr = (Attr) attrs.getNamedItemNS(nsURI, localName);
        return (attr == null) ? null : attr.getValue();
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
        if (_currEvent != START_ELEMENT) {
            /* Quite illogical: this is not an IllegalStateException
             * like other similar ones, but rather an XMLStreamException.
             * But that's how Stax JavaDocs outline how it should be.
             */
            reportParseProblem(ERR_STATE_NOT_START_ELEM);
        }
        // As per [WSTX-244], handling of coalescing, regular differ a lot, so:
        if (_coalescing) {
            StringBuilder text = null;
            // Need to loop to get rid of PIs, comments
            while (true) {
                int type = next();
                if (type == END_ELEMENT) {
                    break;
                }
                if (type == COMMENT || type == PROCESSING_INSTRUCTION) {
                    continue;
                }
                if (((1 << type) & MASK_GET_ELEMENT_TEXT) == 0) {
                    reportParseProblem(ERR_STATE_NOT_TEXTUAL);
                }
                if (text == null) {
                    text = new StringBuilder(getText());
                } else { // uncommon but possible (with comments, PIs):
                    text.append(getText());
                }
            }
            return (text == null) ? "" : text.toString();
        }
        _textBuffer.reset();
        // Need to loop to get rid of PIs, comments
        while (true) {
            int type = next();
            if (type == END_ELEMENT) {
                break;
            }
            if (type == COMMENT || type == PROCESSING_INSTRUCTION) {
                continue;
            }
            if (((1 << type) & MASK_GET_ELEMENT_TEXT) == 0) {
                reportParseProblem(ERR_STATE_NOT_TEXTUAL);
            }
            _textBuffer.append(getText());
        }
        return _textBuffer.get();
    }

    /**
     * Returns type of the last event returned; or START_DOCUMENT before
     * any events has been explicitly returned.
     */
    @Override
    public int getEventType() {
        return _currEvent;
    }

    @Override
    public String getLocalName() {
        if (_currEvent == START_ELEMENT || _currEvent == END_ELEMENT) {
            return _internName(_safeGetLocalName(_currNode));
        }
        if (_currEvent != ENTITY_REFERENCE) {
            reportWrongState(ERR_STATE_NO_LOCALNAME);
        }
        return _internName(_currNode.getNodeName());
    }

    @Override
    public final Location getLocation() {
        return getStartLocation();
    }

    @Override
    public QName getName() {
        if (_currEvent != START_ELEMENT && _currEvent != END_ELEMENT) {
            reportWrongState(ERR_STATE_NOT_START_ELEM);
        }
        return _constructQName(_currNode.getNamespaceURI(), _safeGetLocalName(_currNode), _currNode.getPrefix());
    }

    // // // Namespace access

    @Override
    public NamespaceContext getNamespaceContext() {
        return this;
    }

    @Override
    public int getNamespaceCount() {
        if (_currEvent != START_ELEMENT && _currEvent != END_ELEMENT) {
            reportWrongState(ERR_STATE_NOT_ELEM);
        }
        if (_nsDeclList == null) {
            if (!_cfgNsAware) {
                return 0;
            }
            _calcNsAndAttrLists(_currEvent == START_ELEMENT);
        }
        return _nsDeclList.size() / 2;
    }

    /**
     * Alas, DOM does not expose any of information necessary for
     * determining actual declarations. Thus, have to indicate that
     * there are no declarations.
     */
    @Override
    public String getNamespacePrefix(int index) {
        if (_currEvent != START_ELEMENT && _currEvent != END_ELEMENT) {
            reportWrongState(ERR_STATE_NOT_ELEM);
        }
        if (_nsDeclList == null) {
            if (!_cfgNsAware) {
                handleIllegalNsIndex(index);
            }
            _calcNsAndAttrLists(_currEvent == START_ELEMENT);
        }
        if (index < 0 || (index + index) >= _nsDeclList.size()) {
            handleIllegalNsIndex(index);
        }
        // Note: _nsDeclList entries have been appropriately intern()ed if need be
        return _nsDeclList.get(index + index);
    }

    @Override
    public String getNamespaceURI() {
        if (_currEvent != START_ELEMENT && _currEvent != END_ELEMENT) {
            reportWrongState(ERR_STATE_NOT_ELEM);
        }
        return _internNsURI(_currNode.getNamespaceURI());
    }

    @Override
    public String getNamespaceURI(int index) {
        if (_currEvent != START_ELEMENT && _currEvent != END_ELEMENT) {
            reportWrongState(ERR_STATE_NOT_ELEM);
        }
        if (_nsDeclList == null) {
            if (!_cfgNsAware) {
                handleIllegalNsIndex(index);
            }
            _calcNsAndAttrLists(_currEvent == START_ELEMENT);
        }
        if (index < 0 || (index + index) >= _nsDeclList.size()) {
            handleIllegalNsIndex(index);
        }
        // Note: _nsDeclList entries have been appropriately intern()ed if need be
        return _nsDeclList.get(index + index + 1);
    }

    // Note: implemented as part of NamespaceContext
    //public String getNamespaceURI(String prefix)

    @Override
    public String getPIData() {
        if (_currEvent != PROCESSING_INSTRUCTION) {
            reportWrongState(ERR_STATE_NOT_PI);
        }
        return _currNode.getNodeValue();
    }

    @Override
    public String getPITarget() {
        if (_currEvent != PROCESSING_INSTRUCTION) {
            reportWrongState(ERR_STATE_NOT_PI);
        }
        return _internName(_currNode.getNodeName());
    }

    @Override
    public String getPrefix() {
        if (_currEvent != START_ELEMENT && _currEvent != END_ELEMENT) {
            reportWrongState(ERR_STATE_NOT_ELEM);
        }
        return _internName(_currNode.getPrefix());
    }

    @Override
    public String getText() {
        if (_coalescedText != null) {
            return _coalescedText;
        }
        if (((1 << _currEvent) & MASK_GET_TEXT) == 0) {
            reportWrongState(ERR_STATE_NOT_TEXTUAL);
        }
        return _currNode.getNodeValue();
    }

    @Override
    public char[] getTextCharacters() {
        String text = getText();
        return text.toCharArray();
    }

    @Override
    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int len) {
        if (((1 << _currEvent) & MASK_GET_TEXT_XXX) == 0) {
            reportWrongState(ERR_STATE_NOT_TEXTUAL_XXX);
        }
        String text = getText();
        if (len > text.length()) {
            len = text.length();
        }
        text.getChars(sourceStart, sourceStart + len, target, targetStart);
        return len;
    }

    @Override
    public int getTextLength() {
        if (((1 << _currEvent) & MASK_GET_TEXT_XXX) == 0) {
            reportWrongState(ERR_STATE_NOT_TEXTUAL_XXX);
        }
        return getText().length();
    }

    @Override
    public int getTextStart() {
        if (((1 << _currEvent) & MASK_GET_TEXT_XXX) == 0) {
            reportWrongState(ERR_STATE_NOT_TEXTUAL_XXX);
        }
        return 0;
    }

    @Override
    public boolean hasName() {
        return (_currEvent == START_ELEMENT) || (_currEvent == END_ELEMENT);
    }

    @Override
    public boolean hasNext() {
        return (_currEvent != END_DOCUMENT);
    }

    @Override
    public boolean hasText() {
        return (((1 << _currEvent) & MASK_GET_TEXT) != 0);
    }

    @Override
    public boolean isAttributeSpecified(int index) {
        if (_currEvent != START_ELEMENT) {
            reportWrongState(ERR_STATE_NOT_START_ELEM);
        }
        Element elem = (Element) _currNode;
        Attr attr = (Attr) elem.getAttributes().item(index);
        if (attr == null) {
            handleIllegalAttrIndex(index);
            return false;
        }
        return attr.getSpecified();
    }

    @Override
    public boolean isCharacters() {
        return (_currEvent == CHARACTERS);
    }

    @Override
    public boolean isEndElement() {
        return (_currEvent == END_ELEMENT);
    }

    @Override
    public boolean isStartElement() {
        return (_currEvent == START_ELEMENT);
    }

    @Override
    public boolean isWhiteSpace() {
        if (_currEvent == CHARACTERS || _currEvent == CDATA) {
            String text = getText();
            for (int i = 0, len = text.length(); i < len; ++i) {
                /* !!! If xml 1.1 was to be handled, should check for
                 *   LSEP and NEL too?
                 */
                if (text.charAt(i) > INT_SPACE) {
                    return false;
                }
            }
            return true;
        }
        return (_currEvent == SPACE);
    }

    @Override
    public void require(int type, String nsUri, String localName) throws XMLStreamException {
        int curr = _currEvent;

        /* There are some special cases; specifically, SPACE and CDATA
         * are sometimes reported as CHARACTERS. Let's be lenient by
         * allowing both 'real' and reported types, for now.
         */
        if (curr != type) {
            if (curr == CDATA) {
                curr = CHARACTERS;
            } else if (curr == SPACE) {
                curr = CHARACTERS;
            }
        }

        if (type != curr) {
            throwStreamException(
                "Required type " + Stax2Util.eventTypeDesc(type) + ", current type " + Stax2Util.eventTypeDesc(curr));
        }

        if (localName != null) {
            if (curr != START_ELEMENT && curr != END_ELEMENT && curr != ENTITY_REFERENCE) {
                throwStreamException(
                    "Required a non-null local name, but current token not a START_ELEMENT, END_ELEMENT or ENTITY_REFERENCE (was "
                        + Stax2Util.eventTypeDesc(_currEvent) + ")");
            }
            String n = getLocalName();
            if (!Objects.equals(n, localName)) {
                throwStreamException("Required local name '" + localName + "'; current local name '" + n + "'.");
            }
        }
        if (nsUri != null) {
            if (curr != START_ELEMENT && curr != END_ELEMENT) {
                throwStreamException(
                    "Required non-null NS URI, but current token not a START_ELEMENT or END_ELEMENT (was "
                        + Stax2Util.eventTypeDesc(curr) + ")");
            }

            String uri = getNamespaceURI();
            // No namespace?
            if (nsUri.isEmpty()) {
                if (uri != null && !uri.isEmpty()) {
                    throwStreamException("Required empty namespace, instead have '" + uri + "'.");
                }
            } else {
                if ((!nsUri.equals(uri))) {
                    throwStreamException("Required namespace '" + nsUri + "'; have '" + uri + "'.");
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
        _coalescedText = null;

        /* For most events, we just need to find the next sibling; and
         * that failing, close the parent element. But there are couple
         * of special cases, which are handled first:
         */
        switch (_currEvent) {

            case START_DOCUMENT: // initial state
                /* What to do here depends on what kind of node we started
                 * with...
                 */
                switch (_currNode.getNodeType()) {
                    case Node.DOCUMENT_NODE:
                    case Node.DOCUMENT_FRAGMENT_NODE:
                        // For doc, fragment, need to find first child
                        _currNode = _currNode.getFirstChild();
                        // as per [WSTX-259], need to handle degenerate case of empty fragment, too
                        if (_currNode == null) {
                            return (_currEvent = END_DOCUMENT);
                        }
                        break;

                    case Node.ELEMENT_NODE:
                        // For element, curr node is fine:
                        return (_currEvent = START_ELEMENT);

                    default:
                        throw new XMLStreamException("Internal error: unexpected DOM root node type "
                            + _currNode.getNodeType() + " for node '" + _currNode + "'");
                }
                break;

            case END_DOCUMENT: // end reached: should not call!
                throw new java.util.NoSuchElementException("Can not call next() after receiving END_DOCUMENT");

            case START_ELEMENT: // element returned, need to traverse children, if any
                ++_depth;
                _attrList = null; // so it will not get reused accidentally
            {
                Node firstChild = _currNode.getFirstChild();
                if (firstChild == null) { // empty? need to return virtual END_ELEMENT
                    /* Note: need not clear namespace declarations, because
                     * it'll be the same as for the start elem!
                     */
                    return (_currEvent = END_ELEMENT);
                }
                _nsDeclList = null;

                /* non-empty is easy: let's just swap curr node, and
                 * fall through to regular handling
                 */
                _currNode = firstChild;
                break;
            }

            case END_ELEMENT:

                --_depth;
                // Need to clear these lists
                _attrList = null;
                _nsDeclList = null;

                /* One special case: if we hit the end of children of
                 * the root element (when tree constructed with Element,
                 * instead of Document or DocumentFragment). If so, it'll
                 * be END_DOCUMENT:
                 */
                if (_currNode == _rootNode) {
                    return (_currEvent = END_DOCUMENT);
                }
                // Otherwise need to fall through to default handling:

            default:
            /* For anything else, we can and should just get the
             * following sibling.
             */
            {
                Node next = _currNode.getNextSibling();
                // If sibling, let's just assign and fall through
                if (next != null) {
                    _currNode = next;
                    break;
                }
                /* Otherwise, need to climb up _the stack and either
                 * return END_ELEMENT (if parent is element) or
                 * END_DOCUMENT (if not; needs to be root, then)
                 */
                _currNode = _currNode.getParentNode();
                int type = _currNode.getNodeType();
                if (type == Node.ELEMENT_NODE) {
                    return (_currEvent = END_ELEMENT);
                }
                // Let's do sanity check; should really be Doc/DocFragment
                if (_currNode != _rootNode || (type != Node.DOCUMENT_NODE && type != Node.DOCUMENT_FRAGMENT_NODE)) {
                    throw new XMLStreamException(
                        "Internal error: non-element parent node (" + type + ") that is not the initial root node");
                }
                return (_currEvent = END_DOCUMENT);
            }
        }

        // Ok, need to determine current node type:
        switch (_currNode.getNodeType()) {
            case Node.CDATA_SECTION_NODE:
                if (_coalescing) {
                    coalesceText();
                } else {
                    _currEvent = CDATA;
                }
                break;

            case Node.COMMENT_NODE:
                _currEvent = COMMENT;
                break;

            case Node.DOCUMENT_TYPE_NODE:
                _currEvent = DTD;
                break;

            case Node.ELEMENT_NODE:
                _currEvent = START_ELEMENT;
                break;

            case Node.ENTITY_REFERENCE_NODE:
                _currEvent = ENTITY_REFERENCE;
                break;

            case Node.PROCESSING_INSTRUCTION_NODE:
                _currEvent = PROCESSING_INSTRUCTION;
                break;

            case Node.TEXT_NODE:
                if (_coalescing) {
                    coalesceText();
                } else {
                    _currEvent = CHARACTERS;
                }
                break;

            // Should not get other nodes (notation/entity decl., attr)
            case Node.ATTRIBUTE_NODE:
            case Node.ENTITY_NODE:
            case Node.NOTATION_NODE:
                throw new XMLStreamException("Internal error: unexpected DOM node type " + _currNode.getNodeType()
                    + " (attr/entity/notation?), for node '" + _currNode + "'");

            default:
                throw new XMLStreamException("Internal error: unrecognized DOM node type " + _currNode.getNodeType()
                    + ", for node '" + _currNode + "'");
        }

        return _currEvent;
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
                    throwStreamException("Received non-all-whitespace CHARACTERS or CDATA event in nextTag().");
                    break; // never gets here, but jikes complains without

                case START_ELEMENT:
                case END_ELEMENT:
                    return next;
            }
            throwStreamException(
                "Received event " + Stax2Util.eventTypeDesc(next) + ", instead of START_ELEMENT or END_ELEMENT.");
        }
    }

    /**
     *<p>
     * Note: as per StAX 1.0 specs, this method does NOT close the underlying
     * input reader. That is, unless the new StAX2 property
     * {@link com.azure.xml.implementation.stax2.XMLInputFactory2#P_AUTO_CLOSE_INPUT} is
     * set to true.
     */
    @Override
    public void close() throws XMLStreamException {
        // Since DOM tree has no real input source, nothing to do
    }

    /*
    /**********************************************************************
    /* NamespaceContext
    /**********************************************************************
     */

    @Override
    public String getNamespaceURI(String prefix) {
        /* !!! 26-Apr-2006, TSa: Alas, these methods are DOM Level 3,
         *   i.e. require JDK 1.5 or higher
         */
        /*
        if (prefix.length() == 0) { // def NS
            return _currNode.lookupNamespaceURI(null);
        }
        return _currNode.lookupNamespaceURI(prefix);
        */

        Node n = _currNode;
        boolean defaultNs = (prefix == null) || (prefix.isEmpty());

        while (n != null) {
            NamedNodeMap attrs = n.getAttributes();
            if (attrs != null) {
                for (int i = 0, len = attrs.getLength(); i < len; ++i) {
                    Node attr = attrs.item(i);
                    String thisPrefix = attr.getPrefix();
                    if (thisPrefix == null || thisPrefix.isEmpty()) { // nope
                        if (defaultNs && "xmlns".equals(attr.getLocalName())) {
                            return attr.getNodeValue();
                        }
                    } else if (!defaultNs && "xmlns".equals(thisPrefix)) {
                        if (prefix.equals(attr.getLocalName())) {
                            return attr.getNodeValue();
                        }
                    }
                }
            }
            n = n.getParentNode();
        }
        return null;
    }

    @Override
    public String getPrefix(String namespaceURI) {
        /* !!! 26-Apr-2006, TSa: Alas, these methods are DOM Level 3,
         *   i.e. require JDK 1.5 or higher
         */
        /*
        String prefix = _currNode.lookupPrefix(namespaceURI);
        if (prefix == null) { // maybe default NS?
            String defURI = _currNode.lookupNamespaceURI(null);
            if (defURI != null && defURI.equals(namespaceURI)) {
                return "";
            }
        }
        return prefix;
        */

        Node n = _currNode;
        if (namespaceURI == null) { // not sure if this is even legal but...
            namespaceURI = "";
        }

        while (n != null) {
            NamedNodeMap attrs = n.getAttributes();
            for (int i = 0, len = attrs.getLength(); i < len; ++i) {
                Node attr = attrs.item(i);
                String thisPrefix = attr.getPrefix();
                if (thisPrefix == null || thisPrefix.isEmpty()) {
                    if ("xmlns".equals(attr.getLocalName()) && namespaceURI.equals(attr.getNodeValue())) {
                        return "";
                    }
                } else if ("xmlns".equals(thisPrefix)) {
                    if (namespaceURI.equals(attr.getNodeValue())) {
                        return attr.getLocalName();
                    }
                }
            }
            n = n.getParentNode();
        }
        return null;
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
        String prefix = getPrefix(namespaceURI);
        if (prefix == null) {
            return Collections.emptyIterator();
        }
        return Collections.singletonList(prefix).iterator();
    }

    /*
    /**********************************************************************
    /* TypedXMLStreamReader2 implementation, element
    /**********************************************************************
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
        return dec.getValue();
    }

    @Override
    public byte[] getElementAsBinary() throws XMLStreamException {
        return getElementAsBinary(Base64Variants.getDefaultVariant());
    }

    @Override
    public byte[] getElementAsBinary(Base64Variant v) throws XMLStreamException {
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

    @Override
    public void getElementAs(TypedValueDecoder tvd) throws XMLStreamException {
        String value = getElementText();
        value = Stax2Util.trimSpaces(value);
        try {
            if (value == null) {
                tvd.handleEmptyValue();
            } else {
                tvd.decode(value);
            }
        } catch (IllegalArgumentException iae) {
            throw _constructTypeException(iae, value);
        }
    }

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

    @Override
    public int readElementAsArray(TypedArrayDecoder tad) throws XMLStreamException {
        /* Otherwise either we are just starting (START_ELEMENT), or
         * have collected all the stuff into _textBuffer.
         */
        if (_currEvent == START_ELEMENT) {
            // One special case, no children:
            Node fc = _currNode.getFirstChild();
            if (fc == null) {
                _currEvent = END_ELEMENT;
                return -1;
            }
            _coalescedText = coalesceTypedText(fc);
            _currEvent = CHARACTERS;
            _currNode = _currNode.getLastChild();
        } else {
            if (_currEvent != CHARACTERS && _currEvent != CDATA) {
                // Maybe we are already done?
                if (_currEvent == END_ELEMENT) {
                    return -1;
                }
                reportWrongState(ERR_STATE_NOT_TEXTUAL_OR_ELEM);
            }
            /* One more thing: do we have the data? It is possible
             * that caller has advanced to this text node by itself.
             * We could handle this mostly ok; but that is not a supported
             * use case as per Typed Access API definition (as it can not
             * be reliably supported by all implementations), so:
             */
            if (_coalescedText == null) {
                throw new IllegalStateException(
                    "First call to readElementAsArray() must be for a START_ELEMENT, not directly for a textual event");
            }
        }
        /* Otherwise, need to move pointer to point to the last
         * child node, and fake that it was a textual node
         */
        // Ok, so what do we have left?
        String input = _coalescedText;
        final int end = input.length();
        int ptr = 0;
        int count = 0;
        String value = null;

        try {
            decode_loop: while (ptr < end) {
                // First, any space to skip?
                while (input.charAt(ptr) <= INT_SPACE) {
                    if (++ptr >= end) {
                        break decode_loop;
                    }
                }
                // Then let's figure out non-space char (token)
                int start = ptr;
                ++ptr;
                while (ptr < end && input.charAt(ptr) > INT_SPACE) {
                    ++ptr;
                }
                ++count;
                // And there we have it
                value = input.substring(start, ptr);
                // Plus, can skip trailing space (or at end, just beyond it)
                ++ptr;
                if (tad.decodeValue(value)) {
                    break;
                }
            }
        } catch (IllegalArgumentException iae) {
            // Need to convert to a checked stream exception
            /* Hmmh. This is not an accurate location... but it's
             * about the best we can do
             */
            Location loc = getLocation();
            throw new TypedXMLStreamException(value, iae.getMessage(), loc, iae);
        } finally {
            int len = end - ptr;
            _coalescedText = (len < 1) ? "" : input.substring(ptr);
        }

        if (count < 1) { // end
            _currEvent = END_ELEMENT;
            _currNode = _currNode.getParentNode();
            return -1;
        }
        return count;
    }

    private String coalesceTypedText(Node firstNode) throws XMLStreamException {
        /* This is a bit tricky as we have to collect all the
         * text up end tag, but can not advance to END_ELEMENT
         * event itself (except if there is no content)
         */
        _textBuffer.reset();
        _attrList = null; // so it will not get reused accidentally

        for (Node n = firstNode; n != null; n = n.getNextSibling()) {
            switch (n.getNodeType()) {
                case Node.ELEMENT_NODE:
                    // Illegal to have child elements...
                    throwStreamException(
                        "Element content can not contain child START_ELEMENT when using Typed Access methods");
                case Node.CDATA_SECTION_NODE:
                case Node.TEXT_NODE:
                    _textBuffer.append(n.getNodeValue());
                    break;

                case Node.COMMENT_NODE:
                case Node.PROCESSING_INSTRUCTION_NODE:
                    break;

                default:
                    // Otherwise... do we care? For now, let's do
                    throwStreamException(
                        "Unexpected DOM node type (" + n.getNodeType() + ") when trying to decode Typed content");
            }
        }
        return _textBuffer.get();
    }

    /*
    /**********************************************************************
    /* TypedXMLStreamReader2 implementation, binary data
    /**********************************************************************
     */

    @Override
    public int readElementAsBinary(byte[] resultBuffer, int offset, int maxLength) throws XMLStreamException {
        return readElementAsBinary(resultBuffer, offset, maxLength, Base64Variants.getDefaultVariant());
    }

    @Override
    public int readElementAsBinary(byte[] resultBuffer, int offset, int maxLength, Base64Variant v)
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

        final StringBase64Decoder dec = _base64Decoder();
        int type = _currEvent;
        // First things first: must be acceptable start state:
        if (((1 << type) & MASK_TYPED_ACCESS_BINARY) == 0) {
            if (type == END_ELEMENT) {
                // Minor complication: may have unflushed stuff (non-padded versions)
                if (!dec.hasData()) {
                    return -1;
                }
            } else {
                reportWrongState(ERR_STATE_NOT_TEXTUAL_OR_ELEM);
            }
        }

        // Are we just starting (START_ELEMENT)?
        if (type == START_ELEMENT) {
            // Just need to locate the first text segment (or reach END_ELEMENT)
            while (true) {
                type = next();
                if (type == END_ELEMENT) {
                    // Simple... no textual content
                    return -1;
                }
                if (type == COMMENT || type == PROCESSING_INSTRUCTION) {
                    continue;
                }
                if (((1 << type) & MASK_GET_ELEMENT_TEXT) == 0) {
                    reportParseProblem(ERR_STATE_NOT_TEXTUAL);
                }
                dec.init(v, true, getText());
                break;
            }
        }

        int totalCount = 0;

        main_loop: while (true) {
            // Ok, decode:
            int count;
            try {
                count = dec.decode(resultBuffer, offset, maxLength);
            } catch (IllegalArgumentException iae) {
                throw _constructTypeException(iae, "");
            }
            offset += count;
            totalCount += count;
            maxLength -= count;

            /* And if we filled the buffer we are done. Or, an edge
             * case: reached END_ELEMENT (for non-padded variant)
             */
            if (maxLength < 1 || _currEvent == END_ELEMENT) {
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
                if (((1 << type) & MASK_GET_ELEMENT_TEXT) == 0) {
                    reportParseProblem(ERR_STATE_NOT_TEXTUAL);
                }
                dec.init(v, false, getText());
                break;
            }
        }

        // If nothing was found, needs to be indicated via -1, not 0
        return (totalCount > 0) ? totalCount : -1;
    }

    /*
    /**********************************************************************
    /* TypedXMLStreamReader2 implementation, attribute
    /**********************************************************************
     */

    @Override
    public int getAttributeIndex(String namespaceURI, String localName) {
        return findAttributeIndex(namespaceURI, localName);
    }

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
        return dec.getValue();
    }

    @Override
    public final void getAttributeAs(int index, TypedValueDecoder tvd) throws XMLStreamException {
        String value = getAttributeValue(index);
        value = Stax2Util.trimSpaces(value);
        try {
            if (value == null) {
                tvd.handleEmptyValue();
            } else {
                tvd.decode(value);
            }
        } catch (IllegalArgumentException iae) {
            throw _constructTypeException(iae, value);
        }
    }

    @Override
    public int[] getAttributeAsIntArray(int index) throws XMLStreamException {
        ValueDecoderFactory.IntArrayDecoder dec = _decoderFactory().getIntArrayDecoder();
        _getAttributeAsArray(dec, getAttributeValue(index));
        return dec.getValues();
    }

    @Override
    public long[] getAttributeAsLongArray(int index) throws XMLStreamException {
        ValueDecoderFactory.LongArrayDecoder dec = _decoderFactory().getLongArrayDecoder();
        _getAttributeAsArray(dec, getAttributeValue(index));
        return dec.getValues();
    }

    @Override
    public float[] getAttributeAsFloatArray(int index) throws XMLStreamException {
        ValueDecoderFactory.FloatArrayDecoder dec = _decoderFactory().getFloatArrayDecoder();
        _getAttributeAsArray(dec, getAttributeValue(index));
        return dec.getValues();
    }

    @Override
    public double[] getAttributeAsDoubleArray(int index) throws XMLStreamException {
        ValueDecoderFactory.DoubleArrayDecoder dec = _decoderFactory().getDoubleArrayDecoder();
        _getAttributeAsArray(dec, getAttributeValue(index));
        return dec.getValues();
    }

    @Override
    public int getAttributeAsArray(int index, TypedArrayDecoder tad) throws XMLStreamException {
        return _getAttributeAsArray(tad, getAttributeValue(index));
    }

    protected int _getAttributeAsArray(TypedArrayDecoder tad, String attrValue) throws XMLStreamException {
        int ptr = 0;
        int start;
        final int end = attrValue.length();
        String lexical = null;
        int count = 0;

        try {
            decode_loop: while (ptr < end) {
                // First, any space to skip?
                while (attrValue.charAt(ptr) <= INT_SPACE) {
                    if (++ptr >= end) {
                        break decode_loop;
                    }
                }
                // Then let's figure out non-space char (token)
                start = ptr;
                ++ptr;
                while (ptr < end && attrValue.charAt(ptr) > INT_SPACE) {
                    ++ptr;
                }
                int tokenEnd = ptr;
                ++ptr; // to skip trailing space (or, beyond end)
                // And there we have it
                lexical = attrValue.substring(start, tokenEnd);
                ++count;
                if (tad.decodeValue(lexical)) {
                    if (!checkExpand(tad)) {
                        break;
                    }
                }
            }
        } catch (IllegalArgumentException iae) {
            // Need to convert to a checked stream exception
            Location loc = getLocation();
            throw new TypedXMLStreamException(lexical, iae.getMessage(), loc, iae);
        }
        return count;
    }

    /**
     * Internal method used to see if we can expand the buffer that
     * the array decoder has. Bit messy, but simpler than having
     * separately typed instances; and called rarely so that performance
     * downside of instanceof is irrelevant.
     */
    private boolean checkExpand(TypedArrayDecoder tad) {
        if (tad instanceof ValueDecoderFactory.BaseArrayDecoder) {
            ((ValueDecoderFactory.BaseArrayDecoder) tad).expand();
            return true;
        }
        return false;
    }

    @Override
    public byte[] getAttributeAsBinary(int index) throws XMLStreamException {
        return getAttributeAsBinary(index, Base64Variants.getDefaultVariant());
    }

    @Override
    public byte[] getAttributeAsBinary(int index, Base64Variant v) throws XMLStreamException {
        String lexical = getAttributeValue(index);
        final StringBase64Decoder dec = _base64Decoder();
        dec.init(v, true, lexical);
        try {
            return dec.decodeCompletely();
        } catch (IllegalArgumentException iae) {
            throw _constructTypeException(iae, lexical);
        }
    }

    /*
    /**********************************************************************
    /* XMLStreamReader2 (StAX2) implementation
    /**********************************************************************
     */

    // // // StAX2, per-reader configuration

    @Override
    @Deprecated
    public Object getFeature(String name) {
        // No readable features supported yet
        throw new IllegalArgumentException("Unrecognized feature \"" + name + "\"");
    }

    @Override
    @Deprecated
    public void setFeature(String name, Object value) {
        throw new IllegalArgumentException("Unrecognized feature \"" + name + "\"");
    }

    // // // StAX2, additional traversal methods

    @Override
    public void skipElement() throws XMLStreamException {
        if (_currEvent != START_ELEMENT) {
            reportWrongState(ERR_STATE_NOT_START_ELEM);
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
    public AttributeInfo getAttributeInfo() {
        if (_currEvent != START_ELEMENT) {
            reportWrongState(ERR_STATE_NOT_START_ELEM);
        }
        return this;
    }

    // AttributeInfo impl:

    //public int getAttributeCount()

    @Override
    public int findAttributeIndex(String nsURI, String localName) {
        if (_currEvent != START_ELEMENT) {
            reportWrongState(ERR_STATE_NOT_START_ELEM);
        }
        Element elem = (Element) _currNode;
        NamedNodeMap attrs = elem.getAttributes();
        if (nsURI != null && nsURI.isEmpty()) {
            nsURI = null;
        }
        // Ugh. Horrible clumsy code. But has to do...
        for (int i = 0, len = attrs.getLength(); i < len; ++i) {
            Node attr = attrs.item(i);
            String ln = _safeGetLocalName(attr);
            if (localName.equals(ln)) {
                String thisUri = attr.getNamespaceURI();
                boolean isEmpty = (thisUri == null) || thisUri.isEmpty();
                if (nsURI == null) {
                    if (isEmpty) {
                        return i;
                    }
                } else {
                    if (!isEmpty && nsURI.equals(thisUri)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    // // // StAX2, Additional DTD access

    /**
     * Since this class implements {@link DTDInfo}, method can just
     * return <code>this</code>.
     */
    @Override
    public DTDInfo getDTDInfo() {
        /* Let's not allow it to be accessed during other events -- that
         * way callers won't count on it being available afterwards.
         */
        if (_currEvent != DTD) {
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
    public int getText(Writer w, boolean preserveContents) throws IOException, XMLStreamException {
        String text = getText();
        w.write(text);
        return text.length();
    }

    // // // StAX 2, Other accessors

    /**
     * @return Number of open elements in the stack; 0 when parser is in
     *  prolog/epilog, 1 inside root element and so on.
     */
    @Override
    public int getDepth() {
        return _depth;
    }

    /**
     * @return True, if cursor points to a start or end element that is
     *    constructed from 'empty' element (ends with {@code '/>'});
     *    false otherwise.
     */
    @Override
    public boolean isEmptyElement() {
        // No way to really figure it out via DOM is there?
        return false;
    }

    @Override
    public NamespaceContext getNonTransientNamespaceContext() {
        /* Since DOM does not expose enough functionality to figure
         * out complete declaration stack, can not implement.
         * Can either return null, or a dummy instance. For now, let's
         * do latter:
         */
        return EmptyNamespaceContext.getInstance();
    }

    @Override
    public String getPrefixedName() {
        switch (_currEvent) {
            case START_ELEMENT:
            case END_ELEMENT: {
                String prefix = _currNode.getPrefix();
                String ln = _safeGetLocalName(_currNode);

                if (prefix == null) {
                    return _internName(ln);
                }
                String sb = prefix + ':' + ln;
                return _internName(sb);
            }

            case ENTITY_REFERENCE:
                return getLocalName();

            case PROCESSING_INSTRUCTION:
                return getPITarget();

            case DTD:
                return getDTDRootName();

        }
        throw new IllegalStateException("Current state (" + Stax2Util.eventTypeDesc(_currEvent)
            + ") not START_ELEMENT, END_ELEMENT, ENTITY_REFERENCE, PROCESSING_INSTRUCTION or DTD");
    }

    @Override
    public void closeCompletely() {
        // Nothing special to do...
    }

    /*
    /**********************************************************************
    /* DTDInfo implementation (StAX 2)
    /**********************************************************************
     */

    @Override
    public String getDTDRootName() {
        if (_currEvent == DTD) {
            return _internName(((DocumentType) _currNode).getName());
        }
        return null;
    }

    @Override
    public String getDTDPublicId() {
        if (_currEvent == DTD) {
            return ((DocumentType) _currNode).getPublicId();
        }
        return null;
    }

    @Override
    public String getDTDSystemId() {
        if (_currEvent == DTD) {
            return ((DocumentType) _currNode).getSystemId();
        }
        return null;
    }

    /**
     * @return Internal subset portion of the DOCTYPE declaration, if any;
     *   empty String if none
     */
    @Override
    public String getDTDInternalSubset() {
        /* DOM (level 3) doesn't expose anything extra; would need to
         * synthetize subset... which would only contain some of the
         * entity and notation declarations.
         */
        return null;
    }

    // // StAX2, v2.0

    /*
    /**********************************************************************
    /* LocationInfo implementation (StAX 2)
    /**********************************************************************
     */

    // // // First, the "raw" offset accessors:

    // // // and then the object-based access methods:

    @Override
    public XMLStreamLocation2 getStartLocation() {
        return XMLStreamLocation2.NOT_AVAILABLE;
    }

    @Override
    public XMLStreamLocation2 getCurrentLocation() {
        return XMLStreamLocation2.NOT_AVAILABLE;
    }

    /*
    /**********************************************************************
    /* Stax2 validation: !!! TODO
    /**********************************************************************
     */

    @Override
    public XMLValidator validateAgainst(XMLValidationSchema schema) throws XMLStreamException {
        // Not implemented by the basic reader:
        return null;
    }

    @Override
    public XMLValidator stopValidatingAgainst(XMLValidationSchema schema) throws XMLStreamException {
        // Not implemented by the basic reader:
        return null;
    }

    @Override
    public XMLValidator stopValidatingAgainst(XMLValidator validator) throws XMLStreamException {
        // Not implemented by the basic reader:
        return null;
    }

    @Override
    public ValidationProblemHandler setValidationProblemHandler(ValidationProblemHandler h) {
        // Not implemented by the basic reader
        return null;
    }

    /*
    /**********************************************************************
    /* Internal methods, text gathering
    /**********************************************************************
     */

    protected void coalesceText() {
        _textBuffer.reset();
        _textBuffer.append(_currNode.getNodeValue());

        Node n;
        while ((n = _currNode.getNextSibling()) != null) {
            int type = n.getNodeType();
            if (type != Node.TEXT_NODE && type != Node.CDATA_SECTION_NODE) {
                break;
            }
            _currNode = n;
            _textBuffer.append(_currNode.getNodeValue());
        }
        _coalescedText = _textBuffer.get();

        // Either way, type gets always set to be CHARACTERS
        _currEvent = CHARACTERS;
    }

    /*
    /**********************************************************************
    /* Internal methods, namespace support
    /**********************************************************************
     */

    private QName _constructQName(String uri, String ln, String prefix) {
        // Stupid QName impls barf on nulls...
        return new QName(_internNsURI(uri), _internName(ln), _internName(prefix));
    }

    /**
     * @param attrsToo Whether to include actual attributes too, or
     *   just namespace declarations
     */
    private void _calcNsAndAttrLists(boolean attrsToo) {
        NamedNodeMap attrsIn = _currNode.getAttributes();

        // A common case: neither attrs nor ns decls, can use short-cut
        int len = attrsIn.getLength();
        if (len == 0) {
            _attrList = Collections.emptyList();
            _nsDeclList = Collections.emptyList();
            return;
        }

        if (!_cfgNsAware) {
            _attrList = new ArrayList<>(len);
            for (int i = 0; i < len; ++i) {
                _attrList.add(attrsIn.item(i));
            }
            _nsDeclList = Collections.emptyList();
            return;
        }

        // most should be attributes... and possibly no ns decls:
        ArrayList<Node> attrsOut = null;
        ArrayList<String> nsOut = null;

        for (int i = 0; i < len; ++i) {
            Node attr = attrsIn.item(i);
            String prefix = attr.getPrefix();

            // Prefix?
            if (prefix == null || prefix.isEmpty()) { // nope
                // default ns decl?
                if (!"xmlns".equals(attr.getLocalName())) { // nope
                    if (attrsToo) {
                        if (attrsOut == null) {
                            attrsOut = new ArrayList<>(len - i);
                        }
                        attrsOut.add(attr);
                    }
                    continue;
                }
                prefix = null;
            } else { // explicit ns decl?
                if (!"xmlns".equals(prefix)) { // nope
                    if (attrsToo) {
                        if (attrsOut == null) {
                            attrsOut = new ArrayList<>(len - i);
                        }
                        attrsOut.add(attr);
                    }
                    continue;
                }
                prefix = attr.getLocalName();
            }
            if (nsOut == null) {
                nsOut = new ArrayList<>((len - i) * 2);
            }
            nsOut.add(_internName(prefix));
            nsOut.add(_internNsURI(attr.getNodeValue()));
        }

        _attrList = (attrsOut == null) ? Collections.emptyList() : attrsOut;
        _nsDeclList = (nsOut == null) ? Collections.emptyList() : nsOut;
    }

    private void handleIllegalAttrIndex(int index) {
        Element elem = (Element) _currNode;
        NamedNodeMap attrs = elem.getAttributes();
        int len = attrs.getLength();
        String msg = "Illegal attribute index " + index + "; element <" + elem.getNodeName() + "> has "
            + ((len == 0) ? "no" : String.valueOf(len)) + " attributes";
        throw new IllegalArgumentException(msg);
    }

    private void handleIllegalNsIndex(int index) {
        String msg
            = "Illegal namespace declaration index " + index + " (has " + getNamespaceCount() + " ns declarations)";
        throw new IllegalArgumentException(msg);
    }

    /**
     * Due to differences in how namespace-aware and non-namespace modes
     * work in DOM, different methods are needed. We may or may not be
     * able to detect namespace-awareness mode of the source Nodes
     * directly; but at any rate, should contain some logic for handling
     * problem cases.
     */
    private String _safeGetLocalName(Node n) {
        String ln = n.getLocalName();
        if (ln == null) {
            ln = n.getNodeName();
        }
        return ln;
    }

    /*
    /**********************************************************************
    /* Overridable error reporting methods
    /**********************************************************************
     */

    protected void reportWrongState(int errorType) {
        throw new IllegalStateException(findErrorDesc(errorType, _currEvent));
    }

    protected void reportParseProblem(int errorType) throws XMLStreamException {
        throwStreamException(findErrorDesc(errorType, _currEvent));
    }

    protected void throwStreamException(String msg) throws XMLStreamException {
        throwStreamException(msg, getErrorLocation());
    }

    protected Location getErrorLocation() {
        Location loc = getCurrentLocation();
        if (loc == null) {
            loc = getLocation();
        }
        return loc;
    }

    /**
     * Method called to wrap or convert given conversion-fail exception
     * into a full {@link TypedXMLStreamException},
     *
     * @param iae Problem as reported by converter
     * @param lexicalValue Lexical value (element content, attribute value)
     *    that could not be converted succesfully.
     */
    protected TypedXMLStreamException _constructTypeException(IllegalArgumentException iae, String lexicalValue) {
        String msg = iae.getMessage();
        if (msg == null) {
            msg = "";
        }
        Location loc = getStartLocation();
        if (loc == null) {
            return new TypedXMLStreamException(lexicalValue, msg, iae);
        }
        return new TypedXMLStreamException(lexicalValue, msg, loc);
    }

    protected TypedXMLStreamException _constructTypeException(String msg, String lexicalValue) {
        Location loc = getStartLocation();
        if (loc == null) {
            return new TypedXMLStreamException(lexicalValue, msg);
        }
        return new TypedXMLStreamException(lexicalValue, msg, loc);
    }

    /*
    /**********************************************************************
    /* Other internal methods
    /**********************************************************************
     */

    protected ValueDecoderFactory _decoderFactory() {
        if (_decoderFactory == null) {
            _decoderFactory = new ValueDecoderFactory();
        }
        return _decoderFactory;
    }

    protected StringBase64Decoder _base64Decoder() {
        if (_base64Decoder == null) {
            _base64Decoder = new StringBase64Decoder();
        }
        return _base64Decoder;
    }

    /**
     * Method used to locate error message description to use.
     * Calls sub-classes <code>findErrorDesc()</code> first, and only
     * if no message found, uses default messages defined here.
     */
    protected String findErrorDesc(int errorType, int currEvent) {
        String evtDesc = Stax2Util.eventTypeDesc(currEvent);
        switch (errorType) {
            case ERR_STATE_NOT_START_ELEM:
                return "Current event " + evtDesc + ", needs to be START_ELEMENT";

            case ERR_STATE_NOT_ELEM:
                return "Current event " + evtDesc + ", needs to be START_ELEMENT or END_ELEMENT";

            case ERR_STATE_NO_LOCALNAME:
                return "Current event (" + evtDesc + ") has no local name";

            case ERR_STATE_NOT_PI:
                return "Current event (" + evtDesc + ") needs to be PROCESSING_INSTRUCTION";

            case ERR_STATE_NOT_TEXTUAL:
                return "Current event (" + evtDesc + ") not a textual event";

            case ERR_STATE_NOT_TEXTUAL_OR_ELEM:
                return "Current event (" + evtDesc + " not START_ELEMENT, END_ELEMENT, CHARACTERS or CDATA";

            case ERR_STATE_NOT_TEXTUAL_XXX:
                return "Current event " + evtDesc + ", needs to be one of CHARACTERS, CDATA, SPACE or COMMENT";
        }
        // should never happen, but it'd be bad to throw another exception...
        return "Internal error (unrecognized error type: " + errorType + ")";
    }

    /**
     * Method called to do additional intern()ing for a name, if and as
     * necessary
     */
    protected String _internName(String name) {
        if (name == null) {
            return "";
        }
        return _cfgInternNames ? name.intern() : name;
    }

    protected String _internNsURI(String uri) {
        if (uri == null) {
            return "";
        }
        return _cfgInternNsURIs ? uri.intern() : uri;
    }
}
