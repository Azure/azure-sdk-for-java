// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core;

import com.azure.json.implementation.jackson.core.exc.StreamConstraintsException;

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

    private static final StreamReadConstraints DEFAULT = new StreamReadConstraints(DEFAULT_MAX_DEPTH,
        DEFAULT_MAX_DOC_LEN, DEFAULT_MAX_NUM_LEN, DEFAULT_MAX_STRING_LEN, DEFAULT_MAX_NAME_LEN);

    public static final class Builder {
        private final long maxDocLen;
        private final int maxNestingDepth;
        private final int maxNumLen;
        private int maxStringLen;
        private final int maxNameLen;

        /**
         * Sets the maximum string length (in chars or bytes, depending on input context).
         * The default is 20,000,000. This limit is not exact, the limit is applied when we increase
         * internal buffer sizes and an exception will happen at sizes greater than this limit. Some
         * text values that are a little bigger than the limit may be treated as valid but no text
         * values with sizes less than or equal to this limit will be treated as invalid.
         * <p>
         *   Setting this value to lower than the {@link #maxNumberLength(int)} is not recommended.
         * </p>
         *<p>
         * NOTE: Jackson 2.15.0 initially used a lower setting (5_000_000).
         *
         * @param maxStringLen the maximum string length (in chars or bytes, depending on input context)
         *
         * @return this builder
         * @throws IllegalArgumentException if the maxStringLen is set to a negative value
         */
        public Builder maxStringLength(final int maxStringLen) {
            if (maxStringLen < 0) {
                throw new IllegalArgumentException("Cannot set maxStringLen to a negative value");
            }
            this.maxStringLen = maxStringLen;
            return this;
        }

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

    /**
     * @return the default {@link StreamReadConstraints} (when none is set on the {@link JsonFactory} explicitly)
     * @see #overrideDefaultStreamReadConstraints
     */
    public static StreamReadConstraints defaults() {
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
            throw _constructException("Document nesting depth (%d) exceeds the maximum allowed (%d, from %s)", depth,
                _maxNestingDepth, _constrainRef("getMaxNestingDepth"));
        }
    }

    /**
     * Convenience method that can be used to verify that the
     * document length does not exceed the maximum specified by this
     * constraints object (if any): if it does, a
     * {@link StreamConstraintsException}
     * is thrown.
     *
     * @param len Current length of processed document content
     *
     * @throws StreamConstraintsException If length exceeds maximum
     *
     * @since 2.16
     */
    public void validateDocumentLength(long len) throws StreamConstraintsException {
        if ((len > _maxDocLen)
            // Note: -1L used as marker for "unlimited"
            && (_maxDocLen > 0L)) {
            throw _constructException("Document length (%d) exceeds the maximum allowed (%d, from %s)", len, _maxDocLen,
                _constrainRef("getMaxDocumentLength"));
        }
    }

    /*
    /**********************************************************************
    /* Convenience methods for validation, token lengths
    /**********************************************************************
     */

    /**
     * Convenience method that can be used to verify that a floating-point
     * number of specified length does not exceed maximum specified by this
     * constraints object: if it does, a
     * {@link StreamConstraintsException}
     * is thrown.
     *
     * @param length Length of number in input units
     *
     * @throws StreamConstraintsException If length exceeds maximum
     */
    public void validateFPLength(int length) throws StreamConstraintsException {
        if (length > _maxNumLen) {
            throw _constructException("Number value length (%d) exceeds the maximum allowed (%d, from %s)", length,
                _maxNumLen, _constrainRef("getMaxNumberLength"));
        }
    }

    /**
     * Convenience method that can be used to verify that an integer
     * number of specified length does not exceed maximum specific by this
     * constraints object: if it does, a
     * {@link StreamConstraintsException}
     * is thrown.
     *
     * @param length Length of number in input units
     *
     * @throws StreamConstraintsException If length exceeds maximum
     */
    public void validateIntegerLength(int length) throws StreamConstraintsException {
        if (length > _maxNumLen) {
            throw _constructException("Number value length (%d) exceeds the maximum allowed (%d, from %s)", length,
                _maxNumLen, _constrainRef("getMaxNumberLength"));
        }
    }

    /**
     * Convenience method that can be used to verify that a String
     * of specified length does not exceed maximum specific by this
     * constraints object: if it does, a
     * {@link StreamConstraintsException}
     * is thrown.
     *
     * @param length Length of string in input units
     *
     * @throws StreamConstraintsException If length exceeds maximum
     */
    public void validateStringLength(int length) throws StreamConstraintsException {
        if (length > _maxStringLen) {
            throw _constructException("String value length (%d) exceeds the maximum allowed (%d, from %s)", length,
                _maxStringLen, _constrainRef("getMaxStringLength"));
        }
    }

    /**
     * Convenience method that can be used to verify that a name
     * of specified length does not exceed maximum specific by this
     * constraints object: if it does, a
     * {@link StreamConstraintsException}
     * is thrown.
     *
     * @param length Length of name in input units
     *
     * @throws StreamConstraintsException If length exceeds maximum
     */
    public void validateNameLength(int length) throws StreamConstraintsException {
        if (length > _maxNameLen) {
            throw _constructException("Name length (%d) exceeds the maximum allowed (%d, from %s)", length, _maxNameLen,
                _constrainRef("getMaxNameLength"));
        }
    }

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

    // @since 2.16
    protected StreamConstraintsException _constructException(String msgTemplate, Object... args)
        throws StreamConstraintsException {
        throw new StreamConstraintsException(String.format(msgTemplate, args));
    }

    // @since 2.16
    protected String _constrainRef(String method) {
        return "`StreamReadConstraints." + method + "()`";
    }
}
