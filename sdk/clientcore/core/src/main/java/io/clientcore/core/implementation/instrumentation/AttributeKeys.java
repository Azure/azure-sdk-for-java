// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation;

/**
 * Constants used as keys in semantic logging, tracing, metrics (mostly) following
 * <a href="https://github.com/open-telemetry/semantic-conventions">OpenTelemetry semantic conventions</a>.
 * <p>
 * These keys unify how core logs HTTP requests, responses or anything
 * else and simplify telemetry analysis.
 * <p>
 * When reporting in client libraries, please do the best effort to stay consistent with these keys, but copy the value.
 */
public final class AttributeKeys {
    // Standard attribute names (defined in OpenTelemetry semantic conventions)

    /**
     * A class of error the operation ended with such as a fully-qualified exception type or a domain-specific error code.
     * <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/attributes-registry/error.md#error-type">error.type attribute</a>
     */
    public static final String ERROR_TYPE_KEY = "error.type";

    /**
     * Exception message.
     * <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/attributes-registry/exception.md#exception-message">exception.message attribute</a>
     */
    public static final String EXCEPTION_MESSAGE_KEY = "exception.message";

    /**
     * Exception stacktrace.
     * <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/attributes-registry/exception.md#exception-stacktrace">exception.stacktrace attribute</a>
     */
    public static final String EXCEPTION_STACKTRACE_KEY = "exception.stacktrace";

    /**
     * Exception type.
     * <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/attributes-registry/exception.md#exception-type">exception.type attribute</a>
     */
    public static final String EXCEPTION_TYPE_KEY = "exception.type";

    /**
     * The name of the logging event.
     * <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/attributes-registry/event.md#event-name">event.name attribute</a>
     */
    public static final String EVENT_NAME_KEY = "event.name";

    /**
     * The HTTP request method.
     * <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/attributes-registry/http.md#http-request-method">http.request.method attribute</a>
     */
    public static final String HTTP_REQUEST_METHOD_KEY = "http.request.method";

    /**
     * The ordinal number of request resending attempt (for any reason, including redirects)
     * The value starts with {@code 0} on the first try
     * and should be an {@code int} number.
     * <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/attributes-registry/http.md#http-request-resend-count">http.request.resend_count attribute</a>
     */
    public static final String HTTP_REQUEST_RESEND_COUNT_KEY = "http.request.resend_count";

    /**
     * The size of the request payload body in bytes. It usually matches the value of the Content-Length header.
     * <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/attributes-registry/http.md#http-request-body-size">http.request.body.size attribute</a>
     */
    public static final String HTTP_REQUEST_BODY_SIZE_KEY = "http.request.body.size";

    /**
     * The value of request content length header.
     * <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/attributes-registry/http.md#http-request-header-content-length">http.request.header.content-length attribute</a>
     */
    public static final String HTTP_REQUEST_HEADER_CONTENT_LENGTH_KEY = "http.request.header.content-length";

    /**
     * The value of request traceparent header.
     * <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/attributes-registry/http.md#http-request-header-content-length">http.request.header.content-length attribute</a>
     */
    public static final String HTTP_REQUEST_HEADER_TRACEPARENT_KEY = "http.request.header.traceparent";

    /**
     * The value of response content length header.
     * <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/attributes-registry/http.md#http-response-header">http.response.header.content-length attribute</a>
     */
    public static final String HTTP_RESPONSE_HEADER_CONTENT_LENGTH_KEY = "http.response.header.content-length";

    /**
     * The value of response content type header.
     * <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/attributes-registry/http.md#http-response-header">http.response.header.content-type attribute</a>
     */
    public static final String HTTP_RESPONSE_HEADER_CONTENT_TYPE_KEY = "http.response.header.content-type";

    /**
     * The value of response location header indicating the URL to redirect to.
     * <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/attributes-registry/http.md#http-response-header">http.response.header.location attribute</a>
     */
    public static final String HTTP_RESPONSE_HEADER_LOCATION_KEY = "http.response.header.location";

    /**
     * The size of the response payload body in bytes. It usually matches the value of the Content-Length header.
     * <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/attributes-registry/http.md#http-response-body-size">http.response.body.size attribute</a>
     */
    public static final String HTTP_RESPONSE_BODY_SIZE_KEY = "http.response.body.size";

    /**
     * The HTTP response status code. The value should be a number.
     * <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/attributes-registry/http.md#http-response-status-code">http.response.status_code attribute</a>
     */
    public static final String HTTP_RESPONSE_STATUS_CODE_KEY = "http.response.status_code";

    /**
     * Server domain name if available without reverse DNS lookup; otherwise, IP address or Unix domain socket name.
     * <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/attributes-registry/server.md#server-address">server.address attribute</a>
     */
    public static final String SERVER_ADDRESS_KEY = "server.address";

    /**
     * Server port number.
     * <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/attributes-registry/server.md#server-port">server.port attribute</a>
     */
    public static final String SERVER_PORT_KEY = "server.port";

    /**
     * The request user agent.
     * <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/attributes-registry/user-agent.md#user-agent-original">user_agent.original attribute</a>
     */
    public static final String USER_AGENT_ORIGINAL_KEY = "user_agent.original";

    /**
     * Absolute URL describing a network resource.
     * <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/attributes-registry/url.md#url-full">url.full attribute</a>
     */
    public static final String URL_FULL_KEY = "url.full";

    // Custom attribute names, use with caution
    /**
     * Key representing the exception cause type
     */
    public static final String CAUSE_TYPE_KEY = "cause.type";

    /**
     * Key representing the exception cause message
     */
    public static final String CAUSE_MESSAGE_KEY = "cause.message";

    /**
     * Key representing duration of call in milliseconds, the value should be a number.
     */
    public static final String HTTP_REQUEST_TIME_TO_RESPONSE_KEY = "http.request.time_to_response";

    /**
     * Key representing duration of call in milliseconds, the value should be a number.
     */
    public static final String HTTP_REQUEST_DURATION_KEY = "http.request.duration";

    /**
     * Key representing request body. The value should be populated conditionally
     * if populated at all.
     */
    public static final String HTTP_REQUEST_BODY_CONTENT_KEY = "http.request.body.content";

    /**
     * Key representing response body. The value should be populated conditionally
     * if populated at all.
     */
    public static final String HTTP_RESPONSE_BODY_CONTENT_KEY = "http.response.body.content";

    /**
     * Key representing operation name. The value should be a string.
     * When instrumenting client libraries, it should be language-agnostic operation name provided in typespec
     * or swagger.
     */
    public static final String OPERATION_NAME_KEY = "operation.name";

    /**
     * Key representing maximum number of redirects or retries. It's reported when the number of redirects or retries
     * was exhausted.
     */
    public static final String RETRY_MAX_ATTEMPT_COUNT_KEY = "retry.max_attempt_count";

    /**
     * Key representing delay before next retry attempt in milliseconds. The value should be a number.
     */
    public static final String RETRY_DELAY_KEY = "retry.delay";

    /**
     * Key representing whether the retry jor redirect ust performed was the last attempt.
     */
    public static final String RETRY_WAS_LAST_ATTEMPT_KEY = "retry.was_last_attempt";

    /**
     * Key representing span id on logs.
     */
    public static final String SPAN_ID_KEY = "span.id";

    /**
     * Key representing parent span id on logs.
     */
    public static final String SPAN_PARENT_ID_KEY = "span.parent.id";

    /**
     * Key representing span name on logs.
     */
    public static final String SPAN_NAME_KEY = "span.name";

    /**
     * Key representing span kind on logs.
     */
    public static final String SPAN_KIND_KEY = "span.kind";

    /**
     * Key representing span duration (in milliseconds) on logs. The value should be a number.
     */
    public static final String SPAN_DURATION_KEY = "span.duration";

    /**
     * Key representing trace id on logs.
     */
    public static final String TRACE_ID_KEY = "trace.id";

    private AttributeKeys() {
    }
}
