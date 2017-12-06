/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

class UrlToken {
    private final String text;
    private final Type type;

    UrlToken(String text, Type type) {
        this.text = text;
        this.type = type;
    }

    String text() {
        return text;
    }

    Type type() {
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
        return new UrlToken(text, Type.SCHEME);
    }

    static UrlToken host(String text) {
        return new UrlToken(text, Type.HOST);
    }

    static UrlToken port(String text) {
        return new UrlToken(text, Type.PORT);
    }

    static UrlToken path(String text) {
        return new UrlToken(text, Type.PATH);
    }

    static UrlToken query(String text) {
        return new UrlToken(text, Type.QUERY);
    }

    enum Type {
        SCHEME,

        HOST,

        PORT,

        PATH,

        QUERY,
    }
}
