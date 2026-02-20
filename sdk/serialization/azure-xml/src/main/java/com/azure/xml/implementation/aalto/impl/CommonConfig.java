// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.impl;

import java.util.HashMap;

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

    // // // Information about implementation

    /**
     * This read-only property returns name of the implementation. It
     * can be used to determine implementation-specific feature sets,
     * in case other methods (calling <code>isPropertySupported</code>)
     * does not work adequately.
     */
    protected static final String XSP_IMPLEMENTATION_NAME = "com.azure.xml.implementation.stax2.implName";

    /**
     * This read-only property returns the version of the implementation,
     * and is to be used with implementation name
     * ({@link #XSP_IMPLEMENTATION_NAME}) property.
     */
    protected static final String XSP_IMPLEMENTATION_VERSION = "com.azure.xml.implementation.stax2.implVersion";

    /**
     * This read-only property indicates whether the implementation
     * supports xml 1.1 content; Boolean.TRUE indicates it does,
     * Boolean.FALSE that it does not.
     */
    protected static final String XSP_SUPPORTS_XML11 = "com.azure.xml.implementation.stax2.supportsXml11";

    // // // Re-declared properties from XMLInputFactory

    /**
     * Property that can be set to indicate that namespace information is
     * to be handled in conformance to the xml namespaces specifiation; or
     * false to indicate no namespace handling should be done.
     */
    protected static final String XSP_NAMESPACE_AWARE = "javax.xml.stream.isNamespaceAware";

    /**
     * Property that can be set to specify a problem handler which will get
     * notified of non-fatal problem (validation errors in non-validating mode,
     * warnings). Its value has to be of type
     * {@link javax.xml.stream.XMLReporter}
     */
    protected static final String XSP_PROBLEM_REPORTER = "javax.xml.stream.reporter";

    // // // Generic XML feature support:

    /**
     * Read/write property that can be set to change the level of xml:id
     * specification support, if the implementation implements xml:id
     * specification.
     *<p>
     * Default value is implementation-specific, but recommended default
     * value is <code>XSP_V_XMLID_TYPING</code> for implementations
     * that do support Xml:id specification: those that do not, have to
     * default to <code>XSP_V_XMLID_NONE</code>.
     * For Xml:id-enabled implementations, typing support is the most
     * logical default, since it
     * provides the intuitive behavior of xml:id functionality, as well
     * as reasonable performance (very little overhead in non-validating
     * mode; usual id checking overhead for validating mode).
     */
    protected static final String XSP_SUPPORT_XMLID = "com.azure.xml.implementation.stax2.supportXmlId";

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
        sStdProperties.put(XSP_IMPLEMENTATION_NAME, PROP_IMPL_NAME);
        sStdProperties.put(XSP_IMPLEMENTATION_VERSION, PROP_IMPL_VERSION);

        // XML version support:
        sStdProperties.put(XSP_SUPPORTS_XML11, PROP_SUPPORTS_XML11);

        // Xml:id support:
        sStdProperties.put(XSP_SUPPORT_XMLID, PROP_SUPPORTS_XMLID);

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
     * @return Actual encoding in use, as determined by the processor.
     */
    public abstract String getActualEncoding();

    /*
    /**********************************************************************
    /* Helper methods for sub-classes
    /**********************************************************************
     */

    protected final boolean hasFlag(int flagMask) {
        return (_flags & flagMask) != 0;
    }

    protected final void setFlag(int flagMask, boolean state) {
        if (state) {
            _flags |= flagMask;
        } else {
            _flags &= ~flagMask;
        }
        _flagMods |= flagMask;
    }

}
