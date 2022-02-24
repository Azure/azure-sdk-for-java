// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.messaging.PartitionSupplier;
import com.azure.spring.messaging.core.SendOperationTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test cases to test service bus send operations.
 *
 */
public class ServiceBusTemplateSendTests extends SendOperationTests<ServiceBusTemplate> {

    protected ServiceBusSenderAsyncClient mockSenderClient;
    protected ServiceBusProducerFactory producerFactory;

    @BeforeEach
    public void setUp() {
        this.producerFactory = mock(ServiceBusProducerFactory.class);
        this.mockSenderClient = mock(ServiceBusSenderAsyncClient.class);

        when(this.producerFactory.createProducer(eq(this.destination), any())).thenReturn(this.mockSenderClient);
        when(this.mockSenderClient.sendMessage(isA(ServiceBusMessage.class))).thenReturn(this.mono);

        this.sendOperation = new ServiceBusTemplate(producerFactory);
    }

    @Override
    protected void setupError(String errorMessage) {
        when(this.mockSenderClient.sendMessage(isA(ServiceBusMessage.class))).thenReturn(Mono.error(new IllegalArgumentException(
            errorMessage)));
    }

    @Override
    protected void verifySendCalled(int times) {
        verify(this.mockSenderClient, times(times)).sendMessage(isA(ServiceBusMessage.class));
    }

    @Test
    public void testSendWithPartitionId() {
        PartitionSupplier partitionSupplier = new PartitionSupplier();
        partitionSupplier.setPartitionId("1");
        Mono<Void> mono = this.sendOperation.sendAsync(destination, message, partitionSupplier);

        assertNull(mono.block());
        verifySendWithPartitionId(1);
        verifyPartitionSenderCalled(1);
    }

    @Test
    public void testSendWithPartitionKey() {
        PartitionSupplier partitionSupplier = new PartitionSupplier();
        partitionSupplier.setPartitionKey("key");
        Mono<Void> mono = this.sendOperation.sendAsync(destination, message, partitionSupplier);

        assertNull(mono.block());
        verifySendWithPartitionKey(1);
        verifyGetClientCreator(1);
    }

    @Test
    public void testSendWithSessionId() {
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("key1", "value1");
        valueMap.put("key2", "value2");
        valueMap.put("azure_service_bus_session_id", "TestSessionId");
        Message<?> messageWithSeesionId = new GenericMessage<>("testPayload", valueMap);
        Mono<Void> mono = this.sendOperation.sendAsync(destination, messageWithSeesionId);

        assertNull(mono.block());
        verifySendCalled(1);
    }

    @Test
    public void testSendWithSessionIdAndPartitionKeyDifferent() {
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("key1", "value1");
        valueMap.put("key2", "value2");
        valueMap.put("azure_service_bus_session_id", "TestSessionId");
        valueMap.put("azure_service_bus_partition_key", "TestPartitionKey");
        Message<?> messageWithSeesionIdAndPartitionKey = new GenericMessage<>("testPayload", valueMap);
        Mono<Void> mono = this.sendOperation.sendAsync(destination, messageWithSeesionIdAndPartitionKey);

        assertNull(mono.block());
        verifySendCalled(1);
    }

    @Test
    public void testSendWithoutPartition() {
        Mono<Void> mono = this.sendOperation.sendAsync(destination, message, new PartitionSupplier());

        assertNull(mono.block());
        verifySendCalled(1);
    }

    @Test
    public void testSendWithoutPartitionSupplier() {
        Mono<Void> mono = this.sendOperation.sendAsync(destination, message, null);

        assertNull(mono.block());
        verifySendCalled(1);
    }

    protected void verifyPartitionSenderCalled(int times) {
        verifySendCalled(times);
    }

    @Override
    protected void verifyGetClientCreator(int times) {
        verify(this.producerFactory, times(times)).createProducer(anyString(), any());
    }

    protected void verifySendWithPartitionKey(int times) {
        verifySendCalled(times);
    }

    protected void verifySendWithPartitionId(int times) {
        verifySendCalled(times);
    }

}
