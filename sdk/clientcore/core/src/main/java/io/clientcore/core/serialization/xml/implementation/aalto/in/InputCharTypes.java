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

package io.clientcore.core.serialization.xml.implementation.aalto.in;

import io.clientcore.core.serialization.xml.implementation.aalto.util.XmlCharTypes;

public final class InputCharTypes extends XmlCharTypes {
    // Singleton instances:

    /* Let's create non-UTF types lazily, as there's a good chance
     * they might not be used, thereby possibly reducing memory footprint
     * and startup time
     */

    private static XmlCharTypes sLatin1CharTypes = null;

    /* Note: unlike others, let's create eagerly, not lazily,
     * as this is expected to be the common case:
     */
    private final static XmlCharTypes sUtf8CharTypes = new XmlCharTypes();
    static {
        fillInUtf8Chars(sUtf8CharTypes.TEXT_CHARS, sUtf8CharTypes.ATTR_CHARS, sUtf8CharTypes.NAME_CHARS,
            sUtf8CharTypes.DTD_CHARS, sUtf8CharTypes.OTHER_CHARS);
    }

    public static XmlCharTypes getLatin1CharTypes() {
        if (sLatin1CharTypes == null) {
            sLatin1CharTypes = new XmlCharTypes();
            fillInLatin1Chars(sLatin1CharTypes.TEXT_CHARS, sLatin1CharTypes.ATTR_CHARS, sLatin1CharTypes.NAME_CHARS,
                sLatin1CharTypes.DTD_CHARS, sLatin1CharTypes.OTHER_CHARS);
        }
        return sLatin1CharTypes;
    }

    public static void fillInUtf8Chars(int[] textChars, int[] attrChars, int[] nameChars, int[] dtdChars,
        int[] otherChars) {
        // text chars
        fillIn8BitTextRange(textChars);
        fillInMultiByteTextRange(textChars);

        // attr chars
        fillIn8BitAttrRange(attrChars);
        fillInMultiByteTextRange(attrChars);

        // name chars
        fillIn8BitNameRange(nameChars);
        /* Although 7-bit range uses different values, let's use
         * same byte length markers for 8-bit range (as with text content)
         */
        fillInMultiByteTextRange(nameChars);

        // // DTD chars:
        fillIn8BitDtdRange(dtdChars);
        fillInMultiByteTextRange(dtdChars);

        // ... lotsa matching to do here
        otherChars[']'] = CT_RBRACKET;
        otherChars['>'] = CT_GT;

        // and finally, others (comment, CDATA, PI)
        // let's start with basic text chars:
        fillIn8BitTextRange(otherChars);
        fillInMultiByteTextRange(otherChars);

        /* And then just remove ampersand and lt (not special in any of
         * these events), and add ']', '?' and '-', which mark start of end
         * markers in the events.
         */
        otherChars['&'] = CT_OK;
        otherChars['<'] = CT_OK;

        otherChars[']'] = CT_RBRACKET; // for CDATA
        otherChars['?'] = CT_QMARK; // for PI
        otherChars['-'] = CT_HYPHEN; // for Comment
    }

    private static void fillInMultiByteTextRange(int[] arr) {
        for (int c = 128; c < 256; ++c) {
            int code;

            // Let's use code from UTF-8 decoder, to ensure correctness
            if ((c & 0xE0) == 0xC0) { // 2 bytes (0x0080 - 0x07FF)
                code = CT_MULTIBYTE_2;
            } else if ((c & 0xF0) == 0xE0) { // 3 bytes (0x0800 - 0xFFFF)
                code = CT_MULTIBYTE_3;
            } else if ((c & 0xF8) == 0xF0) {
                // 4 bytes; double-char with surrogates and all...
                code = CT_MULTIBYTE_4;
            } else {
                code = CT_INVALID;
            }
            arr[c] = code;
        }
    }

}
