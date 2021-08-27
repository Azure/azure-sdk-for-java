// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.factory;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.util.ClientOptions;
import com.azure.spring.core.properties.ProxyProperties;
import com.azure.spring.core.properties.client.AmqpClientProperties;
import com.azure.spring.core.properties.client.ClientProperties;
import com.azure.spring.core.properties.retry.RetryProperties;
import org.springframework.lang.NonNull;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.function.BiConsumer;

/**
 * Abstract factory of an AMQP client builder.
 *
 * @param <T> The type of the amqp client builder.
 */
public abstract class AbstractAzureAmqpClientBuilderFactory<T> extends AbstractAzureServiceClientBuilderFactory<T> {

    private ClientOptions clientOptions = new ClientOptions();
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
        AmqpRetryOptions retryOptions = getAmqpRetryOptions(retry);
        consumeAmqpRetryOptions().accept(builder, retryOptions);
    }

    @Override
    protected void configureProxy(T builder) {
        if (getAzureProperties().getProxy() == null) {
            return;
        }
        final ProxyOptions proxyOptions = getProxyOptions(getAzureProperties().getProxy());
        consumeProxyOptions().accept(builder, proxyOptions);
    }

    private AmqpRetryOptions getAmqpRetryOptions(RetryProperties retry) {
        AmqpRetryMode mode;
        if (retry.getBackoff().getMultiplier() > 0) {
            mode = AmqpRetryMode.EXPONENTIAL;
        } else {
            mode = AmqpRetryMode.FIXED;
        }
        AmqpRetryOptions retryOptions = new AmqpRetryOptions();
        retryOptions.setDelay(Duration.ofMillis(retry.getBackoff().getDelay()));
        retryOptions.setMaxDelay(Duration.ofMillis(retry.getBackoff().getMaxDelay()));
        retryOptions.setMode(mode);
        retryOptions.setMaxRetries(retry.getMaxAttempts());
        retryOptions.setTryTimeout(Duration.ofMillis(retry.getTimeout()));
        return retryOptions;
    }

    protected ClientOptions getClientOptions() {
        return clientOptions;
    }

    public void setClientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }

    private ProxyOptions getProxyOptions(@NonNull ProxyProperties properties) {
        ProxyAuthenticationType authenticationType;
        switch (properties.getAuthenticationType()) {
            case "basic":
                authenticationType = ProxyAuthenticationType.BASIC;
                break;
            case "digest":
                authenticationType = ProxyAuthenticationType.DIGEST;
                break;
            default:
                authenticationType = ProxyAuthenticationType.NONE;
        }
        Proxy.Type type;
        switch (properties.getType()) {
            case "http":
                type = Proxy.Type.HTTP;
                break;
            case "socks":
                type = Proxy.Type.SOCKS;
                break;
            default:
                type = Proxy.Type.DIRECT;
        }
        Proxy proxyAddress = new Proxy(type, new InetSocketAddress(properties.getHostname(), properties.getPort()));
        return new ProxyOptions(authenticationType, proxyAddress, properties.getUsername(), properties.getPassword());
    }

}
