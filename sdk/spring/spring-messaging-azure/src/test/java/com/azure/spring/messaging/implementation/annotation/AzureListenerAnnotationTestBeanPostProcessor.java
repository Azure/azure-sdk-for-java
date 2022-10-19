// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.implementation.annotation;

import com.azure.spring.messaging.implementation.config.AzureListenerEndpoint;
import com.azure.spring.messaging.implementation.endpoint.MethodAzureListenerTestEndpoint;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Set;

public class AzureListenerAnnotationTestBeanPostProcessor extends AzureListenerAnnotationBeanPostProcessorAdapter<AzureMessageTestListener> {

    private static final String DEFAULT_TEST_LISTENER_CONTAINER_FACTORY_BEAN_NAME = "azureListenerContainerFactory";

    public AzureListenerAnnotationTestBeanPostProcessor() {
        this.containerFactoryBeanName = DEFAULT_TEST_LISTENER_CONTAINER_FACTORY_BEAN_NAME;
    }

    @Override
    protected Set<AzureMessageTestListener> findListenerMethods(Method method) {
        return AnnotatedElementUtils.getMergedRepeatableAnnotations(method, getListenerType(),
            AzureMessageTestListeners.class);
    }

    @Override
    protected AzureListenerEndpoint createAndConfigureMethodListenerEndpoint(
        AzureMessageTestListener listenerAnnotation, Object bean, Method method, BeanFactory beanFactory,
        MessageHandlerMethodFactory messageHandlerMethodFactory) {
        MethodAzureListenerTestEndpoint endpoint = new MethodAzureListenerTestEndpoint();

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
    protected String getEndpointId(AzureMessageTestListener listenerAnnotation) {
        if (StringUtils.hasText(listenerAnnotation.id())) {
            String id = resolve(listenerAnnotation.id());
            return (id != null ? id : "");
        } else {
            return "org.springframework.azure.AzureListenerEndpointContainer#" + this.counter.getAndIncrement();
        }
    }

    @Override
    protected String getContainerFactoryBeanName(AzureMessageTestListener listenerAnnotation) {
        return listenerAnnotation.containerFactory();
    }

    @Override
    protected Class<AzureMessageTestListener> getListenerType() {
        return AzureMessageTestListener.class;
    }

    @Override
    public String getDefaultAzureListenerAnnotationBeanPostProcessorBeanName() {
        return "azureListenerAnnotationTestBeanPostProcessor";
    }
}
