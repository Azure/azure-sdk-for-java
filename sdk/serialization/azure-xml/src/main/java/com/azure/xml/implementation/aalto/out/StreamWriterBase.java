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

package com.azure.xml.implementation.aalto.out;

import com.azure.xml.implementation.aalto.impl.ErrorConsts;
import com.azure.xml.implementation.aalto.impl.LocationImpl;
import com.azure.xml.implementation.aalto.impl.StreamExceptionBase;
import com.azure.xml.implementation.aalto.util.XmlConsts;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;

/**
 * Base class for {@link XMLStreamReader} implementations.
 */
public final class StreamWriterBase implements NamespaceContext, XMLStreamWriter {
    protected enum State {
        PROLOG, TREE, EPILOG
    }

    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */

    private final WriterConfig _config;

    /**
     * Root namespace context defined for this writer, if any.
     */
    private NamespaceContext _rootNsContext;

    // // Custom:

    /*
    /**********************************************************************
    /* Symbol table for reusing name serializations
    /**********************************************************************
    */

    private final WNameTable _symbols;

    /*
    /**********************************************************************
    /* Output objects
    /**********************************************************************
     */

    /**
     * Actual physical writer to output serialized XML content to
     */
    private final XmlWriter _xmlWriter;

    /*
    /**********************************************************************
    /* State information
    /**********************************************************************
     */

    private State _state = State.PROLOG;

    /**
     * We'll use a virtual root element (like a document node of sort),
     * to simplify other processing, basically such that there is
     * always a current output element instance, even when in prolog
     * or epilog.
     */
    private OutputElement _currElem = OutputElement.createRoot();

    /**
     * Flag that is set to true first time something has been output.
     * Generally needed to keep track of whether XML declaration
     * (START_DOCUMENT) can be output or not.
     */
    private boolean _stateAnyOutput = false;

    /**
     * Flag that is set during time that a start element is "open", ie.
     * START_ELEMENT has been output (and possibly zero or more name
     * space declarations and attributes), before other main-level
     * constructs have been output.
     */
    private boolean _stateStartElementOpen = false;

    /**
     * Flag that indicates that current element is an empty element (one
     * that is explicitly defined as one, by calling a method -- NOT one
     * that just happens to be empty).
     * This is needed to know what to do when next non-ns/attr node
     * is output; normally a new context is opened, but for empty
     * elements not.
     */
    private boolean _stateEmptyElement = false;

    /**
     * Value passed as the expected root element, when using the multiple
     * argument {@code #writeDTD} method. Will be used in structurally
     * validating mode (and in dtd-validating mode, since that automatically
     * enables structural validation as well, to pre-filter well-formedness
     * errors that validators might have trouble dealing with).
     */
    private final String _dtdRootElemName = null;

    /*
    /**********************************************************************
    /* Pool for recycling OutputElement instances.
    /*
    /* Note: although pooling of cheap objects like OutputElement
    /* is usually not a good idea, here it does make sense, as
    /* instances are still short-lived (same as writer's). Since
    /* instances are ONLY reused within this context, they stay in
    /* cheap ("Eden") GC area, and can lead to slight net gain
    /**********************************************************************
     */

    private OutputElement _outputElemPool = null;

    /**
     * Although pooled objects are small, let's limit the pool size
     * nonetheless, to minimize extra memory usage for deeply nested
     * documents. Even just 4 levels might be enough, 8 should cover
     * > 95% of cases
     */
    final static int MAX_POOL_SIZE = 8;

    private int _poolSize = 0;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    public StreamWriterBase(WriterConfig cfg, XmlWriter writer, WNameTable symbols) {
        _config = cfg;
        _xmlWriter = writer;
        _symbols = symbols;
    }

    /*
    /**********************************************************************
    /* Basic Stax API
    /**********************************************************************
     */

    @Override
    public void close() throws XMLStreamException {
        _finishDocument();
    }

    @Override
    public void flush() throws XMLStreamException {
        try {
            _xmlWriter.flush();
        } catch (IOException ie) {
            throw new StreamExceptionBase(ie);
        }
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return this;
    }

    // note: will be defined later on, in NamespaceContext impl part:
    //public String getPrefix(String uri);

    @Override
    public Object getProperty(String name) {
        // true -> mandatory, unrecognized will throw exception, as per Stax javadocs
        return _config.getProperty(name, true);
    }

    @Override
    public void setDefaultNamespace(String uri) {
        _currElem.setDefaultNsURI(uri);
    }

    @Override
    public void setNamespaceContext(NamespaceContext ctxt) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        if (prefix == null) {
            throw new NullPointerException();
        }
        // Are we actually trying to set the default namespace?
        if (prefix.isEmpty()) {
            setDefaultNamespace(uri);
            return;
        }
        if (uri == null) {
            throw new NullPointerException();
        }

        // Let's check that xml/xmlns are not improperly (re)defined
        {
            if (prefix.equals("xml")) {
                if (!uri.equals(XMLConstants.XML_NS_URI)) {
                    throwOutputError(ErrorConsts.ERR_NS_REDECL_XML, uri);
                }
            } else if (prefix.equals("xmlns")) {
                if (!uri.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
                    throwOutputError(ErrorConsts.ERR_NS_REDECL_XMLNS, uri);
                }
            } else {
                // Neither of prefixes.. but how about URIs?
                if (uri.equals(XMLConstants.XML_NS_URI)) {
                    throwOutputError(ErrorConsts.ERR_NS_REDECL_XML_URI, prefix);
                } else if (uri.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
                    throwOutputError(ErrorConsts.ERR_NS_REDECL_XMLNS_URI, prefix);
                }
            }
            // Empty URI can only be bound to the default ns on xml 1.0:
            if (uri.isEmpty()) {
                throwOutputError(ErrorConsts.ERR_NS_EMPTY);
            }
        }
        _setPrefix(prefix, uri);
    }

    public void _setPrefix(String prefix, String uri) {
        _currElem.addPrefix(prefix, uri);
    }

    @Override
    public void writeAttribute(String localName, String value) throws XMLStreamException {
        if (!_stateStartElementOpen) {
            throwOutputError(ErrorConsts.WERR_ATTR_NO_ELEM);
        }
        // note: for attributes, no prefix <=> no namespace, so:
        _writeAttribute(_symbols.findSymbol(localName), value);
    }

    @Override
    public void writeAttribute(String nsURI, String localName, String value) throws XMLStreamException {
        if (!_stateStartElementOpen) {
            throwOutputError(ErrorConsts.WERR_ATTR_NO_ELEM);
        }
        WName name;
        if (nsURI == null || nsURI.isEmpty()) {
            name = _symbols.findSymbol(localName);
        } else {
            String prefix = _currElem.getExplicitPrefix(nsURI, _rootNsContext);
            if (prefix == null) {
                throwOutputError("Unbound namespace URI '" + nsURI + "'");
            }
            name = _symbols.findSymbol(prefix, localName);
        }
        _writeAttribute(name, value);
    }

    @Override
    public void writeAttribute(String prefix, String nsURI, String localName, String value) throws XMLStreamException {
        if (!_stateStartElementOpen) {
            throwOutputError(ErrorConsts.WERR_ATTR_NO_ELEM);
        }
        WName name = (prefix == null || prefix.isEmpty())
            ? _symbols.findSymbol(localName)
            : _symbols.findSymbol(prefix, localName);
        _writeAttribute(name, value);
    }

    @Override
    public void writeCData(String data) throws XMLStreamException {
        _verifyWriteCData();

        try {
            int ix = _xmlWriter.writeCData(data);
            if (ix >= 0) { // unfixable problems?
                _reportNwfContent(ErrorConsts.WERR_CDATA_CONTENT, ix);
            }
        } catch (IOException ioe) {
            throw new StreamExceptionBase(ioe);
        }
    }

    @Override
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        _stateAnyOutput = true;
        if (_stateStartElementOpen) {
            _closeStartElement(_stateEmptyElement);
        }

        // If outside the main element tree, must be all white space,
        // so let's call appropriate method:
        if (inPrologOrEpilog()) {
            writeSpace(text, start, len);
            return;
        }

        if (len > 0) {
            try {
                _xmlWriter.writeCharacters(text, start, len);
            } catch (IOException ioe) {
                throw new StreamExceptionBase(ioe);
            }
        }
    }

    @Override
    public void writeCharacters(String text) throws XMLStreamException {
        _stateAnyOutput = true;
        if (_stateStartElementOpen) {
            _closeStartElement(_stateEmptyElement);
        }

        if (inPrologOrEpilog()) {
            writeSpace(text);
            return;
        }

        try {
            _xmlWriter.writeCharacters(text);
        } catch (IOException ioe) {
            throw new StreamExceptionBase(ioe);
        }
    }

    @Override
    public void writeComment(String data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeDefaultNamespace(String nsURI) throws XMLStreamException {
        if (!_stateStartElementOpen) {
            throwOutputError(ErrorConsts.WERR_NS_NO_ELEM);
        }
        _writeDefaultNamespace(nsURI);
        /* 31-Jan-2008, tatus: Stax TCK expects an implicit prefix
         *  addition binding. So let's do that, then
         */
        setDefaultNamespace(nsURI);
    }

    @Override
    public void writeDTD(String dtd) {
        throw new UnsupportedOperationException();
    }

    /**
     * It is assumed here that caller actually wants whatever is the
     * default namespace (or it is used in "non-namespace" mode, where
     * no namespaces are bound ever). As such we do not have to
     * distinguish between repairing and non-repairing modes.
     */
    @Override
    public void writeEmptyElement(String localName) throws XMLStreamException {
        _verifyStartElement(null, localName);
        WName name = _symbols.findSymbol(localName);
        _writeStartTag(name, true);
    }

    @Override
    public void writeEmptyElement(String nsURI, String localName) throws XMLStreamException {
        String prefix = _currElem.getPrefix(nsURI);
        if (prefix == null) {
            throwOutputError("Unbound namespace URI '" + nsURI + "'");
        }
        WName name;
        if (prefix.isEmpty()) {
            name = _symbols.findSymbol(localName);
            prefix = null;
        } else {
            name = _symbols.findSymbol(prefix, localName);
        }
        _verifyStartElement(prefix, localName);
        _writeStartTag(name, true, nsURI);
    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String nsURI) throws XMLStreamException {
        _verifyStartElement(prefix, localName);
        WName name;
        if (prefix == null || prefix.isEmpty()) {
            name = _symbols.findSymbol(localName);
        } else {
            name = _symbols.findSymbol(prefix, localName);
        }
        _writeStartTag(name, true, nsURI);
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
        _finishDocument();
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        /* Do we need to close up an earlier empty element?
         * (open start element that was not created via call to
         * writeEmptyElement gets handled later on)
         */
        if (_stateStartElementOpen && _stateEmptyElement) {
            _stateEmptyElement = false;
            _closeStartElement(true);
        }

        // Better have something to close... (to figure out what to close)
        if (_state != State.TREE) {
            _reportNwfStructure("No open start element, when trying to write end element");
        }

        OutputElement thisElem = _currElem;

        // Ok, and then let's pop that element from the stack
        _currElem = thisElem.getParent();
        if (_poolSize < MAX_POOL_SIZE) {
            thisElem.addToPool(_outputElemPool);
            _outputElemPool = thisElem;
            ++_poolSize;
        }

        try {
            // Do we have an unfinished start element? If so, will get empty elem
            if (_stateStartElementOpen) {
                /* Can't/shouldn't call _closeStartElement (since we need to
                 * write empty element), but need to do same processing.
                 */
                _stateStartElementOpen = false;
                _xmlWriter.writeStartTagEmptyEnd();
            } else { // Otherwise, full end element
                _xmlWriter.writeEndTag(thisElem.getName());
            }
        } catch (IOException ioe) {
            throw new StreamExceptionBase(ioe);
        }
        if (_currElem.isRoot()) { // (note: we have dummy placeholder elem that contains doc)
            _state = State.EPILOG;
        }
    }

    @Override
    public void writeEntityRef(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeNamespace(String prefix, String nsURI) throws XMLStreamException {
        if (prefix == null || prefix.isEmpty()) {
            writeDefaultNamespace(nsURI);
            return;
        }
        if (!_stateStartElementOpen) {
            throwOutputError(ErrorConsts.WERR_NS_NO_ELEM);
        }
        _writeNamespace(prefix, nsURI);
        // 31-Jan-2008, tatus: Stax TCK expects an implicit prefix
        //  addition binding. So let's do that, then
        setPrefix(prefix, nsURI);
    }

    @Override
    public void writeProcessingInstruction(String target) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeProcessingInstruction(String target, String data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        String enc = _config.getActualEncoding();
        if (enc == null) {
            enc = XmlConsts.STAX_DEFAULT_OUTPUT_ENCODING;
            _config.setActualEncodingIfNotSet(enc);
        }
        _writeStartDocument(XmlConsts.STAX_DEFAULT_OUTPUT_VERSION, enc);
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
        _writeStartDocument(version, _config.getActualEncoding());
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        _writeStartDocument(version, encoding);
    }

    /**
     * It is assumed here that caller actually wants whatever is the
     * default namespace (or it is used in "non-namespace" mode, where
     * no namespaces are bound ever). As such we do not have to
     * distinguish between repairing and non-repairing modes.
     */
    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
        _verifyStartElement(null, localName);
        WName name = _symbols.findSymbol(localName);
        _writeStartTag(name, false);
    }

    @Override
    public void writeStartElement(String nsURI, String localName) throws XMLStreamException {
        String prefix = _currElem.getPrefix(nsURI);
        if (prefix == null) {
            throwOutputError("Unbound namespace URI '" + nsURI + "'");
        }
        WName name;
        if (prefix.isEmpty()) {
            name = _symbols.findSymbol(localName);
            prefix = null;
        } else {
            name = _symbols.findSymbol(prefix, localName);
        }
        _verifyStartElement(prefix, localName);
        _writeStartTag(name, false);
    }

    @Override
    public void writeStartElement(String prefix, String localName, String nsURI) throws XMLStreamException {
        _verifyStartElement(prefix, localName);
        WName name;
        if (prefix == null || prefix.isEmpty()) {
            name = _symbols.findSymbol(localName);
        } else {
            name = _symbols.findSymbol(prefix, localName);
        }
        _writeStartTag(name, false, nsURI);
    }

    /*
    /**********************************************************************
    /* NamespaceContext implementation
    /**********************************************************************
     */

    @Override
    public String getNamespaceURI(String prefix) {
        String uri = _currElem.getNamespaceURI(prefix);
        if (uri == null) {
            if (_rootNsContext != null) {
                uri = _rootNsContext.getNamespaceURI(prefix);
            }
        }
        return uri;
    }

    @Override
    public String getPrefix(String uri) {
        String prefix = _currElem.getPrefix(uri);
        if (prefix == null) {
            if (_rootNsContext != null) {
                prefix = _rootNsContext.getPrefix(uri);
            }
        }
        return prefix;
    }

    @Override
    public Iterator<String> getPrefixes(String uri) {
        return _currElem.getPrefixes(uri, _rootNsContext);
    }

    /*
    /**********************************************************************
    /* XMLStreamWriter2 methods (StAX2)
    /**********************************************************************
     */

    /**
     * Method that can be called to write whitespace-only content.
     * If so, it is to be written as is (with no escaping), and does
     * not contain non-whitespace characters (writer may validate this,
     * and throw an exception if it does).
     *<p>
     * This method is useful for things like outputting indentation.
     *
     * @since 3.0
     */
    public void writeSpace(String text) throws XMLStreamException {
        try {
            _xmlWriter.writeSpace(text);
        } catch (IOException ioe) {
            throw new StreamExceptionBase(ioe);
        }
    }

    /**
     * Method that can be called to write whitespace-only content.
     * If so, it is to be written as is (with no escaping), and does
     * not contain non-whitespace characters (writer may validate this,
     * and throw an exception if it does).
     *<p>
     * This method is useful for things like outputting indentation.
     *
     * @since 3.0
     */
    public void writeSpace(char[] cbuf, int offset, int len) throws XMLStreamException {
        try {
            _xmlWriter.writeSpace(cbuf, offset, len);
        } catch (IOException ioe) {
            throw new StreamExceptionBase(ioe);
        }
    }

    /*
    /**********************************************************************
    /* Stax2, other accessors, mutators
    /**********************************************************************
     */

    /**
     * Method that should return current output location, if the writer
     * keeps track of it; null if it does not.
     */
    public Location getLocation() {
        return new LocationImpl( // pub/sys ids not yet known
            _xmlWriter.getAbsOffset(), _xmlWriter.getRow(), _xmlWriter.getColumn());
    }

    /*
    /**********************************************************************
    /* Package methods (ie not part of public API)
    /**********************************************************************
     */

    /**
     * Method called to close an open start element, when another
     * main-level element (not namespace declaration or attribute)
     * is being output; except for end element which is handled differently.
     */
    private void _closeStartElement(boolean emptyElem) throws XMLStreamException {
        _stateStartElementOpen = false;
        try {
            if (emptyElem) {
                _xmlWriter.writeStartTagEmptyEnd();
            } else {
                _xmlWriter.writeStartTagEnd();
            }
        } catch (IOException ioe) {
            throw new StreamExceptionBase(ioe);
        }

        // Need bit more special handling for empty elements...
        if (emptyElem) {
            OutputElement thisElem = _currElem;
            _currElem = thisElem.getParent();
            if (_currElem.isRoot()) { // Did we close the root? (isRoot() returns true for the virtual "document node")
                _state = State.EPILOG;
            }
            if (_poolSize < MAX_POOL_SIZE) {
                thisElem.addToPool(_outputElemPool);
                _outputElemPool = thisElem;
                ++_poolSize;
            }
        }
    }

    private boolean inPrologOrEpilog() {
        return (_state != State.TREE);
    }

    /*
    /**********************************************************************
    /* Package methods, write helpers
    /**********************************************************************
     */

    private void _writeAttribute(WName name, String value) throws XMLStreamException {
        try {
            _xmlWriter.writeAttribute(name, value);
        } catch (IOException ioe) {
            throw new StreamExceptionBase(ioe);
        }
    }

    private void _writeDefaultNamespace(String uri) throws XMLStreamException {
        WName name = _symbols.findSymbol("xmlns");
        try {
            _xmlWriter.writeAttribute(name, uri);
        } catch (IOException ioe) {
            throw new StreamExceptionBase(ioe);
        }
    }

    private void _writeNamespace(String prefix, String uri) throws XMLStreamException {
        WName name = _symbols.findSymbol("xmlns", prefix);
        try {
            _xmlWriter.writeAttribute(name, uri);
        } catch (IOException ioe) {
            throw new StreamExceptionBase(ioe);
        }
    }

    private void _writeStartDocument(String version, String encoding) throws XMLStreamException {
        /* Not legal to output XML declaration if there has been ANY
         * output prior... that is, if we validate the structure.
         */
        if (_stateAnyOutput) {
            _reportNwfStructure(ErrorConsts.WERR_DUP_XML_DECL);
        }

        _stateAnyOutput = true;

        // !!! If and how to check encoding?
        // if (encoding != null) { }
        if (version != null && !version.isEmpty()) {
            if (!(version.equals(XmlConsts.XML_V_10_STR) || version.equals(XmlConsts.XML_V_11_STR))) {
                _reportNwfContent("Illegal version argument ('" + version + "'); should only use '"
                    + XmlConsts.XML_V_10_STR + "' or '" + XmlConsts.XML_V_11_STR + "'");
            }
        }

        if (version == null || version.isEmpty()) {
            version = XmlConsts.STAX_DEFAULT_OUTPUT_VERSION;
        }
        if (XmlConsts.XML_V_11_STR.equals(version)) {
            _xmlWriter.enableXml11();
        }

        if (encoding != null && !encoding.isEmpty()) {
            /* What about conflicting encoding? Let's only update encoding,
             * if it wasn't set.
             */
            _config.setActualEncodingIfNotSet(encoding);
        }
        try {
            _xmlWriter.writeXmlDeclaration(version, encoding, null);
        } catch (IOException ioe) {
            throw new StreamExceptionBase(ioe);
        }
    }

    private void _writeStartTag(WName name, boolean isEmpty) throws XMLStreamException {
        _stateAnyOutput = true;
        _stateStartElementOpen = true;

        if (_outputElemPool != null) {
            OutputElement newCurr = _outputElemPool;
            _outputElemPool = newCurr.reuseAsChild(_currElem, name);
            --_poolSize;
            _currElem = newCurr;
        } else {
            _currElem = _currElem.createChild(name);
        }
        try {
            _xmlWriter.writeStartTagStart(name);
        } catch (IOException ioe) {
            throw new StreamExceptionBase(ioe);
        }
        _stateEmptyElement = isEmpty;
    }

    private void _writeStartTag(WName name, boolean isEmpty, String uri) throws XMLStreamException {
        _stateAnyOutput = true;
        _stateStartElementOpen = true;

        if (uri == null) { // let's canonicalize to empty String here
            uri = "";
        }

        if (_outputElemPool != null) {
            OutputElement newCurr = _outputElemPool;
            _outputElemPool = newCurr.reuseAsChild(_currElem, name, uri);
            --_poolSize;
            _currElem = newCurr;
        } else {
            _currElem = _currElem.createChild(name, uri);
        }
        try {
            _xmlWriter.writeStartTagStart(name);
        } catch (IOException ioe) {
            throw new StreamExceptionBase(ioe);
        }
        _stateEmptyElement = isEmpty;
    }

    /*
    /**********************************************************************
    /* Package methods, validation
    /**********************************************************************
     */

    /**
     * Method that is called to ensure that we can start writing an
     * element, both from structural point of view, and from syntactic
     * (close previously open start element, if any). Note that since
     * it needs to be called before writing out anything, no namespace
     * bindings have been (or can be) output, and hence given prefix
     * may not be one that actually gets used.
     */
    private void _verifyStartElement(String prefix, String localName) throws XMLStreamException {
        // Need to finish an open start element?
        if (_stateStartElementOpen) {
            _closeStartElement(_stateEmptyElement);
        } else if (_state == State.PROLOG) {
            _verifyRootElement();
        } else if (_state == State.EPILOG) {
            String name = (prefix == null) ? localName : (prefix + ":" + localName);
            _reportNwfStructure(ErrorConsts.WERR_PROLOG_SECOND_ROOT, name);
            /* When outputting a fragment, need to reset this to the
             * tree. No point in trying to verify the root element?
             */
            _state = State.TREE;
        }
    }

    private void _verifyWriteCData() throws XMLStreamException {
        _stateAnyOutput = true;
        if (_stateStartElementOpen) {
            _closeStartElement(_stateEmptyElement);
        }

        // Not legal outside main element tree:
        if (inPrologOrEpilog()) {
            _reportNwfStructure(ErrorConsts.WERR_PROLOG_CDATA);
        }
    }

    private void _verifyWriteDTD() throws XMLStreamException {
        if (_state != State.PROLOG) {
            throw new XMLStreamException("Can not write DOCTYPE declaration (DTD) when not in prolog any more (state "
                + _state + "; start element(s) written)");
        }
        // And let's also check that we only output one...
        if (_dtdRootElemName != null) {
            throw new XMLStreamException("Trying to write multiple DOCTYPE declarations");
        }
    }

    private void _verifyRootElement() {
        // !!! TBI: only relevant if we are actually validating?

        _state = State.TREE;
    }

    /*
    /**********************************************************************
    /* Package methods, basic output problem reporting
    /**********************************************************************
     */

    private static void throwOutputError(String msg) throws XMLStreamException {
        throw new StreamExceptionBase(msg);
    }

    private static void throwOutputError(String format, Object arg) throws XMLStreamException {
        String msg = MessageFormat.format(format, arg);
        throwOutputError(msg);
    }

    /**
     * This is the method called when an output method call violates
     * structural well-formedness checks
     * and structural checking
     * is enabled.
     */
    private static void _reportNwfStructure(String msg) throws XMLStreamException {
        throwOutputError(msg);
    }

    private static void _reportNwfStructure(String msg, Object arg) throws XMLStreamException {
        throwOutputError(msg, arg);
    }

    /**
     * This is the method called when an output method call violates
     * content well-formedness checks
     * and content validation
     * is enabled.
     */
    private static void _reportNwfContent(String msg) throws XMLStreamException {
        throwOutputError(msg);
    }

    private static void _reportNwfContent(String msg, Object arg) throws XMLStreamException {
        throwOutputError(msg, arg);
    }

    /*
    /**********************************************************************
    /* Package methods, output validation problem reporting
    /**********************************************************************
     */

    private void _finishDocument() throws XMLStreamException {
        // Is tree still open?
        if (_state != State.EPILOG) {
            if (_state == State.PROLOG) {
                _reportNwfStructure(ErrorConsts.WERR_PROLOG_NO_ROOT);
            }
            // Need to close the open sub-tree, if it exists...
            // First, do we have an open start element?
            if (_stateStartElementOpen) {
                _closeStartElement(_stateEmptyElement);
            }
            // Then, one by one, need to close open scopes:
            while (_state != State.EPILOG) {
                writeEndElement();
            }
        }

        // Any symbols to merge?
        if (_symbols.maybeDirty()) {
            _symbols.mergeToParent();
        }

        /* And finally, inform the underlying writer that it should flush
         * and release its buffers, and close components it uses if any.
         */
        try {
            _xmlWriter.close(false);
        } catch (IOException ie) {
            throw new StreamExceptionBase(ie);
        }
    }

    @Override
    public String toString() {
        return "[StreamWriter: " + getClass() + ", underlying outputter: "
            + ((_xmlWriter == null) ? "NULL" : _xmlWriter.toString());
    }
}
