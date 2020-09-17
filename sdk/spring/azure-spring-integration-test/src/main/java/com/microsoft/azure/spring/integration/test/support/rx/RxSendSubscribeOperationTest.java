// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.test.support.rx;

import com.microsoft.azure.spring.integration.core.api.CheckpointConfig;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.core.api.RxSendOperation;
import com.microsoft.azure.spring.integration.test.support.pojo.User;
import org.junit.Before;
import org.junit.Test;
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

    protected T sendSubscribeOperation;
    protected String partitionId = "1";
    protected String destination = "test";
    private String payload = "payload";
    private User user = new User(payload);
    private Map<String, Object> headers = new HashMap<>();
    protected Message<User> userMessage = new GenericMessage<>(user, headers);

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

    @SuppressWarnings("unchecked")
    protected Message<User>[] messages =
        IntStream.range(1, 5).mapToObj(String::valueOf).map(User::new).map(u -> new GenericMessage<>(u, headers))
            .toArray(Message[]::new);
    private Message<String> stringMessage = new GenericMessage<>(payload, headers);
    private Message<byte[]> byteMessage = new GenericMessage<>(payload.getBytes(StandardCharsets.UTF_8), headers);

    @Test
    public void testSendString() {
        AssertableSubscriber<String> subscriber =
            subscribe(destination, String.class).map(Message::getPayload).cast(String.class).test();
        sendSubscribeOperation.sendRx(destination, stringMessage);
        subscriber.assertValue(payload).assertNoErrors();
    }

    @Test
    public void testSendByte() {
        AssertableSubscriber<String> subscriber =
            subscribe(destination, byte[].class).map(Message::getPayload).cast(byte[].class).map(String::new)
                .test();
        sendSubscribeOperation.sendRx(destination, byteMessage);
        subscriber.assertValue(payload).assertNoErrors();
    }

    @Test
    public void testSendUser() {
        AssertableSubscriber<User> subscriber =
            subscribe(destination, User.class).map(Message::getPayload).cast(User.class).test();
        sendSubscribeOperation.sendRx(destination, userMessage);
        subscriber.assertValue(user).assertNoErrors();
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
        AssertableSubscriber<User> subscriber =
            subscribe(destination, User.class).map(Message::getPayload).cast(User.class).test();
        Arrays.stream(messages).forEach(m -> sendSubscribeOperation.sendRx(destination, m));
        subscriber.assertValueCount(messages.length).assertNoErrors();
        verifyCheckpointSuccessCalled(messages.length);
    }

    @Before
    public abstract void setUp();

    protected abstract void verifyCheckpointSuccessCalled(int times);

    protected abstract void verifyCheckpointBatchSuccessCalled(int times);

    protected abstract Observable<Message<?>> subscribe(String destination, Class<?> payloadType);

    protected abstract void setCheckpointConfig(CheckpointConfig checkpointConfig);
}
