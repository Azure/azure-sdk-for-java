// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.json;

import com.azure.json.implementation.jackson.core.*;

/**
 * Token writer features specific to JSON backend.
 *
 * @since 2.10
 */
public enum JsonWriteFeature {
    // // // Support for non-standard data format constructs: comments

    // // Quoting/ecsaping-related features

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
    WRITE_NAN_AS_STRINGS(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS),;

    /**
     * For backwards compatibility we may need to map to one of existing {@link JsonGenerator.Feature}s;
     * if so, this is the feature to enable/disable.
     */
    final private JsonGenerator.Feature _mappedFeature;

    JsonWriteFeature(JsonGenerator.Feature mapTo) {
        _mappedFeature = mapTo;
    }

    public JsonGenerator.Feature mappedFeature() {
        return _mappedFeature;
    }
}
