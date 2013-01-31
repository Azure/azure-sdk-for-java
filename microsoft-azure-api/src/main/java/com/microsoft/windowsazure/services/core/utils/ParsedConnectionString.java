/**
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.services.core.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ParsedConnectionString {
    private String connectionString;
    private int currentIndex;

    public ParsedConnectionString(String connectionString) throws ConnectionStringSyntaxException {
        this.connectionString = connectionString;
        this.currentIndex = 0;

        matchConnectionString();
    }

    private void matchConnectionString() throws ConnectionStringSyntaxException {
        matchPair();
        while (charsLeft() && currentChar() == ';') {
            consumeChars(1);
            if (charsLeft()) {
                matchPair();
            }
        }
    }

    private char currentChar() {
        return connectionString.charAt(currentIndex);
    }

    private void consumeChars(int numChars) {
        currentIndex += numChars;
    }

    private boolean charsLeft() {
        return currentIndex < connectionString.length();
    }

    private void matchPair() throws ConnectionStringSyntaxException {
        String key = matchKey();
        matchEquals();
        String value = matchValue();
        saveValue(key, value);
    }

    private String matchKey() throws ConnectionStringSyntaxException {
        String key;
        if (isQuote(currentChar())) {
            key = matchQuoted();
        } else {
            key = matchUnquotedKey();
        }
        return key;
    }

    private String matchQuoted() throws ConnectionStringSyntaxException {
        char quote = currentChar();
        consumeChars(1);

        String value = "";
        while (charsLeft() && currentChar() != quote) {
            value = value + currentChar();
            consumeChars(1);
        }

        if (!charsLeft()) {
            throw new ConnectionStringSyntaxException(String.format("Unterminated quoted string '%1$s", value));
        }
        consumeChars(1);
        return value;
    }

    private String matchUnquotedKey() throws ConnectionStringSyntaxException {
        String key = "";

        while (charsLeft() && currentChar() != '=') {
            key += currentChar();
            consumeChars(1);
        }

        if (!charsLeft()) {
            throw new ConnectionStringSyntaxException(String.format("No value given for key %1$s", key));
        }
        return key;
    }

    private void matchEquals() throws ConnectionStringSyntaxException {
        if (charsLeft()) {
            if (currentChar() == '=') {
                consumeChars(1);
            } else {
                throw new ConnectionStringSyntaxException(String.format(
                        "Expected '=' character at position %1$d, but instead was '%2$c'",
                        this.currentIndex, connectionString.charAt(this.currentIndex)));
            }
        } else {
            throw new ConnectionStringSyntaxException(String.format(
                    "Expected '=' character at position %1$d but it was not found", currentIndex));
        }
    }

    private String matchValue() throws ConnectionStringSyntaxException {
        if (!charsLeft()) {
            throw new ConnectionStringSyntaxException("Expected value for key, not given");
        }
        String value;
        if (isQuote(currentChar())) {
            value = matchQuoted();
        } else {
            value = matchUnquotedValue();
        }
        return value;
    }

    private String matchUnquotedValue() {
        String value = "";

        while (charsLeft() && currentChar() != ';') {
            value += currentChar();
            consumeChars(1);
        }
        return value;
    }

    private boolean isQuote(char c) {
        return c == '"' || c == '\'';
    }

    private void saveValue(String key, String value) throws ConnectionStringSyntaxException {
        Class<?> thisClass = getClass();
        Method setter;
        try {
            setter = thisClass.getDeclaredMethod("set" + key, String.class);
            setter.invoke(this, value);
        } catch (NoSuchMethodException e) {
            throw new ConnectionStringSyntaxException(String.format(
                    "The key '%1$s' is not valid for this connection string", key), e);
        } catch (InvocationTargetException e) {
            throw new ConnectionStringSyntaxException(String.format(
                    "Could not invoke setter for key '%1$s'", key), e);
        } catch (IllegalAccessException e) {
            throw new ConnectionStringSyntaxException(String.format(
                    "Setter for key '%1$s' is not accessible in class %2$s", key, thisClass.getName()), e);
        }
    }
}
