// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ReceiveAsyncOptions;
import com.azure.messaging.servicebus.models.ReceiveMode;
import com.azure.messaging.servicebus.models.ServiceBusErrorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServiceBusMultiSessionProcessorSample {

    public static void main(String[] args) {
        final MyStorageMessageProcessor myMessageProcessor = new MyStorageMessageProcessor();
        String connectionString = System.getenv("AZURE_SERVICEBUS_CONNECTION_STRING");
        String queueName = System.getenv("AZURE_SERVICEBUS_QUEUE_NAME");
        ServiceBusReceiverAsyncClient multiSessionReceiverAsyncClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
            .queueName(queueName)
            .maxConcurrentSessions(1) // This will enable infinite roll-over of next available session id
            .buildAsyncClient();
        final ReceiveAsyncOptions options = new ReceiveAsyncOptions()
            .setEnableAutoComplete(false) // user want to settle the message
            .setMaxAutoRenewDuration(Duration.ofSeconds(20));

        Disposable subscription = multiSessionReceiverAsyncClient.receiveMultiSession(options)
            .flatMap(receivedMessage -> {
               /* System.out.println("Session State : " + multiSessionReceiverAsyncClient.getSessionState(
                    receivedMessage.getSessionId()));*/
                System.out.println("!!! Received Message seq no  "+ receivedMessage.getSequenceNumber() +" SessionId " +  receivedMessage.getSessionId() + " Data : " +  new String (receivedMessage.getBody()));
                if (receivedMessage.isSessionError()) {
                    myMessageProcessor.processError(receivedMessage.getServiceBusErrorContext());
                    return Mono.empty();
                }
                return Mono.empty();
                //return myMessageProcessor.processMessage(receivedMessage, multiSessionReceiverAsyncClient);
            }).subscribe();
        System.out.println(" Waiting for message to arrive .. ");

        // Subscribe is not a blocking call so we sleep here so the program does not end.
        try {
            Thread.sleep(Duration.ofSeconds(3 * 60).toMillis());
        } catch (InterruptedException ignored) {
        }
        System.out.println(" !!! Test DONE !!!  ");
        // Disposing of the subscription will cancel the receive() operation.
        subscription.dispose();

        // Close the receiver.
        multiSessionReceiverAsyncClient.close();
    }
    static class MyStorageMessageProcessor {
        private final Logger logger = LoggerFactory.getLogger(MyStorageMessageProcessor.class);
        private final AtomicBoolean isDisposed = new AtomicBoolean();

        Mono<Void> processMessage(ServiceBusReceivedMessage message, ServiceBusReceiverAsyncClient receiver) {
            System.out.println("Process message here. Session id : " + message.getSessionId()
                + ", message Id :" + message.getMessageId());

            // Process the message here.
            // Change the `messageProcessed` according to you business logic and if you are able to process the
            // message successfully.

            boolean messageProcessed =  true;

            if (messageProcessed) {
                System.out.println(getClass().getName() + " Going to complete message .. ");
                receiver.complete(message).then();
                System.out.println(getClass().getName() + " After  completing message .. ");
                return Mono.empty();
            } else {
                return  receiver.abandon(message).then();
            }
        }
        void processError(ServiceBusErrorContext errorContext) {
            System.out.println("Error !!! . FullyQualifiedNamespace : "
                + errorContext.getFullyQualifiedNamespace()
                + ", EntityPath :" + errorContext.getEntityPath());
        }
    }
}
