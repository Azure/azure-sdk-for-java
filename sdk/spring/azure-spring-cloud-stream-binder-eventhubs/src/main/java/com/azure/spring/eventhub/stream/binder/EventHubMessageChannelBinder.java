// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhub.stream.binder;

import com.azure.spring.eventhub.stream.binder.properties.EventHubConsumerProperties;
import com.azure.spring.eventhub.stream.binder.properties.EventHubExtendedBindingProperties;
import com.azure.spring.eventhub.stream.binder.properties.EventHubProducerProperties;
import com.azure.spring.eventhub.stream.binder.provisioning.EventHubChannelProvisioner;
import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.core.api.StartPosition;
import com.azure.spring.integration.core.api.reactor.DefaultMessageHandler;
import com.azure.spring.integration.eventhub.EventHubClientConfig;
import com.azure.spring.integration.eventhub.api.EventHubOperation;
import com.azure.spring.integration.eventhub.inbound.EventHubInboundChannelAdapter;
import org.springframework.cloud.stream.binder.AbstractMessageChannelBinder;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedPropertiesBinder;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.expression.FunctionExpression;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Warren Zhu
 */
public class EventHubMessageChannelBinder extends
    AbstractMessageChannelBinder<ExtendedConsumerProperties<EventHubConsumerProperties>,
        ExtendedProducerProperties<EventHubProducerProperties>, EventHubChannelProvisioner>
    implements ExtendedPropertiesBinder<MessageChannel, EventHubConsumerProperties, EventHubProducerProperties> {

    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    private final EventHubOperation eventHubOperation;

    private EventHubExtendedBindingProperties bindingProperties = new EventHubExtendedBindingProperties();

    private final Map<String, EventHubInformation> eventHubsInUse = new ConcurrentHashMap<>();

    public EventHubMessageChannelBinder(String[] headersToEmbed, EventHubChannelProvisioner provisioningProvider,
                                        EventHubOperation eventHubOperation) {
        super(headersToEmbed, provisioningProvider);
        this.eventHubOperation = eventHubOperation;
    }

    @Override
    protected MessageHandler createProducerMessageHandler(ProducerDestination destination,
            ExtendedProducerProperties<EventHubProducerProperties> producerProperties, MessageChannel errorChannel) {
        eventHubsInUse.put(destination.getName(), new EventHubInformation(null));

        EventHubClientConfig.EventHubClientConfigBuilder builder = EventHubClientConfig.eventHubClientConifgBuilder();
        builder.setCustomEndpointAddress(producerProperties.getExtension().getCustomEndpointAddress());
        builder.setPrefetchCount(producerProperties.getExtension().getPrefetchCount());
        builder.setRetryOptions(producerProperties.getExtension().getRetryOptions());
        builder.setShareConnection(producerProperties.getExtension().isShareConnection());
        builder.setTransport(producerProperties.getExtension().getTransport());

        this.eventHubOperation.setClientConfig(builder.build());
        DefaultMessageHandler handler = new DefaultMessageHandler(destination.getName(), this.eventHubOperation);
        handler.setBeanFactory(getBeanFactory());
        handler.setSync(producerProperties.getExtension().isSync());
        handler.setSendTimeout(producerProperties.getExtension().getSendTimeout());
        handler.setSendFailureChannel(errorChannel);
        if (producerProperties.isPartitioned()) {
            handler.setPartitionIdExpression(
                EXPRESSION_PARSER.parseExpression("headers['" + BinderHeaders.PARTITION_HEADER + "']"));
        } else {
            handler.setPartitionKeyExpression(new FunctionExpression<Message<?>>(m -> m.getPayload().hashCode()));
        }
        return handler;
    }

    @Override
    protected MessageProducer createConsumerEndpoint(ConsumerDestination destination, String group,
                                                     ExtendedConsumerProperties<EventHubConsumerProperties> properties) {
        eventHubsInUse.put(destination.getName(), new EventHubInformation(group));

        EventHubClientConfig.EventHubClientConfigBuilder builder = EventHubClientConfig.eventHubClientConifgBuilder();
        builder.setCustomEndpointAddress(properties.getExtension().getCustomEndpointAddress());
        builder.setLoadBalancingStrategy(properties.getExtension().getLoadBalancingStrategy());
        builder.setPartitionOwnershipExpirationInterval(properties.getExtension().getPartitionOwnershipExpirationInterval());
        builder.setPrefetchCount(properties.getExtension().getPrefetchCount());
        builder.setRetryOptions(properties.getExtension().getRetryOptions());
        builder.setShareConnection(properties.getExtension().isShareConnection());
        builder.setTrackLastEnqueuedEventProperties(properties.getExtension().isTrackLastEnqueuedEventProperties());
        builder.setTransport(properties.getExtension().getTransport());

        this.eventHubOperation.setClientConfig(builder.build());
        this.eventHubOperation.setStartPosition(properties.getExtension().getStartPosition());
        CheckpointConfig checkpointConfig =
            CheckpointConfig.builder().checkpointMode(properties.getExtension().getCheckpointMode())
                            .checkpointCount(properties.getExtension().getCheckpointCount())
                            .checkpointInterval(properties.getExtension().getCheckpointInterval())
                            .build();
        this.eventHubOperation.setCheckpointConfig(checkpointConfig);

        boolean anonymous = !StringUtils.hasText(group);
        if (anonymous) {
            group = "anonymous." + UUID.randomUUID().toString();
            this.eventHubOperation.setStartPosition(StartPosition.LATEST);
        }
        EventHubInboundChannelAdapter inboundAdapter =
            new EventHubInboundChannelAdapter(destination.getName(), this.eventHubOperation, group);
        inboundAdapter.setBeanFactory(getBeanFactory());
        ErrorInfrastructure errorInfrastructure = registerErrorInfrastructure(destination, group, properties);
        inboundAdapter.setErrorChannel(errorInfrastructure.getErrorChannel());
        return inboundAdapter;
    }


    @Override
    public EventHubConsumerProperties getExtendedConsumerProperties(String destination) {
        return this.bindingProperties.getExtendedConsumerProperties(destination);
    }

    @Override
    public EventHubProducerProperties getExtendedProducerProperties(String destination) {
        return this.bindingProperties.getExtendedProducerProperties(destination);
    }

    @Override
    public String getDefaultsPrefix() {
        return this.bindingProperties.getDefaultsPrefix();
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return this.bindingProperties.getExtendedPropertiesEntryClass();
    }

    public void setBindingProperties(EventHubExtendedBindingProperties bindingProperties) {
        this.bindingProperties = bindingProperties;
    }

    Map<String, EventHubInformation> getEventHubsInUse() {
        return eventHubsInUse;
    }

    static class EventHubInformation {

        private final String consumerGroup;

        EventHubInformation(String consumerGroup) {
            this.consumerGroup = consumerGroup;
        }

        public String getConsumerGroup() {
            return consumerGroup;
        }
    }

}
