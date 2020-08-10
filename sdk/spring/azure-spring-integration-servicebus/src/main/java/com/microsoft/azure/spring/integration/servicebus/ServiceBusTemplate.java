// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.servicebus;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.MessageHandlerOptions;
import com.microsoft.azure.servicebus.SessionHandlerOptions;
import com.microsoft.azure.spring.integration.core.api.CheckpointConfig;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.core.api.PartitionSupplier;
import com.microsoft.azure.spring.integration.core.api.SendOperation;
import com.microsoft.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusSenderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Azure service bus template to support send {@link Message} asynchronously
 *
 * @author Warren Zhu
 * @author Eduardo Sciullo
 */
public class ServiceBusTemplate<T extends ServiceBusSenderFactory> implements SendOperation {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceBusTemplate.class);

    protected final T senderFactory;

    protected ServiceBusClientConfig clientConfig = ServiceBusClientConfig.builder().build();

    protected CheckpointConfig checkpointConfig = CheckpointConfig.builder()
        .checkpointMode(CheckpointMode.RECORD).build();

    protected ServiceBusMessageConverter messageConverter = new ServiceBusMessageConverter();

    public ServiceBusTemplate(@NonNull T senderFactory) {
        this.senderFactory = senderFactory;
        LOG.info("Started ServiceBusTemplate with properties: {}", checkpointConfig);
    }

    private static boolean isValidCheckpointConfig(CheckpointConfig checkpointConfig) {
        return checkpointConfig.getCheckpointMode() == CheckpointMode.MANUAL
            || checkpointConfig.getCheckpointMode() == CheckpointMode.RECORD;
    }

    @Override
    public <U> CompletableFuture<Void> sendAsync(String destination, @NonNull Message<U> message,
                                                 PartitionSupplier partitionSupplier) {
        Assert.hasText(destination, "destination can't be null or empty");
        String partitionKey = getPartitionKey(partitionSupplier);
        IMessage serviceBusMessage = messageConverter.fromMessage(message, IMessage.class);

        if (StringUtils.hasText(partitionKey)) {
            serviceBusMessage.setPartitionKey(partitionKey);
        }

        return this.senderFactory.getOrCreateSender(destination).sendAsync(serviceBusMessage);
    }

    protected MessageHandlerOptions buildHandlerOptions() {
        return new MessageHandlerOptions(this.clientConfig.getConcurrency(), false, Duration.ofMinutes(5));
    }

    protected SessionHandlerOptions buildSessionHandlerOptions() {
        return new SessionHandlerOptions(this.clientConfig.getConcurrency(), false, Duration.ofMinutes(5));
    }

    protected ExecutorService buildHandlerExecutors(String threadPrefix) {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(threadPrefix + "-%d").build();
        return Executors.newFixedThreadPool(this.clientConfig.getConcurrency(), threadFactory);
    }

    private String getPartitionKey(PartitionSupplier partitionSupplier) {
        if (partitionSupplier == null) {
            return "";
        }

        if (StringUtils.hasText(partitionSupplier.getPartitionKey())) {
            return partitionSupplier.getPartitionKey();
        }

        if (StringUtils.hasText(partitionSupplier.getPartitionId())) {
            return partitionSupplier.getPartitionId();
        }

        return "";
    }

    public CheckpointConfig getCheckpointConfig() {
        return checkpointConfig;
    }

    public void setCheckpointConfig(CheckpointConfig checkpointConfig) {
        Assert.state(isValidCheckpointConfig(checkpointConfig),
            "Only MANUAL or RECORD checkpoint mode is supported in ServiceBusTemplate");
        this.checkpointConfig = checkpointConfig;
        LOG.info("ServiceBusTemplate checkpoint config becomes: {}", this.checkpointConfig);
    }

    public ServiceBusMessageConverter getMessageConverter() {
        return messageConverter;
    }
}
