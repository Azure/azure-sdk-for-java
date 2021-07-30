// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.test.support;

import com.azure.spring.integration.core.api.PartitionSupplier;
import com.azure.spring.integration.core.api.SendOperation;
import org.junit.jupiter.api.Test;
import org.springframework.core.NestedRuntimeException;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class SendOperationTest<O extends SendOperation> {

    protected String destination = "event-hub";
    protected Message<?> message;
    protected Mono<Void> mono = Mono.empty();
    protected String partitionKey = "key";
    protected String payload = "payload";
    protected O sendOperation = null;
    private String partitionId = "1";

    public SendOperationTest() {
        Map<String, Object> valueMap = new HashMap<>(2);
        valueMap.put("key1", "value1");
        valueMap.put("key2", "value2");
        message = new GenericMessage<>("testPayload", valueMap);
    }

    protected abstract void setupError(String errorMessage);

    @Test
    public void testSendCreateSenderFailure() throws Throwable {
        whenSendWithException();

        assertThrows(NestedRuntimeException.class,
            () -> this.sendOperation.sendAsync(destination, this.message, null).get());
    }

    @Test
    public void testSendFailure() {
        setupError("future failed.");
        CompletableFuture<Void> future = this.sendOperation.sendAsync(destination, this.message, null);

        try {
            future.get();
            fail("Test should fail.");
        } catch (InterruptedException ie) {
            fail("get() should fail with an ExecutionException.");
        } catch (ExecutionException ee) {
            assertEquals("future failed.", ee.getCause().getMessage());
        }
    }

    @Test
    public void testSendWithPartitionId() throws ExecutionException, InterruptedException {
        PartitionSupplier partitionSupplier = new PartitionSupplier();
        partitionSupplier.setPartitionId(partitionId);
        CompletableFuture<Void> future = this.sendOperation.sendAsync(destination, message, partitionSupplier);

        assertNull(future.get());
        verifySendWithPartitionId(1);
        verifyPartitionSenderCalled(1);
    }

    @Test
    public void testSendWithPartitionKey() throws ExecutionException, InterruptedException {
        PartitionSupplier partitionSupplier = new PartitionSupplier();
        partitionSupplier.setPartitionKey(partitionKey);
        CompletableFuture<Void> future = this.sendOperation.sendAsync(destination, message, partitionSupplier);

        assertNull(future.get());
        verifySendWithPartitionKey(1);
        verifyGetClientCreator(1);
    }

    @Test
    public void testSendWithSessionId() throws ExecutionException, InterruptedException {
        Message<?> messageWithSeesionId = new GenericMessage<>("testPayload",
            Map.of("key1", "value1", "azure_service_bus_session_id", "TestSessionId"));
        CompletableFuture<Void> future = this.sendOperation.sendAsync(destination, messageWithSeesionId);

        assertNull(future.get());
        verifySendCalled(1);
    }

    @Test
    public void testSendWithSessionIdAndPartitionKeyDifferent() throws ExecutionException, InterruptedException {
        Message<?> messageWithSeesionId = new GenericMessage<>("testPayload", Map.of("key1", "value1",
            "azure_service_bus_session_id", "TestSessionId", "azure_service_bus_partition_key", "TestPartitionKey"));
        CompletableFuture<Void> future = this.sendOperation.sendAsync(destination, messageWithSeesionId);

        assertNull(future.get());
        verifySendCalled(1);
    }

    @Test
    public void testSendWithoutPartition() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = this.sendOperation.sendAsync(destination, message, new PartitionSupplier());

        assertNull(future.get());
        verifySendCalled(1);
    }

    @Test
    public void testSendWithoutPartitionSupplier() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = this.sendOperation.sendAsync(destination, message, null);

        assertNull(future.get());
        verifySendCalled(1);
    }

    protected abstract void verifyGetClientCreator(int times);

    protected abstract void verifyPartitionSenderCalled(int times);

    protected abstract void verifySendCalled(int times);

    protected abstract void verifySendWithPartitionId(int times);

    protected abstract void verifySendWithPartitionKey(int times);

    protected abstract void whenSendWithException();

    public Mono<Void> getMono() {
        return mono;
    }

    public void setMono(Mono<Void> mono) {
        this.mono = mono;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public O getSendOperation() {
        return sendOperation;
    }

    public void setSendOperation(O sendOperation) {
        this.sendOperation = sendOperation;
    }
}
