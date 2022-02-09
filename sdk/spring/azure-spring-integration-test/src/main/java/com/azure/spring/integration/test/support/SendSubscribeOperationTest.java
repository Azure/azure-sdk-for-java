// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.test.support;

import com.azure.spring.integration.core.AzureHeaders;
import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.core.api.CheckpointMode;
import com.azure.spring.integration.core.api.Checkpointer;
import com.azure.spring.integration.core.api.SendOperation;
import com.azure.spring.integration.test.support.pojo.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class SendSubscribeOperationTest<T extends SendOperation> {

    protected T sendSubscribeOperation;

    protected String partitionId = "1";
    protected String destination = "test";
    protected String payload = "payload";
    protected User user = new User(payload);
    protected Map<String, Object> headers = new HashMap<>();
    protected Message<User> userMessage = new GenericMessage<>(user, headers);

    protected List<Message<User>> messages = IntStream.range(1, 5)
                                                      .mapToObj(String::valueOf)
                                                      .map(User::new)
                                                      .map(u -> new GenericMessage<>(u, headers))
                                                      .collect(Collectors.toList());
    private final Message<String> stringMessage = new GenericMessage<>(payload, headers);
    private final Message<byte[]> byteMessage = new GenericMessage<>(payload.getBytes(StandardCharsets.UTF_8), headers);

    @BeforeEach
    public abstract void setUp() throws Exception;

    @Test
    public void testSendString() {
        subscribe(destination, this::stringHandler, String.class);
        sendSubscribeOperation.sendAsync(destination, stringMessage);
    }

    @Test
    public void testSendByte() {
        subscribe(destination, this::byteHandler, byte[].class);
        sendSubscribeOperation.sendAsync(destination, byteMessage);
    }

    @Test
    public void testSendUser() {
        subscribe(destination, this::userHandler, User.class);
        sendSubscribeOperation.sendAsync(destination, userMessage);
    }

    @Test
    public void testSendReceiveWithManualCheckpointMode() {
        setCheckpointConfig(CheckpointConfig.builder().checkpointMode(CheckpointMode.MANUAL).build());
        subscribe(destination, this::manualCheckpointHandler, User.class);
        sendSubscribeOperation.sendAsync(destination, userMessage);
    }

    @Test
    public void testSendReceiveWithRecordCheckpointMode() {
        setCheckpointConfig(CheckpointConfig.builder().checkpointMode(CheckpointMode.RECORD).build());
        subscribe(destination, this::recordCheckpointHandler, User.class);
        messages.forEach(m -> sendSubscribeOperation.sendAsync(destination, m));
        verifyCheckpointSuccessCalled(messages.size());
    }

    protected void manualCheckpointHandler(Message<?> message) {
        assertTrue(message.getHeaders().containsKey(AzureHeaders.CHECKPOINTER));
        Checkpointer checkpointer = message.getHeaders().get(AzureHeaders.CHECKPOINTER, Checkpointer.class);
        assertNotNull(checkpointer);
        verifyCheckpointSuccess(checkpointer);
        verifyCheckpointFailure(checkpointer);
    }

    private void recordCheckpointHandler(Message<?> message) {
        //
    }

    private void stringHandler(Message<?> message) {
        assertEquals(payload, message.getPayload());
    }

    private void byteHandler(Message<?> message) {
        assertEquals(payload, new String((byte[]) message.getPayload(), StandardCharsets.UTF_8));
    }

    private void userHandler(Message<?> message) {
        assertEquals(user, message.getPayload());
    }

    protected abstract void verifyCheckpointSuccessCalled(int times);

    protected abstract void verifyCheckpointBatchSuccessCalled(int times);

    protected abstract void verifyCheckpointFailureCalled(int times);

    protected abstract void subscribe(String destination, Consumer<Message<?>> consumer, Class<?> payloadType);

    protected abstract void setCheckpointConfig(CheckpointConfig checkpointConfig);

    protected void verifyCheckpointSuccess(Checkpointer checkpointer) {
        checkpointer.success();
        verifyCheckpointSuccessCalled(1);
    }

    protected void verifyCheckpointFailure(Checkpointer checkpointer) {
        checkpointer.failure();
        verifyCheckpointFailureCalled(1);
    }

    public String getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(String partitionId) {
        this.partitionId = partitionId;
    }

    public T getSendSubscribeOperation() {
        return sendSubscribeOperation;
    }

    public void setSendSubscribeOperation(T sendSubscribeOperation) {
        this.sendSubscribeOperation = sendSubscribeOperation;
    }

    protected void waitMillis(long millis) {
        if (millis <= 0) {
            millis = 30;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignore) {

        }
    }
}
