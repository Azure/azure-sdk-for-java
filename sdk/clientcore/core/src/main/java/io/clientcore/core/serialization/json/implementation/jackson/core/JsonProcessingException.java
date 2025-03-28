// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/*
 * Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */

package io.clientcore.core.serialization.json.implementation.jackson.core;

import java.io.IOException;

/**
 * Intermediate base class for all problems encountered when
 * processing (parsing, generating) JSON content
 * that are not pure I/O problems.
 * Regular {@link IOException}s will be passed through as is.
 * Sub-class of {@link IOException} for convenience.
 */
public class JsonProcessingException extends IOException {
    private final static long serialVersionUID = 123; // eclipse complains otherwise

    protected JsonLocation _location;

    protected JsonProcessingException(String msg, JsonLocation loc, Throwable rootCause) {
        super(msg, rootCause);
        _location = loc;
    }

    protected JsonProcessingException(String msg, JsonLocation loc) {
        this(msg, loc, null);
    }

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
    public JsonLocation getLocation() {
        return _location;
    }

    /**
     * Method that allows accessing underlying processor that triggered
     * this exception; typically either {@link JsonParser} or {@link JsonGenerator}
     * for exceptions that originate from streaming API.
     * Note that it is possible that `null` may be returned if code throwing
     * exception either has no access to processor; or has not been retrofitted
     * to set it; this means that caller needs to take care to check for nulls.
     * Subtypes override this method with co-variant return type, for more
     * type-safe access.
     *
     * @return Originating processor, if available; null if not.
     *
     * @since 2.7
     */
    public Object getProcessor() {
        return null;
    }

    /*
     * /**********************************************************************
     * /* Methods for sub-classes to use, override
     * /**********************************************************************
     */

    /**
     * Accessor that sub-classes can override to append additional
     * information right after the main message, but before
     * source location information.
     *
     * @return Message suffix assigned, if any; {@code null} if none
     */
    protected String getMessageSuffix() {
        return null;
    }

    /*
     * /**********************************************************************
     * /* Overrides of standard methods
     * /**********************************************************************
     */

    /**
     * Default implementation overridden so that we can add location information
     *
     * @return Original {@code message} preceded by optional prefix and followed by
     *   location information, message and location information separated by a linefeed
     */
    @Override
    public String getMessage() {
        String msg = super.getMessage();
        if (msg == null) {
            msg = "N/A";
        }
        JsonLocation loc = getLocation();
        String suffix = getMessageSuffix();
        // mild optimization, if nothing extra is needed:
        if (loc != null || suffix != null) {
            StringBuilder sb = new StringBuilder(100);
            sb.append(msg);
            if (suffix != null) {
                sb.append(suffix);
            }
            if (loc != null) {
                sb.append('\n');
                sb.append(" at ");
                sb.append(loc);
            }
            msg = sb.toString();
        }
        return msg;
    }

    @Override
    public String toString() {
        return getClass().getName() + ": " + getMessage();
    }
}
