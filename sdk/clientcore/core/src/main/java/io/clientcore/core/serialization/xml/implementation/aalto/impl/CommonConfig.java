// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package io.clientcore.core.serialization.xml.implementation.aalto.impl;

import java.util.HashMap;

/**
 * Base class for reader and writer-side configuration/context objects
 */
public abstract class CommonConfig {
    /*
    /**********************************************************************
    /* Internal constants
    /**********************************************************************
     */

    final static int PROP_IMPL_NAME = 1;

    /**
     * Map to use for converting from String property ids to enumeration
     * (ints). Used for faster dispatching.
     */
    final static HashMap<String, Integer> sStdProperties = new HashMap<>(16);
    static {
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
