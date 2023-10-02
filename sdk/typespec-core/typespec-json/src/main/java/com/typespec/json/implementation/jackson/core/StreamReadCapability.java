// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core;

import com.typespec.json.implementation.jackson.core.util.JacksonFeature;

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
public enum StreamReadCapability
    implements JacksonFeature
{
    /**
     * Capability that indicates that data format can expose multiple properties
     * with same name ("duplicates") within one Object context.
     * This is usually not enabled, except for formats like {@code xml} that
     * have content model that does not map cleanly to JSON-based token stream.
     *<p>
     * Capability may be used for allowing secondary mapping of such duplicates
     * in case of using Tree Model (see {@link TreeNode}), or "untyped" databinding
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
    ;

    /**
     * Whether feature is enabled or disabled by default.
     */
    private final boolean _defaultState;

    private final int _mask;

    private StreamReadCapability(boolean defaultState) {
        _defaultState = defaultState;
        _mask = (1 << ordinal());
    }

    @Override
    public boolean enabledByDefault() { return _defaultState; }
    @Override
    public boolean enabledIn(int flags) { return (flags & _mask) != 0; }
    @Override
    public int getMask() { return _mask; }
}
