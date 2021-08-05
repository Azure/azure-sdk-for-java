// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.stream.binder.test;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.springframework.cloud.stream.binder.AbstractBinder;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.binder.PartitionCapableBinderTests;
import org.springframework.cloud.stream.binder.ProducerProperties;
import org.springframework.cloud.stream.binder.AbstractTestBinder;
import org.springframework.cloud.stream.binder.Spy;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.cloud.stream.binder.Binding;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.Assert;
import org.springframework.util.MimeTypeUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Test cases are defined in super class
 *
 * @author Warren Zhu
 */
public abstract class AzurePartitionBinderTests<B extends AbstractTestBinder<
        ? extends AbstractBinder<MessageChannel, CP, PP>, CP, PP>,
        CP extends ConsumerProperties, PP extends ProducerProperties>
        extends PartitionCapableBinderTests<B, CP, PP> {

    @BeforeClass
    public static void enableTests() {
    }

    @Override
    protected boolean usesExplicitRouting() {
        return false;
    }

    @Override
    public Spy spyOn(String name) {
        return null;
    }

    @Override
    public void testClean() throws Exception {
        // No-op
    }

    @Override
    public void testPartitionedModuleJava() {
        // Partitioned consumer mode unsupported yet
    }

    @Override
    public void testPartitionedModuleSpEL() {
        // Partitioned consumer mode unsupported
    }

    @Override
    public void testAnonymousGroup() {
        // azure binder not support anonymous group
    }

    // Same logic as super.testSendAndReceiveNoOriginalContentType() except one line commented below
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void testSendAndReceiveNoOriginalContentType() throws Exception {
        Binder binder = getBinder();

        BindingProperties producerBindingProperties = createProducerBindingProperties(createProducerProperties());
        DirectChannel moduleOutputChannel = createBindableChannel("output", producerBindingProperties);
        BindingProperties inputBindingProperties = createConsumerBindingProperties(createConsumerProperties());
        DirectChannel moduleInputChannel = createBindableChannel("input", inputBindingProperties);
        Binding<MessageChannel> producerBinding =
                binder.bindProducer(String.format("bar%s0", getDestinationNameDelimiter()), moduleOutputChannel,
                        producerBindingProperties.getProducer());
        Binding<MessageChannel> consumerBinding =
                binder.bindConsumer(String.format("bar%s0", getDestinationNameDelimiter()),
                        "testSendAndReceiveNoOriginalContentType", moduleInputChannel, createConsumerProperties());
        binderBindUnbindLatency();

        Message<?> message =
                MessageBuilder.withPayload("foo").setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN)
                              .build();

        // Comment line below since service bus topic operation is event driven mode
        // but subscriber is not ready in the downstream
        //moduleOutputChannel.send(message);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Message<byte[]>> inboundMessageRef = new AtomicReference<>();
        moduleInputChannel.subscribe(message1 -> {
            try {
                inboundMessageRef.set((Message<byte[]>) message1);
            } finally {
                latch.countDown();
            }
        });

        moduleOutputChannel.send(message);
        Assert.isTrue(latch.await(5, TimeUnit.SECONDS), "Failed to receive message");
        Assertions.assertThat(inboundMessageRef.get()).isNotNull();
        Assertions.assertThat(inboundMessageRef.get().getPayload()).isEqualTo("foo".getBytes(StandardCharsets.UTF_8));
        Assertions.assertThat(inboundMessageRef.get().getHeaders().get(MessageHeaders.CONTENT_TYPE).toString())
            .isEqualTo(MimeTypeUtils.TEXT_PLAIN_VALUE);
        producerBinding.unbind();
        consumerBinding.unbind();
    }
}
