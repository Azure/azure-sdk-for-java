// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.implementation.listener.adapter;

import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.messaging.implementation.listener.ListenerExecutionFailedException;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.util.Assert;

/**
 * An Azure Messaging Listener adapter that invokes a configurable
 * InvocableHandlerMethod. Wraps the incoming Azure Message objects to Spring's Message abstraction, copying the
 * Azure message headers using a configurable {@link com.azure.spring.messaging.converter.AzureMessageConverter}.
 *
 */
public abstract class MessagingMessageListenerAdapter {

    @Nullable
    private InvocableHandlerMethod handlerMethod;

    protected Class<?> payloadType = byte[].class;

    protected AzureMessageConverter<?, ?> messageConverter;

    /**
     * Invoke the handler method with {@link Message}.
     * @param message the message.
     * @throws ListenerExecutionFailedException when there's exception when invoking the handler.
     */
    protected void invokeHandler(Message<?> message) {
        InvocableHandlerMethod validHandlerMethod = getHandlerMethod();
        try {
            validHandlerMethod.invoke(message);
        } catch (IllegalArgumentException ex) {
            throw new ListenerExecutionFailedException(ex.getMessage(), ex);
        } catch (MessagingException ex) {
            throw new ListenerExecutionFailedException(
                createMessagingErrorMessage("Listener method could not be invoked with incoming message"), ex);
        } catch (Exception ex) {
            throw new ListenerExecutionFailedException(
                "Listener method '" + validHandlerMethod.getMethod().toGenericString() + "' threw exception", ex);
        }
    }

    protected final String createMessagingErrorMessage(String description) {
        InvocableHandlerMethod validHandlerMethod = getHandlerMethod();
        return description + "\n"
            + "Endpoint handler details:\n"
            + "Method [" + validHandlerMethod.getMethod().toGenericString() + "]\n"
            + "Bean [" + validHandlerMethod.getBean() + "]\n";
    }

    private Class<?> resolveMessagePayloadType(InvocableHandlerMethod method) {
        Class<?>[] parameterTypes = method.getMethod().getParameterTypes();

        Assert.notEmpty(parameterTypes, "Azure message handler method should not have empty parameters");

        //TODO: handle parameter of type Message<T>
        return parameterTypes[0];
    }

    /**
     * Return the {@link AzureMessageConverter} for this listener, being able to convert Message.
     *
     * @return the {@link AzureMessageConverter} for this listener, being able to convert Message.
     */
    public AzureMessageConverter<?, ?> getMessageConverter() {
        return messageConverter;
    }

    /**
     * Set the {@link AzureMessageConverter} for the listener.
     * @param messageConverter the message converter
     */
    public void setMessageConverter(AzureMessageConverter<?, ?> messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Get the handler method.
     *
     * @return the handler method.
     */
    public InvocableHandlerMethod getHandlerMethod() {
        Assert.state(this.handlerMethod != null, "No HandlerMethod set");
        return handlerMethod;
    }

    /**
     * Set the {@link InvocableHandlerMethod} to use to invoke the method processing an incoming {@link Message}.
     *
     * @param handlerMethod InvocableHandlerMethod
     */
    public void setHandlerMethod(InvocableHandlerMethod handlerMethod) {
        this.handlerMethod = handlerMethod;
        this.payloadType = resolveMessagePayloadType(handlerMethod);
    }

    /**
     * Set the message payload type.
     * @param payloadType the payload type.
     */
    public void setPayloadType(Class<?> payloadType) {
        this.payloadType = payloadType;
    }

    /**
     * Get the message payload type.
     * @return the message payload type.
     */
    public Class<?> getPayloadType() {
        return this.payloadType;
    }



}
