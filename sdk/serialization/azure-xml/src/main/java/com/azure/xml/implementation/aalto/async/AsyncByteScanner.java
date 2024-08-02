// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.async;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import com.azure.xml.implementation.aalto.AsyncInputFeeder;
import com.azure.xml.implementation.aalto.AsyncXMLStreamReader;
import com.azure.xml.implementation.aalto.impl.ErrorConsts;
import com.azure.xml.implementation.aalto.in.ByteBasedPNameTable;
import com.azure.xml.implementation.aalto.in.ByteBasedScanner;
import com.azure.xml.implementation.aalto.in.PName;
import com.azure.xml.implementation.aalto.in.ReaderConfig;
import com.azure.xml.implementation.aalto.util.CharsetNames;
import com.azure.xml.implementation.aalto.util.DataUtil;
import com.azure.xml.implementation.aalto.util.XmlCharTypes;

public abstract class AsyncByteScanner extends ByteBasedScanner implements AsyncInputFeeder {
    protected final static int EVENT_INCOMPLETE = AsyncXMLStreamReader.EVENT_INCOMPLETE;

    /*
    /**********************************************************************
    /* State consts
    /**********************************************************************
     */

    /**
     * Default starting state for many events/contexts -- nothing has been
     * seen so far, no  event incomplete. Not used for all event types.
     */
    protected final static int STATE_DEFAULT = 0;

    // // // States for prolog/epilog major state:

    /**
     * State in which a less-than sign has been seen
     */
    protected final static int STATE_PROLOG_INITIAL = 1; // State before document when we may get xml declaration
    protected final static int STATE_PROLOG_SEEN_LT = 2; // "<" seen after xml declaration
    protected final static int STATE_PROLOG_DECL = 3; // "<!" seen after xml declaration

    // // // States for in-tree major state:

    protected final static int STATE_TREE_SEEN_LT = 1; // "<" seen
    protected final static int STATE_TREE_SEEN_AMP = 2; // "&" seen
    protected final static int STATE_TREE_SEEN_EXCL = 3; // "<!" seen
    protected final static int STATE_TREE_SEEN_SLASH = 4; // "</" seen
    protected final static int STATE_TREE_NUMERIC_ENTITY_START = 5; // "&#" and part of value
    protected final static int STATE_TREE_NAMED_ENTITY_START = 6; // "&" and part of name

    // // // States within event types (STATE_DEFAULT is shared):

    // XML declaration parsing
    protected final static int STATE_XMLDECL_AFTER_XML = 1; // "<?xml", need space
    protected final static int STATE_XMLDECL_BEFORE_VERSION = 2; // "<?xml ", can have more spaces
    protected final static int STATE_XMLDECL_VERSION = 3; // "<?xml ", part of "version"
    protected final static int STATE_XMLDECL_AFTER_VERSION = 4; // "<?xml version", need space or '='
    protected final static int STATE_XMLDECL_VERSION_EQ = 5; // "<?xml version=", need space or quote
    protected final static int STATE_XMLDECL_VERSION_VALUE = 6; // parsing version value
    protected final static int STATE_XMLDECL_AFTER_VERSION_VALUE = 7; // version got; need space or '?'
    protected final static int STATE_XMLDECL_BEFORE_ENCODING = 8; // version, value, space got, need '?' or 'e'
    protected final static int STATE_XMLDECL_ENCODING = 9; // parsing "encoding"
    protected final static int STATE_XMLDECL_AFTER_ENCODING = 10; // 'encoding' got, need space or '='
    protected final static int STATE_XMLDECL_ENCODING_EQ = 11; // "encoding="
    protected final static int STATE_XMLDECL_ENCODING_VALUE = 12; // parsing encoding value
    protected final static int STATE_XMLDECL_AFTER_ENCODING_VALUE = 13; // encoding+value gotten; need space or '?'
    protected final static int STATE_XMLDECL_BEFORE_STANDALONE = 14; // after encoding+value+space; get '?' or 's'
    protected final static int STATE_XMLDECL_STANDALONE = 15; // parsing "standalone"
    protected final static int STATE_XMLDECL_AFTER_STANDALONE = 16; // 'standalone' got, need space or '='
    protected final static int STATE_XMLDECL_STANDALONE_EQ = 17; // "standalone="
    protected final static int STATE_XMLDECL_STANDALONE_VALUE = 18; // encoding+value gotten; need space or '?'
    protected final static int STATE_XMLDECL_AFTER_STANDALONE_VALUE = 19; // encoding+value gotten; need space or '?'
    protected final static int STATE_XMLDECL_ENDQ = 20; // "?" at the end of declaration

    // DOCTYPE declaration parsing
    protected final static int STATE_DTD_DOCTYPE = 1; // part of "DOCTYPE"
    protected final static int STATE_DTD_AFTER_DOCTYPE = 2; // "DOCTYPE", need space
    protected final static int STATE_DTD_BEFORE_ROOT_NAME = 3; // optional space before root name
    protected final static int STATE_DTD_ROOT_NAME = 4; // part of root name
    protected final static int STATE_DTD_AFTER_ROOT_NAME = 5; // root name gotten; need a space or '>'
    protected final static int STATE_DTD_BEFORE_IDS = 6; // before "PUBLIC" or "SYSTEM" token
    protected final static int STATE_DTD_PUBLIC_OR_SYSTEM = 7; // parsing "PUBLIC" or "SYSTEM"
    protected final static int STATE_DTD_AFTER_PUBLIC = 8; // "PUBLIC" found, need space
    protected final static int STATE_DTD_AFTER_SYSTEM = 9; // "SYSTEM" found, need space
    protected final static int STATE_DTD_BEFORE_PUBLIC_ID = 10; // after "PUBLIC", space, need quoted public id
    protected final static int STATE_DTD_PUBLIC_ID = 11; // parsing public ID
    protected final static int STATE_DTD_AFTER_PUBLIC_ID = 12; // public ID parsed, need space
    protected final static int STATE_DTD_BEFORE_SYSTEM_ID = 13; // about to parse quoted system id
    protected final static int STATE_DTD_SYSTEM_ID = 14; // parsing system ID
    protected final static int STATE_DTD_AFTER_SYSTEM_ID = 15; // after system ID, optional space, '>' or int subset
    protected final static int STATE_DTD_INT_SUBSET = 16; // parsing internal subset

    protected final static int STATE_DTD_EXPECT_CLOSING_GT = 50; // ']' gotten that should be followed by '>'

    // For CHARACTERS, default is the basic (and only)

    // just seen "&"
    protected final static int STATE_TEXT_AMP = 4;
    // just seen "&#"
    //    protected final static int STATE_TEXT_AMP_AND_HASH = 5;
    // seen '&' and partial name:
    protected final static int STATE_TEXT_AMP_NAME = 6;

    // For comments, STATE_DEFAULT means "<!-" has been seen
    protected final static int STATE_COMMENT_CONTENT = 1; // "<!--"
    protected final static int STATE_COMMENT_HYPHEN = 2; // content, and one '-'
    protected final static int STATE_COMMENT_HYPHEN2 = 3; // content, "--"

    // For cdata, STATE_DEFAULT means that just "<![" has been seen
    protected final static int STATE_CDATA_CONTENT = 1; // start marker seen, maybe some content
    protected final static int STATE_CDATA_C = 2; // "<![C"
    protected final static int STATE_CDATA_CD = 3; // "<![CD"
    protected final static int STATE_CDATA_CDA = 4; // "<![CDA"
    protected final static int STATE_CDATA_CDAT = 5; // "<![CDAT"
    protected final static int STATE_CDATA_CDATA = 6; // "<![CDATA"

    // For PIs, default means that '<?' has been seen, nothing else

    // (note: funny ordering, starting with "quick path" entries)
    protected final static int STATE_PI_AFTER_TARGET = 1; // "<?", target ?>
    protected final static int STATE_PI_AFTER_TARGET_WS = 2; // "<?", target, ws
    protected final static int STATE_PI_AFTER_TARGET_QMARK = 3; // "<?", target, "?"
    protected final static int STATE_PI_IN_TARGET = 4; // "<?", part of target
    protected final static int STATE_PI_IN_DATA = 5; // "<?", target, ws, part of data

    // For start element, DEFAULT means that only '<' has been seen
    protected final static int STATE_SE_ELEM_NAME = 1; // "<" and part of name
    protected final static int STATE_SE_SPACE_OR_END = 2; // after elem name or attr, but need space
    protected final static int STATE_SE_SPACE_OR_ATTRNAME = 3; // after elem/attr and space

    protected final static int STATE_SE_ATTR_NAME = 4; // in attribute name
    protected final static int STATE_SE_SPACE_OR_EQ = 5;
    protected final static int STATE_SE_SPACE_OR_ATTRVALUE = 6;
    protected final static int STATE_SE_ATTR_VALUE_NORMAL = 7;
    protected final static int STATE_SE_ATTR_VALUE_NSDECL = 8;
    protected final static int STATE_SE_SEEN_SLASH = 9;

    // For END_ELEMENT, default means we are parsing name
    protected final static int STATE_EE_NEED_GT = 1;

    /*
    /**********************************************************************
    /* Markers to use for 'pending' character, if
    /* not multi-byte UTF character
    /**********************************************************************
     */

    // Marker when dealing with general CR+LF pair
    protected final static int PENDING_STATE_CR = -1;

    // Parsing of possible XML declaration
    protected final static int PENDING_STATE_XMLDECL_LT = -5; // "<" at start of doc
    protected final static int PENDING_STATE_XMLDECL_LTQ = -6; // "<?" at start of doc
    protected final static int PENDING_STATE_XMLDECL_TARGET = -7; // "<?" at start of doc, part of name

    // Processing Instruction parsing:
    protected final static int PENDING_STATE_PI_QMARK = -15;

    // Comment parsing
    protected final static int PENDING_STATE_COMMENT_HYPHEN1 = -20;
    protected final static int PENDING_STATE_COMMENT_HYPHEN2 = -21;

    // CData parsing
    protected final static int PENDING_STATE_CDATA_BRACKET1 = -30;
    protected final static int PENDING_STATE_CDATA_BRACKET2 = -31;

    protected final static int PENDING_STATE_ENT_SEEN_HASH = -70; // seen &#
    protected final static int PENDING_STATE_ENT_SEEN_HASH_X = -71; // seen &#x
    protected final static int PENDING_STATE_ENT_IN_DEC_DIGIT = -72; // seen &# and 1 or more decimals
    protected final static int PENDING_STATE_ENT_IN_HEX_DIGIT = -73; // seen &#x and 1 or more hex digits
    //    final static int PENDING_STATE_ENT_IN_NAME = -; // seen & and part of the name

    // partially handled entities within attribute/ns values use pending state as well
    protected final static int PENDING_STATE_ATTR_VALUE_AMP = -60;
    protected final static int PENDING_STATE_ATTR_VALUE_AMP_HASH = -61;
    protected final static int PENDING_STATE_ATTR_VALUE_AMP_HASH_X = -62;
    protected final static int PENDING_STATE_ATTR_VALUE_ENTITY_NAME = -63;
    protected final static int PENDING_STATE_ATTR_VALUE_DEC_DIGIT = -64;
    protected final static int PENDING_STATE_ATTR_VALUE_HEX_DIGIT = -65;

    protected final static int PENDING_STATE_TEXT_AMP = -80; // seen &
    protected final static int PENDING_STATE_TEXT_AMP_HASH = -81; // seen &#
    protected final static int PENDING_STATE_TEXT_DEC_ENTITY = -82; // seen &# and 1 or more decimals
    protected final static int PENDING_STATE_TEXT_HEX_ENTITY = -83; // seen &#x and 1 or more hex digits
    protected final static int PENDING_STATE_TEXT_IN_ENTITY = -84; // seen & and part of entity name
    protected final static int PENDING_STATE_TEXT_BRACKET1 = -85; // seen ]
    protected final static int PENDING_STATE_TEXT_BRACKET2 = -86; // seen ]]

    /*
    /**********************************************************************
    /* Decoding, symbol handling
    /**********************************************************************
     */

    /**
     * This is a simple container object that is used to access the
     * decoding tables for characters. Indirection is needed since
     * we actually support multiple utf-8 compatible encodings, not
     * just utf-8 itself.
     *<p>
     * NOTE: non-final due to xml declaration handling occurring later.
     */
    protected XmlCharTypes _charTypes;

    /**
     * For now, symbol table contains prefixed names. In future it is
     * possible that they may be split into prefixes and local names?
     *<p>
     * NOTE: non-final for async scanners
     */
    protected ByteBasedPNameTable _symbols;

    /**
     * This buffer is used for name parsing. Will be expanded if/as
     * needed; 32 ints can hold names 128 ascii chars long.
     */
    protected int[] _quadBuffer = new int[32];

    /*
    /**********************************************************************
    /* General state tracking
    /**********************************************************************
     */

    /**
     * Due to asynchronous nature of parsing, we may know what
     * event we are trying to parse, even if it's not yet
     * complete. Type of that event is stored here.
     */
    protected int _nextEvent = EVENT_INCOMPLETE;

    /**
     * In addition to the event type, there is need for additional
     * state information
     */
    protected int _state;

    /**
     * For token/state combinations that are 'shared' between
     * events (or embedded in them), this is where the surrounding
     * event state is retained.
     */
    protected int _surroundingEvent = EVENT_INCOMPLETE;

    /**
     * There are some multi-byte combinations that must be handled
     * as a unit: CR+LF linefeeds, multi-byte UTF-8 characters, and
     * multi-character end markers for comments and PIs.
     * Since they can be split across input buffer
     * boundaries, first byte(s) may need to be temporarily stored.
     *<p>
     * If so, this int will store byte(s), in little-endian format
     * (that is, first pending byte is at 0x000000FF, second [if any]
     * at 0x0000FF00, and third at 0x00FF0000). This can be
     * (and is) used to figure out actual number of bytes pending,
     * for multi-byte (UTF-8) character decoding.
     *<p>
     * Note: it is assumed that if value is 0, there is no data.
     * Thus, if 0 needed to be added pending, it has to be masked.
     */
    protected int _pendingInput = 0;

    /**
     * Flag that is sent when calling application indicates that there will
     * be no more input to parse.
     */
    protected boolean _endOfInput = false;

    /*
    /**********************************************************************
    /* Name/entity parsing state
    /**********************************************************************
     */

    /**
     * Number of complete quads parsed for current name (quads
     * themselves are stored in {@link #_quadBuffer}).
     */
    protected int _quadCount;

    /**
     * Bytes parsed for the current, incomplete, quad
     */
    protected int _currQuad;

    /**
     * Number of bytes pending/buffered, stored in {@link #_currQuad}
     */
    protected int _currQuadBytes = 0;

    /**
     * Entity value accumulated so far
     */
    protected int _entityValue = 0;

    /*
    /**********************************************************************
    /* (Start) element parsing state
    /**********************************************************************
     */

    protected boolean _elemAllNsBound;

    protected boolean _elemAttrCount;

    protected byte _elemAttrQuote;

    protected PName _elemAttrName;

    /**
     * Pointer for the next character of currently being parsed value
     * within attribute value buffer
     */
    protected int _elemAttrPtr;

    /**
     * Pointer for the next character of currently being parsed namespace
     * URI for the current namespace declaration
     */
    protected int _elemNsPtr;

    /*
    /**********************************************************************
    /* Other state
    /**********************************************************************
     */

    /**
     * Flag that indicates whether we are inside a declaration during parsing
     * of internal DTD subset.
     */
    protected boolean _inDtdDeclaration;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    protected AsyncByteScanner(ReaderConfig cfg) {
        super(cfg);
        // 03-Apr-2018, tatu: Can not yet fetch `_charTypes` or `_symbols` since we
        //   do not necessarily know actual encoding from XML declaration
        //        _charTypes = cfg.getCharTypes();
        //        _symbols = cfg.getBBSymbols();
    }

    /**
     * Initialization method to call when encoding has been definitely figured out,
     * from XML declarations, or, from lack of one (using defaults).
     *
     * @since 1.1.1
     */
    protected void _activateEncoding() {
        // 04-Apr-2018, tatu: Not sure if we should try to enforce; gets tricky so for now
        //    simply make first call stick
        if (_symbols == null) {
            _charTypes = _config.getCharTypes();
            _symbols = _config.getBBSymbols();
        }
    }

    @Override
    public void endOfInput() {
        _endOfInput = true;
    }

    @Override
    protected void _releaseBuffers() {
        super._releaseBuffers();
        if ((_symbols != null) && _symbols.maybeDirty()) {
            _config.updateBBSymbols(_symbols);
        }
    }

    /**
     * Since the async scanner has no access to whatever passes content,
     * there is no input source in same sense as with blocking scanner;
     * and there is nothing to close. But we can at least mark input
     * as having ended.
     */
    @Override
    protected void _closeSource() throws IOException {
        // nothing to do, we are done.
        _endOfInput = true;
    }

    /*
    /**********************************************************************
    /* Shared helper methods
    /**********************************************************************
     */

    protected void verifyAndSetXmlVersion() throws XMLStreamException {
        if (_textBuilder.equalsString("1.0")) {
            _config.setXmlVersion("1.0");
        } else if (_textBuilder.equalsString("1.1")) {
            _config.setXmlVersion("1.1");
        } else {
            reportInputProblem(
                "Unrecognized XML version '" + _textBuilder.contentsAsString() + "' (expected '1.0' or '1.1')");
        }
    }

    protected void verifyAndSetXmlEncoding() throws XMLStreamException {
        String enc = CharsetNames.normalize(_textBuilder.contentsAsString());
        if ((!CharsetNames.CS_UTF8.equals(enc))
            && (!CharsetNames.CS_US_ASCII.equals(enc))
            && (!CharsetNames.CS_ISO_LATIN1.equals(enc))) {
            reportInputProblem("Unsupported encoding '" + enc + "': only UTF-8 and US-ASCII support by async parser");
        }
        // 03-Apr-2018, tatu: Need to overwrite default (UTF-8) if declared otherwise.
        //    And besides changing configs need to force use of new symbol tables, too...
        _config.setXmlEncoding(enc);
        if (enc != null) {
            _config.setActualEncoding(enc);
        }
        _charTypes = _config.getCharTypes();
    }

    protected void verifyAndSetXmlStandalone() throws XMLStreamException {
        if (_textBuilder.equalsString("yes")) {
            _config.setXmlStandalone(Boolean.TRUE);
        } else if (_textBuilder.equalsString("no")) {
            _config.setXmlStandalone(Boolean.FALSE);
        } else {
            reportInputProblem(
                "Invalid standalone value '" + _textBuilder.contentsAsString() + "': can only use 'yes' and 'no'");
        }
    }

    protected void verifyAndSetPublicId() throws XMLStreamException {
        _publicId = _textBuilder.contentsAsString();
    }

    protected void verifyAndSetSystemId() throws XMLStreamException {
        _systemId = _textBuilder.contentsAsString();
    }

    /*
    /**********************************************************************
    /* Content accessors for less performance-critical sections
    /**********************************************************************
     */

    protected abstract byte _currentByte() throws XMLStreamException;

    protected abstract byte _nextByte() throws XMLStreamException;

    protected abstract byte _prevByte() throws XMLStreamException;

    /*
    /**********************************************************************
    /* Abstract methods for subclasses to implement wrt prolog/epilog
    /**********************************************************************
     */

    protected abstract int handlePI() throws XMLStreamException;

    protected abstract boolean handleDTDInternalSubset(boolean init) throws XMLStreamException;

    protected abstract int handleComment() throws XMLStreamException;

    protected abstract int handleStartElementStart(byte b) throws XMLStreamException;

    protected abstract int handleStartElement() throws XMLStreamException;

    protected abstract PName parsePName() throws XMLStreamException;

    protected abstract PName parseNewName(byte b) throws XMLStreamException;

    protected abstract boolean asyncSkipSpace() throws XMLStreamException;

    protected abstract boolean handlePartialCR() throws XMLStreamException;

    /*
    /**********************************************************************
    /* Second-level parsing; character content (in tree)
    /**********************************************************************
     */

    @Override
    protected final void finishToken() throws XMLStreamException {
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
                ErrorConsts.throwInternalError();
        }
    }

    /**
     * Method called to initialize state for CHARACTERS event, after
     * just a single byte has been seen. What needs to be done next
     * depends on whether coalescing mode is set or not: if it is not
     * set, just a single character needs to be decoded, after which
     * current event will be incomplete, but defined as CHARACTERS.
     * In coalescing mode, the whole content must be read before
     * current event can be defined. The reason for difference is
     * that when <code>XMLStreamReader.next()</code> returns, no
     * blocking can occur when calling other methods.
     *
     * @return Event type detected; either CHARACTERS, if at least
     *   one full character was decoded (and can be returned),
     *   EVENT_INCOMPLETE if not (part of a multi-byte character
     *   split across input buffer boundary)
     */
    protected abstract int startCharacters(byte b) throws XMLStreamException;

    protected abstract boolean handleAttrValue() throws XMLStreamException;

    protected abstract boolean handleNsDecl() throws XMLStreamException;

    /*
    /**********************************************************************
    /* Abstract methods from base class, parsing
    /**********************************************************************
     */

    @Override
    protected void finishCData() throws XMLStreamException {
        // N/A
        throwInternal();
    }

    @Override
    protected void finishComment() throws XMLStreamException {
        // N/A
        throwInternal();
    }

    @Override
    protected void finishDTD(boolean copyContents) throws XMLStreamException {
        // N/A
        throwInternal();
    }

    @Override
    protected void finishPI() throws XMLStreamException {
        // N/A
        throwInternal();
    }

    @Override
    protected void finishSpace() throws XMLStreamException {
        // N/A
        throwInternal();
    }

    // // token-skip methods

    /**
     * @return True if the whole characters segment was succesfully
     *   skipped; false if not
     */
    @Override
    protected abstract boolean skipCharacters() throws XMLStreamException;

    @Override
    protected void skipCData() throws XMLStreamException {
        // should never be called
        throwInternal();
    }

    @Override
    protected void skipComment() throws XMLStreamException {
        // should never be called
        throwInternal();
    }

    @Override
    protected void skipPI() throws XMLStreamException {
        // should never be called
        throwInternal();
    }

    @Override
    protected void skipSpace() throws XMLStreamException {
        // should never be called
        throwInternal();
    }

    @Override
    protected boolean loadMore() throws XMLStreamException {
        // should never get called
        throwInternal();
        return false; // never gets here
    }

    @Override
    protected abstract void finishCharacters() throws XMLStreamException;

    /*
    /**********************************************************************
    /* Internal methods, name decoding
    /**********************************************************************
     */

    /**
     * Method called to process a sequence of bytes that is likely to
     * be a PName. At this point we encountered an end marker, and
     * may either hit a formerly seen well-formed PName; an as-of-yet
     * unseen well-formed PName; or a non-well-formed sequence (containing
     * one or more non-name chars without any valid end markers).
     *
     * @param lastQuad Word with last 0 to 3 bytes of the PName; not included
     *   in the quad array
     * @param lastByteCount Number of bytes contained in lastQuad; 0 to 3.
     */
    protected final PName findPName(int lastQuad, int lastByteCount) throws XMLStreamException {
        // First, need to push back the byte read but not used:
        --_inputPtr;
        int qlen = _quadCount;
        // Also: if last quad is empty, will need take last from qbuf.
        if (lastByteCount == 0) {
            lastQuad = _quadBuffer[--qlen];
            lastByteCount = 4;
        }
        // Separate handling for short names:
        if (qlen <= 1) { // short name?
            if (qlen == 0) { // 4-bytes or less; only has 'lastQuad' defined
                int hash = ByteBasedPNameTable.calcHash(lastQuad);
                PName name = _symbols.findSymbol(hash, lastQuad, 0);
                if (name == null) {
                    // Let's simplify things a bit, and just use array based one then:
                    _quadBuffer[0] = lastQuad;
                    name = addPName(_symbols, hash, _quadBuffer, 1, lastByteCount);
                }
                return name;
            }
            int firstQuad = _quadBuffer[0];
            int hash = ByteBasedPNameTable.calcHash(firstQuad, lastQuad);
            PName name = _symbols.findSymbol(hash, firstQuad, lastQuad);
            if (name == null) {
                // As above, let's just use array, then
                _quadBuffer[1] = lastQuad;
                name = addPName(_symbols, hash, _quadBuffer, 2, lastByteCount);
            }
            return name;
        }
        // Nope, long (3 quads or more). At this point, the last quad is
        // not yet in the array, let's add:
        if (qlen >= _quadBuffer.length) { // let's just double?
            _quadBuffer = DataUtil.growArrayBy(_quadBuffer, _quadBuffer.length);
        }
        _quadBuffer[qlen++] = lastQuad;
        int hash = ByteBasedPNameTable.calcHash(_quadBuffer, qlen);
        PName name = _symbols.findSymbol(hash, _quadBuffer, qlen);
        if (name == null) {
            name = addPName(_symbols, hash, _quadBuffer, qlen, lastByteCount);
        }
        return name;
    }

    protected final PName addPName(ByteBasedPNameTable symbols, int hash, int[] quads, int qlen, int lastQuadBytes)
        throws XMLStreamException {
        return addUTFPName(symbols, _charTypes, hash, quads, qlen, lastQuadBytes);
    }

    /*
    /**********************************************************************
    /* Internal methods, input validation
    /**********************************************************************
     */

    /**
     * Method called to verify validity of given character (from entity) and
     * append it to the text buffer
     */
    protected void verifyAndAppendEntityCharacter(int charFromEntity) throws XMLStreamException {
        verifyXmlChar(charFromEntity);
        // Ok; does it need a surrogate though? (over 16 bits)
        if ((charFromEntity >> 16) != 0) {
            charFromEntity -= 0x10000;
            _textBuilder.append((char) (0xD800 | (charFromEntity >> 10)));
            charFromEntity = 0xDC00 | (charFromEntity & 0x3FF);
        }
        _textBuilder.append((char) charFromEntity);
    }

    /**
     * Checks that a character for a PublicId
     *
     * @param c A character
     * @return true if the character is valid for use in the Public ID
     * of an XML doctype declaration
     *
     * @see "http://www.w3.org/TR/xml/#NT-PubidLiteral"
     */
    protected boolean validPublicIdChar(int c) {
        return c == 0xA ||                     //<LF>
            c == 0xD ||                     //<CR>
            c == 0x20 ||                    //<SPACE>
            (c >= '0' && c <= '9') ||       //[0-9]
            (c >= '@' && c <= 'Z') ||       //@[A-Z]
            (c >= 'a' && c <= 'z') || c == '!' || (c >= 0x23 && c <= 0x25) ||     //#$%
            (c >= 0x27 && c <= 0x2F) ||     //'()*+,-./
            (c >= ':' && c <= ';') || c == '=' || c == '?' || c == '_';
    }

    /*
    /**********************************************************************
    /* Internal methods, error handling
    /**********************************************************************
     */

    @Override
    protected int decodeCharForError(byte b) throws XMLStreamException {
        // !!! TBI
        return b;
    }

    protected void checkPITargetName(PName targetName) throws XMLStreamException {
        String ln = targetName.getLocalName();
        if (ln.equalsIgnoreCase("xml") && !targetName.hasPrefix()) {
            reportInputProblem(ErrorConsts.ERR_WF_PI_XML_TARGET);
        }
    }

    protected int throwInternal() {
        throw new IllegalStateException("Internal error: should never execute this code path");
    }

    protected void reportInvalidOther(int mask, int ptr) throws XMLStreamException {
        _inputPtr = ptr;
        reportInvalidOther(mask);
    }

    /*
    /**********************************************************************
    /* Shared implementation for handling XML prolog; less performance
    /* sensitive so need not inline access
    /**********************************************************************
     */

    @Override
    public final int nextFromProlog(boolean isProlog) throws XMLStreamException {
        // Had fully complete event? Need to reset state etc:
        if (_currToken != EVENT_INCOMPLETE) {
            // First: keep track of where event started
            setStartLocation();

            // yet one more special case: after START_DOCUMENT need to check things...
            if (_currToken == START_DOCUMENT) {
                _currToken = EVENT_INCOMPLETE;
                if (_tokenName != null) {
                    _nextEvent = PROCESSING_INSTRUCTION;
                    _state = STATE_PI_AFTER_TARGET;
                    checkPITargetName(_tokenName);
                    return handlePI();
                }
            } else {
                _currToken = _nextEvent = EVENT_INCOMPLETE;
                _state = STATE_DEFAULT;
            }
        }

        // Ok, do we know which event it will be?
        if (_nextEvent == EVENT_INCOMPLETE) { // nope
            // The very first thing: XML declaration handling
            if (_state == STATE_PROLOG_INITIAL) {
                if (_inputPtr >= _inputEnd) {
                    return _currToken;
                }
                // Ok: see if we have what looks like XML declaration; process:
                if (_pendingInput != 0) { // already parsing (potential) XML declaration
                    Boolean b = startXmlDeclaration(); // is or may be XML declaration, so:
                    if (b == null) { // not yet known; bail out
                        return EVENT_INCOMPLETE;
                    }
                    if (b == Boolean.FALSE) { // no real XML declaration; synthesize one
                        return _startDocumentNoXmlDecl();
                    }
                    return handleXmlDeclaration();
                }
                if (_currentByte() == BYTE_LT) { // first byte, see if it could be XML declaration
                    ++_inputPtr;
                    _pendingInput = PENDING_STATE_XMLDECL_LT;
                    Boolean b = startXmlDeclaration(); // is or may be XML declaration, so:
                    if (b == null) {
                        return EVENT_INCOMPLETE;
                    }
                    if (b == Boolean.FALSE) { // no real XML declaration; synthesize one
                        return _startDocumentNoXmlDecl();
                    }
                    return handleXmlDeclaration();
                }
                // can't be XML declaration
                _state = STATE_DEFAULT;
                return _startDocumentNoXmlDecl();
            }

            // First: did we have a lone CR at the end of the buffer?
            if (_pendingInput != 0) { // yup
                if (!handlePartialCR()) {
                    return _currToken;
                }
            }
            while (_state == STATE_DEFAULT) {
                if (_inputPtr >= _inputEnd) { // no more input available
                    if (_endOfInput) { // for good? That may be fine
                        setStartLocation();
                        return TOKEN_EOI;
                    }
                    return _currToken;
                }
                byte b = _nextByte();

                // Really should get white space or '<'... anything else is
                // pretty much an error.
                if (b == BYTE_LT) { // root element, comment, proc instr?
                    _state = STATE_PROLOG_SEEN_LT;
                    break;
                }
                if (b == BYTE_SPACE || b == BYTE_CR || b == BYTE_LF || b == BYTE_TAB) {
                    // Prolog/epilog ws is to be skipped, not part of Infoset
                    if (!asyncSkipSpace()) { // ran out of input?
                        if (_endOfInput) { // for good? That may be fine
                            setStartLocation();
                            return TOKEN_EOI;
                        }
                        return _currToken;
                    }
                } else {
                    reportPrologUnexpChar(isProlog, decodeCharForError(b), null);
                }
            }
            if (_state == STATE_PROLOG_SEEN_LT) {
                if (_inputPtr >= _inputEnd) {
                    return _currToken;
                }
                byte b = _nextByte();
                if (b == BYTE_EXCL) { // comment or DOCTYPE declaration?
                    _state = STATE_PROLOG_DECL;
                    return handlePrologDeclStart(isProlog);
                }
                if (b == BYTE_QMARK) { // PI
                    _nextEvent = PROCESSING_INSTRUCTION;
                    _state = STATE_DEFAULT;
                    return handlePI();
                }
                if (b == BYTE_SLASH || !isProlog) {
                    reportPrologUnexpElement(isProlog, b);
                }
                return handleStartElementStart(b);
            }
            if (_state == STATE_PROLOG_DECL) {
                return handlePrologDeclStart(isProlog);
            }
            // should never have anything else...
            return throwInternal();
        }

        // At this point, we do know the event type
        switch (_nextEvent) {
            case START_ELEMENT:
                return handleStartElement();

            case START_DOCUMENT:
                return handleXmlDeclaration();

            case PROCESSING_INSTRUCTION:
                return handlePI();

            case COMMENT:
                return handleComment();

            case DTD:
                return handleDTD();
        }
        return throwInternal(); // should never get here
    }

    /**
     * Helper method called when it is determined that the document does NOT start with
     * an xml declaration. Needs to return START_DOCUMENT, and initialize other state
     * appropriately.
     */
    protected int _startDocumentNoXmlDecl() throws XMLStreamException {
        // 03-Apr-2018, tatu: We can finalize encoding at this point
        _activateEncoding();
        _currToken = START_DOCUMENT;
        return START_DOCUMENT;
    }

    private int handlePrologDeclStart(boolean isProlog) throws XMLStreamException {
        if (_inputPtr >= _inputEnd) { // nothing we can do?
            return EVENT_INCOMPLETE;
        }
        byte b = _nextByte();
        // So far, we have seen "<!", need to know if it's DTD or COMMENT
        if (b == BYTE_HYPHEN) {
            _nextEvent = COMMENT;
            _state = STATE_DEFAULT;
            return handleComment();
        }
        if (b == BYTE_D) {
            _nextEvent = DTD;
            _state = STATE_DEFAULT;
            return handleDTD();
        }
        reportPrologUnexpChar(isProlog, decodeCharForError(b), " (expected '-' for COMMENT)");
        return EVENT_INCOMPLETE; // never gets here
    }

    /**
     * Method that deals with recognizing XML declaration, but not with parsing
     * its contents.
     *
     * @return null if parsing is inconclusive (may or may not be XML declaration);
     *   Boolean.TRUE if complete XML declaration, and Boolean.FALSE if something
     *   else
     */
    private Boolean startXmlDeclaration() throws XMLStreamException {
        if (_inputPtr >= _inputEnd) {
            return null;
        }
        if (_pendingInput == PENDING_STATE_XMLDECL_LT) { // "<" at start of doc
            if (_currentByte() != BYTE_QMARK) { // some other
                _pendingInput = 0;
                _state = STATE_PROLOG_SEEN_LT;
                return Boolean.FALSE;
            }
            ++_inputPtr;
            _pendingInput = PENDING_STATE_XMLDECL_LTQ;
            if (_inputPtr >= _inputEnd) {
                return null;
            }
        }
        if (_pendingInput == PENDING_STATE_XMLDECL_LTQ) { // "<?" at start of doc
            byte b = _nextByte();
            _tokenName = _parseNewXmlDeclName(b);
            if (_tokenName == null) { // incomplete
                _pendingInput = PENDING_STATE_XMLDECL_TARGET;
                return null;
            }
            // xml or not?
            if (!"xml".equals(_tokenName.getPrefixedName())) { // nope: some other PI
                _pendingInput = 0;
                _state = STATE_PI_AFTER_TARGET;
                _nextEvent = PROCESSING_INSTRUCTION;
                checkPITargetName(_tokenName);
                return Boolean.FALSE;
            }
        } else if (_pendingInput == PENDING_STATE_XMLDECL_TARGET) { // "<?" at start of doc, part of name
            if ((_tokenName = _parseXmlDeclName()) == null) { // incomplete
                return null;
            }
            if (!"xml".equals(_tokenName.getPrefixedName())) {
                _pendingInput = 0;
                _state = STATE_PI_AFTER_TARGET;
                _nextEvent = PROCESSING_INSTRUCTION;
                checkPITargetName(_tokenName);
                return Boolean.FALSE;
            }
        } else {
            throwInternal();
        }
        _pendingInput = 0;
        _nextEvent = START_DOCUMENT;
        _state = STATE_XMLDECL_AFTER_XML;
        return Boolean.TRUE;
    }

    /**
     * Method called to complete parsing of XML declaration, once it has
     * been reliably detected.
     *
     * @return Completed token (START_DOCUMENT), if fully parsed; incomplete (EVENT_INCOMPLETE)
     *   otherwise
     */
    private int handleXmlDeclaration() throws XMLStreamException {
        // First: left-over CRs?
        if (_pendingInput == PENDING_STATE_CR) {
            if (!handlePartialCR()) {
                return EVENT_INCOMPLETE;
            }
        }

        main_loop: while (_inputPtr < _inputEnd) {
            switch (_state) {
                case STATE_XMLDECL_AFTER_XML: // "<?xml", need space
                {
                    byte b = _nextByte();
                    if (b == BYTE_SPACE || b == BYTE_CR || b == BYTE_LF || b == BYTE_TAB) {
                        _state = STATE_XMLDECL_BEFORE_VERSION;
                    } else {
                        reportPrologUnexpChar(true, decodeCharForError(b),
                            " (expected space after 'xml' in xml declaration)");
                    }
                }
                    if (_inputPtr >= _inputEnd) {
                        break;
                    }
                    // fall through
                case STATE_XMLDECL_BEFORE_VERSION:
                    if (!asyncSkipSpace()) { // not enough input
                        break;
                    }
                    if ((_tokenName = _parseNewXmlDeclName(_nextByte())) == null) { // incomplete
                        _state = STATE_XMLDECL_VERSION;
                        break;
                    }
                    if (!_tokenName.hasPrefixedName("version")) {
                        reportInputProblem("Unexpected keyword '" + _tokenName.getPrefixedName()
                            + "' in XML declaration: expected 'version'");
                    }
                    _state = STATE_XMLDECL_AFTER_VERSION;
                    continue main_loop;

                case STATE_XMLDECL_VERSION: // "<?xml ", part of "version"
                    if ((_tokenName = _parseXmlDeclName()) == null) { // incomplete
                        break;
                    }
                    if (!_tokenName.hasPrefixedName("version")) {
                        reportInputProblem("Unexpected keyword '" + _tokenName.getPrefixedName()
                            + "' in XML declaration: expected 'version'");
                    }
                    _state = STATE_XMLDECL_AFTER_VERSION;
                    if (_inputPtr >= _inputEnd) {
                        break;
                    }
                    // fall through
                case STATE_XMLDECL_AFTER_VERSION: // "<?xml version", need space or '='
                    if (!asyncSkipSpace()) { // not enough input
                        break;
                    } {
                    byte b = _nextByte();
                    if (b != BYTE_EQ) {
                        reportPrologUnexpChar(true, decodeCharForError(b),
                            " (expected '=' after 'version' in xml declaration)");
                    }
                }
                    _state = STATE_XMLDECL_VERSION_EQ;
                    if (_inputPtr >= _inputEnd) {
                        break;
                    }
                    // fall through
                case STATE_XMLDECL_VERSION_EQ: // "<?xml version=", need space or quote
                    if (!asyncSkipSpace()) { // skip space, if any
                        break;
                    }
                    _elemAttrQuote = _nextByte();
                    if (_elemAttrQuote != BYTE_QUOT && _elemAttrQuote != BYTE_APOS) {
                        reportPrologUnexpChar(true, decodeCharForError(_elemAttrQuote),
                            " (expected '\"' or ''' in xml declaration for version value)");
                    } {
                    char[] buf = _textBuilder.resetWithEmpty();
                    if (_inputPtr >= _inputEnd || !parseXmlDeclAttr(buf, 0)) {
                        _state = STATE_XMLDECL_VERSION_VALUE;
                        break;
                    }
                }
                    verifyAndSetXmlVersion();
                    _state = STATE_XMLDECL_AFTER_VERSION_VALUE;
                    continue main_loop;

                case STATE_XMLDECL_VERSION_VALUE: // parsing version value
                    if (!parseXmlDeclAttr(_textBuilder.getBufferWithoutReset(), _textBuilder.getCurrentLength())) {
                        _state = STATE_XMLDECL_VERSION_VALUE;
                        break;
                    }
                    verifyAndSetXmlVersion();
                    _state = STATE_XMLDECL_AFTER_VERSION_VALUE;
                    if (_inputPtr >= _inputEnd) {
                        break;
                    }
                    // fall through

                case STATE_XMLDECL_AFTER_VERSION_VALUE: // version got; need space or '?'
                {
                    byte b = _nextByte();
                    if (b == BYTE_QMARK) {
                        _state = STATE_XMLDECL_ENDQ;
                        continue main_loop;
                    }
                    if (b == BYTE_SPACE || b == BYTE_CR || b == BYTE_LF || b == BYTE_TAB) {
                        _state = STATE_XMLDECL_BEFORE_ENCODING;
                    } else {
                        reportPrologUnexpChar(true, decodeCharForError(b),
                            " (expected space after version value in xml declaration)");
                    }
                }
                    if (_inputPtr >= _inputEnd) {
                        break;
                    }
                    // fall through

                case STATE_XMLDECL_BEFORE_ENCODING: // version, value, space got, need '?' or 'e'
                    if (!asyncSkipSpace()) { // not enough input
                        break;
                    } {
                    byte b = _nextByte();
                    if (b == BYTE_QMARK) {
                        _state = STATE_XMLDECL_ENDQ;
                        continue main_loop;
                    }
                    if ((_tokenName = _parseNewXmlDeclName(b)) == null) { // incomplete
                        _state = STATE_XMLDECL_ENCODING;
                        break;
                    }
                    // Can actually also get "standalone" instead...
                    if (_tokenName.hasPrefixedName("encoding")) {
                        _state = STATE_XMLDECL_AFTER_ENCODING;
                    } else if (_tokenName.hasPrefixedName("standalone")) {
                        _state = STATE_XMLDECL_AFTER_STANDALONE;
                        continue main_loop;
                    } else {
                        reportInputProblem("Unexpected keyword '" + _tokenName.getPrefixedName()
                            + "' in XML declaration: expected 'encoding'");
                    }
                }
                    continue main_loop;

                case STATE_XMLDECL_ENCODING: // parsing "encoding"
                    if ((_tokenName = _parseXmlDeclName()) == null) { // incomplete
                        break;
                    }
                    // Can actually also get "standalone" instead...
                    if (_tokenName.hasPrefixedName("encoding")) {
                        _state = STATE_XMLDECL_AFTER_ENCODING;
                    } else if (_tokenName.hasPrefixedName("standalone")) {
                        _state = STATE_XMLDECL_AFTER_STANDALONE;
                        continue main_loop;
                    } else {
                        reportInputProblem("Unexpected keyword '" + _tokenName.getPrefixedName()
                            + "' in XML declaration: expected 'encoding'");
                    }
                    if (_inputPtr >= _inputEnd) {
                        break;
                    }
                    // fall through
                case STATE_XMLDECL_AFTER_ENCODING: // got "encoding"; must get ' ' or '='
                    if (!asyncSkipSpace()) { // not enough input
                        break;
                    } {
                    byte b = _nextByte();
                    if (b != BYTE_EQ) {
                        reportPrologUnexpChar(true, decodeCharForError(b),
                            " (expected '=' after 'encoding' in xml declaration)");
                    }
                }
                    _state = STATE_XMLDECL_ENCODING_EQ;
                    if (_inputPtr >= _inputEnd) {
                        break;
                    }
                    // fall through
                case STATE_XMLDECL_ENCODING_EQ: // "encoding="
                    if (!asyncSkipSpace()) { // skip space, if any
                        break;
                    }
                    _elemAttrQuote = _nextByte();
                    if (_elemAttrQuote != BYTE_QUOT && _elemAttrQuote != BYTE_APOS) {
                        reportPrologUnexpChar(true, decodeCharForError(_elemAttrQuote),
                            " (expected '\"' or ''' in xml declaration for encoding value)");
                    }
                    _state = STATE_XMLDECL_ENCODING_VALUE; {
                    char[] buf = _textBuilder.resetWithEmpty();
                    if (_inputPtr >= _inputEnd || !parseXmlDeclAttr(buf, 0)) {
                        _state = STATE_XMLDECL_ENCODING_VALUE;
                        break;
                    }
                }
                    verifyAndSetXmlEncoding();
                    _state = STATE_XMLDECL_AFTER_ENCODING_VALUE;
                    break;

                case STATE_XMLDECL_ENCODING_VALUE: // parsing encoding value
                    if (!parseXmlDeclAttr(_textBuilder.getBufferWithoutReset(), _textBuilder.getCurrentLength())) {
                        _state = STATE_XMLDECL_ENCODING_VALUE;
                        break;
                    }
                    verifyAndSetXmlEncoding();
                    _state = STATE_XMLDECL_AFTER_ENCODING_VALUE;
                    if (_inputPtr >= _inputEnd) {
                        break;
                    }
                    // fall through

                case STATE_XMLDECL_AFTER_ENCODING_VALUE: // encoding+value gotten; need space or '?'
                {
                    byte b = _nextByte();
                    if (b == BYTE_QMARK) {
                        _state = STATE_XMLDECL_ENDQ;
                        continue main_loop;
                    }
                    if (b == BYTE_SPACE || b == BYTE_CR || b == BYTE_LF || b == BYTE_TAB) {
                        _state = STATE_XMLDECL_BEFORE_STANDALONE;
                    } else {
                        reportPrologUnexpChar(true, decodeCharForError(b),
                            " (expected space after encoding value in xml declaration)");
                    }
                }
                    if (_inputPtr >= _inputEnd) {
                        break;
                    }
                    // fall through

                case STATE_XMLDECL_BEFORE_STANDALONE: // after encoding+value+space; get '?' or 's'
                    if (!asyncSkipSpace()) { // not enough input
                        break;
                    } {
                    byte b = _nextByte();
                    if (b == BYTE_QMARK) {
                        _state = STATE_XMLDECL_ENDQ;
                        continue main_loop;
                    }
                    if ((_tokenName = _parseNewXmlDeclName(b)) == null) { // incomplete
                        _state = STATE_XMLDECL_STANDALONE;
                        break;
                    }
                    if (!_tokenName.hasPrefixedName("standalone")) {
                        reportInputProblem("Unexpected keyword '" + _tokenName.getPrefixedName()
                            + "' in XML declaration: expected 'standalone'");
                    }
                }
                    _state = STATE_XMLDECL_AFTER_STANDALONE;
                    continue main_loop;

                case STATE_XMLDECL_STANDALONE: // parsing "standalone"
                    if ((_tokenName = _parseXmlDeclName()) == null) { // incomplete
                        break;
                    }
                    if (!_tokenName.hasPrefixedName("standalone")) {
                        reportInputProblem("Unexpected keyword 'encoding' in XML declaration: expected 'standalone'");
                    }
                    _state = STATE_XMLDECL_AFTER_STANDALONE;
                    if (_inputPtr >= _inputEnd) {
                        break;
                    }
                    // fall through
                case STATE_XMLDECL_AFTER_STANDALONE: // got "standalone"; must get ' ' or '='
                    if (!asyncSkipSpace()) { // not enough input
                        break;
                    } {
                    byte b = _nextByte();
                    if (b != BYTE_EQ) {
                        reportPrologUnexpChar(true, decodeCharForError(b),
                            " (expected '=' after 'standalone' in xml declaration)");
                    }
                }
                    _state = STATE_XMLDECL_STANDALONE_EQ;
                    if (_inputPtr >= _inputEnd) {
                        break;
                    }
                    // fall through
                case STATE_XMLDECL_STANDALONE_EQ: // "standalone="
                    if (!asyncSkipSpace()) { // skip space, if any
                        break;
                    }
                    _elemAttrQuote = _nextByte();
                    if (_elemAttrQuote != BYTE_QUOT && _elemAttrQuote != BYTE_APOS) {
                        reportPrologUnexpChar(true, decodeCharForError(_elemAttrQuote),
                            " (expected '\"' or ''' in xml declaration for standalone value)");
                    } {
                    char[] buf = _textBuilder.resetWithEmpty();
                    if (_inputPtr >= _inputEnd || !parseXmlDeclAttr(buf, 0)) {
                        _state = STATE_XMLDECL_STANDALONE_VALUE;
                        break;
                    }
                }
                    verifyAndSetXmlStandalone();
                    _state = STATE_XMLDECL_AFTER_STANDALONE_VALUE;
                    continue main_loop;

                case STATE_XMLDECL_STANDALONE_VALUE: // encoding+value gotten; need space or '?'

                    if (!parseXmlDeclAttr(_textBuilder.getBufferWithoutReset(), _textBuilder.getCurrentLength())) {
                        _state = STATE_XMLDECL_STANDALONE_VALUE;
                        break;
                    }
                    verifyAndSetXmlStandalone();
                    _state = STATE_XMLDECL_AFTER_STANDALONE_VALUE;
                    if (_inputPtr >= _inputEnd) {
                        break;
                    }
                    // fall through
                case STATE_XMLDECL_AFTER_STANDALONE_VALUE: // encoding+value gotten; need space or '?'
                    if (!asyncSkipSpace()) { // skip space, if any
                        break;
                    }
                    if (_nextByte() != BYTE_QMARK) {
                        reportPrologUnexpChar(true, decodeCharForError(_prevByte()),
                            " (expected '?>' to end xml declaration)");
                    }
                    _state = STATE_XMLDECL_ENDQ;
                    if (_inputPtr >= _inputEnd) {
                        break;
                    }
                    // fall through

                case STATE_XMLDECL_ENDQ:
                    // Better clear up decoded name, to avoid later problems (would be taken as PI)
                    _tokenName = null;
                    _state = STATE_DEFAULT;
                    _nextEvent = EVENT_INCOMPLETE;
                    if (_nextByte() != BYTE_GT) {
                        reportPrologUnexpChar(true, decodeCharForError(_prevByte()),
                            " (expected '>' to end xml declaration)");
                    }
                    // 03-Apr-2018, tatu: Finally! Done with XML declaration, we know the encoding for sure.
                    _activateEncoding();
                    return START_DOCUMENT;

                default:
                    throwInternal();
            }
        }

        return EVENT_INCOMPLETE;
    }

    private int handleDTD() throws XMLStreamException {
        // First: left-over CRs?
        if (_pendingInput == PENDING_STATE_CR) {
            if (!handlePartialCR()) {
                return EVENT_INCOMPLETE;
            }
        }
        if (_state == STATE_DTD_INT_SUBSET) {
            if (handleDTDInternalSubset(false)) { // got it!
                _state = STATE_DTD_EXPECT_CLOSING_GT;
            } else {
                return EVENT_INCOMPLETE;
            }
        }

        main_loop: while (_inputPtr < _inputEnd) {
            switch (_state) {
                case STATE_DEFAULT: // seen 'D'
                    _tokenName = parseNewName(BYTE_D);
                    if (_tokenName == null) {
                        _state = STATE_DTD_DOCTYPE;
                        return EVENT_INCOMPLETE;
                    }
                    if (!"DOCTYPE".equals(_tokenName.getPrefixedName())) {
                        reportPrologProblem(true, "expected 'DOCTYPE'");
                    }
                    _state = STATE_DTD_AFTER_DOCTYPE;
                    continue main_loop;

                case STATE_DTD_DOCTYPE:
                    _tokenName = parsePName();
                    if (_tokenName == null) {
                        _state = STATE_DTD_DOCTYPE;
                        return EVENT_INCOMPLETE;
                    }
                    if (!"DOCTYPE".equals(_tokenName.getPrefixedName())) {
                        reportPrologProblem(true, "expected 'DOCTYPE'");
                    }
                    if (_inputPtr >= _inputEnd) {
                        break;
                    }
                    // fall through
                case STATE_DTD_AFTER_DOCTYPE: {
                    byte b = _nextByte();
                    if (b == BYTE_SPACE || b == BYTE_CR || b == BYTE_LF || b == BYTE_TAB) {
                        _state = STATE_DTD_BEFORE_ROOT_NAME;
                    } else {
                        reportPrologUnexpChar(true, decodeCharForError(b), " (expected space after 'DOCTYPE')");
                    }
                }

                // fall through (ok to skip bounds checks, async-skip does it)
                case STATE_DTD_BEFORE_ROOT_NAME:
                    if (!asyncSkipSpace()) { // not enough input
                        break;
                    }
                    if ((_tokenName = parseNewName(_nextByte())) == null) { // incomplete
                        _state = STATE_DTD_ROOT_NAME;
                        break;
                    }
                    _state = STATE_DTD_ROOT_NAME;
                    continue main_loop;

                case STATE_DTD_ROOT_NAME:
                    if ((_tokenName = parsePName()) == null) { // incomplete
                        break;
                    }
                    _state = STATE_DTD_AFTER_ROOT_NAME;
                    if (_inputPtr >= _inputEnd) {
                        break;
                    }
                    // fall through
                case STATE_DTD_AFTER_ROOT_NAME: {
                    byte b = _nextByte();
                    if (b == BYTE_GT) {
                        _state = STATE_DEFAULT;
                        _nextEvent = EVENT_INCOMPLETE;
                        return DTD;
                    }
                    if (b == BYTE_SPACE || b == BYTE_CR || b == BYTE_LF || b == BYTE_TAB) {
                        _state = STATE_DTD_BEFORE_IDS;
                    } else {
                        reportPrologUnexpChar(true, decodeCharForError(b),
                            " (expected space after root name in DOCTYPE declaration)");
                    }
                }

                // fall through (ok to skip bounds checks, async-skip does it)
                case STATE_DTD_BEFORE_IDS:
                    if (!asyncSkipSpace()) { // not enough input
                        break;
                    } {
                    byte b = _nextByte();
                    if (b == BYTE_GT) {
                        _state = STATE_DEFAULT;
                        _nextEvent = EVENT_INCOMPLETE;
                        return DTD;
                    }
                    PName name;
                    if ((name = parseNewName(b)) == null) {
                        _state = STATE_DTD_PUBLIC_OR_SYSTEM;
                        break;
                    }
                    String str = name.getPrefixedName();
                    if ("PUBLIC".equals(str)) {
                        _state = STATE_DTD_AFTER_PUBLIC;
                    } else if ("SYSTEM".equals(str)) {
                        _state = STATE_DTD_AFTER_SYSTEM;
                    } else {
                        reportPrologProblem(true, "unexpected token '" + str + "': expected either PUBLIC or SYSTEM");
                    }
                }
                    continue main_loop;

                case STATE_DTD_PUBLIC_OR_SYSTEM: {
                    PName name;
                    if ((name = parsePName()) == null) {
                        _state = STATE_DTD_PUBLIC_OR_SYSTEM;
                        break;
                    }
                    String str = name.getPrefixedName();
                    if ("PUBLIC".equals(str)) {
                        _state = STATE_DTD_AFTER_PUBLIC;
                    } else if ("SYSTEM".equals(str)) {
                        _state = STATE_DTD_AFTER_SYSTEM;
                    } else {
                        reportPrologProblem(true, "unexpected token '" + str + "': expected either PUBLIC or SYSTEM");
                    }
                }
                    continue main_loop;

                case STATE_DTD_AFTER_PUBLIC: {
                    byte b = _nextByte();
                    if (b == BYTE_SPACE || b == BYTE_CR || b == BYTE_LF || b == BYTE_TAB) {
                        _state = STATE_DTD_BEFORE_PUBLIC_ID;
                    } else {
                        reportPrologUnexpChar(true, decodeCharForError(b), " (expected space after PUBLIC keyword)");
                    }
                }
                    continue main_loop;

                case STATE_DTD_AFTER_SYSTEM: {
                    byte b = _nextByte();
                    if (b == BYTE_SPACE || b == BYTE_CR || b == BYTE_LF || b == BYTE_TAB) {
                        _state = STATE_DTD_BEFORE_SYSTEM_ID;
                    } else {
                        reportPrologUnexpChar(true, decodeCharForError(b), " (expected space after SYSTEM keyword)");
                    }
                }
                    continue main_loop;

                case STATE_DTD_BEFORE_PUBLIC_ID:
                    if (!asyncSkipSpace()) {
                        break;
                    }
                    _elemAttrQuote = _nextByte();
                    if (_elemAttrQuote != BYTE_QUOT && _elemAttrQuote != BYTE_APOS) {
                        reportPrologUnexpChar(true, decodeCharForError(_elemAttrQuote),
                            " (expected '\"' or ''' for PUBLIC ID)");
                    } {
                    char[] buf = _textBuilder.resetWithEmpty();
                    if (_inputPtr >= _inputEnd || !parseDtdId(buf, 0, false)) {
                        _state = STATE_DTD_PUBLIC_ID;
                        break;
                    }
                }
                    verifyAndSetPublicId();
                    _state = STATE_DTD_AFTER_PUBLIC_ID;
                    continue main_loop;

                case STATE_DTD_PUBLIC_ID:
                    if (!parseDtdId(_textBuilder.getBufferWithoutReset(), _textBuilder.getCurrentLength(), false)) {
                        break;
                    }
                    verifyAndSetPublicId();
                    _state = STATE_DTD_AFTER_PUBLIC_ID;
                    if (_inputPtr >= _inputEnd) {
                        break;
                    }
                    // fall through
                case STATE_DTD_AFTER_PUBLIC_ID: {
                    byte b = _nextByte();
                    if (b == BYTE_SPACE || b == BYTE_CR || b == BYTE_LF || b == BYTE_TAB) {
                        _state = STATE_DTD_BEFORE_SYSTEM_ID;
                    } else {
                        reportPrologUnexpChar(true, decodeCharForError(b), " (expected space after PUBLIC ID)");
                    }
                }
                // fall through (ok to skip bounds checks, async-skip does it)

                case STATE_DTD_BEFORE_SYSTEM_ID:
                    if (!asyncSkipSpace()) {
                        break;
                    }
                    _elemAttrQuote = _nextByte();
                    if (_elemAttrQuote != BYTE_QUOT && _elemAttrQuote != BYTE_APOS) {
                        reportPrologUnexpChar(true, decodeCharForError(_elemAttrQuote),
                            " (expected '\"' or ''' for SYSTEM ID)");
                    } {
                    char[] buf = _textBuilder.resetWithEmpty();
                    if (_inputPtr >= _inputEnd || !parseDtdId(buf, 0, true)) {
                        _state = STATE_DTD_SYSTEM_ID;
                        break;
                    }
                }
                    verifyAndSetSystemId();
                    _state = STATE_DTD_AFTER_SYSTEM_ID;
                    continue main_loop;

                case STATE_DTD_SYSTEM_ID:
                    if (!parseDtdId(_textBuilder.getBufferWithoutReset(), _textBuilder.getCurrentLength(), true)) {
                        break;
                    }
                    verifyAndSetSystemId();
                    _state = STATE_DTD_AFTER_SYSTEM_ID;
                    if (_inputPtr >= _inputEnd) {
                        break;
                    }
                    // fall through

                case STATE_DTD_AFTER_SYSTEM_ID:
                    if (!asyncSkipSpace()) {
                        break;
                    } {
                    byte b = _nextByte();
                    if (b == BYTE_GT) {
                        _state = STATE_DEFAULT;
                        _nextEvent = EVENT_INCOMPLETE;
                        return DTD;
                    }
                    if (b != BYTE_LBRACKET) {
                        reportPrologUnexpChar(true, decodeCharForError(_elemAttrQuote),
                            " (expected either '[' for internal subset, or '>' to end DOCTYPE)");
                    }
                }
                    _state = STATE_DTD_INT_SUBSET;
                    if (handleDTDInternalSubset(true)) {
                        _state = STATE_DTD_EXPECT_CLOSING_GT;
                    } else {
                        return EVENT_INCOMPLETE;
                    }
                    // fall through

                case STATE_DTD_EXPECT_CLOSING_GT:
                    if (!asyncSkipSpace()) {
                        break;
                    } {
                    byte b = _nextByte();
                    if (b != BYTE_GT) {
                        reportPrologUnexpChar(true, b, "expected '>' to end DTD");
                    }
                }
                    _state = STATE_DEFAULT;
                    _nextEvent = EVENT_INCOMPLETE;
                    return DTD;

                default:
                    throwInternal();
            }
        }
        return _currToken;
    }

    private boolean parseDtdId(char[] outputBuffer, int outputPtr, boolean system) throws XMLStreamException {
        final int quote = _elemAttrQuote;
        while (_inputPtr < _inputEnd) {
            int ch = _nextByte() & 0xFF;
            if (ch == quote) {
                _textBuilder.setCurrentLength(outputPtr);
                return true;
            }
            if (!system && !validPublicIdChar(ch)) {
                reportPrologUnexpChar(true, decodeCharForError((byte) ch),
                    " (not valid in " + (system ? "SYSTEM" : "PUBLIC") + " ID)");
            }
            if (outputPtr >= outputBuffer.length) {
                outputBuffer = _textBuilder.finishCurrentSegment();
                outputPtr = 0;
            }
            outputBuffer[outputPtr++] = (char) ch;
        }
        _textBuilder.setCurrentLength(outputPtr);
        return false;
    }

    // // // NOTE: specialized versions of `parsePName`, `parseNewName`, to be
    // // //  used in decoding `xml` and pseudo-attributes of XML declaration
    // // //  Tricky part here is that it predates possible encoding declaration
    // // //  so it is essentially part of bootstrapping

    private PName _parseNewXmlDeclName(byte b) throws XMLStreamException {
        int q = b & 0xFF;
        if (q < INT_A) { // lowest acceptable start char, except for ':' that would be allowed in non-ns mode
            throwUnexpectedChar(q, "; expected a name start character");
        }
        _quadCount = 0;
        _currQuad = q;
        _currQuadBytes = 1;
        return _parseXmlDeclName();
    }

    private PName _parseXmlDeclName() throws XMLStreamException {
        int q = _currQuad;

        while (true) {
            int i;

            switch (_currQuadBytes) {
                case 0:
                    if (_inputPtr >= _inputEnd) {
                        return null; // all pointers have been set
                    }
                    q = _nextByte() & 0xFF;
                    // Since name char validity is checked later on, only do quickie lookup
                    if (q < 65) { // 'A'
                        if (q < 45 || q > 58 || q == 47) {
                            return _findXmlDeclName(q, 0);
                        }
                    }
                    // fall through
                case 1:
                    if (_inputPtr >= _inputEnd) { // need to store pointers
                        _currQuad = q;
                        _currQuadBytes = 1;
                        return null;
                    }
                    i = _nextByte() & 0xFF;
                    if (i < 65) { // 'A'
                        if (i < 45 || i > 58 || i == 47) {
                            return _findXmlDeclName(q, 1);
                        }
                    }
                    q = (q << 8) | i;
                    // fall through
                case 2:
                    if (_inputPtr >= _inputEnd) { // need to store pointers
                        _currQuad = q;
                        _currQuadBytes = 2;
                        return null;
                    }
                    i = _nextByte() & 0xFF;
                    if (i < 65) { // 'A'
                        if (i < 45 || i > 58 || i == 47) {
                            return _findXmlDeclName(q, 2);
                        }
                    }
                    q = (q << 8) | i;
                    // fall through
                case 3:
                    if (_inputPtr >= _inputEnd) { // need to store pointers
                        _currQuad = q;
                        _currQuadBytes = 3;
                        return null;
                    }
                    i = _nextByte() & 0xFF;
                    if (i < 65) { // 'A'
                        if (i < 45 || i > 58 || i == 47) {
                            return _findXmlDeclName(q, 3);
                        }
                    }
                    q = (q << 8) | i;
            }

            // If we get this far, need to add full quad into result array and update state
            if (_quadCount == 0) { // first quad
                _quadBuffer[0] = q;
                _quadCount = 1;
            } else {
                if (_quadCount >= _quadBuffer.length) { // let's just double?
                    _quadBuffer = DataUtil.growArrayBy(_quadBuffer, _quadBuffer.length);
                }
                _quadBuffer[_quadCount++] = q;
            }
            _currQuadBytes = 0;
        }
    }

    protected final PName _findXmlDeclName(int lastQuad, int lastByteCount) throws XMLStreamException {
        int qlen = _quadCount;
        // Also: if last quad is empty, will need take last from qbuf.
        if (lastByteCount == 0) {
            lastQuad = _quadBuffer[--qlen];
            // NOTE: do not change since we may need to delegate with original value,
            // and byte count not checked here
            //            lastByteCount = 4;
        }

        // First things first: we are very likely to find one of short pseudo-attributes, so:
        PName pname;

        switch (qlen) {
            case 0: // 4-bytes or less; only has 'lastQuad' defined
                pname = AsyncXmlDeclHelper.find(lastQuad);
                break;

            case 1:
                pname = AsyncXmlDeclHelper.find(_quadBuffer[0], lastQuad);
                break;

            case 2:
                pname = AsyncXmlDeclHelper.find(_quadBuffer[0], _quadBuffer[1], lastQuad);
                break;

            default:
                pname = null;
        }
        if (pname != null) {
            // Need to push back the byte read but not used:
            --_inputPtr;
            return pname;
        }

        // Otherwise most likely a processing instruction instead of XML declaration. A few
        // ways we could deal with it, but for now let's finalize symbol table etc, delegate
        _activateEncoding();
        return findPName(lastQuad, lastByteCount);
    }

    /**
     * Method called to try to parse an XML pseudo-attribute value. This is relatively
     * simple, since we can't have linefeeds or entities; and although there are exact
     * rules for what is allowed, we can do coarse parsing and only later on verify
     * validity (for encoding could do stricter parsing in future?)
     *<p>
     * NOTE: pseudo-attribute values required to be 7-bit ASCII so can do crude cast.
     *
     * @return True if we managed to parse the whole pseudo-attribute
     */
    protected boolean parseXmlDeclAttr(char[] outputBuffer, int outputPtr) throws XMLStreamException {
        final int quote = _elemAttrQuote;
        while (_inputPtr < _inputEnd) {
            int ch = _nextByte() & 0xFF;
            if (ch == quote) {
                _textBuilder.setCurrentLength(outputPtr);
                return true;
            }
            // this is not exact check; but does work for all legal (valid) characters:
            if (ch <= INT_SPACE || ch > INT_z) {
                reportPrologUnexpChar(true, decodeCharForError((byte) ch),
                    " (not valid in XML pseudo-attribute values)");
            }
            if (outputPtr >= outputBuffer.length) {
                outputBuffer = _textBuilder.finishCurrentSegment();
                outputPtr = 0;
            }
            outputBuffer[outputPtr++] = (char) ch;
        }
        _textBuilder.setCurrentLength(outputPtr);
        return false;
    }
}
