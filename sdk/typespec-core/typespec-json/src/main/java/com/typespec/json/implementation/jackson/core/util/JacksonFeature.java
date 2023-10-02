// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.util;

/**
 * Basic API implemented by Enums used for simple Jackson "features": on/off
 * settings and capabilities exposed as something that can be internally
 * represented as bit sets.
 * Designed to be used with {@link JacksonFeatureSet}.
 *
 * @since 2.12
 */
public interface JacksonFeature
{
    /**
     * Accessor for checking whether this feature is enabled by default.
     *
     * @return Whether this instance is enabled by default or not
     */
    public boolean enabledByDefault();

    /**
     * Returns bit mask for this feature instance; must be a single bit,
     * that is of form {@code 1 << N}.
     *
     * @return Bit mask of this feature
     */
    public int getMask();

    /**
     * Convenience method for checking whether feature is enabled in given bitmask.
     *
     * @param flags Bit field that contains a set of enabled features of this type
     *
     * @return True if this feature is enabled in passed bit field
     */
    public boolean enabledIn(int flags);
}
