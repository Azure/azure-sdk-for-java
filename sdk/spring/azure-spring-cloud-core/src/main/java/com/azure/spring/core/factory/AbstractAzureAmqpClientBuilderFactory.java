// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.factory;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.util.ClientOptions;
import com.azure.spring.core.converter.AzureAmqpProxyOptionsConverter;
import com.azure.spring.core.converter.AzureAmqpRetryOptionsConverter;
import com.azure.spring.core.properties.client.AmqpClientProperties;
import com.azure.spring.core.properties.client.ClientProperties;
import com.azure.spring.core.properties.retry.RetryProperties;

import java.util.function.BiConsumer;

/**
 * Abstract factory of an AMQP client builder.
 *
 * @param <T> The type of the amqp client builder.
 */
public abstract class AbstractAzureAmqpClientBuilderFactory<T> extends AbstractAzureServiceClientBuilderFactory<T> {

    private ClientOptions clientOptions = new ClientOptions();
    private final AzureAmqpProxyOptionsConverter proxyOptionsConverter = new AzureAmqpProxyOptionsConverter();
    private final AzureAmqpRetryOptionsConverter retryOptionsConverter = new AzureAmqpRetryOptionsConverter();
    protected abstract BiConsumer<T, ProxyOptions> consumeProxyOptions();
    protected abstract BiConsumer<T, AmqpTransportType> consumeAmqpTransportType();
    protected abstract BiConsumer<T, AmqpRetryOptions> consumeAmqpRetryOptions();
    protected abstract BiConsumer<T, ClientOptions> consumeClientOptions();

    @Override
    protected void configureCore(T builder) {
        configureAzureEnvironment(builder);
        configureCredential(builder);
        configureAmqpClient(builder);
    }

    protected void configureAmqpClient(T builder) {
        configureClientProperties(builder);
        configureAmqpTransportProperties(builder);
        configureProxy(builder);
        configureRetry(builder);
    }

    protected void configureAmqpTransportProperties(T builder) {
        final ClientProperties clientProperties = getAzureProperties().getClient();
        if (clientProperties == null) {
            return;
        }

        final AmqpClientProperties properties;
        if (clientProperties instanceof AmqpClientProperties) {
            properties = (AmqpClientProperties) clientProperties;
            consumeAmqpTransportType().accept(builder, properties.getTransportType());
        }
    }

    protected void configureClientProperties(T builder) {
        configureApplicationId(builder);
        consumeClientOptions().accept(builder, this.clientOptions);
    }

    @Override
    protected void configureApplicationId(T builder) {
        this.clientOptions.setApplicationId(getApplicationId());
    }

    @Override
    protected void configureRetry(T builder) {
        final RetryProperties retry = getAzureProperties().getRetry();
        if (retry == null) {
            return;
        }
        AmqpRetryOptions retryOptions = retryOptionsConverter.convert(retry);
        consumeAmqpRetryOptions().accept(builder, retryOptions);
    }

    @Override
    protected void configureProxy(T builder) {
        if (getAzureProperties().getProxy() == null) {
            return;
        }
        final ProxyOptions proxyOptions = proxyOptionsConverter.convert(getAzureProperties().getProxy());
        consumeProxyOptions().accept(builder, proxyOptions);
    }

    protected ClientOptions getClientOptions() {
        return clientOptions;
    }

    public void setClientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }



}
