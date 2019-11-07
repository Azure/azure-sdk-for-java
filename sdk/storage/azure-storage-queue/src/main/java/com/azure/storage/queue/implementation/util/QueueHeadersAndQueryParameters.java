// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue.implementation.util;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Storage Queue whitelist headers and parameters for log options.
 */
public class QueueHeadersAndQueryParameters {
    private static final Set<String> QUEUE_HEADERS = Stream.of(
        "Access-Control-Allow-Origin", "Cache-Control", "Content-Length", "Content-Type", "Date", "Request-Id",
        "traceparent", "Transfer-Encoding", "User-Agent", "x-ms-client-request-id", "x-ms-date", "x-ms-error-code",
        "x-ms-request-id", "x-ms-return-client-request-id", "x-ms-version", "x-ms-approximate-messages-count",
        "x-ms-popreceipt", "x-ms-time-next-visible")
        .collect(Collectors.toCollection(HashSet::new));

    /**
     * Gets the Storage Queue whitelist headers for log.
     *
     * @return the list of Storage Queue whitelist headers.
     */
    public static Set<String> getQueueHeaders() {
        return QUEUE_HEADERS;
    }

    private static final Set<String> QUEUE_QUERY_PARAMETERS = Stream.of(
        "comp", "maxresults", "rscc", "rscd", "rsce", "rscl", "rsct", "se", "si", "sip", "sp", "spr", "sr", "srt",
        "ss", "st", "sv", "include", "marker", "prefix", "messagettl", "numofmessages", "peekonly", "popreceipt",
        "visibilitytimeout")
        .collect(Collectors.toCollection(HashSet::new));

    /**
     * Gets the Storage Queue whitelist query parameters for log.
     *
     * @return the list of Storage Queue whitelist query parameters.
     */
    public static Set<String> getQueueQueryParameters() {
        return QUEUE_QUERY_PARAMETERS;
    }
}
