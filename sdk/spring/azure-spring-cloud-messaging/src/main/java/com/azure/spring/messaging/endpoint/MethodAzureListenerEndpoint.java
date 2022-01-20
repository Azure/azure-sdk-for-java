// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.endpoint;

import com.azure.spring.messaging.listener.AzureMessageHandler;
import com.azure.spring.messaging.listener.DefaultAzureMessageHandler;
import com.azure.spring.messaging.container.MessageListenerContainer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.lang.Nullable;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.util.Assert;
import org.springframework.util.StringValueResolver;

import java.lang.reflect.Method;

/**
 * A {@link AzureListenerEndpoint} providing the method to invoke to process
 * an incoming message for this endpoint.
 *
 * @author Warren Zhu
 */
public class MethodAzureListenerEndpoint extends AbstractAzureListenerEndpoint implements BeanFactoryAware {

    @Nullable
    private Object bean;

    @Nullable
    private Method method;

    @Nullable
    private MessageHandlerMethodFactory messageHandlerMethodFactory;

    @Nullable
    private StringValueResolver embeddedValueResolver;

    /**
     * Set the {@link BeanFactory} to use to resolve expressions (may be {@code null}).
     */
    @Override
    public void setBeanFactory(@Nullable BeanFactory beanFactory) {
        if (this.embeddedValueResolver == null && beanFactory instanceof ConfigurableBeanFactory) {
            this.embeddedValueResolver = new EmbeddedValueResolver((ConfigurableBeanFactory) beanFactory);
        }
    }

    @Override
    protected AzureMessageHandler createMessageHandler(MessageListenerContainer container) {
        Assert.state(this.messageHandlerMethodFactory != null,
                "Could not create message listener - MessageHandlerMethodFactory not set");
        DefaultAzureMessageHandler messageListener = new DefaultAzureMessageHandler();
        InvocableHandlerMethod invocableHandlerMethod =
                this.messageHandlerMethodFactory.createInvocableHandlerMethod(getBean(), getMethod());
        messageListener.setHandlerMethod(invocableHandlerMethod);

        return messageListener;
    }

    @Override
    protected StringBuilder getEndpointDescription() {
        return super.getEndpointDescription().append(" | bean='").append(this.bean).append("'").append(" | method='")
                    .append(this.method).append("'");
    }

    /**
     * Get the object instance that should manage this endpoint.
     * @return the target bean instance.
     */
    public Object getBean() {
        return bean;
    }

    /**
     * Set the object instance that should manage this endpoint.
     * @param bean the target bean instance.
     */
    public void setBean(Object bean) {
        this.bean = bean;
    }

    /**
     * Get the method to invoke to process a message managed by this endpoint.
     * @return the method to invoke to process a message managed by this endpoint.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Set the method to invoke to process a message managed by this endpoint.
     * @param method the target method for the {@link #bean}.
     */
    public void setMethod(Method method) {
        this.method = method;
    }

    /**
     * Set the {@link MessageHandlerMethodFactory} to use to build the
     * {@link InvocableHandlerMethod} responsible to manage the invocation
     * of this endpoint.
     * @param messageHandlerMethodFactory the {@link MessageHandlerMethodFactory} instance.
     */
    public void setMessageHandlerMethodFactory(MessageHandlerMethodFactory messageHandlerMethodFactory) {
        this.messageHandlerMethodFactory = messageHandlerMethodFactory;
    }
}
