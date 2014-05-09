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

package com.microsoft.windowsazure.core.utils;

import com.microsoft.windowsazure.core.pipeline.ConnectionStringField;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Base class representing an Azure connection string. This provides parsing
 * logic to split the string up into the various fields.
 */
public abstract class ParsedConnectionString {
    private String connectionString;
    private int currentIndex;
    private String currentKey;
    private String currentValue;

    public ParsedConnectionString(String connectionString)
            throws ConnectionStringSyntaxException {
        // Forcing a separator at the end makes implementation below
        // easier - don't have to check for end of string everywhere
        this.connectionString = connectionString + ';';
        this.currentIndex = 0;

        matchConnectionString();
    }

    private char currentChar() {
        return connectionString.charAt(currentIndex);
    }

    private void consumeChar() {
        currentIndex++;
    }

    private boolean charsLeft() {
        return currentIndex < connectionString.length();
    }

    //
    // PEG Grammar for a connection string:
    //
    // ConnectionString <- Attribute (Separator+ Attribute)* Separator* END
    // Attribute <- Spacing (KeyValuePair / &Separator)
    // KeyValuePair <- Key Equals Spacing Value
    // Key <- QuotedKey / RawKey
    // QuotedKey <- DoubleQuote (!DoubleQuote .)* DoubleQuote / SingleQuote
    // (!SingleQuote .)* SingleQuote
    // RawKey <- (!Equals .)+
    // Equals <- Spacing '='
    // Value <- QuotedValue / RawValue
    // QuotedValue <- DoubleQuote (!DoubleQuote .)* DoubleQuote / SingleQuote
    // (!SingleQuote .*) SingleQuote
    // RawValue <- (!Separator .)*
    // Separator <- Spacing ';'
    // Spacing <- WS*
    // WS <- ' ' / '\t'
    // END <- !.
    //

    private boolean matchConnectionString()
            throws ConnectionStringSyntaxException {
        if (!matchAttribute()) {
            throw new ConnectionStringSyntaxException(String.format(
                    "Could not parse connection string '%1$s'",
                    connectionString));
        }

        boolean moreAttributes = true;
        int bookmark;

        while (moreAttributes) {
            bookmark = currentIndex;
            boolean separators = false;
            while (matchSeparator()) {
                separators = true;
            }
            moreAttributes = separators && matchAttribute();
            if (!moreAttributes) {
                currentIndex = bookmark;
            }
        }

        while (matchSeparator()) { }

        if (!matchEND()) {
            throw new ConnectionStringSyntaxException(String.format(
                    "Expected end of connection string '%1$s', did not get it",
                    connectionString));
        }

        return true;
    }

    private boolean matchAttribute() throws ConnectionStringSyntaxException {
        matchSpacing();
        if (matchKeyValuePair()) {
            return true;
        }
        int bookmark = currentIndex;
        boolean matchedSeparator = matchSeparator();
        currentIndex = bookmark;
        return matchedSeparator;
    }

    private boolean matchKeyValuePair() throws ConnectionStringSyntaxException {
        if (!charsLeft()) {
            return false;
        }
        if (!matchKey()) {
            throw new ConnectionStringSyntaxException(
                    String.format(
                            "Expected key in connection string '%1$s' at position %2$d but did not find one",
                            connectionString, currentIndex));
        }
        if (!matchEquals()) {
            throw new ConnectionStringSyntaxException(
                    String.format(
                            "Expected '=' character in connection string '%1$s' after key near position %2$d",
                            connectionString, currentIndex));
        }
        if (!matchValue()) {
            throw new ConnectionStringSyntaxException(
                    String.format(
                            "Expected value in connection string '%1$s' for key '%3$s' at position %2$d but did not find one",
                            connectionString, currentIndex, currentKey));
        }

        saveValue(currentKey, currentValue);
        return true;
    }

    private boolean matchKey() throws ConnectionStringSyntaxException {
        return matchQuotedKey() || matchRawKey();
    }

    private boolean matchQuotedKey() throws ConnectionStringSyntaxException {
        if (!charsLeft()) {
            return false;
        }
        String value = "";
        char quote = currentChar();
        int bookmark = currentIndex;

        if (quote == '"' || quote == '\'') {
            consumeChar();
            while (charsLeft() && currentChar() != quote) {
                value += currentChar();
                consumeChar();
            }
            if (!charsLeft()) {
                throw new ConnectionStringSyntaxException(
                        String.format(
                                "Unterminated quoted value in string '%1$s', starting at character %2$d",
                                connectionString, bookmark));
            }
            consumeChar();
            currentKey = value;
            return true;
        } else {
            return false;
        }
    }

    private boolean matchRawKey() throws ConnectionStringSyntaxException {
        if (!charsLeft()) {
            return false;
        }

        String value = "";
        int bookmark = currentIndex;

        while (charsLeft() && !matchEquals()) {
            value += currentChar();
            consumeChar();
            bookmark = currentIndex;
        }

        if (!charsLeft()) {
            throw new ConnectionStringSyntaxException(
                    String.format(
                            "Expected '=' in connection string '%1$s', key started at index %2$d",
                            connectionString, bookmark));
        }

        currentIndex = bookmark;
        currentKey = value;
        return true;
    }

    private boolean matchEquals() {
        int bookmark = currentIndex;
        matchSpacing();
        if (charsLeft() && currentChar() == '=') {
            consumeChar();
            return true;
        }
        currentIndex = bookmark;
        return false;
    }

    private boolean matchValue() throws ConnectionStringSyntaxException {
        return matchQuotedValue() || matchRawValue();
    }

    private boolean matchQuotedValue() throws ConnectionStringSyntaxException {
        String value = "";
        char quote = currentChar();
        int bookmark = currentIndex;

        if (quote == '"' || quote == '\'') {
            consumeChar();
            while (charsLeft() && currentChar() != quote) {
                value += currentChar();
                consumeChar();
            }
            if (!charsLeft()) {
                throw new ConnectionStringSyntaxException(
                        String.format(
                                "Unterminated quoted value in string '%1$s', starting at character %2$d",
                                connectionString, bookmark));
            }
            consumeChar();
            currentValue = value;
            return true;
        } else {
            return false;
        }
    }

    private boolean matchRawValue() {
        String value = "";
        int bookmark = currentIndex;

        while (charsLeft() && !matchSeparator()) {
            value += currentChar();
            consumeChar();
            bookmark = currentIndex;
        }
        currentIndex = bookmark;
        currentValue = value;
        return true;
    }

    private boolean matchSeparator() {
        int bookmark = currentIndex;
        matchSpacing();
        if (charsLeft() && currentChar() == ';') {
            consumeChar();
            return true;
        }
        currentIndex = bookmark;
        return false;
    }

    private boolean matchSpacing() {
        while (matchWS()) { }

        return true;
    }

    private boolean matchWS() {
        if (charsLeft() && (currentChar() == ' ' || currentChar() == '\t')) {
            consumeChar();
            return true;
        }
        return false;
    }

    private boolean matchEND() {
        return !(currentIndex < connectionString.length());
    }

    /**
     * Store the value for the given key.
     * 
     * Default implementation looks for a property setter with the matching name
     * or the @ConnectionStringField annotation on the setter method to set the
     * field. You can override this method for other behavior.
     * 
     * @param key
     *            Key to store value under. If keys repeat, older values are
     *            overwritten.
     * @param value
     *            value to associate with the key.
     * 
     * @throws ConnectionStringSyntaxException
     */
    protected void saveValue(String key, String value)
            throws ConnectionStringSyntaxException {
        Method setter;
        try {
            setter = findSetter(key);
            setter.invoke(this, value);
        } catch (NoSuchMethodException e) {
            throw new ConnectionStringSyntaxException(String.format(
                    "The key '%1$s' is not valid for this connection string",
                    key), e);
        } catch (InvocationTargetException e) {
            throw new ConnectionStringSyntaxException(String.format(
                    "Could not invoke setter for key '%1$s'", key), e);
        } catch (IllegalAccessException e) {
            throw new ConnectionStringSyntaxException(String.format(
                    "Setter for key '%1$s' is not accessible in class %2$s",
                    key, getClass().getName()), e);
        }
    }

    private Method findSetter(String key) throws NoSuchMethodException {
        Class<?> thisClass = getClass();
        for (Method m : thisClass.getDeclaredMethods()) {
            if (methodMatches(m, key)) {
                return m;
            }
        }
        throw new NoSuchMethodException();
    }

    private boolean methodMatches(Method method, String key) {
        return matchesViaAnnotation(method, key) || matchesByName(method, key);
    }

    private boolean matchesViaAnnotation(Method method, String key) {
        ConnectionStringField annotation = method
                .getAnnotation(ConnectionStringField.class);
        return annotation != null
                && annotation.name().toLowerCase().equals(key.toLowerCase());
    }

    private boolean matchesByName(Method method, String key) {
        return method.getName().toLowerCase().equals("set" + key.toLowerCase());
    }
}
