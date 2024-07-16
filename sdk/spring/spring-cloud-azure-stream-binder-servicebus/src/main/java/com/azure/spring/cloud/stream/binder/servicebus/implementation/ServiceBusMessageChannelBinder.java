// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.implementation;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils;
import com.azure.spring.cloud.stream.binder.servicebus.config.ServiceBusProcessorFactoryCustomizer;
import com.azure.spring.cloud.stream.binder.servicebus.config.ServiceBusProducerFactoryCustomizer;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusProducerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.implementation.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.integration.core.handler.DefaultMessageHandler;
import com.azure.spring.integration.core.implementation.instrumentation.DefaultInstrumentation;
import com.azure.spring.integration.core.implementation.instrumentation.DefaultInstrumentationManager;
import com.azure.spring.integration.core.implementation.instrumentation.InstrumentationSendCallback;
import com.azure.spring.integration.core.instrumentation.Instrumentation;
import com.azure.spring.integration.core.instrumentation.InstrumentationManager;
import com.azure.spring.integration.servicebus.implementation.health.ServiceBusProcessorInstrumentation;
import com.azure.spring.integration.servicebus.inbound.ServiceBusInboundChannelAdapter;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.messaging.servicebus.core.ServiceBusProcessorFactory;
import com.azure.spring.messaging.servicebus.core.ServiceBusTemplate;
import com.azure.spring.messaging.servicebus.core.listener.ServiceBusMessageListenerContainer;
import com.azure.spring.messaging.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.messaging.servicebus.core.properties.ProducerProperties;
import com.azure.spring.messaging.servicebus.core.properties.ServiceBusContainerProperties;
import com.azure.spring.messaging.servicebus.core.DefaultServiceBusNamespaceProcessorFactory;
import com.azure.spring.messaging.servicebus.core.DefaultServiceBusNamespaceProducerFactory;
import com.azure.spring.messaging.servicebus.implementation.properties.merger.ProcessorPropertiesMerger;
import com.azure.spring.messaging.servicebus.support.ServiceBusMessageHeaders;
import com.azure.spring.messaging.servicebus.implementation.support.converter.ServiceBusMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binder.AbstractMessageChannelBinder;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedPropertiesBinder;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.expression.FunctionExpression;
import org.springframework.integration.support.DefaultErrorMessageStrategy;
import org.springframework.integration.support.ErrorMessageStrategy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.spring.integration.core.instrumentation.Instrumentation.Type.CONSUMER;
import static com.azure.spring.integration.core.instrumentation.Instrumentation.Type.PRODUCER;

/**
 *
 */
public class ServiceBusMessageChannelBinder extends
    AbstractMessageChannelBinder<ExtendedConsumerProperties<ServiceBusConsumerProperties>,
            ExtendedProducerProperties<ServiceBusProducerProperties>,
            ServiceBusChannelProvisioner>
    implements
    ExtendedPropertiesBinder<MessageChannel, ServiceBusConsumerProperties, ServiceBusProducerProperties> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusMessageChannelBinder.class);
    private static final DefaultErrorMessageStrategy DEFAULT_ERROR_MESSAGE_STRATEGY = new DefaultErrorMessageStrategy();
    private static final String EXCEPTION_MESSAGE = "exception-message";

    private ServiceBusExtendedBindingProperties bindingProperties = new ServiceBusExtendedBindingProperties();
    private NamespaceProperties namespaceProperties;
    private ServiceBusTemplate serviceBusTemplate;
    private ServiceBusProcessorFactory processorFactory;
    private AzureMessageConverter<ServiceBusReceivedMessage, ServiceBusMessage> messageConverter = new ServiceBusMessageConverter();
    private final List<ServiceBusMessageListenerContainer> serviceBusMessageListenerContainers = new ArrayList<>();
    private final InstrumentationManager instrumentationManager = new DefaultInstrumentationManager();
    private final Map<String, ExtendedProducerProperties<ServiceBusProducerProperties>>
        extendedProducerPropertiesMap = new ConcurrentHashMap<>();

    private final List<ServiceBusProducerFactoryCustomizer> producerFactoryCustomizers = new ArrayList<>();
    private final List<ServiceBusProcessorFactoryCustomizer> processorFactoryCustomizers = new ArrayList<>();

    /**
     * Construct a {@link ServiceBusMessageChannelBinder} with the specified headersToEmbed and {@link ServiceBusChannelProvisioner}.
     *
     * @param headersToEmbed the headers to embed
     * @param provisioningProvider the provisioning provider
     */
    public ServiceBusMessageChannelBinder(String[] headersToEmbed, ServiceBusChannelProvisioner provisioningProvider) {
        super(headersToEmbed, provisioningProvider);
    }

    @Override
    protected MessageHandler createProducerMessageHandler(
        ProducerDestination destination,
        ExtendedProducerProperties<ServiceBusProducerProperties> producerProperties,
        MessageChannel errorChannel) {
        Assert.notNull(getServiceBusTemplate(), "ServiceBusTemplate can't be null when create a producer");

        extendedProducerPropertiesMap.put(destination.getName(), producerProperties);
        DefaultMessageHandler handler = new DefaultMessageHandler(destination.getName(), this.serviceBusTemplate);
        handler.setBeanFactory(getBeanFactory());
        handler.setSync(producerProperties.getExtension().isSync());
        handler.setSendTimeout(producerProperties.getExtension().getSendTimeout().toMillis());
        handler.setSendFailureChannel(errorChannel);
        String instrumentationId = Instrumentation.buildId(PRODUCER, destination.getName());

        handler.setSendCallback(new InstrumentationSendCallback(instrumentationId, instrumentationManager));

        if (producerProperties.isPartitioned()) {
            handler.setPartitionKeyExpressionString(
                "'partitionKey-' + headers['" + BinderHeaders.PARTITION_HEADER + "']");
        } else {
            handler.setPartitionKeyExpression(new FunctionExpression<Message<?>>(m -> m.getPayload().hashCode()));
        }

        return handler;
    }

    @Override
    protected MessageProducer createConsumerEndpoint(ConsumerDestination destination, String group,
                                                     ExtendedConsumerProperties<ServiceBusConsumerProperties> properties) {
        final ServiceBusInboundChannelAdapter inboundAdapter;

        ServiceBusContainerProperties containerProperties = createContainerProperties(destination, group, properties);
        ServiceBusMessageListenerContainer listenerContainer = new ServiceBusMessageListenerContainer(getProcessorFactory(), containerProperties);

        serviceBusMessageListenerContainers.add(listenerContainer);

        inboundAdapter = new ServiceBusInboundChannelAdapter(listenerContainer);
        String instrumentationId = Instrumentation.buildId(CONSUMER, destination.getName() + "/" + getGroup(group));
        ErrorInfrastructure errorInfrastructure = registerErrorInfrastructure(destination, getGroup(group), properties);

        inboundAdapter.setBeanFactory(getBeanFactory());
        inboundAdapter.setInstrumentationManager(instrumentationManager);
        inboundAdapter.setInstrumentationId(instrumentationId);
        inboundAdapter.setErrorChannel(errorInfrastructure.getErrorChannel());
        inboundAdapter.setMessageConverter(messageConverter);
        return inboundAdapter;
    }

    /**
     * Create {@link ServiceBusContainerProperties} from the extended {@link ServiceBusConsumerProperties}.
     * @param destination reference to the consumer destination.
     * @param group the consumer group.
     * @param properties the consumer properties.
     * @return the {@link ServiceBusContainerProperties}.
     */
    ServiceBusContainerProperties createContainerProperties(
        ConsumerDestination destination,
        String group,
        ExtendedConsumerProperties<ServiceBusConsumerProperties> properties) {
        ServiceBusContainerProperties containerProperties = new ServiceBusContainerProperties();
        AzurePropertiesUtils.copyAzureCommonProperties(properties.getExtension(), containerProperties);
        ProcessorPropertiesMerger.copyProcessorPropertiesIfNotNull(properties.getExtension(), containerProperties);
        containerProperties.setEntityName(destination.getName());
        containerProperties.setSubscriptionName(group);
        return containerProperties;
    }

    @Override
    protected MessageHandler getErrorMessageHandler(ConsumerDestination destination,
                                                    String group,
                                                    final ExtendedConsumerProperties<ServiceBusConsumerProperties> properties) {
        return message -> {
            Assert.state(message instanceof ErrorMessage, "Expected an ErrorMessage, not a "
                + message.getClass().toString() + " for: " + message);

            ErrorMessage errorMessage = (ErrorMessage) message;
            Message<?> amqpMessage = errorMessage.getOriginalMessage();

            if (amqpMessage == null) {
                logger.error("No raw message header in " + message);
            } else {
                Throwable cause = (Throwable) message.getPayload();

                if (properties.getExtension().isRequeueRejected()) {
                    deadLetter(destination.getName(), amqpMessage, EXCEPTION_MESSAGE,
                        cause.getCause() != null ? cause.getCause().getMessage() : cause.getMessage());
                } else {
                    abandon(destination.getName(), amqpMessage);
                }
            }
        };
    }

    /**
     * Moves a message to the dead-letter sub-queue with dead-letter reason.
     *
     * @param <T> the type of message payload
     * @param destination the destination
     * @param message the message
     * @param deadLetterReason the dead-letter reason
     * @param deadLetterErrorDescription the dead-letter error description
     */
    public <T> void deadLetter(String destination,
                               Message<T> message,
                               String deadLetterReason,
                               String deadLetterErrorDescription) {
        Assert.hasText(destination, "destination can't be null or empty");
        final ServiceBusReceivedMessageContext messageContext = (ServiceBusReceivedMessageContext) message.getHeaders()
                                                                                                          .get(
                                                                                                              ServiceBusMessageHeaders.RECEIVED_MESSAGE_CONTEXT);
        if (messageContext != null) {
            messageContext.deadLetter();
        }
    }

    /**
     * Abandons the message in this context.
     *
     * @param <T> the type of message payload
     * @param destination the destination
     * @param message the message
     */
    public <T> void abandon(String destination, Message<T> message) {
        Assert.hasText(destination, "destination can't be null or empty");
        final ServiceBusReceivedMessageContext messageContext = (ServiceBusReceivedMessageContext) message.getHeaders()
                                                                                                          .get(
                                                                                                              ServiceBusMessageHeaders.RECEIVED_MESSAGE_CONTEXT);
        if (messageContext != null) {
            messageContext.abandon();
        }
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

    /**
     * Set binding properties.
     *
     * @param bindingProperties the binding properties
     */
    public void setBindingProperties(ServiceBusExtendedBindingProperties bindingProperties) {
        this.bindingProperties = bindingProperties;
    }

    private ServiceBusTemplate getServiceBusTemplate() {
        if (this.serviceBusTemplate == null) {
            DefaultServiceBusNamespaceProducerFactory factory = new DefaultServiceBusNamespaceProducerFactory(
                this.namespaceProperties, getProducerPropertiesSupplier());

            producerFactoryCustomizers.forEach(customizer -> customizer.customize(factory));

            factory.addListener((name, client) -> {
                DefaultInstrumentation instrumentation = new DefaultInstrumentation(name, PRODUCER);
                instrumentation.setStatus(Instrumentation.Status.UP);
                instrumentationManager.addHealthInstrumentation(instrumentation);
            });
            this.serviceBusTemplate = new ServiceBusTemplate(factory);
        }
        return this.serviceBusTemplate;
    }

    private ServiceBusProcessorFactory getProcessorFactory() {
        if (this.processorFactory == null) {
            this.processorFactory = new DefaultServiceBusNamespaceProcessorFactory(this.namespaceProperties);

            processorFactoryCustomizers.forEach(customizer -> customizer.customize(this.processorFactory));

            this.processorFactory.addListener((name, client) -> {
                String subscriptionName = client.getSubscriptionName();
                boolean isTopic = StringUtils.hasText(subscriptionName);
                String entityName = isTopic ? client.getTopicName() : client.getQueueName();
                String instrumentationName = entityName + "/" + getGroup(subscriptionName);
                Instrumentation instrumentation = new ServiceBusProcessorInstrumentation(instrumentationName, CONSUMER, Duration.ofMinutes(2));
                instrumentation.setStatus(Instrumentation.Status.UP);
                instrumentationManager.addHealthInstrumentation(instrumentation);
            });
        }
        return this.processorFactory;
    }

    private PropertiesSupplier<String, ProducerProperties> getProducerPropertiesSupplier() {
        return key -> {
            if (this.extendedProducerPropertiesMap.containsKey(key)) {
                ServiceBusProducerProperties producerProperties = this.extendedProducerPropertiesMap.get(key)
                    .getExtension();
                producerProperties.setEntityName(key);
                return producerProperties;
            } else {
                LOGGER.debug("Can't find extended properties for {}", key);
                return null;
            }
        };
    }

    /**
     * Set namespace properties.
     *
     * @param namespaceProperties the namespace properties
     */
    public void setNamespaceProperties(NamespaceProperties namespaceProperties) {
        this.namespaceProperties = namespaceProperties;
    }


    /**
     * Set message converter.
     *
     * @param messageConverter the message converter
     */
    public void setMessageConverter(AzureMessageConverter<ServiceBusReceivedMessage, ServiceBusMessage> messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Get instrumentation manager.
     *
     * @return instrumentationManager the instrumentation manager
     */
    public InstrumentationManager getInstrumentationManager() {
        return instrumentationManager;
    }

    private String getGroup(String group) {
        return group != null ? group : "";
    }

    /**
     * Add a producer factory customizer.
     *
     * @param producerFactoryCustomizer The producer factory customizer to add.
     */
    public void addProducerFactoryCustomizer(ServiceBusProducerFactoryCustomizer producerFactoryCustomizer) {
        if (producerFactoryCustomizer != null) {
            this.producerFactoryCustomizers.add(producerFactoryCustomizer);
        }
    }

    /**
     * Add a processor factory customizer.
     *
     * @param processorFactoryCustomizer The processor factory customizer to add.
     */
    public void addProcessorFactoryCustomizer(ServiceBusProcessorFactoryCustomizer processorFactoryCustomizer) {
        if (processorFactoryCustomizer != null) {
            this.processorFactoryCustomizers.add(processorFactoryCustomizer);
        }
    }

}
