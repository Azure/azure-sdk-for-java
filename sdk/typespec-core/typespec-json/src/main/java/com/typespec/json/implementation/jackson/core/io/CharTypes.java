// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.io;

import java.util.Arrays;

@SuppressWarnings("cast")
public final class CharTypes
{
    protected final static char[] HC = "0123456789ABCDEF".toCharArray();
    protected final static byte[] HB;
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
    protected final static int[] sInputCodes;
    static {
        /* 96 would do for most cases (backslash is ASCII 94)
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
     * Additionally we can combine UTF-8 decoding info into similar
     * data table.
     */
    protected final static int[] sInputCodesUTF8;
    static {
        final int[] table = new int[sInputCodes.length];
        System.arraycopy(sInputCodes, 0, table, 0, table.length);
        for (int c = 128; c < 256; ++c) {
            int code;

            // We'll add number of bytes needed for decoding
            if ((c & 0xE0) == 0xC0) { // 2 bytes (0x0080 - 0x07FF)
                code = 2;
            } else if ((c & 0xF0) == 0xE0) { // 3 bytes (0x0800 - 0xFFFF)
                code = 3;
            } else if ((c & 0xF8) == 0xF0) {
                // 4 bytes; double-char with surrogates and all...
                code = 4;
            } else {
                // And -1 seems like a good "universal" error marker...
                code = -1;
            }
            table[c] = code;
        }
        sInputCodesUTF8 = table;
    }

    /**
     * To support non-default (and -standard) unquoted field names mode,
     * need to have alternate checking.
     * Basically this is list of 8-bit ASCII characters that are legal
     * as part of Javascript identifier
     */
    protected final static int[] sInputCodesJsNames;
    static {
        final int[] table = new int[256];
        // Default is "not a name char", mark ones that are
        Arrays.fill(table, -1);
        // Assume rules with JS same as Java (change if/as needed)
        for (int i = 33; i < 256; ++i) {
            if (Character.isJavaIdentifierPart((char) i)) {
                table[i] = 0;
            }
        }
        /* As per [JACKSON-267], '@', '#' and '*' are also to be accepted as well.
         * And '-' (for hyphenated names); and '+' for sake of symmetricity...
         */
        table['@'] = 0;
        table['#'] = 0;
        table['*'] = 0;
        table['-'] = 0;
        table['+'] = 0;
        sInputCodesJsNames = table;
    }

    /**
     * This table is similar to Latin-1, except that it marks all "high-bit"
     * code as ok. They will be validated at a later point, when decoding
     * name
     */
    protected final static int[] sInputCodesUtf8JsNames;
    static {
        final int[] table = new int[256];
        // start with 8-bit JS names
        System.arraycopy(sInputCodesJsNames, 0, table, 0, table.length);
        Arrays.fill(table, 128, 128, 0);
        sInputCodesUtf8JsNames = table;
    }

    /**
     * Decoding table used to quickly determine characters that are
     * relevant within comment content.
     */
    protected final static int[] sInputCodesComment;
    static {
        final int[] buf = new int[256];
        // but first: let's start with UTF-8 multi-byte markers:
        System.arraycopy(sInputCodesUTF8, 128, buf, 128, 128);

        // default (0) means "ok" (skip); -1 invalid, others marked by char itself
        Arrays.fill(buf, 0, 32, -1); // invalid white space
        buf['\t'] = 0; // tab is still fine
        buf['\n'] = '\n'; // lf/cr need to be observed, ends cpp comment
        buf['\r'] = '\r';
        buf['*'] = '*'; // end marker for c-style comments
        sInputCodesComment = buf;
    }

    /**
     * Decoding table used for skipping white space and comments.
     *
     * @since 2.3
     */
    protected final static int[] sInputCodesWS;
    static {
        // but first: let's start with UTF-8 multi-byte markers:
        final int[] buf = new int[256];
        System.arraycopy(sInputCodesUTF8, 128, buf, 128, 128);

        // default (0) means "not whitespace" (end); 1 "whitespace", -1 invalid,
        // 2-4 UTF-8 multi-bytes, others marked by char itself
        //
        Arrays.fill(buf, 0, 32, -1); // invalid white space
        buf[' '] = 1;
        buf['\t'] = 1;
        buf['\n'] = '\n'; // lf/cr need to be observed, ends cpp comment
        buf['\r'] = '\r';
        buf['/'] = '/'; // start marker for c/cpp comments
        buf['#'] = '#'; // start marker for YAML comments
        sInputCodesWS = buf;
    }

    /**
     * Lookup table used for determining which output characters in
     * 7-bit ASCII range need to be quoted.
     */
    protected final static int[] sOutputEscapes128;
    static {
        int[] table = new int[128];
        // Control chars need generic escape sequence
        for (int i = 0; i < 32; ++i) {
            // 04-Mar-2011, tatu: Used to use "-(i + 1)", replaced with constant
            table[i] = CharacterEscapes.ESCAPE_STANDARD;
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
    protected final static int[] sHexValues = new int[256];
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

    public static int[] getInputCodeLatin1() { return sInputCodes; }
    public static int[] getInputCodeUtf8() { return sInputCodesUTF8; }

    public static int[] getInputCodeLatin1JsNames() { return sInputCodesJsNames; }
    public static int[] getInputCodeUtf8JsNames() { return sInputCodesUtf8JsNames; }

    public static int[] getInputCodeComment() { return sInputCodesComment; }
    public static int[] getInputCodeWS() { return sInputCodesWS; }

    /**
     * Accessor for getting a read-only encoding table for first 128 Unicode
     * code points (single-byte UTF-8 characters).
     * Value of 0 means "no escaping"; other positive values that value is character
     * to use after backslash; and negative values that generic (backslash - u)
     * escaping is to be used.
     *
     * @return 128-entry {@code int[]} that contains escape definitions
     */
    public static int[] get7BitOutputEscapes() { return sOutputEscapes128; }

    /**
     * Alternative to {@link #get7BitOutputEscapes()} when a non-standard quote character
     * is used.
     *
     * @param quoteChar Character used for quoting textual values and property names;
     *    usually double-quote but sometimes changed to single-quote (apostrophe)
     *
     * @return 128-entry {@code int[]} that contains escape definitions
     *
     * @since 2.10
     */
    public static int[] get7BitOutputEscapes(int quoteChar) {
        if (quoteChar == '"') {
            return sOutputEscapes128;
        }
        return AltEscapes.instance.escapesFor(quoteChar);
    }

    public static int charToHex(int ch)
    {
        // 08-Nov-2019, tatu: As per [core#540] and [core#578], changed to
        //   force masking here so caller need not do that.
        return sHexValues[ch & 0xFF];
    }

    // @since 2.13
    public static char hexToChar(int ch)
    {
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
    public static void appendQuoted(StringBuilder sb, String content)
    {
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
                int value = c;  // widening
                sb.append(HC[value >> 4]);
                sb.append(HC[value & 0xF]);
            } else { // "named", i.e. prepend with slash
                sb.append((char) escCode);
            }
        }
    }

    public static char[] copyHexChars() {
        return (char[]) HC.clone();
    }

    public static byte[] copyHexBytes() {
        return (byte[]) HB.clone();
    }

    /**
     * Helper used for lazy initialization of alternative escape (quoting)
     * table, used for escaping content that uses non-standard quote
     * character (usually apostrophe).
     *
     * @since 2.10
     */
    private static class AltEscapes {
        public final static AltEscapes instance = new AltEscapes();

        private int[][] _altEscapes = new int[128][];

        public int[] escapesFor(int quoteChar) {
            int[] esc = _altEscapes[quoteChar];
            if (esc == null) {
                esc = Arrays.copyOf(sOutputEscapes128, 128);
                // Only add escape setting if character does not already have it
                if (esc[quoteChar] == 0) {
                    esc[quoteChar] = CharacterEscapes.ESCAPE_STANDARD;
                }
                _altEscapes[quoteChar] = esc;
            }
            return esc;
        }
    }
}

