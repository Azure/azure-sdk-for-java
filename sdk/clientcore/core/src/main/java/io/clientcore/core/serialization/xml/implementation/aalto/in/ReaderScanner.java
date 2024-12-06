// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package io.clientcore.core.serialization.xml.implementation.aalto.in;

import io.clientcore.core.serialization.xml.implementation.aalto.impl.ErrorConsts;
import io.clientcore.core.serialization.xml.implementation.aalto.impl.LocationImpl;
import io.clientcore.core.serialization.xml.implementation.aalto.impl.StreamExceptionBase;
import io.clientcore.core.serialization.xml.implementation.aalto.util.DataUtil;
import io.clientcore.core.serialization.xml.implementation.aalto.util.TextBuilder;
import io.clientcore.core.serialization.xml.implementation.aalto.util.XmlCharTypes;
import io.clientcore.core.serialization.xml.implementation.aalto.util.XmlChars;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

import static io.clientcore.core.serialization.xml.implementation.aalto.util.XmlConsts.CHAR_LF;
import static io.clientcore.core.serialization.xml.implementation.aalto.util.XmlConsts.CHAR_NULL;
import static io.clientcore.core.serialization.xml.implementation.aalto.util.XmlConsts.CHAR_SPACE;

/**
 * This is the concrete scanner implementation used when input comes
 * as a {@link Reader}. In general using this scanner is quite
 * a bit less optimal than that of {@link InputStream} based
 * scanner. Nonetheless, it is included for completeness, since Stax
 * interface allows passing Readers as input sources.
 */
@SuppressWarnings("fallthrough")
public final class ReaderScanner implements XMLStreamConstants, NamespaceContext {
    // // // Constants:

    /**
     * String that identifies CDATA section (after "&lt;![" prefix)
     */
    private final String CDATA_STR = "CDATA[";

    /**
     * This token type signifies end-of-input, in cases where it can be
     * returned. In other cases, an exception may be thrown.
     */
    public final static int TOKEN_EOI = -1;

    /**
     * This constant defines the highest Unicode character allowed
     * in XML content.
     */
    private final static int MAX_UNICODE_CHAR = 0x10FFFF;

    private final static int INT_NULL = 0;
    private final static int INT_CR = '\r';
    private final static int INT_LF = '\n';
    private final static int INT_TAB = '\t';
    private final static int INT_SPACE = 0x0020;

    private final static int INT_QMARK = '?';
    private final static int INT_AMP = '&';
    private final static int INT_LT = '<';
    private final static int INT_GT = '>';
    private final static int INT_QUOTE = '"';
    private final static int INT_APOS = '\'';
    private final static int INT_COLON = ':';
    private final static int INT_SLASH = '/';

    private final static int INT_A = 'A';

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

    /**
     * Although java chars are basically UTF-16 in memory, the closest
     * match for char types is Latin1.
     */
    private final static XmlCharTypes sCharTypes = InputCharTypes.getLatin1CharTypes();

    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */

    /**
     * Underlying InputStream to use for reading content.
     */
    private final Reader _in;
    private final ReaderConfig _config;
    private final boolean _cfgCoalescing;

    /*
    /**********************************************************************
    /* Tokenization state
    /**********************************************************************
     */

    private int _currToken = START_DOCUMENT;

    private boolean _tokenIncomplete = false;

    /**
     * Number of START_ELEMENT events returned for which no END_ELEMENT
     * has been returned; including current event.
     */
    private int _depth = 0;

    /**
     * Textual content of the current event
     */
    private final TextBuilder _textBuilder;

    /**
     * Flag set to indicate that an entity is pending
     */
    private boolean _entityPending = false;

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
    private char[] _nameBuffer;

    /**
     * Current name associated with the token, if any. Name of the
     * current element, target of processing instruction, or name
     * of an unexpanded entity.
     */
    private PNameC _tokenName = null;

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
    private boolean _isEmptyTag = false;

    /**
     * Information about the current element on the stack
     */
    private ElementScope _currElem;

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
    private NsDeclaration _lastNsDecl = null;

    /**
     * This is a temporary state variable, valid during START_ELEMENT
     * event. For those events, contains number of namespace declarations
     * available. For END_ELEMENT, this count is computed on the fly.
     */
    private int _currNsCount = 0;

    /**
     * Default namespace binding is a per-document singleton, like
     * explicit bindings, and used for elements (never for attributes).
     */
    private final NsBinding _defaultNs = NsBinding.createDefaultNs();

    /**
     * Array containing all prefix bindings needed within the current
     * document, so far (if any). These bindings are not in a particular
     * order, and they specifically do NOT represent actual namespace
     * declarations parsed from xml content.
     */
    private NsBinding[] _nsBindings;

    private int _nsBindingCount = 0;

    /**
     * Although unbound pname instances can be easily and safely reused,
     * bound ones are per-document. However, it makes sense to try to
     * reuse them too; at least using a minimal static cache, activate
     * only after certain number of cache misses (to avoid overhead for
     * tiny documents, or documents with few or no namespace prefixes).
     */
    private PNameC[] _nsBindingCache = null;

    private int _nsBindMisses = 0;

    /*
    /**********************************************************************
    /* Attribute info
    /**********************************************************************
     */

    private final AttributeCollector _attrCollector;

    private int _attrCount = 0;

    /*
    /**********************************************************************
    /* Minimal location info for all impls
    /**********************************************************************
     */

    /**
     * Number of bytes that were read and processed before the contents
     * of the current buffer; used for calculating absolute offsets.
     */
    private long _pastBytesOrChars;

    /**
     * The row on which the character to read next is on. Note that
     * it is 0-based, so API will generally add one to it before
     * returning the value
     */
    private int _currRow;

    /**
     * Offset used to calculate the column value given current input
     * buffer pointer. May be negative, if the first character of the
     * row was contained within an earlier buffer.
     */
    private int _rowStartOffset;

    /**
     * Offset (in chars or bytes) at start of current token
     */
    private long _startRawOffset;

    /**
     * Current row at start of current (last returned) token
     */
    private long _startRow = -1L;

    /**
     * Current column at start of current (last returned) token
     */
    private long _startColumn = -1L;

    /*
    /**********************************************************************
    /* Input buffering
    /**********************************************************************
     */

    private final char[] _inputBuffer;

    private int _inputPtr;

    private int _inputEnd;

    /**
     * Storage location for a single character that can not be pushed
     * back (for example, multi-byte char)
     */
    private int mTmpChar = INT_NULL;

    /*
    /**********************************************************************
    /* Symbol handling
    /**********************************************************************
    */

    /**
     * For now, symbol table contains prefixed names. In future it is
     * possible that they may be split into prefixes and local names?
     */
    private final CharBasedPNameTable _symbols;

    public ReaderScanner(ReaderConfig cfg, Reader r, char[] buffer, int ptr, int last) {
        _config = cfg;

        _cfgCoalescing = cfg.willCoalesceText();
        _textBuilder = TextBuilder.createRecyclableBuffer(_config);
        _attrCollector = new AttributeCollector();
        _nameBuffer = new char[ReaderConfig.DEFAULT_SMALL_BUFFER_LEN];
        _currRow = 0;

        _in = r;
        _inputBuffer = buffer;
        _inputPtr = ptr;
        _inputEnd = last;
        _pastBytesOrChars = 0; // should it be passed by caller?
        _rowStartOffset = 0; // should probably be passed by caller...

        _symbols = cfg.getCBSymbols();
    }

    /**
     * Method called at point when the parsing process has ended (either
     * by encountering end of the input, or via explicit close), and
     * buffers can and should be released.
     *
     */
    public void close() throws XMLStreamException {
        _releaseBuffers();
    }

    private void _releaseBuffers() {
        _textBuilder.recycle(true);
        if (_symbols.maybeDirty()) {
            _config.updateCBSymbols(_symbols);
        }
    }

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

    public int nextFromProlog(boolean isProlog) throws XMLStreamException {
        if (_tokenIncomplete) { // left-overs from last thingy?
            skipToken();
        }

        // First: keep track of where event started
        setStartLocation();

        // Ok: we should get a WS or '<'. So, let's skip through WS
        while (true) {
            // Any more data? Just need a single byte
            if (_inputPtr >= _inputEnd) {
                if (!loadMore()) {
                    setStartLocation();
                    return TOKEN_EOI;
                }
            }
            int c = _inputBuffer[_inputPtr++] & 0xFF;

            // Really should get white space or '<'...
            if (c == '<') {
                break;
            }
            if (c != ' ') {
                if (c == '\n') {
                    markLF();
                } else if (c == '\r') {
                    if (_inputPtr >= _inputEnd) {
                        if (!loadMore()) {
                            markLF();
                            setStartLocation();
                            return TOKEN_EOI;
                        }
                    }
                    if (_inputBuffer[_inputPtr] == '\n') {
                        ++_inputPtr;
                    }
                    markLF();
                } else if (c != '\t') {
                    reportPrologUnexpChar(isProlog, c, null);
                }
            }
        }

        // Ok, got LT:
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed(COMMENT);
        }
        char c = _inputBuffer[_inputPtr++];
        if (c == '!') { // comment/DOCTYPE? (CDATA not legal)
            return handlePrologDeclStart(isProlog);
        }
        if (c == '?') {
            return handlePIStart();
        }
        /* End tag not allowed if no open tree; and only one root
         * element (one root-level start tag)
         */
        if (c == '/' || !isProlog) {
            reportPrologUnexpElement(isProlog, c);
        }
        return handleStartElement(c);
    }

    public int nextFromTree() throws XMLStreamException {
        if (_tokenIncomplete) { // left-overs?
            if (skipToken()) { // Figured out next event (ENTITY_REFERENCE)?
                // !!! We don't yet parse DTD, don't know real contents
                return _nextEntity();
            }
        } else { // note: START_ELEMENT/END_ELEMENT never incomplete
            if (_currToken == START_ELEMENT) {
                if (_isEmptyTag) {
                    // Important: retain same start location as with START_ELEMENT, don't overwrite
                    --_depth;
                    return (_currToken = END_ELEMENT);
                }
            } else if (_currToken == END_ELEMENT) {
                _currElem = _currElem.getParent();
                // Any namespace declarations that need to be unbound?
                while (_lastNsDecl != null && _lastNsDecl.getLevel() >= _depth) {
                    _lastNsDecl = _lastNsDecl.unbind();
                }
            } else {
                // It's possible CHARACTERS entity with an entity ref:
                if (_entityPending) {
                    _entityPending = false;
                    return _nextEntity();
                }
            }
        }
        // and except for special cases, mark down actual start location of the event
        setStartLocation();

        /* Any more data? Although it'd be an error not to get any,
         * let's leave error reporting up to caller
         */
        if (_inputPtr >= _inputEnd) {
            if (!loadMore()) {
                setStartLocation();
                return TOKEN_EOI;
            }
        }
        char c = _inputBuffer[_inputPtr];

        /* Can get pretty much any type; start/end element, comment/PI,
         * CDATA, text, entity reference...
         */
        if (c == '<') { // root element, comment, proc instr?
            ++_inputPtr;
            c = (_inputPtr < _inputEnd) ? _inputBuffer[_inputPtr++] : loadOne();
            if (c == '!') { // comment or CDATA
                return handleCommentOrCdataStart();
            }
            if (c == '?') {
                return handlePIStart();
            }
            if (c == '/') {
                return handleEndElement();
            }
            return handleStartElement(c);
        }
        if (c == '&') { // entity reference
            ++_inputPtr;
            /* Need to expand; should indicate either text, or an unexpanded
             * entity reference
             */
            int i = handleEntityInText();
            if (i == 0) { // general entity
                return (_currToken = ENTITY_REFERENCE);
            }
            /* Nope, a char entity; need to indicate it came from an entity.
             * Since we may want to store the char as is, too, let's negate
             * entity-based char
             */
            mTmpChar = -i;
        } else {
            /* Let's store it for future reference. May or may not be used --
             * so let's not advance input ptr quite yet.
             */
            mTmpChar = c;
        }
        // text, possibly/probably ok
        _tokenIncomplete = true;
        return (_currToken = CHARACTERS);
    }

    /**
     * This method is called to ensure that the current token/event has been
     * completely parsed, such that we have all the data needed to return
     * it (textual content, PI data, comment text etc)
     */
    private void finishToken() throws XMLStreamException {
        _tokenIncomplete = false;
        switch (_currToken) {
            case PROCESSING_INSTRUCTION:
                finishPI();
                break;

            case CHARACTERS:
                finishCharacters();
                break;

            case COMMENT:
                finishComment();
                break;

            case SPACE:
                finishSpace();
                break;

            case DTD:
                finishDTD(true); // true -> get text
                break;

            case CDATA:
                finishCData();
                break;

            default:
                throw new RuntimeException("Internal error");
        }
    }

    /**
     * Helper method used to isolate things that need to be (re)set in
     * cases where
     */
    private int _nextEntity() {
        // !!! Also, have to assume start location has been set or such
        _textBuilder.resetWithEmpty();
        // !!! TODO: handle start location?
        return (_currToken = ENTITY_REFERENCE);
    }

    /**
     * This method is called to essentially skip remaining of the
     * current token (data of PI etc)
     *
     * @return True If by skipping we also figured out following event
     *   type (and assigned its type to _currToken); false if that remains
     *   to be done
     */
    private boolean skipToken() throws XMLStreamException {
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
    public Location getCurrentLocation() {
        return LocationImpl.fromZeroBased(_pastBytesOrChars + _inputPtr, _currRow, _inputPtr - _rowStartOffset);
    }

    private void markLF(int offset) {
        _rowStartOffset = offset;
        ++_currRow;
    }

    private void markLF() {
        _rowStartOffset = _inputPtr;
        ++_currRow;
    }

    private void setStartLocation() {
        _startRawOffset = _pastBytesOrChars + _inputPtr;
        _startRow = _currRow;
        _startColumn = _inputPtr - _rowStartOffset;
    }

    public Location getStartLocation() {
        // !!! TODO: deal with impedance wrt int/long (flaw in Stax API)
        int row = (int) _startRow;
        int col = (int) _startColumn;
        return LocationImpl.fromZeroBased(_startRawOffset, row, col);
    }

    /*
    /**********************************************************************
    /* Public scanner interface, other methods
    /**********************************************************************
     */

    public boolean hasEmptyStack() {
        return (_depth == 0);
    }

    /*
    /**********************************************************************
    /* Data accessors, names:
    /**********************************************************************
     */

    public PNameC getName() {
        return _tokenName;
    }

    public QName getQName() {
        return _tokenName.constructQName(_defaultNs);
    }

    /*
    /**********************************************************************
    /* Data accessors, (element) text:
    /**********************************************************************
     */

    public String getText() throws XMLStreamException {
        if (_tokenIncomplete) {
            finishToken();
        }
        return _textBuilder.contentsAsString();
    }

    public int getTextLength() throws XMLStreamException {
        if (_tokenIncomplete) {
            finishToken();
        }
        return _textBuilder.size();
    }

    public char[] getTextCharacters() throws XMLStreamException {
        if (_tokenIncomplete) {
            finishToken();
        }
        return _textBuilder.getTextBuffer();
    }

    public int getTextCharacters(int srcStart, char[] target, int targetStart, int len) throws XMLStreamException {
        if (_tokenIncomplete) {
            finishToken();
        }
        return _textBuilder.contentsToArray(srcStart, target, targetStart, len);
    }

    public boolean isTextWhitespace() throws XMLStreamException {
        if (_tokenIncomplete) {
            finishToken();
        }
        return _textBuilder.isAllWhitespace();
    }

    /*
    /**********************************************************************
    /* Data accessors, attributes:
    /**********************************************************************
     */

    public int getAttrCount() {
        return _attrCount;
    }

    public String getAttrLocalName(int index) {
        // Note: caller checks indices:
        return _attrCollector.getName(index).getLocalName();
    }

    public QName getAttrQName(int index) {
        // Note: caller checks indices:
        return _attrCollector.getQName(index);
    }

    public String getAttrNsURI(int index) {
        // Note: caller checks indices:
        return _attrCollector.getName(index).getNsUri();
    }

    public String getAttrPrefix(int index) {
        // Note: caller checks indices:
        return _attrCollector.getName(index).getPrefix();
    }

    public String getAttrValue(int index) {
        // Note: caller checks indices
        return _attrCollector.getValue(index);
    }

    public String getAttrValue(String nsURI, String localName) {
        /* Collector may not be reset if there are no attributes,
         * need to check if any could be found first:
         */
        if (_attrCount < 1) {
            return null;
        }
        return _attrCollector.getValue(nsURI, localName);
    }

    /*
    /**********************************************************************
    /* Data accessors, namespace declarations:
    /**********************************************************************
     */

    public int getNsCount() {
        if (_currToken == START_ELEMENT) {
            return _currNsCount;
        }
        return (_lastNsDecl == null) ? 0 : _lastNsDecl.countDeclsOnLevel(_depth);
    }

    public String getNamespacePrefix(int index) {
        return findCurrNsDecl(index).getBinding().mPrefix;
    }

    public String getNamespaceURI(int index) {
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

    public String getNamespaceURI() {
        String uri = _tokenName.getNsUri();
        // Null means it uses the default ns:
        return (uri == null) ? _defaultNs.mURI : uri;
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
    /* 2nd level parsing
    /**********************************************************************
     */

    private int handlePrologDeclStart(boolean isProlog) throws XMLStreamException {
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        char c = _inputBuffer[_inputPtr++];
        if (c == '-') { // Comment?
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            c = _inputBuffer[_inputPtr++];
            if (c == '-') {
                _tokenIncomplete = true;
                return (_currToken = COMMENT);
            }
        } else if (c == 'D') { // DOCTYPE?
            if (isProlog) { // no DOCTYPE in epilog
                handleDtdStart();
                return DTD;
            }
        }

        /* error... for error recovery purposes, let's just pretend
         * like it was unfinished CHARACTERS, though.
         */
        _tokenIncomplete = true;
        _currToken = CHARACTERS;
        reportPrologUnexpChar(isProlog, c, " (expected '-' for COMMENT)");
        return _currToken; // never gets here
    }

    private void handleDtdStart() throws XMLStreamException {
        matchAsciiKeyword("DOCTYPE");
        // And then some white space and root  name
        char c = skipInternalWs(true, "after DOCTYPE keyword, before root name");
        _tokenName = parsePName(c);
        c = skipInternalWs(false, null);

        String _systemId;
        if (c == 'P') { // PUBLIC
            matchAsciiKeyword("PUBLIC");
            c = skipInternalWs(true, null);
            parsePublicId(c);
            c = skipInternalWs(true, null);
            _systemId = parseSystemId(c);
            c = skipInternalWs(false, null);
        } else if (c == 'S') { // SYSTEM
            matchAsciiKeyword("SYSTEM");
            c = skipInternalWs(true, null);
            _systemId = parseSystemId(c);
            c = skipInternalWs(false, null);
        } else {
            _systemId = null;
        }

        /* Ok; so, need to get either an internal subset, or the
         * end:
         */
        if (c == '>') { // fine, we are done
            _tokenIncomplete = false;
            _currToken = DTD;
            return;
        }

        if (c != '[') { // If not end, must have int. subset
            String msg = (_systemId != null)
                ? " (expected '[' for the internal subset, or '>' to end DOCTYPE declaration)"
                : " (expected a 'PUBLIC' or 'SYSTEM' keyword, '[' for the internal subset, or '>' to end DOCTYPE declaration)";
            reportTreeUnexpChar(c, msg);
        }

        /* Need not parse the int. subset yet, can leave as is, and then
         * either skip or parse later on
         */
        _tokenIncomplete = true;
        _currToken = DTD;
    }

    private int handleCommentOrCdataStart() throws XMLStreamException {
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        char c = _inputBuffer[_inputPtr++];

        // Let's first see if it's a comment (simpler)
        if (c == '-') { // Comment
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            c = _inputBuffer[_inputPtr++];
            if (c != '-') {
                reportTreeUnexpChar(c, " (expected '-' for COMMENT)");
            }
            _tokenIncomplete = true;
            return (_currToken = COMMENT);
        }

        // If not, should be CDATA:
        if (c == '[') { // CDATA
            _currToken = CDATA;
            for (int i = 0; i < 6; ++i) {
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                c = _inputBuffer[_inputPtr++];
                if (c != CDATA_STR.charAt(i)) {
                    reportTreeUnexpChar(c, " (expected '" + CDATA_STR.charAt(i) + "' for CDATA section)");
                }
            }
            _tokenIncomplete = true;
            return CDATA;
        }
        reportTreeUnexpChar(c, " (expected either '-' for COMMENT or '[CDATA[' for CDATA section)");
        return TOKEN_EOI; // never gets here
    }

    private int handlePIStart() throws XMLStreamException {
        _currToken = PROCESSING_INSTRUCTION;

        // Ok, first, need a name
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        // Ok, first, need a name
        char c = _inputBuffer[_inputPtr++];
        _tokenName = parsePName(c);
        { // but is it "xml" (case insensitive)?
            String ln = _tokenName.getLocalName();
            if (ln.equalsIgnoreCase("xml") && _tokenName.getPrefix() == null) {
                reportInputProblem(ErrorConsts.ERR_WF_PI_XML_TARGET);
            }
        }

        /* Let's then verify that we either get a space, or closing
         * '?>': this way we'll catch some problems right away, and also
         * simplify actual processing of contents.
         */
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        c = _inputBuffer[_inputPtr++];
        if (c <= INT_SPACE) {
            // Ok, let's skip the white space...
            while (true) {
                if (c == '\n') {
                    markLF();
                } else if (c == '\r') {
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    if (_inputBuffer[_inputPtr] == '\n') {
                        ++_inputPtr;
                    }
                    markLF();
                } else if (c != ' ' && c != '\t') {
                    throwInvalidSpace(c);
                }
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                c = _inputBuffer[_inputPtr];
                if (c > 0x0020) {
                    break;
                }
                ++_inputPtr;
            }
            // Ok, got non-space, need to push back:
            _tokenIncomplete = true;
        } else {
            if (c != INT_QMARK) {
                reportMissingPISpace(c);
            }
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            c = _inputBuffer[_inputPtr++];
            if (c != '>') {
                reportMissingPISpace(c);
            }
            _textBuilder.resetWithEmpty();
            _tokenIncomplete = false;
        }

        return PROCESSING_INSTRUCTION;
    }

    /**
     * @return Code point for the entity that expands to a valid XML
     *    content character.
     */
    private int handleCharEntity() throws XMLStreamException {
        // Hex or decimal?
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        char c = _inputBuffer[_inputPtr++];
        int value = 0;
        if (c == 'x') { // hex
            while (true) {
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                c = _inputBuffer[_inputPtr++];
                if (c == ';') {
                    break;
                }
                value = value << 4;
                if (c <= '9' && c >= '0') {
                    value += (c - '0');
                } else if (c >= 'a' && c <= 'f') {
                    value += 10 + (c - 'a');
                } else if (c >= 'A' && c <= 'F') {
                    value += 10 + (c - 'A');
                } else {
                    throwUnexpectedChar(c, "; expected a hex digit (0-9a-fA-F)");
                }
                if (value > MAX_UNICODE_CHAR) { // Overflow?
                    reportEntityOverflow();
                }
            }
        } else { // numeric (decimal)
            while (c != ';') {
                if (c <= '9' && c >= '0') {
                    value = (value * 10) + (c - '0');
                    if (value > MAX_UNICODE_CHAR) { // Overflow?
                        reportEntityOverflow();
                    }
                } else {
                    throwUnexpectedChar(c, "; expected a decimal number");
                }
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                c = _inputBuffer[_inputPtr++];
            }
        }

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
                reportInvalidXmlChar(value);
            }
        }
        return value;
    }

    private int handleStartElement(char c) throws XMLStreamException {
        _currToken = START_ELEMENT;
        _currNsCount = 0;
        PNameC elemName = parsePName(c);

        /* Ok. Need to create a qualified name. Simplest for element
         * in default ns (no extra work -- expressed as null binding);
         * otherwise need to find binding
         */
        String prefix = elemName.getPrefix();
        boolean allBound; // flag to check 'late' bindings

        if (prefix == null) { // element in default ns
            allBound = true; // which need not be bound
        } else {
            elemName = bindName(elemName, prefix);
            allBound = elemName.isBound();
        }

        _tokenName = elemName;
        _currElem = new ElementScope(elemName, _currElem);

        // And then attribute parsing loop:
        int attrPtr = 0;

        while (true) {
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            c = _inputBuffer[_inputPtr++];
            // Intervening space to skip?
            if (c <= INT_SPACE) {
                do {
                    if (c == INT_LF) {
                        markLF();
                    } else if (c == INT_CR) {
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        if (_inputBuffer[_inputPtr] == '\n') {
                            ++_inputPtr;
                        }
                        markLF();
                    } else if (c != ' ' && c != '\t') {
                        throwInvalidSpace(c);
                    }
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    c = _inputBuffer[_inputPtr++];
                } while (c <= INT_SPACE);
            } else if (c != INT_SLASH && c != INT_GT) {
                throwUnexpectedChar(c, " expected space, or '>' or \"/>\"");
            }

            // Ok; either need to get an attribute name, or end marker:
            if (c == INT_SLASH) {
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                c = _inputBuffer[_inputPtr++];
                if (c != '>') {
                    throwUnexpectedChar(c, " expected '>'");
                }
                _isEmptyTag = true;
                break;
            } else if (c == '>') {
                _isEmptyTag = false;
                break;
            } else if (c == '<') {
                reportInputProblem("Unexpected '<' character in element (missing closing '>'?)");
            }

            // Ok, an attr name:
            PNameC attrName = parsePName(c);
            prefix = attrName.getPrefix();

            boolean isNsDecl;

            if (prefix == null) { // can be default ns decl:
                isNsDecl = (attrName.getLocalName().equals("xmlns"));
            } else {
                // May be a namespace decl though?
                if (prefix.equals("xmlns")) {
                    isNsDecl = true;
                } else {
                    attrName = bindName(attrName, prefix);
                    if (allBound) {
                        allBound = attrName.isBound();
                    }
                    isNsDecl = false;
                }
            }

            // Optional space to skip again
            while (true) {
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                c = _inputBuffer[_inputPtr++];
                if (c > INT_SPACE) {
                    break;
                }
                if (c == '\n') {
                    markLF();
                } else if (c == '\r') {
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    if (_inputBuffer[_inputPtr] == '\n') {
                        ++_inputPtr;
                    }
                    markLF();
                } else if (c != ' ' && c != '\t') {
                    throwInvalidSpace(c);
                }
            }

            if (c != '=') {
                throwUnexpectedChar(c, " expected '='");
            }

            // Optional space to skip again
            while (true) {
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                c = _inputBuffer[_inputPtr++];
                if (c > INT_SPACE) {
                    break;
                }
                if (c == '\n') {
                    markLF();
                } else if (c == '\r') {
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    if (_inputBuffer[_inputPtr] == '\n') {
                        ++_inputPtr;
                    }
                    markLF();
                } else if (c != ' ' && c != '\t') {
                    throwInvalidSpace(c);
                }
            }

            if (c != '"' && c != '\'') {
                throwUnexpectedChar(c, " Expected a quote");
            }

            /* Ok, finally: value parsing. However, ns URIs are to be handled
             * different from attribute values... let's offline URIs, since
             * they should be less common than attribute values.
             */
            if (isNsDecl) { // default ns, or explicit?
                handleNsDeclaration(attrName, c);
                ++_currNsCount;
            } else { // nope, a 'real' attribute:
                attrPtr = collectValue(attrPtr, c, attrName);
            }
        }
        {
            // Note: this call also checks attribute uniqueness
            int act = _attrCollector.finishLastValue(attrPtr);
            if (act < 0) { // error, dup attr indicated by -1
                act = _attrCollector.getCount(); // let's get correct count
                reportInputProblem(_attrCollector.getErrorMsg());
            }
            _attrCount = act;
        }
        ++_depth;

        /* Was there any prefix that wasn't bound prior to use?
         * That's legal, assuming declaration was found later on...
         * let's check
         */
        if (!allBound) {
            if (!elemName.isBound()) { // element itself unbound
                reportUnboundPrefix(_tokenName, false);
            }
            for (int i = 0, len = _attrCount; i < len; ++i) {
                PNameC attrName = _attrCollector.getName(i);
                if (!attrName.isBound()) {
                    reportUnboundPrefix(attrName, true);
                }
            }
        }

        return START_ELEMENT;
    }

    /**
     * This method implements the tight loop for parsing attribute
     * values. It's off-lined from the main start element method to
     * simplify main method, which makes code more maintainable
     * and possibly easier for JIT/HotSpot to optimize.
     */
    private int collectValue(int attrPtr, char quoteChar, PNameC attrName) throws XMLStreamException {
        char[] attrBuffer = _attrCollector.startNewValue(attrName, attrPtr);
        final int[] TYPES = sCharTypes.ATTR_CHARS;

        value_loop: while (true) {
            char c;

            ascii_loop: while (true) {
                int ptr = _inputPtr;
                if (ptr >= _inputEnd) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                }
                if (attrPtr >= attrBuffer.length) {
                    attrBuffer = _attrCollector.valueBufferFull();
                }
                int max = _inputEnd;
                {
                    int max2 = ptr + (attrBuffer.length - attrPtr);
                    if (max2 < max) {
                        max = max2;
                    }
                }
                while (ptr < max) {
                    c = _inputBuffer[ptr++];
                    if (c <= 0xFF) {
                        if (TYPES[c] != 0) {
                            _inputPtr = ptr;
                            break ascii_loop;
                        }
                    } else if (c >= 0xD800) { // surrogates and 0xFFFE/0xFFFF
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                    attrBuffer[attrPtr++] = c;
                }
                _inputPtr = ptr;
            }

            if (c <= 0xFF) {
                switch (TYPES[c]) {
                    case XmlCharTypes.CT_INVALID:
                        handleInvalidXmlChar(c);
                    case XmlCharTypes.CT_WS_CR:
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        if (_inputBuffer[_inputPtr] == '\n') {
                            ++_inputPtr;
                        }
                        // fall through
                    case XmlCharTypes.CT_WS_LF:
                        markLF();
                        // fall through
                    case XmlCharTypes.CT_WS_TAB:
                        // Plus, need to convert these all to simple space
                        c = ' ';
                        break;

                    case XmlCharTypes.CT_LT:
                        throwUnexpectedChar(c, "'<' not allowed in attribute value");
                    case XmlCharTypes.CT_AMP:
                        int d = handleEntityInText();
                        if (d == 0) { // unexpanded general entity... not good
                            reportUnexpandedEntityInAttr(false);
                        }
                        // Ok; does it need a surrogate though? (over 16 bits)
                        if ((d >> 16) != 0) {
                            d -= 0x10000;
                            attrBuffer[attrPtr++] = (char) (0xD800 | (d >> 10));
                            d = 0xDC00 | (d & 0x3FF);
                            if (attrPtr >= attrBuffer.length) {
                                attrBuffer = _attrCollector.valueBufferFull();
                            }
                        }
                        c = (char) d;
                        break;

                    case XmlCharTypes.CT_ATTR_QUOTE:
                        if (c == quoteChar) {
                            break value_loop;
                        }

                        // default:
                        // Other chars are not important here...
                }
            } else {
                if (c < 0xE000) {
                    // if ok, returns second surrogate; otherwise exception
                    char d = checkSurrogate(c);
                    attrBuffer[attrPtr++] = c;
                    // Need to ensure room for one more
                    if (attrPtr >= attrBuffer.length) {
                        attrBuffer = _attrCollector.valueBufferFull();
                    }
                    c = d;
                } else if (c >= 0xFFFE) {
                    c = handleInvalidXmlChar(c);
                }
            }
            // We know there's room for at least one more char
            attrBuffer[attrPtr++] = c;
        }

        return attrPtr;
    }

    /**
     * Method called from the main START_ELEMENT handling loop, to
     * parse namespace URI values.
     */
    private void handleNsDeclaration(PNameC name, char quoteChar) throws XMLStreamException {
        int attrPtr = 0;
        char[] attrBuffer = _nameBuffer;

        while (true) {
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            char c = _inputBuffer[_inputPtr++];
            if (c == quoteChar) {
                break;
            }
            if (c == '&') { // entity
                int d = handleEntityInText();
                if (d == 0) { // general entity; should never happen
                    reportUnexpandedEntityInAttr(true);
                }
                // Ok; does it need a surrogate though? (over 16 bits)
                if ((d >> 16) != 0) {
                    if (attrPtr >= attrBuffer.length) {
                        _nameBuffer = attrBuffer = DataUtil.growArrayBy(attrBuffer, attrBuffer.length);
                    }
                    d -= 0x10000;
                    attrBuffer[attrPtr++] = (char) (0xD800 | (d >> 10));
                    d = 0xDC00 | (d & 0x3FF);
                }
                c = (char) d;
            } else if (c == '<') { // error
                throwUnexpectedChar(c, "'<' not allowed in attribute value");
            } else {
                if (c < INT_SPACE) {
                    if (c == '\n') {
                        markLF();
                    } else if (c == '\r') {
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        if (_inputBuffer[_inputPtr] == '\n') {
                            ++_inputPtr;
                        }
                        markLF();
                        c = '\n';
                    } else if (c != '\t') {
                        throwInvalidSpace(c);
                    }
                }
            }
            if (attrPtr >= attrBuffer.length) {
                _nameBuffer = attrBuffer = DataUtil.growArrayBy(attrBuffer, attrBuffer.length);
            }
            attrBuffer[attrPtr++] = c;
        }

        /* Simple optimization: for default ns removal (or, with
         * ns 1.1, any other as well), will use empty value... no
         * need to try to intern:
         */
        if (attrPtr == 0) {
            bindNs(name, "");
        } else {
            String uri = _config.canonicalizeURI(attrBuffer, attrPtr);
            bindNs(name, uri);
        }
    }

    private int handleEndElement() throws XMLStreamException {
        --_depth;

        _currToken = END_ELEMENT;
        // Ok, at this point we have seen '/', need the name
        _tokenName = _currElem.getName();
        String pname = _tokenName.getPrefixedName();
        char c;
        int i = 0;
        int len = pname.length();
        do {
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            c = _inputBuffer[_inputPtr++];
            if (c != pname.charAt(i)) {
                reportUnexpectedEndTag(pname);
            }
        } while (++i < len);

        // Can still have a problem, if name didn't end there...
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        c = _inputBuffer[_inputPtr++];
        if (c <= ' ') {
            c = skipInternalWs(false, null);
        } else if (c != '>') {
            if (c == ':' || XmlChars.is10NameChar(c)) {
                reportUnexpectedEndTag(pname);
            }
        }
        if (c != '>') {
            throwUnexpectedChar(c, " expected space or closing '>'");
        }
        return END_ELEMENT;
    }

    private int handleEntityInText() throws XMLStreamException {
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        char c = _inputBuffer[_inputPtr++];
        if (c == '#') {
            return handleCharEntity();
        }
        String start;
        if (c == 'a') { // amp or apos?
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            c = _inputBuffer[_inputPtr++];
            if (c == 'm') { // amp?
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                c = _inputBuffer[_inputPtr++];
                if (c == 'p') {
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    c = _inputBuffer[_inputPtr++];
                    if (c == ';') {
                        return INT_AMP;
                    }
                    start = "amp";
                } else {
                    start = "am";
                }
            } else if (c == 'p') { // apos?
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                c = _inputBuffer[_inputPtr++];
                if (c == 'o') {
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    c = _inputBuffer[_inputPtr++];
                    if (c == 's') {
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        c = _inputBuffer[_inputPtr++];
                        if (c == ';') {
                            return INT_APOS;
                        }
                        start = "apos";
                    } else {
                        start = "apo";
                    }
                } else {
                    start = "ap";
                }
            } else {
                start = "a";
            }
        } else if (c == 'l') { // lt?
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            c = _inputBuffer[_inputPtr++];
            if (c == 't') {
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                c = _inputBuffer[_inputPtr++];
                if (c == ';') {
                    return INT_LT;
                }
                start = "lt";
            } else {
                start = "l";
            }
        } else if (c == 'g') { // gt?
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            c = _inputBuffer[_inputPtr++];
            if (c == 't') {
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                c = _inputBuffer[_inputPtr++];
                if (c == ';') {
                    return INT_GT;
                }
                start = "gt";
            } else {
                start = "g";
            }
        } else if (c == 'q') { // quot?
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            c = _inputBuffer[_inputPtr++];
            if (c == 'u') {
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                c = _inputBuffer[_inputPtr++];
                if (c == 'o') {
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    c = _inputBuffer[_inputPtr++];
                    if (c == 't') {
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        c = _inputBuffer[_inputPtr++];
                        if (c == ';') {
                            return INT_QUOTE;
                        }
                        start = "quot";
                    } else {
                        start = "quo";
                    }
                } else {
                    start = "qu";
                }
            } else {
                start = "q";
            }
        } else {
            start = "";
        }

        final int[] TYPES = sCharTypes.NAME_CHARS;

        /* All righty: we have the beginning of the name, plus the first
         * char too. So let's see what we can do with it.
         */
        char[] cbuf = _nameBuffer;
        int cix = 0;
        for (int len = start.length(); cix < len; ++cix) {
            cbuf[cix] = start.charAt(cix);
        }
        //int colon = -1;
        while (c != ';') {
            boolean ok;

            // Has to be a valid name start char though:
            if (c <= 0xFF) {
                switch (TYPES[c]) {
                    case XmlCharTypes.CT_NAME_NONE:
                    case XmlCharTypes.CT_NAME_COLON: // not ok for entities?
                    case XmlCharTypes.CT_NAME_NONFIRST:
                        ok = (cix > 0);
                        break;

                    case XmlCharTypes.CT_NAME_ANY:
                        ok = true;
                        break;

                    default:
                        ok = false;
                        break;
                }
            } else {
                if (c < 0xE000) {
                    // if ok, returns second surrogate; otherwise exception
                    int value = decodeSurrogate(c);
                    if (cix >= cbuf.length) {
                        _nameBuffer = cbuf = DataUtil.growArrayBy(cbuf, cbuf.length);
                    }
                    cbuf[cix++] = c;
                    c = _inputBuffer[_inputPtr - 1]; // was read by decode func
                    ok = XmlChars.is10NameChar(value);
                } else if (c >= 0xFFFE) {
                    c = handleInvalidXmlChar(c);
                    ok = false; // never gets here
                } else {
                    ok = true;
                }
            }
            if (!ok) {
                reportInvalidNameChar(c, cix);
            }
            if (cix >= cbuf.length) {
                _nameBuffer = cbuf = DataUtil.growArrayBy(cbuf, cbuf.length);
            }
            cbuf[cix++] = c;
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            c = _inputBuffer[_inputPtr++];
        }

        // Ok, let's construct a (temporary) entity name, then:
        String pname = new String(cbuf, 0, cix);
        // (note: hash is dummy... not to be compared to anything etc)
        _tokenName = new PNameC(pname, null, pname, 0);

        /* One more thing: do we actually allow entities in this mode
         * and with this event?
         */
        if (_config.willExpandEntities()) {
            reportInputProblem("General entity reference (&" + pname
                + ";) encountered in entity expanding mode: operation not (yet) implemented");
        }
        return 0;
    }

    private void finishComment() throws XMLStreamException {
        final int[] TYPES = sCharTypes.OTHER_CHARS;
        final char[] inputBuffer = _inputBuffer;
        char[] outputBuffer = _textBuilder.resetWithEmpty();
        int outPtr = 0;

        main_loop: while (true) {
            char c;

            // Then the tight ascii non-funny-char loop:
            ascii_loop: while (true) {
                int ptr = _inputPtr;
                if (ptr >= _inputEnd) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                }
                if (outPtr >= outputBuffer.length) {
                    outputBuffer = _textBuilder.finishCurrentSegment();
                    outPtr = 0;
                }
                int max = _inputEnd;
                {
                    int max2 = ptr + (outputBuffer.length - outPtr);
                    if (max2 < max) {
                        max = max2;
                    }
                }
                while (ptr < max) {
                    c = inputBuffer[ptr++];
                    if (c <= 0xFF) {
                        if (TYPES[c] != 0) {
                            _inputPtr = ptr;
                            break ascii_loop;
                        }
                    } else if (c >= 0xD800) { // surrogates and 0xFFFE/0xFFFF
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                    outputBuffer[outPtr++] = c;
                }
                _inputPtr = ptr;
            }

            if (c <= 0xFF) {
                switch (TYPES[c]) {
                    case XmlCharTypes.CT_INVALID:
                        handleInvalidXmlChar(c);
                    case XmlCharTypes.CT_WS_CR: {
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        if (inputBuffer[_inputPtr] == '\n') {
                            ++_inputPtr;
                        }
                        markLF();
                    }
                        c = '\n';
                        break;

                    case XmlCharTypes.CT_WS_LF:
                        markLF();
                        break;

                    case XmlCharTypes.CT_HYPHEN: // '-->'?
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        if (_inputBuffer[_inputPtr] == '-') { // ok, must be end then
                            ++_inputPtr;
                            if (_inputPtr >= _inputEnd) {
                                loadMoreGuaranteed();
                            }
                            if (_inputBuffer[_inputPtr++] != '>') {
                                reportDoubleHyphenInComments();
                            }
                            break main_loop;
                        }
                        break;
                    // default:
                    // Other types are not important here..
                }
            } else {  // high-range, surrogates etc
                if (c < 0xE000) {
                    // if ok, returns second surrogate; otherwise exception
                    char d = checkSurrogate(c);
                    outputBuffer[outPtr++] = c;
                    if (outPtr >= outputBuffer.length) {
                        outputBuffer = _textBuilder.finishCurrentSegment();
                        outPtr = 0;
                    }
                    c = d;
                } else if (c >= 0xFFFE) {
                    c = handleInvalidXmlChar(c);
                }
            }
            // We know there's room for one more:
            outputBuffer[outPtr++] = c;
        }
        _textBuilder.setCurrentLength(outPtr);
    }

    private void finishPI() throws XMLStreamException {
        final int[] TYPES = sCharTypes.OTHER_CHARS;
        final char[] inputBuffer = _inputBuffer;
        char[] outputBuffer = _textBuilder.resetWithEmpty();
        int outPtr = 0;

        main_loop: while (true) {
            char c;

            // Then the tight ascii non-funny-char loop:
            ascii_loop: while (true) {
                int ptr = _inputPtr;
                if (ptr >= _inputEnd) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                }
                if (outPtr >= outputBuffer.length) {
                    outputBuffer = _textBuilder.finishCurrentSegment();
                    outPtr = 0;
                }
                int max = _inputEnd;
                {
                    int max2 = ptr + (outputBuffer.length - outPtr);
                    if (max2 < max) {
                        max = max2;
                    }
                }
                while (ptr < max) {
                    c = inputBuffer[ptr++];
                    if (c <= 0xFF) {
                        if (TYPES[c] != 0) {
                            _inputPtr = ptr;
                            break ascii_loop;
                        }
                    } else if (c >= 0xD800) { // surrogates and 0xFFFE/0xFFFF
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                    outputBuffer[outPtr++] = c;
                }
                _inputPtr = ptr;
            }

            if (c <= 0xFF) {
                switch (TYPES[c]) {
                    case XmlCharTypes.CT_WS_CR: {
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        if (inputBuffer[_inputPtr] == CHAR_LF) {
                            ++_inputPtr;
                        }
                        markLF();
                        c = '\n';
                    }
                        break;

                    case XmlCharTypes.CT_WS_LF:
                        markLF();
                        break;

                    case XmlCharTypes.CT_QMARK: // '?>'?
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        if (_inputBuffer[_inputPtr] == '>') {
                            ++_inputPtr;
                            break main_loop;
                        }
                        break;
                    // default:
                    // Other types are not important here...
                }
            } else {  // high-range, surrogates etc
                if (c < 0xE000) {
                    // if ok, returns second surrogate; otherwise exception
                    char d = checkSurrogate(c);
                    outputBuffer[outPtr++] = c;
                    if (outPtr >= outputBuffer.length) {
                        outputBuffer = _textBuilder.finishCurrentSegment();
                        outPtr = 0;
                    }
                    c = d;
                } else if (c >= 0xFFFE) {
                    c = handleInvalidXmlChar(c);
                }
            }
            // We know there's room for one more:
            outputBuffer[outPtr++] = c;
        }
        _textBuilder.setCurrentLength(outPtr);
    }

    private void finishDTD(boolean copyContents) throws XMLStreamException {
        char[] outputBuffer = copyContents ? _textBuilder.resetWithEmpty() : null;
        int outPtr = 0;

        final int[] TYPES = sCharTypes.DTD_CHARS;
        boolean inDecl = false; // in declaration/directive?
        int quoteChar = 0; // inside quoted string?

        main_loop: while (true) {
            char c;

            /* First we'll have a quickie loop for speeding through
             * uneventful chars...
             */
            ascii_loop: while (true) {
                int ptr = _inputPtr;
                if (ptr >= _inputEnd) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                }
                int max = _inputEnd;
                if (outputBuffer != null) {
                    if (outPtr >= outputBuffer.length) {
                        outputBuffer = _textBuilder.finishCurrentSegment();
                        outPtr = 0;
                    }
                    {
                        int max2 = ptr + (outputBuffer.length - outPtr);
                        if (max2 < max) {
                            max = max2;
                        }
                    }
                }
                while (ptr < max) {
                    c = _inputBuffer[ptr++];
                    if (c <= 0xFF) {
                        if (TYPES[c] != 0) {
                            _inputPtr = ptr;
                            break ascii_loop;
                        }
                    } else if (c >= 0xD800) { // surrogates and 0xFFFE/0xFFFF
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                    if (outputBuffer != null) {
                        outputBuffer[outPtr++] = c;
                    }
                }
                _inputPtr = ptr;
            }

            if (c <= 0xFF) {
                switch (TYPES[c]) {
                    case XmlCharTypes.CT_INVALID:
                        handleInvalidXmlChar(c);
                    case XmlCharTypes.CT_WS_CR: {
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        if (_inputBuffer[_inputPtr] == '\n') {
                            ++_inputPtr;
                        }
                        markLF();
                    }
                        c = '\n';
                        break;

                    case XmlCharTypes.CT_WS_LF:
                        markLF();
                        break;

                    case XmlCharTypes.CT_DTD_QUOTE: // apos or quot
                        if (quoteChar == 0) {
                            quoteChar = c;
                        } else {
                            if (quoteChar == c) {
                                quoteChar = 0;
                            }
                        }
                        break;

                    case XmlCharTypes.CT_DTD_LT:
                        if (!inDecl) {
                            inDecl = true;
                        }
                        break;

                    case XmlCharTypes.CT_DTD_GT:
                        if (quoteChar == 0) {
                            inDecl = false;
                        }
                        break;

                    case XmlCharTypes.CT_DTD_RBRACKET:
                        if (!inDecl && quoteChar == 0) {
                            break main_loop;
                        }
                        break;
                    // default:
                    // Other types are not important here...
                }
            } else {  // high-range, surrogates etc
                if (c < 0xE000) {
                    // if ok, returns second surrogate; otherwise exception
                    char d = checkSurrogate(c);
                    if (outputBuffer != null) {
                        outputBuffer[outPtr++] = c;
                        if (outPtr >= outputBuffer.length) {
                            outputBuffer = _textBuilder.finishCurrentSegment();
                            outPtr = 0;
                        }
                    }
                    c = d;
                } else if (c >= 0xFFFE) {
                    c = handleInvalidXmlChar(c);
                }
            }

            if (outputBuffer != null) { // has room for one more
                outputBuffer[outPtr++] = c;
            }
        }
        if (outputBuffer != null) {
            _textBuilder.setCurrentLength(outPtr);
        }

        // but still need to match the '>'...
        char c = skipInternalWs(false, null);
        if (c != '>') {
            throwUnexpectedChar(c, " expected '>' after the internal subset");
        }
    }

    private void finishCData() throws XMLStreamException {
        final int[] TYPES = sCharTypes.OTHER_CHARS;
        final char[] inputBuffer = _inputBuffer;
        char[] outputBuffer = _textBuilder.resetWithEmpty();
        int outPtr = 0;

        /* At this point, space (if any) has been skipped, and we are
         * to parse and store the contents
         */
        main_loop: while (true) {
            char c;
            // Then the tight ascii non-funny-char loop:
            ascii_loop: while (true) {
                int ptr = _inputPtr;
                if (ptr >= _inputEnd) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                }
                if (outPtr >= outputBuffer.length) {
                    outputBuffer = _textBuilder.finishCurrentSegment();
                    outPtr = 0;
                }
                int max = _inputEnd;
                {
                    int max2 = ptr + (outputBuffer.length - outPtr);
                    if (max2 < max) {
                        max = max2;
                    }
                }
                while (ptr < max) {
                    c = inputBuffer[ptr++];
                    if (c <= 0xFF) {
                        if (TYPES[c] != 0) {
                            _inputPtr = ptr;
                            break ascii_loop;
                        }
                    } else if (c >= 0xD800) { // surrogates and 0xFFFE/0xFFFF
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                    outputBuffer[outPtr++] = c;
                }
                _inputPtr = ptr;
            }
            // And then exceptions:
            if (c <= 0xFF) {
                switch (TYPES[c]) {
                    case XmlCharTypes.CT_INVALID:
                        handleInvalidXmlChar(c);
                    case XmlCharTypes.CT_WS_CR: {
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        if (inputBuffer[_inputPtr] == '\n') {
                            ++_inputPtr;
                        }
                        markLF();
                    }
                        c = '\n';
                        break;

                    case XmlCharTypes.CT_WS_LF:
                        markLF();
                        break;

                    case XmlCharTypes.CT_RBRACKET: // close ']]>' marker?
                        /* Ok: let's just parse all consequtive right brackets,
                         * and see if followed by greater-than char. This because
                         * we can only push back at most one char at a time, and
                         * thus can't easily just check a subset
                         */
                        int count = 0; // ignore first bracket
                        char d;

                        do {
                            if (_inputPtr >= _inputEnd) {
                                loadMoreGuaranteed();
                            }
                            d = _inputBuffer[_inputPtr];
                            if (d != ']') {
                                break;
                            }
                            ++_inputPtr;
                            ++count;
                        } while (true);

                        // Was the marker found?
                        boolean ok = (d == '>' && count >= 1);
                        if (ok) {
                            --count;
                        }
                        // Brackets to copy to output?
                        for (; count > 0; --count) {
                            outputBuffer[outPtr++] = ']';
                            if (outPtr >= outputBuffer.length) {
                                outputBuffer = _textBuilder.finishCurrentSegment();
                                outPtr = 0;
                            }
                        }
                        if (ok) {
                            ++_inputPtr; // to consume '>'
                            break main_loop;
                        }
                        break;
                    // default:
                    // Other types are not important here...
                }
            } else {  // high-range, surrogates etc
                if (c < 0xE000) {
                    // if ok, returns second surrogate; otherwise exception
                    char d = checkSurrogate(c);
                    outputBuffer[outPtr++] = c;
                    if (outPtr >= outputBuffer.length) {
                        outputBuffer = _textBuilder.finishCurrentSegment();
                        outPtr = 0;
                    }
                    c = d;
                } else if (c >= 0xFFFE) {
                    c = handleInvalidXmlChar(c);
                }
            }
            // Ok, can output the char; there's room for one char at least
            outputBuffer[outPtr++] = c;
        }

        _textBuilder.setCurrentLength(outPtr);
        /* 03-Feb-2009, tatu: To support coalescing mode, may need to
         *   do some extra work
         */
        if (_cfgCoalescing && !_entityPending) {
            finishCoalescedText();
        }
    }

    private void finishCharacters() throws XMLStreamException {
        int outPtr;
        char[] outputBuffer;

        // Ok, so what was the first char / entity?
        {
            int c = mTmpChar;
            if (c < 0) { // from entity; can just copy as is
                c = -c;
                outputBuffer = _textBuilder.resetWithEmpty();
                outPtr = 0;
                if ((c >> 16) != 0) { // surrogate pair?
                    c -= 0x10000;
                    /* Note: after resetting the buffer, it's known to have
                     * space for more than 2 chars we need to add
                     */
                    outputBuffer[outPtr++] = (char) (0xD800 | (c >> 10));
                    c = 0xDC00 | (c & 0x3FF);
                }
                outputBuffer[outPtr++] = (char) c;
            } else { // white space that we are interested in?
                if (c == INT_CR || c == INT_LF) {
                    ++_inputPtr; // wasn't advanced yet, in this case
                    outPtr = checkInTreeIndentation((char) c);
                    if (outPtr < 0) {
                        return;
                    }
                    // Above call also initializes the text builder appropriately
                    outputBuffer = _textBuilder.getBufferWithoutReset();
                } else {
                    outputBuffer = _textBuilder.resetWithEmpty();
                    outPtr = 0;
                }
            }
        }

        final int[] TYPES = sCharTypes.TEXT_CHARS;
        final char[] inputBuffer = _inputBuffer;

        main_loop: while (true) {
            char c;

            // Then the tight ascii non-funny-char loop:
            ascii_loop: while (true) {
                int ptr = _inputPtr;
                if (ptr >= _inputEnd) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                }
                if (outPtr >= outputBuffer.length) {
                    outputBuffer = _textBuilder.finishCurrentSegment();
                    outPtr = 0;
                }
                int max = _inputEnd;
                {
                    int max2 = ptr + (outputBuffer.length - outPtr);
                    if (max2 < max) {
                        max = max2;
                    }
                }
                while (ptr < max) {
                    c = inputBuffer[ptr++];
                    if (c <= 0xFF) {
                        if (TYPES[c] != 0) {
                            _inputPtr = ptr;
                            break ascii_loop;
                        }
                    } else if (c >= 0xD800) { // surrogates and 0xFFFE/0xFFFF
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                    outputBuffer[outPtr++] = c;
                }
                _inputPtr = ptr;
            }
            // And then exceptions:
            if (c <= 0xFF) {
                switch (TYPES[c]) {
                    case XmlCharTypes.CT_INVALID:
                        handleInvalidXmlChar(c);
                    case XmlCharTypes.CT_WS_CR: {
                        int ptr = _inputPtr;
                        if (ptr >= _inputEnd) {
                            loadMoreGuaranteed();
                            ptr = _inputPtr;
                        }
                        if (inputBuffer[ptr] == '\n') {
                            ++_inputPtr;
                        }
                        markLF();
                    }
                        c = '\n';
                        break;

                    case XmlCharTypes.CT_WS_LF:
                        markLF();
                        break;

                    case XmlCharTypes.CT_LT:
                        --_inputPtr;
                        break main_loop;

                    case XmlCharTypes.CT_AMP: {
                        int d = handleEntityInText();
                        if (d == 0) { // unexpandable general parsed entity
                            // _inputPtr set by entity expansion method
                            _entityPending = true;
                            break main_loop;
                        }
                        // Ok; does it need a surrogate though? (over 16 bits)
                        if ((d >> 16) != 0) {
                            d -= 0x10000;
                            outputBuffer[outPtr++] = (char) (0xD800 | (d >> 10));
                            // Need to ensure room for one more char
                            if (outPtr >= outputBuffer.length) {
                                outputBuffer = _textBuilder.finishCurrentSegment();
                                outPtr = 0;
                            }
                            d = (0xDC00 | (d & 0x3FF));
                        }
                        c = (char) d;
                    }
                        break;

                    case XmlCharTypes.CT_RBRACKET: // ']]>'?
                    {
                        // Let's then just count number of brackets --
                        // in case they are not followed by '>'
                        int count = 1;
                        while (true) {
                            if (_inputPtr >= _inputEnd) {
                                loadMoreGuaranteed();
                            }
                            c = inputBuffer[_inputPtr];
                            if (c != ']') {
                                break;
                            }
                            ++_inputPtr; // to skip past bracket
                            ++count;
                        }
                        if (c == '>' && count > 1) {
                            reportIllegalCDataEnd();
                        }
                        // Nope. Need to output all brackets, then; except
                        // for one that can be left for normal output
                        while (count > 1) {
                            outputBuffer[outPtr++] = ']';
                            if (outPtr >= outputBuffer.length) {
                                outputBuffer = _textBuilder.finishCurrentSegment();
                                outPtr = 0;
                            }
                            // Need to ensure room for one more char
                            --count;
                        }
                    }
                        // Can just output the first ']' along normal output
                        c = ']';
                        break;
                    // default:
                    // Other types are not important here...
                }
            } else {  // high-range, surrogates etc
                if (c < 0xE000) {
                    // if ok, returns second surrogate; otherwise exception
                    char d = checkSurrogate(c);
                    outputBuffer[outPtr++] = c;
                    if (outPtr >= outputBuffer.length) {
                        outputBuffer = _textBuilder.finishCurrentSegment();
                        outPtr = 0;
                    }
                    c = d;
                } else if (c >= 0xFFFE) {
                    c = handleInvalidXmlChar(c);
                }
            }
            outputBuffer[outPtr++] = c;
        }
        _textBuilder.setCurrentLength(outPtr);

        // 03-Feb-2009, tatu: Need to support coalescing mode too:
        if (_cfgCoalescing && !_entityPending) {
            finishCoalescedText();
        }
    }

    private void finishSpace() throws XMLStreamException {
        /* Ok: so, mTmpChar contains first space char. If it looks
         * like indentation, we can probably optimize a bit...
         */
        char tmp = (char) mTmpChar;
        char[] outputBuffer;
        int outPtr;

        if (tmp == '\r' || tmp == '\n') {
            outPtr = checkPrologIndentation(tmp);
            if (outPtr < 0) {
                return;
            }
            // Above call also initializes the text builder appropriately
            outputBuffer = _textBuilder.getBufferWithoutReset();
        } else {
            outputBuffer = _textBuilder.resetWithEmpty();
            outputBuffer[0] = tmp;
            outPtr = 1;
        }

        int ptr = _inputPtr;

        while (true) {
            if (ptr >= _inputEnd) {
                if (!loadMore()) {
                    break;
                }
                ptr = _inputPtr;
            }
            char c = _inputBuffer[ptr];
            if (c > INT_SPACE) {
                break;
            }
            ++ptr;

            if (c == INT_LF) {
                markLF(ptr);
            } else if (c == INT_CR) {
                if (ptr >= _inputEnd) {
                    if (!loadMore()) { // still need to output the lf
                        if (outPtr >= outputBuffer.length) {
                            outputBuffer = _textBuilder.finishCurrentSegment();
                            outPtr = 0;
                        }
                        outputBuffer[outPtr++] = '\n';
                        break;
                    }
                    ptr = _inputPtr;
                }
                if (_inputBuffer[ptr] == '\n') {
                    ++ptr;
                }
                markLF(ptr);
                c = '\n'; // need to convert to canonical lf
            } else if (c != ' ' && c != '\t') {
                _inputPtr = ptr;
                throwInvalidSpace(c);
            }

            // Ok, can output the char
            if (outPtr >= outputBuffer.length) {
                outputBuffer = _textBuilder.finishCurrentSegment();
                outPtr = 0;
            }
            outputBuffer[outPtr++] = c;
        }

        _inputPtr = ptr;
        _textBuilder.setCurrentLength(outPtr);
    }

    /*
    /**********************************************************************
    /* 2nd level parsing for coalesced text
    /**********************************************************************
     */

    /**
     * Method that gets called after a primary text segment (of type
     * CHARACTERS or CDATA, not applicable to SPACE) has been read in
     * text buffer. Method has to see if the following event would
     * be textual as well, and if so, read it (and any other following
     * textual segments).
     */
    private void finishCoalescedText() throws XMLStreamException {
        while (true) {
            // no matter what, will need (and can get) one char
            if (_inputPtr >= _inputEnd) {
                if (!loadMore()) { // most likely an error, will be handled later on
                    return;
                }
            }

            if (_inputBuffer[_inputPtr] == '<') { // markup of some kind
                /* In worst case, need 3 chars ("<![") all in all to know
                 * if we are getting a CDATA section
                 */
                if ((_inputPtr + 3) >= _inputEnd) {
                    if (!loadAndRetain()) {
                        // probably an error, but will be handled later
                        return;
                    }
                }
                if (_inputBuffer[_inputPtr + 1] != '!' || _inputBuffer[_inputPtr + 2] != '[') {
                    // can't be CDATA, we are done here
                    return;
                }
                // but let's verify it still:
                _inputPtr += 3;
                for (int i = 0; i < 6; ++i) {
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    char c = _inputBuffer[_inputPtr++];
                    if (c != CDATA_STR.charAt(i)) {
                        reportTreeUnexpChar(c, " (expected '" + CDATA_STR.charAt(i) + "' for CDATA section)");
                    }
                }
                finishCoalescedCData();
            } else { // textual (or entity, error etc)
                finishCoalescedCharacters();
                if (_entityPending) {
                    break;
                }
            }
        }
    }

    // note: code mostly copied from 'finishCharacters', just simplified
    // in some places
    private void finishCoalescedCData() throws XMLStreamException {
        final int[] TYPES = sCharTypes.OTHER_CHARS;
        final char[] inputBuffer = _inputBuffer;

        char[] outputBuffer = _textBuilder.getBufferWithoutReset();
        int outPtr = _textBuilder.getCurrentLength();

        /* At this point, space (if any) has been skipped, and we are
         * to parse and store the contents
         */
        main_loop: while (true) {
            char c;
            // Then the tight ascii non-funny-char loop:
            ascii_loop: while (true) {
                int ptr = _inputPtr;
                if (ptr >= _inputEnd) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                }
                if (outPtr >= outputBuffer.length) {
                    outputBuffer = _textBuilder.finishCurrentSegment();
                    outPtr = 0;
                }
                int max = _inputEnd;
                {
                    int max2 = ptr + (outputBuffer.length - outPtr);
                    if (max2 < max) {
                        max = max2;
                    }
                }
                while (ptr < max) {
                    c = inputBuffer[ptr++];
                    if (c <= 0xFF) {
                        if (TYPES[c] != 0) {
                            _inputPtr = ptr;
                            break ascii_loop;
                        }
                    } else if (c >= 0xD800) { // surrogates and 0xFFFE/0xFFFF
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                    outputBuffer[outPtr++] = c;
                }
                _inputPtr = ptr;
            }
            // And then exceptions:
            if (c <= 0xFF) {
                switch (TYPES[c]) {
                    case XmlCharTypes.CT_INVALID:
                        handleInvalidXmlChar(c);
                    case XmlCharTypes.CT_WS_CR: {
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        if (inputBuffer[_inputPtr] == '\n') {
                            ++_inputPtr;
                        }
                        markLF();
                    }
                        c = '\n';
                        break;

                    case XmlCharTypes.CT_WS_LF:
                        markLF();
                        break;

                    case XmlCharTypes.CT_RBRACKET: // close ']]>' marker?
                        /* Ok: let's just parse all consequtive right brackets,
                         * and see if followed by greater-than char. This because
                         * we can only push back at most one char at a time, and
                         * thus can't easily just check a subset
                         */
                        int count = 0; // ignore first bracket
                        char d;

                        do {
                            if (_inputPtr >= _inputEnd) {
                                loadMoreGuaranteed();
                            }
                            d = _inputBuffer[_inputPtr];
                            if (d != ']') {
                                break;
                            }
                            ++_inputPtr;
                            ++count;
                        } while (true);

                        // Was the marker found?
                        boolean ok = (d == '>' && count >= 1);
                        if (ok) {
                            --count;
                        }
                        // Brackets to copy to output?
                        for (; count > 0; --count) {
                            outputBuffer[outPtr++] = ']';
                            if (outPtr >= outputBuffer.length) {
                                outputBuffer = _textBuilder.finishCurrentSegment();
                                outPtr = 0;
                            }
                        }
                        if (ok) {
                            ++_inputPtr; // to consume '>'
                            break main_loop;
                        }
                        break;
                    // default:
                    // Other types are not important here...
                }
            } else {  // high-range, surrogates etc
                if (c < 0xE000) {
                    // if ok, returns second surrogate; otherwise exception
                    char d = checkSurrogate(c);
                    outputBuffer[outPtr++] = c;
                    if (outPtr >= outputBuffer.length) {
                        outputBuffer = _textBuilder.finishCurrentSegment();
                        outPtr = 0;
                    }
                    c = d;
                } else if (c >= 0xFFFE) {
                    c = handleInvalidXmlChar(c);
                }
            }
            // Ok, can output the char; there's room for one char at least
            outputBuffer[outPtr++] = c;
        }
        _textBuilder.setCurrentLength(outPtr);
    }

    // note: code mostly copied from 'finishCharacters', just simplified
    // in some places
    private void finishCoalescedCharacters() throws XMLStreamException {
        // first char can't be from (char) entity (wrt finishCharacters)

        final int[] TYPES = sCharTypes.TEXT_CHARS;
        final char[] inputBuffer = _inputBuffer;

        char[] outputBuffer = _textBuilder.getBufferWithoutReset();
        int outPtr = _textBuilder.getCurrentLength();

        main_loop: while (true) {
            char c;

            ascii_loop: while (true) { // tight loop for ascii chars
                int ptr = _inputPtr;
                if (ptr >= _inputEnd) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                }
                if (outPtr >= outputBuffer.length) {
                    outputBuffer = _textBuilder.finishCurrentSegment();
                    outPtr = 0;
                }
                int max = _inputEnd;
                {
                    int max2 = ptr + (outputBuffer.length - outPtr);
                    if (max2 < max) {
                        max = max2;
                    }
                }
                while (ptr < max) {
                    c = inputBuffer[ptr++];
                    if (c <= 0xFF) {
                        if (TYPES[c] != 0) {
                            _inputPtr = ptr;
                            break ascii_loop;
                        }
                    } else if (c >= 0xD800) { // surrogates and 0xFFFE/0xFFFF
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                    outputBuffer[outPtr++] = c;
                }
                _inputPtr = ptr;
            }
            // And then exceptions:
            if (c <= 0xFF) {
                switch (TYPES[c]) {
                    case XmlCharTypes.CT_INVALID:
                        handleInvalidXmlChar(c);
                    case XmlCharTypes.CT_WS_CR: {
                        int ptr = _inputPtr;
                        if (ptr >= _inputEnd) {
                            loadMoreGuaranteed();
                            ptr = _inputPtr;
                        }
                        if (inputBuffer[ptr] == '\n') {
                            ++_inputPtr;
                        }
                        markLF();
                    }
                        c = '\n';
                        break;

                    case XmlCharTypes.CT_WS_LF:
                        markLF();
                        break;

                    case XmlCharTypes.CT_LT:
                        --_inputPtr;
                        break main_loop;

                    case XmlCharTypes.CT_AMP: {
                        int d = handleEntityInText();
                        if (d == 0) { // unexpandable general parsed entity
                            // _inputPtr set by entity expansion method
                            _entityPending = true;
                            break main_loop;
                        }
                        // Ok; does it need a surrogate though? (over 16 bits)
                        if ((d >> 16) != 0) {
                            d -= 0x10000;
                            outputBuffer[outPtr++] = (char) (0xD800 | (d >> 10));
                            // Need to ensure room for one more char
                            if (outPtr >= outputBuffer.length) {
                                outputBuffer = _textBuilder.finishCurrentSegment();
                                outPtr = 0;
                            }
                            d = (0xDC00 | (d & 0x3FF));
                        }
                        c = (char) d;
                    }
                        break;

                    case XmlCharTypes.CT_RBRACKET: // ']]>'?
                    {
                        // Let's then just count number of brackets --
                        // in case they are not followed by '>'
                        int count = 1;
                        while (true) {
                            if (_inputPtr >= _inputEnd) {
                                loadMoreGuaranteed();
                            }
                            c = inputBuffer[_inputPtr];
                            if (c != ']') {
                                break;
                            }
                            ++_inputPtr; // to skip past bracket
                            ++count;
                        }
                        if (c == '>' && count > 1) {
                            reportIllegalCDataEnd();
                        }
                        // Nope. Need to output all brackets, then; except
                        // for one that can be left for normal output
                        while (count > 1) {
                            outputBuffer[outPtr++] = ']';
                            if (outPtr >= outputBuffer.length) {
                                outputBuffer = _textBuilder.finishCurrentSegment();
                                outPtr = 0;
                            }
                            // Need to ensure room for one more char
                            --count;
                        }
                    }
                        // Can just output the first ']' along normal output
                        c = ']';
                        break;
                    // default:
                    // Other types are not important here...
                }
            } else {  // high-range, surrogates etc
                if (c < 0xE000) {
                    // if ok, returns second surrogate; otherwise exception
                    char d = checkSurrogate(c);
                    outputBuffer[outPtr++] = c;
                    if (outPtr >= outputBuffer.length) {
                        outputBuffer = _textBuilder.finishCurrentSegment();
                        outPtr = 0;
                    }
                    c = d;
                } else if (c >= 0xFFFE) {
                    c = handleInvalidXmlChar(c);
                }
            }
            outputBuffer[outPtr++] = c;
        }
        _textBuilder.setCurrentLength(outPtr);
    }

    /**
     * Method that gets called after a primary text segment (of type
     * CHARACTERS or CDATA, not applicable to SPACE) has been skipped.
     * Method has to see if the following event would
     * be textual as well, and if so, skip it (and any other following
     * textual segments).
     *
     * @return True if we encountered an unexpandable entity
     */
    private boolean skipCoalescedText() throws XMLStreamException {
        while (true) {
            // no matter what, will need (and can get) one char
            if (_inputPtr >= _inputEnd) {
                if (!loadMore()) { // most likely an error, will be handled later on
                    return false;
                }
            }

            if (_inputBuffer[_inputPtr] == '<') { // markup of some kind
                /* In worst case, need 3 chars ("<![") all in all to know
                 * if we are getting a CDATA section
                 */
                if ((_inputPtr + 3) >= _inputEnd) {
                    if (!loadAndRetain()) { // probably an error, but will be handled later
                        return false;
                    }
                }
                if (_inputBuffer[_inputPtr + 1] != '!' || _inputBuffer[_inputPtr + 2] != '[') {
                    // can't be CDATA, we are done here
                    return false;
                }
                // but let's verify it still:
                _inputPtr += 3;
                for (int i = 0; i < 6; ++i) {
                    if (_inputPtr >= _inputEnd) {
                        loadMoreGuaranteed();
                    }
                    char c = _inputBuffer[_inputPtr++];
                    if (c != CDATA_STR.charAt(i)) {
                        reportTreeUnexpChar(c, " (expected '" + CDATA_STR.charAt(i) + "' for CDATA section)");
                    }
                }
                skipCData();
            } else { // textual (or entity, error etc)
                if (skipCharacters()) {
                    return true;
                }
            }
        }
    }

    /*
    /**********************************************************************
    /* 2nd level parsing for skipping content
    /**********************************************************************
     */

    private void skipComment() throws XMLStreamException {
        final int[] TYPES = sCharTypes.OTHER_CHARS;
        final char[] inputBuffer = _inputBuffer;

        while (true) {
            char c;

            // Then the tight ascii non-funny-char loop:
            ascii_loop: while (true) {
                int ptr = _inputPtr;
                int max = _inputEnd;
                if (ptr >= max) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                    max = _inputEnd;
                }
                while (ptr < max) {
                    c = inputBuffer[ptr++];
                    if (c <= 0xFF) {
                        if (TYPES[c] != 0) {
                            _inputPtr = ptr;
                            break ascii_loop;
                        }
                    } else if (c >= 0xD800) { // surrogates and 0xFFFE/0xFFFF
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                }
                _inputPtr = ptr;
            }

            if (c <= 0xFF) {
                switch (TYPES[c]) {
                    case XmlCharTypes.CT_INVALID:
                        handleInvalidXmlChar(c);
                    case XmlCharTypes.CT_WS_CR: {
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        if (inputBuffer[_inputPtr] == '\n') {
                            ++_inputPtr;
                        }
                        markLF();
                    }
                        break;

                    case XmlCharTypes.CT_WS_LF:
                        markLF();
                        break;

                    case XmlCharTypes.CT_HYPHEN: // '-->'?
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        if (_inputBuffer[_inputPtr] == '-') { // ok, must be end then
                            ++_inputPtr;
                            if (_inputPtr >= _inputEnd) {
                                loadMoreGuaranteed();
                            }
                            if (_inputBuffer[_inputPtr++] != '>') {
                                reportDoubleHyphenInComments();
                            }
                            return;
                        }
                        break;
                }

                // default:
                // Other types are not important here...
            }
        }
    }

    private void skipPI() throws XMLStreamException {
        final int[] TYPES = sCharTypes.OTHER_CHARS;
        final char[] inputBuffer = _inputBuffer;

        while (true) {
            char c;

            // Then the tight ascii non-funny-char loop:
            ascii_loop: while (true) {
                int ptr = _inputPtr;
                int max = _inputEnd;
                if (ptr >= max) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                    max = _inputEnd;
                }
                while (ptr < max) {
                    c = inputBuffer[ptr++];
                    if (c <= 0xFF) {
                        if (TYPES[c] != 0) {
                            _inputPtr = ptr;
                            break ascii_loop;
                        }
                    } else if (c >= 0xD800) { // surrogates and 0xFFFE/0xFFFF
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                }
                _inputPtr = ptr;
            }

            if (c <= 0xFF) {
                switch (TYPES[c]) {
                    case XmlCharTypes.CT_WS_CR: {
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        if (inputBuffer[_inputPtr] == CHAR_LF) {
                            ++_inputPtr;
                        }
                        markLF();
                    }
                        break;

                    case XmlCharTypes.CT_WS_LF:
                        markLF();
                        break;

                    case XmlCharTypes.CT_QMARK: // '?>'?
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        if (_inputBuffer[_inputPtr] == '>') {
                            ++_inputPtr;
                            return;
                        }
                        break;
                    // default:
                    // Other types are not important here...
                }
            } else {  // high-range, surrogates etc
                if (c < 0xE000) {
                    // if ok, returns second surrogate; otherwise exception
                    /*char d =*/ checkSurrogate(c);
                } else if (c >= 0xFFFE) {
                    handleInvalidXmlChar(c);
                }
            }
            // skipping, no need to output
        }
    }

    /**
     * @return True, if an unexpanded entity was encountered (and
     *   is now pending)
     */
    private boolean skipCharacters() throws XMLStreamException {
        final int[] TYPES = sCharTypes.TEXT_CHARS;
        final char[] inputBuffer = _inputBuffer;

        while (true) {
            char c;

            // Then the tight ascii non-funny-char loop:
            ascii_loop: while (true) {
                int ptr = _inputPtr;
                int max = _inputEnd;
                if (ptr >= max) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                    max = _inputEnd;
                }
                while (ptr < max) {
                    c = inputBuffer[ptr++];
                    if (c <= 0xFF) {
                        if (TYPES[c] != 0) {
                            _inputPtr = ptr;
                            break ascii_loop;
                        }
                    } else if (c >= 0xD800) { // surrogates and 0xFFFE/0xFFFF
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                }
                _inputPtr = ptr;
            }

            if (c <= 0xFF) {
                switch (TYPES[c]) {
                    case XmlCharTypes.CT_INVALID:
                        handleInvalidXmlChar(c);
                    case XmlCharTypes.CT_WS_CR: {
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        if (inputBuffer[_inputPtr] == CHAR_LF) {
                            ++_inputPtr;
                        }
                        markLF();
                    }
                        break;

                    case XmlCharTypes.CT_WS_LF:
                        markLF();
                        break;

                    case XmlCharTypes.CT_LT:
                        --_inputPtr;
                        return false;

                    case XmlCharTypes.CT_AMP: {
                        int d = handleEntityInText();
                        if (d == 0) { // unexpandable general parsed entity
                            return true;
                        }
                    }
                        break;

                    case XmlCharTypes.CT_RBRACKET: // ']]>'?
                    {
                        // Let's then just count number of brackets --
                        // in case they are not followed by '>'
                        int count = 1;
                        while (true) {
                            if (_inputPtr >= _inputEnd) {
                                loadMoreGuaranteed();
                            }
                            c = inputBuffer[_inputPtr];
                            if (c != ']') {
                                break;
                            }
                            ++_inputPtr; // to skip past bracket
                            ++count;
                        }
                        if (c == '>' && count > 1) {
                            reportIllegalCDataEnd();
                        }
                    }
                        // Can just output the first ']' along normal output
                        break;

                    // default:
                    // Other types are not important here...
                }
            } else {  // high-range, surrogates etc
                if (c < 0xE000) {
                    // if ok, returns second surrogate; otherwise exception
                    /*char d =*/ checkSurrogate(c);
                } else if (c >= 0xFFFE) {
                    handleInvalidXmlChar(c);
                }
            }
        }
    }

    private void skipCData() throws XMLStreamException {
        final int[] TYPES = sCharTypes.OTHER_CHARS;
        final char[] inputBuffer = _inputBuffer;

        while (true) {
            char c;

            // Then the tight ascii non-funny-char loop:
            ascii_loop: while (true) {
                int ptr = _inputPtr;
                int max = _inputEnd;
                if (ptr >= max) {
                    loadMoreGuaranteed();
                    ptr = _inputPtr;
                    max = _inputEnd;
                }
                while (ptr < max) {
                    c = inputBuffer[ptr++];
                    if (c <= 0xFF) {
                        if (TYPES[c] != 0) {
                            _inputPtr = ptr;
                            break ascii_loop;
                        }
                    } else if (c >= 0xD800) { // surrogates and 0xFFFE/0xFFFF
                        _inputPtr = ptr;
                        break ascii_loop;
                    }
                }
                _inputPtr = ptr;
            }

            if (c <= 0xFF) {
                switch (TYPES[c]) {
                    case XmlCharTypes.CT_INVALID:
                        handleInvalidXmlChar(c);
                    case XmlCharTypes.CT_WS_CR: {
                        int ptr = _inputPtr;
                        if (ptr >= _inputEnd) {
                            loadMoreGuaranteed();
                            ptr = _inputPtr;
                        }
                        if (inputBuffer[ptr] == CHAR_LF) {
                            ++ptr;
                            ++_inputPtr;
                        }
                        markLF(ptr);
                    }
                        break;

                    case XmlCharTypes.CT_WS_LF:
                        markLF();
                        break;

                    case XmlCharTypes.CT_RBRACKET: // ']]>'?
                    {
                        // end is nigh?
                        int count = 0;

                        do {
                            if (_inputPtr >= _inputEnd) {
                                loadMoreGuaranteed();
                            }
                            ++count;
                            c = _inputBuffer[_inputPtr++];
                        } while (c == ']');

                        if (c == '>') {
                            if (count > 1) { // gotcha
                                return;
                            }
                            // can still skip plain ']>'...
                        } else {
                            --_inputPtr; // need to push back last char
                        }
                    }
                        break;

                    // default:
                    // Other types are not important here...
                }
            } else {  // high-range, surrogates etc
                if (c < 0xE000) {
                    // if ok, returns second surrogate; otherwise exception
                    /*char d =*/ checkSurrogate(c);
                } else if (c >= 0xFFFE) {
                    handleInvalidXmlChar(c);
                }
            }
        }
    }

    private void skipSpace() throws XMLStreamException {
        // mTmpChar has a space, but it's been checked, can ignore
        int ptr = _inputPtr;

        while (true) {
            if (ptr >= _inputEnd) {
                if (!loadMore()) {
                    break;
                }
                ptr = _inputPtr;
            }
            char c = _inputBuffer[ptr];
            if (c > ' ') {
                break;
            }
            ++ptr;

            if (c == '\n') {
                markLF(ptr);
            } else if (c == '\r') {
                if (ptr >= _inputEnd) {
                    if (!loadMore()) {
                        break;
                    }
                    ptr = _inputPtr;
                }
                if (_inputBuffer[ptr] == '\n') {
                    ++ptr;
                }
                markLF(ptr);
            } else if (c != ' ' && c != '\t') {
                _inputPtr = ptr;
                throwInvalidSpace(c);
            }
        }
        _inputPtr = ptr;
    }

    /*
    /**********************************************************************
    /* Entity/name handling
    /**********************************************************************
     */

    /**
     * @return First byte following skipped white space
     */
    private char skipInternalWs(boolean reqd, String msg) throws XMLStreamException {
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        char c = _inputBuffer[_inputPtr++];
        if (c > INT_SPACE) {
            if (!reqd) {
                return c;
            }
            reportTreeUnexpChar(c, " (expected white space " + msg + ")");
        }
        do {
            // But let's first handle the space we already got:
            if (c == '\n') {
                markLF();
            } else if (c == '\r') {
                if (_inputPtr >= _inputEnd) {
                    loadMoreGuaranteed();
                }
                if (_inputBuffer[_inputPtr] == '\n') {
                    ++_inputPtr;
                }
                markLF();
            } else if (c != ' ' && c != '\t') {
                throwInvalidSpace(c);
            }

            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            c = _inputBuffer[_inputPtr++];
        } while (c <= INT_SPACE);

        return c;
    }

    private void matchAsciiKeyword(String keyw) throws XMLStreamException {
        for (int i = 1, len = keyw.length(); i < len; ++i) {
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            char c = _inputBuffer[_inputPtr++];
            if (c != keyw.charAt(i)) {
                reportTreeUnexpChar(c, " (expected '" + keyw.charAt(i) + "' for " + keyw + " keyword)");
            }
        }
    }

    /**
     *<p>
     * Note: consequtive white space is only considered indentation,
     * if the following token seems like a tag (start/end). This so
     * that if a CDATA section follows, it can be coalesced in
     * coalescing mode. Although we could check if coalescing mode is
     * enabled, this should seldom have significant effect either way,
     * so it removes one possible source of problems in coalescing mode.
     *
     * @return -1, if indentation was handled; offset in the output
     *    buffer, if not
     */
    private int checkInTreeIndentation(char c) throws XMLStreamException {
        if (c == '\r') {
            // First a degenerate case, a lone \r:
            if (_inputPtr >= _inputEnd && !loadMore()) {
                _textBuilder.resetWithIndentation(0, CHAR_SPACE);
                return -1;
            }
            if (_inputBuffer[_inputPtr] == '\n') {
                ++_inputPtr;
            }
        }
        markLF();
        // Then need an indentation char (or start/end tag):
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        c = _inputBuffer[_inputPtr];
        if (c != ' ' && c != '\t') {
            // May still be indentation, if it's lt + non-exclamation mark
            if (c == '<') {
                if ((_inputPtr + 1) < _inputEnd && _inputBuffer[_inputPtr + 1] != '!') {
                    _textBuilder.resetWithIndentation(0, ' ');
                    return -1;
                }
            }
            char[] outputBuffer = _textBuilder.resetWithEmpty();
            outputBuffer[0] = '\n';
            _textBuilder.setCurrentLength(1);
            return 1;
        }
        // So how many do we get?
        ++_inputPtr;
        int count = 1;
        int max = (c == ' ') ? TextBuilder.MAX_INDENT_SPACES : TextBuilder.MAX_INDENT_TABS;
        while (count <= max) {
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            char c2 = _inputBuffer[_inputPtr];
            if (c2 != c) {
                // Has to be followed by a start/end tag...
                if (c2 == '<' && (_inputPtr + 1) < _inputEnd && _inputBuffer[_inputPtr + 1] != '!') {
                    _textBuilder.resetWithIndentation(count, c);
                    return -1;
                }
                break;
            }
            ++_inputPtr;
            ++count;
        }
        // Nope, hit something else, or too long: need to just copy the stuff
        // we know buffer has enough room either way
        char[] outputBuffer = _textBuilder.resetWithEmpty();
        outputBuffer[0] = '\n';
        for (int i = 1; i <= count; ++i) {
            outputBuffer[i] = c;
        }
        count += 1; // to account for leading lf
        _textBuilder.setCurrentLength(count);
        return count;
    }

    /**
     * @return -1, if indentation was handled; offset in the output
     *    buffer, if not
     */
    private int checkPrologIndentation(char c) throws XMLStreamException {
        if (c == '\r') {
            // First a degenerate case, a lone \r:
            if (_inputPtr >= _inputEnd && !loadMore()) {
                _textBuilder.resetWithIndentation(0, CHAR_SPACE);
                return -1;
            }
            if (_inputBuffer[_inputPtr] == '\n') {
                ++_inputPtr;
            }
        }
        markLF();
        // Ok, indentation char?
        if (_inputPtr >= _inputEnd && !loadMore()) {
            _textBuilder.resetWithIndentation(0, CHAR_SPACE);
            return -1;
        }
        c = _inputBuffer[_inputPtr]; // won't advance past the char yet
        if (c != ' ' && c != '\t') {
            // If lt, it's still indentation ok:
            if (c == '<') { // need
                _textBuilder.resetWithIndentation(0, CHAR_SPACE);
                return -1;
            }
            // Nope... something else
            char[] outputBuffer = _textBuilder.resetWithEmpty();
            outputBuffer[0] = '\n';
            _textBuilder.setCurrentLength(1);
            return 1;
        }
        // So how many do we get?
        ++_inputPtr;
        int count = 1;
        int max = (c == ' ') ? TextBuilder.MAX_INDENT_SPACES : TextBuilder.MAX_INDENT_TABS;
        while (true) {
            if (_inputPtr >= _inputEnd && !loadMore()) {
                break;
            }
            if (_inputBuffer[_inputPtr] != c) {
                break;
            }
            ++_inputPtr;
            ++count;
            if (count >= max) { // ok, can't share... but can build it still
                // we know buffer has enough room
                char[] outputBuffer = _textBuilder.resetWithEmpty();
                outputBuffer[0] = '\n';
                for (int i = 1; i <= count; ++i) {
                    outputBuffer[i] = c;
                }
                count += 1; // to account for leading lf
                _textBuilder.setCurrentLength(count);
                return count;
            }
        }
        // Ok, gotcha?
        _textBuilder.resetWithIndentation(count, c);
        return -1;
    }

    private PNameC parsePName(char c) throws XMLStreamException {
        char[] nameBuffer = _nameBuffer;

        /* Let's do just quick sanity check first; a thorough check will be
         * done later on if necessary, now we'll just do the very cheap
         * check to catch extra spaces etc.
         */
        if (c < INT_A) { // lowest acceptable start char, except for ':' that would be allowed in non-ns mode
            throwUnexpectedChar(c, "; expected a name start character");
        }
        nameBuffer[0] = c;
        int hash = c;
        int ptr = 1;

        while (true) {
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            c = _inputBuffer[_inputPtr];
            int d = c;
            if (d < 65) {
                // Ok; "_" (45), "." (46) and "0"-"9"/":" (48 - 57/58) still name chars
                if (d < 45 || d > 58 || d == 47) {
                    // End of name, a single ascii char?
                    PNameC n = _symbols.findSymbol(nameBuffer, 0, ptr, hash);
                    if (n == null) {
                        n = addPName(nameBuffer, ptr, hash);
                    }
                    return n;
                }
            }
            ++_inputPtr;
            if (ptr >= nameBuffer.length) {
                _nameBuffer = nameBuffer = DataUtil.growArrayBy(nameBuffer, nameBuffer.length);
            }
            nameBuffer[ptr++] = c;
            hash = (hash * 31) + d;
        }
    }

    private PNameC addPName(char[] nameBuffer, int nameLen, int hash) throws XMLStreamException {
        // Let's validate completely, now:
        char c = nameBuffer[0];
        int namePtr = 1;
        int last_colon = -1; // where the colon is

        if (c < 0xD800 || c >= 0xE000) {
            if (!XmlChars.is10NameStartChar(c)) {
                reportInvalidNameChar(c, 0);
            }
        } else {
            if (nameLen == 1) {
                reportInvalidFirstSurrogate(c);
            }
            // Only returns if ok; throws exception otherwise
            checkSurrogateNameChar(c, nameBuffer[1], 0);
            ++namePtr;
        }

        for (; namePtr < nameLen; ++namePtr) {
            c = nameBuffer[namePtr];

            if (c < 0xD800 || c >= 0xE000) {
                if (c == ':') {
                    if (last_colon >= 0) {
                        reportMultipleColonsInName();
                    }
                    last_colon = namePtr;
                } else {
                    if (!XmlChars.is10NameChar(c)) {
                        reportInvalidNameChar(c, namePtr);
                    }
                }
            } else {
                if ((namePtr + 1) >= nameLen) { // unpaired surrogate
                    reportInvalidFirstSurrogate(c);
                }
                checkSurrogateNameChar(c, nameBuffer[namePtr + 1], namePtr);
            }
        }
        return _symbols.addSymbol(nameBuffer, 0, nameLen, hash);
    }

    private void parsePublicId(char quoteChar) throws XMLStreamException {
        char[] outputBuffer = _nameBuffer;
        int outPtr = 0;
        final int[] TYPES = XmlCharTypes.PUBID_CHARS;
        boolean addSpace = false;

        while (true) {
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            // Easier to check without char type table, first:
            char c = _inputBuffer[_inputPtr++];
            if (c == quoteChar) {
                break;
            }
            if ((c > 0xFF) || TYPES[c] != XmlCharTypes.PUBID_OK) {
                throwUnexpectedChar(c, " in public identifier");
            }

            // White space? Needs to be coalecsed
            if (c <= INT_SPACE) {
                addSpace = true;
                continue;
            }
            if (addSpace) {
                if (outPtr >= outputBuffer.length) {
                    outputBuffer = _textBuilder.finishCurrentSegment();
                    outPtr = 0;
                }
                outputBuffer[outPtr++] = ' ';
                addSpace = false;
            }
            if (outPtr >= outputBuffer.length) {
                _nameBuffer = outputBuffer = DataUtil.growArrayBy(outputBuffer, outputBuffer.length);
                outPtr = 0;
            }
            outputBuffer[outPtr++] = c;
        }
    }

    private String parseSystemId(char quoteChar) throws XMLStreamException {
        char[] outputBuffer = _nameBuffer;
        int outPtr = 0;
        // attribute types are closest matches, so let's use them
        final int[] TYPES = sCharTypes.ATTR_CHARS;
        //boolean spaceToAdd = false;

        main_loop: while (true) {
            if (_inputPtr >= _inputEnd) {
                loadMoreGuaranteed();
            }
            char c = _inputBuffer[_inputPtr++];
            if (TYPES[c] != 0) {
                switch (TYPES[c]) {
                    case XmlCharTypes.CT_INVALID:
                        handleInvalidXmlChar(c);
                    case XmlCharTypes.CT_WS_CR: {
                        if (_inputPtr >= _inputEnd) {
                            loadMoreGuaranteed();
                        }
                        if (_inputBuffer[_inputPtr] == '\n') {
                            ++_inputPtr;
                        }
                        markLF();
                    }
                        c = '\n';
                        break;

                    case XmlCharTypes.CT_WS_LF:
                        markLF();
                        break;

                    case XmlCharTypes.CT_ATTR_QUOTE:
                        if (c == quoteChar) {
                            break main_loop;
                        }
                }
            }
            if (outPtr >= outputBuffer.length) {
                _nameBuffer = outputBuffer = DataUtil.growArrayBy(outputBuffer, outputBuffer.length);
                outPtr = 0;
            }
            outputBuffer[outPtr++] = c;
        }
        return new String(outputBuffer, 0, outPtr);
    }

    /*
    /**********************************************************************
    /* Other parsing helper methods
    /**********************************************************************
     */

    /**
     * This method is called to verify that a surrogate
     * pair found describes a legal surrogate pair (ie. expands
     * to a legal XML char)
     */
    private char checkSurrogate(char firstChar) throws XMLStreamException {
        if (firstChar >= 0xDC00) {
            reportInvalidFirstSurrogate(firstChar);
        }
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        char sec = _inputBuffer[_inputPtr++];
        if (sec < 0xDC00 || sec >= 0xE000) {
            reportInvalidSecondSurrogate(sec);
        }
        // And the composite, is it ok?
        return sec;
    }

    private void checkSurrogateNameChar(char firstChar, char sec, int index) throws XMLStreamException {
        if (firstChar >= 0xDC00) {
            reportInvalidFirstSurrogate(firstChar);
        }
        if (sec < 0xDC00 || sec >= 0xE000) {
            reportInvalidSecondSurrogate(sec);
        }
        // And the composite, is it ok?
        int val = ((firstChar - 0xD800) << 10) + 0x10000;
        reportInvalidNameChar(val, index);
    }

    /**
     * This method is similar to <code>checkSurrogate</code>, but
     * returns the actual character code encoded by the surrogate
     * pair. This is needed if further validation rules (such as name
     * charactert checks) are to be done.
     */
    private int decodeSurrogate(char firstChar) throws XMLStreamException {
        if (firstChar >= 0xDC00) {
            reportInvalidFirstSurrogate(firstChar);
        }
        if (_inputPtr >= _inputEnd) {
            loadMoreGuaranteed();
        }
        char sec = _inputBuffer[_inputPtr++];
        if (sec < 0xDC00 || sec >= 0xE000) {
            reportInvalidSecondSurrogate(sec);
        }
        // And the composite, is it ok?
        return ((firstChar - 0xD800) << 10) + 0x10000;
    }

    private void reportInvalidFirstSurrogate(char ch) throws XMLStreamException {
        reportInputProblem(
            "Invalid surrogate character (code 0x" + Integer.toHexString(ch) + "): can not start a surrogate pair");
    }

    private void reportInvalidSecondSurrogate(char ch) throws XMLStreamException {
        reportInputProblem("Invalid surrogate character (code " + Integer.toHexString(ch)
            + "): is not legal as the second part of a surrogate pair");
    }

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
    private PNameC bindName(PNameC name, String prefix) {
        // First, do we have a cache, to perhaps find bound name from?
        if (_nsBindingCache != null) {
            PNameC cn = _nsBindingCache[name.unboundHashCode() & BIND_CACHE_MASK];
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
            PNameC bn = name.createBoundName(b);
            if (_nsBindingCache == null) {
                if (++_nsBindMisses < BIND_MISSES_TO_ACTIVATE_CACHE) {
                    return bn;
                }
                _nsBindingCache = new PNameC[BIND_CACHE_SIZE];
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
    private NsBinding findOrCreateBinding(String prefix) {
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
    private void bindNs(PNameC name, String uri) throws XMLStreamException {
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
    private void checkImmutableBinding(String prefix, String uri) throws XMLStreamException {
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
    private void loadMoreGuaranteed() throws XMLStreamException {
        loadMoreGuaranteed(_currToken);
    }

    private void loadMoreGuaranteed(int tt) throws XMLStreamException {
        if (!loadMore()) {
            reportInputProblem("Unexpected end-of-input when trying to parse " + ErrorConsts.tokenTypeDesc(tt));
        }
    }

    /*
    /**********************************************************************
    /* Helper methods for sub-classes, error reporting
    /**********************************************************************
     */

    private void reportInputProblem(String msg) throws XMLStreamException {
        /* 29-Mar-2008, tatus: Not sure if these are all Well-Formedness
         *   Constraint (WFC) violations? They should be... ?
         */
        throw new StreamExceptionBase(msg, getCurrentLocation());
    }

    /**
     * Method called when a call to expand an entity within attribute
     * value fails to expand it.
     */
    private void reportUnexpandedEntityInAttr(boolean isNsDecl) throws XMLStreamException {
        reportInputProblem("Unexpanded ENTITY_REFERENCE (" + _tokenName + ") in "
            + (isNsDecl ? "namespace declaration" : "attribute value"));
    }

    private void reportPrologUnexpElement(boolean isProlog, int ch) throws XMLStreamException {
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

    private void reportPrologUnexpChar(boolean isProlog, int ch, String msg) throws XMLStreamException {
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

    private void reportTreeUnexpChar(int ch, String msg) throws XMLStreamException {
        String fullMsg = ErrorConsts.SUFFIX_IN_TREE;
        if (msg != null) {
            fullMsg += msg;
        }
        throwUnexpectedChar(ch, fullMsg);
    }

    private void reportInvalidNameChar(int ch, int index) throws XMLStreamException {
        if (ch == INT_COLON) {
            reportInputProblem(
                "Invalid colon in name: at most one colon allowed in element/attribute names, and none in PI target or entity names");
        }
        if (index == 0) {
            reportInputProblem("Invalid name start character (0x" + Integer.toHexString(ch) + ")");
        }
        reportInputProblem("Invalid name character (0x" + Integer.toHexString(ch) + ")");
    }

    private void reportInvalidXmlChar(int ch) throws XMLStreamException {
        if (ch == 0) {
            reportInputProblem("Invalid null character");
        }
        if (ch < 32) {
            reportInputProblem("Invalid white space character (0x" + Integer.toHexString(ch) + ")");
        }
        reportInputProblem("Invalid xml content character (0x" + Integer.toHexString(ch) + ")");
    }

    /**
     * Called when there's an unexpected char after PI target (non-ws,
     * not part of {@code '?>'} end marker
     */
    private void reportMissingPISpace(int ch) throws XMLStreamException {
        throwUnexpectedChar(ch, ": expected either white space, or closing '?>'");
    }

    private void reportDoubleHyphenInComments() throws XMLStreamException {
        reportInputProblem("String '--' not allowed in comment (missing '>'?)");
    }

    private void reportMultipleColonsInName() throws XMLStreamException {
        reportInputProblem("Multiple colons not allowed in names");
    }

    private void reportEntityOverflow() throws XMLStreamException {
        reportInputProblem("Illegal character entity: value higher than max allowed (0x"
            + Integer.toHexString(MAX_UNICODE_CHAR) + ")");
    }

    private void reportInvalidNsIndex(int index) {
        /* 24-Jun-2006, tatus: Stax API doesn't specify what (if anything)
         *   should be thrown. Ref. Impl. throws IndexOutOfBounds, which
         *   makes sense; could also throw IllegalArgumentException.
         */
        throw new IndexOutOfBoundsException("Illegal namespace declaration index, " + index
            + ", current START_ELEMENT/END_ELEMENT has " + getNsCount() + " declarations");
    }

    private void reportUnboundPrefix(PNameC name, boolean isAttr) throws XMLStreamException {
        reportInputProblem("Unbound namespace prefix '" + name.getPrefix() + "' (for "
            + (isAttr ? "attribute" : "element") + " name '" + name.getPrefixedName() + "')");
    }

    private void reportDuplicateNsDecl(String prefix) throws XMLStreamException {
        if (prefix == null) {
            reportInputProblem("Duplicate namespace declaration for the default namespace");
        } else {
            reportInputProblem("Duplicate namespace declaration for prefix '" + prefix + "'");
        }
    }

    private void reportIllegalNsDecl(String prefix) throws XMLStreamException {
        reportInputProblem("Illegal namespace declaration: can not re-bind prefix '" + prefix + "'");
    }

    private void reportIllegalNsDecl(String prefix, String uri) throws XMLStreamException {
        reportInputProblem(
            "Illegal namespace declaration: can not bind URI '" + uri + "' to prefix other than '" + prefix + "'");
    }

    private void reportUnexpectedEndTag(String expName) throws XMLStreamException {
        reportInputProblem("Unexpected end tag: expected </" + expName + ">");
    }

    // Thrown when ']]>' found in text content
    private void reportIllegalCDataEnd() throws XMLStreamException {
        reportInputProblem("String ']]>' not allowed in textual content, except as the end marker of CDATA section");
    }

    private void throwUnexpectedChar(int i, String msg) throws XMLStreamException {
        // But first, let's check illegals
        if (i < 32 && i != '\r' && i != '\n' && i != '\t') {
            throwInvalidSpace(i);
        }
        char c = (char) i;
        String excMsg = "Unexpected character " + XmlChars.getCharDesc(c) + msg;
        reportInputProblem(excMsg);
    }

    private void throwNullChar() throws XMLStreamException {
        reportInputProblem("Illegal character (NULL, unicode 0) encountered: not valid in any content");
    }

    private char handleInvalidXmlChar(int i) throws XMLStreamException {
        char c = (char) i;
        if (c == CHAR_NULL) {
            throwNullChar();
        }

        reportInputProblem("Illegal XML character (" + XmlChars.getCharDesc(c) + ")");

        //will not reach this block
        return (char) i;
    }

    private void throwInvalidSpace(int i) throws XMLStreamException {
        char c = (char) i;
        if (c == CHAR_NULL) {
            throwNullChar();
        }
        reportInputProblem("Illegal character (" + XmlChars.getCharDesc(c) + ")");
    }

    /*
    /**********************************************************************
    /* Input loading
    /**********************************************************************
     */

    private boolean loadMore() throws XMLStreamException {
        _pastBytesOrChars += _inputEnd;
        _rowStartOffset -= _inputEnd;
        _inputPtr = 0;

        try {
            int count = _in.read(_inputBuffer, 0, _inputBuffer.length);
            if (count < 1) {
                _inputEnd = 0;
                if (count == 0) {
                    /* Sanity check; should never happen with correctly written
                     * InputStreams...
                     */
                    reportInputProblem("Reader returned 0 bytes, even when asked to read up to " + _inputBuffer.length);
                }
                return false;
            }
            _inputEnd = count;
            return true;
        } catch (IOException ioe) {
            throw new StreamExceptionBase(ioe);
        }
    }

    private char loadOne() throws XMLStreamException {
        if (!loadMore()) {
            reportInputProblem("Unexpected end-of-input when trying to parse "
                + ErrorConsts.tokenTypeDesc(javax.xml.stream.XMLStreamConstants.COMMENT));
        }
        return _inputBuffer[_inputPtr++];
    }

    private boolean loadAndRetain() throws XMLStreamException {
        _pastBytesOrChars += _inputPtr;
        _rowStartOffset -= _inputPtr;

        int remaining = (_inputEnd - _inputPtr); // must be > 0
        System.arraycopy(_inputBuffer, _inputPtr, _inputBuffer, 0, remaining);
        _inputPtr = 0;
        _inputEnd = remaining; // temporarily set to cover copied stuff

        try {
            do {
                int max = _inputBuffer.length - _inputEnd;
                int count = _in.read(_inputBuffer, _inputEnd, max);
                if (count < 1) {
                    if (count == 0) {
                        // Sanity check, should never happen with non-buggy readers/stream
                        reportInputProblem("Reader returned 0 bytes, even when asked to read up to " + max);
                    }
                    return false;
                }
                _inputEnd += count;
            } while (_inputEnd < 3);
            return true;
        } catch (IOException ioe) {
            throw new StreamExceptionBase(ioe);
        }
    }
}
