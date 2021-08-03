// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.core.api.CheckpointMode;
import com.azure.spring.integration.core.api.PartitionSupplier;
import com.azure.spring.integration.core.api.SendOperation;
import com.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.azure.spring.integration.servicebus.factory.ServiceBusSenderFactory;
import com.azure.spring.integration.servicebus.health.Instrumentation;
import com.azure.spring.integration.servicebus.health.InstrumentationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.azure.spring.integration.core.api.CheckpointMode.MANUAL;
import static com.azure.spring.integration.core.api.CheckpointMode.RECORD;

/**
 * Azure Service Bus template to support send {@link Message} asynchronously.
 *
 * @author Warren Zhu
 * @author Eduardo Sciullo
 */
public class ServiceBusTemplate<T extends ServiceBusSenderFactory> implements SendOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusTemplate.class);
    private static final CheckpointConfig CHECKPOINT_RECORD = CheckpointConfig.builder().checkpointMode(RECORD).build();
    private static final ServiceBusMessageConverter DEFAULT_CONVERTER = new ServiceBusMessageConverter();
    protected InstrumentationManager instrumentationManager = new InstrumentationManager();
    protected final T clientFactory;
    protected CheckpointConfig checkpointConfig = CHECKPOINT_RECORD;
    protected ServiceBusClientConfig clientConfig = ServiceBusClientConfig.builder().build();
    protected ServiceBusMessageConverter messageConverter;

    public ServiceBusTemplate(@NonNull T senderFactory) {
        this(senderFactory, DEFAULT_CONVERTER);
    }

    public ServiceBusTemplate(@NonNull T senderFactory, @NonNull ServiceBusMessageConverter messageConverter) {
        this.clientFactory = senderFactory;
        this.messageConverter = messageConverter;
        LOGGER.info("Started ServiceBusTemplate with properties: {}", checkpointConfig);
    }

    @Override
    public <U> CompletableFuture<Void> sendAsync(String destination,
                                                 Message<U> message,
                                                 PartitionSupplier partitionSupplier) {
        Assert.hasText(destination, "destination can't be null or empty");
        ServiceBusSenderAsyncClient senderAsyncClient = null;
        ServiceBusMessage serviceBusMessage = messageConverter.fromMessage(message, ServiceBusMessage.class);

        if (Objects.nonNull(serviceBusMessage) && !StringUtils.hasText(serviceBusMessage.getPartitionKey())) {
            String partitionKey = getPartitionKey(partitionSupplier);
            serviceBusMessage.setPartitionKey(partitionKey);
        }
        Instrumentation instrumentation = new Instrumentation(destination, Instrumentation.Type.SUPPLY);
        try {
            instrumentationManager.addHealthInstrumentation(instrumentation);
            senderAsyncClient = this.clientFactory.getOrCreateSender(destination);
            instrumentationManager.getHealthInstrumentation(instrumentation).markStartedSuccessfully();
        } catch (Exception e) {
            instrumentationManager.getHealthInstrumentation(instrumentation).markStartFailed(e);
            LOGGER.error("ServiceBus senderAsyncClient startup failed, Caused by " + e.getMessage());
            throw new ServiceBusRuntimeException("ServiceBus send client startup failed, Caused by " + e.getMessage(), e);
        }

        return senderAsyncClient.sendMessage(serviceBusMessage).toFuture();
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
