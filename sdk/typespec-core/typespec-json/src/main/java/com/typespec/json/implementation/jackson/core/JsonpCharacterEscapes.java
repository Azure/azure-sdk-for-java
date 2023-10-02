// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core;

import com.typespec.json.implementation.jackson.core.io.CharacterEscapes;
import com.typespec.json.implementation.jackson.core.io.SerializedString;

/**
 * Convenience {@link CharacterEscapes} implementation that escapes
 * Unicode characters `0x2028` and `0x2029` (in addition to characters
 * escaped otherwise), which are apparently considered linefeeds as
 * per newer Javascript specifications, and consequently problematic
 * when using JSONP (see https://en.wikipedia.org/wiki/JSONP).
 *
 * @since 2.8
 */
public class JsonpCharacterEscapes extends CharacterEscapes
{
    private static final long serialVersionUID = 1L;

    private static final int[] asciiEscapes = CharacterEscapes.standardAsciiEscapesForJSON();
    private static final SerializedString escapeFor2028 = new SerializedString("\\u2028");
    private static final SerializedString escapeFor2029 = new SerializedString("\\u2029");

    private static final JsonpCharacterEscapes sInstance = new JsonpCharacterEscapes();

    public static JsonpCharacterEscapes instance() {
        return sInstance;
    }

    @Override
    public SerializableString getEscapeSequence(int ch)
    {
        switch (ch) {
        case 0x2028:
            return escapeFor2028;
        case 0x2029:
            return escapeFor2029;
        default:
            return null;
        }
    }

    @Override
    public int[] getEscapeCodesForAscii() {
        return asciiEscapes;
    }
}
