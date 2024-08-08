// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */
package com.azure.json.implementation.jackson.core;

/**
 * Container for commonly used Base64 variants:
 *<ul>
 * <li> {@link #MIME}
 * <li> {@link #MIME_NO_LINEFEEDS}
 * </ul>
 * See entries for full description of differences.
 *<p>
 * Note that for default {@link Base64Variant} instances listed above, configuration
 * is such that if padding is written on output, it will also be required on
 * reading. This behavior may be changed by using methods:
 *
 * @author Tatu Saloranta
 */
public final class Base64Variants {
    final static String STD_BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    /**
     * This variant is what most people would think of "the standard"
     * Base64 encoding.
     *<p>
     * See <a href="http://en.wikipedia.org/wiki/Base64">wikipedia Base64 entry</a> for details.
     *<p>
     * Note that although this can be thought of as the standard variant,
     * it is <b>not</b> the default for Jackson: no-linefeeds alternative
     * is instead used because of JSON requirement of escaping all linefeeds.
     */
    public final static Base64Variant MIME;
    static {
        MIME = new Base64Variant("MIME", 76);
    }

    /**
     * Slightly non-standard modification of {@link #MIME} which does not
     * use linefeeds (max line length set to infinite). Useful when linefeeds
     * wouldn't work well (possibly in attributes), or for minor space savings
     * (save 1 linefeed per 76 data chars, ie. ~1.4% savings).
     */
    public final static Base64Variant MIME_NO_LINEFEEDS;
    static {
        MIME_NO_LINEFEEDS = new Base64Variant(MIME, "MIME-NO-LINEFEEDS", Integer.MAX_VALUE);
    }

    /**
     * Method used to get the default variant -- {@link #MIME_NO_LINEFEEDS} -- for cases
     * where caller does not explicitly specify the variant.
     * We will prefer no-linefeed version because linefeeds in JSON values
     * must be escaped, making linefeed-containing variants sub-optimal.
     *
     * @return Default variant ({@code MIME_NO_LINEFEEDS})
     */
    public static Base64Variant getDefaultVariant() {
        return MIME_NO_LINEFEEDS;
    }

}
