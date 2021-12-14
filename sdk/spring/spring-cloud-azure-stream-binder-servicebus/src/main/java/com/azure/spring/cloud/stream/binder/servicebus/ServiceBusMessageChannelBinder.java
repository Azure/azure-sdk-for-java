// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus;

import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusProducerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.integration.handler.DefaultMessageHandler;
import com.azure.spring.integration.instrumentation.DefaultInstrumentation;
import com.azure.spring.integration.instrumentation.DefaultInstrumentationManager;
import com.azure.spring.integration.instrumentation.Instrumentation;
import com.azure.spring.integration.instrumentation.InstrumentationManager;
import com.azure.spring.integration.instrumentation.InstrumentationSendCallback;
import com.azure.spring.integration.servicebus.inbound.ServiceBusInboundChannelAdapter;
import com.azure.spring.integration.servicebus.inbound.health.ServiceBusProcessorInstrumentation;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.servicebus.core.ServiceBusProcessorContainer;
import com.azure.spring.servicebus.core.ServiceBusTemplate;
import com.azure.spring.servicebus.core.processor.DefaultServiceBusNamespaceProcessorFactory;
import com.azure.spring.servicebus.core.producer.DefaultServiceBusNamespaceProducerFactory;
import com.azure.spring.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.servicebus.core.properties.ProcessorProperties;
import com.azure.spring.servicebus.core.properties.ProducerProperties;
import com.azure.spring.servicebus.support.ServiceBusMessageHeaders;
import com.azure.spring.servicebus.support.converter.ServiceBusMessageConverter;
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
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.spring.integration.instrumentation.Instrumentation.Type.CONSUMER;
import static com.azure.spring.integration.instrumentation.Instrumentation.Type.PRODUCER;
import static com.azure.spring.servicebus.core.processor.DefaultServiceBusNamespaceProcessorFactory.INVALID_SUBSCRIPTION;

/**
 *
 */
public class ServiceBusMessageChannelBinder extends
    AbstractMessageChannelBinder<ExtendedConsumerProperties<ServiceBusConsumerProperties>,
            ExtendedProducerProperties<ServiceBusProducerProperties>,
            ServiceBusChannelProvisioner>
    implements
    ExtendedPropertiesBinder<MessageChannel, ServiceBusConsumerProperties, ServiceBusProducerProperties> {

    private ServiceBusExtendedBindingProperties bindingProperties = new ServiceBusExtendedBindingProperties();
    private NamespaceProperties namespaceProperties;
    private ServiceBusTemplate serviceBusTemplate;
    private ServiceBusProcessorContainer processorContainer;
    private ServiceBusMessageConverter messageConverter = new ServiceBusMessageConverter();
    private final InstrumentationManager instrumentationManager = new DefaultInstrumentationManager();
    private final Map<String, ExtendedProducerProperties<ServiceBusProducerProperties>>
        extendedProducerPropertiesMap = new ConcurrentHashMap<>();
    private final Map<Tuple2<String, String>, ExtendedConsumerProperties<ServiceBusConsumerProperties>>
        extendedConsumerPropertiesMap = new ConcurrentHashMap<>();
    private static final DefaultErrorMessageStrategy DEFAULT_ERROR_MESSAGE_STRATEGY = new DefaultErrorMessageStrategy();

    private static final String EXCEPTION_MESSAGE = "exception-message";
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusMessageChannelBinder.class);

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
        handler.setSendTimeout(producerProperties.getExtension().getSendTimeout());
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
        if (group == null) {
            group = INVALID_SUBSCRIPTION;
        }
        extendedConsumerPropertiesMap.put(Tuples.of(destination.getName(), group), properties);
        final ServiceBusInboundChannelAdapter inboundAdapter;
        if (!INVALID_SUBSCRIPTION.equals(group)) {
            inboundAdapter =
                new ServiceBusInboundChannelAdapter(getProcessorContainer(), destination.getName(), group,
                    buildCheckpointConfig(properties));
        } else {
            inboundAdapter =
                new ServiceBusInboundChannelAdapter(getProcessorContainer(), destination.getName(),
                    buildCheckpointConfig(properties));
        }
        inboundAdapter.setBeanFactory(getBeanFactory());
        String instrumentationId = Instrumentation.buildId(CONSUMER, destination.getName() + "/" + (!INVALID_SUBSCRIPTION.equals(group) ? group : ""));
        inboundAdapter.setInstrumentationManager(instrumentationManager);
        inboundAdapter.setInstrumentationId(instrumentationId);
        ErrorInfrastructure errorInfrastructure = registerErrorInfrastructure(destination, !INVALID_SUBSCRIPTION.equals(group) ? group : "", properties);
        inboundAdapter.setErrorChannel(errorInfrastructure.getErrorChannel());
        inboundAdapter.setMessageConverter(messageConverter);
        return inboundAdapter;
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
     * @param <T> the type
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
     * @param <T> the type
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

    private CheckpointConfig buildCheckpointConfig(
        ExtendedConsumerProperties<ServiceBusConsumerProperties> properties) {

        return new CheckpointConfig(properties.getExtension().getCheckpointMode());
    }

    private ServiceBusTemplate getServiceBusTemplate() {
        if (this.serviceBusTemplate == null) {
            DefaultServiceBusNamespaceProducerFactory factory = new DefaultServiceBusNamespaceProducerFactory(
                this.namespaceProperties, getProducerPropertiesSupplier());

            factory.addListener((name, client) -> {
                DefaultInstrumentation instrumentation = new DefaultInstrumentation(name, PRODUCER);
                instrumentation.markUp();
                instrumentationManager.addHealthInstrumentation(instrumentation.getId(), instrumentation);
            });
            this.serviceBusTemplate = new ServiceBusTemplate(factory);
        }
        return this.serviceBusTemplate;
    }

    private ServiceBusProcessorContainer getProcessorContainer() {
        if (this.processorContainer == null) {
            DefaultServiceBusNamespaceProcessorFactory factory = new DefaultServiceBusNamespaceProcessorFactory(
                this.namespaceProperties, getProcessorPropertiesSupplier());

            factory.addListener((name, subscription, client) -> {
                String instrumentationName = name + "/" + subscription == null ? "" : subscription;
                Instrumentation instrumentation = new ServiceBusProcessorInstrumentation(instrumentationName, CONSUMER, Duration.ofMinutes(2));
                instrumentation.markUp();
                instrumentationManager.addHealthInstrumentation(instrumentation.getId(), instrumentation);
            });

            this.processorContainer = new ServiceBusProcessorContainer(factory);
        }
        return this.processorContainer;
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

    private PropertiesSupplier<Tuple2<String, String>, ProcessorProperties> getProcessorPropertiesSupplier() {
        return key -> {
            if (this.extendedConsumerPropertiesMap.containsKey(key)) {
                ServiceBusConsumerProperties consumerProperties = this.extendedConsumerPropertiesMap.get(key)
                    .getExtension();
                consumerProperties.setEntityName(key.getT1());
                consumerProperties.setSubscriptionName(key.getT2());
                return consumerProperties;
            } else {
                LOGGER.debug("Can't find extended properties for destination {}, group {}", key.getT1(), key.getT2());
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
    public void setMessageConverter(ServiceBusMessageConverter messageConverter) {
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
}
