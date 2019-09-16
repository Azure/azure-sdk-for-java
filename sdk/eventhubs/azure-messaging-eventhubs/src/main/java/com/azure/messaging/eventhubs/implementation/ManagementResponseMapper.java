// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import java.util.Map;

/**
 * Deserializes an AMQP response from the {@link EventHubManagementNode} into an object.
 */
public interface ManagementResponseMapper {
    /**
     * Deserializes an AMQP response body to an object of type, {@code T}.
     *
     * @param amqpBody Map of keys and values read from the AMQP message body.
     * @param deserializedType Class for the deserialized response body.
     * @param <T> Type to create from the AMQP response body.
     *
     * @return The object from the AMQP response body. Otherwise, {@code null} if it could not be deserialized.
     */
    <T> T deserialize(Map<?, ?> amqpBody, Class<T> deserializedType);
}
