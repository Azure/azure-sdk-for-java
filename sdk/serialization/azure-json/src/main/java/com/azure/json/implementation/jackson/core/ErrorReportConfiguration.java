// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core;

import java.io.Serializable;

/**
 * Container for configuration values used when handling errorneous token inputs. 
 * For example, unquoted text segments.
 * <p>
 * Currently default settings are
 * <ul>
 *     <li>Maximum length of token to include in error messages (see {@link #_maxErrorTokenLength})
 *     <li>Maximum length of raw content to include in error messages (see {@link #_maxRawContentLength})
 * </ul>
 *
 * @since 2.16
 */
public class ErrorReportConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Default value for {@link #_maxErrorTokenLength}.
     */
    public static final int DEFAULT_MAX_ERROR_TOKEN_LENGTH = 256;

    /**
     * Previously was {@code com.fasterxml.jackson.core.io.ContentReference#DEFAULT_MAX_CONTENT_SNIPPET}.
     * Default value for {@link #_maxRawContentLength}.
     */
    public static final int DEFAULT_MAX_RAW_CONTENT_LENGTH = 500;

    /**
     * Maximum length of token to include in error messages
     *
     * @see Builder#maxErrorTokenLength(int)
     */
    protected final int _maxErrorTokenLength;

    /**
     * Maximum length of raw content to include in error messages
     * 
     * @see Builder#maxRawContentLength(int) 
     */
    protected final int _maxRawContentLength;

    private static ErrorReportConfiguration DEFAULT
        = new ErrorReportConfiguration(DEFAULT_MAX_ERROR_TOKEN_LENGTH, DEFAULT_MAX_RAW_CONTENT_LENGTH);

    /**
     * Override the default ErrorReportConfiguration. These defaults are only used when {@link JsonFactory}
     * instances are not configured with their own ErrorReportConfiguration.
     * <p>
     * Library maintainers should not set this as it will affect other code that uses Jackson.
     * Library maintainers who want to configure ErrorReportConfiguration for the Jackson usage within their
     * lib should create <code>ObjectMapper</code> instances that have a {@link JsonFactory} instance with
     * the required ErrorReportConfiguration.
     * <p>
     * This method is meant for users delivering applications. If they use this, they set it when they start
     * their application to avoid having other code initialize their mappers before the defaults are overridden.
     *
     * @param errorReportConfiguration new default for ErrorReportConfiguration (a null value will reset to built-in default)
     * @see #defaults()
     * @see #builder()
     */
    public static void
        overrideDefaultErrorReportConfiguration(final ErrorReportConfiguration errorReportConfiguration) {
        if (errorReportConfiguration == null) {
            DEFAULT = new ErrorReportConfiguration(DEFAULT_MAX_ERROR_TOKEN_LENGTH, DEFAULT_MAX_RAW_CONTENT_LENGTH);
        } else {
            DEFAULT = errorReportConfiguration;
        }
    }

    /*
    /**********************************************************************
    /* Builder
    /**********************************************************************
     */

    public static final class Builder {
        private int maxErrorTokenLength;
        private int maxRawContentLength;

        /**
         * @param maxErrorTokenLength Maximum error token length setting to use
         *
         * @return This factory instance (to allow call chaining)
         *
         * @throws IllegalArgumentException if {@code maxErrorTokenLength} is less than 0
         */
        public Builder maxErrorTokenLength(final int maxErrorTokenLength) {
            validateMaxErrorTokenLength(maxErrorTokenLength);
            this.maxErrorTokenLength = maxErrorTokenLength;
            return this;
        }

        /**
         * @param maxRawContentLength Maximum raw content setting to use
         * 
         * @see ErrorReportConfiguration#_maxRawContentLength
         *
         * @return This builder instance (to allow call chaining)
         */
        public Builder maxRawContentLength(final int maxRawContentLength) {
            validateMaxRawContentLength(maxRawContentLength);
            this.maxRawContentLength = maxRawContentLength;
            return this;
        }

        Builder() {
            this(DEFAULT_MAX_ERROR_TOKEN_LENGTH, DEFAULT_MAX_RAW_CONTENT_LENGTH);
        }

        Builder(final int maxErrorTokenLength, final int maxRawContentLength) {
            this.maxErrorTokenLength = maxErrorTokenLength;
            this.maxRawContentLength = maxRawContentLength;
        }

        Builder(ErrorReportConfiguration src) {
            this.maxErrorTokenLength = src._maxErrorTokenLength;
            this.maxRawContentLength = src._maxRawContentLength;
        }

        public ErrorReportConfiguration build() {
            return new ErrorReportConfiguration(maxErrorTokenLength, maxRawContentLength);
        }
    }

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    protected ErrorReportConfiguration(final int maxErrorTokenLength, final int maxRawContentLength) {
        _maxErrorTokenLength = maxErrorTokenLength;
        _maxRawContentLength = maxRawContentLength;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * @return the default {@link ErrorReportConfiguration} (when none is set on the {@link JsonFactory} explicitly)
     * @see #overrideDefaultErrorReportConfiguration(ErrorReportConfiguration)
     */
    public static ErrorReportConfiguration defaults() {
        return DEFAULT;
    }

    /**
     * @return New {@link Builder} initialized with settings of configuration instance
     */
    public Builder rebuild() {
        return new Builder(this);
    }

    /*
    /**********************************************************************
    /* Accessors
    /**********************************************************************
     */

    /**
     * Accessor for {@link #_maxErrorTokenLength}
     *
     * @return Maximum length of token to include in error messages
     * @see Builder#maxErrorTokenLength(int)
     */
    public int getMaxErrorTokenLength() {
        return _maxErrorTokenLength;
    }

    /**
     * Accessor for {@link #_maxRawContentLength}
     *
     * @return Maximum length of token to include in error messages
     * @see Builder#maxRawContentLength(int)
     */
    public int getMaxRawContentLength() {
        return _maxRawContentLength;
    }

    /*
    /**********************************************************************
    /* Convenience methods for validation
    /**********************************************************************
     */

    /**
     * Convenience method that can be used verify valid {@link #_maxErrorTokenLength}.
     * If invalid value is passed in, {@link IllegalArgumentException} is thrown.
     *
     * @param maxErrorTokenLength Maximum length of token to include in error messages
     */
    static void validateMaxErrorTokenLength(int maxErrorTokenLength) throws IllegalArgumentException {
        if (maxErrorTokenLength < 0) {
            throw new IllegalArgumentException(
                String.format("Value of maxErrorTokenLength (%d) cannot be negative", maxErrorTokenLength));
        }
    }

    static void validateMaxRawContentLength(int maxRawContentLength) {
        if (maxRawContentLength < 0) {
            throw new IllegalArgumentException(
                String.format("Value of maxRawContentLength (%d) cannot be negative", maxRawContentLength));
        }
    }

}
