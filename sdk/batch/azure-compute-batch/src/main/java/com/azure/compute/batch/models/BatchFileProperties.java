// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.models;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;

/**
 * Class representing the properties of a file on a Compute Node or Task in Azure Batch.
 * The properties are extracted from the HTTP response headers received from a Batch service request.
 */
public final class BatchFileProperties {
    private final HttpHeaders headers;

    private BatchFileProperties(HttpHeaders headers) {
        this.headers = headers;
    }

    /**
     * Creates a {@link BatchFileProperties} instance from the HTTP response headers returned by a Batch service
     * request.
     *
     * @param headers The {@link HttpHeaders} containing the headers from which to extract file properties.
     * @return A new {@link BatchFileProperties} instance populated from the provided headers.
     */
    public static BatchFileProperties fromHeaders(HttpHeaders headers) {
        return new BatchFileProperties(headers);
    }

    /**
     * Retrieves the creation time of the file.
     *
     * @return The creation time of the file as an OffsetDateTime object, or null if the header is not present.
     */
    public OffsetDateTime getOcpCreationTime() {
        String headerValue = headers.getValue(HttpHeaderName.fromString("ocp-creation-time"));
        return headerValue != null ? OffsetDateTime.parse(headerValue, DateTimeFormatter.RFC_1123_DATE_TIME) : null;
    }

    /**
     * Determines whether the file object represents a directory.
     *
     * @return True if the file object represents a directory, false otherwise.
     */
    public boolean isDirectory() {
        String headerValue = headers.getValue(HttpHeaderName.fromString("ocp-batch-file-isdirectory"));
        return Boolean.parseBoolean(headerValue);
    }

    /**
     * Retrieves the URL of the file.
     *
     * @return The URL of the file as a string.
     */
    public String getOcpBatchFileUrl() {
        return headers.getValue(HttpHeaderName.fromString("ocp-batch-file-url"));
    }

    /**
     * Retrieves the file mode attribute in octal format.
     *
     * @return The file mode attribute of the file as a string in octal format.
     */
    public String getOcpBatchFileMode() {
        return headers.getValue(HttpHeaderName.fromString("ocp-batch-file-mode"));
    }

    /**
     * Retrieves the length of the file.
     *
     * @return The length of the file as a long. If the header is not present or not parseable, returns 0.
     */
    public long getContentLength() {
        String value = headers.getValue(HttpHeaderName.CONTENT_LENGTH);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            // Log or handle the error as appropriate
            return 0; // Default to 0 if the header is not a valid long
        }
    }
}
