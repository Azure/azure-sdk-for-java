// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core;

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
public abstract class JacksonException extends java.io.IOException
{
    private final static long serialVersionUID = 123; // eclipse complains otherwise

    protected JacksonException(String msg) {
        super(msg);
    }

    protected JacksonException(Throwable t) {
        super(t);
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

    /**
     * Method that allows accessing the original "message" argument,
     * without additional decorations (like location information)
     * that overridden {@link #getMessage} adds.
     *
     * @return Original, unmodified {@code message} argument used to construct
     *    this exception instance
     */
    public abstract String getOriginalMessage();

    /**
     * Method that allows accessing underlying processor that triggered
     * this exception; typically either {@link JsonParser} or {@link JsonGenerator}
     * for exceptions that originate from streaming API, but may be other types
     * when thrown by databinding.
     *<p>
     * Note that it is possible that {@code null} may be returned if code throwing
     * exception either has no access to the processor; or has not been retrofitted
     * to set it; this means that caller needs to take care to check for nulls.
     * Subtypes override this method with co-variant return type, for more
     * type-safe access.
     * 
     * @return Originating processor, if available; {@code null} if not.
     */
    public abstract Object getProcessor();
}
