// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core;

import com.typespec.json.implementation.jackson.core.util.JacksonFeature;

/**
 * Marker interface that is to be implemented by data format - specific features.
 * Interface used since Java Enums can not extend classes or other Enums, but
 * they can implement interfaces; and as such we may be able to use limited
 * amount of generic functionality.
 *<p>
 * Since 2.12 this is more of an extra marker feature, as its core API is now
 * defined in more general {@link JacksonFeature}.
 * 
 * @since 2.6
 */
public interface FormatFeature
    extends JacksonFeature // since 2.12
{
    /**
     * Accessor for checking whether this feature is enabled by default.
     */
    @Override
    public boolean enabledByDefault();
    
    /**
     * Returns bit mask for this feature instance; must be a single bit,
     * that is of form <code>(1 &lt;&lt; N)</code>
     */
    @Override
    public int getMask();

    /**
     * Convenience method for checking whether feature is enabled in given bitmask
     */
    @Override
    public boolean enabledIn(int flags);
}
