// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;


import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.perf.test.core.TestDataCreationHelper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Performance test.
 */
public class ReceiveAndLockMessageTest extends ServiceTest<ServiceBusStressOptions> {
    private final ClientLogger logger = new ClientLogger(ReceiveAndLockMessageTest.class);
    private final ServiceBusStressOptions options;
    private final String messageContent;
    private final Duration testDuration;
    private ServiceBusReceiverAsyncClient receiverAsync;

    /**
     * Creates test object
     * @param options to set performance test options.
     */
    public ReceiveAndLockMessageTest(ServiceBusStressOptions options) {
        super(options, ServiceBusReceiveMode.PEEK_LOCK);
        this.options = options;
        this.messageContent = TestDataCreationHelper.generateRandomString(options.getMessagesSizeBytesToSend());

        testDuration = Duration.ofSeconds(options.getDuration() - 1);
    }

    @Override
    public Mono<Void> setupAsync() {
        // Since test does warm up and test many times, we are sending many messages, so we will have them available.
        return Mono.defer(() -> {
            int total = options.getMessagesToSend() * TOTAL_MESSAGE_MULTIPLIER;

            List<ServiceBusMessage> messages = new ArrayList<>();
            for (int i = 0; i < total; ++i) {
                ServiceBusMessage message =  new ServiceBusMessage(messageContent);
                message.setMessageId(UUID.randomUUID().toString());
                messages.add(message);
            }
            return senderAsync.sendMessages(messages);
        });
    }

    @Override
    public void run() {
        IterableStream<ServiceBusReceivedMessage> messages = receiver
            .receiveMessages(options.getMessagesToReceive());

        int count = 0;
        for (ServiceBusReceivedMessage message : messages) {
            receiver.complete(message);
            ++count;
        }

        if (count <= 0) {
            throw logger.logExceptionAsWarning(new RuntimeException("Error. Should have received some messages."));
        }
    }

    @Override
    public Mono<Void> runAsync() {
      /*  Flux<ServiceBusReceivedMessage> messagesFlux = Flux.defer(() -> {
            String connectionString = System.getenv(AZURE_SERVICE_BUS_CONNECTION_STRING);
            if (CoreUtils.isNullOrEmpty(connectionString)) {
                throw logger.logExceptionAsError(new IllegalArgumentException("Environment variable "
                    + AZURE_SERVICE_BUS_CONNECTION_STRING + " must be set."));
            }

            String queueName = System.getenv(AZURE_SERVICEBUS_QUEUE_NAME);
            if (CoreUtils.isNullOrEmpty(queueName)) {
                throw logger.logExceptionAsError(new IllegalArgumentException("Environment variable "
                    + AZURE_SERVICEBUS_QUEUE_NAME + " must be set."));
            }

            final ServiceBusClientBuilder baseBuilder = new ServiceBusClientBuilder()
                .proxyOptions(ProxyOptions.SYSTEM_DEFAULTS)
                .retryOptions(new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(60)))
                .transportType(AmqpTransportType.AMQP)
                .connectionString(connectionString);

            return baseBuilder
                .receiver()
                .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                .queueName(queueName)
                .buildAsyncClient().receiveMessages();
        });
        */

        Mono<ServiceBusReceiverAsyncClient> receiverAsyncMono = Mono.defer(() -> {
            System.out.println(new Date() + " !!!! Now to create the receiver async client, get the connection string");
            String connectionString = System.getenv(AZURE_SERVICE_BUS_CONNECTION_STRING);
            if (CoreUtils.isNullOrEmpty(connectionString)) {
                throw logger.logExceptionAsError(new IllegalArgumentException("Environment variable "
                    + AZURE_SERVICE_BUS_CONNECTION_STRING + " must be set."));
            }

            String queueName = System.getenv(AZURE_SERVICEBUS_QUEUE_NAME);
            if (CoreUtils.isNullOrEmpty(queueName)) {
                throw logger.logExceptionAsError(new IllegalArgumentException("Environment variable "
                    + AZURE_SERVICEBUS_QUEUE_NAME + " must be set."));
            }

            final ServiceBusClientBuilder baseBuilder = new ServiceBusClientBuilder()
                .proxyOptions(ProxyOptions.SYSTEM_DEFAULTS)
                .retryOptions(new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(60)))
                .transportType(AmqpTransportType.AMQP)
                .connectionString(connectionString);
            System.out.println(new Date() + " !!!! Now create the receiver async client.");

            ServiceBusReceiverAsyncClient receiverClient = baseBuilder
                .receiver()
                .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                .queueName(queueName)
                .buildAsyncClient();

            return Mono.just(receiverClient);
        });

        final Mono<Long> timeout = Mono.delay(testDuration);

        return receiverAsyncMono.map(serviceBusReceiverAsyncClient -> {
            return serviceBusReceiverAsyncClient.receiveMessages()
                .flatMap( m -> {
                    return serviceBusReceiverAsyncClient.complete(m);
                }).then();
        });
            /*flatMap(serviceBusReceiverAsyncClient -> {
            System.out.println(new Date() + " !!!! Created the receiver  client :" + serviceBusReceiverAsyncClient);
            //return Mono.empty();
            serviceBusReceiverAsyncClient.receiveMessages()
                .flatMap(message -> {
                    System.out.println(new Date() + " !!!! received Message SQ :" + message.getSequenceNumber());
                    return serviceBusReceiverAsyncClient.complete(message);
                });
        }).then();*/

        /*return Flux.usingWhen(
            receiverAsyncMono.flatMap(serviceBusReceiverAsyncClient -> {
                System.out.println(new Date() + " !!!! Created the receiver  client :" + serviceBusReceiverAsyncClient);
                return serviceBusReceiverAsyncClient.receiveMessages()
                    .flatMap(message -> {
                        System.out.println(new Date() + " !!!! received Message SQ :" + message.getSequenceNumber());
                        return serviceBusReceiverAsyncClient.complete(message);
                    });
            }),
            receiverM -> Mono.when(timeout),
            closing
                -> {
                System.out.println(new Date() + " closing the receiver.");
               // this.endTime = System.nanoTime();

                return Mono.defer(() -> {
                    System.out.println(new Date() + " closing the receiver.");
                    return Mono.empty();
                });
            })
            .doFinally(signal -> System.out.println("Finished cleaning up processor resources."))
            .then();
        */

      /*  return receiverAsync
            .receiveMessages()
            .take(Duration.ofMinutes(2))
            //.take(options.getMessagesToReceive())
            .flatMap(message -> {
                return receiverAsync.complete(message).thenReturn(true);
            }, 1).then();
        */
    }
}
