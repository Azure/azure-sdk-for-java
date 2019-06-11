// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.AmqpLink;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Flux;

import java.io.Closeable;

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
     * {@link Closeable#close() AmqpReceiveLink.close()} or an unrecoverable error occurs on the AMQP link.
     */
    Flux<Message> receive();

    /**
     * Adds the specified number of credits to the link.
     *
     * The number of link credits initialises to zero. It is the application's responsibility to call this method to
     * allow the receiver to receive {@code credits} more deliveries.
     *
     * @param credits Number of credits to add to the receive link.
     */
    void addCredits(int credits);
}
