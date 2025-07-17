// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.utils;

import java.util.Objects;

/**
 * Represents a token during {@link java.net.URI} parsing.
 */
public final class UriToken {
    private final String text;
    private final UriTokenType type;

    /**
     * Creates a new UriToken object with the specified text and type.
     *
     * @param text The text of the token.
     * @param type The type of the token.
     */
    public UriToken(String text, UriTokenType type) {
        this.text = text;
        this.type = type;
    }

    /**
     * Gets the text of the token.
     *
     * @return The text of the token.
     */
    public String text() {
        return text;
    }

    /**
     * Gets the type of the token.
     *
     * @return The type of the token.
     */
    public UriTokenType type() {
        return type;
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == this) {
            return true;
        }

        if (!(rhs instanceof UriToken)) {
            return false;
        }

        UriToken other = (UriToken) rhs;
        return type == other.type && Objects.equals(text, other.text);
    }

    @Override
    public String toString() {
        return "\"" + text + "\" (" + type + ")";
    }

    @Override
    public int hashCode() {
        return (text == null ? 0 : text.hashCode()) ^ type.hashCode();
    }

    static UriToken scheme(String text) {
        return new UriToken(text, UriTokenType.SCHEME);
    }

    static UriToken host(String text) {
        return new UriToken(text, UriTokenType.HOST);
    }

    static UriToken port(String text) {
        return new UriToken(text, UriTokenType.PORT);
    }

    static UriToken path(String text) {
        return new UriToken(text, UriTokenType.PATH);
    }

    static UriToken query(String text) {
        return new UriToken(text, UriTokenType.QUERY);
    }
}
