// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.factory;

import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.resourcemanager.servicebus.models.Topic;
import com.azure.spring.cloud.context.core.util.Memoizer;
import com.azure.spring.cloud.context.core.util.Tuple;
import com.azure.spring.integration.servicebus.ServiceBusRuntimeException;
import org.springframework.util.StringUtils;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Default implementation of {@link ServiceBusTopicClientFactory}. Client will
 * be cached to improve performance
 *
 * @author Warren Zhu
 */
//TODO: The logic of instantiating topic processor client needs to be put in this class
public class DefaultServiceBusTopicClientFactory extends AbstractServiceBusSenderFactory
    implements ServiceBusTopicClientFactory {

    private static final String SUBSCRIPTION_PATH = "%s/subscriptions/%s";

    public DefaultServiceBusTopicClientFactory(String connectionString) {
        super(connectionString);
    }
}
