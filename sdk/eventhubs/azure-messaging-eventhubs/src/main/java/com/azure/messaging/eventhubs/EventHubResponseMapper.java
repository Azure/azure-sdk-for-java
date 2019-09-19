// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ManagementChannel;
import com.azure.messaging.eventhubs.implementation.ManagementResponseMapper;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Deserializses AMQP responses from the Event Hub management node.
 */
class EventHubResponseMapper implements ManagementResponseMapper {
    private final ClientLogger logger = new ClientLogger(EventHubResponseMapper.class);

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(Map<?, ?> amqpBody, Class<T> deserializedType) {
        if (deserializedType == PartitionProperties.class) {
            return (T) toPartitionProperties(amqpBody);
        } else if (deserializedType == EventHubProperties.class) {
            return (T) toEventHubProperties(amqpBody);
        } else {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(Locale.US,
                "Class '%s' is not a supported deserializable type.", deserializedType)));
        }
    }

    private EventHubProperties toEventHubProperties(Map<?, ?> amqpBody) {
        return new EventHubProperties(
            (String) amqpBody.get(ManagementChannel.MANAGEMENT_ENTITY_NAME_KEY),
            ((Date) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_CREATED_AT)).toInstant(),
            (String[]) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_PARTITION_IDS));
    }

    private PartitionProperties toPartitionProperties(Map<?, ?> amqpBody) {
        return new PartitionProperties(
            (String) amqpBody.get(ManagementChannel.MANAGEMENT_ENTITY_NAME_KEY),
            (String) amqpBody.get(ManagementChannel.MANAGEMENT_PARTITION_NAME_KEY),
            (Long) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_BEGIN_SEQUENCE_NUMBER),
            (Long) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_LAST_ENQUEUED_SEQUENCE_NUMBER),
            (String) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_LAST_ENQUEUED_OFFSET),
            ((Date) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_LAST_ENQUEUED_TIME_UTC)).toInstant(),
            (Boolean) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_PARTITION_IS_EMPTY));
    }
}
