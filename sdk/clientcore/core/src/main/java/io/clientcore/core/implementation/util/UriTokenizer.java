// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.util;

class UriTokenizer {
    private final String text;
    private final int textLength;
    private UriTokenizerState state;
    private int currentIndex;
    private UriToken currentToken;

    UriTokenizer(String text) {
        this(text, UriTokenizerState.SCHEME_OR_HOST);
    }

    UriTokenizer(String text, UriTokenizerState state) {
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

    UriToken current() {
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
                    currentToken = UriToken.scheme(scheme);
                    if (!hasCurrentCharacter()) {
                        state = UriTokenizerState.DONE;
                    } else {
                        state = UriTokenizerState.HOST;
                    }
                    break;

                case SCHEME_OR_HOST:
                    final String schemeOrHost = readUntil(true);
                    if (!hasCurrentCharacter()) {
                        currentToken = UriToken.host(schemeOrHost);
                        state = UriTokenizerState.DONE;
                        break;
                    }

                    c = currentCharacter();
                    if (c == ':') {
                        if (peekMatchesSchemeSeparator(false)) {
                            currentToken = UriToken.scheme(schemeOrHost);
                            state = UriTokenizerState.HOST;
                        } else {
                            currentToken = UriToken.host(schemeOrHost);
                            state = UriTokenizerState.PORT;
                        }
                    } else if (c == '/') {
                        currentToken = UriToken.host(schemeOrHost);
                        state = UriTokenizerState.PATH;
                    } else if (c == '?') {
                        currentToken = UriToken.host(schemeOrHost);
                        state = UriTokenizerState.QUERY;
                    }
                    break;

                case HOST:
                    peekMatchesSchemeSeparator(true);

                    final String host = readUntil(true);
                    currentToken = UriToken.host(host);

                    if (!hasCurrentCharacter()) {
                        state = UriTokenizerState.DONE;
                        break;
                    }

                    c = currentCharacter();
                    if (c == ':') {
                        state = UriTokenizerState.PORT;
                    } else if (c == '/') {
                        state = UriTokenizerState.PATH;
                    } else {
                        state = UriTokenizerState.QUERY;
                    }
                    break;

                case PORT:
                    c = currentCharacter();
                    if (c == ':') {
                        nextCharacter();
                    }

                    final String port = readUntil(false);
                    currentToken = UriToken.port(port);

                    if (!hasCurrentCharacter()) {
                        state = UriTokenizerState.DONE;
                        break;
                    }

                    if (currentCharacter() == '/') {
                        state = UriTokenizerState.PATH;
                    } else {
                        state = UriTokenizerState.QUERY;
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

                    currentToken = UriToken.path(path);

                    if (!hasCurrentCharacter()) {
                        state = UriTokenizerState.DONE;
                    } else {
                        state = UriTokenizerState.QUERY;
                    }
                    break;

                case QUERY:
                    if (currentCharacter() == '?') {
                        nextCharacter();
                    }

                    final String query = readRemaining();
                    currentToken = UriToken.query(query);
                    state = UriTokenizerState.DONE;
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
