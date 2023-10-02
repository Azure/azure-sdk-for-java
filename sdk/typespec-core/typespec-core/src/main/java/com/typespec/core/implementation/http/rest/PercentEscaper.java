// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.http.rest;

import com.typespec.core.util.CoreUtils;
import com.typespec.core.util.logging.ClientLogger;

import java.util.Arrays;

/**
 * An escaper that escapes URL data through percent encoding.
 */
public final class PercentEscaper {
    private static final char[] HEX_CHARACTERS = "0123456789ABCDEF".toCharArray();

    private static final boolean[] SAFE_CHARACTERS;

    static {
        // ASCII alphanumerics are always safe to use.
        SAFE_CHARACTERS = new boolean[256];
        Arrays.fill(SAFE_CHARACTERS, 'a', 'z' + 1, true);
        Arrays.fill(SAFE_CHARACTERS, 'A', 'Z' + 1, true);
        Arrays.fill(SAFE_CHARACTERS, '0', '9' + 1, true);
    }

    private static final ClientLogger LOGGER = new ClientLogger(PercentEscaper.class);

    private final boolean usePlusForSpace;
    private final boolean[] safeCharacterPoints;

    /**
     * Creates a percent escaper.
     *
     * @param safeCharacters Collection of characters that won't be escaped.
     * @param usePlusForSpace If true {@code ' '} will be escaped as {@code '+'} instead of {@code "%20"}.
     */
    public PercentEscaper(String safeCharacters, boolean usePlusForSpace) {
        this.usePlusForSpace = usePlusForSpace;

        if (usePlusForSpace && safeCharacters != null && safeCharacters.contains(" ")) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "' ' as a safe character with 'usePlusForSpace = true' is an invalid configuration."));
        }

        this.safeCharacterPoints = Arrays.copyOf(SAFE_CHARACTERS, 256); // 256 works as only ASCII characters are safe.
        if (!CoreUtils.isNullOrEmpty(safeCharacters)) {
            safeCharacters.codePoints().forEach(c -> safeCharacterPoints[c] = true);
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

        StringBuilder escapedBuilder = null;
        int last = 0;
        int index = 0;
        int end = original.length();
        char[] buffer = new char[12]; // largest possible buffer

        /*
         * When the UTF-8 character is more than one byte the bytes will be converted to hex in reverse order to allow
         * for simpler logic being used. To make this easier a temporary character array will be used to keep track of
         * the conversion.
         */
        while (index < end) {
            int codePoint = getCodePoint(original, index, end);
            int toIndex = index;

            if (codePoint < 256 && safeCharacterPoints[codePoint]) {
                // This is a safe character, use it as is.
                // All safe characters should be ASCII.
                index++;
                continue;
            }

            // Supplementary code points are comprised of two characters in the string.
            // Check for supplementary code points after checking for safe characters as safe characters are always
            // 1 index.
            index += (Character.isSupplementaryCodePoint(codePoint)) ? 2 : 1;

            if (escapedBuilder == null) {
                escapedBuilder = new StringBuilder((int) Math.ceil(original.length() * 1.5));
            }

            escapedBuilder.append(original, last, toIndex);
            last = index;

            if (usePlusForSpace && codePoint == ' ') {
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
                buffer[0] = '%';
                buffer[3] = '%';

                buffer[5] = HEX_CHARACTERS[codePoint & 0xF];

                codePoint >>>= 4;
                buffer[4] = HEX_CHARACTERS[0x8 | (codePoint & 0x3)];

                codePoint >>>= 2;
                buffer[2] = HEX_CHARACTERS[codePoint & 0xF];

                codePoint >>>= 4;
                buffer[1] = HEX_CHARACTERS[codePoint | 0xC];

                escapedBuilder.append(buffer, 0, 6);
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
                buffer[0] = '%';
                buffer[1] = 'E';
                buffer[3] = '%';
                buffer[6] = '%';

                buffer[8] = HEX_CHARACTERS[codePoint & 0xF];

                codePoint >>>= 4;
                buffer[7] = HEX_CHARACTERS[0x8 | (codePoint & 0x3)];

                codePoint >>>= 2;
                buffer[5] = HEX_CHARACTERS[codePoint & 0xF];

                codePoint >>>= 4;
                buffer[4] = HEX_CHARACTERS[0x8 | (codePoint & 0x3)];

                codePoint >>>= 2;
                buffer[2] = HEX_CHARACTERS[codePoint & 0xF];

                escapedBuilder.append(buffer, 0, 9);
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
                buffer[0] = '%';
                buffer[1] = 'F';
                buffer[3] = '%';
                buffer[6] = '%';
                buffer[9] = '%';

                buffer[11] = HEX_CHARACTERS[codePoint & 0xF];

                codePoint >>>= 4;
                buffer[10] = HEX_CHARACTERS[0x8 | (codePoint & 0x3)];

                codePoint >>>= 2;
                buffer[8] = HEX_CHARACTERS[codePoint & 0xF];

                codePoint >>>= 4;
                buffer[7] = HEX_CHARACTERS[0x8 | (codePoint & 0x3)];

                codePoint >>>= 2;
                buffer[5] = HEX_CHARACTERS[codePoint & 0xF];

                codePoint >>>= 4;
                buffer[4] = HEX_CHARACTERS[0x8 | (codePoint & 0x3)];

                codePoint >>>= 2;
                buffer[2] = HEX_CHARACTERS[codePoint & 0x7];

                escapedBuilder.append(buffer);
            }
        }

        if (escapedBuilder == null) {
            return original;
        }

        if (last < end) {
            escapedBuilder.append(original, last, end);
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
