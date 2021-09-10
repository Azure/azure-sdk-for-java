// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.core.rx;

import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.checkpoint.CheckpointMode;
import com.azure.spring.messaging.core.RxSendOperation;
import com.azure.spring.messaging.support.pojo.User;
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

public abstract class RxSendSubscribeOperationTest<T extends RxSendOperation> {

    protected String destination = "test";
    protected String partitionId = "1";
    protected T sendSubscribeOperation;
    private Map<String, Object> headers = new HashMap<>();
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
    protected Message<User> userMessage = new GenericMessage<>(user, headers);

    protected abstract void setCheckpointConfig(CheckpointConfig checkpointConfig);

    @BeforeEach
    public abstract void setUp();

    protected abstract Observable<Message<?>> subscribe(String destination, Class<?> payloadType);

    @Test
    public void testSendByte() {
        AssertableSubscriber<String> subscriber = subscribe(destination, byte[].class).map(Message::getPayload)
                                                                                      .cast(byte[].class)
                                                                                      .map(String::new)
                                                                                      .test();
        sendSubscribeOperation.sendRx(destination, byteMessage);
        subscriber.assertValue(payload).assertNoErrors();
    }

    @Test
    public void testSendReceiveWithManualCheckpointMode() {
        setCheckpointConfig(CheckpointConfig.builder().checkpointMode(CheckpointMode.MANUAL).build());

        Observable<Message<?>> observable = subscribe(destination, User.class);
        AssertableSubscriber<User> userSubscriber = observable.map(Message::getPayload).cast(User.class).test();
        sendSubscribeOperation.sendRx(destination, userMessage);
        userSubscriber.assertValue(user).assertNoErrors();
        verifyCheckpointSuccessCalled(0);
    }

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

    @Test
    public void testSendString() {
        AssertableSubscriber<String> subscriber = subscribe(destination, String.class).map(Message::getPayload)
                                                                                      .cast(String.class)
                                                                                      .test();
        sendSubscribeOperation.sendRx(destination, stringMessage);
        subscriber.assertValue(payload).assertNoErrors();
    }

    @Test
    public void testSendUser() {
        AssertableSubscriber<User> subscriber = subscribe(destination, User.class).map(Message::getPayload)
                                                                                  .cast(User.class)
                                                                                  .test();
        sendSubscribeOperation.sendRx(destination, userMessage);
        subscriber.assertValue(user).assertNoErrors();
    }

    protected abstract void verifyCheckpointBatchSuccessCalled(int times);

    protected abstract void verifyCheckpointSuccessCalled(int times);

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
}
