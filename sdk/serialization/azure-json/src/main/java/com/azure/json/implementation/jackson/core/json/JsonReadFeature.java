// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.json;

import com.azure.json.implementation.jackson.core.*;

/**
 * Token reader (parser) features specific to JSON backend.
 * Eventual replacement for JSON-specific {@link JsonParser.Feature}s.
 *
 * @since 2.10
 */
public enum JsonReadFeature {
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
    ALLOW_JAVA_COMMENTS(JsonParser.Feature.ALLOW_COMMENTS),

    // // // Support for non-standard data format constructs: number representations

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
     *  <li>"Infinity" (for positive infinity)
     *  <li>"-Infinity" (for negative infinity)
     *  <li>"NaN" (for other not-a-numbers, like result of division by zero)
     *</ul>
     *<p>
     * Since JSON specification does not allow use of such values,
     * this is a non-standard feature, and as such disabled by default.
     */
    @SuppressWarnings("deprecation")
    ALLOW_NON_NUMERIC_NUMBERS(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);

    /**
     * For backwards compatibility we may need to map to one of existing {@link JsonParser.Feature}s;
     * if so, this is the feature to enable/disable.
     */
    final private JsonParser.Feature _mappedFeature;

    JsonReadFeature(JsonParser.Feature mapTo) {
        _mappedFeature = mapTo;
    }

    public JsonParser.Feature mappedFeature() {
        return _mappedFeature;
    }
}
