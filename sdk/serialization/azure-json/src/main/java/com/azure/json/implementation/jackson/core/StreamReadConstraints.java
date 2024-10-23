// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core;

/**
 * The constraints to use for streaming reads: used to guard against malicious
 * input by preventing processing of "too big" input constructs (values,
 * structures).
 * Constraints are registered with {@code TokenStreamFactory} (such as
 * {@code JsonFactory}); if nothing explicitly specified, default
 * constraints are used.
 *<p>
 * Currently constrained aspects, with default settings, are:
 * <ul>
 *  <li>Maximum Number value length: default 1000 (see {@link #DEFAULT_MAX_NUM_LEN})
 *   </li>
 *  <li>Maximum String value length: default 20_000_000 (see {@link #DEFAULT_MAX_STRING_LEN})
 *   </li>
 *  <li>Maximum Property name length: default 50_000 (see {@link #DEFAULT_MAX_NAME_LEN})
 *   </li>
 *  <li>Maximum Nesting depth: default 1000 (see {@link #DEFAULT_MAX_DEPTH})
 *   </li>
 *  <li>Maximum Document length: default {@code unlimited} (coded as {@code -1},
 *      (see {@link #DEFAULT_MAX_DOC_LEN})
 *   </li>
 * </ul>
 *
 * @since 2.15
 */
public class StreamReadConstraints implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Default setting for maximum depth: see {@link Builder#maxNestingDepth(int)} for details.
     */
    public static final int DEFAULT_MAX_DEPTH = 1000;

    /**
     * Default setting for maximum document length:
     * see {@link Builder#maxDocumentLength} for details.
     */
    public static final long DEFAULT_MAX_DOC_LEN = -1L;

    /**
     * @since 2.16
     */
    public static final int DEFAULT_MAX_NUM_LEN = 1000;

    /**
     * Default setting for maximum string length: see {@link Builder#maxStringLength(int)}
     * for details.
     *<p>
     * NOTE: Jackson 2.15.0 initially used a lower setting (5_000_000).
     */
    public static final int DEFAULT_MAX_STRING_LEN = 20_000_000;

    /**
     * Default setting for maximum name length: see {@link Builder#maxNameLength(int)}
     * for details.
     *
     * @since 2.16
     */
    public static final int DEFAULT_MAX_NAME_LEN = 50_000;

    protected final int _maxNestingDepth;
    protected final long _maxDocLen;

    protected final int _maxNumLen;
    protected final int _maxStringLen;
    protected final int _maxNameLen;

    public static final class Builder {
        private final long maxDocLen;
        private final int maxNestingDepth;
        private final int maxNumLen;
        private final int maxStringLen;
        private final int maxNameLen;

        Builder() {
            this(DEFAULT_MAX_DEPTH, DEFAULT_MAX_DOC_LEN, DEFAULT_MAX_NUM_LEN, DEFAULT_MAX_STRING_LEN,
                DEFAULT_MAX_NAME_LEN);
        }

        Builder(final int maxNestingDepth, final long maxDocLen, final int maxNumLen, final int maxStringLen,
            final int maxNameLen) {
            this.maxNestingDepth = maxNestingDepth;
            this.maxDocLen = maxDocLen;
            this.maxNumLen = maxNumLen;
            this.maxStringLen = maxStringLen;
            this.maxNameLen = maxNameLen;
        }

        public StreamReadConstraints build() {
            return new StreamReadConstraints(maxNestingDepth, maxDocLen, maxNumLen, maxStringLen, maxNameLen);
        }
    }

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    @Deprecated // since 2.16
    protected StreamReadConstraints(final int maxNestingDepth, final long maxDocLen, final int maxNumLen,
        final int maxStringLen) {
        this(maxNestingDepth, DEFAULT_MAX_DOC_LEN, maxNumLen, maxStringLen, DEFAULT_MAX_NAME_LEN);
    }

    /**
     * @param maxNestingDepth Maximum input document nesting to allow
     * @param maxDocLen Maximum input document length to allow
     * @param maxNumLen Maximum number representation length to allow
     * @param maxStringLen Maximum String value length to allow
     * @param maxNameLen Maximum Object property name length to allow
     *
     * @since 2.16
     */
    protected StreamReadConstraints(final int maxNestingDepth, final long maxDocLen, final int maxNumLen,
        final int maxStringLen, final int maxNameLen) {
        _maxNestingDepth = maxNestingDepth;
        _maxDocLen = maxDocLen;
        _maxNumLen = maxNumLen;
        _maxStringLen = maxStringLen;
        _maxNameLen = maxNameLen;
    }

    public static Builder builder() {
        return new Builder();
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

    /*
    /**********************************************************************
    /* Convenience methods for validation, token lengths
    /**********************************************************************
     */

    /*
    /**********************************************************************
    /* Convenience methods for validation, other
    /**********************************************************************
     */

    /*
    /**********************************************************************
    /* Error reporting
    /**********************************************************************
     */

}
