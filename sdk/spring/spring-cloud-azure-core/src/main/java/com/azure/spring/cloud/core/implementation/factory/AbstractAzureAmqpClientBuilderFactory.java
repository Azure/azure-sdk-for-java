// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.factory;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.util.ClientOptions;
import com.azure.spring.cloud.core.aware.ClientOptionsAware;
import com.azure.spring.cloud.core.aware.ProxyOptionsAware;
import com.azure.spring.cloud.core.aware.RetryOptionsAware;
import com.azure.spring.cloud.core.properties.AzureProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

import static com.azure.spring.cloud.core.implementation.converter.AzureAmqpProxyOptionsConverter.AMQP_PROXY_CONVERTER;
import static com.azure.spring.cloud.core.implementation.converter.AzureAmqpRetryOptionsConverter.AMQP_RETRY_CONVERTER;

/**
 * Abstract factory of an AMQP client builder.
 *
 * @param <T> The type of the amqp client builder.
 */
public abstract class AbstractAzureAmqpClientBuilderFactory<T> extends AbstractAzureServiceClientBuilderFactory<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAzureAmqpClientBuilderFactory.class);
    private ClientOptions clientOptions = new ClientOptions();

    /**
     * Return a {@link BiConsumer} of how the {@link T} builder consume a {@link ProxyOptions}.
     * @return The consumer of how the {@link T} builder consume a {@link ProxyOptions}.
     */
    protected abstract BiConsumer<T, ProxyOptions> consumeProxyOptions();

    /**
     * Return a {@link BiConsumer} of how the {@link T} builder consume a {@link AmqpTransportType}.
     * @return The consumer of how the {@link T} builder consume a {@link AmqpTransportType}.
     */
    protected abstract BiConsumer<T, AmqpTransportType> consumeAmqpTransportType();

    /**
     * Return a {@link BiConsumer} of how the {@link T} builder consume a {@link AmqpRetryOptions}.
     * @return The consumer of how the {@link T} builder consume a {@link AmqpRetryOptions}.
     */
    protected abstract BiConsumer<T, AmqpRetryOptions> consumeAmqpRetryOptions();

    /**
     * Return a {@link BiConsumer} of how the {@link T} builder consume a {@link ClientOptions}.
     * @return The consumer of how the {@link T} builder consume a {@link ClientOptions}.
     */
    protected abstract BiConsumer<T, ClientOptions> consumeClientOptions();

    @Override
    protected void configureCore(T builder) {
        super.configureCore(builder);
        configureAmqpClient(builder);
    }

    /**
     * Configure the AMQP related properties to the builder.
     *
     * @param builder The builder of the AMQP-based service client.
     */
    protected void configureAmqpClient(T builder) {
        configureClientProperties(builder);
        configureAmqpTransportProperties(builder);
    }

    /**
     * Configure the transport properties to the builder.
     * @param builder The builder of the AMQP-based service client.
     */
    protected void configureAmqpTransportProperties(T builder) {
        final ClientOptionsAware.Client client = getAzureProperties().getClient();
        if (client == null) {
            return;
        }

        final ClientOptionsAware.AmqpClient amqpClient;
        if (client instanceof ClientOptionsAware.AmqpClient) {
            amqpClient = (ClientOptionsAware.AmqpClient) client;
            consumeAmqpTransportType().accept(builder, amqpClient.getTransportType());
        }
    }

    /**
     * Configure the client properties to the builder.
     * @param builder The builder of the AMQP-based service client.
     */
    protected void configureClientProperties(T builder) {
        consumeClientOptions().accept(builder, this.clientOptions);
    }

    @Override
    protected BiConsumer<T, String> consumeApplicationId() {
        return (builder, id) -> this.clientOptions.setApplicationId(id);
    }

    @Override
    protected void configureRetry(T builder) {
        RetryOptionsAware.Retry retry = null;
        AzureProperties azureProperties = getAzureProperties();
        if (azureProperties instanceof RetryOptionsAware) {
            retry = ((RetryOptionsAware) azureProperties).getRetry();
        }

        if (retry == null) {
            return;
        }

        if (retry instanceof RetryOptionsAware.AmqpRetry) {
            AmqpRetryOptions retryOptions = AMQP_RETRY_CONVERTER.convert((RetryOptionsAware.AmqpRetry) retry);
            consumeAmqpRetryOptions().accept(builder, retryOptions);
        } else {
            LOGGER.debug("The provided retry options is not a RetryOptionsAware.AmqpRetry type.");
        }
    }

    @Override
    protected void configureProxy(T builder) {
        ProxyOptionsAware.Proxy proxy = getAzureProperties().getProxy();
        if (proxy == null) {
            return;
        }

        final ProxyOptions proxyOptions = AMQP_PROXY_CONVERTER.convert(proxy);
        if (proxyOptions != null) {
            consumeProxyOptions().accept(builder, proxyOptions);
        } else {
            LOGGER.debug("No AMQP proxy properties available.");
        }
    }

    /**
     * Get the {@link ClientOptions} used by the AMQP client.
     * @return The client options.
     */
    protected ClientOptions getClientOptions() {
        return clientOptions;
    }

    /**
     * Set the client options.
     * @param clientOptions The client options used by the AMQP client.
     */
    public void setClientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }

}
