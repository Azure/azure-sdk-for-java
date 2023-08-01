// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.implementation.core.config;

import com.azure.spring.messaging.implementation.config.AbstractAzureListenerEndpoint;
import com.azure.spring.messaging.implementation.config.AzureListenerEndpoint;
import com.azure.spring.messaging.implementation.config.MethodAzureListenerEndpoint;
import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.messaging.listener.MessageListenerContainer;
import com.azure.spring.messaging.implementation.listener.adapter.MessagingMessageListenerAdapter;
import com.azure.spring.messaging.servicebus.implementation.core.listener.adapter.RecordMessagingMessageListenerAdapter;
import com.azure.spring.messaging.servicebus.support.converter.ServiceBusMessageConverter;
import org.springframework.beans.factory.BeanFactory;
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
 */
public class MethodServiceBusListenerEndpoint extends AbstractAzureListenerEndpoint
    implements MethodAzureListenerEndpoint {

    @Nullable
    private Object bean;

    @Nullable
    private Method method;

    @Nullable
    protected MessageHandlerMethodFactory messageHandlerMethodFactory;

    @Nullable
    private StringValueResolver embeddedValueResolver;


    @Override
    public void setBeanFactory(@Nullable BeanFactory beanFactory) {
        if (this.embeddedValueResolver == null && beanFactory instanceof ConfigurableBeanFactory) {
            this.embeddedValueResolver = new EmbeddedValueResolver((ConfigurableBeanFactory) beanFactory);
        }
    }

    @Override
    protected StringBuilder getEndpointDescription() {
        return super.getEndpointDescription()
                    .append(" | bean='").append(this.bean).append("'")
                    .append(" | method='").append(this.method).append("'");
    }

    @Override
    protected MessagingMessageListenerAdapter createMessageListener(MessageListenerContainer listenerContainer,
                                                                    AzureMessageConverter<?, ?> messageConverter) {

        Assert.state(this.messageHandlerMethodFactory != null,
            "Could not create message listener - MessageHandlerMethodFactory not set");
        MessagingMessageListenerAdapter messageListener = createMessageListenerInstance(messageConverter);

        Object bean = getBean();
        Method method = getMethod();
        Assert.state(bean != null && method != null, "No bean+method set on endpoint");

        InvocableHandlerMethod invocableHandlerMethod = this.messageHandlerMethodFactory.createInvocableHandlerMethod(
            bean, method);

        messageListener.setHandlerMethod(invocableHandlerMethod);

        return messageListener;
    }

    /**
     * Create an empty {@link MessagingMessageListenerAdapter} instance.
     *
     * @param messageConverter the converter (may be null).
     * @return the {@link MessagingMessageListenerAdapter} instance.
     */
    protected MessagingMessageListenerAdapter createMessageListenerInstance(
        @Nullable AzureMessageConverter<?, ?> messageConverter) {

        MessagingMessageListenerAdapter listener = new RecordMessagingMessageListenerAdapter();
        if (messageConverter instanceof ServiceBusMessageConverter) {
            listener.setMessageConverter(messageConverter);
        }

        return listener;
    }

    @Override
    @Nullable
    public Object getBean() {
        return bean;
    }

    @Override
    public void setBean(@Nullable Object bean) {
        this.bean = bean;
    }

    @Override
    @Nullable
    public Method getMethod() {
        return method;
    }

    @Override
    public void setMethod(@Nullable Method method) {
        this.method = method;
    }

    @Override
    public void setMessageHandlerMethodFactory(@Nullable MessageHandlerMethodFactory messageHandlerMethodFactory) {
        this.messageHandlerMethodFactory = messageHandlerMethodFactory;
    }
}
