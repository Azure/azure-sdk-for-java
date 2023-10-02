// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util;

class UrlTokenizer {
    private final String text;
    private final int textLength;
    private UrlTokenizerState state;
    private int currentIndex;
    private UrlToken currentToken;

    UrlTokenizer(String text) {
        this(text, UrlTokenizerState.SCHEME_OR_HOST);
    }

    UrlTokenizer(String text, UrlTokenizerState state) {
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
        if (hasCurrentCharacter()) {
            currentIndex += 1;
        }
    }

    /*
     * Checks if the next range of characters matches the scheme-host separator (://)
     */
    private boolean peekMatchesSchemeSeparator(boolean step) {
        if ("://".regionMatches(0, text, currentIndex, 3)) {
            if (step) {
                currentIndex += 3;
            }
            return true;
        }

        return false;
    }

    UrlToken current() {
        return currentToken;
    }

    boolean next() {
        if (!hasCurrentCharacter()) {
            currentToken = null;
        } else {
            char c;
            switch (state) {
                case SCHEME:
                    final String scheme = readUntilNotLetterOrDigit();
                    currentToken = UrlToken.scheme(scheme);
                    if (!hasCurrentCharacter()) {
                        state = UrlTokenizerState.DONE;
                    } else {
                        state = UrlTokenizerState.HOST;
                    }
                    break;

                case SCHEME_OR_HOST:
                    final String schemeOrHost = readUntil(true);
                    if (!hasCurrentCharacter()) {
                        currentToken = UrlToken.host(schemeOrHost);
                        state = UrlTokenizerState.DONE;
                        break;
                    }

                    c = currentCharacter();
                    if (c == ':') {
                        if (peekMatchesSchemeSeparator(false)) {
                            currentToken = UrlToken.scheme(schemeOrHost);
                            state = UrlTokenizerState.HOST;
                        } else {
                            currentToken = UrlToken.host(schemeOrHost);
                            state = UrlTokenizerState.PORT;
                        }
                    } else if (c == '/') {
                        currentToken = UrlToken.host(schemeOrHost);
                        state = UrlTokenizerState.PATH;
                    } else if (c == '?') {
                        currentToken = UrlToken.host(schemeOrHost);
                        state = UrlTokenizerState.QUERY;
                    }
                    break;

                case HOST:
                    peekMatchesSchemeSeparator(true);

                    final String host = readUntil(true);
                    currentToken = UrlToken.host(host);

                    if (!hasCurrentCharacter()) {
                        state = UrlTokenizerState.DONE;
                        break;
                    }

                    c = currentCharacter();
                    if (c == ':') {
                        state = UrlTokenizerState.PORT;
                    } else if (c == '/') {
                        state = UrlTokenizerState.PATH;
                    } else {
                        state = UrlTokenizerState.QUERY;
                    }
                    break;

                case PORT:
                    c = currentCharacter();
                    if (c == ':') {
                        nextCharacter();
                    }

                    final String port = readUntil(false);
                    currentToken = UrlToken.port(port);

                    if (!hasCurrentCharacter()) {
                        state = UrlTokenizerState.DONE;
                        break;
                    }

                    if (currentCharacter() == '/') {
                        state = UrlTokenizerState.PATH;
                    } else {
                        state = UrlTokenizerState.QUERY;
                    }
                    break;

                case PATH:
                    int index = text.indexOf('?', currentIndex);
                    String path;
                    if (index == -1) {
                        path = text.substring(currentIndex);
                        currentIndex = textLength;
                    } else {
                        path = text.substring(currentIndex, index);
                        currentIndex = index;
                    }

                    currentToken = UrlToken.path(path);

                    if (!hasCurrentCharacter()) {
                        state = UrlTokenizerState.DONE;
                    } else {
                        state = UrlTokenizerState.QUERY;
                    }
                    break;

                case QUERY:
                    if (currentCharacter() == '?') {
                        nextCharacter();
                    }

                    final String query = readRemaining();
                    currentToken = UrlToken.query(query);
                    state = UrlTokenizerState.DONE;
                    break;

                default:
                    break;
            }
        }

        return currentToken != null;
    }

    private String readUntilNotLetterOrDigit() {
        if (!hasCurrentCharacter()) {
            return "";
        }

        int start = currentIndex;

        while (hasCurrentCharacter()) {
            final char currentCharacter = currentCharacter();
            if (!Character.isLetterOrDigit(currentCharacter)) {
                return text.substring(start, currentIndex);
            }

            nextCharacter();
        }

        return text.substring(start);
    }

    private String readUntil(boolean checkForColon) {
        if (!hasCurrentCharacter()) {
            return "";
        }

        int start = currentIndex;

        while (hasCurrentCharacter()) {
            char c = currentCharacter();
            if ((checkForColon && c == ':') || c == '/' || c == '?') {
                return text.substring(start, currentIndex);
            }

            nextCharacter();
        }

        return text.substring(start);
    }

    private String readRemaining() {
        String result = "";
        if (currentIndex < textLength) {
            result = text.substring(currentIndex, textLength);
            currentIndex = textLength;
        }
        return result;
    }
}
