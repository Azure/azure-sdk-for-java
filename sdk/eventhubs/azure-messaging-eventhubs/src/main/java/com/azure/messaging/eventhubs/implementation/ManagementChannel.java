// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.implementation.AmqpConstants;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.RequestResponseChannel;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.messaging.eventhubs.EventHubProperties;
import com.azure.messaging.eventhubs.PartitionProperties;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Channel responsible for Event Hubs related metadata and management plane operations. Management plane operations
 * include another partition, increasing quotas, etc.
 */
public class ManagementChannel implements EventHubManagementNode {
    // Well-known keys from the management service responses and requests.
    public static final String MANAGEMENT_ENTITY_NAME_KEY = "name";
    public static final String MANAGEMENT_PARTITION_NAME_KEY = "partition";
    public static final String MANAGEMENT_RESULT_PARTITION_IDS = "partition_ids";
    public static final String MANAGEMENT_RESULT_CREATED_AT = "created_at";
    public static final String MANAGEMENT_RESULT_BEGIN_SEQUENCE_NUMBER = "begin_sequence_number";
    public static final String MANAGEMENT_RESULT_LAST_ENQUEUED_SEQUENCE_NUMBER = "last_enqueued_sequence_number";
    public static final String MANAGEMENT_RESULT_LAST_ENQUEUED_OFFSET = "last_enqueued_offset";
    public static final String MANAGEMENT_RESULT_LAST_ENQUEUED_TIME_UTC = "last_enqueued_time_utc";
    public static final String MANAGEMENT_RESULT_RUNTIME_INFO_RETRIEVAL_TIME_UTC = "runtime_info_retrieval_time_utc";
    public static final String MANAGEMENT_RESULT_PARTITION_IS_EMPTY = "is_partition_empty";

    // Well-known keys for management plane service requests.
    private static final String MANAGEMENT_ENTITY_TYPE_KEY = "type";
    private static final String MANAGEMENT_OPERATION_KEY = "operation";
    private static final String MANAGEMENT_SECURITY_TOKEN_KEY = "security_token";

    // Well-known values for the service request.
    private static final String READ_OPERATION_VALUE = "READ";
    private static final String MANAGEMENT_EVENTHUB_ENTITY_TYPE = AmqpConstants.VENDOR + ":eventhub";
    private static final String MANAGEMENT_PARTITION_ENTITY_TYPE = AmqpConstants.VENDOR + ":partition";

    private final TokenCredential tokenProvider;
    private final Mono<RequestResponseChannel> channelMono;
    private final String eventHubName;
    private final MessageSerializer messageSerializer;
    private final TokenManagerProvider tokenManagerProvider;

    /**
     * Creates an instance that is connected to the {@code eventHubName}'s management node.
     *
     * @param responseChannelMono Mono that completes with a new {@link RequestResponseChannel}.
     * @param eventHubName The name of the Event Hub.
     * @param credential Credential to authorize user for access to the Event Hub.
     * @param tokenManagerProvider Provides a token manager that will keep track and maintain tokens.
     * @param messageSerializer Maps responses from the management channel.
     */
    ManagementChannel(Mono<RequestResponseChannel> responseChannelMono, String eventHubName, TokenCredential credential,
                      TokenManagerProvider tokenManagerProvider, MessageSerializer messageSerializer) {

        this.tokenManagerProvider = Objects.requireNonNull(tokenManagerProvider,
            "'tokenManagerProvider' cannot be null.");
        this.tokenProvider = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.eventHubName = Objects.requireNonNull(eventHubName, "'eventHubName' cannot be null.");
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");

        // Cache the first response from this mono, so we don't keep creating it.
        this.channelMono = Objects.requireNonNull(responseChannelMono, "'responseChannelMono' cannot be null.")
            .cache();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<EventHubProperties> getEventHubProperties() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put(MANAGEMENT_ENTITY_TYPE_KEY, MANAGEMENT_EVENTHUB_ENTITY_TYPE);
        properties.put(MANAGEMENT_ENTITY_NAME_KEY, eventHubName);
        properties.put(MANAGEMENT_OPERATION_KEY, READ_OPERATION_VALUE);

        return getProperties(properties, EventHubProperties.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<PartitionProperties> getPartitionProperties(String partitionId) {
        final Map<String, Object> properties = new HashMap<>();
        properties.put(MANAGEMENT_ENTITY_TYPE_KEY, MANAGEMENT_PARTITION_ENTITY_TYPE);
        properties.put(MANAGEMENT_ENTITY_NAME_KEY, eventHubName);
        properties.put(MANAGEMENT_PARTITION_NAME_KEY, partitionId);
        properties.put(MANAGEMENT_OPERATION_KEY, READ_OPERATION_VALUE);

        return getProperties(properties, PartitionProperties.class);
    }

    private <T> Mono<T> getProperties(Map<String, Object> properties, Class<T> responseType) {
        final String tokenAudience = tokenManagerProvider.getResourceString(eventHubName);

        return tokenProvider.getToken(new TokenRequestContext().addScopes(tokenAudience)).flatMap(accessToken -> {
            properties.put(MANAGEMENT_SECURITY_TOKEN_KEY, accessToken.getToken());

            final Message request = Proton.message();
            final ApplicationProperties applicationProperties = new ApplicationProperties(properties);
            request.setApplicationProperties(applicationProperties);

            return channelMono.flatMap(x -> x.sendWithAck(request))
                .map(message -> messageSerializer.deserialize(message, responseType));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        final RequestResponseChannel channel = channelMono.block(Duration.ofSeconds(60));
        if (channel != null) {
            channel.close();
        }
    }
}
