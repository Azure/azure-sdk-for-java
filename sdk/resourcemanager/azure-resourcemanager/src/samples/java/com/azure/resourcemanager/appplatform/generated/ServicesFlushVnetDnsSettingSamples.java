// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.appplatform.generated;

/**
 * Samples for Services FlushVnetDnsSetting.
 */
public final class ServicesFlushVnetDnsSettingSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/stable/2023-12-01/examples/
     * Services_FlushVnetDnsSetting.json
     */
    /**
     * Sample code: Services_FlushVnetDnsSetting.
     * 
     * @param azure The entry point for accessing resource management APIs in Azure.
     */
    public static void servicesFlushVnetDnsSetting(com.azure.resourcemanager.AzureResourceManager azure) {
        azure.springServices()
            .manager()
            .serviceClient()
            .getServices()
            .flushVnetDnsSetting("myResourceGroup", "myservice", com.azure.core.util.Context.NONE);
    }
}
