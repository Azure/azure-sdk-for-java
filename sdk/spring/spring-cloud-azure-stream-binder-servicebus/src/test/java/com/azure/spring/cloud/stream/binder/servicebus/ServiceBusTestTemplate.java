// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.messaging.PartitionSupplier;
import com.azure.spring.service.servicebus.processor.RecordMessageProcessingListener;
import com.azure.spring.servicebus.core.ServiceBusTemplate;
import com.azure.spring.servicebus.core.producer.ServiceBusProducerFactory;
import com.azure.spring.servicebus.support.converter.ServiceBusMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A test implementation of {@link ServiceBusTemplate}. This is used for testing.
 * <p>
 * The difference between this test operation and the original one is that test operation will invoke the message
 * processing immediately after sending it out.
 */
public class ServiceBusTestTemplate extends ServiceBusTemplate {

    private final ServiceBusMessageConverter messageConverter = new ServiceBusMessageConverter();
    private final RecordMessageProcessingListener listener;

    public ServiceBusTestTemplate(ServiceBusProducerFactory senderClientFactory, RecordMessageProcessingListener listener) {
        super(senderClientFactory);
        this.listener = listener;

    }

    @Override
    public <U> Mono<Void> sendAsync(String name, Message<U> message, PartitionSupplier partitionSupplier) {
        ServiceBusMessage azureMessage = messageConverter.fromMessage(message, ServiceBusMessage.class);

        final ServiceBusReceivedMessageContext receivedMessageContext = mockReceivedMessageContext(azureMessage);

//        if (queuesByName.containsKey(name)) {
//            queuesByName.get(name).add(receivedMessageContext);
//        } else {
//            queuesByName.put(name, new ArrayList<>(Arrays.asList(receivedMessageContext)));
//        }
//
//        getRandom(processorsByQueue.get(name)).ifPresent(c -> c.processMessage().accept(receivedMessageContext));
        listener.onMessage(receivedMessageContext);
        return Mono.empty();
    }
//
//    @Override
//    @SuppressWarnings({ "rawtypes", "unchecked" })
//    protected void internalSubscribe(String name, Consumer<Message<?>> consumer, Class<?> payloadType) {
//        DefaultServiceBusMessageProcessor messageProcessor = new DefaultServiceBusMessageProcessor(
//            this.checkpointConfig, payloadType, consumer, this.messageConverter);
//
//        if (processorsByQueue.containsKey(name)) {
//            processorsByQueue.get(name).add(messageProcessor);
//        } else {
//            processorsByQueue.put(name, new ArrayList<>(Arrays.asList(messageProcessor)));
//        }
//    }
//
//    @Override
//    public boolean unsubscribe(String name) {
//        processorsByQueue.remove(name);
//        return true;
//    }
//
//    private static <E> Optional<E> getRandom(Collection<E> e) {
//        if (0 == e.size()) {
//            return Optional.empty();
//        }
//        return e.stream().skip(new Random().nextInt(e.size())).findFirst();
//    }

    private ServiceBusReceivedMessageContext mockReceivedMessageContext(@NonNull ServiceBusMessage message) {
        ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);
        when(receivedMessage.getBody()).thenReturn(message.getBody());

        ServiceBusReceivedMessageContext receivedMessageContext = mock(ServiceBusReceivedMessageContext.class);
        when(receivedMessageContext.getMessage()).thenReturn(receivedMessage);
        return receivedMessageContext;
    }

}

