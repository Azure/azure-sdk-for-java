package com.azure.messaging.servicebus.perf;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.perf.core.ServiceBusStressOptions;
import com.azure.messaging.servicebus.perf.core.ServiceTest;
import reactor.core.publisher.Mono;

public class SendMessageTest extends ServiceTest<ServiceBusStressOptions> {
    private ServiceBusMessage message =  new ServiceBusMessage(CONTENTS.getBytes());;

    public SendMessageTest(ServiceBusStressOptions options) {
        super(options);
    }

    private  Mono<Void> sendMessages()
    {
        ServiceBusMessage message =  new ServiceBusMessage(CONTENTS.getBytes());
        return senderAsync.sendMessage(message).then();
    }

    public Mono<Void> globalSetupAsync() {
        /*ServiceBusMessage message =  new ServiceBusMessage(CONTENTS.getBytes());
        return Flux.range(0, options.getMessagesToSend())
            .flatMap(count -> senderAsync.sendMessage(message))
            .then();
        */
        return Mono.empty();
    }

    @Override
    public void run() {
        sender.sendMessage(message);
    }

    @Override
    public Mono<Void> runAsync() {
        return senderAsync.sendMessage(message).then();
    }
}
