// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.implementation.core.annotation;

import com.azure.spring.messaging.implementation.annotation.AzureListenerAnnotationBeanPostProcessorAdapter;
import com.azure.spring.messaging.implementation.annotation.AzureListenerConfigurer;
import com.azure.spring.messaging.implementation.annotation.EnableAzureMessaging;
import com.azure.spring.messaging.implementation.config.AzureListenerEndpoint;
import com.azure.spring.messaging.implementation.config.AzureListenerEndpointRegistrar;
import com.azure.spring.messaging.implementation.config.AzureListenerEndpointRegistry;
import com.azure.spring.messaging.implementation.listener.MessageListenerContainerFactory;
import com.azure.spring.messaging.servicebus.implementation.core.config.MethodServiceBusListenerEndpoint;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Bean post-processor that registers methods annotated with {@link ServiceBusListener}
 * to be invoked by a Azure message listener container created under the cover
 * by a {@link MessageListenerContainerFactory}
 * according to the attributes of the annotation.
 *
 * <p>Annotated methods can use flexible arguments as defined by {@link ServiceBusListener}.
 *
 * <p>This post-processor is automatically registered by the {@link EnableAzureMessaging} annotation.
 *
 * <p>Auto-detects any {@link AzureListenerConfigurer} instances in the container,
 * allowing for customization of the registry to be used, the default container
 * factory or for fine-grained control over endpoints registration. See the
 * {@link EnableAzureMessaging} javadocs for complete usage details.
 *
 * @see ServiceBusListener
 * @see EnableAzureMessaging
 * @see AzureListenerConfigurer
 * @see AzureListenerEndpointRegistrar
 * @see AzureListenerEndpointRegistry
 */
public class ServiceBusListenerAnnotationBeanPostProcessor extends AzureListenerAnnotationBeanPostProcessorAdapter<ServiceBusListener> {


    private static final String DEFAULT_SERVICE_BUS_LISTENER_ANNOTATION_BPP_BEAN_NAME = "serviceBusListenerAnnotationBeanPostProcessor";
    private static final String DEFAULT_SERVICE_BUS_LISTENER_CONTAINER_FACTORY_BEAN_NAME = "azureServiceBusListenerContainerFactory";

    /**
     * Construct an {@link ServiceBusListenerAnnotationBeanPostProcessor} instance with default configuration.
     */
    public ServiceBusListenerAnnotationBeanPostProcessor() {
        this.containerFactoryBeanName = DEFAULT_SERVICE_BUS_LISTENER_CONTAINER_FACTORY_BEAN_NAME;
    }

    @Override
    protected Set<ServiceBusListener> findListenerMethods(Method method) {
        return AnnotatedElementUtils.getMergedRepeatableAnnotations(method, getListenerType(),
            ServiceBusListeners.class);
    }

    @Override
    protected AzureListenerEndpoint createAndConfigureMethodListenerEndpoint(ServiceBusListener listenerAnnotation,
                                                                             Object bean, Method method,
                                                                             BeanFactory beanFactory,
                                                                             MessageHandlerMethodFactory messageHandlerMethodFactory) {
        MethodServiceBusListenerEndpoint endpoint = new MethodServiceBusListenerEndpoint();

        endpoint.setBean(bean);
        endpoint.setMethod(method);
        endpoint.setBeanFactory(beanFactory);
        endpoint.setMessageHandlerMethodFactory(messageHandlerMethodFactory);
        endpoint.setId(getEndpointId(listenerAnnotation));
        endpoint.setDestination(resolve(listenerAnnotation.destination()));

        if (StringUtils.hasText(listenerAnnotation.group())) {
            endpoint.setGroup(resolve(listenerAnnotation.group()));
        }
        if (StringUtils.hasText(listenerAnnotation.concurrency())) {
            endpoint.setConcurrency(resolve(listenerAnnotation.concurrency()));
        }
        return endpoint;
    }

    @Override
    protected String getEndpointId(ServiceBusListener listenerAnnotation) {
        if (StringUtils.hasText(listenerAnnotation.id())) {
            String id = resolve(listenerAnnotation.id());
            return (id != null ? id : "");
        } else {
            return "org.springframework.azure.ServiceBusListenerEndpointContainer#" + this.counter.getAndIncrement();
        }
    }

    @Override
    protected String getContainerFactoryBeanName(ServiceBusListener listenerAnnotation) {
        return listenerAnnotation.containerFactory();
    }

    @Override
    protected Class<ServiceBusListener> getListenerType() {
        return ServiceBusListener.class;
    }

    @Override
    public String getDefaultAzureListenerAnnotationBeanPostProcessorBeanName() {
        return DEFAULT_SERVICE_BUS_LISTENER_ANNOTATION_BPP_BEAN_NAME;
    }
}
