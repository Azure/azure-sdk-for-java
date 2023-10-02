// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */
package com.typespec.json.implementation.jackson.core;

/**
 * Container for commonly used Base64 variants:
 *<ul>
 * <li> {@link #MIME}
 * <li> {@link #MIME_NO_LINEFEEDS}
 * <li> {@link #PEM}
 * <li> {@link #MODIFIED_FOR_URL}
 * </ul>
 * See entries for full description of differences.
 *<p>
 * Note that for default {@link Base64Variant} instances listed above, configuration
 * is such that if padding is written on output, it will also be required on
 * reading. This behavior may be changed by using methods:
 *<ul>
 * <li>{@link Base64Variant#withPaddingAllowed()}
 *  </li>
 * <li>{@link Base64Variant#withPaddingForbidden()}
 *  </li>
 * <li>{@link Base64Variant#withPaddingRequired()}
 *  </li>
 * <li>{@link Base64Variant#withWritePadding(boolean)}
 *  </li>
 *</ul>
 *
 * @author Tatu Saloranta
 */
public final class Base64Variants
{
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
     *<p>
     * Writes padding on output; requires padding when reading (may change later with a call to {@link Base64Variant#withWritePadding})
     */
    public final static Base64Variant MIME;
    static {
        MIME = new Base64Variant("MIME", STD_BASE64_ALPHABET, true, '=', 76);
    }

    /**
     * Slightly non-standard modification of {@link #MIME} which does not
     * use linefeeds (max line length set to infinite). Useful when linefeeds
     * wouldn't work well (possibly in attributes), or for minor space savings
     * (save 1 linefeed per 76 data chars, ie. ~1.4% savings).
     *<p>
     * Writes padding on output; requires padding when reading (may change later with a call to {@link Base64Variant#withWritePadding})
     */
    public final static Base64Variant MIME_NO_LINEFEEDS;
    static {
        MIME_NO_LINEFEEDS = new Base64Variant(MIME, "MIME-NO-LINEFEEDS", Integer.MAX_VALUE);
    }

    /**
     * This variant is the one that predates {@link #MIME}: it is otherwise
     * identical, except that it mandates shorter line length.
     *<p>
     * Writes padding on output; requires padding when reading (may change later with a call to {@link Base64Variant#withWritePadding})
     */
    public final static Base64Variant PEM = new Base64Variant(MIME, "PEM", true, '=', 64);

    /**
     * This non-standard variant is usually used when encoded data needs to be
     * passed via URLs (such as part of GET request). It differs from the
     * base {@link #MIME} variant in multiple ways.
     * First, no padding is used: this also means that it generally can not
     * be written in multiple separate but adjacent chunks (which would not
     * be the usual use case in any case). Also, no linefeeds are used (max
     * line length set to infinite). And finally, two characters (plus and
     * slash) that would need quoting in URLs are replaced with more
     * optimal alternatives (hyphen and underscore, respectively).
     *<p>
     * Does not write padding on output; does not accept padding when reading (may change later with a call to {@link Base64Variant#withWritePadding})
     */
    public final static Base64Variant MODIFIED_FOR_URL;
    static {
        StringBuilder sb = new StringBuilder(STD_BASE64_ALPHABET);
        // Replace plus with hyphen, slash with underscore (and no padding)
        sb.setCharAt(sb.indexOf("+"), '-');
        sb.setCharAt(sb.indexOf("/"), '_');
        // And finally, let's not split lines either, wouldn't work too well with URLs
        MODIFIED_FOR_URL = new Base64Variant("MODIFIED-FOR-URL", sb.toString(), false, Base64Variant.PADDING_CHAR_NONE, Integer.MAX_VALUE);
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

    /**
     * Lookup method for finding one of standard variants by name.
     * If name does not match any of standard variant names,
     * a {@link IllegalArgumentException} is thrown.
     *
     * @param name Name of base64 variant to return
     *
     * @return Standard base64 variant that matches given {@code name}
     *
     * @throws IllegalArgumentException if no standard variant with given name exists
     */
    public static Base64Variant valueOf(String name) throws IllegalArgumentException
    {
        if (MIME._name.equals(name)) {
            return MIME;
        }
        if (MIME_NO_LINEFEEDS._name.equals(name)) {
            return MIME_NO_LINEFEEDS;
        }
        if (PEM._name.equals(name)) {
            return PEM;
        }
        if (MODIFIED_FOR_URL._name.equals(name)) {
            return MODIFIED_FOR_URL;
        }
        if (name == null) {
            name = "<null>";
        } else {
            name = "'"+name+"'";
        }
        throw new IllegalArgumentException("No Base64Variant with name "+name);
    }
}
