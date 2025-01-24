// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.util;

import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Simple utility class that normalizes given character input character
 * set names into canonical (within context of this processor) names
 */
public final class CharsetNames implements XmlConsts {
    /*
    /**********************************************************
    /* Canonical names used internally
    /**********************************************************
     */

    // // // Unicode variants:

    public final static String CS_US_ASCII = "US-ASCII";
    public final static String CS_UTF8 = "UTF-8";

    /**
     * This constants is intentionally vague, so that some other information
     * will be needed to determine the endianness.
     */
    public final static String CS_UTF16 = "UTF-16";

    public final static String CS_UTF16BE = "UTF-16BE";
    public final static String CS_UTF16LE = "UTF-16LE";
    public final static String CS_UTF32 = "UTF-32";
    public final static String CS_UTF32BE = "UTF-32BE";
    public final static String CS_UTF32LE = "UTF-32LE";

    // // // 8-bit ISO encodings:

    public final static String CS_ISO_LATIN1 = "ISO-8859-1";

    // // // Japanese non-unicode encodings:

    public final static String CS_SHIFT_JIS = "Shift_JIS";

    // // // Other oddities:

    public final static String CS_EBCDIC = "EBCDIC";

    /*
    /**********************************************************
    /* Utility methods
    /**********************************************************
     */

    public static String normalize(String csName) {
        if (csName == null || csName.length() < 3) {
            return csName;
        }

        /* Canonical charset names here are from IANA recommendation:
         *   http://www.iana.org/assignments/character-sets
         * but comparison is done loosely (case-insensitive, ignoring
         * spacing, underscore vs. hyphen etc) to try to make detection
         * as extensive as possible.
         */

        /* But first bit of pre-filtering: it seems like 'cs' prefix
         * is applicable to pretty much all actual encodings (as per
         * IANA recommendations; csASCII, csUcs4 etc). So, let's just
         * strip out the prefix if so
         */
        boolean gotCsPrefix = false;
        char c = csName.charAt(0);
        if (c == 'c' || c == 'C') {
            char d = csName.charAt(1);
            if (d == 's' || d == 'S') {
                csName = csName.substring(2);
                c = csName.charAt(0);
                gotCsPrefix = true;
            }
        }

        switch (c) {
            case 'a':
            case 'A':
                if (csName.equals("ASCII") || equalEncodings(csName, "ASCII")) {
                    return CS_US_ASCII;
                }
                break;

            case 'c':
            case 'C':
                // Hmmh. There are boatloads of these... but what to do with them?
                encodingStartsWith(csName, "cs");// !!! TBI
                break;

            case 'e':
            case 'E':
                if (csName.startsWith("EBCDIC") || csName.startsWith("ebcdic")) {
                    return CS_EBCDIC;
                }
                break;

            case 'i':
            case 'I':
                if (csName.equals(CS_ISO_LATIN1)
                    || equalEncodings(csName, CS_ISO_LATIN1)
                    || equalEncodings(csName, "ISO-Latin1")) {
                    return CS_ISO_LATIN1;
                }
                if (encodingStartsWith(csName, "ISO-10646")) {
                    /* Hmmh. There are boatloads of alternatives here, it
                     * seems (see http://www.iana.org/assignments/character-sets
                     * for details)
                     */
                    int ix = csName.indexOf("10646");
                    String suffix = csName.substring(ix + 5);
                    if (equalEncodings(suffix, "UCS-Basic")) {
                        return CS_US_ASCII;
                    }
                    if (equalEncodings(suffix, "Unicode-Latin1")) {
                        return CS_ISO_LATIN1;
                    }
                    if (equalEncodings(suffix, "UCS-2")) {
                        return CS_UTF16; // endianness?
                    }
                    if (equalEncodings(suffix, "UCS-4")) {
                        return CS_UTF32; // endianness?
                    }
                    if (equalEncodings(suffix, "UTF-1")) {
                        // "Universal Transfer Format (1), this is the multibyte encoding, that subsets ASCII-7"???
                        return CS_US_ASCII;
                    }
                    if (equalEncodings(suffix, "J-1")) {
                        // Name: ISO-10646-J-1, Source: ISO 10646 Japanese, see RFC 1815.
                        // ... so what does that really mean? let's consider it ascii
                        return CS_US_ASCII;
                    }
                    if (equalEncodings(suffix, "US-ASCII")) {
                        return CS_US_ASCII;
                    }
                }
                break;

            case 'j':
            case 'J':
                if (equalEncodings(csName, "JIS_Encoding")) {
                    return CS_SHIFT_JIS;
                }
                break;

            case 's':
            case 'S':
                if (equalEncodings(csName, "Shift_JIS")) {
                    return CS_SHIFT_JIS;
                }
                break;

            case 'u':
            case 'U':
                if (csName.length() < 2) { // sanity check
                    break;
                }
                switch (csName.charAt(1)) {
                    case 'c':
                    case 'C':
                        if (equalEncodings(csName, "UCS-2")) {
                            return CS_UTF16;
                        }
                        if (equalEncodings(csName, "UCS-4")) {
                            return CS_UTF32;
                        }
                        break;

                    case 'n': // csUnicodeXxx,
                    case 'N':
                        if (gotCsPrefix) {
                            if (equalEncodings(csName, "Unicode")) {
                                return CS_UTF16; // need BOM
                            }
                            if (equalEncodings(csName, "UnicodeAscii")) {
                                return CS_ISO_LATIN1;
                            }
                            if (equalEncodings(csName, "UnicodeAscii")) {
                                return CS_US_ASCII;
                            }
                        }
                        break;

                    case 's':
                    case 'S':
                        if (equalEncodings(csName, "US-ASCII")) {
                            return CS_US_ASCII;
                        }
                        break;

                    case 't':
                    case 'T':
                        if (csName.equals(CS_UTF8) || equalEncodings(csName, CS_UTF8)) {
                            return CS_UTF8;
                        }
                        if (equalEncodings(csName, "UTF-16BE")) {
                            return CS_UTF16BE;
                        }
                        if (equalEncodings(csName, "UTF-16LE")) {
                            return CS_UTF16LE;
                        }
                        if (equalEncodings(csName, "UTF-16")) {
                            return CS_UTF16;
                        }
                        if (equalEncodings(csName, "UTF-32BE")) {
                            return CS_UTF32BE;
                        }
                        if (equalEncodings(csName, "UTF-32LE")) {
                            return CS_UTF32LE;
                        }
                        if (equalEncodings(csName, "UTF-32")) {
                            return CS_UTF32;
                        }
                        if (equalEncodings(csName, "UTF")) {
                            // 21-Jan-2006, TSa: ??? What is this to do... ?
                            return CS_UTF16;
                        }
                }
                break;
        }

        return csName;
    }

    /**
     * Because of legacy encodings used by earlier JDK versions, we
     * need to be careful when accessing encoding names via JDK
     * classes.
     */
    public static String findEncodingFor(Writer w) {
        if (w instanceof OutputStreamWriter) {
            String enc = ((OutputStreamWriter) w).getEncoding();
            return normalize(enc);
        }
        return null;
    }

    /*
    /**********************************************************
    /* Internal helper methods
    /**********************************************************
     */

    /**
     * Internal constant used to denote END-OF-STRING
     */
    private final static int EOS = 0x10000;

    /**
     * Method that implements a loose String comparison for encoding
     * Strings. It will work like {@link String#equalsIgnoreCase},
     * except that it will also ignore all hyphen, underscore and
     * space characters.
     */
    public static boolean equalEncodings(String str1, String str2) {
        int len1 = str1.length();
        int len2 = str2.length();

        int i1 = 0, i2 = 0;

        // Need to loop completely over both Strings
        while (i1 < len1 || i2 < len2) {
            int c1 = (i1 >= len1) ? EOS : str1.charAt(i1++);
            int c2 = (i2 >= len2) ? EOS : str2.charAt(i2++);

            // Can first do a quick comparison (usually they are equal)
            if (c1 == c2) {
                continue;
            }

            // if not equal, maybe there are WS/hyphen/underscores to skip
            while (c1 <= CHAR_SPACE || c1 == '_' || c1 == '-') {
                c1 = (i1 >= len1) ? EOS : str1.charAt(i1++);
            }
            while (c2 <= CHAR_SPACE || c2 == '_' || c2 == '-') {
                c2 = (i2 >= len2) ? EOS : str2.charAt(i2++);
            }
            // Ok, how about case differences, then?
            if (c1 != c2) {
                // If one is EOF, can't match (one is substring of the other)
                if (c1 == EOS || c2 == EOS) {
                    return false;
                }
                if (Character.toLowerCase((char) c1) != Character.toLowerCase((char) c2)) {
                    return false;
                }
            }
        }

        // If we got this far, we are ok as long as we got through it all
        return true;
    }

    public static boolean encodingStartsWith(String enc, String prefix) {
        int len1 = enc.length();
        int len2 = prefix.length();

        int i1 = 0, i2 = 0;

        // Need to loop completely over both Strings
        while (i1 < len1 || i2 < len2) {
            int c1 = (i1 >= len1) ? EOS : enc.charAt(i1++);
            int c2 = (i2 >= len2) ? EOS : prefix.charAt(i2++);

            // Can first do a quick comparison (usually they are equal)
            if (c1 == c2) {
                continue;
            }

            // if not equal, maybe there are WS/hyphen/underscores to skip
            while (c1 <= CHAR_SPACE || c1 == '_' || c1 == '-') {
                c1 = (i1 >= len1) ? EOS : enc.charAt(i1++);
            }
            while (c2 <= CHAR_SPACE || c2 == '_' || c2 == '-') {
                c2 = (i2 >= len2) ? EOS : prefix.charAt(i2++);
            }
            // Ok, how about case differences, then?
            if (c1 != c2) {
                if (c2 == EOS) { // Prefix done, good!
                    return true;
                }
                if (c1 == EOS) { // Encoding done, not good
                    return false;
                }
                if (Character.toLowerCase((char) c1) != Character.toLowerCase((char) c2)) {
                    return false;
                }
            }
        }

        // Ok, prefix was exactly the same as encoding... that's fine
        return true;
    }
}
