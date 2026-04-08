// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.logging.ClientLogger;

/**
 * Utility class for download eligibility checks
 * used by {@link StorageContentValidationDecoderPolicy}.
 */
final class ContentValidationDecoderUtils {
    private static final ClientLogger LOGGER = new ClientLogger(ContentValidationDecoderUtils.class);

    private ContentValidationDecoderUtils() {
    }

    /**
     * Checks whether the response represents a successful download (GET with 2xx).
     */
    static boolean isDownloadResponse(HttpResponse response) {
        return response.getRequest().getHttpMethod() == HttpMethod.GET && response.getStatusCode() / 100 == 2;
    }

    /**
     * Extracts Content-Length from the response headers.
     *
     * @return The content length, or null if absent or unparseable.
     */
    static Long getContentLength(HttpHeaders headers) {
        String value = headers.getValue(HttpHeaderName.CONTENT_LENGTH);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid content length in response headers: " + value);
            }
        }
        return null;
    }

    /**
     * Returns {@code true} when the response is a download response with a positive content length,
     * making it eligible for structured message decoding.
     */
    static boolean isEligibleDownload(HttpResponse response, Long contentLength) {
        return isDownloadResponse(response) && contentLength != null && contentLength > 0;
    }
}
