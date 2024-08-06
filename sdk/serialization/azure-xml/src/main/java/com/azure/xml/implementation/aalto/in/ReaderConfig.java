// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.in;

import com.azure.xml.implementation.aalto.AaltoInputProperties;
import com.azure.xml.implementation.aalto.impl.CommonConfig;
import com.azure.xml.implementation.aalto.util.BufferRecycler;
import com.azure.xml.implementation.aalto.util.CharsetNames;
import com.azure.xml.implementation.aalto.util.UriCanonicalizer;
import com.azure.xml.implementation.aalto.util.XmlCharTypes;
import com.azure.xml.implementation.aalto.util.XmlConsts;
import com.azure.xml.implementation.stax2.XMLInputFactory2;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Objects;

/**
 * This is the shared configuration object passed by the factory to reader,
 * and by reader to whoever needs it (scanners at least).
 */
public final class ReaderConfig extends CommonConfig {
    public final static int DEFAULT_SMALL_BUFFER_LEN = 60;

    public final static int DEFAULT_CHAR_BUFFER_LEN = 4000;

    public final static int STANDALONE_UNKNOWN = 0;
    public final static int STANDALONE_YES = 1;
    public final static int STANDALONE_NO = 2;

    // Standard Stax flags:
    private final static int F_NS_AWARE = 0x0001;
    private final static int F_COALESCING = 0x0002;
    private final static int F_DTD_AWARE = 0x0004;
    private final static int F_DTD_VALIDATING = 0x0008;
    private final static int F_EXPAND_ENTITIES = 0x0010;

    // Standard Stax2 flags:
    private final static int F_LAZY_PARSING = 0x0100;
    private final static int F_INTERN_NAMES = 0x0200;
    private final static int F_INTERN_NS_URIS = 0x0400;
    private final static int F_REPORT_CDATA = 0x0800;
    private final static int F_PRESERVE_LOCATION = 0x1000;
    private final static int F_AUTO_CLOSE_INPUT = 0x2000;

    // Custom flags:
    private final static int F_RETAIN_ATTRIBUTE_GENERAL_ENTITIES = 0x4000;

    /**
     * These are the default settings for XMLInputFactory.
     */
    private final static int DEFAULT_FLAGS = F_NS_AWARE | F_DTD_AWARE | F_EXPAND_ENTITIES | F_LAZY_PARSING
    // by default we do intern names, ns uris...
        | F_INTERN_NAMES | F_INTERN_NS_URIS
        // and will report CDATA as such (and not as CHARACTERS)
        | F_REPORT_CDATA | F_PRESERVE_LOCATION;

    private final static HashMap<String, Object> sProperties;
    static {
        sProperties = new HashMap<>();
        /* 28-Oct-2006, tatus: Let's recognize it, but not allow to be
         *   disabled. Can/needs to be changed if we'll support it.
         */
        sProperties.put(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
        sProperties.put(XMLInputFactory.IS_VALIDATING,
            //Boolean.FALSE);
            F_DTD_VALIDATING);
        sProperties.put(XMLInputFactory.IS_COALESCING, F_COALESCING);
        sProperties.put(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, F_EXPAND_ENTITIES);
        sProperties.put(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        sProperties.put(XMLInputFactory.SUPPORT_DTD, F_DTD_AWARE);
        sProperties.put(XMLInputFactory.REPORTER, null);
        sProperties.put(XMLInputFactory.RESOLVER, null);
        sProperties.put(XMLInputFactory.ALLOCATOR, null);

        // // // Stax2:
        sProperties.put(XMLInputFactory2.P_LAZY_PARSING, F_LAZY_PARSING);
        sProperties.put(XMLInputFactory2.P_INTERN_NAMES, F_INTERN_NAMES);
        sProperties.put(XMLInputFactory2.P_INTERN_NS_URIS, F_INTERN_NS_URIS);
        sProperties.put(XMLInputFactory2.P_AUTO_CLOSE_INPUT, F_AUTO_CLOSE_INPUT);
        sProperties.put(XMLInputFactory2.P_PRESERVE_LOCATION, F_PRESERVE_LOCATION);

        // (ones with fixed defaults)

        /* Should we ever support this property? For now, we really shouldn't
         * report white space in prolog/epilog, as it's not really part
         * of document content.
         */
        sProperties.put(XMLInputFactory2.P_REPORT_PROLOG_WHITESPACE, Boolean.FALSE);
        sProperties.put(XMLInputFactory2.P_REPORT_CDATA, F_REPORT_CDATA);

        sProperties.put(XMLInputFactory2.P_PRESERVE_LOCATION, Boolean.TRUE);

        // !!! Not really implemented, but let's recognize it
        sProperties.put(XMLInputFactory2.P_DTD_OVERRIDE, null);

        // Custom ones:

        // [aalto-xml#65]: Allow disabling processing of GEs in attribute values:
        sProperties.put(AaltoInputProperties.P_RETAIN_ATTRIBUTE_GENERAL_ENTITIES, F_RETAIN_ATTRIBUTE_GENERAL_ENTITIES);
    }

    /**
     * A single encoding context instance is shared between all ReaderConfig
     * instances created for readers by an input factory. It is used
     * for sharing symbol tables.
     */
    private final EncodingContext mEncCtxt;

    /**
     * For efficient access by qualified name, as well as uniqueness
     * checks, namespace URIs need to be canonicalized.
     */
    private final UriCanonicalizer mCanonicalizer;

    /**
     * Encoding passed in as external information, possibly from source
     * from which xml content was gained from (for example, as an HTTP
     * header, or file metadata).
     */
    private final String mExtEncoding;

    /**
     * Name of the actual encoding that input was found to be in (if any
     * -- can't be determined if a Reader was passed in).
     */
    private String mActualEncoding = null;

    private String mXmlDeclVersion = null;
    private String mXmlDeclEncoding = null;
    private int mXmlDeclStandalone = STANDALONE_UNKNOWN;

    private XMLReporter mReporter;
    private XMLResolver mResolver;

    /*
    /**********************************************************************
    /* Buffer recycling:
    /**********************************************************************
     */

    /**
     * This <code>ThreadLocal</code> contains a {@link SoftReference}
     * to a {@link BufferRecycler} used to provide a low-cost
     * buffer recycling between Reader instances.
     */
    final static ThreadLocal<SoftReference<BufferRecycler>> _recyclerRef = new ThreadLocal<>();

    /**
     * This is the actually container of the recyclable buffers. It
     * is obtained via ThreadLocal/SoftReference combination, if one
     * exists, when Config instance is created. If one does not
     * exist, it will created first time a buffer is returned.
     */
    private BufferRecycler _currRecycler = null;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    private ReaderConfig(String extEnc, EncodingContext encCtxt, int flags, int flagMods, XMLReporter rep,
        XMLResolver res, UriCanonicalizer canonicalizer) {
        super(flags, flagMods);
        mExtEncoding = extEnc;

        /* Ok, let's then see if we can find a buffer recycler. Since they
         * are lazily constructed, and since GC may just flush them out
         * on its whims, it's possible we might not find one. That's ok;
         * we can reconstruct one if and when we are to return one or more
         * buffers.
         */
        SoftReference<BufferRecycler> ref = _recyclerRef.get();
        if (ref != null) {
            _currRecycler = ref.get();
        }
        mEncCtxt = encCtxt;
        _flags = flags;
        _flagMods = flagMods;
        mReporter = rep;
        mResolver = res;
        mCanonicalizer = canonicalizer;
    }

    public ReaderConfig() {
        this(null, new EncodingContext(), DEFAULT_FLAGS, 0, null, null, new UriCanonicalizer());
    }

    public void setActualEncoding(String actualEnc) {
        mActualEncoding = actualEnc;
    }

    public void setXmlDeclInfo(int version, String xmlDeclEnc, String standalone) {
        if (version == XmlConsts.XML_V_10) {
            mXmlDeclVersion = XmlConsts.XML_V_10_STR;
        } else if (version == XmlConsts.XML_V_11) {
            mXmlDeclVersion = XmlConsts.XML_V_11_STR;
        } else {
            mXmlDeclVersion = null;
        }
        mXmlDeclEncoding = xmlDeclEnc;
        if (Objects.equals(standalone, XmlConsts.XML_SA_YES)) {
            mXmlDeclStandalone = STANDALONE_YES;
        } else if (Objects.equals(standalone, XmlConsts.XML_SA_NO)) {
            mXmlDeclStandalone = STANDALONE_NO;
        } else {
            mXmlDeclStandalone = STANDALONE_UNKNOWN;
        }
    }

    /*
    /**********************************************************************
    /* Additional configuration setters
    /**********************************************************************
     */

    // // Stax:

    public void setXMLReporter(XMLReporter r) {
        mReporter = r;
    }

    public void setXMLResolver(XMLResolver r) {
        mResolver = r;
    }
    /*
    /**********************************************************************
    /* Common accessors from CommonConfig
    /**********************************************************************
     */

    public ReaderConfig createNonShared(String extEnc) {
        return new ReaderConfig(extEnc, mEncCtxt, _flags, _flagMods, mReporter, mResolver, mCanonicalizer);
    }

    @Override
    public String getExternalEncoding() {
        return mExtEncoding;
    }

    @Override
    public String getActualEncoding() {
        return mActualEncoding;
    }

    @Override
    public boolean isXml11() {
        return false;
    }

    /*
    /**********************************************************************
    /* Standard accessors, configurable properties
    /**********************************************************************
     */

    @Override
    public Object getProperty(String name, boolean isMandatory) {
        Object ob = sProperties.get(name);
        if (ob == null) {
            // Might still have it though
            if (sProperties.containsKey(name)) {
                return null;
            }
            return super.getProperty(name, isMandatory);
        }
        if (ob instanceof Boolean) {
            return ob;
        }
        if (!(ob instanceof Integer)) {
            throw new RuntimeException("Internal error: unrecognized property value type: " + ob.getClass().getName());
        }
        int f = (Integer) ob;
        return hasFlag(f);
    }

    @Override
    public boolean setProperty(String name, Object value) {
        Object ob = sProperties.get(name);
        if (ob == null) {
            // Might still have it though
            if (sProperties.containsKey(name)) {
                return false;
            }
            return super.setProperty(name, value);
        }
        if (ob instanceof Boolean) { // immutable
            return false;
        }
        if (!(ob instanceof Integer)) {
            throw new RuntimeException("Internal error");
        }
        int f = (Integer) ob;
        boolean state = (Boolean) value;
        setFlag(f, state);
        return true;
    }

    @Override
    public boolean isPropertySupported(String propName) {
        return sProperties.containsKey(propName) || super.isPropertySupported(propName);
    }

    public XMLReporter getXMLReporter() {
        return mReporter;
    }

    public XMLResolver getXMLResolver() {
        return mResolver;
    }

    // // // Stax standard properties

    public boolean willExpandEntities() {
        return hasFlag(F_EXPAND_ENTITIES);
    }

    public boolean willCoalesceText() {
        return hasFlag(F_COALESCING);
    }

    // // // Stax2 standard properties

    public boolean willParseLazily() {
        return hasFlag(F_LAZY_PARSING);
    }

    public boolean willReportCData() {
        return hasFlag(F_REPORT_CDATA);
    }

    public boolean willAutoCloseInput() {
        return hasFlag(F_AUTO_CLOSE_INPUT);
    }

    // // // Support for things that must be explicitly enabled

    // // // Custom properties

    /**
     * Accessor for checking configured state of
     * {@link AaltoInputProperties#P_RETAIN_ATTRIBUTE_GENERAL_ENTITIES}.
     *
     * @return Whether the property is enabled or disabled
     *
     * @since 1.3
     */
    public boolean willRetainAttributeGeneralEntities() {
        return hasFlag(F_RETAIN_ATTRIBUTE_GENERAL_ENTITIES);
    }

    /*
    /**********************************************************************
    /* Accessors, detected properties
    /**********************************************************************
     */

    // // // XML declaration info

    public String getXmlDeclVersion() {
        return mXmlDeclVersion;
    }

    public String getXmlDeclEncoding() {
        return mXmlDeclEncoding;
    }

    public int getXmlDeclStandalone() {
        return mXmlDeclStandalone;
    }

    /*
    /**********************************************************************
    /* Canonicalization support
    /**********************************************************************
     */

    public String canonicalizeURI(char[] buf, int uriLen) {
        return mCanonicalizer.canonicalizeURI(buf, uriLen);
    }

    /*
    /**********************************************************************
    /* Buffer recycling:
    /**********************************************************************
     */

    public char[] allocSmallCBuffer(int minSize) {
        if (_currRecycler != null) {
            char[] result = _currRecycler.getSmallCBuffer(minSize);
            if (result != null) {
                return result;
            }
        }
        // Nope; no recycler, or it has no suitable buffers, let's create:
        return new char[minSize];
    }

    public void freeSmallCBuffer(char[] buffer) {
        // Need to create (and assign) the buffer?
        if (_currRecycler == null) {
            _currRecycler = createRecycler();
        }
        _currRecycler.returnSmallCBuffer(buffer);
    }

    public char[] allocMediumCBuffer(int minSize) {
        if (_currRecycler != null) {
            char[] result = _currRecycler.getMediumCBuffer(minSize);
            if (result != null) {
                return result;
            }
        }
        return new char[minSize];
    }

    public void freeMediumCBuffer(char[] buffer) {
        if (_currRecycler == null) {
            _currRecycler = createRecycler();
        }
        _currRecycler.returnMediumCBuffer(buffer);
    }

    public char[] allocFullCBuffer(int minSize) {
        if (_currRecycler != null) {
            char[] result = _currRecycler.getFullCBuffer(minSize);
            if (result != null) {
                return result;
            }
        }
        return new char[minSize];
    }

    public void freeFullCBuffer(char[] buffer) {
        // Need to create (and assign) the buffer?
        if (_currRecycler == null) {
            _currRecycler = createRecycler();
        }
        _currRecycler.returnFullCBuffer(buffer);
    }

    public byte[] allocFullBBuffer(int minSize) {
        if (_currRecycler != null) {
            byte[] result = _currRecycler.getFullBBuffer(minSize);
            if (result != null) {
                return result;
            }
        }
        return new byte[minSize];
    }

    public void freeFullBBuffer(byte[] buffer) {
        // Need to create (and assign) the buffer?
        if (_currRecycler == null) {
            _currRecycler = createRecycler();
        }
        _currRecycler.returnFullBBuffer(buffer);
    }

    private BufferRecycler createRecycler() {
        BufferRecycler recycler = new BufferRecycler();
        // No way to reuse/reset SoftReference, have to create new always:
        _recyclerRef.set(new SoftReference<>(recycler));
        return recycler;
    }

    /*
    /**********************************************************************
    /* Symbol table reusing, character types
    /**********************************************************************
     */

    public ByteBasedPNameTable getBBSymbols() {
        if (Objects.equals(mActualEncoding, CharsetNames.CS_UTF8)) {
            return mEncCtxt.getUtf8Symbols();
        }
        if (Objects.equals(mActualEncoding, CharsetNames.CS_ISO_LATIN1)) {
            return mEncCtxt.getLatin1Symbols();
        }
        if (Objects.equals(mActualEncoding, CharsetNames.CS_US_ASCII)) {
            return mEncCtxt.getAsciiSymbols();
        }
        throw new Error("Internal error, unknown encoding '" + mActualEncoding + "'");
    }

    public CharBasedPNameTable getCBSymbols() {
        return mEncCtxt.getSymbols();
    }

    public void updateBBSymbols(ByteBasedPNameTable sym) {
        if (Objects.equals(mActualEncoding, CharsetNames.CS_UTF8)) {
            mEncCtxt.updateUtf8Symbols(sym);
        } else if (Objects.equals(mActualEncoding, CharsetNames.CS_ISO_LATIN1)) {
            mEncCtxt.updateLatin1Symbols(sym);
        } else if (Objects.equals(mActualEncoding, CharsetNames.CS_US_ASCII)) {
            mEncCtxt.updateAsciiSymbols(sym);
        } else {
            throw new Error("Internal error, unknown encoding '" + mActualEncoding + "'");
        }
    }

    public void updateCBSymbols(CharBasedPNameTable sym) {
        mEncCtxt.updateSymbols(sym);
    }

    public XmlCharTypes getCharTypes() {
        if (Objects.equals(mActualEncoding, CharsetNames.CS_UTF8)) {
            return InputCharTypes.getUtf8CharTypes();
        }
        if (Objects.equals(mActualEncoding, CharsetNames.CS_ISO_LATIN1)) {
            return InputCharTypes.getLatin1CharTypes();
        }
        if (Objects.equals(mActualEncoding, CharsetNames.CS_US_ASCII)) {
            return InputCharTypes.getAsciiCharTypes();
        }
        throw new Error("Internal error, unknown encoding '" + mActualEncoding + "'");
    }

    /*
    /**********************************************************************
    /* Helper classes
    /**********************************************************************
     */

    /**
     * This is a simple container class that is used to encapsulate
     * per-factory encoding-dependant information like symbol tables.
     */
    final static class EncodingContext {
        ByteBasedPNameTable mUtf8Table;
        ByteBasedPNameTable mLatin1Table;
        ByteBasedPNameTable mAsciiTable;

        /**
         * If there is no encoding to worry about, we only need a single
         * symbol table.
         */
        CharBasedPNameTable mGeneralTable;

        EncodingContext() {
        }

        public synchronized ByteBasedPNameTable getUtf8Symbols() {
            if (mUtf8Table == null) {
                mUtf8Table = new ByteBasedPNameTable(64);
            }
            return new ByteBasedPNameTable(mUtf8Table);
        }

        public synchronized void updateUtf8Symbols(ByteBasedPNameTable sym) {
            mUtf8Table.mergeFromChild(sym);
        }

        public synchronized ByteBasedPNameTable getLatin1Symbols() {
            if (mLatin1Table == null) {
                mLatin1Table = new ByteBasedPNameTable(64);
            }
            return new ByteBasedPNameTable(mLatin1Table);
        }

        public synchronized void updateLatin1Symbols(ByteBasedPNameTable sym) {
            mLatin1Table.mergeFromChild(sym);
        }

        public synchronized ByteBasedPNameTable getAsciiSymbols() {
            if (mAsciiTable == null) {
                mAsciiTable = new ByteBasedPNameTable(64);
            }
            return new ByteBasedPNameTable(mAsciiTable);
        }

        public synchronized void updateAsciiSymbols(ByteBasedPNameTable sym) {
            mAsciiTable.mergeFromChild(sym);
        }

        public synchronized CharBasedPNameTable getSymbols() {
            if (mGeneralTable == null) {
                mGeneralTable = new CharBasedPNameTable(64);
            }
            return new CharBasedPNameTable(mGeneralTable);
        }

        public synchronized void updateSymbols(CharBasedPNameTable sym) {
            mGeneralTable.mergeFromChild(sym);
        }
    }
}
