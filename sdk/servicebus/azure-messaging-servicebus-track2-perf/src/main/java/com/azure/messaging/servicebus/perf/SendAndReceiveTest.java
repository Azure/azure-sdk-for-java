package com.azure.messaging.servicebus.perf;

import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.perf.core.ServiceTest;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class SendAndReceiveTest extends ServiceTest<PerfStressOptions> {

    public SendAndReceiveTest(PerfStressOptions options) {
        super(options);
    }

    public void globalSetup() {
        String id = "getblobstest-" + UUID.randomUUID();
        ServiceBusMessage message = new ServiceBusMessage("".getBytes());
        sender.sendMessage(message);

    }

    @Override
    public void run() {
        IterableStream<ServiceBusReceivedMessageContext> messages = receiver.receiveMessages(1);
        for(ServiceBusReceivedMessageContext messageContext : messages) {

        }
    }

    @Override
    public Mono<Void> runAsync() {
        return senderAsync.sendMessage(null);
    }
}
