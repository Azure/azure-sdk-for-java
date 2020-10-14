// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.servicebus.factory;

import com.microsoft.azure.management.servicebus.ServiceBusNamespace;
import com.microsoft.azure.servicebus.IMessageSender;
import com.microsoft.azure.servicebus.IQueueClient;
import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.spring.cloud.context.core.util.Memoizer;
import com.microsoft.azure.spring.cloud.context.core.util.Tuple;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusRuntimeException;
import org.springframework.util.StringUtils;

import java.util.function.Function;

/**
 * Default implementation of {@link ServiceBusQueueClientFactory}.
 * Client will be cached to improve performance
 *
 * @author Warren Zhu
 */
public class DefaultServiceBusQueueClientFactory extends AbstractServiceBusSenderFactory
    implements ServiceBusQueueClientFactory {

    private final Function<String, IQueueClient> queueClientCreator = Memoizer.memoize(this::createQueueClient);

    public DefaultServiceBusQueueClientFactory(String connectionString) {
        super(connectionString);
    }

    private IQueueClient createQueueClient(String destination) {
        if (resourceManagerProvider != null && StringUtils.hasText(namespace)) {
            ServiceBusNamespace serviceBusNamespace =
                resourceManagerProvider.getServiceBusNamespaceManager().getOrCreate(namespace);
            resourceManagerProvider.getServiceBusQueueManager().getOrCreate(Tuple.of(serviceBusNamespace, destination));
        }

        try {
            return new QueueClient(new ConnectionStringBuilder(connectionString, destination), ReceiveMode.PEEKLOCK);
        } catch (InterruptedException | ServiceBusException e) {
            throw new ServiceBusRuntimeException("Failed to create service bus queue client", e);
        }
    }

    @Override
    public IQueueClient getOrCreateClient(String name) {
        return this.queueClientCreator.apply(name);
    }

    @Override
    public IMessageSender getOrCreateSender(String name) {
        return getOrCreateClient(name);
    }
}
