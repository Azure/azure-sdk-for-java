// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package io.clientcore.core.serialization.xml.implementation.aalto.in;

import io.clientcore.core.serialization.xml.implementation.aalto.impl.CommonConfig;
import io.clientcore.core.serialization.xml.implementation.aalto.stax.InputFactoryImpl;
import io.clientcore.core.serialization.xml.implementation.aalto.util.UriCanonicalizer;
import io.clientcore.core.serialization.xml.implementation.aalto.util.XmlConsts;

import javax.xml.stream.XMLInputFactory;
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
    private final static int F_INTERN_NAMES = 0x0200;
    private final static int F_INTERN_NS_URIS = 0x0400;
    private final static int F_REPORT_CDATA = 0x0800;
    private final static int F_PRESERVE_LOCATION = 0x1000;

    /**
     * These are the default settings for XMLInputFactory.
     */
    private final static int DEFAULT_FLAGS = F_NS_AWARE | F_EXPAND_ENTITIES
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
        sProperties.put(InputFactoryImpl.P_INTERN_NAMES, F_INTERN_NAMES);
        sProperties.put(InputFactoryImpl.P_INTERN_NS_URIS, F_INTERN_NS_URIS);

        // (ones with fixed defaults)

        sProperties.put(InputFactoryImpl.P_REPORT_CDATA, F_REPORT_CDATA);
        sProperties.put(InputFactoryImpl.P_PRESERVE_LOCATION, Boolean.TRUE);
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

    private String mXmlDeclVersion = null;
    private String mXmlDeclEncoding = null;
    private int mXmlDeclStandalone = STANDALONE_UNKNOWN;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    private ReaderConfig(EncodingContext encCtxt, int flags, int flagMods, UriCanonicalizer canonicalizer) {
        super(flags, flagMods);
        mEncCtxt = encCtxt;
        _flags = flags;
        _flagMods = flagMods;
        mCanonicalizer = canonicalizer;
    }

    public ReaderConfig() {
        this(new EncodingContext(), DEFAULT_FLAGS, 0, new UriCanonicalizer());
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
    /* Common accessors from CommonConfig
    /**********************************************************************
     */

    public ReaderConfig createNonShared() {
        return new ReaderConfig(mEncCtxt, _flags, _flagMods, mCanonicalizer);
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

    // // // Stax standard properties

    public boolean willExpandEntities() {
        return hasFlag(F_EXPAND_ENTITIES);
    }

    public boolean willCoalesceText() {
        return hasFlag(F_COALESCING);
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
    /* Symbol table reusing, character types
    /**********************************************************************
     */

    public CharBasedPNameTable getCBSymbols() {
        return mEncCtxt.getSymbols();
    }

    public void updateCBSymbols(CharBasedPNameTable sym) {
        mEncCtxt.updateSymbols(sym);
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

        /**
         * If there is no encoding to worry about, we only need a single
         * symbol table.
         */
        CharBasedPNameTable mGeneralTable;

        EncodingContext() {
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
