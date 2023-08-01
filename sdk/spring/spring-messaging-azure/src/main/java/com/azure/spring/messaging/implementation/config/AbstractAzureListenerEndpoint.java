// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.implementation.config;

import com.azure.spring.cloud.service.listener.MessageListener;
import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.messaging.listener.MessageListenerContainer;
import com.azure.spring.messaging.implementation.listener.adapter.MessagingMessageListenerAdapter;
import org.springframework.lang.Nullable;

/**
 * Base model for a Azure listener endpoint.
 *
 */
public abstract class AbstractAzureListenerEndpoint implements AzureListenerEndpoint {

    protected String id = "";

    @Nullable
    protected String destination;

    @Nullable
    protected String group;

    @Nullable
    protected String concurrency;

    @Override
    public void setupListenerContainer(MessageListenerContainer listenerContainer, AzureMessageConverter<?, ?> converter) {
        setupMessageListener(listenerContainer, converter);
    }

    /**
     * Create a {@link MessagingMessageListenerAdapter} that is able to serve this endpoint for the
     * specified container.
     *
     * @param listenerContainer the message listener container
     * @param messageConverter the message converter
     * @return AzureMessageHandler
     */
    protected abstract MessagingMessageListenerAdapter createMessageListener(MessageListenerContainer listenerContainer,
                                                                             @Nullable AzureMessageConverter<?, ?> messageConverter);

    /**
     * Return a description for this endpoint.
     * <p>Available to subclasses, for inclusion in their {@code toString()} result.
     * @return StringBuilder
     */
    protected StringBuilder getEndpointDescription() {
        StringBuilder result = new StringBuilder();
        return result.append(getClass().getSimpleName())
                     .append("[").append(this.id).append("] destination=").append(this.destination)
                     .append("' | group='").append(this.group).append("'");
    }
    @Override
    public String toString() {
        return getEndpointDescription().toString();
    }

    @Override
    public String getDestination() {
        return destination;
    }

    /**
     * Set the destination.
     * @param destination the destination.
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public String getGroup() {
        return group;
    }

    /**
     * Set the group for the corresponding listener container.
     * @param group the group for the corresponding listener container.
     */
    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Set the id of this endpoint.
     * @param id the id of this endpoint.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Set the concurrency expression.
     * @param concurrency the concurrency expression.
     */
    public void setConcurrency(String concurrency) {
        this.concurrency = concurrency;
    }

    /**
     * Get the concurrency expression.
     * @return the concurrency expression.
     */
    public String getConcurrency() {
        return concurrency;
    }

    private void setupMessageListener(MessageListenerContainer listenerContainer, AzureMessageConverter<?, ?> converter) {
        MessagingMessageListenerAdapter messageListenerAdapter = createMessageListener(listenerContainer, converter);
        listenerContainer.setupMessageListener((MessageListener<?>) messageListenerAdapter);
    }
}
