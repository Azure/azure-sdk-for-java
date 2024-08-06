// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core;

import com.azure.json.implementation.jackson.core.exc.StreamConstraintsException;

/**
 * The constraints to use for streaming writes: used to guard against problematic
 * output by preventing processing of "too big" output constructs (values,
 * structures).
 * Constraints are registered with {@code TokenStreamFactory} (such as
 * {@code JsonFactory}); if nothing explicitly specified, default
 * constraints are used.
 *<p>
 * Currently constrained aspects, with default settings, are:
 * <ul>
 *  <li>Maximum Nesting depth: default 1000 (see {@link #DEFAULT_MAX_DEPTH})
 *   </li>
 * </ul>
 *
 * @since 2.16
 */
public class StreamWriteConstraints implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Default setting for maximum depth: see {@link Builder#maxNestingDepth(int)} for details.
     */
    public static final int DEFAULT_MAX_DEPTH = 1000;

    protected final int _maxNestingDepth;

    private static final StreamWriteConstraints DEFAULT = new StreamWriteConstraints(DEFAULT_MAX_DEPTH);

    public static final class Builder {
        private final int maxNestingDepth;

        Builder() {
            this(DEFAULT_MAX_DEPTH);
        }

        Builder(final int maxNestingDepth) {
            this.maxNestingDepth = maxNestingDepth;
        }

        public StreamWriteConstraints build() {
            return new StreamWriteConstraints(maxNestingDepth);
        }
    }

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    protected StreamWriteConstraints(final int maxNestingDepth) {
        _maxNestingDepth = maxNestingDepth;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * @return the default {@link StreamWriteConstraints} (when none is set on the {@link JsonFactory} explicitly)
     */
    public static StreamWriteConstraints defaults() {
        return DEFAULT;
    }

    /*
    /**********************************************************************
    /* Accessors
    /**********************************************************************
     */

    /*
    /**********************************************************************
    /* Convenience methods for validation, document limits
    /**********************************************************************
     */

    /**
     * Convenience method that can be used to verify that the
     * nesting depth does not exceed the maximum specified by this
     * constraints object: if it does, a
     * {@link StreamConstraintsException}
     * is thrown.
     *
     * @param depth count of unclosed objects and arrays
     *
     * @throws StreamConstraintsException If depth exceeds maximum
     */
    public void validateNestingDepth(int depth) throws StreamConstraintsException {
        if (depth > _maxNestingDepth) {
            throw _constructException(depth, _maxNestingDepth, _constrainRef());
        }
    }

    /*
    /**********************************************************************
    /* Error reporting
    /**********************************************************************
     */

    // @since 2.16
    protected StreamConstraintsException _constructException(Object... args) throws StreamConstraintsException {
        throw new StreamConstraintsException(
            String.format("Document nesting depth (%d) exceeds the maximum allowed (%d, from %s)", args));
    }

    // @since 2.16
    protected String _constrainRef() {
        return "`StreamWriteConstraints." + "getMaxNestingDepth" + "()`";
    }
}
