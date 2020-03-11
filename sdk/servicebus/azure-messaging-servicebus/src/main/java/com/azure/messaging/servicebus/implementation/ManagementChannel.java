// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.implementation.AmqpConstants;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.RequestResponseChannel;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.Message;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Channel responsible for Service Bus related metadata, peek  and management plane operations.
 * Management plane operations increasing quotas, etc.
 */
public class ManagementChannel implements  ServiceBusManagementNode {
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
    private static final String PEEK_OPERATION_VALUE = AmqpConstants.VENDOR + ":peek-message";
    private static final String MANAGEMENT_EVENTHUB_ENTITY_TYPE = AmqpConstants.VENDOR + ":servicebus";
    private static final String MANAGEMENT_SERVER_TIMEOUT = AmqpConstants.VENDOR + ":server-timeout";


    private final TokenCredential tokenProvider;
    private final Mono<RequestResponseChannel> channelMono;
    private final Scheduler scheduler;
    private final String topicOrQueueName;
    private final MessageSerializer messageSerializer;
    private final TokenManagerProvider tokenManagerProvider;

    ManagementChannel(Mono<RequestResponseChannel> responseChannelMono, String topicOrQueueName,
                      TokenCredential credential, TokenManagerProvider tokenManagerProvider,
                      MessageSerializer messageSerializer, Scheduler scheduler) {

        this.tokenManagerProvider = Objects.requireNonNull(tokenManagerProvider,
            "'tokenManagerProvider' cannot be null.");
        this.tokenProvider = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.topicOrQueueName = Objects.requireNonNull(topicOrQueueName, "'eventHubName' cannot be null.");
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");
        this.channelMono = Objects.requireNonNull(responseChannelMono, "'responseChannelMono' cannot be null.");
        this.scheduler = Objects.requireNonNull(scheduler, "'scheduler' cannot be null.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ServiceBusReceivedMessage> peek() {

        int maxMessages = 2;
        long fromSequenceNumber = 1;
        final Map<String, Object> properties = new HashMap<>();
        properties.put(MANAGEMENT_ENTITY_TYPE_KEY, MANAGEMENT_EVENTHUB_ENTITY_TYPE);
        properties.put(MANAGEMENT_ENTITY_NAME_KEY, topicOrQueueName);
        properties.put(MANAGEMENT_OPERATION_KEY, READ_OPERATION_VALUE);

        return peek(properties, ServiceBusReceivedMessage.class, maxMessages, fromSequenceNumber, null)
            .publishOn(scheduler);
    }

    private <T> Mono<T> peek(Map<String, Object> appProperties, Class<T> responseType,
                             int maxMessages, long fromSequenceNumber, UUID sessionId) {

        final String tokenAudience = tokenManagerProvider.getScopesFromResource(topicOrQueueName);

        return tokenProvider.getToken(new TokenRequestContext().addScopes(tokenAudience)).flatMap(accessToken -> {
            appProperties.put(MANAGEMENT_SECURITY_TOKEN_KEY, accessToken.getToken());

            // set mandatory application properties for AMQP message.
            appProperties.put(MANAGEMENT_OPERATION_KEY, PEEK_OPERATION_VALUE);
            appProperties.put(MANAGEMENT_SERVER_TIMEOUT, "" + 1000 * 30);

            final Message request = Proton.message();
            final ApplicationProperties applicationProperties = new ApplicationProperties(appProperties);
            request.setApplicationProperties(applicationProperties);

            // set mandatory properties on AMQP message body
            HashMap<String, Object> requestBodyMap = new HashMap<>();
            requestBodyMap.put(ServiceBusConstants.REQUEST_RESPONSE_FROM_SEQUENCE_NUMBER, fromSequenceNumber);
            requestBodyMap.put(ServiceBusConstants.REQUEST_RESPONSE_MESSAGE_COUNT, maxMessages);

            if (!Objects.isNull(sessionId)) {
                requestBodyMap.put(ServiceBusConstants.REQUEST_RESPONSE_SESSION_ID, sessionId);
            }

            request.setBody(new AmqpValue(requestBodyMap));

            return channelMono.flatMap(requestResponseChannel -> requestResponseChannel.sendWithAck(request))
                .map(message -> messageSerializer.deserialize(message, responseType));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        if (channelMono instanceof Disposable) {
            ((Disposable) channelMono).dispose();
        }
    }
}
