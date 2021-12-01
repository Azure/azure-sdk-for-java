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

/**
 *
 * @param <T> The type that extends SendOperation.
 */
public abstract class SendSubscribeOperationTest<T extends SendOperation> {

    /**
     * Send subscribe operation.
     */
    protected T sendSubscribeOperation;

    /**
     * partition id.
     */
    protected String partitionId = "1";

    /**
     * The destination.
     */
    protected String destination = "test";

    /**
     * The payload.
     */
    protected String payload = "payload";

    /**
     * The user.
     */
    protected User user = new User(payload);

    /**
     * The headers.
     */
    protected Map<String, Object> headers = new HashMap<>();

    /**
     * The user message.
     */
    protected Message<User> userMessage = new GenericMessage<>(user, headers);

    /**
     * The message list.
     */
    protected List<Message<User>> messages = IntStream.range(1, 5)
                                                      .mapToObj(String::valueOf)
                                                      .map(User::new)
                                                      .map(u -> new GenericMessage<>(u, headers))
                                                      .collect(Collectors.toList());
    private final Message<String> stringMessage = new GenericMessage<>(payload, headers);
    private final Message<byte[]> byteMessage = new GenericMessage<>(payload.getBytes(StandardCharsets.UTF_8), headers);

    /**
     * Set up.
     * @throws Exception The exception.
     */
    @BeforeEach
    public abstract void setUp() throws Exception;

    /**
     * Test send string.
     */
    @Test
    public void testSendString() {
        subscribe(destination, this::stringHandler, String.class);
        sendSubscribeOperation.sendAsync(destination, stringMessage);
    }

    /**
     * Test send byte.
     */
    @Test
    public void testSendByte() {
        subscribe(destination, this::byteHandler, byte[].class);
        sendSubscribeOperation.sendAsync(destination, byteMessage);
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
     *
     * @param message The message.
     */
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

    /**
     *
     * @param times The times.
     */
    protected abstract void verifyCheckpointSuccessCalled(int times);

    /**
     *
     * @param times The times.
     */
    protected abstract void verifyCheckpointBatchSuccessCalled(int times);

    /**
     *
     * @param times The times.
     */
    protected abstract void verifyCheckpointFailureCalled(int times);

    /**
     *
     * @param destination The destination.
     * @param consumer The consumer.
     * @param payloadType The payloadType.
     */
    protected abstract void subscribe(String destination, Consumer<Message<?>> consumer, Class<?> payloadType);

    /**
     *
     * @param checkpointConfig The checkpointConfig.
     */
    protected abstract void setCheckpointConfig(CheckpointConfig checkpointConfig);

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
     * @param checkpointer The check pointer.
     */
    protected void verifyCheckpointFailure(Checkpointer checkpointer) {
        checkpointer.failure();
        verifyCheckpointFailureCalled(1);
    }

    /**
     *
     * @return The partition id.
     */
    public String getPartitionId() {
        return partitionId;
    }

    /**
     *
     * @param partitionId The partition id.
     */
    public void setPartitionId(String partitionId) {
        this.partitionId = partitionId;
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
     * @param millis The millis.
     */
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
