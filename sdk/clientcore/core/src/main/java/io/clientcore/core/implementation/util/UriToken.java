// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.util;

class UriToken {
    private final String text;
    private final UriTokenType type;

    UriToken(String text, UriTokenType type) {
        this.text = text;
        this.type = type;
    }

    String text() {
        return text;
    }

    UriTokenType type() {
        return type;
    }

    @Override
    public boolean equals(Object rhs) {
        return rhs instanceof UriToken && equals((UriToken) rhs);
    }

    public boolean equals(UriToken rhs) {
        return rhs != null && text.equals(rhs.text) && type == rhs.type;
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
