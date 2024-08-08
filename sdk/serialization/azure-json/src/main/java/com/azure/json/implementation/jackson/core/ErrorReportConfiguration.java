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
     */
    protected final int _maxErrorTokenLength;

    /**
     * Maximum length of raw content to include in error messages
     */
    protected final int _maxRawContentLength;

    private static final ErrorReportConfiguration DEFAULT
        = new ErrorReportConfiguration(DEFAULT_MAX_ERROR_TOKEN_LENGTH, DEFAULT_MAX_RAW_CONTENT_LENGTH);

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    protected ErrorReportConfiguration(final int maxErrorTokenLength, final int maxRawContentLength) {
        _maxErrorTokenLength = maxErrorTokenLength;
        _maxRawContentLength = maxRawContentLength;
    }

    /**
     * @return the default {@link ErrorReportConfiguration} (when none is set on the {@link JsonFactory} explicitly)
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
     */
    public int getMaxErrorTokenLength() {
        return _maxErrorTokenLength;
    }

    /**
     * Accessor for {@link #_maxRawContentLength}
     *
     * @return Maximum length of token to include in error messages
     */
    public int getMaxRawContentLength() {
        return _maxRawContentLength;
    }
}
