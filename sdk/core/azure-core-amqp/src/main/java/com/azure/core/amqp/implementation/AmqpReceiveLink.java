// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpLink;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Flux;

import java.util.function.Supplier;

/**
 * A unidirectional link from the client to the message broker that listens for messages.
 *
 * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transport-v1.0-os.html#section-links">AMQP
 * Specification 1.0: Links</a>
 */
public interface AmqpReceiveLink extends AmqpLink {
    /**
     * Initialises the link from the client to the message broker and begins to receive messages from the broker.
     *
     * @return A Flux of AMQP messages which completes when the client calls
     * {@link AutoCloseable#close() AmqpReceiveLink.close()} or an unrecoverable error occurs on the AMQP link.
     */
    Flux<Message> receive();

    /**
     * Schedule to adds the specified number of credits to the link.
     *
     * The number of link credits initialises to zero. It is the application's responsibility to call this method to
     * allow the receiver to receive {@code credits} more deliveries.
     *
     * @param credits Number of credits to add to the receive link.
     */
    void addCredits(int credits);

    /**
     * Adds the specified number of credits to the link.
     *
     * The number of link credits initialises to zero. It is the application's responsibility to call this method to
     * allow the receiver to receive {@code credits} more deliveries.
     *
     * It will update the credits in local memory instantly so {@link #getCredits()} will get
     * the updated credits immediately. But the service side may get the credits added with a latency.
     * As a contrast, {@link #getCredits()} may return an unchanged value for a short while after
     * {@link #addCredits(int)} is called to schedule the credit addition and before the job dispatcher executes it.
     *
     * @param credits Number of credits to add to the receive link.
     */
    void addCreditsInstantly(int credits);

    /**
     * Gets the current number of credits this link has.
     *
     * @return The number of credits (deliveries) this link has.
     */
    int getCredits();

    /**
     * Sets an event listener that is invoked when there are no credits on the link left. If the supplier returns an
     * integer that is {@code null} or less than 1, then no credits are added to the link and no more messages are
     * received on the link.
     *
     * @param creditSupplier Supplier that returns the number of credits to add to the link.
     */
    void setEmptyCreditListener(Supplier<Integer> creditSupplier);
}
