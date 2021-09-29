// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.util.logging.ClientLogger;

/**
 * This class represents an HTTP ETag. A ETag value could be strong or weak ETag.
 * More information, check https://en.wikipedia.org/wiki/HTTP_ETag
 */
public final class ETag {
    private static final ClientLogger LOGGER = new ClientLogger(ETag.class);

    private static final String QUOTE_STRING = "\"";
    private static final String WEAK_ETAG_PREFIX_QUOTE = "W/\"";

    /**
     * The asterisk is a special value representing any resource.
     */
    public static final ETag ALL = new ETag("*");

    private final String eTag;

    /**
     * Creates a new instance of {@link ETag}.
     *
     * @param eTag The HTTP entity tag string value.
     */
    public ETag(String eTag) {
        checkValidETag(eTag);
        this.eTag = eTag;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof ETag)) {
            return false;
        }

        ETag oETag = (ETag) o;

        return this.eTag.equals(oETag.eTag);
    }

    @Override
    public int hashCode() {
        return this.eTag.hashCode();
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

        if (!((eTag.startsWith(QUOTE_STRING) || eTag.startsWith(WEAK_ETAG_PREFIX_QUOTE))
                  && eTag.endsWith(QUOTE_STRING))) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(String.format(
                "The value=%s should be equal to * , be wrapped in quotes, or be wrapped in quotes prefixed by W/",
                eTag)));
        }
    }
}
