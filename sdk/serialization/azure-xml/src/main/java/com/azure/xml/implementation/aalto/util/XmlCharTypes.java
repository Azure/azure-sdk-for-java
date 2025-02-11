// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
/* Woodstox Lite ("wool") XML processor
 *
 * Copyright (c) 2006- Tatu Saloranta, tatu.saloranta@iki.fi
 *
 * Licensed under the License specified in the file LICENSE which is
 * included with the source code.
 * You may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.azure.xml.implementation.aalto.util;

/**
 * This is a simple container class, mostly used to encapsulate details
 * of character typing out of parser/scanner/writer classes, while still
 * making int arrays auto-generated only if needed (esp. for encodings
 * never needed, which may be the case for ascii etc).
 */
public class XmlCharTypes {
    // First, common constants to all (non-name) types:

    public final static int CT_OK = 0;
    public final static int CT_INVALID = 1; // either invalid xml in general, or in this context
    public final static int CT_WS_CR = 2;
    public final static int CT_WS_LF = 3;

    public final static int CT_MULTIBYTE_N = 4; // (too) long encoding
    public final static int CT_MULTIBYTE_2 = 5; // 2-byte encoding
    public final static int CT_MULTIBYTE_3 = 6; // 3-byte encoding
    public final static int CT_MULTIBYTE_4 = 7; // 4-byte encoding

    // Constants for regular char types

    public final static int CT_WS_TAB = 8;
    public final static int CT_LT = 9; // for start/end tags
    public final static int CT_AMP = 10; // for entities
    public final static int CT_RBRACKET = 11; // for ]]> detection
    public final static int CT_QMARK = 12; // for PI
    public final static int CT_HYPHEN = 13; // for Comments
    public final static int CT_ATTR_QUOTE = 14; // ' and ", for attr values

    public final static int CT_GT = 17; // for dtd subset sections

    // // // Constants for DTDs:

    // (first ones from common types)
    public final static int CT_DTD_QUOTE = 8; // ' and ", for attr values
    public final static int CT_DTD_LT = 9; // directive start/end
    public final static int CT_DTD_GT = 10;
    public final static int CT_DTD_RBRACKET = 11; // for ending dtd subset
    public final static int CT_DTD_PERCENT = 12; // for ending dtd subset

    // // // Constants for names:

    /* These are common constants for name char types, shared between
     * both input and output sides:
     */
    public final static int CT_NAME_NONE = 0; // not a valid name char
    public final static int CT_NAME_COLON = 1; // not a valid name char
    public final static int CT_NAME_NONFIRST = 2; // good name char except as first (including colon)
    public final static int CT_NAME_ANY = 3; // good name char, first or any

    // // // Constants for public ids:

    public final static int PUBID_OK = 1;

    // Instance data

    /**
     * Character type table used for regular textual content (for
     * CHARACTERS event)
     */
    public final int[] TEXT_CHARS;

    /**
     * Character type table used for attribute values
     */
    public final int[] ATTR_CHARS;

    /**
     * Character type table used for name characters (note: type ints
     * used different from other tables)
     */
    public final int[] NAME_CHARS;

    /**
     * Character type table used for DTD subsets; contains a few
     * additional types beyond most tables
     */
    public final int[] DTD_CHARS;

    /**
     * Character type table used for events other than CHARACTERS or
     * elements; ie. for comments, PIs, CData, DTD internal subset
     */
    public final int[] OTHER_CHARS;

    /**
     * And finally, we also have shared table for valid public id
     * characters...
     */
    public final static int[] PUBID_CHARS = new int[256];
    static {
        for (int i = 0, last = ('z' - 'a'); i <= last; ++i) {
            PUBID_CHARS['A' + i] = PUBID_OK;
            PUBID_CHARS['a' + i] = PUBID_OK;
        }
        for (int i = '0'; i <= '9'; ++i) {
            PUBID_CHARS[i] = PUBID_OK;
        }

        // 3 main white space types are valid
        PUBID_CHARS[0x0A] = PUBID_OK;
        PUBID_CHARS[0x0D] = PUBID_OK;
        PUBID_CHARS[0x20] = PUBID_OK;

        // And many of punctuation/separator ascii chars too:
        PUBID_CHARS['-'] = PUBID_OK;
        PUBID_CHARS['\''] = PUBID_OK;
        PUBID_CHARS['('] = PUBID_OK;
        PUBID_CHARS[')'] = PUBID_OK;
        PUBID_CHARS['+'] = PUBID_OK;
        PUBID_CHARS[','] = PUBID_OK;
        PUBID_CHARS['.'] = PUBID_OK;
        PUBID_CHARS['/'] = PUBID_OK;
        PUBID_CHARS[':'] = PUBID_OK;
        PUBID_CHARS['='] = PUBID_OK;
        PUBID_CHARS['?'] = PUBID_OK;
        PUBID_CHARS[';'] = PUBID_OK;
        PUBID_CHARS['!'] = PUBID_OK;
        PUBID_CHARS['*'] = PUBID_OK;
        PUBID_CHARS['#'] = PUBID_OK;
        PUBID_CHARS['@'] = PUBID_OK;
        PUBID_CHARS['$'] = PUBID_OK;
        PUBID_CHARS['_'] = PUBID_OK;
        PUBID_CHARS['%'] = PUBID_OK;
    }

    public XmlCharTypes() {
        this(256);
    }

    public XmlCharTypes(int size) {
        TEXT_CHARS = new int[size];
        ATTR_CHARS = new int[size];
        NAME_CHARS = new int[size];
        DTD_CHARS = new int[size];
        OTHER_CHARS = new int[size];
    }

    public static void fillInLatin1Chars(int[] textChars, int[] attrChars, int[] nameChars, int[] dtdChars,
        int[] otherChars) {
        // text:
        fillIn8BitTextRange(textChars);
        // high-order entries are 'ok' by default, no need to fill

        // attr:
        fillIn8BitAttrRange(attrChars);
        // high-order entries are 'ok' by default, no need to fill

        // name chars:
        fillIn8BitNameRange(nameChars);
        // High-order name tokens...
        for (int i = 0xC0; i <= 0xFF; ++i) {
            if (i != 0xD7 && i != 0xF7) {
                nameChars[i] = CT_NAME_ANY;
            }
        }
        nameChars[0xB7] = CT_NAME_NONFIRST;

        // // DTD chars:
        fillIn8BitDtdRange(dtdChars);

        // ... lotsa matching to do here

        // others:
        // let's start with basic text chars:
        fillIn8BitTextRange(otherChars);

        /* And then just remove amp and lt (not special in any of these
         * events), and add ']', '?' and '-', which mark start of end
         * markers in the events.
         */
        otherChars['&'] = CT_OK;
        otherChars['<'] = CT_OK;

        otherChars[']'] = CT_RBRACKET; // for CDATA
        otherChars['?'] = CT_QMARK; // for PI
        otherChars['-'] = CT_HYPHEN; // for Comment
    }

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    private static void fillInCommonTextRange(int[] arr) {
        for (int i = 0; i < 32; ++i) {
            arr[i] = CT_INVALID;
        }
        // And linefeeds are always converted
        arr['\r'] = CT_WS_CR;
        arr['\n'] = CT_WS_LF;
        arr['\t'] = CT_OK; // it's just fine, usually not converted
    }

    public static void fillIn8BitNameRange(int[] arr) {
        for (int i = 'a'; i <= 'z'; ++i) {
            arr[i] = CT_NAME_ANY;
        }
        for (int i = 'A'; i <= 'Z'; ++i) {
            arr[i] = CT_NAME_ANY;
        }
        // Non-letter first chars:
        arr['_'] = CT_NAME_ANY;

        // And then non-first ones:
        arr[':'] = CT_NAME_COLON;
        arr['-'] = CT_NAME_NONFIRST;
        arr['.'] = CT_NAME_NONFIRST;
        for (int i = '0'; i <= '9'; ++i) {
            arr[i] = CT_NAME_NONFIRST;
        }
    }

    /**
     * Called to set state of 7-bit chars in text content
     */
    protected static void fillIn8BitTextRange(int[] arr) {
        fillInCommonTextRange(arr);
        arr['<'] = CT_LT;
        arr['&'] = CT_AMP;
        arr[']'] = CT_RBRACKET;
    }

    /**
     * Called to set state of 7-bit chars in attribute values
     */
    protected static void fillIn8BitAttrRange(int[] arr) {
        fillInCommonTextRange(arr);
        arr['\t'] = CT_WS_TAB;
        arr['<'] = CT_LT;
        arr['&'] = CT_AMP;
        arr['\''] = CT_ATTR_QUOTE;
        arr['"'] = CT_ATTR_QUOTE;
    }

    protected static void fillIn8BitDtdRange(int[] arr) {
        fillInCommonTextRange(arr);
        arr['\''] = CT_DTD_QUOTE;
        arr['"'] = CT_DTD_QUOTE;
        arr['<'] = CT_DTD_LT;
        arr['>'] = CT_DTD_GT;
        // No need to check for lbracket (for now?)
        arr[']'] = CT_DTD_RBRACKET;
        arr['%'] = CT_DTD_PERCENT;
    }
}
