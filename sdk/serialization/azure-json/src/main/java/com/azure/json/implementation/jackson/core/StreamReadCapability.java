// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core;

import com.azure.json.implementation.jackson.core.util.JacksonFeature;

/**
 * Set of on/off capabilities that a {@link JsonParser} for given format
 * (or in case of buffering, original format) has.
 * Used in some cases to adjust aspects of things like content conversions,
 * coercions and validation by format-agnostic functionality.
 * Specific or expected usage documented by individual capability entry
 * Javadocs.
 *
 * @since 2.12
 */
public enum StreamReadCapability implements JacksonFeature {
    /**
     * Capability that indicates that data format can expose multiple properties
     * with same name ("duplicates") within one Object context.
     * This is usually not enabled, except for formats like {@code xml} that
     * have content model that does not map cleanly to JSON-based token stream.
     *<p>
     * Capability may be used for allowing secondary mapping of such duplicates
     * in case of using Tree Model (see {@code TreeNode}), or "untyped" databinding
     * (mapping content as generic {@link java.lang.Object}).
     *<p>
     * Capability is currently only enabled for XML format backend.
     */
    DUPLICATE_PROPERTIES(false),

    /**
     * Capability that indicates that data format may in some cases expose Scalar values
     * (whether typed or untyped) as Object values. There are additional access methods
     * at databind level: this capability may be used to decide whether to attempt to
     * use such methods especially in potentially ambiguous cases.
     *<p>
     * Capability is currently only enabled for XML format backend.
     */
    SCALARS_AS_OBJECTS(false),

    /**
     * Capability that indicates that data format only exposed "untyped" scalars: that is,
     * instead of Number, Boolean and String types all scalar values are reported as
     * text ({@link JsonToken#VALUE_STRING})
     * unless some sort of coercion is implied by caller.
     *<p>
     * This capability is true for many textual formats like CSV, Properties and XML.
     */
    UNTYPED_SCALARS(false),

    /**
     * Capability that indicates whether data format supports reporting of
     * accurate floating point values (with respect to reported numeric type,
     * {@link com.azure.json.implementation.jackson.core.JsonParser.NumberType#DOUBLE}) or not.
     * This usually depends on whether format stores such values natively
     * (as IEEE binary FP formats for {@code java.lang.Float} and {@code java.lang.Double};
     * using some other value preserving presentation for {@code java.math.BigDecimal})
     * or not: most binary formats do, and most textual formats do not (at least for
     * {@code Float} and {@code Double}, specifically).
     *<p>
     * In case of JSON numbers (as well as for most if not all textual formats),
     * all floating-point numbers are represented simply by decimal (10-base)
     * textual representation and can only be represented accurately using
     * {@link java.math.BigDecimal}. But for performance reasons they may be
     * (depending on settings) be exposed as {@link java.lang.Double}s (that is,
     * {@link com.azure.json.implementation.jackson.core.JsonParser.NumberType#DOUBLE}).
     * Note that methods like {@link JsonParser#getNumberValueExact()},
     * {@link JsonParser#getValueAsString()} and
     * {@link JsonParser#getDecimalValue()} report values without
     * precision loss.
     *<p>
     * The main intended use case is to let non-Jackson code to handle cases
     * where exact accuracy is necessary in a way that handling does not incur
     * unnecessary conversions across different formats: for example, when reading
     * binary format, simple access is essentially guaranteed to expose value exactly
     * as encoded by the format (as {@code float}, {@code double} or {@code BigDecimal}),
     * whereas for textual formats like JSON it is necessary to access value explicitly
     * as {@code BigDecimal} using {@code JsonParser#getDecimalValue}.
     *<p>
     * Capability is false for text formats like JSON, but true for binary formats
     * like Smile, MessagePack, etc., where type is precisely and inexpensively
     * indicated by format.
     *
     * @since 2.14
     */
    EXACT_FLOATS(false);

    /**
     * Whether feature is enabled or disabled by default.
     */
    private final boolean _defaultState;

    private final int _mask;

    StreamReadCapability(boolean defaultState) {
        _defaultState = defaultState;
        _mask = (1 << ordinal());
    }

    @Override
    public boolean enabledByDefault() {
        return _defaultState;
    }

    @Override
    public boolean enabledIn(int flags) {
        return (flags & _mask) != 0;
    }

    @Override
    public int getMask() {
        return _mask;
    }
}
