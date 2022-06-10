// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

import java.util.Locale;

/**
 * Represents the context for an AMQP link when an {@link AmqpException} occurs.
 *
 * @see AmqpException
 * @see AmqpErrorContext
 */
public class LinkErrorContext extends SessionErrorContext {
    private static final long serialVersionUID = 2581371351997722504L;

    /**
     * Tracking ID for the error.
     */
    private final String trackingId;

    /**
     * Number of link credits the AMQP link had when the error occurred.
     */
    private final Integer linkCredit;

    /**
     * Creates a new instance with the AMQP link's {@code namespace} and {@code entityPath} information. Allows for
     * optional information about the link if it was successfully opened such as {@code linkCredit} and
     * {@code trackingId}.
     *
     * @param namespace The service namespace of the error context.
     * @param entityPath The remote container the AMQP receive link is fetching messages from.
     * @param trackingId The tracking id for the error. Tracking id can be {@code null} if the error was not thrown from
     *         the remote AMQP message broker.
     * @param linkCredit the number of link credits the current AMQP link has when this error occurred, can be
     *         {@code null} if the receive link has not opened yet.
     * @throws IllegalArgumentException if {@code namespace} or {@code entityPath} is {@code null} or empty.
     */
    public LinkErrorContext(String namespace, String entityPath, String trackingId, Integer linkCredit) {
        super(namespace, entityPath);

        this.trackingId = trackingId;
        this.linkCredit = linkCredit;
    }

    /**
     * Gets the unique tracking identifier for this error. It is possible to be {@code null} if the error was not thrown
     * from the AMQP message broker.
     *
     * @return The unique tracking identifier for this error.
     */
    public String getTrackingId() {
        return trackingId;
    }

    /**
     * Gets the number of credits on the link when the error occurred. Can be {@code null} if the link is not opened.
     *
     * @return The number of credits on the link when the error occurred.
     */
    public Integer getLinkCredit() {
        return linkCredit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(super.toString());

        if (getTrackingId() != null) {
            builder.append(MESSAGE_PARAMETER_DELIMITER);
            builder.append(String.format(Locale.US, "REFERENCE_ID: %s", getTrackingId()));
        }

        if (getLinkCredit() != null) {
            builder.append(MESSAGE_PARAMETER_DELIMITER);
            builder.append(String.format(Locale.US, "LINK_CREDIT: %s", getLinkCredit()));
        }

        return builder.toString();
    }
}
