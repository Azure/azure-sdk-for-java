package com.azure.messaging.servicebus.perf;

import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.perf.core.ServiceBusStressOptions;
import com.azure.messaging.servicebus.perf.core.ServiceTest;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class SendAndReceiveTest extends ServiceTest<ServiceBusStressOptions> {

    public SendAndReceiveTest(ServiceBusStressOptions options) {
        super(options);
    }

    private  Mono<Void> sendMessages()
    {
        ServiceBusMessage message =  new ServiceBusMessage(CONTENTS.getBytes());
        return senderAsync.sendMessage(message).then();
    }

    public Mono<Void> globalSetupAsync() {
        ServiceBusMessage message =  new ServiceBusMessage(CONTENTS.getBytes());
        return Flux.range(0, options.getCount())
            .flatMap(count -> senderAsync.sendMessage(message))
            .then();
    }

    @Override
    public void run() {
        IterableStream<ServiceBusReceivedMessageContext> messages = receiver.receiveMessages(1);
        int receivedMessage = 0;
        for(ServiceBusReceivedMessageContext messageContext : messages) {
            ++receivedMessage;
        }
        System.out.println(" Messages Received : " + receivedMessage);
    }

    @Override
    public Mono<Void> runAsync() {
         return receiverAsync
             .receiveMessages()
             .next()
             .then();
    }
}
