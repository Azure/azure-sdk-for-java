// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

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
        nextCharacter(1);
    }

    private void nextCharacter(int step) {
        if (hasCurrentCharacter()) {
            currentIndex += step;
        }
    }

    /*
     * Checks if the next range of characters matches the scheme-host separator (://)
     */
    private boolean peekMatchesSchemeSeparator() {
        return "://".regionMatches(0, text, currentIndex, 3);
    }

    UrlToken current() {
        return currentToken;
    }

    boolean next() {
        if (!hasCurrentCharacter()) {
            currentToken = null;
        } else {
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
                    final String schemeOrHost = readUntilCharacter(':', '/', '?');
                    if (!hasCurrentCharacter()) {
                        currentToken = UrlToken.host(schemeOrHost);
                        state = UrlTokenizerState.DONE;
                    } else if (currentCharacter() == ':') {
                        if (peekMatchesSchemeSeparator()) {
                            currentToken = UrlToken.scheme(schemeOrHost);
                            state = UrlTokenizerState.HOST;
                        } else {
                            currentToken = UrlToken.host(schemeOrHost);
                            state = UrlTokenizerState.PORT;
                        }
                    } else if (currentCharacter() == '/') {
                        currentToken = UrlToken.host(schemeOrHost);
                        state = UrlTokenizerState.PATH;
                    } else if (currentCharacter() == '?') {
                        currentToken = UrlToken.host(schemeOrHost);
                        state = UrlTokenizerState.QUERY;
                    }
                    break;

                case HOST:
                    if (peekMatchesSchemeSeparator()) {
                        nextCharacter(3);
                    }

                    final String host = readUntilCharacter(':', '/', '?');
                    currentToken = UrlToken.host(host);

                    if (!hasCurrentCharacter()) {
                        state = UrlTokenizerState.DONE;
                    } else if (currentCharacter() == ':') {
                        state = UrlTokenizerState.PORT;
                    } else if (currentCharacter() == '/') {
                        state = UrlTokenizerState.PATH;
                    } else {
                        state = UrlTokenizerState.QUERY;
                    }
                    break;

                case PORT:
                    if (currentCharacter() == ':') {
                        nextCharacter();
                    }

                    final String port = readUntilCharacter('/', '?');
                    currentToken = UrlToken.port(port);

                    if (!hasCurrentCharacter()) {
                        state = UrlTokenizerState.DONE;
                    } else if (currentCharacter() == '/') {
                        state = UrlTokenizerState.PATH;
                    } else {
                        state = UrlTokenizerState.QUERY;
                    }
                    break;

                case PATH:
                    final String path = readUntilCharacter('?');
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

    private String readUntilCharacter(char... terminatingCharacters) {
        if (!hasCurrentCharacter()) {
            return "";
        }

        int start = currentIndex;

        while (hasCurrentCharacter()) {
            char currentCharacter = currentCharacter();
            for (char terminatingCharacter : terminatingCharacters) {
                if (currentCharacter == terminatingCharacter) {
                    return text.substring(start, currentIndex);
                }
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
