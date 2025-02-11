// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package io.clientcore.core.serialization.json.implementation.jackson.core.io;

import java.util.Arrays;

@SuppressWarnings("cast")
public final class CharTypes {
    private final static char[] HC = "0123456789ABCDEF".toCharArray();
    private final static byte[] HB;
    static {
        int len = HC.length;
        HB = new byte[len];
        for (int i = 0; i < len; ++i) {
            HB[i] = (byte) HC[i];
        }
    }

    /**
     * Lookup table used for determining which input characters
     * need special handling when contained in text segment.
     */
    private final static int[] sInputCodes;
    static {
        /*
         * 96 would do for most cases (backslash is ASCII 94)
         * but if we want to do lookups by raw bytes it's better
         * to have full table
         */
        final int[] table = new int[256];
        // Control chars and non-space white space are not allowed unquoted
        for (int i = 0; i < 32; ++i) {
            table[i] = -1;
        }
        // And then string end and quote markers are special too
        table['"'] = 1;
        table['\\'] = 1;
        sInputCodes = table;
    }

    /**
     * Lookup table used for determining which output characters in
     * 7-bit ASCII range need to be quoted.
     */
    private final static int[] sOutputEscapes128;
    static {
        int[] table = new int[128];
        // Control chars need generic escape sequence
        for (int i = 0; i < 32; ++i) {
            // 04-Mar-2011, tatu: Used to use "-(i + 1)", replaced with constant
            table[i] = -1;
        }
        // Others (and some within that range too) have explicit shorter sequences
        table['"'] = '"';
        table['\\'] = '\\';
        // Escaping of slash is optional, so let's not add it
        table[0x08] = 'b';
        table[0x09] = 't';
        table[0x0C] = 'f';
        table[0x0A] = 'n';
        table[0x0D] = 'r';
        sOutputEscapes128 = table;
    }

    /**
     * Lookup table for the first 256 Unicode characters (ASCII / UTF-8)
     * range. For actual hex digits, contains corresponding value;
     * for others -1.
     *<p>
     * NOTE: before 2.10.1, was of size 128, extended for simpler handling
     */
    private final static int[] sHexValues = new int[256];
    static {
        Arrays.fill(sHexValues, -1);
        for (int i = 0; i < 10; ++i) {
            sHexValues['0' + i] = i;
        }
        for (int i = 0; i < 6; ++i) {
            sHexValues['a' + i] = 10 + i;
            sHexValues['A' + i] = 10 + i;
        }
    }

    public static int[] getInputCodeLatin1() {
        return sInputCodes;
    }

    /**
     * Accessor for getting a read-only encoding table for first 128 Unicode
     * code points (single-byte UTF-8 characters).
     * Value of 0 means "no escaping"; other positive values that value is character
     * to use after backslash; and negative values that generic (backslash - u)
     * escaping is to be used.
     *
     * @return 128-entry {@code int[]} that contains escape definitions
     */
    public static int[] get7BitOutputEscapes() {
        return sOutputEscapes128;
    }

    public static int charToHex(int ch) {
        // 08-Nov-2019, tatu: As per [core#540] and [core#578], changed to
        // force masking here so caller need not do that.
        return sHexValues[ch & 0xFF];
    }

    // @since 2.13
    public static char hexToChar(int ch) {
        return HC[ch];
    }

    /**
     * Helper method for appending JSON-escaped version of contents
     * into specific {@link StringBuilder}, using default JSON specification
     * mandated minimum escaping rules.
     *
     * @param sb Buffer to append escaped contents in
     *
     * @param content Unescaped String value to append with escaping applied
     */
    public static void appendQuoted(StringBuilder sb, String content) {
        final int[] escCodes = sOutputEscapes128;
        int escLen = escCodes.length;
        for (int i = 0, len = content.length(); i < len; ++i) {
            char c = content.charAt(i);
            if (c >= escLen || escCodes[c] == 0) {
                sb.append(c);
                continue;
            }
            sb.append('\\');
            int escCode = escCodes[c];
            if (escCode < 0) { // generic quoting (hex value)
                // The only negative value sOutputEscapes128 returns
                // is CharacterEscapes.ESCAPE_STANDARD, which mean
                // appendQuotes should encode using the Unicode encoding;
                // not sure if this is the right way to encode for
                // CharacterEscapes.ESCAPE_CUSTOM or other (future)
                // CharacterEscapes.ESCAPE_XXX values.

                // We know that it has to fit in just 2 hex chars
                sb.append('u');
                sb.append('0');
                sb.append('0');
                // widening
                sb.append(HC[(int) c >> 4]);
                sb.append(HC[(int) c & 0xF]);
            } else { // "named", i.e. prepend with slash
                sb.append((char) escCode);
            }
        }
    }

    public static char[] copyHexChars() {
        return HC.clone();
    }

    public static byte[] copyHexBytes() {
        return HB.clone();
    }
}
