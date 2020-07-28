/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.factory;

import com.microsoft.azure.servicebus.IQueueClient;

/**
 * Factory to return functional creator of service bus queue client
 *
 * @author Warren Zhu
 */
public interface ServiceBusQueueClientFactory extends ServiceBusSenderFactory {
    /**
     * Return a function which accepts service bus queue name, then returns {@link IQueueClient}
     */
    IQueueClient getOrCreateClient(String name);
}
