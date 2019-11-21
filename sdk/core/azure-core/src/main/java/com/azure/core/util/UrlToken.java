// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

class UrlToken {
    private final String text;
    private final UrlTokenType type;

    UrlToken(String text, UrlTokenType type) {
        this.text = text;
        this.type = type;
    }

    String text() {
        return text;
    }

    UrlTokenType type() {
        return type;
    }

    @Override
    public boolean equals(Object rhs) {
        return rhs instanceof UrlToken && equals((UrlToken) rhs);
    }

    public boolean equals(UrlToken rhs) {
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

    static UrlToken scheme(String text) {
        return new UrlToken(text, UrlTokenType.SCHEME);
    }

    static UrlToken host(String text) {
        return new UrlToken(text, UrlTokenType.HOST);
    }

    static UrlToken port(String text) {
        return new UrlToken(text, UrlTokenType.PORT);
    }

    static UrlToken path(String text) {
        return new UrlToken(text, UrlTokenType.PATH);
    }

    static UrlToken query(String text) {
        return new UrlToken(text, UrlTokenType.QUERY);
    }
}
