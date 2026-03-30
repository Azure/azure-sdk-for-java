// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.logging.ClientLogger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing helpers and download eligibility checks
 * used by {@link StorageContentValidationDecoderPolicy}.
 */
final class ContentValidationDecoderUtils {
    private static final ClientLogger LOGGER = new ClientLogger(ContentValidationDecoderUtils.class);

    static final String RETRY_OFFSET_TOKEN = "RETRY-START-OFFSET=";
    private static final Pattern RETRY_OFFSET_PATTERN = Pattern.compile("RETRY-START-OFFSET=(\\d+)");
    private static final Pattern DECODER_OFFSETS_PATTERN
        = Pattern.compile("\\[decoderOffset=(\\d+),lastCompleteSegment=(\\d+)\\]");

    private ContentValidationDecoderUtils() {
    }

    /**
     * Parses the retry start offset from an exception message containing the RETRY-START-OFFSET token.
     *
     * @param message The exception message to parse.
     * @return The retry start offset, or -1 if not found.
     */
    static long parseRetryStartOffset(String message) {
        if (message == null) {
            return -1;
        }
        Matcher matcher = RETRY_OFFSET_PATTERN.matcher(message);
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    /**
     * Parses decoder offset information from enriched exception messages.
     * Format: "[decoderOffset=X,lastCompleteSegment=Y]"
     *
     * @param message The exception message to parse.
     * @return A long array [decoderOffset, lastCompleteSegment], or null if not found.
     */
    static long[] parseDecoderOffsets(String message) {
        if (message == null) {
            return null;
        }
        Matcher matcher = DECODER_OFFSETS_PATTERN.matcher(message);
        if (matcher.find()) {
            try {
                return new long[] { Long.parseLong(matcher.group(1)), Long.parseLong(matcher.group(2)) };
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
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
