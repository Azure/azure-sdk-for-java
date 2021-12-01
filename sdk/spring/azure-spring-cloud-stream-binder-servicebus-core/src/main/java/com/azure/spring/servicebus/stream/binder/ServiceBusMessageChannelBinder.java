// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.stream.binder;

import com.azure.spring.integration.core.DefaultMessageHandler;
import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.core.api.SendOperation;
import com.azure.spring.integration.servicebus.ServiceBusClientConfig;
import com.azure.spring.servicebus.stream.binder.properties.ServiceBusConsumerProperties;
import com.azure.spring.servicebus.stream.binder.properties.ServiceBusExtendedBindingProperties;
import com.azure.spring.servicebus.stream.binder.properties.ServiceBusProducerProperties;
import com.azure.spring.servicebus.stream.binder.provisioning.ServiceBusChannelProvisioner;
import org.springframework.cloud.stream.binder.AbstractMessageChannelBinder;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedPropertiesBinder;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.expression.FunctionExpression;
import org.springframework.integration.support.DefaultErrorMessageStrategy;
import org.springframework.integration.support.ErrorMessageStrategy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.util.Optional;

/**
 * @author Warren Zhu
 * @author Eduardo Sciullo
 */
public abstract class ServiceBusMessageChannelBinder<T extends ServiceBusExtendedBindingProperties> extends
    AbstractMessageChannelBinder<ExtendedConsumerProperties<ServiceBusConsumerProperties>,
        ExtendedProducerProperties<ServiceBusProducerProperties>,
        ServiceBusChannelProvisioner>
    implements
    ExtendedPropertiesBinder<MessageChannel, ServiceBusConsumerProperties, ServiceBusProducerProperties> {

    /**
     * Binding properties.
     */
    protected T bindingProperties;

    private static final DefaultErrorMessageStrategy DEFAULT_ERROR_MESSAGE_STRATEGY = new DefaultErrorMessageStrategy();

    /**
     * Exception message.
     */
    protected static final String EXCEPTION_MESSAGE = "exception-message";

    /**
     *
     * @param headersToEmbed Headers to embed.
     * @param provisioningProvider Provisioning provider.
     */
    public ServiceBusMessageChannelBinder(String[] headersToEmbed, ServiceBusChannelProvisioner provisioningProvider) {
        super(headersToEmbed, provisioningProvider);
    }

    /**
     *
     * @param destination The destination.
     * @param producerProperties The producer properties.
     * @param errorChannel The error channel.
     * @return The message handler.
     */
    @Override
    protected MessageHandler createProducerMessageHandler(
        ProducerDestination destination,
        ExtendedProducerProperties<ServiceBusProducerProperties> producerProperties,
        MessageChannel errorChannel) {

        DefaultMessageHandler handler = new DefaultMessageHandler(destination.getName(), getSendOperation());
        handler.setBeanFactory(getBeanFactory());
        handler.setSync(producerProperties.getExtension().isSync());
        handler.setSendTimeout(producerProperties.getExtension().getSendTimeout());
        handler.setSendFailureChannel(errorChannel);
        if (producerProperties.isPartitioned()) {
            handler.setPartitionKeyExpressionString(
                "'partitionKey-' + headers['" + BinderHeaders.PARTITION_HEADER + "']");
        } else {
            handler.setPartitionKeyExpression(new FunctionExpression<Message<?>>(m -> m.getPayload().hashCode()));
        }

        return handler;
    }

    /**
     *
     * @param channelName The channel name.
     * @return The ServiceBusConsumerProperties.
     */
    @Override
    public ServiceBusConsumerProperties getExtendedConsumerProperties(String channelName) {
        return this.bindingProperties.getExtendedConsumerProperties(channelName);
    }

    /**
     *
     * @param channelName The channel name.
     * @return The ServiceBusProducerProperties.
     */
    @Override
    public ServiceBusProducerProperties getExtendedProducerProperties(String channelName) {
        return this.bindingProperties.getExtendedProducerProperties(channelName);
    }

    /**
     *
     * @return The defaults prefix.
     */
    @Override
    public String getDefaultsPrefix() {
        return this.bindingProperties.getDefaultsPrefix();
    }

    /**
     *
     * @return The extended properties entry class
     */
    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return this.bindingProperties.getExtendedPropertiesEntryClass();
    }

    /**
     *
     * @return The ErrorMessageStrategy.
     */
    @Override
    protected ErrorMessageStrategy getErrorMessageStrategy() {
        return DEFAULT_ERROR_MESSAGE_STRATEGY;
    }

    /**
     *
     * @param bindingProperties The binding properties.
     */
    public void setBindingProperties(T bindingProperties) {
        this.bindingProperties = bindingProperties;
    }

    /**
     *
     * @param properties The ExtendedConsumerProperties.
     * @return The CheckpointConfig.
     */
    protected CheckpointConfig buildCheckpointConfig(
        ExtendedConsumerProperties<ServiceBusConsumerProperties> properties) {

        return CheckpointConfig.builder()
                               .checkpointMode(properties.getExtension().getCheckpointMode())
                               .build();
    }

    /**
     *
     * @param properties The ExtendedConsumerProperties.
     * @return The ServiceBusClientConfig.
     */
    protected ServiceBusClientConfig buildClientConfig(
        ExtendedConsumerProperties<ServiceBusConsumerProperties> properties) {

        ServiceBusConsumerProperties consumerProperties = properties.getExtension();
        return ServiceBusClientConfig.builder()
                                     .setPrefetchCount(consumerProperties.getPrefetchCount())
                                     .setConcurrency(consumerProperties.getConcurrency())
                                     .setSessionsEnabled(consumerProperties.isSessionsEnabled())
                                     // When session disabled, if user don't set maxConcurrentCalls, we should use concurrency
                                     .setMaxConcurrentCalls(Optional.ofNullable(consumerProperties.getMaxConcurrentCalls())
                                                                    .orElse(consumerProperties.isSessionsEnabled()
                                                                        ? 1 : consumerProperties.getConcurrency()))
                                    // When session enabled, if user don't set maxConcurrentSessions, we should use concurrency
                                    .setMaxConcurrentSessions(Optional.ofNullable(consumerProperties.getMaxConcurrentSessions())
                                                                      .orElse(consumerProperties.isSessionsEnabled()
                                                                          ? consumerProperties.getConcurrency() : 1))
                                     .setServiceBusReceiveMode(consumerProperties.getServiceBusReceiveMode())
                                     .setEnableAutoComplete(consumerProperties.isEnableAutoComplete())
                                     .build();
    }

    /**
     *
     * @return The SendOperation.
     */
    abstract SendOperation getSendOperation();

}
