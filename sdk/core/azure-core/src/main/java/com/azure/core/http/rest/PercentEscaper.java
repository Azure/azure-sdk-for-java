// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.util.HashSet;
import java.util.Set;

/**
 * An escaper that escapes URL data through percent encoding.
 */
final class PercentEscaper {
    private static final char[] HEX_CHARACTERS = "0123456789ABCDEF".toCharArray();

    /*
     * The characters in this string are always safe to use.
     */
    private static final String SAFE_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final ClientLogger LOGGER = new ClientLogger(PercentEscaper.class);

    private final boolean usePlusForSpace;
    private final Set<Integer> safeCharacterPoints;

    /**
     * Creates a percent escaper.
     *
     * @param safeCharacters Collection of characters that won't be escaped.
     * @param usePlusForSpace If true {@code ' '} will be escaped as {@code '+'} instead of {@code "%20"}.
     */
    PercentEscaper(String safeCharacters, boolean usePlusForSpace) {
        this.usePlusForSpace = usePlusForSpace;

        if (usePlusForSpace && safeCharacters != null && safeCharacters.contains(" ")) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "' ' as a safe character with 'usePlusForSpace = true' is an invalid configuration."));
        }

        this.safeCharacterPoints = new HashSet<>();
        SAFE_CHARACTERS.codePoints().forEach(safeCharacterPoints::add);
        if (!CoreUtils.isNullOrEmpty(safeCharacters)) {
            safeCharacters.codePoints().forEach(safeCharacterPoints::add);
        }
    }

    /**
     * Escapes a string with the current settings on the escaper.
     *
     * @param original the origin string to escape
     * @return the escaped string
     */
    public String escape(String original) {
        // String is either null or empty, just return it as is.
        if (CoreUtils.isNullOrEmpty(original)) {
            return original;
        }

        StringBuilder escapedBuilder = new StringBuilder();
        int index = 0;
        int end = original.length();

        /*
         * When the UTF-8 character is more than one byte the bytes will be converted to hex in reverse order to allow
         * for simpler logic being used. To make this easier a temporary character array will be used to keep track of
         * the conversion.
         */
        while (index < end) {
            int codePoint = getCodePoint(original, index, end);

            // Supplementary code points are comprised of two characters in the string.
            index += (Character.isSupplementaryCodePoint(codePoint)) ? 2 : 1;

            if (safeCharacterPoints.contains(codePoint)) {
                // This is a safe character, use it as is.
                // All safe characters should be ASCII.
                escapedBuilder.append((char) codePoint);
            } else if (usePlusForSpace && codePoint == ' ') {
                // Character is a space, and we are using '+' instead of "%20".
                escapedBuilder.append('+');
            } else if (codePoint <= 0x7F) {
                // Character is one byte, use format '%xx'.
                // Leading bit is always 0.
                escapedBuilder.append('%');

                // Shift 4 times to the right to get the leading 4 bits and get the corresponding hex character.
                escapedBuilder.append(HEX_CHARACTERS[codePoint >>> 4]);

                // Mask all but the last 4 bits and get the corresponding hex character.
                escapedBuilder.append(HEX_CHARACTERS[codePoint & 0xF]);
            } else if (codePoint <= 0x7FF) {
                /*
                 * Character is two bytes, use the format '%xx%xx'. Leading bits in the first byte are always 110 and
                 * the leading bits in the second byte are always 10. The conversion will happen using the following
                 * logic:
                 *
                 * 1. Mask with bits 1111 to get the last hex character.
                 * 2. Shift right 4 times to move to the next hex quad bits.
                 * 3. Mask with bits 11 and then bitwise or with bits 1000 to get the leading hex in the second byte.
                 * 4. Shift right 2 times to move to the next hex quad bits.
                 *   a. This is only shifted twice since the bits 10 are the encoded value but not in the code point.
                 * 5. Mask with bits 1111 to get the second hex character in the first byte.
                 * 6. Shift right 4 times to move to the next hex quad bits.
                 * 7. Bitwise or with bits 1100 to get the leading hex character.
                 */
                char[] chars = new char[6];
                chars[0] = '%';
                chars[3] = '%';

                chars[5] = HEX_CHARACTERS[codePoint & 0xF];

                codePoint >>>= 4;
                chars[4] = HEX_CHARACTERS[0x8 | (codePoint & 0x3)];

                codePoint >>>= 2;
                chars[2] = HEX_CHARACTERS[codePoint & 0xF];

                codePoint >>>= 4;
                chars[1] = HEX_CHARACTERS[codePoint | 0xC];

                escapedBuilder.append(chars);
            } else if (codePoint <= 0xFFFF) {
                /*
                 * Character is three bytes, use the format '%Ex%xx%xx'. Leading bits in the first byte are always
                 * 1110 (hence it is '%Ex'), the leading bits in both the second and third byte are always 10. The
                 * conversion will happen using the following logic:
                 *
                 * 1. Mask with bits 1111 to get the last hex character.
                 * 2. Shift right 4 times to move to the next hex quad bits.
                 * 3. Mask with bits 11 and then bitwise or with bits 1000 to get the leading hex in the third byte.
                 * 4. Shift right 2 times to move to the next hex quad bits.
                 *   a. This is only shifted twice since the bits 10 are the encoded value but not in the code point.
                 * 5. Repeat steps 1-4 to convert the second byte.
                 * 6. Mask with bits 1111 to get the second hex character in the first byte.
                 *
                 * Note: No work is needed for the leading hex character since it is always 'E'.
                 */
                char[] chars = new char[9];
                chars[0] = '%';
                chars[1] = 'E';
                chars[3] = '%';
                chars[6] = '%';

                chars[8] = HEX_CHARACTERS[codePoint & 0xF];

                codePoint >>>= 4;
                chars[7] = HEX_CHARACTERS[0x8 | (codePoint & 0x3)];

                codePoint >>>= 2;
                chars[5] = HEX_CHARACTERS[codePoint & 0xF];

                codePoint >>>= 4;
                chars[4] = HEX_CHARACTERS[0x8 | (codePoint & 0x3)];

                codePoint >>>= 2;
                chars[2] = HEX_CHARACTERS[codePoint & 0xF];

                escapedBuilder.append(chars);
            } else if (codePoint <= 0x10FFFF) {
                /*
                 * Character is four bytes, use the format '%Fx%xx%xx%xx'. Leading bits in the first byte are always
                 * 11110 (hence it is '%Fx'), the leading bits in the other bytes are always 10. The conversion will
                 * happen using the following logic:
                 *
                 * 1. Mask with bits 1111 to get the last hex character.
                 * 2. Shift right 4 times to move to the next hex quad bits.
                 * 3. Mask with bits 11 and then bitwise or with bits 1000 to get the leading hex in the fourth byte.
                 * 4. Shift right 2 times to move to the next hex quad bits.
                 *   a. This is only shifted twice since the bits 10 are the encoded value but not in the code point.
                 * 5. Repeat steps 1-4 to convert the second and third bytes.
                 * 6. Mask with bits 111 to get the second hex character in the first byte.
                 *
                 * Note: No work is needed for the leading hex character since it is always 'F'.
                 */
                char[] chars = new char[12];
                chars[0] = '%';
                chars[1] = 'F';
                chars[3] = '%';
                chars[6] = '%';
                chars[9] = '%';

                chars[11] = HEX_CHARACTERS[codePoint & 0xF];

                codePoint >>>= 4;
                chars[10] = HEX_CHARACTERS[0x8 | (codePoint & 0x3)];

                codePoint >>>= 2;
                chars[8] = HEX_CHARACTERS[codePoint & 0xF];

                codePoint >>>= 4;
                chars[7] = HEX_CHARACTERS[0x8 | (codePoint & 0x3)];

                codePoint >>>= 2;
                chars[5] = HEX_CHARACTERS[codePoint & 0xF];

                codePoint >>>= 4;
                chars[4] = HEX_CHARACTERS[0x8 | (codePoint & 0x3)];

                codePoint >>>= 2;
                chars[2] = HEX_CHARACTERS[codePoint & 0x7];

                escapedBuilder.append(chars);
            }
        }

        return escapedBuilder.toString();
    }

    /*
     * Java uses UTF-16 to represent Strings, due to characters only being 2 bytes they must use surrogate pairs to
     * get the correct code point for characters above 0xFFFF.
     */
    private static int getCodePoint(String original, int index, int end) {
        char char1 = original.charAt(index++);
        if (!Character.isSurrogate(char1)) {
            // Character isn't a surrogate, return it as is.
            return char1;
        } else if (Character.isHighSurrogate(char1)) {
            // High surrogates will occur first in the string.
            if (index == end) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "String contains trailing high surrogate without paired low surrogate."));
            }

            char char2 = original.charAt(index);
            if (Character.isLowSurrogate(char2)) {
                return Character.toCodePoint(char1, char2);
            }

            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "String contains high surrogate without trailing low surrogate."));
        } else {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "String contains low surrogate without leading high surrogate."));
        }
    }
}
