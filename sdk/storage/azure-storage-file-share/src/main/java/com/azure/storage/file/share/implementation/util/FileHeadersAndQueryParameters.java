// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation.util;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Storage File whitelist headers and parameters for log options.
 */
public class FileHeadersAndQueryParameters {
    private static final Set<String> FILE_HEADERS = Stream.of(
        "Access-Control-Allow-Origin", "Cache-Control", "Content-Length", "Content-Type", "Date", "Request-Id",
        "traceparent", "Transfer-Encoding", "User-Agent", "x-ms-client-request-id", "x-ms-date", "x-ms-error-code",
        "x-ms-request-id", "x-ms-return-client-request-id", "x-ms-version", "Accept-Ranges", "Content-Disposition",
        "Content-Encoding", "Content-Language", "Content-MD5", "Content-Range", "ETag", "Last-Modified", "Server",
        "Vary", "x-ms-content-crc64", "x-ms-copy-action", "x-ms-copy-completion-time", "x-ms-copy-id",
        "x-ms-copy-progress", "x-ms-copy-status", "x-ms-has-immutability-policy", "x-ms-has-legal-hold",
        "x-ms-lease-state", "x-ms-lease-status", "x-ms-range", "x-ms-request-server-encrypted",
        "x-ms-server-encrypted", "x-ms-snapshot", "x-ms-source-range", "x-ms-cache-control",
        "x-ms-content-disposition", "x-ms-content-encoding", "x-ms-content-language", "x-ms-content-length",
        "x-ms-content-md5", "x-ms-content-type", "x-ms-file-attributes", "x-ms-file-change-time",
        "x-ms-file-creation-time", "x-ms-file-id", "x-ms-file-last-write-time", "x-ms-file-parent-id",
        "x-ms-handle-id", "x-ms-number-of-handles-closed", "x-ms-recursive", "x-ms-share-quota", "x-ms-type",
        "x-ms-write")
        .collect(Collectors.toCollection(HashSet::new));

    /**
     * Gets the Storage File whitelist headers for log.
     *
     * @return the list of Storage File whitelist headers.
     */
    public static Set<String> getFileHeaders() {
        return FILE_HEADERS;
    }

    private static final Set<String> FILE_QUERY_PARAMETERS = Stream.of(
        "comp", "maxresults", "rscc", "rscd", "rsce", "rscl", "rsct", "se", "si", "sip", "sp", "spr", "sr", "srt",
        "ss", "st", "sv", "copyid", "restype")
        .collect(Collectors.toCollection(HashSet::new));

    /**
     * Gets the Storage File whitelist query parameters for log.
     *
     * @return the list of Storage File whitelist query parameters.
     */
    public static Set<String> getFileQueryParameters() {
        return FILE_QUERY_PARAMETERS;
    }
}
