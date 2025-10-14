// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation;

/**
 * This class contains the names of the logging events that are emitted by the client core.
 */
public class LoggingEventNames {
    // HTTP logging event names. None of them are defined in otel semantic conventions.
    /**
     * Identifies event that is logged when an HTTP request is sent.
     * Depending on configuration and implementation, this event may be logged when request headers are sent or when
     * the request body is fully written.
     */
    public static final String HTTP_REQUEST_EVENT_NAME = "http.request";

    /**
     * Identifies event that is logged when an HTTP response is received.
     * Depending on configuration and implementation, this event may be logged when response headers and status code
     * are received or when the response body is fully read.
     */
    public static final String HTTP_RESPONSE_EVENT_NAME = "http.response";

    /**
     * Identifies event that is logged when an HTTP request is being redirected to another URL.
     * The event describes whether the redirect will be followed or not along with redirect context.
     */
    public static final String HTTP_REDIRECT_EVENT_NAME = "http.redirect";

    /**
     * Identifies event that is logged after an HTTP request has failed and is considered to be retried.
     * The event describes whether the retry will be performed or not.
     */
    public static final String HTTP_RETRY_EVENT_NAME = "http.retry";

    // Other logging event names

    /**
     * Identifies event that is logged when a span is ended.
     */
    public static final String SPAN_ENDED_EVENT_NAME = "span.ended";
}
