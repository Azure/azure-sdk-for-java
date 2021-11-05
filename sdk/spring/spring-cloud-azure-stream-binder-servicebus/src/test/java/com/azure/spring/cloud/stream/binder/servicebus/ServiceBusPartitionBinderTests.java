//// Copyright (c) Microsoft Corporation. All rights reserved.
//// Licensed under the MIT License.
//
//package com.azure.spring.cloud.stream.binder.servicebus;
//
//import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusConsumerProperties;
//import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusExtendedBindingProperties;
//import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusProducerProperties;
//import com.azure.spring.cloud.stream.binder.servicebus.test.AzurePartitionBinderTests;
//import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;
//import com.azure.spring.servicebus.core.processor.ServiceBusProcessorFactory;
//import com.azure.spring.servicebus.core.properties.NamespaceProperties;
//import org.junit.Before;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.junit.MockitoJUnitRunner;
//import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
//import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
//import org.springframework.cloud.stream.binder.HeaderMode;
//
///**
// * Test cases are defined in super class
// *
// * @author Warren Zhu
// */
//@RunWith(MockitoJUnitRunner.class)
//public class ServiceBusPartitionBinderTests
//    extends AzurePartitionBinderTests<ServiceBusTestBinder,
//    ExtendedConsumerProperties<ServiceBusConsumerProperties>,
//    ExtendedProducerProperties<ServiceBusProducerProperties>> {
//    //TODO (Xiaobing Zhu): It is currently impossible to upgrade JUnit 4 to JUnit 5 due to the inheritance of Spring
//    // unit tests.
//
//    private ServiceBusTestBinder binder;
//
//    @Before
//    public void setUp() {
//        this.binder = new ServiceBusTestBinder();
//        NamespaceProperties namespaceProperties = new NamespaceProperties();
//        namespaceProperties.setNamespace("test");
//        ServiceBusExtendedBindingProperties bindingProperties = new ServiceBusExtendedBindingProperties();
//        this.binder.getBinder().setNamespaceProperties(namespaceProperties);
//
//    }
//
//    @Override
//    protected String getClassUnderTestName() {
//        return ServiceBusTestBinder.class.getSimpleName();
//    }
//
//    @Override
//    protected ServiceBusTestBinder getBinder() throws Exception {
//        return this.binder;
//    }
//
//    @Override
//    protected ExtendedConsumerProperties<ServiceBusConsumerProperties> createConsumerProperties() {
//        ExtendedConsumerProperties<ServiceBusConsumerProperties> properties = new ExtendedConsumerProperties<>(
//            new ServiceBusConsumerProperties());
//        properties.setHeaderMode(HeaderMode.embeddedHeaders);
//        properties.getExtension().getProcessor().setName("test");
//        properties.getExtension().getProcessor().setType(ServiceBusEntityType.QUEUE);
//        properties.getExtension().getProcessor().setNamespace("test");
//        return properties;
//    }
//
//    @Override
//    protected ExtendedProducerProperties<ServiceBusProducerProperties> createProducerProperties() {
//        ExtendedProducerProperties<ServiceBusProducerProperties> properties = new ExtendedProducerProperties<>(
//            new ServiceBusProducerProperties());
//        properties.setHeaderMode(HeaderMode.embeddedHeaders);
//        properties.getExtension().getProducer().setName("test");
//        properties.getExtension().getProducer().setType(ServiceBusEntityType.QUEUE);
//        properties.getExtension().getProducer().setNamespace("test");
//        return properties;
//    }
//
//    @Override
//    public void testOneRequiredGroup() {
//        // Required group test rely on unsupported start position of consumer properties
//    }
//
//    @Override
//    public void testTwoRequiredGroups() {
//        // Required group test rely on unsupported start position of consumer properties
//    }
//}
