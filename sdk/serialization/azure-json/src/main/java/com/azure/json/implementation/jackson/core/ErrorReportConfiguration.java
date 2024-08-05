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

    private static final ErrorReportConfiguration DEFAULT
        = new ErrorReportConfiguration(DEFAULT_MAX_ERROR_TOKEN_LENGTH, DEFAULT_MAX_RAW_CONTENT_LENGTH);

    /*
    /**********************************************************************
    /* Builder
    /**********************************************************************
     */

    public static final class Builder {
        private final int maxErrorTokenLength;
        private final int maxRawContentLength;

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

}
