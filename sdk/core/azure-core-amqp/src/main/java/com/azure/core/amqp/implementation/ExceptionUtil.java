// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.AmqpResponseCode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to map AMQP status codes and error conditions to an exception.
 */
public final class ExceptionUtil {
    private static final String AMQP_REQUEST_FAILED_ERROR = "status-code: %s, status-description: %s";
    private static final Pattern ENTITY_NOT_FOUND_PATTERN =
        Pattern.compile("The messaging entity .* could not be found");

    /**
     * Creates an {@link AmqpException} or Exception based on the {@code errorCondition} from the AMQP request.
     *
     * @param errorCondition The error condition string.
     * @param description The error message.
     * @param errorContext The context that this error occurred in.
     * @return An exception that maps to the {@code errorCondition} provided.
     * @throws IllegalArgumentException when 'errorCondition' is {@code null} or empty, cannot be translated into an
     *     {@link AmqpErrorCondition}, or cannot be determined whether the {@link AmqpErrorCondition} is transient or
     *     not.
     * @see AmqpErrorCondition
     */
    public static Exception toException(String errorCondition, String description, AmqpErrorContext errorContext) {
        if (errorCondition == null) {
            throw new IllegalArgumentException("'null' errorCondition cannot be translated to EventHubException");
        }

        final AmqpErrorCondition condition = AmqpErrorCondition.fromString(errorCondition);
        if (condition == null) {
            return new AmqpException(false, String.format("errorCondition[%s]. description[%s]",
                errorCondition, description), errorContext);
        }

        boolean isTransient;
        switch (condition) {
            case TIMEOUT_ERROR:
            case SERVER_BUSY_ERROR:
            case INTERNAL_ERROR:
            case LINK_DETACH_FORCED:
            case CONNECTION_FORCED:
            case CONNECTION_FRAMING_ERROR:
            case PROTON_IO:
                isTransient = true;
                break;
            case ENTITY_DISABLED_ERROR:
            case LINK_STOLEN:
            case UNAUTHORIZED_ACCESS:
            case LINK_PAYLOAD_SIZE_EXCEEDED:
            case ARGUMENT_ERROR:
            case ARGUMENT_OUT_OF_RANGE_ERROR:
            case PARTITION_NOT_OWNED_ERROR:
            case STORE_LOCK_LOST_ERROR:
            case RESOURCE_LIMIT_EXCEEDED:
            case OPERATION_CANCELLED:
            case MESSAGE_LOCK_LOST:
            case SESSION_LOCK_LOST:
            case SESSION_CANNOT_BE_LOCKED:
            case ENTITY_ALREADY_EXISTS:
            case MESSAGE_NOT_FOUND:
            case SESSION_NOT_FOUND:
                isTransient = false;
                break;
            case NOT_IMPLEMENTED:
            case NOT_ALLOWED:
                return new UnsupportedOperationException(description);
            case NOT_FOUND:
                return distinguishNotFound(description, errorContext);
            default:
                return new AmqpException(false, condition, String.format("errorCondition[%s]. description[%s] "
                        + "Condition could not be mapped to a transient condition.",
                    errorCondition, description), errorContext);
        }

        return new AmqpException(isTransient, condition, description, errorContext);
    }

    /**
     * Given an AMQP response code, it maps it to an exception.
     *
     * @param statusCode AMQP response code.
     * @param statusDescription Message associated with response.
     * @param errorContext The context that this error occurred in.
     * @return An exception that maps to that status code.
     */
    public static Exception amqpResponseCodeToException(int statusCode, String statusDescription,
        AmqpErrorContext errorContext) {

        final AmqpResponseCode amqpResponseCode = AmqpResponseCode.fromValue(statusCode);
        final String message = String.format(AMQP_REQUEST_FAILED_ERROR, statusCode, statusDescription);

        if (amqpResponseCode == null) {
            return new AmqpException(true, message, errorContext);
        }

        switch (amqpResponseCode) {
            case BAD_REQUEST:
                return new IllegalArgumentException(message);
            case NOT_FOUND:
                return distinguishNotFound(statusDescription, errorContext);
            case FORBIDDEN:
                return new AmqpException(false, AmqpErrorCondition.RESOURCE_LIMIT_EXCEEDED, message, errorContext);
            case UNAUTHORIZED:
                return new AmqpException(false, AmqpErrorCondition.UNAUTHORIZED_ACCESS, message, errorContext);
            default:
                return new AmqpException(true, message, errorContext);
        }
    }

    private static AmqpException distinguishNotFound(String message, AmqpErrorContext errorContext) {
        final Matcher m = ENTITY_NOT_FOUND_PATTERN.matcher(message);
        if (m.find()) {
            return new AmqpException(false, AmqpErrorCondition.NOT_FOUND, message, errorContext);
        } else {
            return new AmqpException(true, AmqpErrorCondition.NOT_FOUND,
                String.format(AMQP_REQUEST_FAILED_ERROR, AmqpResponseCode.NOT_FOUND, message),
                errorContext);
        }
    }
}
