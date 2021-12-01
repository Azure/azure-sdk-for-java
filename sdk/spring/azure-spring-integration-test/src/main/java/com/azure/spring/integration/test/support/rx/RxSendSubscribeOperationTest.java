// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.test.support.rx;

import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.core.api.CheckpointMode;
import com.azure.spring.integration.core.api.RxSendOperation;
import com.azure.spring.integration.test.support.pojo.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import rx.Observable;
import rx.observers.AssertableSubscriber;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 *
 * @param <T> The type that extends RxSendOperation.
 */
public abstract class RxSendSubscribeOperationTest<T extends RxSendOperation> {

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
     * The messages.
     */
    @SuppressWarnings("unchecked")
    protected Message<User>[] messages = IntStream.range(1, 5)
                                                  .mapToObj(String::valueOf)
                                                  .map(User::new)
                                                  .map(u -> new GenericMessage<>(u, headers))
                                                  .toArray(Message[]::new);
    private String payload = "payload";
    private Message<byte[]> byteMessage = new GenericMessage<>(payload.getBytes(StandardCharsets.UTF_8), headers);
    private Message<String> stringMessage = new GenericMessage<>(payload, headers);
    private User user = new User(payload);

    /**
     * The userMessage.
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
     * @param payloadType The payload type.
     * @return The Observable.
     */
    protected abstract Observable<Message<?>> subscribe(String destination, Class<?> payloadType);

    /**
     * Test send byte.
     */
    @Test
    public void testSendByte() {
        AssertableSubscriber<String> subscriber = subscribe(destination, byte[].class).map(Message::getPayload)
                                                                                      .cast(byte[].class)
                                                                                      .map(String::new)
                                                                                      .test();
        sendSubscribeOperation.sendRx(destination, byteMessage);
        subscriber.assertValue(payload).assertNoErrors();
    }

    /**
     * Test send receive with manual checkpoint mode.
     */
    @Test
    public void testSendReceiveWithManualCheckpointMode() {
        setCheckpointConfig(CheckpointConfig.builder().checkpointMode(CheckpointMode.MANUAL).build());

        Observable<Message<?>> observable = subscribe(destination, User.class);
        AssertableSubscriber<User> userSubscriber = observable.map(Message::getPayload).cast(User.class).test();
        sendSubscribeOperation.sendRx(destination, userMessage);
        userSubscriber.assertValue(user).assertNoErrors();
        verifyCheckpointSuccessCalled(0);
    }

    /**
     * Test send receive with record checkpoint mode.
     */
    @Test
    public void testSendReceiveWithRecordCheckpointMode() {
        setCheckpointConfig(CheckpointConfig.builder().checkpointMode(CheckpointMode.RECORD).build());
        AssertableSubscriber<User> subscriber = subscribe(destination, User.class).map(Message::getPayload)
                                                                                  .cast(User.class)
                                                                                  .test();
        Arrays.stream(messages).forEach(m -> sendSubscribeOperation.sendRx(destination, m));
        subscriber.assertValueCount(messages.length).assertNoErrors();
        verifyCheckpointSuccessCalled(messages.length);
    }

    /**
     * Test send string.
     */
    @Test
    public void testSendString() {
        AssertableSubscriber<String> subscriber = subscribe(destination, String.class).map(Message::getPayload)
                                                                                      .cast(String.class)
                                                                                      .test();
        sendSubscribeOperation.sendRx(destination, stringMessage);
        subscriber.assertValue(payload).assertNoErrors();
    }

    /**
     * Test send user.
     */
    @Test
    public void testSendUser() {
        AssertableSubscriber<User> subscriber = subscribe(destination, User.class).map(Message::getPayload)
                                                                                  .cast(User.class)
                                                                                  .test();
        sendSubscribeOperation.sendRx(destination, userMessage);
        subscriber.assertValue(user).assertNoErrors();
    }

    /**
     *
     * @param times The times.
     */
    protected abstract void verifyCheckpointBatchSuccessCalled(int times);

    /**
     *
     * @param times The times.
     */
    protected abstract void verifyCheckpointSuccessCalled(int times);

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
}
