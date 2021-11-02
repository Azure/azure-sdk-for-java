// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus;

import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusQueueExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.integration.servicebus.inbound.ServiceBusInboundChannelAdapter;
import com.azure.spring.messaging.core.SendOperation;
import com.azure.spring.servicebus.core.processor.container.ServiceBusQueueProcessorContainer;
import com.azure.spring.servicebus.core.ServiceBusTemplate;
import com.azure.spring.servicebus.core.processor.DefaultServiceBusNamespaceProcessorFactory;
import com.azure.spring.servicebus.core.properties.ProcessorProperties;
import com.azure.spring.servicebus.core.properties.ProducerProperties;
import com.azure.spring.servicebus.core.queue.ServiceBusQueueOperation;
import com.azure.spring.servicebus.core.producer.DefaultServiceBusNamespaceProducerFactory;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * @author Warren Zhu
 */
public class ServiceBusQueueMessageChannelBinder extends
    ServiceBusMessageChannelBinder<ServiceBusQueueExtendedBindingProperties> {

    private ServiceBusQueueOperation serviceBusQueueOperation;
    private ServiceBusQueueProcessorContainer processorContainer;

    public ServiceBusQueueMessageChannelBinder(String[] headersToEmbed,
                                               @NonNull ServiceBusChannelProvisioner provisioningProvider) {
        super(headersToEmbed, provisioningProvider);
        this.bindingProperties = new ServiceBusQueueExtendedBindingProperties();
    }

    @Override
    protected MessageProducer createConsumerEndpoint(ConsumerDestination destination, String group,
                                                     ExtendedConsumerProperties<ServiceBusConsumerProperties> properties) {
        Assert.notNull(getProcessorContainer(), "ServiceBusQueueProcessorContainer can't be null when create a consumer");
        serviceBusInUse.put(destination.getName(), new ServiceBusInformation(group));

        // TODO (xiada) the instance of service bus operation is shared among consumer endpoints, if each of them
        //  doesn't share the same configuration the last will win。 Is it possible that's this is a bug here？
        ServiceBusInboundChannelAdapter inboundAdapter =
            new ServiceBusInboundChannelAdapter(this.processorContainer, destination.getName(), buildCheckpointConfig(properties));
        inboundAdapter.setBeanFactory(getBeanFactory());
        ErrorInfrastructure errorInfrastructure = registerErrorInfrastructure(destination, group, properties);
        inboundAdapter.setErrorChannel(errorInfrastructure.getErrorChannel());
        return inboundAdapter;
    }

    @Override
    protected MessageHandler getErrorMessageHandler(ConsumerDestination destination,
                                                    String group, final ExtendedConsumerProperties<ServiceBusConsumerProperties> properties) {
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
                    processorContainer.deadLetter(destination.getName(), amqpMessage, EXCEPTION_MESSAGE,
                        cause.getCause() != null ? cause.getCause().getMessage() : cause.getMessage());
                } else {
                    processorContainer.abandon(destination.getName(), amqpMessage);
                }
            }
        };
    }

    @Override
    protected SendOperation getSendOperation() {
        if (this.sendOperation == null) {
            this.sendOperation = new ServiceBusTemplate(
                new DefaultServiceBusNamespaceProducerFactory(this.namespaceProperties,
                    getProducerPropertiesSupplier()));
        }
        return this.sendOperation;
    }

    protected PropertiesSupplier<String, ProducerProperties> getProducerPropertiesSupplier() {
        return key -> {
            Map<String, ServiceBusBindingProperties> bindings = bindingProperties.getBindings();
            for (Map.Entry<String, ServiceBusBindingProperties> entry : bindings.entrySet()) {
                ProducerProperties properties = bindings.get(entry.getKey()).getProducer().getProducer();
                if (key.equalsIgnoreCase(properties.getName())) {
                    return properties;
                }
            }
            return null;
        };
    }

    private PropertiesSupplier<String, ProcessorProperties> getProcessorPropertiesSupplier() {
        return key -> {
            Map<String, ServiceBusBindingProperties> bindings = bindingProperties.getBindings();
            for (Map.Entry<String, ServiceBusBindingProperties> entry : bindings.entrySet()) {
                ProcessorProperties properties = bindings.get(entry.getKey()).getConsumer().getProcessor();
                if (key.equals(properties.getName())) {
                    return properties;
                }
            }
            return null;
        };
    }

    private ServiceBusQueueProcessorContainer getProcessorContainer() {
        if (this.processorContainer == null) {
            this.processorContainer = new ServiceBusQueueProcessorContainer(
                new DefaultServiceBusNamespaceProcessorFactory(this.namespaceProperties, getProcessorPropertiesSupplier()));
        }
        return this.processorContainer;
    }
}
