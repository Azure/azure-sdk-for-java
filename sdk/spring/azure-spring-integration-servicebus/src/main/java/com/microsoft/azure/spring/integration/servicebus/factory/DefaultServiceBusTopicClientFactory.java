// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.servicebus.factory;

import com.microsoft.azure.management.servicebus.ServiceBusNamespace;
import com.microsoft.azure.management.servicebus.Topic;
import com.microsoft.azure.servicebus.IMessageSender;
import com.microsoft.azure.servicebus.ISubscriptionClient;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.SubscriptionClient;
import com.microsoft.azure.servicebus.TopicClient;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.spring.cloud.context.core.util.Memoizer;
import com.microsoft.azure.spring.cloud.context.core.util.Tuple;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusRuntimeException;
import org.springframework.util.StringUtils;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Default implementation of {@link ServiceBusTopicClientFactory}.
 * Client will be cached to improve performance
 *
 * @author Warren Zhu
 */
public class DefaultServiceBusTopicClientFactory extends AbstractServiceBusSenderFactory
    implements ServiceBusTopicClientFactory {
    private static final String SUBSCRIPTION_PATH = "%s/subscriptions/%s";
    private final BiFunction<String, String, ISubscriptionClient> subscriptionClientCreator =
        Memoizer.memoize(this::createSubscriptionClient);
    private final Function<String, ? extends IMessageSender> sendCreator = Memoizer.memoize(this::createTopicClient);

    public DefaultServiceBusTopicClientFactory(String connectionString) {
        super(connectionString);
    }

    private ISubscriptionClient createSubscriptionClient(String topicName, String subscription) {

        if (resourceManagerProvider != null && StringUtils.hasText(namespace)) {
            ServiceBusNamespace serviceBusNamespace =
                resourceManagerProvider.getServiceBusNamespaceManager().getOrCreate(namespace);
            Topic topic = resourceManagerProvider.getServiceBusTopicManager()
                .getOrCreate(Tuple.of(serviceBusNamespace, topicName));
            resourceManagerProvider.getServiceBusTopicSubscriptionManager().getOrCreate(Tuple.of(topic, subscription));
        }

        String subscriptionPath = String.format(SUBSCRIPTION_PATH, topicName, subscription);
        try {
            return new SubscriptionClient(new ConnectionStringBuilder(connectionString, subscriptionPath),
                ReceiveMode.PEEKLOCK);
        } catch (InterruptedException | ServiceBusException e) {
            throw new ServiceBusRuntimeException("Failed to create service bus subscription client", e);
        }
    }

    private IMessageSender createTopicClient(String topicName) {
        if (resourceManagerProvider != null && StringUtils.hasText(namespace)) {
            ServiceBusNamespace serviceBusNamespace =
                resourceManagerProvider.getServiceBusNamespaceManager().getOrCreate(namespace);
            resourceManagerProvider.getServiceBusTopicManager().getOrCreate(Tuple.of(serviceBusNamespace, topicName));
        }

        try {
            return new TopicClient(new ConnectionStringBuilder(connectionString, topicName));
        } catch (InterruptedException | ServiceBusException e) {
            throw new ServiceBusRuntimeException("Failed to create service bus topic client", e);
        }
    }

    @Override
    public ISubscriptionClient getOrCreateSubscriptionClient(String topic, String subscription) {
        return this.subscriptionClientCreator.apply(topic, subscription);
    }

    @Override
    public IMessageSender getOrCreateSender(String name) {
        return this.sendCreator.apply(name);
    }
}
