// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/*
 * Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */
package com.azure.json.implementation.jackson.core;

/**
 * Container for commonly used Base64 variants:
 *<ul>
 * <li> {@link #MIME_NO_LINEFEEDS}
 * </ul>
 * See entries for full description of differences.
 *<p>
 * Note that for default {@link Base64Variant} instances listed above, configuration
 * is such that if padding is written on output, it will also be required on
 * reading.
 *
 * @author Tatu Saloranta
 */
public final class Base64Variants {
    final static String STD_BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    /**
     * Slightly non-standard modification of {@code MIME} which does not
     * use linefeeds (max line length set to infinite). Useful when linefeeds
     * wouldn't work well (possibly in attributes), or for minor space savings
     * (save 1 linefeed per 76 data chars, ie. ~1.4% savings).
     *<p>
     * Writes padding on output; requires padding when reading
     */
    public final static Base64Variant MIME_NO_LINEFEEDS = new Base64Variant();

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
