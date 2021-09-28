// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.messaging.PartitionSupplier;
import com.azure.spring.messaging.core.SendOperation;
import com.azure.spring.servicebus.support.ServiceBusRuntimeException;
import com.azure.spring.messaging.core.SendOperationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test cases to test service bus send operations.
 *
 * @param <T>
 * @param <C>
 */
public abstract class ServiceBusTemplateSendTest<T extends ServiceBusSenderFactory,
                                                    C extends ServiceBusSenderAsyncClient>
    extends SendOperationTest<SendOperation> {

    protected String destination = "event-hub";
    protected Mono<Void> mono = Mono.empty();
    protected String partitionKey = "key";
    protected String payload = "payload";
    private String partitionId = "1";
    protected C mockClient;
    protected T mockClientFactory;

    @BeforeEach
    public abstract void setUp();

    @Override
    protected void setupError(String errorMessage) {
        when(this.mockClient.sendMessage(isA(ServiceBusMessage.class))).thenReturn(Mono.error(new IllegalArgumentException(
            errorMessage)));
    }

    @Override
    protected void verifySendCalled(int times) {
        verify(this.mockClient, times(times)).sendMessage(isA(ServiceBusMessage.class));
    }

    @Test
    public void testSendWithPartitionId() throws ExecutionException, InterruptedException {
        PartitionSupplier partitionSupplier = new PartitionSupplier();
        partitionSupplier.setPartitionId(partitionId);
        Mono<Void> mono = this.sendOperation.sendAsync(destination, message, partitionSupplier);

        assertNull(mono.block());
        verifySendWithPartitionId(1);
        verifyPartitionSenderCalled(1);
    }

    @Test
    public void testSendWithPartitionKey() throws ExecutionException, InterruptedException {
        PartitionSupplier partitionSupplier = new PartitionSupplier();
        partitionSupplier.setPartitionKey(partitionKey);
        Mono<Void> mono = this.sendOperation.sendAsync(destination, message, partitionSupplier);

        assertNull(mono.block());
        verifySendWithPartitionKey(1);
        verifyGetClientCreator(1);
    }

    @Test
    public void testSendWithSessionId() throws ExecutionException, InterruptedException {
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
    public void testSendWithSessionIdAndPartitionKeyDifferent() throws ExecutionException, InterruptedException {
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
    public void testSendWithoutPartition() throws ExecutionException, InterruptedException {
        Mono<Void> mono = this.sendOperation.sendAsync(destination, message, new PartitionSupplier());

        assertNull(mono.block());
        verifySendCalled(1);
    }

    @Test
    public void testSendWithoutPartitionSupplier() throws ExecutionException, InterruptedException {
        Mono<Void> mono = this.sendOperation.sendAsync(destination, message, null);

        assertNull(mono.block());
        verifySendCalled(1);
    }

    protected void verifyPartitionSenderCalled(int times) {
        verifySendCalled(times);
    }

    @Override
    protected void whenSendWithException() {
        when(this.mockClientFactory.getOrCreateSender(anyString())).thenThrow(ServiceBusRuntimeException.class);
    }

    @Override
    protected void verifyGetClientCreator(int times) {
        verify(this.mockClientFactory, times(times)).getOrCreateSender(anyString());
    }

    protected void verifySendWithPartitionKey(int times) {
        verifySendCalled(times);
    }

    protected void verifySendWithPartitionId(int times) {
        verifySendCalled(times);
    }

}
