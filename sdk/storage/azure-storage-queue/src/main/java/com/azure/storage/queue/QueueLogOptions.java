// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.core.http.policy.HttpLogOptions;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The log configuration for Storage Queue.
 */
public final class QueueLogOptions extends HttpLogOptions {
    private static final Set<String> QUEUE_HEADERS = Stream.of(
        "Access-Control-Allow-Origin", "Cache-Control", "Content-Length", "Content-Type", "Date", "Request-Id",
        "traceparent", "Transfer-Encoding", "User-Agent", "x-ms-client-request-id", "x-ms-date", "x-ms-error-code",
        "x-ms-request-id", "x-ms-return-client-request-id", "x-ms-version", "x-ms-approximate-messages-count",
        "x-ms-popreceipt", "x-ms-time-next-visible")
        .collect(Collectors.toCollection(HashSet::new));


    private static final Set<String> QUEUE_QUERY_PARAMETERS = Stream.of(
        "comp", "maxresults", "rscc", "rscd", "rsce", "rscl", "rsct", "se", "si", "sip", "sp", "spr", "sr", "srt",
        "ss", "st", "sv", "include", "marker", "prefix", "messagettl", "numofmessages", "peekonly", "popreceipt",
        "visibilitytimeout")
        .collect(Collectors.toCollection(HashSet::new));

    /**
     * Constructor for combining core and Storage Queue allowed whitelist headers and queries.
     */
    public QueueLogOptions() {
        super.getAllowedHeaderNames().addAll(QUEUE_HEADERS);
        super.getAllowedQueryParamNames().addAll(QUEUE_QUERY_PARAMETERS);
    }

    /**
     * Gets the whitelisted headers that should be logged.
     *
     * @return The list of whitelisted headers of Storage Queue.
     */
    public Set<String> getQueueHeaders() {
        return QUEUE_HEADERS;
    }

    /**
     * Gets the whitelisted query parameters.
     *
     * @return The list of whitelisted query parameters of Storage Queue.
     */
    public Set<String> getQueueQueryParameters() {
        return QUEUE_QUERY_PARAMETERS;
    }
}
