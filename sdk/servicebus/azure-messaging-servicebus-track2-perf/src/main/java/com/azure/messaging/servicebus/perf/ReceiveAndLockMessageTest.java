package com.azure.messaging.servicebus.perf;


import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.models.ReceiveMode;
import com.azure.messaging.servicebus.perf.core.ServiceBusStressOptions;
import com.azure.messaging.servicebus.perf.core.ServiceTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ReceiveAndLockMessageTest extends ServiceTest<ServiceBusStressOptions> {

    public ReceiveAndLockMessageTest(ServiceBusStressOptions options) {
        super(options, ReceiveMode.PEEK_LOCK);
    }

    private Mono<Void> sendMessages() {
        ServiceBusMessage message = new ServiceBusMessage(CONTENTS.getBytes());
        return senderAsync.sendMessage(message).then();
    }

    public Mono<Void> globalSetupAsync() {
        // Since test does warm up and test many times, we are sending many messages, so we will have them available.
        int totalMessageMultiplier = 50;

        ServiceBusMessage message = new ServiceBusMessage(CONTENTS.getBytes());
        return Flux.range(0, options.getMessagesToSend() * totalMessageMultiplier)
            .flatMap(count -> {
                return senderAsync.sendMessage(message);
            })
            .then();
    }

    @Override
    public void run() {
        IterableStream<ServiceBusReceivedMessageContext> messages = receiver.receiveMessages(options.getMessagesToReceive());
        for (ServiceBusReceivedMessageContext messageContext : messages) {
            receiver.complete(messageContext.getMessage().getLockToken());
        }
    }

    @Override
    public Mono<Void> runAsync() {
        Mono<Void> operator = receiverAsync
            .receiveMessages()
            .take(options.getMessagesToReceive())
            .map(messageContext -> {
                receiverAsync.complete(messageContext.getMessage().getLockToken()).block();
                return messageContext;
            })
            .then();
        return operator;
    }
}
