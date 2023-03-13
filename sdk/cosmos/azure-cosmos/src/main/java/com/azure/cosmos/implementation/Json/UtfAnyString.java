package com.azure.cosmos.implementation.Json;

import java.util.Objects;

public class UtfAnyString implements Comparable<String> {
    private final Object buffer;

    public static final UtfAnyString Empty = new UtfAnyString("");

    public boolean isUtf8(){
        return buffer instanceof String;
    }

    public boolean isUtf16() {
        return buffer instanceof String;
    }

    public boolean isNull() {
        return buffer == null;
    }

    public boolean isEmpty() {
        if (buffer == null) {
            return false;
        }

        String text = (String)buffer;
        if (text != null) {
            return text.length() == 0;
        }

        return ((String)buffer).isEmpty();
    }

    public UtfAnyString(String utf8String) {
        buffer = utf8String;
    }

    public static UtfAnyString valueOf(String utf16String) {
        return new UtfAnyString(utf16String);
    }

    public static String valueOf(UtfAnyString str){
        return str.buffer != null ? str.buffer.toString() : null;
    }
    @Override
    public String toString() {
        return buffer != null ? buffer.toString() : null;
    }

    public boolean referenceEquals(UtfAnyString other) {
        return buffer == other.buffer;
    }

    public boolean equals(UtfAnyString other) {
        if (buffer == null) {
            return other.buffer == null;
        }

        String text = (String)buffer;
        if (text != null) {
            return other.equals(text);
        }

        return other.equals((String)buffer);
    }

    public boolean equals(Object obj)
    {
        String text = (String)obj;
        if (text == null)
        {
            String utf8String = (String)obj;
            if ((Object)utf8String == null)
            {
                if (obj instanceof UtfAnyString)
                {
                    UtfAnyString other = (UtfAnyString)obj;
                    return equals(other);
                }

                return false;
            }

            return equals(utf8String);
        }

        return equals(text);
    }

    public boolean equals(String other)
    {
        if (buffer == null)
        {
            return other == null;
        }

        String text = (String)buffer;
        if (text != null)
        {
            return text.equals(other);
        }

        return ((String)buffer).equals(other);
    }

    public static boolean Equals(UtfAnyString left, UtfAnyString right)
    {
        return left.equals(right);
    }

    public static boolean NotEquals(UtfAnyString left, UtfAnyString right)
    {
        return !left.equals(right);
    }

    public static boolean Equals(UtfAnyString left, String right)
    {
        return left.equals(right);
    }

    public static boolean NotEquals(UtfAnyString left, String right)
    {
        return !left.equals(right);
    }

    public static boolean Equals(String left, UtfAnyString right)
    {
        return right.equals(left);
    }

    public static boolean NotEquals(String left, UtfAnyString right)
    {
        return !right.equals(left);
    }

    public static boolean lessThan(UtfAnyString left, UtfAnyString right) { return left.compareTo(right) < 0; }

    public static boolean lessThanEqualTo(UtfAnyString left, UtfAnyString right)
    {
        return left.compareTo(right) <= 0;
    }

    public static boolean greaterThan(UtfAnyString left, UtfAnyString right)
    {
        return left.compareTo(right) > 0;
    }

    public static boolean greaterThanEqualTo(UtfAnyString left, UtfAnyString right)
    {
        return left.compareTo(right) >= 0;
    }

    public static boolean lessThan(UtfAnyString left, String right)
    {
        return left.compareTo(right) < 0;
    }

    public static boolean lessThanEqualTo(UtfAnyString left, String right)
    {
        return left.compareTo(right) <= 0;
    }

    public static boolean greaterThan(UtfAnyString left, String right)
    {
        return left.compareTo(right) > 0;
    }

    public static boolean greaterThanEqualTo(UtfAnyString left, String right)
    {
        return left.compareTo(right) >= 0;
    }

    public static boolean lessThan(String left, UtfAnyString right)
    {
        return right.compareTo(left) >= 0;
    }

    public static boolean lessThanEqualTo(String left, UtfAnyString right)
    {
        return right.compareTo(left) > 0;
    }

    public static boolean greaterThan(String left, UtfAnyString right)
    {
        return right.compareTo(left) <= 0;
    }

    public static boolean greaterThanEqualTo(String left, UtfAnyString right)
    {
        return right.compareTo(left) < 0;
    }

    public int compareTo(UtfAnyString other)
    {
        if (other.buffer == null)
        {
            if (buffer != null)
            {
                return 1;
            }

            return 0;
        }

        String text = (String)other.buffer;
        if (text != null)
        {
            return compareTo(text);
        }

        return compareTo((String)other.buffer);
    }

    public int compareTo(String other)
    {
        if (buffer == null)
        {
            if (other != null)
            {
                return -1;
            }

            return 0;
        }

        String text = (String)buffer;
        if (text != null)
        {
            return text.compareTo(other);
        }

        return ((String)buffer).compareTo(other);
    }
}
