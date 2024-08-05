// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.dom;

import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.*;
import javax.xml.stream.*;
import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.*;

import com.azure.xml.implementation.stax2.ri.EmptyNamespaceContext;
import com.azure.xml.implementation.stax2.ri.dom.DOMWrappingWriter;

import com.azure.xml.implementation.aalto.out.WriterConfig;

/**
 * This is an adapter class that allows building a DOM tree using
 * {@link XMLStreamWriter} interface.
 *<p>
 * Note that the implementation is only to be used for use with
 * <code>javax.xml.transform.dom.DOMResult</code>.
 *<p>
 * Some notes regarding missing/incomplete functionality:
 * <ul>
 *  <li>Namespace-repairing mode not implemented
 *   <li>
 *  <li>Validation functionality not implemented
 *   <li>
 *  </ul>
 * @author Tatu Saloranta
 */
public final class DOMWriterImpl extends DOMWrappingWriter {
    /*
    ////////////////////////////////////////////////////
    // Configuration
    ////////////////////////////////////////////////////
     */

    private final WriterConfig _config;

    /*
    ////////////////////////////////////////////////////
    // State
    ////////////////////////////////////////////////////
     */

    /**
     * This element is the current context element, under which
     * all other nodes are added, until matching end element
     * is output. Null outside of the main element tree.
     *<p>
     * Note: explicit empty element (written using
     * <code>writeEmptyElement</code>) will never become
     * current element.
     */
    private DOMOutputElement _currElem;

    /**
     * This element is non-null right after a call to
     * either <code>writeStartElement</code> and
     * <code>writeEmptyElement</code>, and can be used to
     * add attributes and namespace declarations.
     *<p>
     * Note: while this is often the same as {@link #_currElem},
     * it's not always. Specifically, an empty element (written
     * explicitly using <code>writeEmptyElement</code>) will
     * become open element but NOT current element. Conversely,
     * regular elements will remain current element when
     * non elements are written (text, comments, PI), but
     * not the open element.
     */
    private DOMOutputElement _openElement;

    /**
     *  for NsRepairing mode
     */
    private int[] _autoNsSeq;
    private String _suggestedDefNs = null;
    private final String _automaticNsPrefix;

    /**
     * Map that contains URI-to-prefix entries that point out suggested
     * prefixes for URIs. These are populated by calls to
     * {@link #setPrefix}, and they are only used as hints for binding;
     * if there are conflicts, repairing writer can just use some other
     * prefix.
     */
    HashMap<String, String> _suggestedPrefixes = null;

    /*
    ////////////////////////////////////////////////////
    // Life-cycle
    ////////////////////////////////////////////////////
     */

    private DOMWriterImpl(WriterConfig cfg, Node treeRoot) throws XMLStreamException {
        super(treeRoot, true, cfg.willRepairNamespaces());
        _config = cfg;
        _autoNsSeq = null;
        _automaticNsPrefix = cfg.getAutomaticNsPrefix();

        /* Ok; we need a document node; or an element node; or a document
         * fragment node.
         */
        switch (treeRoot.getNodeType()) {
            case Node.DOCUMENT_NODE:
            case Node.DOCUMENT_FRAGMENT_NODE:
                // both are ok, but no current element
                _currElem = DOMOutputElement.createRoot();
                _openElement = null;
                break;

            case Node.ELEMENT_NODE: // can make sub-tree... ok
            {
                // still need a virtual root node as parent
                DOMOutputElement root = DOMOutputElement.createRoot();
                Element elem = (Element) treeRoot;
                _openElement = _currElem = root.createChild(elem);
            }
                break;

            default: // other Nodes not usable
                throw new XMLStreamException(
                    "Can not create an XMLStreamWriter for a DOM node of type " + treeRoot.getClass());
        }
    }

    public static DOMWriterImpl createFrom(WriterConfig cfg, DOMResult dst) throws XMLStreamException {
        Node rootNode = dst.getNode();
        return new DOMWriterImpl(cfg, rootNode);
    }

    /*
    ////////////////////////////////////////////////////
    // XMLStreamWriter API (Stax 1.0)
    ////////////////////////////////////////////////////
     */

    //public void close() { }
    //public void flush() { }

    @Override
    public NamespaceContext getNamespaceContext() {
        if (!mNsAware) {
            return EmptyNamespaceContext.getInstance();
        }
        return _currElem;
    }

    @Override
    public String getPrefix(String uri) {
        if (!mNsAware) {
            return null;
        }
        if (mNsContext != null) {
            String prefix = mNsContext.getPrefix(uri);
            if (prefix != null) {
                return prefix;
            }
        }
        return _currElem.getPrefix(uri);
    }

    @Override
    public Object getProperty(String name) {
        /* Here we don't want to throw an exception, should the property
         * not be supported; thus passing false as second arg
         */
        return _config.getProperty(name, false);
    }

    @Override
    public void setDefaultNamespace(String uri) {
        _suggestedDefNs = (uri == null || uri.isEmpty()) ? null : uri;
    }

    //public void setNamespaceContext(NamespaceContext context)

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        if (prefix == null) {
            throw new NullPointerException("Can not pass null 'prefix' value");
        }
        // Are we actually trying to set the default namespace?
        if (prefix.isEmpty()) {
            setDefaultNamespace(uri);
            return;
        }
        if (uri == null) {
            throw new NullPointerException("Can not pass null 'uri' value");
        }

        /* Let's verify that xml/xmlns are never (mis)declared; as
         * mandated by XML NS specification
         */
        {
            if (prefix.equals("xml")) {
                if (!uri.equals(XMLConstants.XML_NS_URI)) {
                    throwOutputError("Trying to redeclare prefix 'xml' from its default URI '" + XMLConstants.XML_NS_URI
                        + "' to \"" + uri + "\"");
                }
            } else if (prefix.equals("xmlns")) { // prefix "xmlns"
                if (!uri.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
                    throwOutputError("Trying to declare prefix 'xmlns' (illegal as per NS 1.1 #4)");
                }
                // At any rate; we are NOT to output it
                return;
            } else {
                // Neither of prefixes.. but how about URIs?
                if (uri.equals(XMLConstants.XML_NS_URI)) {
                    throwOutputError("Trying to bind URI '" + XMLConstants.XML_NS_URI + " to prefix \"" + prefix
                        + "\" (can only bind to 'xml')");
                } else if (uri.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
                    throwOutputError("Trying to bind URI '" + XMLConstants.XMLNS_ATTRIBUTE_NS_URI + " to prefix \""
                        + prefix + "\" (can not be explicitly bound)");
                }
            }
        }

        if (_suggestedPrefixes == null) {
            _suggestedPrefixes = new HashMap<>(16);
        }
        _suggestedPrefixes.put(uri, prefix);

    }

    @Override
    public void writeAttribute(String localName, String value) throws XMLStreamException {
        outputAttribute(null, null, localName, value);
    }

    @Override
    public void writeAttribute(String nsURI, String localName, String value) throws XMLStreamException {
        outputAttribute(nsURI, null, localName, value);
    }

    @Override
    public void writeAttribute(String prefix, String nsURI, String localName, String value) throws XMLStreamException {
        outputAttribute(nsURI, prefix, localName, value);
    }

    //public void writeCData(String data)
    //public void writeCharacters(char[] text, int start, int len)
    //public void writeCharacters(String text)
    //public void writeComment(String data)

    @Override
    public void writeDefaultNamespace(String nsURI) {
        if (_openElement == null) {
            throw new IllegalStateException("No currently open START_ELEMENT, cannot write attribute");
        }
        setDefaultNamespace(nsURI);
        _openElement.addAttribute(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns", nsURI);
    }

    //public void writeDTD(String dtd)

    @Override
    public void writeEmptyElement(String localName) throws XMLStreamException {
        writeEmptyElement(null, localName);
    }

    @Override
    public void writeEmptyElement(String nsURI, String localName) throws XMLStreamException {
        // First things first: must

        /* Note: can not just call writeStartElement(), since this
         * element will only become the open elem, but not a parent elem
         */
        createStartElem(nsURI, null, localName, true);
    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String nsURI) throws XMLStreamException {
        if (prefix == null) { // passing null would mean "dont care", if repairing
            prefix = "";
        }
        createStartElem(nsURI, prefix, localName, true);
    }

    @Override
    public void writeEndDocument() {
        _currElem = _openElement = null;
    }

    @Override
    public void writeEndElement() {
        // Simple, just need to traverse up... if we can
        if (_currElem == null || _currElem.isRoot()) {
            throw new IllegalStateException("No open start element to close");
        }
        _openElement = null; // just in case it was open
        _currElem = _currElem.getParent();
    }

    @Override
    public void writeNamespace(String prefix, String nsURI) throws XMLStreamException {
        if (prefix == null || prefix.isEmpty()) {
            writeDefaultNamespace(nsURI);
            return;
        }
        if (!mNsAware) {
            throwOutputError("Can not write namespaces with non-namespace writer.");
        }
        outputAttribute(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns", prefix, nsURI);
        _currElem.addPrefix(prefix, nsURI);
    }

    //public void writeProcessingInstruction(String target)
    //public void writeProcessingInstruction(String target, String data)

    //public void writeStartDocument()
    //public void writeStartDocument(String version)
    //public void writeStartDocument(String encoding, String version)

    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
        writeStartElement(null, localName);
    }

    @Override
    public void writeStartElement(String nsURI, String localName) throws XMLStreamException {
        createStartElem(nsURI, null, localName, false);
    }

    @Override
    public void writeStartElement(String prefix, String localName, String nsURI) throws XMLStreamException {
        createStartElem(nsURI, prefix, localName, false);
    }
    /*
    ////////////////////////////////////////////////////
    // XMLStreamWriter2 API (Stax2 v2.0)
    ////////////////////////////////////////////////////
     */

    @Override
    public boolean isPropertySupported(String name) {
        // !!! TBI: not all these properties are really supported
        return _config.isPropertySupported(name);
    }

    @Override
    public boolean setProperty(String name, Object value) {
        /* Note: can not call local method, since it'll return false for
         * recognized but non-mutable properties
         */
        return _config.setProperty(name, value);
    }

    //public XMLValidator validateAgainst(XMLValidationSchema schema)
    //public XMLValidator stopValidatingAgainst(XMLValidationSchema schema)
    //public XMLValidator stopValidatingAgainst(XMLValidator validator)
    //public ValidationProblemHandler setValidationProblemHandler(ValidationProblemHandler h)
    //public XMLStreamLocation2 getLocation()
    //public String getEncoding() {

    //public void writeCData(char[] text, int start, int len)

    @Override
    public void writeDTD(String rootName, String systemId, String publicId, String internalSubset)
        throws XMLStreamException {
        /* Alas: although we can create a DocumentType object, there
         * doesn't seem to be a way to attach it in DOM-2!
         */
        if (_currElem != null) {
            throw new IllegalStateException("Operation only allowed to the document before adding root element");
        }
        reportUnsupported("writeDTD()");
    }

    //public void writeFullEndElement() throws XMLStreamException

    //public void writeSpace(char[] text, int start, int len)
    //public void writeSpace(String text)

    //public void writeStartDocument(String version, String encoding, boolean standAlone)

    /*
    ////////////////////////////////////////////
    // Impls of abstract methods from base class
    ////////////////////////////////////////////
     */

    @Override
    protected void appendLeaf(Node n) throws IllegalStateException {
        _currElem.appendNode(n);
        _openElement = null;
    }

    /*
    ///////////////////////////////
    // Internal methods
    ///////////////////////////////
     */

    /* Note: copied from regular RepairingNsStreamWriter#writeStartOrEmpty
     * (and its non-repairing counterpart).
     */

    /**
     *  Method called by all start element write methods.
     *
     * @param nsURI Namespace URI to use: null and empty String denote 'no namespace'
     */
    private void createStartElem(String nsURI, String prefix, String localName, boolean isEmpty)
        throws XMLStreamException {
        DOMOutputElement elem;

        if (!mNsAware) {
            if (nsURI != null && !nsURI.isEmpty()) {
                throwOutputError("Can not specify non-empty uri/prefix in non-namespace mode");
            }
            elem = _currElem.createAndAttachChild(mDocument.createElement(localName));
        } else {
            if (mNsRepairing) {
                String actPrefix = validateElemPrefix(prefix, nsURI, _currElem);
                if (actPrefix != null) { // fine, an existing binding we can use:
                    if (!actPrefix.isEmpty()) {
                        elem = _currElem
                            .createAndAttachChild(mDocument.createElementNS(nsURI, actPrefix + ":" + localName));
                    } else {
                        elem = _currElem.createAndAttachChild(mDocument.createElementNS(nsURI, localName));
                    }
                } else { // nah, need to create a new binding...
                    /* Need to ensure that we'll pass "" as prefix, not null,
                     * so it is understood as "I want to use the default NS",
                     * not as "whatever prefix, I don't care"
                     */
                    if (prefix == null) {
                        prefix = "";
                    }
                    actPrefix = generateElemPrefix(prefix, nsURI, _currElem);
                    boolean hasPrefix = (!actPrefix.isEmpty());
                    if (hasPrefix) {
                        localName = actPrefix + ":" + localName;
                    }
                    elem = _currElem.createAndAttachChild(mDocument.createElementNS(nsURI, localName));
                    /* Hmmh. writeNamespace method requires open element
                     * to be defined. So we'll need to set it first
                     * (will be set again at a later point -- would be
                     * good to refactor this method into separate
                     * sub-classes or so)
                     */
                    _openElement = elem;
                    // Need to add new ns declaration as well
                    if (hasPrefix) {
                        writeNamespace(actPrefix, nsURI);
                        elem.addPrefix(actPrefix, nsURI);
                    } else {
                        writeDefaultNamespace(nsURI);
                        elem.setDefaultNsUri(nsURI);
                    }
                }
            } else {
                /* Non-repairing; if non-null prefix (including "" to
                 * indicate "no prefix") passed, use as is, otherwise
                 * try to locate the prefix if got namespace.
                 */
                if (prefix == null && nsURI != null && !nsURI.isEmpty()) {
                    prefix = (_suggestedPrefixes == null) ? null : _suggestedPrefixes.get(nsURI);
                    if (prefix == null) {
                        throwOutputError("Can not find prefix for namespace \"" + nsURI + "\"");
                    }
                }
                if (prefix != null && !prefix.isEmpty()) {
                    localName = prefix + ":" + localName;
                }
                elem = _currElem.createAndAttachChild(mDocument.createElementNS(nsURI, localName));
            }
        }
        /* Got the element; need to make it the open element, and
         * if it's not an (explicit) empty element, current element as well
         */
        _openElement = elem;
        if (!isEmpty) {
            _currElem = elem;
        }
    }

    private void outputAttribute(String nsURI, String prefix, String localName, String value)
        throws XMLStreamException {
        if (_openElement == null) {
            throw new IllegalStateException("No currently open START_ELEMENT, cannot write attribute");
        }

        if (mNsAware) {
            if (mNsRepairing) {
                prefix = findOrCreateAttrPrefix(prefix, nsURI, _openElement);
            }
            if (prefix != null && !prefix.isEmpty()) {
                localName = prefix + ":" + localName;
            }
            _openElement.addAttribute(nsURI, localName, value);
        } else { // non-ns, simple
            if (prefix != null && !prefix.isEmpty()) {
                localName = prefix + ":" + localName;
            }
            _openElement.addAttribute(localName, value);
        }
    }

    private String validateElemPrefix(String prefix, String nsURI, DOMOutputElement elem) throws XMLStreamException {
        /* 06-Feb-2005, TSa: Special care needs to be taken for the
         *   "empty" (or missing) namespace:
         *   (see comments from findOrCreatePrefix())
         */
        if (nsURI == null || nsURI.isEmpty()) {
            String currURL = elem.getDefaultNsUri();
            if (currURL == null || currURL.isEmpty()) {
                // Ok, good:
                return "";
            }
            // Nope, needs to be re-bound:
            return null;
        }

        int status = elem.isPrefixValid(prefix, nsURI, true);
        if (status == DOMOutputElement.PREFIX_OK) {
            return prefix;
        }
        return null;
    }

    /*
    ////////////////////////////////////////////////////
    // Internal methods
    ////////////////////////////////////////////////////
     */

    /**
     * Method called after {@link #findElemPrefix} has returned null,
     * to create and bind a namespace mapping for specified namespace.
     */
    private String generateElemPrefix(String suggPrefix, String nsURI, DOMOutputElement elem) {
        /* Ok... now, since we do not have an existing mapping, let's
         * see if we have a preferred prefix to use.
         */
        /* Except if we need the empty namespace... that can only be
         * bound to the empty prefix:
         */
        if (nsURI == null || nsURI.isEmpty()) {
            return "";
        }

        /* Ok; with elements this is easy: the preferred prefix can
         * ALWAYS be used, since it can mask preceding bindings:
         */
        if (suggPrefix == null) {
            // caller wants this URI to map as the default namespace?
            if (_suggestedDefNs != null && _suggestedDefNs.equals(nsURI)) {
                suggPrefix = "";
            } else {
                suggPrefix = (_suggestedPrefixes == null) ? null : _suggestedPrefixes.get(nsURI);
                if (suggPrefix == null) {
                    /* 16-Oct-2005, TSa: We have 2 choices here, essentially;
                     *   could make elements always try to override the def
                     *   ns... or can just generate new one. Let's do latter
                     *   for now.
                     */
                    if (_autoNsSeq == null) {
                        _autoNsSeq = new int[1];
                        _autoNsSeq[0] = 1;
                    }
                    suggPrefix = elem.generateMapping(_automaticNsPrefix, nsURI, _autoNsSeq);
                }
            }
        }

        // Ok; let's let the caller deal with bindings
        return suggPrefix;
    }

    /**
     * Method called to somehow find a prefix for given namespace, to be
     * used for a new start element; either use an existing one, or
     * generate a new one. If a new mapping needs to be generated,
     * it will also be automatically bound, and necessary namespace
     * declaration output.
     *
     * @param suggPrefix Suggested prefix to bind, if any; may be null
     *   to indicate "no preference"
     * @param nsURI URI of namespace for which we need a prefix
     * @param elem Currently open start element, on which the attribute
     *   will be added.
     */
    private String findOrCreateAttrPrefix(String suggPrefix, String nsURI, DOMOutputElement elem)
        throws XMLStreamException {
        if (nsURI == null || nsURI.isEmpty()) {
            /* Attributes never use the default namespace; missing
             * prefix always leads to the empty ns... so nothing
             * special is needed here.
             */
            return null;
        }
        // Maybe the suggested prefix is properly bound?
        if (suggPrefix != null) {
            int status = elem.isPrefixValid(suggPrefix, nsURI, false);
            if (status == OutputElementBase.PREFIX_OK) {
                return suggPrefix;
            }
            /* Otherwise, if the prefix is unbound, let's just bind
             * it -- if caller specified a prefix, it probably prefers
             * binding that prefix even if another prefix already existed?
             * The remaining case (already bound to another URI) we don't
             * want to touch, at least not yet: it may or not be safe
             * to change binding, so let's just not try it.
             */
            if (status == OutputElementBase.PREFIX_UNBOUND) {
                elem.addPrefix(suggPrefix, nsURI);
                writeNamespace(suggPrefix, nsURI);
                return suggPrefix;
            }
        }

        // If not, perhaps there's another existing binding available?
        String prefix = elem.getExplicitPrefix(nsURI);
        if (prefix != null) { // already had a mapping for the URI... cool.
            return prefix;
        }

        /* Nope, need to create one. First, let's see if there's a
         * preference...
         */
        if (suggPrefix != null) {
            prefix = suggPrefix;
        } else if (_suggestedPrefixes != null) {
            prefix = _suggestedPrefixes.get(nsURI);
            // note: def ns is never added to suggested prefix map
        }

        if (prefix != null) {
            /* Can not use default namespace for attributes.
             * Also, re-binding is tricky for attributes; can't
             * re-bind anything that's bound on this scope... or
             * used in this scope. So, to simplify life, let's not
             * re-bind anything for attributes.
             */
            if (prefix.isEmpty() || (elem.getNamespaceURI(prefix) != null)) {
                prefix = null;
            }
        }

        if (prefix == null) {
            if (_autoNsSeq == null) {
                _autoNsSeq = new int[1];
                _autoNsSeq[0] = 1;
            }
            prefix = _currElem.generateMapping(_automaticNsPrefix, nsURI, _autoNsSeq);
        }

        // Ok; so far so good: let's now bind and output the namespace:
        elem.addPrefix(prefix, nsURI);
        writeNamespace(prefix, nsURI);
        return prefix;
    }
}
