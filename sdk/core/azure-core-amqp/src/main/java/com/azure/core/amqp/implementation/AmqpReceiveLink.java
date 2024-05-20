// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpLink;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UncheckedIOException;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Supplier;

/**
 * A unidirectional link from the client to the message broker that listens for messages.
 *
 * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transport-v1.0-os.html#section-links">AMQP
 * Specification 1.0: Links</a>
 */
public interface AmqpReceiveLink extends AmqpLink {
    /**
     * Gets the unique identifier of the Amqp connection hosting the receive link.
     *
     * @return The connection identifier.
     */
    // Note: Ideally, we may expose connectionId in AmqpLink, but given it's a public contract, lets not do that until a
    // use case needing it.
    String getConnectionId();

    /**
     * Initialises the link from the client to the message broker and begins to receive messages from the broker.
     *
     * @return A Flux of AMQP messages which completes when the client calls
     * {@link AutoCloseable#close() AmqpReceiveLink.close()} or an unrecoverable error occurs on the AMQP link.
     */
    Flux<Message> receive();

    /**
     * Updates the disposition state of a message uniquely identified by the given delivery tag.
     *
     * @param deliveryTag delivery tag of message.
     * @param deliveryState Delivery state of message.
     *
     * @return A Mono that completes when the state is successfully updated and acknowledged by message broker.
     */
    Mono<Void> updateDisposition(String deliveryTag, DeliveryState deliveryState);

    /**
     * Schedule to adds the specified number of credits to the link.
     * <p>
     * The number of link credits initialises to zero. It is the application's responsibility to call this method to
     * allow the receiver to receive {@code credits} more deliveries.
     *
     * @param credits Number of credits to add to the receive link.
     * @return A Mono that completes when the credits are successfully added to the link.
     * @throws IllegalStateException if adding credits to a closed link.
     * @throws UncheckedIOException if the work could not be scheduled on the receive link.
     */
    Mono<Void> addCredits(int credits);

    /**
     * Schedules an event to send a credit to the broker. The API takes a {@link Supplier} that returns the credit
     * to send. The supplier allows providing the most up-to-date credit value when the scheduler picks the scheduled
     * work for execution rather than the credit at the time of scheduling.
     *
     * @param creditSupplier the supplier that returns the credit to send.
     * @throws RejectedExecutionException if the scheduler rejects the scheduling attempt (e.g., the scheduler is closed).
     * @throws UncheckedIOException if an IO error occurs when scheduling.
     */
    void addCredit(Supplier<Long> creditSupplier);

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
