/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

class UrlTokenizer {
    private final String text;
    private final int textLength;
    private State state;
    private int currentIndex;
    private UrlToken currentToken;

    UrlTokenizer(String text) {
        this(text, State.SCHEME_OR_HOST);
    }

    UrlTokenizer(String text, State state) {
        this.text = text;
        this.textLength = (text == null ? 0 : text.length());
        this.state = state;
        this.currentIndex = 0;
        this.currentToken = null;
    }

    private boolean hasCurrentCharacter() {
        return currentIndex < textLength;
    }

    private char currentCharacter() {
        return text.charAt(currentIndex);
    }

    private void nextCharacter() {
        nextCharacter(1);
    }

    private void nextCharacter(int step) {
        if (hasCurrentCharacter()) {
            currentIndex += step;
        }
    }

    private String peekCharacters(int charactersToPeek) {
        int endIndex = currentIndex + charactersToPeek;
        if (textLength < endIndex) {
            endIndex = textLength;
        }
        return text.substring(currentIndex, endIndex);
    }

    UrlToken current() {
        return currentToken;
    }

    boolean next() {
        if (!hasCurrentCharacter()) {
            currentToken = null;
        }
        else {
            switch (state) {
                case SCHEME:
                    final String scheme = readUntilNotLetterOrDigit();
                    currentToken = UrlToken.scheme(scheme);
                    if (!hasCurrentCharacter()) {
                        state = State.DONE;
                    }
                    else {
                        state = State.HOST;
                    }
                    break;

                case SCHEME_OR_HOST:
                    final String schemeOrHost = readUntilCharacter(':', '/', '?');
                    if (!hasCurrentCharacter()) {
                        currentToken = UrlToken.host(schemeOrHost);
                        state = State.DONE;
                    }
                    else if (currentCharacter() == ':') {
                        if (peekCharacters(3).equals("://")) {
                            currentToken = UrlToken.scheme(schemeOrHost);
                            state = State.HOST;
                        }
                        else {
                            currentToken = UrlToken.host(schemeOrHost);
                            state = State.PORT;
                        }
                    }
                    else if (currentCharacter() == '/') {
                        currentToken = UrlToken.host(schemeOrHost);
                        state = State.PATH;
                    }
                    else if (currentCharacter() == '?') {
                        currentToken = UrlToken.host(schemeOrHost);
                        state = State.QUERY;
                    }
                    break;

                case HOST:
                    if (peekCharacters(3).equals("://")) {
                        nextCharacter(3);
                    }

                    final String host = readUntilCharacter(':', '/', '?');
                    currentToken = UrlToken.host(host);

                    if (!hasCurrentCharacter()) {
                        state = State.DONE;
                    }
                    else if (currentCharacter() == ':') {
                        state = State.PORT;
                    }
                    else if (currentCharacter() == '/') {
                        state = State.PATH;
                    }
                    else {
                        state = State.QUERY;
                    }
                    break;

                case PORT:
                    if (currentCharacter() == ':') {
                        nextCharacter();
                    }

                    final String port = readUntilCharacter('/', '?');
                    currentToken = UrlToken.port(port);

                    if (!hasCurrentCharacter()) {
                        state = State.DONE;
                    }
                    else if (currentCharacter() == '/') {
                        state = State.PATH;
                    }
                    else {
                        state = State.QUERY;
                    }
                    break;

                case PATH:
                    final String path = readUntilCharacter('?');
                    currentToken = UrlToken.path(path);

                    if (!hasCurrentCharacter()) {
                        state = State.DONE;
                    }
                    else {
                        state = State.QUERY;
                    }
                    break;

                case QUERY:
                    if (currentCharacter() == '?') {
                        nextCharacter();
                    }

                    final String query = readRemaining();
                    currentToken = UrlToken.query(query);
                    state = State.DONE;
                    break;

                default:
                    break;
            }
        }

        return currentToken != null;
    }

    private String readUntilNotLetterOrDigit() {
        String result = "";

        if (hasCurrentCharacter()) {
            final StringBuilder builder = new StringBuilder();
            while (hasCurrentCharacter()) {
                final char currentCharacter = currentCharacter();
                if (!Character.isLetterOrDigit(currentCharacter)) {
                    break;
                }
                else {
                    builder.append(currentCharacter);
                    nextCharacter();
                }
            }
            result = builder.toString();
        }

        return result;
    }

    private String readUntilCharacter(char... terminatingCharacters) {
        String result = "";

        if (hasCurrentCharacter()) {
            final StringBuilder builder = new StringBuilder();
            boolean foundTerminator = false;
            while (hasCurrentCharacter()) {
                final char currentCharacter = currentCharacter();

                for (final char terminatingCharacter : terminatingCharacters) {
                    if (currentCharacter == terminatingCharacter) {
                        foundTerminator = true;
                        break;
                    }
                }

                if (foundTerminator) {
                    break;
                }
                else {
                    builder.append(currentCharacter);
                    nextCharacter();
                }
            }
            result = builder.toString();
        }

        return result;
    }

    private String readRemaining() {
        String result = "";
        if (currentIndex < textLength) {
            result = text.substring(currentIndex, textLength);
            currentIndex = textLength;
        }
        return result;
    }

    enum State {
        SCHEME,

        SCHEME_OR_HOST,

        HOST,

        PORT,

        PATH,

        QUERY,

        DONE
    }
}
