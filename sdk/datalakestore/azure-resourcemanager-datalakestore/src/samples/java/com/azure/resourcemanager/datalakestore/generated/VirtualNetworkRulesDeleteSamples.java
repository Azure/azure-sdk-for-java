// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datalakestore.generated;

/**
 * Samples for VirtualNetworkRules Delete.
 */
public final class VirtualNetworkRulesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/
     * VirtualNetworkRules_Delete.json
     */
    /**
     * Sample code: Deletes the specified virtual network rule from the specified Data Lake Store account.
     * 
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void deletesTheSpecifiedVirtualNetworkRuleFromTheSpecifiedDataLakeStoreAccount(
        com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
        manager.virtualNetworkRules()
            .deleteWithResponse("contosorg", "contosoadla", "test_virtual_network_rules_name",
                com.azure.core.util.Context.NONE);
    }
}
