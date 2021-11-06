// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus;

import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.integration.instrumentation.DefaultInstrumentation;
import com.azure.spring.integration.instrumentation.DefaultInstrumentationManager;
import com.azure.spring.integration.instrumentation.Instrumentation;
import com.azure.spring.integration.instrumentation.InstrumentationManager;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusProducerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.integration.handler.DefaultMessageHandler;
import com.azure.spring.integration.servicebus.inbound.ServiceBusInboundChannelAdapter;
import com.azure.spring.integration.servicebus.inbound.health.ServiceBusProcessorInstrumentation;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.servicebus.core.ServiceBusProcessorContainer;
import com.azure.spring.servicebus.core.ServiceBusTemplate;
import com.azure.spring.servicebus.core.processor.DefaultServiceBusNamespaceProcessorFactory;
import com.azure.spring.servicebus.core.producer.DefaultServiceBusNamespaceProducerFactory;
import com.azure.spring.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.servicebus.core.properties.ProcessorProperties;
import com.azure.spring.servicebus.core.properties.ProducerProperties;
import com.azure.spring.servicebus.core.properties.SubscriptionPropertiesSupplier;
import com.azure.spring.servicebus.support.ServiceBusMessageHeaders;
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
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.Duration;
import java.util.Map;

import static com.azure.spring.integration.instrumentation.Instrumentation.Type.CONSUMER;
import static com.azure.spring.integration.instrumentation.Instrumentation.Type.PRODUCER;

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
    private final InstrumentationManager instrumentationManager = new DefaultInstrumentationManager();
    private static final DefaultErrorMessageStrategy DEFAULT_ERROR_MESSAGE_STRATEGY = new DefaultErrorMessageStrategy();

    private static final String EXCEPTION_MESSAGE = "exception-message";

    public ServiceBusMessageChannelBinder(String[] headersToEmbed, ServiceBusChannelProvisioner provisioningProvider) {
        super(headersToEmbed, provisioningProvider);
    }

    @Override
    protected MessageHandler createProducerMessageHandler(
        ProducerDestination destination,
        ExtendedProducerProperties<ServiceBusProducerProperties> producerProperties,
        MessageChannel errorChannel) {
        ServiceBusEntityType type = producerProperties.getExtension().getProducer().getType();
        Assert.notNull(type, "Type cannot be null.");

        DefaultMessageHandler handler = new DefaultMessageHandler(destination.getName(), getServiceBusTemplate());
        handler.setBeanFactory(getBeanFactory());
        handler.setSync(producerProperties.getExtension().isSync());
        handler.setSendTimeout(producerProperties.getExtension().getSendTimeout());
        handler.setSendFailureChannel(errorChannel);
        String instrumentationId = Instrumentation.buildId(PRODUCER, destination.getName());

        handler.setSendCallback(new InstrumentationSendCallback(instrumentationManager.getHealthInstrumentation(instrumentationId)));

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
        if (StringUtils.hasText(group)) {
            inboundAdapter =
                new ServiceBusInboundChannelAdapter(getProcessorContainer(), destination.getName(), group,
                    buildCheckpointConfig(properties));
        } else {
            inboundAdapter =
                new ServiceBusInboundChannelAdapter(getProcessorContainer(), destination.getName(),
                    buildCheckpointConfig(properties));
        }
        inboundAdapter.setBeanFactory(getBeanFactory());
        String instrumentationId = Instrumentation.buildId(CONSUMER, destination.getName() + "/" + group != null ? group : "");
        inboundAdapter.setInstrumentation(instrumentationManager.getHealthInstrumentation(instrumentationId));
        ErrorInfrastructure errorInfrastructure = registerErrorInfrastructure(destination, group, properties);
        inboundAdapter.setErrorChannel(errorInfrastructure.getErrorChannel());
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

    public void setBindingProperties(ServiceBusExtendedBindingProperties bindingProperties) {
        this.bindingProperties = bindingProperties;
    }

    private CheckpointConfig buildCheckpointConfig(
        ExtendedConsumerProperties<ServiceBusConsumerProperties> properties) {

        return new CheckpointConfig(properties.getExtension().getCheckpointMode());
    }

    public ServiceBusTemplate getServiceBusTemplate() {
        if (this.serviceBusTemplate == null) {
            DefaultServiceBusNamespaceProducerFactory factory = new DefaultServiceBusNamespaceProducerFactory(
                this.namespaceProperties, getProducerPropertiesSupplier());

            factory.addListener((name) -> {
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

//            factory.addListener((name, subscription) -> {
//                String instrumentationName = name + "/" + subscription == null ? "" : subscription;
//                Instrumentation instrumentation = new ServiceBusProcessorInstrumentation(instrumentationName, CONSUMER, Duration.ofMinutes(2));
//                instrumentation.markUp();
//                instrumentationManager.addHealthInstrumentation(instrumentation.getId(), instrumentation);
//            });

            this.processorContainer = new ServiceBusProcessorContainer(factory);
            this.processorContainer.addListener((name, subscription) -> {
                String instrumentationName = name + "/" + subscription == null ? "" : subscription;
                Instrumentation instrumentation = new ServiceBusProcessorInstrumentation(instrumentationName, CONSUMER, Duration.ofMinutes(2));
                instrumentation.markUp();
                instrumentationManager.addHealthInstrumentation(instrumentation.getId(), instrumentation);
            });
        }
        return this.processorContainer;
    }

    private PropertiesSupplier<String, ProducerProperties> getProducerPropertiesSupplier() {
        return key -> {
            Map<String, ServiceBusBindingProperties> bindings = bindingProperties.getBindings();
            for (Map.Entry<String, ServiceBusBindingProperties> entry : bindings.entrySet()) {
                ProducerProperties properties = bindings.get(entry.getKey()).getProducer().getProducer();
                if (properties.getName() == null) {
                    continue;
                }
                if (key.equalsIgnoreCase(properties.getName())) {
                    return properties;
                }
            }
            return null;
        };
    }

    private SubscriptionPropertiesSupplier<ProcessorProperties> getProcessorPropertiesSupplier() {
        return new SubscriptionPropertiesSupplier<ProcessorProperties>() {
            @Override
            public ProcessorProperties getQueueSubscription(String name) {
                Map<String, ServiceBusBindingProperties> bindings = bindingProperties.getBindings();
                for (Map.Entry<String, ServiceBusBindingProperties> entry : bindings.entrySet()) {
                    ProcessorProperties properties = bindings.get(entry.getKey()).getConsumer().getProcessor();
                    if (properties.getName() == null) {
                        continue;
                    }
                    if (name.equals(properties.getName())) {
                        return properties;
                    }
                }
                return null;
            }

            @Override
            public ProcessorProperties getTopicSubscription(String name, String subscription) {
                Map<String, ServiceBusBindingProperties> bindings = bindingProperties.getBindings();
                for (Map.Entry<String, ServiceBusBindingProperties> entry : bindings.entrySet()) {
                    ProcessorProperties properties = bindings.get(entry.getKey()).getConsumer().getProcessor();
                    if (properties.getName() == null || properties.getSubscriptionName() == null) {
                        continue;
                    }
                    if (name.equals(properties.getName()) && subscription.equals(properties.getSubscriptionName())) {
                        return properties;
                    }
                }
                return null;
            }
        };
    }

    public void setNamespaceProperties(NamespaceProperties namespaceProperties) {
        this.namespaceProperties = namespaceProperties;
    }

    public InstrumentationManager getInstrumentationManager() {
        return instrumentationManager;
    }

    private static class InstrumentationSendCallback implements ListenableFutureCallback<Void> {

        private final Instrumentation instrumentation;

        public InstrumentationSendCallback(Instrumentation instrumentation) {
            this.instrumentation = instrumentation;
        }

        @Override
        public void onFailure(Throwable ex) {
            instrumentation.markDown(ex);
        }

        @Override
        public void onSuccess(Void result) {
            instrumentation.markUp();
        }
    }
}
