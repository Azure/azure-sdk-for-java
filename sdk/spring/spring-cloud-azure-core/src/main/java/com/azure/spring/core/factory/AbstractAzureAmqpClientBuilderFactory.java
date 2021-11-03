// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.factory;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.util.ClientOptions;
import com.azure.spring.core.properties.client.AmqpClientProperties;
import com.azure.spring.core.properties.client.ClientProperties;
import com.azure.spring.core.properties.retry.RetryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

import static com.azure.spring.core.converter.AzureAmqpProxyOptionsConverter.AMQP_PROXY_CONVERTER;
import static com.azure.spring.core.converter.AzureAmqpRetryOptionsConverter.AMQP_RETRY_CONVERTER;

/**
 * Abstract factory of an AMQP client builder.
 *
 * @param <T> The type of the amqp client builder.
 */
public abstract class AbstractAzureAmqpClientBuilderFactory<T> extends AbstractAzureServiceClientBuilderFactory<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAzureAmqpClientBuilderFactory.class);
    private ClientOptions clientOptions = new ClientOptions();
    protected abstract BiConsumer<T, ProxyOptions> consumeProxyOptions();
    protected abstract BiConsumer<T, AmqpTransportType> consumeAmqpTransportType();
    protected abstract BiConsumer<T, AmqpRetryOptions> consumeAmqpRetryOptions();
    protected abstract BiConsumer<T, ClientOptions> consumeClientOptions();

    @Override
    protected void configureCore(T builder) {
        super.configureCore(builder);
        configureAmqpClient(builder);
    }

    protected void configureAmqpClient(T builder) {
        configureClientProperties(builder);
        configureAmqpTransportProperties(builder);
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
        AmqpRetryOptions retryOptions = AMQP_RETRY_CONVERTER.convert(retry);
        consumeAmqpRetryOptions().accept(builder, retryOptions);
    }

    @Override
    protected void configureProxy(T builder) {
        if (getAzureProperties().getProxy() == null) {
            return;
        }

        final ProxyOptions proxyOptions = AMQP_PROXY_CONVERTER.convert(getAzureProperties().getProxy());
        if (proxyOptions != null) {
            consumeProxyOptions().accept(builder, proxyOptions);
        } else {
            LOGGER.debug("No AMQP proxy properties available.");
        }
    }

    protected ClientOptions getClientOptions() {
        return clientOptions;
    }

    public void setClientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }



}
