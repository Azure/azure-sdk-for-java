// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

/**
 * Escapes Java strings as the contents of JSON string literals.
 * <p>
 * Unlike UTF-8 based encoders, unpaired UTF-16 surrogates are escaped as {@code \}{@code uXXXX} rather than rejected,
 * allowing any string read from valid JSON to be written back as valid JSON.
 */
final class JsonStringEscaper {
    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    private JsonStringEscaper() {
    }

    /**
     * Escapes the value, without surrounding quotes.
     *
     * @param value The value to escape.
     * @return The escaped value.
     */
    static String escape(String value) {
        StringBuilder builder = null;
        int length = value.length();
        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);
            String replacement = null;
            boolean escapeUnicode = false;
            if (c == '"') {
                replacement = "\\\"";
            } else if (c == '\\') {
                replacement = "\\\\";
            } else if (c == '\b') {
                replacement = "\\b";
            } else if (c == '\f') {
                replacement = "\\f";
            } else if (c == '\n') {
                replacement = "\\n";
            } else if (c == '\r') {
                replacement = "\\r";
            } else if (c == '\t') {
                replacement = "\\t";
            } else if (c < 0x20) {
                escapeUnicode = true;
            } else if (Character.isHighSurrogate(c)) {
                if (i + 1 < length && Character.isLowSurrogate(value.charAt(i + 1))) {
                    if (builder != null) {
                        builder.append(c).append(value.charAt(i + 1));
                    }
                    i++;
                    continue;
                }
                escapeUnicode = true;
            } else if (Character.isLowSurrogate(c)) {
                escapeUnicode = true;
            }

            if (replacement == null && !escapeUnicode) {
                if (builder != null) {
                    builder.append(c);
                }
                continue;
            }

            if (builder == null) {
                builder = new StringBuilder(length + 16).append(value, 0, i);
            }

            if (replacement != null) {
                builder.append(replacement);
            } else {
                builder.append("\\u")
                    .append(HEX[(c >> 12) & 0xF])
                    .append(HEX[(c >> 8) & 0xF])
                    .append(HEX[(c >> 4) & 0xF])
                    .append(HEX[c & 0xF]);
            }
        }

        return builder == null ? value : builder.toString();
    }
}
