// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.serializer.json.gson.implementation;

import com.azure.json.JsonOptions;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.MalformedJsonException;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Objects;

/**
 * This class is a copy of {@link JsonReader} with fine-grain control over lenient parsing.
 * <p>
 * By default, GSON has a single flag/enum for lenient parsing, where the behavior is all or nothing. The Azure SDK for
 * Java offers finer grain control for whether non-numeric numbers or JSON with comments is supported. This class
 * mutates the default GSON behaviors to control that.
 */
public final class CustomGsonReader extends JsonReader {
    private static final long MIN_INCOMPLETE_INTEGER = Long.MIN_VALUE / 10;

    // Unsupported peek states have been commented out.
    private static final int PEEKED_NONE = 0;
    private static final int PEEKED_BEGIN_OBJECT = 1;
    private static final int PEEKED_END_OBJECT = 2;
    private static final int PEEKED_BEGIN_ARRAY = 3;
    private static final int PEEKED_END_ARRAY = 4;
    private static final int PEEKED_TRUE = 5;
    private static final int PEEKED_FALSE = 6;
    private static final int PEEKED_NULL = 7;
    //    private static final int PEEKED_SINGLE_QUOTED = 8;
    private static final int PEEKED_DOUBLE_QUOTED = 9;
    //    private static final int PEEKED_UNQUOTED = 10;

    /** When this is returned, the string value is stored in peekedString. */
    private static final int PEEKED_BUFFERED = 11;

    //    private static final int PEEKED_SINGLE_QUOTED_NAME = 12;
    private static final int PEEKED_DOUBLE_QUOTED_NAME = 13;
    //    private static final int PEEKED_UNQUOTED_NAME = 14;

    /** When this is returned, the integer value is stored in peekedLong. */
    private static final int PEEKED_LONG = 15;

    private static final int PEEKED_NUMBER = 16;
    private static final int PEEKED_EOF = 17;

    /* State machine when parsing numbers */
    private static final int NUMBER_CHAR_NONE = 0;
    private static final int NUMBER_CHAR_SIGN = 1;
    private static final int NUMBER_CHAR_DIGIT = 2;
    private static final int NUMBER_CHAR_DECIMAL = 3;
    private static final int NUMBER_CHAR_FRACTION_DIGIT = 4;
    private static final int NUMBER_CHAR_EXP_E = 5;
    private static final int NUMBER_CHAR_EXP_SIGN = 6;
    private static final int NUMBER_CHAR_EXP_DIGIT = 7;

    /** The input JSON. */
    private final Reader in;

    private final boolean nonNumericNumbersSupported;
    private final boolean jsonCSupported;

    static final int BUFFER_SIZE = 1024;

    /**
     * Use a manual buffer to easily read and unread upcoming characters, and also so we can create
     * strings without an intermediate StringBuilder. We decode literals directly out of this buffer,
     * so it must be at least as long as the longest token that can be reported as a number.
     */
    private final char[] buffer = new char[BUFFER_SIZE];

    private int pos = 0;
    private int limit = 0;

    private int lineNumber = 0;
    private int lineStart = 0;

    int peeked = PEEKED_NONE;

    /**
     * A peeked value that was composed entirely of digits with an optional leading dash. Positive
     * values may not have a leading 0.
     */
    private long peekedLong;

    /**
     * The number of characters in a peeked number literal. Increment 'pos' by this after reading a
     * number.
     */
    private int peekedNumberLength;

    /**
     * A peeked string that should be parsed on the next double, long or string. This is populated
     * before a numeric value is parsed and used if that parsing fails.
     */
    private String peekedString;

    /*
     * The nesting stack. Using a manual array rather than an ArrayList saves 20%.
     */
    private int[] stack = new int[32];
    private int stackSize = 0;

    {
        stack[stackSize++] = JsonScope.EMPTY_DOCUMENT;
    }

    /*
     * The path members. It corresponds directly to stack: At indices where the
     * stack contains an object (EMPTY_OBJECT, DANGLING_NAME or NONEMPTY_OBJECT),
     * pathNames contains the name at this scope. Where it contains an array
     * (EMPTY_ARRAY, NONEMPTY_ARRAY) pathIndices contains the current index in
     * that array. Otherwise the value is undefined, and we take advantage of that
     * by incrementing pathIndices when doing so isn't useful.
     */
    private String[] pathNames = new String[32];
    private int[] pathIndices = new int[32];

    /** Creates a new instance that reads a JSON-encoded stream from {@code in}. */
    public CustomGsonReader(Reader in, JsonOptions options) {
        super(in);
        this.in = Objects.requireNonNull(in, "in == null");
        if (options == null) {
            this.nonNumericNumbersSupported = true;
            this.jsonCSupported = false;
        } else {
            this.nonNumericNumbersSupported = options.isNonNumericNumbersSupported();
            this.jsonCSupported = options.isJsoncSupported();
        }
    }

    /**
     * Consumes the next token from the JSON stream and asserts that it is the beginning of a new
     * array.
     *
     * @throws IllegalStateException if the next token is not the beginning of an array.
     */
    public void beginArray() throws IOException {
        int p = peeked;
        if (p == PEEKED_NONE) {
            p = doPeek();
        }
        if (p == PEEKED_BEGIN_ARRAY) {
            push(JsonScope.EMPTY_ARRAY);
            pathIndices[stackSize - 1] = 0;
            peeked = PEEKED_NONE;
        } else {
            throw unexpectedTokenError("BEGIN_ARRAY");
        }
    }

    /**
     * Consumes the next token from the JSON stream and asserts that it is the end of the current
     * array.
     *
     * @throws IllegalStateException if the next token is not the end of an array.
     */
    public void endArray() throws IOException {
        int p = peeked;
        if (p == PEEKED_NONE) {
            p = doPeek();
        }
        if (p == PEEKED_END_ARRAY) {
            stackSize--;
            pathIndices[stackSize - 1]++;
            peeked = PEEKED_NONE;
        } else {
            throw unexpectedTokenError("END_ARRAY");
        }
    }

    /**
     * Consumes the next token from the JSON stream and asserts that it is the beginning of a new
     * object.
     *
     * @throws IllegalStateException if the next token is not the beginning of an object.
     */
    public void beginObject() throws IOException {
        int p = peeked;
        if (p == PEEKED_NONE) {
            p = doPeek();
        }
        if (p == PEEKED_BEGIN_OBJECT) {
            push(JsonScope.EMPTY_OBJECT);
            peeked = PEEKED_NONE;
        } else {
            throw unexpectedTokenError("BEGIN_OBJECT");
        }
    }

    /**
     * Consumes the next token from the JSON stream and asserts that it is the end of the current
     * object.
     *
     * @throws IllegalStateException if the next token is not the end of an object.
     */
    public void endObject() throws IOException {
        int p = peeked;
        if (p == PEEKED_NONE) {
            p = doPeek();
        }
        if (p == PEEKED_END_OBJECT) {
            stackSize--;
            pathNames[stackSize] = null; // Free the last path name so that it can be garbage collected!
            pathIndices[stackSize - 1]++;
            peeked = PEEKED_NONE;
        } else {
            throw unexpectedTokenError("END_OBJECT");
        }
    }

    /** Returns true if the current array or object has another element. */
    public boolean hasNext() throws IOException {
        int p = peeked;
        if (p == PEEKED_NONE) {
            p = doPeek();
        }
        return p != PEEKED_END_OBJECT && p != PEEKED_END_ARRAY && p != PEEKED_EOF;
    }

    /** Returns the type of the next token without consuming it. */
    public JsonToken peek() throws IOException {
        int p = peeked;
        if (p == PEEKED_NONE) {
            p = doPeek();
        }

        switch (p) {
            case PEEKED_BEGIN_OBJECT:
                return JsonToken.BEGIN_OBJECT;

            case PEEKED_END_OBJECT:
                return JsonToken.END_OBJECT;

            case PEEKED_BEGIN_ARRAY:
                return JsonToken.BEGIN_ARRAY;

            case PEEKED_END_ARRAY:
                return JsonToken.END_ARRAY;

            case PEEKED_DOUBLE_QUOTED_NAME:
                return JsonToken.NAME;

            case PEEKED_TRUE:
            case PEEKED_FALSE:
                return JsonToken.BOOLEAN;

            case PEEKED_NULL:
                return JsonToken.NULL;

            case PEEKED_DOUBLE_QUOTED:
            case PEEKED_BUFFERED:
                return JsonToken.STRING;

            case PEEKED_LONG:
            case PEEKED_NUMBER:
                return JsonToken.NUMBER;

            case PEEKED_EOF:
                return JsonToken.END_DOCUMENT;

            default:
                throw new AssertionError();
        }
    }

    @SuppressWarnings("fallthrough")
    int doPeek() throws IOException {
        int peekStack = stack[stackSize - 1];
        if (peekStack == JsonScope.EMPTY_ARRAY) {
            stack[stackSize - 1] = JsonScope.NONEMPTY_ARRAY;
        } else if (peekStack == JsonScope.NONEMPTY_ARRAY) {
            // Look for a comma before the next element.
            int c = nextNonWhitespace(true);
            switch (c) {
                case ']':
                    return peeked = PEEKED_END_ARRAY;

                case ';':
                    throw syntaxError("Unsupported array element separator ';'");

                case ',':
                    break;

                default:
                    throw syntaxError("Unterminated array");
            }
        } else if (peekStack == JsonScope.EMPTY_OBJECT || peekStack == JsonScope.NONEMPTY_OBJECT) {
            stack[stackSize - 1] = JsonScope.DANGLING_NAME;
            // Look for a comma before the next element.
            if (peekStack == JsonScope.NONEMPTY_OBJECT) {
                int c = nextNonWhitespace(true);
                switch (c) {
                    case '}':
                        return peeked = PEEKED_END_OBJECT;

                    case ';':
                        throw syntaxError("Unsupported object member separator ';'");

                    case ',':
                        break;

                    default:
                        throw syntaxError("Unterminated object");
                }
            }
            int c = nextNonWhitespace(true);
            switch (c) {
                case '"':
                    return peeked = PEEKED_DOUBLE_QUOTED_NAME;

                case '\'':
                    throw syntaxError("Unsupported name quoting character: '");

                case '}':
                    if (peekStack != JsonScope.NONEMPTY_OBJECT) {
                        return peeked = PEEKED_END_OBJECT;
                    } else {
                        throw syntaxError("Expected name");
                    }
                default:
                    throw syntaxError("Expected name");
            }
        } else if (peekStack == JsonScope.DANGLING_NAME) {
            stack[stackSize - 1] = JsonScope.NONEMPTY_OBJECT;
            // Look for a colon before the value.
            int c = nextNonWhitespace(true);
            switch (c) {
                case ':':
                    break;

                case '=':
                default:
                    throw syntaxError("Expected ':'");
            }
        } else if (peekStack == JsonScope.EMPTY_DOCUMENT) {
            stack[stackSize - 1] = JsonScope.NONEMPTY_DOCUMENT;
        } else if (peekStack == JsonScope.NONEMPTY_DOCUMENT) {
            int c = nextNonWhitespace(false);
            if (c == -1) {
                return peeked = PEEKED_EOF;
            } else {
                throw syntaxError("Unexpected additional value at root of document.");
            }
        } else if (peekStack == JsonScope.CLOSED) {
            throw new IllegalStateException("JsonReader is closed");
        }

        int c = nextNonWhitespace(true);
        switch (c) {
            case ']':
                if (peekStack == JsonScope.EMPTY_ARRAY) {
                    return peeked = PEEKED_END_ARRAY;
                }
                // fall-through to handle ",]"
            case ';':
            case ',':
                // In lenient mode, a 0-length literal in an array means 'null'.
                if (peekStack == JsonScope.EMPTY_ARRAY || peekStack == JsonScope.NONEMPTY_ARRAY) {
                    throw syntaxError("Expected array value");
                } else {
                    throw syntaxError("Unexpected value");
                }
            case '\'':
                //
                throw syntaxError("Unsupported quoting character: '");

            case '"':
                return peeked = PEEKED_DOUBLE_QUOTED;

            case '[':
                return peeked = PEEKED_BEGIN_ARRAY;

            case '{':
                return peeked = PEEKED_BEGIN_OBJECT;

            default:
                pos--; // Don't consume the first character in a literal value.
        }

        int result = peekKeyword();
        if (result != PEEKED_NONE) {
            return result;
        }

        result = peekNumber();
        if (result != PEEKED_NONE) {
            return result;
        }

        if (!isLiteral(buffer[pos])) {
            throw syntaxError("Expected value");
        }

        throw syntaxError("Unsupported unquoted string value.");
    }

    private int peekKeyword() throws IOException {
        // Figure out which keyword we're matching against by its first character.
        char c = buffer[pos];
        String keyword;
        String keywordUpper;
        int peeking;

        // Look at the first letter to determine what keyword we are trying to match.
        if (c == 't' || c == 'T') {
            keyword = "true";
            keywordUpper = "TRUE";
            peeking = PEEKED_TRUE;
        } else if (c == 'f' || c == 'F') {
            keyword = "false";
            keywordUpper = "FALSE";
            peeking = PEEKED_FALSE;
        } else if (c == 'n' || c == 'N') {
            keyword = "null";
            keywordUpper = "NULL";
            peeking = PEEKED_NULL;
        } else {
            return PEEKED_NONE;
        }

        // Confirm that chars [0..length) match the keyword.
        int length = keyword.length();
        for (int i = 0; i < length; i++) {
            if (pos + i >= limit && !fillBuffer(i + 1)) {
                return PEEKED_NONE;
            }
            c = buffer[pos + i];
            boolean matched = c == keyword.charAt(i) || (c == keywordUpper.charAt(i));
            if (!matched) {
                return PEEKED_NONE;
            }
        }

        if ((pos + length < limit || fillBuffer(length + 1)) && isLiteral(buffer[pos + length])) {
            return PEEKED_NONE; // Don't match trues, falsey or nullsoft!
        }

        // We've found the keyword followed either by EOF or by a non-literal character.
        pos += length;
        return peeked = peeking;
    }

    private int peekNumber() throws IOException {
        // Like nextNonWhitespace, this uses locals 'p' and 'l' to save inner-loop field access.
        char[] buffer = this.buffer;
        int p = pos;
        int l = limit;

        long value = 0; // Negative to accommodate Long.MIN_VALUE more easily.
        boolean negative = false;
        boolean fitsInLong = true;
        int last = NUMBER_CHAR_NONE;

        int i = 0;

        charactersOfNumber: for (; true; i++) {
            if (p + i == l) {
                if (i == buffer.length) {
                    // Though this looks like a well-formed number, it's too long to continue reading. Give up
                    // and let the application handle this as an unquoted literal.
                    return PEEKED_NONE;
                }
                if (!fillBuffer(i + 1)) {
                    break;
                }
                p = pos;
                l = limit;
            }

            char c = buffer[p + i];
            switch (c) {
                case '-':
                    if (last == NUMBER_CHAR_NONE) {
                        negative = true;
                        last = NUMBER_CHAR_SIGN;
                        continue;
                    } else if (last == NUMBER_CHAR_EXP_E) {
                        last = NUMBER_CHAR_EXP_SIGN;
                        continue;
                    }
                    return PEEKED_NONE;

                case '+':
                    if (last == NUMBER_CHAR_NONE) {
                        last = NUMBER_CHAR_SIGN;
                        continue;
                    } else if (last == NUMBER_CHAR_EXP_E) {
                        last = NUMBER_CHAR_EXP_SIGN;
                        continue;
                    }
                    return PEEKED_NONE;

                case 'e':
                case 'E':
                    if (last == NUMBER_CHAR_DIGIT || last == NUMBER_CHAR_FRACTION_DIGIT) {
                        last = NUMBER_CHAR_EXP_E;
                        continue;
                    }
                    return PEEKED_NONE;

                case '.':
                    if (last == NUMBER_CHAR_DIGIT) {
                        last = NUMBER_CHAR_DECIMAL;
                        continue;
                    }
                    return PEEKED_NONE;

                default:
                    if (nonNumericNumbersSupported) {
                        if (c == 'N' && l - (p + i) == 3) {
                            if (buffer[p + i + 1] == 'a' && buffer[p + i + 2] == 'N') {
                                peekedNumberLength = i + 3;
                                return peeked = PEEKED_NUMBER;
                            }
                        } else if (c == 'I' && l - (p + i) == 8) {
                            if (buffer[p + i + 1] == 'n'
                                && buffer[p + i + 2] == 'f'
                                && buffer[p + i + 3] == 'i'
                                && buffer[p + i + 4] == 'n'
                                && buffer[p + i + 5] == 'i'
                                && buffer[p + i + 6] == 't'
                                && buffer[p + i + 7] == 'y') {
                                peekedNumberLength = i + 8;
                                return peeked = PEEKED_NUMBER;
                            }
                        }
                    }

                    if (c < '0' || c > '9') {
                        if (!isLiteral(c)) {
                            break charactersOfNumber;
                        }
                        return PEEKED_NONE;
                    }
                    if (last == NUMBER_CHAR_SIGN || last == NUMBER_CHAR_NONE) {
                        value = -(c - '0');
                        last = NUMBER_CHAR_DIGIT;
                    } else if (last == NUMBER_CHAR_DIGIT) {
                        if (value == 0) {
                            return PEEKED_NONE; // Leading '0' prefix is not allowed (since it could be octal).
                        }
                        long newValue = value * 10 - (c - '0');
                        fitsInLong
                            &= value > MIN_INCOMPLETE_INTEGER || (value == MIN_INCOMPLETE_INTEGER && newValue < value);
                        value = newValue;
                    } else if (last == NUMBER_CHAR_DECIMAL) {
                        last = NUMBER_CHAR_FRACTION_DIGIT;
                    } else if (last == NUMBER_CHAR_EXP_E || last == NUMBER_CHAR_EXP_SIGN) {
                        last = NUMBER_CHAR_EXP_DIGIT;
                    }
            }
        }

        // We've read a complete number. Decide if it's a PEEKED_LONG or a PEEKED_NUMBER.
        // Don't store -0 as long; user might want to read it as double -0.0
        // Don't try to convert Long.MIN_VALUE to positive long; it would overflow MAX_VALUE
        if (last == NUMBER_CHAR_DIGIT
            && fitsInLong
            && (value != Long.MIN_VALUE || negative)
            && (value != 0 || !negative)) {
            peekedLong = negative ? value : -value;
            pos += i;
            return peeked = PEEKED_LONG;
        } else if (last == NUMBER_CHAR_DIGIT || last == NUMBER_CHAR_FRACTION_DIGIT || last == NUMBER_CHAR_EXP_DIGIT) {
            peekedNumberLength = i;
            return peeked = PEEKED_NUMBER;
        } else {
            return PEEKED_NONE;
        }
    }

    private boolean isLiteral(char c) throws IOException {
        switch (c) {
            case '/':
            case '\\':
            case ';':
            case '#':
            case '=':
                throw syntaxError("Unsupported character: " + c);

            case '{':
            case '}':
            case '[':
            case ']':
            case ':':
            case ',':
            case ' ':
            case '\t':
            case '\f':
            case '\r':
            case '\n':
                return false;

            default:
                return true;
        }
    }

    /**
     * Returns the next token, a {@link JsonToken#NAME property name}, and consumes it.
     *
     * @throws IllegalStateException if the next token is not a property name.
     */
    public String nextName() throws IOException {
        int p = peeked;
        if (p == PEEKED_NONE) {
            p = doPeek();
        }
        String result;
        if (p == PEEKED_DOUBLE_QUOTED_NAME) {
            result = nextQuotedValue();
        } else {
            throw unexpectedTokenError("a name");
        }
        peeked = PEEKED_NONE;
        pathNames[stackSize - 1] = result;
        return result;
    }

    /**
     * Returns the {@link JsonToken#STRING string} value of the next token, consuming it. If the next
     * token is a number, this method will return its string form.
     *
     * @throws IllegalStateException if the next token is not a string.
     */
    public String nextString() throws IOException {
        int p = peeked;
        if (p == PEEKED_NONE) {
            p = doPeek();
        }
        String result;
        if (p == PEEKED_DOUBLE_QUOTED) {
            result = nextQuotedValue();
        } else if (p == PEEKED_BUFFERED) {
            result = peekedString;
            peekedString = null;
        } else if (p == PEEKED_LONG) {
            result = Long.toString(peekedLong);
        } else if (p == PEEKED_NUMBER) {
            result = new String(buffer, pos, peekedNumberLength);
            pos += peekedNumberLength;
        } else {
            throw unexpectedTokenError("a string");
        }
        peeked = PEEKED_NONE;
        pathIndices[stackSize - 1]++;
        return result;
    }

    /**
     * Returns the {@link JsonToken#BOOLEAN boolean} value of the next token, consuming it.
     *
     * @throws IllegalStateException if the next token is not a boolean.
     */
    public boolean nextBoolean() throws IOException {
        int p = peeked;
        if (p == PEEKED_NONE) {
            p = doPeek();
        }
        if (p == PEEKED_TRUE) {
            peeked = PEEKED_NONE;
            pathIndices[stackSize - 1]++;
            return true;
        } else if (p == PEEKED_FALSE) {
            peeked = PEEKED_NONE;
            pathIndices[stackSize - 1]++;
            return false;
        }
        throw unexpectedTokenError("a boolean");
    }

    /**
     * Consumes the next token from the JSON stream and asserts that it is a literal null.
     *
     * @throws IllegalStateException if the next token is not a JSON null.
     */
    public void nextNull() throws IOException {
        int p = peeked;
        if (p == PEEKED_NONE) {
            p = doPeek();
        }
        if (p == PEEKED_NULL) {
            peeked = PEEKED_NONE;
            pathIndices[stackSize - 1]++;
        } else {
            throw unexpectedTokenError("null");
        }
    }

    /**
     * Returns the {@link JsonToken#NUMBER double} value of the next token, consuming it. If the next
     * token is a string, this method will attempt to parse it as a double using {@link
     * Double#parseDouble(String)}.
     *
     * @throws IllegalStateException if the next token is neither a number nor a string.
     * @throws NumberFormatException if the next literal value cannot be parsed as a double.
     * @throws MalformedJsonException if the next literal value is NaN or Infinity and this reader is
     *     not {@link #setStrictness(Strictness) lenient}.
     */
    public double nextDouble() throws IOException {
        int p = peeked;
        if (p == PEEKED_NONE) {
            p = doPeek();
        }

        if (p == PEEKED_LONG) {
            peeked = PEEKED_NONE;
            pathIndices[stackSize - 1]++;
            return (double) peekedLong;
        }

        if (p == PEEKED_NUMBER) {
            peekedString = new String(buffer, pos, peekedNumberLength);
            pos += peekedNumberLength;
        } else if (p != PEEKED_BUFFERED) {
            throw unexpectedTokenError("a double");
        }

        peeked = PEEKED_BUFFERED;
        double result = Double.parseDouble(peekedString); // don't catch this NumberFormatException.
        if (!nonNumericNumbersSupported && (Double.isNaN(result) || Double.isInfinite(result))) {
            throw syntaxError("JSON forbids NaN and infinities: " + result);
        }
        peekedString = null;
        peeked = PEEKED_NONE;
        pathIndices[stackSize - 1]++;
        return result;
    }

    /**
     * Returns the {@link JsonToken#NUMBER long} value of the next token, consuming it. If the next
     * token is a string, this method will attempt to parse it as a long. If the next token's numeric
     * value cannot be exactly represented by a Java {@code long}, this method throws.
     *
     * @throws IllegalStateException if the next token is neither a number nor a string.
     * @throws NumberFormatException if the next literal value cannot be parsed as a number, or
     *     exactly represented as a long.
     */
    public long nextLong() throws IOException {
        int p = peeked;
        if (p == PEEKED_NONE) {
            p = doPeek();
        }

        if (p == PEEKED_LONG) {
            peeked = PEEKED_NONE;
            pathIndices[stackSize - 1]++;
            return peekedLong;
        }

        if (p == PEEKED_NUMBER) {
            peekedString = new String(buffer, pos, peekedNumberLength);
            pos += peekedNumberLength;
        } else if (p == PEEKED_DOUBLE_QUOTED) {
            peekedString = nextQuotedValue();
            try {
                long result = Long.parseLong(peekedString);
                peeked = PEEKED_NONE;
                pathIndices[stackSize - 1]++;
                return result;
            } catch (NumberFormatException ignored) {
                // Fall back to parse as a double below.
            }
        } else {
            throw unexpectedTokenError("a long");
        }

        peeked = PEEKED_BUFFERED;
        double asDouble = Double.parseDouble(peekedString); // don't catch this NumberFormatException.
        long result = (long) asDouble;
        if (result != asDouble) { // Make sure no precision was lost casting to 'long'.
            throw new NumberFormatException("Expected a long but was " + peekedString + locationString());
        }
        peekedString = null;
        peeked = PEEKED_NONE;
        pathIndices[stackSize - 1]++;
        return result;
    }

    /**
     * Returns the string up to but not including {@code quote}, unescaping any character escape
     * sequences encountered along the way. The opening quote should have already been read. This
     * consumes the closing quote, but does not include it in the returned string.
     */
    private String nextQuotedValue() throws IOException {
        // Like nextNonWhitespace, this uses locals 'p' and 'l' to save inner-loop field access.
        char[] buffer = this.buffer;
        StringBuilder builder = null;
        while (true) {
            int p = pos;
            int l = limit;
            /* the index of the first character not yet appended to the builder. */
            int start = p;
            while (p < l) {
                int c = buffer[p++];

                // In strict mode, throw an exception when meeting unescaped control characters (U+0000
                // through U+001F)
                if (c < 0x20) {
                    throw syntaxError("Unescaped control characters (\\u0000-\\u001F) are not allowed in strict mode");
                } else if (c == '"') {
                    pos = p;
                    int len = p - start - 1;
                    if (builder == null) {
                        return new String(buffer, start, len);
                    } else {
                        builder.append(buffer, start, len);
                        return builder.toString();
                    }
                } else if (c == '\\') {
                    pos = p;
                    int len = p - start - 1;
                    if (builder == null) {
                        int estimatedLength = (len + 1) * 2;
                        builder = new StringBuilder(Math.max(estimatedLength, 16));
                    }
                    builder.append(buffer, start, len);
                    builder.append(readEscapeCharacter());
                    p = pos;
                    l = limit;
                    start = p;
                }
            }

            if (builder == null) {
                int estimatedLength = (p - start) * 2;
                builder = new StringBuilder(Math.max(estimatedLength, 16));
            }
            builder.append(buffer, start, p - start);
            pos = p;
            if (!fillBuffer(1)) {
                throw syntaxError("Unterminated string");
            }
        }
    }

    private void skipQuotedValue() throws IOException {
        // Like nextNonWhitespace, this uses locals 'p' and 'l' to save inner-loop field access.
        char[] buffer = this.buffer;
        do {
            int p = pos;
            int l = limit;
            /* the index of the first character not yet appended to the builder. */
            while (p < l) {
                int c = buffer[p++];
                if (c == '"') {
                    pos = p;
                    return;
                } else if (c == '\\') {
                    pos = p;
                    char unused = readEscapeCharacter();
                    p = pos;
                    l = limit;
                } else if (c == '\n') {
                    lineNumber++;
                    lineStart = p;
                }
            }
            pos = p;
        } while (fillBuffer(1));
        throw syntaxError("Unterminated string");
    }

    /**
     * Returns the {@link JsonToken#NUMBER int} value of the next token, consuming it. If the next
     * token is a string, this method will attempt to parse it as an int. If the next token's numeric
     * value cannot be exactly represented by a Java {@code int}, this method throws.
     *
     * @throws IllegalStateException if the next token is neither a number nor a string.
     * @throws NumberFormatException if the next literal value cannot be parsed as a number, or
     *     exactly represented as an int.
     */
    public int nextInt() throws IOException {
        int p = peeked;
        if (p == PEEKED_NONE) {
            p = doPeek();
        }

        int result;
        if (p == PEEKED_LONG) {
            result = (int) peekedLong;
            if (peekedLong != result) { // Make sure no precision was lost casting to 'int'.
                throw new NumberFormatException("Expected an int but was " + peekedLong + locationString());
            }
            peeked = PEEKED_NONE;
            pathIndices[stackSize - 1]++;
            return result;
        }

        if (p == PEEKED_NUMBER) {
            peekedString = new String(buffer, pos, peekedNumberLength);
            pos += peekedNumberLength;
        } else if (p == PEEKED_DOUBLE_QUOTED) {
            peekedString = nextQuotedValue();
            try {
                result = Integer.parseInt(peekedString);
                peeked = PEEKED_NONE;
                pathIndices[stackSize - 1]++;
                return result;
            } catch (NumberFormatException ignored) {
                // Fall back to parse as a double below.
            }
        } else {
            throw unexpectedTokenError("an int");
        }

        peeked = PEEKED_BUFFERED;
        double asDouble = Double.parseDouble(peekedString); // don't catch this NumberFormatException.
        result = (int) asDouble;
        if (result != asDouble) { // Make sure no precision was lost casting to 'int'.
            throw new NumberFormatException("Expected an int but was " + peekedString + locationString());
        }
        peekedString = null;
        peeked = PEEKED_NONE;
        pathIndices[stackSize - 1]++;
        return result;
    }

    /**
     * Closes this JSON reader and the underlying {@link Reader}.
     *
     * <p>Using the JSON reader after it has been closed will throw an {@link IllegalStateException}
     * in most cases.
     */
    @Override
    public void close() throws IOException {
        peeked = PEEKED_NONE;
        stack[0] = JsonScope.CLOSED;
        stackSize = 1;
        in.close();
    }

    /**
     * Skips the next value recursively. This method is intended for use when the JSON token stream
     * contains unrecognized or unhandled values.
     *
     * <p>The behavior depends on the type of the next JSON token:
     *
     * <ul>
     *   <li>Start of a JSON array or object: It and all of its nested values are skipped.
     *   <li>Primitive value (for example a JSON number): The primitive value is skipped.
     *   <li>Property name: Only the name but not the value of the property is skipped. {@code
     *       skipValue()} has to be called again to skip the property value as well.
     *   <li>End of a JSON array or object: Only this end token is skipped.
     *   <li>End of JSON document: Skipping has no effect, the next token continues to be the end of
     *       the document.
     * </ul>
     */
    public void skipValue() throws IOException {
        int count = 0;
        do {
            int p = peeked;
            if (p == PEEKED_NONE) {
                p = doPeek();
            }

            switch (p) {
                case PEEKED_BEGIN_ARRAY:
                    push(JsonScope.EMPTY_ARRAY);
                    count++;
                    break;

                case PEEKED_BEGIN_OBJECT:
                    push(JsonScope.EMPTY_OBJECT);
                    count++;
                    break;

                case PEEKED_END_ARRAY:
                    stackSize--;
                    count--;
                    break;

                case PEEKED_END_OBJECT:
                    // Only update when object end is explicitly skipped, otherwise stack is not updated
                    // anyways
                    if (count == 0) {
                        // Free the last path name so that it can be garbage collected
                        pathNames[stackSize - 1] = null;
                    }
                    stackSize--;
                    count--;
                    break;

                case PEEKED_DOUBLE_QUOTED:
                    skipQuotedValue();
                    break;

                case PEEKED_DOUBLE_QUOTED_NAME:
                    skipQuotedValue();
                    // Only update when name is explicitly skipped, otherwise stack is not updated anyways
                    if (count == 0) {
                        pathNames[stackSize - 1] = "<skipped>";
                    }
                    break;

                case PEEKED_NUMBER:
                    pos += peekedNumberLength;
                    break;

                case PEEKED_EOF:
                    // Do nothing
                    return;

                default:
                    // For all other tokens there is nothing to do; token has already been consumed from
                    // underlying reader
            }
            peeked = PEEKED_NONE;
        } while (count > 0);

        pathIndices[stackSize - 1]++;
    }

    private void push(int newTop) {
        if (stackSize == stack.length) {
            int newLength = stackSize * 2;
            stack = Arrays.copyOf(stack, newLength);
            pathIndices = Arrays.copyOf(pathIndices, newLength);
            pathNames = Arrays.copyOf(pathNames, newLength);
        }
        stack[stackSize++] = newTop;
    }

    /**
     * Returns true once {@code limit - pos >= minimum}. If the data is exhausted before that many
     * characters are available, this returns false.
     */
    private boolean fillBuffer(int minimum) throws IOException {
        char[] buffer = this.buffer;
        lineStart -= pos;
        if (limit != pos) {
            limit -= pos;
            System.arraycopy(buffer, pos, buffer, 0, limit);
        } else {
            limit = 0;
        }

        pos = 0;
        int total;
        while ((total = in.read(buffer, limit, buffer.length - limit)) != -1) {
            limit += total;

            // if this is the first read, consume an optional byte order mark (BOM) if it exists
            if (lineNumber == 0 && lineStart == 0 && limit > 0 && buffer[0] == '\ufeff') {
                pos++;
                lineStart++;
                minimum++;
            }

            if (limit >= minimum) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the next character in the stream that is neither whitespace nor a part of a comment.
     * When this returns, the returned character is always at {@code buffer[pos-1]}; this means the
     * caller can always push back the returned character by decrementing {@code pos}.
     */
    private int nextNonWhitespace(boolean throwOnEof) throws IOException {
        /*
         * This code uses ugly local variables 'p' and 'l' representing the 'pos'
         * and 'limit' fields respectively. Using locals rather than fields saves
         * a few field reads for each whitespace character in a pretty-printed
         * document, resulting in a 5% speedup. We need to flush 'p' to its field
         * before any (potentially indirect) call to fillBuffer() and reread both
         * 'p' and 'l' after any (potentially indirect) call to the same method.
         */
        char[] buffer = this.buffer;
        int p = pos;
        int l = limit;
        while (true) {
            if (p == l) {
                pos = p;
                if (!fillBuffer(1)) {
                    break;
                }
                p = pos;
                l = limit;
            }

            int c = buffer[p++];
            if (c == '\n') {
                lineNumber++;
                lineStart = p;
                continue;
            } else if (c == ' ' || c == '\r' || c == '\t') {
                continue;
            }

            if (c == '/') {
                pos = p;
                if (p == l) {
                    pos--; // push back '/' so it's still in the buffer when this method returns
                    boolean charsLoaded = fillBuffer(2);
                    pos++; // consume the '/' again
                    if (!charsLoaded) {
                        return c;
                    }
                }

                if (!jsonCSupported) {
                    throw syntaxError("Unsupported JSON C-style comment.");
                }
                char peek = buffer[pos];
                switch (peek) {
                    case '*':
                        // skip a /* c-style comment */
                        pos++;
                        if (!skipTo()) {
                            throw syntaxError("Unterminated comment");
                        }
                        p = pos + 2;
                        l = limit;
                        continue;

                    case '/':
                        // skip a // end-of-line comment
                        pos++;
                        skipToEndOfLine();
                        p = pos;
                        l = limit;
                        continue;

                    default:
                        return c;
                }
            } else if (c == '#') {
                pos = p;
                // Throw on '#' comment.
                throw syntaxError("Unsupported '#' comment.");
            } else {
                pos = p;
                return c;
            }
        }
        if (throwOnEof) {
            throw new EOFException("End of input" + locationString());
        } else {
            return -1;
        }
    }

    /**
     * Advances the position until after the next newline character. If the line is terminated by
     * "\r\n", the '\n' must be consumed as whitespace by the caller.
     */
    private void skipToEndOfLine() throws IOException {
        while (pos < limit || fillBuffer(1)) {
            char c = buffer[pos++];
            if (c == '\n') {
                lineNumber++;
                lineStart = pos;
                break;
            } else if (c == '\r') {
                break;
            }
        }
    }

    /**
     *
     */
    private boolean skipTo() throws IOException {
        int length = "*/".length();
        outer: for (; pos + length <= limit || fillBuffer(length); pos++) {
            if (buffer[pos] == '\n') {
                lineNumber++;
                lineStart = pos + 1;
                continue;
            }
            for (int c = 0; c < length; c++) {
                if (buffer[pos + c] != "*/".charAt(c)) {
                    continue outer;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + locationString();
    }

    String locationString() {
        int line = lineNumber + 1;
        int column = pos - lineStart + 1;
        return " at line " + line + " column " + column + " path " + getPath();
    }

    private String getPath(boolean usePreviousPath) {
        StringBuilder result = new StringBuilder().append('$');
        for (int i = 0; i < stackSize; i++) {
            int scope = stack[i];
            switch (scope) {
                case JsonScope.EMPTY_ARRAY:
                case JsonScope.NONEMPTY_ARRAY:
                    int pathIndex = pathIndices[i];
                    // If index is last path element it points to next array element; have to decrement
                    if (usePreviousPath && pathIndex > 0 && i == stackSize - 1) {
                        pathIndex--;
                    }
                    result.append('[').append(pathIndex).append(']');
                    break;

                case JsonScope.EMPTY_OBJECT:
                case JsonScope.DANGLING_NAME:
                case JsonScope.NONEMPTY_OBJECT:
                    result.append('.');
                    if (pathNames[i] != null) {
                        result.append(pathNames[i]);
                    }
                    break;

                case JsonScope.NONEMPTY_DOCUMENT:
                case JsonScope.EMPTY_DOCUMENT:
                case JsonScope.CLOSED:
                    break;

                default:
                    throw new AssertionError("Unknown scope value: " + scope);
            }
        }
        return result.toString();
    }

    /**
     * Returns a <a href="https://goessner.net/articles/JsonPath/">JSONPath</a> in <i>dot-notation</i>
     * to the next (or current) location in the JSON document. That means:
     *
     * <ul>
     *   <li>For JSON arrays the path points to the index of the next element (even if there are no
     *       further elements).
     *   <li>For JSON objects the path points to the last property, or to the current property if its
     *       name has already been consumed.
     * </ul>
     *
     * <p>This method can be useful to add additional context to exception messages <i>before</i> a
     * value is consumed, for example when the {@linkplain #peek() peeked} token is unexpected.
     */
    public String getPath() {
        return getPath(false);
    }

    /**
     * Returns a <a href="https://goessner.net/articles/JsonPath/">JSONPath</a> in <i>dot-notation</i>
     * to the previous (or current) location in the JSON document. That means:
     *
     * <ul>
     *   <li>For JSON arrays the path points to the index of the previous element.<br>
     *       If no element has been consumed yet it uses the index 0 (even if there are no elements).
     *   <li>For JSON objects the path points to the last property, or to the current property if its
     *       name has already been consumed.
     * </ul>
     *
     * <p>This method can be useful to add additional context to exception messages <i>after</i> a
     * value has been consumed.
     */
    public String getPreviousPath() {
        return getPath(true);
    }

    /**
     * Unescapes the character identified by the character or characters that immediately follow a
     * backslash. The backslash '\' should have already been read. This supports both Unicode escapes
     * "u000A" and two-character escapes "\n".
     *
     * @throws MalformedJsonException if the escape sequence is malformed
     */
    @SuppressWarnings("fallthrough")
    private char readEscapeCharacter() throws IOException {
        if (pos == limit && !fillBuffer(1)) {
            throw syntaxError("Unterminated escape sequence");
        }

        char escaped = buffer[pos++];
        switch (escaped) {
            case 'u':
                if (pos + 4 > limit && !fillBuffer(4)) {
                    throw syntaxError("Unterminated escape sequence");
                }
                // Equivalent to Integer.parseInt(stringPool.get(buffer, pos, 4), 16);
                int result = 0;
                for (int i = pos, end = i + 4; i < end; i++) {
                    char c = buffer[i];
                    result <<= 4;
                    if (c >= '0' && c <= '9') {
                        result += (c - '0');
                    } else if (c >= 'a' && c <= 'f') {
                        result += (c - 'a' + 10);
                    } else if (c >= 'A' && c <= 'F') {
                        result += (c - 'A' + 10);
                    } else {
                        throw syntaxError("Malformed Unicode escape \\u" + new String(buffer, pos, 4));
                    }
                }
                pos += 4;
                return (char) result;

            case 't':
                return '\t';

            case 'b':
                return '\b';

            case 'n':
                return '\n';

            case 'r':
                return '\r';

            case 'f':
                return '\f';

            case '\n':
                lineNumber++;
                lineStart = pos;
                // fall-through

            case '\'':
            case '"':
            case '\\':
            case '/':
                return escaped;

            default:
                // throw error when none of the above cases are matched
                throw syntaxError("Invalid escape sequence");
        }
    }

    /**
     * Throws a new {@link MalformedJsonException} with the given message and information about the
     * current location.
     */
    private MalformedJsonException syntaxError(String message) throws MalformedJsonException {
        throw new MalformedJsonException(message + locationString());
    }

    private IllegalStateException unexpectedTokenError(String expected) throws IOException {
        JsonToken peeked = peek();
        return new IllegalStateException("Expected " + expected + " but was " + peeked + locationString());
    }

    static final class JsonScope {
        private JsonScope() {
        }

        /** An array with no elements requires no separator before the next element. */
        static final int EMPTY_ARRAY = 1;

        /** An array with at least one value requires a separator before the next element. */
        static final int NONEMPTY_ARRAY = 2;

        /** An object with no name/value pairs requires no separator before the next element. */
        static final int EMPTY_OBJECT = 3;

        /** An object whose most recent element is a key. The next element must be a value. */
        static final int DANGLING_NAME = 4;

        /** An object with at least one name/value pair requires a separator before the next element. */
        static final int NONEMPTY_OBJECT = 5;

        /** No top-level value has been started yet. */
        static final int EMPTY_DOCUMENT = 6;

        /** A top-level value has already been started. */
        static final int NONEMPTY_DOCUMENT = 7;

        /** A document that's been closed and cannot be accessed. */
        static final int CLOSED = 8;
    }
}
