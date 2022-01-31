// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.factory;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.client.traits.AmqpTrait;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.spring.core.aware.ClientAware;
import com.azure.spring.core.aware.ProxyAware;
import com.azure.spring.core.aware.RetryAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

import static com.azure.spring.core.implementation.converter.AzureAmqpProxyOptionsConverter.AMQP_PROXY_CONVERTER;
import static com.azure.spring.core.implementation.converter.AzureAmqpRetryOptionsConverter.AMQP_RETRY_CONVERTER;

/**
 * Abstract factory of an AMQP client builder.
 *
 * @param <T> The type of the amqp client builder.
 */
public abstract class AbstractAzureAmqpClientBuilderFactory<T> extends AbstractAzureServiceClientBuilderFactory<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAzureAmqpClientBuilderFactory.class);
    private ClientOptions clientOptions = new ClientOptions();

    /**
     * Return a {@link BiConsumer} of how the {@link T} builder consume a {@link ClientOptions}.
     * @return The consumer of how the {@link T} builder consume a {@link ClientOptions}.
     */
    protected abstract BiConsumer<T, ClientOptions> consumeClientOptions();

    @Override
    protected void configureCore(T builder, Configuration configuration) {
        super.configureCore(builder, configuration);
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
        final ClientAware.Client client = getAzureProperties().getClient();
        if (client == null) {
            return;
        }

        final ClientAware.AmqpClient amqpClient;
        if (client instanceof ClientAware.AmqpClient) {
            amqpClient = (ClientAware.AmqpClient) client;
            if (builder instanceof AmqpTrait) {
                ((AmqpTrait<?>) builder).transportType(amqpClient.getTransportType());
            } else {
                throw new IllegalArgumentException("non amqp builder");
            }
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
        final RetryAware.Retry retry = getAzureProperties().getRetry();
        if (retry == null) {
            return;
        }
        AmqpRetryOptions retryOptions = AMQP_RETRY_CONVERTER.convert(retry);
        if (builder instanceof AmqpTrait) {
            ((AmqpTrait<?>) builder).retryOptions(retryOptions);
        } else  {
            throw new IllegalArgumentException("non amqp builder");
        }
    }

    @Override
    protected void configureProxy(T builder) {
        ProxyAware.Proxy proxy = getAzureProperties().getProxy();
        if (proxy == null) {
            return;
        }

        final ProxyOptions proxyOptions = AMQP_PROXY_CONVERTER.convert(proxy);
        if (proxyOptions != null) {
            if (builder instanceof AmqpTrait) {
                ((AmqpTrait<?>) builder).proxyOptions(proxyOptions);
            } else {
                throw new IllegalArgumentException("non amqp buider");
            }
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
