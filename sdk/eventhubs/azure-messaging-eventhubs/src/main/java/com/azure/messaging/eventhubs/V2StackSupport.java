// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ReactorConnectionCache;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationProperty;
import com.azure.core.util.ConfigurationPropertyBuilder;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.metrics.Meter;
import com.azure.messaging.eventhubs.implementation.EventHubReactorAmqpConnection;

import java.util.concurrent.atomic.AtomicReference;

// Temporary type for Builders to work with the V2-Stack. Type will be removed once migration to new v2 stack is completed.
final class V2StackSupport {
    private static final String ASYNC_CONSUMER_KEY = "com.azure.messaging.eventhubs.asyncConsumer.v2";
    private static final ConfigurationProperty<Boolean> ASYNC_CONSUMER_PROPERTY = ConfigurationPropertyBuilder.ofBoolean(ASYNC_CONSUMER_KEY)
        .environmentVariableName(ASYNC_CONSUMER_KEY)
        .defaultValue(false)
        .shared(true)
        .build();
    private final AtomicReference<Boolean> asyncConsumerFlag = new AtomicReference<>();

    private static final String SYNC_CONSUMER_KEY = "com.azure.messaging.eventhubs.syncConsumer.v2";
    private static final ConfigurationProperty<Boolean> SYNC_CONSUMER_PROPERTY = ConfigurationPropertyBuilder.ofBoolean(SYNC_CONSUMER_KEY)
        .environmentVariableName(SYNC_CONSUMER_KEY)
        .defaultValue(false)
        .shared(true)
        .build();
    private final AtomicReference<Boolean> syncConsumerFlag = new AtomicReference<>();

    private static final String ASYNC_PROCESSOR_KEY = "com.azure.messaging.eventhubs.processor.v2";
    private static final ConfigurationProperty<Boolean> ASYNC_PROCESSOR_PROPERTY = ConfigurationPropertyBuilder.ofBoolean(ASYNC_PROCESSOR_KEY)
        .environmentVariableName(ASYNC_PROCESSOR_KEY)
        .defaultValue(false)
        .shared(true)
        .build();
    private final AtomicReference<Boolean> asyncProcessorFlag = new AtomicReference<>();

    private static final String ASYNC_PRODUCER_KEY = "com.azure.messaging.eventhubs.asyncProducer.v2";
    private static final ConfigurationProperty<Boolean> ASYNC_PRODUCER_PROPERTY = ConfigurationPropertyBuilder.ofBoolean(ASYNC_PRODUCER_KEY)
        .environmentVariableName(ASYNC_PRODUCER_KEY)
        .defaultValue(false)
        .shared(true)
        .build();
    private final AtomicReference<Boolean> asyncProducerFlag = new AtomicReference<>();

    private static final String SYNC_PRODUCER_KEY = "com.azure.messaging.eventhubs.syncProducer.v2";
    private static final ConfigurationProperty<Boolean> SYNC_PRODUCER_PROPERTY = ConfigurationPropertyBuilder.ofBoolean(SYNC_PRODUCER_KEY)
        .environmentVariableName(SYNC_PRODUCER_KEY)
        .defaultValue(false)
        .shared(true)
        .build();
    private final AtomicReference<Boolean> syncProducerFlag = new AtomicReference<>();

    private final ClientLogger logger;

    V2StackSupport(ClientLogger logger) {
        this.logger = logger;
    }
    /**
     * Check if Reactor Async Consumer should use the v2 stack.
     *
     * @param configuration the client configuration.
     * @return true if the Reactor Async consumer should use the v2 stack.
     */
    boolean isAsyncConsumerEnabled(Configuration configuration) {
        return isOptedIn(configuration, ASYNC_CONSUMER_PROPERTY, asyncConsumerFlag);
    }

    /**
     * Check if Sync Consumer should use the v2 stack.
     *
     * @param configuration the client configuration.
     * @return true if the Sync consumer should use the v2 stack.
     */
    boolean isSyncConsumerEnabled(Configuration configuration) {
        return isOptedIn(configuration, SYNC_CONSUMER_PROPERTY, syncConsumerFlag);
    }

    /**
     * Check if Processor Async Consumer should use the v2 stack.
     *
     * @param configuration the client configuration.
     * @return true if the Processor Async consumer should use the v2 stack.
     */
    boolean isAsyncProcessorEnabled(Configuration configuration) {
        return isOptedIn(configuration, ASYNC_PROCESSOR_PROPERTY, asyncProcessorFlag);
    }

    /**
     * Check if Reactor Async Producer should use the v2 stack.
     *
     * @param configuration the client configuration.
     * @return true if the Reactor Async Producer should use the v2 stack.
     */
    boolean isAsyncProducerEnabled(Configuration configuration) {
        return isOptedIn(configuration, ASYNC_PRODUCER_PROPERTY, asyncProducerFlag);
    }

    /**
     * Check if Sync Producer should use the v2 stack.
     *
     * @param configuration the client configuration.
     * @return true if the Sync Producer should use the v2 stack.
     */
    boolean isSyncProducerEnabled(Configuration configuration) {
        return isOptedIn(configuration, SYNC_PRODUCER_PROPERTY, syncProducerFlag);
    }

    // Obtain the shared connection-cache based on the V2-Stack.
    ReactorConnectionCache<EventHubReactorAmqpConnection> getOrCreateConnectionCache(ConnectionOptions connectionOptions,
        MessageSerializer serializer, Meter meter, ClientLogger logger) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not implemented"));
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

    private static ReactorConnectionCache<EventHubReactorAmqpConnection> createConnectionCache(ConnectionOptions connectionOptions,
        MessageSerializer serializer, Meter meter, ClientLogger logger) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not implemented"));
    }
}
