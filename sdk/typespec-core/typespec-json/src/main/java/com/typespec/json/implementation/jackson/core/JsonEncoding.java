// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */

package com.typespec.json.implementation.jackson.core;

/**
 * Enumeration that defines legal encodings that can be used
 * for JSON content, based on list of allowed encodings from
 * <a href="http://www.ietf.org/rfc/rfc4627.txt">JSON specification</a>.
 *<p>
 * Note: if application want to explicitly disregard Encoding
 * limitations (to read in JSON encoded using an encoding not
 * listed as allowed), they can use {@link java.io.Reader} /
 * {@link java.io.Writer} instances as input
 */
public enum JsonEncoding {
    UTF8("UTF-8", false, 8), // N/A for big-endian, really
        UTF16_BE("UTF-16BE", true, 16),
        UTF16_LE("UTF-16LE", false, 16),
        UTF32_BE("UTF-32BE", true, 32),
        UTF32_LE("UTF-32LE", false, 32)
        ;
    
    private final String _javaName;

    private final boolean _bigEndian;

    private final int _bits;
    
    JsonEncoding(String javaName, boolean bigEndian, int bits)
    {
        _javaName = javaName;
        _bigEndian = bigEndian;
        _bits = bits;
    }

    /**
     * Method for accessing encoding name that JDK will support.
     *
     * @return Matching encoding name that JDK will support.
     */
    public String getJavaName() { return _javaName; }

    /**
     * Whether encoding is big-endian (if encoding supports such
     * notion). If no such distinction is made (as is the case for
     * {@link #UTF8}), return value is undefined.
     *
     * @return True for big-endian encodings; false for little-endian
     *   (or if not applicable)
     */
    public boolean isBigEndian() { return _bigEndian; }

    public int bits() { return _bits; }
}
