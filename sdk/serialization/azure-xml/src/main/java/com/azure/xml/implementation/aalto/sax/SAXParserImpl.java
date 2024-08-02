// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
/* Woodstox Lite ("wool") XML processor
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

package com.azure.xml.implementation.aalto.sax;

import java.io.*;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import com.azure.xml.implementation.aalto.AaltoInputProperties;
import org.xml.sax.*;
import org.xml.sax.ext.Attributes2;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.ext.Locator2;
import org.xml.sax.helpers.DefaultHandler;

import com.azure.xml.implementation.aalto.in.*;
import com.azure.xml.implementation.aalto.stax.InputFactoryImpl;
import com.azure.xml.implementation.aalto.util.URLUtil;

@SuppressWarnings("deprecation")
class SAXParserImpl extends SAXParser implements Parser // SAX1
    , XMLReader // SAX2
    , Attributes2 // SAX2
    , Locator2 // SAX2
{
    final InputFactoryImpl _staxFactory;

    /**
     * Since the stream reader would mostly be just a wrapper around
     * the underlying scanner (its main job is to implement Stax
     * interface), we can and should just use the scanner. In effect,
     * this class is then a replacement of StreamReaderImpl, when
     * using SAX interfaces.
     */
    protected XmlScanner _scanner;

    protected AttributeCollector _attrCollector;

    // // // Listeners attached:

    protected ContentHandler _contentHandler;
    protected DTDHandler _dtdHandler;
    private EntityResolver _entityResolver;
    private ErrorHandler _errorHandler;

    private LexicalHandler _lexicalHandler;
    private DeclHandler _declHandler;

    // // // State:

    private int _attrCount;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    SAXParserImpl(InputFactoryImpl sf) {
        _staxFactory = sf;
    }

    @Override
    public final Parser getParser() {
        return this;
    }

    @Override
    public final XMLReader getXMLReader() {
        return this;
    }

    /*
    /**********************************************************************
    /* Configuration, SAXParser
    /**********************************************************************
     */

    @Override
    public boolean isNamespaceAware() {
        return true;
    }

    @Override
    public boolean isValidating() {
        return false;
    }

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        SAXProperty stdProp = SAXUtil.findStdProperty(name);
        if (stdProp != null) {
            switch (stdProp) {
                case DECLARATION_HANDLER:
                    return _declHandler;

                case DOCUMENT_XML_VERSION:
                    // as per [Issue 9], provide version info (is it ok to return potentially null?)
                    return _scanner.getConfig().getXmlDeclVersion();

                case DOM_NODE: // not implemented, won't be
                    return null;

                case LEXICAL_HANDLER:
                    return _lexicalHandler;

                case XML_STRING: // not implemented, won't be
                    return null;
            }
        }
        SAXUtil.reportUnknownProperty(name);
        return null;
    }

    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        SAXProperty stdProp = SAXUtil.findStdProperty(name);

        if (stdProp != null) {
            switch (stdProp) {
                case DECLARATION_HANDLER:
                    _declHandler = (DeclHandler) value;
                    return;

                case DOCUMENT_XML_VERSION:
                    // as per [Issue 9]:
                    _scanner.getConfig().setXmlVersion((value == null) ? null : String.valueOf(value));
                    return;

                case DOM_NODE: // not implemented, won't be
                    return;

                case LEXICAL_HANDLER:
                    _lexicalHandler = (LexicalHandler) value;
                    return;

                case XML_STRING: // not implemented, won't be
                    return;
            }
        }
        SAXUtil.reportUnknownFeature(name);
    }

    /*
    /**********************************************************************
    /* Overrides, SAXParser
    /**********************************************************************
     */

    /* Have to override some methods from SAXParser; JDK
     * implementation is sucky, as it tries to override
     * many things it really should not...
     */

    @Override
    public void parse(InputSource is, HandlerBase hb) throws SAXException, IOException {
        if (hb != null) {
            /* Ok: let's ONLY set if there are no explicit sets... not
             * extremely clear, but JDK tries to set them always so
             * let's at least do damage control.
             */
            if (_contentHandler == null) {
                setDocumentHandler(hb);
            }
            if (_entityResolver == null) {
                setEntityResolver(hb);
            }
            if (_errorHandler == null) {
                setErrorHandler(hb);
            }
            if (_dtdHandler == null) {
                setDTDHandler(hb);
            }
        }
        parse(is);
    }

    @Override
    public void parse(InputSource is, DefaultHandler dh) throws SAXException, IOException {
        if (dh != null) {
            /* Ok: let's ONLY set if there are no explicit sets... not
             * extremely clear, but JDK tries to set them always so
             * let's at least do damage control.
             */
            if (_contentHandler == null) {
                setContentHandler(dh);
            }
            if (_entityResolver == null) {
                setEntityResolver(dh);
            }
            if (_errorHandler == null) {
                setErrorHandler(dh);
            }
            if (_dtdHandler == null) {
                setDTDHandler(dh);
            }
        }
        parse(is);
    }

    /*
    /**********************************************************************
    /* XLMReader (SAX2) implementation: cfg access
    /**********************************************************************
     */

    @Override
    public ContentHandler getContentHandler() {
        return _contentHandler;
    }

    @Override
    public DTDHandler getDTDHandler() {
        return _dtdHandler;
    }

    @Override
    public EntityResolver getEntityResolver() {
        return _entityResolver;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return _errorHandler;
    }

    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException {
        // Standard feature?
        SAXFeature stdFeat = SAXUtil.findStdFeature(name);
        if (stdFeat != null) {
            // fixed?
            Boolean b = SAXUtil.getFixedStdFeatureValue(stdFeat);
            if (b != null) {
                return b.booleanValue();
            }
            // ok, may change:
            switch (stdFeat) {
                case IS_STANDALONE: // read-only, but only during parsing
                    // !!! TBI
                    return true;

                case EXTERNAL_GENERAL_ENTITIES:
                    return Boolean.FALSE
                        .equals(_staxFactory.getProperty(AaltoInputProperties.P_RETAIN_ATTRIBUTE_GENERAL_ENTITIES));

                default:
            }
        } else {
            // any non-standard one we may support?
        }

        // nope, not recognized:
        SAXUtil.reportUnknownFeature(name);
        return false; // never gets here
    }

    // Already implemented for SAXParser
    //public Object getProperty(String name)

    /*
    /**********************************************************************
    /* XLMReader (SAX2) implementation: cfg changing
    /**********************************************************************
     */

    @Override
    public void setContentHandler(ContentHandler handler) {
        _contentHandler = handler;
    }

    @Override
    public void setDTDHandler(DTDHandler handler) {
        _dtdHandler = handler;
    }

    @Override
    public void setEntityResolver(EntityResolver resolver) {
        _entityResolver = resolver;
    }

    @Override
    public void setErrorHandler(ErrorHandler handler) {
        _errorHandler = handler;
    }

    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException {
        // Standard feature?
        SAXFeature stdFeat = SAXUtil.findStdFeature(name);
        if (stdFeat != null) {
            //boolean ok;

            // !!! TBI
            /*
            switch (stdFeat) {
            }
            */
        } else {
            SAXUtil.reportUnknownFeature(name);
        }

    }

    // Already implemented for SAXParser
    //public void setProperty(String name, Object value) 

    /*
    /**********************************************************************
    /* XLMReader (SAX2) implementation: parsing
    /**********************************************************************
     */

    @Override
    public void parse(InputSource input) throws SAXException {
        String enc = input.getEncoding();
        String systemId = input.getSystemId();
        /* Let's ask for default (non-event-reader-bound) reader
         * first. One open question: whether auto-closing needs to be
         * forced? For now, let's assume not (second false, first is for
         * 'isForEventReader')
         */
        ReaderConfig cfg = _staxFactory.getNonSharedConfig(systemId, input.getPublicId(), enc, false, false);
        /* But let's disable lazy parsing: with SAX there's no good
         * way to make use of it (similar to why it's disabled for
         * event readers)
         */
        cfg.doParseLazily(false);

        // Let's figure out input, first, before sending start-doc event
        InputStream is = null;
        Reader r = input.getCharacterStream();
        if (r == null) {
            is = input.getByteStream();
            if (is == null) {
                if (systemId == null) {
                    throw new SAXException(
                        "Invalid InputSource passed: neither character or byte stream passed, nor system id specified");
                }
                try {
                    URL url = URLUtil.urlFromSystemId(systemId);
                    is = URLUtil.inputStreamFromURL(url);
                } catch (IOException ioe) {
                    SAXException saxe = new SAXException(ioe);
                    if (saxe.getCause() == null) {
                        saxe.initCause(ioe);
                    }
                    throw saxe;
                }
            }
        }

        if (_contentHandler != null) {
            _contentHandler.setDocumentLocator(this);
            _contentHandler.startDocument();
        }

        try {
            if (r != null) {
                _scanner = CharSourceBootstrapper.construct(cfg, r).bootstrap();
            } else {
                _scanner = ByteSourceBootstrapper.construct(cfg, is).bootstrap();
            }
            _attrCollector = _scanner.getAttrCollector();
            fireEvents();
        } catch (XMLStreamException strex) {
            throwSaxException(strex);
        } finally {
            if (_contentHandler != null) {
                _contentHandler.endDocument();
            }
            /* Could try holding onto the buffers, too... but
             * maybe it's better to allow them to be reclaimed, if
             * needed by GC
             */
            if (_scanner != null) {
                try {
                    _scanner.close(false); // false -> no forced closing of source
                } catch (XMLStreamException strex) {
                    /* Hmmh. Should we bother trying to throw it? Should
                     * never really happen as it can only occur from
                     * stream.close() failing... which is a useless exception
                     * if it can occur. So, for once, let's just supress it.
                     */
                    ; // intentional no-action
                }
                _scanner = null;
            }
            if (r != null) {
                try {
                    r.close();
                } catch (IOException ioe) {
                    /* whatever */ }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ioe) {
                    /* whatever */ }
            }
        }
    }

    @Override
    public void parse(String systemId) throws SAXException {
        InputSource src = new InputSource(systemId);
        parse(src);
    }

    /*
    /**********************************************************************
    /* Parsing loop, helper methods
    /**********************************************************************
     */

    /**
     * This is the actual "tight event loop" that will send all events
     * between start and end document events. Although we could
     * use the stream reader here, there's not much as it mostly
     * just forwards requests to the scanner: and so we can as well
     * just copy the little code stream reader's next() method has.
     */
    private final void fireEvents() throws SAXException, XMLStreamException {
        // First we are in prolog:
        int type;

        while ((type = _scanner.nextFromProlog(true)) != XMLStreamConstants.START_ELEMENT) {
            fireAuxEvent(type, false);
        }

        // Now just starting the tree, need to process the START_ELEMENT
        fireStartTag();

        int depth = 1;
        while (true) {
            type = _scanner.nextFromTree();
            if (type == XMLStreamConstants.START_ELEMENT) {
                fireStartTag();
                ++depth;
            } else if (type == XMLStreamConstants.END_ELEMENT) {
                fireEndTag();
                if (--depth < 1) {
                    break;
                }
            } else if (type == XMLStreamConstants.CHARACTERS) {
                _scanner.fireSaxCharacterEvents(_contentHandler);
            } else {
                fireAuxEvent(type, true);
            }
        }

        // And then epilog:
        while (true) {
            type = _scanner.nextFromProlog(false);
            if (type == XmlScanner.TOKEN_EOI) {
                break;
            }
            if (type == XmlScanner.SPACE) {
                /* Not to be reported via SAX interface (which may or may not
                 * be different from Stax)
                 */
                continue;
            }
            fireAuxEvent(type, false);
        }
    }

    private final void fireAuxEvent(int type, boolean inTree) throws SAXException, XMLStreamException {
        switch (type) {
            case XMLStreamConstants.COMMENT:
                _scanner.fireSaxCommentEvent(_lexicalHandler);
                break;

            case XMLStreamConstants.CDATA:
                if (_lexicalHandler != null) {
                    _lexicalHandler.startCDATA();
                    _scanner.fireSaxCharacterEvents(_contentHandler);
                    _lexicalHandler.endCDATA();
                } else {
                    _scanner.fireSaxCharacterEvents(_contentHandler);
                }
                break;

            case XMLStreamConstants.DTD:
                if (_lexicalHandler != null) {
                    PName n = _scanner.getName();
                    _lexicalHandler.startDTD(n.getPrefixedName(), _scanner.getDTDPublicId(), _scanner.getDTDSystemId());
                    _lexicalHandler.endDTD();
                }
                break;

            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                _scanner.fireSaxPIEvent(_contentHandler);
                break;

            case XMLStreamConstants.SPACE:
                /* With SAX, only to be sent as an event if inside the
                 * tree, not from within prolog/epilog
                 */
                if (inTree) {
                    _scanner.fireSaxSpaceEvents(_contentHandler);
                }
                break;

            default:
                if (type == XmlScanner.TOKEN_EOI) {
                    throwSaxException("Unexpected end-of-input in " + (inTree ? "tree" : "prolog"));
                }
                throw new RuntimeException("Internal error: unexpected type, " + type);
        }
    }

    private final void fireStartTag() throws SAXException {
        _attrCount = _scanner.getAttrCount();
        _scanner.fireSaxStartElement(_contentHandler, this);
    }

    private final void fireEndTag() throws SAXException {
        _scanner.fireSaxEndElement(_contentHandler);
    }

    /*
    /**********************************************************************
    /* Parser (SAX1) implementation
    /**********************************************************************
     */

    // Already implemented for XMLReader:
    //public void parse(InputSource source)
    //public void parse(String systemId)
    //public void setEntityResolver(EntityResolver resolver)
    //public void setErrorHandler(ErrorHandler handler)

    @Override
    public void setDocumentHandler(DocumentHandler handler) {
        setContentHandler(new DocHandlerWrapper(handler));
    }

    @Override
    public void setLocale(java.util.Locale locale) {
        // Not supported, let's just ignore
    }

    /*
    /**********************************************************************
    /* Attributes (SAX2) implementation
    /**********************************************************************
     */

    @Override
    public int getIndex(String qName) {
        return (_attrCollector == null) ? -1 : _attrCollector.findIndex(null, qName);
    }

    @Override
    public int getIndex(String uri, String localName) {
        return (_attrCollector == null) ? -1 : _attrCollector.findIndex(uri, localName);
    }

    @Override
    public int getLength() {
        return _attrCount;
    }

    @Override
    public String getLocalName(int index) {
        return (index < 0 || index >= _attrCount) ? null : _attrCollector.getName(index).getLocalName();
    }

    @Override
    public String getQName(int index) {
        return (index < 0 || index >= _attrCount) ? null : _attrCollector.getName(index).getPrefixedName();
    }

    @Override
    public String getType(int index) {
        /* 13-Sep-2006, tatus: Note: not yet really implemented, will
         *   just return "CDATA".
         */
        return (index < 0 || index >= _attrCount) ? null : _scanner.getAttrType(index);
    }

    @Override
    public String getType(String qName) {
        int ix = getIndex(qName);
        return (ix < 0) ? null : _scanner.getAttrType(ix);
    }

    @Override
    public String getType(String uri, String localName) {
        int ix = getIndex(uri, localName);
        return (ix < 0) ? null : _scanner.getAttrType(ix);
    }

    @Override
    public String getURI(int index) {
        if (index < 0 || index >= _attrCount) {
            return null;
        }
        String uri = _attrCollector.getName(index).getNsUri();
        return (uri == null) ? "" : uri;
    }

    @Override
    public String getValue(int index) {
        return (index < 0 || index >= _attrCount) ? null : _attrCollector.getValue(index);
    }

    @Override
    public String getValue(String qName) {
        int ix = getIndex(qName);
        return (ix < 0) ? null : _attrCollector.getValue(ix);
    }

    @Override
    public String getValue(String uri, String localName) {
        int ix = getIndex(uri, localName);
        return (ix < 0) ? null : _attrCollector.getValue(ix);
    }

    /*
    /**********************************************************************
    /* Attributes2 (SAX2) implementation
    /**********************************************************************
     */

    /* Note: for now (in absence of DTD processing), none of attributes
     * are declared, and all are specified (can not default without
     * a DTD)
     */

    @Override
    public boolean isDeclared(int index) {
        return false;
    }

    @Override
    public boolean isDeclared(String qName) {
        return false;
    }

    @Override
    public boolean isDeclared(String uri, String localName) {
        return false;
    }

    @Override
    public boolean isSpecified(int index) {
        return true;
    }

    @Override
    public boolean isSpecified(String qName) {
        return true;
    }

    @Override
    public boolean isSpecified(String uri, String localName) {
        return true;
    }

    /*
    /**********************************************************************
    /* Locator (SAX1) implementation
    /**********************************************************************
     */

    @Override
    public int getColumnNumber() {
        return (_scanner != null) ? _scanner.getCurrentColumnNr() : -1;
    }

    @Override
    public int getLineNumber() {
        return (_scanner != null) ? _scanner.getCurrentLineNr() : -1;
    }

    @Override
    public String getPublicId() {
        return (_scanner != null) ? _scanner.getInputPublicId() : null;
    }

    @Override
    public String getSystemId() {
        return (_scanner != null) ? _scanner.getInputSystemId() : null;
    }

    /*
    /**********************************************************************
    /* Locator2 (SAX2) implementation
    /**********************************************************************
     */

    @Override
    public String getEncoding() {
        ReaderConfig cfg = _scanner.getConfig();
        String enc = cfg.getActualEncoding();
        if (enc == null) {
            enc = cfg.getXmlDeclEncoding();
            if (enc == null) {
                enc = cfg.getExternalEncoding();
            }
        }
        return enc;
    }

    @Override
    public String getXMLVersion() {
        return _scanner.getConfig().getXmlDeclVersion();
    }

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    private void throwSaxException(Exception e) throws SAXException {
        SAXParseException se = new SAXParseException(e.getMessage(), (Locator) this, e);
        if (se.getCause() == null) {
            se.initCause(e);
        }
        if (_errorHandler != null) {
            _errorHandler.fatalError(se);
        }
        throw se;
    }

    private void throwSaxException(String msg) throws SAXException {
        SAXParseException se = new SAXParseException(msg, (Locator) this);
        if (_errorHandler != null) {
            _errorHandler.fatalError(se);
        }
        throw se;
    }

    /*
    /**********************************************************************
    /* Helper classes for SAX1 support
    /**********************************************************************
     */

    final static class DocHandlerWrapper implements ContentHandler {
        final DocumentHandler mDocHandler;

        final AttributesWrapper mAttrWrapper = new AttributesWrapper();

        DocHandlerWrapper(DocumentHandler h) {
            mDocHandler = h;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            mDocHandler.characters(ch, start, length);
        }

        @Override
        public void endDocument() throws SAXException {
            mDocHandler.endDocument();
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName == null) {
                qName = localName;
            }
            mDocHandler.endElement(qName);
        }

        @Override
        public void endPrefixMapping(String prefix) {
            // no equivalent in SAX1, ignore
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            mDocHandler.ignorableWhitespace(ch, start, length);
        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException {
            mDocHandler.processingInstruction(target, data);
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            mDocHandler.setDocumentLocator(locator);
        }

        @Override
        public void skippedEntity(String name) {
            // no equivalent in SAX1, ignore
        }

        @Override
        public void startDocument() throws SAXException {
            mDocHandler.startDocument();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
            if (qName == null) {
                qName = localName;
            }
            // Also, need to wrap Attributes to look like AttributeLost
            mAttrWrapper.setAttributes(attrs);
            mDocHandler.startElement(qName, mAttrWrapper);
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) {
            // no equivalent in SAX1, ignore
        }
    }

    final static class AttributesWrapper implements AttributeList {
        Attributes mAttrs;

        public AttributesWrapper() {
        }

        public void setAttributes(Attributes a) {
            mAttrs = a;
        }

        @Override
        public int getLength() {
            return mAttrs.getLength();
        }

        @Override
        public String getName(int i) {
            String n = mAttrs.getQName(i);
            return (n == null) ? mAttrs.getLocalName(i) : n;
        }

        @Override
        public String getType(int i) {
            return mAttrs.getType(i);
        }

        @Override
        public String getType(String name) {
            return mAttrs.getType(name);
        }

        @Override
        public String getValue(int i) {
            return mAttrs.getValue(i);
        }

        @Override
        public String getValue(String name) {
            return mAttrs.getValue(name);
        }
    }
}
