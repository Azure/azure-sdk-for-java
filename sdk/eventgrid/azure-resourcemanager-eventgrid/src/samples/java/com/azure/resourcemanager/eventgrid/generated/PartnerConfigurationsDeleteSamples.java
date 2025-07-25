// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.eventgrid.generated;

/**
 * Samples for PartnerConfigurations Delete.
 */
public final class PartnerConfigurationsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/eventgrid/resource-manager/Microsoft.EventGrid/preview/2025-04-01-preview/examples/
     * PartnerConfigurations_Delete.json
     */
    /**
     * Sample code: PartnerConfigurations_Delete.
     * 
     * @param manager Entry point to EventGridManager.
     */
    public static void partnerConfigurationsDelete(com.azure.resourcemanager.eventgrid.EventGridManager manager) {
        manager.partnerConfigurations().delete("examplerg", com.azure.core.util.Context.NONE);
    }
}
