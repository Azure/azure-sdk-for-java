// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.servicebus.generated;

import com.azure.resourcemanager.servicebus.fluent.models.SBQueueInner;

/**
 * Samples for Queues CreateOrUpdate.
 */
public final class QueuesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/servicebus/resource-manager/Microsoft.ServiceBus/stable/2021-11-01/examples/Queues/SBQueueCreate.
     * json
     */
    /**
     * Sample code: QueueCreate.
     * 
     * @param azure The entry point for accessing resource management APIs in Azure.
     */
    public static void queueCreate(com.azure.resourcemanager.AzureResourceManager azure) {
        azure.serviceBusNamespaces()
            .manager()
            .serviceClient()
            .getQueues()
            .createOrUpdateWithResponse("ArunMonocle", "sdk-Namespace-3174", "sdk-Queues-5647",
                new SBQueueInner().withEnablePartitioning(true), com.azure.core.util.Context.NONE);
    }
}
