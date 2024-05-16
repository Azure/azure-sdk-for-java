// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.amqp.implementation.handler;

import org.apache.qpid.proton.amqp.transport.DeliveryState;

/**
 * Type representing the error returned by the
 * {@link ReceiverUnsettledDeliveries#sendDisposition(String, DeliveryState)}
 * API when it is unable to locate the delivery on the link.
 */
public final class DeliveryNotOnLinkException extends RuntimeException {

    /**
     * Creates a new instance of {@link DeliveryNotOnLinkException} indicating the link was closed.
     *
     * @param deliveryTag The delivery tag for the delivery that was not found.
     * @param desiredState The desired state for the delivery.
     * @return A new instance of {@link DeliveryNotOnLinkException}.
     */
    public static DeliveryNotOnLinkException linkClosed(String deliveryTag, DeliveryState desiredState) {
        return new DeliveryNotOnLinkException("Cannot process the disposition request to set the state as '"
            + desiredState + "' for the delivery with delivery tag (id) '" + deliveryTag + "'."
            + " Reason: Unable to look up the delivery for the delivery tag as the link is closed.");
    }

    /**
     * Creates a new instance of {@link DeliveryNotOnLinkException} indicating the delivery was not found on the link.
     * @param deliveryTag The delivery tag for the delivery that was not found.
     * @param desiredState The desired state for the delivery.
     * @return A new instance of {@link DeliveryNotOnLinkException}.
     */
    public static DeliveryNotOnLinkException noMatchingDelivery(String deliveryTag, DeliveryState desiredState) {
        return new DeliveryNotOnLinkException("Cannot process the disposition request to set the state as '"
            + desiredState + "' for the delivery with delivery tag (id) '" + deliveryTag + "'."
            + " Reason: The delivery with the delivery tag does not exist in the link's DeliveryMap.");
    }

    private DeliveryNotOnLinkException(String message) {
        super(message);
    }
}
