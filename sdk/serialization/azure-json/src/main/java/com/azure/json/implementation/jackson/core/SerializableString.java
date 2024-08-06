// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */

package com.azure.json.implementation.jackson.core;

/**
 * Interface that defines how Jackson package can interact with efficient
 * pre-serialized or lazily-serialized and reused String representations.
 * Typically implementations store possible serialized version(s) so that
 * serialization of String can be done more efficiently, especially when
 * used multiple times.
 *<p>
 * Note that "quoted" in methods means quoting of 'special' characters using
 * JSON backlash notation (and not use of actual double quotes).
 *
 * @see com.fasterxml.jackson.core.io.SerializedString
 */
public interface SerializableString {
    /**
     * Returns unquoted String that this object represents (and offers
     * serialized forms for)
     *
     * @return Unquoted String
     */
    String getValue();

    /*
    /**********************************************************
    /* Accessors for byte sequences
    /**********************************************************
     */

    /**
     * Returns UTF-8 encoded version of unquoted String.
     * Functionally equivalent to (but more efficient than):
     *<pre>
     * getValue().getBytes("UTF-8");
     *</pre>
     *
     * @return UTF-8 encoded version of String, without any escaping
     */
    byte[] asUnquotedUTF8();

}
