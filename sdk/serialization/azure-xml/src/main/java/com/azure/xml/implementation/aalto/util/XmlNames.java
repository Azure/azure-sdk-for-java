// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.util;

/**
 * Simple utility class used for checking validity of xml names.
 */
public final class XmlNames {
    /**
     * Method that can be used to verify whether given String is
     * a valid xml name or not.
     *
     * @return Index of the first character in given String that is
     *   not a valid xml name character, if any; -1 if string is
     *   a valid xml name
     */
    public static int findIllegalNameChar(String name, boolean xml11) {
        int ptr = 0;
        char c = name.charAt(ptr);
        int len = name.length();
        if (c < 0xD800 || c >= 0xE000) {
            if (xml11) {
                if (!XmlChars.is11NameStartChar(c)) {
                    return ptr;
                }
            } else {
                if (!XmlChars.is10NameStartChar(c)) {
                    return ptr;
                }
            }
        } else {
            if (len < 2) {
                return ptr;
            }
            ++ptr;
            // Only returns if ok; throws exception otherwise
            if (validSurrogateNameChar()) {
                return ptr;
            }
        }
        ++ptr;

        if (xml11) {
            for (; ptr < len; ++ptr) {
                c = name.charAt(ptr);
                if (c < 0xD800 || c >= 0xE000) {
                    if (!XmlChars.is11NameChar(c)) {
                        return ptr;
                    }
                } else {
                    if ((ptr + 1) >= len) { // unpaired surrogate
                        return ptr;
                    }
                    if (validSurrogateNameChar()) {
                        return ptr;
                    }
                    ++ptr;
                }
            }
        } else {
            for (; ptr < len; ++ptr) {
                c = name.charAt(ptr);
                if (c < 0xD800 || c >= 0xE000) {
                    if (!XmlChars.is10NameChar(c)) {
                        return ptr;
                    }
                } else {
                    if ((ptr + 1) >= len) { // unpaired surrogate
                        return ptr;
                    }
                    if (validSurrogateNameChar()) {
                        return ptr;
                    }
                    ++ptr;
                }
            }
        }
        return -1;
    }

    private static boolean validSurrogateNameChar() {
        // And the composite, is it ok?
        //        int val = ((firstChar - 0xD800) << 10) + 0x10000;

        // 04-Jan-2021, tatu: As per lgtm.com's warning, yes, due to range checks
        //   for first and second char, cannot exceed maximum
        //        if (val > XmlConsts.MAX_UNICODE_CHAR) {
        //           return false;
        //        }

        // !!! TODO: xml 1.1 vs 1.0 rules: none valid for 1.0, many for 1.1

        // 04-Jan-2021, tatu: Hmmh. Do we really fail on all surrogate characters in
        //   names (for now). That seems incorrect.

        return true;
    }

}
