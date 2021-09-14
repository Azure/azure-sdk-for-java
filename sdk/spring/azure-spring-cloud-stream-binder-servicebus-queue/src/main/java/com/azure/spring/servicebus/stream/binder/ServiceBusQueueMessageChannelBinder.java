// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.stream.binder;

import com.azure.spring.integration.core.api.SendOperation;
import com.azure.spring.integration.servicebus.inbound.ServiceBusQueueInboundChannelAdapter;
import com.azure.spring.servicebus.core.queue.ServiceBusQueueOperation;
import com.azure.spring.servicebus.stream.binder.properties.ServiceBusConsumerProperties;
import com.azure.spring.servicebus.stream.binder.properties.ServiceBusQueueExtendedBindingProperties;
import com.azure.spring.servicebus.stream.binder.provisioning.ServiceBusChannelProvisioner;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.util.Assert;

/**
 * @author Warren Zhu
 */
public class ServiceBusQueueMessageChannelBinder extends
    ServiceBusMessageChannelBinder<ServiceBusQueueExtendedBindingProperties> {

    private final ServiceBusQueueOperation serviceBusQueueOperation;

    public ServiceBusQueueMessageChannelBinder(String[] headersToEmbed,
                                               @NonNull ServiceBusChannelProvisioner provisioningProvider,
                                               @NonNull ServiceBusQueueOperation serviceBusQueueOperation) {
        super(headersToEmbed, provisioningProvider);
        this.serviceBusQueueOperation = serviceBusQueueOperation;
        this.bindingProperties = new ServiceBusQueueExtendedBindingProperties();
    }

    @Override
    protected MessageProducer createConsumerEndpoint(ConsumerDestination destination, String group,
        ExtendedConsumerProperties<ServiceBusConsumerProperties> properties) {

        // TODO (xiada) the instance of service bus operation is shared among consumer endpoints, if each of them
        //  doesn't share the same configuration the last will win。 Is it possible that's this is a bug here？
        this.serviceBusQueueOperation.setCheckpointConfig(buildCheckpointConfig(properties));
        this.serviceBusQueueOperation.setClientConfig(buildClientConfig(properties));
        ServiceBusQueueInboundChannelAdapter inboundAdapter =
                new ServiceBusQueueInboundChannelAdapter(destination.getName(), this.serviceBusQueueOperation);
        inboundAdapter.setBeanFactory(getBeanFactory());
        ErrorInfrastructure errorInfrastructure = registerErrorInfrastructure(destination, group, properties);
        inboundAdapter.setErrorChannel(errorInfrastructure.getErrorChannel());
        return inboundAdapter;
    }

    @Override
    SendOperation getSendOperation() {
        return this.serviceBusQueueOperation;
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
                    serviceBusQueueOperation.deadLetter(destination.getName(), amqpMessage, EXCEPTION_MESSAGE,
                            cause.getCause() != null ? cause.getCause().getMessage() : cause.getMessage());
                } else {
                    serviceBusQueueOperation.abandon(destination.getName(), amqpMessage);
                }
            }
        };
    }

}
