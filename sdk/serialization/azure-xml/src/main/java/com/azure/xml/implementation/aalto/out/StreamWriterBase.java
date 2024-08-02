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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.*;

import com.azure.xml.implementation.stax2.*;
import com.azure.xml.implementation.stax2.validation.*;
import com.azure.xml.implementation.stax2.ri.Stax2WriterImpl;
import com.azure.xml.implementation.stax2.ri.typed.AsciiValueEncoder;
import com.azure.xml.implementation.stax2.ri.typed.ValueEncoderFactory;
import com.azure.xml.implementation.stax2.typed.Base64Variant;
import com.azure.xml.implementation.stax2.typed.Base64Variants;

import com.azure.xml.implementation.aalto.ValidationException;
import com.azure.xml.implementation.aalto.impl.ErrorConsts;
import com.azure.xml.implementation.aalto.impl.IoStreamException;
import com.azure.xml.implementation.aalto.impl.LocationImpl;
import com.azure.xml.implementation.aalto.impl.StreamExceptionBase;
import com.azure.xml.implementation.aalto.util.TextUtil;
import com.azure.xml.implementation.aalto.util.XmlConsts;

/**
 * Base class for {@link XMLStreamReader} implementations.
 */
public abstract class StreamWriterBase extends Stax2WriterImpl implements NamespaceContext, ValidationContext {
    protected enum State {
        PROLOG, TREE, EPILOG
    };

    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */

    protected final WriterConfig _config;

    /**
     * Root namespace context defined for this writer, if any.
     */
    protected NamespaceContext _rootNsContext;

    // // // Config flags:

    // // Basic Stax:

    // // Stax2:

    // // Custom:

    // Note: non-final to support pluggable validators' needs

    protected boolean _cfgCheckStructure;
    protected boolean _cfgCheckContent;
    protected boolean _cfgCheckAttrs;

    protected final boolean _cfgCDataAsText;

    /*
    /**********************************************************************
    /* Symbol table for reusing name serializations
    /**********************************************************************
    */

    protected WNameTable _symbols;

    /*
    /**********************************************************************
    /* Output objects
    /**********************************************************************
     */

    /**
     * Actual physical writer to output serialized XML content to
     */
    protected final XmlWriter _xmlWriter;

    /**
     * When outputting using Typed Access API, we will need
     * encoders. If so, they will created by lazily-constructed
     * factory
     */
    protected ValueEncoderFactory _valueEncoderFactory;

    /*
    /**********************************************************************
    /* Validation support
    /**********************************************************************
     */

    /**
     * Optional validator to use for validating output against
     * one or more schemas, and/or for safe pretty-printing (indentation).
     */
    protected XMLValidator _validator = null;

    /**
     * State value used with validation, to track types of content
     * that is allowed at this point in output stream. Only used if
     * validation is enabled: if so, value is determined via validation
     * callbacks.
     */
    protected int _vldContent = XMLValidator.CONTENT_ALLOW_ANY_TEXT;

    /**
     * Custom validation problem handler, if any.
     */
    protected ValidationProblemHandler _vldProblemHandler = null;

    /*
    /**********************************************************************
    /* State information
    /**********************************************************************
     */

    protected State _state = State.PROLOG;

    /**
     * We'll use a virtual root element (like a document node of sort),
     * to simplify other processing, basically such that there is
     * always a current output element instance, even when in prolog
     * or epilog.
     */
    protected OutputElement _currElem = OutputElement.createRoot();

    /**
     * Flag that is set to true first time something has been output.
     * Generally needed to keep track of whether XML declaration
     * (START_DOCUMENT) can be output or not.
     */
    protected boolean _stateAnyOutput = false;

    /**
     * Flag that is set during time that a start element is "open", ie.
     * START_ELEMENT has been output (and possibly zero or more name
     * space declarations and attributes), before other main-level
     * constructs have been output.
     */
    protected boolean _stateStartElementOpen = false;

    /**
     * Flag that indicates that current element is an empty element (one
     * that is explicitly defined as one, by calling a method -- NOT one
     * that just happens to be empty).
     * This is needed to know what to do when next non-ns/attr node
     * is output; normally a new context is opened, but for empty
     * elements not.
     */
    protected boolean _stateEmptyElement = false;

    /**
     * Value passed as the expected root element, when using the multiple
     * argument {@link #writeDTD} method. Will be used in structurally
     * validating mode (and in dtd-validating mode, since that automatically
     * enables structural validation as well, to pre-filter well-formedness
     * errors that validators might have trouble dealing with).
     */
    protected String _dtdRootElemName = null;

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

    protected OutputElement _outputElemPool = null;

    /**
     * Although pooled objects are small, let's limit the pool size
     * nonetheless, to minimize extra memory usage for deeply nested
     * documents. Even just 4 levels might be enough, 8 should cover
     * > 95% of cases
     */
    final static int MAX_POOL_SIZE = 8;

    protected int _poolSize = 0;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    protected StreamWriterBase(WriterConfig cfg, XmlWriter writer, WNameTable symbols) {
        _config = cfg;
        _xmlWriter = writer;
        _symbols = symbols;

        // // Config settings:

        // !!! TBI:
        _cfgCheckStructure = cfg.willCheckStructure();
        _cfgCheckContent = cfg.willCheckContent();
        _cfgCheckAttrs = cfg.willCheckAttributes();
        _cfgCDataAsText = false;
    }

    /*
    /**********************************************************************
    /* Basic Stax API
    /**********************************************************************
     */

    @Override
    public void close() throws XMLStreamException {
        _finishDocument(false);
    }

    @Override
    public void flush() throws XMLStreamException {
        try {
            _xmlWriter.flush();
        } catch (IOException ie) {
            throw new IoStreamException(ie);
        }
    }

    @Override
    public final NamespaceContext getNamespaceContext() {
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
    public abstract void setDefaultNamespace(String uri) throws XMLStreamException;

    @Override
    public void setNamespaceContext(NamespaceContext ctxt) throws XMLStreamException {
        // This is only allowed before root element output:
        if (_state != State.PROLOG) {
            throwOutputError("Called setNamespaceContext() after having already output root element.");
        }
        _rootNsContext = ctxt;
    }

    @Override
    public final void setPrefix(String prefix, String uri) throws XMLStreamException {
        if (prefix == null) {
            throw new NullPointerException();
        }
        // Are we actually trying to set the default namespace?
        if (prefix.length() == 0) {
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
            if (uri.length() == 0) {
                if (!_config.isXml11()) {
                    throwOutputError(ErrorConsts.ERR_NS_EMPTY);
                }
            }
        }
        _setPrefix(prefix, uri);
    }

    protected abstract void _setPrefix(String prefix, String uri);

    @Override
    public final void writeAttribute(String localName, String value) throws XMLStreamException {
        if (!_stateStartElementOpen) {
            throwOutputError(ErrorConsts.WERR_ATTR_NO_ELEM);
        }
        // note: for attributes, no prefix <=> no namespace, so:
        _writeAttribute(_symbols.findSymbol(localName), value);
    }

    @Override
    public abstract void writeAttribute(String nsURI, String localName, String value) throws XMLStreamException;

    @Override
    public abstract void writeAttribute(String prefix, String nsURI, String localName, String value)
        throws XMLStreamException;

    @Override
    public void writeCData(String data) throws XMLStreamException {
        if (_cfgCDataAsText) {
            writeCharacters(data);
            return;
        }

        _verifyWriteCData();
        // Also, do we do textual content validation?
        if ((_vldContent == XMLValidator.CONTENT_ALLOW_VALIDATABLE_TEXT) && (_validator != null)) {
            // Last arg is false, since we do not know if more text
            // may be added with additional calls
            _validator.validateText(data, false);
        }

        try {
            int ix = _xmlWriter.writeCData(data);
            if (ix >= 0) { // unfixable problems?
                _reportNwfContent(ErrorConsts.WERR_CDATA_CONTENT, Integer.valueOf(ix));
            }
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
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

        // Validator-based validation?
        if (_vldContent <= XMLValidator.CONTENT_ALLOW_WS) {
            if (_vldContent == XMLValidator.CONTENT_ALLOW_NONE) { // never ok
                _reportInvalidContent(CHARACTERS);
            } else { // all-ws is ok...
                if (!TextUtil.isAllWhitespace(text, start, len, _config.isXml11())) {
                    _reportInvalidContent(CHARACTERS);
                }
            }
        } else if (_vldContent == XMLValidator.CONTENT_ALLOW_VALIDATABLE_TEXT) {
            if (_validator != null) {
                // Last arg is false, since we do not know if more text
                // may be added with additional calls
                _validator.validateText(text, start, len, false);
            }
        }
        if (len > 0) {
            try {
                _xmlWriter.writeCharacters(text, start, len);
            } catch (IOException ioe) {
                throw new IoStreamException(ioe);
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

        // Validator-based validation?
        /* Note: although it'd be good to check validity first, we
         * do not know allowed textual content before actually writing
         * pending start element (if any)... so can't call this earlier
         */
        if (_vldContent <= XMLValidator.CONTENT_ALLOW_WS) {
            if (_vldContent == XMLValidator.CONTENT_ALLOW_NONE) { // never ok
                _reportInvalidContent(CHARACTERS);
            } else { // all-ws is ok...
                if (!TextUtil.isAllWhitespace(text, _config.isXml11())) {
                    _reportInvalidContent(CHARACTERS);
                }
            }
        } else if (_vldContent == XMLValidator.CONTENT_ALLOW_VALIDATABLE_TEXT) {
            if (_validator != null) {
                /* Last arg is false, since we do not know if more text
                 * may be added with additional calls
                 */
                _validator.validateText(text, false);
            }
        }

        try {
            _xmlWriter.writeCharacters(text);
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
        }
    }

    @Override
    public void writeComment(String data) throws XMLStreamException {
        _stateAnyOutput = true;
        if (_stateStartElementOpen) {
            _closeStartElement(_stateEmptyElement);
        }
        if (_vldContent == XMLValidator.CONTENT_ALLOW_NONE) { // from DTD, EMPTY
            _reportInvalidContent(COMMENT);
        }

        /* No structural validation needed per se, for comments; they are
         * allowed anywhere in XML content. However, content may need to
         * be checked (by XmlWriter)
         */
        try {
            int ix = _xmlWriter.writeComment(data);
            if (ix >= 0) {
                _reportNwfContent(ErrorConsts.WERR_COMMENT_CONTENT, Integer.valueOf(ix));
            }
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
        }
    }

    @Override
    public abstract void writeDefaultNamespace(String nsURI) throws XMLStreamException;

    @Override
    public final void writeDTD(String dtd) throws XMLStreamException {
        _verifyWriteDTD();
        _dtdRootElemName = ""; // marker to verify only one is output
        try {
            _xmlWriter.writeDTD(dtd);
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
        }
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
        if (_validator != null) {
            _validator.validateElementStart(localName, "", "");
        }
        _writeStartTag(name, true);
    }

    @Override
    public abstract void writeEmptyElement(String nsURI, String localName) throws XMLStreamException;

    @Override
    public abstract void writeEmptyElement(String prefix, String localName, String nsURI) throws XMLStreamException;

    @Override
    public void writeEndDocument() throws XMLStreamException {
        _finishDocument(false);
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

        if (_cfgCheckStructure) {
            /*
            if (expName != null) { // let's just check local name?
                String localName = thisElem.getLocalName();
                if (!localName.equals(expName.getLocalPart())) {
                    _reportNwfStructure("Mismatching close element local name, '"+localName+"'; expected '"+expName.getLocalPart()+"'.");
                }
            }
            */
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
            throw new IoStreamException(ioe);
        }
        if (_currElem.isRoot()) { // (note: we have dummy placeholder elem that contains doc)
            _state = State.EPILOG;
        }

        // Time to validate?
        if (_validator != null) {
            _vldContent = _validator.validateElementEnd(thisElem.getLocalName(), thisElem.getNonNullPrefix(),
                thisElem.getNonNullNamespaceURI());
        }
    }

    @Override
    public void writeEntityRef(String name) throws XMLStreamException {
        _stateAnyOutput = true;
        if (_stateStartElementOpen) {
            _closeStartElement(_stateEmptyElement);
        }

        // Structurally, need to check we are not in prolog/epilog.
        if (_cfgCheckStructure) {
            if (inPrologOrEpilog()) {
                _reportNwfStructure(ErrorConsts.WERR_PROLOG_ENTITY);
            }
        }
        // Validator-based validation?
        if (_vldContent == XMLValidator.CONTENT_ALLOW_NONE) {
            // Char entity, general entity; whatever it is it's invalid...
            _reportInvalidContent(ENTITY_REFERENCE);
        }
        try {
            _xmlWriter.writeEntityReference(_symbols.findSymbol(name));
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
        }
    }

    @Override
    public abstract void writeNamespace(String prefix, String nsURI) throws XMLStreamException;

    @Override
    public void writeProcessingInstruction(String target) throws XMLStreamException {
        writeProcessingInstruction(target, null);
    }

    @Override
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        _stateAnyOutput = true;
        if (_stateStartElementOpen) {
            _closeStartElement(_stateEmptyElement);
        }

        /* Structurally, PIs are always ok (content might not be)...
         * except in empty content models...
         */
        if (_vldContent == XMLValidator.CONTENT_ALLOW_NONE) {
            _reportInvalidContent(PROCESSING_INSTRUCTION);
        }

        try {
            int ix = _xmlWriter.writePI(_symbols.findSymbol(target), data);
            if (ix >= 0) {
                _reportNwfContent(ErrorConsts.WERR_PI_CONTENT, Integer.valueOf(ix));
            }
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
        }
    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        String enc = _config.getActualEncoding();
        if (enc == null) {
            enc = XmlConsts.STAX_DEFAULT_OUTPUT_ENCODING;
            _config.setActualEncodingIfNotSet(enc);
        }
        _writeStartDocument(XmlConsts.STAX_DEFAULT_OUTPUT_VERSION, enc, null);
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
        _writeStartDocument(version, _config.getActualEncoding(), null);
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        _writeStartDocument(version, encoding, null);
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
        if (_validator != null) {
            _validator.validateElementStart(localName, "", "");
        }
        _writeStartTag(name, false);
    }

    @Override
    public abstract void writeStartElement(String nsURI, String localName) throws XMLStreamException;

    @Override
    public abstract void writeStartElement(String prefix, String localName, String nsURI) throws XMLStreamException;

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
    /* TypedXMLStreamWriter2 implementation
    /* (Typed Access API, Stax v3.0)
    /**********************************************************************
     */

    // // // Typed element content write methods

    @Override
    public void writeBoolean(boolean b) throws XMLStreamException {
        writeTypedElement(valueEncoderFactory().getScalarEncoder(b ? "true" : "false"));
    }

    @Override
    public void writeInt(int value) throws XMLStreamException {
        writeTypedElement(valueEncoderFactory().getEncoder(value));
    }

    @Override
    public void writeLong(long value) throws XMLStreamException {
        writeTypedElement(valueEncoderFactory().getEncoder(value));
    }

    @Override
    public void writeFloat(float value) throws XMLStreamException {
        writeTypedElement(valueEncoderFactory().getEncoder(value));
    }

    @Override
    public void writeDouble(double value) throws XMLStreamException {
        writeTypedElement(valueEncoderFactory().getEncoder(value));
    }

    @Override
    public void writeInteger(BigInteger value) throws XMLStreamException {
        // Not optimal, but should do ok:
        writeTypedElement(valueEncoderFactory().getScalarEncoder(value.toString()));
    }

    @Override
    public void writeDecimal(BigDecimal value) throws XMLStreamException {
        // Not optimal, but should do ok:
        writeTypedElement(valueEncoderFactory().getScalarEncoder(value.toString()));
    }

    @Override
    public void writeQName(QName value) throws XMLStreamException {
        writeTypedElement(valueEncoderFactory().getScalarEncoder(_serializeQName(value)));
    }

    @Override
    public final void writeIntArray(int[] value, int from, int length) throws XMLStreamException {
        writeTypedElement(valueEncoderFactory().getEncoder(value, from, length));
    }

    @Override
    public void writeLongArray(long[] value, int from, int length) throws XMLStreamException {
        writeTypedElement(valueEncoderFactory().getEncoder(value, from, length));
    }

    @Override
    public void writeFloatArray(float[] value, int from, int length) throws XMLStreamException {
        writeTypedElement(valueEncoderFactory().getEncoder(value, from, length));
    }

    @Override
    public void writeDoubleArray(double[] value, int from, int length) throws XMLStreamException {
        writeTypedElement(valueEncoderFactory().getEncoder(value, from, length));
    }

    @Override
    public void writeBinary(byte[] value, int from, int length) throws XMLStreamException {
        Base64Variant v = Base64Variants.getDefaultVariant();
        writeTypedElement(valueEncoderFactory().getEncoder(v, value, from, length));
    }

    @Override
    public void writeBinary(Base64Variant v, byte[] value, int from, int length) throws XMLStreamException {
        writeTypedElement(valueEncoderFactory().getEncoder(v, value, from, length));
    }

    private final void writeTypedElement(AsciiValueEncoder enc) throws XMLStreamException {
        _stateAnyOutput = true;
        if (_stateStartElementOpen) {
            _closeStartElement(_stateEmptyElement);
        }
        try {
            _xmlWriter.writeTypedValue(enc);
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
        }
    }

    // // // Typed attribute value write methods

    @Override
    public final void writeBooleanAttribute(String prefix, String nsURI, String localName, boolean value)
        throws XMLStreamException {
        writeTypedAttribute(prefix, nsURI, localName, valueEncoderFactory().getEncoder(value));
    }

    @Override
    public final void writeIntAttribute(String prefix, String nsURI, String localName, int value)
        throws XMLStreamException {
        writeTypedAttribute(prefix, nsURI, localName, valueEncoderFactory().getEncoder(value));
    }

    @Override
    public final void writeLongAttribute(String prefix, String nsURI, String localName, long value)
        throws XMLStreamException {
        writeTypedAttribute(prefix, nsURI, localName, valueEncoderFactory().getEncoder(value));
    }

    @Override
    public final void writeFloatAttribute(String prefix, String nsURI, String localName, float value)
        throws XMLStreamException {
        writeTypedAttribute(prefix, nsURI, localName, valueEncoderFactory().getEncoder(value));
    }

    @Override
    public final void writeDoubleAttribute(String prefix, String nsURI, String localName, double value)
        throws XMLStreamException {
        writeTypedAttribute(prefix, nsURI, localName, valueEncoderFactory().getEncoder(value));
    }

    @Override
    public final void writeIntegerAttribute(String prefix, String nsURI, String localName, BigInteger value)
        throws XMLStreamException {
        writeTypedAttribute(prefix, nsURI, localName, valueEncoderFactory().getScalarEncoder(value.toString()));
    }

    @Override
    public final void writeDecimalAttribute(String prefix, String nsURI, String localName, BigDecimal value)
        throws XMLStreamException {
        writeTypedAttribute(prefix, nsURI, localName, valueEncoderFactory().getScalarEncoder(value.toString()));
    }

    @Override
    public final void writeQNameAttribute(String prefix, String nsURI, String localName, QName value)
        throws XMLStreamException {
        /* Can't use AsciiValueEncoder, since QNames can contain
         * non-ascii characters
         */
        writeAttribute(prefix, nsURI, localName, _serializeQName(value));
    }

    @Override
    public void writeIntArrayAttribute(String prefix, String nsURI, String localName, int[] value)
        throws XMLStreamException {
        writeTypedAttribute(prefix, nsURI, localName, valueEncoderFactory().getEncoder(value, 0, value.length));
    }

    @Override
    public void writeLongArrayAttribute(String prefix, String nsURI, String localName, long[] value)
        throws XMLStreamException {
        writeTypedAttribute(prefix, nsURI, localName, valueEncoderFactory().getEncoder(value, 0, value.length));
    }

    @Override
    public void writeFloatArrayAttribute(String prefix, String nsURI, String localName, float[] value)
        throws XMLStreamException {
        writeTypedAttribute(prefix, nsURI, localName, valueEncoderFactory().getEncoder(value, 0, value.length));
    }

    @Override
    public void writeDoubleArrayAttribute(String prefix, String nsURI, String localName, double[] value)
        throws XMLStreamException {
        writeTypedAttribute(prefix, nsURI, localName, valueEncoderFactory().getEncoder(value, 0, value.length));
    }

    @Override
    public void writeBinaryAttribute(String prefix, String nsURI, String localName, byte[] value)
        throws XMLStreamException {
        Base64Variant v = Base64Variants.getDefaultVariant();
        writeTypedAttribute(prefix, nsURI, localName, valueEncoderFactory().getEncoder(v, value, 0, value.length));
    }

    @Override
    public void writeBinaryAttribute(Base64Variant v, String prefix, String nsURI, String localName, byte[] value)
        throws XMLStreamException {
        writeTypedAttribute(prefix, nsURI, localName, valueEncoderFactory().getEncoder(v, value, 0, value.length));
    }

    /**
     * Need to leave implementation of this method abstract, because
     * repairing and non-repairing modes differ in how names are
     * handled.
     */
    public abstract void writeTypedAttribute(String prefix, String nsURI, String localName, AsciiValueEncoder enc)
        throws XMLStreamException;

    protected abstract String _serializeQName(QName name) throws XMLStreamException;

    /*
    /**********************************************************************
    /* XMLStreamWriter2 methods (StAX2)
    /**********************************************************************
     */

    @Override
    public void writeSpace(String text) throws XMLStreamException {
        try {
            _xmlWriter.writeSpace(text);
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
        }
    }

    @Override
    public void writeSpace(char[] cbuf, int offset, int len) throws XMLStreamException {
        try {
            _xmlWriter.writeSpace(cbuf, offset, len);
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
        }
    }

    // From base class:
    //public void copyEventFromReader(XMLStreamReader2 sr, boolean preserveEventData) throws XMLStreamException

    /*
    /**********************************************************************
    /* Stax2, output handling
    /**********************************************************************
     */

    @Override
    public void closeCompletely() throws XMLStreamException {
        _finishDocument(true);
    }

    /*
    /**********************************************************************
    /* Stax2, config
    /**********************************************************************
     */

    // NOTE: getProperty() defined in Stax 1.0 interface

    @Override
    public boolean isPropertySupported(String name) {
        // !!! TBI: not all these properties are really supported
        return _config.isPropertySupported(name);
    }

    /**
     * @param name Name of the property to set
     * @param value Value to set property to.
     *
     * @return True, if the specified property was <b>succesfully</b>
     *    set to specified value; false if its value was not changed
     */
    @Override
    public boolean setProperty(String name, Object value) {
        /* Note: can not call local method, since it'll return false for
         * recognized but non-mutable properties
         */
        return _config.setProperty(name, value);
    }

    @Override
    public XMLValidator validateAgainst(XMLValidationSchema schema) throws XMLStreamException {
        XMLValidator vld = schema.createValidator(this);

        if (_validator == null) {
            /* Need to enable other validation modes? Structural validation
             * should always be done when we have other validators as well,
             * as well as attribute uniqueness checks.
             */
            _cfgCheckStructure = true;
            _cfgCheckAttrs = true;
            _validator = vld;
        } else {
            _validator = new ValidatorPair(_validator, vld);
        }
        return vld;
    }

    @Override
    public XMLValidator stopValidatingAgainst(XMLValidationSchema schema) throws XMLStreamException {
        XMLValidator[] results = new XMLValidator[2];
        XMLValidator found = null;
        if (ValidatorPair.removeValidator(_validator, schema, results)) { // found
            found = results[0];
            _validator = results[1];
            found.validationCompleted(false);
            if (_validator == null) {
                resetValidationFlags();
            }
        }
        return found;
    }

    @Override
    public XMLValidator stopValidatingAgainst(XMLValidator validator) throws XMLStreamException {
        XMLValidator[] results = new XMLValidator[2];
        XMLValidator found = null;
        if (ValidatorPair.removeValidator(_validator, validator, results)) { // found
            found = results[0];
            _validator = results[1];
            found.validationCompleted(false);
            if (_validator == null) {
                resetValidationFlags();
            }
        }
        return found;
    }

    @Override
    public ValidationProblemHandler setValidationProblemHandler(ValidationProblemHandler h) {
        ValidationProblemHandler oldH = _vldProblemHandler;
        _vldProblemHandler = h;
        return oldH;
    }

    private void resetValidationFlags() {
        _cfgCheckStructure = _config.willCheckStructure();
        _cfgCheckAttrs = _config.willCheckAttributes();
    }

    /*
    /**********************************************************************
    /* Stax2, other accessors, mutators
    /**********************************************************************
     */

    @Override
    public XMLStreamLocation2 getLocation() {
        return new LocationImpl(null, null, // pub/sys ids not yet known
            _xmlWriter.getAbsOffset(), _xmlWriter.getRow(), _xmlWriter.getColumn());
    }

    @Override
    public String getEncoding() {
        return _config.getActualEncoding();
    }

    /*
    /**********************************************************************
    /* StAX2, output methods
    /**********************************************************************
     */

    @Override
    public void writeCData(char[] cbuf, int start, int len) throws XMLStreamException {
        if (_cfgCDataAsText) {
            writeCharacters(cbuf, start, len);
            return;
        }

        _verifyWriteCData();
        int ix;
        try {
            ix = _xmlWriter.writeCData(cbuf, start, len);
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
        }
        if (ix >= 0) { // problems that could not to be fixed?
            _reportNwfContent(ErrorConsts.WERR_CDATA_CONTENT, Integer.valueOf(ix));
        }
    }

    public void writeDTD(DTDInfo info) throws XMLStreamException {
        writeDTD(info.getDTDRootName(), info.getDTDSystemId(), info.getDTDPublicId(), info.getDTDInternalSubset());
    }

    @Override
    public void writeDTD(String rootName, String systemId, String publicId, String internalSubset)
        throws XMLStreamException {
        _verifyWriteDTD();
        _dtdRootElemName = rootName;
        try {
            _xmlWriter.writeDTD(_symbols.findSymbol(rootName), systemId, publicId, internalSubset);
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
        }
    }

    /**
     * Similar to {@link #writeEndElement}, but never allows implicit
     * creation of empty elements.
     */
    @Override
    public void writeFullEndElement() throws XMLStreamException {
        if (_stateStartElementOpen && _stateEmptyElement) {
            _stateEmptyElement = false;
            /* The open element was an empty element (created via
             * explicit call using writeEmptyElement); not affected
             */
            _closeStartElement(true);
        }
        writeEndElement();
    }

    @Override
    public void writeStartDocument(String version, String encoding, boolean standAlone) throws XMLStreamException {
        _writeStartDocument(version, encoding, standAlone ? "yes" : "no");
    }

    @Override
    public void writeRaw(String text) throws XMLStreamException {
        _stateAnyOutput = true;
        if (_stateStartElementOpen) {
            _closeStartElement(_stateEmptyElement);
        }
        try {
            _xmlWriter.writeRaw(text, 0, text.length());
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
        }
    }

    @Override
    public void writeRaw(String text, int start, int offset) throws XMLStreamException {
        _stateAnyOutput = true;
        if (_stateStartElementOpen) {
            _closeStartElement(_stateEmptyElement);
        }
        try {
            _xmlWriter.writeRaw(text, start, offset);
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
        }
    }

    @Override
    public void writeRaw(char[] text, int offset, int length) throws XMLStreamException {
        _stateAnyOutput = true;
        if (_stateStartElementOpen) {
            _closeStartElement(_stateEmptyElement);
        }
        try {
            _xmlWriter.writeRaw(text, offset, length);
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
        }
    }

    /*
    /**********************************************************************
    /* ValidationContext interface (Stax2, validation)
    /**********************************************************************
     */

    @Override
    public String getXmlVersion() {
        return _config.isXml11() ? "1.1" : "1.0";
    }

    @Override
    public QName getCurrentElementName() {
        return _currElem.getQName();
    }

    // already part of NamespaceContext impl above...
    //public String getNamespaceURI(String prefix)

    /**
     * As of now, there is no way to specify the base URI. Could be improved
     * in future, if xml:base is supported.
     */
    @Override
    public String getBaseUri() {
        return null;
    }

    @Override
    public Location getValidationLocation() {
        return getLocation();
    }

    @Override
    public void reportProblem(XMLValidationProblem prob) throws XMLStreamException {
        // Custom handler set? If so, it'll take care of it:
        if (_vldProblemHandler != null) {
            _vldProblemHandler.reportProblem(prob);
            return;
        }

        // Let's throw an exception for fatal and non-fatal errors:
        if (prob.getSeverity() >= XMLValidationProblem.SEVERITY_ERROR) {
            throw ValidationException.create(prob);
        }
    }

    /**
     * Adding default attribute values does not usually make sense on
     * output side, so the implementation is a NOP for now.
     */
    @Override
    public int addDefaultAttribute(String localName, String uri, String prefix, String value) {
        // nothing to do, but to indicate we didn't add it...
        return -1;
    }

    // // // Notation/entity access: not (yet?) implemented

    @Override
    public boolean isNotationDeclared(String name) {
        return false;
    }

    @Override
    public boolean isUnparsedEntityDeclared(String name) {
        return false;
    }

    // // // Attribute access: not yet implemented:

    /* !!! TODO: Implement attribute access (iff validate-attributes
     *   enabled?
     */
    @Override
    public int getAttributeCount() {
        return 0;
    }

    @Override
    public String getAttributeLocalName(int index) {
        return null;
    }

    @Override
    public String getAttributeNamespace(int index) {
        return null;
    }

    @Override
    public String getAttributePrefix(int index) {
        return null;
    }

    @Override
    public String getAttributeValue(int index) {
        return null;
    }

    @Override
    public String getAttributeValue(String nsURI, String localName) {
        return null;
    }

    @Override
    public String getAttributeType(int index) {
        return "";
    }

    @Override
    public int findAttributeIndex(String nsURI, String localName) {
        return -1;
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
    /**
     * Method called to close an open start element, when another
     * main-level element (not namespace declaration or attribute)
     * is being output; except for end element which is handled differently.
     */
    protected void _closeStartElement(boolean emptyElem) throws XMLStreamException {
        _stateStartElementOpen = false;
        try {
            if (emptyElem) {
                _xmlWriter.writeStartTagEmptyEnd();
            } else {
                _xmlWriter.writeStartTagEnd();
            }
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
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

    protected final boolean inPrologOrEpilog() {
        return (_state != State.TREE);
    }

    protected final ValueEncoderFactory valueEncoderFactory() {
        if (_valueEncoderFactory == null) {
            _valueEncoderFactory = new ValueEncoderFactory();
        }
        return _valueEncoderFactory;
    }

    /*
    /**********************************************************************
    /* Package methods, write helpers
    /**********************************************************************
     */

    protected final void _writeAttribute(WName name, String value) throws XMLStreamException {
        if (_cfgCheckAttrs) { // still need to ensure no duplicate attrs?
            _verifyWriteAttr(name);
        }
        try {
            _xmlWriter.writeAttribute(name, value);
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
        }
    }

    protected final void _writeAttribute(WName name, AsciiValueEncoder enc) throws XMLStreamException {
        if (_cfgCheckAttrs) { // still need to ensure no duplicate attrs?
            _verifyWriteAttr(name);
        }
        try {
            _xmlWriter.writeAttribute(name, enc);
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
        }
    }

    protected final void _writeDefaultNamespace(String uri) throws XMLStreamException {
        WName name = _symbols.findSymbol("xmlns");
        try {
            _xmlWriter.writeAttribute(name, uri);
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
        }
    }

    protected final void _writeNamespace(String prefix, String uri) throws XMLStreamException {
        WName name = _symbols.findSymbol("xmlns", prefix);
        try {
            _xmlWriter.writeAttribute(name, uri);
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
        }
    }

    protected void _writeStartDocument(String version, String encoding, String standAlone) throws XMLStreamException {
        /* Not legal to output XML declaration if there has been ANY
         * output prior... that is, if we validate the structure.
         */
        if (_cfgCheckStructure) {
            if (_stateAnyOutput) {
                _reportNwfStructure(ErrorConsts.WERR_DUP_XML_DECL);
            }
        }

        _stateAnyOutput = true;

        if (_cfgCheckContent) {
            // !!! If and how to check encoding?
            // if (encoding != null) { }
            if (version != null && version.length() > 0) {
                if (!(version.equals(XmlConsts.XML_V_10_STR) || version.equals(XmlConsts.XML_V_11_STR))) {
                    _reportNwfContent("Illegal version argument ('" + version + "'); should only use '"
                        + XmlConsts.XML_V_10_STR + "' or '" + XmlConsts.XML_V_11_STR + "'");
                }
            }
        }

        if (version == null || version.length() == 0) {
            version = XmlConsts.STAX_DEFAULT_OUTPUT_VERSION;
        }
        if (XmlConsts.XML_V_11_STR.equals(version)) {
            _config.enableXml11();
            _xmlWriter.enableXml11();
        }

        if (encoding != null && encoding.length() > 0) {
            /* What about conflicting encoding? Let's only update encoding,
             * if it wasn't set.
             */
            _config.setActualEncodingIfNotSet(encoding);
        }
        try {
            _xmlWriter.writeXmlDeclaration(version, encoding, standAlone);
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
        }
    }

    protected void _writeStartTag(WName name, boolean isEmpty) throws XMLStreamException {
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
            throw new IoStreamException(ioe);
        }
        _stateEmptyElement = isEmpty;
    }

    protected void _writeStartTag(WName name, boolean isEmpty, String uri) throws XMLStreamException {
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
            throw new IoStreamException(ioe);
        }
        _stateEmptyElement = isEmpty;
    }

    /*
    /**********************************************************************
    /* Package methods, validation
    /**********************************************************************
     */

    protected final void _verifyWriteAttr(WName name) {
        // !!! TBI
    }

    /**
     * Method that is called to ensure that we can start writing an
     * element, both from structural point of view, and from syntactic
     * (close previously open start element, if any). Note that since
     * it needs to be called before writing out anything, no namespace
     * bindings have been (or can be) output, and hence given prefix
     * may not be one that actually gets used.
     */
    protected void _verifyStartElement(String prefix, String localName) throws XMLStreamException {
        // Need to finish an open start element?
        if (_stateStartElementOpen) {
            _closeStartElement(_stateEmptyElement);
        } else if (_state == State.PROLOG) {
            _verifyRootElement(prefix, localName);
        } else if (_state == State.EPILOG) {
            if (_cfgCheckStructure) {
                String name = (prefix == null) ? localName : (prefix + ":" + localName);
                _reportNwfStructure(ErrorConsts.WERR_PROLOG_SECOND_ROOT, name);
            }
            /* When outputting a fragment, need to reset this to the
             * tree. No point in trying to verify the root element?
             */
            _state = State.TREE;
        }
    }

    protected final void _verifyWriteCData() throws XMLStreamException {
        _stateAnyOutput = true;
        if (_stateStartElementOpen) {
            _closeStartElement(_stateEmptyElement);
        }

        // Not legal outside main element tree:
        if (_cfgCheckStructure) {
            if (inPrologOrEpilog()) {
                _reportNwfStructure(ErrorConsts.WERR_PROLOG_CDATA);
            }
        }
    }

    protected final void _verifyWriteDTD() throws XMLStreamException {
        if (_cfgCheckStructure) {
            if (_state != State.PROLOG) {
                throw new XMLStreamException(
                    "Can not write DOCTYPE declaration (DTD) when not in prolog any more (state " + _state
                        + "; start element(s) written)");
            }
            // And let's also check that we only output one...
            if (_dtdRootElemName != null) {
                throw new XMLStreamException("Trying to write multiple DOCTYPE declarations");
            }
        }
    }

    protected void _verifyRootElement(String prefix, String localName) throws XMLValidationException {
        // !!! TBI: only relevant if we are actually validating?

        _state = State.TREE;
    }

    /*
    /**********************************************************************
    /* Package methods, basic output problem reporting
    /**********************************************************************
     */

    protected static void throwOutputError(String msg) throws XMLStreamException {
        throw new StreamExceptionBase(msg);
    }

    protected static void throwOutputError(String format, Object arg) throws XMLStreamException {
        String msg = MessageFormat.format(format, new Object[] { arg });
        throwOutputError(msg);
    }

    /**
     * Method called when an illegal method (namespace-specific method
     * on non-ns writer) is called by the application.
     */
    protected static void reportIllegalMethod(String msg) throws XMLStreamException {
        throwOutputError(msg);
    }

    /**
     * This is the method called when an output method call violates
     * structural well-formedness checks
     * and structural checking
     * is enabled.
     */
    protected static void _reportNwfStructure(String msg) throws XMLStreamException {
        throwOutputError(msg);
    }

    protected static void _reportNwfStructure(String msg, Object arg) throws XMLStreamException {
        throwOutputError(msg, arg);
    }

    /**
     * This is the method called when an output method call violates
     * content well-formedness checks
     * and content validation
     * is enabled.
     */
    protected static void _reportNwfContent(String msg) throws XMLStreamException {
        throwOutputError(msg);
    }

    protected static void _reportNwfContent(String msg, Object arg) throws XMLStreamException {
        throwOutputError(msg, arg);
    }

    /**
     * This is the method called when an output method call violates
     * attribute well-formedness checks (trying to output dup attrs)
     * and name validaty checking
     * is enabled.
     */
    protected static void _reportNwfAttr(String msg) throws XMLStreamException {
        throwOutputError(msg);
    }

    protected static void _reportNwfAttr(String msg, Object arg) throws XMLStreamException {
        throwOutputError(msg, arg);
    }

    protected static void _reportNwfName(String msg) throws XMLStreamException {
        throwOutputError(msg);
    }

    protected static void throwFromIOE(IOException ioe) throws XMLStreamException {
        throw new IoStreamException(ioe);
    }

    protected static void reportIllegalArg(String msg) throws IllegalArgumentException {
        throw new IllegalArgumentException(msg);
    }

    /*
    protected static void throwIllegalState(String msg)
        throws IllegalArgumentException
    {
        throw new IllegalStateException(msg);
    }
    */

    /*
    /**********************************************************************
    /* Package methods, output validation problem reporting
    /**********************************************************************
     */

    protected void _reportInvalidContent(int evtType) throws XMLStreamException {
        switch (_vldContent) {
            case XMLValidator.CONTENT_ALLOW_NONE:
                _reportValidationProblem(MessageFormat.format(ErrorConsts.VERR_EMPTY, _currElem.getNameDesc(),
                    ErrorConsts.tokenTypeDesc(evtType)));
                break;

            case XMLValidator.CONTENT_ALLOW_WS:
                _reportValidationProblem(MessageFormat.format(ErrorConsts.VERR_NON_MIXED, _currElem.getNameDesc()));
                break;

            case XMLValidator.CONTENT_ALLOW_VALIDATABLE_TEXT:
            case XMLValidator.CONTENT_ALLOW_ANY_TEXT:
                /* Not 100% sure if this should ever happen... depends on
                 * interpretation of 'any' content model?
                 */
                _reportValidationProblem(MessageFormat.format(ErrorConsts.VERR_ANY, _currElem.getNameDesc(),
                    ErrorConsts.tokenTypeDesc(evtType)));
                break;

            default: // should never occur:
                _reportValidationProblem("Internal error: trying to report invalid content for " + evtType);
        }
    }

    public void _reportValidationProblem(String msg) throws XMLStreamException {
        reportProblem(new XMLValidationProblem(getValidationLocation(), msg, XMLValidationProblem.SEVERITY_ERROR));
    }

    /*
    /**********************************************************************
    /* Package methods, output validation problem reporting
    /**********************************************************************
     */

    private final void _finishDocument(boolean forceRealClose) throws XMLStreamException {
        // Is tree still open?
        if (_state != State.EPILOG) {
            if (_cfgCheckStructure && _state == State.PROLOG) {
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
            _xmlWriter.close(forceRealClose);
        } catch (IOException ie) {
            throw new IoStreamException(ie);
        }
    }

    @Override
    public String toString() {
        return "[StreamWriter: " + getClass() + ", underlying outputter: "
            + ((_xmlWriter == null) ? "NULL" : _xmlWriter.toString());
    }
}
