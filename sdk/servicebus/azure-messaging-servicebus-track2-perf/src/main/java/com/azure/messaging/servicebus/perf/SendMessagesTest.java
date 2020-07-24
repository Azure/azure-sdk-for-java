package com.azure.messaging.servicebus.perf;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.models.ReceiveMode;
import com.azure.messaging.servicebus.perf.core.ServiceBusStressOptions;
import com.azure.messaging.servicebus.perf.core.ServiceTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class SendMessagesTest extends ServiceTest<ServiceBusStressOptions> {
    private List<ServiceBusMessage> messages = new ArrayList<>();

    public SendMessagesTest(ServiceBusStressOptions options) {
        super(options, ReceiveMode.PEEK_LOCK);
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
                System.out.println(count + ". Adding message in List." );
                messages.add(message);
                return Mono.empty();
            })
            .then();
    }

    @Override
    public void run() {
        sender.sendMessages(messages);
    }

    @Override
    public Mono<Void> runAsync() {
        return senderAsync.sendMessages(messages).then();
    }
}
