// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core;

import com.typespec.json.implementation.jackson.core.util.JacksonFeature;

/**
 * Set of on/off capabilities that a {@link JsonGenerator} for given format
 * (or in case of buffering, original format) has.
 * Used in some cases to adjust aspects of things like content conversions and
 * coercions by format-agnostic functionality.
 * Specific or expected usage documented by individual capability entry Javadocs.
 *
 * @since 2.12
 */
public enum StreamWriteCapability
    implements JacksonFeature
{
    /**
     * Capability that indicates that the data format is able to express binary
     * data natively, without using textual encoding like Base64.
     *<p>
     * Capability is currently enabled for all binary formats and none of textual
     * formats.
     */
    CAN_WRITE_BINARY_NATIVELY(false),

    /**
     * Capability that indicates that the data format is able to write
     * "formatted numbers": that is, output of numbers is done as Strings
     * and caller is allowed to pass in logical number values as Strings.
     *<p>
     * Capability is currently enabled for most textual formats and none of binary
     * formats.
     */
    CAN_WRITE_FORMATTED_NUMBERS(false)
    ;

    /**
     * Whether feature is enabled or disabled by default.
     */
    private final boolean _defaultState;

    private final int _mask;

    private StreamWriteCapability(boolean defaultState) {
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
