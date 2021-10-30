// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.checkpoint.CheckpointMode;
import com.azure.spring.messaging.PartitionSupplier;
import com.azure.spring.messaging.core.SendOperation;
import com.azure.spring.servicebus.core.sender.ServiceBusSenderClientFactory;
import com.azure.spring.servicebus.support.ServiceBusClientConfig;
import com.azure.spring.servicebus.support.ServiceBusRuntimeException;
import com.azure.spring.servicebus.support.converter.ServiceBusMessageConverter;
import com.azure.spring.servicebus.health.Instrumentation;
import com.azure.spring.servicebus.health.InstrumentationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static com.azure.spring.messaging.checkpoint.CheckpointMode.MANUAL;
import static com.azure.spring.messaging.checkpoint.CheckpointMode.RECORD;

/**
 * Azure Service Bus template to support send {@link Message} asynchronously.
 *
 * @author Warren Zhu
 * @author Eduardo Sciullo
 */
public class ServiceBusTemplate<T extends ServiceBusSenderClientFactory, P> implements SendOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusTemplate.class);
    private static final CheckpointConfig CHECKPOINT_RECORD = CheckpointConfig.builder().checkpointMode(RECORD).build();
    private static final ServiceBusMessageConverter DEFAULT_CONVERTER = new ServiceBusMessageConverter();
    protected InstrumentationManager instrumentationManager = new InstrumentationManager();
    protected final T senderClientFactory;
    protected final P processorClientFactory;
    protected CheckpointConfig checkpointConfig = CHECKPOINT_RECORD;
    protected ServiceBusMessageConverter messageConverter;

    public ServiceBusTemplate(@NonNull T senderFactory, @NonNull P processorFactory) {
        this(senderFactory, processorFactory, DEFAULT_CONVERTER);
    }

    public ServiceBusTemplate(@NonNull T senderFactory, @NonNull P processorFactory, @NonNull ServiceBusMessageConverter messageConverter) {
        this.senderClientFactory = senderFactory;
        this.processorClientFactory = processorFactory;
        this.messageConverter = messageConverter;
        LOGGER.info("Started ServiceBusTemplate with properties: {}", checkpointConfig);
    }

    @Override
    public <U> Mono<Void> sendAsync(String destination,
                                    Message<U> message,
                                    PartitionSupplier partitionSupplier) {
        Assert.hasText(destination, "destination can't be null or empty");
        ServiceBusSenderAsyncClient senderAsyncClient = null;
        ServiceBusMessage serviceBusMessage = messageConverter.fromMessage(message, ServiceBusMessage.class);

        if (Objects.nonNull(serviceBusMessage) && !StringUtils.hasText(serviceBusMessage.getPartitionKey())) {
            String partitionKey = getPartitionKey(partitionSupplier);
            serviceBusMessage.setPartitionKey(partitionKey);
        }
        Instrumentation instrumentation = new Instrumentation(destination, Instrumentation.Type.PRODUCE);
        try {
            instrumentationManager.addHealthInstrumentation(instrumentation);
            senderAsyncClient = this.senderClientFactory.createSender(destination);
            instrumentationManager.getHealthInstrumentation(instrumentation).markStartedSuccessfully();
        } catch (Exception e) {
            instrumentationManager.getHealthInstrumentation(instrumentation).markStartFailed(e);
            LOGGER.error("ServiceBus senderAsyncClient startup failed, Caused by " + e.getMessage());
            throw new ServiceBusRuntimeException("ServiceBus send client startup failed, Caused by " + e.getMessage(), e);
        }

        return senderAsyncClient.sendMessage(serviceBusMessage);
    }

    public InstrumentationManager getInstrumentationManager() {
        return instrumentationManager;
    }

    public CheckpointConfig getCheckpointConfig() {
        return checkpointConfig;
    }

    public void setCheckpointConfig(CheckpointConfig checkpointConfig) {
        if (checkpointConfig == null) {
            return;
        }
        Assert.state(isValidCheckpointMode(checkpointConfig.getCheckpointMode()),
            "Only MANUAL or RECORD checkpoint " + "mode is supported " + "in " + "ServiceBusTemplate");
        this.checkpointConfig = checkpointConfig;
        LOGGER.info("ServiceBusTemplate checkpoint config becomes: {}", this.checkpointConfig);
    }

    public ServiceBusMessageConverter getMessageConverter() {
        return messageConverter;
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

    private static boolean isValidCheckpointMode(CheckpointMode mode) {
        return mode == MANUAL || mode == RECORD;
    }

}
