package com.azure.messaging.servicebus.perf;

import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.perf.core.ServiceBusStressOptions;
import com.azure.messaging.servicebus.perf.core.ServiceTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public class ReceiveMessageTest extends ServiceTest<ServiceBusStressOptions> {

    public ReceiveMessageTest(ServiceBusStressOptions options) {
        super(options);
    }

    private  Mono<Void> sendMessages()
    {
        ServiceBusMessage message =  new ServiceBusMessage(CONTENTS.getBytes());
        return senderAsync.sendMessage(message).then();
    }

    public Mono<Void> globalSetupAsync() {
        ServiceBusMessage message =  new ServiceBusMessage(CONTENTS.getBytes());
        return Flux.range(0, options.getMessagesToSend())
            .flatMap(count -> {

                return senderAsync.sendMessage(message);
            })
            .then();
    }

    @Override
    public void run() {
        IterableStream<ServiceBusReceivedMessageContext> messages = receiver.receiveMessages(options.getMessagesToReceive());
        int receivedMessage = 0;
        for(ServiceBusReceivedMessageContext messageContext : messages) {
            ++receivedMessage;
            System.out.println(" Sync Messages Received Sequence No: " + messageContext.getMessage().getSequenceNumber());
        }
        System.out.println(" Messages Received : " + receivedMessage);
    }

    @Override
    public Mono<Void> runAsync() {
         return receiverAsync
             .receiveMessages()
             .take(options.getMessagesToReceive())
             .map(messageContext -> {
                 System.out.println(" Async Messages Received Sequence No: " + messageContext.getMessage().getSequenceNumber());
                 return messageContext;
             })
             .then();
    }
}
