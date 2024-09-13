// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.MessageFlux;
import com.azure.messaging.eventhubs.implementation.AmqpReceiveLinkProcessor;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Flux;

import java.util.Objects;

final class MessageFluxWrapper {
    private final AmqpReceiveLinkProcessor receiveLinkProcessor;
    private final MessageFlux messageFlux;
    private final boolean isV2;

    MessageFluxWrapper(MessageFlux messageFlux) {
        this.messageFlux = Objects.requireNonNull(messageFlux,  "'messageFlux' cannot be null.");
        this.receiveLinkProcessor = null;
        this.isV2 = true;
    }

    MessageFluxWrapper(AmqpReceiveLinkProcessor receiveLinkProcessor) {
        this.receiveLinkProcessor = Objects.requireNonNull(receiveLinkProcessor,  "'amqpReceiveLinkProcessor' cannot be null.");
        this.messageFlux = null;
        this.isV2 = false;
    }

    Flux<Message> flux() {
        return isV2 ? messageFlux : receiveLinkProcessor;
    }

    boolean isTerminated() {
        return isV2 ? false : receiveLinkProcessor.isTerminated();
    }

    void cancel() {
        if (!isV2) {
            receiveLinkProcessor.cancel();
        }
    }
}
