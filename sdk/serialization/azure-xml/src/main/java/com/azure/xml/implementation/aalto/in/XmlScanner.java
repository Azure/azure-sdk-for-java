// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.in;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import com.azure.xml.implementation.stax2.XMLStreamLocation2;
import com.azure.xml.implementation.stax2.typed.Base64Variant;
import com.azure.xml.implementation.stax2.typed.TypedArrayDecoder;
import com.azure.xml.implementation.stax2.typed.TypedValueDecoder;
import com.azure.xml.implementation.stax2.typed.TypedXMLStreamException;
import com.azure.xml.implementation.stax2.ri.typed.CharArrayBase64Decoder;

import com.azure.xml.implementation.aalto.WFCException;
import com.azure.xml.implementation.aalto.impl.*;
import com.azure.xml.implementation.aalto.util.*;

/**
 * This is the abstract base class for all scanner implementations,
 * defining operations the actual parser requires from the low-level
 * scanners.
 * Scanners are encoding and input type (byte, char / stream, block)
 * specific, so there are many implementations.
 */
public abstract class XmlScanner implements XmlConsts, XMLStreamConstants, NamespaceContext {

    // // // Constants:

    /**
     * String that identifies CDATA section (after "&lt;![" prefix)
     */
    final protected String CDATA_STR = "CDATA[";

    /**
     * This token type signifies end-of-input, in cases where it can be
     * returned. In other cases, an exception may be thrown.
     */
    public final static int TOKEN_EOI = -1;

    /**
     * This constant defines the highest Unicode character allowed
     * in XML content.
     */
    protected final static int MAX_UNICODE_CHAR = 0x10FFFF;

    protected final static int INT_NULL = 0;
    protected final static int INT_CR = '\r';
    protected final static int INT_LF = '\n';
    protected final static int INT_TAB = '\t';
    protected final static int INT_SPACE = 0x0020;

    protected final static int INT_QMARK = '?';
    protected final static int INT_AMP = '&';
    protected final static int INT_LT = '<';
    protected final static int INT_GT = '>';
    protected final static int INT_QUOTE = '"';
    protected final static int INT_APOS = '\'';
    protected final static int INT_COLON = ':';
    protected final static int INT_SLASH = '/';
    protected final static int INT_EQ = '=';

    protected final static int INT_A = 'A';

    // // // Config for bound PName cache:

    /**
     * Let's activate cache quite soon, no need to wait for hundreds
     * of misses; just try to avoid cache construction if all we get
     * is soap envelope element or such.
     */
    private final static int BIND_MISSES_TO_ACTIVATE_CACHE = 10;

    /**
     * Size of the bind cache can be reasonably small, and should
     * still get high enough hit rate
     */
    private final static int BIND_CACHE_SIZE = 0x40;

    private final static int BIND_CACHE_MASK = 0x3F;

    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */

    protected final ReaderConfig _config;

    /**
     * Whether validity checks (wrt. name and text characters)
     * and normalization (linefeeds) is to be
     * done using xml 1.1 rules, or basic xml 1.0 rules. Default
     * is 1.0.
     */
    protected final boolean _xml11;

    protected final boolean _cfgCoalescing;

    /* Note: non-final since it may need to be disabled after
     * construction.
     */
    protected boolean _cfgLazyParsing;

    /*
    /**********************************************************************
    /* Tokenization state
    /**********************************************************************
     */

    protected int _currToken = START_DOCUMENT;

    protected boolean _tokenIncomplete = false;

    /**
     * Number of START_ELEMENT events returned for which no END_ELEMENT
     * has been returned; including current event.
     */
    protected int _depth = 0;

    /**
     * Textual content of the current event
     */
    protected final TextBuilder _textBuilder;

    /**
     * Flag set to indicate that an entity is pending
     */
    protected boolean _entityPending = false;

    /*
    /**********************************************************************
    /* Name/String handling
    /**********************************************************************
     */

    /**
     * Similarly, need a char buffer for actual String construction
     * (in future, could perhaps use StringBuilder?). It is used
     * for holding things like names (element, attribute), and
     * attribute values.
     */
    protected char[] _nameBuffer;

    /**
     * Current name associated with the token, if any. Name of the
     * current element, target of processing instruction, or name
     * of an unexpanded entity.
     */
    protected PName _tokenName = null;

    /*
    /**********************************************************************
    /* Element information
    /**********************************************************************
     */

    /**
     * Flag that is used if the current state is <code>START_ELEMENT</code>
     * or <code>END_ELEMENT</code>, to indicate if the underlying physical
     * tag is a so-called empty tag (one ending with "/&gt;")
     */
    protected boolean _isEmptyTag = false;

    /**
     * Information about the current element on the stack
     */
    protected ElementScope _currElem;

    /**
     * Public id of the current event (DTD), if any.
     */
    protected String _publicId;

    /**
     * System id of the current event (DTD), if any.
     */
    protected String _systemId;

    /*
    /**********************************************************************
    /* Namespace binding
    /**********************************************************************
     */

    /**
     * Pointer to the last namespace declaration encountered. Because of backwards
     * linking, it also serves as the head of the linked list of all active
     * namespace declarations starting from the most recent one.
     */
    protected NsDeclaration _lastNsDecl = null;

    /**
     * This is a temporary state variable, valid during START_ELEMENT
     * event. For those events, contains number of namespace declarations
     * available. For END_ELEMENT, this count is computed on the fly.
     */
    protected int _currNsCount = 0;

    /**
     * Default namespace binding is a per-document singleton, like
     * explicit bindings, and used for elements (never for attributes).
     */
    protected NsBinding _defaultNs = NsBinding.createDefaultNs();

    /**
     * Array containing all prefix bindings needed within the current
     * document, so far (if any). These bindings are not in a particular
     * order, and they specifically do NOT represent actual namespace
     * declarations parsed from xml content.
     */
    protected NsBinding[] _nsBindings;

    protected int _nsBindingCount = 0;

    /**
     * Although unbound pname instances can be easily and safely reused,
     * bound ones are per-document. However, it makes sense to try to
     * reuse them too; at least using a minimal static cache, activate
     * only after certain number of cache misses (to avoid overhead for
     * tiny documents, or documents with few or no namespace prefixes).
     */
    protected PName[] _nsBindingCache = null;

    protected int _nsBindMisses = 0;

    /*
    /**********************************************************************
    /* Support for non-transient NamespaceContext
    /**********************************************************************
     */

    /**
     * Last returned {@link NamespaceContext}, created for a call
     * to {@link #getNonTransientNamespaceContext}, iff this would
     * still be a valid context.
     */
    protected FixedNsContext _lastNsContext = FixedNsContext.EMPTY_CONTEXT;

    /*
    /**********************************************************************
    /* Attribute info
    /**********************************************************************
     */

    protected final AttributeCollector _attrCollector;

    protected int _attrCount = 0;

    /*
    /**********************************************************************
    /* Minimal location info for all impls
    /**********************************************************************
     */

    /**
     * Number of bytes that were read and processed before the contents
     * of the current buffer; used for calculating absolute offsets.
     */
    protected long _pastBytesOrChars;

    /**
     * The row on which the character to read next is on. Note that
     * it is 0-based, so API will generally add one to it before
     * returning the value
     */
    protected int _currRow;

    /**
     * Offset used to calculate the column value given current input
     * buffer pointer. May be negative, if the first character of the
     * row was contained within an earlier buffer.
     */
    protected int _rowStartOffset;

    /**
     * Offset (in chars or bytes) at start of current token
     */
    protected long _startRawOffset;

    /**
     * Current row at start of current (last returned) token
     */
    protected long _startRow = -1L;

    /**
     * Current column at start of current (last returned) token
     */
    protected long _startColumn = -1L;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    protected XmlScanner(ReaderConfig cfg) {
        _config = cfg;

        _cfgCoalescing = cfg.willCoalesceText();
        _cfgLazyParsing = cfg.willParseLazily();
        _xml11 = cfg.isXml11();
        _textBuilder = TextBuilder.createRecyclableBuffer(_config);
        _attrCollector = new AttributeCollector(cfg);
        _nameBuffer = cfg.allocSmallCBuffer(ReaderConfig.DEFAULT_SMALL_BUFFER_LEN);
        _currRow = 0;
    }

    /**
     * Method called at point when the parsing process has ended (either
     * by encountering end of the input, or via explicit close), and
     * buffers can and should be released.
     *
     * @param forceCloseSource True if the underlying input source is
     *   to be closed, independent of whether auto-close has been set
     *   to true via configuration (or if the scanner manages the input
     *   source)
     */
    public final void close(boolean forceCloseSource) throws XMLStreamException {
        _releaseBuffers();
        if (forceCloseSource || _config.willAutoCloseInput()) {
            try {
                _closeSource();
            } catch (IOException ioe) {
                throw new IoStreamException(ioe);
            }
        }
    }

    protected void _releaseBuffers() {
        _textBuilder.recycle(true);
        if (_nameBuffer != null) {
            char[] buf = _nameBuffer;
            _nameBuffer = null;
            _config.freeSmallCBuffer(buf);
        }
    }

    protected abstract void _closeSource() throws IOException;

    /*
    /**********************************************************************
    /* Package access methods, needed by SAX impl
    /**********************************************************************
     */

    public ReaderConfig getConfig() {
        return _config;
    }

    /*
    /**********************************************************************
    /* Public scanner interface, iterating
    /**********************************************************************
     */

    // // // First, main iteration methods

    public abstract int nextFromProlog(boolean isProlog) throws XMLStreamException;

    public abstract int nextFromTree() throws XMLStreamException;

    /**
     * This method is called to ensure that the current token/event has been
     * completely parsed, such that we have all the data needed to return
     * it (textual content, PI data, comment text etc)
     */
    protected abstract void finishToken() throws XMLStreamException;

    /**
     * This method is called to essentially skip remaining of the
     * current token (data of PI etc)
     *
     * @return True If by skipping we also figured out following event
     *   type (and assigned its type to _currToken); false if that remains
     *   to be done
     */
    protected final boolean skipToken() throws XMLStreamException {
        _tokenIncomplete = false;
        switch (_currToken) {
            case PROCESSING_INSTRUCTION:
                skipPI();
                break;

            case CHARACTERS:
                if (skipCharacters()) { // encountered an entity
                    // _tokenName already set, just need to set curr token
                    _currToken = ENTITY_REFERENCE;
                    return true;
                }
                if (_cfgCoalescing) {
                    if (skipCoalescedText()) { // encountered an entity
                        _currToken = ENTITY_REFERENCE;
                        return true;
                    }
                }
                break;

            case COMMENT:
                skipComment();
                break;

            case SPACE:
                skipSpace();
                break;

            case CDATA:
                skipCData();
                if (_cfgCoalescing) {
                    skipCoalescedText();
                    if (_entityPending) { // encountered an entity
                        _currToken = ENTITY_REFERENCE;
                        return true;
                    }
                }
                break;

            case DTD:
                finishDTD(false); // false -> skip subset text
                break;

            default:
                throw new Error(
                    "Internal error, unexpected incomplete token type " + ErrorConsts.tokenTypeDesc(_currToken));
        }
        return false;
    }

    /*
    /**********************************************************************
    /* Public scanner interface, location access
    /**********************************************************************
     */

    /**
     * @return Current input location
     */
    public abstract XMLStreamLocation2 getCurrentLocation();

    public final XMLStreamLocation2 getStartLocation() {
        // !!! TODO: deal with impedance wrt int/long (flaw in Stax API)
        int row = (int) _startRow;
        int col = (int) _startColumn;
        return LocationImpl.fromZeroBased(_config.getPublicId(), _config.getSystemId(), _startRawOffset, row, col);
    }

    /*
    /**********************************************************************
    /* Public scanner interface, other methods
    /**********************************************************************
     */

    public final boolean hasEmptyStack() {
        return (_depth == 0);
    }

    public final int getDepth() {
        return _depth;
    }

    public final boolean isEmptyTag() {
        return _isEmptyTag;
    }

    /*
    /**********************************************************************
    /* Data accessors, names:
    /**********************************************************************
     */

    public final PName getName() {
        return _tokenName;
    }

    public final QName getQName() {
        return _tokenName.constructQName(_defaultNs);
    }

    public final String getDTDPublicId() {
        return _publicId;
    }

    public final String getDTDSystemId() {
        return _systemId;
    }

    /*
    /**********************************************************************
    /* Data accessors, (element) text:
    /**********************************************************************
     */

    public final String getText() throws XMLStreamException {
        if (_tokenIncomplete) {
            finishToken();
        }
        return _textBuilder.contentsAsString();
    }

    public final int getTextLength() throws XMLStreamException {
        if (_tokenIncomplete) {
            finishToken();
        }
        return _textBuilder.size();
    }

    public final char[] getTextCharacters() throws XMLStreamException {
        if (_tokenIncomplete) {
            finishToken();
        }
        return _textBuilder.getTextBuffer();
    }

    public final int getTextCharacters(int srcStart, char[] target, int targetStart, int len)
        throws XMLStreamException {
        if (_tokenIncomplete) {
            finishToken();
        }
        return _textBuilder.contentsToArray(srcStart, target, targetStart, len);
    }

    public final int getText(Writer w) throws XMLStreamException {
        if (_tokenIncomplete) {
            finishToken();
        }
        /* !!! Preserve or not, we'll hold the contents in memory.
         *   Could be improved if necessary.
         */
        try {
            return _textBuilder.rawContentsTo(w);
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
        }
    }

    public final boolean isTextWhitespace() throws XMLStreamException {
        if (_tokenIncomplete) {
            finishToken();
        }
        return _textBuilder.isAllWhitespace();
    }

    /**
     * Method called by the stream reader to decode space-separated tokens
     * that are part of the current text event, using given decoder.
     *
     * @param reset If true, need to tell text buffer to reset its decoding
     *   state; if false, shouldn't
     */
    public final int decodeElements(TypedArrayDecoder tad, boolean reset) throws XMLStreamException {
        if (_tokenIncomplete) {
            finishToken();
        }

        try {
            return _textBuilder.decodeElements(tad, reset);
        } catch (TypedXMLStreamException tex) {
            // Need to add location?
            Location loc = getCurrentLocation();
            String lexical = tex.getLexical();
            IllegalArgumentException iae = (IllegalArgumentException) tex.getCause();
            throw new TypedXMLStreamException(lexical, tex.getMessage(), loc, iae);
        }
    }

    /**
     * Method called by the stream reader to reset given base64 decoder
     * with data from the current text event.
     */
    public final void resetForDecoding(Base64Variant v, CharArrayBase64Decoder dec, boolean firstChunk)
        throws XMLStreamException {
        if (_tokenIncomplete) {
            finishToken();
        }
        _textBuilder.resetForBinaryDecode(v, dec, firstChunk);
    }

    /*
    /**********************************************************************
    /* Data accessors, firing SAX events
    /**********************************************************************
     */

    /*
    /**********************************************************************
    /* Data accessors, attributes:
    /**********************************************************************
     */

    public final int getAttrCount() {
        return _attrCount;
    }

    public final String getAttrLocalName(int index) {
        // Note: caller checks indices:
        return _attrCollector.getName(index).getLocalName();
    }

    public final QName getAttrQName(int index) {
        // Note: caller checks indices:
        return _attrCollector.getQName(index);
    }

    public final String getAttrNsURI(int index) {
        // Note: caller checks indices:
        return _attrCollector.getName(index).getNsUri();
    }

    public final String getAttrPrefix(int index) {
        // Note: caller checks indices:
        return _attrCollector.getName(index).getPrefix();
    }

    public final String getAttrValue(int index) {
        // Note: caller checks indices
        return _attrCollector.getValue(index);
    }

    public final String getAttrValue(String nsURI, String localName) {
        /* Collector may not be reset if there are no attributes,
         * need to check if any could be found first:
         */
        if (_attrCount < 1) {
            return null;
        }
        return _attrCollector.getValue(nsURI, localName);
    }

    public final void decodeAttrValue(int index, TypedValueDecoder tvd) {
        _attrCollector.decodeValue(index, tvd);
    }

    /**
     * Method called to decode the attribute value that consists of
     * zero or more space-separated tokens.
     * Decoding is done using the decoder provided.
     * @return Number of tokens decoded
     */
    public final int decodeAttrValues(int index, TypedArrayDecoder tad) throws XMLStreamException {
        return _attrCollector.decodeValues(index, tad, this);
    }

    public final byte[] decodeAttrBinaryValue(int index, Base64Variant v, CharArrayBase64Decoder dec)
        throws XMLStreamException {
        return _attrCollector.decodeBinaryValue(index, v, dec, this);
    }

    public final int findAttrIndex(String nsURI, String localName) {
        /* Collector may not be reset if there are no attributes,
         * need to check if any could be found first:
         */
        if (_attrCount < 1) {
            return -1;
        }
        return _attrCollector.findIndex(nsURI, localName);
    }

    public final String getAttrType() {
        // Note: caller checks indices:
        // !!! TBI
        return "CDATA";
    }

    public final boolean isAttrSpecified() {
        // !!! TBI
        // (for now works ok as we don't handle DTD info, no attr value defaults)
        return true;
    }

    /*
    /**********************************************************************
    /* Data accessors, namespace declarations:
    /**********************************************************************
     */

    public final int getNsCount() {
        if (_currToken == START_ELEMENT) {
            return _currNsCount;
        }
        return (_lastNsDecl == null) ? 0 : _lastNsDecl.countDeclsOnLevel(_depth);
    }

    public final String getNamespacePrefix(int index) {
        return findCurrNsDecl(index).getBinding().mPrefix;
    }

    public final String getNamespaceURI(int index) {
        return findCurrNsDecl(index).getBinding().mURI;
    }

    private NsDeclaration findCurrNsDecl(int index) {
        NsDeclaration nsDecl = _lastNsDecl;
        /* 17-Sep-2006, tatu: There is disparity between START/END_ELEMENT;
         *   with START_ELEMENT, _depth is one higher than that of ns
         *   declarations; with END_ELEMENT, the same
         */
        int level = _depth;
        int count;
        // 20-Jan-2011, tatu: Hmmh... since declarations are in reverse order should we reorder?
        if (_currToken == START_ELEMENT) {
            count = _currNsCount - 1 - index;
            --level;
        } else {
            count = index;
        }

        while (nsDecl != null && nsDecl.getLevel() == level) {
            if (count == 0) {
                return nsDecl;
            }
            --count;
            nsDecl = nsDecl.getPrev();
        }
        reportInvalidNsIndex(index);
        return null; // never gets here
    }

    // Part of NamespaceContext impl below
    //public final String getNsUri(String prefix)

    public final String getNamespaceURI() {
        String uri = _tokenName.getNsUri();
        // Null means it uses the default ns:
        return (uri == null) ? _defaultNs.mURI : uri;
    }

    public final NamespaceContext getNonTransientNamespaceContext() {
        _lastNsContext = _lastNsContext.reuseOrCreate(_lastNsDecl);
        return _lastNsContext;
    }

    /*
    /**********************************************************************
    /* NamespaceContext implementation
    /**********************************************************************
     */

    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException(ErrorConsts.ERR_NULL_ARG);
        }
        if (prefix.isEmpty()) { // default namespace?
            // Need to check if it's null, too, to convert
            String uri = _defaultNs.mURI;
            return (uri == null) ? "" : uri;
        }
        // xml, xmlns?
        if (prefix.equals(XMLConstants.XML_NS_PREFIX)) {
            return XMLConstants.XML_NS_URI;
        }
        if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        }
        // Nope, a specific other prefix
        NsDeclaration nsDecl = _lastNsDecl;
        while (nsDecl != null) {
            if (nsDecl.hasPrefix(prefix)) {
                return nsDecl.getCurrNsURI();
            }
            nsDecl = nsDecl.getPrev();
        }
        return null;
    }

    @Override
    public String getPrefix(String nsURI) {
        /* As per JDK 1.5 JavaDocs, null is illegal; but no mention
         * about empty String (""). But that should
         */
        if (nsURI == null) {
            throw new IllegalArgumentException(ErrorConsts.ERR_NULL_ARG);
        }
        if (nsURI.equals(XMLConstants.XML_NS_URI)) {
            return XMLConstants.XML_NS_PREFIX;
        }
        if (nsURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
            return XMLConstants.XMLNS_ATTRIBUTE;
        }
        // First: does the default namespace bind to the URI?
        if (nsURI.equals(_defaultNs.mURI)) {
            return "";
        }
        /* Need to loop twice; first find a prefix, then ensure it's
         * not masked by a later declaration
         */
        main_loop: for (NsDeclaration nsDecl = _lastNsDecl; nsDecl != null; nsDecl = nsDecl.getPrev()) {
            if (nsDecl.hasNsURI(nsURI)) {
                // Ok: but is prefix masked?
                String prefix = nsDecl.getPrefix();
                // Plus, default ns wouldn't do (since current one was already checked)
                if (prefix != null) {
                    for (NsDeclaration decl2 = _lastNsDecl; decl2 != nsDecl; decl2 = decl2.getPrev()) {
                        if (decl2.hasPrefix(prefix)) {
                            continue main_loop;
                        }
                    }
                    return prefix;
                }
            }
        }
        return null;
    }

    @Override
    public Iterator<String> getPrefixes(String nsURI) {
        if (nsURI == null) {
            throw new IllegalArgumentException(ErrorConsts.ERR_NULL_ARG);
        }
        if (nsURI.equals(XMLConstants.XML_NS_URI)) {
            return Collections.singletonList(XMLConstants.XML_NS_PREFIX).iterator();
        }
        if (nsURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
            return Collections.singletonList(XMLConstants.XMLNS_ATTRIBUTE).iterator();
        }
        ArrayList<String> l = null;

        // First, the default ns?
        if (nsURI.equals(_defaultNs.mURI)) {
            l = new ArrayList<>();
            l.add("");
        }

        main_loop: for (NsDeclaration nsDecl = _lastNsDecl; nsDecl != null; nsDecl = nsDecl.getPrev()) {
            if (nsDecl.hasNsURI(nsURI)) {
                // Ok: but is prefix masked?
                String prefix = nsDecl.getPrefix();
                // Plus, default ns wouldn't do (since current one was already checked)
                if (prefix != null) {
                    for (NsDeclaration decl2 = _lastNsDecl; decl2 != nsDecl; decl2 = decl2.getPrev()) {
                        if (decl2.hasPrefix(prefix)) {
                            continue main_loop;
                        }
                    }
                    if (l == null) {
                        l = new ArrayList<>();
                    }
                    l.add(prefix);
                }
            }
        }

        if (l == null) {
            return Collections.emptyIterator();
        }
        if (l.size() == 1) {
            return Collections.singletonList(l.get(0)).iterator();
        }
        return l.iterator();
    }

    /*
    /**********************************************************************
    /* Abstract methods for sub-classes to implement
    /**********************************************************************
     */

    // // token-finish methods

    protected abstract void finishCharacters() throws XMLStreamException;

    protected abstract void finishCData() throws XMLStreamException;

    protected abstract void finishComment() throws XMLStreamException;

    protected abstract void finishDTD(boolean copyContents) throws XMLStreamException;

    protected abstract void finishPI() throws XMLStreamException;

    protected abstract void finishSpace() throws XMLStreamException;

    // // token-skip methods

    /**
     * @return True, if an unexpanded entity was encountered (and
     *   is now pending)
     */
    protected abstract boolean skipCharacters() throws XMLStreamException;

    protected abstract void skipCData() throws XMLStreamException;

    protected abstract void skipComment() throws XMLStreamException;

    protected abstract void skipPI() throws XMLStreamException;

    protected abstract void skipSpace() throws XMLStreamException;

    /**
     * Secondary skip method called after primary text segment
     * has been skipped, and we are in coalescing mode.
     *
     * @return True, if an unexpanded entity was encountered (and
     *   is now pending)
     */
    protected abstract boolean skipCoalescedText() throws XMLStreamException;

    // // Raw input access:

    protected abstract boolean loadMore() throws XMLStreamException;

    /*
    /**********************************************************************
    /* Basic namespace binding methods
    /**********************************************************************
     */

    /**
     * This method is called to find/create a fully qualified (bound)
     * name (element / attribute), for a name with prefix. For non-prefixed
     * names this method will not get called
     */
    protected final PName bindName(PName name, String prefix) {
        // First, do we have a cache, to perhaps find bound name from?
        if (_nsBindingCache != null) {
            PName cn = _nsBindingCache[name.unboundHashCode() & BIND_CACHE_MASK];
            if (cn != null && cn.unboundEquals(name)) {
                return cn;
            }
        }

        // If no cache, or not found there, need to first find binding
        for (int i = 0, len = _nsBindingCount; i < len; ++i) {
            NsBinding b = _nsBindings[i];
            if (!Objects.equals(b.mPrefix, prefix)) { // prefixes are canonicalized
                continue;
            }
            // Ok, match!
            // Can we bubble prefix closer to the head?
            if (i > 0) {
                _nsBindings[i] = _nsBindings[i - 1];
                _nsBindings[i - 1] = b;
            }
            // Plus, should we cache it?
            PName bn = name.createBoundName(b);
            if (_nsBindingCache == null) {
                if (++_nsBindMisses < BIND_MISSES_TO_ACTIVATE_CACHE) {
                    return bn;
                }
                _nsBindingCache = new PName[BIND_CACHE_SIZE];
            }
            _nsBindingCache[bn.unboundHashCode() & BIND_CACHE_MASK] = bn;
            return bn;
        }

        // If not even binding, need to create that first

        // No match; perhaps "xml"? But is "xmlns" legal to use too?
        if (Objects.equals(prefix, "xml")) {
            return name.createBoundName(NsBinding.XML_BINDING);
        }
        /* Nope. Need to create a new binding. For such entries, let's
         * not try caching, yet, but let's note it as a miss
         */
        ++_nsBindMisses;
        return name.createBoundName(createNewBinding(prefix));
    }

    /**
     * Method called when a namespace declaration needs to find the
     * binding object (essentially a per-prefix-per-document canonical
     * container object)
     */
    protected final NsBinding findOrCreateBinding(String prefix) {
        // !!! TODO: switch to hash at size N?

        // TEST only (for ns-soap.xml):
        //int MAX = (_nsBindingCount > 8) ? 8 : _nsBindingCount;
        //for (int i = 0; i < MAX; ++i) {

        for (int i = 0, len = _nsBindingCount; i < len; ++i) {
            NsBinding b = _nsBindings[i];
            if (Objects.equals(b.mPrefix, prefix)) { // prefixes are interned
                if (i > 0) { // let's do bubble it up a notch... can speed things up
                    _nsBindings[i] = _nsBindings[i - 1];
                    _nsBindings[i - 1] = b;
                }
                return b;
            }
        }

        if (Objects.equals(prefix, "xml")) {
            return NsBinding.XML_BINDING;
        }
        if (Objects.equals(prefix, "xmlns")) {
            return NsBinding.XMLNS_BINDING;
        }
        // Nope. Need to create a new binding
        return createNewBinding(prefix);
    }

    private NsBinding createNewBinding(String prefix) {
        NsBinding b = new NsBinding(prefix);
        if (_nsBindingCount == 0) {
            _nsBindings = new NsBinding[16];
        } else if (_nsBindingCount >= _nsBindings.length) {
            _nsBindings = (NsBinding[]) DataUtil.growAnyArrayBy(_nsBindings, _nsBindings.length);
        }
        _nsBindings[_nsBindingCount] = b;
        ++_nsBindingCount;
        return b;
    }

    /**
     * Method called when we are ready to bind a declared namespace.
     */
    protected final void bindNs(PName name, String uri) throws XMLStreamException {
        NsBinding ns;
        String prefix = name.getPrefix();

        if (prefix == null) { // default ns
            ns = _defaultNs;
        } else {
            prefix = name.getLocalName();
            ns = findOrCreateBinding(prefix);
            if (ns.isImmutable()) { // xml, xmlns
                checkImmutableBinding(prefix, uri);
            }
        }

        /* 28-Oct-2006, tatus: Also need to ensure that neither
         *   xml nor xmlns-bound namespaces are bound to any
         *   other prefixes. Since we know that URIs are intern()ed,
         *   can just do identity comparison
         */
        if (!ns.isImmutable()) {
            if (Objects.equals(uri, XMLConstants.XML_NS_URI)) {
                reportIllegalNsDecl("xml", XMLConstants.XML_NS_URI);
            } else if (Objects.equals(uri, XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
                reportIllegalNsDecl("xmlns", XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
            }
        }
        // Already declared in current scope?
        if (_lastNsDecl != null && _lastNsDecl.alreadyDeclared(prefix, _depth)) {
            reportDuplicateNsDecl(prefix);
        }
        _lastNsDecl = new NsDeclaration(ns, uri, _lastNsDecl, _depth);
    }

    /**
     * Method called when an immutable ns prefix (xml, xmlns) is
     * encountered.
     */
    protected final void checkImmutableBinding(String prefix, String uri) throws XMLStreamException {
        if (!Objects.equals(prefix, "xml") || !uri.equals(XMLConstants.XML_NS_URI)) {
            reportIllegalNsDecl(prefix);
        }
    }

    /*
    /**********************************************************************
    /* Helper methods for sub-classes, input data
    /**********************************************************************
     */

    /**
     * Method that tries to load at least one more byte into buffer;
     * and if that fails, throws an appropriate EOI exception.
     */
    protected final void loadMoreGuaranteed() throws XMLStreamException {
        if (!loadMore()) {
            reportInputProblem("Unexpected end-of-input when trying to parse " + ErrorConsts.tokenTypeDesc(_currToken));
        }
    }

    protected final void loadMoreGuaranteed(int tt) throws XMLStreamException {
        if (!loadMore()) {
            reportInputProblem("Unexpected end-of-input when trying to parse " + ErrorConsts.tokenTypeDesc(tt));
        }
    }

    /*
    /**********************************************************************
    /* Helper methods for sub-classes, character validity checks
    /**********************************************************************
     */

    protected final void verifyXmlChar(int value) throws XMLStreamException {
        // Ok, and then need to check result is a valid XML content char:
        if (value >= 0xD800) { // note: checked for overflow earlier
            if (value < 0xE000) { // no surrogates via entity expansion
                reportInvalidXmlChar(value);
            }
            if (value == 0xFFFE || value == 0xFFFF) {
                reportInvalidXmlChar(value);
            }
        } else if (value < 32) {
            // XML 1.1 allows most other chars; 1.0 does not:
            if (value != INT_LF && value != INT_CR && value != INT_TAB) {
                if (!_xml11 || value == 0) {
                    reportInvalidXmlChar(value);
                }
            }
        }
    }

    /*
    /**********************************************************************
    /* Helper methods for sub-classes, error reporting
    /**********************************************************************
     */

    protected void reportInputProblem(String msg) throws XMLStreamException {
        /* 29-Mar-2008, tatus: Not sure if these are all Well-Formedness
         *   Constraint (WFC) violations? They should be... ?
         */
        throw new WFCException(msg, getCurrentLocation());
    }

    /**
     * Method called when a call to expand an entity within attribute
     * value fails to expand it.
     */
    protected void reportUnexpandedEntityInAttr(boolean isNsDecl) throws XMLStreamException {
        reportInputProblem("Unexpanded ENTITY_REFERENCE (" + _tokenName + ") in "
            + (isNsDecl ? "namespace declaration" : "attribute value"));
    }

    protected void reportPrologUnexpElement(boolean isProlog, int ch) throws XMLStreamException {
        if (ch < 0) { // just to be safe, in case caller passed signed byte
            ch &= 0x7FFFF;
        }
        if (ch == '/') { // end element
            if (isProlog) {
                reportInputProblem("Unexpected end element in prolog: malformed XML document, expected root element");
            }
            reportInputProblem("Unexpected end element in epilog: malformed XML document (unbalanced start/end tags?)");
        }

        // Otherwise, likely start element. But check for invalid white space for funsies
        if (ch < 32) {
            String type = isProlog ? ErrorConsts.SUFFIX_IN_PROLOG : ErrorConsts.SUFFIX_IN_EPILOG;
            throwUnexpectedChar(ch, "Unrecognized directive " + type);
        }
        reportInputProblem("Second root element in content: malformed XML document, only one allowed");
    }

    protected void reportPrologUnexpChar(boolean isProlog, int ch, String msg) throws XMLStreamException {
        String fullMsg = isProlog ? ErrorConsts.SUFFIX_IN_PROLOG : ErrorConsts.SUFFIX_IN_EPILOG;
        if (msg == null) {
            if (ch == '&') {
                throwUnexpectedChar(ch, fullMsg + "; no entities allowed");
            }
        } else {
            fullMsg += msg;
        }
        throwUnexpectedChar(ch, fullMsg);
    }

    protected void reportTreeUnexpChar(int ch, String msg) throws XMLStreamException {
        String fullMsg = ErrorConsts.SUFFIX_IN_TREE;
        if (msg != null) {
            fullMsg += msg;
        }
        throwUnexpectedChar(ch, fullMsg);
    }

    protected void reportInvalidNameChar(int ch, int index) throws XMLStreamException {
        if (ch == INT_COLON) {
            reportInputProblem(
                "Invalid colon in name: at most one colon allowed in element/attribute names, and none in PI target or entity names");
        }
        if (index == 0) {
            reportInputProblem("Invalid name start character (0x" + Integer.toHexString(ch) + ")");
        }
        reportInputProblem("Invalid name character (0x" + Integer.toHexString(ch) + ")");
    }

    protected void reportInvalidXmlChar(int ch) throws XMLStreamException {
        if (ch == 0) {
            reportInputProblem("Invalid null character");
        }
        if (ch < 32) {
            reportInputProblem("Invalid white space character (0x" + Integer.toHexString(ch) + ")");
        }
        reportInputProblem("Invalid xml content character (0x" + Integer.toHexString(ch) + ")");
    }

    protected void reportEofInName() throws XMLStreamException {
        reportInputProblem("Unexpected end-of-input in name (parsing " + ErrorConsts.tokenTypeDesc(_currToken) + ")");
    }

    /**
     * Called when there's an unexpected char after PI target (non-ws,
     * not part of {@code '?>'} end marker
     */
    protected void reportMissingPISpace(int ch) throws XMLStreamException {
        throwUnexpectedChar(ch, ": expected either white space, or closing '?>'");
    }

    protected void reportDoubleHyphenInComments() throws XMLStreamException {
        reportInputProblem("String '--' not allowed in comment (missing '>'?)");
    }

    protected void reportMultipleColonsInName() throws XMLStreamException {
        reportInputProblem("Multiple colons not allowed in names");
    }

    protected void reportEntityOverflow() throws XMLStreamException {
        reportInputProblem("Illegal character entity: value higher than max allowed (0x"
            + Integer.toHexString(MAX_UNICODE_CHAR) + ")");
    }

    protected void reportInvalidNsIndex(int index) {
        /* 24-Jun-2006, tatus: Stax API doesn't specify what (if anything)
         *   should be thrown. Ref. Impl. throws IndexOutOfBounds, which
         *   makes sense; could also throw IllegalArgumentException.
         */
        throw new IndexOutOfBoundsException("Illegal namespace declaration index, " + index
            + ", current START_ELEMENT/END_ELEMENT has " + getNsCount() + " declarations");
    }

    protected void reportUnboundPrefix(PName name, boolean isAttr) throws XMLStreamException {
        reportInputProblem("Unbound namespace prefix '" + name.getPrefix() + "' (for "
            + (isAttr ? "attribute" : "element") + " name '" + name.getPrefixedName() + "')");
    }

    protected void reportDuplicateNsDecl(String prefix) throws XMLStreamException {
        if (prefix == null) {
            reportInputProblem("Duplicate namespace declaration for the default namespace");
        } else {
            reportInputProblem("Duplicate namespace declaration for prefix '" + prefix + "'");
        }
    }

    protected void reportIllegalNsDecl(String prefix) throws XMLStreamException {
        reportInputProblem("Illegal namespace declaration: can not re-bind prefix '" + prefix + "'");
    }

    protected void reportIllegalNsDecl(String prefix, String uri) throws XMLStreamException {
        reportInputProblem(
            "Illegal namespace declaration: can not bind URI '" + uri + "' to prefix other than '" + prefix + "'");
    }

    protected void reportUnexpectedEndTag(String expName) throws XMLStreamException {
        reportInputProblem("Unexpected end tag: expected </" + expName + ">");
    }

    // Thrown when ']]>' found in text content
    protected void reportIllegalCDataEnd() throws XMLStreamException {
        reportInputProblem("String ']]>' not allowed in textual content, except as the end marker of CDATA section");
    }

    protected void throwUnexpectedChar(int i, String msg) throws XMLStreamException {
        // But first, let's check illegals
        if (i < 32 && i != '\r' && i != '\n' && i != '\t') {
            throwInvalidSpace(i);
        }
        char c = (char) i;
        String excMsg = "Unexpected character " + XmlChars.getCharDesc(c) + msg;
        reportInputProblem(excMsg);
    }

    protected void throwNullChar() throws XMLStreamException {
        reportInputProblem("Illegal character (NULL, unicode 0) encountered: not valid in any content");
    }

    protected char handleInvalidXmlChar(int i) throws XMLStreamException {
        final IllegalCharHandler iHandler = _config.getIllegalCharHandler();

        if (iHandler != null) {
            return iHandler.convertIllegalChar(i);
        }

        char c = (char) i;
        if (c == CHAR_NULL) {
            throwNullChar();
        }

        String msg = "Illegal XML character (" + XmlChars.getCharDesc(c) + ")";
        if (_xml11) {
            if (i < INT_SPACE) {
                msg += " [note: in XML 1.1, it could be included via entity expansion]";
            }
        }
        reportInputProblem(msg);

        //will not reach this block
        return (char) i;
    }

    protected void throwInvalidSpace(int i) throws XMLStreamException {
        char c = (char) i;
        if (c == CHAR_NULL) {
            throwNullChar();
        }
        String msg = "Illegal character (" + XmlChars.getCharDesc(c) + ")";
        if (_xml11) {
            if (i < INT_SPACE) {
                msg += " [note: in XML 1.1, it could be included via entity expansion]";
            }
        }
        reportInputProblem(msg);
    }
}
