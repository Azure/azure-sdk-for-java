// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.implementation.util;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Storage Data Lake whitelist headers and parameters for log options.
 */
public class DataLakeHeadersAndQueryParameters {
    private static final Set<String> DATALAKE_HEADERS = Stream.of(
        "Access-Control-Allow-Origin", "Cache-Control", "Content-Length", "Content-Type", "Date", "Request-Id",
        "traceparent", "Transfer-Encoding", "User-Agent", "x-ms-client-request-id", "x-ms-date", "x-ms-error-code",
        "x-ms-request-id", "x-ms-return-client-request-id", "x-ms-version", "Accept-Ranges", "Content-Disposition",
        "Content-Encoding", "Content-Language", "Content-MD5", "Content-Range", "ETag", "Last-Modified", "Server",
        "Vary", "x-ms-content-crc64", "x-ms-copy-action", "x-ms-copy-completion-time", "x-ms-copy-id",
        "x-ms-copy-progress", "x-ms-copy-status", "x-ms-has-immutability-policy", "x-ms-has-legal-hold",
        "x-ms-lease-state", "x-ms-lease-status", "x-ms-range", "x-ms-request-server-encrypted",
        "x-ms-server-encrypted", "x-ms-snapshot", "x-ms-source-range", "If-Match", "If-Modified-Since",
        "If-None-Match", "If-Unmodified-Since", "x-ms-access-tier", "x-ms-access-tier-change-time",
        "x-ms-access-tier-inferred", "x-ms-account-kind", "x-ms-archive-status", "x-ms-blob-append-offset",
        "x-ms-blob-cache-control", "x-ms-blob-committed-block-count", "x-ms-blob-condition-appendpos",
        "x-ms-blob-condition-maxsize", "x-ms-blob-content-disposition", "x-ms-blob-content-encoding",
        "x-ms-blob-content-language", "x-ms-blob-content-length", "x-ms-blob-content-md5", "x-ms-blob-content-type",
        "x-ms-blob-public-access", "x-ms-blob-sequence-number", "x-ms-blob-type", "x-ms-copy-destination-snapshot",
        "x-ms-creation-time", "x-ms-default-encryption-scope", "x-ms-delete-snapshots",
        "x-ms-delete-type-permanent", "x-ms-deny-encryption-scope-override", "x-ms-encryption-algorithm",
        "x-ms-if-sequence-number-eq", "x-ms-if-sequence-number-le", "x-ms-if-sequence-number-lt",
        "x-ms-incremental-copy", "x-ms-lease-action", "x-ms-lease-break-period", "x-ms-lease-duration",
        "x-ms-lease-id", "x-ms-lease-time", "x-ms-page-write", "x-ms-proposed-lease-id",
        "x-ms-range-get-content-md5", "x-ms-rehydrate-priority", "x-ms-sequence-number-action", "x-ms-sku-name",
        "x-ms-source-content-md5", "x-ms-source-if-match", "x-ms-source-if-modified-since",
        "x-ms-source-if-none-match", "x-ms-source-if-unmodified-since", "x-ms-tag-count",
        "x-ms-encryption-key-sha256")
        .collect(Collectors.toCollection(HashSet::new));

    /**
     * Gets the Storage Data Lake whitelist headers for log.
     *
     * @return the list of Storage Data Lake whitelist headers.
     */
    public static Set<String> getDataLakeHeaders() {
        return DATALAKE_HEADERS;
    }

    private static final Set<String> DATALAKE_QUERY_PARAMETERS = Stream.of(
        "comp", "maxresults", "rscc", "rscd", "rsce", "rscl", "rsct", "se", "si", "sip", "sp", "spr", "sr", "srt",
        "ss", "st", "sv", "include", "marker", "prefix", "copyid", "restype", "blockid", "blocklisttype",
        "delimiter", "prevsnapshot", "ske", "skoid", "sks", "skt", "sktid", "skv", "snapshot")
        .collect(Collectors.toCollection(HashSet::new));

    /**
     * Gets the Storage Data Lake whitelist query parameters for log.
     *
     * @return the list of Storage Data Lake whitelist query parameters.
     */
    public static Set<String> getDataLakeQueryParameters() {
        return DATALAKE_QUERY_PARAMETERS;
    }

}
