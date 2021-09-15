// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.util.logging.ClientLogger;

/**
 * This class represents an HTTP ETag.
 */
public final class ETag {
    private static final ClientLogger LOGGER = new ClientLogger(ETag.class);

    private static final String EMPTY_STRING = "";
    private static final String QUOTE_STRING = "\"";
    private static final String WEAK_ETAG_PREFIX_QUOTE = "W/\"";

    public static final String DEFAULT_FORMAT = "G";
    public static final String HEADER_FORMAT = "H";
    public static final ETag ALL = new ETag("*");

    private final String eTag;

    /**
     * Creates a new instance of {@link ETag}.
     *
     * @param eTag The eTag string value.
     */
    public ETag(String eTag) {
        checkValidETag(eTag);
        this.eTag = eTag;
    }

    /**
     * It returns the ETag value in specific format. If {@link #DEFAULT_FORMAT}, it turns the original {@code eTag}
     * value. If {@link #HEADER_FORMAT}, the {@code eTag} value is quoted. For example, if eTag = 12345,
     * it returns as it is if DEFAULT_FORMAT, "12345" if HEADER_FORMAT.
     *
     * @param format A valid format value is {@link #DEFAULT_FORMAT} and {@link #HEADER_FORMAT}
     *
     * @return The ETag value in specific format.
     */
    public String toString(String format) {
        if (format == null) {
            return EMPTY_STRING;
        }
        // TODO: what if weak ETag?
        if (DEFAULT_FORMAT.equals(format)) {
            return eTag;
        } else if (HEADER_FORMAT.equals(format)) {
            return String.format("\"%s\"", eTag);
        }

        throw LOGGER.logExceptionAsError(new IllegalArgumentException(
            String.format("Invalid format string, \"%s\".", format)));
    }

    /**
     * Checks if the {@code eTag} a valid ETag value. Valid ETags show below,
     *  - The special character, '*'.
     *  - A strong ETag, which the value is wrapped in quotes, ex, "12345".
     *  - A weak ETag, which value is wrapped in quotes and prefixed by "W/", ex, W/"12345".
     *
     * @param eTag ETag string value.
     */
    private void checkValidETag(String eTag) {
        if (eTag == null || ALL.equals(eTag))
            return;

        if ((eTag.startsWith(QUOTE_STRING) || eTag.startsWith(WEAK_ETAG_PREFIX_QUOTE))
                && eTag.endsWith(QUOTE_STRING)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(String.format(
                "The value=%s should be equal to * , be wrapped in quotes, or be wrapped in quotes prefixed by W/",
                eTag)));
        }
    }
}
