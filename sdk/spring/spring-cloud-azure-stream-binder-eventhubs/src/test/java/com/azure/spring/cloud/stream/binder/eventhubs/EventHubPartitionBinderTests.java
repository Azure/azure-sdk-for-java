// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

//package com.azure.spring.cloud.stream.binder.eventhubs;
//
//import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubConsumerProperties;
//import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubProducerProperties;
//import com.azure.spring.cloud.stream.binder.servicebus.test.AzurePartitionBinderTests;
//import com.azure.spring.eventhubs.core.EventHubsTemplate;
//import com.azure.spring.eventhubs.core.producer.DefaultEventHubProducerFactory;
//import com.azure.spring.service.eventhubs.properties.EventProcessingProperties;
//import org.junit.Before;
//import org.mockito.MockitoAnnotations;
//import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
//import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
//import org.springframework.cloud.stream.binder.HeaderMode;
//
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
///**
// * Test cases are defined in super class
// *
// * @author Warren Zhu
// */
//public class EventHubPartitionBinderTests extends
//    AzurePartitionBinderTests<EventHubTestBinder, ExtendedConsumerProperties<EventHubConsumerProperties>,
//        ExtendedProducerProperties<EventHubProducerProperties>> {
//    //TODO (Xiaobing Zhu): It is currently impossible to upgrade JUnit 4 to JUnit 5 due to the inheritance of Spring unit tests.
//
//
////
////    @Mock
////    EventContext eventContext;
////
////    @Mock
////    PartitionContext partitionContext;
//
//    private EventHubTestBinder binder;
//
//    @Before
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
////        when(this.eventContext.getPartitionContext()).thenReturn(this.partitionContext);
////        when(this.partitionContext.getPartitionId()).thenReturn("1");
//        EventHubNamespaceProcessorsContainer processorsContainer = mock(EventHubNamespaceProcessorsContainer.class);
//        when()
//
//        EventHubsTemplate eventHubsTemplate = new EventHubsTestTemplate(
//            mock(DefaultEventHubProducerFactory.class), );
//
//        this.binder = new EventHubTestBinder(eventHubsTemplate, processorsContainer);
//    }
//
//    @Override
//    protected String getClassUnderTestName() {
//        return EventHubTestBinder.class.getSimpleName();
//    }
//
//    @Override
//    protected EventHubTestBinder getBinder() {
//        return this.binder;
//    }
//
//    @Override
//    protected ExtendedConsumerProperties<EventHubConsumerProperties> createConsumerProperties() {
//        ExtendedConsumerProperties<EventHubConsumerProperties> properties =
//            new ExtendedConsumerProperties<>(new EventHubConsumerProperties());
//        properties.setHeaderMode(HeaderMode.embeddedHeaders);
//        properties.getExtension().setStartPosition(EventProcessingProperties.StartPosition.EARLIEST);
//        return properties;
//    }
//
//    @Override
//    protected ExtendedProducerProperties<EventHubProducerProperties> createProducerProperties() {
//        ExtendedProducerProperties<EventHubProducerProperties> properties =
//            new ExtendedProducerProperties<>(new EventHubProducerProperties());
//        properties.setHeaderMode(HeaderMode.embeddedHeaders);
//        return properties;
//    }
//}
