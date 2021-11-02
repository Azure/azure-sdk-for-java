// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus;

import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusQueueExtendedBindingProperties;

/**
 * @author Warren Zhu
 */
public class ServiceBusQueueMessageChannelBinder extends
    ServiceBusMessageChannelBinder<ServiceBusQueueExtendedBindingProperties> {

//    private ServiceBusQueueOperation serviceBusQueueOperation;
//    private ServiceBusQueueProcessorContainer processorContainer;
//
//    public ServiceBusQueueMessageChannelBinder(String[] headersToEmbed,
//                                               @NonNull ServiceBusChannelProvisioner provisioningProvider) {
//        super(headersToEmbed, provisioningProvider);
//        this.bindingProperties = new ServiceBusQueueExtendedBindingProperties();
//    }
//
//    @Override
//    protected MessageProducer createConsumerEndpoint(ConsumerDestination destination, String group,
//                                                     ExtendedConsumerProperties<ServiceBusConsumerProperties> properties) {
//        Assert.notNull(getProcessorContainer(), "ServiceBusQueueProcessorContainer can't be null when create a consumer");
//        serviceBusInUse.put(destination.getName(), new ServiceBusInformation(group));
//
//        // TODO (xiada) the instance of service bus operation is shared among consumer endpoints, if each of them
//        //  doesn't share the same configuration the last will win。 Is it possible that's this is a bug here？
//        ServiceBusInboundChannelAdapter inboundAdapter =
//            new ServiceBusInboundChannelAdapter(this.processorContainer, destination.getName(), buildCheckpointConfig(properties));
//        inboundAdapter.setBeanFactory(getBeanFactory());
//        ErrorInfrastructure errorInfrastructure = registerErrorInfrastructure(destination, group, properties);
//        inboundAdapter.setErrorChannel(errorInfrastructure.getErrorChannel());
//        return inboundAdapter;
//    }
//
//    @Override
//    protected MessageHandler getErrorMessageHandler(ConsumerDestination destination,
//                                                    String group, final ExtendedConsumerProperties<ServiceBusConsumerProperties> properties) {
//        return message -> {
//            Assert.state(message instanceof ErrorMessage, "Expected an ErrorMessage, not a "
//                + message.getClass().toString() + " for: " + message);
//
//            ErrorMessage errorMessage = (ErrorMessage) message;
//            Message<?> amqpMessage = errorMessage.getOriginalMessage();
//
//            if (amqpMessage == null) {
//                logger.error("No raw message header in " + message);
//            } else {
//                Throwable cause = (Throwable) message.getPayload();
//
//                if (properties.getExtension().isRequeueRejected()) {
//                    processorContainer.deadLetter(destination.getName(), amqpMessage, EXCEPTION_MESSAGE,
//                        cause.getCause() != null ? cause.getCause().getMessage() : cause.getMessage());
//                } else {
//                    processorContainer.abandon(destination.getName(), amqpMessage);
//                }
//            }
//        };
//    }

}
