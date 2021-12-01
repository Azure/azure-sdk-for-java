// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.test.support.reactor;

import com.azure.spring.integration.core.AzureHeaders;
import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.core.api.CheckpointMode;
import com.azure.spring.integration.core.api.reactor.Checkpointer;
import com.azure.spring.integration.core.api.reactor.SendOperation;
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

/**
 *
 * @param <T> The type that SendOperation.
 */
public abstract class SendSubscribeOperationTest<T extends SendOperation> {

    /**
     * The destination.
     */
    protected String destination = "test";

    /**
     * The partition id.
     */
    protected String partitionId = "1";

    /**
     * Send subscribe operation.
     */
    protected T sendSubscribeOperation;

    private Map<String, Object> headers = new HashMap<>();

    /**
     * The message list.
     */
    protected List<Message<User>> messages = IntStream.range(1, 5)
                                                      .mapToObj(String::valueOf)
                                                      .map(User::new)
                                                      .map(u -> new GenericMessage<>(u, headers))
                                                      .collect(Collectors.toList());
    private String payload = "payload";
    private Message<byte[]> byteMessage = new GenericMessage<>(payload.getBytes(StandardCharsets.UTF_8), headers);
    private Message<String> stringMessage = new GenericMessage<>(payload, headers);
    private User user = new User(payload);

    /**
     * The user message.
     */
    protected Message<User> userMessage = new GenericMessage<>(user, headers);

    /**
     *
     * @param checkpointConfig The checkpointConfig.
     */
    protected abstract void setCheckpointConfig(CheckpointConfig checkpointConfig);

    /**
     * Set up.
     */
    @BeforeEach
    public abstract void setUp();

    /**
     *
     * @param destination The destination.
     * @param consumer The consumer.
     * @param payloadType The payloadType.
     */
    protected abstract void subscribe(String destination, Consumer<Message<?>> consumer, Class<?> payloadType);

    /**
     * Test send byte.
     */
    @Test
    public void testSendByte() {
        subscribe(destination, this::byteHandler, byte[].class);
        sendSubscribeOperation.sendAsync(destination, byteMessage);
    }

    /**
     * Test send receive with manual checkpoint mode.
     */
    @Test
    public void testSendReceiveWithManualCheckpointMode() {
        setCheckpointConfig(CheckpointConfig.builder().checkpointMode(CheckpointMode.MANUAL).build());
        subscribe(destination, this::manualCheckpointHandler, User.class);
        sendSubscribeOperation.sendAsync(destination, userMessage);
    }

    /**
     * Test send receive with record checkpoint mode.
     */
    @Test
    public void testSendReceiveWithRecordCheckpointMode() {
        setCheckpointConfig(CheckpointConfig.builder().checkpointMode(CheckpointMode.RECORD).build());
        subscribe(destination, this::recordCheckpointHandler, User.class);
        messages.forEach(m -> sendSubscribeOperation.sendAsync(destination, m));
        verifyCheckpointSuccessCalled(messages.size());
    }

    /**
     * Test send string.
     */
    @Test
    public void testSendString() {
        subscribe(destination, this::stringHandler, String.class);
        sendSubscribeOperation.sendAsync(destination, stringMessage);
    }

    /**
     * Test send user.
     */
    @Test
    public void testSendUser() {
        subscribe(destination, this::userHandler, User.class);
        sendSubscribeOperation.sendAsync(destination, userMessage);
    }

    /**
     *
     * @param times The times.
     * @deprecated This is deprecated.
     */
    @Deprecated
    protected abstract void verifyCheckpointBatchSuccessCalled(int times);

    /**
     *
     * @param checkpointer The check pointer.
     */
    protected void verifyCheckpointFailure(Checkpointer checkpointer) {
        checkpointer.failure();
        verifyCheckpointFailureCalled(1);
    }

    /**
     *
     * @param times The times.
     */
    protected abstract void verifyCheckpointFailureCalled(int times);

    /**
     *
     * @param checkpointer The check pointer.
     */
    protected void verifyCheckpointSuccess(Checkpointer checkpointer) {
        checkpointer.success();
        verifyCheckpointSuccessCalled(1);
    }

    /**
     *
     * @param times The times.
     */
    protected abstract void verifyCheckpointSuccessCalled(int times);

    private void byteHandler(Message<?> message) {
        assertEquals(payload, new String((byte[]) message.getPayload(), StandardCharsets.UTF_8));
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

    private void userHandler(Message<?> message) {
        assertEquals(user, message.getPayload());
    }

    /**
     *
     * @return The sendSubscribeOperation.
     */
    public T getSendSubscribeOperation() {
        return sendSubscribeOperation;
    }

    /**
     *
     * @param sendSubscribeOperation The sendSubscribeOperation.
     */
    public void setSendSubscribeOperation(T sendSubscribeOperation) {
        this.sendSubscribeOperation = sendSubscribeOperation;
    }

    /**
     *
     * @return The partitionId.
     */
    public String getPartitionId() {
        return partitionId;
    }

    /**
     *
     * @param partitionId The partitionId.
     */
    public void setPartitionId(String partitionId) {
        this.partitionId = partitionId;
    }
}
