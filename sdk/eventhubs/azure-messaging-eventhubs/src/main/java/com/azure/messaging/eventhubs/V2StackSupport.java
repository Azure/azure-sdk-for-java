// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.implementation.AmqpLinkProvider;
import com.azure.core.amqp.implementation.AzureTokenManagerProvider;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ReactorConnectionCache;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.amqp.implementation.StringUtil;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationProperty;
import com.azure.core.util.ConfigurationPropertyBuilder;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.metrics.Meter;
import com.azure.messaging.eventhubs.implementation.EventHubReactorAmqpConnection;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;

// Temporary type for Builders to work with the V2-Stack. Type will be removed once migration to new v2 stack is completed.
final class V2StackSupport {
    private static final String V2_STACK_KEY = "com.azure.messaging.eventhubs.v2";
    private static final ConfigurationProperty<Boolean> V2_STACK_PROPERTY = ConfigurationPropertyBuilder.ofBoolean(V2_STACK_KEY)
        .environmentVariableName(V2_STACK_KEY)
        .defaultValue(false)
        .shared(true)
        .build();
    private final AtomicReference<Boolean> v2StackFlag = new AtomicReference<>();

    private static final String SESSION_CHANNEL_CACHE_KEY = "com.azure.core.amqp.cache";
    private static final ConfigurationProperty<Boolean> SESSION_CHANNEL_CACHE_PROPERTY = ConfigurationPropertyBuilder.ofBoolean(SESSION_CHANNEL_CACHE_KEY)
        .environmentVariableName(SESSION_CHANNEL_CACHE_KEY)
        .defaultValue(true) // "SessionCache" and "RequestResponseChannelCache" are enabled by default if v2 stack is opted in.
        .shared(true)
        .build();
    private final AtomicReference<Boolean> sessionChannelCacheFlag = new AtomicReference<>();

    private final ClientLogger logger;

    V2StackSupport(ClientLogger logger) {
        this.logger = logger;
    }

    /**
     * Check if clients should use the v2 stack.
     *
     * @param configuration the client configuration.
     * @return true if the clients should use the v2 stack.
     */
    boolean isV2StackEnabled(Configuration configuration) {
        return isOptedIn(configuration, V2_STACK_PROPERTY, v2StackFlag);
    }

    /**
     * SessionCache and RequestResponseChannelCache are enabled by default if the v2 stack is opted in via
     * 'com.azure.messaging.eventhubs.v2', but application may opt out these two caches by setting
     * 'com.azure.core.amqp.cache' to false.
     *
     * @param configuration the client configuration.
     * @return true if SessionCache and RequestResponseChannelCache are enabled.
     */
    boolean isSessionChannelCacheEnabled(Configuration configuration) {
        if (!isV2StackEnabled(configuration)) {
            return false;
        }
        return !isOptedOut(configuration, SESSION_CHANNEL_CACHE_PROPERTY, sessionChannelCacheFlag);
    }

    private boolean isOptedOut(Configuration configuration, ConfigurationProperty<Boolean> configProperty,
        AtomicReference<Boolean> choiceFlag) {
        final Boolean flag = choiceFlag.get();
        if (flag != null) {
            return flag;
        }

        final boolean isOptedOut;
        final String propName = configProperty.getName();
        if (configuration != null) {
            // If application override the default 'true' with 'false' then app is opting-out the feature configProperty representing.
            isOptedOut = !configuration.get(configProperty);
        } else {
            assert !CoreUtils.isNullOrEmpty(propName);
            if (!CoreUtils.isNullOrEmpty(System.getenv(propName))) {
                isOptedOut = "false".equalsIgnoreCase(System.getenv(propName));
            } else if (!CoreUtils.isNullOrEmpty(System.getProperty(propName))) {
                isOptedOut = "false".equalsIgnoreCase(System.getProperty(propName));
            } else {
                isOptedOut = false;
            }
        }
        if (choiceFlag.compareAndSet(null, isOptedOut)) {
            logger.verbose("Selected configuration {}={}", propName, isOptedOut);
            if (isOptedOut) {
                final String logMessage = "If your application fails to work without explicitly setting {} configuration to 'false', please file an urgent issue at https://github.com/Azure/azure-sdk-for-java/issues/new/choose";
                logger.info(logMessage, propName);
            }
        }
        return choiceFlag.get();
    }

    private boolean isOptedIn(Configuration configuration, ConfigurationProperty<Boolean> configProperty,
        AtomicReference<Boolean> choiceFlag) {
        final Boolean flag = choiceFlag.get();
        if (flag != null) {
            return flag;
        }

        final String propName = configProperty.getName();
        final boolean isOptedIn;
        if (configuration != null) {
            isOptedIn = configuration.get(configProperty);
        } else {
            assert !CoreUtils.isNullOrEmpty(propName);
            if (!CoreUtils.isNullOrEmpty(System.getenv(propName))) {
                isOptedIn = "true".equalsIgnoreCase(System.getenv(propName));
            } else if (!CoreUtils.isNullOrEmpty(System.getProperty(propName))) {
                isOptedIn = "true".equalsIgnoreCase(System.getProperty(propName));
            } else {
                isOptedIn = false;
            }
        }
        if (choiceFlag.compareAndSet(null, isOptedIn)) {
            logger.verbose("Selected configuration {}={}", propName, isOptedIn);
        }
        return choiceFlag.get();
    }

    ReactorConnectionCache<EventHubReactorAmqpConnection> createConnectionCache(ConnectionOptions connectionOptions,
        Supplier<String> eventHubNameSupplier, MessageSerializer serializer, Meter meter, boolean useSessionChannelCache) {
        final Supplier<EventHubReactorAmqpConnection> connectionSupplier = () -> {
            final String connectionId = StringUtil.getRandomString("MF");
            final TokenManagerProvider tokenManagerProvider = new AzureTokenManagerProvider(
                connectionOptions.getAuthorizationType(), connectionOptions.getFullyQualifiedNamespace(),
                connectionOptions.getAuthorizationScope());
            final ReactorProvider provider = new ReactorProvider();
            final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(provider, meter);
            final AmqpLinkProvider linkProvider = new AmqpLinkProvider();
            return new EventHubReactorAmqpConnection(connectionId,
                connectionOptions, eventHubNameSupplier.get(), provider, handlerProvider, linkProvider, tokenManagerProvider,
                serializer, true, useSessionChannelCache);
        };
        final String fullyQualifiedNamespace = connectionOptions.getFullyQualifiedNamespace();
        final String entityPath = eventHubNameSupplier.get();
        final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(connectionOptions.getRetry());
        final Map<String, Object> loggingContext = Collections.singletonMap(ENTITY_PATH_KEY, entityPath);
        return new ReactorConnectionCache<>(connectionSupplier, fullyQualifiedNamespace, entityPath, retryPolicy, loggingContext);
    }
}
