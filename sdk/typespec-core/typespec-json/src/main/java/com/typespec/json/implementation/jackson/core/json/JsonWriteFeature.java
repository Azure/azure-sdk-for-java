// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.json;

import com.typespec.json.implementation.jackson.core.*;

/**
 * Token writer features specific to JSON backend.
 *
 * @since 2.10
 */
public enum JsonWriteFeature
    implements FormatFeature
{
    // // // Support for non-standard data format constructs: comments

    // // Quoting/ecsaping-related features
    
    /**
     * Feature that determines whether JSON Object field names are
     * quoted using double-quotes, as specified by JSON specification
     * or not. Ability to disable quoting was added to support use
     * cases where they are not usually expected, which most commonly
     * occurs when used straight from Javascript.
     *<p>
     * Feature is enabled by default (since it is required by JSON specification).
     */
    @SuppressWarnings("deprecation")
    QUOTE_FIELD_NAMES(true, JsonGenerator.Feature.QUOTE_FIELD_NAMES),

    /**
     * Feature that determines whether "NaN" ("not a number", that is, not
     * real number) float/double values are output as JSON strings.
     * The values checked are Double.Nan,
     * Double.POSITIVE_INFINITY and Double.NEGATIVE_INIFINTY (and 
     * associated Float values).
     * If feature is disabled, these numbers are still output using
     * associated literal values, resulting in non-conforming
     * output.
     *<p>
     * Feature is enabled by default.
     */
    @SuppressWarnings("deprecation")
    WRITE_NAN_AS_STRINGS(true, JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS),

    /**
     * Feature that forces all regular number values to be written as JSON Strings,
     * instead of as JSON Numbers.
     * Default state is 'false', meaning that Java numbers are to
     * be serialized using basic numeric representation but
     * if enabled all such numeric values are instead written out as
     * JSON Strings instead.
     *<p>
     * One use case is to avoid problems with Javascript limitations:
     * since Javascript standard specifies that all number handling
     * should be done using 64-bit IEEE 754 floating point values,
     * result being that some 64-bit integer values can not be
     * accurately represent (as mantissa is only 51 bit wide).
     *<p>
     * Feature is disabled by default.
     */
    @SuppressWarnings("deprecation")
    WRITE_NUMBERS_AS_STRINGS(false, JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS),
    
    /**
     * Feature that specifies that all characters beyond 7-bit ASCII
     * range (i.e. code points of 128 and above) need to be output
     * using format-specific escapes (for JSON, backslash escapes),
     * if format uses escaping mechanisms (which is generally true
     * for textual formats but not for binary formats).
     *<p>
     * Feature is disabled by default.
     */
    @SuppressWarnings("deprecation")
    ESCAPE_NON_ASCII(false, JsonGenerator.Feature.ESCAPE_NON_ASCII),

//23-Nov-2015, tatu: for [core#223], if and when it gets implemented
    /*
     * Feature that specifies handling of UTF-8 content that contains
     * characters beyond BMP (Basic Multilingual Plane), which are
     * represented in UCS-2 (Java internal character encoding) as two
     * "surrogate" characters. If feature is enabled, these surrogate
     * pairs are separately escaped using backslash escapes; if disabled,
     * native output (4-byte UTF-8 sequence, or, with char-backed output
     * targets, writing of surrogates as is which is typically converted
     * by {@link java.io.Writer} into 4-byte UTF-8 sequence eventually)
     * is used.
     *<p>
     * Note that the original JSON specification suggests use of escaping;
     * but that this is not correct from standard UTF-8 handling perspective.
     * Because of two competing goals, this feature was added to allow either
     * behavior to be used, but defaulting to UTF-8 specification compliant
     * mode.
     *<p>
     * Feature is disabled by default.
     */
//    ESCAPE_UTF8_SURROGATES(false, JsonGenerator.Feature.ESCAPE_UTF8_SURROGATES),

    ;

    final private boolean _defaultState;
    final private int _mask;

    /**
     * For backwards compatibility we may need to map to one of existing {@link JsonGenerator.Feature}s;
     * if so, this is the feature to enable/disable.
     */
    final private JsonGenerator.Feature _mappedFeature;
    
    /**
     * Method that calculates bit set (flags) of all features that
     * are enabled by default.
     *
     * @return Bit mask of all features that are enabled by default
     */
    public static int collectDefaults()
    {
        int flags = 0;
        for (JsonWriteFeature f : values()) {
            if (f.enabledByDefault()) {
                flags |= f.getMask();
            }
        }
        return flags;
    }
    
    private JsonWriteFeature(boolean defaultState,
            JsonGenerator.Feature  mapTo) {
        _defaultState = defaultState;
        _mask = (1 << ordinal());
        _mappedFeature = mapTo;
    }

    @Override
    public boolean enabledByDefault() { return _defaultState; }
    @Override
    public int getMask() { return _mask; }
    @Override
    public boolean enabledIn(int flags) { return (flags & _mask) != 0; }

    public JsonGenerator.Feature mappedFeature() { return _mappedFeature; }
}
