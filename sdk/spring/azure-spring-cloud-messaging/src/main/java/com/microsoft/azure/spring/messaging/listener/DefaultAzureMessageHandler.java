// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.messaging.listener;

import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.util.Assert;

/**
 * @author Warren Zhu
 * @see Message
 * @see InvocableHandlerMethod
 */
public class DefaultAzureMessageHandler implements AzureMessageHandler {

    @Nullable
    private InvocableHandlerMethod handlerMethod;

    private Class<?> messagePayloadType;

    private String createMessagingErrorMessage(String description) {
        InvocableHandlerMethod handlerMethod = getHandlerMethod();
        StringBuilder sb =
                new StringBuilder(description).append("\n").append("Endpoint handler details:\n").append("Method [")
                                              .append(handlerMethod.getMethod()).append("]\n").append("Bean [")
                                              .append(handlerMethod.getBean()).append("]\n");
        return sb.toString();
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        InvocableHandlerMethod handlerMethod = getHandlerMethod();
        try {
            handlerMethod.invoke(message);
        } catch (MessagingException ex) {
            throw new ListenerExecutionFailedException(
                    createMessagingErrorMessage("Listener method could not be invoked with incoming message"), ex);
        } catch (Exception ex) {
            throw new ListenerExecutionFailedException(
                    "Listener method '" + handlerMethod.getMethod().toGenericString() + "' threw exception", ex);
        }
    }

    private Class<?> resolveMessagePayloadType(InvocableHandlerMethod method) {
        Class<?>[] parameterTypes = method.getMethod().getParameterTypes();

        Assert.notEmpty(parameterTypes, "Azure message handler method should not have empty parameters");

        //TODO: handle parameter of type Message<T>
        return parameterTypes[0];
    }

    public InvocableHandlerMethod getHandlerMethod() {
        return handlerMethod;
    }

    /**
     * Set the {@link InvocableHandlerMethod} to use to invoke the method
     * processing an incoming {@link Message}.
     *
     * @param handlerMethod InvocableHandlerMethod
     */
    public void setHandlerMethod(InvocableHandlerMethod handlerMethod) {
        this.handlerMethod = handlerMethod;
        this.messagePayloadType = resolveMessagePayloadType(this.handlerMethod);
    }

    @Override
    public Class<?> getMessagePayloadType() {
        return messagePayloadType;
    }
}
