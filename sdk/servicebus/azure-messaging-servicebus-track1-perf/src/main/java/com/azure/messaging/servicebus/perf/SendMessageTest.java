package com.azure.messaging.servicebus.perf;

import com.azure.messaging.servicebus.perf.core.ServiceBusStressOptions;
import com.azure.messaging.servicebus.perf.core.ServiceTest;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class SendMessageTest extends ServiceTest<ServiceBusStressOptions> {
    private Message message  = null;

    public SendMessageTest(ServiceBusStressOptions options) throws InterruptedException, ExecutionException, ServiceBusException {
        super(options, ReceiveMode.PEEKLOCK);
    }



    public Mono<Void> globalSetupAsync() {
        String messageId = UUID.randomUUID().toString();
        message = new Message(CONTENTS);
        message.setMessageId(messageId);
        return Mono.empty();
    }

    @Override
    public void run() {
        try {

            sender.send(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ServiceBusException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Mono<Void> runAsync() {
        try {
            sender.sendAsync(message).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return Mono.empty();
    }
}
