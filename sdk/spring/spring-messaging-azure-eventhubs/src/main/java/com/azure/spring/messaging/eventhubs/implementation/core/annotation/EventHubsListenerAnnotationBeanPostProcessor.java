// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.implementation.core.annotation;

import com.azure.spring.messaging.eventhubs.implementation.core.config.MethodEventHubsListenerEndpoint;
import com.azure.spring.messaging.implementation.annotation.AzureListenerAnnotationBeanPostProcessorAdapter;
import com.azure.spring.messaging.implementation.annotation.AzureListenerConfigurer;
import com.azure.spring.messaging.implementation.annotation.EnableAzureMessaging;
import com.azure.spring.messaging.implementation.config.AzureListenerEndpoint;
import com.azure.spring.messaging.implementation.config.AzureListenerEndpointRegistrar;
import com.azure.spring.messaging.implementation.config.AzureListenerEndpointRegistry;
import com.azure.spring.messaging.implementation.listener.MessageListenerContainerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Bean post-processor that registers methods annotated with {@link EventHubsListener}
 * to be invoked by an Azure message listener container created under the cover
 * by a {@link MessageListenerContainerFactory} according to the attributes of the annotation.
 *
 * <p>Annotated methods can use flexible arguments as defined by {@link EventHubsListener}.
 *
 * <p>This post-processor is automatically registered by the {@link EnableAzureMessaging} annotation.
 *
 * <p>Auto-detects any {@link AzureListenerConfigurer} instances in the container,
 * allowing for customization of the registry to be used, the default container
 * factory or for fine-grained control over endpoints registration. See the
 * {@link EnableAzureMessaging} javadocs for complete usage details.
 *
 * @see EventHubsListener
 * @see EnableAzureMessaging
 * @see AzureListenerConfigurer
 * @see AzureListenerEndpointRegistrar
 * @see AzureListenerEndpointRegistry
 */
public class EventHubsListenerAnnotationBeanPostProcessor extends AzureListenerAnnotationBeanPostProcessorAdapter<EventHubsListener> {

    private static final String DEFAULT_EVENT_HUBS_LISTENER_ANNOTATION_BPP_BEAN_NAME = "eventHubsListenerAnnotationBeanPostProcessor";
    private static final String DEFAULT_EVENT_HUBS_LISTENER_CONTAINER_FACTORY_BEAN_NAME = "azureEventHubsListenerContainerFactory";

    /**
     * Construct an {@link EventHubsListenerAnnotationBeanPostProcessor} instance with default configuration.
     */
    public EventHubsListenerAnnotationBeanPostProcessor() {
        this.containerFactoryBeanName = DEFAULT_EVENT_HUBS_LISTENER_CONTAINER_FACTORY_BEAN_NAME;
    }

    @Override
    protected Set<EventHubsListener> findListenerMethods(Method method) {
        return AnnotatedElementUtils.getMergedRepeatableAnnotations(method, getListenerType(),
            EventHubsListeners.class);
    }

    @Override
    protected AzureListenerEndpoint createAndConfigureMethodListenerEndpoint(EventHubsListener listenerAnnotation,
                                                                             Object bean, Method method,
                                                                             BeanFactory beanFactory,
                                                                             MessageHandlerMethodFactory messageHandlerMethodFactory) {
        MethodEventHubsListenerEndpoint endpoint = new MethodEventHubsListenerEndpoint();

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
        endpoint.setBatchListener(false);

        return endpoint;
    }

    @Override
    protected String getEndpointId(EventHubsListener listenerAnnotation) {
        if (StringUtils.hasText(listenerAnnotation.id())) {
            String id = resolve(listenerAnnotation.id());
            return (id != null ? id : "");
        } else {
            return "org.springframework.azure.EventHubsListenerEndpointContainer#" + this.counter.getAndIncrement();
        }
    }

    @Override
    protected String getContainerFactoryBeanName(EventHubsListener listenerAnnotation) {
        return listenerAnnotation.containerFactory();
    }

    @Override
    protected Class<EventHubsListener> getListenerType() {
        return EventHubsListener.class;
    }

    @Override
    public String getDefaultAzureListenerAnnotationBeanPostProcessorBeanName() {
        return DEFAULT_EVENT_HUBS_LISTENER_ANNOTATION_BPP_BEAN_NAME;
    }
}
