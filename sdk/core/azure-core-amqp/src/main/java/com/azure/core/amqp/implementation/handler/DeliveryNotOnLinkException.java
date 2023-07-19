// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.amqp.implementation.handler;

import org.apache.qpid.proton.amqp.transport.DeliveryState;

/**
 * Type representing the error returned by the {@link ReceiverUnsettledDeliveries#sendDisposition(String, DeliveryState)}
 * API when it is unable to locate the delivery on the link.
 */
final class DeliveryNotOnLinkException extends RuntimeException {

    static DeliveryNotOnLinkException linkClosed(String deliveryTag) {
        return new DeliveryNotOnLinkException("Cannot send disposition."
            + " Reason: Unable to look up the delivery for the delivery tag " + deliveryTag + " as the link is closed.");
    }

    static DeliveryNotOnLinkException noMatchingDelivery(String deliveryTag) {
        return new DeliveryNotOnLinkException("Cannot send disposition."
            + " Reason: The delivery with the delivery tag " + deliveryTag + " does not exist in the link's DeliveryMap.");
    }

    private DeliveryNotOnLinkException(String message) {
        super(message);
    }
}
