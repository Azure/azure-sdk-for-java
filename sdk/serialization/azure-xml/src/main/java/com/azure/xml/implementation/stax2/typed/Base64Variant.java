// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
/* Stax2 extension for basic Stax API (JSR-173).
 *
 * Copyright (c) 2005- Tatu Saloranta, tatu.saloranta@iki.fi
 *
 * Licensed under the License specified in file LICENSE, included with
 * the source code.
 * You may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.azure.xml.implementation.stax2.typed;

import java.util.Arrays;

/**
 * This abstract base class is used to define specific details of which
 * variant of Base64 encoding/decoding is to be used. Although there is
 * somewhat standard basic version (so-called "MIME Base64"), other variants
 * exists, see <a href="http://en.wikipedia.org/wiki/Base64">Base64 Wikipedia entry</a> for details.
 *<p>
 * Implementation notes:
 *<ul>
 * <li>The main complication here is trying to limit access to the underlying
efficient encoding/decoding tables -- they are needed for fast operation,
but it is potentially risky to expose raw arrays since they can not be
protected against modification. The approach here is to try to limit access
essentially to the main base64 codec classes; but this leads to bit awkward
class structure</li>
 *</ul>
 *
 * @author Tatu Saloranta
 *
 * @since 3.0.0
 */
public final class Base64Variant {
    /**
     * Placeholder used by "no padding" variant, to be used when a character
     * value is needed.
     */
    final static char PADDING_CHAR_NONE = '\0';

    /**
     * Marker used to denote ascii characters that do not correspond
     * to a 6-bit value (in this variant), and is not used as a padding
     * character.
     */
    public final static int BASE64_VALUE_INVALID = -1;

    /**
     * Marker used to denote ascii character (in decoding table) that
     * is the padding character using this variant (if any).
     */
    public final static int BASE64_VALUE_PADDING = -2;

    /*
    ////////////////////////////////////////////////////
    // Encoding/decoding tables
    ////////////////////////////////////////////////////
     */

    /**
     * Decoding table used for base 64 decoding.
     */
    private final int[] _asciiToBase64 = new int[128];

    /**
     * Encoding table used for base 64 decoding when output is done
     * as characters.
     */
    private final char[] _base64ToAsciiC = new char[64];

    /**
     * Alternative encoding table used for base 64 decoding when output is done
     * as ascii bytes.
     */
    private final byte[] _base64ToAsciiB = new byte[64];

    /*
    ////////////////////////////////////////////////////
    // Other configuration
    ////////////////////////////////////////////////////
     */

    /**
     * Symbolic name of variant; used for diagnostics/debugging.
     */
    final String _name;

    /**
     * Whether this variant uses padding or not.
     */
    final boolean _usesPadding;

    /**
     * Characted used for padding, if any ({@link #PADDING_CHAR_NONE} if not).
     */
    final char _paddingChar;

    /**
     * Maximum number of encoded base64 characters to output during encoding
     * before adding a linefeed, if line length is to be limited
     * ({@link java.lang.Integer#MAX_VALUE} if not limited).
     *<p>
     * Note: for some output modes (when writing attributes) linefeeds may
     * need to be avoided, and this value ignored.
     */
    final int _maxLineLength;

    /*
    ////////////////////////////////////////////////////
    // Life-cycle
    ////////////////////////////////////////////////////
     */

    public Base64Variant(String name, String base64Alphabet, boolean usesPadding, char paddingChar, int maxLineLength) {
        _name = name;
        _usesPadding = usesPadding;
        _paddingChar = paddingChar;
        _maxLineLength = maxLineLength;

        // Ok and then we need to create codec tables.

        // First the main encoding table:
        int alphaLen = base64Alphabet.length();
        if (alphaLen != 64) {
            throw new IllegalArgumentException("Base64Alphabet length must be exactly 64 (was " + alphaLen + ")");
        }

        // And then secondary encoding table and decoding table:
        base64Alphabet.getChars(0, alphaLen, _base64ToAsciiC, 0);
        Arrays.fill(_asciiToBase64, BASE64_VALUE_INVALID);
        for (int i = 0; i < alphaLen; ++i) {
            char alpha = _base64ToAsciiC[i];
            _base64ToAsciiB[i] = (byte) alpha;
            _asciiToBase64[alpha] = i;
        }

        // Plus if we use padding, add that in too
        if (usesPadding) {
            _asciiToBase64[paddingChar] = BASE64_VALUE_PADDING;
        }
    }

    /**
     * "Copy constructor" that can be used when the base alphabet is identical
     * to one used by another variant except for the maximum line length
     * (and obviously, name).
     */
    public Base64Variant(Base64Variant base, String name, int maxLineLength) {
        this(base, name, base._usesPadding, base._paddingChar, maxLineLength);
    }

    /**
     * "Copy constructor" that can be used when the base alphabet is identical
     * to one used by another variant, but other details (padding, maximum
     * line length) differ
     */
    public Base64Variant(Base64Variant base, String name, boolean usesPadding, char paddingChar, int maxLineLength) {
        _name = name;
        byte[] srcB = base._base64ToAsciiB;
        System.arraycopy(srcB, 0, this._base64ToAsciiB, 0, srcB.length);
        char[] srcC = base._base64ToAsciiC;
        System.arraycopy(srcC, 0, this._base64ToAsciiC, 0, srcC.length);
        int[] srcV = base._asciiToBase64;
        System.arraycopy(srcV, 0, this._asciiToBase64, 0, srcV.length);

        _usesPadding = usesPadding;
        _paddingChar = paddingChar;
        _maxLineLength = maxLineLength;
    }

    /*
    ////////////////////////////////////////////////////
    // Public accessors
    ////////////////////////////////////////////////////
     */

    public String getName() {
        return _name;
    }

    public boolean usesPadding() {
        return _usesPadding;
    }

    public boolean usesPaddingChar(char c) {
        return c == _paddingChar;
    }

    public char getPaddingChar() {
        return _paddingChar;
    }

    /*
    ////////////////////////////////////////////////////
    // Decoding support
    ////////////////////////////////////////////////////
     */

    /**
     * @return 6-bit decoded value, if valid character;
     */
    public int decodeBase64Char(char c) {
        return ((int) c <= 127) ? _asciiToBase64[c] : BASE64_VALUE_INVALID;
    }

    /*
    ////////////////////////////////////////////////////
    // Encoding support
    ////////////////////////////////////////////////////
     */

    /*
    ////////////////////////////////////////////////////
    // other methods
    ////////////////////////////////////////////////////
     */

    @Override
    public String toString() {
        return _name;
    }
}
