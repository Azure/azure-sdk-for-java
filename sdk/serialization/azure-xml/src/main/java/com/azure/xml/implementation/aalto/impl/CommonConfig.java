// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.impl;

import java.util.*;

import com.azure.xml.implementation.stax2.XMLStreamProperties;

/**
 * Base class for reader and writer-side configuration/context objects
 */
public abstract class CommonConfig {
    /*
    /**********************************************************************
    /* Implementation info
    /**********************************************************************
     */

    protected final static String IMPL_NAME = "aalto";

    /* !!! TBI: get from props file or so? Or build as part of Ant
     *    build process?
     */
    /**
     * This is "major.minor" version used for purposes of determining
     * the feature set. Patch level is not included, since those should
     * not affect API or feature set. Using applications should be
     * prepared to take additional levels, however, just not depend
     * on those being available.
     */
    protected final static String IMPL_VERSION = "0.9";

    /*
    /**********************************************************************
    /* Internal constants
    /**********************************************************************
     */

    final static int PROP_IMPL_NAME = 1;
    final static int PROP_IMPL_VERSION = 2;

    final static int PROP_SUPPORTS_XML11 = 3;
    final static int PROP_SUPPORTS_XMLID = 4;

    /**
     * Map to use for converting from String property ids to enumeration
     * (ints). Used for faster dispatching.
     */
    final static HashMap<String, Integer> sStdProperties = new HashMap<>(16);
    static {
        // Basic information about the implementation:
        sStdProperties.put(XMLStreamProperties.XSP_IMPLEMENTATION_NAME, PROP_IMPL_NAME);
        sStdProperties.put(XMLStreamProperties.XSP_IMPLEMENTATION_VERSION, PROP_IMPL_VERSION);

        // XML version support:
        sStdProperties.put(XMLStreamProperties.XSP_SUPPORTS_XML11, PROP_SUPPORTS_XML11);

        // Xml:id support:
        sStdProperties.put(XMLStreamProperties.XSP_SUPPORT_XMLID, PROP_SUPPORTS_XMLID);

        /* 23-Apr-2008, tatus: Additional interoperability property,
         *    one that Sun implementation uses. Can map tor Stax2
         *    property quite easily.
         */
        sStdProperties.put("http://java.sun.com/xml/stream/properties/implementation-name", PROP_IMPL_NAME);
    }

    /**
     * Bitset for all on/off values for this configuration object.
     */
    protected int _flags;

    /**
     * Bitset that indicates all explicit changes to on/off values; clear
     * bits indicate settings that are unmodified default values.
     */
    protected int _flagMods;

    protected CommonConfig(int flags, int flagMods) {
        _flags = flags;
        _flagMods = flagMods;
    }

    /*
    /**********************************************************************
    /* Public API, generic StAX config methods
    /**********************************************************************
     */

    /**
     * @param isMandatory If true, unrecognized property should
     *  result in {@link IllegalArgumentException}
     */
    public Object getProperty(String propName, boolean isMandatory) {
        Integer I = sStdProperties.get(propName);
        if (I != null) {
            switch (I) {
                case PROP_IMPL_NAME:
                    return IMPL_NAME;

                case PROP_IMPL_VERSION:
                    return IMPL_VERSION;

                case PROP_SUPPORTS_XML11: // nope, not really
                    return Boolean.FALSE;

                case PROP_SUPPORTS_XMLID:
                    return Boolean.FALSE;
            }
        }
        if (isMandatory) {
            throw new IllegalArgumentException("Unrecognized property '" + propName + "'");
        }
        return null;
    }

    public boolean isPropertySupported(String propName) {
        return sStdProperties.containsKey(propName);
    }

    /**
     * @return True, if the specified property was <b>succesfully</b>
     *    set to specified value; false if its value was not changed
     */
    public boolean setProperty(String propName, Object value) {
        Integer I = sStdProperties.get(propName);
        if (I != null) { // can't set any of std props
            return false;
        }
        throw new IllegalArgumentException("Unrecognized property '" + propName + "'");
    }

    /*
    /**********************************************************************
    /* Public API beyond Stax2
    /**********************************************************************
     */

    /**
     * This method returns name of encoding that has been passed
     * explicitly to the reader or writer, from outside. An example
     * is that HTTP server may pass encoding as declared in HTTP
     * headers. This should either be null (if none passed), or the
     * same as actual encoding (which is determined from physical
     * stream contents [for readers], or from encoder
     * properties / configuration [for writers]
     *
     * @return Encoding that has been passed externally by the application
     */
    public abstract String getExternalEncoding();

    /**
     * @return Actual encoding in use, as determined by the processor.
     */
    public abstract String getActualEncoding();

    /**
     * @return True, if the processing will be done according to Xml 1.1
     *  rules; false if according to xml 1.0
     */
    public abstract boolean isXml11();

    /*
    /**********************************************************************
    /* Helper methods for sub-classes
    /**********************************************************************
     */

    protected final boolean hasFlag(int flagMask) {
        return (_flags & flagMask) != 0;
    }

    protected final boolean hasExplicitFlag(int flagMask) {
        return hasFlag(flagMask) && hasFlagBeenModified(flagMask);
    }

    protected final void setFlag(int flagMask, boolean state) {
        if (state) {
            _flags |= flagMask;
        } else {
            _flags &= ~flagMask;
        }
        _flagMods |= flagMask;
    }

    /**
     * Method for checking whether specific configuration flag
     * has been explicitly modified (set, clear; regardless of
     * whether state actually changed), or is it the default
     * value.
     *
     * @return False, if flag in question has its default value,
     *   true if a call has been made that could have changed it.
     */
    protected final boolean hasFlagBeenModified(int flagMask) {
        return (_flagMods & flagMask) != 0;
    }
}
