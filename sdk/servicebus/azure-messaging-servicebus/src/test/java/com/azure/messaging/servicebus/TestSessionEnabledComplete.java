package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TestSessionEnabledComplete {
    static Scheduler scheduler = Schedulers.parallel();



    public static void main(String[] args) throws InterruptedException {
        String queueName = "queue-session-0";
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString("")
            .proxyOptions(ProxyOptions.SYSTEM_DEFAULTS)
            .transportType(AmqpTransportType.AMQP)
            .scheduler(scheduler)
            .receiver().buildAsyncClient();

       // ServiceBusSenderAsyncClient sender = builder.sender().buildAsyncClient();
            //.queueName(queueName).buildAsyncClient();
            // Arrange

            final byte[] sessionState = "Finished".getBytes(UTF_8);
            final String messageId = UUID.randomUUID().toString();
            //final ServiceBusMessage messageToSend = getMessage(messageId, true);

            //sendMessage(messageToSend).block(Duration.ofSeconds(10));
            System.out.println("!!!! Test Will receive Message");
            // Act
            AtomicReference<ServiceBusReceivedMessage> receivedMessage = new AtomicReference<>();
            //AtomicReference<String> session = new AtomicReference<>();
        /*StepVerifier.create(receiver.receiveMessages()
            .take(1)
            .flatMap(message -> {
                logger.info("SessionId: {}. LockToken: {}. LockedUntil: {}. Message received.",
                    message.getSessionId(), message.getLockToken(), message.getLockedUntil());
                receivedMessage.set(message);
                System.out.println("!!!! Message received and setting session state session ID");
                return receiver.setSessionState(sessionState);
            }))
            .expectComplete()
            .verify()
           .verifyComplete();*/
       /* System.out.println("!!!! Getting  session state ");
        StepVerifier.create(receiver.getSessionState())
            .assertNext(state -> {
                System.out.println("!!!!Got   session state ");
                logger.info("State received: {}", new String(state, UTF_8));
                assertArrayEquals(sessionState, state);
            })
            .verifyComplete();*/
            ServiceBusReceivedMessage message = receiver.receiveMessages().take(1).blockFirst();
            System.out.println("!!!! Got the message SQ Num : " + message.getSequenceNumber());
           // Assertions.assertEquals(sessionId, message.getSessionId());
            //messagesPending.decrementAndGet();
            System.out.println("!!!! Now completing the message");
            receiver.complete(message)
                .doOnNext(aVoid -> {
                    System.out.println("!!!! Test doOnNext .. completed the message.");
                })
                .doOnSuccess(aVoid -> {
                    System.out.println("!!!! doOnSuccess Test successfully completed the message.");
                })
                .block(Duration.ofSeconds(15));
            System.out.println("!!!! Completed message Test Ends !!!! ");
            //messagesPending.decrementAndGet();
            TimeUnit.SECONDS.sleep(30);

    }
}
