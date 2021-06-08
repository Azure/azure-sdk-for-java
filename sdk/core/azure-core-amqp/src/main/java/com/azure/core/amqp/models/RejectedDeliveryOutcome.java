// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.annotation.Fluent;

import java.util.Map;
import java.util.Objects;

/**
 * The rejected delivery outcome.
 * <p>
 * At the target, the rejected outcome is used to indicate that an incoming message is invalid and therefore
 * unprocessable. The rejected outcome when applied to a message will cause the delivery-count to be incremented in the
 * header of the rejected message.
 * </p>
 * <p>
 * At the source, the rejected outcome means that the target has informed the source that the message was rejected, and
 * the source has taken the necessary action. The delivery SHOULD NOT ever spontaneously attain the rejected state at
 * the source.
 * </p>
 *
 * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#type-rejected">Rejected
 *     outcome</a>
 */
@Fluent
public final class RejectedDeliveryOutcome extends DeliveryOutcome {
    private final AmqpErrorCondition errorCondition;
    private Map<String, Object> errorInfo;

    /**
     * Creates an instance with the given error condition.
     *
     * @param errorCondition The error condition.
     */
    public RejectedDeliveryOutcome(AmqpErrorCondition errorCondition) {
        super(DeliveryState.REJECTED);
        this.errorCondition = Objects.requireNonNull(errorCondition, "'errorCondition' cannot be null.");
    }

    /**
     * Diagnostic information about the cause of the message rejection.
     *
     * @return Diagnostic information about the cause of the message rejection.
     */
    public AmqpErrorCondition getErrorCondition() {
        return errorCondition;
    }

    /**
     * Gets the error description.
     *
     * @return Gets the error condition.
     */
    public String getErrorDescription() {
        return errorCondition.getErrorCondition();
    }

    /**
     * Gets a map of additional error information.
     *
     * @return Map of additional error information.
     */
    public Map<String, Object> getErrorInfo() {
        return errorInfo;
    }

    /**
     * Sets a map with additional error information.
     *
     * @param errorInfo Error information associated with the rejection.
     *
     * @return The updated {@link RejectedDeliveryOutcome} object.
     */
    public RejectedDeliveryOutcome setErrorInfo(Map<String, Object> errorInfo) {
        this.errorInfo = errorInfo;
        return this;
    }
}
