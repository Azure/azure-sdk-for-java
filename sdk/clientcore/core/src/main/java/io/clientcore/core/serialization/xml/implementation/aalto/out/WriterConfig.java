// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package io.clientcore.core.serialization.xml.implementation.aalto.out;

import io.clientcore.core.serialization.xml.implementation.aalto.impl.CommonConfig;
import io.clientcore.core.serialization.xml.implementation.aalto.stax.OutputFactoryImpl;
import io.clientcore.core.serialization.xml.implementation.aalto.util.XmlConsts;

import javax.xml.stream.XMLOutputFactory;
import java.util.HashMap;

/**
 * This is the shared configuration object passed by the factory to writer.
 */
public final class WriterConfig extends CommonConfig {
    // Standard Stax flags:
    final static int F_NS_REPAIRING = 0x0001;

    // Standard Stax2 flags:
    final static int F_NS_AWARE = 0x0020;
    final static int F_AUTO_EMPTY_ELEMS = 0x0040;

    // No flags are set by default, yet...
    final static int DEFAULT_FLAGS = F_NS_AWARE;

    private final static HashMap<String, Integer> sProperties;
    static {
        sProperties = new HashMap<>();
        sProperties.put(XMLOutputFactory.IS_REPAIRING_NAMESPACES, F_NS_REPAIRING);

        // Stax2:

        // not configurable, but are recognized
        sProperties.put(XmlConsts.XSP_NAMESPACE_AWARE, F_NS_AWARE);
        sProperties.put(XmlConsts.XSP_PROBLEM_REPORTER, null);

        // and then writer-side properties, mostly unsupported but recognized

        sProperties.put(OutputFactoryImpl.P_AUTOMATIC_EMPTY_ELEMENTS, F_AUTO_EMPTY_ELEMS);
    }

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

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    private WriterConfig(int flags, int flagMods, EncodingContext encCtxt) {
        super(flags, flagMods);
        _encodingContext = encCtxt;
        _flags = flags;
        _flagMods = flagMods;
    }

    public WriterConfig() {
        this(DEFAULT_FLAGS, 0, new EncodingContext());
    }

    /*
    /**********************************************************************
    /* Common accessors from CommonConfig
    /**********************************************************************
     */

    public WriterConfig createNonShared() {
        return new WriterConfig(_flags, _flagMods, _encodingContext);
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
        int f = I;

        if (f >= 0) {
            return hasFlag(f);
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
        int f = I;

        if (f >= 0) { // boolean values
            boolean state = (Boolean) value;
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

        return false;
    }

    @Override
    public boolean isPropertySupported(String propName) {
        return sProperties.containsKey(propName) || super.isPropertySupported(propName);
    }

    /*
    /**********************************************************************
    /* Symbol table reusing, character types
    /**********************************************************************
     */

    public WNameTable getCharSymbols(CharXmlWriter f) {
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

        WNameTable mCharTable;

        EncodingContext() {
        }

        public synchronized WNameTable getCharSymbols(CharXmlWriter f) {
            if (mCharTable == null) {
                mCharTable = new WNameTable(64);
            }
            return mCharTable.createChild(f);
        }
    }
}
