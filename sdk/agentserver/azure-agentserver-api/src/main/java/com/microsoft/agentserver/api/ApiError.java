// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Structured HTTP error body as defined by the API spec.
 * Serialised inside an outer envelope: {@code { "error": ApiError }}.
 * <p>
 * Required fields: {@link #message}, {@link #type}, {@link #code} (which may be
 * {@code null} but is always present). Optional fields ({@link #param},
 * {@link #details}, {@link #additionalInfo}) are omitted when {@code null}.
 *
 * @param message        human-readable description (always present)
 * @param type           error category, e.g. {@code "invalid_request_error"}, {@code "server_error"} (always present)
 * @param code           sub-code such as {@code "invalid_parameters"}, {@code "unsupported_parameter"};
 *                       always serialised, may be {@code null}
 * @param param          the offending request parameter, JSON-path or path-param name; optional
 * @param details        nested error objects for multi-error validation; optional
 * @param additionalInfo arbitrary supplemental key/value context; optional
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
    @JsonProperty("message") String message,
    @JsonProperty("type") String type,
    // `code` is always serialised (may be null) per the envelope spec.
    @JsonInclude(JsonInclude.Include.ALWAYS) @JsonProperty("code") String code,
    @JsonProperty("param") String param,
    @JsonProperty("details") List<ApiError> details,
    @JsonProperty("additionalInfo") Map<String, Object> additionalInfo) {

    /**
     * Standard error type for client-side / 4xx errors.
     */
    public static final String TYPE_INVALID_REQUEST = "invalid_request_error";
    /**
     * Standard error type for unhandled server errors (500).
     */
    public static final String TYPE_SERVER_ERROR = "server_error";

    /**
     * Generic 4xx sub-code used when no more specific code applies.
     */
    public static final String CODE_INVALID_REQUEST = "invalid_request_error";
    /**
     * Malformed path identifier.
     */
    public static final String CODE_INVALID_PARAMETERS = "invalid_parameters";
    /**
     * Parameter present but unsupported in this combination.
     */
    public static final String CODE_UNSUPPORTED_PARAMETER = "unsupported_parameter";
    /**
     * Required parameter missing from the request.
     */
    public static final String CODE_MISSING_REQUIRED_PARAMETER = "missing_required_parameter";
    /**
     * Unknown / unrecognised parameter on the request.
     */
    public static final String CODE_UNKNOWN_PARAMETER = "unknown_parameter";
    /**
     * A generic field-level validation failure inside a {@code details[]} entry.
     */
    public static final String CODE_INVALID_VALUE = "invalid_value";
    /**
     * Unhandled server-side failure.
     */
    public static final String CODE_SERVER_ERROR = "server_error";

    /**
     * Convenience for the common 4xx case with no offending parameter.
     */
    public static ApiError invalidRequest(String message) {
        return new ApiError(message, TYPE_INVALID_REQUEST, CODE_INVALID_REQUEST, null, null, null);
    }

    /**
     * Convenience for a 4xx case with a specific sub-code and offending parameter.
     */
    public static ApiError invalidRequest(String message, String code, String param) {
        return new ApiError(message, TYPE_INVALID_REQUEST, code, param, null, null);
    }

    /**
     * Convenience for a 500.
     */
    public static ApiError serverError(String message) {
        return new ApiError(message, TYPE_SERVER_ERROR, CODE_SERVER_ERROR, null, null, null);
    }
}

