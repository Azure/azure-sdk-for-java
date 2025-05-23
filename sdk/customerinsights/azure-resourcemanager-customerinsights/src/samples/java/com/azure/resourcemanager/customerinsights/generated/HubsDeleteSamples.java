// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.customerinsights.generated;

/**
 * Samples for Hubs Delete.
 */
public final class HubsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/HubsDelete
     * .json
     */
    /**
     * Sample code: Hubs_Delete.
     * 
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void hubsDelete(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.hubs().delete("TestHubRG", "sdkTestHub", com.azure.core.util.Context.NONE);
    }
}
