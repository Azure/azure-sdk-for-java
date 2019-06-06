package com.azure.eventhubs.implementation;

import com.azure.core.amqp.AmqpConnection;
import reactor.core.publisher.Mono;

public interface EventHubConnection extends AmqpConnection {
    Mono<EventHubManagementNode> getManagementNode();
}
