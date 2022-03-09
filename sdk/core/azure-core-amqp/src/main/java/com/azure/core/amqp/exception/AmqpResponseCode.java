// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Error response codes returned from AMQP.
 */
public enum AmqpResponseCode {
    /**
     * ACCEPTED.
     */
    ACCEPTED(202),
    /**
     * OK.
     */
    OK(200),
    /**
     * BAD_REQUEST.
     */
    BAD_REQUEST(400),
    /**
     * NOT_FOUND.
     */
    NOT_FOUND(404),
    /**
     * FORBIDDEN.
     */
    FORBIDDEN(403),
    /**
     * INTERNAL_SERVER_ERROR.
     */
    INTERNAL_SERVER_ERROR(500),
    /**
     * UNAUTHORIZED.
     */
    UNAUTHORIZED(401),

    /**
     * CONTINUE.
     */
    CONTINUE(100),
    /**
     * SWITCHING_PROTOCOLS.
     */
    SWITCHING_PROTOCOLS(101),
    /**
     * CREATED.
     */
    CREATED(201),
    /**
     * NON_AUTHORITATIVE_INFORMATION.
     */
    NON_AUTHORITATIVE_INFORMATION(203),
    /**
     * NO_CONTENT.
     */
    NO_CONTENT(204),
    /**
     * RESET_CONTENT.
     */
    RESET_CONTENT(205),
    /**
     * PARTIAL_CONTENT.
     */
    PARTIAL_CONTENT(206),
    /**
     * AMBIGUOUS.
     */
    AMBIGUOUS(300),
    /**
     * MULTIPLE_CHOICES.
     */
    MULTIPLE_CHOICES(300),
    /**
     * MOVED.
     */
    MOVED(301),
    /**
     * MOVED_PERMANENTLY.
     */
    MOVED_PERMANENTLY(301),
    /**
     * FOUND.
     */
    FOUND(302),
    /**
     * REDIRECT.
     */
    REDIRECT(302),
    /**
     * REDIRECT_METHOD.
     */
    REDIRECT_METHOD(303),
    /**
     * SEE_OTHER.
     */
    SEE_OTHER(303),
    /**
     * NOT_MODIFIED.
     */
    NOT_MODIFIED(304),
    /**
     * USE_PROXY.
     */
    USE_PROXY(305),
    /**
     * UNUSED.
     */
    UNUSED(306),
    /**
     * REDIRECT_KEEP_VERB.
     */
    REDIRECT_KEEP_VERB(307),
    /**
     * TEMPORARY_REDIRECT.
     */
    TEMPORARY_REDIRECT(307),
    /**
     * PAYMENT_REQUIRED.
     */
    PAYMENT_REQUIRED(402),
    /**
     * METHOD_NOT_ALLOWED.
     */
    METHOD_NOT_ALLOWED(405),
    /**
     * NOT_ACCEPTABLE.
     */
    NOT_ACCEPTABLE(406),
    /**
     * PROXY_AUTHENTICATION_REQUIRED.
     */
    PROXY_AUTHENTICATION_REQUIRED(407),
    /**
     * REQUEST_TIMEOUT.
     */
    REQUEST_TIMEOUT(408),
    /**
     * CONFLICT.
     */
    CONFLICT(409),
    /**
     * GONE.
     */
    GONE(410),
    /**
     * LENGTH_REQUIRED.
     */
    LENGTH_REQUIRED(411),
    /**
     * PRECONDITION_FAILED.
     */
    PRECONDITION_FAILED(412),
    /**
     * REQUEST_ENTITY_TOO_LARGE.
     */
    REQUEST_ENTITY_TOO_LARGE(413),
    /**
     * REQUEST_URI_TOO_LONG.
     */
    REQUEST_URI_TOO_LONG(414),
    /**
     * UNSUPPORTED_MEDIA_TYPE.
     */
    UNSUPPORTED_MEDIA_TYPE(415),
    /**
     * REQUESTED_RANGE_NOT_SATISFIABLE.
     */
    REQUESTED_RANGE_NOT_SATISFIABLE(416),
    /**
     * EXPECTATION_FAILED.
     */
    EXPECTATION_FAILED(417),
    /**
     * UPGRADE_REQUIRED.
     */
    UPGRADE_REQUIRED(426),
    /**
     * NOT_IMPLEMENTED.
     */
    NOT_IMPLEMENTED(501),
    /**
     * BAD_GATEWAY.
     */
    BAD_GATEWAY(502),
    /**
     * SERVICE_UNAVAILABLE.
     */
    SERVICE_UNAVAILABLE(503),
    /**
     * GATEWAY_TIMEOUT.
     */
    GATEWAY_TIMEOUT(504),
    /**
     * HTTP_VERSION_NOT_SUPPORTED.
     */
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
