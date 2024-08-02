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

package com.azure.xml.implementation.aalto.out;

import com.azure.xml.implementation.aalto.util.XmlCharTypes;
import com.azure.xml.implementation.aalto.util.XmlChars;

public final class OutputCharTypes extends XmlCharTypes {
    /**
     * Although many encodings (Latin1, Ascii) could use shorter tables,
     * for UTF-8 2k makes sense, as it will then cover both one and
     * type byte sequences. And this being the case, let's use the same
     * size for all encodings.
     */
    final static int MAIN_TABLE_SIZE = 2048;

    // Note: this is only used on writer-side, thus overlaps with previous one:
    public final static int CT_OUTPUT_MUST_QUOTE = CT_MULTIBYTE_N;

    /* Also, dealing with names is bit more complicated, as there's
     * both validity, and possible encoding, to tackle...
     * So let's try if we can figure out combinations: only 1 and 2-byte
     * encodings are covered by the table, fortunately
     *<p>
     * Note: values must not overlap with base class' constants
     */

    /**
     * Unencodable means that while the name char may be acceptable
     * per se, it can not be encode using current encoding
     */
    public final static int CT_OUTPUT_NAME_UNENCODABLE = 4;
    public final static int CT_OUTPUT_NAME_NONFIRST_MB2 = 5;
    public final static int CT_OUTPUT_NAME_ANY_MB2 = 6;

    // Singleton instances:

    private static XmlCharTypes sAsciiCharTypes = null;

    private static XmlCharTypes sLatin1CharTypes = null;

    /* Note: unlike others, let's create eagerly, not lazily,
     * as this is expected to be the common case:
     */
    private final static XmlCharTypes sUtf8CharTypes = new XmlCharTypes(MAIN_TABLE_SIZE);
    static {
        /* On output side, utf-8 handling is bit different though;
         * 8-bit range is affected but in different way. So let's actually
         * start from vanilla Latin1 settings:
         */
        fillInLatin1Chars(sUtf8CharTypes.TEXT_CHARS, sUtf8CharTypes.ATTR_CHARS, sUtf8CharTypes.NAME_CHARS,
            sUtf8CharTypes.DTD_CHARS, sUtf8CharTypes.OTHER_CHARS);
        /* And then just note that all 8-bit textual things need
         * two-byte encoding (not applicable for name tables, though,
         * uses separate vales)
         */
        modifyForUtf8(sUtf8CharTypes.TEXT_CHARS);
        modifyForUtf8(sUtf8CharTypes.ATTR_CHARS);
        modifyForUtf8(sUtf8CharTypes.DTD_CHARS);
        modifyForUtf8(sUtf8CharTypes.OTHER_CHARS);

        /* But beyond that, not all name characters between 256 and 2047
         * are legal...
         */
        modifyUtf8Names(sUtf8CharTypes.NAME_CHARS);

        /* One final point: within attribute values, we need tad more
         * quoting for some things.
         */
        modifyForAttrWrite(sUtf8CharTypes.ATTR_CHARS);
    }

    public final static XmlCharTypes getUtf8CharTypes() {
        return sUtf8CharTypes;
    }

    public final static XmlCharTypes getLatin1CharTypes() {
        if (sLatin1CharTypes == null) {
            sLatin1CharTypes = new XmlCharTypes(MAIN_TABLE_SIZE);
            fillInLatin1Chars(sLatin1CharTypes.TEXT_CHARS, sLatin1CharTypes.ATTR_CHARS, sLatin1CharTypes.NAME_CHARS,
                sLatin1CharTypes.DTD_CHARS, sLatin1CharTypes.OTHER_CHARS);
            modifyForLatin1(sLatin1CharTypes.TEXT_CHARS);
            modifyForLatin1(sLatin1CharTypes.ATTR_CHARS);
            modifyForLatin1(sLatin1CharTypes.DTD_CHARS);
            modifyForLatin1(sLatin1CharTypes.OTHER_CHARS);
            // not applicable for names

            // Also, extra quoting for some chars in attr values
            modifyForAttrWrite(sLatin1CharTypes.ATTR_CHARS);
        }
        return sLatin1CharTypes;
    }

    public final static XmlCharTypes getAsciiCharTypes() {
        if (sAsciiCharTypes == null) {
            sAsciiCharTypes = new XmlCharTypes(MAIN_TABLE_SIZE);
            // We'll start with 8-bit char set
            fillInLatin1Chars(sAsciiCharTypes.TEXT_CHARS, sAsciiCharTypes.ATTR_CHARS, sAsciiCharTypes.NAME_CHARS,
                sAsciiCharTypes.DTD_CHARS, sAsciiCharTypes.OTHER_CHARS);
            // And then just require quoting for non-7-bit chars
            modifyForAscii(sAsciiCharTypes.TEXT_CHARS);
            modifyForAscii(sAsciiCharTypes.ATTR_CHARS);
            modifyForAscii(sAsciiCharTypes.DTD_CHARS);
            modifyForAscii(sAsciiCharTypes.OTHER_CHARS);
            modifyAsciiNames(sAsciiCharTypes.NAME_CHARS);

            // Also, extra quoting for some chars in attr values
            modifyForAttrWrite(sAsciiCharTypes.ATTR_CHARS);
        }
        return sAsciiCharTypes;
    }

    private static void modifyForLatin1(int[] charTable) {
        /* And also mark 0x7F - 0x9F (although for xml 1.1, could
         * consider not quoting 0x85?)
         */
        for (int i = 0x7F; i <= 0x9F; ++i) {
            charTable[i] = CT_OUTPUT_MUST_QUOTE;
        }
        requireQuotingAfter(charTable, 0xFF);
    }

    private static void modifyLatin1Names(int[] charTable) {
        for (int i = 0x100, len = charTable.length; i < len; ++i) {
            // Just need to indicate none should be 'ok'...
            int val = charTable[i];
            if (val == CT_NAME_NONFIRST || val == CT_NAME_ANY) {
                charTable[i] = CT_OUTPUT_NAME_UNENCODABLE;
            }
        }
    }

    private static void modifyForUtf8(int[] charTable) {
        for (int i = 0x80, len = charTable.length; i < len; ++i) {
            // Let's not modify entries that indicate 'must quote' or invalid:
            if (charTable[i] == CT_OK) {
                charTable[i] = CT_MULTIBYTE_2;
            }
        }
    }

    private static void modifyUtf8Names(int[] charTable) {
        /* !!! For now, we'll just use xml 1.0 rules, for 1.1 need
         *   to use separate set of tables.
         */
        for (int i = 0x80, len = charTable.length; i < len; ++i) {
            if (XmlChars.is10NameStartChar(i)) {
                charTable[i] = CT_OUTPUT_NAME_ANY_MB2;
            } else if (XmlChars.is10NameChar(i)) {
                charTable[i] = CT_OUTPUT_NAME_NONFIRST_MB2;
            } else {
                charTable[i] = CT_NAME_NONE;
            }
        }
    }

    private static void modifyForAscii(int[] charTable) {
        requireQuotingAfter(charTable, 0x7F);
    }

    private static void modifyAsciiNames(int[] charTable) {
        modifyLatin1Names(charTable);
        for (int i = 0x80, len = charTable.length; i < len; ++i) {
            // Just need to indicate none should be 'ok'...
            int val = charTable[i];
            if (val == CT_NAME_NONFIRST || val == CT_NAME_ANY) {
                charTable[i] = CT_OUTPUT_NAME_UNENCODABLE;
            }
        }
    }

    private static void modifyForAttrWrite(int[] charTable) {
        charTable['\t'] = CT_OUTPUT_MUST_QUOTE;
    }

    private static void requireQuotingAfter(int[] charTable, int lastValid) {
        // For the most part, like Latin1
        for (int i = lastValid + 1, len = charTable.length; i < len; ++i) {
            // Just need to indicate none should be 'ok'...
            if (charTable[i] == CT_OK) {
                charTable[i] = CT_OUTPUT_MUST_QUOTE;
            }
        }
    }
}
