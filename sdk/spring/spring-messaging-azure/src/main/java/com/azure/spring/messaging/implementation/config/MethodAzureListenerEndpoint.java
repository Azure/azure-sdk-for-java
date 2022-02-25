// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.implementation.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.lang.Nullable;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;

import java.lang.reflect.Method;

/**
 * Model for a method Azure listener endpoint.
 *
 */
public interface MethodAzureListenerEndpoint extends BeanFactoryAware, AzureListenerEndpoint {

    /**
     * Set the {@link BeanFactory} to use to resolve expressions (maybe {@code null}).
     *
     * @param beanFactory the bean factory.
     */
    @Override
    void setBeanFactory(@Nullable BeanFactory beanFactory) throws BeansException;


    /**
     * Get the object instance that should manage this endpoint.
     * @return the target bean instance.
     */
    Object getBean();

    /**
     * Set the object instance that should manage this endpoint.
     *
     * @param bean the target bean instance.
     */
    void setBean(Object bean);

    /**
     * Get the method to invoke to process a message managed by this endpoint.
     * @return the method to invoke to process a message managed by this endpoint.
     */
    Method getMethod();

    /**
     * Set the method to invoke to process a message managed by this endpoint.
     *
     * @param method the target method for the {@link #getBean()}.
     */
    void setMethod(Method method);

    /**
     * Set the {@link MessageHandlerMethodFactory} to use to build the
     * {@link InvocableHandlerMethod} responsible to manage the invocation
     * of this endpoint.
     *
     * @param messageHandlerMethodFactory the {@link MessageHandlerMethodFactory} instance.
     */
    void setMessageHandlerMethodFactory(MessageHandlerMethodFactory messageHandlerMethodFactory);

}
