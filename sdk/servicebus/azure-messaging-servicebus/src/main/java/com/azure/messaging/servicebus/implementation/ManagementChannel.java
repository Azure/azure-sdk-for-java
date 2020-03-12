// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.implementation.AmqpConstants;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.RequestResponseChannel;
import com.azure.core.amqp.implementation.TokenManager;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.Message;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

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
    private static final String PEEK_OPERATION_VALUE = AmqpConstants.VENDOR + ":peek-message";
    private static final String MANAGEMENT_SERVICEBUS_ENTITY_TYPE = AmqpConstants.VENDOR + ":servicebus";
    private static final String MANAGEMENT_SERVER_TIMEOUT = AmqpConstants.VENDOR + ":server-timeout";

    private final Mono<RequestResponseChannel> channelMono;
    private final Scheduler scheduler;
    private final MessageSerializer messageSerializer;

    /*This is to maintain cbs node and get authorization.*/
    private final TokenManager cbsBasedTokenManager;

    // Maintain last peek sequence number
    private AtomicReference<Long>  lastPeekedSequenceNumber = new AtomicReference<>(0L);
    private AtomicReference<Boolean> cbsBasedTokenManagerCalled = new AtomicReference<>(false);



    ManagementChannel(Mono<RequestResponseChannel> responseChannelMono,
                      MessageSerializer messageSerializer, Scheduler scheduler, TokenManager cbsBasedTokenManager
    ) {
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");
        this.channelMono = Objects.requireNonNull(responseChannelMono, "'responseChannelMono' cannot be null.");
        this.scheduler = Objects.requireNonNull(scheduler, "'scheduler' cannot be null.");
        this.cbsBasedTokenManager = Objects.requireNonNull(cbsBasedTokenManager,
            "'cbsBasedTokenManager' cannot be null.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ServiceBusReceivedMessage> peek(long fromSequenceNumber) {

        return peek(ServiceBusReceivedMessage.class, 1, fromSequenceNumber, null)
            .last()
            .publishOn(scheduler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ServiceBusReceivedMessage> peek() {

        return peek(this.lastPeekedSequenceNumber.get() + 1);
    }

    private <T> Flux<T> peek(Class<T> responseType,
                             int maxMessages, long fromSequenceNumber, UUID sessionId) {
        return
            Mono.defer(() -> {
                if (!cbsBasedTokenManagerCalled.get()) {
                    return cbsBasedTokenManager
                        .authorize()
                        .doOnNext(refreshCBSTokenTime -> {
                            cbsBasedTokenManagerCalled.set(true);
                        })
                        .then();
                } else {
                    return Mono.empty();
                }
            })
            .then(
                  channelMono.flatMap(requestResponseChannel -> {

                      Map<String, Object> appProperties = new HashMap<>();
                      // set mandatory application properties for AMQP message.
                      appProperties.put(MANAGEMENT_OPERATION_KEY, PEEK_OPERATION_VALUE);
                      //TODO(hemanttanwar) fix timeour configuration
                      appProperties.put(MANAGEMENT_SERVER_TIMEOUT, "30000");

                      final Message request = Proton.message();
                      final ApplicationProperties applicationProperties = new ApplicationProperties(appProperties);
                      request.setApplicationProperties(applicationProperties);

                      // set mandatory properties on AMQP message body
                      HashMap<String, Object> requestBodyMap = new HashMap<>();
                      requestBodyMap.put(ManagementConstants.REQUEST_RESPONSE_FROM_SEQUENCE_NUMBER, fromSequenceNumber);
                      requestBodyMap.put(ManagementConstants.REQUEST_RESPONSE_MESSAGE_COUNT, maxMessages);

                      if (!Objects.isNull(sessionId)) {
                          requestBodyMap.put(ManagementConstants.REQUEST_RESPONSE_SESSION_ID, sessionId);
                      }

                      request.setBody(new AmqpValue(requestBodyMap));

                      return requestResponseChannel.sendWithAck(request);
                  })
            ).flatMapMany(amqpMessage -> {
                @SuppressWarnings("unchecked")
                List<T> messageListObj =  messageSerializer.deserialize(amqpMessage, List.class);

                 // Assign the last sequence number so that we can peek from next time
                if (messageListObj.size() > 0) {
                    ServiceBusReceivedMessage receivedMessage = (ServiceBusReceivedMessage) messageListObj
                        .get(messageListObj.size() - 1);
                    if (receivedMessage.getSequenceNumber() > 0) {
                        this.lastPeekedSequenceNumber.set(receivedMessage.getSequenceNumber());
                    }
                }
                return Flux.fromIterable(messageListObj);
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
