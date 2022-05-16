// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.checkpoint;

import reactor.core.publisher.Mono;

/**
 * A callback to perform checkpoint for received messages.
 * When a manual checkpoint mode is used in Event Hubs or autoComplete is set to false in Service Bus,
 * {@link Checkpointer} will be put in messages as the header
 * {@link com.azure.spring.messaging.AzureHeaders#CHECKPOINTER}.
 *
 * <p>
 * Example
 * </p>
 * <pre>{@code
 * Checkpointer checkpointer = message.getHeaders().get(AzureHeaders.CHECKPOINTER, Checkpointer.class);
 * checkpointer.success()
 *             .doOnSuccess(success -> LOGGER.info("Successfully checkpoint {}", message.getPayload()))
 *             .doOnError(e -> LOGGER.error("Fail to checkpoint the message", e))
 *             .block();
 * }</pre>
 */
public interface Checkpointer {

    /**
     * Acknowledge success of current message. Please check result to detect failure
     * @return Mono Void
     */
    Mono<Void> success();

    /**
     * Acknowledge failure of current message. Please check result to detect failure
     * @return Mono Void
     */
    Mono<Void> failure();
}
