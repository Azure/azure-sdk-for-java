// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.out;

import java.util.*;
import java.lang.ref.SoftReference;

import javax.xml.stream.XMLOutputFactory; // to get constants

import com.azure.xml.implementation.stax2.XMLOutputFactory2;
import com.azure.xml.implementation.stax2.XMLStreamProperties;

import com.azure.xml.implementation.aalto.impl.CommonConfig;
import com.azure.xml.implementation.aalto.util.BufferRecycler;

/**
 * This is the shared configuration object passed by the factory to writer.
 */
public final class WriterConfig extends CommonConfig {
    // // // Constants for defaults

    protected final static String DEFAULT_AUTOMATIC_NS_PREFIX = "ans";

    // Standard Stax flags:
    final static int F_NS_REPAIRING = 0x0001;

    // Standard Stax2 flags:
    final static int F_AUTO_CLOSE_OUTPUT = 0x0010;
    final static int F_NS_AWARE = 0x0020;
    final static int F_AUTO_EMPTY_ELEMS = 0x0040;

    // Non-flag (object) properties

    final static int PROP_AUTO_NS_PREFIX = -2;

    // No flags are set by default, yet...
    final static int DEFAULT_FLAGS = F_NS_AWARE;

    private final static HashMap<String, Integer> sProperties;
    static {
        sProperties = new HashMap<String, Integer>();
        sProperties.put(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Integer.valueOf(F_NS_REPAIRING));

        // Stax2:

        // not configurable, but are recognized
        sProperties.put(XMLStreamProperties.XSP_NAMESPACE_AWARE, Integer.valueOf(F_NS_AWARE));
        sProperties.put(XMLStreamProperties.XSP_PROBLEM_REPORTER, null);

        // and then writer-side properties, mostly unsupported but recognized

        sProperties.put(XMLOutputFactory2.P_AUTO_CLOSE_OUTPUT, Integer.valueOf(F_AUTO_CLOSE_OUTPUT));

        sProperties.put(XMLOutputFactory2.P_AUTOMATIC_EMPTY_ELEMENTS, Integer.valueOf(F_AUTO_EMPTY_ELEMS));
        sProperties.put(XMLOutputFactory2.P_AUTOMATIC_NS_PREFIX, Integer.valueOf(PROP_AUTO_NS_PREFIX));
        sProperties.put(XMLOutputFactory2.P_TEXT_ESCAPER, null);
        sProperties.put(XMLOutputFactory2.P_ATTR_VALUE_ESCAPER, null);
    }

    /*
    /**********************************************************************
    /* Configurable object properties:
    /**********************************************************************
     */

    private String _propAutoNsPrefix;

    /*
    /**********************************************************************
    /* Output/stream state:
    /**********************************************************************
     */

    /**
     * A single encoding context instance is shared between all WriterConfig
     * instances created for readers by an output factory. It is used
     * for sharing symbol tables.
     */
    private final EncodingContext _encodingContext;

    /**
     * Encoding passed in either during construction, or via xml
     * declaration write.
     */
    private String _encoding;

    /*
    /**********************************************************************
    /* Buffer recycling:
    /**********************************************************************
     */

    /**
     * This <code>ThreadLocal</code> contains a {@link SoftRerefence}
     * to a {@link BufferRecycler} used to provide a low-cost
     * buffer recycling between Reader instances.
     */
    final static ThreadLocal<SoftReference<BufferRecycler>> mRecyclerRef
        = new ThreadLocal<SoftReference<BufferRecycler>>();

    /**
     * This is the actually container of the recyclable buffers. It
     * is obtained via ThreadLocal/SoftReference combination, if one
     * exists, when Config instance is created. If one does not
     * exist, it will created first time a buffer is returned.
     */
    BufferRecycler _currRecycler = null;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    private WriterConfig(String encoding, int flags, int flagMods, EncodingContext encCtxt, String autoNsPrefix) {
        super(flags, flagMods);
        _encoding = encoding;
        _encodingContext = encCtxt;

        /* Ok, let's then see if we can find a buffer recycler. Since they
         * are lazily constructed, and since GC may just flush them out
         * on its whims, it's possible we might not find one. That's ok;
         * we can reconstruct one if and when we are to return one or more
         * buffers.
         */
        SoftReference<BufferRecycler> ref = mRecyclerRef.get();
        if (ref != null) {
            _currRecycler = ref.get();
        }
        _flags = flags;
        _flagMods = flagMods;
        _propAutoNsPrefix = autoNsPrefix;
    }

    public WriterConfig() {
        this(null, DEFAULT_FLAGS, 0, new EncodingContext(), DEFAULT_AUTOMATIC_NS_PREFIX);
    }

    public void setActualEncodingIfNotSet(String enc) {
        if (_encoding == null || _encoding.length() == 0) {
            _encoding = enc;
        }
    }

    public void doAutoCloseOutput(boolean state) {
        setFlag(F_AUTO_CLOSE_OUTPUT, state);
    }

    public void enableXml11() {
        // !!! TBI
    }

    /*
    /**********************************************************************
    /* Common accessors from CommonConfig
    /**********************************************************************
     */

    public WriterConfig createNonShared() {
        return new WriterConfig(_encoding, _flags, _flagMods, _encodingContext, _propAutoNsPrefix);
    }

    @Override
    public boolean isXml11() {
        return false;
    }

    @Override
    public String getExternalEncoding() {
        /* !!! 01-Jan-2007, tatus: Can we distinguish this from the
         *   actual encoding? Should we be able to?
         */
        return getActualEncoding();
    }

    @Override
    public String getActualEncoding() {
        return _encoding;
    }

    public String getPreferredEncoding() {
        /* If we did have 2 separate encoding decls, we'd prefer actual
         * over external. For now, doesn't matter
         */
        return _encoding;
    }

    /*
    /**********************************************************************
    /* Accessors, configurable properties
    /**********************************************************************
     */

    @Override
    public Object getProperty(String name, boolean isMandatory) {
        Integer I = sProperties.get(name);
        if (I == null) {
            // Might still have it though
            if (sProperties.containsKey(name)) {
                return null;
            }
            return super.getProperty(name, isMandatory);
        }
        int f = I.intValue();

        if (f >= 0) {
            return hasFlag(f);
        }
        switch (f) {
            case PROP_AUTO_NS_PREFIX:
                return _propAutoNsPrefix;
        }

        // Need to handle non numerics separately?
        return null;
    }

    @Override
    public boolean setProperty(String name, Object value) {
        Integer I = sProperties.get(name);
        if (I == null) {
            // Might still have it though
            if (sProperties.containsKey(name)) {
                return false;
            }
            return super.setProperty(name, value);
        }
        int f = I.intValue();

        if (f >= 0) { // boolean values
            boolean state = ((Boolean) value).booleanValue();
            // Some properties not supported:
            if (f == F_NS_AWARE) {
                if (!state) {
                    //throw new IllegalArgumentException("Can not disable namespace-support for stream writers");
                    return false; // not supported
                }
            }
            setFlag(f, state);
            return true;
        }
        // object values ones
        switch (f) {
            case PROP_AUTO_NS_PREFIX:
                _propAutoNsPrefix = value.toString();
                return true;
        }

        return false;
    }

    @Override
    public boolean isPropertySupported(String propName) {
        return sProperties.containsKey(propName) || super.isPropertySupported(propName);
    }

    /*
    /**********************************************************************
    /* Accessors
    /**********************************************************************
     */

    // // // Configuration, Stax std props:

    public boolean willRepairNamespaces() {
        return hasFlag(F_NS_REPAIRING);
    }

    public boolean isNamespaceAware() {
        // !!! TBI
        return hasFlag(F_NS_AWARE);
    }

    // // // Stax2 standard properties

    public boolean willAutoCloseOutput() {
        return hasFlag(F_AUTO_CLOSE_OUTPUT);
    }

    /**
     * @return Prefix to use as the base for automatically generated
     *   namespace prefixes ("namespace prefix prefix", so to speak).
     *   Defaults to "axns".
     */
    public String getAutomaticNsPrefix() {
        return _propAutoNsPrefix;
    }

    /*
    /**********************************************************************
    /* Stax2 additions
    /**********************************************************************
     */

    public void configureForXmlConformance() {
        // !!! TBI
        /*
        doValidateAttributes(true);
        doValidateContent(true);
        doValidateStructure(true);
        doValidateNames(true);
        */
    }

    public void configureForRobustness() {
        // !!! TBI
        /*
        doValidateAttributes(true);
        doValidateStructure(true);
        doValidateNames(true);
        
        doValidateContent(true);
        doFixContent(true);
        */
    }

    /**
     * For Woodstox, setting this profile disables most checks for validity;
     * specifically anything that can have measurable performance impact.
     * 
     */
    public void configureForSpeed() {
        // !!! TBI
        /*
        doValidateAttributes(false);
        doValidateContent(false);
        doValidateNames(false);
        
        // Structural validation is cheap: can be left enabled (if already so)
        //doValidateStructure(false);
        */
    }

    /*
    /**********************************************************************
    /* Impl specific additions, validation
    /**********************************************************************
     */

    public boolean willCheckStructure() {
        // !!! TBI
        return true;
    }

    public boolean willCheckContent() {
        // !!! TBI
        return true;
    }

    public boolean willCheckNames() {
        // !!! TBI
        return false;
    }

    public boolean willCheckAttributes() {
        // !!! TBI
        return false;
    }

    public boolean willFixContent() {
        // !!! TBI
        return true;
    }

    public boolean willEscapeCR() {
        // !!! TBI
        return true;
    }

    /*
    /**********************************************************************
    /* Buffer recycling:
    /**********************************************************************
     */

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
        mRecyclerRef.set(new SoftReference<BufferRecycler>(recycler));
        return recycler;
    }

    /*
    /**********************************************************************
    /* Symbol table reusing, character types
    /**********************************************************************
     */

    public WNameTable getUtf8Symbols(WNameFactory f) {
        return _encodingContext.getUtf8Symbols(f);
    }

    public WNameTable getLatin1Symbols(WNameFactory f) {
        return _encodingContext.getLatin1Symbols(f);
    }

    public WNameTable getAsciiSymbols(WNameFactory f) {
        return _encodingContext.getAsciiSymbols(f);
    }

    public WNameTable getCharSymbols(WNameFactory f) {
        return _encodingContext.getCharSymbols(f);
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
        WNameTable mUtf8Table;
        WNameTable mLatin1Table;
        WNameTable mAsciiTable;

        WNameTable mCharTable;

        EncodingContext() {
        }

        public synchronized WNameTable getUtf8Symbols(WNameFactory f) {
            if (mUtf8Table == null) {
                mUtf8Table = new WNameTable(64);
            }
            return mUtf8Table.createChild(f);
        }

        public synchronized WNameTable getLatin1Symbols(WNameFactory f) {
            if (mLatin1Table == null) {
                mLatin1Table = new WNameTable(64);
            }
            return mLatin1Table.createChild(f);
        }

        public synchronized WNameTable getAsciiSymbols(WNameFactory f) {
            if (mAsciiTable == null) {
                mAsciiTable = new WNameTable(64);
            }
            return mAsciiTable.createChild(f);
        }

        public synchronized WNameTable getCharSymbols(WNameFactory f) {
            if (mCharTable == null) {
                mCharTable = new WNameTable(64);
            }
            return mCharTable.createChild(f);
        }
    }
}
