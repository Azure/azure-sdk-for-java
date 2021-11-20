// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus;

import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusProducerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.integration.instrumentation.Instrumentation;
import com.azure.spring.integration.servicebus.inbound.health.ServiceBusProcessorInstrumentation;
import com.azure.spring.messaging.checkpoint.CheckpointMode;
import com.azure.spring.service.servicebus.processor.RecordMessageProcessingListener;
import com.azure.spring.service.servicebus.processor.consumer.ErrorContextConsumer;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.servicebus.core.ServiceBusProcessorContainer;
import com.azure.spring.servicebus.core.ServiceBusTemplate;
import com.azure.spring.servicebus.core.producer.DefaultServiceBusNamespaceProducerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.HeaderMode;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;

import static com.azure.spring.integration.instrumentation.Instrumentation.Type.CONSUMER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ServiceBusBinderHealthIndicatorTest {

    @Mock
    private ConfigurableListableBeanFactory beanFactory;

    @Mock
    private ConsumerDestination consumerDestination;

    @Mock
    private ProducerDestination producerDestination;

    private final ServiceBusExtendedBindingProperties extendedBindingProperties =
        new ServiceBusExtendedBindingProperties();

    private ExtendedProducerProperties<ServiceBusProducerProperties> producerProperties;

    private ExtendedConsumerProperties<ServiceBusConsumerProperties> consumerProperties;

    private final ServiceBusProducerProperties serviceBusProducerProperties = new ServiceBusProducerProperties();

    private final ServiceBusConsumerProperties serviceBusConsumerProperties = new ServiceBusConsumerProperties();
    @Mock
    private MessageChannel errorChannel;

    private ServiceBusMessageChannelBinder binder = new ServiceBusMessageChannelBinder(
        BinderHeaders.STANDARD_HEADERS, new ServiceBusChannelProvisioner());

    private ServiceBusHealthIndicator serviceBusHealthIndicator;
    private static final String ENTITY_NAME = "test-entity";
    private static final String GROUP = "test";
    private static final String NAMESPACE_NAME = "test-namespace";

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        GenericApplicationContext context = new GenericApplicationContext();
        binder.setApplicationContext(context);
        serviceBusHealthIndicator = new ServiceBusHealthIndicator(binder);
    }

    @Test
    public void testNoInstrumentationInUse() {
        final Health health = serviceBusHealthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testServiceBusProducerHealthIndicatorIsUp() {
        prepareProducerProperties();
        when(producerDestination.getName()).thenReturn(ENTITY_NAME);
        binder.createProducerMessageHandler(producerDestination, producerProperties, errorChannel);
        ServiceBusTemplate serviceBusTemplate = (ServiceBusTemplate) ReflectionTestUtils.getField(binder,
                "serviceBusTemplate");
        DefaultServiceBusNamespaceProducerFactory producerFactory =
            (DefaultServiceBusNamespaceProducerFactory) ReflectionTestUtils.getField(serviceBusTemplate,
                "producerFactory");
        producerFactory.createProducer(ENTITY_NAME);
        final Health health = serviceBusHealthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

//    @SuppressWarnings({ "unchecked", "rawtypes" })
//    @Test
//    public void testServiceBusProducerHealthIndicatorIsDown() {
//        prepareProducerProperties();
//        when(producerDestination.getName()).thenReturn(ENTITY_NAME);
//        DefaultMessageHandler producerMessageHandler =
//            (DefaultMessageHandler) binder.createProducerMessageHandler(producerDestination, producerProperties,
//                errorChannel);
//        producerMessageHandler.setBeanFactory(beanFactory);
//        producerMessageHandler.handleMessage(MessageBuilder.withPayload("test").setHeader(AzureHeaders.PARTITION_KEY, "fake-key").build());
//        final Health health = serviceBusHealthIndicator.health();
//        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
//    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testServiceBusProcessorHealthIndicatorIsUp() {
        prepareConsumerProperties();
        when(consumerDestination.getName()).thenReturn(ENTITY_NAME);
        binder.createConsumerEndpoint(consumerDestination, null, consumerProperties);
        ServiceBusProcessorContainer processorContainer =
            (ServiceBusProcessorContainer) ReflectionTestUtils.getField(binder,
            "processorContainer");
        TestMessageProcessingListener listener = new TestMessageProcessingListener();
        listener.setInstrumentation(binder.getInstrumentationManager().getHealthInstrumentation(Instrumentation.buildId(CONSUMER, ENTITY_NAME)));
        processorContainer.subscribe(ENTITY_NAME, listener);

        final Health health = serviceBusHealthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

//    @SuppressWarnings({ "unchecked", "rawtypes" })
//    @Test
//    @Ignore
//    public void testServiceBusProcessorHealthIndicatorIsDown() {
//        prepareConsumerProperties();
//        when(consumerDestination.getName()).thenReturn(ENTITY_NAME);
//        ServiceBusInboundChannelAdapter consumerEndpoint = (ServiceBusInboundChannelAdapter) binder.createConsumerEndpoint(consumerDestination, null,
//            createConsumerProperties());
//        ServiceBusProcessorContainer processorContainer =
//            (ServiceBusProcessorContainer) ReflectionTestUtils.getField(binder,
//                "processorContainer");
////        DefaultServiceBusNamespaceProcessorFactory processorFactory =
////            (DefaultServiceBusNamespaceProcessorFactory) ReflectionTestUtils.getField(processorContainer,
////                "processorFactory");
////        processorFactory.createProcessor(ENTITY_NAME, new TestMessageProcessingListener());
//        TestMessageProcessingListener listener = new TestMessageProcessingListener();
//        listener.setInstrumentation(binder.getInstrumentationManager().getHealthInstrumentation(Instrumentation.buildId(CONSUMER, ENTITY_NAME)));
//        processorContainer.subscribe(ENTITY_NAME, listener);
//        final Health health = serviceBusHealthIndicator.health();
//        assertThat(health.getStatus()).isEqualTo(Status.UP);
//    }

    private void prepareProducerProperties() {
        serviceBusProducerProperties.setEntityName(ENTITY_NAME);
        serviceBusProducerProperties.setEntityType(ServiceBusEntityType.TOPIC);
        serviceBusProducerProperties.setNamespace(NAMESPACE_NAME);
        serviceBusProducerProperties.setSync(false);
        ServiceBusBindingProperties bindingProperties = new ServiceBusBindingProperties();
        bindingProperties.setProducer(serviceBusProducerProperties);

        extendedBindingProperties.setBindings(new HashMap<String, ServiceBusBindingProperties>() {{
                put(ENTITY_NAME, bindingProperties);
            }});
        binder.setBindingProperties(extendedBindingProperties);

        producerProperties = new ExtendedProducerProperties<>(serviceBusProducerProperties);
        producerProperties.setHeaderMode(HeaderMode.embeddedHeaders);
    }

    private void prepareConsumerProperties() {
        serviceBusConsumerProperties.setEntityName(ENTITY_NAME);
        serviceBusConsumerProperties.setEntityType(ServiceBusEntityType.QUEUE);
        serviceBusConsumerProperties.setNamespace(NAMESPACE_NAME);
        serviceBusConsumerProperties.setCheckpointMode(CheckpointMode.RECORD);
        ServiceBusBindingProperties bindingProperties = new ServiceBusBindingProperties();
        bindingProperties.setConsumer(serviceBusConsumerProperties);

        extendedBindingProperties.setBindings(new HashMap<String, ServiceBusBindingProperties>() {{
                put(ENTITY_NAME, bindingProperties);
            }});
        binder.setBindingProperties(extendedBindingProperties);

        consumerProperties = new ExtendedConsumerProperties<>(serviceBusConsumerProperties);
        consumerProperties.setHeaderMode(HeaderMode.embeddedHeaders);
    }

    static class TestMessageProcessingListener implements RecordMessageProcessingListener {
        private Instrumentation instrumentation;

        @Override
        public void onMessage(ServiceBusReceivedMessageContext messageContext) {

        }

        @Override
        public ErrorContextConsumer getErrorContextConsumer() {
            return errorContext -> {
                if (instrumentation != null) {
                    if (instrumentation instanceof ServiceBusProcessorInstrumentation) {
                        ((ServiceBusProcessorInstrumentation) instrumentation).markError(errorContext);
                    } else {
                        instrumentation.markDown(errorContext.getException());
                    }
                }
            };
        }

        public void setInstrumentation(Instrumentation instrumentation) {
            this.instrumentation = instrumentation;
        }
    }
}
