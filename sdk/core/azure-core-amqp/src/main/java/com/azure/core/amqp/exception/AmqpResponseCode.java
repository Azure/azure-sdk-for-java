// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Error response codes returned from AMQP.
 */
public enum AmqpResponseCode {
    ACCEPTED(202),
    OK(200),
    BAD_REQUEST(400),
    NOT_FOUND(404),
    FORBIDDEN(403),
    INTERNAL_SERVER_ERROR(500),
    UNAUTHORIZED(401),

    CONTINUE(100),
    SWITCHING_PROTOCOLS(101),
    CREATED(201),
    NON_AUTHORITATIVE_INFORMATION(203),
    NO_CONTENT(204),
    RESET_CONTENT(205),
    PARTIAL_CONTENT(206),
    AMBIGUOUS(300),
    MULTIPLE_CHOICES(300),
    MOVED(301),
    MOVED_PERMANENTLY(301),
    FOUND(302),
    REDIRECT(302),
    REDIRECT_METHOD(303),
    SEE_OTHER(303),
    NOT_MODIFIED(304),
    USE_PROXY(305),
    UNUSED(306),
    REDIRECT_KEEP_VERB(307),
    TEMPORARY_REDIRECT(307),
    PAYMENT_REQUIRED(402),
    METHOD_NOT_ALLOWED(405),
    NOT_ACCEPTABLE(406),
    PROXY_AUTHENTICATION_REQUIRED(407),
    REQUEST_TIMEOUT(408),
    CONFLICT(409),
    GONE(410),
    LENGTH_REQUIRED(411),
    PRECONDITION_FAILED(412),
    REQUEST_ENTITY_TOO_LARGE(413),
    REQUEST_URI_TOO_LONG(414),
    UNSUPPORTED_MEDIA_TYPE(415),
    REQUESTED_RANGE_NOT_SATISFIABLE(416),
    EXPECTATION_FAILED(417),
    UPGRADE_REQUIRED(426),
    NOT_IMPLEMENTED(501),
    BAD_GATEWAY(502),
    SERVICE_UNAVAILABLE(503),
    GATEWAY_TIMEOUT(504),
    HTTP_VERSION_NOT_SUPPORTED(505);

    private static final Map<Integer, AmqpResponseCode> VALUE_MAP = new HashMap<>();

    static {
        for (AmqpResponseCode code : AmqpResponseCode.values()) {
            VALUE_MAP.put(code.value, code);
        }
    }

    private final int value;

    AmqpResponseCode(final int value) {
        this.value = value;
    }

    /**
     * Creates an AmqpResponseCode for the provided integer {@code value}.
     *
     * @param value The integer value representing an error code.
     * @return The corresponding AmqpResponseCode for the provided value, or {@code null} if no matching response code
     * is found.
     */
    public static AmqpResponseCode fromValue(final int value) {
        return VALUE_MAP.get(value);
    }

    /**
     * Gets the integer value of the AmqpResponseCode
     *
     * @return The integer value of the AmqpResponseCode
     */
    public int getValue() {
        return this.value;
    }
}
