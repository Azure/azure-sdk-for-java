// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus;

import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusTopicExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.servicebus.core.processor.container.ServiceBusTopicProcessorContainer;
import org.springframework.lang.NonNull;

/**
 * @author Warren Zhu
 */
public class ServiceBusTopicMessageChannelBinder extends
        ServiceBusMessageChannelBinder<ServiceBusTopicExtendedBindingProperties> {

    private ServiceBusTopicProcessorContainer processorContainer;

    public ServiceBusTopicMessageChannelBinder(String[] headersToEmbed,
            @NonNull ServiceBusChannelProvisioner provisioningProvider) {
        super(headersToEmbed, provisioningProvider);
        this.bindingProperties = new ServiceBusTopicExtendedBindingProperties();
    }

//    @Override
//    protected MessageProducer createConsumerEndpoint(ConsumerDestination destination, String group,
//            ExtendedConsumerProperties<ServiceBusConsumerProperties> properties) {
//        Assert.notNull(getProcessorContainer(), "Service Bus Topic Processor Container can't be null when create a consumer");
//        serviceBusInUse.put(destination.getName(), new ServiceBusInformation(group));
//
//        boolean anonymous = !StringUtils.hasText(group);
//        if (anonymous) {
//            group = "anonymous." + UUID.randomUUID();
//        }
//        ServiceBusTopicInboundChannelAdapter inboundAdapter =
//                new ServiceBusTopicInboundChannelAdapter(this.processorContainer, destination.getName(), group, buildCheckpointConfig(properties));
//        inboundAdapter.setBeanFactory(getBeanFactory());
//        ErrorInfrastructure errorInfrastructure = registerErrorInfrastructure(destination, group, properties);
//        inboundAdapter.setErrorChannel(errorInfrastructure.getErrorChannel());
//        return inboundAdapter;
//    }
//
//    @Override
//    protected SendOperation getSendOperation() {
//        if (this.sendOperation == null) {
//                this.sendOperation = new ServiceBusTemplate(
//                    new DefaultServiceBusNamespaceTopicProducerClientFactory(this.namespaceProperties,
//                        getProducerPropertiesSupplier()));
//        }
//        return this.sendOperation;
//    }
//
//    protected PropertiesSupplier<Tuple2<String, String>, ProducerProperties> getProducerPropertiesSupplier() {
//        return key -> {
//            Map<String, ServiceBusBindingProperties> bindings = bindingProperties.getBindings();
//            for (Map.Entry<String, ServiceBusBindingProperties> entry : bindings.entrySet()) {
//                ProducerProperties properties = bindings.get(entry.getKey()).getProducer().getProducer();
//                if (key.equalsIgnoreCase(properties.getType())) {
//                    return properties;
//                }
//            }
//            return null;
//        };
//    }
//
//    private PropertiesSupplier<Tuple2<String, String>, ProcessorProperties> getProcessorPropertiesSupplier() {
//        return key -> {
//            Map<String, ServiceBusBindingProperties> bindings = bindingProperties.getBindings();
//            for (Map.Entry<String, ServiceBusBindingProperties> entry : bindings.entrySet()) {
//                ProcessorProperties properties = bindings.get(entry.getKey()).getConsumer().getProcessor();
//                if (key.equals(Tuples.of(properties.getType(), properties.getSubscriptionName()))) {
//                    return properties;
//                }
//            }
//            return null;
//        };
//    }
//
//    private ServiceBusTopicProcessorContainer getProcessorContainer() {
//        if (this.processorContainer == null) {
//            this.processorContainer = new ServiceBusTopicProcessorContainer(
//                new DefaultServiceBusNamespaceTopicProcessorClientFactory(this.namespaceProperties, getProcessorPropertiesSupplier()));
//        }
//        return this.processorContainer;
//    }
}
