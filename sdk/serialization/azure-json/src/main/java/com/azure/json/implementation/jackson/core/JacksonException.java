// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core;

/**
 * Base class for all Jackson-produced checked exceptions.
 *<p>
 * For Jackson 2.x this base type is not widely used (instead, its main subtype
 * {@link JsonProcessingException} is): it is provided more for forwards-compatibility
 * purposes as 3.x will base all other types directly on it and deprecate
 * {@link JsonProcessingException} (as well as chance its type to unchecked).
 *
 * @since 2.12
 */
public abstract class JacksonException extends java.io.IOException {
    private final static long serialVersionUID = 123; // eclipse complains otherwise

    protected JacksonException(String msg) {
        super(msg);
    }

    protected JacksonException(String msg, Throwable rootCause) {
        super(msg, rootCause);
        // 23-Sep-2020, tatu: before 2.12, had null checks for some reason...
        //   But I don't think that is actually required; Javadocs for
        //   `java.lang.Throwable` constructor claim {@code null} is fine.
        /*        if (rootCause != null) {
            initCause(rootCause);
        }*/
    }

    /*
    /**********************************************************************
    /* Extended API
    /**********************************************************************
     */

    /**
     * Accessor for location information related to position within input
     * or output (depending on operation), if available; if not available
     * may return {@code null}.
     *<p>
     * Accuracy of location information depends on backend (format) as well
     * as (in some cases) operation being performed.
     *
     * @return Location in input or output that triggered the problem reported, if
     *    available; {@code null} otherwise.
     */
    public abstract JsonLocation getLocation();
}
