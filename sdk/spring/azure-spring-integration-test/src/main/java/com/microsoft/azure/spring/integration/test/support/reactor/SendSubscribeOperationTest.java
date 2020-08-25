// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.test.support.reactor;

import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.api.CheckpointConfig;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.core.api.reactor.Checkpointer;
import com.microsoft.azure.spring.integration.core.api.reactor.SendOperation;
import com.microsoft.azure.spring.integration.test.support.pojo.User;
import org.junit.Before;
import org.junit.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public abstract class SendSubscribeOperationTest<T extends SendOperation> {

    protected T sendSubscribeOperation;
    protected String partitionId = "1";
    protected String destination = "test";
    private String payload = "payload";
    private User user = new User(payload);
    private Map<String, Object> headers = new HashMap<>();
    protected Message<User> userMessage = new GenericMessage<>(user, headers);

    protected List<Message<User>> messages =
        IntStream.range(1, 5).mapToObj(String::valueOf).map(User::new).map(u -> new GenericMessage<>(u, headers))
            .collect(Collectors.toList());
    private Message<String> stringMessage = new GenericMessage<>(payload, headers);
    private Message<byte[]> byteMessage = new GenericMessage<>(payload.getBytes(StandardCharsets.UTF_8), headers);

    public T getSendSubscribeOperation() {
        return sendSubscribeOperation;
    }

    public void setSendSubscribeOperation(T sendSubscribeOperation) {
        this.sendSubscribeOperation = sendSubscribeOperation;
    }

    public String getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(String partitionId) {
        this.partitionId = partitionId;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Message<User> getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(Message<User> userMessage) {
        this.userMessage = userMessage;
    }

    public List<Message<User>> getMessages() {
        return messages;
    }

    public void setMessages(List<Message<User>> messages) {
        this.messages = messages;
    }

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

    private void manualCheckpointHandler(Message<?> message) {
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

    @Before
    public abstract void setUp();

    protected abstract void verifyCheckpointSuccessCalled(int times);

    @Deprecated
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
}
