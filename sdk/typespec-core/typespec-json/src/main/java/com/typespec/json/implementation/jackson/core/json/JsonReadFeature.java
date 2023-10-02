// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.json;

import com.typespec.json.implementation.jackson.core.*;

/**
 * Token reader (parser) features specific to JSON backend.
 * Eventual replacement for JSON-specific {@link com.typespec.json.implementation.jackson.core.JsonParser.Feature}s.
 *
 * @since 2.10
 */
public enum JsonReadFeature
    implements FormatFeature
{
    // // // Support for non-standard data format constructs: comments

    /**
     * Feature that determines whether parser will allow use
     * of Java/C/C++ style comments (both '/'+'*' and
     * '//' varieties) within parsed content or not.
     *<p>
     * Since JSON specification does not mention comments as legal
     * construct,
     * this is a non-standard feature; however, in the wild
     * this is extensively used. As such, feature is
     * <b>disabled by default</b> for parsers and must be
     * explicitly enabled.
     */
    ALLOW_JAVA_COMMENTS(false, JsonParser.Feature.ALLOW_COMMENTS),

    /**
     * Feature that determines whether parser will allow use
     * of YAML comments, ones starting with '#' and continuing
     * until the end of the line. This commenting style is common
     * with scripting languages as well.
     *<p>
     * Since JSON specification does not mention comments as legal
     * construct,
     * this is a non-standard feature. As such, feature is
     * <b>disabled by default</b> for parsers and must be
     * explicitly enabled.
     */
    ALLOW_YAML_COMMENTS(false, JsonParser.Feature.ALLOW_YAML_COMMENTS),

    // // // Support for non-standard data format constructs: quoting/escaping

    /**
     * Feature that determines whether parser will allow use
     * of single quotes (apostrophe, character '\'') for
     * quoting Strings (names and String values). If so,
     * this is in addition to other acceptable markers.
     *<p>
     * Since JSON specification requires use of double quotes for
     * field names,
     * this is a non-standard feature, and as such disabled by default.
     */
    ALLOW_SINGLE_QUOTES(false, JsonParser.Feature.ALLOW_SINGLE_QUOTES),

    /**
     * Feature that determines whether parser will allow use
     * of unquoted field names (which is allowed by Javascript,
     * but not by JSON specification).
     *<p>
     * Since JSON specification requires use of double quotes for
     * field names,
     * this is a non-standard feature, and as such disabled by default.
     */
    ALLOW_UNQUOTED_FIELD_NAMES(false, JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES),

    /**
     * Feature that determines whether parser will allow
     * JSON Strings to contain unescaped control characters
     * (ASCII characters with value less than 32, including
     * tab and line feed characters) or not.
     * If feature is set false, an exception is thrown if such a
     * character is encountered.
     *<p>
     * Since JSON specification requires quoting for all control characters,
     * this is a non-standard feature, and as such disabled by default.
     */
    @SuppressWarnings("deprecation")
    ALLOW_UNESCAPED_CONTROL_CHARS(false, JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS),

    /**
     * Feature that can be enabled to accept quoting of all character
     * using backslash quoting mechanism: if not enabled, only characters
     * that are explicitly listed by JSON specification can be thus
     * escaped (see JSON spec for small list of these characters)
     *<p>
     * Since JSON specification requires quoting for all control characters,
     * this is a non-standard feature, and as such disabled by default.
     */
    @SuppressWarnings("deprecation")
    ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER(false, JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER),

    // // // Support for non-standard data format constructs: number representations
    
    /**
     * Feature that determines whether parser will allow
     * JSON integral numbers to start with additional (ignorable) 
     * zeroes (like: 000001). If enabled, no exception is thrown, and extra
     * nulls are silently ignored (and not included in textual representation
     * exposed via {@link JsonParser#getText}).
     *<p>
     * Since JSON specification does not allow leading zeroes,
     * this is a non-standard feature, and as such disabled by default.
     */
    @SuppressWarnings("deprecation")
    ALLOW_LEADING_ZEROS_FOR_NUMBERS(false, JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS),

    /**
     * Feature that determines whether parser will allow
     * JSON decimal numbers to start with a decimal point
     * (like: .123). If enabled, no exception is thrown, and the number
     * is parsed as though a leading 0 had been present.
     *<p>
     * Since JSON specification does not allow leading decimal,
     * this is a non-standard feature, and as such disabled by default.
     *
     * @since 2.11
     */
    @SuppressWarnings("deprecation")
    ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS(false, JsonParser.Feature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS),

    /**
     * Feature that allows parser to recognize set of
     * "Not-a-Number" (NaN) tokens as legal floating number
     * values (similar to how many other data formats and
     * programming language source code allows it).
     * Specific subset contains values that
     * <a href="http://www.w3.org/TR/xmlschema-2/">XML Schema</a>
     * (see section 3.2.4.1, Lexical Representation)
     * allows (tokens are quoted contents, not including quotes):
     *<ul>
     *  <li>"INF" (for positive infinity), as well as alias of "Infinity"
     *  <li>"-INF" (for negative infinity), alias "-Infinity"
     *  <li>"NaN" (for other not-a-numbers, like result of division by zero)
     *</ul>
     *<p>
     * Since JSON specification does not allow use of such values,
     * this is a non-standard feature, and as such disabled by default.
     */
    @SuppressWarnings("deprecation")
    ALLOW_NON_NUMERIC_NUMBERS(false, JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS),

    // // // Support for non-standard data format constructs: array/value separators
     
    /**
     * Feature allows the support for "missing" values in a JSON array: missing
     * value meaning sequence of two commas, without value in-between but only
     * optional white space.
     * Enabling this feature will expose "missing" values as {@link JsonToken#VALUE_NULL}
     * tokens, which typically become Java nulls in arrays and {@link java.util.Collection}
     * in data-binding.
     * <p>
     * For example, enabling this feature will represent a JSON array <code>["value1",,"value3",]</code>
     * as <code>["value1", null, "value3", null]</code> 
     * <p>
     * Since the JSON specification does not allow missing values this is a non-compliant JSON
     * feature and is disabled by default.
     */
    @SuppressWarnings("deprecation")
    ALLOW_MISSING_VALUES(false, JsonParser.Feature.ALLOW_MISSING_VALUES),

    /**
     * Feature that determines whether {@link JsonParser} will allow for a single trailing
     * comma following the final value (in an Array) or member (in an Object). These commas
     * will simply be ignored.
     * <p>
     * For example, when this feature is enabled, <code>[true,true,]</code> is equivalent to
     * <code>[true, true]</code> and <code>{"a": true,}</code> is equivalent to
     * <code>{"a": true}</code>.
     * <p>
     * When combined with <code>ALLOW_MISSING_VALUES</code>, this feature takes priority, and
     * the final trailing comma in an array declaration does not imply a missing
     * (<code>null</code>) value. For example, when both <code>ALLOW_MISSING_VALUES</code>
     * and <code>ALLOW_TRAILING_COMMA</code> are enabled, <code>[true,true,]</code> is
     * equivalent to <code>[true, true]</code>, and <code>[true,true,,]</code> is equivalent to
     * <code>[true, true, null]</code>.
     * <p>
     * Since the JSON specification does not permit trailing commas, this is a non-standard
     * feature, and as such disabled by default.
     */
    @SuppressWarnings("deprecation")
    ALLOW_TRAILING_COMMA(false, JsonParser.Feature.ALLOW_TRAILING_COMMA),
    ;

    final private boolean _defaultState;
    final private int _mask;

    /**
     * For backwards compatibility we may need to map to one of existing {@link JsonParser.Feature}s;
     * if so, this is the feature to enable/disable.
     */
    final private JsonParser.Feature _mappedFeature;
    
    /**
     * Method that calculates bit set (flags) of all features that
     * are enabled by default.
     *
     * @return Bit mask of all features that are enabled by default
     */
    public static int collectDefaults()
    {
        int flags = 0;
        for (JsonReadFeature f : values()) {
            if (f.enabledByDefault()) {
                flags |= f.getMask();
            }
        }
        return flags;
    }
    
    private JsonReadFeature(boolean defaultState,
            JsonParser.Feature  mapTo) {
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

    public JsonParser.Feature mappedFeature() { return _mappedFeature; }
}
