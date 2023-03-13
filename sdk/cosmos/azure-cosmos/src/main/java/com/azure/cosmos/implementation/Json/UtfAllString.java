package com.azure.cosmos.implementation.Json;

import java.nio.charset.StandardCharsets;

public final class UtfAllString
{
    private final Utf8Memory utf8String;
    private final String utf16String;
    private final Utf8Memory utf8EscapedString;
    private final String utf16EscapedString;

    private UtfAllString(Utf8Memory utf8String, String utf16String, Utf8Memory utf8EscapedString, String utf16EscapedString)
    {
        this.utf8String = utf8String;
        this.utf16String = utf16String;
        this.utf8EscapedString = utf8EscapedString;
        this.utf16EscapedString = utf16EscapedString;
    }

    public Utf8Memory getUtf8String() {
        return utf8String;
    }

    public String getUtf16String() {
        return utf16String;
    }

    public Utf8Memory getUtf8EscapedString() {
        return utf8EscapedString;
    }

    public String getUtf16EscapedString() {
        return utf16EscapedString;
    }

    public static UtfAllString create(String utf16String){
        if(utf16String == null)
        {
            throw new IllegalArgumentException("string cannot be null");
        }

        Utf8Memory utf8String = Utf8Memory.unsafeCreateNoValidation(StandardCharsets.UTF_8.encode(utf16String));
        String utf16EscapedString = utf16String;
        utf16EscapedString = utf16EscapedString.substring(1, utf16EscapedString.length() - 1);

        Utf8Memory utf8EscapedString = Utf8Memory.unsafeCreateNoValidation(StandardCharsets.UTF_8.encode(utf16EscapedString));

        return new UtfAllString(utf8String, utf16String, utf8EscapedString, utf16EscapedString);
    }

}
