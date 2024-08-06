// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.util;

/**
 * Container similar to {@link java.util.EnumSet} meant for storing sets of
 * {@link JacksonFeature}s (usually {@link java.lang.Enum}s): main
 * difference being that these sets are immutable. Also only supports relatively
 * small sets of features: specifically, up to 31 features.
 *
 * @since 2.12
 */
public final class JacksonFeatureSet<F extends JacksonFeature> implements java.io.Serializable // since 2.16
{
    private static final long serialVersionUID = 1L;

    private final int _enabled;

    /**
     * Constructor for creating instance with specific bitmask, wherein
     * {@code 1} bit means matching {@link JacksonFeature} is enabled and
     * {@code 0} disabled.
     *
     * @param bitmask Bitmask for features that are enabled
     */
    private JacksonFeatureSet(int bitmask) {
        _enabled = bitmask;
    }

    /**
     * Mutant factory for getting a set in which specified feature is enabled:
     * will either return this instance (if no change), or newly created set (if there
     * is change).
     *
     * @param feature Feature to enable in set returned
     *
     * @return Newly created set of state of feature changed; {@code this} if not
     */
    public JacksonFeatureSet<F> with(F feature) {
        int newMask = _enabled | feature.getMask();
        return (newMask == _enabled) ? this : new JacksonFeatureSet<>(newMask);
    }

    /**
     * Mutant factory for getting a set in which specified feature is disabled:
     * will either return this instance (if no change), or newly created set (if there
     * is change).
     *
     * @param feature Feature to disable in set returned
     *
     * @return Newly created set of state of feature changed; {@code this} if not
     */
    public JacksonFeatureSet<F> without(F feature) {
        int newMask = _enabled & ~feature.getMask();
        return (newMask == _enabled) ? this : new JacksonFeatureSet<>(newMask);
    }

    /**
     * Main accessor for checking whether given feature is enabled in this feature set.
     *
     * @param feature Feature to check
     *
     * @return True if feature is enabled in this set; false otherwise
     */
    public boolean isEnabled(F feature) {
        return (feature.getMask() & _enabled) != 0;
    }

}
