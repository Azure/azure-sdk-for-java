// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus;

import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusProducerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.integration.handler.DefaultMessageHandler;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.core.SendOperation;
import com.azure.spring.servicebus.support.ServiceBusClientConfig;
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

    protected T bindingProperties;

    private static final DefaultErrorMessageStrategy DEFAULT_ERROR_MESSAGE_STRATEGY = new DefaultErrorMessageStrategy();

    protected static final String EXCEPTION_MESSAGE = "exception-message";

    public ServiceBusMessageChannelBinder(String[] headersToEmbed, ServiceBusChannelProvisioner provisioningProvider) {
        super(headersToEmbed, provisioningProvider);
    }

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

    @Override
    public ServiceBusConsumerProperties getExtendedConsumerProperties(String channelName) {
        return this.bindingProperties.getExtendedConsumerProperties(channelName);
    }

    @Override
    public ServiceBusProducerProperties getExtendedProducerProperties(String channelName) {
        return this.bindingProperties.getExtendedProducerProperties(channelName);
    }

    @Override
    public String getDefaultsPrefix() {
        return this.bindingProperties.getDefaultsPrefix();
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return this.bindingProperties.getExtendedPropertiesEntryClass();
    }

    @Override
    protected ErrorMessageStrategy getErrorMessageStrategy() {
        return DEFAULT_ERROR_MESSAGE_STRATEGY;
    }

    public void setBindingProperties(T bindingProperties) {
        this.bindingProperties = bindingProperties;
    }

    protected CheckpointConfig buildCheckpointConfig(
        ExtendedConsumerProperties<ServiceBusConsumerProperties> properties) {

        return CheckpointConfig.builder()
                               .checkpointMode(properties.getExtension().getCheckpointMode())
                               .build();
    }

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

    abstract SendOperation getSendOperation();

}
