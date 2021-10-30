// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus;

import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusQueueExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.integration.servicebus.inbound.ServiceBusQueueInboundChannelAdapter;
import com.azure.spring.messaging.core.SendOperation;
import com.azure.spring.servicebus.core.ServiceBusTemplate;
import com.azure.spring.servicebus.core.processor.DefaultServiceBusNamespaceQueueProcessorClientFactory;
import com.azure.spring.servicebus.core.properties.ProcessorProperties;
import com.azure.spring.servicebus.core.properties.ProducerProperties;
import com.azure.spring.servicebus.core.queue.ServiceBusQueueOperation;
import com.azure.spring.servicebus.core.queue.ServiceBusQueueTemplate;
import com.azure.spring.servicebus.core.sender.DefaultServiceBusNamespaceQueueSenderClientFactory;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.util.Assert;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Map;

/**
 * @author Warren Zhu
 */
public class ServiceBusQueueMessageChannelBinder extends
    ServiceBusMessageChannelBinder<ServiceBusQueueExtendedBindingProperties> {

    public ServiceBusQueueMessageChannelBinder(String[] headersToEmbed,
                                               @NonNull ServiceBusChannelProvisioner provisioningProvider) {
        super(headersToEmbed, provisioningProvider);
        this.bindingProperties = new ServiceBusQueueExtendedBindingProperties();
    }

    @Override
    protected MessageProducer createConsumerEndpoint(ConsumerDestination destination, String group,
                                                     ExtendedConsumerProperties<ServiceBusConsumerProperties> properties) {
        Assert.notNull(getServiceBusTemplate(), "ServiceBusTemplate can't be null when create a consumer");
        serviceBusInUse.put(destination.getName(), new ServiceBusInformation(group));

        // TODO (xiada) the instance of service bus operation is shared among consumer endpoints, if each of them
        //  doesn't share the same configuration the last will win。 Is it possible that's this is a bug here？
        this.serviceBusTemplate.setCheckpointConfig(buildCheckpointConfig(properties));
        ServiceBusQueueInboundChannelAdapter inboundAdapter =
            new ServiceBusQueueInboundChannelAdapter(destination.getName(), this.serviceBusTemplate);
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
                    ServiceBusTemplate.deadLetter(destination.getName(), amqpMessage, EXCEPTION_MESSAGE,
                        cause.getCause() != null ? cause.getCause().getMessage() : cause.getMessage());
                } else {
                    ServiceBusTemplate.abandon(destination.getName(), amqpMessage);
                }
            }
        };
    }

    @Override
    protected ServiceBusTemplate getServiceBusTemplate() {
        if (this.serviceBusTemplate == null) {
            this.serviceBusTemplate = new ServiceBusQueueTemplate(
                new DefaultServiceBusNamespaceQueueSenderClientFactory(this.namespaceProperties,
                    getProducerPropertiesSupplier()),
                new DefaultServiceBusNamespaceQueueProcessorClientFactory(this.namespaceProperties,
                    getProcessorPropertiesSupplier()));
        }
        return this.serviceBusTemplate;
    }

    private PropertiesSupplier<String, ProducerProperties> getProducerPropertiesSupplier() {
        return key -> {
            Map<String, ServiceBusBindingProperties> bindings = bindingProperties.getBindings();
            for (Map.Entry<String, ServiceBusBindingProperties> entry : bindings.entrySet()) {
                ProducerProperties properties = bindings.get(entry.getKey()).getProducer().getProducer();
                if (key.equalsIgnoreCase(properties.getQueueName())) {
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
                if (key.equals(properties.getQueueName())) {
                    return properties;
                }
            }
            return null;
        };
    }

}
