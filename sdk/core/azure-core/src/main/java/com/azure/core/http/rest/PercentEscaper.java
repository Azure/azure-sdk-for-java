// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * An escaper that escapes URL data through percent encoding.
 */
final class PercentEscaper {

    private static final String[] HEX = {
        "%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07",
        "%08", "%09", "%0a", "%0b", "%0c", "%0d", "%0e", "%0f",
        "%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17",
        "%18", "%19", "%1a", "%1b", "%1c", "%1d", "%1e", "%1f",
        "%20", "%21", "%22", "%23", "%24", "%25", "%26", "%27",
        "%28", "%29", "%2a", "%2b", "%2c", "%2d", "%2e", "%2f",
        "%30", "%31", "%32", "%33", "%34", "%35", "%36", "%37",
        "%38", "%39", "%3a", "%3b", "%3c", "%3d", "%3e", "%3f",
        "%40", "%41", "%42", "%43", "%44", "%45", "%46", "%47",
        "%48", "%49", "%4a", "%4b", "%4c", "%4d", "%4e", "%4f",
        "%50", "%51", "%52", "%53", "%54", "%55", "%56", "%57",
        "%58", "%59", "%5a", "%5b", "%5c", "%5d", "%5e", "%5f",
        "%60", "%61", "%62", "%63", "%64", "%65", "%66", "%67",
        "%68", "%69", "%6a", "%6b", "%6c", "%6d", "%6e", "%6f",
        "%70", "%71", "%72", "%73", "%74", "%75", "%76", "%77",
        "%78", "%79", "%7a", "%7b", "%7c", "%7d", "%7e", "%7f",
        "%80", "%81", "%82", "%83", "%84", "%85", "%86", "%87",
        "%88", "%89", "%8a", "%8b", "%8c", "%8d", "%8e", "%8f",
        "%90", "%91", "%92", "%93", "%94", "%95", "%96", "%97",
        "%98", "%99", "%9a", "%9b", "%9c", "%9d", "%9e", "%9f",
        "%a0", "%a1", "%a2", "%a3", "%a4", "%a5", "%a6", "%a7",
        "%a8", "%a9", "%aa", "%ab", "%ac", "%ad", "%ae", "%af",
        "%b0", "%b1", "%b2", "%b3", "%b4", "%b5", "%b6", "%b7",
        "%b8", "%b9", "%ba", "%bb", "%bc", "%bd", "%be", "%bf",
        "%c0", "%c1", "%c2", "%c3", "%c4", "%c5", "%c6", "%c7",
        "%c8", "%c9", "%ca", "%cb", "%cc", "%cd", "%ce", "%cf",
        "%d0", "%d1", "%d2", "%d3", "%d4", "%d5", "%d6", "%d7",
        "%d8", "%d9", "%da", "%db", "%dc", "%dd", "%de", "%df",
        "%e0", "%e1", "%e2", "%e3", "%e4", "%e5", "%e6", "%e7",
        "%e8", "%e9", "%ea", "%eb", "%ec", "%ed", "%ee", "%ef",
        "%f0", "%f1", "%f2", "%f3", "%f4", "%f5", "%f6", "%f7",
        "%f8", "%f9", "%fa", "%fb", "%fc", "%fd", "%fe", "%ff"
    };

    private final boolean usePlusForSpace;

    private final List<Character> safeChars = new ArrayList<>();

    /**
     * Creates a percent escaper.
     * @param safeChars a collection of characters that will not be escaped
     * @param usePlusForSpace escape ' ' as '+' if true, "%20" otherwise
     */
    PercentEscaper(String safeChars, boolean usePlusForSpace) {
        for (int i = 0; i != safeChars.length(); i++) {
            this.safeChars.add(safeChars.charAt(i));
        }
        this.usePlusForSpace = usePlusForSpace;
    }

    /**
     * Creates a percent escaper with default settings and encode ' ' as "%20".
     */
    PercentEscaper() {
        this("-._~", false);
    }

    /**
     * Escapes a string with the current settings on the escaper.
     * @param original the origin string to escape
     * @return the escaped string
     */
    public String escape(String original) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i != utf16ToAscii(original).length(); i++) {
            char c = original.charAt(i);
            if (c == ' ') {
                output.append(usePlusForSpace ? "+" : HEX[' ']);
            } else if (c >= 'a' && c <= 'z') {
                output.append(c);
            } else if (c >= 'A' && c <= 'Z') {
                output.append(c);
            } else if (c >= '0' && c <= '9') {
                output.append(c);
            } else if (safeChars.contains(c)) {
                output.append(c);
            } else {
                output.append(HEX[c]);
            }
        }
        return output.toString();
    }

    private String utf16ToAscii(String input) {
        byte[] ascii = new byte[input.length()];
        for (int i = 0; i < input.length(); i++) {
            ascii[i] = (byte) input.charAt(i);
        }
        return new String(ascii, StandardCharsets.UTF_8);
    }
}
